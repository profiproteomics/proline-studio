/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.gui.dialog.xic;

import fr.proline.studio.utils.IconManager;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;

/**
 *
 * @author VD225637
 */
public class DefineQuantParamsPanel extends JPanel{
    
    private static DefineQuantParamsPanel m_singleton = null;
    private JTextField m_extractionMoZTolTF;
    
    private JTextField m_clusteringMoZTolTF;
    private JTextField m_clusteringTimeTolTF;
    private JComboBox m_clusteringTimeComputationCB;
    private JComboBox m_clusteringintensityComputationCB;
     
    private JComboBox m_alnmMethodCB;
    private JTextField m_alnmMassIntervalTF;
    private JTextField m_alnmMaxIterationTF;
    private JComboBox m_alnmSmoothingMethodCB;
    private JTextField m_alnmSmoothingWinSizeTF;
    private JTextField m_alnmSmoothingWinOverlapTF;
    private JTextField m_alnmSmoothingMinWinlandmarksTF;
    private JTextField m_alnmFTMoZTolTF;
    private JTextField m_alnmFTTimeTolTF;
    
    private JComboBox m_FTFilterNameCB;
    private JComboBox m_FTFilterOperatorCB;
    private JTextField m_FTFilterValueTF;
    
    private JTextField m_FTMappingMoZTolTF;
    private JTextField m_FTMappingTimeTolTF;
    
    private JComboBox m_normalizationCB;
    private JCheckBox m_detectFeatureChB;
    private JCheckBox m_validatedPSMsChB;
    
    
    private DefineQuantParamsPanel() {
       
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        add(createWizardPanel(), c);


        c.gridy++;
        c.weighty = 1;
        
        add(createMainPanel(), c);
        
    }
   
    public static DefineQuantParamsPanel getDefineQuantPanel() {
         if (m_singleton == null) {
            m_singleton = new DefineQuantParamsPanel();
        }

        return m_singleton;
    }
    
