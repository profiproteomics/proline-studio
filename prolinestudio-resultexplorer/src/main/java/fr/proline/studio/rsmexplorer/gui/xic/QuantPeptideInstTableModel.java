package fr.proline.studio.rsmexplorer.gui.xic;

import fr.proline.core.orm.msi.PeptideReadablePtmString;
import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.msi.dto.DCluster;
import fr.proline.core.orm.msi.dto.DMasterQuantPeptide;
import fr.proline.core.orm.msi.dto.DMasterQuantProteinSet;
import fr.proline.core.orm.msi.dto.DPeptideInstance;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.core.orm.msi.dto.DQuantPeptide;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.core.orm.uds.dto.DQuantitationChannel;
import fr.proline.studio.extendedtablemodel.ExtraDataType;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.dam.tasks.xic.DatabaseLoadXicMasterQuantTask;
import fr.proline.studio.dam.tasks.xic.DatabaseModifyPeptideTask;
import fr.proline.studio.export.ExportFontData;
import fr.proline.studio.table.ExportModelUtilities;
import fr.proline.studio.filter.ConvertValueInterface;
import fr.proline.studio.filter.DoubleFilter;
import fr.proline.studio.filter.Filter;
import fr.proline.studio.filter.IntegerFilter;
import fr.proline.studio.filter.StringDiffFilter;
import fr.proline.studio.filter.StringFilter;
import fr.proline.studio.graphics.PlotInformation;
import fr.proline.studio.graphics.PlotType;
import fr.proline.studio.gui.DefaultFloatingPanel;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.rsmexplorer.DataBoxViewerManager;
import fr.proline.studio.rsmexplorer.gui.renderer.CompareValueRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.FloatRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.PeptideRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.ScoreRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.TimeRenderer;
import fr.proline.studio.extendedtablemodel.CompoundTableModel;
import fr.proline.studio.extendedtablemodel.GlobalTableModelInterface;
import fr.proline.studio.table.LazyData;
import fr.proline.studio.table.LazyTable;
import fr.proline.studio.table.LazyTableModel;
import fr.proline.studio.table.TableDefaultRendererManager;
import fr.proline.studio.table.renderer.BigFloatOrDoubleRenderer;
import fr.proline.studio.table.renderer.DefaultLeftAlignRenderer;
import fr.proline.studio.table.renderer.DefaultRightAlignRenderer;
import fr.proline.studio.table.renderer.DoubleRenderer;
import fr.proline.studio.table.renderer.GrayedRenderer;
import fr.proline.studio.types.QuantitationType;
import fr.proline.studio.types.XicGroup;
import fr.proline.studio.types.XicMode;
import fr.proline.studio.utils.CyclicColorPalette;
import fr.proline.studio.utils.StringUtils;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.table.TableCellRenderer;
import org.apache.poi.hssf.util.HSSFColor;

/**
 *
 * @author VD225637
 */
public class QuantPeptideInstTableModel  extends LazyTableModel implements GlobalTableModelInterface {

    public static final int COLTYPE_PEPTIDE_ID = 0;
    public static final int COLTYPE_MQPEPTIDE_SELECTION_LEVEL = 1;
    public static final int COLTYPE_PEPTIDE_NAME = 2;
    public static final int COLTYPE_PEPTIDE_PTM = 3;
    public static final int COLTYPE_PEPTIDE_SCORE = 4;
    public static final int COLTYPE_PEPTIDE_CHARGE = 5;
    public static final int COLTYPE_PEPTIDE_MOZ = 6;
    public static final int COLTYPE_PEPTIDE_RETENTION_TIME = 7;
    public static final int COLTYPE_PEPTIDE_PROTEIN_SET_COUNT = 8;
    public static final int COLTYPE_PEPTIDE_PROTEIN_SET_NAMES = 9;
    public static final int COLTYPE_OVERVIEW = 10;
    public static final int COLTYPE_PEPTIDE_CLUSTER = 11;
    public static final int COLTYPE_PEPTIDE_INST_ID = 12;
    public static final int LAST_STATIC_COLUMN = COLTYPE_PEPTIDE_INST_ID;
    private static final String[] m_columnNames = {"Id", "Validate/Invalidate", "Peptide Sequence", "PTMs", "Score", "Charge", "m/z", "RT", "Protein Set Count", "Protein Sets", "Overview", "Cluster","Pep. Instance Id"};
    private static final String[] m_toolTipColumns = {"MasterQuantPeptide Id", "Validate or Invalidate Peptide", "Identified Peptide Sequence", "Post Translational Modifications", "Score", "Charge", "Mass to Charge Ratio", "Retention Time (min)", "Protein Set Count", "Protein Sets", "Overview", "Cluster Number","Peptide Instance ID"};

    public static final int COLTYPE_SELECTION_LEVEL = 0;
    public static final int COLTYPE_IDENT_PSM = 1;
    public static final int COLTYPE_PSM = 2;
    public static final int COLTYPE_RAW_ABUNDANCE = 3;
    public static final int COLTYPE_ABUNDANCE = 4;

    private int m_overviewType;

    private static final String[] m_columnNamesQC = {"Sel. level", "Ident. PSMs count", "PSMs count", "Raw abundance", "Abundance"};
    private static final String[] m_toolTipQC = {"Selection level", "Identification peptides matches count", "Peptides matches count", "Raw abundance", "Abundance"};

    private static final String[] m_columnNamesQC_SC = {"Sel. level", "Ident. PSMs count", "Basic SC", "Specific SC"};
    private static final String[] m_toolTipQC_SC = {"Selection level", "Identification peptides matches count", "Basic Spectral Count", "Specific Spectral Count"};

    private List<DMasterQuantPeptide> m_quantPeptides = null;
    private DQuantitationChannel[] m_quantChannels = null;
    private int m_quantChannelNumber;

    private String m_modelName;

    private AbstractDataBox m_databox;

    private boolean m_isXICMode;
    private long m_projectId;

    private final ScoreRenderer m_scoreRenderer = new ScoreRenderer();

    private HashSet<Integer> m_modifiedLevels = new HashSet<>();

    public QuantPeptideInstTableModel(LazyTable table, boolean xicMode) {
        super(table);
        m_isXICMode = xicMode;
        if (xicMode) {
            m_overviewType = COLTYPE_ABUNDANCE;
        } else {
            m_overviewType = COLTYPE_RAW_ABUNDANCE;
        }
    }

    public void setDatabox(AbstractDataBox databox) {
        m_databox = databox;
    }

    @Override
    public int getColumnCount() {
        if (m_quantChannels == null) {
            return m_columnNames.length;
        } else {
            if (m_isXICMode) {
                return m_columnNames.length + m_quantChannelNumber * m_columnNamesQC.length;
            } else {
                return m_columnNames.length + m_quantChannelNumber * m_columnNamesQC_SC.length;
            }
        }
    }

