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

import fr.proline.core.orm.msi.dto.DProteinMatch;
import fr.proline.core.orm.msi.dto.DBioSequence;
import fr.proline.studio.extendedtablemodel.ExtraDataType;
import fr.proline.studio.export.ExportModelUtilities;
import fr.proline.studio.export.ExportFontData;
import fr.proline.studio.filter.*;
import fr.proline.studio.graphics.PlotInformation;
import fr.proline.studio.graphics.PlotType;
import fr.proline.studio.progress.ProgressInterface;
import fr.proline.studio.table.renderer.DefaultAlignRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.FloatRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.SamesetRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.ScoreRenderer;
import fr.proline.studio.table.DecoratedTableModel;
import fr.proline.studio.extendedtablemodel.GlobalTableModelInterface;
import fr.proline.studio.table.LazyData;
import fr.proline.studio.table.TableDefaultRendererManager;
import fr.proline.studio.utils.IconManager;
import fr.proline.studio.utils.URLCellRenderer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.table.TableCellRenderer;

/**
 * Table Model for Proteins
 * @author JM235353
 */
public class ProteinTableModel extends DecoratedTableModel implements GlobalTableModelInterface {

    public enum Column {
        PROTEIN_ID("Id", Long.class, 0),
        PROTEIN_NAME("Protein", String.class, 1),
        PROTEIN_DESCRIPTION("Description", String.class, 2),
        SAMESET_SUBSET("Sameset / Subset", Sameset.class, 3),
        PROTEIN_SCORE("Score", Float.class, 4),
        PROTEIN_PEPTIDES_COUNT("Peptides", Integer.class, 5),
        OBSERVABLE_PEPTIDES("Observable Peptides", Integer.class, 6),
        PROTEIN_UNIQUE_PEPTIDE_SEQUENCES_COUNT("Sequence Count", Integer.class, 7),
        PROTEIN_MASS("Mass", Float.class, 8);
        
        String m_name; 
        Class m_class;
        int m_colId;
        
        Column(String n, Class cl, int colId) {
            m_name = n; 
            m_class = cl;
            m_colId = colId;
        }
    }
     
    private DProteinMatch[] m_sameSetMatches = null;
    private DProteinMatch[] m_subSetMatches = null;
    private Long m_rsmId = null;
    private long m_representativeProteinMatchId;


    private ProgressInterface m_progressInterface = null;

    private ScoreRenderer m_scoreRenderer = new ScoreRenderer();
    
    private String m_modelName;
    
    public ProteinTableModel(ProgressInterface progressInterface) {
        m_progressInterface = progressInterface;
    }

    public DProteinMatch getProteinMatch(int row) {


        int sameSetNb = m_sameSetMatches.length;
        if (row < sameSetNb) {
            return m_sameSetMatches[row];
        }
        return m_subSetMatches[row - sameSetNb];
    }

    @Override
    public int getColumnCount() {
        return Column.values().length;
    }

    @Override
    public String getColumnName(int col) {
        return Column.values()[col].m_name;
    }

    @Override
    public String getToolTipForHeader(int col) {

        if (col == Column.SAMESET_SUBSET.ordinal()) {
            String urlTypical = IconManager.getURLForIcon(IconManager.IconType.TYPICAL);
            String urlSameset = IconManager.getURLForIcon(IconManager.IconType.SAME_SET);
            String urlSubset = IconManager.getURLForIcon(IconManager.IconType.SUB_SET);
            return "<html><img src=\"" + urlTypical + "\">&nbsp;Typical Protein in Sameset<br><img src=\"" + urlSameset + "\">&nbsp;Sameset<br><img src=\"" + urlSubset + "\">&nbsp;Subset</html>";
        } else {
            return getColumnName(col);
        }

    }
    
    @Override
    public String getTootlTipValue(int row, int col) {
        return null;
    }


    @Override
    public Class getColumnClass(int col) {
        return Column.values()[col].m_class;
    }

    @Override
    public int getRowCount() {
        if (m_sameSetMatches == null) {
            return 0;
        }


        return m_sameSetMatches.length + m_subSetMatches.length;
    }

