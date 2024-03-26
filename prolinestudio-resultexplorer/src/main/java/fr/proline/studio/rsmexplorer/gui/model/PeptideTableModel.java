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
package fr.proline.studio.rsmexplorer.gui.model;

import fr.proline.core.orm.msi.Peptide;
import fr.proline.core.orm.msi.PeptideReadablePtmString;
import fr.proline.core.orm.msi.SequenceMatch;
import fr.proline.core.orm.msi.dto.DSpectrum;
import fr.proline.core.orm.msi.dto.DMsQuery;
import fr.proline.core.orm.msi.dto.DPeptideInstance;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.core.orm.msi.dto.DProteinSet;
import fr.proline.studio.corewrapper.util.PeptideClassesUtils;
import fr.proline.studio.extendedtablemodel.ExtraDataType;
import fr.proline.studio.export.ExportFontData;
import fr.proline.studio.export.ExportModelUtilities;
import fr.proline.studio.filter.*;
import fr.proline.studio.graphics.PlotInformation;
import fr.proline.studio.graphics.PlotType;
import fr.proline.studio.table.renderer.DefaultAlignRenderer;
import fr.proline.studio.table.renderer.DoubleRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.FloatRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.MsQueryRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.PeptideRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.ScoreRenderer;
import fr.proline.studio.table.DecoratedTableModel;
import fr.proline.studio.extendedtablemodel.GlobalTableModelInterface;
import fr.proline.studio.table.ExportFontModelUtilities;
import fr.proline.studio.table.LazyData;
import fr.proline.studio.table.TableColumn;
import fr.proline.studio.table.TableDefaultRendererManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.table.TableCellRenderer;

/**
 * Table Model for PeptideInstance (peptides of a ProteinMatch in a Rsm)
 *
 * @author JM235353
 */
public class PeptideTableModel extends DecoratedTableModel implements GlobalTableModelInterface {

  public static final int COLTYPE_PEPTIDE_ID = 0;
  public static final int COLTYPE_PEPTIDE_PREVIOUS_AA = 1;
  public static final int COLTYPE_PEPTIDE_NAME = 2;
  public static final int COLTYPE_PEPTIDE_NEXT_AA = 3;
  public static final int COLTYPE_PEPTIDE_LENGTH = 4;
  public static final int COLTYPE_PEPTIDE_PTM = 5;
  public static final int COLTYPE_PEPTIDE_PSM_SCORE = 6;
  public static final int COLTYPE_PEPTIDE_START = 7;
  public static final int COLTYPE_PEPTIDE_STOP = 8;
  public static final int COLTYPE_PEPTIDE_CALCULATED_MASS = 9;
  public static final int COLTYPE_PEPTIDE_EXPERIMENTAL_MOZ = 10;
  public static final int COLTYPE_PEPTIDE_PPM = 11;
  public static final int COLTYPE_PEPTIDE_CHARGE = 12;
  public static final int COLTYPE_PEPTIDE_MISSED_CLIVAGE = 13;
  public static final int COLTYPE_PEPTIDE_RANK = 14;
  public static final int COLTYPE_PEPTIDE_RETENTION_TIME = 15;
  public static final int COLTYPE_PEPTIDE_PROTEIN_SET_COUNT = 16;
  public static final int COLTYPE_PEPTIDE_PROTEIN_SET_NAMES = 17;
  public static final int COLTYPE_PEPTIDE_ION_PARENT_INTENSITY = 18;
  public static final int COLTYPE_PEPTIDE_MSQUERY = 19;
  public static final int COLTYPE_SPECTRUM_TITLE = 20;
  public static final int COLTYPE_PEPTIDE_SCORE = 21;
  public static final int COLTYPE_PEPTIDE_NONE_AMBIGUOUS_SEQ = 22;


//  private static final TableColumn[] COLUMNS = new TableColumn[COLTYPE_PEPTIDE_SCORE+1];

