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
package fr.proline.studio.msfiles;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openide.util.Exceptions;

/**
 *
 * @author AK249877
 */
public class WorkingSetUtil {

    public static JSONObject readJSON() {
        File baseLocationFile = new File(".");

        String canonicalPath = null;
        try {
            canonicalPath = baseLocationFile.getCanonicalPath();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        File jsonFile = new File(canonicalPath + File.separator + "working_sets.json");

        if (jsonFile.exists()) {

            JSONObject jsonObject = null;

            FileReader reader = null;
            try {
                reader = new FileReader(canonicalPath + File.separator + "working_sets.json");
                JSONParser jsonParser = new JSONParser();
                jsonObject = (JSONObject) jsonParser.parse(reader);
            } catch (FileNotFoundException ex) {
                return saveJSON(null);
            } catch (IOException | ParseException ex) {
                Exceptions.printStackTrace(ex);
                return saveJSON(null);
            } finally {
                try {
                    reader.close();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }

            return jsonObject;

        } else {
            return saveJSON(null);
        }
    }

    public static JSONObject saveJSON(JSONArray array) {

        File baseLocationFile = new File(".");

        String canonicalPath = null;
        try {
            canonicalPath = baseLocationFile.getCanonicalPath();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        JSONObject obj = new JSONObject();

        if (array != null) {
            obj.put("working_sets", array);
        } else {
            obj.put("working_sets", new JSONArray());
        }

        try (FileWriter file = new FileWriter(canonicalPath + File.separator + File.separator + "working_sets.json")) {

            file.write(obj.toJSONString());
            file.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return obj;
    }
    
    public static File getTempDirectory(){
        File baseLocationFile = new File(".");

        String canonicalPath = null;
        try {
            canonicalPath = baseLocationFile.getCanonicalPath();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        File tempDir = new File(canonicalPath + File.separator + "mzdb_temp");
        
        if(!tempDir.exists()){
            tempDir.mkdir();
        }
        
        return tempDir;
    }

}
