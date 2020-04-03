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

import fr.proline.core.orm.util.TransientDataAllocationListener;
import fr.proline.core.orm.util.TransientDataInterface;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Get track of allocated TransientData to be able to free memory when there are
 * no longer used
 * 
 * @author Jean-Philippe
 */
public class TransientMemoryCacheManager implements TransientDataAllocationListener {
    
    protected static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.TransientMemoryCacheManager");
    
    private static TransientMemoryCacheManager m_singleton = null;

    public HashMap<TransientDataInterface, HashSet<TransientMemoryClientInterface>> m_cacheToClients = new HashMap<>();
    public HashMap<TransientMemoryClientInterface, HashSet<TransientDataInterface>> m_clientToCaches = new HashMap<>();
    
    public static TransientMemoryCacheManager getSingleton() {
        if (m_singleton == null) {
            m_singleton = new TransientMemoryCacheManager();
        }  
        return m_singleton;
    }
    
    @Override
    public void memoryAllocated(TransientDataInterface cache) {
        m_logger.debug("Cache Allocated "+cache);
        
        if (m_cacheToClients.containsKey(cache)) {
            return;
        }
        m_cacheToClients.put(cache, new HashSet<TransientMemoryClientInterface>());

    }
    
    public void linkCache(TransientMemoryClientInterface client, TransientDataInterface cache) {
        
        if (cache == null) {
            return;
        }
        
        m_logger.debug("Link Cache "+cache);
        
        HashSet<TransientMemoryClientInterface> clientSet = m_cacheToClients.get(cache);
        if (clientSet == null) {
            clientSet = new HashSet<>();
            m_cacheToClients.put(cache, clientSet);
        }
        clientSet.add(client);
        
        HashSet<TransientDataInterface> cacheSet = m_clientToCaches.get(client);
        if (cacheSet == null) {
            cacheSet = new HashSet<>();
            m_clientToCaches.put(client, cacheSet);
        }
        cacheSet.add(cache);
    }
    
    public void unlinkCache(TransientMemoryClientInterface client) {
        
        HashSet<TransientDataInterface> cacheSet = m_clientToCaches.get(client);
        if (cacheSet == null) {
            return;
        }
        
        m_logger.debug("Unlink Cache "+client);
        
        m_clientToCaches.remove(client);
        for (TransientDataInterface cache : cacheSet) {
            HashSet<TransientMemoryClientInterface> clientSet = m_cacheToClients.get(cache);
            if (clientSet != null) {
                clientSet.remove(client);
            }
            
        }

    }
    
    public void freeUnusedCache() {
        m_logger.debug("Free unused Caches");
        
        HashSet<TransientDataInterface> tmpSet = new HashSet<>(m_cacheToClients.keySet());
        for (TransientDataInterface cache : tmpSet) {
            if (m_cacheToClients.get(cache).isEmpty()) {
                cache.clearMemory();
                m_cacheToClients.remove(cache);
            }
        }
    }
    
    public ArrayList<String> getFreeCacheList() {
        
        return getCacheList(true);
    }

    public ArrayList<String> getUsedCacheList() {
 
        return getCacheList(false);
    }

    private ArrayList<String> getCacheList(boolean free) {
        ArrayList<String> cacheList = new ArrayList<>();
        for (TransientDataInterface cache : m_cacheToClients.keySet()) {
            if (free && m_cacheToClients.get(cache).isEmpty()) {
                cacheList.add(cache.getMemoryName());
            } else if (!free && !m_cacheToClients.get(cache).isEmpty()) {
                for (TransientMemoryClientInterface client : m_cacheToClients.get(cache)) {
                    String clientName = client.getMemoryClientName();
                    cacheList.add(clientName+": "+cache.getMemoryName());
                }
            }
        }
        return cacheList;
    }

    
}
