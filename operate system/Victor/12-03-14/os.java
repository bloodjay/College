import java.util.*;

//OS class
//holds all the functions called by SOS: startup, crint, drmint, dskint, tro, svc
public class os
{
	//"global" variables for OS class
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
		
		//update worstfit location;
		memory.worstFit();

		//if the job can fit into memory and the drum is not busy, call siodrum, which moves job from
		//drum to memory or the other way
		if (memory.canFit(arrivingJob.size) && !scheduler.drumBusy)
		{
			sos.siodrum(arrivingJob.num, arrivingJob.size, arrivingJob.location, 0);
			//************************************************************************************************************************************************
			//debugging purposes line here. want to know when is the drum marked busy and when is not
			System.out.println("Drum bussy? "scheduler.drumBusy + " **************************************************************************************************************************");
			//***********************************************************************************************************************************************
			scheduler.drumBusy = true;
		}
		//if it can't fit or the drum is busy, put it into the wait queue
		else
			scheduler.waitQ.add(arrivingJob);

		//if there is no job on the ready queue and the drum is busy, put CPU in idle mode.
		if (scheduler.readyQ.isEmpty() && scheduler.drumBusy)
		{
			a[0] = 1;
			return;
		}
		//but if the ready queue is empty but drum is not busy, check the waitQ
		else if (scheduler.readyQ.isEmpty() && !scheduler.drumBusy)
		//if waitQ is not empty, call siodrum to move the job into memory
			if (!scheduler.waitQ.isEmpty())
			{
				/******************************************************************************************************************************************
				need to fix this, move a job from waiting queue to memory when drum is not busy and memory has enough space.
				*/
				System.out.println("8888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888");
			}
			//if waitQ is also empty, put CPU in idle
			else
			{
				a[0] = 1;
				return;
			}
		//else, run job on queue
	
