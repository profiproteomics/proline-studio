/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio;

import fr.proline.core.om.model.msi.IResultFile;
import fr.proline.core.om.model.msi.Protein;
import fr.proline.core.om.model.msi.SeqDatabase;
import fr.proline.core.om.provider.msi.IProteinProvider;
import fr.proline.core.om.provider.msi.IProteinProvider$class;
import fr.proline.core.om.provider.msi.ProvidersFactory;
import fr.proline.core.om.provider.msi.impl.ORMPTMProvider;
import fr.proline.core.om.provider.msi.impl.ORMPeptideProvider;
import fr.proline.core.om.provider.msi.impl.ORMSeqDatabaseProvider;
import fr.proline.module.parser.mascot.MascotResultFileProvider;
import fr.proline.repository.ProlineRepository;
import fr.proline.studio.dbs.ProlineDbManagment;
import java.io.File;
import java.net.URISyntaxException;
import java.util.HashMap;
import org.hamcrest.CoreMatchers;
import org.junit.*;
import org.openide.util.Exceptions;
import scala.Option;
import scala.Some;
import scala.collection.Seq;

/**
 *
 * @author VD225637
 */
public class ParseMascotIdentTest {
    
    protected final static String DB_CONFIG_PROP="/dbs_test_connection.properties";
     
    public ParseMascotIdentTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {       
        ProlineDbManagmentUtil.getOrInitDbManagment();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {        
        ProvidersFactory.registerPeptideProvider(ParseMascotIdent.PROVIDER_KEY, new ORMPeptideProvider(ProlineDbManagment.getProlineDbManagment().getEntityManager(ProlineRepository.Databases.PS, false)));
        ProvidersFactory.registerPTMProvider(ParseMascotIdent.PROVIDER_KEY, new ORMPTMProvider(ProlineDbManagment.getProlineDbManagment().getEntityManager(ProlineRepository.Databases.PS, false)));
        ProvidersFactory.registerProteinProvider(ParseMascotIdent.PROVIDER_KEY, new ProteinFakeProvider());
        ProvidersFactory.registerSeqDatabaseProvider(ParseMascotIdent.PROVIDER_KEY, new ORMSeqDatabaseProvider(ProlineDbManagment.getProlineDbManagment().getEntityManager(ProlineRepository.Databases.PDI, false)));
    }
    
    @After
    public void tearDown() {
    }
    
    @Test
    public void testParseDatFile(){
        File mascotFile = null;
        try {
            mascotFile = new File(this.getClass().getResource("/F065306.dat").toURI());
        } catch (URISyntaxException ex) {
            Exceptions.printStackTrace(ex);
            Assert.fail(ex.getMessage());
        }
       IResultFile rf = MascotResultFileProvider.getResultFile(mascotFile, ParseMascotIdent.PROVIDER_KEY);    
       Assert.assertThat(rf, CoreMatchers.notNullValue());
    }
    
    
     public class ProteinFakeProvider implements IProteinProvider{
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
