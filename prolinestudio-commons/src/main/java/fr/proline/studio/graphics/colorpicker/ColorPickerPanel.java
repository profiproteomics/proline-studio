/* 
 * Copyright (C) 2019 VD225637
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1 
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License 
 * along with this program; If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
 */
package fr.proline.studio.graphics.colorpicker;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Based on HSL for panels, RGB for display
 * @author jm235353
 */
public class ColorPickerPanel extends JPanel implements ColorDataInterface {
    
    private final int MAIN_SIZE = 200;
    private final int DELTA = 1;
    private final int MARGIN_HEIGHT = 6;
    
    private final BasicStroke STROKE_1 = new BasicStroke(1);
    private final BasicStroke STROKE_3 = new BasicStroke(3);
    
    final int PALETTE_DELTA = 4;
    final int PALETTE_SQUARE_SIZE = 18;
    final int PALETTE_WIDTH = 9;
    
    private boolean m_imageUnset = true;
    
    // rgb color code
    private int m_r = 255;
    private int m_g = 255;
    private int m_b = 255;
    private int m_alpha = 255;
    
    private float m_hue = 0;
    private float m_saturation = 0;
    private float m_brightness = 1;
    
    private Color[] m_palette  = null;
    
    private boolean m_colorIsChanging = false;
    
    private final ArrayList<ColorChanged> m_colorChangedListeners = new ArrayList<>();
    
    public ColorPickerPanel(Color[] palette) {

        setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        
        HSBPanel hsbPanel = new HSBPanel();
        c.gridx = 0;
        c.gridy = 0;
        add(hsbPanel, c);
        
        BrightnessSelectionPanel bPanel = new BrightnessSelectionPanel();
        c.gridx++;
        add(bPanel, c);
        
        RGBSelectionPanel rgbSelectionPanel = new RGBSelectionPanel();
        c.gridx++;
        c.gridheight = 2;
        c.weighty = 1;
        add(rgbSelectionPanel, c);
        
        c.gridy++;
        c.gridx = 0;
        c.gridheight = 1;
        c.gridwidth = 2;
        c.weightx = 0;
        c.weighty = 0;
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.NONE;

        ColorPalettePanel palettePanel = new ColorPalettePanel(this, palette, PALETTE_SQUARE_SIZE, PALETTE_DELTA, PALETTE_WIDTH);
        add(palettePanel, c);
        
        
        m_colorChangedListeners.add(rgbSelectionPanel);
    }
    
    public void setColor(Color c) {
        propagateColorChanged(c.getRed(), c.getGreen(), c.getBlue());
        propagateAlphaChanged(c.getAlpha());
    }
    public void setColor(int r, int g, int b, int alpha) {
        propagateColorChanged(r, g, b);
        propagateAlphaChanged(alpha);
    }
    
    public Color getColor() {
        return new Color(m_r,m_g,m_b,m_alpha);
    }
    
    /*public final void setPalette(Color[] palette) {
        m_palette = palette;
    }*/
    
    public void repaintAll() {
        m_imageUnset = true;
        repaint();
    }
    
    @Override
    public void propagateColorChanged(int r, int g, int b) {
        propagateColorChanged(r, g, b, true);
    }
    public void propagateColorChanged(int r, int g, int b, boolean calculateHSB) {
        
        if (m_colorIsChanging) {
            return;
        }
        
        if ((r == m_r) && (g == m_g) && (b == m_b)) {
            if (!calculateHSB) {
                // rgb has not changed, but hsb has perhaps changed
                repaintAll();
            }
            return;
        }
        
        m_colorIsChanging = true; // avoid multiple call due to Change Listener for instance
        try {

            m_r = r;
            m_g = g;
            m_b = b;
            
            if (calculateHSB) {
                m_hue = getH();
                m_saturation = getS();
                m_brightness = getB();
            }

            // warn color listeners
            for (ColorChanged c : m_colorChangedListeners) {
                c.colorChanged();
            }

        } finally {
            m_colorIsChanging = false;
        }
        
        repaintAll();
    }
    
    
    public void propagateColorChanged(float h, float s, float b) {
        
        if (m_colorIsChanging) {
            return;
        }
        
        m_hue = h;
        m_saturation = s;
        m_brightness = b;
        
        int rgbValue = Color.HSBtoRGB(h, s, b);
        
        int r2 = (rgbValue & 0x00ff0000) >> 16;
        int g2 = (rgbValue & 0x0000ff00) >> 8;
        int b2 = (rgbValue & 0x000000ff);

        propagateColorChanged(r2, g2, b2, false);
    }
    
