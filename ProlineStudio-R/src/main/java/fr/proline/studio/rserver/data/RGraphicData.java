package fr.proline.studio.rserver.data;

import java.awt.Image;


/**
 *
 * @author JM235353
 */
public class RGraphicData extends AbstractRData {

    private Image m_image;
    
    public RGraphicData(String name) {
        m_dataType = AbstractRData.DataTypes.GRAPHIC;
        m_name = name;
    }
    
    public void setImage(Image image) {
        m_image = image;
    }
    
    public Image getImage() {
        return m_image;
    } 
}