		runningJob.timeSlice -= curTime - prevTime;
		runningJob.runTime += curTime - prevTime;
		/*if job finished time quantum and used it's max CPU time, then
		 *it is completed, remove it from queue and memory*/
		if (runningJob.timeSlice == 0 && runningJob.runTime == runningJob.maxTime)
		{
			//remove job from memory
			memory.remove(runningJob.location, runningJob.size);
			//remove job from queue
			scheduler.readyQ.remove();
			//if the ready queue is empty, return control to sos with no jobs to run
			if (scheduler.readyQ.isEmpty())
				a[0] = 1;
			return;
				
		}
		//else, there should be a running job, run it
		else
			a[0] = 2;
			p[2] = runningJob.location;
			p[3] = runningJob.size;
			p[4] = runningJob.timeSlice;

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
		p[4] = runningJob.timeSlice;
		return;
	}

	//Drmint - called when job finishes moving from drum to mem, or vice versa
	public static void Drmint(int []a, int []p)
	{
		//************************************************************************************************************************************************
		//debugging purposes line here. want to know when is the drum marked busy and when is not
		System.out.println(" Drum bussy? "scheduler.drumBusy + " **************************************************************************************************************************");
		//***********************************************************************************************************************************************
		scheduler.drumBusy = false;
		prevTime = curTime;
		curTime = p[5];
		/*if there is a job running, check if it is completed then remove it,
		 *else just keep running it */
		if (runningJob !=null)
		{
			runningJob.timeSlice -= curTime - prevTime;
			runningJob.runTime += curTime - prevTime;
			/*if job finished time quantum and used it's max CPU time, then
			 *it is completed, remove it from queue and memory*/
			if (runningJob.timeSlice == 0 && runningJob.runTime !=0)
			{	
				//remove job from memory
				memory.remove(runningJob.location, runningJob.size);
				scheduler.readyQ.remove();
			}
		}
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
		p[4] = runningJob.timeSlice;
/************************************************************************************************************************************************************
this loop is set for debugging purposes 
it prints our version of memory to be compared to sos memory*/
		for (int i=0; i<25; i++)
		{
			if ((i+1)%4 == 0)
				System.out.println("\n");
			System.out.println(i +"" + memory.mem[i] + "\t\t" + (i + 25) +"" + memory.mem[i + 25] + "\t\t" + + (i + 50) +"" + memory.mem[i + 50 ] + "\t\t" + + (i + 75) +"" + memory.mem[i + 75]);
		}
		System.out.println("WORST FIT LOCTION " + memory.worstFitLocation + "\n\n");
//************************************************************************************************************************************************************
		return;
	}

	//TRO - called when a job completes its time quantum
	public static void Tro(int []a, int []p)
	{
		//update time variables
		prevTime = curTime;
		curTime = p[5];
		runningJob.timeSlice -= curTime - prevTime;
		runningJob.runTime += curTime - prevTime;
		/*If job remaining time is less than time quantum and the job still needs time to
		 *complete, then make the time quantum the remaining time for the job */
		if (runningJob.maxTime - runningJob.runTime <= 5 && runningJob.maxTime - runningJob.runTime !=0)
			runningJob.timeSlice = runningJob.maxTime - runningJob.runTime;
		/*else if job finished time quantum and used it's max CPU time, then
		 *it is completed, remove it from queue and memory*/
		else if (runningJob.timeSlice == 0 && runningJob.maxTime == runningJob.runTime)
		{
			//remove job from memory
			memory.remove(runningJob.location, runningJob.size);
			scheduler.readyQ.remove();
			//if there is no job in the ready queue, the CPU is idle
			if (scheduler.readyQ.isEmpty()) {
				a[0] = 1;
				runningJob = null;
				return;
			}
		
			//otherwise, the next job in the queue is now the running job
			runningJob = scheduler.readyQ.peek();
		
			//Resume the job to run
			a[0] = 2;
			p[2] = runningJob.location;
			p[3] = runningJob.size;
			p[4] = runningJob.timeSlice;
		
			return;
		}
		//else, the job still needs time for completion, therefore is given a new time quantum
		else
			runningJob.timeSlice = runningJob.TIMEQUANTUM;
		System.out.print("\n\n///////------*****TRO*****------///////\n\n");
		
		//push running job to back and start next job in queue
		scheduler.roundRobin();
		runningJob = scheduler.readyQ.peek();
		
		//Resume job to run
		a[0] = 2;
		p[2] = runningJob.location;
		p[3] = runningJob.size;
		p[4] = runningJob.timeSlice;
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
/************************************************************************************************************************************************************
this loop is set for debugging purposes 
it prints our version of memory to be compared to sos memory*/
		for (int i=0; i<25; i++)
		{
			if ((i+1)%4 == 0)
				System.out.println("\n");
			System.out.println(i +"" + memory.mem[i] + "\t\t" + (i + 25) +"" + memory.mem[i + 25] + "\t\t" + + (i + 50) +"" + memory.mem[i + 50 ] + "\t\t" + + (i + 75) +"" + memory.mem[i + 75]);
		}
		System.out.println("WORST FIT LOCATION " + memory.worstFitLocation + "\n\n");
//************************************************************************************************************************************************************
					break;
				}
		
				//otherwise, the next job in the queue is now the running job
				runningJob = scheduler.readyQ.peek();
		
				//Resume the job to run
				a[0] = 2;
				p[2] = runningJob.location;
				p[3] = runningJob.size;
				p[4] = runningJob.timeSlice;
		
				break;
		
			//a=6 - request I/O
			case 6:
		
				System.out.print("\n\n///////------*****SVC-I/O*****------///////\n\n");
		
				//call siodisk - which starts I/O for the given job
				sos.siodisk(runningJob.num);
				
				//Update its CPU running time
				runningJob.timeSlice -= curTime - prevTime;
				runningJob.runTime += curTime - prevTime;
				
				//set the latchBit to true, since the job is doing I/O
				scheduler.readyQ.peek().latchBit = true;
				
				//run next job
				runningJob = scheduler.readyQ.peek();
				a[0] = 2;
				p[2] = runningJob.location;
				p[3] = runningJob.size;
				p[4] = runningJob.timeSlice;
		
				break;
			
			//a=7 - block
			case 7:
		
				System.out.print("\n\n///////------*****SVC-BLOCK*****------///////\n\n");
				//Update its CPU running time
				runningJob.timeSlice -= curTime - prevTime;
				runningJob.runTime += curTime - prevTime;
				
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
					p[4] = runningJob.timeSlice;
				}
		
				break;
		}
		
		return;
	}
}