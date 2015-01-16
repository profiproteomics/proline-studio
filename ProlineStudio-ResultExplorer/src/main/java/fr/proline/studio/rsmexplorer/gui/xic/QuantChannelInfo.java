/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.gui.xic;

import fr.proline.core.orm.uds.dto.DQuantitationChannel;
import fr.proline.studio.utils.CyclicColorPalette;
import java.awt.Color;

/**
 * map quantChannel info and map info, especilaly to maintain the same colors between the maps and the quantChannels
 * @author MB243701
 */
public class QuantChannelInfo {
    
    private DQuantitationChannel[] m_quantChannels = null;

    
    public QuantChannelInfo(DQuantitationChannel[] quantChannels) {
        this.m_quantChannels = quantChannels;
    }

    public DQuantitationChannel[] getQuantChannels() {
        return m_quantChannels;
    }

    public void setQuantChannels(DQuantitationChannel[] quantChannels) {
        this.m_quantChannels = quantChannels;
    }
    
    
    public String getRsmHtmlColor(int quantChIndex) {
        return  CyclicColorPalette.getHTMLColor(quantChIndex);
    }
    
    public Color getMapColor (String mapName) {
        for(int i=0; i<m_quantChannels.length; i++) {
            String rawFileName = m_quantChannels[i].getRawFileName();
            int id = rawFileName.indexOf('.') ;
            if (id != -1) {
                rawFileName = rawFileName.substring(0, id);
            }
            if (rawFileName.equals(mapName)) {
                return CyclicColorPalette.getColor(i);
            }
        }
        return CyclicColorPalette.getColor(m_quantChannels.length);
    }
    
    public Color getMapColor (Long mapId) {
        for(int i=0; i<m_quantChannels.length; i++) {
            Long lcmsRawMapId = m_quantChannels[i].getLcmsRawMapId();
            if (lcmsRawMapId != null && lcmsRawMapId.equals(mapId)) {
                return CyclicColorPalette.getColor(i);
            }
        }
        return CyclicColorPalette.getColor(m_quantChannels.length);
    }
    
    public String getMapTitle (Long mapId) {
        for (DQuantitationChannel m_quantChannel : m_quantChannels) {
            Long lcmsRawMapId = m_quantChannel.getLcmsRawMapId();
            if (lcmsRawMapId != null && lcmsRawMapId.equals(mapId)) {
                return m_quantChannel.getResultFileName();
            }
        }
        return null;
    }
    
        
}
