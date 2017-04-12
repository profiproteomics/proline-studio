package fr.proline.studio.rsmexplorer.gui;

import fr.proline.studio.dpm.serverfilesystem.RootInfo;
import fr.proline.studio.dpm.serverfilesystem.ServerFileSystemView;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.gui.DefaultDialogListener;
import fr.proline.studio.rsmexplorer.gui.dialog.DefaultConverterDialog;
import fr.proline.studio.wizard.ConversionSettings;
import fr.proline.studio.wizard.ConvertionUploadBatch;
import fr.proline.studio.wizard.MzdbUploadBatch;
import fr.proline.studio.wizard.MzdbUploadSettings;
import java.awt.Color;
import java.awt.Frame;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.event.WindowListener;
import java.awt.event.WindowStateListener;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTree;

import javax.swing.TransferHandler;
import javax.swing.tree.TreePath;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;
import org.openide.windows.WindowManager;

/**
 * Class used for Drag and Drop of nodes
 *
 * @author JM235353
 */
public class TreeFileChooserTransferHandler extends TransferHandler {

    private HashMap<JComponent, JComponent> m_components;

    public TreeFileChooserTransferHandler() {
        m_components = new HashMap<JComponent, JComponent>();
    }

    public void addComponent(JComponent component) {
        m_components.put(component, component);
    }

    public void clearHighlights() {
        Iterator<JComponent> it = m_components.keySet().iterator();
        while (it.hasNext()) {
            JComponent key = it.next();
            key.setBackground(null);
        }
    }

    @Override
    public int getSourceActions(JComponent c) {
        return TransferHandler.COPY;
    }

    @Override
    protected Transferable createTransferable(JComponent c) {

        if (c instanceof TreeFileChooser) {
            TreeFileChooser tree = (TreeFileChooser) c;

            ArrayList<File> selectedFiles = tree.getSelectedFiles();
            if (selectedFiles.isEmpty()) {
                return null;
            }

            return new FilesTransferable(selectedFiles);

        }
        return null;
    }

    @Override
    protected void exportDone(JComponent source, Transferable data, int action) {
        clearHighlights();
    }

    @Override
    public boolean canImport(TransferHandler.TransferSupport support) {

        support.setShowDropLocation(true);

        if (support.isDataFlavorSupported(FilesTransferable.Files_FLAVOR)) {
            DropLocation dropLocation = support.getDropLocation();
            if (dropLocation instanceof JTable.DropLocation) {
                return true;
            } else if (m_components.containsKey((JComponent) support.getComponent())) {
                m_components.get((JComponent) support.getComponent()).setBackground(Color.WHITE);
                return true;
            } else if (dropLocation instanceof JTree.DropLocation) {
                return true;
            }
        }

        clearHighlights();
        return false;
    }

