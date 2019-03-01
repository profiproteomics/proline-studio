package fr.proline.studio.rsmexplorer.gui.dialog.exporter;

import fr.proline.studio.dpm.data.CVParam;
import fr.proline.studio.gui.JCheckBoxList;
import fr.proline.studio.utils.IconManager;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import uk.ac.ebi.pride.toolsuite.ols.dialog.OLSDialog;
import uk.ac.ebi.pride.toolsuite.ols.dialog.OLSInputable;
import org.apache.commons.lang3.StringUtils;
import org.openide.windows.WindowManager;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.pride.utilities.ols.web.service.model.Term;

/**
 *
 * @author VD225637
 */
//implements OLSInputable
public class PrideSampleDescPanel extends PrideWizardPanel implements OLSInputable {
    
    private static PrideSampleDescPanel m_panel = null;
    private JTextField m_sampleNameTextField;
    private JTextField m_sampleDescTextField;
    private JComboBox<CVParam> m_sampleSpeciesCB;   
    private JCheckBoxList<CVParam> m_sampleTissueCBList;   
    private JCheckBoxList<CVParam> m_sampleCellCBList;   
    private DefaultComboBoxModel<CVParam> m_sampleSpeciesCBModel;
    
    private String errorMsg;
    
    public static String SPECIES_FIELD="species";
    public static String TISSUE_FIELD="tissue";
    public static String CELLTYPE_FIELD="cell";
    
    private static CVParam EMPTY_PARAM = new CVParam("NONE", "NONE", "NONE", null);
    
    
    public static PrideSampleDescPanel getPrideSampleDescPanel(){
        if(m_panel == null) {
            m_panel = new PrideSampleDescPanel();
        }
        return m_panel;
    }
    
    private PrideSampleDescPanel() {
        super.initWizardPanels("<html><b>Step 3:</b> Sample Definition</html>");
    }
    
    protected void resetPanel(){
        m_sampleNameTextField.setText(null);
        m_sampleDescTextField.setText(null);
        m_sampleSpeciesCB.setSelectedIndex(-1);
        m_sampleTissueCBList.unselectAll();
        m_sampleCellCBList.unselectAll();

    }
        
    @Override
    public HashMap<String, Object> getExportPrideParams(){
        HashMap params = new HashMap();
       if(!StringUtils.isEmpty(m_sampleNameTextField.getText().trim()))
            params.put("sample_name",m_sampleNameTextField.getText().trim());
        if(!StringUtils.isEmpty(m_sampleDescTextField.getText().trim()))
            params.put("sample_desc",m_sampleDescTextField.getText().trim());
        
        List<CVParam> additinalDescription = new ArrayList<>();
        //Add Species
        if(m_sampleSpeciesCB.getSelectedItem()!= null && !m_sampleSpeciesCB.getSelectedItem().equals(EMPTY_PARAM))
            additinalDescription.add(m_sampleSpeciesCB.getItemAt(m_sampleSpeciesCB.getSelectedIndex()));            
        
        //Add all selected Tissue
        for(int i=0; i<m_sampleTissueCBList.getListSize(); i++){
            if(m_sampleTissueCBList.isVisible(i)){            
                additinalDescription.add(m_sampleTissueCBList.getElementAt(i));
            }
        }
        
        //Add all selected Cell type
        for(int i=0; i<m_sampleCellCBList.getListSize(); i++){
            if(m_sampleCellCBList.isVisible(i)){            
                additinalDescription.add(m_sampleCellCBList.getElementAt(i));
            }
        }
        
        if(!additinalDescription.isEmpty())
            params.put("sample_additional",additinalDescription);
 

        return params;
    }