    @Override
    public String getColumnName(int col) {
        if (col <= LAST_STATIC_COLUMN) {
            return m_columnNames[col];
        } else if (m_quantChannels != null) {

            int nbQc;
            int id;
            if (m_isXICMode) {
                nbQc = (col - m_columnNames.length) / m_columnNamesQC.length;
                id = col - m_columnNames.length - (nbQc * m_columnNamesQC.length);
            } else {
                nbQc = (col - m_columnNames.length) / m_columnNamesQC_SC.length;
                id = col - m_columnNames.length - (nbQc * m_columnNamesQC_SC.length);
            }

            StringBuilder sb = new StringBuilder();
            String rsmHtmlColor = CyclicColorPalette.getHTMLColor(nbQc);
            sb.append("<html><font color='").append(rsmHtmlColor).append("'>&#x25A0;&nbsp;</font>");
            if (m_isXICMode) {
                sb.append(m_columnNamesQC[id]);
            } else {
                sb.append(m_columnNamesQC_SC[id]);
            }
            sb.append("<br/>");
            sb.append(m_quantChannels[nbQc].getName());

            sb.append("</html>");
            return sb.toString();
        } else {
            return ""; // should not happen
        }
    }

    @Override
    public String getExportColumnName(int col) {
        if (col <= LAST_STATIC_COLUMN) {
            return m_columnNames[col];
        } else if (m_quantChannels != null) {

            int nbQc;
            int id;
            if (m_isXICMode) {
                nbQc = (col - m_columnNames.length) / m_columnNamesQC.length;
                id = col - m_columnNames.length - (nbQc * m_columnNamesQC.length);
            } else {
                nbQc = (col - m_columnNames.length) / m_columnNamesQC_SC.length;
                id = col - m_columnNames.length - (nbQc * m_columnNamesQC_SC.length);
            }

            StringBuilder sb = new StringBuilder();
            if (m_isXICMode) {
                sb.append(m_columnNamesQC[id]);
            } else {
                sb.append(m_columnNamesQC_SC[id]);
            }
            sb.append(" ");
            sb.append(m_quantChannels[nbQc].getName());

            return sb.toString();
        } else {
            return ""; // should not happen
        }

    }

    @Override
    public String getExportRowCell(int row, int col) {

        // Retrieve Quant Peptide
        DMasterQuantPeptide peptide = m_quantPeptides.get(row);
        DPeptideInstance peptideInstance = peptide.getPeptideInstance();

        switch (col) {
            case COLTYPE_PEPTIDE_ID: {
                return peptide.getId() == -1 ? "" : Long.toString(peptide.getId());
            }
            case COLTYPE_MQPEPTIDE_SELECTION_LEVEL: {
                if (m_isXICMode) {
                    return Boolean.toString(peptide.getSelectionLevel() == 2);
                } else {
                    return "";
                }
            }
            case COLTYPE_PEPTIDE_NAME: {
                if (peptideInstance == null) {
                    return "";
                } else {
                    if (peptideInstance.getBestPeptideMatch() != null) {
                        return peptideInstance.getBestPeptideMatch().getPeptide().getSequence();
                    } else {
                        return "";
                    }
                }
            }
            case COLTYPE_PEPTIDE_PTM: {
                if (peptideInstance == null) {
                    return "";
                } else if (peptideInstance.getBestPeptideMatch() != null) {
                    boolean ptmStringLoadeed = peptideInstance.getBestPeptideMatch().getPeptide().getTransientData().isPeptideReadablePtmStringLoaded();
                    if (!ptmStringLoadeed) {
                        return null;
                    }
                    String ptm = "";
                    PeptideReadablePtmString ptmString = peptideInstance.getBestPeptideMatch().getPeptide().getTransientData().getPeptideReadablePtmString();
                    if (ptmString != null) {
                        ptm = ptmString.getReadablePtmString();
                    }

                    return ptm;
                } else {
                    return "";
                }
            }
            case COLTYPE_PEPTIDE_SCORE: {

                if (peptideInstance == null) {
                    return "";
                } else if (peptideInstance.getBestPeptideMatch() != null) {
                    Float score = Float.valueOf((float) peptideInstance.getBestPeptideMatch().getScore());
                    return String.valueOf(score);
                } else {
                    return "";
                }

            }
            case COLTYPE_PEPTIDE_PROTEIN_SET_COUNT: {
                if (peptideInstance == null) {
                    return "";
                } else {
                    return Integer.toString(peptideInstance.getValidatedProteinSetCount());
                }
            }
            case COLTYPE_PEPTIDE_PROTEIN_SET_NAMES: {
                if (peptideInstance == null) {
                    return "";
                } else {
                    DPeptideMatch peptideMatch = peptideInstance.getBestPeptideMatch();
                    String[] proteinSetNames = peptideMatch.getProteinSetStringArray();
                    if (proteinSetNames == null) {
                        return "";
                    } else {
                        for (int i = 0; i < proteinSetNames.length; i++) {
                            String name = proteinSetNames[i];
                            if (i < proteinSetNames.length - 1) {
                                m_sb.append(name).append(", ");
                            } else {
                                m_sb.append(name);
                            }
                        }
                        String t = m_sb.toString();
                        m_sb.setLength(0);
                        return t;
                    }
                }
            }
            case COLTYPE_PEPTIDE_CHARGE: {
                if (peptideInstance == null) {
                    return "";
                } else {
                    return String.valueOf(peptideInstance.getBestPeptideMatch().getCharge());
                }

            }
            case COLTYPE_PEPTIDE_MOZ: {
                if (peptideInstance == null) {
                    return "";
                } else {
                    return String.valueOf(peptideInstance.getBestPeptideMatch().getExperimentalMoz());
                }

            }
            case COLTYPE_PEPTIDE_RETENTION_TIME: {
                if (peptideInstance == null) {
                    return "";
                } else {
                    Float f = peptideInstance.getElutionTime();
                    if(f!=null)
                        return StringUtils.getTimeInMinutes(peptideInstance.getElutionTime(),2);
                    else 
                         return "";                    
                }

            }
            case COLTYPE_OVERVIEW:
                return "";

            case COLTYPE_PEPTIDE_CLUSTER: {
                if (peptideInstance == null) {
                    return "";
                } else {
                    DCluster cluster = peptide.getCluster();
                    if (cluster == null) {
                        return "";
                    } else {
                        return Integer.toString(cluster.getClusterId());
                    }
                }
            }
            case COLTYPE_PEPTIDE_INST_ID : {
                if (peptideInstance == null) 
                    return "";
                return peptideInstance.getId() == -1 ? "" : Long.toString(peptideInstance.getId());
            }
            default: {
                // Quant Channel columns 
                int nbQc;
                int id;
                if (m_isXICMode) {
                    nbQc = (col - m_columnNames.length) / m_columnNamesQC.length;
                    id = col - m_columnNames.length - (nbQc * m_columnNamesQC.length);
                } else {
                    nbQc = (col - m_columnNames.length) / m_columnNamesQC_SC.length;
                    id = col - m_columnNames.length - (nbQc * m_columnNamesQC_SC.length);
                }
                if (peptideInstance == null) {
                    switch (id) {
                        case COLTYPE_SELECTION_LEVEL:
                            return Integer.toString(0);
                        case COLTYPE_ABUNDANCE:
                            return Float.toString(0);
                        case COLTYPE_RAW_ABUNDANCE:
                            Float.toString(0);
                        case COLTYPE_PSM:
                            return Integer.toString(0);
                        case COLTYPE_IDENT_PSM:
                            return Integer.toString(0);
                    }
                } else {

                    // retrieve quantPeptide for the quantChannelId
                    Map<Long, DQuantPeptide> quantPeptideByQchIds = peptide.getQuantPeptideByQchIds();
                    if (quantPeptideByQchIds == null) {
                        switch (id) {
                            case COLTYPE_SELECTION_LEVEL:
                                return Integer.toString(0);
                            case COLTYPE_ABUNDANCE:
                                return Float.toString(0);
                            case COLTYPE_RAW_ABUNDANCE:
                                Float.toString(0);
                            case COLTYPE_PSM:
                                return Integer.toString(0);
                            case COLTYPE_IDENT_PSM:
                                return Integer.toString(0);
                        }
                    } else {

                        DQuantPeptide quantPeptide = quantPeptideByQchIds.get(m_quantChannels[nbQc].getId());
                        if (quantPeptide == null) {
                            switch (id) {
                                case COLTYPE_SELECTION_LEVEL:
                                    return Integer.toString(0);
                                case COLTYPE_ABUNDANCE:
                                    return Float.toString(0);
                                case COLTYPE_RAW_ABUNDANCE:
                                    Float.toString(0);
                                case COLTYPE_PSM:
                                    return Integer.toString(0);
                                case COLTYPE_IDENT_PSM:
                                    return Integer.toString(0);
                            }
                        } else {
                            switch (id) {
                                case COLTYPE_SELECTION_LEVEL:
                                    return (quantPeptide.getSelectionLevel() == null ? Integer.toString(0) : Integer.toString(quantPeptide.getSelectionLevel()));
                                case COLTYPE_ABUNDANCE:
                                    return ((quantPeptide.getAbundance() == null || quantPeptide.getAbundance().isNaN()) ? Float.toString(0) : Float.toString(quantPeptide.getAbundance()));
                                case COLTYPE_RAW_ABUNDANCE:
                                    return ((quantPeptide.getRawAbundance() == null || quantPeptide.getRawAbundance().isNaN()) ? Float.toString(0) : Float.toString(quantPeptide.getRawAbundance()));
                                case COLTYPE_PSM:
                                    return (quantPeptide.getPeptideMatchesCount() == null ? Integer.toString(0) : Integer.toString(quantPeptide.getPeptideMatchesCount()));
                                case COLTYPE_IDENT_PSM:
                                    return (quantPeptide.getIdentPeptideMatchCount() == null ? Integer.toString(0) : Integer.toString(quantPeptide.getIdentPeptideMatchCount()));
                            }
                        }
                    }
                }
            }
        }
        return "";
    }
    
