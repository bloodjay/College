//This data structure just holds information on jobs
public class jobTable
{
	public static int TIMEQUANTUM = 10;
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
	public jobTable(int n, int s, int t, int l)
	{
		num = n;
		size = s;
		runTime = 0;
		maxTime = t;
		location = l;
		timeSlice = TIMEQUANTUM;
		latchBit =  false;
		blockBit = false;
	}
	
}