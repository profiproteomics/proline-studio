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
import fr.proline.studio.table.LazyTable;
import fr.proline.studio.export.ExportModelUtilities;
import fr.proline.studio.export.ExportFontData;
import fr.proline.studio.filter.DoubleFilter;
import fr.proline.studio.filter.Filter;
import fr.proline.studio.filter.IntegerFilter;
import fr.proline.studio.filter.StringDiffFilter;
import fr.proline.studio.graphics.PlotInformation;
import fr.proline.studio.graphics.PlotType;
import fr.proline.studio.table.renderer.DefaultAlignRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.FloatRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.ScoreRenderer;
import fr.proline.studio.extendedtablemodel.GlobalTableModelInterface;
import fr.proline.studio.table.DecoratedTableModel;
import fr.proline.studio.table.LazyData;
import fr.proline.studio.table.TableDefaultRendererManager;
import fr.proline.studio.utils.URLCellRenderer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.HashMap;
import javax.swing.*;
import javax.swing.table.TableCellRenderer;

/**
 * Table Model for Peptide Matches
 * @author JM235353
 */
public class ProteinsOfPeptideMatchTableModel extends DecoratedTableModel implements GlobalTableModelInterface {

    public static final int COLTYPE_PROTEIN_ID             = 0;
    public static final int COLTYPE_PROTEIN_NAME           = 1;
    public static final int COLTYPE_PROTEIN_SCORE          = 2;
    public static final int COLTYPE_PROTEIN_PEPTIDES_COUNT = 3;
    public static final int COLTYPE_PROTEIN_MASS           = 4;
    private static final String[] m_columnNames = {"Id","Protein", "Score", "Peptides", "Mass"};
    
    private DProteinMatch[] m_proteinMatchArray = null;

    private String m_modelName;
    
    private ScoreRenderer m_scoreRenderer = new ScoreRenderer();
    
    public ProteinsOfPeptideMatchTableModel(LazyTable table) {

    }
    
    public DProteinMatch getProteinMatch(int row) {

        return m_proteinMatchArray[row];
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
        return getColumnName(col);
    }
    
    @Override
    public String getTootlTipValue(int row, int col) {
        return null;
    }
    
    @Override
    public Class getColumnClass(int col) {
        switch (col) {
            case COLTYPE_PROTEIN_ID:
                return Long.class;
            case COLTYPE_PROTEIN_NAME:
                return String.class;
            case COLTYPE_PROTEIN_SCORE:
                return Float.class;
            case COLTYPE_PROTEIN_PEPTIDES_COUNT:
                return Integer.class;
            case COLTYPE_PROTEIN_MASS:
                return Float.class;
        }
        return null;
    }

    @Override
    public int getRowCount() {
        if (m_proteinMatchArray == null) {
            return 0;
        }

        return m_proteinMatchArray.length;
    }

    @Override
    public Object getValueAt(int row, int col) {

        DProteinMatch proteinMatch = m_proteinMatchArray[row];

        switch (col) {
            case COLTYPE_PROTEIN_ID:
                return proteinMatch.getId();
            case COLTYPE_PROTEIN_NAME:
                return proteinMatch.getAccession();
            case COLTYPE_PROTEIN_SCORE:
                return proteinMatch.getScore();
            case COLTYPE_PROTEIN_PEPTIDES_COUNT:
                return proteinMatch.getPeptideCount();
            case COLTYPE_PROTEIN_MASS:               
                DBioSequence bioSequence = proteinMatch.getDBioSequence();
                if (bioSequence != null) {                    
                    return Float.valueOf((float) bioSequence.getMass());
                } else 
                    return Float.NaN; // unknown
            }
                                        
        return null; // should never happen
    }

    public void setData(DProteinMatch[] proteinMatchArray) {
        m_proteinMatchArray = proteinMatchArray;
       
        updateMinMax();

        fireTableDataChanged();

    }
    
    private void updateMinMax() {
        
        if (m_proteinMatchArray == null) {
            return;
        }

        if (m_scoreRenderer == null) {
            return;
        }

        float maxScore = 0;
        int size = m_proteinMatchArray.length;
        for (int i = 0; i < size; i++) {
            DProteinMatch proteinMatch = m_proteinMatchArray[i];
            float score = proteinMatch.getScore();
            if (score > maxScore) {
                maxScore = score;
            }
        }
        m_scoreRenderer.setMaxValue(maxScore);

    }
    
