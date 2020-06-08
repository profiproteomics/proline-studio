/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.corewrapper.data;

import fr.proline.studio.utils.StringUtils;

/**
 * Define constant related to Compute Post Processing parameters.
 *
 * @author VD225637
 */
//VD TODO add camelCase to snake_case to use in properties ! 
public class QuantPostProcessingParams {
    
    public final static String SETTINGS_KEY = "QuantPostProcessing";
    public final static String PREVIOUS_SETTINGS_KEY = "QuantProfile";
    public final static String CURRENT_VERSION ="3.0";
    public final static String CONFIG_VERSION = "config_version";
    public final static String PARAM_VERSION_KEY = SETTINGS_KEY + ".parametersVersion";
                
    //-- ABUNDANCE_SUMMARIZING methods and keys
    public final static String ABUNDANCE_SUMMARIZING_METHOD = "abundance_summarizing_method";//V2    
    public final static String ABUNDANCE_SUMMARIZER_METHOD = "abundance_summarizer_method";//V1
    public final static String PEPTIDE_ABUNDANCE_SUMMARIZING_METHOD = "peptide_abundance_summarizing_method";//V3    
    
    public final static String ABUNDANCE_SUMMARIZING_MEAN_METHOD_VALUE = "Mean";
    public final static String ABUNDANCE_SUMMARIZING_MEAN_METHOD_KEY = "MEAN";
    public final static String ABUNDANCE_SUMMARIZING_MEAN_TOP3_METHOD_VALUE = "Mean of top 3 peptides";
    public final static String ABUNDANCE_SUMMARIZING_MEAN_TOP3_METHOD_KEY = "MEAN_OF_TOP3";
    public final static String ABUNDANCE_SUMMARIZING_MEDIAN_METHOD_VALUE = "Median";
    public final static String ABUNDANCE_SUMMARIZING_MEDIAN_METHOD_KEY = "MEDIAN";
    public final static String ABUNDANCE_SUMMARIZING_MEDIAN_BP_METHOD_VALUE = "Median Biological Profile";
    public final static String ABUNDANCE_SUMMARIZING_MEDIAN_BP_METHOD_KEY = "MEDIAN_BIOLOGICAL_PROFILE";
    public final static String ABUNDANCE_SUMMARIZING_MEDIAN_PROFILE_METHOD_VALUE = "Median Profile";
    public final static String ABUNDANCE_SUMMARIZING_MEDIAN_PROFILE_METHOD_KEY = "MEDIAN_PROFILE";
    public final static String ABUNDANCE_SUMMARIZING_SUM_METHOD_VALUE = "Sum";
    public final static String ABUNDANCE_SUMMARIZING_SUM_METHOD_KEY = "SUM";
    public final static String ABUNDANCE_SUMMARIZING_LFQ_METHOD_VALUE = "Median Ratio Fitting";
    public final static String ABUNDANCE_SUMMARIZING_LFQ_METHOD_KEY = "LFQ";  
    
    private final static String[] ABUNDANCE_SUMMARIZING_METHOD_VALUES =  {ABUNDANCE_SUMMARIZING_MEAN_METHOD_VALUE, ABUNDANCE_SUMMARIZING_MEAN_TOP3_METHOD_VALUE, 
                        ABUNDANCE_SUMMARIZING_MEDIAN_METHOD_VALUE, ABUNDANCE_SUMMARIZING_MEDIAN_BP_METHOD_VALUE, ABUNDANCE_SUMMARIZING_MEDIAN_PROFILE_METHOD_VALUE,
                        ABUNDANCE_SUMMARIZING_SUM_METHOD_VALUE, ABUNDANCE_SUMMARIZING_LFQ_METHOD_VALUE };    
    private final static String[] ABUNDANCE_SUMMARIZING_METHOD_KEYS = {ABUNDANCE_SUMMARIZING_MEAN_METHOD_KEY, ABUNDANCE_SUMMARIZING_MEAN_TOP3_METHOD_KEY, ABUNDANCE_SUMMARIZING_MEDIAN_METHOD_KEY,
                        ABUNDANCE_SUMMARIZING_MEDIAN_BP_METHOD_KEY, ABUNDANCE_SUMMARIZING_MEDIAN_PROFILE_METHOD_KEY, ABUNDANCE_SUMMARIZING_SUM_METHOD_KEY, 
                        ABUNDANCE_SUMMARIZING_LFQ_METHOD_KEY};
    //--
    
