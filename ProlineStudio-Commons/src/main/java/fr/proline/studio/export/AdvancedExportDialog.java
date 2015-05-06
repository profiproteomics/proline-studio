package fr.proline.studio.export;

import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.utils.IconManager;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.prefs.Preferences;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

import org.jdesktop.swingx.JXTable;
import org.openide.util.NbPreferences;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


/**
 *
 * @author AW
 * 
 */
public class AdvancedExportDialog extends DefaultDialog  {


    private static AdvancedExportDialog m_singletonImageDialog = null;
    private static AdvancedExportDialog m_singletonImage2Dialog = null;
    private static AdvancedExportDialog m_singletonExcelDialog = null;
    private static AdvancedExportDialog m_singletonServerDialog = null;
 
    private int m_exportType;
    
    private JTextField m_fileTextField;
    private JComboBox m_exporTypeCombobox;
    private JCheckBox m_exportAllPSMsChB;
    private Boolean m_showExportAllPSMsChB;

    private JXTable m_table = null;
    private JPanel m_panel = null;
    private ImageExporterInterface m_imageExporter = null;
    
    private JFileChooser m_fchooser;
    private List<FileNameExtensionFilter> m_filterList = new ArrayList<>();
    
    private DefaultDialog.ProgressTask m_task = null;
    
    private String m_exportName = null;
    
    
    // created by AW:
	private JTabbedPane tabbedPane;
	private JPanel panel;
	private JLabel lblExportExcelTabs;
	private JTable table;
	private JScrollPane scrollPane;
	private JLabel lblOrientation;
	private JTextField m_configFile;
	private JLabel lblFormat;
	private JLabel lblDateFormat;
	private JLabel lblNumberSeparator;
	private JLabel lblProteinSets;
	private JButton btnNewButton;
	private JButton btnLoad;
	//---
	private JComboBox comboBox_ProteinSets;
	private JComboBox comboBox_DateFormat;
	private JComboBox comboBox_Format;
	private JComboBox comboBox_NumberSeparator;
	private JComboBox comboBox_Orientation;
	//private JButton addFileButton;
	private JLabel lblExportToFile;
	private JCheckBox chk_ExportOptions;
	private ExportConfig m_exportConfig;
	private ExportConfig m_exportDefaultConfig;
	private JLabel lbl_exportType;
    
    public static AdvancedExportDialog getDialog(Window parent, JXTable table, String exportName) {
        if (m_singletonExcelDialog == null) {
            m_singletonExcelDialog = new AdvancedExportDialog(parent, ExporterFactory.EXPORT_TABLE);
        }

        m_singletonExcelDialog.m_table = table;
        m_singletonExcelDialog.m_exportName = exportName;
        
        return m_singletonExcelDialog;
    }
    
  
    
    public static AdvancedExportDialog getDialog(Window parent, JPanel panel,  String exportName) {
        if (m_singletonImageDialog == null) {
            m_singletonImageDialog = new AdvancedExportDialog(parent, ExporterFactory.EXPORT_IMAGE);
        }

        m_singletonImageDialog.m_panel = panel;
        m_singletonImageDialog.m_imageExporter = null;
        m_singletonImageDialog.m_exportName = exportName;

        return m_singletonImageDialog;
    }
    
    public static AdvancedExportDialog getDialog(Window parent, JPanel panel, ImageExporterInterface imageExporter, String exportName) {
        if (m_singletonImage2Dialog == null) {
            m_singletonImage2Dialog = new AdvancedExportDialog(parent, ExporterFactory.EXPORT_IMAGE2);
        }

        m_singletonImage2Dialog.m_panel = panel;
        m_singletonImage2Dialog.m_imageExporter = imageExporter;
        m_singletonImage2Dialog.m_exportName = exportName;

        return m_singletonImage2Dialog;
    }
    
    
    public static AdvancedExportDialog getDialog(Window parent, Boolean showExportAllPSMsOption) {
        if (m_singletonServerDialog == null) {
            m_singletonServerDialog = new AdvancedExportDialog(parent, ExporterFactory.EXPORT_FROM_SERVER, showExportAllPSMsOption);
        } else if(!m_singletonServerDialog.m_showExportAllPSMsChB.equals(showExportAllPSMsOption)){
            m_singletonServerDialog = new AdvancedExportDialog(parent, ExporterFactory.EXPORT_FROM_SERVER, showExportAllPSMsOption);
        }

        return m_singletonServerDialog;
    }
    
