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
import fr.proline.studio.table.ExportModelUtilities;
import fr.proline.studio.export.ExportFontData;
import fr.proline.studio.filter.*;
import fr.proline.studio.graphics.PlotInformation;
import fr.proline.studio.graphics.PlotType;
import fr.proline.studio.table.renderer.DefaultLeftAlignRenderer;
import fr.proline.studio.table.renderer.DefaultRightAlignRenderer;
import fr.proline.studio.table.renderer.DoubleRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.FloatRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.MsQueryRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.PeptideRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.ScoreRenderer;
import fr.proline.studio.table.DecoratedTableModel;
import fr.proline.studio.extendedtablemodel.GlobalTableModelInterface;
import fr.proline.studio.table.LazyData;
import fr.proline.studio.table.TableDefaultRendererManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.table.TableCellRenderer;


/**
 * Table Model for PeptideInstance (peptides of a ProteinMatch in a Rsm)
 * @author JM235353
 */
public class PeptideTableModel extends DecoratedTableModel implements GlobalTableModelInterface {

    public static final int COLTYPE_PEPTIDE_ID = 0;
    public static final int COLTYPE_PEPTIDE_PREVIOUS_AA = 1;
    public static final int COLTYPE_PEPTIDE_NAME = 2;
    public static final int COLTYPE_PEPTIDE_NEXT_AA = 3;
    public static final int COLTYPE_PEPTIDE_PTM = 4;
    public static final int COLTYPE_PEPTIDE_SCORE = 5;
    public static final int COLTYPE_PEPTIDE_START = 6;
    public static final int COLTYPE_PEPTIDE_STOP = 7;
    public static final int COLTYPE_PEPTIDE_CALCULATED_MASS = 8;
    public static final int COLTYPE_PEPTIDE_EXPERIMENTAL_MOZ = 9;
    public static final int COLTYPE_PEPTIDE_PPM = 10;
    public static final int COLTYPE_PEPTIDE_CHARGE = 11;
    public static final int COLTYPE_PEPTIDE_MISSED_CLIVAGE = 12;
    public static final int COLTYPE_PEPTIDE_RANK = 13;
    public static final int COLTYPE_PEPTIDE_RETENTION_TIME = 14;
    public static final int COLTYPE_PEPTIDE_PROTEIN_SET_COUNT = 15;
    public static final int COLTYPE_PEPTIDE_PROTEIN_SET_NAMES = 16;
    public static final int COLTYPE_PEPTIDE_ION_PARENT_INTENSITY = 17;
    public static final int COLTYPE_PEPTIDE_MSQUERY = 18;
    public static final int COLTYPE_SPECTRUM_TITLE = 19;
    private static final String[] m_columnNames = {"Id", "Prev. AA", "Peptide", "Next AA", "PTM", "Score", "Start", "Stop", "Calc. Mass", "Exp. MoZ", "Ppm", "Charge", "Missed Cl.", "Rank", "RT", "Protein Set Count", "Protein Sets", "Ion Parent Int.", "MsQuery", "Spectrum Title"};
    private static final String[] m_columnTooltips = {"Peptide Inst. Id", "Previous Amino Acid","Peptide", "Next Amino Acid", "Post Translational Modifications", "Score", "Start", "Stop", "Calculated Mass", "Experimental Mass to Charge Ration", "parts-per-million", "Charge", "Missed Clivage", "Pretty Rank", "Retention Time (min)", "Protein Set Count", "Protein Sets", "Ion Parent Intensity", "MsQuery", "Spectrum Title"};
    
    
    private DPeptideInstance[] m_peptideInstances = null;

    private ScoreRenderer m_scoreRenderer = new ScoreRenderer();

    private String m_modelName;
    
    public DPeptideInstance getPeptide(int row) {
        return m_peptideInstances[row];

    }
    
    public int findRow(long peptideMatchId) {

        if (m_peptideInstances ==null) {
            return -1;
        }
        
        int nb = m_peptideInstances.length;
        for (int i=0;i<nb;i++) {
            DPeptideInstance peptideInstance = m_peptideInstances[i];
            if (peptideInstance.getBestPeptideMatch().getId() == peptideMatchId) {
                return i;
            }
        }
        return -1;
        
    }
    
    @Override
    public int getColumnCount() {
        return m_columnNames.length;
    }

    @Override
    public String getColumnName(int col) {
        return m_columnNames[col];
    }

    @Override
    public String getToolTipForHeader(int col) {
        return m_columnTooltips[col];
    }
    
