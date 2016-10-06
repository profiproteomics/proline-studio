/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.actions.identification;

import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.parameter.ObjectParameter;
import fr.proline.studio.parameter.ParameterList;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;
import fr.proline.studio.rsmexplorer.tree.identification.IdentificationTree;
import java.io.File;
import java.util.Hashtable;
import javax.swing.JOptionPane;
import org.openide.util.NbPreferences;

/**
 *
 * @author AK249877
 */
public class ImportManager {

    public static final String DEFAULT_SEARCH_RESULT_NAME_SOURCE_KEY = "DefaultSearchResultNameSource";
    public static final String SEARCH_RESULT_NAME_SOURCE = "SEARCH_RESULT_NAME";
    public static final String PEAKLIST_PATH_SOURCE = "PEAKLIST_PATH";
    public static final String MSI_SEARCH_FILE_NAME_SOURCE = "MSI_SEARCH_FILE_NAME";
    private static final String GENERAL_APPLICATION_SETTINGS = "General Application Settings";

    public static final Hashtable<String, Boolean> VALID_NAME_SOURCES = new Hashtable<String, Boolean>() {
        {
            put(SEARCH_RESULT_NAME_SOURCE, true);
            put(PEAKLIST_PATH_SOURCE, true);
            put(MSI_SEARCH_FILE_NAME_SOURCE, true);
        }
    };

    public ImportManager() {
        ;
    }

    public static void importRenaming(DDataset dataset, DataSetNode identificationNode, IdentificationTree tree) {

        ParameterList parameterList = new ParameterList(GENERAL_APPLICATION_SETTINGS);
        Object[] objectTable = {ImportManager.SEARCH_RESULT_NAME_SOURCE, ImportManager.PEAKLIST_PATH_SOURCE, ImportManager.MSI_SEARCH_FILE_NAME_SOURCE};
        ObjectParameter parameter = new ObjectParameter(ImportManager.DEFAULT_SEARCH_RESULT_NAME_SOURCE_KEY, "Default Search Result Name Source", objectTable, 2, null);
        parameterList.add(parameter);
        parameterList.loadParameters(NbPreferences.root(), true);

        String naming = (String) parameter.getObjectValue();

        if (dataset.getResultSet() == null) {
            DataSetData.fetchRsetAndRsmForOneDataset(dataset);
        }

        if (dataset == null || dataset.getResultSet() == null || dataset.getResultSet().getMsiSearch() == null) {
            return;
        }

        String newName = "";

        newName = (dataset.getResultSet().getMsiSearch().getResultFileName() == null) ? "" : dataset.getResultSet().getMsiSearch().getResultFileName();
        if (newName.contains(".")) {
            newName = newName.substring(0, newName.indexOf("."));
        }

        if (naming.equalsIgnoreCase(ImportManager.SEARCH_RESULT_NAME_SOURCE)) {
            newName = dataset.getResultSet().getName();
        } else if (naming.equalsIgnoreCase(ImportManager.PEAKLIST_PATH_SOURCE)) {
            newName = (dataset.getResultSet().getMsiSearch().getPeaklist().getPath() == null) ? "" : dataset.getResultSet().getMsiSearch().getPeaklist().getPath();
            if (newName.contains(File.separator)) {
                newName = newName.substring(newName.lastIndexOf(File.separator) + 1);
            }
        }

        if (!newName.equalsIgnoreCase("")) {
            identificationNode.rename(newName, tree);
            dataset.setName(newName);
            tree.rename(identificationNode, newName);
        } else {
            JOptionPane.showMessageDialog(null, "Selected ResultSet was not transfered with the required name");
        }
    }

}