    @Override
    public Object getValueAt(int row, int col) {

        // Retrieve Protein Match
        DProteinMatch proteinMatch = getProteinMatch(row);

        switch (Column.values()[col]) {
            case PROTEIN_ID:
                return proteinMatch.getId();
            case PROTEIN_NAME:
                return proteinMatch.getAccession();
            case PROTEIN_DESCRIPTION:
                return proteinMatch.getDescription();
            case SAMESET_SUBSET:
                if (proteinMatch.getId() == m_representativeProteinMatchId) {
                    return Sameset.getSameset(Sameset.TYPICAL_SAMESET);
                } else if (row < m_sameSetMatches.length) {
                    return Sameset.getSameset(Sameset.SAMESET);
                } else {
                    return Sameset.getSameset(Sameset.SUBSET);
                }
            case PROTEIN_SCORE:
                Float score = Float.valueOf(proteinMatch.getPeptideSet(m_rsmId).getScore());
                return score;
            case OBSERVABLE_PEPTIDES:
                Integer observablePeptideCount = proteinMatch.getObservablePeptidesCount();
                return observablePeptideCount;
            case PROTEIN_PEPTIDES_COUNT:
                return proteinMatch.getPeptideSet(m_rsmId).getPeptideCount();
            case PROTEIN_UNIQUE_PEPTIDE_SEQUENCES_COUNT: 
                try { 
                    return ((Integer)proteinMatch.getPeptideSet(m_rsmId).getSequenceCount());
                } catch (Exception e) { return null;}
            case PROTEIN_MASS:
                DBioSequence bioSequence = proteinMatch.getDBioSequence();
                if (bioSequence != null) {
                    return Float.valueOf((float) bioSequence.getMass());
                }
                return null;
        }
        return null; // should never happen
    }

    public void setData(long rsmId, long representativeProteinMatchId, DProteinMatch[] sameSetMatches, DProteinMatch[] subSetMatches) {
        m_rsmId = rsmId;
        m_representativeProteinMatchId = representativeProteinMatchId;
        m_sameSetMatches = sameSetMatches;
        m_subSetMatches = subSetMatches;

        fireTableDataChanged();


    }

    public int findRowToSelect(String searchedText) {

        //JPM.TODO ?

        int rowToBeSelected = 0;
        if ((searchedText != null) && (searchedText.length() > 0)) {

            String searchedTextUpper = searchedText.toUpperCase();

            for (int row = 0; row < getRowCount(); row++) {
                String proteinName = ((String) getValueAt(row, Column.PROTEIN_NAME.ordinal())).toUpperCase();
                if (proteinName.indexOf(searchedTextUpper) != -1) {
                    rowToBeSelected = row;
                }
            }
        }

        return rowToBeSelected;
    }


    @Override
    public void addFilters(LinkedHashMap<Integer, Filter> filtersMap) {

        filtersMap.put(Column.PROTEIN_NAME.ordinal(), new StringDiffFilter(getColumnName(Column.PROTEIN_NAME.ordinal()), null, Column.PROTEIN_NAME.ordinal()));
        filtersMap.put(Column.PROTEIN_DESCRIPTION.ordinal(), new StringDiffFilter(getColumnName(Column.PROTEIN_DESCRIPTION.ordinal()), null, Column.PROTEIN_DESCRIPTION.ordinal()));
        filtersMap.put(Column.PROTEIN_SCORE.ordinal(), new DoubleFilter(getColumnName(Column.PROTEIN_SCORE.ordinal()), null, Column.PROTEIN_SCORE.ordinal()));
        filtersMap.put(Column.OBSERVABLE_PEPTIDES.ordinal(), new IntegerFilter(getColumnName(Column.OBSERVABLE_PEPTIDES.ordinal()), null, Column.OBSERVABLE_PEPTIDES.ordinal()));
        filtersMap.put(Column.PROTEIN_PEPTIDES_COUNT.ordinal(), new IntegerFilter(getColumnName(Column.PROTEIN_PEPTIDES_COUNT.ordinal()), null, Column.PROTEIN_PEPTIDES_COUNT.ordinal()));
        filtersMap.put(Column.PROTEIN_UNIQUE_PEPTIDE_SEQUENCES_COUNT.ordinal(), new IntegerFilter(getColumnName(Column.PROTEIN_UNIQUE_PEPTIDE_SEQUENCES_COUNT.ordinal()), null, Column.PROTEIN_UNIQUE_PEPTIDE_SEQUENCES_COUNT.ordinal()));
        filtersMap.put(Column.PROTEIN_MASS.ordinal(), new DoubleFilter(getColumnName(Column.PROTEIN_MASS.ordinal()), null, Column.PROTEIN_MASS.ordinal()));

    }

    @Override
    public int getLoadingPercentage() {
        return m_progressInterface.getLoadingPercentage();
    }

    @Override
    public boolean isLoaded() {
        return m_progressInterface.isLoaded();
    }
    
        @Override
    public String getDataColumnIdentifier(int columnIndex) {
        return getColumnName(columnIndex);
    }

