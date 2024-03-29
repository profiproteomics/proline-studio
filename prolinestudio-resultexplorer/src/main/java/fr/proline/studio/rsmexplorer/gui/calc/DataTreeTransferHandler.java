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
package fr.proline.studio.rsmexplorer.gui.calc;

import fr.proline.studio.rserver.RServerManager;
import fr.proline.studio.table.TableInfo;
import fr.proline.studio.rsmexplorer.gui.calc.DataTree.DataNode;
import fr.proline.studio.rsmexplorer.gui.calc.functions.AbstractFunction;
import fr.proline.studio.rsmexplorer.gui.calc.graph.DataGraphNode;
import fr.proline.studio.rsmexplorer.gui.calc.graph.FunctionGraphNode;
import fr.proline.studio.rsmexplorer.gui.calc.graph.GraphicGraphNode;
import fr.proline.studio.rsmexplorer.gui.calc.graphics.AbstractGraphic;
import fr.proline.studio.rsmexplorer.gui.calc.macros.AbstractMacro;
import java.awt.Point;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;

/**
 * Used for Drag & Drop of the data analyzer from DataTree to GraphPanel 
 * @author JM235353
 */
public class DataTreeTransferHandler extends TransferHandler {

    private  GraphPanel m_graphPanel;
    
    public DataTreeTransferHandler(GraphPanel panel) {
        m_graphPanel = panel;
    }
    
    @Override
    public int getSourceActions(JComponent c) {
        return TransferHandler.COPY;
    }
    
    @Override
    protected Transferable createTransferable(JComponent c) {

        if (c instanceof DataTree) {
            DataTree tree = (DataTree) c;

            // only Dataset which are not being changed can be transferred
            // furthermore Dataset must be of the same project
            DataNode[] selectedNodes = tree.getSelectedNodes();
            int nbSelectedNode = selectedNodes.length;
            for (int i=0;i<nbSelectedNode;i++) {
                DataNode node = selectedNodes[i];
                DataNode.DataNodeType nodeType = node.getType();
                if ((nodeType != DataNode.DataNodeType.VIEW_DATA) && (nodeType != DataNode.DataNodeType.FUNCTION) && (nodeType != DataNode.DataNodeType.GRAPHIC) && (nodeType != DataNode.DataNodeType.MACRO) && (nodeType != DataNode.DataNodeType.USERMACRO)) {
                    return null;
                }

                // check for R functions that R server is started
                boolean actionNotAllowed = (node.isRNeeded() && (!RServerManager.getRServerManager().isRStarted()));
                if (actionNotAllowed) {
                    return null;
                }
                    
                
            } 

            DataTreeTransferable.TransferData data = new DataTreeTransferable.TransferData();
            data.setDataNodes(selectedNodes);
            Integer transferKey =  DataTreeTransferable.register(data);

            
            
            return new DataTreeTransferable(transferKey);

        }
        return null;
    }
    
    
    @Override
    public boolean canImport(TransferHandler.TransferSupport support) {

        support.setShowDropLocation(true);
        if (support.isDataFlavorSupported(DataTreeTransferable.DATANODE_FLAVOR)) {

            // drop path
            DropLocation dropLocation = support.getDropLocation();
            if (dropLocation == null) {
                // should not happen
                return false;
            }
            
            if (dropLocation instanceof JTree.DropLocation) {
                return false;
            }
            
            
            return true;

        }

        return false;
    }

    @Override
    public boolean importData(TransferSupport support) {

        if (canImport(support)) {

            try {
                DataTreeTransferable transfer = (DataTreeTransferable) support.getTransferable().getTransferData(DataTreeTransferable.DATANODE_FLAVOR);
                DataTreeTransferable.TransferData data = DataTreeTransferable.getData(transfer.getTransferKey());
                
                
                DataNode[] dataNodes = data.getDataNodes();
                TransferHandler.DropLocation dropLocation = support.getDropLocation();
                Point p = dropLocation.getDropPoint();
                int x = p.x;
                int y = p.y;
                GraphPanel graphPanel = ((GraphPanel)support.getComponent());
                for (DataNode node : dataNodes) {
     
                    switch (node.getType()) {

                        case VIEW_DATA: {
                            TableInfo tableInfo = ((DataTree.ViewDataNode) node).getTableInfo();
                            DataGraphNode graphNode = new DataGraphNode(tableInfo, m_graphPanel);
                            graphPanel.addGraphNode(graphNode, x, y);
                            break;
                        }
                        case FUNCTION: {
                            AbstractFunction function = ((DataTree.FunctionNode) node).getFunction().cloneFunction(m_graphPanel);
                            FunctionGraphNode graphNode = new FunctionGraphNode(function, m_graphPanel);
                            graphPanel.addGraphNode(graphNode, x, y);
                            break;
                        }
                        case GRAPHIC: {
                            AbstractGraphic graphic = ((DataTree.GraphicNode) node).getGraphic().cloneGraphic(m_graphPanel);
                            GraphicGraphNode graphNode = new GraphicGraphNode(m_graphPanel, graphic);
                            graphPanel.addGraphNode(graphNode, x, y);
                            break;
                        }
                        case MACRO: {
                            AbstractMacro macro = ((DataTree.MacroNode) node).getMacro();
                            graphPanel.addMacroToGraph(macro, x, y);
                            break;
                        }
                        case USERMACRO: {
                            String macroXML = ((DataTree.UserMacroNode) node).getXMLMacro();
                            try {
                                UserMacroParser.getGraphFileManager().parseFile(macroXML, graphPanel, x, y);
                            } catch (Exception e) {
                                System.out.println(e);
                            }
                            break;
                        }

                    }
                    x += DataGraphNode.WIDTH/2;
                    y += DataGraphNode.HEIGHT_MIN/2; 
                    
                }


            } catch (UnsupportedFlavorException | IOException e) {
                // should never happen
                //m_logger.error(getClass().getSimpleName() + " DnD error ", e);
                return false;
            }

            
        }
        
        return false;
    }
    
}