    @Override
    public boolean importData(TransferHandler.TransferSupport support) {

        if (support.getComponent() instanceof JTable) {

            try {
                JTable table = (JTable) support.getComponent();
                JTable.DropLocation dl = (JTable.DropLocation) support.getDropLocation();

                int index = table.convertRowIndexToModel(dl.getRow());

                int max = table.getModel().getRowCount();
                if (index < 0 || index > max) {
                    return false;
                }

                FilesTransferable transferable = (FilesTransferable) support.getTransferable().getTransferData(FilesTransferable.Files_FLAVOR);
                ArrayList<File> transferredFiles = transferable.getFiles();

                ArrayList<Integer> indices = new ArrayList<>();
                for (int i = 0; i < transferredFiles.size(); i++) {
                    indices.add(index + i);
                }

                TreeFileChooserTableModelInterface model = (TreeFileChooserTableModelInterface) table.getModel();

                if (!model.canSetFiles(indices)) {
                    JOptionPane.showMessageDialog(null, "An mzDB file is already linked to this dataset. Operation is cancelled.", "Warning", JOptionPane.WARNING_MESSAGE);
                    return false;
                }

                if (model.shouldConfirmCorruptFiles(indices)) {
                    int reply = JOptionPane.showConfirmDialog(null, "Previous quantitation has been run with existing mzDB file.\n Are you sure you want to overwrite this link.", "Warning", JOptionPane.YES_NO_OPTION);
                    if (reply == JOptionPane.NO_OPTION) {
                        return false;
                    }
                }

                model.setFiles(transferredFiles, index);

            } catch (Exception e) {
                //should not happen
                return false;
            }
        } else if (support.getComponent() instanceof DropZoneInterface) {
            try {

                DropZoneInterface dropZone = (DropZoneInterface) support.getComponent();

                FilesTransferable transferable = (FilesTransferable) support.getTransferable().getTransferData(FilesTransferable.Files_FLAVOR);
                ArrayList<File> transferredFiles = transferable.getFiles();

                ArrayList<File> samples = new ArrayList<File>();

                for (int i = 0; i < transferredFiles.size(); i++) {

                    if (transferredFiles.get(i).isFile()) {
                        samples.add(transferredFiles.get(i));
                    } else {
                        File[] listOfFiles = transferredFiles.get(i).listFiles();
                        for (File file : listOfFiles) {
                            if (file.isFile()) {
                                samples.add(file);
                            }
                        }

                    }
                }

                dropZone.addSamples(samples);

            } catch (Exception e) {
                //should not happen
                return false;
            }

            return false;
        } else if (support.getComponent() instanceof TreeFileChooser) {

            try {

                FilesTransferable transferable = (FilesTransferable) support.getTransferable().getTransferData(FilesTransferable.Files_FLAVOR);
                ArrayList<File> transferredFiles = transferable.getFiles();

                JTree.DropLocation treeDropLocation = (JTree.DropLocation) support.getDropLocation();
                TreePath treePath = treeDropLocation.getPath();

                String textTreePath = treePath.toString();

                textTreePath = textTreePath.replaceAll("\\[", "").replaceAll("\\]", "");

                String[] pathNodes = textTreePath.split(",");

                for (int i = 0; i < pathNodes.length; i++) {
                    pathNodes[i] = pathNodes[i].trim();
                }

                ArrayList<String> labels = ServerFileSystemView.getServerFileSystemView().getLabels(RootInfo.TYPE_MZDB_FILES);
                String parentLabel = null;
                int parentIndex = -1;

                for (int i = 0; i < pathNodes.length; i++) {

                    for (int j = 0; j < labels.size(); j++) {
                        String node = pathNodes[i];
                        String label = labels.get(j);

                        if (node.compareToIgnoreCase(label) == 0) {
                            parentLabel = pathNodes[i];
                            parentIndex = i;
                            break;
                        }
                    }

                }

                if (parentLabel == null || parentIndex == -1) {
                    return false;
                }

                StringBuilder destinationBuilder = new StringBuilder();

                for (int i = parentIndex + 1; i < pathNodes.length; i++) {
                    destinationBuilder.append(File.separator).append(pathNodes[i]);
                }
                destinationBuilder.append(File.separator);

                String destination = destinationBuilder.toString();

                //Here we must do a small modification so that mzdb is deleted if it is a result of a conversion drag!
                MzdbUploadSettings uploadSettings = new MzdbUploadSettings(!transferredFiles.get(0).getAbsolutePath().endsWith(".mzdb"), (boolean) false, parentLabel, destination);

                if (transferredFiles.get(0).getAbsolutePath().endsWith(".mzdb")) {

                    //Here prepare mzdb samples HashMap!
                    HashMap<File, MzdbUploadSettings> uploadSamples = new HashMap<File, MzdbUploadSettings>();
                    for (int i = 0; i < transferredFiles.size(); i++) {
                        if (transferredFiles.get(i).isFile()) {
                            uploadSamples.put(transferredFiles.get(i), uploadSettings);
                        } else {
                            File[] listOfFiles = transferredFiles.get(i).listFiles();
                            for (File file : listOfFiles) {
                                if (file.isFile()) {
                                    uploadSamples.put(file, uploadSettings);
                                }
                            }
                        }
                    }

                    //Uploading Task
                    MzdbUploadBatch uploadBatch = new MzdbUploadBatch(uploadSamples);
                    Thread thread = new Thread(uploadBatch);
                    thread.start();

                } else if (transferredFiles.get(0).getAbsolutePath().endsWith(".raw")) {

                    Preferences preferences = NbPreferences.root();

                    String converterPath = preferences.get("Conversion/Upload_Settings.Converter_(.exe)", null);

                    if (converterPath == null) {
                        Frame f = WindowManager.getDefault().getMainWindow();

                        DefaultConverterDialog dialog = DefaultConverterDialog.getDialog(f);

                        dialog.addDefaultDialogListener(new DefaultDialogListener() {

                            @Override
                            public void okPerformed(DefaultDialog d) {
                                
                                launchConversion(transferredFiles, preferences.get("Conversion/Upload_Settings.Converter_(.exe)", null), uploadSettings);

                            }

                            @Override
                            public void cancelPerformed(DefaultDialog d) {
                                ;
                            }

                            @Override
                            public void defaultPerformed(DefaultDialog d) {
                                ;
                            }

                            @Override
                            public void backPerformed(DefaultDialog d) {
                                ;
                            }

                            @Override
                            public void savePerformed(DefaultDialog d) {
                                ;
                            }

                            @Override
                            public void loadPerformed(DefaultDialog d) {
                                ;
                            }

                        });

                        dialog.setLocationRelativeTo(f);
                        dialog.setVisible(true);

                    } else {     
                        launchConversion(transferredFiles, converterPath, uploadSettings);
                    }

                }

            } catch (UnsupportedFlavorException | IOException ex) {
                Exceptions.printStackTrace(ex);
            }

        }

        return true;

    }

    private void launchConversion(ArrayList<File> transferredFiles, String converterPath, MzdbUploadSettings uploadSettings) {
        //Here prepare raw samples HashMap!
        HashMap<File, ConversionSettings> conversionSamples = new HashMap<File, ConversionSettings>();
        for (int i = 0; i < transferredFiles.size(); i++) {

            ConversionSettings conversionSettings = new ConversionSettings(converterPath, transferredFiles.get(i).getParent(), false, true);
            conversionSettings.setUploadSettings(uploadSettings);

            if (transferredFiles.get(i).isFile()) {
                conversionSamples.put(transferredFiles.get(i), conversionSettings);
            } else {
                File[] listOfFiles = transferredFiles.get(i).listFiles();
                for (File file : listOfFiles) {
                    if (file.isFile()) {
                        conversionSamples.put(file, conversionSettings);
                    }
                }
            }
        }

        ConvertionUploadBatch conversionBatch = new ConvertionUploadBatch(conversionSamples);
        Thread thread = new Thread(conversionBatch);
        thread.start();
    }

}