    @Override
    public Class getDataColumnClass(int columnIndex) {
        
        if (Column.values()[columnIndex].equals(Column.SAMESET_SUBSET)) {
            return String.class;
        }

        return getColumnClass(columnIndex);
    }

    @Override
    public Object getDataValueAt(int rowIndex, int columnIndex) {
        if (Column.values()[columnIndex].equals(Column.SAMESET_SUBSET)) {
           return ((Sameset) getValueAt(rowIndex, columnIndex)).toString();
        }
        return getValueAt(rowIndex, columnIndex);
    }

    @Override
    public int[] getKeysColumn() {
        int[] keys = { Column.PROTEIN_NAME.m_colId, Column.PROTEIN_ID.m_colId };
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
        return Column.PROTEIN_NAME.m_colId;
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
        return -1L; // not needed
    }

    @Override
    public LazyData getLazyData(int row, int col) {
        return null; // not needed
    }

    @Override
    public void givePriorityTo(Long taskId, int row, int col) {
        // not needed
    }

    @Override
    public void sortingChanged(int col) {
        // not needed
    }

    @Override
    public int getSubTaskId(int col) {
        return -1; // not needed
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
    public String getExportRowCell(int row, int col) {
        switch (Column.values()[col]) {
            case SAMESET_SUBSET: {
                DProteinMatch proteinMatch = getProteinMatch(row);
                if (proteinMatch.getId() == m_representativeProteinMatchId) {
                    return Sameset.getSameset(Sameset.TYPICAL_SAMESET).toString();
                } else if (row < m_sameSetMatches.length) {
                    return Sameset.getSameset(Sameset.SAMESET).toString();
                } else {
                    return Sameset.getSameset(Sameset.SUBSET).toString();
                }
            }

        }
        return ExportModelUtilities.getExportRowCell(this, row, col);
    }
    
    @Override
    public ArrayList<ExportFontData> getExportFonts(int row, int col) {
        return null;
    }

    @Override
    public String getExportColumnName(int col) {
        return getColumnName(col);
    }

    
    public static class Sameset {
        
        private static final int TYPICAL_SAMESET  = 0;
        private static final int SAMESET  = 1;
        private static final int SUBSET  = 2;

        private int m_type;
        
        private static Sameset[] samesetArray = { new Sameset(TYPICAL_SAMESET), new Sameset(SAMESET), new Sameset(SUBSET) };
        
        private Sameset(int type) {
            m_type = type;
        }
        
        public static Sameset getSameset(int type) {
            return samesetArray[type];
        }
        
        public boolean isTypical() {
            return (m_type == TYPICAL_SAMESET);
        }
        
        public boolean isSameset() {
            return (m_type == SAMESET);
        }
        
        @Override
        public String toString() {
            if (isTypical()) {
                return "typical";
            } else if (isSameset()) {
                return "sameset";
            } else {
                return "subset";
            }
        }
    }
    
    @Override
    public TableCellRenderer getRenderer(int row, int col) {

        if (m_rendererMap.containsKey(col)) {
            return m_rendererMap.get(col);
        }
        
        TableCellRenderer renderer = null;
        
        switch (Column.values()[col]) {

            case PROTEIN_NAME: {
                renderer =  new URLCellRenderer("URL_Template_Protein_Accession", "http://www.uniprot.org/uniprot/", ProteinTableModel.Column.PROTEIN_NAME.ordinal());
                break;
            }
            case PROTEIN_DESCRIPTION: {
                renderer = new DefaultAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class), JLabel.LEFT);
                break;
            }
            case SAMESET_SUBSET: {
                renderer = new SamesetRenderer();
                break;
            }
            case PROTEIN_SCORE: {
                renderer = m_scoreRenderer;
                break;
            }
            case PROTEIN_MASS: {
                renderer = new FloatRenderer(new DefaultAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class), JLabel.RIGHT));
                break;
            }
            case OBSERVABLE_PEPTIDES:
            case PROTEIN_PEPTIDES_COUNT:
            case PROTEIN_UNIQUE_PEPTIDE_SEQUENCES_COUNT: {
                renderer = new DefaultAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(Integer.class), JLabel.RIGHT);
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
        list.add(new ExtraDataType(DProteinMatch.class, true));
        registerSingleValuesAsExtraTypes(list);
        return list;
    }

    @Override
    public Object getValue(Class c) {
        return getSingleValue(c);
    }

    @Override
    public Object getRowValue(Class c, int row) {
        if (c.equals(DProteinMatch.class)) {
            return getProteinMatch(row);
        }
        return null;
    }
    
    @Override
    public Object getColValue(Class c, int col) {
        return null;
    }
}