  private static final TableColumn[] COLUMNS = new TableColumn.Builder()
          .add(COLTYPE_PEPTIDE_ID, "Id", "Peptide Instance Id", Long.class)
          .add(COLTYPE_PEPTIDE_PREVIOUS_AA,"Prev. AA", "Previous Amino Acid", String.class)
          .add(COLTYPE_PEPTIDE_NAME,"Peptide", "Peptide", DPeptideMatch.class)
          .add(COLTYPE_PEPTIDE_NEXT_AA,"Next AA", "Next Amino Acid", String.class)
          .add(COLTYPE_PEPTIDE_LENGTH,"Length", "Length", Integer.class)
          .add(COLTYPE_PEPTIDE_PTM,"PTMs", "Post Translational Modifications",  String.class)
          .add(COLTYPE_PEPTIDE_PSM_SCORE,"PSM Score", "Best PSM Score",  Float.class)
          .add(COLTYPE_PEPTIDE_START,"Start", "Start position",  Integer.class)
          .add(COLTYPE_PEPTIDE_STOP,"Stop", "Stop position", Integer.class)
          .add(COLTYPE_PEPTIDE_CALCULATED_MASS,"Calc. Mass", "Calculated Mass", Double.class)
          .add(COLTYPE_PEPTIDE_EXPERIMENTAL_MOZ,"Exp. MoZ", "Experimental Mass to Charge Ratio", Double.class)
          .add(COLTYPE_PEPTIDE_PPM,"Ppm", "parts-per-million", Float.class)
          .add(COLTYPE_PEPTIDE_CHARGE,"Charge", "Charge",  Integer.class)
          .add(COLTYPE_PEPTIDE_MISSED_CLIVAGE,"Missed Cl.", "Missed Cleavages", Integer.class)
          .add(COLTYPE_PEPTIDE_RANK,"Rank", "Pretty Rank", Integer.class)
          .add(COLTYPE_PEPTIDE_RETENTION_TIME,"RT", "Retention Time (min)",  Float.class)
          .add(COLTYPE_PEPTIDE_PROTEIN_SET_COUNT, "Protein Set Count", "Protein Sets Count", Integer.class)
          .add(COLTYPE_PEPTIDE_PROTEIN_SET_NAMES, "Protein Sets", "List of matching Protein Sets", String.class)
          .add(COLTYPE_PEPTIDE_ION_PARENT_INTENSITY,"Ion Parent Int.", "Ion Parent Intensity", Float.class)
          .add(COLTYPE_PEPTIDE_MSQUERY,"MsQuery", "MsQuery", DMsQuery.class)
          .add(COLTYPE_SPECTRUM_TITLE, "Spectrum Title", "Spectrum Title", String.class)
          .add(COLTYPE_PEPTIDE_SCORE, "Score", "Peptide Score", Float.class)
          .add(COLTYPE_PEPTIDE_NONE_AMBIGUOUS_SEQ, "None Ambiguous Seq", "Peptide Sequence with Ambiguous AA substituted ", String.class).build();

    private DPeptideInstance[] m_peptideInstances = null;
    private ScoreRenderer m_scoreRenderer = new ScoreRenderer();
    private String m_modelName;

    public DPeptideInstance getPeptide(int row) {
        return m_peptideInstances[row];

    }

    public int findRow(long peptideMatchId) {

        if (m_peptideInstances == null) {
            return -1;
        }

        int nb = m_peptideInstances.length;
        for (int i = 0; i < nb; i++) {
            DPeptideInstance peptideInstance = m_peptideInstances[i];
            if (peptideInstance.getBestPeptideMatch().getId() == peptideMatchId) {
                return i;
            }
        }
        return -1;

    }

    @Override
    public int getColumnCount() {
        return COLUMNS.length;
    }

    @Override
    public String getColumnName(int col) {
        return COLUMNS[col].getName();
    }

    @Override
    public String getToolTipForHeader(int col) {
        return COLUMNS[col].getTooltip();
    }

    @Override
    public String getTootlTipValue(int row, int col) {
        return null;
    }

    @Override
    public Class getColumnClass(int col) {
        return COLUMNS[col].getType();
    }

    @Override
    public int getRowCount() {
        if (m_peptideInstances == null) {
            return 0;
        }

        return m_peptideInstances.length;
    }

