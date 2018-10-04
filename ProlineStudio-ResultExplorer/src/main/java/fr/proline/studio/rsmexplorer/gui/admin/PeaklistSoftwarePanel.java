package fr.proline.studio.rsmexplorer.gui.admin;

import fr.proline.core.orm.uds.PeaklistSoftware;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.DatabaseDataManager;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.gui.DefaultDialog;
import static fr.proline.studio.gui.DefaultDialog.BUTTON_CANCEL;
import fr.proline.studio.gui.InfoDialog;
import fr.proline.studio.table.DecoratedTable;
import fr.proline.studio.table.DecoratedTableModel;
import fr.proline.studio.table.TableDefaultRendererManager;
import fr.proline.studio.table.TablePopupMenu;
import fr.proline.studio.table.renderer.DefaultLeftAlignRenderer;
import fr.proline.studio.utils.IconManager;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.TableCellRenderer;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author JM235353
 */
public class PeaklistSoftwarePanel extends JPanel {
    
    private JDialog m_dialogOwner = null;
    private Boolean m_isEditable = true;
    
    private PeaklistSoftwareTable m_peaklistSoftwareTable;
    

    public PeaklistSoftwarePanel(JDialog dialog, Boolean editable) {
        m_isEditable = editable;
        m_dialogOwner = dialog;
        
        setBorder(BorderFactory.createTitledBorder("Peaklist Softwares"));
        
        setLayout(new java.awt.GridBagLayout());
        initInternalPanel();
    }
    
