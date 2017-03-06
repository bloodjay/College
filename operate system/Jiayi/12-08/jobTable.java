//This data structure just holds information on jobs
public class jobTable
{
	public static final int TIMEQUANTUM = 25;
	public int num;
	public int size;
	public int runTime;
	public int maxTime;
	public int location;
	public int timeSlice;
	public boolean latchBit; //latchBit means it's doing I/O
	public boolean blockBit; //blockBit means it's blocked from CPU

	//default constructor to initialize without parameters
	public jobTable(){}

	//constructor with parameters for Job:
	//number, size, maxCPUTime, and location
	public jobTable(int n, int s, int t)
	{
		num = n;
		size = s;
		runTime = 0;
		maxTime = t;
		if (TIMEQUANTUM > t)
			timeSlice = t;
		else
			timeSlice = TIMEQUANTUM;
		latchBit =  false;
		blockBit = false;
	}
	
}