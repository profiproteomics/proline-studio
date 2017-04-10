/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.wizard;

import java.io.File;

/**
 *
 * @author AK249877
 */
public interface ConversionListener {
    
    public void ConversionPerformed(File f, ConversionSettings settings);
    
}
