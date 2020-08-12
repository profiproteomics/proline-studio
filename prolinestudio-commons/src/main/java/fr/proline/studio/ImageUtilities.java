package fr.proline.studio;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class ImageUtilities {

    public static ImageIcon loadImageIcon(String path, boolean b) {
        try {
            return new ImageIcon(ImageIO.read(ClassLoader.getSystemResource(path)));
        } catch (IOException e) {

        }
        return null;
    }

    public static Image loadImage(String path, boolean b) {
        try {
            return ImageIO.read(ClassLoader.getSystemResource("image/button1.png"));
        } catch (IOException e) {

        }
        return null;
    }


}
