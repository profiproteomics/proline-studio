/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.corewrapper.data;

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
    public final static String PARAM_VERSION_KEY = SETTINGS_KEY + ".parametersVersion";
     
    //V1 Only params
    public final static String DISCARD_OXIDIZED_PEPTIDES = "discard_oxidized_peptides";
    public final static String DISCARD_MISS_CLEAVED_PEPTIDES_V1 = "discard_missed_cleaved_peptides";//V1
    public final static String ABUNDANCE_SUMMARIZING_METHOD_V1 = "abundance_summarizer_method";//V1
        
    //Commons and V2 params
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

    public final static String MODIFIED_PEPTIDE_FILTERING_DISCARD_ALL_FORMS_METHOD_VALUE = "Discard all forms";
    public final static String MODIFIED_PEPTIDE_FILTERING_DISCARD_ALL_FORMS_METHOD_KEY = "DISCARD_ALL_FORMS";
    public final static String MODIFIED_PEPTIDE_FILTERING_DISCARD_MODIFIED_FORMS_METHOD_VALUE = "Discard modified forms only";
    public final static String MODIFIED_PEPTIDE_FILTERING_DISCARD_MODIFIED_FORMS_METHOD_KEY = "DISCARD_MODIFIED_FORMS";    
    
    private final static String[] MODIFIED_PEPTIDE_FILTERING_METHOD_VALUES = {MODIFIED_PEPTIDE_FILTERING_DISCARD_ALL_FORMS_METHOD_VALUE, MODIFIED_PEPTIDE_FILTERING_DISCARD_MODIFIED_FORMS_METHOD_VALUE};
    private final static String[] MODIFIED_PEPTIDE_FILTERING_METHOD_KEYS = {MODIFIED_PEPTIDE_FILTERING_DISCARD_ALL_FORMS_METHOD_KEY, MODIFIED_PEPTIDE_FILTERING_DISCARD_MODIFIED_FORMS_METHOD_KEY};
    
    public final static String ION_ABUNDANCE_SUMMARIZING_BEST_ION_METHOD_VALUE = "Best Ion";
    public final static String ION_ABUNDANCE_SUMMARIZING_BEST_ION_METHOD_KEY = "BEST_ION";
    public final static String ION_ABUNDANCE_SUMMARIZING_SUM_METHOD_VALUE = "Sum";
    public final static String ION_ABUNDANCE_SUMMARIZING_SUM_METHOD_KEY = "SUM";    
        
    private final static String[] ION_ABUNDANCE_SUMMARIZING_METHOD_VALUES = {ION_ABUNDANCE_SUMMARIZING_BEST_ION_METHOD_VALUE, ION_ABUNDANCE_SUMMARIZING_SUM_METHOD_VALUE};
    private final static String[] ION_ABUNDANCE_SUMMARIZING_METHOD_KEYS = {ION_ABUNDANCE_SUMMARIZING_BEST_ION_METHOD_KEY,ION_ABUNDANCE_SUMMARIZING_SUM_METHOD_KEY};
    
    
    public final static String USE_ONLY_SPECIFIC_PEPTIDES = "use_only_specific_peptides";
    public final static String DISCARD_MISS_CLEAVED_PEPTIDES = "discard_miss_cleaved_peptides"; 
    public final static String DISCARD_MODIFIED_PEPTIDES = "discard_modified_peptides";
    public final static String MODIFIED_PEPTIDE_FILTERING_METHOD = "modified_peptide_filtering_method";
    public final static String PTM_DEFINITION_IDS_TO_DISCARD = "ptm_definition_ids_to_discard";
    public final static String DISCARD_PEPTIDES_SHARING_PEAKELS = "discard_peptides_sharing_peakels";
    public final static String ABUNDANCE_SUMMARIZING_METHOD = "abundance_summarizing_method";
    public final static String APPLY_NORMALIZATION = "apply_normalization";
    public final static String ION_ABUNDANCE_SUMMARIZING_METHOD = "pep_ion_abundance_summarizing_method";

    public final static String CONFIG_VERSION = "config_version";
    
    public static String getSnakeCase(String value){
        
    }
    
    public static final String[] getAbundanceSummarizingMethodValues(){
        return ABUNDANCE_SUMMARIZING_METHOD_VALUES;
    }
    
    public static final String[] getAbundanceSummarizingMethodKeys(){
        return ABUNDANCE_SUMMARIZING_METHOD_KEYS;
    }
    
    
    public static final String[] getModifiedPeptideFilteringMethodValues(){
        return MODIFIED_PEPTIDE_FILTERING_METHOD_VALUES;
    }
    
    public static final String[] getModifiedPeptideFilteringMethodKeys(){
        return MODIFIED_PEPTIDE_FILTERING_METHOD_KEYS;
    }    
    
    public static  final String[] getIonAbundanceSummarizingMethodValues(){
        return ION_ABUNDANCE_SUMMARIZING_METHOD_VALUES;
    }
    
    public static final String[] getIonAbundanceSummarizingMethodKeys(){
        return ION_ABUNDANCE_SUMMARIZING_METHOD_KEYS;
    }        
    
}
