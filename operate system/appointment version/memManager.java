//This class is used for memory management
//It handles keeping track of how much memory is used
public class memManager
{
	//memory will be an array of boolean values
	//false means not used
	//true means used
	//(Java doesn't allow boolean values to be 0 or 1)
	public boolean[] mem; //array representation of memory
	public int worstFitSize; //largest available free chunk in memory
	public int worstFitLocation; //location of said chunk

	//constructor
	public memManager()
	{
		mem = new boolean[100];

		for (boolean i: mem)
			i = false;

		worstFitSize = 100;
		worstFitLocation = 0;
	}

	//function to check if job of size can fit in memory
	public boolean canFit(int size)
	{
		worstFit();
		if (worstFitSize >= size)
			return true;

		return false;
	}

	//function to add job into memory
	//by default, added to worst fit
	//requires size of job
	public void add(int size)
	{
		if (worstFitSize < size)
			return;

		for (int i=worstFitLocation; i<worstFitLocation + size; i++)
			mem[i] = true;

		//update worst fit size and location
		worstFit();

		return;
	}

	//function to remove job from memory
	//requires size and location of job to be removed
	public void remove(int location, int size)
	{
		for (int i=location; i<size + location; i++)
			mem[i] = false;

		//update worst fit size and location
		worstFit();

		return;
	}

	//function to update size and location of worst fit space in mem
	public void worstFit()
	{
		int size = 0;
		int temp = 0;
		int index = 0;

		//This loop will look for the largest contiguous free space
		for (int i=0; i<100; i++) {
			if (!mem[i]) {
				temp++;

				if (temp > size) {
					size = temp;
					index = i - size + 1;
				}
			}

			else if (mem[i])
				temp = 0;
		}

		//the findings from above are stored into
		//the location and size variables
		worstFitLocation = index;
		worstFitSize = size;

		return;
	}
}