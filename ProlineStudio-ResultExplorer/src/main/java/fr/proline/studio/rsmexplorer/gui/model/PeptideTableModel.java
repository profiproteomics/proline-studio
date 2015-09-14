package fr.proline.studio.rsmexplorer.gui.model;


import fr.proline.core.orm.msi.Peptide;
import fr.proline.core.orm.msi.PeptideReadablePtmString;
import fr.proline.core.orm.msi.SequenceMatch;
import fr.proline.core.orm.msi.dto.DMsQuery;
import fr.proline.core.orm.msi.dto.DPeptideInstance;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.core.orm.msi.dto.DProteinSet;
import fr.proline.studio.filter.*;
import fr.proline.studio.graphics.PlotInformation;
import fr.proline.studio.graphics.PlotType;
import fr.proline.studio.rsmexplorer.gui.renderer.DefaultLeftAlignRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.DefaultRightAlignRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.DoubleRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.FloatRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.PeptideRenderer;
import fr.proline.studio.table.DecoratedTableModel;
import fr.proline.studio.table.GlobalTableModelInterface;
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
    public static final int COLTYPE_PEPTIDE_SCORE = 4;
    public static final int COLTYPE_PROTEIN_SETS_MATCHES = 5;
    public static final int COLTYPE_PEPTIDE_START = 6;
    public static final int COLTYPE_PEPTIDE_STOP = 7;
    public static final int COLTYPE_PEPTIDE_CALCULATED_MASS = 8;
    public static final int COLTYPE_PEPTIDE_EXPERIMENTAL_MOZ = 9;
    //public static final int COLTYPE_PEPTIDE_DELTA_MOZ = 9;
    public static final int COLTYPE_PEPTIDE_PPM = 10;
    public static final int COLTYPE_PEPTIDE_CHARGE = 11;
    public static final int COLTYPE_PEPTIDE_MISSED_CLIVAGE = 12;
    public static final int COLTYPE_PEPTIDE_RETENTION_TIME = 13;
    public static final int COLTYPE_PEPTIDE_ION_PARENT_INTENSITY = 14;
    public static final int COLTYPE_PEPTIDE_PTM = 15;
    private static final String[] m_columnNames = {"Id", "Prev. AA", "Peptide", "Next AA", "Score", "Protein S. Matches", "Start", "Stop", "Calc. Mass", "Exp. MoZ", "Ppm"/*"Delta MoZ"*/, "Charge", "Missed Cl.", "RT", "Ion Parent Int.", "PTM"};
    private static final String[] m_columnTooltips = {"Peptide Inst. Id", "Previous Amino Acid","Peptide", "Next Amino Acid", "Score", "Protein Set Matches", "Start", "Stop", "Calculated Mass", "Experimental Mass to Charge Ration", "parts-per-million"/*"Delta Mass to Charge Ratio"*/, "Charge", "Missed Clivage", "Retention Time", "Ion Parent Intensity", "Post Translational Modifications"};
    
    
    private DPeptideInstance[] m_peptideInstances = null;



    private String m_modelName;
    
    public DPeptideInstance getPeptide(int row) {
        return m_peptideInstances[row];

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
            case COLTYPE_PROTEIN_SETS_MATCHES:
            case COLTYPE_PEPTIDE_PTM:
                return String.class;
            case COLTYPE_PEPTIDE_SCORE:
            case COLTYPE_PEPTIDE_RETENTION_TIME:
            case COLTYPE_PEPTIDE_ION_PARENT_INTENSITY:
            case COLTYPE_PEPTIDE_PPM: //COLTYPE_PEPTIDE_DELTA_MOZ:
                return Float.class;
            case COLTYPE_PEPTIDE_EXPERIMENTAL_MOZ:
            case COLTYPE_PEPTIDE_CALCULATED_MASS:
                return Double.class;
            case COLTYPE_PEPTIDE_CHARGE:
            case COLTYPE_PEPTIDE_MISSED_CLIVAGE:
            case COLTYPE_PEPTIDE_START:
            case COLTYPE_PEPTIDE_STOP:    
                return Integer.class;
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
            case COLTYPE_PROTEIN_SETS_MATCHES: {
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
                
                double deltaMoz = (double) peptideMatch.getDeltaMoz();
                double calculatedMass = peptideMatch.getPeptide().getCalculatedMass();
                double charge = (double) peptideMatch.getCharge();
                final double CSTE = 1.007825;
                final double EXP_CSTE = StrictMath.pow(10, 6);
                float ppm = (float) ((deltaMoz * EXP_CSTE) / ((calculatedMass + (charge * CSTE)) / charge));

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
                return peptideInstance.getElutionTime();
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
        filtersMap.put(COLTYPE_PROTEIN_SETS_MATCHES, new StringFilter(getColumnName(COLTYPE_PROTEIN_SETS_MATCHES), null, COLTYPE_PROTEIN_SETS_MATCHES));
        filtersMap.put(COLTYPE_PEPTIDE_START, new IntegerFilter(getColumnName(COLTYPE_PEPTIDE_START), null, COLTYPE_PEPTIDE_START));
        filtersMap.put(COLTYPE_PEPTIDE_STOP, new IntegerFilter(getColumnName(COLTYPE_PEPTIDE_STOP), null, COLTYPE_PEPTIDE_STOP));
        filtersMap.put(COLTYPE_PEPTIDE_CALCULATED_MASS, new DoubleFilter(getColumnName(COLTYPE_PEPTIDE_CALCULATED_MASS), null, COLTYPE_PEPTIDE_CALCULATED_MASS));
        filtersMap.put(COLTYPE_PEPTIDE_EXPERIMENTAL_MOZ, new DoubleFilter(getColumnName(COLTYPE_PEPTIDE_EXPERIMENTAL_MOZ), null, COLTYPE_PEPTIDE_EXPERIMENTAL_MOZ));
        filtersMap.put(COLTYPE_PEPTIDE_PPM, new DoubleFilter(getColumnName(COLTYPE_PEPTIDE_PPM), null, COLTYPE_PEPTIDE_PPM));
        filtersMap.put(COLTYPE_PEPTIDE_CHARGE, new IntegerFilter(getColumnName(COLTYPE_PEPTIDE_CHARGE), null, COLTYPE_PEPTIDE_CHARGE));
        filtersMap.put(COLTYPE_PEPTIDE_MISSED_CLIVAGE, new IntegerFilter(getColumnName(COLTYPE_PEPTIDE_MISSED_CLIVAGE), null, COLTYPE_PEPTIDE_MISSED_CLIVAGE));
        filtersMap.put(COLTYPE_PEPTIDE_RETENTION_TIME, new DoubleFilter(getColumnName(COLTYPE_PEPTIDE_RETENTION_TIME), null, COLTYPE_PEPTIDE_RETENTION_TIME));
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
        return -1l; // not used
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
    public int getBestXAxisColIndex(PlotType plotType) {
        return -1;
    }

    @Override
    public int getBestYAxisColIndex(PlotType plotType) {
        return -1;
    }

    @Override
    public String getExportRowCell(int row, int col) {
        return null;
    }

    @Override
    public String getExportColumnName(int col) {
        return getColumnName(col);
    }

    @Override
    public TableCellRenderer getRenderer(int col) {

        if (m_rendererMap.containsKey(col)) {
            return m_rendererMap.get(col);
        }
        
        TableCellRenderer renderer = null;
        switch (col) {
            case COLTYPE_PEPTIDE_PREVIOUS_AA:
            case COLTYPE_PEPTIDE_NEXT_AA:
            case COLTYPE_PROTEIN_SETS_MATCHES: 
            case COLTYPE_PEPTIDE_PTM: {
                renderer = new DefaultLeftAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class));
                break;
            }
            case COLTYPE_PEPTIDE_NAME: {
                renderer = new PeptideRenderer();
                break;
            }
            case COLTYPE_PEPTIDE_SCORE:
            case COLTYPE_PEPTIDE_PPM: {
                renderer = new FloatRenderer(new DefaultRightAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class)));
                break;
            }
            case COLTYPE_PEPTIDE_START:
            case COLTYPE_PEPTIDE_STOP:
            case COLTYPE_PEPTIDE_CHARGE:
            case COLTYPE_PEPTIDE_MISSED_CLIVAGE: 
            case COLTYPE_PEPTIDE_RETENTION_TIME:
            case COLTYPE_PEPTIDE_ION_PARENT_INTENSITY: {
                renderer = new DefaultRightAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(Integer.class));
                break;
            }
            case COLTYPE_PEPTIDE_CALCULATED_MASS:
            case COLTYPE_PEPTIDE_EXPERIMENTAL_MOZ: {
                renderer =  new DoubleRenderer( new DefaultRightAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class)));
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


    
}
