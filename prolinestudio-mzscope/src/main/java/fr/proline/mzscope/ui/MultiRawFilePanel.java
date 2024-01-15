/* 
 * Copyright (C) 2019
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
package fr.proline.mzscope.ui;

import fr.proline.mzscope.model.*;
import fr.proline.mzscope.utils.MzScopeCallback;
import fr.proline.mzscope.ui.model.MzScopePreferences;
import fr.proline.mzscope.utils.Display;
import fr.proline.studio.graphics.marker.IntervalMarker;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.concurrent.ExecutionException;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
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

    private final List<IRawFile> rawfiles;
    private final Map<IRawFile, IChromatogram> chromatogramByRawFile;
    private final Map<String, Color> colorByRawFilename;

    private IRawFile currentRawFile;

    public MultiRawFilePanel(List<IRawFile> rawfiles) {
        super();
        this.rawfiles = rawfiles;
        chromatogramByRawFile = new HashMap();
        colorByRawFilename = new HashMap();
        currentRawFile = this.rawfiles.get(0);
        for (IRawFile rawFile : rawfiles) {
            chromatogramByRawFile.put(rawFile, null);
            colorByRawFilename.put(rawFile.getName(), null);
        }
        displayTIC(-1);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                updateToolbar();
            }
        });
    }

    @Override
    public IRawFile getCurrentRawfile() {
        return currentRawFile;
    }

    @Override
    public List<IRawFile> getAllRawfiles() {
        return rawfiles;
    }

    @Override
    public void extractAndDisplay(final ExtractionRequest params, Display display, MzScopeCallback callback) {
       // in this implementation display is ignored : always REPLACE since we will extract one IChromatogram per RawFile
        SwingWorker worker = new SwingWorker<Integer, IChromatogram>() {
            Display display = new Display(Display.Mode.SERIES);
            @Override
            protected Integer doInBackground() throws Exception {
                int count = 0;
                for (IRawFile rawFile : rawfiles) {
                    IChromatogram c = rawFile.getXIC(params);
                    count++;
                    publish(c);
                }
                return count;
            }

            @Override
            protected void process(List<IChromatogram> chunks) {
                for (int k = 0; k < chunks.size(); k++) {
                    logger.info("display chromato number {}",k);
                    displayChromatogram(chunks.get(k), display);
                }
            }

            @Override
            protected void done() {
                try {
                    chromatogramPanel.setCurrentChromatogram(chromatogramByRawFile.get(currentRawFile));
                    MzScopePreferences.getInstance().setLastExtractionRequest(params);
                    logger.info("{} TIC chromatogram extracted", get());
                } catch (InterruptedException | ExecutionException e) {
                    logger.error("Error while reading chromatogram");
                }
            }
        };

        worker.execute();
    }

    @Override
    public IChromatogram getCurrentChromatogram() {
        return chromatogramByRawFile.get(currentRawFile);
    }
    
    @Override
    public Color displayChromatogram(IChromatogram chromato, Display display) {
       setMsMsEventButtonEnabled(true);
       Color plotColor = super.displayChromatogram(chromato, display);
       colorByRawFilename.put(chromato.getRawFilename(), plotColor);
       return plotColor ;
    }

    @Override
    public void displayChromatograms(Map<IRawFile, IChromatogram> chromatogramByRawFile, Display display) {
        //TODO: implements a true multi chomatogram display
        super.displayChromatograms(chromatogramByRawFile, display);
    }

    @Override
    protected void displayTIC(int msLevel) {
        final List<IRawFile> rawFiles = new ArrayList<>(rawfiles);
        if (rawFileLoading != null){
            rawFileLoading.setWaitingState(true);
        }
        logger.info("Display {} TIC chromatograms", rawFiles.size());
        SwingWorker worker = new SwingWorker<Integer, IChromatogram>() {

            Display display = new Display(Display.Mode.SERIES);
            
            @Override
            protected Integer doInBackground() throws Exception {
                int count = 0;    
                for (IRawFile rawFile : rawFiles) {
                    IChromatogram c = rawFile.getTIC(msLevel);
                    chromatogramByRawFile.put(rawFile, c);
                    publish(c);
                }
                return count;
            }

            @Override
            protected void process(List<IChromatogram> chunks) {
                for (int k = 0; k < chunks.size(); k++) {
                    logger.info("diplay TIC number {}",k);
                    displayChromatogram(chunks.get(k), display);
                }
                setMsMsEventButtonEnabled(false);
            }

            @Override
            protected void done() {
                try {
                    chromatogramPanel.setCurrentChromatogram(chromatogramByRawFile.get(currentRawFile));
                    logger.info("{} TIC chromatogram extracted", get());
                } catch (InterruptedException | ExecutionException e) {
                    logger.error("Error while reading chromatogram");
                }finally{
                    if (rawFileLoading != null){
                        rawFileLoading.setWaitingState(false);
                    }
                }
            }
        };

        worker.execute();
    }
    
    // override display feature to display all xic
    @Override
    public void displayPeakel(final IPeakel peakel) {
        double ppm = MzScopePreferences.getInstance().getMzPPMTolerance();
        final double maxMz = peakel.getMz() + peakel.getMz() * ppm / 1e6;
        final double minMz = peakel.getMz() - peakel.getMz() * ppm / 1e6;

        final List<IRawFile> rawFiles = new ArrayList<>(rawfiles);
        final ExtractionRequest extractionRequest = ExtractionRequest.builder(this).setMz(peakel.getMz()).setMzTolPPM((float)ppm).build();

        SwingWorker worker = new SwingWorker<Integer, IChromatogram>() {
            int count = 0;
            boolean isFirstProcessCall = true;
                
            @Override
            protected Integer doInBackground() throws Exception {
                //return getCurrentRawfile().getXIC(minMz, maxMz);
                for (IRawFile rawFile : rawFiles) {
                    IChromatogram c = rawFile.getXIC(extractionRequest);
                    count++;
                    publish(c);
                }
                return count;
            }
            
            @Override
            protected void process(List<IChromatogram> chunks) {
                int k = 0;
                Display display = new Display(Collections
                    .singletonList(new IntervalMarker(null, Color.ORANGE, Color.RED, peakel.getFirstElutionTime(), peakel.getLastElutionTime())));
                if (isFirstProcessCall) {
                    logger.info("display first chromato");
                    isFirstProcessCall = false;
                    displayChromatogram(chunks.get(0), new Display(Display.Mode.REPLACE));
                    k = 1;
                    displayScan(getCurrentRawfile().getSpectrumId(peakel.getElutionTime()));
                    chromatogramPanel.displayFeature(peakel, display);
                }
                for (; k < chunks.size(); k++) {
                    logger.info("add additionnal chromato");
                    displayChromatogram(chunks.get(k), new Display(Display.Mode.OVERLAY));
                    displayScan(getCurrentRawfile().getSpectrumId(peakel.getElutionTime()));
                    chromatogramPanel.displayFeature(peakel, display);
                }
            }

            @Override
            protected void done() {
                try {
                    chromatogramPanel.setCurrentChromatogram(chromatogramByRawFile.get(currentRawFile));
                    MzScopePreferences.getInstance().setLastExtractionRequest(extractionRequest);
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
        SwingWorker worker = new SwingWorker<Integer, IChromatogram>() {
            
            Display display = new Display(Display.Mode.SERIES);
            
            @Override
            protected Integer doInBackground() throws Exception {
                int count = 0;
                for (IRawFile rawFile : rawFiles) {
                    IChromatogram c = rawFile.getBPI();
                    count++;
                    publish(c);
                }
                return count;
            }

            @Override
            protected void process(List<IChromatogram> chunks) {
                for (int k = 0; k < chunks.size(); k++) {
                    logger.info("display BPI chromato number {}", k);
                    displayChromatogram(chunks.get(k), display);
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
        ActionListener changeCurrentChromatogramAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                AbstractButton aButton = (AbstractButton) actionEvent.getSource();
                logger.debug("Selected: " + aButton.getText());
                // search for rawFile
                IRawFile rawFile = getRawFile(aButton.getText());
                if (rawFile != null) {
                    chromatogramPanel.setCurrentChromatogram(chromatogramByRawFile.get(rawFile));
                    currentRawFile = rawFile;
                    if(chromatogramPanel.getCurrentScanTime() != null) {
                        int scanIdx = rawFile.getSpectrumId(chromatogramPanel.getCurrentScanTime());
                        currentScan = null;
                        displayScan(scanIdx);
                    }
                    //update MS2 events if shown 
                    if (getShowMS2Button().isSelected()){
                       showMSMSEvents();
                    }
                }
            }
        };
        for (IRawFile rawFile : rawfiles) {
            JRadioButtonMenuItem mi = new JRadioButtonMenuItem(rawFile.getName());
            mi.setIcon(new Icon(){
                @Override
                public void paintIcon(Component c, Graphics g, int x, int y) {
                    Color previousColor = g.getColor();
                    g.setColor(getPlotColor(rawFile.getName()));
                    g.fillRect(x, y, 10, 10);
                    g.setColor(previousColor);
                }

                @Override
                public int getIconWidth() {
                    return 10;
                }

                @Override
                public int getIconHeight() {
                    return 10;
                }
            
            });
            mi.addActionListener(changeCurrentChromatogramAction);
            popupMenu.add(mi);
            bg.add(mi);
        }
        ((JRadioButtonMenuItem)popupMenu.getComponent(0)).setSelected(true);
        final JButton currentChromatoBtn = new JButton("Chr");
        setToolTipText("Display TIC IChromatogram");
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
    public Color getPlotColor(String rawFilename) {
        return colorByRawFilename.get(rawFilename);
    }

}
