package fr.proline.mzscope.ui.event;

import java.util.EventListener;

/**
 *
 * @author MB243701
 */
public interface ExtractionStateListener extends EventListener {

   public void extractionStateChanged(ExtractionEvent event);
   
}
