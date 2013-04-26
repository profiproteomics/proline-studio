package com.thierry.filtering.report;

import java.awt.Color;

public interface ReportBuilder {
	
	public void open();
	
	public void startTable();
	
	public void startTableHeader();
	
	public void addColumnHeader(String header);
	
	public void endTableHeader();
	
	public void startLine();
	
	public void addCell(Color bgColor, Color fgColor, String content);
	
	public void endLine();
	
	public void endTable();
	
	public void close();

}
