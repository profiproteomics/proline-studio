package fr.proline.mzscope.utils;

public class BinarySearch {

	int iterations = 0; 
	int fails = 0;
	
	public BinarySearch() {
		
	}
	
	public int search(float[] values, float key, int imin, int imax) {
		if (key < values[imin]) {
			fails++; 
			return searchIndex(values, key, 0, imax);
		}
		if (key > values[imax]) {
			fails++;
			return searchIndex(values, key, imin, values.length);
		}
		return searchIndex(values, key, imin, imax);
	}
	
	public int searchIndex(float[] values, float key, int imin, int imax) {
		int imid = 0;
		if ((key < values[0]) || (key > values[values.length-1])) return -1;
		while (imax >= imin) {
			imid = (imin + imax) >> 1;
			if (imid >= imax) break;
			iterations++;
			if (values[imid] < key)
				imin = imid + 1;
			else
				imax = imid;
		}
	    return (values[imin] == key) ? imin : imin - 1;
		
	}

        public int searchIndex(double[] values, double key, int imin, int imax) {
		int imid = 0;
		if ((key < values[0]) || (key > values[values.length-1])) return -1;
		while (imax >= imin) {
			imid = (imin + imax) >> 1;
			if (imid >= imax) break;
			iterations++;
			if (values[imid] < key)
				imin = imid + 1;
			else
				imax = imid;
		}
	    return (values[imin] == key) ? imin : imin - 1;
	}

	public int getIterations() {
		return iterations;
	}

	public int getFails() {
		return fails;
	}
	
	
}
