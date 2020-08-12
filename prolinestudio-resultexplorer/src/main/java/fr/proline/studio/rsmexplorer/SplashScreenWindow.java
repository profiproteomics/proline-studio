package fr.proline.studio.rsmexplorer;

import fr.proline.studio.utils.IconManager;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class SplashScreenWindow extends JWindow {

    private Image m_image = null;

    public SplashScreenWindow() {
        super();

        m_image = IconManager.getImage(IconManager.IconType.SPLASH);

        int width = m_image.getWidth(null);
        int height = m_image.getHeight(null);
        setSize(new Dimension(width, height));

        setLocationRelativeTo(null);
        setAlwaysOnTop(true);


    }

    public void paint(Graphics g){

        g.drawImage(m_image,0,0,null);
    }
}