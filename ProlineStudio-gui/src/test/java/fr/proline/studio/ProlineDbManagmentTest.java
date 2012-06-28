/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio;

import fr.proline.core.om.model.msi.PtmDefinition;
import fr.proline.core.om.provider.msi.impl.ORMPTMProvider;
import fr.proline.repository.ConnectionPrototype;
import fr.proline.repository.DatabaseConnector;
import fr.proline.repository.ProlineRepository;
import fr.proline.studio.dbs.ProlineDbManagment;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.persistence.EntityManager;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Assert;
import org.junit.Test;
import org.openide.util.Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.None;
import scala.Option;

/**
 *
 * @author VD225637
 */
public class ProlineDbManagmentTest {
    
    protected static Logger logger = LoggerFactory.getLogger(ProlineDbManagmentTest.class);
    protected final static String DB_CONFIG_PROP="/dbs_test_connection.properties";
    
    public ProlineDbManagmentTest() {
    }

    @org.junit.BeforeClass
    public static void setUpClass() throws Exception {
        try {
            ConnectionPrototype conProto = new ConnectionPrototype(DB_CONFIG_PROP);            
            conProto.protocoleValue("./target/test-classes/");           
            repo = ProlineRepository.getRepositoryManager(conProto);           
            ProlineDbManagment.intiProlineDbManagment(repo);
        } catch (IOException ioex) {
            Exceptions.printStackTrace(ioex);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);  
            Assert.fail(ex.getMessage());
        }
    }

    @org.junit.AfterClass
    public static void tearDownClass() throws Exception {
        try {
            ProlineDbManagment.getProlineDbManagment().closeAll();            
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    protected static ProlineRepository repo;
    
    @Before
    public void setUp() {

    }
    
    @After
    public void tearDown() {        
    }
    
    @Test
    public void readPSDBInfo(){
        EntityManager psEM = null;
        try {
            
            //Test JDBC Connection 
            DatabaseConnector dc = ProlineDbManagment.getProlineDbManagment().getDatabaseConnector(ProlineRepository.Databases.PS);
            Assert.assertThat(dc.getConnection().getMetaData().getURL(), CoreMatchers.equalTo("jdbc:h2:file:./target/test-classes/test_ps"));
            
            //Test EntityManager Connection 
            psEM = ProlineDbManagment.getProlineDbManagment().getEntityManager(ProlineRepository.Databases.PS, false);
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
            psEM.close();
        }
    }
        
    @Test
    public void testGetUDSConnection(){
        try {
            
            DatabaseConnector udsDbConn1= ProlineDbManagment.getProlineDbManagment().getDatabaseConnector(ProlineRepository.Databases.UDS);
            DatabaseConnector udsDbConn2 = repo.getConnector(ProlineRepository.Databases.UDS);
            Assert.assertSame(udsDbConn1,udsDbConn2);

            Connection conn = udsDbConn1.getConnection();
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
