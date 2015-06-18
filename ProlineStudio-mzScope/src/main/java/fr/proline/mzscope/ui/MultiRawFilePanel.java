package fr.proline.mzscope.ui;

import fr.profi.mzdb.model.Feature;
import fr.proline.mzscope.model.Chromatogram;
import fr.proline.mzscope.model.IRawFile;
import fr.proline.mzscope.model.MzScopePreferences;
import fr.proline.studio.graphics.PlotLinear;
import fr.proline.studio.graphics.marker.IntervalMarker;
import fr.proline.studio.graphics.marker.LineMarker;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author CB205360
 */
public class MultiRawFilePanel extends AbstractRawFilePanel {

    final private static Logger logger = LoggerFactory.getLogger(MultiRawFilePanel.class);

    private List<IRawFile> rawfiles;
    private Map<IRawFile, Chromatogram> mapChromatogramForRawFile;
    private Map<IRawFile, Color> mapColorForRawFile;

    public MultiRawFilePanel(List<IRawFile> rawfiles) {
        super();
        this.rawfiles = rawfiles;
        mapChromatogramForRawFile = new HashMap();
        mapColorForRawFile = new HashMap();
        for (IRawFile rawFile : rawfiles) {
            mapChromatogramForRawFile.put(rawFile, null);
            mapColorForRawFile.put(rawFile, null);
        }
        displayTIC();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                updateToolbar();
            }
        });
    }

    @Override
    public IRawFile getCurrentRawfile() {
        return currentChromatogram.rawFile;
    }

    @Override
    public void scanMouseClicked(MouseEvent e, double minMz, double maxMz, int xicModeDisplay){
        extractChromatogram(minMz, maxMz);
    }

    @Override
    public void extractChromatogram(final double minMz, final double maxMz) {
        SwingWorker worker = new SwingWorker<Integer, Chromatogram>() {

            int count = 0;

            @Override
            protected Integer doInBackground() throws Exception {

                for (IRawFile rawFile : rawfiles) {
                    Chromatogram c = rawFile.getXIC(minMz, maxMz);
                    count++;
                    publish(c);
                }
                return count;
            }

            @Override
            protected void process(List<Chromatogram> chunks) {
                Chromatogram c = chunks.get(chunks.size() - 1);
                if (count == 1) {
                    logger.info("display first chromato");
                    displayChromatogram(c);
                } else {
                    logger.info("add additionnal chromato");
                    addChromatogram(c);
                }
            }

            @Override
            protected void done() {
                try {
                    logger.info("{} TIC chromatogram extracted", get());
                } catch (InterruptedException | ExecutionException e) {
                    logger.error("Error while reading chromatogram");
                }
            }
        };

        worker.execute();
    }
    
    @Override
    public Color displayChromatogram(Chromatogram chromato) {
       setMsMsEventButtonEnabled(true);
       Color plotColor = super.displayChromatogram(chromato);
       mapColorForRawFile.put(chromato.rawFile, plotColor);
       return plotColor ;
    }
    
    @Override
    public Color addChromatogram(Chromatogram chromato) {
       Color plotColor = super.addChromatogram(chromato);
       mapColorForRawFile.put(chromato.rawFile, plotColor);
       return plotColor ;
    }

    @Override
    protected void displayTIC() {
        final List<IRawFile> rawFiles = new ArrayList<>(rawfiles);
        logger.info("Display {} TIC chromatograms", rawFiles.size());
        SwingWorker worker = new SwingWorker<Integer, Chromatogram>() {

            int count = 0;
            boolean isFirstProcessCall = true;

            @Override
            protected Integer doInBackground() throws Exception {

                for (IRawFile rawFile : rawFiles) {
                    Chromatogram c = rawFile.getTIC();
                    mapChromatogramForRawFile.put(rawFile, c);
                    count++;
                    publish(c);
                }
                return count;
            }

            @Override
            protected void process(List<Chromatogram> chunks) {
                int k = 0;
                if (isFirstProcessCall) {
                    logger.info("display first chromato");
                    isFirstProcessCall = false;
                    displayChromatogram(chunks.get(0));
                    k = 1;
                }
                for (; k < chunks.size(); k++) {
                    logger.info("add additionnal chromato");
                    addChromatogram(chunks.get(k));
                }
                setMsMsEventButtonEnabled(false);
            }

            @Override
            protected void done() {
                try {
                    logger.info("{} TIC chromatogram extracted", get());
                } catch (InterruptedException | ExecutionException e) {
                    logger.error("Error while reading chromatogram");
                }
            }
        };

        worker.execute();
    }
    
    // override display feature to display all xic
    @Override
    public void displayFeature(final Feature f) {
        double ppm = MzScopePreferences.getInstance().getMzPPMTolerance();
        final double maxMz = f.getMz() + f.getMz() * ppm / 1e6;
        final double minMz = f.getMz() - f.getMz() * ppm / 1e6;

        final List<IRawFile> rawFiles = new ArrayList<>(rawfiles);
        SwingWorker worker = new SwingWorker<Integer, Chromatogram>() {
            int count = 0;
            boolean isFirstProcessCall = true;

                
            @Override
            protected Integer doInBackground() throws Exception {
                //return getCurrentRawfile().getXIC(minMz, maxMz);
                for (IRawFile rawFile : rawFiles) {
                    Chromatogram c = rawFile.getXIC(minMz, maxMz);
                    count++;
                    publish(c);
                }
                return count;
            }
            
            @Override
            protected void process(List<Chromatogram> chunks) {
                int k = 0;
                if (isFirstProcessCall) {
                    logger.info("display first chromato");
                    isFirstProcessCall = false;
                    displayChromatogram(chunks.get(0));
                    k = 1;
                    displayScan(f.getBasePeakel().getApexScanId());
                    PlotLinear chromatogramPlot= chromatogramPlots.isEmpty()?null:chromatogramPlots.get(0);
                    if (chromatogramPlot != null){
                        chromatogramPlot.clearMarkers();
                        chromatogramPlot.addMarker(new IntervalMarker(chromatogramPlotPanel, Color.ORANGE, Color.RED, f.getBasePeakel().getFirstElutionTime() / 60.0, f.getBasePeakel().getLastElutionTime() / 60.0));
                        chromatogramPlot.addMarker(new LineMarker(chromatogramPlotPanel,f.getElutionTime() / 60.0, Color.BLUE) );
                    }
                    
                }
                for (; k < chunks.size(); k++) {
                    logger.info("add additionnal chromato");
                    addChromatogram(chunks.get(k));
                    displayScan(f.getBasePeakel().getApexScanId());
                    
                    PlotLinear chromatogramPlot= chromatogramPlots.isEmpty()?null:chromatogramPlots.get(0);
                    if (chromatogramPlot != null){
                        chromatogramPlot.clearMarkers();
                        chromatogramPlot.addMarker(new IntervalMarker(chromatogramPlotPanel, Color.ORANGE, Color.RED,  f.getBasePeakel().getFirstElutionTime() / 60.0, f.getBasePeakel().getLastElutionTime() / 60.0));
                        chromatogramPlot.addMarker(new LineMarker(chromatogramPlotPanel,f.getElutionTime() / 60.0, Color.BLUE) );
                    }
                }
            }

            @Override
            protected void done() {
                try {
                    logger.info("{} Display Feature", get());
                } catch (InterruptedException | ExecutionException e) {
                    logger.error("Error while displaying feature");
                }
            }
        };
        worker.execute();

    }

    @Override
    protected void displayBPI() {
        final List<IRawFile> rawFiles = new ArrayList<>(rawfiles);
        logger.info("Display {} BPI chromatogram", rawFiles.size());
        SwingWorker worker = new SwingWorker<Integer, Chromatogram>() {
            int count = 0;
            boolean isFirstProcessCall = true;

            @Override
            protected Integer doInBackground() throws Exception {

                for (IRawFile rawFile : rawFiles) {
                    Chromatogram c = rawFile.getBPI();
                    count++;
                    publish(c);
                }
                return count;
            }

            @Override
            protected void process(List<Chromatogram> chunks) {
                int k = 0;
                if (isFirstProcessCall) {
                    logger.info("display first chromato");
                    isFirstProcessCall = false;
                    displayChromatogram(chunks.get(0));
                    k = 1;
                }
                for (; k < chunks.size(); k++) {
                    logger.info("add additionnal chromato");
                    addChromatogram(chunks.get(k));
                }
                setMsMsEventButtonEnabled(false);
            }

            @Override
            protected void done() {
                try {
                    logger.info("{} BPI chromatogram extracted", get());
                } catch (InterruptedException | ExecutionException e) {
                    logger.error("Error while reading chromatogram");
                }
            }
        };

        worker.execute();
    }
    
    private IRawFile getRawFile(String fileName){
        for (IRawFile rawFile : rawfiles) {
            if (rawFile.getName().equals(fileName)) {
                return rawFile;
            }
        }
        return null;
    }

    protected JToolBar updateToolbar() {
        final JPopupMenu popupMenu = new JPopupMenu();
        ButtonGroup bg = new ButtonGroup();
        final Map<String, JRadioButtonMenuItem> map = new HashMap<>();
        ActionListener changeCurrentChromatogramAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                AbstractButton aButton = (AbstractButton) actionEvent.getSource();
                logger.debug("Selected: " + aButton.getText());
                // search for rawFile
                IRawFile rawFile = getRawFile(aButton.getText());
                if (rawFile != null) {
                    currentChromatogram = mapChromatogramForRawFile.get(rawFile);
                    if(currentScanTime != null) {
                        int scanIdx = rawFile.getScanId(currentScanTime);
                        displayScan(scanIdx);
                    }
                    if (listMsMsMarkers != null && !listMsMsMarkers.isEmpty()) {
                        hideMSMSEvents();
                        showMSMSEvents();
                    }
                }
            }
        };
        for (IRawFile rawFile : rawfiles) {
            JRadioButtonMenuItem mi = new JRadioButtonMenuItem(rawFile.getName());
            mi.addActionListener(changeCurrentChromatogramAction);
            popupMenu.add(mi);
            bg.add(mi);
            map.put(rawFile.getName(), mi);
        }
        map.get(rawfiles.get(0).getName()).setSelected(true);
        final JButton currentChromatoBtn = new JButton("Chr");
        setToolTipText("Display TIC Chromatogram");
        currentChromatoBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                popupMenu.show(currentChromatoBtn, 0, currentChromatoBtn.getHeight());
            }
        });
        chromatogramToolbar.addSeparator();
        chromatogramToolbar.add(currentChromatoBtn);
        return chromatogramToolbar;
    }

    @Override
    public Color getPlotColor(IRawFile rawFile) {
        return mapColorForRawFile.get(rawFile);
    }

}