    public static AdvancedExportDialog getDialog(Window parent, Boolean showExportAllPSMsOption, int exportType) {
        if (m_singletonServerDialog == null) {
            m_singletonServerDialog = new AdvancedExportDialog(parent, exportType, showExportAllPSMsOption);
        } else if(!m_singletonServerDialog.m_showExportAllPSMsChB.equals(showExportAllPSMsOption)){
            m_singletonServerDialog = new AdvancedExportDialog(parent, exportType, showExportAllPSMsOption);
        }

        return m_singletonServerDialog;
    }

    public void setTask(DefaultDialog.ProgressTask task) {
        m_task = task;
    }
    
    
    private AdvancedExportDialog(Window parent, int type) {
     this(parent, type, null);   
    }
            
    private AdvancedExportDialog(Window parent, int type, Boolean showExportAllPSMsOption) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);
        m_showExportAllPSMsChB = showExportAllPSMsOption;
        m_exportType = type;
        
//        setTitle("Export");

        setHelpURL("http://biodev.extra.cea.fr/docs/proline/doku.php?id=how_to:studio:exportdata");

    
	
        setInternalComponent(createAdvancedExportPanel());
        loadDefaultExportConfig();
		//loadExportConfig();// is loaded upon request
		if(m_exportDefaultConfig!=null )
		{
			fillExportFormatTable(m_exportDefaultConfig, m_exportConfig);
		}
		
       
        
        setButtonName(BUTTON_OK, ((m_exportType == ExporterFactory.EXPORT_IMAGE)
        					   || (m_exportType == ExporterFactory.EXPORT_IMAGE2)) ? "Export Image" : "Export");

        
        
        String defaultExportPath;
        Preferences preferences = NbPreferences.root();
        if ((m_exportType == ExporterFactory.EXPORT_TABLE) || (m_exportType == ExporterFactory.EXPORT_FROM_SERVER) || (m_exportType == ExporterFactory.EXPORT_XIC)) {
           defaultExportPath = preferences.get("DefaultExcelExportPath", "");
        } else { // IMAGE
           defaultExportPath = preferences.get("DefaultImageExportPath", "");
        }
        if (defaultExportPath.length()>0) {
            m_fchooser = new JFileChooser(new File(defaultExportPath));
        } else {
            m_fchooser = new JFileChooser();
        }
        m_fchooser.setMultiSelectionEnabled(false);
        

    }
    
    

	private void loadDefaultExportConfig() {
		   m_exportDefaultConfig = new ExportConfig();
			String jsonString = "";
			try {
				//GetExportInformationTask
				
				jsonString = new String(Files.readAllBytes(Paths.get("D:\\Proline\\export perso\\allFieldsIdent.json")));
			} catch (IOException e) {
				
				e.printStackTrace();
			}
			   
			
			Gson gson = new Gson();
			String messageHashMapJsonString = jsonString;
			m_exportDefaultConfig = gson.fromJson(messageHashMapJsonString, m_exportDefaultConfig.getClass());
			
			fillExportPossibleValues(m_exportDefaultConfig);
		}
	
  
	  
    private void loadExportConfig() {
    	 // decode json 
        m_exportConfig = new ExportConfig();
		String jsonString = "";
		Path filePath = Paths.get(m_configFile.getText());
		try {
			jsonString = new String(Files.readAllBytes(filePath));
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		   
		if(!filePath.equals("")) {
			Gson gson = new Gson();
			String messageHashMapJsonString = jsonString;
			m_exportConfig = gson.fromJson(messageHashMapJsonString, m_exportConfig.getClass());
			
		}
    	
    }
    

	private void fillExportPossibleValues(ExportConfig param) {
		if(param!=null) {
		
			
			if(param.sheet_presentation_values!=null) {
				comboBox_Orientation.setModel(new DefaultComboBoxModel(param.sheet_presentation_values));
				// TODO: add the right selection to this list of choices (also for the following 3 sections)
			}
			if(param.format_values!=null) {
				comboBox_Format.setModel(new DefaultComboBoxModel(param.format_values));
			}
			if(param.date_format_values!=null) {
				comboBox_DateFormat.setModel(new DefaultComboBoxModel(param.date_format_values));
			}

			if(param.decimal_separator_values!=null) {
				comboBox_NumberSeparator.setModel(new DefaultComboBoxModel(param.decimal_separator_values));
			}
			
			comboBox_ProteinSets.setModel(new DefaultComboBoxModel(new String[] { "all", "validated only"}));

		}
		
		
		
	}


	private void fillExportFormatTable(ExportConfig defaultParam, ExportConfig param) {
		//reset panes:
		
		tabbedPane.removeAll();	
		
		List<String> defaultSheetIdsList = new ArrayList<String>();
		List<String> sheetIdsList = new ArrayList<String>();
		if(param!=null) {
			for(int i=0; i<param.sheets.length;i++) {
				sheetIdsList.add(param.sheets[i].id);
			}
		}
		for(int i=0; i<defaultParam.sheets.length;i++) {
			defaultSheetIdsList.add(defaultParam.sheets[i].id);
		}
		
		
		// create tab panes
		for(int i = 0; i<defaultParam.sheets.length;i++) {
			panel = new JPanel();
			tabbedPane.addTab(defaultParam.sheets[i].title, null, panel, null);
			panel.setLayout(new BorderLayout(0, 0));
			//panel.setLayout(null);
			// read fields to fill in jtable into this tabbed pane
			
			scrollPane = new JScrollPane();
			//scrollPane.setBounds(0, 0, 699, 290);
			panel.add(scrollPane);
			
			table = new JTable();
			
			DefaultTableModel tableModel = new javax.swing.table.DefaultTableModel(
					
					new Object [][] {

		            }, 
		            new String[] {
							"Internal field name", "Displayed field name", "Shown"
						}
		        ) {
		            Class[] types = new Class [] {
		            		java.lang.String.class,java.lang.String.class,java.lang.Boolean.class
		            };
		            boolean[] canEdit = new boolean [] {
		                false,false, false
		            };
				@Override
				public Class getColumnClass(int columnIndex) {
					return types[columnIndex];
				}

				@Override
				public boolean isCellEditable(int rowIndex, int columnIndex) {
					return canEdit[columnIndex];
				}
			};
			
			scrollPane.setViewportView(table);
			
			// ---add ability to enable/disable individual tabs
			tabbedPane.setEnabledAt(i, true); // true by default
			tabbedPane.setToolTipTextAt(i, "Right click to Enable/Disable");
			tabbedPane.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent arg0) {
					
					if (SwingUtilities.isRightMouseButton(arg0) ) {
						boolean tabEnabled=  tabbedPane.isEnabledAt(tabbedPane.indexAtLocation(arg0.getX(), arg0.getY()));
						int tabIndex = tabbedPane.indexAtLocation(arg0.getX(),arg0.getY());
						tabbedPane.setEnabledAt(tabIndex, !tabEnabled);
						arg0.consume();
					} 
						
				}
			});
			
			
			
			
			// we want to find which field is at the same time default field and custom field. (to mark it).
			ExportExcelSheet paramSheet = null; 
			if(param!=null) {
				// convert [] into iterable:
				List<ExportExcelSheet> paramSheets_list = new ArrayList<ExportExcelSheet>(Arrays.asList(param.sheets));
				
				// find the corresponding sheet in custom param file
				paramSheet = null; 
				for(int k=0; k<paramSheets_list.size();k++) {
					//if(defaultSheetIdsList.contains(sheets_list.get(k).id)) {
					if(param.sheets[k].id.equals(defaultParam.sheets[i].id)) {
						paramSheet = param.sheets[k];
						tabbedPane.setEnabledAt(i, true);
					}
				}
			}
			
			for(int j=0; j< defaultParam.sheets[i].fields.length ; j++) {
				
				Vector v = new Vector();
				v.add(defaultParam.sheets[i].fields[j].id);
				v.add(defaultParam.sheets[i].fields[j].title);
				
				if(paramSheet!=null) { // if the sheet is present in both files
					if( isDefaultParamFieldContainedInCustomParamFieldsList(defaultParam.sheets[i].fields[j] , paramSheet.fields ) ) {
						v.add(true);
					} else {
						v.add(false);	
					}
				} else { // if only default then display isDefault value
					v.add(defaultParam.sheets[i].fields[j].default_displayed);
				}
					tableModel.addRow(v);
			}

			table.setModel(tableModel);
			//table.getColumnModel().getColumn(0).setPreferredWidth(141);

		}
	}
	
	private boolean isDefaultParamFieldContainedInCustomParamFieldsList (ExportExcelSheetField sheetFieldToCompare,  ExportExcelSheetField[] fields) {
		// this method checks if sheetFieldToCompare is contained in the fields list of elements. It compares by the id only.
		
		for(int i=0; i<fields.length; i++) {
			if(fields[i].id.equals(sheetFieldToCompare.id)) {
				return true;
			}
		}
		return false;
	}
	

    public final JPanel createAdvancedExportPanel() {

    	// JPanel exportPanel = new JPanel(new GridBagLayout());
    	final JPanel exportPanel = new JPanel();
    	//exportPanel.setLayout(null);
    	exportPanel.setSize(new Dimension(800, 250));
   		
        // added 
           
    	setSize(new Dimension(600, 400));
    	//setSize(new Dimension(858, 450));
    	setBounds(100, 100, 772, 600);
    	
   		final JPanel insidePanel = new JPanel(null);
   		exportPanel.add(insidePanel);
   		insidePanel.setSize(new Dimension(800, 600));
   		insidePanel.setPreferredSize(new Dimension(750, 600));
		

		final JPanel optionPane = new JPanel();
		optionPane.setVisible(false);
		optionPane.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
//		optionPane.setSize(new Dimension(600, 650));
//		optionPane.setPreferredSize(new Dimension(600, 650));
//		optionPane.setBounds(new Rectangle(20, 53, 636, 446));
		optionPane.setSize(new Dimension(600, 650));
		optionPane.setPreferredSize(new Dimension(800, 500));
		optionPane.setBounds(new Rectangle(10, 105, 736, 446));
		
		//optionPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		//setContentPane(insidePanel); // ? ou exportPane? setContentPane(insidePanel);
		insidePanel.setLayout(null);
		insidePanel.add(optionPane);
		optionPane.setLayout(null);
		//
		setSize(new Dimension(800, 250));
		setPreferredSize(new Dimension(800, 250));
		
		tabbedPane = new JTabbedPane(JTabbedPane.BOTTOM);
		tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		tabbedPane.setBounds(10, 125, 600, 318);
		optionPane.add(tabbedPane);
		
		
		lblExportExcelTabs = new JLabel("Configuration file:");
		lblExportExcelTabs.setBounds(10, 15, 110, 14);
		optionPane.add(lblExportExcelTabs);
		
		scrollPane = new JScrollPane();
		scrollPane.setBounds(0, 0, 699, 290);
		
		table = new JTable();
		scrollPane.setViewportView(table);
		table.setModel(new DefaultTableModel(
			new Object[][] {
				
			},
			new String[] {
				"Internal field name", "Displayed field name", "Shown"
			}
		) {
			Class[] columnTypes = new Class[] {
				Object.class, String.class, Boolean.class
			};
			public Class getColumnClass(int columnIndex) {
				return columnTypes[columnIndex];
			}
			boolean[] columnEditables = new boolean[] {
				false, true, true
			};
			public boolean isCellEditable(int row, int column) {
				return columnEditables[column];
			}
		});
		table.getColumnModel().getColumn(0).setPreferredWidth(141);
		
		
		
		 // *-*-*-
		table.setDragEnabled(true);
		table.setDropMode(DropMode.INSERT_ROWS);
		table.setTransferHandler(new TableRowTransferHandler(table)); 
		
		comboBox_Orientation = new JComboBox();
		comboBox_Orientation.setModel(new DefaultComboBoxModel(new String[] {"Rows", "Columns"}));
		comboBox_Orientation.setName("");
		comboBox_Orientation.setBounds(10, 94, 91, 20);
		optionPane.add(comboBox_Orientation);
		
		lblOrientation = new JLabel("Orientation:");
		lblOrientation.setBounds(10, 69, 91, 14);
		optionPane.add(lblOrientation);
		
		m_configFile = new JTextField();
		m_configFile.setText(""); // m_configFile.setText("d:\\Proline\\export\\testExport.txt");
		m_configFile.setBounds(123, 12, 232, 20);
		optionPane.add(m_configFile);
		m_configFile.setColumns(10);
		
		lblFormat = new JLabel("Format:");
		lblFormat.setBounds(152, 70, 55, 14);
		optionPane.add(lblFormat);
		
		comboBox_Format = new JComboBox();
		comboBox_Format.setModel(new DefaultComboBoxModel(new String[] {"xlsx", "xls", "csv"}));
		comboBox_Format.setBounds(152, 94, 55, 20);
		optionPane.add(comboBox_Format);
		
		comboBox_DateFormat = new JComboBox();
		comboBox_DateFormat.setModel(new DefaultComboBoxModel(new String[] {"YYYYMMDD HH:mm:ss", "DDMMYYYY HH:mm:ss", "MMDDYYYY HH:mm:ss"}));
		comboBox_DateFormat.setBounds(243, 94, 161, 20);
		optionPane.add(comboBox_DateFormat);
		
		lblDateFormat = new JLabel("Date format:");
		lblDateFormat.setBounds(243, 69, 68, 14);
		optionPane.add(lblDateFormat);
		
		comboBox_NumberSeparator = new JComboBox();
		comboBox_NumberSeparator.setModel(new DefaultComboBoxModel(new String[] {".", ","}));
		comboBox_NumberSeparator.setBounds(447, 94, 49, 20);
		optionPane.add(comboBox_NumberSeparator);
		
		lblNumberSeparator = new JLabel("Number separator:");
		lblNumberSeparator.setBounds(447, 70, 110, 14);
		optionPane.add(lblNumberSeparator);
		
		lblProteinSets = new JLabel("Protein sets:");
		lblProteinSets.setBounds(593, 70, 89, 14);
		optionPane.add(lblProteinSets);
		
		comboBox_ProteinSets = new JComboBox();
		comboBox_ProteinSets.setModel(new DefaultComboBoxModel(new String[] {"Validated", "Not validated"}));
		comboBox_ProteinSets.setBounds(593, 94, 121, 20);
		optionPane.add(comboBox_ProteinSets);
		
		btnNewButton = new JButton("Save");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveConfigFile();
			}
		});
		//btnNewButton.setIcon(new ImageIcon(ExportDialog.class.getResource("/com/sun/java/swing/plaf/windows/icons/FloppyDrive.gif")));
		btnNewButton.setIcon(IconManager.getIcon(IconManager.IconType.SAVE_SETTINGS));
		btnNewButton.setBounds(468, 11, 89, 23);
		optionPane.add(btnNewButton);
		
		btnLoad = new JButton("Load");
		btnLoad.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				loadConfigFile();
			}
		});
		btnLoad.setIcon(IconManager.getIcon(IconManager.IconType.OPEN_FILE));
		//btnLoad.setIcon(new ImageIcon(ExportDialog.class.getResource("/javax/swing/plaf/metal/icons/ocean/directory.gif")));
		btnLoad.setBounds(365, 11, 89, 23);
		optionPane.add(btnLoad);
		
		lblExportToFile = new JLabel("Export to file:");
		lblExportToFile.setBounds(10, 15, 77, 27);
		insidePanel.add(lblExportToFile);
		
		m_fileTextField = new JTextField();
		m_fileTextField.setBounds(86, 15, 374, 27);
		insidePanel.add(m_fileTextField);
		m_fileTextField.setColumns(50);
		
	
		// ---// copier à partir de là
		final JButton addFileButton = new JButton("");
		addFileButton.setBounds(470, 14, 27, 30); //addFileButton.setBounds(470, 15, 114, 27);
		insidePanel.add(addFileButton);
		addFileButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
			}
		});
		
		//addFileButton.setIcon(new ImageIcon(ExportDialog.class.getResource("/com/sun/java/swing/plaf/windows/icons/TreeOpen.gif")));
		addFileButton.setIcon(IconManager.getIcon(IconManager.IconType.OPEN_FILE));
		
		chk_ExportOptions = new JCheckBox("Custom export");
		chk_ExportOptions.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				optionPane.setVisible(chk_ExportOptions.isSelected());
				setSize(new Dimension(exportPanel.getWidth()+6 /* drift? */, 250 + 400 *(chk_ExportOptions.isSelected()?1:0))); // elongate the window if option is selected
			}
		});
		chk_ExportOptions.setBounds(626, 71, 124, 27);
		insidePanel.add(chk_ExportOptions);

        addFileButton.addActionListener(new ActionListener() {

           @Override
           public void actionPerformed(ActionEvent e) {

                   
        	  
               ExporterFactory.ExporterInfo exporterInfo = (ExporterFactory.ExporterInfo) m_exporTypeCombobox.getSelectedItem();
               
               if (exporterInfo != null) {
                   FileNameExtensionFilter filter = new FileNameExtensionFilter(exporterInfo.getName(), exporterInfo.getFileExtension());
                   FileNameExtensionFilter existFilter = getFilterWithSameExtensions(filter);
                   
                   if (existFilter == null) {
                       m_fchooser.addChoosableFileFilter(filter);
                       m_filterList.add(filter);
                       m_fchooser.setFileFilter(filter);
                   }else {
                       m_fchooser.setFileFilter(existFilter);
                   }
               }

               String textFile = m_fileTextField.getText().trim();

               if (textFile.length()>0) {
                   File currentFile = new File(textFile);
                   if (currentFile.isDirectory()) {
                       m_fchooser.setCurrentDirectory(currentFile);
                   } else {
                       m_fchooser.setSelectedFile(currentFile);
                   }
               }

               
               int result = m_fchooser.showOpenDialog(addFileButton);
               if (result == JFileChooser.APPROVE_OPTION) {
                   File file = m_fchooser.getSelectedFile();
                   
                   String absolutePath = file.getAbsolutePath();
                   String fileName = file.getName();
                   if (fileName.indexOf('.') == -1) {
                       absolutePath += "."+exporterInfo.getFileExtension();
                   }
                   m_fileTextField.setText(absolutePath);
               }
           }
		   });
		
		  
		
	   if((m_exportType == ExporterFactory.EXPORT_FROM_SERVER || m_exportType == ExporterFactory.EXPORT_XIC) && m_showExportAllPSMsChB  ){
	
	       m_exportAllPSMsChB = new JCheckBox(" Export all PSMs");
	       insidePanel.add(m_exportAllPSMsChB);
	       m_exportAllPSMsChB.setBounds(6, 78, 114, 20);
	   }
	   
	   lbl_exportType = new JLabel("Export Type:");
       lbl_exportType.setBounds(10, 44, 93, 27);
       insidePanel.add(lbl_exportType);
	   
	   m_exporTypeCombobox = new JComboBox(ExporterFactory.getList(m_exportType).toArray());
	   m_exporTypeCombobox.setSelectedIndex(0);
	   m_exporTypeCombobox.setBounds(86, 47, 206, 20);
	   insidePanel.add(new JSeparator(SwingConstants.HORIZONTAL));
	   insidePanel.add(new JLabel("Export Type:"));
	   insidePanel.add(m_exporTypeCombobox);
	   
	   setSize(new Dimension(exportPanel.getWidth(), 250 + 400 *(!chk_ExportOptions.isSelected()?1:0))); // elongate the window if option is selected
	   //----
		return exportPanel;
           
		
		
    }
    
    
    protected void loadConfigFile() {

        String configFile = m_configFile.getText().trim();

        if (configFile.length()>0) {
            File currentFile = new File(configFile);
            if (currentFile.isDirectory()) {
                m_fchooser.setCurrentDirectory(currentFile);
            } else {
                m_fchooser.setSelectedFile(currentFile);
            }
        }

        
        int result = m_fchooser.showOpenDialog(btnLoad);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = m_fchooser.getSelectedFile();
            
            String absolutePath = file.getAbsolutePath();
            file.getName();
            m_configFile.setText(absolutePath);
            loadExportConfig();
            if(m_exportDefaultConfig!=null )
            {
         	   fillExportFormatTable(m_exportDefaultConfig, m_exportConfig);
            }
    		
        }    
		
	}



	protected void saveConfigFile() {
        String configFile = m_configFile.getText().trim();

        JFileChooser m_fchooser = new JFileChooser();
		   if (configFile.length()>0) {
            File currentFile = new File(configFile);
            if (currentFile.isDirectory()) {
                m_fchooser.setCurrentDirectory(currentFile);
            } else {
                m_fchooser.setSelectedFile(currentFile);
            }
            
        }

        
        int result = m_fchooser.showSaveDialog(btnLoad);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = m_fchooser.getSelectedFile();
            
            String absolutePath = file.getAbsolutePath();
            file.getName();
            m_configFile.setText(absolutePath);
            File f = new File(absolutePath);
            
            if (f.exists()) {
                String message = "The file already exists. Do you want to overwrite it ?";
                String title = "Overwrite ?";
                String[] options = {"Yes","No"};
                int reply = JOptionPane.showOptionDialog(this, message, title, JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, "Yes");
                if (reply != JOptionPane.YES_OPTION) {
                	System.out.println("save cancelled");
                	return; // cancel save
                }
            }
            
           
            final Path path = Paths.get(absolutePath);
    
                BufferedWriter writer = null;
				try {
					writer = Files.newBufferedWriter(path,
					    StandardCharsets.UTF_8, StandardOpenOption.CREATE);
					
					Gson gson = new GsonBuilder().setPrettyPrinting().create();
					String jsonString = gson.toJson(m_exportConfig);
					writer.write(jsonString);
					writer.flush();
				} catch (IOException e1) {
				
				//	e1.printStackTrace();
				}
    		
        }
        
		
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

                    

                ExporterFactory.ExporterInfo exporterInfo = (ExporterFactory.ExporterInfo) m_exporTypeCombobox.getSelectedItem();
                
                if (exporterInfo != null) {
                    FileNameExtensionFilter filter = new FileNameExtensionFilter(exporterInfo.getName(), exporterInfo.getFileExtension());
                    FileNameExtensionFilter existFilter = getFilterWithSameExtensions(filter);
                    
                    if (existFilter == null) {
                        m_fchooser.addChoosableFileFilter(filter);
                        m_filterList.add(filter);
                        m_fchooser.setFileFilter(filter);
                    }else {
                        m_fchooser.setFileFilter(existFilter);
                    }
                }

                String textFile = m_fileTextField.getText().trim();

                if (textFile.length()>0) {
                    File currentFile = new File(textFile);
                    if (currentFile.isDirectory()) {
                        m_fchooser.setCurrentDirectory(currentFile);
                    } else {
                        m_fchooser.setSelectedFile(currentFile);
                    }
                }

                
                int result = m_fchooser.showOpenDialog(addFileButton);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File file = m_fchooser.getSelectedFile();
                    
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

        if((m_exportType == ExporterFactory.EXPORT_FROM_SERVER || m_exportType == ExporterFactory.EXPORT_XIC) && m_showExportAllPSMsChB  ){
            //Allow specific parameter in this case
            c.gridy++;
            c.gridx = 0;
            c.gridwidth = 2;
            m_exportAllPSMsChB = new JCheckBox(" Export all PSMs");
            exportPanel.add(m_exportAllPSMsChB, c);
        }
        
        
        m_exporTypeCombobox = new JComboBox(ExporterFactory.getList(m_exportType).toArray());
        m_exporTypeCombobox.setSelectedIndex(0);


        c.gridy++;
        c.gridx = 0;
        c.gridwidth = 1;
        exportPanel.add(new JLabel("Export Type:"), c);

        c.gridx++;
        c.gridwidth = 2;
        exportPanel.add(m_exporTypeCombobox, c);

        return exportPanel;
    }

    public String getFileName() {
        return m_fileTextField.getText().trim();
    }
    
    public Boolean exportAllPSMs(){
        if(m_exportType == ExporterFactory.EXPORT_FROM_SERVER)
            return m_exportAllPSMsChB.isSelected();
        else return null;
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
        
        if (f.exists()) {
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
        } else if (m_exportType == ExporterFactory.EXPORT_FROM_SERVER || m_exportType == ExporterFactory.EXPORT_XIC) {

            startTask(m_singletonServerDialog.m_task);
            
            Preferences preferences = NbPreferences.root();
            preferences.put("DefaultExcelExportPath", f.getAbsoluteFile().getParentFile().getName());
            
        } else if (m_exportType == ExporterFactory.EXPORT_IMAGE) {
            BufferedImage bi = new BufferedImage(m_panel.getSize().width, m_panel.getSize().height, BufferedImage.TYPE_INT_ARGB);
            Graphics g = bi.createGraphics();
            m_panel.paint(g);
            g.dispose();
            try {
                ImageIO.write(bi, "png", new File(fileName));
            } catch (IOException e) {
                LoggerFactory.getLogger("ProlineStudio.ResultExplorer").error("Error exporting png", e);
            }

            Preferences preferences = NbPreferences.root();
            preferences.put("DefaultExcelImagePath", f.getAbsoluteFile().getParentFile().getName());
            return true;
        } else if (m_exportType == ExporterFactory.EXPORT_IMAGE2) {
        	
            try {
                ExporterFactory.ExporterInfo exporterInfo = (ExporterFactory.ExporterInfo) m_exporTypeCombobox.getSelectedItem();

                if (exporterInfo.getName().contains("png")) {

                    LoggerFactory.getLogger("ProlineStudio.ResultExplorer").info("exporting png file...to: " + f.toPath().toString());
                    m_imageExporter.generatePngImage(fileName);

                } else if (exporterInfo.getName().contains("svg")) { // svg output

                    LoggerFactory.getLogger("ProlineStudio.ResultExplorer").info("exporting svg file...to: " + f.toPath().toString());
                    m_imageExporter.generateSvgImage(fileName);

                }

               
            } catch (Exception e) {
            	LoggerFactory.getLogger("ProlineStudio.ResultExplorer").error("error " + e.getMessage() + " while exporting picture");
            }
            
            Preferences preferences = NbPreferences.root();
            preferences.put("DefaultExcelImagePath", f.getAbsoluteFile().getParentFile().getName());
            
            return true;
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
    
    /* compare filter based on the extensions, returns null if the filter is not already in the list, otherwise returns the object filter in the list*/
    private FileNameExtensionFilter getFilterWithSameExtensions(FileNameExtensionFilter filter) {
        FileNameExtensionFilter existFilter = null;
        
        String[] newExtension = filter.getExtensions();
        int nbNew = newExtension.length;
        for (FileNameExtensionFilter f : m_filterList) {
            String[] extensions = f.getExtensions();
            boolean sameExt = true;
            int nbE = extensions.length;
            if (nbE == nbNew) {
                for (int k = 0; k < nbE; k++) {
                    if (!extensions[k].equals(newExtension[k])) {
                        sameExt = false;
                        break;
                    }
                }
            }
            if (sameExt) {
                existFilter = f;
                break;
            }
        }
        return existFilter;

    }
    

	
}
