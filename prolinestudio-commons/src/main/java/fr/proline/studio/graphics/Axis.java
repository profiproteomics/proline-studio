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
package fr.proline.studio.graphics;

import fr.proline.studio.graphics.cursor.AbstractCursor;
import fr.proline.studio.gui.InfoDialog;
import fr.proline.studio.utils.IconManager;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.openide.windows.WindowManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for axis
 *
 * @author JM235353
 */
public abstract class Axis {

    protected static final Stroke DASHED = new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER, 10.0f, new float[]{2.0f, 2.0f}, 0.0f);
    protected static final Logger m_logger = LoggerFactory.getLogger(Axis.class);
    protected String m_title = null;
    protected Font m_titleFont = null;
    protected FontMetrics m_titleFontMetrics;
    protected Font m_valuesFont = null;
    protected FontMetrics m_valuesFontMetrics = null;
    protected Font m_valuesDiagonalFont = null;

    protected int m_x;
    protected int m_y;
    protected int m_width;
    protected int m_height;

    // value of data
    protected double m_minValue;
    protected double m_maxValue;
    protected double m_lockedMinValue = Double.NaN;

    // Min tick and max tick
    protected double m_minTick;
    protected double m_maxTick;
    protected double m_tickSpacing;

    protected DecimalFormat m_df;
    protected DecimalFormat m_dfPlot;
    protected int m_fractionalDigits = -1;
    protected int m_integerDigits = -1;
    protected boolean m_log = false;
    protected boolean m_selected = false;
    protected boolean m_isInteger = false;
    protected boolean m_isEnum = false;
    protected boolean m_isPixel = false;

    protected BasePlotPanel m_plotPanel;

    protected AxisRangePanel m_rangePanel;
    protected boolean m_mustDrawDiagonalLabels = false;
    protected int m_labelMinWidth = 0;
    protected int m_labelMaxWidth = 0;
    protected int m_minimumAxisHeight = 0;
    protected int m_minimumAxisWidth = 0;

    protected static final double LOG_MIN_VALUE = 10e-9;
            
    public boolean displayAxis() {
        return !m_isPixel;
    }

    /**
     * a small panel can be defined by user the Min Max of the axis
     */
    public static class AxisRangePanel extends JPanel {

        private JTextField m_minTextField;
        private JTextField m_maxTextField;
        private Axis m_axis;

        public AxisRangePanel(Axis axis) {

            m_axis = axis;
            setBorder(BorderFactory.createLineBorder(Color.darkGray, 1, true));
            setOpaque(true);
            setLayout(new FlowLayout());

            JButton closeButton = new JButton(IconManager.getIcon(IconManager.IconType.CROSS_SMALL7));
            closeButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
            //closeButton.setBorderPainted(false);
            closeButton.setFocusPainted(false);
            closeButton.setContentAreaFilled(false);

            closeButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setVisible(false);
                }
            });

            m_minTextField = new JTextField(8);
            m_minTextField.setToolTipText("Set axis lower bound");
            m_minTextField.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    updateRange();
                }
            });

            m_maxTextField = new JTextField(8);
            m_maxTextField.setToolTipText("Set axis upper bound");
            m_maxTextField.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    updateRange();
                }
            });

            add(closeButton);
            add(m_minTextField);
            add(new JLabel(" to "));
            add(m_maxTextField);

            Dimension d = getPreferredSize();
            setBounds(0, 0, (int) d.getWidth(), (int) d.getHeight());

            MouseAdapter dragGestureAdapter = new MouseAdapter() {
                int dX, dY;

                @Override
                public void mouseDragged(MouseEvent e) {
                    Component panel = e.getComponent();

                    int newX = e.getLocationOnScreen().x - dX;
                    int newY = e.getLocationOnScreen().y - dY;

                    Component parentComponent = panel.getParent();
                    int parentX = parentComponent.getX();
                    if (newX < parentX) {
                        newX = parentX;
                    }
                    int parentY = parentComponent.getY();
                    if (newY < parentY) {
                        newY = parentY;
                    }
                    int parentWidth = parentComponent.getWidth();
                    if (newX + panel.getWidth() > parentWidth - parentX) {
                        newX = parentWidth - parentX - panel.getWidth();
                    }
                    int parentHeight = parentComponent.getHeight();
                    if (newY + panel.getHeight() > parentHeight - parentY) {
                        newY = parentHeight - parentY - panel.getHeight();
                    }

                    panel.setLocation(newX, newY);

                    dX = e.getLocationOnScreen().x - panel.getX();
                    dY = e.getLocationOnScreen().y - panel.getY();
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    JPanel panel = (JPanel) e.getComponent();
                    panel.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                    dX = e.getLocationOnScreen().x - panel.getX();
                    dY = e.getLocationOnScreen().y - panel.getY();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    JPanel panel = (JPanel) e.getComponent();
                    panel.setCursor(null);
                }
            };

            addMouseMotionListener(dragGestureAdapter);
            addMouseListener(dragGestureAdapter);

            setVisible(false);
        }

        private void updateRange() {
            if (m_maxTextField.getText().isEmpty() || m_minTextField.getText().isEmpty()) {
                return;
            }

            double max;
            double min;
            try {
                max = Double.parseDouble(m_maxTextField.getText().trim());
                min = Double.parseDouble(m_minTextField.getText().trim());
            } catch (NumberFormatException nfe) {
                InfoDialog errorDialog = new InfoDialog(WindowManager.getDefault().getMainWindow(), InfoDialog.InfoType.WARNING, "Error", "Min or max value contains non numerical values.");
                errorDialog.setButtonVisible(InfoDialog.BUTTON_CANCEL, false);
                errorDialog.centerToWindow(WindowManager.getDefault().getMainWindow());
                errorDialog.setVisible(true);
                return;
            }

            if (min < max) {
                if (m_axis.isLog()) {
                    min = Math.pow(10, min);
                    max = Math.pow(10, max);
                }
                m_axis.setRange(min, max);
                m_axis.m_plotPanel.repaintUpdateDoubleBuffer();
            } else {
                InfoDialog errorDialog = new InfoDialog(WindowManager.getDefault().getMainWindow(), InfoDialog.InfoType.WARNING, "Error", "Min value cannot be greater than or equal to the max value.");
                errorDialog.setButtonVisible(InfoDialog.BUTTON_CANCEL, false);
                errorDialog.centerToWindow(WindowManager.getDefault().getMainWindow());
                errorDialog.setVisible(true);
            }
        }
    }

    public Axis(BasePlotPanel p) {
        m_plotPanel = p;
        m_rangePanel = new AxisRangePanel(this);
    }

    public int getX() {
        return m_x;
    }

    public int getY() {
        return m_y;
    }

    public BasePlotPanel getPlotPanel() {
        return m_plotPanel;
    }

    public AxisRangePanel getRangePanel() {
        return m_rangePanel;
    }

    public int getMiniMumAxisHeight(int minHeightFromParent) {
        return StrictMath.max(m_minimumAxisHeight, minHeightFromParent);
    }

    public void setSpecificities(boolean isInteger, boolean isEnum, boolean isPixel) {
        m_isInteger = isInteger;
        m_isEnum = isEnum;
        m_isPixel = isPixel;
    }

    public boolean isEnum() {
        return m_isEnum;
    }

    public void setLog(boolean log) {
        m_log = log;
        m_df = null; // reinit for display
        
        m_plotPanel.updatePlotsForLog();
    }

    public boolean isLog() {
        return m_log;
    }

    public boolean canBeInLog() {
        return (!m_isEnum);
    }

    public void setSize(int x, int y, int width, int height) {
        m_x = x;
        m_y = y;
        m_width = width;
        m_height = height;
    }

    public void setRange(double min, double max) {
        if (!Double.isNaN(m_lockedMinValue) && (min < m_lockedMinValue) && m_lockedMinValue < max) {
            m_minValue = m_lockedMinValue;
        } else {
            m_minValue = min;
        }
        m_maxValue = max;
        m_minTick = m_minValue;
        m_maxTick = max;
    }

    public void lockMinValue(Double min) {
        m_lockedMinValue = min;
    }

    public void setTitle(String title) {
        m_title = title;
    }

    public String getTitle() {
        return m_title;
    }

    public boolean setSelected(boolean v) {
        boolean changed = v ^ m_selected;
        m_selected = v;
        return changed;
    }

    public boolean isSelected() {
        return m_selected;
    }

    public abstract void paint(Graphics2D g);

    public abstract void paintCursor(Graphics2D g, AbstractCursor cursor, boolean selected);

    public abstract int valueToPixel(double v);

    public abstract double pixelToValue(int pixel);
    
    public abstract double deltaPixelToDeltaValue(int deltaPixel);
    
    public abstract double deltaPixelToLogMultValue(int deltaPixel);

    public double getMinValue() {
        if ((m_log) && (m_minValue<=LOG_MIN_VALUE)) {
            return LOG_MIN_VALUE;
        }
        return m_minValue;
    }

    public double getMaxValue() {
        if ((m_log) && (m_maxValue<=LOG_MIN_VALUE*10)) {
            return LOG_MIN_VALUE*10;
        }
        return m_maxValue;
    }

    public double getMinTick() {
        if (m_log) {
            return Math.pow(10, m_minTick);
        }
        return m_minTick;
    }

    public double getMaxTick() {
        if (m_log) {
            return Math.pow(10, m_maxTick);
        }
        return m_maxTick;
    }

    public int getWidth() {
        return m_width;
    }

    public int getHeight() {
        return m_height;
    }

    public boolean inside(int x, int y) {
        return (x >= m_x) && (x < m_x + m_width) && (y > m_y) && (y < m_y + m_height);
    }

    protected DecimalFormat selectDecimalFormat(int fractionalDigits, int integerDigits) {
        String pattern;
        if (m_log) {
            pattern = ("0.0E0");
        } else if (fractionalDigits <= 0) {
            // integer value
            if (integerDigits > 4) {
                pattern = ("0.0E0"); // scientific notation for numbers with too much integerDigits "66666"
            } else {
                pattern = "#";  // number like "3"
            }
        } else if (fractionalDigits > 3) { // 3 is 
            pattern = ("0.0"); // scientific notation for numbers with too much fractionalDigits "0.0000532"
            fractionalDigits--;
            while (fractionalDigits > 0) {
                pattern += "#";
                fractionalDigits--;
            }
            pattern += "E0";
        } else {
            pattern = "#.";
            while (fractionalDigits > 0) {
                pattern += "#";
                fractionalDigits--;
            }
        }

        DecimalFormat df = new DecimalFormat(pattern);
        DecimalFormatSymbols sym = DecimalFormatSymbols.getInstance();
        sym.setDecimalSeparator('.');
        df.setDecimalFormatSymbols(sym);

        return new DecimalFormat(pattern);
    }

    protected DecimalFormat selectLogDecimalFormat() {
        return new DecimalFormat("0.0E0");
    }

    public DecimalFormat getExternalDecimalFormat() {
        return m_dfPlot;
    }

    public interface EnumXInterface {

        public String getEnumValueX(int index, boolean fromData);
    }

    public interface EnumYInterface {

        public String getEnumValueY(int index, boolean fromData);
        public String getEnumValueY(int index, boolean fromData, Axis axis);
    }
}
