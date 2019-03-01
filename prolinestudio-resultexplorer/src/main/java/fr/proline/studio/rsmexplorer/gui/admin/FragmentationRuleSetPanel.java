package fr.proline.studio.rsmexplorer.gui.admin;

import fr.proline.core.orm.uds.FragmentationRule;
import fr.proline.core.orm.uds.FragmentationRuleSet;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.DatabaseDataManager;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.export.ExportButton;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.parameter.SettingsButton;
import fr.proline.studio.progress.ProgressInterface;
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
import java.util.Iterator;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author JM235353
 */
public class FragmentationRuleSetPanel extends JPanel {
    
    private JDialog m_dialogOwner = null;
    private FragmentationRuleSetTable m_fragmentRuleSetsTable = null;
    private Boolean m_isEditable = true;
    
    public FragmentationRuleSetPanel(JDialog dialog) {
        this(dialog,true);
    }
    
    public FragmentationRuleSetPanel(JDialog dialog, Boolean editable) {
        m_dialogOwner = dialog;
        m_isEditable = editable;
        
        setBorder(BorderFactory.createTitledBorder("Fragmentation Rule Sets"));        
        setLayout(new java.awt.GridBagLayout());
        
        JScrollPane tableScrollPane = new JScrollPane();        
        m_fragmentRuleSetsTable = new FragmentationRuleSetTable();
        tableScrollPane.setViewportView(m_fragmentRuleSetsTable);
        m_fragmentRuleSetsTable.setFillsViewportHeight(true);

        JButton addFRSButton = new JButton("Add Fragmentation Rule Set");
        addFRSButton.setIcon(IconManager.getIcon(IconManager.IconType.PLUS_16X16));
        addFRSButton.setEnabled(m_isEditable);
//        JButton modifyFRSButton = new JButton("Modify Fragmentation Rule Set");
//        modifyFRSButton.setIcon(IconManager.getIcon(IconManager.IconType.PROPERTIES));
        
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.weightx = 0;
        c.weighty = 1.0;
        c.fill = GridBagConstraints.NONE;
        JToolBar toolbar = initToolbar();
        add(toolbar, c);
         
        c.gridx++;
        c.weightx = 1.0;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.BOTH;
        add(tableScrollPane, c);
        
        
        c.gridy++;
        c.gridwidth = 1;
        c.weighty = 0;
        add(Box.createHorizontalGlue(), c);
        
        c.gridx++;
        c.weightx = 0;
        add(addFRSButton, c);
        
//        c.gridx++;
//        add(modifyFRSButton, c);
        
        addFRSButton.addActionListener(new ActionListener() {
             @Override
            public void actionPerformed(ActionEvent e) {
                FragmentationRuleSetDialog frsDialog = FragmentationRuleSetDialog.getDialog(m_dialogOwner);
                frsDialog.setLocationRelativeTo(m_dialogOwner);
                frsDialog.setVisible(true);
                if(frsDialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {
                    JOptionPane.showConfirmDialog(m_dialogOwner, "Are you sure you want to create "+frsDialog.getName()+" ?\n It will be saved in Datastore. No remove will be possible.");
                    AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {
                        @Override
                        public boolean mustBeCalledInAWT() {
                            return true;
                        }

                        @Override
                        public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
                            if (success) {
                                    m_fragmentRuleSetsTable.updateFragmentationRuleSets();
                            }
                        }

                    };
                    
                    fr.proline.studio.dam.tasks.DatabaseAdminTask task = new fr.proline.studio.dam.tasks.DatabaseAdminTask(callback);
                    task.initAddFragmentationRuleSet(frsDialog.getName(),frsDialog.getSelectedFragmentationRule());
                    AccessDatabaseThread.getAccessDatabaseThread().addTask(task);
                    
                }
            }
        });
        
//        modifyFRSButton.addActionListener(new ActionListener() {
//             @Override
//            public void actionPerformed(ActionEvent e) {
//                 JOptionPane.showConfirmDialog(m_dialogOwner,"Modify Fragmentation Rule Set Not Yet Implemented");
//            }
//        });
    }

    
    private JToolBar initToolbar() {
        JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
        toolbar.setFloatable(false);

        SettingsButton m_settingsButton = new SettingsButton((ProgressInterface) m_fragmentRuleSetsTable.getModel(), m_fragmentRuleSetsTable);
        ExportButton exportButton = new ExportButton( (ProgressInterface) m_fragmentRuleSetsTable.getModel(), "Fragmentation Rule Set", m_fragmentRuleSetsTable);

        toolbar.add(m_settingsButton);
        toolbar.add(exportButton);
        
        return toolbar;
    }
        
