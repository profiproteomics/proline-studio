/* 
 * Copyright (C) 2019 VD225637
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1 
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License 
 * along with this program; If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
 */
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
