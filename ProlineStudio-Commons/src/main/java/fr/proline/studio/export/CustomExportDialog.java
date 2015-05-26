package fr.proline.studio.export;

import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.utils.IconManager;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
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
import java.util.List;
import java.util.Vector;
import java.util.prefs.Preferences;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

import org.jdesktop.swingx.JXTable;
import org.openide.util.NbPreferences;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


/**
 *
 * @author AW
 *
 */
public class CustomExportDialog extends DefaultDialog {

    private final static Logger logger = LoggerFactory.getLogger("ProlineStudio.Commons");

    private static CustomExportDialog m_singletonImageDialog = null;
    private static CustomExportDialog m_singletonImage2Dialog = null;
    private static CustomExportDialog m_singletonExcelDialog = null;
    private static CustomExportDialog m_singletonServerDialog = null;

    private int m_exportType;

    private JTextField m_fileTextField;
    private JComboBox m_exporTypeCombobox;
    private JCheckBox m_exportAllPSMsChB;
    private Boolean m_showExportAllPSMsChB;

    private JXTable m_table = null;
    private JPanel m_panel = null;
    private ImageExporterInterface m_imageExporter = null;

    private JFileChooser m_fchooser;
    private JFileChooser m_exportFchooser;
    private List<FileNameExtensionFilter> m_filterList = new ArrayList<>();
    private static final String jsonExtension = "json";

    private DefaultDialog.ProgressTask m_task = null;

    private String m_exportName = null;

    // created by AW:
    private JTabbedPane m_tabbedPane;
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
    //private JComboBox comboBox_Format;
    private JComboBox comboBox_NumberSeparator;
    private JComboBox comboBox_Orientation;
    //private JButton addFileButton;
    private JLabel lblExportToFile;
    private JCheckBox chk_ExportOptions;
    private ExportConfig m_exportConfig;
    private ExportConfig m_exportDefaultConfig;
    private JLabel lbl_exportType;

    private String[] m_presentation; // to complete information not displayed in jtable nor tabpane.
    private String[] m_sheetId;  // to complete information not displayed in jtable nor tabpane.
    private String[] m_sheetTitle;  // to complete information not displayed in jtable nor tabpane.
    protected boolean m_updateInProgress = true; // indicate when the table is built (to avoid calling event handler on every table update)

	private JPanel panel_1;

	private JLabel lblExportProfile;

	private JComboBox comboBox_exportProfile;
    

    public static CustomExportDialog getDialog(Window parent, JXTable table, String exportName) {
        if (m_singletonExcelDialog == null) {
            m_singletonExcelDialog = new CustomExportDialog(parent, ExporterFactory.EXPORT_TABLE);
        }

        m_singletonExcelDialog.m_table = table;
        m_singletonExcelDialog.m_exportName = exportName;

        return m_singletonExcelDialog;
    }

    public static CustomExportDialog getDialog(Window parent, JPanel panel, String exportName) {
        if (m_singletonImageDialog == null) {
            m_singletonImageDialog = new CustomExportDialog(parent, ExporterFactory.EXPORT_IMAGE);
        }

        m_singletonImageDialog.m_panel = panel;
        m_singletonImageDialog.m_imageExporter = null;
        m_singletonImageDialog.m_exportName = exportName;

        return m_singletonImageDialog;
    }

    public static CustomExportDialog getDialog(Window parent, JPanel panel, ImageExporterInterface imageExporter, String exportName) {
        if (m_singletonImage2Dialog == null) {
            m_singletonImage2Dialog = new CustomExportDialog(parent, ExporterFactory.EXPORT_IMAGE2);
        }

        m_singletonImage2Dialog.m_panel = panel;
        m_singletonImage2Dialog.m_imageExporter = imageExporter;
        m_singletonImage2Dialog.m_exportName = exportName;

        return m_singletonImage2Dialog;
    }

    public static CustomExportDialog getDialog(Window parent, Boolean showExportAllPSMsOption) {
        if (m_singletonServerDialog == null) {
            m_singletonServerDialog = new CustomExportDialog(parent, ExporterFactory.EXPORT_FROM_SERVER, showExportAllPSMsOption);
        } else if (!m_singletonServerDialog.m_showExportAllPSMsChB.equals(showExportAllPSMsOption)) {
            m_singletonServerDialog = new CustomExportDialog(parent, ExporterFactory.EXPORT_FROM_SERVER, showExportAllPSMsOption);
        }

        return m_singletonServerDialog;
    }

    public static CustomExportDialog getDialog(Window parent, Boolean showExportAllPSMsOption, int exportType) {
        if (m_singletonServerDialog == null) {
            m_singletonServerDialog = new CustomExportDialog(parent, exportType, showExportAllPSMsOption);
        } else if (!m_singletonServerDialog.m_showExportAllPSMsChB.equals(showExportAllPSMsOption)) {
            m_singletonServerDialog = new CustomExportDialog(parent, exportType, showExportAllPSMsOption);
        }

        return m_singletonServerDialog;
    }