     public void propagateAlphaChanged(int alpha) {
         if (m_colorIsChanging) {
             return;
         }

         m_colorIsChanging = true; // avoid multiple call due to Change Listener for instance
         try {

             m_alpha = alpha;
             // warn color listeners
             for (ColorChanged c : m_colorChangedListeners) {
                 c.alphaChanged();
             }
         } finally {
            m_colorIsChanging = false;
         }
     }
    
    private float[] getHSBArray() {
        Color.RGBtoHSB(m_r, m_g, m_b, hsbArray);
        return hsbArray;
    }
    private float getH() {
        return getHSBArray()[0];
    }
    private float getS() {
        return getHSBArray()[1];
    }
    private float getB() {
        return getHSBArray()[2];
    }
    private float[] hsbArray = new float[3];

    @Override
    public int getRed() {
        return m_r;
    }

    @Override
    public int getGreen() {
        return m_g;
    }

    @Override
    public int getBlue() {
        return m_b;
    }

    @Override
    public void addListener(ColorDataInterface colorDataInterface) {
        // not used
    }
    
    private class HSBPanel extends JPanel implements MouseListener, MouseMotionListener {
        
        private final int OVAL_SIZE = 8;
        
        private final Dimension m_dimension = new Dimension(MAIN_SIZE+MARGIN_HEIGHT+DELTA+DELTA+MARGIN_HEIGHT,MAIN_SIZE+MARGIN_HEIGHT+DELTA+DELTA+MARGIN_HEIGHT);
        
        private BufferedImage m_image = null;
        


        
        public HSBPanel() {
            setPreferredSize(m_dimension);
            addMouseListener(this);
            addMouseMotionListener(this);
        }
        
        @Override
        public void paint(Graphics g) {
            if (m_imageUnset) {
                initImage();
                m_imageUnset = false;
            }
            
            Graphics2D g2d = (Graphics2D) g;
            
            g2d.drawImage(m_image, 0, 0, this);

            int x = MARGIN_HEIGHT + DELTA + Math.round((m_hue * MAIN_SIZE));
            int y = MARGIN_HEIGHT + DELTA + Math.round((m_saturation * MAIN_SIZE));
            g2d.setColor(Color.black);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setStroke(STROKE_3);
            g.drawOval(x-OVAL_SIZE/2, y-OVAL_SIZE/2, OVAL_SIZE, OVAL_SIZE);
            g2d.setColor(Color.white);
            g2d.setStroke(STROKE_1);
            g.drawOval(x-OVAL_SIZE/2, y-OVAL_SIZE/2, OVAL_SIZE, OVAL_SIZE);
            
        }
        
        private void initImage() {
            if (m_image == null) {
                m_image = new BufferedImage(m_dimension.width, m_dimension.height, BufferedImage.TYPE_INT_ARGB);
            }
            Graphics2D g2d = m_image.createGraphics();
            

            
            float b = m_brightness;
            int width = MAIN_SIZE;
            int height = MAIN_SIZE;
            for (int x=0;x<width;x++) {
                float h = ((float)x)/((float)width);
                if (h<0) h=0;
                else if (h>1) h=1;
                for (int y=0;y<height;y++) {
                    float s = ((float)y)/((float)height);
                    g2d.setColor(Color.getHSBColor(h, s, b));
                    g2d.drawLine(x+MARGIN_HEIGHT+DELTA, y+MARGIN_HEIGHT+DELTA, x+MARGIN_HEIGHT+DELTA, y+MARGIN_HEIGHT+DELTA);
                }
            }
            
            g2d.setColor(Color.black);
            g2d.drawRect(MARGIN_HEIGHT, MARGIN_HEIGHT, MAIN_SIZE+DELTA, MAIN_SIZE+DELTA);
        }
        
