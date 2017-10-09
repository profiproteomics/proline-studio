package fr.proline.studio.graphics;

import fr.proline.studio.comparedata.CompareDataInterface;
import fr.proline.studio.graphics.marker.LabelMarker;
import fr.proline.studio.graphics.marker.coordinates.PercentageCoordinates;
import fr.proline.studio.graphics.venndiagram.Circle;
import fr.proline.studio.graphics.venndiagram.IntersectArea;
import fr.proline.studio.graphics.venndiagram.Set;
import fr.proline.studio.graphics.venndiagram.SetList;
import fr.proline.studio.parameter.ColorParameter;
import fr.proline.studio.parameter.IntegerParameter;
import fr.proline.studio.parameter.ParameterList;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JSlider;
import javax.swing.JSpinner;

/**
 *
 * @author JM235353
 */
public class PlotVennDiagram extends PlotMultiDataAbstract {
 
    private SetList m_setList = null;

    
    private boolean firstPaint = true;
    
    private ArrayList<ParameterList> m_parameterListArray = null;
    private final ColorParameter m_colorParameter;
    private final IntegerParameter m_thicknessParameter;
    private final IntegerParameter m_zoomParameter;
    private final IntegerParameter m_rotateParameter;
    private final IntegerParameter m_splitParameter;
    private final IntegerParameter  m_xTranslationParameter;
    private final IntegerParameter  m_yTranslationParameter;
    private final ParameterList m_colorParameterList;
    private final ArrayList<ColorParameter> m_colorAreaParameterList;
    