    @Override
    public ArrayList<ExportFontData> getExportFonts(int row, int col) {
        if (col == COLTYPE_PEPTIDE_NAME) {
            DMasterQuantPeptide quantPeptide = m_quantPeptides.get(row);
            DPeptideInstance peptideInstance = quantPeptide.getPeptideInstance();
            return ExportModelUtilities.getExportFonts(peptideInstance);
        }
        return null;
    }

    @Override
    public String getToolTipForHeader(int col) {
        if (col == COLTYPE_OVERVIEW) {
            return m_toolTipColumns[col] + " on " + (m_isXICMode ? m_toolTipQC[m_overviewType] : m_toolTipQC_SC[m_overviewType]);
        }
        if (col <= LAST_STATIC_COLUMN) {
            return m_toolTipColumns[col];
        } else if (m_quantChannels != null) {
            int nbQc;
            int id;
            if (m_isXICMode) {
                nbQc = (col - m_columnNames.length) / m_columnNamesQC.length;
                id = col - m_columnNames.length - (nbQc * m_columnNamesQC.length);
            } else {
                nbQc = (col - m_columnNames.length) / m_columnNamesQC_SC.length;
                id = col - m_columnNames.length - (nbQc * m_columnNamesQC_SC.length);
            }
            String rawFilePath = StringUtils.truncate(m_quantChannels[nbQc].getRawFilePath(), 50);

            StringBuilder sb = new StringBuilder();
            String rsmHtmlColor = CyclicColorPalette.getHTMLColor(nbQc);
            sb.append("<html><font color='").append(rsmHtmlColor).append("'>&#x25A0;&nbsp;</font>");
            if (m_isXICMode) {
                sb.append(m_toolTipQC[id]);
            } else {
                sb.append(m_toolTipQC_SC[id]);
            }
            sb.append("<br/>");
            sb.append(m_quantChannels[nbQc].getFullName());
            sb.append("<br/>");
            sb.append(rawFilePath);

            sb.append("</html>");
            return sb.toString();
        } else {
            return ""; // should not happen
        }
    }

    @Override
    public Class getColumnClass(int col) {
        if (col == COLTYPE_PEPTIDE_ID) {
            return String.class;
        } else if (col == COLTYPE_MQPEPTIDE_SELECTION_LEVEL) {
            if (m_isXICMode) {
                return Boolean.class;
            } else {
                return String.class;
            }
        } else if (col == COLTYPE_OVERVIEW) {
            return CompareValueRenderer.CompareValue.class;
        }
        return LazyData.class;
    }

    @Override
    public int getSubTaskId(int col) {
        return DatabaseLoadXicMasterQuantTask.SUB_TASK_PEPTIDE_INSTANCE;
    }

    @Override
    public int getRowCount() {
        if (m_quantPeptides == null) {
            return 0;
        }
        return m_quantPeptides.size();
    }

    /**
     * returns the tooltip to display for a given row and a given col for the
     * cluster returns the abundances list
     *
     * @param row
     * @param col
     * @return
     */
    @Override
    public String getTootlTipValue(int row, int col) {
        if (m_quantPeptides == null || row < 0) {
            return "";
        }

        // Retrieve Quant Peptide
        DMasterQuantPeptide peptide = m_quantPeptides.get(row);
        DCluster cluster = peptide.getCluster();
        if (col == COLTYPE_PEPTIDE_CLUSTER && cluster != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("<html>");
            sb.append("Cluster ");
            sb.append(cluster.getClusterId());
            sb.append("<br/>");
            if (cluster.getAbundances() != null) {
                sb.append("<table><tr> ");
                List<Float> abundances = cluster.getAbundances();
                for (int a = 0; a < m_quantChannels.length; a++) {
                    sb.append("<td>");
                    String rsmHtmlColor = CyclicColorPalette.getHTMLColor(a);
                    sb.append("<html><font color='").append(rsmHtmlColor).append("'>&#x25A0;&nbsp;</font>");
                    sb.append("Abundance");
                    sb.append("<br/>");
                    sb.append(m_quantChannels[a].getFullName());
                    sb.append("</td>");
                }
                sb.append("</tr><tr> ");
                // we suppose that the abundances are in the "good" order
                for (Float abundance : abundances) {
                    sb.append("<td>");
                    sb.append(abundance.isNaN() ? "" : abundance);
                    sb.append("</td>");
                }
                sb.append("</tr></table>");
            }
            sb.append("</html>");
            return sb.toString();
        } else if (cluster != null) {
            int a = getAbundanceCol(col);
            if (a >= 0) {
                StringBuilder sb = new StringBuilder();
                sb.append("<html>");
                sb.append("Cluster ");
                sb.append(cluster.getClusterId());
                sb.append("<br/>");
                if (cluster.getAbundances() != null) {
                    sb.append("<table><tr> ");
                    List<Float> abundances = cluster.getAbundances();
                    sb.append("<td>");
                    String rsmHtmlColor = CyclicColorPalette.getHTMLColor(a);
                    sb.append("<html><font color='").append(rsmHtmlColor).append("'>&#x25A0;&nbsp;</font>");
                    sb.append("Abundance");
                    sb.append("<br/>");
                    sb.append(m_quantChannels[a].getFullName());
                    sb.append("</td>");
                    sb.append("</tr><tr> ");
                    // we suppose that the abundances are in the "good" order
                    sb.append("<td>");
                    sb.append(abundances.get(a).isNaN() ? "" : abundances.get(a));
                    sb.append("</td>");
                    sb.append("</tr></table>");
                }
                sb.append("</html>");
                return sb.toString();
            }
        }
        return "";
    }