    private void initInternalPanel(){

        JScrollPane tableScrollPane = new JScrollPane();
        m_peaklistSoftwareTable = new PeaklistSoftwareTable(); 
        tableScrollPane.setViewportView(m_peaklistSoftwareTable);
        m_peaklistSoftwareTable.setFillsViewportHeight(true);
        
        
        JButton addPeaklistButton = new JButton("Add Peaklist Software");
        addPeaklistButton.setIcon(IconManager.getIcon(IconManager.IconType.PLUS_16X16));
        addPeaklistButton.setEnabled(m_isEditable);
        String buttonTitle = "View Peaklist Software";
        if(m_isEditable)
            buttonTitle = "Modify Peaklist Software";
            
        JButton modifyPeaklistButton = new JButton(buttonTitle);
        modifyPeaklistButton.setIcon(IconManager.getIcon(IconManager.IconType.PROPERTIES));
//        modifyPeaklistButton.setEnabled(m_isEditable);
        JPanel testPanel = createTestPanel();

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 3;
        c.weightx = 1.0;
        c.weighty = 1.0;
        add(tableScrollPane, c);
        
        c.gridy++;
        c.weighty = 0;
        add(testPanel, c);
        
        c.gridy++;
        c.gridwidth = 1;
        c.weighty = 0;
        add(Box.createHorizontalGlue(), c);
        
        c.gridx++;
        c.weightx = 0;
//        c.fill = GridBagConstraints.HORIZONTAL;
        add(addPeaklistButton, c);
        
        c.gridx++;        
        add(modifyPeaklistButton, c);
        
        
        addPeaklistButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                PeakListSoftwareDialog peaklistSoftwareDialog = PeakListSoftwareDialog.getDialog(m_dialogOwner, PeakListSoftwareDialog.DialogMode.CREATE_USER);
                peaklistSoftwareDialog.setLocationRelativeTo(m_dialogOwner);
                peaklistSoftwareDialog.setVisible(true);
                if (peaklistSoftwareDialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {
                        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {
                            @Override
                            public boolean mustBeCalledInAWT() {
                                return true;
                            }

                            @Override
                            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
                                if (success) {
                                    m_peaklistSoftwareTable.updatePeaklistSoftwares();
                                }
                            }

                        };

                        fr.proline.studio.dam.tasks.DatabaseAdminTask task = new fr.proline.studio.dam.tasks.DatabaseAdminTask(callback);
                        task.initAddPeakListSoftware(peaklistSoftwareDialog.getPeaklistSoftwareName(), peaklistSoftwareDialog.getVersion(), peaklistSoftwareDialog.getSpectrumTitleParsingRule());
                        AccessDatabaseThread.getAccessDatabaseThread().addTask(task);
                }
                
                

            }
            
        });
        
        modifyPeaklistButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = m_peaklistSoftwareTable.getSelectedRow();
                if (selectedRow == -1) {
                    return;
                }
                
                PeakListSoftwareDialog peaklistSoftwareDialog = m_isEditable ? (PeakListSoftwareDialog.getDialog(m_dialogOwner, PeakListSoftwareDialog.DialogMode.MODIFY_USER)) : (PeakListSoftwareDialog.getDialog(m_dialogOwner, PeakListSoftwareDialog.DialogMode.VIEW_USER));
                peaklistSoftwareDialog.setLocationRelativeTo(m_dialogOwner);
                
                PeaklistSoftwareTableModel model = (PeaklistSoftwareTableModel) m_peaklistSoftwareTable.getModel();
                PeaklistSoftware peaklistSoftware = model.getPeaklistSoftware(m_peaklistSoftwareTable.convertRowIndexToModel(selectedRow));
                peaklistSoftwareDialog.setPeaklistSoftwareInfo(peaklistSoftware.getName(), peaklistSoftware.getVersion(), peaklistSoftware.getSpecTitleParsingRule());
                peaklistSoftwareDialog.setVisible(true);
                
                if (peaklistSoftwareDialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {
                    
                    
                    
                    String name = peaklistSoftwareDialog.getPeaklistSoftwareName();
                    String version = peaklistSoftwareDialog.getVersion();

                    if ((name.compareTo(peaklistSoftware.getName()) != 0) || (version.compareTo(peaklistSoftware.getVersion()) != 0) ) {
                        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

     
                            @Override
                            public boolean mustBeCalledInAWT() {
                                return false;
                            }

                            @Override
                            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
                                 if (success) {
                                    // reload users
                                    m_peaklistSoftwareTable.updatePeaklistSoftwares();
                                } else {
                                    //JPM.TODO
                                }
                            }

                        };

                        fr.proline.studio.dam.tasks.DatabaseAdminTask task = new fr.proline.studio.dam.tasks.DatabaseAdminTask(callback);
                        task.initModifyPeakListSoftware(peaklistSoftware.getId(), peaklistSoftwareDialog.getPeaklistSoftwareName(), peaklistSoftwareDialog.getVersion());

                        
                        AccessDatabaseThread.getAccessDatabaseThread().addTask(task);
                    }

                }
            }
            
        });
        
    }
    
    private JPanel createTestPanel() {
        JPanel testPanel = new JPanel(new java.awt.GridBagLayout());
        testPanel.setLayout(new java.awt.GridBagLayout());
        testPanel.setBorder(BorderFactory.createTitledBorder("Spectrum Title Parsing Test"));
        
        JLabel spectrumTitleLabel = new JLabel("Spectrum Title:");
        JTextField spectrumTitleTextField = new JTextField(30);
        JButton testButton = new JButton("Test", IconManager.getIcon(IconManager.IconType.TEST));

        testButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                
                StringBuilder sb = new StringBuilder();
                
                String spectrumTitle = spectrumTitleTextField.getText().trim();
                int rowSelected = m_peaklistSoftwareTable.getSelectedRow();
                PeaklistSoftware selectedPS = (( PeaklistSoftwareTableModel) m_peaklistSoftwareTable.getModel()).getPeaklistSoftware(rowSelected);
                                                
                parse(sb, StringUtils.defaultIfEmpty(selectedPS.getSpecTitleParsingRule().getRawFileIdentifier(),""), "Raw File Identifier :",spectrumTitle);
                parse(sb, StringUtils.defaultIfEmpty(selectedPS.getSpecTitleParsingRule().getFirstCycle(),""), "First Cycle :",spectrumTitle);
                parse(sb, StringUtils.defaultIfEmpty(selectedPS.getSpecTitleParsingRule().getLastCycle(),""), "Last Cycle :",spectrumTitle);
                parse(sb, StringUtils.defaultIfEmpty(selectedPS.getSpecTitleParsingRule().getFirstScan(),""), "First Scan :",spectrumTitle);
                parse(sb, StringUtils.defaultIfEmpty(selectedPS.getSpecTitleParsingRule().getLastScan(),""), "Last Scan :",spectrumTitle);
                parse(sb, StringUtils.defaultIfEmpty(selectedPS.getSpecTitleParsingRule().getFirstTime(),""), "First Time :",spectrumTitle);
                parse(sb, StringUtils.defaultIfEmpty(selectedPS.getSpecTitleParsingRule().getLastTime(),""), "Last Time :",spectrumTitle);

                InfoDialog parseResultDialog = new InfoDialog(m_dialogOwner, InfoDialog.InfoType.NO_ICON, "Spectrum Title Parsing Result", sb.toString());
                parseResultDialog.setButtonVisible(InfoDialog.BUTTON_OK, false);
                parseResultDialog.setButtonName(BUTTON_CANCEL, "Close");
                parseResultDialog.centerToWindow(m_dialogOwner);
                parseResultDialog.setVisible(true);
            }
            
        });
        
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        c.weighty = 0;
        testPanel.add(spectrumTitleLabel, c);
        
        c.gridx++;
        c.weightx = 1;
        testPanel.add(spectrumTitleTextField, c);
        
        c.gridx++;
        c.weightx = 0;
        testPanel.add(testButton, c);
        
        
        return testPanel;
    }
    
    
    private void parse(StringBuilder sb, String rule, String fieldName, String stringToParse) {
        sb.append(fieldName);
        if (!rule.isEmpty()) {
            Pattern pattern = Pattern.compile(rule);
            Matcher match = pattern.matcher(stringToParse);
            boolean findAMatch = match.find();
            if (findAMatch) {
                String firstMatch = match.group(1);
                sb.append(firstMatch);
            }
            
        }
        sb.append('\n');
    }
        
    public class PeaklistSoftwareTable extends DecoratedTable {

        public PeaklistSoftwareTable() {
            
            initData();
        }
        
        private void initData() {
            PeaklistSoftware[]  peaklistSoftwares = DatabaseDataManager.getDatabaseDataManager().getPeaklistSoftwaresArray();
            PeaklistSoftwareTableModel tableModel = new PeaklistSoftwareTableModel(peaklistSoftwares);

            setModel(tableModel);
            
            setRowSelectionInterval(0, 0);
        }

        public void updatePeaklistSoftwares() {
            
            int selectedRow = getSelectedRow();
            
            PeaklistSoftwareTableModel tableModel = (PeaklistSoftwareTableModel) getModel();
            
            long previouslySelectedId = -1;
            if (selectedRow != -1) {
                selectedRow = convertRowIndexToModel(selectedRow);
                PeaklistSoftware peakListSoftware = tableModel.getPeaklistSoftware(selectedRow);
                previouslySelectedId = peakListSoftware.getId();
            }
            
            
            PeaklistSoftware[]  peaklistSoftwares = DatabaseDataManager.getDatabaseDataManager().getPeaklistSoftwaresArray();
            tableModel.setPeaklistSoftwares(peaklistSoftwares);
            
            if (previouslySelectedId != -1) {
                int row = tableModel.findPeakListSoftware(previouslySelectedId);
                if (row != -1) {
                    row = convertRowIndexToView(row);
                    setRowSelectionInterval(row, row);
                }
                
            }
            
            
        }
        
        @Override
        public void valueChanged(ListSelectionEvent e) {

            super.valueChanged(e);
        }
        
        @Override
        public TablePopupMenu initPopupMenu() {
            return null;
        }

        @Override
        public void prepostPopupMenu() {
        }
        
    }
    
    public class PeaklistSoftwareTableModel extends DecoratedTableModel {

        private ArrayList<PeaklistSoftware> m_peaklistSoftwareArray = null;
        
        public static final int COLTYPE_NAME = 0;
        public static final int COLTYPE_VERSION = 1;
        
        private final String[] m_columnNames = {"Peaklist Software", "Version"};

        public PeaklistSoftwareTableModel(PeaklistSoftware[]  peaklistSoftwareArray) {
            m_peaklistSoftwareArray = new ArrayList<>(peaklistSoftwareArray.length);
            m_peaklistSoftwareArray.addAll(Arrays.asList(peaklistSoftwareArray));
            
        }
        
        public int findPeakListSoftware(long id) {

            for (int row = 0; row < m_peaklistSoftwareArray.size(); row++) {
                PeaklistSoftware peaklistSoftware =  m_peaklistSoftwareArray.get(row);
                if (peaklistSoftware.getId() == id) {
                    return row;
                }
            }
            return -1;
        }

        
        public void setPeaklistSoftwares(PeaklistSoftware[]  peaklistSoftwareArray) {
            m_peaklistSoftwareArray.clear();
            m_peaklistSoftwareArray.addAll(Arrays.asList(peaklistSoftwareArray));
            fireTableDataChanged();
        }
        
        public PeaklistSoftware getPeaklistSoftware(int row) {
            if (m_peaklistSoftwareArray == null) {
                return null;
            }
            return m_peaklistSoftwareArray.get(row);
        }
        
        
        @Override
        public String getColumnName(int col) {
            return m_columnNames[col];
        }
        
        @Override
        public Class getColumnClass(int col) {
            return String.class;
        }
        
        @Override
        public int getRowCount() {
            if (m_peaklistSoftwareArray == null) {
                return 0;
            }
            return m_peaklistSoftwareArray.size();
        }

        @Override
        public int getColumnCount() {
            return m_columnNames.length;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            
            PeaklistSoftware peaklistSoftware = m_peaklistSoftwareArray.get(rowIndex);
            
            switch (columnIndex) {
                case COLTYPE_NAME: {
                    return peaklistSoftware.getName();
                }
                case COLTYPE_VERSION: {
                    return peaklistSoftware.getVersion();
                }
            }
            return null;
        }

        @Override
        public String getToolTipForHeader(int col) {
            return null;
        }

        @Override
        public String getTootlTipValue(int row, int col) {
            return null;
        }

    @Override
    public TableCellRenderer getRenderer(int row, int col) {

        if (m_rendererMap.containsKey(col)) {
            return m_rendererMap.get(col);
        }
        
        TableCellRenderer renderer = null;
        
        switch (col) {

            case COLTYPE_NAME: {
                renderer =  new DefaultLeftAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class));
                break;
            }
            case COLTYPE_VERSION: {
                renderer = new DefaultLeftAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class));
                break;
            }

        }

        m_rendererMap.put(col, renderer);
        return renderer;
        

    }
    private final HashMap<Integer, TableCellRenderer> m_rendererMap = new HashMap();
        
    }


}
