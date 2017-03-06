import java.util.*;
import java.io.*;

//OS class
//holds all the functions called by SOS: startup, crint, drmint, dskint, tro, svc
//and other helper functions
public class os
{
	//"global" variables for OS class
	static jobTable runningJob; //job that is being run
	static memManager memory; //objects for memory management
	static CPUScheduler scheduler; //and CPU Scheduling
	static int timeSlice; //default time slice or time quantum for jobs
	static int curTime; //current time
	static int prevTime; //previous time - used to measure time b/w interrupts
	static boolean drumBusy; //indicates that the drum is busy
	static boolean drumDirection; //indicates which way a job is being swapped
	static boolean blockedOrWait; //indicates which queue the job being swapped belongs to
	static boolean ioBusy; //indicates that there is a job currently doing I/O

	//Startup - initializes all variables and objects
	public static void startup()
	{
		runningJob = new jobTable();
		memory = new memManager();
		scheduler = new CPUScheduler();
		curTime = 0;
		prevTime = 0;
		drumBusy = false;
		drumDirection = false; //T - mem->drum, F - drum->mem
		blockedOrWait = false; //T - blocked, F - wait
		ioBusy = false;
		return;
	}

	//Crint - called when job arrives on drum
	//p: 1 = jobNum, 2 = priority, 3 = size, 4 = maxCPUTime, 5 = currentTime
	public static void Crint(int []a, int []p)
	{
		//update time variables
		updateTime(p[5]);
			
		//Put the arriving job at the end of the waitQ
		scheduler.waitQ.add(new jobTable(p[1], p[3], p[4]));

		//swap in a job from drum - see function for more details
		swapFromDrum();
		
		//if the ready queue is empty OR
		//if the ready queue is empty AND the drum is busy, put CPU in idle mode.
		if (scheduler.readyQ.isEmpty() || (scheduler.readyQ.isEmpty() && drumBusy)) {
			idleCPU(a);
			return;
		}

		//update timeslice of current job
		updateTimeSlice();

		//if the current job has completed its time, remove it
		checkRunTime();

		runCPU(a, p);

		return;
	}

	//Diskint - called when a job finishes I/O
	public static void Dskint(int []a, int []p)
	{
		//update time variables
		updateTime(p[5]);

		//IO is no longer running, set to false
		ioBusy = false;

		if (!scheduler.readyQ.isEmpty())
			updateTimeSlice();

		//Since it finished I/O, it is no longer latched nor is it on the IO queue
		scheduler.ioQ.peek().latchBit = false;
		scheduler.ioQ.peek().inIoQ = false;

		//if a job was meant to be terminated after it completed IO, remove it from memory
		if (scheduler.ioQ.peek().killBit)
			memory.remove(scheduler.ioQ.peek().location, scheduler.ioQ.peek().size);

		//if the job is blocked, put it back on the ready  queue
		else if (scheduler.ioQ.peek().blockBit) {
			scheduler.ioQ.peek().blockBit = false;
			scheduler.readyQ.add(scheduler.ioQ.peek());
		}

		scheduler.ioQ.remove();
		
		//if there are more jobs on the ioQ, run IO
		if (!scheduler.ioQ.isEmpty()) {
			sos.siodisk(scheduler.ioQ.peek().num);
			ioBusy = true;

			//if the job on the IO queue exists on the ready queue, update its entry in the ready queue
			if (scheduler.readyQ.indexOf(scheduler.ioQ.peek()) != -1)
				scheduler.readyQ.get(scheduler.readyQ.indexOf(scheduler.ioQ.peek())).latchBit = true;
		}
		
		//if the ready queue is empty, swap jobs into memory and idle the CPU
		if (scheduler.readyQ.isEmpty()) {
			swapFromDrum();
			idleCPU(a);
		}
		else
			runCPU(a, p);
		return;
	}

