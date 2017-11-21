package fr.proline.studio.rsmexplorer.gui.dialog.xic;

import fr.proline.studio.parameter.AbstractParameter;
import fr.proline.studio.parameter.DoubleParameter;
import fr.proline.studio.parameter.IntegerParameter;
import fr.proline.studio.parameter.ObjectParameter;
import fr.proline.studio.parameter.ParameterList;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import org.openide.util.NbPreferences;

/**
 *
 * @author JM235353
 */
public class DefineQuantParamsSimplifiedPanel extends AbstractDefineQuantParamsPanel {
    

    

    public DefineQuantParamsSimplifiedPanel(boolean readOnly, boolean readValues) {
        super(readOnly);

        m_parameterList = new ParameterList("XicParameters");
        createParameters();
        
        if (readValues) {
            m_parameterList.updateIsUsed(NbPreferences.root());
        } else {
            m_parameterList.setDefaultValues();
        }
        for (AbstractParameter param : m_parameterList) {
            param.setUsed(true);
        }

        setLayout(new GridBagLayout());
        
        final GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);


        JPanel mainPanel = createMainPanel();
        m_scrollPane = new JScrollPane();
        m_scrollPane.setViewportView(mainPanel);
        m_scrollPane.createVerticalScrollBar();
        
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        add(m_scrollPane, c);

    }

    @Override
    public boolean isSimplifiedPanel() {
        return true;
    }
    
    private void createParameters() {
        
        m_extractionMoZTolTF = new JTextField();
        m_extractionMoZTolTF.setEnabled(!m_readOnly);
        DoubleParameter extractionMoZTolParameter = new DoubleParameter("extractionMoZTol", "Extraction moz tolerance", m_extractionMoZTolTF, new Double(5), new Double(0), null);
        m_parameterList.add(extractionMoZTolParameter);

        
        m_alignmentMethodCB = new JComboBox(ALIGNMENT_METHOD_VALUES);
        m_alignmentMethodCB.setEnabled(!m_readOnly);
        m_alignmentMethodParameter = new ObjectParameter("alignmentMethod", "Alignment Method", m_alignmentMethodCB, ALIGNMENT_METHOD_VALUES, ALIGNMENT_METHOD_KEYS,  1, null);
        m_parameterList.add(m_alignmentMethodParameter);

        m_alignmentMaxIterationTF = new JTextField();
        m_alignmentMaxIterationTF.setEnabled(!m_readOnly);
        IntegerParameter alignmentMaxIterationParameter = new IntegerParameter("alignmentMaxIteration", "Alignment max iteration", m_alignmentMaxIterationTF, new Integer(3), new Integer(1), null);
        m_parameterList.add(alignmentMaxIterationParameter);
        
        m_alignmentSmoothingMethodCB = new JComboBox(ALIGNMENT_SMOOTHING_METHOD_VALUES);
        m_alignmentSmoothingMethodCB.setEnabled(!m_readOnly);
        m_alignmentSmoothingMethodParameter = new ObjectParameter("alignmentSmoothingMethod", "Alignment Smoothing Method", m_alignmentSmoothingMethodCB, ALIGNMENT_SMOOTHING_METHOD_VALUES, ALIGNMENT_SMOOTHING_METHOD_KEYS,  0, null);
        m_parameterList.add(m_alignmentSmoothingMethodParameter);

        
        m_alignmentSmoothingWinSizeTF = new JTextField();
        m_alignmentSmoothingWinSizeTF.setEnabled(!m_readOnly);
        IntegerParameter alignmentSmoothingWinSizeParameter = new IntegerParameter("alignmentTimeInterval", "Alignment time interval", m_alignmentSmoothingWinSizeTF, new Integer(200), new Integer(1), null);
        m_parameterList.add(alignmentSmoothingWinSizeParameter);

         m_alignmentSmoothingMinWinlandmarksTF = new JTextField();
         m_alignmentSmoothingMinWinlandmarksTF.setEnabled(!m_readOnly);
        IntegerParameter alignmentSmoothingMinWinlandmarksParameter = new IntegerParameter("smoothingMinimumNumberOfLandmarks", "Smoothing minimum number of landmarks", m_alignmentSmoothingMinWinlandmarksTF, new Integer(50), new Integer(1), null);
        m_parameterList.add(alignmentSmoothingMinWinlandmarksParameter);

        
        m_alignmentFeatureMappingTimeToleranceTF = new JTextField();
        m_alignmentFeatureMappingTimeToleranceTF.setEnabled(!m_readOnly);
        DoubleParameter alignmentFeatureMappingTimeToleranceParameter = new DoubleParameter("featureMappingTimeTolerance", "Feature Mapping Time Tolerance", m_alignmentFeatureMappingTimeToleranceTF, new Double(600), new Double(1), null);
        m_parameterList.add(alignmentFeatureMappingTimeToleranceParameter);

        
        m_featureMappingTimeTolTF = new JTextField();
        m_featureMappingTimeTolTF.setEnabled(!m_readOnly);
        DoubleParameter featureMappingTimeTolParameter = new DoubleParameter("featureTimeTol", "Feature time tolerance", m_featureMappingTimeTolTF, new Double(60), new Double(0), null);
        m_parameterList.add(featureMappingTimeTolParameter);

    }
    

    

    
    

    
    /**
     * set the quanti params
     * @param quantParams 
     */
    @Override
    public void setQuantParams(Map<String,Object>  quantParams){
        Map<String,Object> extRactParams = (Map<String,Object>) quantParams.get("extraction_params");
        m_extractionMoZTolTF.setText(""+Double.parseDouble(extRactParams.get("moz_tol").toString()));


        for(int i=0; i<ALIGNMENT_METHOD_KEYS.length; i++){
            if (ALIGNMENT_METHOD_KEYS[i].equals((String) quantParams.get("aln_method_name"))){
                m_alignmentMethodCB.setSelectedIndex(i);
                break;
            }
        }
        
        Map<String,Object> alnParams =(Map<String,Object>) quantParams.get("aln_params");
        m_alignmentMaxIterationTF.setText(""+Integer.parseInt(alnParams.get("max_iterations").toString()));
        for(int i=0; i<ALIGNMENT_SMOOTHING_METHOD_KEYS.length; i++){
            if (ALIGNMENT_SMOOTHING_METHOD_KEYS[i].equals((String) alnParams.get("smoothing_method_name"))){
                m_alignmentSmoothingMethodCB.setSelectedIndex(i);
                break;
            }
        }
        Map<String,Object> smootingParams =(Map<String,Object>) alnParams.get("smoothing_params");
        m_alignmentSmoothingWinSizeTF.setText(""+Integer.parseInt(smootingParams.get("window_size").toString()));
        //m_alignmentSmoothingWinOverlapTF.setText(""+Integer.parseInt(smootingParams.get("window_overlap").toString()));
        m_alignmentSmoothingMinWinlandmarksTF.setText(""+Integer.parseInt(smootingParams.get("min_window_landmarks").toString()));
        Map<String,Object> alnFtParams = (Map<String,Object>) alnParams.get("ft_mapping_params");
        m_alignmentFeatureMappingTimeToleranceTF.setText(""+Double.parseDouble(alnFtParams.get("time_tol").toString()));

        
        Map<String,Object> ftMappingParams =(Map<String,Object>) quantParams.get("ft_mapping_params");
        try{
            m_featureMappingTimeTolTF.setText(""+Double.parseDouble(ftMappingParams.get("time_tol").toString()));
        }catch(NumberFormatException ex){
            m_logger.error("error while settings quanti params "+ex);
            m_featureMappingTimeTolTF.setText("60.0");
        }


    }
    
    
    @Override
    public Map<String,Object> getQuantParams(){
        Map<String,Object> params = new HashMap<>();
        
        Map<String,Object> extRactParams = new HashMap<>();
        extRactParams.put("moz_tol", m_extractionMoZTolTF.getText());
        extRactParams.put("moz_tol_unit", "PPM");
        params.put("extraction_params", extRactParams);
        
        Map<String,Object> clusterParams = new HashMap<>();
        clusterParams.put("moz_tol", m_extractionMoZTolTF.getText());
        clusterParams.put("moz_tol_unit","PPM");
        clusterParams.put("time_tol", "15");
        clusterParams.put("time_computation", "MOST_INTENSE");
        clusterParams.put("intensity_computation", "MOST_INTENSE");
        params.put("clustering_params", clusterParams);    
        
        params.put("aln_method_name", m_alignmentMethodParameter.getStringValue());
        Map<String,Object> alnParams = new HashMap<>();
        alnParams.put("mass_interval", "20000");
        alnParams.put("max_iterations", m_alignmentMaxIterationTF.getText());
        alnParams.put("smoothing_method_name", m_alignmentSmoothingMethodParameter.getStringValue());   
        Map<String,Object> smootingParams = new HashMap<>();
        smootingParams.put("window_size", m_alignmentSmoothingWinSizeTF.getText());
        smootingParams.put("window_overlap", "20");
        smootingParams.put("min_window_landmarks", m_alignmentSmoothingMinWinlandmarksTF.getText());
        alnParams.put("smoothing_params", smootingParams);     
        Map<String,Object> alnFtParams = new HashMap<>();
        alnFtParams.put("moz_tol", m_extractionMoZTolTF.getText());
        alnFtParams.put("moz_tol_unit", "PPM");
        alnFtParams.put("time_tol",  m_alignmentFeatureMappingTimeToleranceTF.getText());
        alnParams.put("ft_mapping_params", alnFtParams);
        params.put("aln_params", alnParams); 

        Map<String,Object> ftParams = new HashMap<>();     
        ftParams.put("name", "INTENSITY");
        ftParams.put("operator", "GT");
        ftParams.put("value", "0.0");
        params.put("ft_filter", ftParams); 
         
        params.put("ft_mapping_method_name", FEATURE_MAPPING_METHOD_KEYS[0]);
        Map<String,Object> ftMappingParams = new HashMap<>();
        ftMappingParams.put("moz_tol", m_extractionMoZTolTF.getText());
        ftMappingParams.put("moz_tol_unit", "PPM");
        ftMappingParams.put("time_tol", m_featureMappingTimeTolTF.getText());        
        params.put("ft_mapping_params", ftMappingParams); 


        params.put("detect_features", false);
        params.put("start_from_validated_peptides", true);
        params.put("detect_peakels", true);

        return params;
    }

    
    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        mainPanel.add(createHeaderPanel(), c);

        c.gridy++;
        mainPanel.add(createAlignmentPanel(), c);
        
        c.gridy++;
        mainPanel.add(createFTPanel(), c);
        
        c.gridy++;
        c.weighty = 1;
        mainPanel.add(Box.createVerticalGlue(), c);
        
        return mainPanel;
    }

    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        
        JLabel extractionMoZTolLabel = new JLabel("Moz tolerance (ppm):");
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth=1;
        headerPanel.add(extractionMoZTolLabel, c);

        c.gridx++;
        c.gridwidth=1;
        c.weightx = 1;
        headerPanel.add(m_extractionMoZTolTF, c);
        
        return headerPanel;
    }
    
    private JPanel createAlignmentPanel(){
        JPanel panelA = new JPanel(new BorderLayout());
        
        JPanel alignmentPanel = new JPanel(new GridBagLayout());
        alignmentPanel.setBorder(createTitledBorder(" Map Alignment ", 1));
        
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST ;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        
        c.gridx = 0;
        c.gridy = 0;                
        alignmentPanel.add(new JLabel("method:"), c);        
        c.gridx++;     
        c.weightx = 1;
        alignmentPanel.add(m_alignmentMethodCB, c);
        c.weightx = 0;

        
        c.gridy++;
        c.gridx = 0;
        alignmentPanel.add(new JLabel("max iteration:"), c);
        c.gridx++;   
        c.weightx = 1;
        alignmentPanel.add(m_alignmentMaxIterationTF, c);
        c.weightx = 0;

        // Add Alignement Smoothing specific params
        JPanel smootingPanel = new JPanel(new GridBagLayout());
        smootingPanel.setBorder(createTitledBorder(" Smoothing ", 2));

        GridBagConstraints c1 = new GridBagConstraints();
        c1.anchor = GridBagConstraints.NORTHWEST;
        c1.fill = GridBagConstraints.BOTH;
        c1.insets = new java.awt.Insets(5, 5, 5, 5);

        c1.gridx = 0;
        c1.gridy = 0;                
        smootingPanel.add(new JLabel("method:"), c1);        
        c1.gridx++;     
        c1.weightx = 1;
        smootingPanel.add(m_alignmentSmoothingMethodCB, c1);
        c1.weightx = 0;
                
        c1.gridy++;
        c1.gridx = 0;
        smootingPanel.add(new JLabel("alignment time interval (s):"), c1);
        c1.gridx++;  
        c1.weightx = 1;
        smootingPanel.add(m_alignmentSmoothingWinSizeTF, c1);
        c1.weightx = 0;
    
        

        c1.gridy++;
        c1.gridx = 0;
        smootingPanel.add(new JLabel("minimum number of landmarks :"), c1);
        c1.gridx++; 
        c1.weightx = 1;
        smootingPanel.add(m_alignmentSmoothingMinWinlandmarksTF, c1);
        c1.weightx = 0;
        
        c.gridy++;
        c.gridx = 0;
        c.weightx = 1.0;
        c.gridwidth = 2;
        alignmentPanel.add(smootingPanel, c);

        
        // Add Alignement  FT mapping specific params
        JPanel ftParamsPanel = new JPanel(new GridBagLayout());
        ftParamsPanel.setBorder(createTitledBorder(" Feature Mapping ", 2));

        c1 = new GridBagConstraints();
        c1.anchor = GridBagConstraints.FIRST_LINE_START;
        c1.fill = GridBagConstraints.BOTH;
        c1.insets = new java.awt.Insets(5, 5, 5, 5);

        c1.gridx = 0;
        c1.gridy = 0;    
             
             
        ftParamsPanel.add(new JLabel("time tolerance (s):"), c1);        
        c1.gridx++;   
        c1.weightx = 1;
        ftParamsPanel.add(m_alignmentFeatureMappingTimeToleranceTF, c1);
        c1.weightx = 0;
        
        c.gridy++;
        c.gridx = 0;
        c.weightx = 1.0;
        c.gridwidth = 2;
        alignmentPanel.add(ftParamsPanel, c);                

        panelA.add(alignmentPanel, BorderLayout.NORTH);
        return panelA ;                 
    }           
    
    private JPanel createFTPanel(){
        JPanel panelF = new JPanel(new BorderLayout());
        
        JPanel masterMapPanel = new JPanel(new GridBagLayout());
        masterMapPanel.setBorder(createTitledBorder(" Master Map ", 1));
        
        
        GridBagConstraints cm = new GridBagConstraints();
        cm.anchor = GridBagConstraints.FIRST_LINE_START ;
        cm.fill = GridBagConstraints.BOTH;
        cm.insets = new java.awt.Insets(5, 5, 5, 5);
        
        
        
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST ;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        
        c.gridx = 0;
        c.gridy = 0;

        cm.gridy = 0;
        cm.gridx = 0;
        cm.weightx = 1.0;
        cm.gridwidth = 2;


         // Add FT mapping specific params
        JPanel ftParamsPanel = new JPanel(new GridBagLayout());
        ftParamsPanel.setBorder(createTitledBorder(" Feature Mapping ", 2));

        GridBagConstraints c1 = new GridBagConstraints();
        c1.anchor = GridBagConstraints.NORTHWEST ;
        c1.fill = GridBagConstraints.BOTH;
        c1.insets = new java.awt.Insets(5, 5, 5, 5);

        c1.gridx = 0;
        c1.gridy = 0;                

             
        ftParamsPanel.add(new JLabel("time tolerance (s):"), c1);        
        c1.gridx++;     
        c1.weightx = 1;
        ftParamsPanel.add(m_featureMappingTimeTolTF, c1);
        c1.weightx = 0;
        
        c.gridy++;
        c.gridx = 0;
        c.weightx = 1.0;
        c.gridwidth=2;
        
        cm.gridy++;
        cm.gridx = 0;
        cm.weightx = 1.0;
        cm.gridwidth = 2;
        masterMapPanel.add(ftParamsPanel, cm);

        panelF.add(masterMapPanel, BorderLayout.NORTH);
        return panelF;                
    }
    

    
    
}
