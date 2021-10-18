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
package fr.proline.studio.rsmexplorer.tree;

import fr.proline.studio.dam.data.AbstractData;
import fr.proline.studio.utils.IconManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Super class for all nodes
 * @author JM235353
 */
public abstract class AbstractNode extends DefaultMutableTreeNode implements Cloneable {

    public enum NodeTypes {
        TREE_PARENT,
        PROJECT_IDENTIFICATION,
        PROJECT_QUANTITATION,
        DATA_SET,
        DATA_ALL_IMPORTED,
        HOUR_GLASS,
        BIOLOGICAL_SAMPLE,
        BIOLOGICAL_GROUP,
        BIOLOGICAL_SAMPLE_ANALYSIS,
        RUN,
        DATASET_REFERENCE
    }
    private static Action[] m_actionInstance = null;
    protected NodeTypes m_type;

    protected boolean m_isChanging = false;
    
    protected boolean m_isDisabled = false;

    protected static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");
    
    public AbstractNode(/*Children children,*/ NodeTypes type, AbstractData data) {
        super(data);
        m_type = type;
    }

    public NodeTypes getType() {
        return m_type;
    }
    
    public AbstractNode getLowestAncestor(){
        AbstractNode ancestor = this;
        while(ancestor.getParent()!=null && ancestor.getParent().getParent()!=null){
            ancestor = (AbstractNode) ancestor.getParent();
        }
        return ancestor;
    }
    
    public AbstractData getData() {
        return (AbstractData) getUserObject();
    }

    public abstract ImageIcon getIcon(boolean expanded);
    
    public ImageIcon getIcon(IconManager.IconType iconType) {
        if (m_isChanging) {
            return IconManager.getIconWithHourGlass(iconType);
        }
        if(m_isDisabled) {
            return IconManager.getGrayedIcon(iconType);
        }        
        return IconManager.getIcon(iconType);
    }
    
    public boolean searchChildNodeOfAType(NodeTypes type) {
        if (m_type == type) {
            return true;
        }

        /*
         * Node[] nodes = getChildren().getNodes();
         *
         * for (Node nodeCur : nodes) { if (
         * ((AbstractNode)nodeCur).searchChildNodeOfAType(type) ) { return true; }
        }
         */ //JPM.TODO : put back when the test code is removed


        return false;
    }

    @Override
    public String toString() {
        AbstractData data = getData();
        if (data == null) {
            return "";
        }

        return data.getName();
    }

    public boolean isInTrash() {
        return false;
    }
    
    public boolean canBeDeleted() {
        return false;
    }
    
    public void setIsChanging(boolean isChanging) {
        m_isChanging = isChanging;
    }
    
    public boolean isChanging() {
        return m_isChanging;
    }
    
    public void setIsDisabled(boolean isDisabled) {
        m_isDisabled = isDisabled;
    }
    
    public boolean isDisabled() {
        return m_isDisabled;
    }
    
    public String getToolTipText() {
        return null;
    }

    //Default implementation.

    /**
     * Load all Data needed by the Properties view.
     * Callback should the be called to display the information.
     *
     * Default implementation just call callback Runnable
     * @param callback
     */
    public void loadDataForProperties(Runnable callback) {
        // nothing to do
        callback.run();
    }

    public abstract AbstractNode copyNode();
    
    public void copyChildren(AbstractNode copyParent) {
        int nbChildren = getChildCount();

        for (int i = 0; i < nbChildren; i++) {
            AbstractNode childNode = (AbstractNode) getChildAt(i);
            if (childNode.isChanging()) {
                // do not copy changing nodes
                continue;
            }
     
           AbstractNode childCopy = childNode.copyNode();
           if (childCopy == null) {
               continue;
           }
           copyParent.add(childCopy);  
        }
    }
}