    public PlotVennDiagram(BasePlotPanel plotPanel, CompareDataInterface compareDataInterface, CrossSelectionInterface crossSelectionInterface, int[] cols) {
        super(plotPanel, PlotType.VENN_DIAGRAM_PLOT, compareDataInterface, crossSelectionInterface);
        
        update(cols, null); 
        
        ActionListener repaintAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                m_plotPanel.forceUpdateDoubleBuffer();
                m_plotPanel.repaint();
            }

        };
        
        m_colorParameterList = new ParameterList("Colors");
        m_colorParameter = new ColorParameter("COLOR_BORDER_VENNDIAGRAM", "Border Color", Color.white);
        m_colorParameterList.add(m_colorParameter);
        m_thicknessParameter = new IntegerParameter("THICKNESS_BORDER_VENNDIAGRAM", "Border Thickness", JSpinner.class, 5, 1, 10);
        m_colorParameterList.add(m_thicknessParameter);
        
        m_colorAreaParameterList = new ArrayList<>();

        ParameterList transformationsParameterList = new ParameterList("Transformations");
        m_rotateParameter = new IntegerParameter("ROTATE_FACTOR_VENNDIAGRAM", "Rotation", JSlider.class, 0, 0, 359);
        m_zoomParameter = new IntegerParameter("ZOOM_FACTOR_VENNDIAGRAM", "Zooming", JSlider.class, 100, 10, 100);
        m_splitParameter = new IntegerParameter("SPLIT_FACTOR_VENNDIAGRAM", "Splitting", JSlider.class, 0, 0, 200);
        m_xTranslationParameter = new IntegerParameter("TRANSLATE_X_VENNDIAGRAM", "X Translation", JSlider.class, 0, -200, 200);
        m_yTranslationParameter = new IntegerParameter("TRANSLATE_Y_VENNDIAGRAM", "Y Translation", JSlider.class, 0, -200, 200);
        transformationsParameterList.add(m_rotateParameter);
        transformationsParameterList.add(m_zoomParameter);
        transformationsParameterList.add(m_splitParameter);
        transformationsParameterList.add(m_xTranslationParameter);
        transformationsParameterList.add(m_yTranslationParameter);
        
        
        m_parameterListArray = new  ArrayList<>(2);
        m_parameterListArray.add(transformationsParameterList);
        m_parameterListArray.add(m_colorParameterList);
        

        
        m_colorParameter.setExternalActionListener(repaintAction);
        m_rotateParameter.setExternalActionListener(repaintAction);
        m_zoomParameter.setExternalActionListener(repaintAction);
        m_splitParameter.setExternalActionListener(repaintAction);
        m_thicknessParameter.setExternalActionListener(repaintAction);
        m_xTranslationParameter.setExternalActionListener(repaintAction);
        m_yTranslationParameter.setExternalActionListener(repaintAction);
        
        // disable selection buttons
        m_plotPanel.enableButton(BasePlotPanel.PlotToolbarListener.BUTTONS.GRID, false);
        m_plotPanel.enableButton(BasePlotPanel.PlotToolbarListener.BUTTONS.EXPORT_SELECTION, false);
        m_plotPanel.enableButton(BasePlotPanel.PlotToolbarListener.BUTTONS.IMPORT_SELECTION, false);
    }

    @Override
    public boolean needsXAxis() {
        return false;
    }

    @Override
    public boolean needsYAxis() {
        return false;
    }

    @Override
    public void parametersChanged() {
        int labelIndex = 0;
        int areaIndex = 0;
        for (IntersectArea intersectArea : m_setList.getGeneratedAreas()) {

            LabelMarker marker = (LabelMarker) m_markersList.get(labelIndex);
            marker.setReferenceColor(m_colorAreaParameterList.get(areaIndex).getColor());

            labelIndex++;
            areaIndex++;
        }
    }

    @Override
    public void paint(Graphics2D g2d) {

        if (m_setList == null) {
            return;
        }

        int width = m_plotPanel.getWidth();
        int height = m_plotPanel.getHeight();


        
        
        boolean scaled = m_setList.scale(width, height, 10);

        if (scaled || firstPaint) {
            m_setList.generateAreas();
            
            // must be updated because axis depend of the pixel height and width for venn diagram
            m_plotPanel.updateAxis(this);
            m_plotPanel.getXAxis().setSize(0, height, width, 0);
            m_plotPanel.getYAxis().setSize(0, 0, 0, height);
        }

        if (m_setList.getGeneratedAreas() == null) {
            return;
        }
        
        
        if (firstPaint) {
            
            firstPaint = false;

            prepareColorAreaParameterList();
            int labelIndex = 0;
            int areaIndex = 0;
            for (IntersectArea intersectArea : m_setList.getGeneratedAreas()) {
                /*Set s = intersectArea.getOnlySet();
                if (s == null) {
                    areaIndex++;
                    continue;
                }*/
                labelIndex++;
                double percentageX = 60d/width;
                double percentageY = 1-((30d*labelIndex)/height);
                
                LabelMarker marker = new LabelMarker(m_plotPanel, new PercentageCoordinates(percentageX, percentageY), intersectArea.getDisplayName(this) , LabelMarker.ORIENTATION_XY_MIDDLE, LabelMarker.ORIENTATION_XY_MIDDLE, m_colorAreaParameterList.get(areaIndex).getColor());
                addMarker(marker);

                areaIndex++;
            }
        }

        AffineTransform previousTransform = g2d.getTransform();
        
 
        Integer translateX = (Integer) m_xTranslationParameter.getObjectValue();
        double translateFactorX = (translateX!= null) ? translateX : 0;
        Integer translateY = (Integer) m_yTranslationParameter.getObjectValue();
        double translateFactorY = (translateY!= null) ? translateY : 0;
        if ((translateFactorX!=0) || (translateFactorY!=0)) {
            g2d.transform(AffineTransform.getTranslateInstance(translateFactorX, translateFactorY));
        }
        
        Integer zoom = (Integer) m_zoomParameter.getObjectValue();
        double zoomFactor = (zoom!= null) ? ((double)zoom)/100d : 1;
        if ((zoom!= null) && (zoom < 100)) {
            g2d.transform(AffineTransform.getScaleInstance(zoomFactor, zoomFactor));
        }
        
        Integer rotation = (Integer) m_rotateParameter.getObjectValue();
        if ((rotation!= null) && (rotation > 0)) {
            
            double angle = ((double) rotation.intValue())*Math.PI/180;
            double centerX = (width/2) ; //* zoomFactor;
            double centerY = (height/2) ; //* zoomFactor;
            g2d.transform(AffineTransform.getRotateInstance( angle , centerX, centerY));

        }
        
        Integer split = (Integer) m_splitParameter.getObjectValue();
        double splitFactor = (split != null) ? split : 0;

        int centerSplitX = width/2;
        int centerSplitY = height/2;

        // fill areas
        ArrayList<Area> areaListForSplitFactor = null;
        int index = 0;
        for (IntersectArea intersectArea : m_setList.getGeneratedAreas()) {
            g2d.setColor(m_colorAreaParameterList.get(index).getColor());
            index++;

            Area a = intersectArea.getArea();

            if (splitFactor > 0) {
                
                Rectangle r = a.getBounds();
                int centerX = r.x + r.width / 2;
                int centerY = r.y + r.height / 2;
                double trX = splitFactor*((double)(centerX-centerSplitX))/((double)(width-centerSplitX));
                double trY = splitFactor*((double)(centerY-centerSplitY))/((double)(height-centerSplitY));
                
                Area splittedArea = a.createTransformedArea(AffineTransform.getTranslateInstance(trX, trY));
                if (areaListForSplitFactor == null) {
                    areaListForSplitFactor = new ArrayList();
                }
                areaListForSplitFactor.add(splittedArea);
                g2d.fill(splittedArea);
            } else {
                
                g2d.fill(a);
            }

        }

       
        
        Color color = m_colorParameter.getColor();
        g2d.setColor(color);
        Stroke previousStroke = g2d.getStroke();
        
        Integer thickness = (Integer) m_thicknessParameter.getObjectValue();
        if (thickness == null) {
            thickness = 5;
        }
        g2d.setStroke( new BasicStroke(thickness));
        
        
        if (splitFactor > 0) {
            for (Area a : areaListForSplitFactor) {
                g2d.draw(a);
            }
        } else {
            for (Set set : m_setList.getList()) {
                Circle c = set.getCircle();

                int x = (int) Math.round(c.getX() - c.getRadius());
                int y = (int) Math.round(c.getY() - c.getRadius());
                int size = (int) Math.round(c.getRadius() * 2);
                g2d.drawOval(x, y, size, size);

            }
        }

        g2d.setStroke(previousStroke);
        
        g2d.setTransform(previousTransform);
    }

    @Override
    public String getToolTipText(double x, double y) {
        return null;
    }

    @Override
    public void update() {
        
        final double EPSILON = 1e-15;
        
        // Sets Creation
        m_setList = new SetList();
        
        int nbCols = m_cols.length;
        int nbRows = m_compareDataInterface.getRowCount();
        for (int i=0;i<nbCols;i++) {
            int colId = m_cols[i];
            
            int nbValues = 0;
            for (int j=0;j<nbRows;j++) {
                Number value = (Number) m_compareDataInterface.getDataValueAt(j, colId);

                if (value != null) {
                    double d = value.doubleValue();
                    if ((! Double.isNaN(d)) && (Math.abs(d)>EPSILON)) {
                        nbValues++;
                    }    
                }
                
            }
            Set s = new Set(m_compareDataInterface.getDataColumnIdentifier(colId), nbValues, i);
            m_setList.addSet(s);
            
        }
        
        for (int i=0;i<nbCols;i++) {
            int colId1 = m_cols[i];
            for (int j=i+1;j<nbCols;j++) {
                int colId2 = m_cols[j];
                int nbValues = 0;
                for (int k = 0; k < nbRows; k++) {
                    Number value1 = (Number) m_compareDataInterface.getDataValueAt(k, colId1);

                    if (value1 != null) {
                        double d1 = value1.doubleValue();
                        if ((!Double.isNaN(d1)) && (Math.abs(d1) > EPSILON)) {
                            
                            Number value2 = (Number) m_compareDataInterface.getDataValueAt(k, colId2);

                            if (value2 != null) {
                                double d2 = value2.doubleValue();
                                if ((!Double.isNaN(d2)) && (Math.abs(d2) > EPSILON)) {
                                    nbValues++;
                                }
                            }

                        }
                    }
                }
                if (nbValues > 0) {
                    m_setList.addIntersection(m_setList.getSet(i), m_setList.getSet(j), nbValues);
                }
            }
        }

        allIntersections();
        
        m_setList.approximateSolution();
        m_setList.optimizeSolution();

        if (m_markersList != null) {
            m_markersList.clear();
        }
        
        firstPaint = true;
        
        m_plotPanel.repaint();
        
    }

    
    public int getIntersectionSize(Set[] setArray) {
        int intersectionOfSetsId = 0;
        for (Set s : setArray) {
            intersectionOfSetsId += (int) Math.round(Math.pow(2,s.getId()));
        }
        
        return m_allIntersections.get(intersectionOfSetsId);
    }
    public void allIntersections() {
        
        final double EPSILON = 1e-15;
        
        int nbRows = m_compareDataInterface.getRowCount();
        
        m_allIntersections.clear();
        
        ArrayList<Set> setList = m_setList.getList();
        int nb = setList.size();
        int powNb = (int) Math.round(Math.pow(2, nb));
        
        for (int intersectionOfSetsId = 1; intersectionOfSetsId < powNb; intersectionOfSetsId++) {
            
            int nbValues = 0;
            for (int i = 0; i < nbRows; i++) {
                
                boolean valuesFound = true;
                for (int setId = 0; setId < nb; setId++) {
                    int pow = (int) Math.round(Math.pow(2, setId));
                    if ((pow & intersectionOfSetsId) > 0) {
                        int colId = m_cols[setId];
                        Number value = (Number) m_compareDataInterface.getDataValueAt(i, colId);
                        if (value != null) {
                            double d2 = value.doubleValue();
                            if ((Double.isNaN(d2)) || (Math.abs(d2) <= EPSILON)) {
                                valuesFound = false;
                                break;
                            }
                        } else {
                            valuesFound = false;
                            break;
                        }
                    }
                }
                if (valuesFound) {
                    nbValues++;
                }
            }
            m_allIntersections.put(intersectionOfSetsId, nbValues);
            
        }
    }
    private HashMap<Integer, Integer> m_allIntersections = new HashMap();
    
    @Override
    public boolean select(double x, double y, boolean append) {
        return false;
    }

    @Override
    public boolean select(Path2D.Double path, double minX, double maxX, double minY, double maxY, boolean append) {
        return false;
    }

    @Override
    public ArrayList<ParameterList> getParameters() {
        
        m_currentColor = (Color) m_colorParameter.getObjectValue();
        m_currentThickness = m_thicknessParameter.getStringValue();
        m_currentZoom = m_zoomParameter.getStringValue();
        m_currentRotate = m_rotateParameter.getStringValue();
        m_currentSplit = m_splitParameter.getStringValue();
        m_currentXTranslation = m_xTranslationParameter.getStringValue();
        m_currentYTranslation = m_yTranslationParameter.getStringValue();
        
        prepareColorAreaParameterList();
        
        
        return m_parameterListArray;
    }

    private void prepareColorAreaParameterList() {
        int size = m_setList.getGeneratedAreas().size();
        int sizeColor = m_colorAreaParameterList.size();
        if (size != sizeColor) {
            m_colorAreaParameterList.clear();

            ActionListener repaintAction = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    m_plotPanel.forceUpdateDoubleBuffer();
                    m_plotPanel.repaint();
                }

            };

            int index = 1;
            for (IntersectArea intersectArea : m_setList.getGeneratedAreas()) {
                ColorParameter param = new ColorParameter("COLOR_AREA_VENNDIAGRAM" + index, "Color " + index, m_setList.getColor((index-1)));
                param.setExternalActionListener(repaintAction);
                m_colorAreaParameterList.add(param);
                m_colorParameterList.add(param);
                index++;
            }
        }
        
        m_currentAreaColorList.clear();
        for (ColorParameter parameter : m_colorAreaParameterList) {
            m_currentAreaColorList.add(parameter.getColor());
        }
    }
    
    @Override
    public boolean parametersCanceled() {
        m_colorParameter.setColor(m_currentColor);
        m_thicknessParameter.setValue(m_currentThickness);
        m_zoomParameter.setValue(m_currentZoom);
        m_rotateParameter.setValue(m_currentRotate);
        m_splitParameter.setValue(m_currentSplit);
        m_xTranslationParameter.setValue(m_currentXTranslation);
        m_yTranslationParameter.setValue(m_currentYTranslation);
        int index = 0;
        for (ColorParameter parameter : m_colorAreaParameterList) {
            parameter.setColor(m_currentAreaColorList.get(index));
            index++;
        }
        
        return true;
    }
    private Color m_currentColor;
    private String m_currentThickness;
    private String m_currentZoom;
    private String m_currentRotate;
    private String m_currentSplit;
    private String m_currentXTranslation;
    private String m_currentYTranslation;
    private ArrayList<Color> m_currentAreaColorList = new ArrayList<>();
    
    @Override
    public boolean isMouseOnPlot(double x, double y) {
        return false;
    }

    @Override
    public boolean isMouseOnSelectedPlot(double x, double y) {
        return false;
    }

    @Override
    public String getEnumValueX(int index, boolean fromData) {
        return null;
    }

    @Override
    public String getEnumValueY(int index, boolean fromData) {
        return null;
    }

    @Override
    public double getNearestXData(double x) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double getNearestYData(double y) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double getXMin() {
        return 0;
    }

    @Override
    public double getXMax() {
        if (m_plotPanel == null) {
            return 0;
        }
        return m_plotPanel.getWidth();
    }

    @Override
    public double getYMin() {
        return 0;
    }

    @Override
    public double getYMax() {
        if (m_plotPanel == null) {
            return 0;
        }
        return m_plotPanel.getHeight();
    }

    @Override
    public ArrayList<Long> getSelectedIds() {
        return null;
    }

    @Override
    public void setSelectedIds(ArrayList<Long> selection) {
        
    }


    
}
