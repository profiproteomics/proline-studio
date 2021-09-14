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
package fr.proline.studio.rsmexplorer;

import fr.proline.studio.WindowManager;
import fr.proline.studio.dock.AbstractTopPanel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

/**
 *
 * @author JM235353
 */
public class DataBoxViewerManager {

    public static final int REASON_PEPTIDE_SUPPRESSED = 0;
    public static final int REASON_PROTEINS_REFINED = 1;
    
    public static void loadedDataModified(long projectId, Long rsetId, Long rsmId, Class c, ArrayList modificationsList, int reason) {

        Set<AbstractTopPanel> tcs = WindowManager.getDefault().getMainWindow().getTopPanels();
        Iterator<AbstractTopPanel> itTop = tcs.iterator();
        while (itTop.hasNext()) {
            AbstractTopPanel topComponent = itTop.next();
            if (topComponent instanceof DataBoxViewerTopPanel) {

                DataBoxViewerTopPanel databoxViewerTP = ((DataBoxViewerTopPanel) topComponent);

                long pId = databoxViewerTP.getProjectId();
                if (pId != projectId) {
                    continue;
                }

                databoxViewerTP.loadedDataModified(rsetId, rsmId, c, modificationsList, reason);

            }
        }
    }
}
