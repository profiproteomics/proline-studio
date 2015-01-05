package fr.proline.studio.graphics;

import fr.proline.studio.parameter.ColorOrGradientParameter;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.LinearGradientPaint;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import javax.swing.ButtonGroup;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

/**
 *
 * @author JM235353
 */
public class ColorOrGradientChooserPanel extends JPanel {

    private JRadioButton m_colorRadioButton = null;
    private JRadioButton m_gradientRadioButton = null;
    
    private InternalColorButton m_colorSelectionButton = null;
    private GradientSelectorPanel m_gradientSelector = null;

    private ColorOrGradient m_colorOrGradient;

    private JComboBox m_gradientParamComboBox = null;
    
    public ColorOrGradientChooserPanel(ColorOrGradient color, ArrayList<ReferenceIdName> gradientParam) {
        setLayout(new GridBagLayout());

        m_colorOrGradient = color;
        
        m_colorRadioButton = new JRadioButton();
        m_gradientRadioButton = new JRadioButton();
        ButtonGroup group = new ButtonGroup();
        group.add(m_colorRadioButton);
        group.add(m_gradientRadioButton);
        
        m_gradientParamComboBox = new JComboBox(gradientParam.toArray());
        
        if (color.isColorSelected()) {
            m_colorRadioButton.setSelected(true);
        } else {
            m_gradientRadioButton.setSelected(true);
        }
        
        
        m_colorSelectionButton = new InternalColorButton();
        m_colorSelectionButton.initActionListener();
        m_colorSelectionButton.setBackground(color.getColor());
        
        m_gradientSelector = new GradientSelectorPanel();
        m_gradientSelector.setLinearGradient(color.getGradient());

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        add(m_colorRadioButton, c);
        
        c.gridx++;
        c.gridy = 0;
        c.weightx = 1.0;
        add(m_colorSelectionButton, c);
        
        c.gridx = 0;
        c.gridy++;
        c.weightx = 0;
        add(m_gradientRadioButton, c);
        
        c.gridx++;
        c.weightx = 1.0;
        add(m_gradientSelector, c);
        
        c.gridy++;
        add(m_gradientParamComboBox, c);

    }

    public ReferenceIdName getSelectedReferenceIdName() {
        return (ReferenceIdName) m_gradientParamComboBox.getSelectedItem();
    }
    
    public ColorOrGradient getColor() {
        m_colorOrGradient.setColor(m_colorSelectionButton.getBackground());
        m_colorOrGradient.setGradient(m_gradientSelector.getGradient());
        if (m_colorRadioButton.isSelected()) {
            m_colorOrGradient.setColorSelected();
        } else {
            m_colorOrGradient.setGradientSelected();
        }
        return m_colorOrGradient;
    }

    public void setColor(ColorOrGradient c) {
        m_colorOrGradient = c;
        m_colorSelectionButton.setBackground(c.getColor());
        m_gradientSelector.setLinearGradient(c.getGradient());
    }

    public class InternalColorButton extends ColorButton {

        public InternalColorButton() {
        }
        