    @Override
    public String getTootlTipValue(int row, int col) {
        return null;
    }
    
    
    
    @Override
    public Class getColumnClass(int col) {
        switch (col){
            case COLTYPE_PEPTIDE_ID:
                return Long.class;
            case COLTYPE_PEPTIDE_PREVIOUS_AA:
                return String.class;
            case COLTYPE_PEPTIDE_NAME:
                return DPeptideMatch.class;
            case COLTYPE_PEPTIDE_NEXT_AA:
                return String.class;
            case COLTYPE_PEPTIDE_PROTEIN_SET_NAMES:
            case COLTYPE_PEPTIDE_PTM:
            case COLTYPE_SPECTRUM_TITLE:
                return String.class;
            case COLTYPE_PEPTIDE_SCORE:
            case COLTYPE_PEPTIDE_RETENTION_TIME:
            case COLTYPE_PEPTIDE_ION_PARENT_INTENSITY:
            case COLTYPE_PEPTIDE_PPM:
                return Float.class;
            case COLTYPE_PEPTIDE_EXPERIMENTAL_MOZ:
            case COLTYPE_PEPTIDE_CALCULATED_MASS:
                return Double.class;
            case COLTYPE_PEPTIDE_CHARGE:
            case COLTYPE_PEPTIDE_MISSED_CLIVAGE:
            case COLTYPE_PEPTIDE_START:
            case COLTYPE_PEPTIDE_STOP:    
            case COLTYPE_PEPTIDE_RANK:
            case COLTYPE_PEPTIDE_PROTEIN_SET_COUNT:
                return Integer.class;
            case COLTYPE_PEPTIDE_MSQUERY:
                return DMsQuery.class;
        }
        return null; // should not happen
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
        
        switch (col) {
            case COLTYPE_PEPTIDE_ID:
                return peptideInstance.getId();
            case COLTYPE_PEPTIDE_PREVIOUS_AA: {
                SequenceMatch sequenceMatch = peptideInstance.getBestPeptideMatch().getSequenceMatch();
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
                // Retrieve typical Peptide Match
                DPeptideMatch peptideMatch = (DPeptideMatch) peptideInstance.getBestPeptideMatch();
                return peptideMatch;
            }
            case COLTYPE_PEPTIDE_NEXT_AA: {
                SequenceMatch sequenceMatch = peptideInstance.getBestPeptideMatch().getSequenceMatch();
                if (sequenceMatch == null) {
                    return "";
                }
                Character residueAfter= sequenceMatch.getResidueAfter();
                if (residueAfter != null) {
                    return String.valueOf(Character.toUpperCase(residueAfter));
                } else {
                    return "";
                }
            }
            case COLTYPE_PEPTIDE_SCORE: {
                // Retrieve typical Peptide Match
                DPeptideMatch peptideMatch = (DPeptideMatch) peptideInstance.getBestPeptideMatch();
                if (peptideMatch == null) {
                    return null; // should never happen   
                }
                return peptideMatch.getScore();
            }
            case COLTYPE_PEPTIDE_RANK: {
                // Retrieve typical Peptide Match
                DPeptideMatch peptideMatch = (DPeptideMatch) peptideInstance.getBestPeptideMatch();
                if (peptideMatch == null) {
                    return null; // should never happen   
                }
                return (peptideMatch.getCDPrettyRank() == null) ? null : peptideMatch.getCDPrettyRank();
            }
            case COLTYPE_PEPTIDE_PROTEIN_SET_COUNT: {
                DPeptideMatch peptideMatch = (DPeptideMatch) peptideInstance.getBestPeptideMatch();
                if (peptideMatch == null) {
                    return null;
                }
                Peptide p = peptideMatch.getPeptide();
                if (p == null) {
                    return null;
                }
                ArrayList<DProteinSet> proteinSetList = p.getTransientData().getProteinSetArray();
                if (proteinSetList == null) {
                    return null;
                }
                
                return proteinSetList.size();
            }
            case COLTYPE_PEPTIDE_PROTEIN_SET_NAMES: {
                DPeptideMatch peptideMatch = (DPeptideMatch) peptideInstance.getBestPeptideMatch();
                if (peptideMatch == null) {
                    return ""; // should never happen   
                }
                Peptide p = peptideMatch.getPeptide();
                if (p == null) {
                    return "";
                }
                ArrayList<DProteinSet> proteinSetList = p.getTransientData().getProteinSetArray();
                if (proteinSetList == null) {
                    return "";
                }

                StringBuilder display = new StringBuilder();
                int nbProteinGroups = proteinSetList.size();
                display.append(nbProteinGroups);
                display.append(" (");
                for (int i = 0; i < nbProteinGroups; i++) {
                    display.append(((DProteinSet)proteinSetList.get(i)).getTypicalProteinMatch().getAccession());
                    if (i + 1 < nbProteinGroups) {
                        display.append(',');
                    }
                }
                display.append(')');

                return display.toString();

            }
            case COLTYPE_PEPTIDE_CHARGE: {
                DPeptideMatch peptideMatch = (DPeptideMatch) peptideInstance.getBestPeptideMatch();
                if (peptideMatch == null) {
                    return ""; // should never happen   
                }
                return Integer.valueOf(peptideMatch.getCharge());
            }
            case COLTYPE_PEPTIDE_EXPERIMENTAL_MOZ: {
                DPeptideMatch peptideMatch = (DPeptideMatch) peptideInstance.getBestPeptideMatch();
                if (peptideMatch == null) {
                    return null; // should never happen   
                }
                return Double.valueOf(peptideMatch.getExperimentalMoz());
            }
            /*case COLTYPE_PEPTIDE_DELTA_MOZ: {
                DPeptideMatch peptideMatch = (DPeptideMatch) peptideInstance.getTransientData().getBestPeptideMatch();
                if (peptideMatch == null) {
                    return null; // should never happen   
                }
                return Float.valueOf(peptideMatch.getDeltaMoz());
            }*/           
            case COLTYPE_PEPTIDE_PPM: {
                DPeptideMatch peptideMatch = (DPeptideMatch) peptideInstance.getBestPeptideMatch();
                if (peptideMatch == null) {
                    return null; // should never happen   
                }
                
                float ppm = PeptideClassesUtils.getPPMFor(peptideMatch, peptideMatch.getPeptide());  

                return Float.valueOf(ppm);

            }
            case COLTYPE_PEPTIDE_START: {
                SequenceMatch sequenceMatch = peptideInstance.getBestPeptideMatch().getSequenceMatch();
                if (sequenceMatch == null) {
                    return null;
                }
                int start = sequenceMatch.getId().getStart();
                return Integer.valueOf(start);
            }
            case COLTYPE_PEPTIDE_STOP: {
                SequenceMatch sequenceMatch = peptideInstance.getBestPeptideMatch().getSequenceMatch();
                if (sequenceMatch == null) {
                    return null;
                }
                int stop = sequenceMatch.getId().getStop();
                return Integer.valueOf(stop);
            }
            case COLTYPE_PEPTIDE_CALCULATED_MASS: {
                DPeptideMatch peptideMatch = (DPeptideMatch) peptideInstance.getBestPeptideMatch();
                if (peptideMatch != null) {
                    Peptide p = peptideMatch.getPeptide();
                    if (p != null) {
                        return p.getCalculatedMass();
                    }
                }
                return null;
            }
            case COLTYPE_PEPTIDE_MISSED_CLIVAGE: {
                DPeptideMatch peptideMatch = (DPeptideMatch) peptideInstance.getBestPeptideMatch();
                if (peptideMatch == null) {
                    return null;
                }
                return Integer.valueOf(peptideMatch.getMissedCleavage());
            }
            case COLTYPE_PEPTIDE_RETENTION_TIME: {
                DPeptideMatch peptideMatch = (DPeptideMatch) peptideInstance.getBestPeptideMatch();
                if (peptideMatch == null) {
                    return null;
                }
                return peptideMatch.getRetentionTime();
            }
            case COLTYPE_PEPTIDE_MSQUERY: {
                DPeptideMatch peptideMatch = (DPeptideMatch) peptideInstance.getBestPeptideMatch();
                if (peptideMatch == null) {
                    return null;
                }
                DMsQuery msQuery = peptideMatch.getMsQuery();
                return msQuery;
            }
            case COLTYPE_SPECTRUM_TITLE: {
                DPeptideMatch peptideMatch = (DPeptideMatch) peptideInstance.getBestPeptideMatch();
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
                DPeptideMatch peptideMatch = (DPeptideMatch) peptideInstance.getBestPeptideMatch();
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
                DPeptideMatch peptideMatch = (DPeptideMatch) peptideInstance.getBestPeptideMatch();
                if (peptideMatch == null) {
                     return "";
                }
                
                Peptide p = peptideMatch.getPeptide();
                if (p == null) {
                    return "";
                }
                
                boolean ptmStringLoadeed = p.getTransientData().isPeptideReadablePtmStringLoaded();
                if (!ptmStringLoadeed) {
                    return "";
                }
                
                
                String ptm = "";
                PeptideReadablePtmString ptmString = p.getTransientData().getPeptideReadablePtmString();
                if (ptmString != null) {
                    ptm = ptmString.getReadablePtmString();
                }
                
                return ptm;

               
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

        filtersMap.put(COLTYPE_PEPTIDE_PREVIOUS_AA, new StringFilter(getColumnName(COLTYPE_PEPTIDE_PREVIOUS_AA), null,COLTYPE_PEPTIDE_PREVIOUS_AA));

        ConvertValueInterface peptideConverter = new ConvertValueInterface() {
            @Override
            public Object convertValue(Object o) {
                if (o == null) {
                    return null;
                }
                return ((DPeptideMatch) o).getPeptide().getSequence();
            }
            
        };
        filtersMap.put(COLTYPE_PEPTIDE_NAME, new StringFilter(getColumnName(COLTYPE_PEPTIDE_NAME), peptideConverter, COLTYPE_PEPTIDE_NAME));
        
        filtersMap.put(COLTYPE_PEPTIDE_NEXT_AA, new StringFilter(getColumnName(COLTYPE_PEPTIDE_NEXT_AA), null, COLTYPE_PEPTIDE_NEXT_AA));
        filtersMap.put(COLTYPE_PEPTIDE_SCORE, new DoubleFilter(getColumnName(COLTYPE_PEPTIDE_SCORE), null, COLTYPE_PEPTIDE_SCORE));
        filtersMap.put(COLTYPE_PEPTIDE_RANK, new IntegerFilter(getColumnName(COLTYPE_PEPTIDE_RANK), null, COLTYPE_PEPTIDE_RANK));
        filtersMap.put(COLTYPE_PEPTIDE_PROTEIN_SET_COUNT, new IntegerFilter(getColumnName(COLTYPE_PEPTIDE_PROTEIN_SET_COUNT), null, COLTYPE_PEPTIDE_PROTEIN_SET_COUNT));
        filtersMap.put(COLTYPE_PEPTIDE_PROTEIN_SET_NAMES, new StringFilter(getColumnName(COLTYPE_PEPTIDE_PROTEIN_SET_NAMES), null, COLTYPE_PEPTIDE_PROTEIN_SET_NAMES));
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

        filtersMap.put(COLTYPE_SPECTRUM_TITLE, new StringFilter(getColumnName(COLTYPE_SPECTRUM_TITLE), null, COLTYPE_SPECTRUM_TITLE));
        filtersMap.put(COLTYPE_PEPTIDE_ION_PARENT_INTENSITY, new DoubleFilter(getColumnName(COLTYPE_PEPTIDE_ION_PARENT_INTENSITY), null, COLTYPE_PEPTIDE_ION_PARENT_INTENSITY));
        filtersMap.put(COLTYPE_PEPTIDE_PTM, new StringFilter(getColumnName(COLTYPE_PEPTIDE_PTM), null, COLTYPE_PEPTIDE_PTM));
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
        int[] keys = { COLTYPE_PEPTIDE_NAME, COLTYPE_PEPTIDE_ID };
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
            return ExportModelUtilities.getExportFonts(m_peptideInstances[row]);
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
                renderer = new DefaultLeftAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class));
                break;
            }
            case COLTYPE_PEPTIDE_NAME: {
                renderer = new PeptideRenderer();
                break;
            }
            case COLTYPE_PEPTIDE_SCORE: {
                renderer = m_scoreRenderer;
                break;
            }
            case COLTYPE_PEPTIDE_PPM: {
                renderer = new FloatRenderer(new DefaultRightAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class)));
                break;
            }
            case COLTYPE_PEPTIDE_RETENTION_TIME: {
                renderer = new FloatRenderer(new DefaultRightAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class)), 4);
                break;
            }
            case COLTYPE_PEPTIDE_START:
            case COLTYPE_PEPTIDE_STOP:
            case COLTYPE_PEPTIDE_CHARGE:
            case COLTYPE_PEPTIDE_MISSED_CLIVAGE: 
            case COLTYPE_PEPTIDE_ION_PARENT_INTENSITY:
            case COLTYPE_PEPTIDE_PROTEIN_SET_COUNT: {
                renderer = new DefaultRightAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(Integer.class));
                break;
            }
            case COLTYPE_PEPTIDE_CALCULATED_MASS:
            case COLTYPE_PEPTIDE_EXPERIMENTAL_MOZ: {
                renderer =  new DoubleRenderer( new DefaultRightAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class)));
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
