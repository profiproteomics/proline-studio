/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
