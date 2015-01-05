package fr.proline.studio.graphics;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JColorChooser;

/**
 *
 * @author JM235353
 */
public class ColorButton extends JButton {

    private static final int WIDTH = 100;
    private static final int HEIGHT = 35;

    private final Dimension m_dimension = new Dimension(WIDTH, HEIGHT);

    public ColorButton() {
    }
    
    public void initActionListener() {
        addActionListener(getActionListener());
    }
    
    protected ActionListener getActionListener() {
        
        final ColorButton colorButton = this;
        ActionListener a = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {


                Color newColor = JColorChooser.showDialog(colorButton, null, colorButton.getBackground());
                if (newColor != null) {
                    colorButton.setBackground(newColor);
                }
            }
        };
        return a;
    }

    public void setColor(Color c) {
        setBackground(c);
    }
    
    public Color getColor() {
        return getBackground();
    }

    @Override
    public Dimension getPreferredSize() {
        return m_dimension;
    }

    @Override
    protected void paintComponent(Graphics g) {
        g.setColor(Color.white); // necessary for transparent colors
        g.fillRect(0, 0, getWidth(), getHeight());

        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());

        g.setColor(Color.darkGray);
        g.drawRect(0, 0, getWidth(), getHeight());

    }
}
