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

import fr.proline.core.orm.msi.Peaklist;
import fr.proline.core.orm.uds.RawFile;
import fr.proline.core.orm.uds.Run;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.data.AbstractData;
import fr.proline.studio.dam.data.RunInfoData;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabasePeaklistTask;
import fr.proline.studio.dam.tasks.DatabaseRunsTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.utils.IconManager;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import org.openide.nodes.Sheet;

/**
 * Tree Node representing a Run
 *
 * @author JM235353
 */
public class XICRunNode extends AbstractNode {

    public enum Search {

        BASED_ON_IDENTIFIER, BASED_ON_PATH
    }

    private DefaultTreeModel m_treeModel = null;
//    private final AbstractTree m_tree;
    private AbstractTableModel m_tableModel;
    protected List<XICRunNodeInitListener> listenerList;   /** List of listeners */

    public XICRunNode(AbstractData data, DefaultTreeModel treeModel) {
        super(NodeTypes.RUN, data);
        listenerList = new ArrayList<>();

        m_treeModel = treeModel;
    }

    
    //
    //  Managing Listeners
    //
    /**
     * Adds a XICRunNodeInitListener that's notified when XICRunNode has been initialize 
     *
     * @param   l   the XICRunNodeInitListener
     */
    public void addXICRunNodeInitListener(XICRunNodeInitListener l) {
        listenerList.add(l);
    }

    /**
     * Removes a XICRunNodeInitListener from the list that's notified when 
     * XICRunNode has been initialize 
     *
     * @param   l   the XICRunNodeInitListener
     */
    public void removeTableModelListener(XICRunNodeInitListener l) {
        listenerList.remove( l);
    }

    private void fireXICRunNodeInitialized() {     
        for (XICRunNodeInitListener l :  listenerList) {
            l.initCompleted(this);
        }
    }
    
    
    public AbstractTableModel getTableModel() {
        return m_tableModel;
    }

