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

import com.thierry.filtering.ColorUtil;

public class HTMLReportBuilder implements ReportBuilder {
	
	private StringBuilder builder = new StringBuilder(1024*512);

	public void addCell(Color bgColor, Color fgColor, String content) {
		String bgColorStr = "#" + ColorUtil.getHexName(bgColor);
		String fgColorStr = "#" + ColorUtil.getHexName(fgColor);
		builder.append("<td bgColor="); 
		builder.append(bgColorStr);
		builder.append(">");
		builder.append("<font color=");
		builder.append(fgColorStr); 
		builder.append(">");
		builder.append(content);
		builder.append("</font></td>");

	}

	public void addColumnHeader(String header) {
		builder.append("<th>");
		builder.append(header);
		builder.append("</th>");

	}

	public void endTable() {
		builder.append("</table>");
	}

	public void startTableHeader() {
		builder.append("<tr>");
	}

	public void close() {
		builder.append("</body></html>");

	}

	public void open() {
		builder.append("<html><body>");

	}

	public void endLine() {
		builder.append("</tr>");
		
	}

	public void endTableHeader() {
		builder.append("</tr>");
		
	}

	public void startLine() {
		builder.append("<tr>");
		
	}

	public void startTable() {
		builder.append("<table>");
		
	}

	@Override
	public String toString() {
		return builder.toString();
	}
	
}
