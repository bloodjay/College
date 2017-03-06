import java.util.*;

//This class is used to schedule jobs to run on the CPU
//more specifically, it reorders jobs based on
//whether or not they are blocked
public class CPUScheduler
{
	public LinkedList<jobTable> readyQ; //Queue that holds jobs ready to run
	public LinkedList<jobTable> waitQ; //Queue that holds jobs waiting to come into mem
	public LinkedList<jobTable> ioQ;
	public boolean unblockedJobs; //whether or not unblocked jobs exist in mem
	public static boolean drumBusy = false;
	public static boolean ioBusy = false;
	public LinkedList<jobTable> drumQ;

	//constructor
	public CPUScheduler()
	{
		readyQ = new LinkedList<jobTable>();
		waitQ = new LinkedList<jobTable>();
		ioQ = new LinkedList<jobTable>();
		
		unblockedJobs = true;
	}

	//This function brings unblocked jobs to the front of the queue
	public void reschedule()
	{
		//This for statement will check if there are any
		//unblocked jobs in memory (only jobs in memory are in readyQ)
		for (int j = 0; j < readyQ.size(); j ++)
		{
		
System.out.println(readyQ.peek().num + " " + readyQ.peek().blockBit + "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			boolean temp = false;
			if (readyQ.peek().blockBit == false) 
			{
				unblockedJobs = true;
				temp = true;
			}
			else if (!temp)
				unblockedJobs = false;
		}
		//if there are unblocked jobs in memory, this will
		//bring an unblocked job to the front of the queue
		while (readyQ.peek().blockBit && unblockedJobs) 
		{
System.out.println(readyQ.peek().num + " " + readyQ.peek().blockBit + "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			readyQ.add(readyQ.peek());
			readyQ.remove();
System.out.println(readyQ.peek().num + " " + readyQ.peek().blockBit + "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		}

		return;
	}

	//This function takes the job in front and puts it to the end
	//If the next job is blocked, the reschedule function is called
	//to move an unblocked job to the front.
	public void roundRobin()
	{
		//if there is more than one element in the queue,
		//put the front to the back
		if (readyQ.size() > 1) {
			readyQ.add(readyQ.peek());
			readyQ.remove();
		}
	
		//if front job is blocked, reschedule
		if (readyQ.peek().blockBit)
			reschedule();
	
		return;
	}
	
	public void sjnWaitQ ()
	{
		
		jobTable temp;
		int jtemp, itemp;
		if (waitQ.size()> 2)
			for (int i=1; i<waitQ.size()-1; i++)
				{
					for ( int j=i+1; j<waitQ.size(); j++)
					{
					jtemp = waitQ.get(j).maxTime / waitQ.get(j).size;
					itemp = waitQ.get(i).maxTime / waitQ.get(i).size;
						if (jtemp < itemp)
						//if(waitQ.get(j).maxTime - waitQ.get(j).runTime < waitQ.get(i).maxTime - waitQ.get(i).runTime)
						//if(waitQ.get(j).size < waitQ.get(i).size)
						{
							temp = waitQ.get(i);
							waitQ.set(i, waitQ.get(j));
							waitQ.set(j, temp);
						}
					}
				}
				for (int i=1; i<waitQ.size()-1; i++)
				{
					for ( int j=i+1; j<waitQ.size(); j++)
					{
					
						if (waitQ.get(j).killIt && !waitQ.get(i).killIt)
						{
							temp = waitQ.get(i);
							waitQ.set(i, waitQ.get(j));
							waitQ.set(j, temp);
						}
					}
				}
		return;
	}
	
	public void srtn()
	{
		jobTable temp;
		if (readyQ.size()> 2)
			for (int i=0; i<readyQ.size()-1; i++)
				{
					for ( int j=i+1; j<readyQ.size(); j++)
						if(readyQ.get(j).maxTime - readyQ.get(j).runTime < readyQ.get(i).maxTime - readyQ.get(i).runTime)
						{
							temp = readyQ.get(i);
							readyQ.set(i, readyQ.get(j));
							readyQ.set(j, temp);
						}						
					
				}
		return;
	}
}