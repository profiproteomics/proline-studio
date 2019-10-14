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
package fr.proline.mzscope.ui;

import fr.proline.mzscope.model.IFeature;

/**
 * feature/peak selected in the FeatureTable and should be display 
 * @author MB243701
 */
public interface IFeatureViewer {
    /**
     * display Feature in the raw file corresponding to the rawFile
     * @param f 
     */
    public void displayFeatureInRawFile(IFeature f);
    
    /**
     * display Feature in the current raw file
     * @param f 
     */
    public void displayFeatureInCurrentRawFile(IFeature f);
}
