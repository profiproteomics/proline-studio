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
package fr.proline.studio.dam.memory;

/**
 *
 * @author Jean-Philippe
 */
public class MemoryReference implements Comparable<MemoryReference> {

    private String m_cacheName;
    private String m_clientName;

    public MemoryReference(String cacheName, String clientName) {
        m_cacheName = cacheName;
        m_clientName = clientName;
    }

    public String getCacheName() {
        return m_cacheName;
    }

    public String getClientName() {
        return m_clientName;
    }

    @Override
    public int compareTo(MemoryReference o) {
        int cmp = m_cacheName.compareTo(o.m_cacheName);
        if (cmp != 0) {
            return cmp;
        }

        if (m_clientName == null && o.m_clientName == null){
            return 0;
        } else if(m_clientName == null){
            return 1;
        } else if(o.m_clientName == null){
            return -1;
        }

        return m_clientName.compareTo(o.m_clientName);
    }

}
