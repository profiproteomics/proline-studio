/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.mzscope.ui;

import fr.proline.mzscope.model.IRawFile;
import fr.proline.mzscope.ui.event.RawFileListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.EventListenerList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * list which contains the mzdb filenames
 *
 * @author MB243701
 */
public class RawFilesPanel extends JPanel {

    final private static Logger logger = LoggerFactory.getLogger(RawFilesPanel.class);

    private RawFileListModel rawFilesListModel;
    private JList rawFilesList;
    private JPopupMenu popupMenu;
    private JScrollPane scrollPane;
    private JLabel openedRawFilesLabel;
    private JMenuItem viewRawFileMI;
    private JMenuItem openRawFileMI;
    private JMenuItem closeRawFileMI;
    private JMenuItem closeAllFileMI;
    private JMenuItem extractFeaturesMI;
    private JMenuItem detectPeakelsMI;
    private JMenuItem exportChromatogramMI;
    private EventListenerList listenerList = new EventListenerList();

    /**
     * Creates new form RawFilesPanel
     */
    public RawFilesPanel() {
        initComponents();
        rawFilesListModel = (RawFileListModel) rawFilesList.getModel();
    }

    private void initComponents() {
        openedRawFilesLabel = new JLabel();
        scrollPane = new JScrollPane();
        rawFilesList = new JList();

        openedRawFilesLabel.setText("Raw files");

        rawFilesList.setModel(new RawFileListModel());
        
        initPopupMenu();
        rawFilesList.setComponentPopupMenu(popupMenu);
        rawFilesList.setLayoutOrientation(javax.swing.JList.HORIZONTAL_WRAP);
        rawFilesList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                rawFilesListMouseClicked(evt);
            }

            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                rawFilesListMousePressed(evt);
            }
        });
        scrollPane.setViewportView(rawFilesList);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                        .addGap(5, 5, 5)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(layout.createSequentialGroup()
                                        .addComponent(scrollPane)
                                        .addGap(5, 5, 5))
                                .addGroup(layout.createSequentialGroup()
                                        .addComponent(openedRawFilesLabel)
                                        .addContainerGap(156, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                        .addGap(5, 5, 5)
                        .addComponent(openedRawFilesLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(5, 5, 5)
                        .addComponent(scrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 192, Short.MAX_VALUE)
                        .addGap(5, 5, 5))
        );

        openedRawFilesLabel.getAccessibleContext().setAccessibleName("Raw files");
    }

    private void rawFilesListMousePressed(MouseEvent evt) {
        updatePopupMenuEnabled();
        if (rawFilesList.getSelectedIndex() > -1 && SwingUtilities.isRightMouseButton(evt)) {
            popupMenu.show((JComponent) evt.getSource(), evt.getX(), evt.getY());
        }
    }

    private void updatePopupMenuEnabled() {
        int nbS = rawFilesList.getSelectedValuesList().size();
        boolean isRaw = rawFilesListModel.getFiles().length > 0;
        boolean atLeastOneRawSelected = isRaw && (nbS > 0);
        boolean oneRawSelected = isRaw && (nbS == 1);
        
        openRawFileMI.setEnabled(true);
        viewRawFileMI.setEnabled(atLeastOneRawSelected);
        closeRawFileMI.setEnabled(oneRawSelected);
        closeAllFileMI.setEnabled(isRaw);
        extractFeaturesMI.setEnabled(atLeastOneRawSelected);
        detectPeakelsMI.setEnabled(atLeastOneRawSelected);
        exportChromatogramMI.setEnabled(atLeastOneRawSelected);
    }
    
    
    private void initPopupMenu() {
        popupMenu = new JPopupMenu() ;
        // open raw file
        openRawFileMI = new JMenuItem();
        openRawFileMI.setText("Open Rawfile...");
        openRawFileMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));
        openRawFileMI.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                openRawFileMIActionPerformed(evt);
            }
        });
        popupMenu.add(openRawFileMI);
        popupMenu.addSeparator();
        // view data
        viewRawFileMI = new JMenuItem();
        viewRawFileMI.setText("View data");
        viewRawFileMI.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                viewRawFileMIActionPerformed(evt);
            }
        });
        popupMenu.add(viewRawFileMI);
        // close raw file
        closeRawFileMI = new JMenuItem();
        closeRawFileMI.setText("Close Rawfile");
        closeRawFileMI.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                closeRawFileMIActionPerformed(evt);
            }
        });
        popupMenu.add(closeRawFileMI);
        popupMenu.addSeparator();
        // close all files
        closeAllFileMI = new JMenuItem();
        closeAllFileMI.setText("Close All Rawfile...");
        closeAllFileMI.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                closeAllFileMIActionPerformed(evt);
            }
        });
        popupMenu.add(closeAllFileMI);
        popupMenu.addSeparator();
        // extract Features
        extractFeaturesMI = new JMenuItem();
        extractFeaturesMI.setText("Extract Features...");
        extractFeaturesMI.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                extractFeaturesMIActionPerformed(evt);
            }
        });
        popupMenu.add(extractFeaturesMI);
        // detect peakels
        detectPeakelsMI = new JMenuItem();
        detectPeakelsMI.setText("Detect Peakels...");
        detectPeakelsMI.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                detectPeakelsMIActionPerformed(evt);
            }
        });
        popupMenu.add(detectPeakelsMI);
        // export chromatogram
        exportChromatogramMI = new JMenuItem();
        exportChromatogramMI.setText("Export Chromatogram");
        exportChromatogramMI.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                exportChromatogramMIActionPerformed(evt);
            }
        });
        popupMenu.add(exportChromatogramMI);
    }

    private void rawFilesListMouseClicked(MouseEvent evt) {
        if (evt.getClickCount() == 2) {
            if (rawFilesList.getSelectedIndex() > -1) {
                fireDisplayRaw((IRawFile) rawFilesList.getSelectedValue());
            }
        }
    }

    private void viewRawFileMIActionPerformed(ActionEvent evt) {
        if (rawFilesList.getSelectedValuesList().size() > 1) {
            fireDisplayRaw(rawFilesList.getSelectedValuesList());
        } else {
            fireDisplayRaw((IRawFile) rawFilesList.getSelectedValue());
        }
    }

    private void openRawFileMIActionPerformed(ActionEvent evt) {
        fireOpenRawFile();
    }

    private void closeRawFileMIActionPerformed(ActionEvent evt) {
        if ((IRawFile) rawFilesList.getSelectedValue() != null) {
            fireCloseRawFile((IRawFile) rawFilesList.getSelectedValue());
        }
    }

    private void closeAllFileMIActionPerformed(ActionEvent evt) {
        fireCloseAllFiles();
    }

    private void extractFeaturesMIActionPerformed(ActionEvent evt) {
        if (rawFilesList.getSelectedValuesList().size() > 1) {
            fireExtractFeatures(rawFilesList.getSelectedValuesList());
        } else if ((IRawFile) rawFilesList.getSelectedValue() != null) {
            fireExtractFeatures((IRawFile) rawFilesList.getSelectedValue());
        }
    }

    private void detectPeakelsMIActionPerformed(ActionEvent evt) {
        if (rawFilesList.getSelectedValuesList().size() > 1) {
            fireDetectPeakels(rawFilesList.getSelectedValuesList());
        } else if ((IRawFile) rawFilesList.getSelectedValue() != null) {
            fireDetectPeakels((IRawFile) rawFilesList.getSelectedValue());
        }
    }

    private void exportChromatogramMIActionPerformed(ActionEvent evt) {
        if (rawFilesList.getSelectedValuesList().size() > 1) {
            fireExportChromatogram(rawFilesList.getSelectedValuesList());
        } else if ((IRawFile) rawFilesList.getSelectedValue() != null) {
            fireExportChromatogram((IRawFile) rawFilesList.getSelectedValue());
        }
    }

    public void addFile(IRawFile file) {
        rawFilesListModel.add(file);

    }

    public void removeFile(IRawFile file) {
        rawFilesListModel.removeFile(file);
    }
    
    public void removeAllFiles() {
        rawFilesListModel.removeAllFiles();
    }

    public void addRawFileListener(RawFileListener listener) {
        listenerList.add(RawFileListener.class, listener);
    }

    public void removeRawFileListener(RawFileListener listener) {
        listenerList.remove(RawFileListener.class, listener);
    }

    private void fireDisplayRaw(IRawFile rawfile) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = 0; i < listeners.length; i = i + 2) {
            if (listeners[i] == RawFileListener.class) {
                ((RawFileListener) listeners[i + 1]).displayRaw(rawfile);
            }
        }
    }

    private void fireDisplayRaw(List<IRawFile> rawfiles) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = 0; i < listeners.length; i = i + 2) {
            if (listeners[i] == RawFileListener.class) {
                ((RawFileListener) listeners[i + 1]).displayRaw(rawfiles);
            }
        }
    }

    private void fireOpenRawFile() {
        Object[] listeners = listenerList.getListenerList();
        for (int i = 0; i < listeners.length; i = i + 2) {
            if (listeners[i] == RawFileListener.class) {
                ((RawFileListener) listeners[i + 1]).openRawFile();
            }
        }
    }

    private void fireCloseRawFile(IRawFile rawFile) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = 0; i < listeners.length; i = i + 2) {
            if (listeners[i] == RawFileListener.class) {
                ((RawFileListener) listeners[i + 1]).closeRawFile(rawFile);
            }
        }
    }

    private void fireCloseAllFiles() {
        Object[] listeners = listenerList.getListenerList();
        for (int i = 0; i < listeners.length; i = i + 2) {
            if (listeners[i] == RawFileListener.class) {
                ((RawFileListener) listeners[i + 1]).closeAllFiles();
            }
        }
    }

    private void fireExtractFeatures(IRawFile rawfile) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = 0; i < listeners.length; i = i + 2) {
            if (listeners[i] == RawFileListener.class) {
                ((RawFileListener) listeners[i + 1]).extractFeatures(rawfile);
            }
        }
    }

    private void fireExtractFeatures(List<IRawFile> rawfiles) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = 0; i < listeners.length; i = i + 2) {
            if (listeners[i] == RawFileListener.class) {
                ((RawFileListener) listeners[i + 1]).extractFeatures(rawfiles);
            }
        }
    }

    private void fireDetectPeakels(IRawFile rawfile) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = 0; i < listeners.length; i = i + 2) {
            if (listeners[i] == RawFileListener.class) {
                ((RawFileListener) listeners[i + 1]).detectPeakels(rawfile);
            }
        }
    }

    private void fireDetectPeakels(List<IRawFile> rawfiles) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = 0; i < listeners.length; i = i + 2) {
            if (listeners[i] == RawFileListener.class) {
                ((RawFileListener) listeners[i + 1]).detectPeakels(rawfiles);
            }
        }
    }

    private void fireExportChromatogram(IRawFile rawfile) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = 0; i < listeners.length; i = i + 2) {
            if (listeners[i] == RawFileListener.class) {
                ((RawFileListener) listeners[i + 1]).exportChromatogram(rawfile);
            }
        }
    }

    private void fireExportChromatogram(List<IRawFile> rawfiles) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = 0; i < listeners.length; i = i + 2) {
            if (listeners[i] == RawFileListener.class) {
                ((RawFileListener) listeners[i + 1]).exportChromatogram(rawfiles);
            }
        }
    }
}
