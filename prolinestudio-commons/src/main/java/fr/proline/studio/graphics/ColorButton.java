package fr.proline.studio.graphics;

import fr.proline.studio.graphics.colorpicker.ColorDataInterface;
import fr.proline.studio.graphics.colorpicker.ColorPickerDialog;
import fr.proline.studio.gui.DefaultDialog;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.JButton;
import org.openide.windows.WindowManager;

/**
 *
 * @author JM235353
 */
public class ColorButton extends JButton implements ColorDataInterface {

    private static final int WIDTH = 135;
    private static final int HEIGHT = 35;

    private final Dimension m_dimension = new Dimension(WIDTH, HEIGHT);

    private ArrayList<ColorDataInterface> m_listeners = null;
    
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

                ColorPickerDialog colorPickerDialog = new ColorPickerDialog(WindowManager.getDefault().getMainWindow(), colorButton.getBackground());
                colorPickerDialog.setLocationRelativeTo(colorButton);
                colorPickerDialog.setVisible(true);
                if (colorPickerDialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {
                    colorButton.setBackground(colorPickerDialog.getColor());
                    colorChangedToListeners();
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
        
        int height = getHeight();
        int width = getWidth();
        
        // white background
        g.setColor(Color.white); // necessary for transparent colors
        g.fillRect(0, 0, width, height);

        // draw a board with gray squares
        g.setColor(Color.gray);
        int y = 0;
        while (true) {

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
            if (y * SQUARE_SIZE > height) {
                break;
            }
        }
        
        g.setColor(getBackground());
        g.fillRect(0, 0, width, height);

        g.setColor(Color.darkGray);
        g.drawRect(0, 0, width-1, height-1);

    }
    private final int SQUARE_SIZE = 10;

    @Override
    public void propagateColorChanged(int r, int g, int b) {
        setBackground(new Color(r,g,b, getBackground().getAlpha()));
    }

    @Override
    public int getRed() {
        return getBackground().getRed();
    }

    @Override
    public int getGreen() {
        return getBackground().getGreen();
    }

    @Override
    public int getBlue() {
        return getBackground().getBlue();
    }

    @Override
    public void addListener(ColorDataInterface colorDataInterface) {
        if (m_listeners == null) {
            m_listeners = new ArrayList<>();
        }
        m_listeners.add(colorDataInterface);
    }
    
    public void colorChangedToListeners() {
        if (m_listeners == null) {
            return;
        }
        for (ColorDataInterface cdi : m_listeners) {
            cdi.propagateColorChanged(getRed(), getGreen(), getBlue());
        }
    }
}
