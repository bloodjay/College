import java.util.*;

//This class is used to schedule jobs to run on the CPU
//more specifically, it reorders jobs based on
//whether or not they are blocked
public class CPUScheduler
{
	public LinkedList<jobTable> readyQ; //Queue that holds jobs ready to run
	public LinkedList<jobTable> waitQ; //Queue that holds jobs waiting to come into mem
	public boolean unblockedJobs; //whether or not unblocked jobs exist in mem

	//constructor
	public CPUScheduler()
	{
		readyQ = new LinkedList<jobTable>();
		waitQ = new LinkedList<jobTable>();
		unblockedJobs = true;
	}

	//This function brings unblocked jobs to the front of the queue
	public void reschedule()
	{
		//This for statement will check if there are any
		//unblocked jobs in memory (only jobs in memory are in readyQ)
		for (jobTable j: readyQ)
			if (!j.blockBit) {
				unblockedJobs = true;
				break;
			}
			else
				unblockedJobs = false;

		//if there are unblocked jobs in memory, this will
		//bring an unblocked job to the front of the queue
		while (readyQ.peek().blockBit && unblockedJobs) {
			readyQ.add(readyQ.peek());
			readyQ.remove();
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
}