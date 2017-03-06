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

	//Startup - initializes all variables and objects
	public static void startup()
	{
		System.out.print("\n\n///////------*****STARTUP*****------///////\n\n");

		sos.ontrace();
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

		//Put the arriving job at the end of the waitQ
		scheduler.waitQ.add(new jobTable(p[1], p[3], p[4]));
		
		//update worstfit location;
		memory.worstFit();

		//if the next job in the waitQ can fit into memory and the drum is not busy
		if (memory.canFit(scheduler.waitQ.peek().size) && !scheduler.drumBusy)
		{
		//call siodrum to transfer the job
			memory.worstFit();
			scheduler.waitQ.peek().location = memory.worstFitLocation;
			sos.siodrum(scheduler.waitQ.peek().num, scheduler.waitQ.peek().size, memory.worstFitLocation, 0);
			memory.add(scheduler.waitQ.peek().size);
			scheduler.drumBusy = true;
		}
		
		//if there is no job on the ready queue and the drum is busy, put CPU in idle mode.
		if (scheduler.readyQ.isEmpty() && scheduler.drumBusy)
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
			p[1] = runningJob.num;
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
		p[1] = runningJob.num;
		p[2] = runningJob.location;
		p[3] = runningJob.size;
		p[4] = runningJob.timeSlice;
		return;
	}

	//Drmint - called when job finishes moving from drum to mem, or vice versa
	public static void Drmint(int []a, int []p)
	{
	//update time variables
		prevTime = curTime;
		curTime = p[5];
		//add job into ready queue
		//memory.add(scheduler.waitQ.peek().size);
		scheduler.readyQ.add(scheduler.waitQ.peek());
		scheduler.waitQ.remove();		
		scheduler.drumBusy = false;
		
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
		runningJob = scheduler.readyQ.peek();
		System.out.print("\n\n///////------*****DRMINT*****------///////\n\n");
		
		

		//Resume job to run
		a[0] = 2;
		p[1] = runningJob.num;
		p[2] = runningJob.location;
		p[3] = runningJob.size;
		p[4] = runningJob.timeSlice;
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
		 *complete, then make the time quantum = remaining time for the job */
		if (runningJob.maxTime - runningJob.runTime < runningJob.TIMEQUANTUM && runningJob.maxTime - runningJob.runTime !=0)
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
			p[1] = runningJob.num;
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
		p[1] = runningJob.num;
		p[2] = runningJob.location;
		p[3] = runningJob.size;
		p[4] = runningJob.timeSlice;
                test(a,p);
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
				p[1] = runningJob.num;
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
				scheduler.roundRobin();
				runningJob = scheduler.readyQ.peek();
				a[0] = 2;
				p[1] = runningJob.num;
				p[2] = runningJob.location;
				p[3] = runningJob.size;
				//if the next job remaining time is less than the timequantum, make the timeslice the remainig time
				if (runningJob.maxTime - runningJob.runTime < runningJob.TIMEQUANTUM)
						runningJob.timeSlice = runningJob.maxTime - runningJob.runTime;
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
					p[1] = runningJob.num;
					p[2] = runningJob.location;
					p[3] = runningJob.size;
					//if the next job remaining time is less than the timequantum, make the timeslice the remainig time
					if (runningJob.maxTime - runningJob.runTime < runningJob.TIMEQUANTUM)
						runningJob.timeSlice = runningJob.maxTime - runningJob.runTime;
					p[4] = runningJob.timeSlice;
				}
		
				break;
		}
		test(a,p);
		return;
	}
        public static void test(int []at,int []pt)
        {if(runningJob!=null)
        System.out.println("RJ_S,T,B,L,N="+runningJob.size+" "+runningJob.timeSlice+" "+runningJob.blockBit+" "+runningJob.latchBit+" "+runningJob.num);
        System.out.println(scheduler.readyQ.size());
        System.out.println("a[0]="+at[0]+"  "+"p[1..5]="+pt[1]+" "+pt[2]+" "+pt[3]+" "+pt[4]+" "+pt[5]);
        for(jobTable j: scheduler.readyQ)
        {System.out.println("RQ_S,T,B,L,N="+j.size+" "+runningJob.timeSlice+" "+runningJob.blockBit+" "+runningJob.latchBit+" "+runningJob.num);}
        }
}