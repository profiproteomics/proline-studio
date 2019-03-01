package fr.proline.mzscope.utils;

public class OutputUtils {

	public static void outputValues(float[] v) {
		for (int i = 0; i < v.length; i++) {
			System.out.println(i+"\t"+v[i]);
		}		
	}
	
	public static void outputValues(float[] v, float[] w) {
		for (int i = 0; i < v.length; i++) {
			System.out.println(i+"\t"+v[i]+"\t"+w[i]);
		}		
	}
	
}