            //-- quanti Params
            /*"quantitation_config": {
		"extraction_params": {
			"moz_tol": "5",
			"moz_tol_unit": "PPM"
		},
		"clustering_params": {
			"moz_tol": "5",
			"moz_tol_unit": "PPM",
			"time_tol": "15",
			"time_computation": "MOST_INTENSE",
			"intensity_computation": "MOST_INTENSE"
		},
		"aln_method_name": "ITERATIVE",
		"aln_params": {
			"mass_interval": "20000",
			"max_iterations": "3",
			"smoothing_method_name": "TIME_WINDOW",
			"smoothing_params": {
				"window_size": "200",
				"window_overlap": "20",
				"min_window_landmarks": "50"
			},
			"ft_mapping_params": {
				"moz_tol": "5",
				"moz_tol_unit": "PPM",
				"time_tol": "600"
			}
		},
		"ft_filter": {
			"name": "INTENSITY",
			"operator": "GT",
			"value": "0"
		},
		"ft_mapping_params": {
			"moz_tol": "10",
			"moz_tol_unit": "PPM",
			"time_tol": "120"
		},
		"normalization_method": "MEDIAN_RATIO"
	}*/
    public Map<String,Object> getQuantParams(){
        Map<String,Object> params = new HashMap<>();
        
        Map<String,Object> extRactParams = new HashMap<>();
        extRactParams.put("moz_tol", m_extractionMoZTolTF.getText());
        extRactParams.put("moz_tol_unit", "PPM");
        params.put("extraction_params", extRactParams);
        
        Map<String,Object> clusterParams = new HashMap<>();
        clusterParams.put("moz_tol", m_clusteringMoZTolTF.getText());
        clusterParams.put("moz_tol_unit","PPM");
        clusterParams.put("time_tol", m_clusteringTimeTolTF.getText());
        clusterParams.put("time_computation", m_clusteringTimeComputationCB.getSelectedItem().toString());
        clusterParams.put("intensity_computation", m_clusteringintensityComputationCB.getSelectedItem().toString());
        params.put("clustering_params", clusterParams);    
        
        params.put("aln_method_name", m_alnmMethodCB.getSelectedItem().toString());
        Map<String,Object> alnParams = new HashMap<>();
        alnParams.put("mass_interval", m_alnmMassIntervalTF.getText());
        alnParams.put("max_iterations", m_alnmMaxIterationTF.getText());
        alnParams.put("smoothing_method_name", m_alnmSmoothingMethodCB.getSelectedItem().toString());   
        Map<String,Object> smootingParams = new HashMap<>();
        smootingParams.put("window_size", m_alnmSmoothingWinSizeTF.getText());
        smootingParams.put("window_overlap", m_alnmSmoothingWinOverlapTF.getText());
        smootingParams.put("min_window_landmarks", m_alnmSmoothingMinWinlandmarksTF.getText());
        alnParams.put("smoothing_params", smootingParams);     
        Map<String,Object> alnFtParams = new HashMap<>();
        alnFtParams.put("moz_tol", m_alnmFTMoZTolTF.getText());
        alnFtParams.put("moz_tol_unit", "PPM");
        alnFtParams.put("time_tol",  m_alnmFTTimeTolTF.getText());
        alnParams.put("ft_mapping_params", alnFtParams);
        params.put("aln_params", alnParams); 

        Map<String,Object> ftParams = new HashMap<>();     
        ftParams.put("name", m_FTFilterNameCB.getSelectedItem().toString());
        ftParams.put("operator", m_FTFilterOperatorCB.getSelectedItem().toString());
        ftParams.put("value", m_FTFilterValueTF.getText());
        params.put("ft_filter", ftParams); 
         
        Map<String,Object> ftMappingParams = new HashMap<>();
        ftMappingParams.put("moz_tol", m_FTMappingMoZTolTF.getText());
        ftMappingParams.put("moz_tol_unit", "PPM");
        ftMappingParams.put("time_tol", m_FTMappingTimeTolTF.getText());        
        params.put("ft_mapping_params", ftMappingParams); 
        params.put("normalization_method", m_normalizationCB.getSelectedItem().toString());
        params.put("detect_features", m_detectFeatureChB.isSelected());
        params.put("start_from_validated_peptides", m_validatedPSMsChB.isEnabled() && m_validatedPSMsChB.isSelected());
        return params;
    }
        
    private JPanel createWizardPanel() {
        JPanel wizardPanel = new JPanel();
        wizardPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        JLabel wizardLabel = new JLabel("<html><b>Step 4:</b> Specify quantitation parameters.</html>");
        wizardLabel.setIcon(IconManager.getIcon(IconManager.IconType.WAND_HAT));
        
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        wizardPanel.add(wizardLabel, c);
        
        return wizardPanel;
    }
    
    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        
        JLabel extractionMoZTolLabel = new JLabel("Extraction moz tolerance (ppm):");
        m_extractionMoZTolTF = new JTextField(10);
        m_extractionMoZTolTF.setText("5"); //VDS : TODO Manage Preference !
        c.gridx = 0;
        c.gridy = 0;
        mainPanel.add(extractionMoZTolLabel, c);

        c.gridx++;
//        c.weightx = 1;
        mainPanel.add(m_extractionMoZTolTF, c);
        
        c.gridy++;
        c.gridx = 0;
        c.gridwidth = 2;
        mainPanel.add(createClusteringPanel(), c);
        
        c.gridy++;
        c.gridx = 0;
        c.gridwidth = 2;
        mainPanel.add(createAlignementPanel(), c);

        c.gridy++;
        c.gridx = 0;
        c.gridwidth = 2;
        mainPanel.add(createFTPanel(), c);

        c.gridy++;
        c.gridx = 0;
        c.weightx = 0;
        c.gridwidth = 1;
        mainPanel.add(new JLabel("Normalzation "), c);
        m_normalizationCB = new JComboBox();
        m_normalizationCB.addItem("INTENSITY_SUM");
        m_normalizationCB.addItem("MEDIAN_INTENSITY");
        m_normalizationCB.addItem("MEDIAN_RATIO");
        m_normalizationCB.setSelectedItem("MEDIAN_RATIO");//VDS : TODO Manage Preference !
        c.gridx++;
        c.weightx = 1;
        mainPanel.add(m_normalizationCB, c);
        