    public void setTableModel(AbstractTableModel tableModel) {
        m_tableModel = tableModel;
    }

    
    public void init(final DDataset dataset, final AbstractTableModel tableModel) {
       
        m_tableModel = tableModel;

        setIsChanging(true);

        // look if we find a Raw File
        if (dataset.isIdentification()) {

            final HashMap<String, RawFile> rawfileFounds = new HashMap<>(1);
            final Run[] runOut = new Run[1];
            final XICRunNode xicRunNode = this;

            AbstractDatabaseCallback getRawfileCallback = new AbstractDatabaseCallback() {

                @Override
                public boolean mustBeCalledInAWT() {
                    return true;
                }

                @Override
                public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
                    if (success) {
                        if (!rawfileFounds.isEmpty()) {

                            RawFile rawFile = rawfileFounds.entrySet().iterator().next().getValue();

                            RunInfoData runInfoData = ((RunInfoData) getData());
                            runInfoData.setLinkedRawFile(rawFile);
                            runInfoData.setRun(runOut[0]);

                            runInfoData.setStatus(RunInfoData.Status.LINKED_IN_DATABASE);

                            setIsChanging(false);
                            m_treeModel.nodeChanged(xicRunNode);

                            fireXICRunNodeInitialized();
                            if (tableModel != null) {
                                // a table model display data in this Xic Run Node, so it must be updated
                                tableModel.fireTableDataChanged();
                            }

                        } else { //No RawFile associated to dataset 
                            
                            //try to get info from peaklist
                            Peaklist[] peaklistResult = new Peaklist[1];

                            AbstractDatabaseCallback getPeakListCallback = new AbstractDatabaseCallback() {

                                @Override
                                public boolean mustBeCalledInAWT() {
                                    return true;
                                }

                                @Override
                                public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
                                    RunInfoData  runInfoData = (RunInfoData) getData();
                                    if (success) {
                                        if (peaklistResult[0] != null) {
                                            Peaklist peaklist = peaklistResult[0];

                                            if (peaklist.getRawFileIdentifier() != null && !peaklist.getRawFileIdentifier().equalsIgnoreCase("")) {

                                                //Previous RawFileIdentifier exists
                                                runInfoData.setMessage("Search " + peaklist.getRawFileIdentifier());
                                                m_treeModel.nodeChanged(xicRunNode);
                                                
                                                searchPotentialRawFiles(peaklist.getRawFileIdentifier(), tableModel, Search.BASED_ON_IDENTIFIER);

                                            } else if (peaklist.getPath() != null && !peaklist.getPath().equalsIgnoreCase("")) {

                                                //Previous RawFileIdentifier does not exist so we will try to search for the peaklistResult path!
                                                String searchString = peaklist.getPath();

                                                // remove .raw , or .raw-1.mgf
                                                int indexRaw = searchString.toLowerCase().indexOf(".raw");
                                                if (indexRaw != -1) {
                                                    searchString = searchString.substring(0, indexRaw);
                                                }
                                                // remove .mgf, ...
                                                int indexMgf = searchString.toLowerCase().indexOf(".mgf");
                                                if (indexMgf != -1) {
                                                    searchString = searchString.substring(0, indexMgf);
                                                }

                                                // remove all code before \ / or ~
                                                int index = searchString.lastIndexOf('/');
                                                index = Math.max(index, searchString.lastIndexOf('\\'));
                                                index = Math.max(index, searchString.lastIndexOf('~'));
                                                if (index != -1) {
                                                    searchString = searchString.substring(index + 1);
                                                }

                                                runInfoData.setPeakListPath(searchString);

                                                if ((searchString == null) || (searchString.isEmpty())) {
                                                    searchString = "*";
                                                } else {
                                                    searchString = "*" + searchString + "*";
                                                }

                                                runInfoData.setMessage("Search " + searchString);
                                                runInfoData.setStatus(RunInfoData.Status.MISSING);

                                                m_treeModel.nodeChanged(xicRunNode);

                                                searchPotentialRawFiles(searchString, tableModel, Search.BASED_ON_PATH);

                                            } else {
                                                // No Info in peaklist found!
                                              runInfoData.setMessage("<html><font color='#FF0000'>No Info (Partial peaklist)</font></html>");
                                              runInfoData.setStatus(RunInfoData.Status.MISSING);
                                              fireXICRunNodeInitialized();
                                            }
                                        } else {
                                            // No peaklist found!
                                            runInfoData.setMessage("<html><font color='#FF0000'>Unavailable Peaklist</font></html>");
                                            runInfoData.setStatus(RunInfoData.Status.MISSING);
                                            fireXICRunNodeInitialized();
                                        }

                                        setIsChanging(false);
                                        m_treeModel.nodeChanged(xicRunNode);
                                        if (tableModel != null) {
                                            // a table model display data in this Xic Run Node, so it must be updated
                                            tableModel.fireTableDataChanged();
                                        }
                                    } else { //failed to get Peaklist ! 
                                        runInfoData.setStatus(RunInfoData.Status.MISSING);
                                        runInfoData.setMessage("<html><font color='#FF0000'>Unavailable Peaklist</font></html>");
                                        fireXICRunNodeInitialized();
                                        m_treeModel.nodeChanged(xicRunNode);
                                        if (tableModel != null) {
                                            // a table model display data in this Xic Run Node, so it must be updated
                                            tableModel.fireTableDataChanged();
                                        }
                                    }
                                }
                            };

                            DatabasePeaklistTask peaklistTask = new DatabasePeaklistTask(getPeakListCallback);
                            peaklistTask.initLoadPeaklistForRS(dataset.getResultSetId(), dataset.getProject().getId(), peaklistResult);
                            AccessDatabaseThread.getAccessDatabaseThread().addTask(peaklistTask);
                        }
                    } else {
                        // it failed !
                        //popup
                        m_treeModel.removeNodeFromParent((MutableTreeNode) xicRunNode.getParent());
                        if (tableModel != null) {
                            // a table model display data in this Xic Run Node, so it must be updated
                            tableModel.fireTableDataChanged();
                        }
                    }
                    

                }
            };

