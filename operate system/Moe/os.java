import java.util.*;

//OS class
//holds all the functions called by SOS: startup, crint, drmint, dskint, tro, svc
public class os
{
	//"gloabal" variables for OS class
	static jobTable arrivingJob; //job that most recently came into system
	static jobTable runningJob; //job that is being run

	static memManager memory; //objects for memory management
	static CPUScheduler scheduler; //and CPU Scheduling
	//*NOTE: the ready queue and wait queue are part of scheduler

	static int timeSlice; //default time slice or time quantum for jobs
	static int curTime; //current time
	static int prevTime; //previous time - used to measure run time of a job

	//Startup - initializes all variables and objects
	public static void startup()
	{
		System.out.print("\n\n///////------*****STARTUP*****------///////\n\n");

		sos.ontrace();

		arrivingJob = new jobTable();
		runningJob = new jobTable();

		memory = new memManager();

		scheduler = new CPUScheduler();

		timeSlice = 5;
		curTime = 0;
		prevTime = 0;

		return;
	}

	//Crint - called when job arrives on drum
	//p: 1 = jobNum, 2 = priority, 3 = size, 4 = maxCPUTime, 5 = currentTime
	public static void Crint(int []a, int []p)
	{
		//update time variables
		prevTime = curTime;
		curTime = p[5];
		System.out.print("\n\n///////------*****CRINT*****------///////\n\n");

		//create temp jobTable object of job that just came onto the drum
		arrivingJob = new jobTable(p[1], p[3], p[4], memory.worstFitLocation);

		//if the job can fit into memory, call siodrum, which moves from
		//drum to memory or the other way
		if (memory.canFit(arrivingJob.size))
			sos.siodrum(arrivingJob.num, arrivingJob.size, arrivingJob.location, 0);
		//if it can't fit, put it into the wait queue
		else
			scheduler.waitQ.add(arrivingJob);

		//if there is no job on the ready queue, idle CPU
		if (scheduler.readyQ.isEmpty())
			a[0] = 1;
		//else, run job on queue
		else {
			runningJob.runTime += prevTime + curTime;
			a[0] = 2;
			p[2] = runningJob.location;
			p[3] = runningJob.size;
			p[4] = timeSlice;
		}

		return;
	}

	//Diskint - called when a job finishes I/O
	public static void Dskint(int []a, int []p)
	{
		//update time variables
		prevTime = curTime;
		curTime = p[5];
		System.out.print("\n\n///////------*****DSKINT*****------///////\n\n");

		//Since it finished I/O, it is no longer latched and is unblocked
		scheduler.readyQ.peek().latchBit = false;
		scheduler.readyQ.peek().blockBit = false;
		runningJob = scheduler.readyQ.peek();
		
		//Resume job to run
		a[0] = 2;
		p[2] = runningJob.location;
		p[3] = runningJob.size;
		p[4] = timeSlice;
		return;
	}

	//Drmint - called when job finishes moving from drum to mem, or vice versa
	public static void Drmint(int []a, int []p)
	{
		prevTime = curTime;
		curTime = p[5];
		System.out.print("\n\n///////------*****DRMINT*****------///////\n\n");
		
		//add job into memory
		memory.add(arrivingJob.size);

		//add job into ready queue
		scheduler.readyQ.add(arrivingJob);
		runningJob = scheduler.readyQ.peek();

		//Resume job to run
		a[0] = 2;
		p[2] = runningJob.location;
		p[3] = runningJob.size;
		p[4] = timeSlice;

		return;
	}

	//TRO - called when a job completes its time quantum
	public static void Tro(int []a, int []p)
	{
		//update time variables
		prevTime = curTime;
		curTime = p[5];
		System.out.print("\n\n///////------*****TRO*****------///////\n\n");
		
		//push running job to back and start next job in queue
		scheduler.roundRobin();
		runningJob = scheduler.readyQ.peek();
		
		//Resume job to run
		a[0] = 2;
		p[2] = runningJob.location;
		p[3] = runningJob.size;
		p[4] = timeSlice;
		return;
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
				System.out.print("\n\n///////------*****SVC-TERM*****------///////\n\n");
		
				//remove job from memory
				memory.remove(runningJob.location, runningJob.size);
				scheduler.readyQ.remove();
		
				//if there is no job in the ready queue, the CPU is idle
				if (scheduler.readyQ.isEmpty()) {
					a[0] = 1;
					runningJob = null;
					break;
				}
		
				//otherwise, the next job in the queue is now the running job
				runningJob = scheduler.readyQ.peek();
		
				//Resume the job to run
				a[0] = 2;
				p[2] = runningJob.location;
				p[3] = runningJob.size;
				p[4] = timeSlice;
		
				break;
		
			//a=6 - request I/O
			case 6:
		
				System.out.print("\n\n///////------*****SVC-I/O*****------///////\n\n");
		
				//call siodisk - which starts I/O for the given job
				sos.siodisk(runningJob.num);
		
				//set the latchBit to true, since the job is doing I/O
				scheduler.readyQ.peek().latchBit = true;
				runningJob = scheduler.readyQ.peek();
				
				//Resume job to run
				a[0] = 2;
				p[2] = runningJob.location;
				p[3] = runningJob.size;
				p[4] = timeSlice;
		
				break;
			
			//a=7 - block
			case 7:
		
				System.out.print("\n\n///////------*****SVC-BLOCK*****------///////\n\n");
		
				//set blockBit to true
				scheduler.readyQ.peek().blockBit = true;

				//blocked jobs can't run on CPU, so reschedule
				scheduler.reschedule();
				runningJob = scheduler.readyQ.peek();

				//if the running job is blocked and latched, the CPU is idle
				if (runningJob.blockBit && runningJob.latchBit)
					a[0] = 1;

				//else resume job to run
				else{
					a[0] = 2;
					p[2] = runningJob.location;
					p[3] = runningJob.size;
					p[4] = timeSlice;
				}
		
				break;
		}
		
		return;
	}
}