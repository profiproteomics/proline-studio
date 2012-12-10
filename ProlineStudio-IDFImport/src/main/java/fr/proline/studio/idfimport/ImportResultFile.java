/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.idfimport;

import fr.proline.core.om.model.msi.IResultFileProvider;
import fr.proline.core.om.provider.msi.ProvidersFactory;
import fr.proline.core.om.provider.msi.impl.ORMPTMProvider;
import fr.proline.core.om.provider.msi.impl.ORMPeptideProvider;
import fr.proline.core.om.provider.msi.impl.ORMProteinProvider;
import fr.proline.core.om.provider.msi.impl.ORMSeqDatabaseProvider;
import fr.proline.core.service.msi.ResultFileImporterJPAStorer;
import fr.proline.repository.ProlineRepository;
import fr.proline.studio.repositorymgr.ProlineDBManagement;
import java.io.File;
import java.util.Map;
import java.util.Map.Entry;
import javax.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Tuple2;
import scala.collection.mutable.Builder;

/**
 *
 * @author VD225637
 */
public class ImportResultFile {
    private final static String PROVIDER_KEY_PREFIX="ProlineStudio_";
    public final static String IMPORT_RF_PROJECT_ID="resultfile.project.id";
    public final static String IMPORT_RF_INSTRUMNET_ID="resultfile.instrument.id";
    
    protected static Logger logger = LoggerFactory.getLogger(ImportResultFile.class);
    private String finalProviderKey ;
    protected Integer targetResultSetId;
    protected  IResultFileProvider rfProvider;
    
    public ImportResultFile(IResultFileProvider rfProvider) {
        this.rfProvider = rfProvider;
        initializeOMProviders();

        //VD TODO FIXME : 
        logger.debug(" VALUE JavaLibPath ",System.getProperty("java.library.path") );
        System.setProperty("java.library.path", "./");
    }
   
    private String getProviderKey (){
        if(finalProviderKey == null)
            finalProviderKey = PROVIDER_KEY_PREFIX.concat(rfProvider.fileType());
        return finalProviderKey;
    }
            
     private void initializeOMProviders() {            
	ProlineDBManagement dbMngt = ProlineDBManagement.getProlineDBManagement();
        // TODO Init PDI db connexion	  
        EntityManager pdiEM = dbMngt.getEntityManager(ProlineRepository.Databases.PDI, false);
        EntityManager psEM = dbMngt.getEntityManager(ProlineRepository.Databases.PS, false);
        
        ProvidersFactory.registerPeptideProvider(getProviderKey(), new ORMPeptideProvider(psEM));
        ProvidersFactory.registerPTMProvider(getProviderKey(), new ORMPTMProvider(psEM));
        ProvidersFactory.registerProteinProvider(getProviderKey(), new ORMProteinProvider(pdiEM));
        ProvidersFactory.registerSeqDatabaseProvider(getProviderKey(), new ORMSeqDatabaseProvider(pdiEM));
        
    }
    
    public boolean parseIdfResultFile(File idfResultFile, Map<String, Object> parseProperties){
        
        Builder builder = scala.collection.immutable.Map$.MODULE$.newBuilder();
//        scala.collection.mutable.Map propScalaMap2 = JavaConversions.mapAsScalaMap(parseProperties);
        for(Entry<String, Object> e: parseProperties.entrySet()){
            if(e.getKey() != IMPORT_RF_INSTRUMNET_ID && e.getKey() != IMPORT_RF_PROJECT_ID){
                Tuple2<String, Object> t2 = new Tuple2<>(e.getKey(), e.getValue());                    
                builder.$plus$eq(t2);
            }            
        }
//        builder.$plus$plus$eq(propScalaMap2);
        scala.collection.immutable.Map<String, Object> propScalaMap = (scala.collection.immutable.Map<String, Object>) builder.result();
       
        ResultFileImporterJPAStorer importer = new ResultFileImporterJPAStorer( ProlineDBManagement.getProlineDBManagement().getAssociatedDBManagement(),
                                                              ((Integer)parseProperties.get(IMPORT_RF_PROJECT_ID)),                                                                
                                                              idfResultFile,
                                                              rfProvider.fileType(), 
                                                              getProviderKey(),
                                                              ((Integer)parseProperties.get(IMPORT_RF_INSTRUMNET_ID)),
                                                              propScalaMap,null);
        importer.runService();
        targetResultSetId = importer.getTargetResultSetId();
        return true;
    }
        
    public Integer getLastParsedResultSetId(){
        return targetResultSetId;
    }
            
       
}