        c.gridy++;
        c.gridx = 0;
        c.weightx = 0;
        c.gridwidth = 2;
        m_detectFeatureChB = new JCheckBox("Detect all features");
        m_detectFeatureChB.setSelected(false);//VDS : TODO Manage Preference !
        m_detectFeatureChB.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                m_validatedPSMsChB.setEnabled(!m_detectFeatureChB.isSelected());
            }
        });
        mainPanel.add(m_detectFeatureChB, c);
        
        c.gridy++;
        c.gridx = 0;
        c.weightx = 0;
        c.gridwidth = 2;
        m_validatedPSMsChB = new JCheckBox("Only validated PSM");
        m_validatedPSMsChB.setSelected(true);//VDS : TODO Manage Preference !
        mainPanel.add(m_validatedPSMsChB, c);
        
                
        return mainPanel;
    }
    
    private JPanel createClusteringPanel(){
        
        JPanel clusteringPanel = new JPanel(new GridBagLayout());
        clusteringPanel.setBorder(BorderFactory.createTitledBorder(" Clustering "));
        
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.VERTICAL;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        
        c.gridx = 0;
        c.gridy = 0;                
        clusteringPanel.add(new JLabel("moz tolerance (ppm):"), c);        
        c.gridx++;       
        m_clusteringMoZTolTF = new JTextField(10);
        m_clusteringMoZTolTF.setText("5"); //VDS : TODO Manage Preference !
        clusteringPanel.add(m_clusteringMoZTolTF, c);
        
        
        c.gridy++;
        c.gridx = 0;
        clusteringPanel.add(new JLabel("time tolerance:"), c);
        c.gridx++;       
        m_clusteringTimeTolTF = new JTextField(10);
        m_clusteringTimeTolTF.setText("5"); //VDS : TODO Manage Preference !
        clusteringPanel.add(m_clusteringTimeTolTF, c);

        c.gridy++;
        c.gridx = 0;
        clusteringPanel.add(new JLabel("time computation:"), c);
        c.gridx++;               
        m_clusteringTimeComputationCB = new JComboBox();
        m_clusteringTimeComputationCB.addItem("MOST_INTENSE");
        m_clusteringTimeComputationCB.addItem("MEDIAN");
        m_clusteringTimeComputationCB.setSelectedItem("MOST_INTENSE");//VDS : TODO Manage Preference !
        clusteringPanel.add(m_clusteringTimeComputationCB, c);

        c.gridy++;
        c.gridx = 0;
        clusteringPanel.add(new JLabel("intensity computation:"), c);
        c.gridx++;       
        m_clusteringintensityComputationCB = new JComboBox();
        m_clusteringintensityComputationCB.addItem("MOST_INTENSE");
        m_clusteringintensityComputationCB.addItem("SUM");
        m_clusteringintensityComputationCB.setSelectedItem("MOST_INTENSE");//VDS : TODO Manage Preference !
        clusteringPanel.add(m_clusteringintensityComputationCB, c);


        return clusteringPanel;                
    }
    
    private JPanel createAlignementPanel(){
        
        JPanel alignmentPanel = new JPanel(new GridBagLayout());
        alignmentPanel.setBorder(BorderFactory.createTitledBorder(" Alignment "));
        
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        
        c.gridx = 0;
        c.gridy = 0;                
        alignmentPanel.add(new JLabel("method:"), c);        
        c.gridx++;       
        m_alnmMethodCB = new JComboBox();
        m_alnmMethodCB.addItem("EXHAUSTIVE");
        m_alnmMethodCB.addItem("ITERATIVE");
        m_alnmMethodCB.setSelectedItem("ITERATIVE");//VDS : TODO Manage Preference !
        alignmentPanel.add(m_alnmMethodCB, c);
        
        
        c.gridy++;
        c.gridx = 0;
        alignmentPanel.add(new JLabel("mass interval:"), c);
        c.gridx++;       
        m_alnmMassIntervalTF = new JTextField(10);
        m_alnmMassIntervalTF.setText("20000"); //VDS TODO : hide ? see DBO description
        m_alnmMassIntervalTF.setEditable(false);//VDS - DBO info :  Mass Interval (hidden / non-editable)
        alignmentPanel.add(m_alnmMassIntervalTF, c);

        c.gridy++;
        c.gridx = 0;
        alignmentPanel.add(new JLabel("max iteration:"), c);
        c.gridx++;   
        m_alnmMaxIterationTF = new JTextField(10);
        m_alnmMaxIterationTF.setText("3"); //VDS: TODO Manage Preference !
        alignmentPanel.add(m_alnmMaxIterationTF, c);

              // Add Alignement Smoothing specific params
        JPanel smootingPanel = new JPanel(new GridBagLayout());
        smootingPanel.setBorder(BorderFactory.createTitledBorder(" Smoothing "));

        GridBagConstraints c1 = new GridBagConstraints();
        c1.anchor = GridBagConstraints.NORTHWEST;
        c1.fill = GridBagConstraints.BOTH;
        c1.insets = new java.awt.Insets(5, 5, 5, 5);

        c1.gridx = 0;
        c1.gridy = 0;                
        smootingPanel.add(new JLabel("method:"), c1);        
        c1.gridx++;       
        m_alnmSmoothingMethodCB = new JComboBox();
        m_alnmSmoothingMethodCB.addItem("LANDMARK_RANGE");
        m_alnmSmoothingMethodCB.addItem("LOESS");
        m_alnmSmoothingMethodCB.addItem("TIME_WINDOW");
        m_alnmSmoothingMethodCB.setSelectedItem("TIME_WINDOW");//VDS : TODO Manage Preference !
        smootingPanel.add(m_alnmSmoothingMethodCB, c1);
                
        c1.gridy++;
        c1.gridx = 0;
        smootingPanel.add(new JLabel("alignment time interval:"), c1);
        c1.gridx++;   
        m_alnmSmoothingWinSizeTF = new JTextField(10);
        m_alnmSmoothingWinSizeTF.setText("200");//VDS : TODO Manage Preference !
        smootingPanel.add(m_alnmSmoothingWinSizeTF, c1);
    
        c1.gridy++;
        c1.gridx = 0;
        smootingPanel.add(new JLabel("sliding window overlap (%):"), c1);
        c1.gridx++;   
        m_alnmSmoothingWinOverlapTF = new JTextField(10);
        m_alnmSmoothingWinOverlapTF.setText("20");//VDS : TODO Manage Preference !
        smootingPanel.add(m_alnmSmoothingWinOverlapTF, c1);

        c1.gridy++;
        c1.gridx = 0;
        smootingPanel.add(new JLabel("minimum number of landmarks :"), c1);
        c1.gridx++;   
        m_alnmSmoothingMinWinlandmarksTF = new JTextField(10);
        m_alnmSmoothingMinWinlandmarksTF.setText("50");//VDS : TODO Manage Preference !
        smootingPanel.add(m_alnmSmoothingMinWinlandmarksTF, c1);
        
        c.gridy++;
        c.gridx = 0;
        c.weightx = 1.0;
        c.gridwidth = 2;
        alignmentPanel.add(smootingPanel, c);

        
        // Add Alignement  FT mapping specific params
        JPanel ftParamsPanel = new JPanel(new GridBagLayout());
        ftParamsPanel.setBorder(BorderFactory.createTitledBorder(" Feature mapping params "));

        c1 = new GridBagConstraints();
        c1.anchor = GridBagConstraints.NORTHWEST;
        c1.fill = GridBagConstraints.BOTH;
        c1.insets = new java.awt.Insets(5, 5, 5, 5);

        c1.gridx = 0;
        c1.gridy = 0;                
        ftParamsPanel.add(new JLabel("moz tolerance (ppm):"), c1);        
        c1.gridx++;       
        m_alnmFTMoZTolTF = new JTextField(10);
        m_alnmFTMoZTolTF.setText("5");//VDS : TODO Manage Preference !
        ftParamsPanel.add(m_alnmFTMoZTolTF, c1);
        
        c1.gridx = 0;
        c1.gridy++;                
        ftParamsPanel.add(new JLabel("time tolerance:"), c1);        
        c1.gridx++;       
        m_alnmFTTimeTolTF = new JTextField(10);
        m_alnmFTTimeTolTF.setText("600");//VDS : TODO Manage Preference !
        ftParamsPanel.add(m_alnmFTTimeTolTF, c1);
        
        c.gridy++;
        c.gridx = 0;
        c.weightx = 1.0;
        c.gridwidth = 2;
        alignmentPanel.add(ftParamsPanel, c);                

        return alignmentPanel;                
    }           
    
      private JPanel createFTPanel(){
        
        JPanel ftPanel = new JPanel(new GridBagLayout());
        ftPanel.setBorder(BorderFactory.createTitledBorder(" Feature Filter "));
        
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        
        c.gridx = 0;
        c.gridy = 0;                
        ftPanel.add(new JLabel("name:"), c);        
        c.gridx++;       
        m_FTFilterNameCB  = new JComboBox();
        m_FTFilterNameCB.addItem("INTENSITY");
        m_FTFilterNameCB.addItem("RELATIVE_INTENSITY");
        m_FTFilterNameCB.setSelectedItem("INTENSITY");//VDS : TODO Manage Preference !
        ftPanel.add(m_FTFilterNameCB, c);
               
        c.gridy++;
        c.gridx = 0;
        ftPanel.add(new JLabel("operator:"), c);
        c.gridx++;       
        m_FTFilterOperatorCB = new JComboBox();
        m_FTFilterOperatorCB.addItem("GT");
        m_FTFilterOperatorCB.addItem("LT");
        ftPanel.add(m_FTFilterOperatorCB, c);

        c.gridy++;
        c.gridx = 0;
        ftPanel.add(new JLabel("value:"), c);
        c.gridx++;       
        m_FTFilterValueTF = new JTextField(10);
        m_FTFilterValueTF.setText("0");//VDS : TODO Manage Preference !
        ftPanel.add(m_FTFilterValueTF, c);

         // Add FT mapping specific params
        JPanel ftParamsPanel = new JPanel(new GridBagLayout());
        ftParamsPanel.setBorder(BorderFactory.createTitledBorder(" Mapping params "));

        GridBagConstraints c1 = new GridBagConstraints();
        c1.anchor = GridBagConstraints.NORTHWEST;
        c1.fill = GridBagConstraints.BOTH;
        c1.insets = new java.awt.Insets(5, 5, 5, 5);

        c1.gridx = 0;
        c1.gridy = 0;                
        ftParamsPanel.add(new JLabel("moz tolerance (ppm):"), c1);        
        c1.gridx++;       
        m_FTMappingMoZTolTF = new JTextField(10);
        m_FTMappingMoZTolTF.setText("10");//VDS : TODO Manage Preference !
        ftParamsPanel.add(m_FTMappingMoZTolTF, c1);
        
        c1.gridx = 0;
        c1.gridy++;                
        ftParamsPanel.add(new JLabel("time tolerance:"), c1);        
        c1.gridx++;       
        m_FTMappingTimeTolTF = new JTextField(10);
        m_FTMappingTimeTolTF.setText("120");//VDS : TODO Manage Preference !
        ftParamsPanel.add(m_FTMappingTimeTolTF, c1);
        
        c.gridy++;
        c.gridx = 0;
        c.weightx = 1.0;
        c.gridwidth=2;
        ftPanel.add(ftParamsPanel, c); 


        return ftPanel;                
    }
    
    
    
}