    @Override
    public Object getValueAt(int row, int col) {

        // Retrieve Peptide Instance
        DPeptideInstance peptideInstance = m_peptideInstances[row];
        DPeptideMatch peptideMatch = (DPeptideMatch) peptideInstance.getBestPeptideMatch();
        SequenceMatch sequenceMatch = peptideMatch.getSequenceMatch();

        switch (col) {
            case COLTYPE_PEPTIDE_ID:
                return peptideInstance.getId();
            case COLTYPE_PEPTIDE_PREVIOUS_AA: {
                if (sequenceMatch == null) {
                    return "";
                }
                Character residueBefore = sequenceMatch.getResidueBefore();
                if (residueBefore != null) {
                    return String.valueOf(Character.toUpperCase(residueBefore));
                } else {
                    return "";
                }
            }
            case COLTYPE_PEPTIDE_NAME: {
                return peptideMatch;
            }
            case COLTYPE_PEPTIDE_NEXT_AA: {
                if (sequenceMatch == null) {
                    return "";
                }
                Character residueAfter = sequenceMatch.getResidueAfter();
                if (residueAfter != null) {
                    return String.valueOf(Character.toUpperCase(residueAfter));
                } else {
                    return "";
                }
            }
            case COLTYPE_PEPTIDE_LENGTH: {
                if (peptideMatch == null) {
                    return null;
                } else {
                    return peptideMatch.getPeptide().getSequence().length();
                }
            }
            case COLTYPE_PEPTIDE_PSM_SCORE: {
                if (peptideMatch == null) {
                    return null; // should never happen   
                }
                return peptideMatch.getScore();
            }
            case COLTYPE_PEPTIDE_RANK: {
                if (peptideMatch == null) {
                    return null; // should never happen   
                }
                return (peptideMatch.getCDPrettyRank() == null) ? null : peptideMatch.getCDPrettyRank();
            }
            case COLTYPE_PEPTIDE_PROTEIN_SET_COUNT: {
                if (peptideMatch == null) {
                    return null;
                }
                Peptide p = peptideMatch.getPeptide();
                if (p == null) {
                    return null;
                }
                HashSet<DProteinSet> proteinSetList = new HashSet(p.getTransientData().getProteinSetArray());
                if (proteinSetList == null) {
                    return null;
                }

                return proteinSetList.size();
            }
            case COLTYPE_PEPTIDE_PROTEIN_SET_NAMES: {
                if (peptideMatch == null) {
                    return ""; // should never happen   
                }
                Peptide p = peptideMatch.getPeptide();
                if (p == null || p.getTransientData().getProteinSetArray() == null) {
                    return "";
                }
                HashSet<DProteinSet> proteinSetList = new HashSet(p.getTransientData().getProteinSetArray());
                if (proteinSetList.isEmpty()) {
                    return "";
                }

                StringBuilder display = new StringBuilder();
                int nbProteinGroups = proteinSetList.size();
                display.append(nbProteinGroups);
                display.append(" (");
                final int startSize = display.length();
                proteinSetList.forEach(ps -> {
                    if (display.length() > startSize) {
                        display.append(',');
                    }
                    display.append(ps.getTypicalProteinMatch().getAccession());
                });

                display.append(')');

                return display.toString();

            }
            case COLTYPE_PEPTIDE_CHARGE: {
                if (peptideMatch == null) {
                    return ""; // should never happen   
                }
                return Integer.valueOf(peptideMatch.getCharge());
            }
            case COLTYPE_PEPTIDE_EXPERIMENTAL_MOZ: {
                if (peptideMatch == null) {
                    return null; // should never happen   
                }
                return Double.valueOf(peptideMatch.getExperimentalMoz());
            }
            case COLTYPE_PEPTIDE_PPM: {
                if (peptideMatch == null) {
                    return null; // should never happen   
                }
                float ppm = PeptideClassesUtils.getPPMFor(peptideMatch, peptideMatch.getPeptide());
                return Float.valueOf(ppm);
            }
            case COLTYPE_PEPTIDE_START: {
                if (sequenceMatch == null) {
                    return null;
                }
                int start = sequenceMatch.getId().getStart();
                return Integer.valueOf(start);
            }
            case COLTYPE_PEPTIDE_STOP: {
                if (sequenceMatch == null) {
                    return null;
                }
                int stop = sequenceMatch.getId().getStop();
                return Integer.valueOf(stop);
            }
            case COLTYPE_PEPTIDE_CALCULATED_MASS: {
                if (peptideMatch != null) {
                    Peptide p = peptideMatch.getPeptide();
                    if (p != null) {
                        return p.getCalculatedMass();
                    }
                }
                return null;
            }
            case COLTYPE_PEPTIDE_MISSED_CLIVAGE: {
                if (peptideMatch == null) {
                    return null;
                }
                return Integer.valueOf(peptideMatch.getMissedCleavage());
            }
            case COLTYPE_PEPTIDE_RETENTION_TIME: {
                if (peptideMatch == null) {
                    return null;
                }
                return peptideMatch.getRetentionTime();
            }
            case COLTYPE_PEPTIDE_MSQUERY: {
                if (peptideMatch == null) {
                    return null;
                }
                DMsQuery msQuery = peptideMatch.getMsQuery();
                return msQuery;
            }
            case COLTYPE_SPECTRUM_TITLE: {
                if (peptideMatch == null) {
                    return null;
                }
                DMsQuery msQuery = peptideMatch.getMsQuery();
                if (msQuery == null) {
                    return null;
                }
                DSpectrum s = msQuery.getDSpectrum();
                if (s == null) {
                    return null;
                }
                return s.getTitle();
            }
            case COLTYPE_PEPTIDE_ION_PARENT_INTENSITY: {
                if (peptideMatch != null) {
                    DMsQuery msQuery = peptideMatch.getMsQuery();
                    if (msQuery != null) {
                        Float precursorIntensity = msQuery.getPrecursorIntensity();
                        return precursorIntensity;

                    }
                }
                return null;
            }
            case COLTYPE_PEPTIDE_PTM: {
                if (peptideMatch == null) {
                    return "";
                }

                Peptide p = peptideMatch.getPeptide();
                if (p == null) {
                    return "";
                }

                boolean ptmStringLoaded = p.getTransientData().isPeptideReadablePtmStringLoaded();
                if (!ptmStringLoaded) {
                    return "";
                }

                String ptm = "";
                PeptideReadablePtmString ptmString = p.getTransientData().getPeptideReadablePtmString();
                if (ptmString != null) {
                    ptm = ptmString.getReadablePtmString();
                }

                return ptm;

            }

            case COLTYPE_PEPTIDE_SCORE: {
              Map<String, Object> properties = peptideInstance.getProperties();
              if ((properties == null) || !properties.containsKey("score")){
                return null;
              }
              Double score = (Double)((Map<String, Object>)properties.get("score")).get("score");
              return score.floatValue();
            }

          case COLTYPE_PEPTIDE_NONE_AMBIGUOUS_SEQ: {
            if (peptideMatch == null) {
              return "";
            }

            return peptideMatch.getDisambiguatedSeq();

          }
        }
        return null; // should never happen
    }

