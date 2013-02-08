/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.idfimport;

/*import fr.proline.core.orm.util.DataStoreConnectorFactory;
import fr.proline.repository.IDatabaseConnector;
import java.io.File;
import java.net.URISyntaxException;
import org.junit.*;
import org.openide.util.Exceptions;*/

/**
 *
 * @author VD225637
 */
public class ParseMascotIdentTest {
    /*
    protected final static String DB_CONFIG_PROP="/dbs_test_connection.properties";
     */
    public ParseMascotIdentTest() {
    }

//    @BeforeClass
    public static void setUpClass() throws Exception {       
      //  getOrInitDbManager();
    }

   // @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
   // @Before
    public void setUp() {        
//        ProvidersFactory.registerPeptideProvider(ImportResultFile.PROVIDER_KEY, new ORMPeptideProvider(ProlineDbManagment.getProlineDbManagment().getEntityManager(ProlineRepository.Databases.PS, false)));
//        ProvidersFactory.registerPTMProvider(ImportResultFile.PROVIDER_KEY, new ORMPTMProvider(ProlineDbManagment.getProlineDbManagment().getEntityManager(ProlineRepository.Databases.PS, false)));
//        ProvidersFactory.registerProteinProvider(ImportResultFile.PROVIDER_KEY, new ProteinFakeProvider());
//        ProvidersFactory.registerSeqDatabaseProvider(ImportResultFile.PROVIDER_KEY, new ORMSeqDatabaseProvider(ProlineDbManagment.getProlineDbManagment().getEntityManager(ProlineRepository.Databases.PDI, false)));
    }
    
 //   @After
    public void tearDown() {
    }
    
//    @Test
    public void testParseDatFile(){
      /*  File mascotFile = null;
        try {
            mascotFile = new File(this.getClass().getResource("/F065306.dat").toURI());
        } catch (URISyntaxException ex) {
            Exceptions.printStackTrace(ex);
            Assert.fail(ex.getMessage());
        }*/
//       IResultFile rf = MascotResultFileProvider.getResultFile(mascotFile, ImportResultFile.PROVIDER_KEY_);    
//       Assert.assertThat(rf, CoreMatchers.notNullValue());
    }
        
    /*private static DataStoreConnectorFactory getOrInitDbManager()  {        
        try {
            if(!DataStoreConnectorFactory.getInstance().isInitialized())
            DataStoreConnectorFactory.getInstance().initialize(DB_CONFIG_PROP);
        } catch(Exception e){
            Exceptions.printStackTrace(e);    
            return null;
        }        
    
        return DataStoreConnectorFactory.getInstance();
    }*/
    
}