    public void setTask(DefaultDialog.ProgressTask task) {
        m_task = task;
    }

    private CustomExportDialog(Window parent, int type) {
        this(parent, type, null);
    }

    private CustomExportDialog(Window parent, int type, Boolean showExportAllPSMsOption) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);
        m_showExportAllPSMsChB = showExportAllPSMsOption;
        m_exportType = type;

        setTitle("Export");

        setHelpURL("http://biodev.extra.cea.fr/docs/proline/doku.php?id=how_to:studio:exportdata");

        setInternalComponent(createCustomExportPanel());
       
        setButtonName(BUTTON_OK, ((m_exportType == ExporterFactory.EXPORT_IMAGE)
                || (m_exportType == ExporterFactory.EXPORT_IMAGE2)) ? "Export Image" : "Export");

        String defaultExportPath;
        Preferences preferences = NbPreferences.root();
        if ((m_exportType == ExporterFactory.EXPORT_TABLE) || (m_exportType == ExporterFactory.EXPORT_FROM_SERVER) || (m_exportType == ExporterFactory.EXPORT_XIC)) {
            defaultExportPath = preferences.get("DefaultExcelExportPath", "");
        } else { // IMAGE
            defaultExportPath = preferences.get("DefaultImageExportPath", "");
        }
        if (defaultExportPath.length() > 0) {
            m_fchooser = new JFileChooser(new File(defaultExportPath));
            m_exportFchooser = new JFileChooser(new File(defaultExportPath));
        } else {
            m_fchooser = new JFileChooser();
            m_exportFchooser = new JFileChooser();
        }
        m_fchooser.setMultiSelectionEnabled(false);
        m_exportFchooser.setMultiSelectionEnabled(false);
        FileNameExtensionFilter filterJson = new FileNameExtensionFilter("Custom Export Config (."+jsonExtension+")", jsonExtension);
        m_exportFchooser.setFileFilter(filterJson);

    }

    private void loadExportConfig() {
        // decode json 
        m_exportConfig = new ExportConfig();
        String jsonString = "";
        Path filePath = Paths.get(m_configFile.getText());
        try {
            jsonString = new String(Files.readAllBytes(filePath));
        } catch (IOException e) {

            logger.error("Error while loading config "+e);
        }

        if (!filePath.toString().equals("")) {
            Gson gson = new Gson();
            String messageHashMapJsonString = jsonString;
            m_exportConfig = gson.fromJson(messageHashMapJsonString, m_exportConfig.getClass());

        }
    }

	private void fillExportPossibleValues(ExportConfig param) {
		if(param!=null) {
		
			
			if(param.sheet_presentation_values!=null) {
				comboBox_Orientation.setModel(new DefaultComboBoxModel(param.sheet_presentation_values));
			}

			if (param.format_values != null) {
            	String[] reformatedParamValues = new String[param.format_values.length];
            	for (int i = 0; i < param.format_values.length; i++) {
            		if(param.format_values[i].contains("xls")) {
            			reformatedParamValues[i] = "Excel (." + param.format_values[i].toString() + ")";
            		}
            		else if (param.format_values[i].contains("tsv") ) {
            			reformatedParamValues[i] = "Tabulation separated values (." + param.format_values[i].toString() + ")";
            		}
            		else if (param.format_values[i].contains("csv") )  {
            			reformatedParamValues[i] = "Comma separated values (." + param.format_values[i].toString() + ")";
            		}
				} 
            	m_exporTypeCombobox.setModel(new DefaultComboBoxModel(reformatedParamValues));
            }
			
			if(param.date_format_values!=null) {
				comboBox_DateFormat.setModel(new DefaultComboBoxModel(param.date_format_values));
			}

			if(param.decimal_separator_values!=null) {
				comboBox_NumberSeparator.setModel(new DefaultComboBoxModel(param.decimal_separator_values));
			}
			
			comboBox_ProteinSets.setModel(new DefaultComboBoxModel(new String[] {"All","Validated only"}));
			
			comboBox_exportProfile.setModel(new DefaultComboBoxModel(new String[] {"Best", "All"}));
			
		}
	}

    
	private void selectLoadedExportValues(ExportConfig param) {
		if(param!=null) {
		
			if(param.date_format!=null) {
				comboBox_DateFormat.setSelectedItem(param.date_format);
			}

			if(param.decimal_separator!=null) {
				comboBox_NumberSeparator.setSelectedItem(param.decimal_separator);
			}
			
			if(param.data_export.all_protein_set)
			{
				comboBox_ProteinSets.setSelectedIndex(0);
			} else {
				comboBox_ProteinSets.setSelectedIndex(1);
			}
			if(param.data_export.best_profile) 
			{
				comboBox_exportProfile.setSelectedIndex(0);	
			} 	else {
				comboBox_exportProfile.setSelectedIndex(1);
			}
			
			
		}
		
	}
	

	
	private void fillExportFormatTable(ExportConfig defaultParam, ExportConfig param) {
		//reset panes:
		
		m_updateInProgress = true;
		m_tabbedPane.removeAll();	
		m_presentation = new String[defaultParam.sheets.length]; 
		m_sheetId = new String[defaultParam.sheets.length];
		m_sheetTitle = new String[defaultParam.sheets.length];
		
		
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
		
		
		// strategy: we first loop through default param tabs.
		// --------   then if we find that there is a corresponding tab in custom param file,
		// we add it and fill fields from custom file first, then if some fields are missing, 
		// we add them from default file.
		// if some tabs are missing, we add the tabs too but disable them.
		// 
		// create tab panes
		for(int i = 0; i<defaultParam.sheets.length;i++) {
			
			panel = new JPanel();
			m_tabbedPane.addTab(defaultParam.sheets[i].title, null, panel, null);
			m_presentation[i]=defaultParam.sheets[i].presentation;
			m_sheetId[i]=defaultParam.sheets[i].id;
			m_sheetTitle[i]=defaultParam.sheets[i].title;
			
			
			panel.setLayout(new BorderLayout(0, 0));
			// read fields to fill in jtable into this tabbed pane
			
			scrollPane = new JScrollPane();
			scrollPane.setBounds(0, 0, 600, 290);
			panel.add(scrollPane);
			
			table = new JTable();

			table.setDragEnabled(true);
			table.setDropMode(DropMode.INSERT_ROWS);
			table.setSelectionMode(0); // Allow only 1 item to be selected TODO: add possibility to select blocks or multiple independent rows to move (with drag n drop)
			table.setTransferHandler(new TableRowTransferHandler(table));
			
			// drag n drop handling:
			table.addMouseMotionListener(new MouseMotionListener() {
			    public void mouseDragged(MouseEvent e) {
			    	e.consume();
			    	JComponent c = (JComponent) e.getSource();
			        TransferHandler handler = c.getTransferHandler();
			        handler.exportAsDrag(c, e, TransferHandler.MOVE);
			    }

			    public void mouseMoved(MouseEvent e) {
			    }
			});
			

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
		                false,true, true
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
			// add the data into the model
			
			// ---add ability to enable/disable individual tabs
			if(defaultParam.sheets[i].default_displayed) 
			{ 
				m_tabbedPane.setEnabledAt(i, true); 
			} else {
				m_tabbedPane.setEnabledAt(i, false); 
			}
			m_tabbedPane.setToolTipTextAt(i, "Right click to Enable/Disable");
			
			
			
			// perform specific code if custom file loaded on top of default one:
			ExportExcelSheet paramSheet = null;  // the currently processed param sheet
			
			if(param!=null) { // if we loaded a param file (if not then do nothing here).
				// find the corresponding sheet in custom param file compared to the defaultConfig file being looped
				
				// first, mark it as disabled, if in the loop we find it, then enable the tab sheet.
				m_tabbedPane.setEnabledAt(i, false);
				boolean sheetHasBeenFoundInCustomParam = false;
				
				for(int k=0; k<param.sheets.length/*paramSheets_list.size()*/;k++) {
					
					if(param.sheets[k].id.equals(defaultParam.sheets[i].id)) {
						paramSheet = param.sheets[k];
						sheetHasBeenFoundInCustomParam = true;
						m_tabbedPane.setEnabledAt(i, true);
						if(param.sheets[k].presentation.equals("rows")) {
							m_presentation[i] = "rows";
						}
						else {
							m_presentation[i] = "columns";
						}
						
						
					}
				}
				
			
				// if tab sheet is present:
				
				if(sheetHasBeenFoundInCustomParam/*sheetContainedInCustomParamConfig(defaultParam.sheets[i],param, index)*/) {
					// as we found the paramSheet, we now scan through it to check if fields are present or not.

					
				    // add custom fields first 
					for(int j=0; j< paramSheet.fields.length ; j++) {
								
						Vector v = new Vector();
						//v.add(defaultParam.sheets[i].fields[j].id);
						v.add(paramSheet.fields[j].id);
						v.add(paramSheet.fields[j].title);
						v.add(true);
						
						tableModel.addRow(v);
						
					}
				} else { // param sheet has not been found, we add anyway the fields from defaut tab sheet.
					

					for(int j=0; j< defaultParam.sheets[i].fields.length ; j++) {
						
						Vector v = new Vector();
					
						v.add(defaultParam.sheets[i].fields[j].id);
						v.add(defaultParam.sheets[i].fields[j].title);
						v.add(defaultParam.sheets[i].fields[j].default_displayed);
						tableModel.addRow(v);
						
						
					}
				}
				
				// now add remaining non already added default fields at the end of custom fields list (for current sheet)
				for(int j=0; j< defaultParam.sheets[i].fields.length ; j++) {
					
					Vector v = new Vector();
					if(paramSheet!=null) { // check If Default Field Present In paramSheet
						
						String customField = getCustomFieldIfFieldContainedInFieldsList(defaultParam.sheets[i].fields[j], paramSheet.fields);
						if(customField==null) {
							v.add(defaultParam.sheets[i].fields[j].id);
							v.add(defaultParam.sheets[i].fields[j].title);
							v.add(false);
							tableModel.addRow(v);
						}
					} 
					
					
					
				}
				
			}
			if(param==null) { // add default fields anyway (before any custom config is loaded)
				for(int j=0; j< defaultParam.sheets[i].fields.length ; j++) {
					
					Vector v = new Vector();
					 // add default field 
					v.add(defaultParam.sheets[i].fields[j].id);
					v.add(defaultParam.sheets[i].fields[j].title);
					v.add(defaultParam.sheets[i].fields[j].default_displayed);
					tableModel.addRow(v);
				}
				
			}
			//
			table.setModel(tableModel);
			//table.getColumnModel().getColumn(0).setPreferredWidth(20);
				
			
			
			
			
		}
		// adjust current displayed tab with correct presentation mode:
		updatePresentationModeForNewlySelectedTab();
		
		m_updateInProgress = false;
		
	}
	


	private String getCustomFieldIfFieldContainedInFieldsList (ExportExcelSheetField sheetFieldToCompare,  ExportExcelSheetField[] fields) {
		// this method checks if sheetFieldToCompare is contained in the fields list of elements. It compares by the id only and return 
		
		for(int i=0; i<fields.length; i++) {
			if(fields[i].id.equals(sheetFieldToCompare.id)) {
				return fields[i].title; // return the custom title field
			}
		}
		return null;
	}
	
    public final JPanel createCustomExportPanel() {

        // JPanel exportPanel = new JPanel(new GridBagLayout());
        final JPanel exportPanel = new JPanel();
        //exportPanel.setLayout(null);
        exportPanel.setSize(new Dimension(700, 250));

        // added 
        setSize(new Dimension(600, 400));
        //setSize(new Dimension(858, 450));
        setBounds(100, 100, 772, 600);

        final JPanel insidePanel = new JPanel(null);
        exportPanel.add(insidePanel);
        insidePanel.setSize(new Dimension(700, 600));
        insidePanel.setPreferredSize(new Dimension(700, 600));

        final JPanel optionPane = new JPanel();
        optionPane.setVisible(false);
        optionPane.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
        optionPane.setSize(new Dimension(600, 650));
        optionPane.setPreferredSize(new Dimension(700, 500));
        optionPane.setBounds(new Rectangle(10, 105, 590, 446));

		insidePanel.setLayout(null);
        insidePanel.add(optionPane);
        optionPane.setLayout(null);
        //
        setSize(new Dimension(800, 250));
        setPreferredSize(new Dimension(800, 250));

		panel_1 = new JPanel();
		panel_1.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panel_1.setBounds(10, 99, 570, 336);
		optionPane.add(panel_1);
		panel_1.setLayout(null);
		
		
		
        lblExportExcelTabs = new JLabel("Configuration file:");
        lblExportExcelTabs.setBounds(10, 15, 110, 14);
        optionPane.add(lblExportExcelTabs);

        scrollPane = new JScrollPane();
        scrollPane.setBounds(0, 0, 400, 190);

        table = new JTable();
        scrollPane.setViewportView(table);
        table.setModel(new DefaultTableModel(
                new Object[][]{},
                new String[]{
                    "Internal field name", "Displayed field name", "Shown"
                }
        ) {
            Class[] columnTypes = new Class[]{
                Object.class, String.class, Boolean.class
            };

            public Class getColumnClass(int columnIndex) {
                return columnTypes[columnIndex];
            }
            boolean[] columnEditables = new boolean[]{
                false, true, true
            };

            public boolean isCellEditable(int row, int column) {
                return columnEditables[column];
            }
        });
        table.getColumnModel().getColumn(0).setPreferredWidth(141);

        // *-*-*-


        comboBox_Orientation = new JComboBox();
		comboBox_Orientation.setBounds(79, 8, 91, 20);
		panel_1.add(comboBox_Orientation);
		comboBox_Orientation.setModel(new DefaultComboBoxModel(new String[] {"rows", "columns"}));
		comboBox_Orientation.setName("");
		comboBox_Orientation.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(!m_updateInProgress) {
					presentationModeChanged();
				}
			}
		});
		

        lblOrientation = new JLabel("Orientation:");
		lblOrientation.setBounds(10, 11, 91, 14);
		panel_1.add(lblOrientation);
		
        m_configFile = new JTextField();
        m_configFile.setText(""); // m_configFile.setText("d:\\Proline\\export\\testExport.txt");
        m_configFile.setBounds(123, 12, 232, 20);
        optionPane.add(m_configFile);
        m_configFile.setColumns(10);

