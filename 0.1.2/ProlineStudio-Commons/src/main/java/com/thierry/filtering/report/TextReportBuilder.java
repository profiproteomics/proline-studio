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
