package fr.proline.mzscope.ui;

import fr.proline.mzscope.model.Chromatogram;
import fr.proline.mzscope.model.IFeature;
import fr.proline.mzscope.model.IRawFile;
import fr.proline.mzscope.model.Ms1ExtractionRequest;
import fr.proline.mzscope.model.MzScopeCallback;
import fr.proline.mzscope.model.MzScopePreferences;
import fr.proline.mzscope.utils.MzScopeConstants.DisplayMode;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

    private final List<IRawFile> rawfiles;
    private final Map<IRawFile, Chromatogram> mapChromatogramForRawFile;
    private final Map<String, Color> mapColorByRawFilename;

    public MultiRawFilePanel(List<IRawFile> rawfiles) {
        super();
        this.rawfiles = rawfiles;
        mapChromatogramForRawFile = new HashMap();
        mapColorByRawFilename = new HashMap();
        for (IRawFile rawFile : rawfiles) {
            mapChromatogramForRawFile.put(rawFile, null);
            mapColorByRawFilename.put(rawFile.getName(), null);
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
        if (chromatogramPanel.getCurrentChromatogram() != null){
            return getRawFile(chromatogramPanel.getCurrentChromatogram().rawFilename);
        }
        return null;
    }

    @Override
    public void extractAndDisplayChromatogram(final Ms1ExtractionRequest params, DisplayMode mode, MzScopeCallback callback) {
       // in this implementation displayMode is ignored : always REPLACE since we will extract one Chromatogram per RawFile
        SwingWorker worker = new SwingWorker<Integer, Chromatogram>() {
            int count = 0;
            @Override
            protected Integer doInBackground() throws Exception {

                for (IRawFile rawFile : rawfiles) {
                    Chromatogram c = rawFile.getXIC(params);
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
                    displayChromatogram(c, DisplayMode.REPLACE);
                } else {
                    logger.info("add additionnal chromato");
                    displayChromatogram(c, DisplayMode.OVERLAY);
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
    public Color displayChromatogram(Chromatogram chromato, DisplayMode mode) {
       setMsMsEventButtonEnabled(true);
       Color plotColor = super.displayChromatogram(chromato, mode);
       mapColorByRawFilename.put(chromato.rawFilename, plotColor);
       return plotColor ;
    }
    
    @Override
    protected void displayTIC() {
        final List<IRawFile> rawFiles = new ArrayList<>(rawfiles);
        if (rawFileLoading != null){
            rawFileLoading.setWaitingState(true);
        }
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
                    displayChromatogram(chunks.get(0), DisplayMode.REPLACE);
                    k = 1;
                }
                for (; k < chunks.size(); k++) {
                    logger.info("add additionnal chromato");
                    displayChromatogram(chunks.get(k), DisplayMode.OVERLAY);
                }
                setMsMsEventButtonEnabled(false);
            }

            @Override
            protected void done() {
                try {
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
    public void displayFeature(final IFeature f) {
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
                    Ms1ExtractionRequest params = Ms1ExtractionRequest.builder().setMaxMz(maxMz).setMinMz(minMz).build();
                    Chromatogram c = rawFile.getXIC(params);
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
                    displayChromatogram(chunks.get(0), DisplayMode.REPLACE);
                    k = 1;
                    displayScan(getCurrentRawfile().getSpectrumId(f.getElutionTime()));
                    chromatogramPanel.displayFeature(f);
                }
                for (; k < chunks.size(); k++) {
                    logger.info("add additionnal chromato");
                    displayChromatogram(chunks.get(k), DisplayMode.OVERLAY);
                    displayScan(getCurrentRawfile().getSpectrumId(f.getElutionTime()));
                    chromatogramPanel.displayFeature(f);
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
                    displayChromatogram(chunks.get(0), DisplayMode.REPLACE);
                    k = 1;
                }
                for (; k < chunks.size(); k++) {
                    logger.info("add additionnal chromato");
                    displayChromatogram(chunks.get(k), DisplayMode.OVERLAY);
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
                    chromatogramPanel.setCurrentChromatogram(mapChromatogramForRawFile.get(rawFile));
                    if(chromatogramPanel.getCurrentScanTime() != null) {
                        int scanIdx = rawFile.getSpectrumId(chromatogramPanel.getCurrentScanTime());
                        displayScan(scanIdx);
                    }
                        hideMSMSEvents();
                        showMSMSEvents();
                }
            }
        };
        for (IRawFile rawFile : rawfiles) {
            JRadioButtonMenuItem mi = new JRadioButtonMenuItem(rawFile.getName());
            mi.addActionListener(changeCurrentChromatogramAction);
            popupMenu.add(mi);
            bg.add(mi);
        }
        ((JRadioButtonMenuItem)popupMenu.getComponent(0)).setSelected(true);
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
    public Color getPlotColor(String rawFilename) {
        return mapColorByRawFilename.get(rawFilename);
    }

}
