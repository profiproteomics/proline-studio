/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.dam.tasks.ptm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import fr.proline.studio.dam.tasks.data.ptm.JSONPTMDataset;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openide.util.Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author VD225637
 */
public class PTMDatasetReaderTest {

    private static Logger LOG = LoggerFactory.getLogger(PTMDatasetReaderTest.class);
    public PTMDatasetReaderTest() {
    }
    
    static String jsonDS;
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @Before
    public void setUp() {
        StringBuilder contentBuilder = new StringBuilder();
        try {
            File f = new File("src/test/java/fr/proline/studio/dam/tasks/ptm/ptmDataset.JSON");
            LOG.info(" FILE PATH "+f.getAbsolutePath());

            InputStream fileIS = new FileInputStream(f);    
            BufferedReader br = new BufferedReader(new InputStreamReader(fileIS));
            String line = br.readLine();
            while(line !=null ) {
                contentBuilder.append(line);        
                line = br.readLine();
            }           
            jsonDS = contentBuilder.toString();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void testReadPTMDataset() {
        Assert.assertTrue(jsonDS != null);
        Long[] leafIds = {4l, 3l};

        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        JSONPTMDataset values;
        try {
            values = mapper.readValue(jsonDS, JSONPTMDataset.class);

            Assert.assertEquals(2, values.leafResultSummaryIds.length);
            Assert.assertEquals(leafIds, values.leafResultSummaryIds);

            Assert.assertEquals(4, values.ptmIds.length);
            Assert.assertTrue(Arrays.asList(values.ptmIds).contains(27l));
            
            Assert.assertEquals(18, values.ptmSites.length);
            Assert.assertEquals(19, values.ptmClusters.length);
            
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            Assert.fail("Error reading JSON");
        }
    }
    
}