	//Drmint - called when job finishes moving from drum to mem, or vice versa
	public static void Drmint(int []a, int []p)
	{
		//update time variables
		updateTime(p[5]);
		drumBusy = false;

		//if a job was swapped into the drum
		if (drumDirection == true) {
			if (scheduler.readyQ.isEmpty())
				idleCPU(a);
			else {
				//update the time and check if the job completed its time
				updateTimeSlice();
				checkRunTime();
				runCPU(a, p);
			}
			return;
		}

		//if the job that called drmint was on the blocked queue,
		//add it to the IO queue and remove it from the blocked queue		
		if (blockedOrWait) {
			scheduler.ioQ.add(scheduler.blockedQ.peek());
			scheduler.blockedQ.remove();
		}
		//if the job that called drmint was on the wait queue,
		//add it to the ready queue and remove it from the wait queue
		else {
			scheduler.readyQ.add(scheduler.waitQ.peek());
			scheduler.waitQ.remove();
		}

		//if a job is running, check if it completed its time
		if (runningJob !=null) {
			updateTimeSlice();
			checkRunTime();
		}

		if (scheduler.readyQ.isEmpty())
			idleCPU(a);
		else
			runCPU(a, p);
		return;
	}

	//TRO - called when a job completes its time quantum
	public static void Tro(int []a, int []p)
	{
		//update time variables
		updateTime(p[5]);
		updateTimeSlice();

		//check if the running job completed its time
		checkRunTime();
		
		if (scheduler.readyQ.isEmpty())
			idleCPU(a);
		else
			runCPU(a, p);
		return;
	}

	//SVC - called in one of three instances
	//terminate, which means a job wants to finish
	//request I/O, which means a job wants to do I/O
	//block, which means a job does not want to run on the CPU until it finishes I/O
	public static void Svc(int []a, int []p)
	{
		//update time variables
		updateTime(p[5]);
		
		//the three mentioned instances are denoted by the value of a
		switch (a[0])
		{
			//a=5 - terminate
			case 5:
				//if the job in question is doing or needs to do IO, set its killBit to true
				if (runningJob.inIoQ)
					scheduler.ioQ.get(scheduler.ioQ.indexOf(runningJob)).killBit = true;
				//if not, remove the job from memory
				else
					memory.remove(runningJob.location, runningJob.size);

				scheduler.readyQ.remove();
				swapFromDrum();

				if (scheduler.readyQ.isEmpty())
					idleCPU(a);
				else
					runCPU(a, p);
				break;
		
			//a=6 - request I/O
			case 6:
				updateTimeSlice();

				//if the running job is not in the IO queue, put it in the IO queue
				if (!runningJob.inIoQ) {
					scheduler.ioQ.add(runningJob);
					runningJob.inIoQ = true;
				}

				//if the IO is not busy,
				if (!ioBusy) {
					//run IO for the next job on the IO queue
					sos.siodisk(scheduler.ioQ.peek().num);
					ioBusy = true;
					//set the latchBit to true, since the job is doing I/O
					runningJob.latchBit = true;
				}
				
				runCPU(a, p);
				break;
			
			//a=7 - block
			case 7:
				updateTimeSlice();

				//if the job is NOT on the IO queue, run the CPU and move on
				if (!runningJob.inIoQ) {
					runCPU(a, p);
					break;
				}

				//set blockBit to true, remove it from the ready queue
				runningJob.blockBit = true;
				scheduler.readyQ.remove();

				//if the job is latched, update its changes in the IO queue
				if (runningJob.latchBit)
					scheduler.ioQ.set(scheduler.ioQ.indexOf(runningJob), runningJob);

				//if it is not latched, push the job onto the drum
				else
					swapToDrum();

				if (scheduler.readyQ.isEmpty())
					idleCPU(a);
				else
					runCPU(a, p);
				break;
		}
		return;
	}

