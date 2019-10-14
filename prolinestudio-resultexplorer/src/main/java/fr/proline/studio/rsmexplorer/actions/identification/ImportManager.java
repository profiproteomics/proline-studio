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
package fr.proline.studio.rsmexplorer.actions.identification;

import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseDataSetTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.parameter.ObjectParameter;
import fr.proline.studio.parameter.ParameterList;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;
import fr.proline.studio.rsmexplorer.tree.identification.IdentificationTree;
import java.util.Hashtable;
import javax.swing.JOptionPane;
import org.openide.util.NbPreferences;

/**
 *
 * @author AK249877
 */
public class ImportManager {

    public static final String DEFAULT_SEARCH_RESULT_NAME_SOURCE_KEY = "DefaultSearchResultNameSource";
    public static final String MASCOT_DAEMON_RULE = "MASCOT_DAEMON_RULE";
    public static final String SEARCH_RESULT_NAME_SOURCE = "SEARCH_RESULT_NAME";
    public static final String PEAKLIST_PATH_SOURCE = "PEAKLIST_PATH";
    public static final String MSI_SEARCH_FILE_NAME_SOURCE = "MSI_SEARCH_FILE_NAME";
    private static final String GENERAL_APPLICATION_SETTINGS = "General Application Settings";

    public static final Hashtable<String, Boolean> VALID_NAME_SOURCES = new Hashtable<String, Boolean>() {
        {
            put(SEARCH_RESULT_NAME_SOURCE, true);
            put(PEAKLIST_PATH_SOURCE, true);
            put(MSI_SEARCH_FILE_NAME_SOURCE, true);
            put(MASCOT_DAEMON_RULE, true);
        }
    };

    public ImportManager() {
        ;
    }

    public static void importRenaming(DDataset dataset, DataSetNode identificationNode, IdentificationTree tree) {
        ParameterList parameterList = new ParameterList(GENERAL_APPLICATION_SETTINGS);
        Object[] objectTable = {ImportManager.SEARCH_RESULT_NAME_SOURCE, ImportManager.PEAKLIST_PATH_SOURCE, ImportManager.MSI_SEARCH_FILE_NAME_SOURCE, ImportManager.MASCOT_DAEMON_RULE};
        ObjectParameter parameter = new ObjectParameter(ImportManager.DEFAULT_SEARCH_RESULT_NAME_SOURCE_KEY, "Default Search Result Name Source", objectTable, 2, null);
        parameterList.add(parameter);
        parameterList.loadParameters(NbPreferences.root());

        String naming = (String) parameter.getObjectValue();

        // we have to load the result set
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {

                if (IdentificationTree.renameNode(dataset, naming, identificationNode, tree)) {
                    JOptionPane.showMessageDialog(null, "Selected ResultSet was not transfered with the required name");
                }

            }
        };

        if (dataset.getResultSet()==null) {
            // ask asynchronous loading of data
            DatabaseDataSetTask task = new DatabaseDataSetTask(callback);
            task.initLoadRsetAndRsm(dataset);
            AccessDatabaseThread.getAccessDatabaseThread().addTask(task);
        } else {

            if (IdentificationTree.renameNode(dataset, naming, identificationNode, tree)) {
                JOptionPane.showMessageDialog(null, "Selected ResultSet was not transfered with the required name");
            }
        }

    }

}
