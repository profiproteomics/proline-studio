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
package fr.proline.studio.python.data;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.python.core.PyObject;

/**
 * Python Encapsulation of an image 
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
