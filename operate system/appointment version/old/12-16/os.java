import java.util.*;

//OS class
//holds all the functions called by SOS: startup, crint, drmint, dskint, tro, svc
public class os
{
	//"global" variables for OS class
	static jobTable runningJob; //job that is being run

	static memManager memory; //objects for memory management
	static CPUScheduler scheduler; //and CPU Scheduling
	//*NOTE: the ready queue and wait queue are part of scheduler

	static int timeSlice; //default time slice or time quantum for jobs
	static int curTime; //current time
	static int prevTime; //previous time - used to measure run time of a job

	static boolean drumBusy; //indicates that the drum is busy
	static boolean ioBusy; //indicates that there is a job currently doing I/O

	//Startup - initializes all variables and objects
	public static void startup()
	{
		//sos.ontrace();

		runningJob = new jobTable();
		memory = new memManager();
		scheduler = new CPUScheduler();
		curTime = 0;
		prevTime = 0;
		drumBusy = false;
		ioBusy = false;
	}

	//Crint - called when job arrives on drum
	//p: 1 = jobNum, 2 = priority, 3 = size, 4 = maxCPUTime, 5 = currentTime
	public static void Crint(int []a, int []p)
	{	
		//update time variables
		prevTime = curTime;
		curTime = p[5];
			
		//Put the arriving job at the end of the waitQ
		if (scheduler.waitQ.size() < 3 || p[4] < 15000)
			scheduler.waitQ.add(new jobTable(p[1], p[3], p[4]));

		swap();
		
		//if there is no job on the ready queue and the drum is busy, put CPU in idle mode.
		if (scheduler.readyQ.isEmpty() || (scheduler.readyQ.isEmpty() && drumBusy)) {
			a[0] = 1;
			runningJob = null;
			return;
		}

		//else, run job on queue
		if (!scheduler.readyQ.isEmpty()) {
			runningJob = scheduler.readyQ.peek();
			Tupdate();
		}

		/*if job finished time quantum and used it's max CPU time, then
		 *it is completed, remove it from queue and memory*/
		if (runningJob.runTime == runningJob.maxTime) {
			//remove job from memory
			memory.remove(runningJob.location, runningJob.size);
			//remove job from queue
			scheduler.readyQ.remove();
			//update worstfit location;
			memory.worstFit();

			swap();

			//if the ready queue is empty, return control to sos with no jobs to run
			if (scheduler.readyQ.isEmpty()) {
				a[0] = 1;
				runningJob = null;
				return;
			}
			
			runningJob = scheduler.readyQ.peek();
		}

		//else, there should be a running job, run it
		else {
			running(a,p);
		}
	}

	//Diskint - called when a job finishes I/O
	public static void Dskint(int []a, int []p)
	{
		//update time variables
		prevTime = curTime;
		curTime = p[5];
		ioBusy = false;

		if (!scheduler.readyQ.isEmpty()) {
			Tupdate();
		}

		//Since it finished I/O, it is no longer latched and is unblocked
		scheduler.ioQ.peek().latchBit = false;
		scheduler.ioQ.peek().inIoQ = false;

		if (scheduler.ioQ.peek().blockBit && !scheduler.ioQ.peek().killBit) {
			scheduler.ioQ.peek().blockBit = false;
			scheduler.readyQ.add(scheduler.ioQ.peek());
		}

		if (scheduler.ioQ.peek().killBit) {
			memory.remove(scheduler.ioQ.peek().location, scheduler.ioQ.peek().size);
			//if drum is not busy, check if the next job on the waitQ want to move out of memory
			swap();
		}

		scheduler.ioQ.remove();
		
		//if there is more jobs on the ioQ, run them. else set the ioQ busy to false.
		if (!scheduler.ioQ.isEmpty()) {
			if (scheduler.ioQ.peek().inCore == 0)
				scheduler.ioQ.remove();
			
			sos.siodisk(scheduler.ioQ.peek().num);

			if (!scheduler.ioQ.peek().blockBit && !scheduler.ioQ.peek().killBit)
				scheduler.readyQ.get(scheduler.readyQ.indexOf(scheduler.ioQ.peek())).latchBit = true;
			ioBusy = true;
		}
		
		if (scheduler.readyQ.isEmpty()) {
			a[0] = 1;
			runningJob = null;
			return;
		}

		scheduler.srtn();
		runningJob = scheduler.readyQ.peek();

		/*if running job finished time quantum and used it's max CPU time, then
		 *it is completed, remove it from queue and memory*/
		if (runningJob.runTime == runningJob.maxTime) {
			if (!runningJob.inIoQ)
				//remove job from memory
				memory.remove(runningJob.location, runningJob.size);

			//remove job from queue
			scheduler.readyQ.remove();
			//if drum is not busy, check if the next job on the waitQ want to move out of memory
			swap();
			//if the ready queue is empty, return control to sos with no jobs to run
			if (scheduler.readyQ.isEmpty())	{
				a[0] = 1;
				runningJob = null;
				return;
			}
		}

		//Resume job to run
		runningJob = scheduler.readyQ.peek();
                running(a,p);
	}

