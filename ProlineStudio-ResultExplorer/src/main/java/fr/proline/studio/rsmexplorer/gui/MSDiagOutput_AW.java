package fr.proline.studio.rsmexplorer.gui;



public class MSDiagOutput_AW {
	
//	new MSDiagOutput(
//		      matrix = data,
//		      outputType = MSDiagOutputTypes.Table,
//		      cellType = scala.Double.toString,
//		      description = "Calculated masses per charge",
//		      columnNames = columnNames.toSeq,
//		      xAxisDescription = "Masses",
//		      yAxisDescription = "Charges")

//	Array[Array[Any]] matrix , // the output table containing all data
//  val outputType: MSDiagOutputTypes.MSDiagOutputType, // the type of information that is represented by the table (chart, table, histogram, etc)
//  val cellType: String = scala.Int.toString, // the (native) type of cell values (integer, string, float)
//  val description: String = "", // the string description of the output (ie. Number of matches)
//  val columnNames: Seq[_] = Seq[String](), // the column headers
//  val xAxisDescription: String = "", // the string description of the X axis (ie. Retention times)
//  val yAxisDescription: String = "" // the string description of the Y axis (ie. Score)
//  ) {
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
	
//	class MSDiagOutputType  {
//		public String  outputType;
//				
//	}
	
	public MSDiagOutput_AW (/*Array<Array<Double>>*/ Double[][] matrix,
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
		   

	
//	public MSDiagOutput_AW generateFakeTable () {
//		ArrayList<ArrayList<Double>> matrix = null ;
//		ArrayList<Double> array = new ArrayList<Double>();
//		array.add(1.0);
//		array.add(501.291092);
//		array.add(1008.596771);
//		array.add(716.4439398290601); 
//		array.add(688.354401);
//		matrix.add((ArrayList<Double>) array);
//		array = new ArrayList<Double>();
//		array.add(2.0);
//		array.add(501.291092);
//		array.add(1008.596771);
//		array.add(716.4439398290601); 
//		array.add(688.354401);
//		matrix.add((ArrayList<Double>) array);
//		array = new ArrayList<Double>();
//		array.add(3.0);
//		array.add(501.291092);
//		array.add(1008.596771);
//		array.add(716.4439398290601); 
//		array.add(688.354401);
//		matrix.add((ArrayList<Double>) array);
//		ArrayList<String> listOfCols  = new ArrayList<String>();
//		listOfCols.add("Charge");
//		listOfCols.add("Lowest Mz");
//		listOfCols.add("Highest Mz");
//		listOfCols.add("Average Mz");
//		listOfCols.add("Median Mz");
//		listOfCols.add("Masses");
//		listOfCols.add("Charges");
//		//new MSDiagOutput_AW(matrix, outputType, cellType, description, columnNames, xAxisDescription, yAxisDescription);
//		return new MSDiagOutput_AW(matrix, MSDiagOutputType.Table, scala.Double.toString(), "Calculated masses per charge", (Seq<String> ) listOfCols,"","")
//		  }
	
}


