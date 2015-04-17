package fr.proline.studio.rsmexplorer.gui.calc.graphui;

import fr.proline.studio.comparedata.AbstractJoinDataModel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;

/**
 *
 * @author JM235353
 */
public class KeyLinkWidget {
        
        private final DataWidget m_dataRepresentation1;
        private final DataWidget m_dataRepresentation2;
        
        private final Point m_p1 = new Point();
        private final Point m_p2 = new Point();
        
        private final AbstractJoinDataModel m_joinModel;
        
         public KeyLinkWidget(DataWidget dataRepresentation1, DataWidget dataRepresentation2, AbstractJoinDataModel joinModel) {
            m_dataRepresentation1 = dataRepresentation1;
            m_dataRepresentation2 = dataRepresentation2;
            m_joinModel = joinModel;
        }
         
         public void draw(Graphics g) {
             g.setColor(Color.blue);
             
             m_dataRepresentation1.getKeyPosition(m_joinModel.getSelectedKey1(), m_p1);
             m_dataRepresentation2.getKeyPosition(m_joinModel.getSelectedKey1(), m_p2);
             
             g.drawLine(m_p1.x, m_p1.y, m_p2.x, m_p2.y);
         }
    }