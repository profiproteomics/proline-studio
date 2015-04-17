package fr.proline.studio.rsmexplorer.gui.calc.graph;

import fr.proline.studio.python.data.TableInfo;
import fr.proline.studio.rsmexplorer.gui.calc.functions.AbstractFunction;
import static fr.proline.studio.rsmexplorer.gui.calc.graph.GraphNode.HEIGHT;
import java.awt.Color;
import java.awt.LinearGradientPaint;
import java.awt.geom.Point2D;
import java.util.LinkedList;

/**
 *
 * @author JM235353
 */
public class FunctionGraphNode extends GraphNode {
    
    private static final Color FRAME_COLOR = new Color(178,45,114);
    private static final Color BACKGROUND_COLOR = new Color(239,186,209);

    private AbstractFunction m_function;
    
    public FunctionGraphNode(AbstractFunction function) {
        m_function = function;
        m_outConnector = new GraphConnector(true);
        
        int nbParameters = function.getNumberOfInParameters();
        if (nbParameters > 0) {
            m_inConnectors = new LinkedList<>();
            for (int i = 0; i < nbParameters; i++) {
                m_inConnectors.add(new GraphConnector(false));
            }
        }
    }

    @Override
    public String getName() {
        return "Join";
    }

    @Override
    public Color getFrameColor() {
        return FRAME_COLOR;
    }

    @Override
    public LinearGradientPaint getBackgroundGradient() {
        Point2D start = new Point2D.Float(m_x, m_y);
        Point2D end = new Point2D.Float(m_x, m_y+HEIGHT);
        float[] dist = {0.0f, 0.5f, 0.501f, 1.0f};
        Color[] colors = {Color.white, FRAME_COLOR.brighter(),FRAME_COLOR, FRAME_COLOR.brighter()};
        return new LinearGradientPaint(start, end, dist, colors);
    }



    

    
}