    public void setData(DPeptideInstance[] peptideInstances) {
        m_peptideInstances = peptideInstances;
        fireTableDataChanged();
    }

    public DPeptideInstance[] getPeptideInstances() {
        return m_peptideInstances;
    }

    @Override
    public void addFilters(LinkedHashMap<Integer, Filter> filtersMap) {

        filtersMap.put(COLTYPE_PEPTIDE_PREVIOUS_AA, new StringDiffFilter(getColumnName(COLTYPE_PEPTIDE_PREVIOUS_AA), null, COLTYPE_PEPTIDE_PREVIOUS_AA));

        ConvertValueInterface peptideConverter = new ConvertValueInterface() {
            @Override
            public Object convertValue(Object o) {
                if (o == null) {
                    return null;
                }
                return ((DPeptideMatch) o).getPeptide().getSequence();
            }

        };
        filtersMap.put(COLTYPE_PEPTIDE_NAME, new StringDiffFilter(getColumnName(COLTYPE_PEPTIDE_NAME), peptideConverter, COLTYPE_PEPTIDE_NAME));
        filtersMap.put(COLTYPE_PEPTIDE_NONE_AMBIGUOUS_SEQ, new StringDiffFilter(getColumnName(COLTYPE_PEPTIDE_NONE_AMBIGUOUS_SEQ), null, COLTYPE_PEPTIDE_NONE_AMBIGUOUS_SEQ));

        filtersMap.put(COLTYPE_PEPTIDE_NEXT_AA, new StringDiffFilter(getColumnName(COLTYPE_PEPTIDE_NEXT_AA), null, COLTYPE_PEPTIDE_NEXT_AA));
        filtersMap.put(COLTYPE_PEPTIDE_LENGTH, new IntegerFilter(getColumnName(COLTYPE_PEPTIDE_LENGTH), null, COLTYPE_PEPTIDE_LENGTH));
        filtersMap.put(COLTYPE_PEPTIDE_PSM_SCORE, new DoubleFilter(getColumnName(COLTYPE_PEPTIDE_PSM_SCORE), null, COLTYPE_PEPTIDE_PSM_SCORE));
        filtersMap.put(COLTYPE_PEPTIDE_RANK, new IntegerFilter(getColumnName(COLTYPE_PEPTIDE_RANK), null, COLTYPE_PEPTIDE_RANK));
        filtersMap.put(COLTYPE_PEPTIDE_PROTEIN_SET_COUNT, new IntegerFilter(getColumnName(COLTYPE_PEPTIDE_PROTEIN_SET_COUNT), null, COLTYPE_PEPTIDE_PROTEIN_SET_COUNT));
        filtersMap.put(COLTYPE_PEPTIDE_PROTEIN_SET_NAMES, new StringDiffFilter(getColumnName(COLTYPE_PEPTIDE_PROTEIN_SET_NAMES), null, COLTYPE_PEPTIDE_PROTEIN_SET_NAMES));
        filtersMap.put(COLTYPE_PEPTIDE_START, new IntegerFilter(getColumnName(COLTYPE_PEPTIDE_START), null, COLTYPE_PEPTIDE_START));
        filtersMap.put(COLTYPE_PEPTIDE_STOP, new IntegerFilter(getColumnName(COLTYPE_PEPTIDE_STOP), null, COLTYPE_PEPTIDE_STOP));
        filtersMap.put(COLTYPE_PEPTIDE_CALCULATED_MASS, new DoubleFilter(getColumnName(COLTYPE_PEPTIDE_CALCULATED_MASS), null, COLTYPE_PEPTIDE_CALCULATED_MASS));
        filtersMap.put(COLTYPE_PEPTIDE_EXPERIMENTAL_MOZ, new DoubleFilter(getColumnName(COLTYPE_PEPTIDE_EXPERIMENTAL_MOZ), null, COLTYPE_PEPTIDE_EXPERIMENTAL_MOZ));
        filtersMap.put(COLTYPE_PEPTIDE_PPM, new DoubleFilter(getColumnName(COLTYPE_PEPTIDE_PPM), null, COLTYPE_PEPTIDE_PPM));
        filtersMap.put(COLTYPE_PEPTIDE_CHARGE, new IntegerFilter(getColumnName(COLTYPE_PEPTIDE_CHARGE), null, COLTYPE_PEPTIDE_CHARGE));
        filtersMap.put(COLTYPE_PEPTIDE_MISSED_CLIVAGE, new IntegerFilter(getColumnName(COLTYPE_PEPTIDE_MISSED_CLIVAGE), null, COLTYPE_PEPTIDE_MISSED_CLIVAGE));
        filtersMap.put(COLTYPE_PEPTIDE_RETENTION_TIME, new DoubleFilter(getColumnName(COLTYPE_PEPTIDE_RETENTION_TIME), null, COLTYPE_PEPTIDE_RETENTION_TIME));

        ConvertValueInterface msQueryConverter = new ConvertValueInterface() {
            @Override
            public Object convertValue(Object o) {
                if (o == null) {
                    return null;
                }
                return ((DMsQuery) o).getInitialId();
            }

        };
        filtersMap.put(COLTYPE_PEPTIDE_MSQUERY, new IntegerFilter(getColumnName(COLTYPE_PEPTIDE_MSQUERY), msQueryConverter, COLTYPE_PEPTIDE_MSQUERY));

        filtersMap.put(COLTYPE_SPECTRUM_TITLE, new StringDiffFilter(getColumnName(COLTYPE_SPECTRUM_TITLE), null, COLTYPE_SPECTRUM_TITLE));
        filtersMap.put(COLTYPE_PEPTIDE_ION_PARENT_INTENSITY, new DoubleFilter(getColumnName(COLTYPE_PEPTIDE_ION_PARENT_INTENSITY), null, COLTYPE_PEPTIDE_ION_PARENT_INTENSITY));
        filtersMap.put(COLTYPE_PEPTIDE_PTM, new StringDiffFilter(getColumnName(COLTYPE_PEPTIDE_PTM), null, COLTYPE_PEPTIDE_PTM));
    }

