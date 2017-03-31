package fr.proline.studio.rsmexplorer.gui;

import fr.proline.studio.dpm.serverfilesystem.RootInfo;
import fr.proline.studio.dpm.serverfilesystem.ServerFileSystemView;
import fr.proline.studio.export.ExporterFactory;
import fr.proline.studio.wizard.ConversionSettings;
import fr.proline.studio.wizard.ConvertionUploadBatch;
import fr.proline.studio.wizard.MzdbUploadBatch;
import fr.proline.studio.wizard.MzdbUploadSettings;
import java.awt.Color;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
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

                ArrayList<File> samples = new ArrayList<>();

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

                JTree.DropLocation treeDropLocation = (JTree.DropLocation) support.getDropLocation();
                TreePath treePath = treeDropLocation.getPath();

                String textTreePath = treePath.toString();
                
                textTreePath = textTreePath.replaceAll("\\[", "").replaceAll("\\]", "");

                String[] pathNodes = textTreePath.split(",");
                
                for(int i=0; i<pathNodes.length; i++){
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

                if (parentLabel == null || parentIndex==-1) {
                    return false;
                }
                
                StringBuilder destinationBuilder = new StringBuilder();
                
                for(int i=parentIndex+1; i<pathNodes.length; i++){
                    destinationBuilder.append(File.separator).append(pathNodes[i]);
                }
                destinationBuilder.append(File.separator);
                
                String destination = destinationBuilder.toString();

                MzdbUploadSettings uploadSettings = new MzdbUploadSettings(false, (boolean) false, parentLabel, destination);

                if (samples.get(0).getAbsolutePath().endsWith(".mzdb")) {

                    //Uploading Task
                    MzdbUploadBatch uploadBatch = new MzdbUploadBatch(samples, uploadSettings);
                    Thread thread = new Thread(uploadBatch);
                    thread.start();

                } else if (samples.get(0).getAbsolutePath().endsWith(".raw")) {

                    Preferences preferences = NbPreferences.root();

                    String convertersPath = preferences.get("mzDB_Settings.Converter_(.exe)", null);

                    if (convertersPath == null) {
                        return false;
                    }

                    ConversionSettings conversionSettings = new ConversionSettings(convertersPath, "/", false, true);

                    conversionSettings.setUploadSettings(uploadSettings);

                    ConvertionUploadBatch conversionBatch = new ConvertionUploadBatch(samples, conversionSettings);

                }

            } catch (UnsupportedFlavorException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }

        }

        return true;

    }

}