        @Override
        public void mouseClicked(MouseEvent e) {}

        @Override
        public void mousePressed(MouseEvent e) {
            changeHS(e.getX(), e.getY());
        }

        @Override
        public void mouseReleased(MouseEvent e) {
           changeHS(e.getX(), e.getY());
        }

        @Override
        public void mouseEntered(MouseEvent e) {}

        @Override
        public void mouseExited(MouseEvent e) {}

        @Override
        public void mouseDragged(MouseEvent e) {
            changeHS(e.getX(), e.getY());
        }

        @Override
        public void mouseMoved(MouseEvent e) {}
        
        private void changeHS(int x, int y) {
            
            if (x < MARGIN_HEIGHT + DELTA) {
                x = MARGIN_HEIGHT + DELTA;
            } else if (x > MARGIN_HEIGHT + DELTA + MAIN_SIZE) {
                x = MARGIN_HEIGHT + DELTA + MAIN_SIZE;
            }
            x -= (MARGIN_HEIGHT + DELTA);

            float h = ((float) x) / ((float) MAIN_SIZE);
            if (h<0) {
                h = 0;
            } else if (h>1) {
                h = 1;
            }
            
            if (y<MARGIN_HEIGHT + DELTA) {
                y = MARGIN_HEIGHT + DELTA;
            } else if (y>MARGIN_HEIGHT+DELTA+MAIN_SIZE) {
                y = MARGIN_HEIGHT+DELTA+MAIN_SIZE;
            }
            y -= (MARGIN_HEIGHT + DELTA);

            float s = ((float) y) / ((float) MAIN_SIZE);
            if (s<0) {
                s = 0;
            } else if (s>1) {
                s = 1;
            }
            
            propagateColorChanged(h, s, m_brightness);

        }
    }
    
    private class BrightnessSelectionPanel extends JPanel implements MouseListener, MouseMotionListener {
        
        
        
        private final int TRIANGLE_SIZE = 12;
        private final int COL_WIDTH = 20;
        private final int COL_HEIGHT = MAIN_SIZE;
        private final Dimension m_dimension = new Dimension(DELTA+COL_WIDTH+DELTA+TRIANGLE_SIZE, MARGIN_HEIGHT+DELTA+COL_HEIGHT+DELTA+MARGIN_HEIGHT);
        
        private int[] m_triangleX = new int[3];
        private int[] m_triangleY = new int[3];
        
        public BrightnessSelectionPanel() {
            setPreferredSize(m_dimension);
            addMouseListener(this);
            addMouseMotionListener(this);
        }
        
        @Override
        public void paint(Graphics g) {
            
            g.setColor(UIManager.getColor ("Panel.background"));
            g.fillRect(0, 0, getWidth(), getHeight());
            
            float h = m_hue;
            float s = m_saturation;
            for (int y = 0; y < COL_HEIGHT; y++) {
                float b = ((float) y) / ((float) COL_HEIGHT);
                if (b<0) b=0;
                else if (b>1) b=1;
                g.setColor(Color.getHSBColor(h, s, b));
                g.drawLine(DELTA,y+MARGIN_HEIGHT+DELTA,DELTA+COL_WIDTH,y+MARGIN_HEIGHT+DELTA);
            }
            
            int x = DELTA + COL_WIDTH + DELTA;
            int yBase = Math.round(m_brightness * COL_HEIGHT) + MARGIN_HEIGHT + DELTA;
            m_triangleX[0] = x+DELTA;
            m_triangleY[0] = yBase;
            m_triangleX[1] = x + DELTA + TRIANGLE_SIZE / 2;
            m_triangleY[1] = yBase - TRIANGLE_SIZE / 2;
            m_triangleX[2] = x + DELTA + TRIANGLE_SIZE / 2;
            m_triangleY[2] = yBase + TRIANGLE_SIZE / 2;

            g.setColor(Color.black);
            g.fillPolygon(m_triangleX, m_triangleY, m_triangleX.length);

            g.setColor(Color.black);
            g.drawRect(0, MARGIN_HEIGHT, DELTA+COL_WIDTH+DELTA, DELTA+COL_HEIGHT);

        }

