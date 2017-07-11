package fr.proline.studio.graphics.venndiagram;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;
import javax.swing.JPanel;

/**
 *
 * @author JM235353
 */
public class TmpVennDiagramPanel extends JPanel {
    
    private ArrayList<Set> m_setArrayList;
    
    public TmpVennDiagramPanel(ArrayList<Set> setArrayList) {
        m_setArrayList = setArrayList;
    }
    
    public void paint(Graphics g) {
        int width = getWidth();
        int height = getHeight();
        
        g.setColor(Color.white);
        g.fillRect(0, 0, width, height);
        
        g.setColor(Color.black);
        for (Set set : m_setArrayList) {
            Circle c = set.getCircle();
            
            int x = (int) Math.round(c.getX()-c.getRadius());
            int y = (int) Math.round(c.getY()-c.getRadius());
            int size = (int) Math.round(c.getRadius()*2);
            g.drawOval(x, y, size, size);
        }
    }
    
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(600,600);
    }
    
}
