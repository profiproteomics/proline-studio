package fr.proline.studio.rsmexplorer.gui.calc.graphui;

import fr.proline.studio.comparedata.CompareDataInterface;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;

/**
 *
 * @author JM235353
 */
public class DataWidget {
        
        private final CompareDataInterface m_dataInterface;
        private final boolean m_keysAtRight;
        
        private int m_x;
        private int m_y;
        private int m_height;
        private int m_width;
        
        private Font m_font = null;
        private Font m_fontBold = null;
        private int m_hgtBold;
        private int m_hgtPlain;
        private int m_ascentBold;
        //private int m_ascentPlain;
        
        private static final int MARGIN_X = 5;
        private static final int MARGIN_Y = 5;
        
        public DataWidget(CompareDataInterface dataInterface, boolean keysAtRight) {
            m_dataInterface = dataInterface;
            m_keysAtRight = keysAtRight;
        }
        
        public void setPosition(int x, int y) {
            m_x = x;
            m_y = y;
        }
        
        public int getX() {
            return m_x;
        }
        public int getY() {
            return m_y;
        }
        public int getWidth() {
            return m_width;
        }
        public int getHeight() {
            return m_height;
        }
        
        public void draw(Graphics g) {
            if (m_font == null) {
                m_font = new Font(" TimesRoman ", Font.PLAIN, 11);
                m_fontBold = m_font.deriveFont(Font.BOLD);
                
                FontMetrics metricsBold = g.getFontMetrics(m_fontBold);
                FontMetrics metricsPlain = g.getFontMetrics(m_font);

                m_hgtBold = metricsBold.getHeight();
                m_ascentBold = metricsBold.getAscent();
                
                
                m_hgtPlain = metricsPlain.getHeight();
                //m_ascentPlain = metricsPlain.getAscent();
                
                
                int nbData = m_dataInterface.getColumnCount();
                
                String name = m_dataInterface.getName();
                int maxSize = metricsBold.stringWidth(name);
                
                for (int i=0;i<nbData;i++) {
                    String text = m_dataInterface.getDataColumnIdentifier(i);
                    int size = metricsPlain.stringWidth(text);
                    if (size > maxSize) {
                        maxSize = size;
                    }
                }
                
                m_width = MARGIN_X*2+maxSize;
                m_height = m_hgtBold+nbData*m_hgtPlain+(nbData+2)*MARGIN_Y;

            }
            
            g.setColor(new Color(255,204,99));
            g.fillRect(m_x, m_y, m_width, m_hgtBold+MARGIN_Y+MARGIN_Y/2);

            g.setColor(Color.black);
            g.drawRect(m_x, m_y, m_width, m_hgtBold+MARGIN_Y+MARGIN_Y/2);
            g.drawRect(m_x, m_y, m_width, m_height);

            
            
            g.setFont(m_fontBold);
            g.drawString( m_dataInterface.getName(), m_x+MARGIN_X, m_y+MARGIN_Y+m_ascentBold);
            
            g.setFont(m_font);
            int nbData = m_dataInterface.getColumnCount();
            for (int i = 0; i < nbData; i++) {
                String text = m_dataInterface.getDataColumnIdentifier(i);
                g.drawString( text, m_x+MARGIN_X, m_y+MARGIN_Y+m_ascentBold+m_hgtBold+(MARGIN_Y+m_hgtPlain)*i);
            }
            
            int[] keysColumn = m_dataInterface.getKeysColumn();
            for (int i=0;i<keysColumn.length;i++) {
                int columnIndex = keysColumn[i];
                int diameter = m_hgtPlain/3;
                if (m_keysAtRight) {
                    g.drawRect(m_x+m_width, m_y+MARGIN_Y+m_ascentBold+m_hgtBold+(MARGIN_Y+m_hgtPlain)*columnIndex-(m_hgtPlain-diameter)/2, diameter, diameter);
                    //g.drawArc(m_x+m_width-diameter/2+1,m_y+MARGIN_Y+m_ascentBold+m_hgtBold+(MARGIN_Y+m_hgtPlain)*columnIndex-m_hgtPlain/2 , diameter, diameter, 270, 180);
                } else {
                    g.drawRect(m_x-diameter, m_y+MARGIN_Y+m_ascentBold+m_hgtBold+(MARGIN_Y+m_hgtPlain)*columnIndex-(m_hgtPlain-diameter)/2, diameter, diameter);
                    //g.drawArc(m_x-diameter+1,m_y+MARGIN_Y+m_ascentBold+m_hgtBold+(MARGIN_Y+m_hgtPlain)*columnIndex-m_hgtPlain/2 , diameter, diameter, 90, 180);
                }
            }
            
            
        }
        
        public void getKeyPosition(int columnIndex, Point p) {
            int diameter = m_hgtPlain/3;
            if (m_keysAtRight) {
                p.x = m_x+m_width+diameter/2;
                p.y = m_y+MARGIN_Y+m_ascentBold+m_hgtBold+(MARGIN_Y+m_hgtPlain)*columnIndex-(m_hgtPlain-diameter)/2+diameter/2;
            } else {
                p.x = m_x-diameter+diameter/2;
                p.y = m_y+MARGIN_Y+m_ascentBold+m_hgtBold+(MARGIN_Y+m_hgtPlain)*columnIndex-(m_hgtPlain-diameter)/2 +diameter/2;
            }
        }
        
    }
