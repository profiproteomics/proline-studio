/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.actions.identification;

import java.util.Hashtable;

/**
 *
 * @author AK249877
 */
public class ImportManager {

    public static final String DEFAULT_SEARCH_RESULT_NAME_SOURCE_KEY = "DefaultSearchResultNameSource";
    public static final String SEARCH_RESULT_NAME_SOURCE = "SEARCH_RESULT_NAME";
    public static final String PEAKLIST_PATH_SOURCE = "PEAKLIST_PATH";
    public static final String MSI_SEARCH_FILE_NAME_SOURCE = "MSI_SEARCH_FILE_NAME";

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

}
