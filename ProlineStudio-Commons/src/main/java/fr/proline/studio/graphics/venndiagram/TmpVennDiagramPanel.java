package fr.proline.studio.graphics.venndiagram;

import fr.proline.studio.utils.CyclicColorPalette;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Area;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 *
 * @author JM235353
 */
public class TmpVennDiagramPanel extends JPanel {
    
    private SetList m_setList;

    private static final BasicStroke STROKE_2 = new BasicStroke(2);
    
    private int areaToPaint = 0;
    
    public TmpVennDiagramPanel(SetList setList) {
        m_setList = setList;
        
        /*int delay = 1000; //milliseconds
        ActionListener taskPerformer = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                repaint();
            }
        };
        new Timer(delay, taskPerformer).start();*/

    }
    
    @Override
    public void paint(Graphics g) {
        
        Graphics2D g2d = (Graphics2D) g;

        
        int width = getWidth();
        int height = getHeight();
        
        g.setColor(Color.white);
        g.fillRect(0, 0, width, height);
        
        
        int colorIndex = 0;
        for (IntersectArea intersectArea : m_setList.getGeneratedAreas()) {
            
            //if (colorIndex == areaToPaint) {
                g.setColor(CyclicColorPalette.getColor(colorIndex));
                Area a = intersectArea.getArea();
                g2d.fill(a);
                
                /*g.setColor(Color.black);
                g.drawString(String.valueOf(areaToPaint), 10, 10);*/
            //}
            colorIndex++;
        }
        areaToPaint++;
        if (areaToPaint == m_setList.getGeneratedAreas().size()) {
            areaToPaint = 0;
        }
        
        g.setColor(Color.black);
        g2d.setStroke(STROKE_2);
        /*for (IntersectArea intersectArea : m_setList.getGeneratedAreas()) {

            Area a = intersectArea.getArea();
            g2d.draw(a);
        }*/
        
        for (Set set : m_setList.getList()) {
            Circle c = set.getCircle();

            int x = (int) Math.round(c.getX() - c.getRadius());
            int y = (int) Math.round(c.getY() - c.getRadius());
            int size = (int) Math.round(c.getRadius() * 2);
            g.drawOval(x, y, size, size);
            
            
            g.drawString(set.getName(), x+size/2, y+size/2);
        }
        
        
    }
    
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(600,600);
    }
    
}
