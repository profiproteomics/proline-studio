package fr.proline.studio.export;


import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.utils.IconManager;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.prefs.Preferences;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.jdesktop.swingx.JXTable;
import org.openide.util.NbPreferences;
import org.slf4j.LoggerFactory;


/**
 *
 * @author JM235353
 */
public class ExportDialog extends DefaultDialog {



	private static ExportDialog m_singletonImageDialog = null;
	private static ExportDialog m_singletonImage2Dialog = null;
    private static ExportDialog m_singletonExcelDialog = null;
    private static ExportDialog m_singletonServerDialog = null;
 
    private int m_exportType;
    
    private JTextField m_fileTextField;
    private JComboBox m_exporTypeCombobox;

    private JXTable m_table = null;
    private JPanel m_panel = null;
    private ExportPictureWrapper m_svgFileWrapper = null;
    
    private DefaultDialog.ProgressTask m_task = null;
    
    private String m_exportName = null;
    
    public static ExportDialog getDialog(Window parent, JXTable table, String exportName) {
        if (m_singletonExcelDialog == null) {
            m_singletonExcelDialog = new ExportDialog(parent, ExporterFactory.EXPORT_TABLE);
        }

        m_singletonExcelDialog.m_table = table;
        m_singletonExcelDialog.m_exportName = exportName;
        
        return m_singletonExcelDialog;
    }
    
  
    
    public static ExportDialog getDialog(Window parent, JPanel panel,  String exportName) {
        if (m_singletonImageDialog == null) {
            m_singletonImageDialog = new ExportDialog(parent, ExporterFactory.EXPORT_IMAGE);
        }

        m_singletonImageDialog.m_panel = panel;
        m_singletonImageDialog.m_svgFileWrapper = null;
        m_singletonImageDialog.m_exportName = exportName;

        return m_singletonImageDialog;
    }
    
    public static ExportDialog getDialog(Window parent, JPanel panel, ExportPictureWrapper svgFileWrapper, String exportName) {
        if (m_singletonImage2Dialog == null) {
            m_singletonImage2Dialog = new ExportDialog(parent, ExporterFactory.EXPORT_IMAGE2);
        }

        m_singletonImage2Dialog.m_panel = panel;
        m_singletonImage2Dialog.m_svgFileWrapper = svgFileWrapper;
        m_singletonImage2Dialog.m_exportName = exportName;

        return m_singletonImage2Dialog;
    }
    
    
    public static ExportDialog getDialog(Window parent) {
        if (m_singletonServerDialog == null) {
            m_singletonServerDialog = new ExportDialog(parent, ExporterFactory.EXPORT_FROM_SERVER);
        }

        

        return m_singletonServerDialog;
    }

