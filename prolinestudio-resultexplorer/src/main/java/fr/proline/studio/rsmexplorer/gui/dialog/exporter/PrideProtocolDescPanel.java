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
package fr.proline.studio.rsmexplorer.gui.dialog.exporter;

import fr.proline.studio.dpm.data.CVParam;
import fr.proline.studio.utils.IconManager;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import uk.ac.ebi.pride.toolsuite.ols.dialog.OLSInputable;
import uk.ac.ebi.pride.utilities.ols.web.service.model.Term;
import uk.ac.ebi.pride.toolsuite.ols.dialog.OLSDialog;

import org.apache.commons.lang3.StringUtils;
import org.openide.windows.WindowManager;
import org.slf4j.LoggerFactory;


/**
 *
 * @author VD225637
 */
public class PrideProtocolDescPanel extends PrideWizardPanel implements OLSInputable {

    private static PrideProtocolDescPanel m_panel = null;
//    private JTextArea m_protocolDescriptionTextArea;
    private JTextField m_protocolNameTextField;
    private JTable m_stepsDescriptionTable;

    public static String STEPS_FIELD = "steps";

    private String errorMsg;

    private PrideProtocolDescPanel() {
        super.initWizardPanels("<html><b>Step 2:</b> Protocol Description</html>");
    }

    public static PrideProtocolDescPanel getPrideProtocolDescPanel() {
        if (m_panel == null) {
            m_panel = new PrideProtocolDescPanel();
        }
        return m_panel;
    }
    
    protected void resetPanel(){
        m_protocolNameTextField.setText(null);
        ((StepDescriptionTableModel)m_stepsDescriptionTable.getModel()).clearCVParams();
    }

    @Override
    public HashMap<String, Object> getExportPrideParams() {
        HashMap params = new HashMap();
//        if (!StringUtils.isEmpty(m_protocolDescriptionTextArea.getText().trim())) {
//            params.put("protocol_description", m_protocolDescriptionTextArea.getText().trim());
//        }
        if(m_protocolNameTextField.getText() != null && !StringUtils.isEmpty(m_protocolNameTextField.getText().trim()))
          params.put("protocol_name",m_protocolNameTextField.getText().trim());
        
        List<CVParam> steps  = ((StepDescriptionTableModel) m_stepsDescriptionTable.getModel()).getStepsCVParams();        
        if(!steps.isEmpty())
            params.put("protocol_steps",steps);
        return params;
    }

    @Override
    protected Component checkExportPrideParams() {
        errorMsg  = null;
        String name = m_protocolNameTextField.getText();
        if(name == null ||  name.trim().isEmpty()){
            errorMsg = "A Protocol name should be specified.";
            return m_protocolNameTextField;
        }
        
        List<CVParam> steps  = ((StepDescriptionTableModel) m_stepsDescriptionTable.getModel()).getStepsCVParams();
        if(steps.isEmpty()){
            errorMsg = "At least one Protocol Step should be specified.";
            return m_stepsDescriptionTable;
        }
        return null;
    }

    @Override
    protected String getErrorMessage() {
        return errorMsg;
    }

    @Override
    protected JPanel createMainPanel() {
        JPanel mainPanel = new JPanel();
        mainPanel.setBorder(BorderFactory.createTitledBorder("Full Protocol description"));
        mainPanel.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        //DEV IMPLEM START
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        JLabel protocolLabel = new JLabel("Protocol Name* :");
        mainPanel.add(protocolLabel, c);

        m_protocolNameTextField = new JTextField(30);
        c.gridx++;
        //c.gridwidth = 2;
        c.weightx = 1;
        mainPanel.add(m_protocolNameTextField, c);

        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 2;
        c.weighty = 1;
        mainPanel.add(createStepsPanel(), c);
        //DEV IMPLEM END

// CURRENT IMPLEM START
//        c.anchor = GridBagConstraints.NORTHWEST;
//        c.fill = GridBagConstraints.BOTH;
//        c.gridx = 0;
//        c.gridy = 0;
//        c.weightx=1;
//        c.weighty=1;
//        m_protocolDescriptionTextArea  = new JTextArea();
//        m_protocolDescriptionTextArea.setText("<Protocol>\n\t<ProtocolName>In Gel Protein Digestion</ProtocolName>\n\t<ProtocolSteps>\n"
//                + "\t\t<StepDescription>\n\t\t\t<cvParam cvLabel=\"PRIDE\" accession=\"PRIDE:0000025\" name=\"Reduction\" value=\"DTT\" />\n\t\t</StepDescription>\n"
//                + "\t\t<StepDescription>\n\t\t\t<cvParam cvLabel=\"PRIDE\" accession=\"PRIDE:0000026\" name=\"Alkylation\" value=\"iodoacetamide\" />\n\t\t</StepDescription>\n"
//                + "\t\t<StepDescription>\n\t\t\t<cvParam cvLabel=\"PRIDE\" accession=\"PRIDE:0000160\" name=\"Enzyme\" value=\"Trypsin\" />\n\t\t</StepDescription>\n"
//                + "\t</ProtocolSteps>\n</Protocol>");
//        JScrollPane protocolSP = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
//        protocolSP.setViewportView(m_protocolDescriptionTextArea);
//        mainPanel.add(protocolSP, c);
        // CURRENT IMPLEM END
        return mainPanel;
    }

