package fr.proline.studio.rsmexplorer.gui.spectrum;

import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import fr.proline.core.orm.msi.ObjectTree;
import fr.proline.core.orm.msi.dto.DPeptideMatch;

/**
 *
 * @author JM235353
 */
public class PeptideFragmentationData {

    private DPeptideMatch m_peptideMatch;
    private TheoreticalFragmentSeries_AW[] m_fragSer;
    private FragmentMatch_AW[] m_fragMa;
    public boolean isEmpty = true;

    public PeptideFragmentationData(DPeptideMatch peptideMatch, ObjectTree objectTree) {
        
        m_peptideMatch = peptideMatch;
        
        if (objectTree != null) {
            String clobData = objectTree.getClobData();
            String jsonProperties = clobData;
            if (!jsonProperties.equals("")) {
                JsonParser parser = new JsonParser();
                Gson gson = new Gson();

                JsonObject array = parser.parse(jsonProperties).getAsJsonObject();
                FragmentationJsonProperties jsonProp = gson.fromJson(array, FragmentationJsonProperties.class);
                // logging
                //LoggerFactory.getLogger("ProlineStudio.ResultExplorer").debug(
                //		jsonProperties
                //		);

                // compute the charge for each fragment match from the label
                for (FragmentMatch_AW fragMa : jsonProp.frag_matches) {
                    fragMa.computeChargeFromLabel();
                }

                m_fragSer = jsonProp.frag_table;
                m_fragMa = jsonProp.frag_matches;
                isEmpty = false;
            }
        }

    }
    
    public DPeptideMatch getPeptideMatch() {
        return m_peptideMatch;
    }

    public TheoreticalFragmentSeries_AW[] getFragmentSeries() {
        return m_fragSer;
    }

    public FragmentMatch_AW[] getFragmentMatch() {
        return m_fragMa;
    }

    public static class FragmentMatch_AW {

        public String label;
        public Double moz;
        public Double calculated_moz;
        public Float intensity;
        public int charge = 1; // the charge taken from the serie (++ means double charged)

        public void computeChargeFromLabel() {
            this.charge = 0;
            if (label != null) {
                for (int i = 0; i < label.length(); i++) {
                    if (label.charAt(i) == '+') {
                        this.charge++;
                    }
                }
            }
            if (this.charge == 0) {
                this.charge = 1;
            }

        }
        public int countSeq (char ch) { // counts the number of times a subSeq appears in label
        	// serves to count the number of * or 0 in order to compare more easily 
        	 int occurence = 0;
             if (label != null) {
                 for (int i = 0; i < label.length(); i++) {
                     if (label.charAt(i) == ch) {
                         occurence++;
                     }
                 }
             }
             return occurence;
        }
    }

    public static class FragmentationJsonProperties {

        public int ms_query_initial_id;
        public int peptide_match_rank;
        public TheoreticalFragmentSeries_AW[] frag_table;
        public FragmentMatch_AW[] frag_matches;
    }

    public class TheoreticalFragmentSeries_AW {

        public String frag_series;
        public double[] masses;
        public int charge = 1; // default to 1 because it is used to multiply
        // the m/z to obtain real mass values for aa
        // calculation

        public void computeChargeFromLabel() {
            this.charge = 0;
            if (frag_series != null) {
                for (int i = 0; i < frag_series.length(); i++) {
                    if (frag_series.charAt(i) == '+') {
                        this.charge++;
                    }
                }
            }
            if (this.charge == 0) {
                this.charge = 1;
            }

        }
        
        public int countSeq (char ch) { // counts the number of times a subSeq appears in label
        	// serves to count the number of * or 0 in order to compare more easily 
        	 int occurence = 0;
             if (frag_series != null) {
                 for (int i = 0; i < frag_series.length(); i++) {
                     if (frag_series.charAt(i) == ch) {
                         occurence++;
                     }
                 }
             }
             return occurence;
        }
    }
}