    /**
     * returns -1 if the col is not an Abundance Col, otherwise the id in the
     * quantChannel tab
     *
     * @param col
     * @return
     */
    private int getAbundanceCol(int col) {
        if (m_quantChannels != null) {
            int nbQc;
            int id;
            if (m_isXICMode) {
                nbQc = (col - m_columnNames.length) / m_columnNamesQC.length;
                id = col - m_columnNames.length - (nbQc * m_columnNamesQC.length);
            } else {
                nbQc = (col - m_columnNames.length) / m_columnNamesQC_SC.length;
                id = col - m_columnNames.length - (nbQc * m_columnNamesQC_SC.length);
            }
            if (id == COLTYPE_ABUNDANCE) {
                return nbQc;
            }
            return -1;
        }
        return -1;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (!m_isXICMode) {
            return false;
        }
        if (columnIndex != COLTYPE_MQPEPTIDE_SELECTION_LEVEL) {
            return false;
        }
        DMasterQuantPeptide peptide = m_quantPeptides.get(rowIndex);
        DPeptideInstance peptideInstance = peptide.getPeptideInstance();
        if (peptideInstance == null) {
            return true;
        }
        Float f = peptideInstance.getElutionTime();
        return (f != null); // a peptide with an elutime is linked to a PeptideIon and MasterQuantComponent

    }

    @Override
    public void setValueAt(Object aValue, int row, int col) {

        Integer rowKey = row;
        if (m_modifiedLevels.contains(rowKey)) {
            m_modifiedLevels.remove(rowKey);
        } else {
            m_modifiedLevels.add(rowKey);
        }

        ((XicPeptidePanel) m_databox.getPanel()).displayValidatePanel(!m_modifiedLevels.isEmpty());

        /*
         DMasterQuantPeptide masterQuantPeptide = m_quantPeptides.get(row);
         masterQuantPeptide.setSelectionLevel(((Boolean) aValue) ? 2 : 0);

        
         final ArrayList<DMasterQuantProteinSet> masterQuantProteinSetModified = new ArrayList<>();
        
         AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

         @Override
         public boolean mustBeCalledInAWT() {
         return true;
         }

         @Override
         public void run(boolean success, long taskId, SubTask subTask, boolean finished) {

         if (!success) {
         return; // should not happen
         }

         // propagate modifications to the previous views
         DataBoxViewerManager.loadedDataModified(m_projectId, m_databox.getRsetId(), m_databox.getRsmId(), DMasterQuantProteinSet.class, masterQuantProteinSetModified, DataBoxViewerManager.REASON_PEPTIDE_SUPPRESSED);
         }
         };

         // ask asynchronous loading of data
         DatabaseModifyPeptideTask task = new DatabaseModifyPeptideTask(callback);
         task.initDisablePeptide(m_projectId, masterQuantPeptide, masterQuantProteinSetModified);
         AccessDatabaseThread.getAccessDatabaseThread().addTask(task);
        
        
         fireTableCellUpdated(row, col);*/
    }

    public void cancelModifications() {
        if (m_modifiedLevels.isEmpty()) {
            return;
        }
        m_modifiedLevels.clear();
        fireTableDataChanged();
    }

    public void validateModifications(final DefaultFloatingPanel panel) {
        if (m_modifiedLevels.isEmpty()) {
            return;
        }

        ArrayList<DMasterQuantPeptide> listToModify = new ArrayList<>();
        Iterator<Integer> rowIt = m_modifiedLevels.iterator();
        while (rowIt.hasNext()) {
            Integer row = rowIt.next();
            DMasterQuantPeptide masterQuantPeptide = m_quantPeptides.get(row);
            listToModify.add(masterQuantPeptide);
        }

        final ArrayList<DMasterQuantProteinSet> masterQuantProteinSetModified = new ArrayList<>();

        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {

                if (!success) {
                    panel.actionFinished(false, null);
                    return; // should not happen
                }

                // propagate modifications to the previous views
                DataBoxViewerManager.loadedDataModified(m_projectId, m_databox.getRsetId(), m_databox.getRsmId(), DMasterQuantProteinSet.class, masterQuantProteinSetModified, DataBoxViewerManager.REASON_PEPTIDE_SUPPRESSED);

                m_modifiedLevels.clear();

                fireTableDataChanged();

                panel.actionFinished(true, null);
            }
        };