    //-- MODIFIED_PEPTIDE_FILTERING methods and keys
    public final static String DISCARD_MODIFIED_PEPTIDES = "discard_modified_peptides"; //V2&3
    public final static String DISCARD_OXIDIZED_PEPTIDES = "discard_oxidized_peptides"; //V1
    
    public final static String PTM_DEFINITION_IDS_TO_DISCARD = "ptm_definition_ids_to_discard"; 
    public final static String MODIFIED_PEPTIDE_FILTERING_METHOD = "modified_peptide_filtering_method";
    public final static String MODIFIED_PEPTIDE_FILTER_CONFIG = "modified_peptide_filter_config";    
    
    public final static String MODIFIED_PEPTIDE_FILTERING_DISCARD_ALL_FORMS_METHOD_VALUE = "Discard all forms";
    public final static String MODIFIED_PEPTIDE_FILTERING_DISCARD_ALL_FORMS_METHOD_KEY = "DISCARD_ALL_FORMS";
    public final static String MODIFIED_PEPTIDE_FILTERING_DISCARD_MODIFIED_FORMS_METHOD_VALUE = "Discard modified forms only";
    public final static String MODIFIED_PEPTIDE_FILTERING_DISCARD_MODIFIED_FORMS_METHOD_KEY = "DISCARD_MODIFIED_FORMS";     
    
    private final static String[] MODIFIED_PEPTIDE_FILTERING_METHOD_VALUES = {MODIFIED_PEPTIDE_FILTERING_DISCARD_ALL_FORMS_METHOD_VALUE, MODIFIED_PEPTIDE_FILTERING_DISCARD_MODIFIED_FORMS_METHOD_VALUE};
    private final static String[] MODIFIED_PEPTIDE_FILTERING_METHOD_KEYS = {MODIFIED_PEPTIDE_FILTERING_DISCARD_ALL_FORMS_METHOD_KEY, MODIFIED_PEPTIDE_FILTERING_DISCARD_MODIFIED_FORMS_METHOD_KEY};
    //--
    
    //-- IONS ABUNDANCE_SUMMARIZING methods and keys
    public final static String PEP_ION_ABUNDANCE_SUMMARIZING_METHOD = "pep_ion_abundance_summarizing_method";
    public final static String ION_ABUNDANCE_SUMMARIZING_BEST_ION_METHOD_VALUE = "Best Ion";
    public final static String ION_ABUNDANCE_SUMMARIZING_BEST_ION_METHOD_KEY = "BEST_ION";
    public final static String ION_ABUNDANCE_SUMMARIZING_SUM_METHOD_VALUE = "Sum";
    public final static String ION_ABUNDANCE_SUMMARIZING_SUM_METHOD_KEY = "SUM";    
    
    private final static String[] ION_ABUNDANCE_SUMMARIZING_METHOD_VALUES = {ION_ABUNDANCE_SUMMARIZING_BEST_ION_METHOD_VALUE, ION_ABUNDANCE_SUMMARIZING_SUM_METHOD_VALUE};
    private final static String[] ION_ABUNDANCE_SUMMARIZING_METHOD_KEYS = {ION_ABUNDANCE_SUMMARIZING_BEST_ION_METHOD_KEY,ION_ABUNDANCE_SUMMARIZING_SUM_METHOD_KEY};
    //--
    
    // --PEPTIDES_SELECTION methods and keys
    public final static String USE_ONLY_SPECIFIC_PEPTIDES = "use_only_specific_peptides"; //V1.0 & 2.0 
    public final static String PEPTIDE_SELECTION_METHOD = "peptides_selection_method"; //V3.0 
    
    public final static String PEPTIDES_SELECTION_ALL_PEPTIDES_METHOD_VALUE = "All Peptides";
    public final static String PEPTIDES_SELECTION_ALL_PEPTIDES_METHOD_KEY = "ALL_PEPTIDES";
    public final static String PEPTIDES_SELECTION_SPECIFIC_METHOD_VALUE = "Specific";
    public final static String PEPTIDES_SELECTION_SPECIFIC_METHOD_KEY = "SPECIFIC";
    public final static String PEPTIDES_SELECTION_RAZOR_AND_SPECIFIC_METHOD_VALUE = "Razor and Specific";
    public final static String PEPTIDES_SELECTION_RAZOR_AND_SPECIFIC_METHOD_KEY = "RAZOR_AND_SPECIFIC";    
    
