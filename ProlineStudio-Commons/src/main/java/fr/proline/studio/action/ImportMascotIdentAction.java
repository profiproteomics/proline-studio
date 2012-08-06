/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.action;

import fr.proline.core.om.model.msi.Protein;
import fr.proline.core.om.model.msi.SeqDatabase;
import fr.proline.core.om.provider.msi.IProteinProvider;
import fr.proline.core.om.provider.msi.IProteinProvider$class;
import fr.proline.core.om.provider.msi.ProvidersFactory;
import fr.proline.core.om.provider.msi.impl.ORMPTMProvider;
import fr.proline.core.om.provider.msi.impl.ORMPeptideProvider;
import fr.proline.core.om.provider.msi.impl.ORMSeqDatabaseProvider;
import fr.proline.repository.ProlineRepository;
import fr.proline.studio.ParseMascotIdent;
import static fr.proline.studio.action.Bundle.*;
import fr.proline.studio.dbs.ProlineDbManagment;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.HashMap;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Option;
import scala.Some;
import scala.collection.Seq;

@ActionID(category = "File",
id = "fr.proline.prolinestudio.action.ImportMascotIdent")
@ActionRegistration(displayName = "#CTL_ImportMascotIdent")
@ActionReferences({
    @ActionReference(path = "Menu/File", position = 1300)
})
@Messages({"CTL_ImportMascotIdent=Import Mascot Identification",
           "error.title=Import Result Error",
           "invalid.dbs.managment=The Databases managment system has not been initialized. Initialize it before running import",
           "mascot.parse.error=An error occured while parsing Mascot result file"
})
public final class ImportMascotIdentAction extends AbstractAction { // extends AbstractProgressAction {

    private static Logger logger =  LoggerFactory.getLogger(ImportMascotIdentAction.class);    
    ProlineDbManagment pdbM;
    
    @Override
    public void actionPerformed(ActionEvent e) {
       runAction();        
    }
    //
    // AbstractProgressAction method containing action to execute
//    @Override
    public void runAction() {
        JFileChooser fchooser = new JFileChooser();
        fchooser.setMultiSelectionEnabled(false);
        fchooser.setFileFilter(new FileNameExtensionFilter("Mascot identification result", "dat"));
        File mascotFile;
        int result =  fchooser.showOpenDialog(null);
        switch (result) {
            case JFileChooser.APPROVE_OPTION :
                mascotFile = fchooser.getSelectedFile();   
                break;
            case JFileChooser.ERROR_OPTION :
            case JFileChooser.CANCEL_OPTION :
            default :
                return;                         
        }
        logger.debug(" Import Mascot dat file {} ", mascotFile.getAbsolutePath());         
             
        try {
            if(pdbM == null)
                pdbM= ProlineDbManagment.getProlineDbManagment();
        }catch(UnsupportedOperationException uos){
            JOptionPane.showMessageDialog(null, invalid_dbs_managment(),error_title(),JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        initializeOMProviders();
        ParseMascotIdent parser = new ParseMascotIdent();
        boolean parseResult = parser.parseMascotResult(mascotFile);
        if(!parseResult)
            JOptionPane.showMessageDialog(null, mascot_parse_error(),error_title(),JOptionPane.ERROR_MESSAGE);
        logger.debug(" Foward Result Set ");
    }

    private void initializeOMProviders() {            
	  
        // TODO Init PDI db connexion	  
        ProvidersFactory.registerPeptideProvider(ParseMascotIdent.PROVIDER_KEY, new ORMPeptideProvider(pdbM.getEntityManager(ProlineRepository.Databases.PS, false)));
        ProvidersFactory.registerPTMProvider(ParseMascotIdent.PROVIDER_KEY, new ORMPTMProvider(pdbM.getEntityManager(ProlineRepository.Databases.PS, false)));
        ProvidersFactory.registerProteinProvider(ParseMascotIdent.PROVIDER_KEY, new ProteinFakeProvider());
        ProvidersFactory.registerSeqDatabaseProvider(ParseMascotIdent.PROVIDER_KEY, new ORMSeqDatabaseProvider(pdbM.getEntityManager(ProlineRepository.Databases.PDI, false)));
        
    }

//    @Override
    public String getProgressBarMessage() {
        return CTL_ImportMascotIdent();
    }
    
 
    class ProteinFakeProvider implements IProteinProvider{
        private HashMap <String, Protein> protByAcc = new HashMap<String, Protein>();
        
        @Override
        public Option<Protein>[] getProteins(Seq<Object> seq) {
            Option<Protein>[] retArray =  new Option[1];            
            retArray[0] = Option.empty();
            return retArray;
        }

        @Override
        public Option<Protein> getProtein(int i) {
            return IProteinProvider$class.getProtein(this, i);
        }

        @Override
        public Option<Protein> getProtein(String string) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
         public Option<Protein> getProtein(String accession, SeqDatabase seqDb) {
             Option<Protein> retVal;
             Protein p = protByAcc.get(accession.concat(seqDb.name()));
             if(p == null){
                p= new Protein("AACCCMMM", Protein.generateNewId(),"aa" );
                protByAcc.put(accession.concat(seqDb.name()),p);
             }
            retVal = new Some(p);        
            return retVal;
        }
     
        
    }
}