    public void setTask(DefaultDialog.ProgressTask task) {
        m_task = task;
    }
    
    
    private ExportDialog(Window parent, int type) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);

        m_exportType = type;
        
        setTitle("Export");

        setHelpURL("http://biodev.extra.cea.fr/docs/proline/doku.php?id=how_to:studio:exportdata");

        setInternalComponent(createExportPanel());

        setButtonVisible(BUTTON_DEFAULT, false);
        
        
        setButtonName(BUTTON_OK, ((m_exportType == ExporterFactory.EXPORT_IMAGE)
        					   || (m_exportType == ExporterFactory.EXPORT_IMAGE2)) ? "Export Image" : "Export");

        Preferences preferences = NbPreferences.root();
        String defaultExportPath;
        
        if ((m_exportType == ExporterFactory.EXPORT_TABLE) || (m_exportType == ExporterFactory.EXPORT_FROM_SERVER)) {
           defaultExportPath = preferences.get("DefaultExcelExportPath", "");
        } else { // IMAGE
           defaultExportPath = preferences.get("DefaultImageExportPath", "");
        }
        
        m_fileTextField.setText(defaultExportPath);
    }

    public final JPanel createExportPanel() {

        JPanel exportPanel = new JPanel(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        m_fileTextField = new JTextField(30);
        exportPanel.add(m_fileTextField, c);



        final JButton addFileButton = new JButton(IconManager.getIcon(IconManager.IconType.OPEN_FILE));
        addFileButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        addFileButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                JFileChooser fchooser = new JFileChooser();

                fchooser.setMultiSelectionEnabled(false);

                ExporterFactory.ExporterInfo exporterInfo = (ExporterFactory.ExporterInfo) m_exporTypeCombobox.getSelectedItem();
                
                if (exporterInfo != null) {
                    FileNameExtensionFilter filter = new FileNameExtensionFilter(exporterInfo.getName(), exporterInfo.getFileExtension());
                    fchooser.addChoosableFileFilter(filter);
                    fchooser.setFileFilter(filter);
                }

                File currentFile = new File(m_fileTextField.getText().trim());

                if (currentFile != null) {
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
                        absolutePath += "."+exporterInfo.getFileExtension();
                    }
                    m_fileTextField.setText(absolutePath);
                }
            }
        });

        c.gridx+=2;
        c.gridwidth = 1;
        exportPanel.add(addFileButton, c);

        m_exporTypeCombobox = new JComboBox(ExporterFactory.getList(m_exportType).toArray());
        m_exporTypeCombobox.setSelectedIndex(0);


        c.gridy++;
        c.gridx = 0;
        exportPanel.add(new JLabel("Export Type:"), c);

        c.gridx++;
        c.gridwidth = 2;
        exportPanel.add(m_exporTypeCombobox, c);

        return exportPanel;
    }

    public String getFileName() {
        return m_fileTextField.getText().trim();
    }
    
    public ExporterFactory.ExporterInfo getExporterInfo() {
        return (ExporterFactory.ExporterInfo) m_exporTypeCombobox.getSelectedItem();
    }
    
    @Override
    protected boolean okCalled() {

        String fileName = m_fileTextField.getText().trim();

        if (fileName.length() == 0) {
            setStatus(true, "You must fill the file name.");
            highlight(m_fileTextField);
            return false;
        }

        File f = new File(fileName);
        FileWriter fw = null;
        try {
            fw = new FileWriter(f);
            fw.write("t");
        } catch (Exception e) {
            setStatus(true, fileName + " is not writable.");
            highlight(m_fileTextField);
            return false;
        } finally {
            try {
                if (fw != null) {
                    fw.close();
                }

                f.delete();
            } catch (Exception e2) {
            }
        }

        if (m_exportType == ExporterFactory.EXPORT_TABLE) {

            final ExporterFactory.ExporterInfo exporterInfo = getExporterInfo();

            ExportManager exporter = new ExportManager(m_table);
            ProgressTask exportTask = exporter.getTask(exporterInfo.getExporter(), m_exportName, fileName);

            startTask(exportTask);

            Preferences preferences = NbPreferences.root();
            preferences.put("DefaultExcelExportPath", f.getAbsoluteFile().getParentFile().getName());
        } else if (m_exportType == ExporterFactory.EXPORT_FROM_SERVER) {

            startTask(m_singletonServerDialog.m_task);
            
            Preferences preferences = NbPreferences.root();
            preferences.put("DefaultExcelExportPath", f.getAbsoluteFile().getParentFile().getName());
            
        } else if (m_exportType == ExporterFactory.EXPORT_IMAGE2
                ||  (m_exportType == ExporterFactory.EXPORT_IMAGE)) {
        	BufferedImage bi = null;
        	Graphics g = null; 
        	if(m_panel != null) { // then we treat as bitmap output only 
	            bi = new BufferedImage(m_panel.getSize().width, m_panel.getSize().height, BufferedImage.TYPE_INT_ARGB);
	            g = bi.createGraphics();
	            m_panel.paint(g);
	            g.dispose();
        	}
        	else
        	{
        		
        	}
            try {
            	ExporterFactory.ExporterInfo exporterInfo = (ExporterFactory.ExporterInfo) m_exporTypeCombobox.getSelectedItem();
    	
    			if(exporterInfo.getFileExtension().contains("png")) {
    				LoggerFactory.getLogger("ProlineStudio.ResultExplorer").info("exporting png file...to:" + f.toPath().toString());
    	            if(exporterInfo.getName().contains("3000x2000")) {
    					Files.copy(m_svgFileWrapper.m_graphicFile2.toPath() , new File(fileName).toPath());
    				} 
    				else {
	    				ImageIO.write(bi,"png",new File(fileName));
    				}
    				
    			} else if(exporterInfo.getName().contains("jfreesvg")) {
    					LoggerFactory.getLogger("ProlineStudio.ResultExplorer").info("exporting jfresvg file...to:" + f.toPath().toString());
    					Files.copy(m_svgFileWrapper.m_graphicFile.toPath(), f.toPath());
    			
    			} else if(exporterInfo.getName().contains("batik")) { // svg output
    				
    					LoggerFactory.getLogger("ProlineStudio.ResultExplorer").info("exporting svg file...to:" + f.toPath().toString());
        	            Files.copy(m_svgFileWrapper.m_graphicFile3.toPath(), f.toPath());
    			}	
    			
            	
            	
               
            } catch (Exception e) {
            	LoggerFactory.getLogger("ProlineStudio.ResultExplorer").error("error " + e.getMessage() + " while exporting picture");
            }
            
            Preferences preferences = NbPreferences.root();
            preferences.put("DefaultExcelImagePath", f.getAbsoluteFile().getParentFile().getName());
            setVisible(false);
        } 
        
        
        return false;

    }
    
    @Override
    public void setVisible(boolean v) {
        
        if (!v) {
            m_table = null;
            m_panel = null;
            m_exportName= null;
        }
        super.setVisible(v);
    }
    
        
    @Override
    protected boolean cancelCalled() {


        return true;
    }
}
