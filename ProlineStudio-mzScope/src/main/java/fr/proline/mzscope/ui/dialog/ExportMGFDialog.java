/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.mzscope.ui.dialog;

import fr.profi.mzdb.io.writer.mgf.PrecursorMzComputation;
import fr.proline.mzscope.model.MzScopePreferences;
import fr.proline.studio.export.ExportDialog;
import fr.proline.studio.export.ExporterFactory;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.utils.IconManager;
import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * export a rawFile as MGF file
 *
 * @author MB243701
 */
public class ExportMGFDialog extends DefaultDialog {

    private static ExportMGFDialog singletonMGFDialog = null;
    

    private JFileChooser fchooser;

    private JTextField fileTextField;
    private List<FileNameExtensionFilter> filterList = new ArrayList<>();
    private JComboBox exportTypeCombobox;
    private JTextField mzTolField;
    private JComboBox precursorCombobox ;
    private JTextField intensityCutoffField ;
    private JCheckBox cbExportProlineTitle;
    
    private Map<Integer, PrecursorMzComputation> mapPrecursor;
    private String[] precursorList;
    
    private DefaultDialog.ProgressTask m_task ;
    
    private float mzTolPPM  = 10.0f;
    private PrecursorMzComputation precComp = PrecursorMzComputation.MAIN_PRECURSOR_MZ;
    private float intensityCutoff = 0f;
    private boolean exportProlineTitle = false;
    private String mgfFileName;

    public static ExportMGFDialog getDialog(Window parent,  String title) {
        if (singletonMGFDialog == null) {
            singletonMGFDialog = new ExportMGFDialog(parent, title);
        }
        return singletonMGFDialog;
    }

