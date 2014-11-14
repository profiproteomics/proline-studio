package fr.proline.studio.rsmexplorer.gui;



public class MSDiagOutput_AW {
	

	public Object [][] /*Array<Array<Double>>*/ matrix;
	public MSDiagOutputTypes output_typeZZ;  // the type of information that is represented by the table (chart, table, histogram, etc)
	public String cell_type; // the (native) type of cell values (integer, string, float)
	public String description; // the string description of the output (ie Number of matches)
	public String[] /*Seq<String>*/ column_names; // the column headers
	public String x_axis_description; // the string description of the X axis (ie. Retention times)
	public String y_axis_description; 
	
	public enum MSDiagOutputTypes {
		Table ("table"),
		Histogram ("histogram"),
		Pie ("pie"),
		Chromatogram ("chromatogram");
				
		private String Value = "";
		MSDiagOutputTypes(String value) {
			this.Value = value;
		}
		public String toString() {
			return Value;
		}
	}
	

	
	public MSDiagOutput_AW ( Double[][] matrix,
		//MSDiagOutputType outputType,  // the type of information that is represented by the table (chart, table, histogram, etc)
		String cellType, // the (native) type of cell values (integer, string, float)
		String description, // the string description of the output (ie Number of matches)
		String[]/*Seq<String>*/ column_names, // the column headers
		String x_axis_description, // the string description of the X axis (ie. Retention times)
		String y_axis_description) {
		
			this.matrix = matrix;
			this.output_typeZZ = output_typeZZ;
			this.cell_type = cellType;
			this.description = description;
			this.column_names = column_names;
			this.x_axis_description = x_axis_description;
			this.y_axis_description = y_axis_description;
		
		
	}
		   

	
}