//        lblFormat = new JLabel("Format:");
//        lblFormat.setBounds(10, 44, 55, 14);
//        optionPane.add(lblFormat);

        m_exporTypeCombobox = new JComboBox();
        m_exporTypeCombobox.setModel(new DefaultComboBoxModel(new String[]{"xlsx", "xls", "csv", "tsv"}));
        //m_exporTypeCombobox.setBounds(10, 68, 55, 20);
        //optionPane.add(comboBox_Format);

        comboBox_DateFormat = new JComboBox();
        comboBox_DateFormat.setModel(new DefaultComboBoxModel(new String[]{"YYYYMMDD HH:mm:ss", "DDMMYYYY HH:mm:ss", "MMDDYYYY HH:mm:ss"}));
        comboBox_DateFormat.setBounds(10, 65, 161, 20);
        optionPane.add(comboBox_DateFormat);

        lblDateFormat = new JLabel("Date format:");
        lblDateFormat.setBounds(10, 44, 68, 14);
        optionPane.add(lblDateFormat);

        comboBox_NumberSeparator = new JComboBox();
        comboBox_NumberSeparator.setModel(new DefaultComboBoxModel(new String[]{".", ","}));
        comboBox_NumberSeparator.setBounds(180, 65, 49, 20);
        optionPane.add(comboBox_NumberSeparator);

        lblNumberSeparator = new JLabel("Number separator:");
        lblNumberSeparator.setBounds(180, 43, 110, 14);
        optionPane.add(lblNumberSeparator);

        lblProteinSets = new JLabel("Protein sets:");
        lblProteinSets.setBounds(304, 44, 89, 14);
        optionPane.add(lblProteinSets);

        comboBox_ProteinSets = new JComboBox();
        comboBox_ProteinSets.setModel(new DefaultComboBoxModel(new String[]{"All", "Validated only"}));
        comboBox_ProteinSets.setBounds(304, 65, 121, 20);
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

		
        m_tabbedPane = new JTabbedPane(JTabbedPane.BOTTOM);
		m_tabbedPane.addChangeListener(new ChangeListener() {
	        public void stateChanged(ChangeEvent e) {
	        	if(!m_updateInProgress ) { // update only when no update in progress
	        		updatePresentationModeForNewlySelectedTab();
	        	}
	        }
	    });
		m_tabbedPane.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if (SwingUtilities.isRightMouseButton(arg0) ) {
					boolean tabEnabled=  m_tabbedPane.isEnabledAt(m_tabbedPane.indexAtLocation(arg0.getX(), arg0.getY()));
					int tabIndex = m_tabbedPane.indexAtLocation(arg0.getX(),arg0.getY());
					m_tabbedPane.setEnabledAt(tabIndex, !tabEnabled);
					repaint();
					arg0.consume();
				} 
					
			} 
		});
		
		m_tabbedPane.setBounds(10, 36, 550, 289);
		
		panel_1.add(m_tabbedPane);
		m_tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		
		comboBox_exportProfile = new JComboBox();
		comboBox_exportProfile.setModel(new DefaultComboBoxModel(new String[] {"Best", "All"}));
		comboBox_exportProfile.setBounds(448, 65, 109, 20);
		optionPane.add(comboBox_exportProfile);
		
		lblExportProfile = new JLabel("Export profile:");
		lblExportProfile.setBounds(448, 44, 89, 14);
		optionPane.add(lblExportProfile);
		
        lblExportToFile = new JLabel("Export to file:");
        lblExportToFile.setBounds(10, 15, 77, 27);
        insidePanel.add(lblExportToFile);

        m_fileTextField = new JTextField();
        m_fileTextField.setBounds(86, 15, 374, 27);
        insidePanel.add(m_fileTextField);
        m_fileTextField.setColumns(50);

        // ---// copier ÃƒÂ  partir de lÃƒÂ 
       
        chk_ExportOptions = new JCheckBox("Custom export");
        chk_ExportOptions.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                optionPane.setVisible(chk_ExportOptions.isSelected());
                if(!chk_ExportOptions.isSelected()) {
                	//disable custom parameters and restore default ones
                	m_exportConfig = m_exportDefaultConfig;
                	fillExportFormatTable(m_exportDefaultConfig, m_exportConfig);
                }
                setSize(new Dimension(exportPanel.getWidth() + 6 /* drift? */, 250 + 400 * (chk_ExportOptions.isSelected() ? 1 : 0))); // elongate the window if option is selected
            }
        });
        chk_ExportOptions.setBounds(476, 71, 124, 27);
        insidePanel.add(chk_ExportOptions);
        
        final JButton addFileButton = new JButton("");
        addFileButton.setBounds(470, 14, 27, 30); //addFileButton.setBounds(470, 15, 114, 27);
        insidePanel.add(addFileButton);
        

        addFileButton.setIcon(IconManager.getIcon(IconManager.IconType.OPEN_FILE));

        addFileButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                //ExporterFactory.ExporterInfo exporterInfo = (ExporterFactory.ExporterInfo) m_exporTypeCombobox.getSelectedItem();

                if (m_exporTypeCombobox.getSelectedItem() != null) {
                	FileNameExtensionFilter filter = null;
                	if(m_exporTypeCombobox.getSelectedItem().toString().contains("xls")) 
                	{
                		filter = new FileNameExtensionFilter("Excel File (.xlsx)", "xlsx");
                	} else if(m_exporTypeCombobox.getSelectedItem().toString().contains("tsv")) {
                		filter = new FileNameExtensionFilter("Tabulation Separated Values (.tsv)","tsv");
                	
                	}
                    m_fchooser.addChoosableFileFilter(filter);
                    m_filterList.add(filter);
                    m_fchooser.setFileFilter(filter);
                	
                    
                }

                String textFile = m_fileTextField.getText().trim();

                if (textFile.length() > 0) {
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
                        absolutePath += "."  + "xlsx"; //+ exporterInfo.getFileExtension();
                    }
                    m_fileTextField.setText(absolutePath);
                }
            }
        });

        if ((m_exportType == ExporterFactory.EXPORT_FROM_SERVER || m_exportType == ExporterFactory.EXPORT_XIC) && m_showExportAllPSMsChB) {
            m_exportAllPSMsChB = new JCheckBox(" Export all PSMs");
            m_exportAllPSMsChB.setBounds(6, 78, 114, 20);
            insidePanel.add(m_exportAllPSMsChB);
        }

        lbl_exportType = new JLabel("Export Type:");
        lbl_exportType.setBounds(10, 44, 93, 27);
        insidePanel.add(lbl_exportType);

        //m_exporTypeCombobox = new JComboBox(ExporterFactory.getList(m_exportType).toArray());
        m_exporTypeCombobox = new JComboBox();
       // m_exporTypeCombobox.setSelectedIndex(0);
        m_exporTypeCombobox.setBounds(86, 47, 206, 20);
        insidePanel.add(new JSeparator(SwingConstants.HORIZONTAL));
        insidePanel.add(new JLabel("Export Type:"));
        insidePanel.add(m_exporTypeCombobox);

        setSize(new Dimension(exportPanel.getWidth(), 250 + 400 * (!chk_ExportOptions.isSelected() ? 1 : 0))); // elongate the window if option is selected
        //----
        return exportPanel;

    }

    protected void loadConfigFile() {

         //-------
    	
        String configFile = m_configFile.getText().trim();

        if (configFile.length() > 0) {
            File currentFile = new File(configFile);
            if (currentFile.isDirectory()) {
            	m_exportFchooser.setCurrentDirectory(currentFile);
            } else {
            	m_exportFchooser.setSelectedFile(currentFile);
            }
        }

        int result = m_exportFchooser.showOpenDialog(btnLoad);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = m_exportFchooser.getSelectedFile();

            String absolutePath = file.getAbsolutePath();
            
            m_configFile.setText(absolutePath);
            loadExportConfig();
            if (m_exportDefaultConfig != null) {
                fillExportFormatTable(m_exportDefaultConfig, m_exportConfig);
                selectAppropriatePresentationMode();
           	    selectLoadedExportValues(m_exportConfig);
            }

        }

    }


	private void selectAppropriatePresentationMode() {
		// select the right presentation mode based on the currently selected tab.
		int selectedTab = m_tabbedPane.getSelectedIndex();
		if(m_presentation[selectedTab].equals("rows")) {
			comboBox_Orientation.setSelectedIndex(0);
		} else {
			comboBox_Orientation.setSelectedIndex(1);
		}
		
	}
	
    protected void saveConfigFile() {
        String configFile = m_configFile.getText().trim();
        
        if (configFile.length() > 0) {
            File currentFile = new File(configFile);
            if (currentFile.isDirectory()) {
            	m_exportFchooser.setCurrentDirectory(currentFile);
            } else {
            	m_exportFchooser.setSelectedFile(currentFile);
            }

        }

        int result = m_exportFchooser.showSaveDialog(btnLoad);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = m_exportFchooser.getSelectedFile();

            String absolutePath = file.getAbsolutePath();
            // add json if needed
            if (!absolutePath.endsWith("."+jsonExtension)){
                absolutePath += "."+jsonExtension;
            }
            m_configFile.setText(absolutePath);
            File f = new File(absolutePath);

            if (f.exists()) {
                String message = "The file already exists. Do you want to overwrite it ?";
                String title = "Overwrite ?";
                String[] options = {"Yes", "No"};
                int reply = JOptionPane.showOptionDialog(this, message, title, JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, "Yes");
                if (reply != JOptionPane.YES_OPTION) {
                    
                    return; // cancel save
                }
                f.delete();
            }

           
            final Path path = Paths.get(absolutePath);
    
            BufferedWriter writer = null;
            try {
                writer = Files.newBufferedWriter(path,
                        StandardCharsets.UTF_8, StandardOpenOption.CREATE);

                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                String jsonString = gson.toJson(generateConfigFileFromGUI());
                writer.write(jsonString);
                writer.flush();
            } catch (IOException e1) {
                logger.error("Error while saving the configuration " + e1);
            } finally {
                try {
                    if (writer != null) {
                        writer.close();
                    }
                } catch (Exception e) {
                    logger.error("Error while saving the configuration " + e);
                }
            }

        }

    }

    
    
    protected void updatePresentationModeForNewlySelectedTab() {
		//if( m_presentation!=null && m_tabbedPane!=null) {
		m_updateInProgress = true;
		if(m_presentation[m_tabbedPane.getSelectedIndex()].equals("rows")) {
			comboBox_Orientation.setSelectedIndex(0);
		} else {
			comboBox_Orientation.setSelectedIndex(1);
		}
		m_updateInProgress = false;
		//}
	}



	protected void presentationModeChanged() {
		// update the m_presentation attribute when changed for a specific ExportConfigSheet

		int selectedTab = m_tabbedPane.getSelectedIndex();
		if(comboBox_Orientation.getSelectedIndex()==0)
		{
			m_presentation[selectedTab] = "rows";
		}
		else if(comboBox_Orientation.getSelectedIndex()==1)
		{
			m_presentation[selectedTab] = "columns";
		}
		
		
	}
	
	
    protected ExportConfig generateConfigFileFromGUI() {
        
        // this method creates an ExportConfig structure to export.
        ExportConfig ec = new ExportConfig();

		// global parameters 
		if(m_exporTypeCombobox.getSelectedIndex()==0) 
		{
			ec.format = "xlsx";
		} else if(m_exporTypeCombobox.getSelectedIndex()==1){
			ec.format = "tsv";
		}
		if(comboBox_NumberSeparator.getSelectedIndex()==0) 
		{
			ec.decimal_separator = ".";
		} else if(comboBox_NumberSeparator.getSelectedIndex()==1) 
		{
			ec.decimal_separator = ",";
		}
		if(comboBox_DateFormat.getSelectedIndex()==0) 
		{
			ec.date_format = "YYYY:MM:DD HH:mm:ss";
		}
		else if(comboBox_DateFormat.getSelectedIndex()==1) 
		{
			ec.date_format = "YYYY:MM:DD";
		}
		
        ec.data_export = new ExportDataExport();
        ec.data_export.all_protein_set = comboBox_ProteinSets.getSelectedItem().equals("All");

        ec.data_export.best_profile = comboBox_exportProfile.getSelectedItem().equals("Best");
        
//		// extra infos for default options (sent from server only)
        ec.format_values = null; //["xlsx","tsv"],
        ec.decimal_separator_values = null; //": [".",","],
        ec.date_format_values = null; //": ["YYYY:MM:DD HH:mm:ss","YYYY:MM:DD"],
        ec.sheet_presentation_values = null; //": ["rows","columns"]

        int nbActiveTabs = 0;
        for (int i = 0; i < m_tabbedPane.getTabCount(); i++) { // go through tab panes and jtables
            if (m_tabbedPane.isEnabledAt(i)) {
                nbActiveTabs++;
            }
        }
        ec.sheets = new ExportExcelSheet[nbActiveTabs];

        int usedTabNumber = 0; // the tab location for the new structure (smaller than the full table - disabled tabs)
        for (int i = 0; i < m_tabbedPane.getTabCount(); i++) { // go through tab panes and jtables
            if (m_tabbedPane.isEnabledAt(i)) { // save only enabled panes (hence excel sheets)

                // get the jtable out of the jpane...
                JPanel panelTemp = (JPanel) m_tabbedPane.getComponentAt(i);
                JScrollPane jsp = (JScrollPane) panelTemp.getComponent(0);
                JTable tableRef = (JTable) jsp.getViewport().getComponents()[0];

                int nbRows = tableRef.getRowCount();
                int nbSelectedRows = 0;
                for (int row = 0; row < nbRows; row++) { // count selected rows to be exported
                    
                    if (tableRef.getValueAt(row, 2).equals(true)) {
                        nbSelectedRows++;
                    }
                }
                ec.sheets[usedTabNumber] = new ExportExcelSheet();

                ec.sheets[usedTabNumber].id = m_sheetId[i];
                ec.sheets[usedTabNumber].title = m_sheetTitle[i];
                ec.sheets[usedTabNumber].presentation = m_presentation[i];

                ec.sheets[usedTabNumber].fields = new ExportExcelSheetField[nbSelectedRows];

                // copy all selected sheet fields into new structure
                int newStructRow = 0; // position in new sheet structure 
                for (int currentRow = 0; currentRow < nbRows; currentRow++) {
                    
                    if (tableRef.getValueAt(currentRow, 2).equals(true)) { // if selected row then add it
                        ec.sheets[usedTabNumber].fields[newStructRow] = new ExportExcelSheetField();

                        ec.sheets[usedTabNumber].fields[newStructRow].id = tableRef.getValueAt(currentRow, 0).toString();
                        ec.sheets[usedTabNumber].fields[newStructRow].title = tableRef.getValueAt(currentRow, 1).toString();
                        newStructRow++;
                    }
                }

                usedTabNumber++;
            }
        }

        return ec;

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
                    } else {
                        m_fchooser.setFileFilter(existFilter);
                    }
                }

                String textFile = m_fileTextField.getText().trim();

                if (textFile.length() > 0) {
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
                        absolutePath += "." + exporterInfo.getFileExtension();
                    }
                    m_fileTextField.setText(absolutePath);
                }
            }
        });

        c.gridx += 2;
        c.gridwidth = 1;
        exportPanel.add(addFileButton, c);

        if ((m_exportType == ExporterFactory.EXPORT_FROM_SERVER || m_exportType == ExporterFactory.EXPORT_XIC) && m_showExportAllPSMsChB) {
            //Allow specific parameter in this case
            c.gridy++;
            c.gridx = 0;
            c.gridwidth = 2;
            //m_exportAllPSMsChB = new JCheckBox(" Export all PSMs");
            //m_exportAllPSMsChB.setBounds(6, 78, 114, 20);
    	    //insidePanel.add(m_exportAllPSMsChB);
    	    
            //exportPanel.add(m_exportAllPSMsChB, c);
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

    public Boolean exportAllPSMs() {
        if (m_exportType == ExporterFactory.EXPORT_FROM_SERVER) {
            return m_exportAllPSMsChB.isSelected();
        } else {
            return null;
        }
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
            String[] options = {"Yes", "No"};
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

        // check config
        ExportConfig config  = generateConfigFileFromGUI();
        String msgError = checkTitles(config);
        if (!msgError.isEmpty()){
            JOptionPane.showMessageDialog(this, msgError);
            return false;
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
            m_exportName = null;
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

    /***
     * returns the JSON String corresponding to the export configuration
     * @return 
     */
    public String getExportConfig() {
        logger.debug("getExportConfig");
        m_exportConfig = generateConfigFileFromGUI();
        return m_exportConfig == null ? null : new GsonBuilder().create().toJson(m_exportConfig);
    }
    
    public String getFileExtension(){
        m_exportConfig = generateConfigFileFromGUI();
        return m_exportConfig == null ? null : m_exportConfig.format;
    }

    /**
     * set the defaultConfiguration
     *
     * @param configStr the JSON string
     */
    public void setDefaultExportConfig(String configStr) {
    	 
        logger.debug("setDefaultExportConfig");
        m_configFile.setText(""); 
        m_exportDefaultConfig = new Gson().fromJson(configStr, ExportConfig.class);
        fillExportPossibleValues(m_exportDefaultConfig);
        if (m_exportDefaultConfig != null) {
            fillExportFormatTable(m_exportDefaultConfig, m_exportConfig);
        }
    }
    
    /* return true if the configuration is ok regarding the titles (should not be empty and 1 sheet can not contain 2 same title) */
    private String checkTitles(ExportConfig config){
        String errorsOnConfig = "";
        ExportExcelSheet[] allSheets =  config.sheets;
        int s = 1;
        for (ExportExcelSheet sheet : allSheets) {
            if (sheet.title == null || sheet.title.trim().isEmpty()){
                errorsOnConfig +="The sheet at position "+(s)+" has no title! \n";
            }
            ExportExcelSheetField[] allFields = sheet.fields;
            int f = 0;
            for(ExportExcelSheetField field : allFields){
                if (field.title == null || field.title.trim().isEmpty()){
                    errorsOnConfig += "The field in the sheet "+sheet.title+" at position "+(f+1)+" has no title! \n";
                }else if (sheet.containsFieldTitle(field.title, f)){
                    errorsOnConfig += "The field "+field.title+" in the sheet "+sheet.title+" (at position "+(f+1)+") is already defined. \n";
                }
                f++;
            }
            s++;
        }
        return errorsOnConfig;
    }
    
}
