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
		//System.out.print("\n\n///////------*****STARTUP*****------///////\n\n");

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
			
		//System.out.print("\n\n///////------*****CRINT*****------///////\n\n");

		//Put the arriving job at the end of the waitQ
		if (scheduler.waitQ.size() < 3 || p[4] < 15000)
			scheduler.waitQ.add(new jobTable(p[1], p[3], p[4]));
		//arange the waitQ
		scheduler.sjnWaitQ();
		
		//update worstfit location;
		memory.worstFit();
		scheduler.sjnWaitQ();
		//if drum is not busy, check if the next job on the waitQ want to move out of memory
		if (!scheduler.drumBusy && !scheduler.waitQ.isEmpty() && scheduler.waitQ.peek().inCore == 1 && !scheduler.waitQ.peek().latchBit)
		{
			sos.siodrum(scheduler.waitQ.peek().num, scheduler.waitQ.peek().size, scheduler.waitQ.peek().location, 1);
			//since is being moved out, i/o cannot be done for it, let i/o queue know
			scheduler.ioQ.get(scheduler.ioQ.indexOf(scheduler.waitQ.peek())).inCore = 0;
			scheduler.waitQ.peek().moving = true;
			scheduler.drumBusy = true;
		}
		//else if the drum is not busy and next job in the waitQ wants to move in, check if it can fit into memory
		else if (!scheduler.drumBusy && !scheduler.waitQ.isEmpty() && scheduler.waitQ.peek().inCore == 0 && memory.canFit(scheduler.waitQ.peek().size))
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
			runningJob = null;
			return;
		}
		//else, run job on queue
		if (!scheduler.readyQ.isEmpty())
		{
			scheduler.srtn();
			runningJob = scheduler.readyQ.peek();
			runningJob.timeSlice -= curTime - prevTime;
			runningJob.runTime += curTime - prevTime;
		}
		else
		{
			a[0] = 1;
			runningJob = null;
			return;
		}
		/*if job finished time quantum and used it's max CPU time, then
		 *it is completed, remove it from queue and memory*/
		if (runningJob.runTime == runningJob.maxTime && !runningJob.inIoQ)
		{
			//remove job from memory
			memory.remove(runningJob.location, runningJob.size);
			//remove job from queue
			scheduler.readyQ.remove();
			//update worstfit location;
			memory.worstFit();
			//if drum is not busy, check if the next job on the waitQ want to move out of memory
			if (!scheduler.drumBusy && !scheduler.waitQ.isEmpty() && scheduler.waitQ.peek().inCore == 1 && !scheduler.waitQ.peek().latchBit)
			{
				sos.siodrum(scheduler.waitQ.peek().num, scheduler.waitQ.peek().size, scheduler.waitQ.peek().location, 1);
				//since is being moved out, i/o cannot be done for it, let i/o queue know
				scheduler.ioQ.get(scheduler.ioQ.indexOf(scheduler.waitQ.peek())).inCore = 0;
				scheduler.waitQ.peek().moving = true;
				scheduler.drumBusy = true;
			}
			//else if the drum is not busy and next job in the waitQ wants to move in, check if it can fit into memory
			else if (!scheduler.drumBusy && !scheduler.waitQ.isEmpty() && scheduler.waitQ.peek().inCore == 0 && memory.canFit(scheduler.waitQ.peek().size))
			{
			//call siodrum to transfer the job
				memory.worstFit();
				scheduler.waitQ.peek().location = memory.worstFitLocation;
				sos.siodrum(scheduler.waitQ.peek().num, scheduler.waitQ.peek().size, memory.worstFitLocation, 0);
				memory.add(scheduler.waitQ.peek().size);
				scheduler.drumBusy = true;
			}
			//if the ready queue is empty, return control to sos with no jobs to run
			if (scheduler.readyQ.isEmpty())
			{
				a[0] = 1;
				runningJob = null;
			}
			else 
			{
				scheduler.srtn();
				runningJob = scheduler.readyQ.peek();
			}
			return;
				
		}
		//else, there should be a running job, run it
		else
			a[0] = 2;
			p[1] = runningJob.num;
			p[2] = runningJob.location;
			p[3] = runningJob.size;
			//if the next job remaining time is less than the timequantum, make the timeslice the remainig time
			if (runningJob.maxTime - runningJob.runTime < runningJob.TIMEQUANTUM)
				runningJob.timeSlice = runningJob.maxTime - runningJob.runTime;
			p[4] = runningJob.timeSlice;

		return;
	}

	//Diskint - called when a job finishes I/O
	public static void Dskint(int []a, int []p)
	{
		//update time variables
		prevTime = curTime;
		curTime = p[5];
		scheduler.ioBusy = false;
		if (!scheduler.readyQ.isEmpty())
		{
			scheduler.readyQ.peek().timeSlice -= curTime - prevTime;
			scheduler.readyQ.peek().runTime += curTime - prevTime;
		}
		//System.out.print("\n\n///////------*****DSKINT*****------///////\n\n");
		//since its i/o is done, incase it was added to be swaped out, remove it from the waitQ
		scheduler.waitQ.remove(scheduler.ioQ.peek());
		//Since it finished I/O, it is no longer latched and is unblocked
		scheduler.ioQ.peek().latchBit = false;
		scheduler.ioQ.peek().inIoQ = false;
		if (scheduler.ioQ.peek().blockBit && !scheduler.ioQ.peek().killIt)
		{
			scheduler.ioQ.peek().blockBit = false;
			scheduler.readyQ.add(scheduler.ioQ.peek());
		}
		if (scheduler.ioQ.peek().killIt)
		{
			memory.remove(scheduler.ioQ.peek().location, scheduler.ioQ.peek().size);
			//if drum is not busy, check if the next job on the waitQ want to move out of memory
			if (!scheduler.drumBusy && !scheduler.waitQ.isEmpty() && scheduler.waitQ.peek().inCore == 1 && !scheduler.waitQ.peek().latchBit)
			{
				sos.siodrum(scheduler.waitQ.peek().num, scheduler.waitQ.peek().size, scheduler.waitQ.peek().location, 1);
				//since is being moved out, i/o cannot be done for it, let i/o queue know
				scheduler.ioQ.get(scheduler.ioQ.indexOf(scheduler.waitQ.peek())).inCore = 0;
				scheduler.waitQ.peek().moving = true;
				scheduler.drumBusy = true;
			}
			//else if the drum is not busy and next job in the waitQ wants to move in, check if it can fit into memory
			else if (!scheduler.drumBusy && !scheduler.waitQ.isEmpty() && scheduler.waitQ.peek().inCore == 0 && memory.canFit(scheduler.waitQ.peek().size))
			{
				//call siodrum to transfer the job
				memory.worstFit();
				scheduler.waitQ.peek().location = memory.worstFitLocation;
				sos.siodrum(scheduler.waitQ.peek().num, scheduler.waitQ.peek().size, memory.worstFitLocation, 0);
				memory.add(scheduler.waitQ.peek().size);
				scheduler.drumBusy = true;
			}
		}
		scheduler.ioQ.remove();
		//if there is more jobs on the ioQ, run them. else set the ioQ busy to false.
		if (!scheduler.ioQ.isEmpty())
		{
			if (scheduler.ioQ.peek().inCore == 0 || scheduler.ioQ.peek().moving)
			{
				scheduler.ioQ.remove();
			}
			if (!scheduler.ioQ.isEmpty()){
			sos.siodisk(scheduler.ioQ.peek().num);
			if (!scheduler.ioQ.peek().blockBit && !scheduler.ioQ.peek().killIt)
				scheduler.readyQ.get(scheduler.readyQ.indexOf(scheduler.ioQ.peek())).latchBit = true;
			scheduler.ioBusy = true;}
		}
		if (scheduler.readyQ.isEmpty())
		{
			a[0] = 1;
			runningJob = null;
			return;
		}
		scheduler.srtn();
		runningJob = scheduler.readyQ.peek();
		/*if running job finished time quantum and used it's max CPU time, then
		 *it is completed, remove it from queue and memory*/
		if (runningJob.runTime == runningJob.maxTime)
		{
			if (!runningJob.inIoQ)
			{
				//remove job from memory
				memory.remove(runningJob.location, runningJob.size);
			}
			else
				scheduler.ioQ.get(scheduler.ioQ.indexOf(runningJob)).killIt = true;
			//remove job from queue
			scheduler.readyQ.remove();
			//update worstfit location;
			memory.worstFit();
			//if drum is not busy, check if the next job on the waitQ want to move out of memory
			if (!scheduler.drumBusy && !scheduler.waitQ.isEmpty() && scheduler.waitQ.peek().inCore == 1 && !scheduler.waitQ.peek().latchBit)
			{
				sos.siodrum(scheduler.waitQ.peek().num, scheduler.waitQ.peek().size, scheduler.waitQ.peek().location, 1);
				//since is being moved out, i/o cannot be done for it, let i/o queue know
				scheduler.ioQ.get(scheduler.ioQ.indexOf(scheduler.waitQ.peek())).inCore = 0;
				scheduler.waitQ.peek().moving = true;
				scheduler.drumBusy = true;
			}
			//else if the drum is not busy and next job in the waitQ wants to move in, check if it can fit into memory
			else if (!scheduler.drumBusy && !scheduler.waitQ.isEmpty() && scheduler.waitQ.peek().inCore == 0 && memory.canFit(scheduler.waitQ.peek().size))
			{
			//call siodrum to transfer the job
				memory.worstFit();
				scheduler.waitQ.peek().location = memory.worstFitLocation;
				sos.siodrum(scheduler.waitQ.peek().num, scheduler.waitQ.peek().size, memory.worstFitLocation, 0);
				memory.add(scheduler.waitQ.peek().size);
				scheduler.drumBusy = true;
			}
			//if the ready queue is empty, return control to sos with no jobs to run
			if (scheduler.readyQ.isEmpty())
			{
				a[0] = 1;
				runningJob = null;
				return;
			}
		}
		scheduler.srtn();
		//Resume job to run
		runningJob = scheduler.readyQ.peek();
		a[0] = 2;
		p[1] = runningJob.num;
		p[2] = runningJob.location;
		p[3] = runningJob.size;
		//if the next job remaining time is less than the timequantum, make the timeslice the remainig time
		runningJob.timeSlice = runningJob.maxTime - runningJob.runTime;
		p[4] = runningJob.timeSlice;
		return;
	}

	//Drmint - called when job finishes moving from drum to mem, or vice versa
	public static void Drmint(int []a, int []p)
	{
	
	//update time variables
		prevTime = curTime;
		curTime = p[5];
		scheduler.drumBusy = false;
		//if the job incorebit = 0, the job was moved in, add it to the readyQ
		if (scheduler.waitQ.peek().inCore == 0)
		{
			scheduler.waitQ.peek().inCore = 1;
			scheduler.readyQ.add(scheduler.waitQ.peek());
			scheduler.waitQ.remove();
		}
		//else, if the job incorebit = 1, the job was moved out of memory, remove it from memory and put it at the back of the waitQ
		else
		{
			scheduler.waitQ.peek().inCore = 0;
			memory.remove(scheduler.waitQ.peek().location, scheduler.waitQ.peek().size);
			scheduler.waitQ.add(scheduler.waitQ.peek());
			scheduler.waitQ.remove();
			
		}
		//arange the waitQ
		scheduler.sjnWaitQ();
		//update worstfit location;
		memory.worstFit();
		//if drum is not busy, check if the next job on the waitQ want to move out of memory
		if (!scheduler.drumBusy && !scheduler.waitQ.isEmpty() && scheduler.waitQ.peek().inCore == 1 && !scheduler.waitQ.peek().latchBit)
		{
			sos.siodrum(scheduler.waitQ.peek().num, scheduler.waitQ.peek().size, scheduler.waitQ.peek().location, 1);
			//since is being moved out, i/o cannot be done for it, let i/o queue know
			scheduler.ioQ.get(scheduler.ioQ.indexOf(scheduler.waitQ.peek())).inCore = 0;
			scheduler.waitQ.peek().moving = true;
			scheduler.drumBusy = true;
		}
		//else if the drum is not busy and next job in the waitQ wants to move in, check if it can fit into memory
		else if (!scheduler.drumBusy && !scheduler.waitQ.isEmpty() && scheduler.waitQ.peek().inCore == 0 && memory.canFit(scheduler.waitQ.peek().size))
		{
		//call siodrum to transfer the job
			memory.worstFit();
			scheduler.waitQ.peek().location = memory.worstFitLocation;
			sos.siodrum(scheduler.waitQ.peek().num, scheduler.waitQ.peek().size, memory.worstFitLocation, 0);
			memory.add(scheduler.waitQ.peek().size);
			scheduler.drumBusy = true;
		}
		/*if there is a job running, check if it is completed then remove it,
		 *else just keep running it */
		if (runningJob !=null)
		{
			scheduler.readyQ.peek().timeSlice -= curTime - prevTime;
			scheduler.readyQ.peek().runTime += curTime - prevTime;
			/*if job finished time quantum and used it's max CPU time, then
			 *it is completed, remove it from queue and memory*/
			if (scheduler.readyQ.peek().runTime == runningJob.maxTime)
			{	
				if (scheduler.readyQ.peek().inIoQ)
					scheduler.ioQ.get(scheduler.ioQ.indexOf(runningJob)).killIt = true;
				else
				{
					//remove job from memory
					memory.remove(runningJob.location, runningJob.size);
				}
				scheduler.readyQ.remove();
				//update worstfit location;
				memory.worstFit();
				//if drum is not busy, check if the next job on the waitQ want to move out of memory
				if (!scheduler.drumBusy && !scheduler.waitQ.isEmpty() && scheduler.waitQ.peek().inCore == 1 && !scheduler.waitQ.peek().latchBit)
				{
					sos.siodrum(scheduler.waitQ.peek().num, scheduler.waitQ.peek().size, scheduler.waitQ.peek().location, 1);
					//since is being moved out, i/o cannot be done for it, let i/o queue know
					scheduler.ioQ.get(scheduler.ioQ.indexOf(scheduler.waitQ.peek())).inCore = 0;
					scheduler.waitQ.peek().moving = true;
					scheduler.drumBusy = true;
				}
				//else if the drum is not busy and next job in the waitQ wants to move in, check if it can fit into memory
				else if (!scheduler.drumBusy && !scheduler.waitQ.isEmpty() && scheduler.waitQ.peek().inCore == 0 && memory.canFit(scheduler.waitQ.peek().size))
				{
				//call siodrum to transfer the job
					memory.worstFit();
					scheduler.waitQ.peek().location = memory.worstFitLocation;
					sos.siodrum(scheduler.waitQ.peek().num, scheduler.waitQ.peek().size, memory.worstFitLocation, 0);
					memory.add(scheduler.waitQ.peek().size);
					scheduler.drumBusy = true;
				}
			}
		}
		scheduler.srtn();
		runningJob = scheduler.readyQ.peek();
		//System.out.print("\n\n///////------*****DRMINT*****------///////\n\n");
		
		

		//Resume job to run
		a[0] = 2;
		p[1] = runningJob.num;
		p[2] = runningJob.location;
		p[3] = runningJob.size;
		//if the next job remaining time is less than the timequantum, make the timeslice the remainig time
			runningJob.timeSlice = runningJob.maxTime - runningJob.runTime;
		p[4] = runningJob.timeSlice;
		return;
	}

	//TRO - called when a job completes its time quantum
	public static void Tro(int []a, int []p)
	{
		//update time variables
		prevTime = curTime;
		curTime = p[5];
		scheduler.readyQ.peek().timeSlice -= curTime - prevTime;
		scheduler.readyQ.peek().runTime += curTime - prevTime;
		/*else if job finished time quantum and used it's max CPU time, then
		 *it is completed, remove it from queue and memory*/
		if (scheduler.readyQ.peek().maxTime == scheduler.readyQ.peek().runTime)
		{
			if (scheduler.readyQ.peek().inIoQ)
				scheduler.ioQ.get(scheduler.ioQ.indexOf(runningJob)).killIt = true;
			else
			{
				//remove job from memory
				memory.remove(runningJob.location, runningJob.size);
			}
			scheduler.readyQ.remove();
			//update worstfit location;
			memory.worstFit();
			scheduler.sjnWaitQ();
			//if drum is not busy, check if the next job on the waitQ want to move out of memory
			if (!scheduler.drumBusy && !scheduler.waitQ.isEmpty() && scheduler.waitQ.peek().inCore == 1 && !scheduler.waitQ.peek().latchBit)
			{
				sos.siodrum(scheduler.waitQ.peek().num, scheduler.waitQ.peek().size, scheduler.waitQ.peek().location, 1);
				//since is being moved out, i/o cannot be done for it, let i/o queue know
				scheduler.ioQ.get(scheduler.ioQ.indexOf(scheduler.waitQ.peek())).inCore = 0;
				scheduler.waitQ.peek().moving = true;
				scheduler.drumBusy = true;
			}
			//else if the drum is not busy and next job in the waitQ wants to move in, check if it can fit into memory
			else if (!scheduler.drumBusy && !scheduler.waitQ.isEmpty() && scheduler.waitQ.peek().inCore == 0 && memory.canFit(scheduler.waitQ.peek().size))
			{
			//call siodrum to transfer the job
				memory.worstFit();
				scheduler.waitQ.peek().location = memory.worstFitLocation;
				sos.siodrum(scheduler.waitQ.peek().num, scheduler.waitQ.peek().size, memory.worstFitLocation, 0);
				memory.add(scheduler.waitQ.peek().size);
				scheduler.drumBusy = true;
			}
			//if there is no job in the ready queue, the CPU is idle
			if (scheduler.readyQ.isEmpty()) {
				a[0] = 1;
				runningJob = null;
				return;
			}
		
			//otherwise, the next job in the queue is now the running job
			scheduler.srtn();
			runningJob = scheduler.readyQ.peek();
		
			//Resume the job to run
			a[0] = 2;
			p[1] = runningJob.num;
			p[2] = runningJob.location;
			p[3] = runningJob.size;
				runningJob.timeSlice = runningJob.maxTime - runningJob.runTime;
			p[4] = runningJob.timeSlice;
		
			return;
		}
		//else, the job still needs time for completion, therefore is given a new time quantum
		else
			scheduler.readyQ.peek().timeSlice = scheduler.readyQ.peek().maxTime - scheduler.readyQ.peek().runTime;
		//System.out.print("\n\n///////------*****TRO*****------///////\n\n");
		
		//push running job to back and start next job in queue
		scheduler.srtn();
		runningJob = scheduler.readyQ.peek();
		
		//Resume job to run
		a[0] = 2;
		p[1] = runningJob.num;
		p[2] = runningJob.location;
		p[3] = runningJob.size;
		//if the next job remaining time is less than the timequantum, make the timeslice the remainig time
		if (runningJob.maxTime - runningJob.runTime < runningJob.TIMEQUANTUM)
				runningJob.timeSlice = runningJob.maxTime - runningJob.runTime;
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
				//System.out.print("\n\n///////------*****SVC-TERM*****------///////\n\n");
		
				//remove job from memory
				if (runningJob.inIoQ)
				{
					scheduler.ioQ.get(scheduler.ioQ.indexOf(runningJob)).killIt = true;
				}
				else
				{
					memory.remove(runningJob.location, runningJob.size);
				}
				scheduler.readyQ.remove();
				//update worstfit location;
				memory.worstFit();
				scheduler.sjnWaitQ();
				//if drum is not busy, check if the next job on the waitQ want to move out of memory
				if (!scheduler.drumBusy && !scheduler.waitQ.isEmpty() && scheduler.waitQ.peek().inCore == 1 && !scheduler.waitQ.peek().latchBit)
				{
					sos.siodrum(scheduler.waitQ.peek().num, scheduler.waitQ.peek().size, scheduler.waitQ.peek().location, 1);
					//since is being moved out, i/o cannot be done for it, let i/o queue know
//scheduler.ioQ.get(scheduler.ioQ.indexOf(scheduler.waitQ.peek())).inCore = 0;
					scheduler.waitQ.peek().moving = true;
					scheduler.drumBusy = true;
				}
				//else if the drum is not busy and next job in the waitQ wants to move in, check if it can fit into memory
				else if (!scheduler.drumBusy && !scheduler.waitQ.isEmpty() && scheduler.waitQ.peek().inCore == 0 && memory.canFit(scheduler.waitQ.peek().size))
				{
				//call siodrum to transfer the job
					memory.worstFit();
					scheduler.waitQ.peek().location = memory.worstFitLocation;
					sos.siodrum(scheduler.waitQ.peek().num, scheduler.waitQ.peek().size, memory.worstFitLocation, 0);
					memory.add(scheduler.waitQ.peek().size);
					scheduler.drumBusy = true;
				}
				//if there is no job in the ready queue, the CPU is idle
				if (scheduler.readyQ.isEmpty()) {
					a[0] = 1;
					runningJob = null;
					break;
				}
		
				//otherwise, the next job in the queue is now the running job
				scheduler.srtn();
				runningJob = scheduler.readyQ.peek();
		
				//Resume the job to run
				a[0] = 2;
				p[1] = runningJob.num;
				p[2] = runningJob.location;
				p[3] = runningJob.size;
				//if the next job remaining time is less than the timequantum, make the timeslice the remainig time
					runningJob.timeSlice = runningJob.maxTime - runningJob.runTime;
				p[4] = runningJob.timeSlice;
		
				break;
		
			//a=6 - request I/O
			case 6:
		
				//System.out.print("\n\n///////------*****SVC-I/O*****------///////\n\n");
				//Update its CPU running time
				scheduler.readyQ.peek().timeSlice -= curTime - prevTime;
				scheduler.readyQ.peek().runTime += curTime - prevTime;
				//scheduler.readyQ.peek().runTime = runningJob.runTime;
				//scheduler.readyQ.peek().timeSlice = runningJob.timeSlice;
				if (!runningJob.inIoQ)
					scheduler.ioQ.add(runningJob);
				scheduler.readyQ.peek().inIoQ = true;
				if (!scheduler.ioBusy && !scheduler.ioQ.isEmpty())
				{
					if (scheduler.ioQ.peek().inCore == 0 || scheduler.ioQ.peek().moving)
					{
						scheduler.ioQ.add(scheduler.ioQ.peek());
						scheduler.ioQ.remove();
					}
				//call siodisk - which starts I/O for the given job
					sos.siodisk(scheduler.ioQ.peek().num);
					scheduler.ioBusy = true;
					//set the latchBit to true, since the job is doing I/O
					scheduler.readyQ.peek().latchBit = true;
				}
				
				//run next job
				scheduler.srtn();
				runningJob = scheduler.readyQ.peek();
				a[0] = 2;
				p[1] = runningJob.num;
				p[2] = runningJob.location;
				p[3] = runningJob.size;
						runningJob.timeSlice = runningJob.maxTime - runningJob.runTime;
				p[4] = runningJob.timeSlice;
		
				break;
			
			//a=7 - block
			case 7:
		
				//System.out.print("\n\n///////------*****SVC-BLOCK*****------///////\n\n");
				//Update its CPU running time
				scheduler.readyQ.peek().timeSlice -= curTime - prevTime;
				scheduler.readyQ.peek().runTime += curTime - prevTime;
				if (!scheduler.readyQ.peek().latchBit && !scheduler.readyQ.peek().inIoQ)
				{
					scheduler.srtn();
					runningJob = scheduler.readyQ.peek();
					a[0] = 2;
					p[1] = runningJob.num;
					p[2] = runningJob.location;
					p[3] = runningJob.size;
						runningJob.timeSlice = runningJob.maxTime - runningJob.runTime;
					p[4] = runningJob.timeSlice;
					break;
				}
				//set blockBit to true
				scheduler.readyQ.peek().blockBit = true;
				scheduler.ioQ.set(scheduler.ioQ.indexOf(scheduler.readyQ.peek()), scheduler.readyQ.peek());
				/*if (scheduler.readyQ.peek().maxTime > 10000)
				{
					scheduler.waitQ.add(scheduler.readyQ.peek());
					scheduler.ioQ.get(scheduler.ioQ.indexOf(scheduler.readyQ.peek())).killIt = true;
				}
				*/
				scheduler.sjnWaitQ();
				scheduler.readyQ.remove();
				//if drum is not busy, check if the next job on the waitQ want to move out of memory
				if (!scheduler.drumBusy && !scheduler.waitQ.isEmpty() && scheduler.waitQ.peek().inCore == 1 && !scheduler.waitQ.peek().latchBit)
				{
					sos.siodrum(scheduler.waitQ.peek().num, scheduler.waitQ.peek().size, scheduler.waitQ.peek().location, 1);
					scheduler.waitQ.peek().moving = true;
					scheduler.drumBusy = true;
				}
				//else if the drum is not busy and next job in the waitQ wants to move in, check if it can fit into memory
				else if (!scheduler.drumBusy && !scheduler.waitQ.isEmpty() && scheduler.waitQ.peek().inCore == 0 && memory.canFit(scheduler.waitQ.peek().size))
				{
				//call siodrum to transfer the job
					memory.worstFit();
					scheduler.waitQ.peek().location = memory.worstFitLocation;
					sos.siodrum(scheduler.waitQ.peek().num, scheduler.waitQ.peek().size, memory.worstFitLocation, 0);
					memory.add(scheduler.waitQ.peek().size);
					scheduler.drumBusy = true;
				}
				//if readyQ is not empty, make the running job the next job
				if (!scheduler.readyQ.isEmpty())
				{
					scheduler.srtn();
					runningJob = scheduler.readyQ.peek();
				}
				else
				{
					a[0] = 1;
					runningJob = null;
					break;
				}

				//if the running job is blocked and latched, the CPU is idle
				if (runningJob.blockBit && runningJob.latchBit)
				{
					a[0] = 1;
					runningJob = null;
				}
				//else resume job to run
				else{
					a[0] = 2;
					p[1] = runningJob.num;
					p[2] = runningJob.location;
					p[3] = runningJob.size;
						runningJob.timeSlice = runningJob.maxTime - runningJob.runTime;
					p[4] = runningJob.timeSlice;
				}
		
				break;
		}
		
		return;
	}
}