    @Override
    public int getLoadingPercentage() {
        return 100;
    }

    @Override
    public boolean isLoaded() {
        return true;
    }

    @Override
    public String getDataColumnIdentifier(int columnIndex) {
        return getColumnName(columnIndex);
    }

    @Override
    public Class getDataColumnClass(int columnIndex) {
        if (columnIndex == COLTYPE_PEPTIDE_NAME) {
            return String.class;
        }
        return getColumnClass(columnIndex);
    }

    @Override
    public Object getDataValueAt(int rowIndex, int columnIndex) {
        if (columnIndex == COLTYPE_PEPTIDE_NAME) {
            return ((DPeptideMatch) getValueAt(rowIndex, columnIndex)).getPeptide().getSequence();
        }
        return getValueAt(rowIndex, columnIndex);
    }

    @Override
    public int[] getKeysColumn() {
        int[] keys = {COLTYPE_PEPTIDE_NAME, COLTYPE_PEPTIDE_ID};
        return keys;
    }

    @Override
    public void setName(String name) {
        m_modelName = name;
    }

    @Override
    public String getName() {
        return m_modelName;
    }

    @Override
    public int getInfoColumn() {
        return COLTYPE_PEPTIDE_NAME;
    }

    @Override
    public Map<String, Object> getExternalData() {
        return null;
    }

