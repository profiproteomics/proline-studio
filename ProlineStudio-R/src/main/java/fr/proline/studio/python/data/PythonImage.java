package fr.proline.studio.python.data;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.python.core.PyObject;

/**
 *
 * @author jm235353
 */
public class PythonImage extends PyObject {

    private BufferedImage m_img = null;
    
    public PythonImage(String filePath) {
        try {
            m_img = ImageIO.read(new File(filePath));
        } catch (IOException e) {
        }
    }
    
    public BufferedImage getImage() {
        return m_img;
    }
}