        @Override
        protected ActionListener getActionListener() {

            final ColorButton colorButton = this;
            ActionListener a = new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {

                    if (!m_colorRadioButton.isSelected()) {
                        m_colorRadioButton.setSelected(true);
                    }

                    Color newColor = JColorChooser.showDialog(m_colorSelectionButton, null, m_colorSelectionButton.getBackground());
                    if (newColor != null) {
                        m_colorSelectionButton.setBackground(newColor);
                    }
                }
            };
            return a;
        }

    }
    
    public class GradientSelectorPanel extends JPanel implements MouseListener {
    
        
        
        private final Dimension m_dimension = new Dimension(WIDTH, HEIGHT);
        
        private static final int WIDTH = 100;
        private static final int HEIGHT = 35;
        private static final int DELTA_X = 10;
        private static final int DELTA_Y_TOP = 5;
        private static final int DELTA_Y_BOTTOM = 10;
        
        private LinearGradientPaint m_linearGradientPaint;
        private Color[] m_gradientColors;
        private float[] m_gradientFractions;
        
        private final ArrayList<ColorSelector> m_colorSelectors = new ArrayList<>();
        
        public GradientSelectorPanel() {
            m_linearGradientPaint = null;
            
            addMouseListener(this);
        }
    
        public void setLinearGradient(LinearGradientPaint linearGradientPaint) {
            m_linearGradientPaint = linearGradientPaint;
            
            m_gradientColors = m_linearGradientPaint.getColors();
            m_gradientFractions = m_linearGradientPaint.getFractions();

            m_colorSelectors.clear();
            for (int i=0;i<m_gradientColors.length;i++) {
                ColorSelector colorSelector = new ColorSelector(m_gradientColors[i], m_gradientFractions[i]);
                m_colorSelectors.add(colorSelector);
            }
            
            repaint();
        }
        
        public LinearGradientPaint getGradient() {
            return m_linearGradientPaint;
        }
        
        @Override
        public Dimension getPreferredSize() {
            return m_dimension;
        }
        
        @Override
        public void paint(Graphics g) {
            
            Graphics2D g2D = (Graphics2D) g;
            g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            g.setColor(Color.white);
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(Color.darkGray);
            g.drawRect(0, 0, getWidth(), getHeight());
            
            if (m_linearGradientPaint != null) {
                g2D.setPaint(m_linearGradientPaint);
                Rectangle2D r2d = new Rectangle2D.Double(DELTA_X, DELTA_Y_TOP, getWidth()-2*DELTA_X, getHeight()-DELTA_Y_TOP-DELTA_Y_BOTTOM);
                g2D.fill(r2d);
            }
            
            g.setColor(Color.darkGray);
            g.drawRect(DELTA_X, DELTA_Y_TOP, getWidth()-2*DELTA_X, getHeight()-DELTA_Y_TOP-DELTA_Y_BOTTOM);
            
            for (int i=0;i<m_colorSelectors.size();i++) {
                m_colorSelectors.get(i).paint(g, DELTA_X, getWidth()-DELTA_X, getHeight()-DELTA_Y_BOTTOM);
            }
        }
        


        @Override
        public void mouseClicked(MouseEvent e) {
            
            if (!m_gradientRadioButton.isSelected()) {
                m_gradientRadioButton.setSelected(true);
            }

            for (int i=0; i<m_colorSelectors.size(); i++) {
                if (m_colorSelectors.get(i).contains(e.getX(), e.getY())) {
                    
                    // use new color
                    m_gradientColors[i] = m_colorSelectors.get(i).getColor();
                    
                    m_linearGradientPaint = getGradientForPanel(m_gradientColors, m_gradientFractions);
                    
                    repaint();
                    return;
                }
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {}

        @Override
        public void mouseReleased(MouseEvent e) {}

        @Override
        public void mouseEntered(MouseEvent e) {}

        @Override
        public void mouseExited(MouseEvent e) {}
    
        public class ColorSelector {
            
            private Color m_color;
            private float m_fraction;
            
            private Polygon m_p;
            
            private static final int TRIANGLE_SIZE = 10;
            
            public ColorSelector(Color c, float f) {
                m_color = c;
                m_fraction = f;
                m_p = new Polygon();
            }
            
            public boolean contains(int x, int y) {
                if (m_p == null) {
                    return false;
                }
                if (m_p.contains(x, y)) {
                    Color newColor = JColorChooser.showDialog(null, null, m_color);
                    if (newColor != null) {
                        m_color = newColor;
                    }
                    
                    return true;
                }
                
                return false;
            }
            
            public void setColor(Color c) {
                m_color = c;
            }
            
            public Color getColor() {
                return m_color;
            }
            
            public void setFraction(float f) {
                m_fraction = f;
                m_p.reset();
            }
            
            public void paint(Graphics g, int x0, int x1, int yBase) {
                g.setColor(m_color);
                
                if (m_p.npoints == 0) {
                    int x = Math.round(x0+(x1-x0)*m_fraction);
                    m_p.addPoint(x, yBase-TRIANGLE_SIZE/2);
                    m_p.addPoint(x+TRIANGLE_SIZE/2,yBase+TRIANGLE_SIZE/2);
                    m_p.addPoint(x-TRIANGLE_SIZE/2,yBase+TRIANGLE_SIZE/2);
                }
                
                g.setColor(Color.white);
                g.fillPolygon(m_p); // fill as white first for transparent colors
                g.setColor(m_color);
                g.fillPolygon(m_p);
                g.setColor(Color.darkGray);
                g.drawPolygon(m_p);
            }

        }
        
    }
    
    public static LinearGradientPaint getGradientForPanel(Color[] colors, float[] fractions) {
        return new LinearGradientPaint(GradientSelectorPanel.DELTA_X, GradientSelectorPanel.DELTA_Y_TOP, GradientSelectorPanel.WIDTH - 2 * GradientSelectorPanel.DELTA_X, GradientSelectorPanel.HEIGHT - GradientSelectorPanel.DELTA_Y_TOP - GradientSelectorPanel.DELTA_Y_BOTTOM, fractions, colors);
    }
    
    

}
