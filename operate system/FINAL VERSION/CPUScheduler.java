import java.util.*;

//This class is used to schedule jobs to run on the CPU
public class CPUScheduler
{
	public LinkedList<jobTable> readyQ; //Queue that holds jobs ready to run
	public LinkedList<jobTable> waitQ; //Queue that holds jobs waiting to come into mem
	public LinkedList<jobTable> ioQ; //Queue that holds jobs waiting to do IO
	public LinkedList<jobTable> blockedQ; //Queue that holds jobs that have been blocked

	//constructor
	public CPUScheduler()
	{
		readyQ = new LinkedList<jobTable>();
		waitQ = new LinkedList<jobTable>();
		ioQ = new LinkedList<jobTable>();
		blockedQ = new LinkedList<jobTable>();
	}

	//This function reorders jobs on the blocked queue and puts the shortest on top
	public void sjnBlockedQ()
	{
		if (blockedQ.isEmpty() || os.drumBusy)
			return;

		int size = 1000000000;
		int temp;
		jobTable tempJ = new jobTable();

		for (jobTable j : blockedQ) {
			temp = j.size;
			if (temp < size) {
				size = temp;
				tempJ = j;
			}
		}

		blockedQ.remove(tempJ);
		blockedQ.addFirst(tempJ);

		return;
	}

	//This function reorders jobs on the wait queue and puts the job that
	//has the smallest CPU time to size ratio.
	//(A short job that is also small will be placed before a short job that is large)
	public void sjnWaitQ()
	{
		if (waitQ.isEmpty() || os.drumBusy)
			return;

		double minRatio = 999999999999.0;
		double temp;
		jobTable tempJ = new jobTable();

		for (jobTable j : waitQ) {
			temp = j.maxTime/j.size;
			if (temp < minRatio) {
				minRatio = temp;
				tempJ = j;
			}
		}

		waitQ.remove(tempJ);
		waitQ.addFirst(tempJ);

		return;
	}

	//This function reorders the ready queue to put the jobs with the 
	//shortest remaining time on top
	public void srtn()
	{
		if (readyQ.isEmpty())
			return;

		double maxRatio = 10000000000.0;
		double temp;
		jobTable tempJ = new jobTable();

		for (jobTable j : readyQ) {
			temp = (j.maxTime - j.runTime)/j.size;
			if (temp < maxRatio) {
				maxRatio = temp;
				tempJ = j;
			}
		}

		readyQ.remove(tempJ);
		readyQ.addFirst(tempJ);

		return;
	}
}