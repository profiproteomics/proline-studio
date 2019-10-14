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

import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;

/**
 * Tree to select the identifications to drag and drop to the XIC design tree
 * @author JM235353
 */
public class IdentificationSelectionTree extends AbstractTree implements TreeWillExpandListener {

    public IdentificationSelectionTree(AbstractNode top, boolean loadAllAtOnce) {

        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        XICTransferHandler handler = new XICTransferHandler(true, this);
        setTransferHandler(handler);

        setDragEnabled(true);

        initTree(top);

        if (loadAllAtOnce) {
            loadAllAtOnce(top, true);
        } else {
            startLoading(top, true);
        }
        

    }

    @Override
    protected final void initTree(AbstractNode top) {
        super.initTree(top);

        addTreeWillExpandListener(this);
    }

    @Override
    public void rename(AbstractNode rsmNode, String newName) {
    }
    
    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {

        TreePath path = event.getPath();
        AbstractNode nodeExpanded = (AbstractNode) path.getLastPathComponent();

        // check if the node contains a GlassHourNode (ie : children are not loaded)
        if (nodeExpanded.getChildCount() > 0) {
            AbstractNode childNode = (AbstractNode) nodeExpanded.getChildAt(0);
            if (childNode.getType() == AbstractNode.NodeTypes.HOUR_GLASS) {
                startLoading(nodeExpanded, false);
            }
        }

    }

    @Override
    public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
    }
}
