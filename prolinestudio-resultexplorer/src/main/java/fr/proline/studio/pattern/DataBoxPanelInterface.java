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
package fr.proline.studio.pattern;

import fr.proline.studio.gui.SplittedPanelContainer;

/**
 * All panels which are linked to a databox must implement this interface
 * 3 abstract method are relative to task loading, loaded progress information
 *
 * @author JM235353
 */
public interface DataBoxPanelInterface extends SplittedPanelContainer.UserActions {

    public void addSingleValue(Object v);

    public void setDataBox(AbstractDataBox dataBox);

    public AbstractDataBox getDataBox();

    /**
     * set progress status
     * see setLoading(id, calculation =false) calculating = false
     *
     * @param id, numero de task
     */
    public void setLoading(int id);

    /**
     * set progress status
     * task=id begin with calculating state
     *
     * @param id
     * @param calculating, task = id, it's state of calculate
     */
    public void setLoading(int id, boolean calculating);

    /**
     * set progress status
     * task = id, finished
     *
     * @param id
     */
    public void setLoaded(int id);

}