    protected JPanel createStepsPanel() {
        JPanel stepPanel = new JPanel(new GridBagLayout());
        stepPanel.setBorder(BorderFactory.createTitledBorder(" Steps Description* "));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;        
        c.gridx = 0;
        c.gridy = 0;
        stepPanel.add(Box.createHorizontalGlue(), c);
        c.gridx++;
        c.anchor = GridBagConstraints.NORTHEAST;
        c.fill = GridBagConstraints.NONE;
        JButton m_addStepDescButton = new JButton("Add Step", IconManager.getIcon(IconManager.IconType.PLUS));
        m_addStepDescButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        stepPanel.add(m_addStepDescButton, c);
        m_addStepDescButton.addActionListener(new ActionListener() {

            @Override   
            public void actionPerformed(ActionEvent e) {
                new OLSDialog((JDialog) SwingUtilities.getAncestorOfClass(JDialog.class, PrideProtocolDescPanel.getPrideProtocolDescPanel()),
                        PrideProtocolDescPanel.getPrideProtocolDescPanel(), true, STEPS_FIELD, "", null, false);
                
            }
        });
        
        c.gridx++;
        c.anchor = GridBagConstraints.NORTHEAST;
        c.fill = GridBagConstraints.NONE;
        JButton m_clearStepDescButton = new JButton("Clear Steps", IconManager.getIcon(IconManager.IconType.CLEAR_ALL));
        m_clearStepDescButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        stepPanel.add(m_clearStepDescButton, c);        
        m_clearStepDescButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ((StepDescriptionTableModel)m_stepsDescriptionTable.getModel()).clearCVParams();                
            }
        });

        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy++;
        c.weightx=1;
        c.weighty=1;
        m_stepsDescriptionTable = new JTable();
        m_stepsDescriptionTable.setModel(new StepDescriptionTableModel());
        JScrollPane  m_stepsDescriptionJScrollPane = new javax.swing.JScrollPane();
        m_stepsDescriptionJScrollPane.setViewportView(m_stepsDescriptionTable);
        stepPanel.add(m_stepsDescriptionJScrollPane, c);
        
        c.gridx=0;
        c.gridy++;
        c.weighty=1;
        stepPanel.add(Box.createVerticalGlue(), c); 
        
        return stepPanel;
    }

    @Override
    public void insertOLSResult(String field, Term selectedValue, Term accession, String ontologyShort, String ontologyLong, int modifiedRow, String mappedTerm, List<String> metadata) {
        LoggerFactory.getLogger("ProlineStudio.ResultExplorer").debug("field : {}, selectedValue : {}  , accession : {} , ontologyShort : {} ontologyLong: {},  modifiedRow : {} mappedTerm: {} , nbr other : {}", field, selectedValue, accession, ontologyShort, ontologyLong, modifiedRow, mappedTerm, metadata);

        if (field == null) {
            return;
        }

        if (field.equals(STEPS_FIELD)) {
            CVParam newParam = new CVParam(ontologyShort, accession.getLabel(), selectedValue.getLabel(), "");
//                        CVParam newParam = new CVParam(ontologyShort, accession, selectedValue, "");
            if (modifiedRow == -1) {
                ((StepDescriptionTableModel) this.m_stepsDescriptionTable.getModel()).addCVParam(newParam);
            } else {
                ((StepDescriptionTableModel) this.m_stepsDescriptionTable.getModel()).replaceCVParam(newParam, modifiedRow);
            }
        }
    }

    @Override
    public Window getWindow() {
        return WindowManager.getDefault().getMainWindow();
    }
 
    class StepDescriptionTableModel extends AbstractTableModel {

        public static final int STEP_DESCR_INDEX_COLINDEX = 0;
        public static final int STEP_DESCR_CVPARAM_COLINDEX = 1;
        public static final int STEP_DESCR_VALUE_COLINDEX = 2;

        private CVParam EMPTY_PARAM = new CVParam("NONE", "NONE", "NONE", null);
        private final Class[] colClasses = {java.lang.Integer.class, java.lang.String.class, java.lang.String.class};
        private final Boolean[] colEditable = {false, false, true};
        private final String[] columnNames = {"Index", "CV Param", "Value"};

        private List<CVParam> cvParams = new ArrayList<>();

        public StepDescriptionTableModel() {
            super();
            cvParams.add(EMPTY_PARAM);
        }

        public void addCVParam(CVParam newParam) {
            if(cvParams.get(0).equals(EMPTY_PARAM)){
                cvParams.set(0, newParam);
                fireTableRowsUpdated(0,0);
            } else{
                cvParams.add(newParam);
                fireTableRowsInserted(getRowCount()-1, getRowCount()-1);
            }           
        }

        public void clearCVParams(){
            int end=getRowCount()-1;
            cvParams.clear();
            cvParams.add(EMPTY_PARAM);
            fireTableRowsDeleted(0, end);
        }
        
        public void replaceCVParam(CVParam newParam, int index) {
            cvParams.set(index, newParam);
            fireTableRowsUpdated(index,index);
        }

        public List<CVParam> getStepsCVParams(){
            List<CVParam> definedParams = new ArrayList<>(cvParams.size());
            for(CVParam definedStep : cvParams){
                if(!definedStep.equals(EMPTY_PARAM))
                    definedParams.add(definedStep);
            }
            return definedParams;
        }
        
        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }

        @Override
        public int getRowCount() {
            return cvParams.size();
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            switch (columnIndex) {
                case STEP_DESCR_INDEX_COLINDEX:
                    return rowIndex;

                case STEP_DESCR_CVPARAM_COLINDEX:
                    return cvParams.get(rowIndex).toString();

                case STEP_DESCR_VALUE_COLINDEX:
                    return cvParams.get(rowIndex).getValue();
            }

            return null;
        }
   
        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if(columnIndex == STEP_DESCR_VALUE_COLINDEX){
                cvParams.get(rowIndex).setValue(aValue.toString());
                fireTableCellUpdated(rowIndex, columnIndex);
            }
        }
    
        @Override
        public Class getColumnClass(int columnIndex) {
            return colClasses[columnIndex];
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return colEditable[columnIndex];
        }
    }
}