	//Drmint - called when job finishes moving from drum to mem, or vice versa
	public static void Drmint(int []a, int []p)
	{
		//update time variables
		prevTime = curTime;
		curTime = p[5];
		drumBusy = false;
	
		//if the job incorebit = 0, the job was moved in, add it to the readyQ
		if (scheduler.waitQ.peek().inCore == 0) {
			scheduler.waitQ.peek().inCore = 1;
			scheduler.readyQ.add(scheduler.waitQ.peek());
			scheduler.waitQ.remove();
		}
		//else, if the job incorebit = 1, the job was moved out of memory, remove it from memory and put it at the back of the waitQ
		else {
			scheduler.waitQ.peek().inCore = 0;
			memory.remove(scheduler.waitQ.peek().location, scheduler.waitQ.peek().size);
			scheduler.waitQ.add(scheduler.waitQ.peek());
			scheduler.waitQ.remove();
		}

		swap();
		
		/*if there is a job running, check if it is completed then remove it,
		 *else just keep running it */
		if (runningJob !=null) {
			Tupdate();
			/*if job finished time quantum and used it's max CPU time, then
			 *it is completed, remove it from queue and memory*/
			if (scheduler.readyQ.peek().runTime == runningJob.maxTime) {	
				if (scheduler.readyQ.peek().inIoQ)
					scheduler.ioQ.get(scheduler.ioQ.indexOf(runningJob)).killBit = true;
				else
					memory.remove(runningJob.location, runningJob.size);

				scheduler.readyQ.remove();
				swap();
			}
		}

		scheduler.srtn();
		runningJob = scheduler.readyQ.peek();
                running(a,p);
	}

	//TRO - called when a job completes its time quantum
	public static void Tro(int []a, int []p)
	{
		//update time variables
		prevTime = curTime;
		curTime = p[5];
		Tupdate();

		/*else if job finished time quantum and used it's max CPU time, then
		 *it is completed, remove it from queue and memory*/
		if (scheduler.readyQ.peek().maxTime == scheduler.readyQ.peek().runTime)	{
			if (scheduler.readyQ.peek().inIoQ)
				scheduler.ioQ.get(scheduler.ioQ.indexOf(runningJob)).killBit = true;
			else
				memory.remove(runningJob.location, runningJob.size);

			scheduler.readyQ.remove();
			swap();

			if (scheduler.readyQ.isEmpty()) {
				a[0] = 1;
				runningJob = null;
				return;
			}
		
			//otherwise, the next job in the queue is now the running job
			runningJob = scheduler.readyQ.peek();
		
			//Resume the job to run
                        running(a,p);
		
			return;
		}

		//push running job to back and start next job in queue
		runningJob = scheduler.readyQ.peek();
		
		//Resume job to run
                running(a,p);
	}

	//SVC - called in one of three instances
	//terminate, which means a job wants to finish
	//request I/O, which means a job wants to do I/O
	//block, which means a job does not want to run on the CPU until it finishes I/O
	public static void Svc(int []a, int []p)
	{
		//update time variables
		prevTime = curTime;
		curTime = p[5];
		
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
				scheduler.sjnWaitQ();
				swap();

				if (scheduler.readyQ.isEmpty()) {
					a[0] = 1;
					runningJob = null;
					break;
				}
		
				//otherwise, the next job in the queue is now the running job
				runningJob = scheduler.readyQ.peek();
		
				//Resume the job to run
				running(a,p);		
				break;
		
			//a=6 - request I/O
			case 6:
				//Update its CPU running time
				Tupdate();
				if (!runningJob.inIoQ)
					scheduler.ioQ.add(runningJob);

				scheduler.readyQ.peek().inIoQ = true;

				if (!ioBusy && !scheduler.ioQ.isEmpty()) {
					//call siodisk - which starts I/O for the given job
					sos.siodisk(scheduler.ioQ.peek().num);
					ioBusy = true;
					//set the latchBit to true, since the job is doing I/O
					scheduler.readyQ.peek().latchBit = true;
				}
				
				//run next job
				runningJob = scheduler.readyQ.peek();
				running(a,p);
				break;
			
			//a=7 - block
			case 7:
				Tupdate();

				if (!scheduler.readyQ.peek().latchBit && !scheduler.readyQ.peek().inIoQ) {
					running(a,p);
					break;
				}

				//set blockBit to true
				scheduler.readyQ.peek().blockBit = true;
				scheduler.ioQ.set(scheduler.ioQ.indexOf(scheduler.readyQ.peek()), scheduler.readyQ.peek());

				scheduler.sjnWaitQ();
				scheduler.readyQ.remove();
				//if drum is not busy, check if the next job on the waitQ want to move out of memory
				swap();
				//if readyQ is not empty, make the running job the next job
				if (!scheduler.readyQ.isEmpty())
					runningJob = scheduler.readyQ.peek();
				else {
					a[0] = 1;
					runningJob = null;
					break;
				}

				//if the running job is blocked and latched, the CPU is idle
				if (runningJob.blockBit && runningJob.latchBit) {
					a[0] = 1;
					runningJob = null;
				}
				//else resume job to run
				else{
					running(a,p);
				}
				break;
		}
	}

	public static void swap()
	{
		if (!drumBusy && !scheduler.waitQ.isEmpty() && memory.canFit(scheduler.waitQ.peek().size)) {
			//call siodrum to transfer the job
			scheduler.waitQ.peek().location = memory.worstFitLocation;
			sos.siodrum(scheduler.waitQ.peek().num, scheduler.waitQ.peek().size, memory.worstFitLocation, 0);
			memory.add(scheduler.waitQ.peek().size);
			drumBusy = true;
		}
	}
        public static void running(int []a, int[]p)
	{
	    			a[0] = 2;
					p[2] = runningJob.location;
					p[3] = runningJob.size;
					p[4] = runningJob.timeSlice;
	}
        public static void Tupdate()
        {scheduler.readyQ.peek().timeSlice -= curTime - prevTime;
	scheduler.readyQ.peek().runTime += curTime - prevTime;}
}