/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.gui;

import java.util.HashMap;

/**
 *
 * @author AK249877
 */
public interface DropZoneInterface {
    
    public void addSample(Object o);
    
    public void addSamples(Object o);
    
    public void removeSample(Object key);
    
    public void removeAllSamples();
    
    public HashMap getAllSamples();
    
    public void clearDropZone();
    
}
