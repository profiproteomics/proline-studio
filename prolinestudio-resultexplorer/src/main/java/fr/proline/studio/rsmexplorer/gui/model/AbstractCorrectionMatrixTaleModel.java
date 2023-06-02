package fr.proline.studio.rsmexplorer.gui.model;

import fr.proline.studio.rsmexplorer.gui.renderer.FloatRenderer;
import fr.proline.studio.table.DecoratedTableModel;
import fr.proline.studio.table.TableDefaultRendererManager;
import fr.proline.studio.table.renderer.DefaultAlignRenderer;
import fr.proline.studio.table.renderer.DefaultColoredCellRenderer;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractCorrectionMatrixTaleModel extends DecoratedTableModel {

    protected List<String> columnNames;
    List<MassReporter> massReporters;


    protected int valueCoeffColumWidth = 50;

    //Known TMT methods
    public final static String TMT_6PLEX_METHOD = "TMT 6plex";
    public final static String TMT_10PLEX_METHOD = "TMT 10plex";
    public final static String TMT_11PLEX_METHOD = "TMT 11plex";
    public final static String TMT_16PLEX_METHOD = "TMT 16plex";
    public final static String TMT_18PLEX_METHOD = "TMT 18plex";


    public int getCenterColIndex(){
        return (columnNames.size()/2);
    }

    public int getCoeffColumWidth() {
        return valueCoeffColumWidth;
    }

    @Override
    public String getToolTipForHeader(int col) {
        if(col<columnNames.size())
            return columnNames.get(col);
        return "";
    }


    @Override
    public String getTootlTipValue(int row, int col) {
        return null;
    }

    protected int getCoeffIndex(int colIndex){
        int coefIndex;
        if(colIndex ==0 ||colIndex == getCenterColIndex() ||colIndex>=getColumnCount())
            return -1;
        if(colIndex< getCenterColIndex()) { //value in -2 or -1 columns
            coefIndex = colIndex - 1;//remove name column
        } else {
            coefIndex = colIndex - 2;//remove name column + separator column
        }
        return coefIndex;
    }

    public boolean isCellEditable(int row, int col) {
        MassReporter mr = massReporters.get(row);
        int coefIndex = getCoeffIndex(col);
        if(coefIndex == -1)
            return false;

        return mr.isCoeffApplicable(coefIndex);
    }

    @Override
    public String getColumnName(int column) {
        if(column> columnNames.size())
            return null;
        return columnNames.get(column);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if(columnIndex==0 || columnIndex== getCenterColIndex())
            return String.class;
        return Float.class;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        MassReporter mr = massReporters.get(rowIndex);

        if(columnIndex ==0 ||columnIndex == getCenterColIndex())
            return;
        try {
            Float newValue = Float.valueOf(aValue.toString());

            int coefIndex = getCoeffIndex(columnIndex);
            if(coefIndex ==-1)
                return;
            mr.coef.set(coefIndex, newValue);
        } catch (NumberFormatException nfe){
            nfe.printStackTrace();
        }
    }

    @Override
    public TableCellRenderer getRenderer(int row, int col) {
        TableCellRenderer renderer;
        if(col == 0){
            renderer = new DefaultAlignRenderer(new DefaultColoredCellRenderer(HighlighterFactory.GENERIC_GRAY), JLabel.LEFT);
        } else if (col ==getCenterColIndex()) {
            renderer = new DefaultAlignRenderer(new DefaultColoredCellRenderer(HighlighterFactory.GENERIC_GRAY), JLabel.RIGHT);
        } else {
            MassReporter mr = massReporters.get(row);
            int coefIndex = getCoeffIndex(col);
            if (coefIndex == -1) {
                renderer = new DefaultAlignRenderer(new DefaultColoredCellRenderer(HighlighterFactory.GENERIC_GRAY), JLabel.LEFT);
            } else {
                if (mr.isCoeffApplicable(coefIndex)) {
                    renderer = new FloatRenderer(new DefaultAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class), JLabel.RIGHT), 2);
                } else {
                    renderer = new DefaultColoredCellRenderer(new Color(255, 242, 204));
                }
            }
        }
        return renderer;
    }

    @Override
    public int getRowCount() {
        return massReporters.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        MassReporter mr = massReporters.get(rowIndex);
        if(columnIndex ==0)
            return mr.name;
        if(columnIndex == getCenterColIndex())
            return "100";

        Float coef = 0.0f;
        int coefIndex = getCoeffIndex(columnIndex);

        if(mr.coef.size()>coefIndex && coefIndex!=-1)
            coef= mr.coef.get(coefIndex); //remove name column
        return coef;
    }

    public abstract String getPurityMatrixAsString();


    static class MassReporter {

        String name;
        int correctionCount;
        List<Float> coef;

        public MassReporter(String name, int correctionCount, int[] indexes) {
            this.name = name;
            this.correctionCount = correctionCount;
            this.coef = new ArrayList<>(correctionCount);
            for (int i=0; i<correctionCount; i++){
                int finalI = i;
                if(Arrays.stream(indexes).anyMatch(index -> index == finalI))
                    this.coef.add(Float.NaN);
                else
                    this.coef.add(0.0f);
            }
        }

        public List<Float> getCoefWithoutNan(){
            return coef.stream()
                    .map(value -> Float.isNaN(value) ? 0.0f : value)
                    .collect(Collectors.toList());
        }

        public boolean isCoeffApplicable(int index){
            if(index>=correctionCount)
                return false;
            return !this.coef.get(index).isNaN();
        }

        public Float getCoeffSum(){
            return coef.stream().filter(value -> !Float.isNaN(value)).reduce(0.0f, Float::sum);
        }

    }
}
