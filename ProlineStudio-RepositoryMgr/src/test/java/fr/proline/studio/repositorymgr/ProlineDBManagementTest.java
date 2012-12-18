/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.repositorymgr;

import fr.proline.core.om.model.msi.PtmDefinition;
import fr.proline.core.om.model.msi.SeqDatabase;
import fr.proline.core.om.provider.msi.impl.ORMPTMProvider;
import fr.proline.core.om.provider.msi.impl.ORMSeqDatabaseProvider;
import fr.proline.core.orm.pdi.ProteinIdentifier;
import fr.proline.core.orm.util.DatabaseManager;
import fr.proline.repository.IDatabaseConnector;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import org.hamcrest.CoreMatchers;
import org.junit.*;
import org.openide.util.Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Option;

/**
 *
 * @author VD225637
 */
public class ProlineDBManagementTest {
    
    protected static Logger logger = LoggerFactory.getLogger(ProlineDBManagementTest.class);
    
    public ProlineDBManagementTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        ProlineDatabaseManagerUtil.getOrInitDbManagment();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        try {
            DatabaseManager.getInstance().closeAll(); 
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }
    
//     @Test
    public void readPSDBInfo(){
        EntityManager psEM = null;
        try {
            
            //Test JDBC Connection 
            IDatabaseConnector dc = DatabaseManager.getInstance().getPsDbConnector();
            Assert.assertThat(dc.getDataSource().getConnection().getMetaData().getURL(), CoreMatchers.equalTo("jdbc:h2:file:target/test-classes/test_ps"));
            
            //Test EntityManager Connection 
            psEM = dc.getEntityManagerFactory().createEntityManager();
            fr.proline.core.orm.ps.PtmSpecificity  ptmORMSpecif = psEM.find(fr.proline.core.orm.ps.PtmSpecificity.class, 100);
            Assert.assertNotNull(ptmORMSpecif);
            logger.debug("PTM Def 100 = "+ptmORMSpecif.getLocation()+" - "+ptmORMSpecif.getResidue()+" - "+ptmORMSpecif.getPtm().getShortName());
            Assert.assertEquals("Anywhere",ptmORMSpecif.getLocation());
            Assert.assertEquals("Oxidation",ptmORMSpecif.getPtm().getShortName());
            
            //Test ORMPTMProvider Connection 
            ORMPTMProvider ptmPro=    new ORMPTMProvider(psEM);
            Option<PtmDefinition> ptmDef = ptmPro.getPtmDefinition(100);
            Assert.assertNotNull(ptmDef.get());
            
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
             Assert.fail(ex.getMessage());
        } finally {
            if(psEM != null)
              psEM.close();
        }
    }
    
//    @Test
    public void readPDIDBInfo(){
        EntityManager pdiEM = null;
        try {
            
            //Test JDBC Connection 
            IDatabaseConnector dc = DatabaseManager.getInstance().getPdiDbConnector();            
            Assert.assertThat(dc.getDataSource().getConnection().getMetaData().getURL(), CoreMatchers.equalTo("jdbc:h2:file:target/test-classes/test_pdi"));
            
            //Test EntityManager Connection 
            pdiEM = dc.getEntityManagerFactory().createEntityManager();
            //Debug purpose
            Map<String,Object> pdiProp = pdiEM.getProperties();
            for(Entry<String, Object> e: pdiProp.entrySet()){
               logger.debug("--------- Next Obj "+e.getKey()+" = "+e.getValue());
            }
            TypedQuery tq = pdiEM.createQuery("From fr.proline.core.orm.pdi.ProteinIdentifier",fr.proline.core.orm.pdi.ProteinIdentifier.class);
            List<ProteinIdentifier> all = tq.getResultList();
            for(ProteinIdentifier pi : all){
               logger.debug("--------- Next ProteinIdentifier "+pi.getId());
            }
            
            fr.proline.core.orm.pdi.ProteinIdentifier  protId = pdiEM.find(fr.proline.core.orm.pdi.ProteinIdentifier.class, 341);
            Assert.assertNotNull(protId);
            Assert.assertEquals("1A1D_PSESP",protId.getValue());
            Assert.assertThat(protId.getBioSequence().getLength(), CoreMatchers.equalTo(338));
            
            //Test ORMPTMProvider Connection 
            ORMSeqDatabaseProvider seqDbPro=    new ORMSeqDatabaseProvider(pdiEM);
            Option<SeqDatabase> seqDB = seqDbPro.getSeqDatabase(33);
            Assert.assertNotNull(seqDB.get());
            
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
             Assert.fail(ex.getMessage());
        } finally {
            if(pdiEM != null)
                pdiEM.close();
        }
    }
         
//    @Test
    public void testGetUDSConnection(){
        try {            
            IDatabaseConnector udsDbConn1 = DatabaseManager.getInstance().getUdsDbConnector();            
            Assert.assertNotNull(udsDbConn1); 
            
            Connection conn = udsDbConn1.getDataSource().getConnection();
            String URL = conn.getMetaData().getURL();                        
            Assert.assertThat(URL, CoreMatchers.equalTo("jdbc:h2:file:./target/test-classes/test_uds"));
            
            PreparedStatement ps = conn.prepareStatement("select * from USER_ACCOUNT");
            ResultSet rs = ps.executeQuery();            
            int rowcount = 0;
            if (rs.last()) {
                rowcount = rs.getRow();               
            }
            Assert.assertThat(rowcount, CoreMatchers.equalTo(3));
            
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            Assert.fail(ex.getMessage());
        }
    }
}