        @Override
        public void mouseClicked(MouseEvent e) {}

        @Override
        public void mousePressed(MouseEvent e) {
            changeBrightness(e.getY());
        }

        @Override
        public void mouseReleased(MouseEvent e) {
           changeBrightness(e.getY());
        }

        @Override
        public void mouseEntered(MouseEvent e) {}

        @Override
        public void mouseExited(MouseEvent e) {}

        @Override
        public void mouseDragged(MouseEvent e) {
            changeBrightness(e.getY());
        }

        @Override
        public void mouseMoved(MouseEvent e) {}
        
        private void changeBrightness(int y) {
            if (y<MARGIN_HEIGHT + DELTA) {
                y = MARGIN_HEIGHT + DELTA;
            } else if (y>MARGIN_HEIGHT+DELTA+COL_HEIGHT) {
                y = MARGIN_HEIGHT+DELTA+COL_HEIGHT;
            }
            y -= (MARGIN_HEIGHT + DELTA);
            float b = ((float) y) / ((float) COL_HEIGHT);
            
            propagateColorChanged(m_hue, m_saturation, b);

        }
    }
    
    
    private class RGBSelectionPanel extends JPanel implements ColorChanged {
        
        private final JSlider m_RSlider;
        private final JSlider m_GSlider;
        private final JSlider m_BSlider;
        private final JSlider m_ASlider;
        
        private final JSpinner m_Rspinner;
        private final JSpinner m_Gspinner;
        private final JSpinner m_Bspinner;
        private final JSpinner m_Aspinner;
        
        
        private RGBSelectionPanel() {
            
            //setOpaque(true);
            //setBackground(Color.white);
 
            setLayout(new GridBagLayout());
            

            GridBagConstraints c = new GridBagConstraints();
            c.anchor = GridBagConstraints.NORTHWEST;
            c.fill = GridBagConstraints.BOTH;
            c.insets = new java.awt.Insets(5, 5, 0, 5);
            
            // create objects
            JLabel rLabel = new JLabel("Red :");
            JLabel gLabel = new JLabel("Green :");
            JLabel bLabel = new JLabel("Blue :");
            JLabel aLabel = new JLabel("Alpha :");
            
            m_RSlider = new JSlider(0,255,255);
            m_GSlider = new JSlider(0,255,255);
            m_BSlider = new JSlider(0,255,255);
            m_ASlider = new JSlider(0,255,255);
            

            m_Rspinner = new JSpinner(new SpinnerNumberModel(255, 0, 255, 1));
            m_Gspinner = new JSpinner(new SpinnerNumberModel(255, 0, 255, 1));
            m_Bspinner = new JSpinner(new SpinnerNumberModel(255, 0, 255, 1));
            m_Aspinner = new JSpinner(new SpinnerNumberModel(255, 0, 255, 1));
            
            // create actions
            ChangeListener sliderChangeListener = new ChangeListener() {

                @Override
                public void stateChanged(ChangeEvent e) {
                    propagateColorChanged(m_RSlider.getValue(), m_GSlider.getValue(), m_BSlider.getValue());
                }
                
            };
            
            m_RSlider.addChangeListener(sliderChangeListener);
            m_GSlider.addChangeListener(sliderChangeListener);
            m_BSlider.addChangeListener(sliderChangeListener);
            
            m_ASlider.addChangeListener(new ChangeListener() {

                @Override
                public void stateChanged(ChangeEvent e) {
                    propagateAlphaChanged(m_ASlider.getValue());
                }

            });
            
            ChangeListener spinnerChangeListener = new ChangeListener() {

                @Override
                public void stateChanged(ChangeEvent e) {
                    int r = ((SpinnerNumberModel) m_Rspinner.getModel()).getNumber().intValue();
                    int g = ((SpinnerNumberModel) m_Gspinner.getModel()).getNumber().intValue();
                    int b = ((SpinnerNumberModel) m_Bspinner.getModel()).getNumber().intValue();
                    propagateColorChanged(r, g, b);
                }
                
            };
            
            m_Rspinner.addChangeListener(spinnerChangeListener);
            m_Gspinner.addChangeListener(spinnerChangeListener);
            m_Bspinner.addChangeListener(spinnerChangeListener);
            
            m_Aspinner.addChangeListener(new ChangeListener() {

                @Override
                public void stateChanged(ChangeEvent e) {
                    int alpha = ((SpinnerNumberModel) m_Aspinner.getModel()).getNumber().intValue();
                    propagateAlphaChanged(alpha);
                }

            });
            
            // add objects to panel
            c.gridx = 0;
            c.gridy = 0;
            c.anchor = GridBagConstraints.NORTHEAST;
            add(rLabel, c);
            
            c.gridy++;
            add(gLabel, c);
            
            c.gridy++;
            add(bLabel, c);
            
            c.gridy++;
            add(aLabel, c);

            c.gridx++;
            c.gridy = 0;
            c.weightx = 1;
            c.anchor = GridBagConstraints.NORTHWEST;
            add(m_RSlider, c);

            c.gridy++;
            add(m_GSlider, c);
            
            c.gridy++;
            add(m_BSlider, c);
            
            c.gridy++;
            add(m_ASlider, c);
            
            c.gridx++;
            c.gridy = 0;
            c.weightx = 0;
            add(m_Rspinner, c);
            
            c.gridy++;
            add(m_Gspinner, c);
            
            c.gridy++;
            add(m_Bspinner, c);
            
            c.gridy++;
            add(m_Aspinner, c);
            
            c.gridx = 0;
            c.gridy++;
            c.gridwidth = 3;
            c.weightx = 1;
            c.weighty = 1;
            add(new SelectedColorPanel(), c);
            
        }

