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
import java.awt.BorderLayout;
import java.awt.Color;
import java.io.File;
import java.text.DecimalFormat;

import javax.swing.JToolBar;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.PieSectionLabelGenerator;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;
import org.slf4j.LoggerFactory;


import fr.proline.studio.export.ExportButton;
import fr.proline.studio.gui.HourglassPanel;

import org.slf4j.Logger;

/**
 * Panel used to display a pie chart for MSDiag
 *
 * @author AW
 */
public class MSDiag_PieChart extends HourglassPanel {
    
    public static final String SERIES_NAME = "Pie chart";
    
    protected static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");
    private static final long serialVersionUID = 1L;

    private final DefaultPieDataset m_dataSet;
    private final JFreeChart m_chart;
 
    private javax.swing.JPanel m_pieChartPanel;

    private final RsetMSDiagPanel m_msdiagPanel;

   

    /**
     * Creates new form MSDiag_PieChart
     */
    public MSDiag_PieChart(RsetMSDiagPanel rsetMSDiagPanel) {
        
    	m_msdiagPanel = rsetMSDiagPanel;
    	
        m_dataSet = new DefaultPieDataset();
        m_chart = ChartFactory.createPieChart(
	            "Chart Title",  // chart title
	            m_dataSet ,             // data
	            true,               // include legend
	            true,
	            false
	        );
         
        initComponents();
        
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        ChartPanel cp = new ChartPanel(m_chart, true);
        
        cp.setMinimumDrawWidth(0);
        cp.setMinimumDrawHeight(0);
        cp.setMaximumDrawWidth(Integer.MAX_VALUE); // make the legend to have a fixed size and not strecht it 
        cp.setMaximumDrawHeight(Integer.MAX_VALUE); // when the windows becomes bigger.
        m_chart.getPlot().setBackgroundPaint(Color.white);
       
        m_pieChartPanel = cp;
        
        JToolBar toolbar = initToolbar();
        
        add(toolbar, BorderLayout.WEST);
        add(m_pieChartPanel, BorderLayout.CENTER);
        
        
    }
    
    private JToolBar initToolbar() {
        
        JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
        toolbar.setFloatable(false);
   
        FlipButton flipModeButton = new FlipButton("flip button text", m_msdiagPanel);
        toolbar.add(flipModeButton);
        
        ExportButton exportImageButton = new ExportButton("pieChart", m_pieChartPanel);
        toolbar.add(exportImageButton);
       
        
        return toolbar;
        
    }
    
    public void setData(MSDiagOutput_AW msdo) {
        
        constructPieChart(msdo);
     }


   
    
    private void constructPieChart(MSDiagOutput_AW msdo) {

        // clear all data
        m_dataSet.clear();
    	
        if (msdo == null) {
            return;
        }
        
        
        // Set title
        String title = msdo.description;
        m_chart.setTitle(title);

        m_chart.getPlot().setBackgroundPaint(Color.white);
        PieSectionLabelGenerator gen = new StandardPieSectionLabelGenerator(
                "{0}: {1} ({2})", new DecimalFormat("0"), new DecimalFormat("0%"));
        ((PiePlot) m_chart.getPlot()).setLabelGenerator(gen);
            
        
        if(msdo.matrix.length == 1) { // then there is some data
	    	if(msdo.matrix[0].length == 2) { // then both data are present
		        m_dataSet.setValue(msdo.column_names[0], (Double) Math.abs((double) msdo.matrix[0][0]));
			    m_dataSet.setValue(msdo.column_names[1], (Double) Math.abs((double) msdo.matrix[0][1]));
			    ((PiePlot) m_chart.getPlot()).setSectionPaint(msdo.column_names[0], new Color(220,220,220/*255,60,60*/));
		        ((PiePlot) m_chart.getPlot()).setSectionPaint(msdo.column_names[1], new Color(60,60,220));
		    }
	    }
	    
    }

}