    private final static String[] PEPTIDES_SELECTION_METHOD_VALUES = {PEPTIDES_SELECTION_SPECIFIC_METHOD_VALUE, PEPTIDES_SELECTION_RAZOR_AND_SPECIFIC_METHOD_VALUE, PEPTIDES_SELECTION_ALL_PEPTIDES_METHOD_VALUE};
    private final static String[] PEPTIDES_SELECTION_METHOD_KEYS = {PEPTIDES_SELECTION_SPECIFIC_METHOD_KEY,PEPTIDES_SELECTION_RAZOR_AND_SPECIFIC_METHOD_KEY , PEPTIDES_SELECTION_ALL_PEPTIDES_METHOD_KEY};
        
    // -- other params
    public final static String DISCARD_MISS_CLEAVED_PEPTIDES = "discard_miss_cleaved_peptides";  //V3
    public final static String DISCARD_MISS_CLEAVED_PEPTIDES_PREV = "discard_missed_cleaved_peptides";//V1&2
    public final static String DISCARD_PEPTIDES_SHARING_PEAKELS = "discard_peptides_sharing_peakels";//V1&2
    public final static String DISCARD_PEP_IONS_SHARING_PEAKELS = "discard_pep_ions_sharing_peakels";// V3        
    public final static String APPLY_PROFILE_CLUSTERING = "apply_profile_clustering";

    //Common params to peptide and protein...
    public final static String STAT_CONFIG = "stat_config";
    public final static String STAT_TESTS_ALPHA = "stat_tests_alpha";
    public final static String APPLY_MISS_VAL_INFERENCE = "apply_miss_val_inference";
    public final static String APPLY_VARIANCE_CORRECTION = "apply_variance_correction";
    public final static String APPLY_T_TEST = "apply_ttest";
    public final static String APPLY_Z_TEST = "apply_ztest";
    public final static String APPLY_NORMALIZATION = "apply_normalization"; 

        
    //Constants for settings params, not for tasks (JSON) params
    public final static String PREFIX_PTM_IDS_TO_DISCARD = "discardPeptideModification_"; 
    public final static String ION_ABUNDANCE_SUMMARIZER_METHOD_SETTING_V2 = "ionAbundanceSummarizerMethod";
    public final static String MODIFIED_PEPTIDES_FILTERING_METHOD_SETTING_V1_2 = "modifiedPeptidesFilteringMethod";
    
    public static final String[] getPeptideAbundanceSummarizingMethodValues(){
        return ABUNDANCE_SUMMARIZING_METHOD_VALUES;
    }
    
    public static final String[] getPeptideAbundanceSummarizingMethodKeys(){
        return ABUNDANCE_SUMMARIZING_METHOD_KEYS;
    }
    
    
    public static final String[] getModifiedPeptideFilteringMethodValues(){
        return MODIFIED_PEPTIDE_FILTERING_METHOD_VALUES;
    }
    
    public static final String[] getModifiedPeptideFilteringMethodKeys(){
        return MODIFIED_PEPTIDE_FILTERING_METHOD_KEYS;
    }    
    
    public static  final String[] getPepIonAbundanceSummarizingMethodValues(){
        return ION_ABUNDANCE_SUMMARIZING_METHOD_VALUES;
    }
    
    public static final String[] getPepIonAbundanceSummarizingMethodKeys(){
        return ION_ABUNDANCE_SUMMARIZING_METHOD_KEYS;
    }        
    
    public static  final String[] getPeptidesSelectionMethodValues(){
        return PEPTIDES_SELECTION_METHOD_VALUES;
    }
    
    public static final String[] getPeptidesSelectionMethodKeys(){
        return PEPTIDES_SELECTION_METHOD_KEYS;
    }   

    public static final String getPrefixedParam(String prefixe, String key){       
        StringBuilder sb = new StringBuilder(prefixe);
        sb.append("_").append(key);
        return sb.toString();
    }
       
   public static final String getPrefixedSettingKey(String prefixe, String key){       
       return prefixe+StringUtils.snakeCasetoCamelCase(key, true);
   }

   public static final String getSettingKey(String param){
       return StringUtils.snakeCasetoCamelCase(param, false);
   }
}
