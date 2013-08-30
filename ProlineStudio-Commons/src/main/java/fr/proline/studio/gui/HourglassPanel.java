package fr.proline.studio.gui;

import fr.proline.studio.utils.IconManager;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * Panel being able to display a loading message
 * @author JM235353
 */
public class HourglassPanel extends JPanel implements ActionListener {
    
    private boolean m_loading = false;
    private boolean m_calculating = false;
    
    private int m_id = -1;

    public void setLoading(int id, boolean calculating) {
        m_id = id;
        m_calculating = calculating;
        setLoading(true);
    }
    
    public void setLoading(int id) {
       setLoading(id, false);
    }
    
    public void setLoaded(int id) {
        if (id>=m_id) {
            setLoading(false);
        }
    }
    
    private void setLoading(boolean loading) {
        boolean needRepaint = m_loading ^ loading;
        m_loading = loading;
        if (needRepaint) {
            if (!m_loading) {
                // loading is finished, we repaint now
                repaint();
            } else {
                // loading has started, we do not repaint now
                // to let time to the loading to be finished when it is rapid
                Timer timer = new Timer(500, this);
                timer.setRepeats(false);
                timer.start(); 
            }
                
        }
    }
    
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        
        if (!m_loading) {
            return;
        }
        
        int height = getHeight();
        int width = getWidth();
        
        
        final int PAD = 10;
        final int INTERNAL_PAD = 5;
        final int ICON_WIDTH = 16;
        final int BOX_HEIGHT = INTERNAL_PAD*2+ICON_WIDTH;
        final int BOX_WIDTH = 130;
        g.setColor(Color.white);
        g.fillRect(PAD, height-BOX_HEIGHT-PAD, BOX_WIDTH, BOX_HEIGHT);
        g.setColor(Color.darkGray);
        g.drawRect(PAD+2, height-BOX_HEIGHT-PAD+2, BOX_WIDTH-4, BOX_HEIGHT-4);
        
        ImageIcon hourGlassIcon = IconManager.getIcon(IconManager.IconType.HOUR_GLASS);
        g.drawImage(hourGlassIcon. getImage(), PAD+INTERNAL_PAD,  height-BOX_HEIGHT-PAD+INTERNAL_PAD, null);
        
        if (m_loadingFont == null) {
            m_loadingFont = new Font("SansSerif", Font.BOLD, 12);
            FontMetrics metrics = g.getFontMetrics(m_loadingFont);
            m_fontAscent = metrics.getAscent();

        }
        g.setFont(m_loadingFont);
        g.setColor(Color.black);
        if (m_calculating) {
            g.drawString("Calculating...", PAD+INTERNAL_PAD*2+ICON_WIDTH, height-BOX_HEIGHT-PAD+INTERNAL_PAD+m_fontAscent);
        } else {
            g.drawString("Loading Data...", PAD+INTERNAL_PAD*2+ICON_WIDTH, height-BOX_HEIGHT-PAD+INTERNAL_PAD+m_fontAscent);
        }
        
    }
    private static Font m_loadingFont = null;
    private static int m_fontAscent = 0;

    @Override
    public void actionPerformed(ActionEvent e) {
        if (m_loading) {
            // loading is not finished, we repaint to show the loading message
            repaint();
        }
    }
    
    
    
    
}
