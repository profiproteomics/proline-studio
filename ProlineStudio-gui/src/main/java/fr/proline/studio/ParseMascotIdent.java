/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio;

import fr.proline.core.om.model.msi.IResultFile;
import fr.proline.core.om.model.msi.ResultSet;
import fr.proline.module.parser.mascot.MascotResultFileProvider;
import java.io.File;

/**
 *
 * @author VD225637
 */
public class ParseMascotIdent {
    public final static String PROVIDER_KEY="ProlineStudio_Mascot";

    protected ResultSet newResultSet;
    
    public ParseMascotIdent() {
        
    }
   
    
    public ResultSet parseMascotResult(File mascotFile ){
        IResultFile rf = MascotResultFileProvider.getResultFile(mascotFile, PROVIDER_KEY);
        newResultSet = rf.getResultSet(false);   
        return newResultSet;
    }
        
    public ResultSet getLastParsedResultSet(){
        return newResultSet;
    }
            
    
}
