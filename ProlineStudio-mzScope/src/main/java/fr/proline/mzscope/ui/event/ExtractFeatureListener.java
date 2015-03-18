/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.mzscope.ui.event;

import java.util.EventListener;

/**
 *
 * @author MB243701
 */
public interface ExtractFeatureListener extends EventListener {
    /**
     * set the features extraction and the peakels detection
     * @param extractFeatures
     * @param detectPeakels 
     */
    public void extractFeatureListener(boolean extractFeatures, boolean detectPeakels);
}