    private class FragmentationRuleSetTable extends DecoratedTable {

        public FragmentationRuleSetTable() {            
            setSortable(false);            
            initData();            
        }
        
        private void initData(){
            FragmentationRuleSet[] allFragmRuleSet = DatabaseDataManager.getDatabaseDataManager().getFragmentationRuleSetsArray();
            FragmentationRuleSetTableModel model  = new FragmentationRuleSetTableModel(allFragmRuleSet);
            setModel(model);
            /*Set fixed col width */
//            int colCount = model.getColumnCount();            
//            int width = 80;
//            for (int i = 0; i < colCount; i++) {
//                TableColumn col = columnModel.getColumn(i);
//                col.setWidth(width);
//                col.setPreferredWidth(width);
//            }
//            
            setRowSelectionInterval(0, 0);
        }
        
       public void updateFragmentationRuleSets() {
                        
            FragmentationRuleSetTableModel tableModel = (FragmentationRuleSetTableModel) getModel();
                                   
            FragmentationRuleSet[] allFragmRuleSet = DatabaseDataManager.getDatabaseDataManager().getFragmentationRuleSetsArray();
            tableModel.setData(allFragmRuleSet);                        
        }
       
        @Override
        public TablePopupMenu initPopupMenu() {
            return null; //not used
        }

        @Override
        public void prepostPopupMenu() {
            //not used
        }
        
    }
    
    private class FragmentationRuleSetTableModel  extends DecoratedTableModel implements ProgressInterface {
        
        private ArrayList<FragmentationRuleSet> m_fragmentationRuleSetArray = null;
        private FragmentationRule[] m_rules = DatabaseDataManager.getDatabaseDataManager().getFragmentationRulesArray();
        TableCellRenderer m_renderer = null; 
                
        
        public FragmentationRuleSetTableModel(FragmentationRuleSet[] fragmentationRuleSets) {
            m_fragmentationRuleSetArray = new ArrayList<>(fragmentationRuleSets.length);
            m_fragmentationRuleSetArray.addAll(Arrays.asList(fragmentationRuleSets));
            m_renderer = new DefaultLeftAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class));
        }
        
        
        public void setData(FragmentationRuleSet[] fragmentationRuleSets){
            m_fragmentationRuleSetArray.clear();
            m_fragmentationRuleSetArray.addAll(Arrays.asList(fragmentationRuleSets));
            fireTableStructureChanged();
        }
        
         
        @Override
        public int getRowCount() {
            return m_rules.length;                 
        }

        @Override
        public int getColumnCount() {
            int nbCol = 2; //rules 
            if (m_fragmentationRuleSetArray != null) 
                nbCol += m_fragmentationRuleSetArray.size();
            return nbCol; 
        }

        @Override
        public String getColumnName(int col) {
            if(col == 0 || col == (m_fragmentationRuleSetArray.size()+1))
                return "Ion Series";
            else
                return m_fragmentationRuleSetArray.get(col-1).getName();
//            if(col == 0)
//                return "Fragmentation Rule Set";
//            return m_rules[col-1].getDescription();
        }
        
        @Override
        public Class getColumnClass(int col) {
            return String.class;
        }
        
        
        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if(columnIndex == 0 || columnIndex == (m_fragmentationRuleSetArray.size()+1)){
                String name = m_rules[rowIndex].getDescription();
                if(m_rules[rowIndex].getFragmentationSeries() != null)
                    name = m_rules[rowIndex].getFragmentationSeries().getName();
                return name;
            } else {
                FragmentationRuleSet fragmRuleSet = m_fragmentationRuleSetArray.get(columnIndex-1);
                Iterator<FragmentationRule> frIt =  fragmRuleSet.getFragmentationRules().iterator();
                while(frIt.hasNext()){
                    FragmentationRule nextFr = frIt.next();
                    if(nextFr.getId() == (rowIndex+1))
                        return "x";                   
                }
                //Not found 
                return "";
            }
        }

        @Override
        public String getToolTipForHeader(int col) {
            return getColumnName(col);
        }

        @Override
        public String getTootlTipValue(int row, int col) {
            if(col == 0 || col == (m_fragmentationRuleSetArray.size()+1)) //Ion Series columns
                return m_rules[row].getDescription();            
            return "Indicate if ion serie is defined in "+getToolTipForHeader(col);            
        }

        @Override
        public TableCellRenderer getRenderer(int row, int col) {
           return m_renderer;
            
        }

        @Override
        public boolean isLoaded() {
            return true;
        }

        @Override
        public int getLoadingPercentage() {
            return 100;
        }

    }


}
