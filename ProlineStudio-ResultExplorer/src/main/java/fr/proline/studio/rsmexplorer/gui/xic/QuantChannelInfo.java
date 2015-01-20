/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.gui.xic;

import fr.proline.core.orm.lcms.Feature;
import fr.proline.core.orm.uds.dto.DQuantitationChannel;
import fr.proline.studio.utils.CyclicColorPalette;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    
    
    public Color getMapColor (Long mapId) {
        for(int i=0; i<m_quantChannels.length; i++) {
            if (compareMap( m_quantChannels[i], mapId)) {
                return CyclicColorPalette.getColor(i);
            }
        }
        return CyclicColorPalette.getColor(m_quantChannels.length);
    }
    
    public String getMapTitle (Long mapId) {
        for (DQuantitationChannel m_quantChannel : m_quantChannels) {
            if (compareMap( m_quantChannel, mapId)) {
                return m_quantChannel.getResultFileName();
            }
        }
        return null;
    }
    
    private int getQCId(long mapId) {
        int id = 0;
        for (DQuantitationChannel m_quantChannel : m_quantChannels) {
            if (compareMap( m_quantChannel, mapId)) {
                return id;
            }
            id++;
        }
        return -1;
    }
    
    private boolean compareMap(DQuantitationChannel m_quantChannel, Long mapId) {
        Long lcmsRawMapId = m_quantChannel.getLcmsRawMapId();
        Long lcmsMapId = m_quantChannel.getLcmsMapId();
        if (lcmsRawMapId != null && lcmsRawMapId.equals(mapId)) {
            return true;
        }
        if (lcmsMapId != null && lcmsMapId.equals(mapId)) {
            return true;
        }
        return false;
    }
    
        
}
