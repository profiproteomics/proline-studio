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
package com.thierry.filtering.report;

import java.awt.Color;

public class TextReportBuilder implements ReportBuilder {
	
	private static final String LINE_SEPARATOR = System.getProperty("line.separator");

	private StringBuilder builder = new StringBuilder(1024*256);
	
	public void addCell(Color bgColor, Color fgColor, String content) {
		builder.append(content);
		builder.append("\t");
		
	}

	public void addColumnHeader(String header) {
		builder.append(header);
		builder.append("\t");
		
	}

	public void close() {
		// do nothing 
		
	}

	public void endLine() {
		builder.deleteCharAt(builder.length() - 1).append(LINE_SEPARATOR);
		
	}

	public void endTable() {
		
	}

	public void endTableHeader() {
		builder.deleteCharAt(builder.length() - 1).append(LINE_SEPARATOR);
	}

	public void open() {
		
	}

	public void startLine() {
		
	}

	public void startTable() {
		
	}

	public void startTableHeader() {
		
	}

	
	@Override
	public String toString() {
		return builder.toString();
	}
}