    private ExportMGFDialog(Window parent, String title) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);
        setTitle("Export MGF "+title);
        setHelpURL("http://biodev.extra.cea.fr/docs/proline/doku.php?id=how_to:studio:mzscope");
        EnumSet<PrecursorMzComputation> precursorSet = EnumSet.allOf( PrecursorMzComputation.class );
        precursorList = new String[precursorSet.size()];
        mapPrecursor = new HashMap();
        int i=0;
        for (PrecursorMzComputation p: precursorSet){
            precursorList[i] = p.getUserParamName();
            mapPrecursor.put(i, p);
            i++;
        }
        
        setInternalComponent(createExportPanel());
        setButtonName(BUTTON_OK, "Export");

        fchooser = new JFileChooser();
        fchooser.setMultiSelectionEnabled(false);

    }
    

    private JPanel createExportPanel() {
        JPanel exportPanel = new JPanel(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        fileTextField = new JTextField(30);
        exportPanel.add(fileTextField, c);

        final JButton addFileButton = new JButton(IconManager.getIcon(IconManager.IconType.OPEN_FILE));
        addFileButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        addFileButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                ExporterFactory.ExporterInfo exporterInfo = (ExporterFactory.ExporterInfo) exportTypeCombobox.getSelectedItem();

                if (exporterInfo != null) {
                    FileNameExtensionFilter filter = new FileNameExtensionFilter(exporterInfo.getName(), exporterInfo.getFileExtension());
                    FileNameExtensionFilter existFilter = ExportDialog.getFilterWithSameExtensions(filter, filterList);

                    if (existFilter == null) {
                        fchooser.addChoosableFileFilter(filter);
                        filterList.add(filter);
                        fchooser.setFileFilter(filter);
                    } else {
                        fchooser.setFileFilter(existFilter);
                    }
                }

                String textFile = fileTextField.getText().trim();

                if (textFile.length() > 0) {
                    File currentFile = new File(textFile);
                    if (currentFile.isDirectory()) {
                        fchooser.setCurrentDirectory(currentFile);
                    } else {
                        fchooser.setSelectedFile(currentFile);
                    }
                }

                int result = fchooser.showOpenDialog(addFileButton);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File file = fchooser.getSelectedFile();

                    String absolutePath = file.getAbsolutePath();
                    String fileName = file.getName();
                    if (fileName.indexOf('.') == -1) {
                        absolutePath += "." + exporterInfo.getFileExtension();
                    }
                    fileTextField.setText(absolutePath);
                }
            }
        });

        c.gridx += 2;
        c.gridwidth = 1;
        exportPanel.add(addFileButton, c);

        exportTypeCombobox = new JComboBox(ExporterFactory.getList(ExporterFactory.EXPORT_MGF).toArray());
        exportTypeCombobox.setSelectedIndex(0);

        c.gridy++;
        c.gridx = 0;
        c.gridwidth = 1;
        exportPanel.add(new JLabel("Export Type:"), c);

        c.gridx++;
        c.gridwidth = 2;
        exportPanel.add(exportTypeCombobox, c);
        
        c.gridy++;
        c.gridx = 0;
        c.gridwidth = 1;
        exportPanel.add(new JLabel("m/z tolerance (ppm):"), c);
        
        c.gridx++;
        c.gridwidth = 2;
        mzTolField = new JTextField();
        mzTolField.setText(Float.toString(MzScopePreferences.getInstance().getMzPPMTolerance()));
        exportPanel.add(mzTolField, c);
        
        c.gridy++;
        c.gridx = 0;
        c.gridwidth = 1;
        exportPanel.add(new JLabel("Precursor m/z computation:"), c);
        c.gridx++;
        c.gridwidth = 2;
        
        precursorCombobox = new JComboBox(precursorList);
        precursorCombobox.setSelectedIndex(1); //MAIN_PRECURSOR_MZ by default
        exportPanel.add(precursorCombobox, c);
        
        c.gridy++;
        c.gridx = 0;
        c.gridwidth = 1;
        exportPanel.add(new JLabel("Intensity Cutoff:"), c);
        
        c.gridx++;
        c.gridwidth = 2;
        intensityCutoffField = new JTextField();
        intensityCutoffField.setText(new Float(0.0).toString());
        exportPanel.add(intensityCutoffField, c);
        
        c.gridy++;
        c.gridx = 0;
        c.gridwidth = 3;
        cbExportProlineTitle = new JCheckBox("Export Proline Title");
        cbExportProlineTitle.setSelected(false);
        exportPanel.add(cbExportProlineTitle, c);

        return exportPanel;
    }
    
    public void setTask(DefaultDialog.ProgressTask task) {
        m_task = task;
    }
    
    @Override
    protected boolean cancelCalled() {
        return true;
    }
    
    @Override
    protected boolean okCalled() {
        String fileName = fileTextField.getText().trim();

        if (fileName.length() == 0) {
            setStatus(true, "You must fill the file name.");
            highlight(fileTextField);
            return false;
        }

        File mgfFile = new File(fileName);
        
        if (mgfFile.exists()) {
            String message = "The file already exists. Do you want to overwrite it ?";
            String title = "Overwrite ?";
            String[] options = {"Yes","No"};
            int reply = JOptionPane.showOptionDialog(this, message, title, JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, "Yes");
            if (reply != JOptionPane.YES_OPTION) {
                setStatus(true, "File already exists.");
                return false;
            }
        }
        
        FileWriter fw = null;
        try {
            fw = new FileWriter(mgfFile);
            fw.write("t");
        } catch (Exception e) {
            setStatus(true, fileName + " is not writable.");
            highlight(fileTextField);
            return false;
        } finally {
            try {
                if (fw != null) {
                    fw.close();
                }

                mgfFile.delete();
            } catch (Exception e2) {
            }
        }
        
        // checkParameters
        
        mgfFileName = fileName;
        try {
            mzTolPPM = Float.parseFloat(mzTolField.getText());
        } catch (NumberFormatException e) {
            highlight(mzTolField);
            return false;
        }
        try{
            precComp = mapPrecursor.get(precursorCombobox.getSelectedIndex());
        }catch(Exception e){
            highlight(precursorCombobox);
            return false;
        }
        try {
            intensityCutoff = Float.parseFloat(intensityCutoffField.getText());
        } catch (NumberFormatException e) {
            highlight(intensityCutoffField);
            return false;
        }
        exportProlineTitle = cbExportProlineTitle.isSelected();
        
        
    
        startTask(m_task);
        return false;
    }

    public float getMzTolPPM() {
        return mzTolPPM;
    }

    public PrecursorMzComputation getPrecComp() {
        return precComp;
    }

    public float getIntensityCutoff() {
        return intensityCutoff;
    }

    public boolean isExportProlineTitle() {
        return exportProlineTitle;
    }

    public String getMgfFileName() {
        return mgfFileName;
    }

    
    
}