	//This function will swap jobs from memory to the drum
	//It is used for blocked jobs
	public static void swapToDrum()
	{
		if (!drumBusy && runningJob.maxTime > 1000) {
			sos.siodrum(runningJob.num, runningJob.size, runningJob.location, 1);
			drumDirection = true;
			drumBusy = true;
			memory.remove(runningJob.location, runningJob.size);
			scheduler.ioQ.remove(runningJob);
			scheduler.blockedQ.add(runningJob);
		}
		return;
	}

	//swaps jobs from drum to memory
	//This will give priority to new jobs to come in from the wait queue
	//If the smallest job from the wait queue can't fit or the wait queue is empty,
	//A blocked job is brought into memory (if it follows those same conditions)
	public static void swapFromDrum()
	{
		if (drumBusy)
			return;

		//sjnWaitQ will reorder the wait queue and put the smallest job at the top
		scheduler.sjnWaitQ();

		if (!scheduler.waitQ.isEmpty() && memory.canFit(scheduler.waitQ.peek().size)) {
			scheduler.waitQ.peek().location = memory.worstFitLocation;
			sos.siodrum(scheduler.waitQ.peek().num, scheduler.waitQ.peek().size, scheduler.waitQ.peek().location, 0);
			drumDirection = false;
			memory.add(scheduler.waitQ.peek().size);
			drumBusy = true;
			blockedOrWait = false;
			return;
		}		

		//sjnBlockedQ will reorder the blocked queue and put the smallest job at the top
		scheduler.sjnBlockedQ();

		if (!ioBusy && !scheduler.blockedQ.isEmpty() && memory.canFit(scheduler.blockedQ.peek().size)) {
			scheduler.blockedQ.peek().location = memory.worstFitLocation;
			sos.siodrum(scheduler.blockedQ.peek().num, scheduler.blockedQ.peek().size, scheduler.blockedQ.peek().location, 0);
			drumDirection = false;
			memory.add(scheduler.blockedQ.peek().size);
			drumBusy = true;
			blockedOrWait = true;
			return;
		}
		return;
	}

	//sets the a parameter to 1 to signify an idle cpu
	public static void idleCPU(int []a)
	{
		runDisk();
		a[0] = 1;
		runningJob = null;
		return;
	}

	//sets the a and p parameters to tell SOS to run the CPU with the given job
	public static void runCPU(int []a, int []p)
	{
		scheduler.srtn();
		runningJob = scheduler.readyQ.peek();
		a[0] = 2;
		p[2] = runningJob.location;
		p[3] = runningJob.size;
		p[4] = runningJob.timeSlice;
		return;
	}

	//does IO for the next job on the IO queue
	public static void runDisk()
	{
		if (!ioBusy && !scheduler.ioQ.isEmpty()) {
			sos.siodisk(scheduler.ioQ.peek().num);
			ioBusy = true;
			scheduler.ioQ.peek().latchBit = true;
		}
		return;
	}

	//updates time between interrupts
	public static void updateTime(int t)
	{
		prevTime = curTime;
		curTime = t;
		return;
	}

	//updates the timeslice and runtime of the current job
	public static void updateTimeSlice()
	{
		runningJob.timeSlice -= curTime - prevTime;
		runningJob.runTime += curTime - prevTime;
		return;
	}

	//checks to see if the running job has completed its allocated time.
	//If it has, it is removed from memory and the ready queue
	//if it still needs to do IO, it will stay in memory and be
	//terminated once its IO is complete
	public static void checkRunTime()
	{
		if (!scheduler.readyQ.isEmpty() && scheduler.readyQ.peek().maxTime == scheduler.readyQ.peek().runTime)	{
			if (scheduler.readyQ.peek().inIoQ)
				scheduler.ioQ.get(scheduler.ioQ.indexOf(runningJob)).killBit = true;
			else
				memory.remove(runningJob.location, runningJob.size);

			scheduler.readyQ.remove();
			swapFromDrum();
		}
		return;
	}
}