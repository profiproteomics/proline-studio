/* 
 * Copyright (C) 2019
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
package fr.proline.studio.pattern.extradata;

/**
 *
 * Used to give specific parameters to next graphic databox
 * 
 * @author JM235353
 */
public class GraphicExtraData {
    
    private final Boolean m_keepZoom;
    private final Double m_limitMinY;
    
    public GraphicExtraData(Boolean keepZoom, Double limitMinY) {
        m_keepZoom = keepZoom;
        m_limitMinY = limitMinY;
    }
    
    public Boolean getKeepZoom() {
        return m_keepZoom;
    }
    
    public Double getLimitMinY() {
        return m_limitMinY;
    }
}