            DatabaseRunsTask task = new DatabaseRunsTask(getRawfileCallback);
            task.initLoadRawFile(dataset.getId(), rawfileFounds, runOut);
            AccessDatabaseThread.getAccessDatabaseThread().addTask(task);
        } else {
            throw new RuntimeException("Creating XIC Run Node without an identification dataset.");
        }
    }

    private void searchPotentialRawFiles(String searchString, final AbstractTableModel tableModel, Search search ) {


        final HashMap<String, RawFile> m_rawFileMap = new HashMap<>();

        AbstractDatabaseCallback searchRawCallback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
                    setIsChanging(false);

                RunInfoData runInfoData = ((RunInfoData) getData());

                if (m_rawFileMap.isEmpty()) {
                    runInfoData.setMessage("<html><font color='#FF0000'>Missing Raw File</font></html>");
                    runInfoData.setStatus(RunInfoData.Status.MISSING);
                } else if (m_rawFileMap.size() == 1) {
                    RawFile rawFile = m_rawFileMap.entrySet().iterator().next().getValue();
                    runInfoData.setSelectedRawFile(rawFile);
                    runInfoData.setRun(rawFile.getRuns().get(0));

                    if (search == Search.BASED_ON_PATH) {
                        runInfoData.setStatus(RunInfoData.Status.SYSTEM_PROPOSED);
                    } else if (search == Search.BASED_ON_IDENTIFIER) {
                        runInfoData.setStatus(RunInfoData.Status.LAST_DEFINED);
                    }

                } else {
                    runInfoData.setPotentialRawFiles(m_rawFileMap);
                    runInfoData.setStatus(RunInfoData.Status.MISSING);
                    runInfoData.setMessage("<html><font color='#FF0000'>Multiple Raw Files</font></html>");                      
                }

                fireXICRunNodeInitialized();

                if (tableModel != null) {
                    tableModel.fireTableDataChanged();
                }
            }
        };

        // ask asynchronous loading of data
        DatabaseRunsTask task = new DatabaseRunsTask(searchRawCallback);
        task.initSearchRawFile(searchString, m_rawFileMap);
        AccessDatabaseThread.getAccessDatabaseThread().addTask(task);
    }

    @Override
    public String toString() {
        AbstractData data = getData();
        if (data == null) {
            return "Loading";
        }
        return super.toString();
    }

    @Override
    public ImageIcon getIcon(boolean expanded) {
        return getIcon(IconManager.IconType.FILE); //JPM.TODO : to be changed
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

    public void setRawFile(final File selectedFile, ActionListener doneCallback) {

        // we search the raw file in the database, if we found it, we set this one
        // if we do not find it, we use the one choosed by the user
        setIsChanging(true);
        m_treeModel.nodeChanged(this);

        String searchString = selectedFile.getName().substring(0, selectedFile.getName().lastIndexOf('.'));

        final HashMap<String, RawFile> m_rawFilesMap = new HashMap<>();
        final TreeNode _this = this;

        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {

                RunInfoData runInfoData = ((RunInfoData) getData());
                if (m_rawFilesMap.size() == 1) {

                    // we have found the raw file in the database, we use this one
                    RawFile rawFile = m_rawFilesMap.values().iterator().next();
                    runInfoData.setSelectedRawFile(rawFile);
                    runInfoData.setRun(rawFile.getRuns().get(0));

                } else {
                    runInfoData.setRawFileOnDisk(selectedFile);
                }
                setIsChanging(false);
                m_treeModel.nodeChanged(_this);

                if (doneCallback != null) {
                    doneCallback.actionPerformed(null);
                }

            }
        };

        // ask asynchronous loading of data
        DatabaseRunsTask task = new DatabaseRunsTask(callback);
        task.initSearchRawFile(searchString, m_rawFilesMap);
        AccessDatabaseThread.getAccessDatabaseThread().addTask(task);

    }

    public String getPeakListPath() {
        RunInfoData data = ((RunInfoData) getData());
        if (data == null) {
            return null;
        }
        return data.getPeakListPath();
    }

}
