/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio;

import fr.proline.core.om.model.msi.IResultFile;
import fr.proline.core.om.model.msi.ResultSet;
import fr.proline.module.parser.mascot.MascotResultFileProvider;
import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author VD225637
 */
public class ParseMascotIdent {
    public final static String PROVIDER_KEY="ProlineStudio_Mascot";
    protected static Logger logger = LoggerFactory.getLogger(ParseMascotIdent.class);
    
    protected ResultSet fwrdResultSet;
    protected ResultSet decoyResultSet;
    
    public ParseMascotIdent() {
        //VD TODO FIXME : 
        logger.debug(" VALUE JavaLibPath ",System.getProperty("java.library.path") );
        System.setProperty("java.library.path", "./");
    }
   
    
    public boolean parseMascotResult(File mascotFile ){
        IResultFile rf = MascotResultFileProvider.getResultFile(mascotFile, PROVIDER_KEY);
        fwrdResultSet = rf.getResultSet(false);   
        decoyResultSet = rf.getResultSet(true);   
        return true;
    }
        
    public ResultSet getLastParsedFowardRSet(){
        return fwrdResultSet;
    }
            
    public ResultSet getLastParsedDecoyRSet(){
        return decoyResultSet;
    }
       
}
