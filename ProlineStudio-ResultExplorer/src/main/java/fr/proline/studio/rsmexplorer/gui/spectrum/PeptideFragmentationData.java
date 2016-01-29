package fr.proline.studio.rsmexplorer.gui.spectrum;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

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
    private TheoreticalFragmentSeries[] m_fragSer;
    private FragmentMatch[] m_fragMa;
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

                if (jsonProp.frag_matches == null) {
                    System.out.println("no fragment match");
                    jsonProp.frag_matches = new FragmentMatch[0];
                }

                // sort theoretical frag table according to serie's label for proper display in frag jtable
                ArrayList<TheoreticalFragmentSeries> fragSerArrayList = new ArrayList<TheoreticalFragmentSeries>();
                if (jsonProp.frag_table != null) {
                    for (TheoreticalFragmentSeries frag : jsonProp.frag_table) {
                        fragSerArrayList.add(frag);
                    }
                }
                //Sorting
                Collections.sort(fragSerArrayList, new Comparator<TheoreticalFragmentSeries>() {
                    public int compare(TheoreticalFragmentSeries s1, TheoreticalFragmentSeries s2) {
                        return s1.frag_series.compareToIgnoreCase(s2.frag_series);
                    }
                });

                //convert fragSeries ArrayList to array []
                m_fragSer = new TheoreticalFragmentSeries[fragSerArrayList.size()];//jsonProp.frag_table;
                for (int i = 0; i < fragSerArrayList.size(); i++) {
                    m_fragSer[i] = fragSerArrayList.get(i);
                }

                m_fragMa = jsonProp.frag_matches;
                isEmpty = false;
            }
        }

    }

    public DPeptideMatch getPeptideMatch() {
        return m_peptideMatch;
    }

    public TheoreticalFragmentSeries[] getFragmentSeries() {
        return m_fragSer;
    }

    public FragmentMatch[] getFragmentMatch() {
        return m_fragMa;
    }

    public static class FragmentMatch {

        public String label;
        public Double moz;
        public Double calculated_moz;
        public Float intensity;
        private transient int charge = 0; // the charge taken from the serie (++ means double charged)
        private transient String frag_series = null; // will be intialized when acceced
        private transient int position = -1;

        public int getCharge() {
            if (this.charge == 0) {
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
            return this.charge;
        }

        public int getPosition() {
            if (position < 0) {
                position = Integer.parseInt(label.substring(label.indexOf('(') + 1, label.indexOf(')')));
            }
            return position;
        }

        public String getSeriesName() {
            if (frag_series == null) {
                frag_series = label.substring(0, label.indexOf('(')) + label.substring(label.indexOf(')') + 1);
            }
            return frag_series;
        }

        public int countSeq(char ch) { // counts the number of times a subSeq appears in label
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
        public TheoreticalFragmentSeries[] frag_table;
        public FragmentMatch[] frag_matches;
    }

    public class TheoreticalFragmentSeries {

        public String frag_series;
        public double[] masses;
        private transient int charge = 0; // default to 1 because it is used to multiply
        // the m/z to obtain real mass values for aa
        // calculation

        public int getCharge() {
            if (this.charge == 0) {
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
            return charge;
        }

        public int countSeq(char ch) { // counts the number of times a subSeq appears in label
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
