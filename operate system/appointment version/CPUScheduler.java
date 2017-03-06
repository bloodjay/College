import java.util.*;

//This class is used to schedule jobs to run on the CPU
//more specifically, it reorders jobs based on
//whether or not they are blocked
public class CPUScheduler
{
	public LinkedList<jobTable> readyQ; //Queue that holds jobs ready to run
	public LinkedList<jobTable> waitQ; //Queue that holds jobs waiting to come into mem
	public LinkedList<jobTable> ioQ;
	public LinkedList<jobTable> blockedQ;

	//constructor
	public CPUScheduler()
	{
		readyQ = new LinkedList<jobTable>();
		waitQ = new LinkedList<jobTable>();
		ioQ = new LinkedList<jobTable>();
		blockedQ = new LinkedList<jobTable>();
	}

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