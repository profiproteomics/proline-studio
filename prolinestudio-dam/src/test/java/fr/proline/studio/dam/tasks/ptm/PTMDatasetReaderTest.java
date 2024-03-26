/* 
 * Copyright (C) 2019
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1 
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License 
 * along with this program; If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
 */
package fr.proline.studio.dam.tasks.ptm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import fr.proline.studio.Exceptions;
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
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        JSONPTMDataset values;
        try {
            values = mapper.readValue(jsonDS, JSONPTMDataset.class);

            Assert.assertEquals(2, values.leafResultSummaryIds.length);
            Assert.assertArrayEquals(leafIds, values.leafResultSummaryIds);

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
