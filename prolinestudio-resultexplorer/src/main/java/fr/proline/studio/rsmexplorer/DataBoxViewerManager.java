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
package fr.proline.studio.rsmexplorer;

import fr.proline.studio.WindowManager;
import fr.proline.studio.dock.AbstractTopPanel;

import java.util.*;

/**
 *
 * @author JM235353
 */
public class DataBoxViewerManager {

    public static final String MODIFIED_TITLE_SUFFIX ="***";

    public enum REASON_MODIF{
        REASON_PEPTIDE_SUPPRESSED((byte) 1,true),
        REASON_PROTEINS_REFINED((byte)2,false),
        REASON_PTMCLUSTER_MERGED((byte)4, true),
        REASON_PTMCLUSTER_MODIFIED((byte)8,true),
        REASON_PTMDATASET_SAVED((byte)16, false);

        private byte m_reasonValue;
        private boolean m_shouldSave;

        REASON_MODIF(byte reasonValue, boolean shouldSave){
            m_reasonValue = reasonValue;
            m_shouldSave = shouldSave;
        }

        public byte getReasonValue(){
            return m_reasonValue;
        }

        public static  REASON_MODIF getReasonModifFor(byte value){
            REASON_MODIF[]  allVals = REASON_MODIF.values();
            for (REASON_MODIF allVal : allVals) {
                if (isReasonDefine(allVal, value))
                    return allVal;
            }
            return null;
        }

        public boolean shouldBeSaved(){
            return m_shouldSave;
        }

        public static boolean isReasonDefine(REASON_MODIF reason, byte value){
            return ( (reason.getReasonValue() & value) == reason.getReasonValue());
        }

    }

    public static void loadedDataModified(long projectId, Long rsetId, Long rsmId, Class c, ArrayList modificationsList, byte reason) {

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
