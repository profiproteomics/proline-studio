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
import java.util.HashMap;
import java.util.Iterator;
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
    
    private final static String EXPORT_PROTEIN_ALL = "All";
    private final static String EXPORT_PROTEIN_VALIDATED = "Validated only";
    private final static String[] EXPORT_PROTEIN_VALUES = new String[]{EXPORT_PROTEIN_VALIDATED, EXPORT_PROTEIN_ALL};

    private static CustomExportDialog m_singletonImageDialog = null;
    private static CustomExportDialog m_singletonImage2Dialog = null;
    private static CustomExportDialog m_singletonExcelDialog = null;
    private static CustomExportDialog m_singletonServerDialog = null;

    private int m_exportType;

    private JTextField m_fileTextField;
    private JComboBox m_exporTypeCombobox;
    private JCheckBox m_exportAllPSMsChB;
    private Boolean m_showExportAllPSMsChB;

    // true if the user has to choose a file, false if it's a directory, in case of tsv or multi export
    private static boolean m_fileExportMode;

    private JXTable m_table = null;
    private JPanel m_panel = null;
    private ImageExporterInterface m_imageExporter = null;

    private JFileChooser m_fchooser;
    private JFileChooser m_exportFchooser;
    private List<FileNameExtensionFilter> m_filterList = new ArrayList<>();
    private static final String jsonExtension = "json";
    private static final String tsvExtension = "tsv";

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
    public ExportConfig m_exportConfig;
    private ExportConfig m_exportDefaultConfig;
    private JLabel lbl_exportType;

    //private String[] m_presentation; // to complete information not displayed in jtable nor tabpane.
    //private String[] m_sheetId;  // to complete information not displayed in jtable nor tabpane.
    //private String[] m_sheetTitle;  // to complete information not displayed in jtable nor tabpane.
    protected boolean m_updateInProgress = true; // indicate when the table is built (to avoid calling event handler on every table update)
    public HashMap<String,String> m_tabTitleIdHashMap; // <title,id> keeps track of id/title for tabs, in case of renaming.
    public HashMap<String,String> m_presentationHashMap; // sheetId,presentation
    private JPanel panel_1;

    private JLabel lblExportProfile;

    private JComboBox comboBox_exportProfile;

	private JLabel lblMouseRightclickOn;

    public static CustomExportDialog getDialog(Window parent, JXTable table, String exportName) {
        if (m_singletonExcelDialog == null) {
            m_singletonExcelDialog = new CustomExportDialog(parent, ExporterFactory.EXPORT_TABLE, true);
        }

        m_singletonExcelDialog.m_table = table;
        m_singletonExcelDialog.m_exportName = exportName;

        return m_singletonExcelDialog;
    }

    public static CustomExportDialog getDialog(Window parent, JPanel panel, String exportName) {
        if (m_singletonImageDialog == null) {
            m_singletonImageDialog = new CustomExportDialog(parent, ExporterFactory.EXPORT_IMAGE, true);
        }

        m_singletonImageDialog.m_panel = panel;
        m_singletonImageDialog.m_imageExporter = null;
        m_singletonImageDialog.m_exportName = exportName;

        return m_singletonImageDialog;
    }

    public static CustomExportDialog getDialog(Window parent, JPanel panel, ImageExporterInterface imageExporter, String exportName) {
        if (m_singletonImage2Dialog == null) {
            m_singletonImage2Dialog = new CustomExportDialog(parent, ExporterFactory.EXPORT_IMAGE2, true);
        }

        m_singletonImage2Dialog.m_panel = panel;
        m_singletonImage2Dialog.m_imageExporter = imageExporter;
        m_singletonImage2Dialog.m_exportName = exportName;

        return m_singletonImage2Dialog;
    }

    public static CustomExportDialog getDialog(Window parent, Boolean showExportAllPSMsOption, boolean fileExportMode) {
        m_fileExportMode = fileExportMode;
        if (m_singletonServerDialog == null) {
            m_singletonServerDialog = new CustomExportDialog(parent, ExporterFactory.EXPORT_FROM_SERVER, showExportAllPSMsOption, fileExportMode);
        } else if (!m_singletonServerDialog.m_showExportAllPSMsChB.equals(showExportAllPSMsOption)) {
            m_singletonServerDialog = new CustomExportDialog(parent, ExporterFactory.EXPORT_FROM_SERVER, showExportAllPSMsOption, fileExportMode);
        }
        return m_singletonServerDialog;
    }

    public static CustomExportDialog getDialog(Window parent, Boolean showExportAllPSMsOption, int exportType, boolean fileExportMode) {
        if (m_singletonServerDialog == null) {
            m_singletonServerDialog = new CustomExportDialog(parent, exportType, showExportAllPSMsOption, fileExportMode);
        } else if (!m_singletonServerDialog.m_showExportAllPSMsChB.equals(showExportAllPSMsOption)) {
            m_singletonServerDialog = new CustomExportDialog(parent, exportType, showExportAllPSMsOption, fileExportMode);
        }

        return m_singletonServerDialog;
    }

    public void setTask(DefaultDialog.ProgressTask task) {
        m_task = task;
    }

    private CustomExportDialog(Window parent, int type, boolean fileExportMode) {
        this(parent, type, null, fileExportMode);
    }

    private CustomExportDialog(Window parent, int type, Boolean showExportAllPSMsOption, boolean fileExportMode) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);
        m_showExportAllPSMsChB = showExportAllPSMsOption;
        m_exportType = type;
        m_fileExportMode = fileExportMode;

        setTitle("Export");
        //setButtonVisible(BUTTON_HELP, true);
        setHelpURL("http://biodev.extra.cea.fr/docs/proline/doku.php?id=how_to:studio:exportdata");

        m_tabTitleIdHashMap = new HashMap<String,String>(); // this is used to store tab id/tab title matching
		
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
        FileNameExtensionFilter filterJson = new FileNameExtensionFilter("Custom Export Config (." + jsonExtension + ")", jsonExtension);
        m_exportFchooser.setFileFilter(filterJson);

    }
    
    public boolean isFileExportMode(){
        return m_fileExportMode ;
    }
    
    private boolean isTsv(){
        return  m_exporTypeCombobox.getSelectedItem() != null && m_exporTypeCombobox.getSelectedItem().toString().contains(tsvExtension);
    }
    
        
    private void loadExportConfig() {
        // decode json 
        m_exportConfig = new ExportConfig();
        String jsonString = "";
        Path filePath = Paths.get(m_configFile.getText());
        try {
            jsonString = new String(Files.readAllBytes(filePath));
        } catch (IOException e) {

            logger.error("Error while loading config " + e);
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
//			if(param.format_values!=null) {
//				m_exporTypeCombobox.setModel(new DefaultComboBoxModel(param.format_values));
//			}
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
			if(param.data_export.all_protein_set)
			{
				comboBox_ProteinSets.setSelectedIndex(0);
			} else {
				comboBox_ProteinSets.setSelectedIndex(1);
			}
			comboBox_exportProfile.setModel(new DefaultComboBoxModel(new String[] {"Best", "All"}));
			if(param.data_export.best_profile) 
			{
				System.out.println("best profile or not: TRUE***");
				comboBox_exportProfile.setSelectedIndex(0);	
			} 	else {
				System.out.println("best profile or not: ---FALSE");
				comboBox_exportProfile.setSelectedIndex(1);
			}
			
			
		}
		
		
	}

    private void selectLoadedExportValues(ExportConfig param) {
        if (param != null) {

            if (param.date_format != null) {
                comboBox_DateFormat.setSelectedItem(param.date_format);
            }

            if (param.decimal_separator != null) {
                comboBox_NumberSeparator.setSelectedItem(param.decimal_separator);
            }

            if (param.data_export.all_protein_set) {
                comboBox_ProteinSets.setSelectedItem(EXPORT_PROTEIN_ALL);
            } else {
                comboBox_ProteinSets.setSelectedItem(EXPORT_PROTEIN_VALIDATED);
            }
            if (param.data_export.best_profile) {
                comboBox_exportProfile.setSelectedIndex(0);
            } else {
                comboBox_exportProfile.setSelectedIndex(1);
            }

        }

    }

    private void fillExportFormatTable(ExportConfig defaultParam, ExportConfig param) {
		//reset panes:
	
		m_updateInProgress = true;
		m_tabbedPane.removeAll();	
		
		m_presentationHashMap = new HashMap<String,String>();
		// get list of sheets from defaut.
		m_tabTitleIdHashMap.clear(); 
		for(int i =0;i<defaultParam.sheets.length;i++) {
			m_tabTitleIdHashMap.put(defaultParam.sheets[i].title, defaultParam.sheets[i].id);
		
		}
		ArrayList<String> addedTabs = new ArrayList<String>(); // tabs added (which are in default and custom)
		// create tab panes
		if(param!=null) {
			for(int i = 0; i<param.sheets.length;i++) {
				System.out.println("processing custom tab sheet " + i);
				panel = new JPanel();
				if(m_tabTitleIdHashMap.containsValue(param.sheets[i].id)) 
				{ 
					m_tabbedPane.addTab(param.sheets[i].title, null, panel, null);
					m_presentationHashMap.put(param.sheets[i].id, param.sheets[i].presentation);
					// put id in tooltip in order to find the tab title from the tooltip even if renamed.
					// TODO: find a better way...
					m_tabbedPane.setToolTipTextAt(i, param.sheets[i].id /*"Right click to Enable/Disable"*/);
					addedTabs.add(param.sheets[i].id);
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
					
		
					//CustomExportTableModel tableModel = new CustomExportTableModel();
					DefaultTableModel tableModel = new javax.swing.table.DefaultTableModel(
							
							new Object [][] {
		
				            }, 
				            new String[] {
									"Internal field name", "Displayed field name (editable)", "Shown"
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
	//				m_tabbedPane.setEnabledAt(i, defaultParam.sheets[i].default_displayed); // true if from a saved file also
					//m_tabbedPane.setToolTipTextAt(i, "Right click to Enable/Disable");
				
					// now add the fields
					//HashMap<String,String> defaultFieldsHashMap = new HashMap<String,String>();
					//String defaultSheetId = m_tabTitleIdHashMap.get(param.sheets[i].title);
					// add fields contained both in param and defaultparam
					ArrayList<String> defaultFieldsList = getFieldsFromParamSheet(defaultParam.sheets,param.sheets[i].id);
					ArrayList<String> addedFieldsList = new ArrayList<String>();// to know which fields have already been added
					
					for (int j=0;j<param.sheets[i].fields.length;j++) {
						
						// if the field to add is contained in default field list
						if(defaultFieldsList.contains(param.sheets[i].fields[j].id)) {
							Vector v = new Vector();
							v.add(param.sheets[i].fields[j].id);
							v.add(param.sheets[i].fields[j].title);
							v.add(true);
							tableModel.addRow(v);
							addedFieldsList.add(param.sheets[i].fields[j].id); 
						} 
					}
					// now add the remaining default fields not already added from custom config
					int sheetIndexInDefaultConfig = getIndexOfSheet(defaultParam, param.sheets[i].id);// find the right sheet in default config
					for(int j=0;j<defaultParam.sheets[sheetIndexInDefaultConfig].fields.length;j++) { 
						if(!addedFieldsList.contains(defaultParam.sheets[sheetIndexInDefaultConfig].fields[j].id)) {
							// add the remaining fields to add
							Vector v = new Vector();
							v.add(defaultParam.sheets[sheetIndexInDefaultConfig].fields[j].id);
							v.add(defaultParam.sheets[sheetIndexInDefaultConfig].fields[j].title);
							v.add(false);
							tableModel.addRow(v);
						}
					}
					table.setModel(tableModel);
				} else {
					// if not in default, do not add it!
				}
				
			}
		}
		m_updateInProgress = false;
	// now add the remaining default sheets that are not already added.
		
			
		int nbCustomTabsAdded = addedTabs.size();
		for(int i=0; i< defaultParam.sheets.length ; i++) {
			System.out.println("--> checking for missing heet: "+ defaultParam.sheets[i].id);	
			
			if(!addedTabs.contains(defaultParam.sheets[i].id)) {
				nbCustomTabsAdded++;
				// add the missing tab
				panel = new JPanel();	
				m_tabbedPane.addTab(defaultParam.sheets[i].title, null, panel, null);
				m_presentationHashMap.put(defaultParam.sheets[i].id, defaultParam.sheets[i].presentation);
				// put id in tooltip in order to find the tab title from the tooltip even if renamed.
				// TODO: find a better way...
				m_tabbedPane.setToolTipTextAt(nbCustomTabsAdded  -1, defaultParam.sheets[i].id /*"Right click to Enable/Disable"*/);
				if(param!=null) 
				{
					m_tabbedPane.setEnabledAt(nbCustomTabsAdded  -1, false); // disable default not saved tab
				}
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
				
	
				//CustomExportTableModel tableModel = new CustomExportTableModel();
				DefaultTableModel tableModel = new javax.swing.table.DefaultTableModel(
						
						new Object [][] {
	
			            }, 
			            new String[] {
								"Internal field name", "Displayed field name (editable)", "Shown"
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
				
				
				// add the default fields for the missing default tab sheets
				for(int j=0;j<defaultParam.sheets[i].fields.length;j++) {
					Vector v2 = new Vector();
					v2.add(defaultParam.sheets[i].fields[j].id);
					v2.add(defaultParam.sheets[i].fields[j].title);
					v2.add(defaultParam.sheets[i].fields[j].default_displayed);
					tableModel.addRow(v2);
				}
				table.setModel(tableModel);
			}
			
				
		}
			
		
	}
		
		

    

    private int getIndexOfSheet(ExportConfig config, String sheetId) {
		int index=-1 ; //config.sheets.length; // if not found, then value is size of table
    	for(int i=0;i<config.sheets.length;i++) {
    		if(config.sheets[i].id.equals(sheetId)) {
    			index=i;
    		}
    	}
    	return index; 
    	
    }
    
    private ArrayList<String> getFieldsFromParamSheet(ExportExcelSheet[] sheets, String sheetId) {
    	ArrayList<String> fieldsId = new ArrayList<String>();
    	for(int i=0;i<sheets.length;i++) {
    		if(sheets[i].id.equals(sheetId)) {
    			
    			for(int j=0;j<sheets[i].fields.length;j++) {
    				fieldsId.add(sheets[i].fields[j].id);
    			}
    		}
    	}
    	
		return(fieldsId);
    }
    private String getCustomFieldIfFieldContainedInFieldsList(ExportExcelSheetField sheetFieldToCompare, ExportExcelSheetField[] fields) {
        // this method checks if sheetFieldToCompare is contained in the fields list of elements. It compares by the id only and return
        for (ExportExcelSheetField field : fields) {
            if (field.id.equals(sheetFieldToCompare.id)) {
                return field.title; // return the custom title field
            }
        }
        return null;
    }

    public final JPanel createCustomExportPanel() {

        // JPanel exportPanel = new JPanel(new GridBagLayout());
        final JPanel exportPanel = new JPanel();
        //exportPanel.setLayout(null);
        //exportPanel.setSize(new Dimension(400, 250));

        // added 
        setBounds(100, 100, 600, 600); // gives absolute position in x, relative to the main ProlineStudio window...
        setPreferredSize(new Dimension(580, 250)); // size of the main CustomExportDialog window.

        final JPanel insidePanel = new JPanel(null);
        exportPanel.add(insidePanel);
        insidePanel.setPreferredSize(new Dimension(580, 600)); //size of the inside panel that contains all parameters to set

        final JPanel optionPane = new JPanel();
        optionPane.setVisible(false);
        optionPane.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
        optionPane.setSize(new Dimension(600, 650));
        optionPane.setSize(new Dimension(600, 650));
        optionPane.setPreferredSize(new Dimension(710, 500));
        optionPane.setBounds(new Rectangle(10, 105, 560, 446));// position du panneau dans la boite.

        insidePanel.setLayout(null);
        insidePanel.add(optionPane);
        optionPane.setLayout(null);

        panel_1 = new JPanel();
        panel_1.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
        panel_1.setBounds(10, 102, 540, 300);
        optionPane.add(panel_1);
        panel_1.setLayout(null);

        lblExportExcelTabs = new JLabel("Configuration file:");
        lblExportExcelTabs.setBounds(10, 15, 110, 14);
        optionPane.add(lblExportExcelTabs);

        scrollPane = new JScrollPane();

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

            @Override
            public Class getColumnClass(int columnIndex) {
                return columnTypes[columnIndex];
            }
            boolean[] columnEditables = new boolean[]{
                false, true, true
            };

            @Override
            public boolean isCellEditable(int row, int column) {
                return columnEditables[column];
            }
        });
        table.getColumnModel().getColumn(0).setPreferredWidth(141);

        // *-*-*-
        comboBox_Orientation = new JComboBox();
        comboBox_Orientation.setBounds(79, 8, 91, 20);
        panel_1.add(comboBox_Orientation);
        comboBox_Orientation.setModel(new DefaultComboBoxModel(new String[]{"rows", "columns"}));
        comboBox_Orientation.setName("");
        comboBox_Orientation.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!m_updateInProgress) {
                    presentationModeChanged();
                }
            }
        });

        lblOrientation = new JLabel("Orientation:");
        lblOrientation.setBounds(10, 11, 91, 14);
        panel_1.add(lblOrientation);

        m_configFile = new JTextField();
        m_configFile.setText("");
        m_configFile.setBounds(100, 12, 255, 20);
        optionPane.add(m_configFile);
        m_configFile.setColumns(10);

        m_exporTypeCombobox = new JComboBox();
        m_exporTypeCombobox.setModel(new DefaultComboBoxModel(new String[]{"xlsx", "xls", "csv", "tsv"}));

        comboBox_DateFormat = new JComboBox();
        comboBox_DateFormat.setModel(new DefaultComboBoxModel(new String[]{"YYYYMMDD HH:mm:ss", "DDMMYYYY HH:mm:ss", "MMDDYYYY HH:mm:ss"}));
        comboBox_DateFormat.setBounds(88, 41, 161, 20);
        optionPane.add(comboBox_DateFormat);

        lblDateFormat = new JLabel("Date format:");
        lblDateFormat.setBounds(10, 44, 68, 14);
        optionPane.add(lblDateFormat);

        comboBox_NumberSeparator = new JComboBox();
        comboBox_NumberSeparator.setModel(new DefaultComboBoxModel(new String[]{".", ","}));
        comboBox_NumberSeparator.setBounds(109, 69, 49, 20);
        optionPane.add(comboBox_NumberSeparator);

        lblNumberSeparator = new JLabel("Number separator:");
        lblNumberSeparator.setBounds(10, 72, 110, 14);
        optionPane.add(lblNumberSeparator);

        lblProteinSets = new JLabel("Protein sets:");
        lblProteinSets.setBounds(349, 44, 89, 14);
        optionPane.add(lblProteinSets);

        comboBox_ProteinSets = new JComboBox();
        comboBox_ProteinSets.setModel(new DefaultComboBoxModel(EXPORT_PROTEIN_VALUES));
        comboBox_ProteinSets.setBounds(429, 43, 121, 20);
        optionPane.add(comboBox_ProteinSets);

        btnNewButton = new JButton("Save");
        btnNewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveConfigFile();
            }
        });
        btnNewButton.setIcon(IconManager.getIcon(IconManager.IconType.SAVE_SETTINGS));
        btnNewButton.setBounds(468, 11, 82, 23);
        optionPane.add(btnNewButton);

        btnLoad = new JButton("Load");
        btnLoad.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadConfigFile();
            }
        });
        btnLoad.setIcon(IconManager.getIcon(IconManager.IconType.OPEN_FILE));
        btnLoad.setBounds(365, 11, 89, 23);
        optionPane.add(btnLoad);

		m_tabbedPane = new DnDTabbedPane(JTabbedPane.BOTTOM);
		m_tabbedPane.addChangeListener(new ChangeListener() {
	        public void stateChanged(ChangeEvent e) {
	        	//System.out.println("DND potentially performed!");
	        	//recalculateTabsIds(); // because drag n drop looses tolltiptext info, rebuild it
	        	//recalculateTabTitleIdHashMap();
	        	updatePresentationModeForNewlySelectedTab();
	        	
	        }
	    });
		
        m_tabbedPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent arg0) {
                if (SwingUtilities.isRightMouseButton(arg0)) {
                    boolean tabEnabled = m_tabbedPane.isEnabledAt(m_tabbedPane.indexAtLocation(arg0.getX(), arg0.getY()));
                    int tabIndex = m_tabbedPane.indexAtLocation(arg0.getX(), arg0.getY());
                    m_tabbedPane.setEnabledAt(tabIndex, !tabEnabled);
                    repaint();
                    arg0.consume();
                }

            }
        });

        m_tabbedPane.setBounds(10, 36, 520, 248);
        // add listener to allow tab rename:
        TabTitleEditListener l = new TabTitleEditListener(m_tabbedPane, this);
        m_tabbedPane.addMouseListener(l);

        panel_1.add(m_tabbedPane);
        m_tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);


		
		lblMouseRightclickOn = new JLabel("Mouse Right-Click on tab to enable/disable sheet");
		lblMouseRightclickOn.setBounds(282, 11, 248, 14);
		panel_1.add(lblMouseRightclickOn);
		
        comboBox_exportProfile = new JComboBox();
        comboBox_exportProfile.setModel(new DefaultComboBoxModel(new String[]{"Best", "All"}));
        comboBox_exportProfile.setBounds(448, 69, 102, 20);
        optionPane.add(comboBox_exportProfile);

       
        lblExportProfile = new JLabel("Export profile:");
        lblExportProfile.setBounds(349, 72, 89, 14);
        optionPane.add(lblExportProfile);

        lblExportToFile = new JLabel("Export to file:");
        lblExportToFile.setBounds(10, 15, 77, 27);
        insidePanel.add(lblExportToFile);

        m_fileTextField = new JTextField();
        m_fileTextField.setBounds(86, 15, 374, 27);
        insidePanel.add(m_fileTextField);
        m_fileTextField.setColumns(50);

        // ---
        chk_ExportOptions = new JCheckBox("Custom export");
        chk_ExportOptions.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                optionPane.setVisible(chk_ExportOptions.isSelected());
                if (!chk_ExportOptions.isSelected()) {
                    //disable custom parameters and restore default ones
                	m_exportConfig = m_exportDefaultConfig;
                    fillExportFormatTable(m_exportDefaultConfig, m_exportConfig);
                    recalculateTabsIds();
                    recalculateTabTitleIdHashMap();
                    //m_exportConfig = m_exportDefaultConfig; // copy config to allow modifications: TODO: check if copy by value better
                }
                setSize(new Dimension(exportPanel.getWidth() + 6 /* drift? */, 200 + 400 * (chk_ExportOptions.isSelected() ? 1 : 0))); // elongate the window if option is selected
                //setPreferredSize(new Dimension(exportPanel.getWidth() + 6 /* drift? */, 200 + 400 * (chk_ExportOptions.isSelected() ? 1 : 0))); // elongate the window if option is selected
            }
        });
        chk_ExportOptions.setBounds(476, 71, 124, 27);
        insidePanel.add(chk_ExportOptions);

        final JButton addFileButton = new JButton("");
        addFileButton.setBounds(470, 14, 27, 30);
        insidePanel.add(addFileButton);

        addFileButton.setIcon(IconManager.getIcon(IconManager.IconType.OPEN_FILE));

        addFileButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                //ExporterFactory.ExporterInfo exporterInfo = (ExporterFactory.ExporterInfo) m_exporTypeCombobox.getSelectedItem();
                if (m_exporTypeCombobox.getSelectedItem() != null) {
                    FileNameExtensionFilter filter = null;
                    if (m_exporTypeCombobox.getSelectedItem().toString().contains("xls")) {
                        filter = new FileNameExtensionFilter("Excel File (.xlsx)", "xlsx");
                    } else if (m_exporTypeCombobox.getSelectedItem().toString().contains("tsv")) {
                        filter = new FileNameExtensionFilter("Tabulation Separated Values (.tsv)", "tsv");
                    }
                    if (m_fileExportMode){
                        // file mode
                        m_fchooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                        m_fchooser.addChoosableFileFilter(filter);
                        m_filterList.add(filter);
                        m_fchooser.setFileFilter(filter);
                    }else{
                        // directory
                        m_fchooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    }
                }

                String textFile = m_fileTextField.getText().trim();
                if (textFile.length() > 0) {
                    File currentFile = new File(textFile);
                    if (currentFile.isDirectory()) {
                        m_fchooser.setCurrentDirectory(currentFile);
                    } else {
                        if (m_fileExportMode){
                            m_fchooser.setSelectedFile(currentFile);
                        }else{
                            m_fchooser.setCurrentDirectory(currentFile);
                        }
                    }
                }

                int result = m_fchooser.showOpenDialog(addFileButton);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File file = m_fchooser.getSelectedFile();

                    String absolutePath = file.getAbsolutePath();
                    String fileName = file.getName();
                    if (m_fileExportMode){
                        if (fileName.indexOf('.') == -1) {
                            if (isTsv()){
                                absolutePath += "." + tsvExtension;
                            }else{
                                absolutePath += "." + "xlsx"; //+ exporterInfo.getFileExtension();
                            }
                        }
                    }else{
                        if (!fileName.endsWith("\\")){
                            absolutePath += "\\";
                        }
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
        setPreferredSize(new Dimension(580, 200)); // size of the main CustomExportDialog window.
        //----
        return exportPanel;

    }

    protected void recalculateTabTitleIdHashMap() {
    	//System.out.println("RECALCULTAING TAB title id hashmap");
    	//because after renamed, rebuild it in order to keep tabs ids stored .
    	m_tabTitleIdHashMap.clear();
    	for(int i = 0;i<m_tabbedPane.getTabCount();i++) {
    		
    			m_tabTitleIdHashMap.put(m_tabbedPane.getTitleAt(i),m_tabbedPane.getToolTipTextAt(i));
    	}
    		
		
	}
    

    protected void recalculateTabsIds() {
    	//because drag n drop looses tooltiptext info, rebuild it in order to keep tabs ids stored there.
    	
    	System.out.println("RECALCULTAING tab ids ");
    			
    	// 1st: get the list of ids from defaultParam
    	// 2nd: find which one is missing from list
    	// 3: add the missing one to tooltiptext.
    	if(m_exportDefaultConfig == null)  {
    		return;
    	}
    	if(m_exportDefaultConfig.sheets.length==0) {
    		return;
    	}
    	// 1st
    	ArrayList<String> idFullList = new ArrayList<String>();
    	for(int i = 0;i<m_exportDefaultConfig.sheets.length;i++) {
    		idFullList.add(m_exportDefaultConfig.sheets[i].id);
    	}
    	
    	
		int removedAtIndex=-1;
    	//HashMap<String,String> tabIdTitleHashMap = new HashMap<String,String>(); // title, id
    	for(int i = 0;i<m_tabbedPane.getTabCount();i++) {
    		
    		if(m_tabbedPane.getToolTipTextAt(i)==null) { // if tool tip has been erased
    			removedAtIndex = i;
    			
    			String currentTabTitle = m_tabbedPane.getTitleAt(removedAtIndex);
    	    	String tabId= m_tabTitleIdHashMap.get(currentTabTitle);
    	    	
				//m_tabbedPane.setToolTipTextAt(removedAtIndex, tabId);
//    	    	System.out.println("RECALCULTAING tab id: " + removedAtIndex + " currentTabTitle:" + currentTabTitle +
//    	    			",id= " + tabId);
        	
    		} else {
    			idFullList.remove(m_tabbedPane.getToolTipTextAt(i));
    		// recreate complete list but the one missing
    			//tabIdTitleHashMap.put(m_tabbedPane.getTitleAt(i),m_tabbedPane.getToolTipTextAt(i));
    		}
    	}
    	if(removedAtIndex>-1) {
    		if(idFullList.size()>1) {
    			System.out.println("Problem: more than one missing ID");
    		}  else 
    		if(idFullList.size()==1) {
    			System.out.println("Fixed the missing id: " + idFullList.get(0));
    			m_tabbedPane.setToolTipTextAt(removedAtIndex, idFullList.get(0));
    		} 
	    }
    	// deduce which one is missing by removing the found ones:
//    	for (String tabTitle : tabIdTitleHashMap.keySet()) { 
//			m_tabTitleIdHashMap.remove(tabTitle);
//		}
//    	if(m_tabTitleIdHashMap.size())
		
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
            	// reorder param to contain all fields...
	            
	            //ExportConfig ordered = completeParamWithNonUsedDefaultsAndReturnSheetsToDisable(m_exportDefaultConfig,m_exportConfig);
	       	    fillExportFormatTable(m_exportDefaultConfig, m_exportConfig);
	            //disableTabsOnlyInDefaultParam(m_exportDefaultConfig,m_exportConfig);
	       	    // make the ordered config the new custom param config (even with tabs that were not saved previously)
	            //m_exportConfig = ordered;
	       	  updatePresentationModeForNewlySelectedTab();
	       	  selectLoadedExportValues(m_exportConfig);
	       	
	       	  //m_exportConfig = m_exportDefaultConfig; // allows all fields to be present in m_exportConfig, in case some tabs were disabled and avoid problems in processing...
          } else {
          	System.out.println("no default config loaded!");
          }

        }

    }

	private ExportConfig completeParamWithNonUsedDefaultsAndReturnSheetsToDisable(ExportConfig defaultParam, ExportConfig param) {
		//
		//reorder default tabs according to custom param file. (and hence solve the problem of after loading a custom file, no right order with tabs)
		// 
		ExportConfig orderedExportConfig = new ExportConfig();
		
		ArrayList<String> usedSheetsIds = new ArrayList<String>();
		//ArrayList<ExportExcelSheet> sheetsArrayList = new ArrayList<ExportExcelSheet>(Arrays.asList(param.sheets));
		// size it to the default elements size
		orderedExportConfig.sheets = new ExportExcelSheet[defaultParam.sheets.length];
		
		// fill the array with 1- the custom param sheet elements
		for(int i=0; i<param.sheets.length;i++) {
			usedSheetsIds.add(param.sheets[i].id);
			System.out.println("sheet id used: " + param.sheets[i].id );
			// copy data (by value)
			orderedExportConfig.sheets[i] = new ExportExcelSheet();
			orderedExportConfig.sheets[i].id = param.sheets[i].id;
			orderedExportConfig.sheets[i].title = param.sheets[i].title;
			orderedExportConfig.sheets[i].presentation = param.sheets[i].presentation;
			orderedExportConfig.sheets[i].default_displayed = true; // to display them.

			orderedExportConfig.sheets[i].fields = new ExportExcelSheetField[defaultParam.sheets[i].fields.length];
			// TODO: check if size is bigger (in case defaultparam is smaller than previous version of param, if fields have been removed from default)
//			for(int debug=0;debug<param.sheets.length;debug++)
//			{
			int debug=i;
				System.out.println("param sheets:" +param.sheets.length + " orderedExport sheets: " + orderedExportConfig.sheets.length 
						+ " defaultPAram sheets:" + defaultParam.sheets.length);
				System.out.println("> nb fields for param:" + param.sheets[i].fields.length
						+  "> nb fields for ordered:" + orderedExportConfig.sheets[i].fields.length
						+  "> nb fields for default:" + defaultParam.sheets[i].fields.length);
//			}
			ArrayList<String> usedFieldsIds = new ArrayList<String>();
			for(int j=0;j<param.sheets[i].fields.length;j++) {
				System.out.println(" ++ param sheet °"+ i + " : " + param.sheets[i].id + " field:" + j + " : " + param.sheets[i].fields[j].id);
				usedFieldsIds.add(param.sheets[i].fields[j].id); // to check later if field already added or not
				orderedExportConfig.sheets[i].fields[j] = new ExportExcelSheetField();
				orderedExportConfig.sheets[i].fields[j].id = param.sheets[i].fields[j].id;
				orderedExportConfig.sheets[i].fields[j].title = param.sheets[i].fields[j].title;
				orderedExportConfig.sheets[i].fields[j].default_displayed = true; 
			}
			// add missing fields in present sheet in param compared to defautparam.
			int newIndex=param.sheets[i].fields.length;
			for(int j=0;j<defaultParam.sheets[i].fields.length;j++) {
				
				if(!usedFieldsIds.contains(defaultParam.sheets[i].fields[j].id)) {
				
					System.out.println("going through defaultparam sheet field j=" + j + "/" + defaultParam.sheets[i].fields.length);
					System.out.println("field id:" + defaultParam.sheets[i].fields[j].id);
					System.out.print("going through ordered sheet field i=" + newIndex + "/");
					System.out.println(orderedExportConfig.sheets[i].fields.length);
					orderedExportConfig.sheets[i].fields[newIndex] = new ExportExcelSheetField();
					
					orderedExportConfig.sheets[i].fields[newIndex].id = defaultParam.sheets[i].fields[j].id;
					orderedExportConfig.sheets[i].fields[newIndex].title = defaultParam.sheets[i].fields[j].title;
					orderedExportConfig.sheets[i].fields[newIndex].default_displayed = false;
					newIndex++;
				}
			}
			
		}
		
		
		
		
		// fill the array with 2- the remaining sheets from defaultParam
		int extraSheets=0;
		for(int i=0; i<defaultParam.sheets.length;i++) {
			System.out.println(i + ": checking if sheet id " + defaultParam.sheets[i].id + " is contained in usedsheet id");
			if(!usedSheetsIds.contains(defaultParam.sheets[i].id)) { // if sheet id is not already there...
				// add the missing sheets
				System.out.println(i + ": adding the missing sheet: " + defaultParam.sheets[i].id);
				//orderedExportConfig.sheets[param.sheets.length + extraSheets] = defaultParam.sheets[i];
				int newIndex = param.sheets.length + extraSheets;
				orderedExportConfig.sheets[newIndex] = new ExportExcelSheet();
				orderedExportConfig.sheets[newIndex].id = defaultParam.sheets[i].id;
				orderedExportConfig.sheets[newIndex].title = defaultParam.sheets[i].title;
				orderedExportConfig.sheets[newIndex].presentation = defaultParam.sheets[i].presentation;
				orderedExportConfig.sheets[newIndex].default_displayed = false; // to NOT display them as they were not saved

				orderedExportConfig.sheets[newIndex].fields = new ExportExcelSheetField[defaultParam.sheets[i].fields.length];
				for(int j=0;j<defaultParam.sheets[i].fields.length;j++) {
					orderedExportConfig.sheets[newIndex].fields[j] = new ExportExcelSheetField();
					System.out.println("new index, i,j,ordered size, param size:" +newIndex +","+ i  +","+ j  +","+ orderedExportConfig.sheets.length  +","+ param.sheets.length );
					
					
					orderedExportConfig.sheets[newIndex].fields[j].id = defaultParam.sheets[i].fields[j].id;
					orderedExportConfig.sheets[newIndex].fields[j].title = defaultParam.sheets[i].fields[j].title;
					orderedExportConfig.sheets[newIndex].fields[j].default_displayed = defaultParam.sheets[i].fields[j].default_displayed; 
				}
				extraSheets++;
			}
			
		}
		//orderedExportConfig.sheets = (ExportExcelSheet[]) sheetsArrayList.toArray();
		
		// add the missing data in the new export config:
		orderedExportConfig.data_export = param.data_export;
		orderedExportConfig.data_export.all_protein_set = param.data_export.all_protein_set;
		orderedExportConfig.data_export.best_profile = param.data_export.best_profile;
		orderedExportConfig.date_format = param.date_format;
		orderedExportConfig.decimal_separator = param.decimal_separator;
		orderedExportConfig.format = param.format;
		
		
		return(orderedExportConfig);
	}
	

	private void disableTabsOnlyInDefaultParam(ExportConfig defaultParam, ExportConfig param) {
		//
		//reorder default tabs according to custom param file. (and hence solve the problem of after loading a custom file, no right order with tabs)
		// 
		ExportConfig orderedExportConfig = new ExportConfig();
		
		ArrayList<String> usedSheetsIds = new ArrayList<String>();
		//ArrayList<ExportExcelSheet> sheetsArrayList = new ArrayList<ExportExcelSheet>(Arrays.asList(param.sheets));
	//	orderedExportConfig.sheets = new ExportExcelSheet[defaultParam.sheets.length];
		
		// fill the array with 1- the custom param elements
		for(int i=0; i<param.sheets.length;i++) {
			usedSheetsIds.add(param.sheets[i].id);
			System.out.println("sheet id used: " + param.sheets[i].id);
	//		orderedExportConfig.sheets[i] = param.sheets[i];
		}
		
		
		// fill the array with 2- the remaining elements from defaultParam
		//int extraSheets=0;
		ArrayList<String> tabsToDisable =new ArrayList<String>();
		for(int i=0; i<defaultParam.sheets.length;i++) {
			System.out.println(i + ": checking if sheet id " + defaultParam.sheets[i].id + " is contained in usedsheet id");
			if(!usedSheetsIds.contains(defaultParam.sheets[i].id)) { // if sheet id is not already there...
				// add the missing sheets
				//orderedExportConfig.sheets[param.sheets.length + extraSheets] = defaultParam.sheets[i];
				tabsToDisable.add(defaultParam.sheets[i].id);
				//extraSheets++;
			}
		}
		for(int i=0; i<defaultParam.sheets.length;i++) {
			if(tabsToDisable.contains(m_tabbedPane.getTitleAt(i))) {
				m_tabbedPane.setEnabledAt(i, false);
			}
		}
		
	}

	
	
	protected void updatePresentationModeForNewlySelectedTab() {
		System.out.println("1-updating presentation mode for newly selected tab: " + m_tabbedPane.getSelectedIndex());
		
		if(!m_updateInProgress ) { // update only when no update in progress
    		//if(m_exportConfig!= null) {
	    	
			System.out.println("2-updating presentation mode for newly selected tab: " + m_tabbedPane.getSelectedIndex());
			System.out.println();
			
			// reassign all tabs names and ids in case some have been moved around
			
			//ExportConfig param = m_exportConfig;
		
			//---
			System.out.println("updating presentation mode for newly selected tab: " + m_tabbedPane.getSelectedIndex());
			
	
				m_updateInProgress = true;
				//String selectedTabTitle = m_tabbedPane.getTitleAt(m_tabbedPane.getSelectedIndex());
				
				recalculateTabsIds();
				recalculateTabTitleIdHashMap();
				String selectedTabId = m_tabbedPane.getToolTipTextAt(m_tabbedPane.getSelectedIndex());
				if(selectedTabId==null) {
				//if(sheetIdToSheetIndex(selectedTabId)==m_exportConfig.sheets.length) {
					System.out.println("ERROR: did not find tab by its id :" +selectedTabId);
					
				} else {
					//if(m_exportConfig.sheets[sheetIdToSheetIndex(selectedTabId)].presentation.equals("rows")) {
					if(m_presentationHashMap.get(selectedTabId).equals("rows")) { 
						comboBox_Orientation.setSelectedIndex(0);
					} else {
						comboBox_Orientation.setSelectedIndex(1);
					}
				}
				m_updateInProgress = false;
		
		
			
    		}
    	//}
		
		//}
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
            if (!absolutePath.endsWith("." + jsonExtension)) {
                absolutePath += "." + jsonExtension;
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

   

    protected void presentationModeChanged() {
		// update the m_presentation attribute when changed for a specific ExportConfigSheet
		System.out.println("presentation mode changed to " + comboBox_Orientation.getSelectedItem());
		int selectedTab = m_tabbedPane.getSelectedIndex();
		recalculateTabsIds();
		recalculateTabTitleIdHashMap();
		String selectedTabId = m_tabbedPane.getToolTipTextAt(selectedTab);
		
		if(comboBox_Orientation.getSelectedIndex()==0)
		{ 
			
			// m_presentationHashMap (tabId, presentation mode)
			m_presentationHashMap.put(selectedTabId,"rows");
			//presentationHashMap.put(m_sheetIdHashMap.get(selectedTab),"rows");
			//m_exportConfig.sheets[sheetNameToSheetIndex(m_tabbedPane.getTitleAt(selectedTab))].presentation = "rows";
			//m_presentation[selectedTab] = "rows";
		
		}
		else if(comboBox_Orientation.getSelectedIndex()==1)
		{
			m_presentationHashMap.put(selectedTabId,"columns");
			//m_presentationHashMap.put(m_sheetIdHashMap.get(selectedTab),"columns");
			//m_exportConfig.sheets[sheetNameToSheetIndex(m_tabbedPane.getTitleAt(selectedTab))].presentation = "columns";
			//m_presentation[selectedTab] = "columns";
		}
	//	System.out.println("presentation mode changed for tab " + selectedTab + " to " +  m_presentationHashMap.get(m_sheetIdHashMap.get(selectedTab))); 
		
		
	}
	
	protected String tabTitleToTabId(String title) { // return the tab id from its known title (supposed to be unique)
//		for(int i=0;i<m_exportDefaultConfig.sheets.length;i++) {
//			if(m_exportDefaultConfig.sheets[i].title.equals(title)) {
//				return m_exportDefaultConfig.sheets[i].id; 
//			}
//		}
		 return m_tabTitleIdHashMap.get(title);
		
		//return null; // should not happen
	}
	
	protected int tabTitleToTabPosition(String tabTitle) {
		// returns the position in int for the specified tab title (excel sheet title)
		// it assumes the title names are unique
		for(int i=0;i<m_tabbedPane.getTabCount();i++) {
			if(m_tabbedPane.getTitleAt(i).equals(tabTitle)) {
				return i;
			}
		}
		return m_tabbedPane.getTabCount(); // return an out of range number, indicating it has not found the tab by its given name.
	}
	protected int sheetNameToSheetIndex(String sheetTitle) {
		// returns the position in int for the specified tab id (excel sheet title)
		// it assumes the names are unique
		for(int i=0;i<m_exportConfig.sheets.length ;i++) {
			if(m_exportConfig.sheets[i].title.equals(sheetTitle)) {
				return i;
			}
		}
		return m_exportConfig.sheets.length; // return an out of range number, indicating it has not found the sheet
	}
	
	protected int sheetIdToSheetIndex(String sheetId) {
		// returns the position in int for the specified tab id (excel sheet title)
		// it assumes the names are unique
		for(int i=0;i<m_exportConfig.sheets.length ;i++) {
			if(m_exportConfig.sheets[i].id.equals(sheetId)) {
				return i;
			}
		}
		return m_exportConfig.sheets.length; // return an out of range number, indicating it has not found the sheet
	}
	
	
	

	protected ExportConfig generateConfigFileFromGUI () {
		System.out.print("scanning table...");
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
        
	
		// extra infos for default options (sent from server only)
		ec.format_values= null  ; //["xlsx","tsv"],
	    ec.decimal_separator_values= null; //": [".",","],
	    ec.date_format_values= null ; //": ["YYYY:MM:DD HH:mm:ss","YYYY:MM:DD"],
	    ec.sheet_presentation_values= null; //": ["rows","columns"]
		
		
		System.out.print("parcours.../nb tab= "+  m_tabbedPane.getTabCount());
		
		int nbActiveTabs=0;
		for(int i = 0; i< m_tabbedPane.getTabCount() ;i++) { // go through tab panes and jtables
			if( m_tabbedPane.isEnabledAt(i)) { 
				nbActiveTabs++;
			}
		}
		System.out.println(">>\n " + nbActiveTabs + " active tabs");
		//ec.sheets = new ExportExcelSheet[nbActiveTabs];	// create the number of sheets that are enabled
		ec.sheets = new ExportExcelSheet[nbActiveTabs];
		
		System.out.println("*** ec sheets count:" + ec.sheets.length);	
		int usedTabNumber =0; // the tab location for the new structure (smaller than the full table - disabled tabs)
		for(int i = 0; i< m_tabbedPane.getTabCount() ;i++) { // go through tab panes and jtables
			if( m_tabbedPane.isEnabledAt(i)) { // save only enabled panes (hence excel sheets)
					
					// get the jtable out of the jpane...
					JPanel panelTemp = (JPanel) m_tabbedPane.getComponentAt(i);
					JScrollPane jsp = (JScrollPane) panelTemp.getComponent(0);
					JTable tableRef = (JTable) jsp.getViewport().getComponents()[0];
	
					System.out.println("668: row count:" + tableRef.getRowCount());
				
					int nbRows = tableRef.getRowCount();
					int nbSelectedRows =0;
					for(int row = 0 ; row < nbRows  ; row++) { // count selected rows to be exported
						System.out.println(" row " + row + " with id=" + tableRef.getValueAt(row, 0));
						if(tableRef.getValueAt(row, 2).equals(true)){
							nbSelectedRows++;
						}
					}
					ec.sheets[usedTabNumber] = new ExportExcelSheet();
					
					//ec.sheets[usedTabNumber].id = m_sheetId[i];
					//ec.sheets[usedTabNumber].id = m_sheetIdHashMap.get(i);
					ec.sheets[usedTabNumber].id = tabTitleToTabId(m_tabbedPane.getTitleAt(i));
//					ec.sheets[usedTabNumber].title = m_sheetTitle[i];
//					ec.sheets[usedTabNumber].presentation = m_presentation[i];
					//ec.sheets[usedTabNumber].title = m_sheetTitleHashMap.get(i);
					ec.sheets[usedTabNumber].title = m_tabbedPane.getTitleAt(i);
					ec.sheets[usedTabNumber].presentation = m_presentationHashMap.get(m_tabbedPane.getToolTipTextAt(i)); //m_exportConfig.sheets[i].presentation;
					
					ec.sheets[usedTabNumber].fields= new ExportExcelSheetField[nbSelectedRows];

					// copy all selected sheet fields into new structure
					int newStructRow=0; // position in new sheet structure 
					for(int currentRow = 0 ; currentRow<nbRows ; currentRow++) {
						System.out.println("current row:" + currentRow );
						if(tableRef.getValueAt(currentRow, 2).equals(true)){ // if selected row then add it
							ec.sheets[usedTabNumber].fields[newStructRow] = new ExportExcelSheetField();
							System.out.println("currentRow= " + currentRow + " i = " + i + " new struct row: " + newStructRow);
							ec.sheets[usedTabNumber].fields[newStructRow].id = tableRef.getValueAt(currentRow, 0).toString();
							ec.sheets[usedTabNumber].fields[newStructRow].title = tableRef.getValueAt(currentRow, 1).toString();
							
							newStructRow++;
						}
					}
					
			 
//					
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
                if (m_fileExportMode){
                    // file mode
                    m_fchooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
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

                }else{
                    // directory
                    m_fchooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                }

                int result = m_fchooser.showOpenDialog(addFileButton);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File file = m_fchooser.getSelectedFile();

                    String absolutePath = file.getAbsolutePath();
                    String fileName = file.getName();
                    if (m_fileExportMode ){
                        if (fileName.indexOf('.') == -1) {
                            absolutePath += "." + (exporterInfo == null ? "xlsx ":exporterInfo.getFileExtension());
                        }
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
        if (isFileExportMode() && f.exists()) {
            String message = "The file already exists. Do you want to overwrite it ?";
            String title = "Overwrite ?";
            String[] options = {"Yes", "No"};
            int reply = JOptionPane.showOptionDialog(this, message, title, JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, "Yes");
            if (reply != JOptionPane.YES_OPTION) {
                setStatus(true, "File already exists.");
                return false;
            }
        }

        if (isFileExportMode()){
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
        }else if (!f.canWrite()){
            setStatus(true, "Cannot write in this directory");
            highlight(m_fileTextField);
            return false;
        }

        // check config
        ExportConfig config = generateConfigFileFromGUI();
        String msgError = checkTitles(config);
        if (!msgError.isEmpty()) {
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

    /**
     * *
     * returns the JSON String corresponding to the export configuration
     *
     * @return
     */
    public String getExportConfig() {
        logger.debug("getExportConfig");
        m_exportConfig = generateConfigFileFromGUI();
        return m_exportConfig == null ? null : new GsonBuilder().create().toJson(m_exportConfig);
    }

    public String getFileExtension() {
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
        // create a hashmap of tabs titles and ids in case of renaming
		m_tabTitleIdHashMap.clear(); 
		for(int i =0;i<m_exportDefaultConfig.sheets.length;i++) {
			m_tabTitleIdHashMap.put(m_exportDefaultConfig.sheets[i].title, m_exportDefaultConfig.sheets[i].id);
			
		}
		
        fillExportPossibleValues(m_exportDefaultConfig);
        //m_exportConfig = m_exportDefaultConfig; // this in order to have the config like the default one, before one is loaded.
        if (m_exportDefaultConfig != null) {
            fillExportFormatTable(m_exportDefaultConfig, m_exportConfig);
            //m_exportConfig = m_exportDefaultConfig; // this in order to have the config like the default one, before one is loaded.
        }
    }

    /* return true if the configuration is ok regarding the titles (should not be empty and 1 sheet can not contain 2 same title) */
    private String checkTitles(ExportConfig config) {
        String errorsOnConfig = "";
        ExportExcelSheet[] allSheets = config.sheets;
        int s = 1;
        for (ExportExcelSheet sheet : allSheets) {
            if (sheet.title == null || sheet.title.trim().isEmpty()) {
                errorsOnConfig += "The sheet at position " + (s) + " has no title! \n";
            }
            ExportExcelSheetField[] allFields = sheet.fields;
            int f = 0;
            for (ExportExcelSheetField field : allFields) {
                if (field.title == null || field.title.trim().isEmpty()) {
                    errorsOnConfig += "The field in the sheet " + sheet.title + " at position " + (f + 1) + " has no title! \n";
                } else if (sheet.containsFieldTitle(field.title, f)) {
                    errorsOnConfig += "The field " + field.title + " in the sheet " + sheet.title + " (at position " + (f + 1) + ") is already defined. \n";
                }
                f++;
            }
            s++;
        }
        return errorsOnConfig;
    }
    
    public void updateFileExport(){
        String text = m_fileTextField.getText().trim();
        if (!text.isEmpty()){
            // file or dir?
            boolean isDir = new File(text).isDirectory();
            if (!isFileExportMode() && !isDir){
                int id = text.lastIndexOf("\\");
                if (id != -1){
                    String newText = text.substring(0, id+1);
                    m_fileTextField.setText(newText);
                }
            }
        }
        m_exportConfig = null; //m_exportDefaultConfig;
        fillExportFormatTable(m_exportDefaultConfig, m_exportConfig);
    }

}
