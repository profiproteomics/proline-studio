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
package fr.proline.studio.rsmexplorer.tree.xic;

import fr.proline.studio.dam.data.AbstractData;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.utils.IconManager;
import javax.swing.ImageIcon;
import org.openide.nodes.Sheet;

/**
 * Tree Node representing a Dataset and RSM used as reference for XIC : Creating
 * XIC from identification Tree or cloning an existing XIC which have DS & RSM
 * references defines
 *
 * @author JM235353
 */
public class DatasetReferenceNode extends AbstractNode {

    private boolean m_invalidReference = false; // define if the reference DS has been modified since XIC was run
    private boolean m_isAggregation;

    public DatasetReferenceNode(AbstractData data) {
        super(NodeTypes.DATASET_REFERENCE, data);
        this.m_isAggregation = false;
    }

  public DatasetReferenceNode(AbstractData data, boolean isAggragation) {
        super(NodeTypes.DATASET_REFERENCE, data);
        this.m_isAggregation = isAggragation;
    }
    public void setInvalidReference(boolean isIncorrect) {
        m_invalidReference = isIncorrect;
    }

    public boolean isInvalidReference() {
        return m_invalidReference;
    }

    @Override
    public String toString() {
        if (m_isAggregation) {
            return super.toString();
        } else {
            return "Identification reference : " + super.toString();
        }
    }

    @Override
    public ImageIcon getIcon(boolean expanded) {
        if (m_invalidReference) {
            return getIcon(IconManager.IconType.REFERENCE_RSM_ERR);
        } else {
            if (m_isAggregation)
                return getIcon(IconManager.IconType.REFERENCE_AGRRE);
            else
                return getIcon(IconManager.IconType.REFERENCE_RSM);
        }
    }

    @Override
    public Sheet createSheet() {
        return null;
    }

    @Override
    public AbstractNode copyNode() {
        return null;
    }

    @Override
    public void loadDataForProperties(Runnable callback) {
    }

    @Override
    public boolean canBeDeleted() {
        return false;
    }
}