    @Override
    public PlotInformation getPlotInformation() {
        return null;
    }

    @Override
    public Long getTaskId() {
        return -1L; // not used
    }

    @Override
    public LazyData getLazyData(int row, int col) {
        return null; // not used
    }

    @Override
    public void givePriorityTo(Long taskId, int row, int col) {
        // not used
    }

    @Override
    public void sortingChanged(int col) {
        return; // not used
    }

    @Override
    public int getSubTaskId(int col) {
        return -1; // not used
    }

    @Override
    public PlotType getBestPlotType() {
        return null;
    }

    @Override
    public int[] getBestColIndex(PlotType plotType) {
        return null;
    }

    @Override
    public String getExportRowCell(int row, int col) {
        return ExportModelUtilities.getExportRowCell(this, row, col);
    }

    @Override
    public ArrayList<ExportFontData> getExportFonts(int row, int col) {
        if (col == COLTYPE_PEPTIDE_NAME) {
            return ExportFontModelUtilities.getExportFonts(m_peptideInstances[row]);
        }
        return null;
    }

    @Override
    public String getExportColumnName(int col) {
        return getColumnName(col);
    }

    @Override
    public TableCellRenderer getRenderer(int row, int col) {

        if (m_rendererMap.containsKey(col)) {
            return m_rendererMap.get(col);
        }

        TableCellRenderer renderer = null;
        switch (col) {
            case COLTYPE_PEPTIDE_PREVIOUS_AA:
            case COLTYPE_PEPTIDE_NEXT_AA:
            case COLTYPE_PEPTIDE_PROTEIN_SET_NAMES:
            case COLTYPE_PEPTIDE_PTM: {
                renderer = new DefaultAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class), JLabel.LEFT);
                break;
            }
            case COLTYPE_PEPTIDE_NAME: {
                renderer = new PeptideRenderer();
                break;
            }
            case COLTYPE_PEPTIDE_SCORE:
            case COLTYPE_PEPTIDE_PSM_SCORE: {
                renderer = m_scoreRenderer;
                break;
            }
            case COLTYPE_PEPTIDE_PPM: {
                renderer = new FloatRenderer(new DefaultAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class), JLabel.RIGHT));
                break;
            }
            case COLTYPE_PEPTIDE_RETENTION_TIME: {
                renderer = new FloatRenderer(new DefaultAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class), JLabel.RIGHT), 4);
                break;
            }
            case COLTYPE_PEPTIDE_START:
            case COLTYPE_PEPTIDE_STOP:
            case COLTYPE_PEPTIDE_CHARGE:
            case COLTYPE_PEPTIDE_MISSED_CLIVAGE:
            case COLTYPE_PEPTIDE_ION_PARENT_INTENSITY:
            case COLTYPE_PEPTIDE_PROTEIN_SET_COUNT: {
                renderer = new DefaultAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(Integer.class), JLabel.RIGHT);
                break;
            }
            case COLTYPE_PEPTIDE_CALCULATED_MASS:
            case COLTYPE_PEPTIDE_EXPERIMENTAL_MOZ: {
                renderer = new DoubleRenderer(new DefaultAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class), JLabel.RIGHT));
                break;
            }
            case COLTYPE_PEPTIDE_MSQUERY: {
                renderer = new MsQueryRenderer();
                break;
            }

        }
        m_rendererMap.put(col, renderer);
        return renderer;

    }
    private final HashMap<Integer, TableCellRenderer> m_rendererMap = new HashMap();

    @Override
    public GlobalTableModelInterface getFrozzenModel() {
        return this;
    }

    @Override
    public ArrayList<ExtraDataType> getExtraDataTypes() {
        ArrayList<ExtraDataType> list = new ArrayList<>();
        list.add(new ExtraDataType(DPeptideInstance.class, true));
        registerSingleValuesAsExtraTypes(list);
        return list;
    }

    @Override
    public Object getValue(Class c) {
        return getSingleValue(c);
    }

    @Override
    public Object getRowValue(Class c, int row) {
        if (c.equals(DPeptideInstance.class)) {
            return m_peptideInstances[row];
        }
        return null;
    }

    @Override
    public Object getColValue(Class c, int col) {
        return null;
    }

}