        @Override
        public void colorChanged() {
            m_RSlider.setValue(m_r);
            m_GSlider.setValue(m_g);
            m_BSlider.setValue(m_b);

            ((SpinnerNumberModel) m_Rspinner.getModel()).setValue(m_r);
            ((SpinnerNumberModel) m_Gspinner.getModel()).setValue(m_g);
            ((SpinnerNumberModel) m_Bspinner.getModel()).setValue(m_b);

        }

        @Override
        public void alphaChanged() {
            m_ASlider.setValue(m_alpha);
            ((SpinnerNumberModel) m_Aspinner.getModel()).setValue(m_alpha);
            repaint();
        }
    }
    
    private class SelectedColorPanel extends JPanel {

        private final int SQUARE_SIZE = 10;
        
        public SelectedColorPanel() {
            
        }
        
        @Override
        public void paint(Graphics g) {
            int height = getHeight();
            int width = getWidth();
            
            g.setColor(Color.white);
            g.fillRect(0,0,width-1, height-1);
            
            // draw a board of white and gray square
            g.setColor(Color.gray);
            int y = 0;
            while(true) {

                int x = 0;
                while (true) {

                    if ((x + y) % 2 == 0) {
                        // draw square
                        g.fillRect(x * SQUARE_SIZE, y * SQUARE_SIZE, SQUARE_SIZE, SQUARE_SIZE);
                    }

                    x++;
                    if (x * SQUARE_SIZE > width) {
                        break;
                    }
                }

                y++;
                if (y*SQUARE_SIZE>height) {
                    break;
                }
            }
            
            // draw the color with alpha channel
            g.setColor(new Color(m_r, m_g, m_b, m_alpha));
            g.fillRect(0,0,width-1, height-1);
            
            // draw a black frame
            g.setColor(Color.black);
            g.drawRect(0,0,width-1, height-1);
            
            
        }


        
    }
    
    
    private interface ColorChanged {
        public void colorChanged();
        public void alphaChanged();
    }
}