        // ask asynchronous loading of data
        DatabaseModifyPeptideTask task = new DatabaseModifyPeptideTask(callback);
        task.initDisablePeptide(m_projectId, listToModify, masterQuantProteinSetModified);
        AccessDatabaseThread.getAccessDatabaseThread().addTask(task);

    }

    @Override
    public Object getValueAt(final int row, int col) {

        // Retrieve Quant Peptide
        DMasterQuantPeptide peptide = m_quantPeptides.get(row);
        DPeptideInstance peptideInstance = peptide.getPeptideInstance();

        switch (col) {
            case COLTYPE_PEPTIDE_ID: {
                return peptide.getId() == -1 ? "" : Long.toString(peptide.getId());
            }
            case COLTYPE_MQPEPTIDE_SELECTION_LEVEL: {
                if (m_isXICMode) {
                    if (m_modifiedLevels.contains(row)) {
                        return (peptide.getSelectionLevel() != 2); // FLIPPED value -> modified value not saved in database
                    }
                    return (peptide.getSelectionLevel() == 2); // 2 = enable ; 1 = peptide disabled by algorithm, 0 = peptide disabled by human
                } else {
                    return "";
                }
            }
            case COLTYPE_PEPTIDE_NAME: {
                LazyData lazyData = getLazyData(row, col);
                if (peptideInstance == null) {
                    lazyData.setData(null);
                    givePriorityTo(m_taskId, row, col);
                } else {
                    if (peptideInstance.getBestPeptideMatch() != null) {
                        lazyData.setData(peptideInstance.getBestPeptideMatch());
                    } else {
                        lazyData.setData(null);
                    }
                }
                return lazyData;

            }

            case COLTYPE_PEPTIDE_PTM: {
                LazyData lazyData = getLazyData(row, col);
                if (peptideInstance == null) {
                    lazyData.setData(null);
                    givePriorityTo(m_taskId, row, col);
                } else if (peptideInstance.getBestPeptideMatch() != null) {
                    boolean ptmStringLoaded = peptideInstance.getBestPeptideMatch().getPeptide().getTransientData().isPeptideReadablePtmStringLoaded();
                    if (!ptmStringLoaded) {
                        return null;
                    }
                    String ptm = "";
                    PeptideReadablePtmString ptmString = peptideInstance.getBestPeptideMatch().getPeptide().getTransientData().getPeptideReadablePtmString();
                    if (ptmString != null) {
                        ptm = ptmString.getReadablePtmString();
                    }

                    lazyData.setData(ptm);
                } else {
                    lazyData.setData("");
                }

                return lazyData;
            }
            case COLTYPE_PEPTIDE_SCORE: {
                LazyData lazyData = getLazyData(row, col);
                if (peptideInstance == null) {
                    lazyData.setData(null);
                    givePriorityTo(m_taskId, row, col);
                } else if (peptideInstance.getBestPeptideMatch() != null) {
                    Float score = Float.valueOf((float) peptideInstance.getBestPeptideMatch().getScore());
                    lazyData.setData(score);
                } else {
                    lazyData.setData(null);
                }
                return lazyData;
            }
            case COLTYPE_PEPTIDE_PROTEIN_SET_COUNT: {
                LazyData lazyData = getLazyData(row, col);
                if (peptideInstance == null) {
                    lazyData.setData(null);
                    givePriorityTo(m_taskId, row, col);
                } else {
                    lazyData.setData(peptideInstance.getValidatedProteinSetCount());
                }
                return lazyData;
            }
            case COLTYPE_PEPTIDE_PROTEIN_SET_NAMES: {

                LazyData lazyData = getLazyData(row, col);

                if (peptideInstance == null) {
                    lazyData.setData(null);
                    givePriorityTo(m_taskId, row, col);
                } else {
                    DPeptideMatch peptideMatch = peptideInstance.getBestPeptideMatch();
                    String[] proteinSetNames = peptideMatch.getProteinSetStringArray();
                    if (proteinSetNames == null) {
                        givePriorityTo(m_taskId, row, col);
                        lazyData.setData(null);
                    } else {
                        for (int i = 0; i < proteinSetNames.length; i++) {
                            String name = proteinSetNames[i];
                            if (i < proteinSetNames.length - 1) {
                                m_sb.append(name).append(", ");
                            } else {
                                m_sb.append(name);
                            }
                        }
                        lazyData.setData(m_sb.toString());
                        m_sb.setLength(0);
                    }
                }
                return lazyData;
            }
            case COLTYPE_PEPTIDE_CHARGE: {
                LazyData lazyData = getLazyData(row, col);
                if (peptideInstance == null) {
                    lazyData.setData(null);
                    givePriorityTo(m_taskId, row, col);
                } else {
                    lazyData.setData(peptideInstance.getBestPeptideMatch().getCharge());
                }
                return lazyData;

            }
            case COLTYPE_PEPTIDE_MOZ: {
                LazyData lazyData = getLazyData(row, col);

                if (peptideInstance == null) {
                    lazyData.setData(null);
                    givePriorityTo(m_taskId, row, col);
                } else {
                    lazyData.setData(peptideInstance.getBestPeptideMatch().getExperimentalMoz());
                }
                return lazyData;

            }
            case COLTYPE_PEPTIDE_RETENTION_TIME: {
                LazyData lazyData = getLazyData(row, col);
                if (peptideInstance == null) {
                    lazyData.setData(null);
                    givePriorityTo(m_taskId, row, col);
                } else {
                    Float f = peptideInstance.getElutionTime();
                    if (f == null) {
                        f = Float.NaN;
                    }
                    lazyData.setData(f);
                }
                return lazyData;

            }
            case COLTYPE_OVERVIEW:
                return new CompareValueRenderer.CompareValue() {

                    @Override
                    public int getNumberColumns() {
                        return m_quantChannels.length;
                    }

                    @Override
                    public Color getColor(int col) {
                        return CyclicColorPalette.getColor(col);
                    }

                    @Override
                    public double getValue(int col) {
                        if (m_overviewType == -1) {
                            return 0; // should not happen
                        }
                        int realCol;
                        if (m_isXICMode) {
                            realCol = LAST_STATIC_COLUMN + 1 + m_overviewType + col * m_columnNamesQC.length;
                        } else {
                            realCol = LAST_STATIC_COLUMN + 1 + m_overviewType + col * m_columnNamesQC_SC.length;
                        }

                        LazyData lazyData = (LazyData) getValueAt(row, realCol);
                        if (lazyData != null && lazyData.getData() != null) {
                            if (Number.class.isAssignableFrom(lazyData.getData().getClass())) {
                                return ((Number) lazyData.getData()).floatValue();
                            }
                        }
                        return 0;
                    }

                    public double getValueNoNaN(int col) {
                        double val = getValue(col);
                        if (val != val) { // NaN value
                            return 0;
                        }
                        return val;
                    }

                    @Override
                    public double getMaximumValue() {
                        int nbCols = getNumberColumns();
                        double maxValue = 0;
                        for (int i = 0; i < nbCols; i++) {
                            double v = getValue(i);
                            if (v > maxValue) {
                                maxValue = v;
                            }
                        }
                        return maxValue;

                    }

                    @Override
                    public double calculateComparableValue() {
                        int nbColumns = getNumberColumns();
                        double mean = 0;
                        for (int i = 0; i < nbColumns; i++) {
                            mean += getValueNoNaN(i);
                        }
                        mean /= nbColumns;

                        double maxDiff = 0;
                        for (int i = 0; i < nbColumns; i++) {
                            double diff = getValueNoNaN(i) - mean;
                            if (diff < 0) {
                                diff = -diff;
                            }
                            if (diff > maxDiff) {
                                maxDiff = diff;
                            }
                        }
                        return maxDiff / mean;
                    }

                    @Override
                    public int compareTo(CompareValueRenderer.CompareValue o) {
                        return Double.compare(calculateComparableValue(), o.calculateComparableValue());
                    }
                };

            case COLTYPE_PEPTIDE_CLUSTER: {
                LazyData lazyData = getLazyData(row, col);
                if (peptideInstance == null) {
                    lazyData.setData(null);
                    givePriorityTo(m_taskId, row, col);
                } else {
                    DCluster cluster = peptide.getCluster();
                    if (cluster == null) {
                        lazyData.setData("");
                    } else {
                        lazyData.setData(String.valueOf(cluster.getClusterId()));
                    }
                }
            }
            case COLTYPE_PEPTIDE_INST_ID : {
                LazyData lazyData = getLazyData(row, col);
                if (peptideInstance == null) {
                    lazyData.setData(null);
                    givePriorityTo(m_taskId, row, col);
                } else {
                    lazyData.setData(peptideInstance.getId());
                }
                return lazyData;
                
            }
            default: {
                // Quant Channel columns 
                LazyData lazyData = getLazyData(row, col);
                if (peptideInstance == null) {
                    lazyData.setData(null);
                    givePriorityTo(m_taskId, row, col);
                } else {

                    // retrieve quantPeptide for the quantChannelId
                    int nbQc;
                    int id;
                    if (m_isXICMode) {
                        nbQc = (col - m_columnNames.length) / m_columnNamesQC.length;
                        id = col - m_columnNames.length - (nbQc * m_columnNamesQC.length);
                    } else {
                        nbQc = (col - m_columnNames.length) / m_columnNamesQC_SC.length;
                        id = col - m_columnNames.length - (nbQc * m_columnNamesQC_SC.length);
                    }
                    Map<Long, DQuantPeptide> quantPeptideByQchIds = peptide.getQuantPeptideByQchIds();
                    if (quantPeptideByQchIds == null) {
                        switch (id) {
                            case COLTYPE_SELECTION_LEVEL:
                                lazyData.setData(Integer.valueOf(0));
                                break;
                            case COLTYPE_ABUNDANCE:
                                lazyData.setData(Float.valueOf(0));
                                break;
                            case COLTYPE_RAW_ABUNDANCE:
                                lazyData.setData(Float.valueOf(0));
                                break;
                            case COLTYPE_PSM:
                                lazyData.setData(Integer.valueOf(0));
                                break;
                            case COLTYPE_IDENT_PSM:
                                lazyData.setData(Integer.valueOf(0));
                                break;
                        }
                    } else {
                        DQuantPeptide quantPeptide = quantPeptideByQchIds.get(m_quantChannels[nbQc].getId());
                        if (quantPeptide == null) {
                            switch (id) {
                                case COLTYPE_SELECTION_LEVEL:
                                    lazyData.setData(Integer.valueOf(0));
                                    break;
                                case COLTYPE_ABUNDANCE:
                                    lazyData.setData(Float.valueOf(0));
                                    break;
                                case COLTYPE_RAW_ABUNDANCE:
                                    lazyData.setData(Float.valueOf(0));
                                    break;
                                case COLTYPE_PSM:
                                    lazyData.setData(Integer.valueOf(0));
                                    break;
                                case COLTYPE_IDENT_PSM:
                                    lazyData.setData(Integer.valueOf(0));
                                    break;
                            }
                        } else {
                            switch (id) {
                                case COLTYPE_SELECTION_LEVEL:
                                    lazyData.setData(quantPeptide.getSelectionLevel());
                                    break;
                                case COLTYPE_ABUNDANCE:
                                    lazyData.setData((quantPeptide.getAbundance() == null || quantPeptide.getAbundance().isNaN()) ? Float.valueOf(0) : quantPeptide.getAbundance());
                                    break;
                                case COLTYPE_RAW_ABUNDANCE:
                                    lazyData.setData((quantPeptide.getRawAbundance() == null || quantPeptide.getRawAbundance().isNaN()) ? Float.valueOf(0) : quantPeptide.getRawAbundance());
                                    break;
                                case COLTYPE_PSM:
                                    lazyData.setData(quantPeptide.getPeptideMatchesCount() == null ? Integer.valueOf(0) : quantPeptide.getPeptideMatchesCount());
                                    break;
                                case COLTYPE_IDENT_PSM:
                                    lazyData.setData(quantPeptide.getIdentPeptideMatchCount() == null ? Integer.valueOf(0) : quantPeptide.getIdentPeptideMatchCount());
                                    break;
                            }
                        }
                    }
                }
                return lazyData;
            }
        }

    }
    private final StringBuilder m_sb = new StringBuilder();

    public void setData(Long taskId, long projectId, DQuantitationChannel[] quantChannels, List<DMasterQuantPeptide> peptides, boolean isXICMode) {
        boolean structureChanged = true;
        m_isXICMode = isXICMode;
        if (isXICMode) {
            m_overviewType = COLTYPE_ABUNDANCE;
        } else {
            m_overviewType = COLTYPE_RAW_ABUNDANCE;
        }
        m_projectId = projectId;
        if (m_quantChannels != null && m_quantChannels.length == quantChannels.length) {
            for (int i = 0; i < m_quantChannels.length; i++) {
                structureChanged = !(m_quantChannels[i].equals(quantChannels[i]));
            }
        }
        m_quantPeptides = peptides;
        m_quantChannels = quantChannels;
        m_quantChannelNumber = quantChannels.length;

        if (structureChanged) {
            fireTableStructureChanged();
        }

        m_taskId = taskId;

        fireTableDataChanged();

    }

    public void dataUpdated() {

        fireTableDataChanged();

    }

    public DMasterQuantPeptide getPeptide(int i) {

        return m_quantPeptides.get(i);
    }

    public int findRow(long peptideId) {

        int nb = m_quantPeptides.size();
        for (int i = 0; i < nb; i++) {
            if (peptideId == m_quantPeptides.get(i).getPeptideInstanceId()) {
                return i;
            }
        }
        return -1;

    }

    public void sortAccordingToModel(ArrayList<Long> peptideIds, CompoundTableModel compoundTableModel) {

        if (m_quantPeptides == null) {
            // data not loaded 
            return;
        }

        HashSet<Long> peptideIdMap = new HashSet<>(peptideIds.size());
        peptideIdMap.addAll(peptideIds);

        int nb = m_table.getRowCount();
        int iCur = 0;
        for (int iView = 0; iView < nb; iView++) {
            int iModel = m_table.convertRowIndexToModel(iView);
            if (compoundTableModel != null) {
                iModel = compoundTableModel.convertCompoundRowToBaseModelRow(iModel);
            }
            // Retrieve Peptide
            DMasterQuantPeptide p = getPeptide(iModel);
            if (peptideIdMap.contains(p.getPeptideInstanceId())) {
                peptideIds.set(iCur++, p.getPeptideInstanceId());
            }
        }

    }

    @Override
    public void addFilters(LinkedHashMap<Integer, Filter> filtersMap) {

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
        filtersMap.put(COLTYPE_PEPTIDE_PTM, new StringFilter(getColumnName(COLTYPE_PEPTIDE_PTM), null, COLTYPE_PEPTIDE_PTM));
        filtersMap.put(COLTYPE_PEPTIDE_SCORE, new DoubleFilter(getColumnName(COLTYPE_PEPTIDE_SCORE), null, COLTYPE_PEPTIDE_SCORE));
        filtersMap.put(COLTYPE_PEPTIDE_CHARGE, new IntegerFilter(getColumnName(COLTYPE_PEPTIDE_CHARGE), null, COLTYPE_PEPTIDE_CHARGE));
        filtersMap.put(COLTYPE_PEPTIDE_MOZ, new DoubleFilter(getColumnName(COLTYPE_PEPTIDE_MOZ), null, COLTYPE_PEPTIDE_MOZ));
        filtersMap.put(COLTYPE_PEPTIDE_PROTEIN_SET_COUNT, new IntegerFilter(getColumnName(COLTYPE_PEPTIDE_PROTEIN_SET_COUNT), null, COLTYPE_PEPTIDE_PROTEIN_SET_COUNT));
        filtersMap.put(COLTYPE_PEPTIDE_PROTEIN_SET_NAMES, new StringFilter(getColumnName(COLTYPE_PEPTIDE_PROTEIN_SET_NAMES), null, COLTYPE_PEPTIDE_PROTEIN_SET_NAMES));
        filtersMap.put(COLTYPE_PEPTIDE_CLUSTER, new StringFilter(getColumnName(COLTYPE_PEPTIDE_CLUSTER), null, COLTYPE_PEPTIDE_CLUSTER));
        int nbCol = getColumnCount();
        for (int i = LAST_STATIC_COLUMN + 1; i < nbCol; i++) {
            int nbQc;
            int id;
            if (m_isXICMode) {
                nbQc = (i - m_columnNames.length) / m_columnNamesQC.length;
                id = i - m_columnNames.length - (nbQc * m_columnNamesQC.length);
            } else {
                nbQc = (i - m_columnNames.length) / m_columnNamesQC_SC.length;
                id = i - m_columnNames.length - (nbQc * m_columnNamesQC_SC.length);
            }
            switch (id) {
                case COLTYPE_SELECTION_LEVEL:
                    filtersMap.put(i, new IntegerFilter(getColumnName(i), null, i));
                    break;
                case COLTYPE_ABUNDANCE:
                    filtersMap.put(i, new DoubleFilter(getColumnName(i), null, i));
                    break;
                case COLTYPE_RAW_ABUNDANCE:
                    filtersMap.put(i, new DoubleFilter(getColumnName(i), null, i));
                    break;
                case COLTYPE_PSM:
                    filtersMap.put(i, new IntegerFilter(getColumnName(i), null, i));
                    break;
                default:
                    filtersMap.put(i, new DoubleFilter(getColumnName(i), null, i));
                    break;
            }
        }
    }

    @Override
    public int getLoadingPercentage() {
        return m_table.getLoadingPercentage();
    }

    @Override
    public boolean isLoaded() {
        return m_table.isLoaded();
    }

    public int getByQCCount() {
        if (m_isXICMode) {
            return m_columnNamesQC.length;
        } else {
            return m_columnNamesQC_SC.length;
        }
    }

    public int getQCCount() {
        return m_quantChannels.length;
    }

    public int getColumStart(int index) {
        if (m_isXICMode) {
            return m_columnNames.length + index * m_columnNamesQC.length;
        } else {
            return m_columnNames.length + index * m_columnNamesQC_SC.length;
        }
    }

    public int getColumStop(int index) {
        if (m_isXICMode) {
            return m_columnNames.length + (1 + index) * m_columnNamesQC.length - 1;
        } else {
            return m_columnNames.length + (1 + index) * m_columnNamesQC_SC.length - 1;
        }
    }

    public String getQCName(int i) {

        StringBuilder sb = new StringBuilder();

        String rsmHtmlColor = CyclicColorPalette.getHTMLColor(i);
        sb.append("<html><font color='").append(rsmHtmlColor).append("'>&#x25A0;&nbsp;</font>");
        sb.append(m_quantChannels[i].getFullName());
        sb.append("</html>");

        return sb.toString();
    }

    public String getByQCMColumnName(int index) {
        return m_isXICMode ? m_columnNamesQC[index] : m_columnNamesQC_SC[index];
    }

    public int getQCNumber(int col) {
        if (m_isXICMode) {
            return (col - m_columnNames.length) / m_columnNamesQC.length;
        } else {
            return (col - m_columnNames.length) / m_columnNamesQC_SC.length;
        }
    }

    public int getTypeNumber(int col) {
        if (m_isXICMode) {
            return (col - m_columnNames.length) % m_columnNamesQC.length;
        } else {
            return (col - m_columnNames.length) % m_columnNamesQC_SC.length;
        }
    }

    public Long getResultSummaryId() {
        if ((m_quantPeptides == null) || (m_quantPeptides.size() == 0)) {
            return null;
        }

        return m_quantPeptides.get(0).getQuantResultSummaryId();
    }

    public void setOverviewType(int overviewType) {
        m_overviewType = overviewType;
        fireTableDataChanged();
    }

    public int getOverviewType() {
        return m_overviewType;
    }

    /**
     * by default the rawAbundance and selectionLevel are hidden return the list
     * of columns ids of these columns
     *
     * @return
     */
    public List<Integer> getDefaultColumnsToHide() {
        List<Integer> listIds = new ArrayList();
        if (m_quantChannels != null) {
            for (int i = m_quantChannels.length - 1; i >= 0; i--) {

                if (m_isXICMode) {
                    listIds.add(m_columnNames.length + COLTYPE_IDENT_PSM + (i * m_columnNamesQC.length));
                    listIds.add(m_columnNames.length + COLTYPE_RAW_ABUNDANCE + (i * m_columnNamesQC.length));
                } else {
                    listIds.add(m_columnNames.length + COLTYPE_IDENT_PSM + (i * m_columnNamesQC_SC.length));
                }

                if (m_isXICMode) {
                    listIds.add(m_columnNames.length + COLTYPE_SELECTION_LEVEL + (i * m_columnNamesQC.length));
                } else {
                    listIds.add(m_columnNames.length + COLTYPE_SELECTION_LEVEL + (i * m_columnNamesQC_SC.length));
                }

            }
        }
        if (!m_isXICMode) {
            listIds.add(COLTYPE_PEPTIDE_CLUSTER);
        }
        return listIds;
    }

    @Override
    public String getDataColumnIdentifier(int columnIndex) {
        if (columnIndex <= LAST_STATIC_COLUMN) {
            return m_columnNames[columnIndex];
        } else {
            int nbQc;
            int id;
            if (m_isXICMode) {
                nbQc = (columnIndex - m_columnNames.length) / m_columnNamesQC.length;
                id = columnIndex - m_columnNames.length - (nbQc * m_columnNamesQC.length);
            } else {
                nbQc = (columnIndex - m_columnNames.length) / m_columnNamesQC_SC.length;
                id = columnIndex - m_columnNames.length - (nbQc * m_columnNamesQC_SC.length);
            }

            StringBuilder sb = new StringBuilder();
            if (m_isXICMode) {
                sb.append(m_columnNamesQC[id]);
            } else {
                sb.append(m_columnNamesQC_SC[id]);
            }
            sb.append(' ');
            sb.append(m_quantChannels[nbQc].getName());

            return sb.toString();
        }
    }

    @Override
    public Class getDataColumnClass(int columnIndex) {
        switch (columnIndex) {
            case COLTYPE_PEPTIDE_ID:
            case COLTYPE_PEPTIDE_INST_ID: {
                return Long.class;
            }
            case COLTYPE_MQPEPTIDE_SELECTION_LEVEL: {
                if (m_isXICMode) {
                    return Boolean.class;
                } else {
                    return String.class;
                }
            }
            case COLTYPE_PEPTIDE_NAME: {
                return String.class;
            }
            case COLTYPE_PEPTIDE_PTM:
            case COLTYPE_PEPTIDE_PROTEIN_SET_NAMES: {
                return String.class;
            }
            case COLTYPE_PEPTIDE_CHARGE:
            case COLTYPE_PEPTIDE_PROTEIN_SET_COUNT: {
                return Integer.class;
            }
            case COLTYPE_PEPTIDE_MOZ: {
                return Double.class;
            }
            case COLTYPE_PEPTIDE_SCORE: {
                return Float.class;
            }
            case COLTYPE_OVERVIEW: {
                return CompareValueRenderer.CompareValue.class;
            }
            case COLTYPE_PEPTIDE_CLUSTER: {
                return String.class;
            }
            case COLTYPE_PEPTIDE_RETENTION_TIME: {
                return Float.class;
            }
            default: {
                int nbQc;
                int id;
                if (m_isXICMode) {
                    nbQc = (columnIndex - m_columnNames.length) / m_columnNamesQC.length;
                    id = columnIndex - m_columnNames.length - (nbQc * m_columnNamesQC.length);
                } else {
                    nbQc = (columnIndex - m_columnNames.length) / m_columnNamesQC_SC.length;
                    id = columnIndex - m_columnNames.length - (nbQc * m_columnNamesQC_SC.length);
                }
                switch (id) {
                    case COLTYPE_SELECTION_LEVEL:
                        return Integer.class;
                    case COLTYPE_ABUNDANCE:
                        return Float.class;
                    case COLTYPE_RAW_ABUNDANCE:
                        return Float.class;
                    case COLTYPE_PSM:
                    case COLTYPE_IDENT_PSM:
                        return Integer.class;
                }

            }
        }
        return null; // should never happen

    }

    @Override
    public Object getDataValueAt(int rowIndex, int columnIndex) {
        Object data = getValueAt(rowIndex, columnIndex);
        if (data instanceof LazyData) {
            data = ((LazyData) data).getData();
            if (data instanceof DPeptideMatch) {
                data = ((DPeptideMatch) data).getPeptide().getSequence();
            }
        }

        DMasterQuantPeptide peptide = m_quantPeptides.get(rowIndex);

        if (columnIndex == COLTYPE_PEPTIDE_ID) {
            return peptide.getId();
        }
        return data;
    }

    @Override
    public int[] getKeysColumn() {
        int[] keys = {COLTYPE_PEPTIDE_NAME};
        return keys;
    }

    @Override
    public int getInfoColumn() {
        return COLTYPE_PEPTIDE_NAME;
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
    public Map<String, Object> getExternalData() {
        return null;
    }

    @Override
    public PlotInformation getPlotInformation() {
        return null;
    }

    @Override
    public PlotType getBestPlotType() {
        return null; //JPM.TODO
    }

    @Override
    public int[] getBestColIndex(PlotType plotType) {
        return null;
    }

    @Override
    public TableCellRenderer getRenderer(int row, int col) {

        Boolean peptideSelected = (m_isXICMode) ? (Boolean) getValueAt(row, COLTYPE_MQPEPTIDE_SELECTION_LEVEL) : true;
        boolean grayed = !peptideSelected;

        if (grayed) {
            if (m_rendererMapGrayed.containsKey(col)) {
                return m_rendererMapGrayed.get(col);
            }
        } else {
            if (m_rendererMap.containsKey(col)) {
                return m_rendererMap.get(col);
            }
        }

        TableCellRenderer renderer = null;

        switch (col) {
            case COLTYPE_PEPTIDE_NAME: {
                renderer = new PeptideRenderer();
                break;
            }
            case COLTYPE_PEPTIDE_SCORE: {
                renderer = m_scoreRenderer;
                break;
            }
            case COLTYPE_PEPTIDE_CHARGE: {
                renderer = new DefaultRightAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(Integer.class));
                break;
            }
            case COLTYPE_PEPTIDE_MOZ: {
                renderer = new DoubleRenderer(new DefaultRightAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class)), 4);
                break;
            }
            case COLTYPE_PEPTIDE_RETENTION_TIME: {
                renderer = new TimeRenderer(new DefaultRightAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class)));
                break;
            }
            case COLTYPE_PEPTIDE_PROTEIN_SET_COUNT: {
                renderer = new DefaultRightAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class));
                break;
            }
            case COLTYPE_PEPTIDE_PTM:
            case COLTYPE_PEPTIDE_CLUSTER: {
                renderer = new DefaultLeftAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class));
                break;
            }
            case COLTYPE_OVERVIEW: {
                renderer = new CompareValueRenderer();
                break;
            }
            default: {
                int nbQc;
                int id;
                if (m_isXICMode) {
                    nbQc = (col - m_columnNames.length) / m_columnNamesQC.length;
                    id = col - m_columnNames.length - (nbQc * m_columnNamesQC.length);
                } else {
                    nbQc = (col - m_columnNames.length) / m_columnNamesQC_SC.length;
                    id = col - m_columnNames.length - (nbQc * m_columnNamesQC_SC.length);
                }
                switch (id) {
                    case COLTYPE_SELECTION_LEVEL:
                    case COLTYPE_PSM:
                    case COLTYPE_IDENT_PSM: {
                        renderer = new DefaultRightAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(Integer.class));
                        break;
                    }
                    case COLTYPE_ABUNDANCE: {
                        if (m_isXICMode) {
                            renderer = new BigFloatOrDoubleRenderer(new DefaultRightAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class)), 0);
                        } else {
                            renderer = new FloatRenderer(new DefaultRightAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class)), 2);
                        }
                        break;
                    }
                    case COLTYPE_RAW_ABUNDANCE: {
                        if (m_isXICMode) {
                            renderer = new BigFloatOrDoubleRenderer(new DefaultRightAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class)), 0);
                        } else {
                            renderer = new FloatRenderer(new DefaultRightAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class)), 0);
                        }
                        break;
                    }

                }

            }
        }

        if (grayed) {
            if (renderer == null) {
                return null;
            }
            renderer = new GrayedRenderer(renderer);
            m_rendererMapGrayed.put(col, renderer);
        } else {
            m_rendererMap.put(col, renderer);
        }

        return renderer;
    }
    private final HashMap<Integer, TableCellRenderer> m_rendererMap = new HashMap();
    private final HashMap<Integer, TableCellRenderer> m_rendererMapGrayed = new HashMap();

    @Override
    public GlobalTableModelInterface getFrozzenModel() {
        return this;
    }

    @Override
    public ArrayList<ExtraDataType> getExtraDataTypes() {
        ArrayList<ExtraDataType> list = new ArrayList<>();
        list.add(new ExtraDataType(DMasterQuantPeptide.class, true));
        list.add(new ExtraDataType(ResultSummary.class, false));
        list.add(new ExtraDataType(DDataset.class, false));
        list.add(new ExtraDataType(QuantChannelInfo.class, false));
        list.add(new ExtraDataType(XicMode.class, false));
        registerSingleValuesAsExtraTypes(list);
        return list;
    }

    @Override
    public Object getValue(Class c) {
        return getSingleValue(c);
    }

    @Override
    public Object getRowValue(Class c, int row) {
        if (c.equals(DMasterQuantPeptide.class)) {
            return m_quantPeptides.get(row);
        }
        return null;
    }

    @Override
    public Object getColValue(Class c, int col) {
        if (c.equals(XicGroup.class)) {
            if (col <= LAST_STATIC_COLUMN) {
                return null;
            } else {
                int nbQc;
                if (m_isXICMode) {
                    nbQc = (col - m_columnNames.length) / m_columnNamesQC.length;
                } else {
                    nbQc = (col - m_columnNames.length) / m_columnNamesQC_SC.length;
                }
                return new XicGroup(m_quantChannels[nbQc].getBiologicalGroupId(), null); //biologicalGroupName.getBiologicalGroupName(); JPM.TODO
            }

        }
        if (c.equals(QuantitationType.class)) {
            if (col <= LAST_STATIC_COLUMN) {
                return null;
            } else {
                int nbQc;
                int id;
                if (m_isXICMode) {
                    nbQc = (col - m_columnNames.length) / m_columnNamesQC.length;
                    id = col - m_columnNames.length - (nbQc * m_columnNamesQC.length);
                } else {
                    nbQc = (col - m_columnNames.length) / m_columnNamesQC_SC.length;
                    id = col - m_columnNames.length - (nbQc * m_columnNamesQC_SC.length);
                }
                if (m_isXICMode) {
                    switch (id) {
                        case COLTYPE_ABUNDANCE:
                            return QuantitationType.getQuantitationType(QuantitationType.ABUNDANCE);
                        case COLTYPE_RAW_ABUNDANCE:
                            return QuantitationType.getQuantitationType(QuantitationType.RAW_ABUNDANCE);
                    }
                } else {
                    switch (id) {
                        case COLTYPE_PSM:
                            return QuantitationType.getQuantitationType(QuantitationType.BASIC_SC);
                        case COLTYPE_RAW_ABUNDANCE:
                            return QuantitationType.getQuantitationType(QuantitationType.SPECIFIC_SC);
                        case COLTYPE_ABUNDANCE:
                            return QuantitationType.getQuantitationType(QuantitationType.WEIGHTED_SC);
                    }
                }

                return null;
            }

        }
        return null;
    }

    
}
