/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.mzscope.ui;

import fr.proline.mzscope.model.MzScopePreferences;
import fr.proline.mzscope.model.Scan;
import fr.proline.mzscope.ui.event.ScanHeaderListener;
import fr.proline.mzscope.util.MzScopeConstants;
import fr.proline.studio.export.ExportButton;
import fr.proline.studio.graphics.PlotAbstract;
import fr.proline.studio.graphics.PlotLinear;
import fr.proline.studio.graphics.PlotPanel;
import fr.proline.studio.graphics.PlotPanelListener;
import fr.proline.studio.graphics.PlotStick;
import fr.proline.studio.graphics.marker.LabelMarker;
import fr.proline.studio.graphics.marker.LineMarker;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.JToolBar;

/**
 *contains the scan panel
 * @author MB243701
 */
public class SpectrumPanel extends JPanel implements ScanHeaderListener, PlotPanelListener{
    private AbstractRawFilePanel rawFilePanel;
    
    protected PlotPanel spectrumPlotPanel;
    protected JToolBar spectrumToolbar;
    private HeaderSpectrumPanel headerSpectrumPanel;
    protected PlotAbstract scanPlot;
    protected Scan currentScan;
    
    private boolean keepMsLevel = true;
    
    protected int xicModeDisplay = MzScopeConstants.MODE_DISPLAY_XIC_REPLACE;

    public SpectrumPanel(AbstractRawFilePanel rawFilePanel) {
        super();
        this.rawFilePanel = rawFilePanel;
    }
    
    public void initChart(){
        // Create Scan Charts
        spectrumPlotPanel = new PlotPanel();
        spectrumPlotPanel.addListener(this);
        spectrumPlotPanel.setDrawCursor(true);
        List<Integer> emptyListScanIndex = new ArrayList<>();
        emptyListScanIndex.add(0);
        boolean multiRawFile = rawFilePanel instanceof MultiRawFilePanel;
        headerSpectrumPanel = new HeaderSpectrumPanel(null, emptyListScanIndex, !multiRawFile);
        headerSpectrumPanel.addScanHeaderListener(this);
        spectrumPlotPanel.repaint();
        
        this.removeAll();
        this.add(headerSpectrumPanel, BorderLayout.NORTH);
        this.add(spectrumPlotPanel, BorderLayout.CENTER);
        this.add(getSpectrumToolbar(), BorderLayout.WEST);
    }
    
    private JToolBar getSpectrumToolbar(){
        spectrumToolbar = new JToolBar(JToolBar.VERTICAL);
        spectrumToolbar.setFloatable(false);
        ExportButton exportImageButton = new ExportButton("Graphic", spectrumPlotPanel);
        spectrumToolbar.add(exportImageButton);

        return spectrumToolbar;
    }

    
    @Override
    public void plotPanelMouseClicked(MouseEvent e, double xValue, double yValue) {
        if (e.getClickCount() == 2) {
            scanPlot.clearMarkers();
            double yStdevLabel = scanPlot.getYMax()*0.1;
            scanPlot.addMarker(new LineMarker(spectrumPlotPanel, xValue, LineMarker.ORIENTATION_VERTICAL));
            scanPlot.addMarker(new LabelMarker(spectrumPlotPanel, xValue, yStdevLabel, "Mass "+xValue, LabelMarker.ORIENTATION_X_RIGHT, LabelMarker.ORIENTATION_Y_TOP));
            double domain = xValue;
            float ppmTol = MzScopePreferences.getInstance().getMzPPMTolerance();
            double maxMz = domain + domain * ppmTol / 1e6;
            double minMz = domain - domain * ppmTol / 1e6;
            rawFilePanel.scanMouseClicked(e, minMz, maxMz, xicModeDisplay);
        }
    }

    @Override
    public void updateScanIndex(Integer scanIndex) {
        rawFilePanel.displayScan(scanIndex);
    }

    @Override
    public void updateRetentionTime(float retentionTime) {
        int scanIdx = rawFilePanel.getCurrentRawfile().getScanId(retentionTime);
        rawFilePanel.displayScan(scanIdx);
    }

    @Override
    public void keepMsLevel(boolean keep) {
        this.keepMsLevel = keep;
        updateScanIndexList();
    }
    
    public void displayScan(Scan scan) {
        
       double minValue = 0.0; 
       double maxValue = 0.0;
       
       if (currentScan != null) {
           minValue = spectrumPlotPanel.getXAxis().getMinValue();
           maxValue = spectrumPlotPanel.getXAxis().getMaxValue();
       }
        
        
        if (scan != null) {
            Color plotColor = rawFilePanel.getPlotColor(rawFilePanel.getCurrentRawfile());
            ScanModel scanModel = new ScanModel(scan);
            scanModel.setColor(plotColor);
            if (scan.getDataType() == Scan.ScanType.CENTROID) { // mslevel2
                //stick plot
                scanPlot = new PlotStick(spectrumPlotPanel, scanModel, scanModel, ScanModel.COLTYPE_SCAN_MASS, ScanModel.COLTYPE_SCAN_INTENSITIES);
                ((PlotStick) scanPlot).setStrokeFixed(true);
                ((PlotStick) scanPlot).setPlotInformation(scanModel.getPlotInformation());
                ((PlotStick) scanPlot).setIsPaintMarker(true);
            } else {
                scanPlot = new PlotLinear(spectrumPlotPanel, scanModel, scanModel, ScanModel.COLTYPE_SCAN_MASS, ScanModel.COLTYPE_SCAN_INTENSITIES);
                ((PlotLinear) scanPlot).setStrokeFixed(true);
                ((PlotLinear) scanPlot).setPlotInformation(scanModel.getPlotInformation());
                ((PlotLinear) scanPlot).setIsPaintMarker(true);
            }
            
            spectrumPlotPanel.setPlot(scanPlot);
            if (currentScan != null) {
               spectrumPlotPanel.getXAxis().setRange(minValue, maxValue);
            }
            spectrumPlotPanel.repaint();
            headerSpectrumPanel.setMzdbFileName(rawFilePanel.getCurrentRawfile().getName());
            currentScan = scan;
            updateScanIndexList();
            headerSpectrumPanel.setScan(currentScan);
        }
    }
    
    private void updateScanIndexList() {
        List<Integer> listScanIndex = new ArrayList();
        if (keepMsLevel) {
            listScanIndex.add(rawFilePanel.getCurrentRawfile().getPreviousScanId(currentScan.getIndex(), currentScan.getMsLevel()));
        } else {
            listScanIndex.add(currentScan.getIndex() - 1);
        }
        listScanIndex.add(currentScan.getIndex());
        if (keepMsLevel) {
            listScanIndex.add(rawFilePanel.getCurrentRawfile().getNextScanId(currentScan.getIndex(), currentScan.getMsLevel()));
        } else {
            listScanIndex.add(currentScan.getIndex() + 1);
        }
        headerSpectrumPanel.setScanIndexList(listScanIndex);
    }
    
    @Override
    public void updateXicDisplayMode(int mode){
        xicModeDisplay = mode;
    }
    
    public int getXicModeDisplay(){
        return this.xicModeDisplay;
    }
    
}
