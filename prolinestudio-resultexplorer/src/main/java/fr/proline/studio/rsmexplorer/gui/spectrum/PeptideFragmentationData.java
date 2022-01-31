/* 
 * Copyright (C) 2019
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
                    xyzReferenceSeriesName = m_fragSer.get(i).frag_series;
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
    private int charge = 0; // default 0 is used as a marker, will be to 1 because it is used to multiply the m/z to obtain real mass values for aa calculation
    private boolean nl = false;
    
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

  public static void main(String[] args) {
    String jsonProperties = "{\"frag_table\":[{\"frag_series\":\"z\",\"nl\":true,\"masses\":[0.0,4438.522957925802,4367.485844115802,4211.384733015802,4110.337054515802,3982.2420914158015,3854.1471283158016,3753.099449815802,3640.015385815802,3538.967707315802,3451.935678875802,3323.8407157758015,3195.7457526758017,3108.7137242358017,2966.6031111358016,2838.5081480358017,2767.4710342258018,2680.4390057858013,2552.3440426858015,2495.3225789458015,2408.2905505058015,2293.2636074058014,2222.2264935958015,2151.1893797858015,2064.1573513458015,2007.1358876058014,1908.0674736558015,1779.9725105558016,1678.9248320558015,1607.8877182458016,1479.8291407458016,1323.7280296458018,1167.626918545802,981.547605545802,825.4464944458022,728.3937305658022,671.3722668258023,570.3245883258022,471.25617437580223,400.21906056580184,287.13499656580217,131.0338854658024]},{\"frag_series\":\"z+2\",\"masses\":[0.0,4440.538607989942,4369.501494179942,4213.400383079942,4112.352704579942,3984.2577414799416,3856.1627783799418,3755.115099879942,3642.031035879942,3540.983357379942,3453.951328939942,3325.8563658399416,3197.761402739942,3110.729374299942,2968.6187611999417,2840.523798099942,2769.486684289942,2682.4546558499414,2554.3596927499416,2497.3382290099416,2410.3062005699417,2295.2792574699415,2224.2421436599416,2153.2050298499416,2066.1730014099417,2009.1515376699413,1910.0831237199413,1781.9881606199415,1680.9404821199414,1609.9033683099415,1481.8447908099415,1325.7436797099417,1169.642568609942,983.563255609942,827.4621445099422,730.4093806299422,673.3879168899423,572.3402383899422,473.27182443994224,402.23471062994184,289.1506466299422,133.0495355299424]},{\"frag_series\":\"z+1\",\"masses\":[0.0,4439.530782957872,4368.493669147872,4212.392558047873,4111.344879547873,3983.2499164478713,3855.1549533478715,3754.107274847872,3641.0232108478717,3539.9755323478716,3452.9435039078717,3324.8485408078714,3196.7535777078715,3109.7215492678715,2967.6109361678714,2839.5159730678715,2768.4788592578716,2681.446830817871,2553.3518677178713,2496.3304039778714,2409.2983755378714,2294.271432437871,2223.2343186278713,2152.1972048178714,2065.1651763778714,2008.1437126378714,1909.0752986878715,1780.9803355878717,1679.9326570878716,1608.8955432778716,1480.8369657778717,1324.7358546778719,1168.634743577872,982.5554305778719,826.4543194778721,729.4015555978722,672.3800918578722,571.3324133578722,472.26399940787223,401.22688559787184,288.14282159787217,132.0417104978724]},{\"frag_series\":\"y\",\"masses\":[0.0,4455.549507026812,4384.512393216812,4228.411282116812,4127.363603616812,3999.2686405168115,3871.1736774168116,3770.125998916812,3657.041934916812,3555.994256416812,3468.962227976812,3340.8672648768115,3212.7723017768117,3125.7402733368117,2983.6296602368116,2855.5346971368117,2784.4975833268118,2697.4655548868113,2569.3705917868115,2512.3491280468115,2425.3170996068116,2310.2901565068114,2239.2530426968115,2168.2159288868115,2081.1839004468115,2024.1624367068114,1925.0940227568115,1796.9990596568116,1695.9513811568115,1624.9142673468116,1496.8556898468116,1340.7545787468118,1184.653467646812,998.574154646812,842.4730435468122,745.4202796668122,688.3988159268123,587.3511374268122,488.28272347681224,417.24560966681184,304.1615456668122,148.0604345668124]},{\"frag_series\":\"y*\",\"masses\":[0.0,4438.522957925802,4367.485844115802,4211.384733015802,4110.337054515802,3982.2420914158015,3854.1471283158016,3753.099449815802,3640.015385815802,3538.967707315802,3451.935678875802,3323.8407157758015,3195.7457526758017,3108.7137242358017,2966.6031111358016,2838.5081480358017,2767.4710342258018,2680.4390057858013,2552.3440426858015,2495.3225789458015,2408.2905505058015,2293.2636074058014,2222.2264935958015,2151.1893797858015,2064.1573513458015,2007.1358876058014,1908.0674736558015,1779.9725105558016,1678.9248320558015,1607.8877182458016,1479.8291407458016,1323.7280296458018,1167.626918545802,981.547605545802,825.4464944458022,728.3937305658022,671.3722668258023,570.3245883258022,471.25617437580223,400.21906056580184,287.13499656580217,0.0]},{\"frag_series\":\"y0\",\"masses\":[0.0,4437.538942343112,4366.501828533112,4210.400717433112,4109.353038933112,3981.2580758331114,3853.1631127331116,3752.115434233112,3639.031370233112,3537.9836917331118,3450.951663293112,3322.8567001931115,3194.7617370931116,3107.7297086531116,2965.6190955531115,2837.5241324531116,2766.4870186431117,2679.4549902031113,2551.3600271031114,2494.3385633631115,2407.3065349231115,2292.2795918231113,2221.2424780131114,2150.2053642031115,2063.1733357631115,2006.1518720231113,1907.0834580731114,1778.9884949731115,1677.9408164731115,1606.9037026631115,1478.8451251631116,1322.7440140631118,1166.642902963112,980.563589963112,824.4624788631122,727.4097149831123,670.3882512431123,569.3405727431123,470.2721587931122,399.23504498311183,286.15098098311216,130.0498698831124]},{\"frag_series\":\"c\",\"masses\":[119.08150406782201,190.118617877822,346.219728977822,447.267407477822,575.362370577822,703.457333677822,804.505012177822,917.589076177822,1018.636754677822,1105.668783117822,1233.763746217822,1361.858709317822,1448.890737757822,1591.0013508578222,1719.0963139578223,1790.1334277678222,1877.1654562078222,2005.2604193078223,2062.2818830478222,2149.313911487822,2264.3408545878224,2335.3779683978223,2406.4150822078223,2493.4471106478222,2550.468574387822,2649.536988337822,2777.631951437822,2878.679629937822,2949.716743747822,3077.775321247822,3233.8764323478217,3389.9775434478215,3576.0568564478217,3732.1579675478215,3829.2107314278214,3886.2321951678214,3987.2798736678214,4086.3482876178214,4157.385401427821,4270.469465427821,4426.570576527821,0.0]}],\"frag_matches\":[{\"label\":\"c(3)\",\"moz\":346.21968,\"calculated_moz\":346.219728977822,\"intensity\":7689.0},{\"label\":\"c(4)\",\"moz\":447.26698,\"calculated_moz\":447.267407477822,\"intensity\":22360.0},{\"label\":\"c(5)\",\"moz\":575.36228,\"calculated_moz\":575.362370577822,\"intensity\":6927.0},{\"label\":\"c(6)\",\"moz\":703.45808,\"calculated_moz\":703.457333677822,\"intensity\":15370.0},{\"label\":\"c(7)\",\"moz\":804.50608,\"calculated_moz\":804.505012177822,\"intensity\":10580.0},{\"label\":\"c(8)\",\"moz\":917.59148,\"calculated_moz\":917.589076177822,\"intensity\":15210.0},{\"label\":\"c(9)\",\"moz\":1018.63718,\"calculated_moz\":1018.636754677822,\"intensity\":30910.0},{\"label\":\"c(10)\",\"moz\":1105.66988,\"calculated_moz\":1105.668783117822,\"intensity\":27410.0},{\"label\":\"c(11)\",\"moz\":1233.76668,\"calculated_moz\":1233.763746217822,\"intensity\":12080.0},{\"label\":\"c(12)\",\"moz\":1361.85958,\"calculated_moz\":1361.858709317822,\"intensity\":17070.0}]}";
    JsonParser parser = new JsonParser();
    Gson gson = new Gson();

    JsonObject array = parser.parse(jsonProperties).getAsJsonObject();
    FragmentationJsonProperties jsonProp = gson.fromJson(array, FragmentationJsonProperties.class);
    TheoreticalFragmentSeries[] series = jsonProp.frag_table;
    System.out.println("done");
  }
}
