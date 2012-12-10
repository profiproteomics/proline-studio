/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.idfimport.action;

import fr.proline.core.om.model.msi.IResultFileProvider;
import fr.proline.core.om.model.msi.ResultFileProviderRegistry;
import fr.proline.studio.idfimport.ImportResultFile;
import fr.proline.studio.idfimport.gui.ImportResultFilesDialog;
import fr.proline.studio.repositorymgr.ProlineDBManagement;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.WindowManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.collection.JavaConversions;


@ActionID(category = "File",
id = "fr.proline.studio.action.ImportResultFileAction")
@ActionRegistration(displayName = "#CTL_ImportResultFile")
@ActionReferences({
    @ActionReference(path = "Menu/File", position = 1300)
})
@Messages({"CTL_ImportResultFile=Import Identification Result",
           "error.title=Import Result Error",
           "invalid.dbs.managment=The Databases managment system has not been initialized. Initialize it before running import",
           "mascot.parse.error=An error occured while parsing Mascot result file",
           "resultFileProviders.type.choose.msg= Choose the type of identification file to import :",
           "resultFileProviders.type.choose.title=Import Result File",
           "resultFile.import.error.msg=An error occured will parsing file ({0})"
})
public final class ImportResultFileAction extends AbstractAction { // extends AbstractProgressAction {

    protected static Logger logger =  LoggerFactory.getLogger(ImportResultFileAction.class);    
    
    @Override
    public void actionPerformed(ActionEvent e) {
       runAction();        
    }
    //
    // AbstractProgressAction method containing action to execute
//    @Override
    public void runAction() {
         logger.debug(" ------- > Start debug ImportResultFileAction ");
         
        //Verify ProlineDBManagement is initialized
        if(!ProlineDBManagement.isInitilized()){
            JOptionPane.showMessageDialog(null, Bundle.invalid_dbs_managment(),Bundle.error_title(),JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Get all availiable ResultFileProvider types and ask user to choose one. 
        Iterator<IResultFileProvider> it = JavaConversions.asJavaIterator(ResultFileProviderRegistry.getAllResultFileProviders());
        Map<String,IResultFileProvider> rfTypeMap = new HashMap<>();
        while(it.hasNext()){
            IResultFileProvider nextRFP = it.next();            
            rfTypeMap.put(nextRFP.fileType(), nextRFP);
            logger.warn(" ------- > ADDED "+nextRFP.fileType());
        }
        
        ArrayList<String> rfpName = new ArrayList(rfTypeMap.keySet());
        logger.warn(" ------- > LIST  "+rfpName); 
        String rfProviderName  = (String)JOptionPane.showInputDialog(null, Bundle.resultFileProviders_type_choose_msg(), Bundle.resultFileProviders_type_choose_title(), JOptionPane.INFORMATION_MESSAGE, null, rfpName.toArray(), rfpName.get(0));
        if(rfProviderName !=null) {            
        
            // Show ImportResultFile dialog with specific properties 
            ImportResultFilesDialog rfpTypeDialog = new ImportResultFilesDialog(null, true, rfTypeMap.get(rfProviderName));
            rfpTypeDialog.setLocationRelativeTo(WindowManager.getDefault().getMainWindow());
            rfpTypeDialog.setVisible(true);

            Map<String, Object> propertiesValues = rfpTypeDialog.getResultFileProperties(); // Specified values

            // Open Identification Result File
            JFileChooser fchooser = new JFileChooser();
            fchooser.setMultiSelectionEnabled(false);
    //        fchooser.setFileFilter(new FileNameExtensionFilter("Mascot identification result", "dat"));
            File resultIdfFile;
            int result =  fchooser.showOpenDialog(null);
            switch (result) {
                case JFileChooser.APPROVE_OPTION :
                    resultIdfFile = fchooser.getSelectedFile();   
                    break;
                case JFileChooser.ERROR_OPTION :
                case JFileChooser.CANCEL_OPTION :
                default :
                    return;                         
            }
            logger.debug(" Import Identification Result file {} ", resultIdfFile.getAbsolutePath()); 

            //Create specific importer
            ImportResultFile importer = new ImportResultFile(rfTypeMap.get(rfProviderName));
            //parse spciefied file
            try {
                importer.parseIdfResultFile(resultIdfFile, propertiesValues);
            }catch (Exception e){
                JOptionPane.showMessageDialog(null, Bundle.resultFile_import_error_msg(e.getMessage()),Bundle.error_title(),JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
                return;
            }

            //Call ResultFileImporter services... 
            logger.debug(" Foward Result Set ");
        }
    }

//    @Override
    public String getProgressBarMessage() {
        return Bundle.CTL_ImportResultFile();
    }
        
}
