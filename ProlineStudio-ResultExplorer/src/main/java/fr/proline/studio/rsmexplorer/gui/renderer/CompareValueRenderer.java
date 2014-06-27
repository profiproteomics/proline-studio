package fr.proline.studio.rsmexplorer.gui.renderer;

import fr.proline.studio.export.ExportTextInterface;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.TableCellRenderer;


/**
 *
 * @author JM235353
 */
public class CompareValueRenderer implements TableCellRenderer, ExportTextInterface {

    private CompareValuePanel m_valuePanel = null;
    
    private static Color STRIPPED_COLOR = UIManager.getColor("UIColorHighlighter.stripingBackground"); //JPM.WART
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

         Color selectionBackground = null;
        if (isSelected) {
            selectionBackground = table.getSelectionBackground();
        } else if (row % 2 == 1) {
            // JPMWART
            selectionBackground = STRIPPED_COLOR;
        }

        
        if (m_valuePanel == null) {
            m_valuePanel = new CompareValuePanel();
        }
        m_valuePanel.init((CompareValue) value, selectionBackground);
        return m_valuePanel;
    }
    

    

    
    @Override
    public String getExportText() {
        return "";
    }


    public abstract static class CompareValue {
        
        public abstract int getNumberColumns();
        
        public abstract Color getColor(int col);
        public abstract double getValue(int col);
        public abstract double getMaximumValue();
    }
    
    
    public static class CompareValuePanel extends JPanel {
        
        
        private static int DELTA_X = 6; 
        private static int DELTA_Y = 2;
        private static int DIM_Y = 20;
        
        private CompareValue m_v;
        private Color m_selectionBackground;
        
        public CompareValuePanel() {
            
        }
        
        public void init(CompareValue v, Color selectionBackground) {
            m_v = v;
            m_selectionBackground = selectionBackground;
        }
        
        @Override
        public Dimension getPreferredSize() {
            int width = (m_v.getNumberColumns() + 2) * DELTA_X;
            return new Dimension(width, DIM_Y);
        }
        
        @Override
        public void paint(Graphics g) {

            if (m_selectionBackground != null) {
                g.setColor(m_selectionBackground);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
            
            int nbCols = m_v.getNumberColumns();

            for (int i = 0; i < nbCols; i++) {
                g.setColor(m_v.getColor(i));
                double maxValue = m_v.getMaximumValue();
                double value = m_v.getValue(i);
                int height = (int) Math.round((value/maxValue)*(DIM_Y-2*DELTA_Y));
                g.fillRect((i+1)*DELTA_X, DIM_Y-height-DELTA_Y, DELTA_X , height);
                g.setColor(Color.gray);
                g.drawRect((i+1)*DELTA_X, DIM_Y-height-DELTA_Y, DELTA_X , height); 
                
            }

        }
        
    }
    
}