    @Override
    protected Component checkExportPrideParams() {
        errorMsg  = null;
        if(StringUtils.isEmpty(m_sampleNameTextField.getText().trim())){
            errorMsg =  "A sample name should be specified";
            return m_sampleNameTextField;
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
        mainPanel.setLayout(new GridBagLayout());
                
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        JLabel sampleNameLabel = new JLabel("Sample Name* :");
        m_sampleNameTextField  = new JTextField(30);
        c.gridx = 0;
        c.gridy = 0;
        mainPanel.add(sampleNameLabel, c);
        
        c.gridx++;
        c.gridwidth = 2;
        c.weightx = 1;
        mainPanel.add(m_sampleNameTextField, c);
               
        JLabel sampleDescLabel = new JLabel("Description:");
        m_sampleDescTextField  = new JTextField(30);
        c.gridx = 0;
        c.weightx = 0.5;
        c.gridy++;
        c.gridwidth = 1;
        mainPanel.add(sampleDescLabel, c);
        
        c.gridx++;
        c.weightx = 1;
        c.gridwidth = 2;
        mainPanel.add(m_sampleDescTextField, c);
        
        
        // Species
        JLabel sampleSpeciesCVLabel = new JLabel("Species:");
        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 1;
        mainPanel.add(sampleSpeciesCVLabel, c);
        
        m_sampleSpeciesCB = new JComboBox<>();
        m_sampleSpeciesCB.setMaximumRowCount(20);
        List<CVParam> speciesCVs = getCVParamsFromRsc("/fr/proline/studio/rsmexplorer/gui/dialog/pride/NEWT_entries.txt");        
        speciesCVs.add(0, EMPTY_PARAM);
        m_sampleSpeciesCBModel = new DefaultComboBoxModel<>(speciesCVs.toArray(new CVParam[speciesCVs.size()]));
        m_sampleSpeciesCB.setModel(m_sampleSpeciesCBModel);
        m_sampleSpeciesCB.setSelectedIndex(1);
        c.gridx++;
        mainPanel.add(m_sampleSpeciesCB, c);
                
        JButton olsSpeciesButton  = new JButton("Other ...",IconManager.getIcon(IconManager.IconType.PLUS));
        olsSpeciesButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        
        olsSpeciesButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                new OLSDialog((JDialog) SwingUtilities.getAncestorOfClass(JDialog.class, PrideSampleDescPanel.getPrideSampleDescPanel()),
                        PrideSampleDescPanel.getPrideSampleDescPanel() , true, SPECIES_FIELD, "NEWT",null, false);
            }
        });        
        c.gridx++;
        c.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(olsSpeciesButton, c);       
        
        //TISSUE
        JLabel sampleTissueCVLabel = new JLabel("Tissue:");
        c.gridx = 0;
        c.gridy++;
        c.fill = GridBagConstraints.BOTH;
        mainPanel.add(sampleTissueCVLabel, c);
        
        List<CVParam> tissueCVs = getCVParamsFromRsc("/fr/proline/studio/rsmexplorer/gui/dialog/pride/BTO_entries.txt");
        tissueCVs.add(0, EMPTY_PARAM);
        List<Boolean> tissueVisibility = new ArrayList<>(tissueCVs.size());
        for(int i=0; i< tissueCVs.size(); i++){
            tissueVisibility.add(Boolean.FALSE);
        }
        m_sampleTissueCBList = new JCheckBoxList(tissueCVs, tissueVisibility);
        m_sampleTissueCBList.setVisibleRowCount(5);
        JScrollPane tissueScrollPane = new JScrollPane();
        tissueScrollPane.setViewportView(m_sampleTissueCBList);    
        c.gridx++;
        mainPanel.add(tissueScrollPane, c);
                
        JButton olsTissueButton  = new JButton("Other ...",IconManager.getIcon(IconManager.IconType.PLUS));
        olsTissueButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        olsTissueButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                new OLSDialog((JDialog) SwingUtilities.getAncestorOfClass(JDialog.class, PrideSampleDescPanel.getPrideSampleDescPanel()),
                        PrideSampleDescPanel.getPrideSampleDescPanel() , true, TISSUE_FIELD, "BTO",null, false);
            }
        });
        
        c.gridx++;
        c.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(olsTissueButton, c);       

        //Cell Type
        JLabel sampleCellTypeCVLabel = new JLabel("Cell Type:");
        c.gridx = 0;
        c.gridy++;
        c.fill = GridBagConstraints.BOTH;
        mainPanel.add(sampleCellTypeCVLabel, c);

        List<CVParam> cellCVs = getCVParamsFromRsc("/fr/proline/studio/rsmexplorer/gui/dialog/pride/CL_entries.txt");
        cellCVs.add(0, EMPTY_PARAM);
        List<Boolean> cellVisibility = new ArrayList<>(cellCVs.size());
        for(int i=0; i< cellCVs.size(); i++){
            cellVisibility.add(Boolean.FALSE);
        }
        m_sampleCellCBList = new JCheckBoxList<>(cellCVs, cellVisibility);
        m_sampleCellCBList.setVisibleRowCount(5);
        JScrollPane cellScrollPane = new JScrollPane();
        cellScrollPane.setViewportView(m_sampleCellCBList); 
   
        c.gridx++;
        mainPanel.add(cellScrollPane, c);
        
        JButton olsCellButton  = new JButton("Other ...",IconManager.getIcon(IconManager.IconType.PLUS));
        olsCellButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        olsCellButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                new OLSDialog((JDialog) SwingUtilities.getAncestorOfClass(JDialog.class, PrideSampleDescPanel.getPrideSampleDescPanel()),
                        PrideSampleDescPanel.getPrideSampleDescPanel() , true,CELLTYPE_FIELD, "NEWT",null, false);
            }
        });
        
        c.gridx++;
        c.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(olsCellButton, c);       
        
        c.gridx=0;
        c.gridy++;
        c.weighty=1;
        mainPanel.add(Box.createVerticalGlue(), c); 
        return mainPanel;
        
    }
    
    
    @Override
    public void insertOLSResult(String field, Term selectedValue, Term accession, String ontologyShort, String ontologyLong, int modifiedRow, String mappedTerm, List<String> metadata){
        LoggerFactory.getLogger("ProlineStudio.ResultExplorer").debug("field : {}, selectedValue : {}  , accession : {} , ontologyShort : {} ontologyLong: {},  modifiedRow : {} mappedTerm: {} , nbr other : {}"
                , field, selectedValue, accession, ontologyShort, ontologyLong, modifiedRow, mappedTerm, metadata);
        if(field == null)
            return;
        if(field.equals(SPECIES_FIELD)){
            CVParam newParam = new CVParam(ontologyShort, accession.getLabel() ,selectedValue.getLabel(),"" );
            m_sampleSpeciesCBModel.addElement(newParam);
            m_sampleSpeciesCB.setSelectedItem(newParam);
        } else  if(field.equals(TISSUE_FIELD)){
            CVParam newParam = new CVParam(ontologyShort, accession.getLabel() ,selectedValue.getLabel(),"" );
            int newElemIndex = m_sampleTissueCBList.getListSize();
            m_sampleTissueCBList.addItem(newElemIndex, newParam, Boolean.TRUE);
            m_sampleTissueCBList.ensureIndexIsVisible(newElemIndex);
        }else {// if(field.equals(CELLTYPE_FIELD)){
            CVParam newParam = new CVParam(ontologyShort, accession.getLabel() ,selectedValue.getLabel(),"" );           
            int newElemIndex = m_sampleCellCBList.getListSize();
            m_sampleCellCBList.addItem(newElemIndex, newParam, Boolean.TRUE);
            m_sampleCellCBList.ensureIndexIsVisible(newElemIndex);
        }
}

    @Override
    public Window getWindow() {
      return WindowManager.getDefault().getMainWindow();
    }

    public  List<CVParam> getCVParamsFromRsc(String fileResource) {
        InputStream is = PrideSampleDescPanel.class.getResourceAsStream(fileResource);
        final List<CVParam> cvParams = new ArrayList<>();
        BufferedReader in = null;
        if (StringUtils.isEmpty(fileResource) || fileResource.lastIndexOf("_entries.txt") == -1) {
            return cvParams;
        }

        int startIndex = fileResource.lastIndexOf('/')+1;
        String cvLabel = fileResource.substring(startIndex, fileResource.lastIndexOf("_entries.txt"));
        try {
            in = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = in.readLine()) != null) {
                String[] tokens = line.split("\t");
                if (tokens.length == 2) {
                    cvParams.add(new CVParam(cvLabel, tokens[0], tokens[1], null));
                }
            }
            in.close();
            return cvParams;
        } catch (IOException ioe) {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ioe2) {
                }
            }
            return cvParams;
        }
    }
}
