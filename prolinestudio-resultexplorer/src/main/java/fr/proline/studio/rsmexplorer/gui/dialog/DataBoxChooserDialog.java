/* 
 * Copyright (C) 2019 VD225637
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1 
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License 
 * along with this program; If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
 */
package fr.proline.studio.rsmexplorer.gui.dialog;

import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.GroupParameter;
import fr.proline.studio.pattern.DataboxManager;
import fr.proline.studio.pattern.ParameterDistance;
import fr.proline.studio.progress.ProgressBarDialog;
import fr.proline.studio.table.DecoratedMarkerTable;
import fr.proline.studio.table.DecoratedTableModel;
import fr.proline.studio.table.TablePopupMenu;
import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellRenderer;



/**
 * Dialog to select a databox as a View to be added in a Window.
 * @author JM235353
 */
public class DataBoxChooserDialog extends DefaultDialog {

    
    private JTextField m_titleTextField = null;
    
    private DataBoxTable m_dataBoxTable = null;
    private JRadioButton m_belowRadioButton;
    private JRadioButton m_tabbedRadioButton;
    private JRadioButton m_splittedRadioButton;
    
    private AbstractDataBox m_previousDatabox = null;
    
    public DataBoxChooserDialog(Window parent, ArrayList<GroupParameter> outParameters, boolean firstView) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);

        setSize(780,420);
        
        setButtonVisible(BUTTON_HELP, false);
        setResizable(true);
        
        TreeMap<ParameterDistance, AbstractDataBox> dataBoxMap = DataboxManager.getDataboxManager().findCompatibleStartingDataboxList(outParameters);

        initDialog(dataBoxMap, firstView);
        
        m_previousDatabox = null;
    }
    
    public DataBoxChooserDialog(Window parent, AbstractDataBox previousDatabox, boolean firstView, Class[] importantInParameter) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);

        setSize(800,320);
        
        setButtonVisible(BUTTON_HELP, false);
        
        TreeMap<ParameterDistance, AbstractDataBox> dataBoxMap = DataboxManager.getDataboxManager().findCompatibleDataboxList(previousDatabox, importantInParameter);

        initDialog(dataBoxMap, firstView);
        
        m_previousDatabox = previousDatabox;
    }

    
    @Override
    public void pack() {
        // forbid pack by overloading the method
    }
            
    private void initDialog(TreeMap<ParameterDistance, AbstractDataBox> dataBoxMap, boolean firstView) {
        setTitle(firstView ? "User Defined Window" : "Add a View");   

        JPanel internalPanel = new JPanel(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        c.weightx=1.0;
        if (firstView) {
            JPanel windowPanel = createWindowPanel();
            internalPanel.add(windowPanel, c);
            c.gridy++;
        }

        c.weighty=1.0;
        JPanel viewPanel = createViewsPanel(dataBoxMap, firstView);
        internalPanel.add(viewPanel, c);

        setInternalComponent(internalPanel);
    }

    private JPanel createWindowPanel() {
        JPanel windowPanel = new JPanel(new GridBagLayout());
        windowPanel.setBorder(BorderFactory.createTitledBorder(" Window Title "));

        JLabel titleLabel = new JLabel("Title : ");
        m_titleTextField = new JTextField();
        
         
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        windowPanel.add(titleLabel, c);
        
        c.gridx++;
        c.weightx = 1.0;
        windowPanel.add(m_titleTextField, c);
        
        return windowPanel;
    }
    
    private JPanel createViewsPanel(TreeMap<ParameterDistance, AbstractDataBox> dataBoxMap, boolean firstView) {
         JPanel viewPanel = new JPanel(new GridBagLayout());
        viewPanel.setBorder(BorderFactory.createTitledBorder(" Views "));
        
        
        // create objects
        JScrollPane scrollPane = new JScrollPane();
        m_dataBoxTable = new DataBoxTable();
        m_dataBoxTable.setModel(new DataBoxTableModel(dataBoxMap));
        
        scrollPane.setViewportView(m_dataBoxTable);
        m_dataBoxTable.setFillsViewportHeight(true);
        
        
        JLabel positionLabel = null;
        if (!firstView) {
            positionLabel = new JLabel("View Position : ");
            m_belowRadioButton = new JRadioButton("Below");
            m_belowRadioButton.setSelected(true);
            m_tabbedRadioButton = new JRadioButton("Tabbed");
            m_splittedRadioButton = new JRadioButton("Splitted");
        }

        
        
        // Group the radio buttons.
        ButtonGroup group = new ButtonGroup();
        group.add(m_belowRadioButton);
        group.add(m_tabbedRadioButton);
        group.add(m_splittedRadioButton);
        
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 4;
        c.weightx = 1.0;
        c.weighty = 1.0;
        viewPanel.add(scrollPane, c);

        if (!firstView) {
            c.weightx = 0.0;
            c.weighty = 0.0;
            c.gridwidth = 1;
            c.gridx = 0;
            c.gridy++;
            viewPanel.add(positionLabel, c);
            c.gridx++;
            viewPanel.add(m_belowRadioButton, c);
            c.gridx++;
            viewPanel.add(m_tabbedRadioButton, c);
            c.gridx++;
            viewPanel.add(m_splittedRadioButton, c);

        }
        
        return viewPanel;
    }

    

 
    @Override
    protected boolean okCalled() {
        
        if ((m_titleTextField != null) && (m_titleTextField.getText().trim().length() == 0)) {
            setStatus(true, "You must fill a Window Title");
             highlight(m_titleTextField);
             return false;
        }
        
        int selectedRow = m_dataBoxTable.getSelectedRow();
        if (selectedRow == -1) {
             setStatus(true, "You must select a View");
             highlight(m_dataBoxTable);
             return false;
        }

        if ((m_previousDatabox!= null) && (!m_previousDatabox.isLoaded())) {

            ProgressBarDialog dialog = ProgressBarDialog.getDialog(this, m_previousDatabox, "Data loading", "This functionnality is not available while data is loading. Please Wait.");
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);

            if (!dialog.isWaitingFinished()) {
                return false;
            }
        }

        m_previousDatabox = null;
        
        return true;
        
       
        
    }
    
    public AbstractDataBox getSelectedDataBox() {
        int selectedRow = m_dataBoxTable.getSelectedRow();
        if (selectedRow == -1) {
            return null;
        }
        
        selectedRow = m_dataBoxTable.convertRowIndexToModel(selectedRow);
        
        return ((DataBoxTableModel)m_dataBoxTable.getModel()).getDataBox(selectedRow);
    }

    public String getWndTitle() {
        return m_titleTextField.getText();
    }
    
    public boolean addBelow() {
        return m_belowRadioButton.isSelected();
    }
    
    public boolean addTabbed() {
        return m_tabbedRadioButton.isSelected();
    }
    
    public boolean addSplitted() {
        return m_splittedRadioButton.isSelected();
    }
    
    private class DataBoxTable extends DecoratedMarkerTable {

        private String m_previouslySelectedWndTitle = null;
        
        public DataBoxTable() {
            setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        }
        
        @Override
        public void addTableModelListener(TableModelListener l) {
            getModel().addTableModelListener(l);
        }
        
        /**
         * Called whenever the value of the selection changes.
         *
         * @param e the event that characterizes the change.
         */
        @Override
        public void valueChanged(ListSelectionEvent e) {

            super.valueChanged(e);

            if (m_titleTextField != null) {
                String text = m_titleTextField.getText().trim();
                if ( (text.length() == 0) || ((m_previouslySelectedWndTitle!=null) && (text.compareTo(m_previouslySelectedWndTitle)==0)) ) {
                    
                    int selectedRow = getSelectedRow();
                    if (selectedRow == -1) {
                        return;
                    }
                    selectedRow = convertRowIndexToModel(selectedRow);
                    
                    String wndTitle = ((String) getModel().getValueAt(selectedRow, DataBoxTableModel.COLTYPE_NAME)).trim();
                    m_titleTextField.setText(wndTitle);
                    m_previouslySelectedWndTitle = wndTitle;
                }
            }
            
        }
        
        @Override
        public TablePopupMenu initPopupMenu() {
            return null;
        }

        // set as abstract
        @Override
        public void prepostPopupMenu() {
            // nothing to do
        }

        

    }
    
    private static class DataBoxTableModel extends DecoratedTableModel {

        public static final int COLTYPE_NAME = 0;
        public static final int COLTYPE_DESCRIPTION = 1;
        private static final String[] columnNames = {"name", "description"};
        private ArrayList<AbstractDataBox> m_databoxList = null;


        public DataBoxTableModel(TreeMap<ParameterDistance, AbstractDataBox> dataBoxMap) {
            
            m_databoxList = new ArrayList<>(dataBoxMap.size());
            Iterator<ParameterDistance> it = dataBoxMap.descendingKeySet().iterator();
            while (it.hasNext()) {
                m_databoxList.add(dataBoxMap.get(it.next()));
            }
            
        }

        public AbstractDataBox getDataBox(int row) {
            if ((row<0) || (row>=m_databoxList.size())) {
                return null;
            }
            return m_databoxList.get(row);
        }
        
        
        @Override
        public Class getColumnClass(int col) {
           return String.class;
        }

        @Override
        public int getRowCount() {
            if (m_databoxList == null) {
                return 0;
            }
            return m_databoxList.size();
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public String getColumnName(int col) {
            return columnNames[col];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            AbstractDataBox dataBox = m_databoxList.get(rowIndex);
            switch (columnIndex) {
                case COLTYPE_NAME: {
                    return dataBox.getTypeName();
                }
                case COLTYPE_DESCRIPTION: {
                    return dataBox.getDescription();
                }


            }
            return null; // should not happen
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
            return null;
        }
    }
    
}
