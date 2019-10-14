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
package fr.proline.mzscope.ui.event;

import java.util.EventListener;

/**
 * scan header events (from the HeaderSpectrumPanel)
 *
 * @author MB243701
 */
public interface ScanHeaderListener extends EventListener {

    /**
     * update the scan index value
     *
     * @param scanIndex new scanIndex value
     */
    public void updateScanIndex(Integer scanIndex);

    /**
     * update the retention time value (in sec)
     *
     * @param retentionTime the new retention time value (in sec)
     */
    public void updateRetentionTime(float retentionTime);

    /**
     * keep the msLevel or not while changing the scanIndex value
     * @param keep 
     */
    public void keepMsLevel(boolean keep);
    
 
}
