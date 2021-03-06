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
	static boolean drumDirection;
	static boolean blockedORwait;
	static boolean ioBusy; //indicates that there is a job currently doing I/O

	//Startup - initializes all variables and objects
	public static void startup()
	{
		sos.ontrace();
		runningJob = new jobTable();
		memory = new memManager();
		scheduler = new CPUScheduler();
		curTime = 0;
		prevTime = 0;
		drumBusy = false;
		drumDirection = false;
		blockedORwait = false;
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
		//if (scheduler.waitQ.size() < 3 || p[4] < 15000)
		scheduler.waitQ.add(new jobTable(p[1], p[3], p[4]));

		//swap job to drum - see function for more details
		swapFromDrum();
		
		//if there is no job on the ready queue OR
		//if the ready queue is empty AND the drum is busy, put CPU in idle mode.
		if (scheduler.readyQ.isEmpty() || (scheduler.readyQ.isEmpty() && drumBusy)) {
			runDisk();
			idleCPU(a);
			return;
		}

		//update timeslice of current job
		updateTimeSlice();

		//if the current job has completed its time, remove it
		if (runningJob.runTime == runningJob.maxTime) {
			removeRunningJob();

			//if the ready queue is empty, idle the CPU
			//otherwise, the next job runs
			if (scheduler.readyQ.isEmpty()) {
				runDisk();
				idleCPU(a);
				return;
			}
			else
				runningJob = scheduler.readyQ.peek();
		}

		runCPU(a, p);

		return;
	}

	//Diskint - called when a job finishes I/O
	public static void Dskint(int []a, int []p)
	{
		//update time variables
		updateTime(p[5]);
		ioBusy = false;

		if (!scheduler.readyQ.isEmpty())
			updateTimeSlice();

		//Since it finished I/O, it is no longer latched
		scheduler.ioQ.peek().latchBit = false;
		scheduler.ioQ.peek().inIoQ = false;

		if (scheduler.ioQ.peek().killBit)
			memory.remove(scheduler.ioQ.peek().location, scheduler.ioQ.peek().size);

		else if (scheduler.ioQ.peek().blockBit) {
			scheduler.ioQ.peek().blockBit = false;
			scheduler.readyQ.add(scheduler.ioQ.peek());
		}

		scheduler.ioQ.remove();
		
		//if there is more jobs on the ioQ, run them
		if (!scheduler.ioQ.isEmpty()) {
			sos.siodisk(scheduler.ioQ.peek().num);
			ioBusy = true;

			if (!scheduler.ioQ.peek().blockBit && !scheduler.ioQ.peek().killBit)
				scheduler.readyQ.get(scheduler.readyQ.indexOf(scheduler.ioQ.peek())).latchBit = true;
		}
		
		if (scheduler.readyQ.isEmpty()) {
			swapFromDrum();
			runDisk();
			idleCPU(a);
			return;
		}

		scheduler.srtn();
		runningJob = scheduler.readyQ.peek();

		if (runningJob.runTime == runningJob.maxTime) {
			if (!runningJob.inIoQ)
				memory.remove(runningJob.location, runningJob.size);

			scheduler.readyQ.remove();
			swapFromDrum();

			if (scheduler.readyQ.isEmpty())	{
				runDisk();
				idleCPU(a);
				return;
			}
			else
				runningJob = scheduler.readyQ.peek();
		}

		runCPU(a, p);
		return;
	}

	//Drmint - called when job finishes moving from drum to mem, or vice versa
	public static void Drmint(int []a, int []p)
	{
		//update time variables
		updateTime(p[5]);
		drumBusy = false;

		if (drumDirection == true) {
			if (scheduler.readyQ.isEmpty()){
				runDisk();
				idleCPU(a);
			}
			else {
				updateTimeSlice();
				if (scheduler.readyQ.peek().runTime == runningJob.maxTime) {
					memory.remove(runningJob.location, runningJob.size);
					scheduler.readyQ.remove();
				}
				runCPU(a, p);
			}
			return;
		}
		
		if (blockedORwait) {
			scheduler.ioQ.add(scheduler.blockedQ.peek());
			scheduler.blockedQ.remove();
		}
		else {
			scheduler.readyQ.add(scheduler.waitQ.peek());
			scheduler.waitQ.remove();
		}

		swapFromDrum();
		
		if (runningJob !=null) {
			updateTimeSlice();
			if (scheduler.readyQ.peek().runTime == runningJob.maxTime) {
				if (scheduler.readyQ.peek().inIoQ)
					scheduler.ioQ.get(scheduler.ioQ.indexOf(runningJob)).killBit = true;
				else
					memory.remove(runningJob.location, runningJob.size);
				scheduler.readyQ.remove();
			}
		}

		if (scheduler.readyQ.isEmpty()) {
			runDisk();
			idleCPU(a);
			return;
		}

		scheduler.srtn();
		runningJob = scheduler.readyQ.peek();

		runCPU(a, p);
		return;
	}

	//TRO - called when a job completes its time quantum
	public static void Tro(int []a, int []p)
	{
		//update time variables
		updateTime(p[5]);
		updateTimeSlice();

		/*else if job finished time quantum and used it's max CPU time, then
		 *it is completed, remove it from queue and memory*/
		if (scheduler.readyQ.peek().maxTime == scheduler.readyQ.peek().runTime)	{
			if (scheduler.readyQ.peek().inIoQ)
				scheduler.ioQ.get(scheduler.ioQ.indexOf(runningJob)).killBit = true;
			else
				memory.remove(runningJob.location, runningJob.size);

			scheduler.readyQ.remove();
			swapFromDrum();

			if (scheduler.readyQ.isEmpty()) {
				runDisk();
				idleCPU(a);
				return;
			}
		
			//otherwise, the next job in the queue is now the running job
			runningJob = scheduler.readyQ.peek();
		
			//Resume the job to run
			runCPU(a, p);
		
			return;
		}

		//Resume job to run
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
				//System.out.print("\n\n///////------*****SVC-TERM*****------///////\n\n");
		
				//remove job from memory
				if (runningJob.inIoQ)
					scheduler.ioQ.get(scheduler.ioQ.indexOf(runningJob)).killBit = true;
				else
					memory.remove(runningJob.location, runningJob.size);

				scheduler.readyQ.remove();
				swapFromDrum();

				if (scheduler.readyQ.isEmpty()) {
					runDisk();
					idleCPU(a);
					break;
				}
		
				//otherwise, the next job in the queue is now the running job
				runningJob = scheduler.readyQ.peek();
		
				//Resume the job to run
				runCPU(a, p);
		
				break;
		
			//a=6 - request I/O
			case 6:
				//Update its CPU running time
				updateTimeSlice();

				if (!runningJob.inIoQ)
					scheduler.ioQ.add(runningJob);

				scheduler.readyQ.peek().inIoQ = true;

				if (!ioBusy) {
					//call siodisk - which starts I/O for the given job
					sos.siodisk(scheduler.ioQ.peek().num);
					ioBusy = true;
					//set the latchBit to true, since the job is doing I/O
					scheduler.readyQ.peek().latchBit = true;
				}
				
				runCPU(a, p);
		
				break;
			
			//a=7 - block
			case 7:
				updateTimeSlice();

				if (!runningJob.latchBit && !runningJob.inIoQ) {
					runCPU(a, p);
					break;
				}

				//set blockBit to true
				runningJob.blockBit = true;
				scheduler.readyQ.remove();

				if (runningJob.latchBit)
					scheduler.ioQ.set(scheduler.ioQ.indexOf(runningJob), runningJob);

				else if (!drumBusy) {
					runningJob.inCore = false;
					sos.siodrum(runningJob.num, runningJob.size, runningJob.location, 1);
					drumDirection = true;
					drumBusy = true;
					memory.remove(runningJob.location, runningJob.size);
					scheduler.ioQ.remove(runningJob);
					scheduler.blockedQ.add(runningJob);
					if (!scheduler.readyQ.isEmpty()) {
						runningJob = scheduler.readyQ.peek();
						runCPU(a, p);
					}
					else {
						runDisk();
						idleCPU(a);
					}
					return;
				}

				swapFromDrum();

				if (scheduler.readyQ.isEmpty()) {
					runDisk();
					idleCPU(a);
					return;
				}

				runningJob = scheduler.readyQ.peek();

				//if the running job is blocked and latched, the CPU is idle
				if (runningJob.blockBit && runningJob.latchBit) {
					runDisk();
					idleCPU(a);
				}
				//else resume job to run
				else
					runCPU(a, p);
				break;
		}
		return;
	}

	public static void swapToDrum()
	{
		System.out.println("TODRUM------------------------");

		return;
	}

	public static void swapFromDrum()
	{
		System.out.println("SWAPFROMDRUM------------------------");

		if (drumBusy)
			return;

		scheduler.sjn();
		if (!scheduler.waitQ.isEmpty() && memory.canFit(scheduler.waitQ.peek().size)) {
			scheduler.waitQ.peek().location = memory.worstFitLocation;
			sos.siodrum(scheduler.waitQ.peek().num, scheduler.waitQ.peek().size, scheduler.waitQ.peek().location, 0);
			drumDirection = false;
			memory.add(scheduler.waitQ.peek().size);
			scheduler.waitQ.peek().inCore = true;
			drumBusy = true;
			blockedORwait = false;
			return;
		}		

		scheduler.sjnBlocked();
		if (!ioBusy && !scheduler.blockedQ.isEmpty() && memory.canFit(scheduler.blockedQ.peek().size)) {
			scheduler.blockedQ.peek().location = memory.worstFitLocation;
			sos.siodrum(scheduler.blockedQ.peek().num, scheduler.blockedQ.peek().size, scheduler.blockedQ.peek().location, 0);
			drumDirection = false;
			memory.add(scheduler.blockedQ.peek().size);
			scheduler.blockedQ.peek().inCore = true;
			drumBusy = true;
			blockedORwait = true;
			return;
		}
		return;
	}

	public static void idleCPU(int []a)
	{
		a[0] = 1;
		runningJob = null;
		return;
	}

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

	public static void runDisk()
	{
		if (!ioBusy && !scheduler.ioQ.isEmpty()) {
			sos.siodisk(scheduler.ioQ.peek().num);
			ioBusy = true;
			scheduler.ioQ.peek().latchBit = true;
		}
		return;
	}

	public static void updateTime(int t)
	{
		prevTime = curTime;
		curTime = t;
		return;
	}

	public static void updateTimeSlice()
	{
		scheduler.readyQ.peek().timeSlice -= curTime - prevTime;
		scheduler.readyQ.peek().runTime += curTime - prevTime;
		return;
	}

	public static void removeRunningJob()
	{
		memory.remove(runningJob.location, runningJob.size);
		scheduler.readyQ.remove();
		return;
	}
}