    /*public int findRowToSelect(String searchedText) {
        int rowToBeSelected = 0;
        if ((searchedText != null) && (searchedText.length() > 0)) {

            String searchedTextUpper = searchedText.toUpperCase();

            for (int row = 0; row < getRowCount(); row++) {
                String proteinName = ((String) getValueAt(row, COLTYPE_PROTEIN_NAME)).toUpperCase();
                if (proteinName.indexOf(searchedTextUpper) != -1) {
                    rowToBeSelected = row;
                }
            }
        }

        return rowToBeSelected;
    }*/

    public int findRow(long proteinMatchId) {

        int nb = m_proteinMatchArray.length;
        for (int i = 0; i < nb; i++) {
            if (proteinMatchId == m_proteinMatchArray[i].getId()) {
                return i;
            }
        }
        return -1;

    }
    

    
    public void dataUpdated() {
        // no need to do an updateMinMax : scores are known at once

        fireTableDataChanged();

    }

    
//    public void sortAccordingToModel(ArrayList<Long> proteinMatchIds, CompoundTableModel compoundTableModel) {
//        
//        if (m_proteinMatchArray == null) {
//            // data not loaded 
//            return;
//        }
//        
//        HashSet<Long> proteinMatchIdMap = new HashSet<>(proteinMatchIds.size());
//        proteinMatchIdMap.addAll(proteinMatchIds);
//
//        int nb = m_table.getRowCount();
//        int iCur = 0;
//        for (int iView = 0; iView < nb; iView++) {
//            int iModel = m_table.convertRowIndexToModel(iView);
//            if (compoundTableModel != null) {
//                iModel = compoundTableModel.convertCompoundRowToBaseModelRow(iModel);
//            }
//            DProteinMatch pm = m_proteinMatchArray[iModel];
//            if (proteinMatchIdMap.contains(pm.getId())) {
//                proteinMatchIds.set(iCur++, pm.getId());
//            }
//        }
//
//    }

    

    @Override
    public void addFilters(LinkedHashMap<Integer, Filter> filtersMap) {

        filtersMap.put(COLTYPE_PROTEIN_NAME, new StringDiffFilter(getColumnName(COLTYPE_PROTEIN_NAME), null, COLTYPE_PROTEIN_NAME));
        filtersMap.put(COLTYPE_PROTEIN_SCORE, new DoubleFilter(getColumnName(COLTYPE_PROTEIN_SCORE), null, COLTYPE_PROTEIN_SCORE));
        filtersMap.put(COLTYPE_PROTEIN_PEPTIDES_COUNT, new IntegerFilter(getColumnName(COLTYPE_PROTEIN_PEPTIDES_COUNT), null, COLTYPE_PROTEIN_PEPTIDES_COUNT));
        filtersMap.put(COLTYPE_PROTEIN_MASS, new DoubleFilter(getColumnName(COLTYPE_PROTEIN_MASS), null, COLTYPE_PROTEIN_MASS));

    }

    @Override
    public boolean isLoaded() {
        return true;
    }

    @Override
    public int getLoadingPercentage() {
        return 100;
    }

    @Override
    public String getDataColumnIdentifier(int columnIndex) {
        return getColumnName(columnIndex);
    }

    @Override
    public Class getDataColumnClass(int columnIndex) {
        return getColumnClass(columnIndex);
    }

    @Override
    public Object getDataValueAt(int rowIndex, int columnIndex) {
        return getValueAt(rowIndex, columnIndex);
    }

    @Override
    public int[] getKeysColumn() {
        int[] keys = { COLTYPE_PROTEIN_NAME, COLTYPE_PROTEIN_ID };
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
        return COLTYPE_PROTEIN_NAME;
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
    public String getExportRowCell(int row, int col) {
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

    @Override
    public TableCellRenderer getRenderer(int row, int col) {

        if (m_rendererMap.containsKey(col)) {
            return m_rendererMap.get(col);
        }
        
        TableCellRenderer renderer = null;
        switch (col) {
            case COLTYPE_PROTEIN_NAME: {
                renderer = new URLCellRenderer("URL_Template_Protein_Accession", "http://www.uniprot.org/uniprot/", COLTYPE_PROTEIN_NAME);
                break;
            }
            case COLTYPE_PROTEIN_SCORE: {
                renderer = m_scoreRenderer;
                break;
            }
            case COLTYPE_PROTEIN_MASS: {
                renderer = new FloatRenderer( new DefaultAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class) , JLabel.RIGHT));
                break;
            }
            case COLTYPE_PROTEIN_PEPTIDES_COUNT: {
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
            return m_proteinMatchArray[row];
        }
        return null;
    }
    
    @Override
    public Object getColValue(Class c, int col) {
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
}
