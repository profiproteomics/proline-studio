package fr.proline.studio.rsmexplorer.gui.spectrum;

import java.util.*;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import fr.proline.core.orm.msi.ObjectTree;
import fr.proline.core.orm.msi.dto.DPeptideMatch;

/**
 * @author JM235353
 */
public class PeptideFragmentationData {

  private DPeptideMatch m_peptideMatch;
  private List<TheoreticalFragmentSeries> m_fragSer;
  private FragmentMatch[] m_fragMatches;
  private boolean isEmpty = true;
  private String abcReferenceSeriesName = "";
  private String xyzReferenceSeriesName = "";
  private String neutralLossStatus = null;

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
          //System.out.println("no fragment match");
          jsonProp.frag_matches = new FragmentMatch[0];
        }

        // sort theoretical frag table according to serie's label for proper display in frag table
        ArrayList<TheoreticalFragmentSeries> fragSerArrayList = new ArrayList<TheoreticalFragmentSeries>();
        if (jsonProp.frag_table != null) {
          for (TheoreticalFragmentSeries frag : jsonProp.frag_table) {
            fragSerArrayList.add(frag);
          }
        }
        //Sort series by name
        Collections.sort(fragSerArrayList, new Comparator<TheoreticalFragmentSeries>() {
          public int compare(TheoreticalFragmentSeries s1, TheoreticalFragmentSeries s2) {
            return s1.frag_series.compareToIgnoreCase(s2.frag_series);
          }
        });

        m_fragSer = fragSerArrayList;

        for (int i = 0; i < m_fragSer.size(); i++) {

          switch (m_fragSer.get(i).frag_series.charAt(0)) {

            case 'a': // either a,b or c do:
            case 'b':
            case 'c':
              if (m_fragSer.get(i).frag_series.length() == 1) { // it's a 'a/b/c' ion
                if (!abcReferenceSeriesName.equals("b")) {// only if b not already defined, else we keep b
                  abcReferenceSeriesName = m_fragSer.get(i).frag_series;
                }
              }
              break;
            case 'v':
            case 'w':
            case 'x':
            case 'y':
              if (m_fragSer.get(i).frag_series.length() == 1) { // it's a 'x/y/z' ion
                if (!xyzReferenceSeriesName.equals("y")) {// only if b not already defined, else we keep b
                  xyzReferenceSeriesName = m_fragSer.get(i).frag_series;
                }
              }
              break;
            case 'z':
              if (m_fragSer.get(i).frag_series.length() == 3) {
                if (m_fragSer.get(i).frag_series.equals("z+1")) {
                  if (!xyzReferenceSeriesName.equals("z")) {// only if y not already defined, else we keep b
                    xyzReferenceSeriesName = "(z+1)";
                  }
                }
              }
              break;
            default:
              break;
          }
        }

        m_fragMatches = jsonProp.frag_matches;

        //try to guess neutral loss of series by searching within matches

        FragmentMatch maxNLMatch = Arrays.stream(m_fragMatches).filter(m -> (m.neutral_loss_mass != null) && (m.neutral_loss_mass > 0))
                .filter(m -> (getTheoreticalFragmentSeries(m.getSeriesName()) != null) && (getTheoreticalFragmentSeries(m.getSeriesName()).isMatching(m)))
                .max(Comparator.comparing(FragmentMatch::getNeutralLoss)).orElse(null);

        if (maxNLMatch != null)
          neutralLossStatus = new StringBuilder("-").append(maxNLMatch.getNeutralLoss()).append(" (shown in table)").toString();
        isEmpty = false;
      }
    }
  }

  public String getNeutralLossStatus() {
    return neutralLossStatus;
  }

  public DPeptideMatch getPeptideMatch() {
    return m_peptideMatch;
  }

  public List<TheoreticalFragmentSeries> getTheoreticalFragmentSeries() {
    return m_fragSer;
  }

  public TheoreticalFragmentSeries getTheoreticalFragmentSeries(String name) {
    return m_fragSer.stream().filter(s -> s.frag_series.equals(name)).findFirst().orElse(null);
  }

  public int getTheoreticalFragmentSeriesIndex(String name) {
    for (int i = 0; i < m_fragSer.size(); i++) {
      if (m_fragSer.get(i).frag_series.equals(name))
          return i;
    }
    return -1;
  }


  public FragmentMatch[] getFragmentMatches() {
    return m_fragMatches;
  }

  public String getABCReferenceSeriesName() {
    return abcReferenceSeriesName;
  }

  public String getXYZReferenceSeriesName() {
    return xyzReferenceSeriesName;
  }

  public boolean isEmpty() {
    return isEmpty;
  }


  public static class FragmentMatch {

    public String label;
    public Double moz;
    public Double calculated_moz;
    public Float intensity;
    public Float neutral_loss_mass;

    private transient int charge = 0; // the charge taken from the serie (++ means double charged)
    private transient String frag_series = null; // will be initialized when accessed
    private transient int position = -1;

    public int getCharge() {
      if ((this.charge == 0) && (label != null)) {
        this.charge = countCharOccurences(label, '+');
      }
      if (this.charge == 0) {
        this.charge = 1;
      }

      return this.charge;
    }

    public Float getNeutralLoss() {
      return neutral_loss_mass;
    }

    public boolean isABCSerie() {
      char s = label.charAt(0);
      return (s == 'a') || (s == 'b') || (s == 'c');
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
    private transient int charge = 0; // default to 1 because it is used to multiply the m/z to obtain real mass values for aa calculation

    public int getCharge() {
      if ((this.charge == 0) && (frag_series != null)) {
        this.charge = countCharOccurences(frag_series, '+');
      }
      if (this.charge == 0) {
        this.charge = 1;
      }
      return charge;
    }


    public boolean isMatching(FragmentMatch match) {
      if ((match.getCharge() == getCharge()) && match.getSeriesName().equals(frag_series)) {
          int index = match.isABCSerie() ? match.getPosition()-1 : masses.length - match.getPosition();
          return ( Math.abs(match.calculated_moz - masses[index]) < 0.1);
      }
      return false;
    }
  }

  private static int countCharOccurences(String seriesName, char ch) { // counts the number of times a subSeq appears in label
    // serves to count the number of * or 0 in order to compare more easily
    int occurence = 0;
    if (seriesName != null) {
      for (int i = 0; i < seriesName.length(); i++) {
        if (seriesName.charAt(i) == ch) {
          occurence++;
        }
      }
    }
    return occurence;
  }
}
