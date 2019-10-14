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
package fr.proline.studio.rsmexplorer.gui;



public class MSDiagOutput_AW {
	

	public Object [][]  matrix;
	public MSDiagOutputTypes output_type;  // the type of information that is represented by the table (chart, table, histogram, etc)
	public String cell_type; // the (native) type of cell values (integer, string, float)
	public String description; // the string description of the output (ie Number of matches)
	public String[] column_names; // the column headers
	public String x_axis_description; // the string description of the X axis (ie. Retention times)
	public String y_axis_description; 
	private Integer prefered_order;
	
	public class MSDiagOutputTypes {
		// the following section has been disabled (as enum instead of class), need to find a way to deserialize enums...
	/*	Table ("table"),
		Histogram ("histogram"),
		Pie ("pie"),
		Chromatogram ("chromatogram");
			*/	
		public String value = "";
		MSDiagOutputTypes(String aValue) {
			this.value = aValue;
		}
		public String toString() {
			return value;
		}
	}
	
	public MSDiagOutput_AW ( Double[][] matrix, String cellType, String description, String[] column_names, String x_axis_description, String y_axis_description) {
		this(matrix, cellType, description, column_names, x_axis_description, y_axis_description, 0);
	}
	
	public MSDiagOutput_AW ( Double[][] matrix,
		//MSDiagOutputType outputType,  // the type of information that is represented by the table (chart, table, histogram, etc)
		String cellType, // the (native) type of cell values (integer, string, float)
		String description, // the string description of the output (ie Number of matches)
		String[]/*Seq<String>*/ column_names, // the column headers
		String x_axis_description, // the string description of the X axis (ie. Retention times)
		String y_axis_description,
		Integer order) {
		
			this.matrix = matrix;
			this.output_type = output_type;
			this.cell_type = cellType;
			this.description = description;
			this.column_names = column_names;
			this.x_axis_description = x_axis_description;
			this.y_axis_description = y_axis_description;
			this.prefered_order = order;
		
	}

	public Integer getOrder() {
		if(prefered_order == null)
			return 0;
		return prefered_order;
	}
		   

	
}


