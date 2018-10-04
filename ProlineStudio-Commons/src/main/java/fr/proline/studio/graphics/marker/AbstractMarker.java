package fr.proline.studio.graphics.marker;

import fr.proline.studio.graphics.BasePlotPanel;
import java.awt.Graphics2D;

/**
 * Base class for all markers which can be plot in a BasePlotPanel
 *
 * @author JM235353
 */
public abstract class AbstractMarker implements Cloneable {

    protected BasePlotPanel m_plotPanel;
    protected boolean m_isVisible;

    public AbstractMarker(BasePlotPanel plotPanel) {
        m_plotPanel = plotPanel;
    }

    public boolean isVisible() {
        return m_isVisible;
    }

    public void setVisible(boolean m_isVisible) {
        this.m_isVisible = m_isVisible;
    }

    public abstract void paint(Graphics2D g);

    protected AbstractMarker clone() throws CloneNotSupportedException {
        return (AbstractMarker) super.clone();
    }

    public AbstractMarker clone(BasePlotPanel plotPanel) throws CloneNotSupportedException {
        AbstractMarker clone = clone();
        clone.m_plotPanel = plotPanel;
        return clone;
    }

}
