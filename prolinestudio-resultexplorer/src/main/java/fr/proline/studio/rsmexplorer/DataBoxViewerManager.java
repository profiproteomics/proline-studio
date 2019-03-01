package fr.proline.studio.rsmexplorer;

import java.util.ArrayList;
import java.util.Iterator;
import org.openide.windows.TopComponent;

/**
 *
 * @author JM235353
 */
public class DataBoxViewerManager {

    public static final int REASON_PEPTIDE_SUPPRESSED = 0;
    public static final int REASON_PROTEINS_REFINED = 1;
    
    public static void loadedDataModified(long projectId, Long rsetId, Long rsmId, Class c, ArrayList modificationsList, int reason) {

        Iterator<TopComponent> itTop = TopComponent.getRegistry().getOpened().iterator();
        while (itTop.hasNext()) {
            TopComponent topComponent = itTop.next();
            if (topComponent instanceof DataBoxViewerTopComponent) {

                DataBoxViewerTopComponent databoxViewerTP = ((DataBoxViewerTopComponent) topComponent);

                long pId = databoxViewerTP.getProjectId();
                if (pId != projectId) {
                    continue;
                }

                databoxViewerTP.loadedDataModified(rsetId, rsmId, c, modificationsList, reason);

            }
        }
    }
}
