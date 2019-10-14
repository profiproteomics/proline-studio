/* 
 * Copyright (C) 2019 VD225637
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
package fr.proline.studio.rsmexplorer.gui.model.properties;

import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.uds.BiologicalSample;
import fr.proline.core.orm.uds.QuantitationMethod;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.core.orm.uds.dto.DMasterQuantitationChannel;
import fr.proline.core.orm.uds.dto.DQuantitationChannel;
import fr.proline.studio.table.DataGroup;
import fr.proline.studio.utils.SerializedPropertiesUtil;
import java.awt.Color;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;


/**
 *
 * @author JM235353
 */
public class XICPropertiesTableModel extends AbstractPropertiesTableModel {
    
    private ArrayList<DDataset> m_datasetArrayList = null;

    public XICPropertiesTableModel() {

    }

    @Override
    public void setData(ArrayList<DDataset> datasetArrayList) {

        m_datasetArrayList = datasetArrayList;

        int nbDataset = datasetArrayList.size();
        m_datasetNameArray = new ArrayList<>(nbDataset);
        m_projectIdArray = new ArrayList<>(nbDataset);
        m_datasetIdArray = new ArrayList<>(nbDataset);
        int maxMasterQuantitationChannels = 0;
        for (int i=0;i<nbDataset;i++) {
            DDataset dataset = datasetArrayList.get(i);
            m_datasetNameArray.add(dataset.getName());
            m_projectIdArray.add(dataset.getProject().getId());
            m_datasetIdArray.add(dataset.getId());
            
            
            List<DMasterQuantitationChannel> quantitationChannels = dataset.getMasterQuantitationChannels();
            int nbMasterQuantitationChannels = (quantitationChannels == null) ? 0 : quantitationChannels.size();
            if (nbMasterQuantitationChannels > maxMasterQuantitationChannels) {
                maxMasterQuantitationChannels = nbMasterQuantitationChannels;
            }

        }
        
        int[] maxQuantitationChannels = new int[maxMasterQuantitationChannels];
        
        for (int i=0;i<nbDataset;i++) {
            DDataset dataset = datasetArrayList.get(i);
            List<DMasterQuantitationChannel> masterQuantitationChannels = dataset.getMasterQuantitationChannels();
            
            int idMasterQuantitationChannel = 0;
            Iterator<DMasterQuantitationChannel> it = masterQuantitationChannels.iterator(); 
            while (it.hasNext()) {
                DMasterQuantitationChannel masterQuantitationChannel = it.next();
                List<DQuantitationChannel> quantitationChannels =  masterQuantitationChannel.getQuantitationChannels();
                if (quantitationChannels.size()>maxQuantitationChannels[idMasterQuantitationChannel]) {
                    maxQuantitationChannels[idMasterQuantitationChannel] = quantitationChannels.size();
                }
                idMasterQuantitationChannel++;
            }
        }
            
        /*QuantitationChannel quantitationChannel = itChannel.next();
                            sheet.put(createQuantitationChannelSheetSet(quantitationChannel, id));
                            if (quantitationChannel.getBiologicalSample() != null) {
                                sheet.put(createBiologicalSampleSheetSet(quantitationChannel.getBiologicalSample(), id));
                            }
                            id++;*/

        
        
        m_rowCount = -1; // will be recalculated later
         
        

        
        
        if (m_dataGroupList == null) {
            m_dataGroupList = new ArrayList<>();
            
            int startRow = 0;
            DataGroup group = new GeneralInformationGroup(startRow); m_dataGroupList.add(group); startRow += group.getRowCount();
            group = new IdentificationSummaryGroup(startRow, datasetArrayList); m_dataGroupList.add(group); startRow += group.getRowCount();
            group = new QuantProcessingConfigGroup(startRow, datasetArrayList, false); m_dataGroupList.add(group); startRow+=group.getRowCount();

            // check if we have a Post Quant Processing Config
            boolean postQuantProcessingConfig = false;
            for (int i = 0; i < nbDataset; i++) {
                DDataset dataset = datasetArrayList.get(i);
                try {
                    Map<String, Object> objectTreeAsMap = dataset.getPostQuantProcessingConfigAsMap();
                    if ((objectTreeAsMap != null) && (!objectTreeAsMap.isEmpty())) {
                        postQuantProcessingConfig = true;
                    }
                } catch (Exception e) {
                }
            }
            if (postQuantProcessingConfig) {
                group = new QuantProcessingConfigGroup(startRow, datasetArrayList, true); m_dataGroupList.add(group); startRow+=group.getRowCount();
            }
            group = new QuantitationMethodGroup(startRow); m_dataGroupList.add(group); startRow+=group.getRowCount();
            for (int i=0;i<maxMasterQuantitationChannels;i++) {
                group = new MasterQuantitationChannelGroup(startRow, i); m_dataGroupList.add(group); startRow+=group.getRowCount();
                for (int j=0;j<maxQuantitationChannels[i];j++) {
                    group = new QuantitationChannelGroup(startRow, i, j); m_dataGroupList.add(group); startRow+=group.getRowCount();
                    group = new BiologicalSampleGroup(startRow, i, j); m_dataGroupList.add(group); startRow+=group.getRowCount();
                }
            }


        }
        
         m_loaded = true;
        
        fireTableStructureChanged();
    }

    
        
    /**
     * GeneralInformationGroup
     */
    public class GeneralInformationGroup extends DataGroup {

        private static final int ROWTYPE_XIC_ID = 0;
        private static final int ROWTYPE_XIC_DESCRIPTION = 1;
        
        private static final int ROW_COUNT = 1; // get in sync
        private final Color GROUP_COLOR_BACKGROUND = new Color(0, 0, 0);

        public GeneralInformationGroup(int rowStart) {
            super("General Information", rowStart);
        }

        @Override
        public GroupObject getGroupNameAt(int rowIndex) {
            switch (rowIndex) {
                case ROWTYPE_XIC_ID:
                    return new GroupObject("XIC id", this);
                case ROWTYPE_XIC_DESCRIPTION:
                    return new GroupObject("XIC Description", this);
            }

            return null;
        }

        @Override
        public GroupObject getGroupValueAt(int rowIndex, int columnIndex) {

            DDataset dataset = m_datasetArrayList.get(columnIndex);

            switch (rowIndex) {
                case ROWTYPE_XIC_ID:
                    return new GroupObject(String.valueOf(dataset.getId()), this);
                case ROWTYPE_XIC_DESCRIPTION:
                    return new GroupObject(dataset.getDescription(), this);
            }

            return null;
        }

        @Override
        public Color getGroupColor(int row) {
            return GROUP_COLOR_BACKGROUND;
        }

        @Override
        public int getRowCountImpl() {
            return ROW_COUNT;
        }

    }
    
    public class IdentificationSummaryGroup extends DataGroup {

        private static final int ROWTYPE_IDENTIFICATION_SUMMARY_ID = 0;
        private static final int ROWTYPE_DESCRIPTION = 1;
        private static final int ROWTYPE_DATE = 2;
        
        private static final int ROW_COUNT = 3; // get in sync
        private final Color GROUP_COLOR_BACKGROUND = new Color(254,163,71);

        //For extra dynamic information 
        private ArrayList<String> m_valuesName = new ArrayList<String>();
        private HashMap<Integer, HashMap<String, String>> m_valuesMap = new HashMap<>();

        
        public IdentificationSummaryGroup(int rowStart, ArrayList<DDataset> datasetArrayList) {
            super("Identification Summary", rowStart);
            
            TreeSet<String> keysSet = new TreeSet<String>();
            int nbDataset = datasetArrayList.size();
            
            try {
                for (int i = 0; i < nbDataset; i++) {
                    DDataset dataset = datasetArrayList.get(i);
                    ResultSummary rsm = dataset.getResultSummary();
                    if (rsm == null) {
                        continue;
                    }
                    
                    //GET Properties Specific informations
//                    Map<String, Object> map = dataset.getResultSummary().getSerializedPropertiesAsMap();
                   
//                    SerializedPropertiesUtil.getProperties(propertiesList, "Identification Summary Information", map);
//                    m_valuesMap.put(Integer.valueOf(i), propertiesList);
//                    for (String key : propertiesList.keySet()) {
//                        if (!key.contains("validation_properties / results /") && !key.contains("validation_properties / params /")) {
//                            keysSet.add(key);
//                        }
//                    }
//                    
                    //GET SchemaName Specific information
                    HashMap<String, String> propertiesList = new HashMap<String, String>();                    
                    m_valuesMap.put(Integer.valueOf(i), propertiesList);                    
                    Map<String, Long> schemaNames = rsm.getObjectTreeIdByName();
                    if(schemaNames != null && !schemaNames.isEmpty()){
                        for(String nextSchName : schemaNames.keySet()){
                            propertiesList.put(nextSchName, "defined");
                            keysSet.add(nextSchName);
                        }
                    }
                }
            } catch (Exception e) {
                // should not happen
            }   
            
            for (String key : keysSet) {
                m_valuesName.add(key);
            }
        }

        @Override
        public GroupObject getGroupNameAt(int rowIndex) {
            switch (rowIndex) {
                case ROWTYPE_IDENTIFICATION_SUMMARY_ID:
                    return new GroupObject("Identification Summary id", this);
                case ROWTYPE_DESCRIPTION:
                    return new GroupObject("Description", this);
                case ROWTYPE_DATE:
                    return new GroupObject("Date", this);
                default:
                    return new GroupObject(m_valuesName.get(rowIndex - ROW_COUNT), this);                    
            }
            
        }

        @Override
        public GroupObject getGroupValueAt(int rowIndex, int columnIndex) {

            DDataset dataset = m_datasetArrayList.get(columnIndex);
            ResultSummary rsm = dataset.getResultSummary();
            if (rsm == null) {
                return new GroupObject("", this);
            }

            switch (rowIndex) {
                case ROWTYPE_IDENTIFICATION_SUMMARY_ID:
                    return new GroupObject(String.valueOf(rsm.getId()), this);
                case ROWTYPE_DESCRIPTION:
                    return new GroupObject(rsm.getDescription(), this);
                case ROWTYPE_DATE:
                    Timestamp timeStamp = rsm.getModificationTimestamp();
                    DateFormat df = DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault());
                    return new GroupObject(df.format(timeStamp), this);
                default: {
                    String key = m_valuesName.get(rowIndex - ROW_COUNT);
                    String value = m_valuesMap.get(columnIndex).get(key);
                    if (value == null) {
                        value = "";
                    }
                    return new GroupObject(value, this);
                }
                    
            }

        }

        @Override
        public Color getGroupColor(int row) {
            return GROUP_COLOR_BACKGROUND;
        }

        @Override
        public int getRowCountImpl() {
            return ROW_COUNT+ m_valuesName.size();
        }

    }
    
    
    
    /**
     * QuantProcessingConfigGroup
     */
    public class QuantProcessingConfigGroup extends DataGroup {

        private Color m_groupColorBackground;
        
        private ArrayList<String> m_valuesName = new ArrayList<>();
        private HashMap<Integer, HashMap<String, String>> m_valuesMap = new HashMap<>();
        
        public QuantProcessingConfigGroup(int rowStart, ArrayList<DDataset> datasetArrayList, boolean post) {
            super(post ? "Post Quantitation Processing Config" : "Quantitation Processing Config", rowStart);

            m_groupColorBackground = (post) ? new Color(76,120,107) : new Color(76,166,107);
            
            // retrieve information on maps
            TreeSet<String> keysSet = new TreeSet<>();
            int nbDataset = datasetArrayList.size();

            try {
                for (int i = 0; i < nbDataset; i++) {
                    DDataset dataset = datasetArrayList.get(i);

                    Map<String, Object> objectTreeAsMap = post ? dataset.getPostQuantProcessingConfigAsMap() : dataset.getQuantProcessingConfigAsMap();

                    HashMap<String, String> propertiesList = new HashMap<>();
                    SerializedPropertiesUtil.getProperties(propertiesList, post ? "Post Quantitation Processing Config" : "Quantitation Processing Config", objectTreeAsMap);

                    m_valuesMap.put(Integer.valueOf(i), propertiesList);
                    
                    for (String key : propertiesList.keySet()) {
                        keysSet.add(key);
                    }
                }
            } catch (Exception e) {
                // should not happen
            }
            
            for (String key : keysSet) {
                m_valuesName.add(key);
            }
        }
        
        @Override
        public GroupObject getGroupNameAt(int rowIndex) {
            return new GroupObject(m_valuesName.get(rowIndex), this);

        }
        
        @Override
        public GroupObject getGroupValueAt(int rowIndex, int columnIndex) {

            String key = m_valuesName.get(rowIndex);
            String value = m_valuesMap.get(columnIndex).get(key);
            if (value == null) {
                value = "";
            }
            return new GroupObject(value, this);

        }

        @Override
        public Color getGroupColor(int row) {
            return m_groupColorBackground;
        }

        @Override
        public int getRowCountImpl() {
            return m_valuesName.size();
        }

    }
    
    /**
     * GeneralInformationGroup
     */
    public class QuantitationMethodGroup extends DataGroup {

        private static final int ROWTYPE_QUANTI_METHOD_NAME = 0;
        private static final int ROWTYPE_QUANTI_METHOD_TYPE = 1;
        private static final int ROWTYPE_QUANTI_METHOD_ABUNDANCE_UNIT = 2;
        
        private static final int ROW_COUNT = 3; // get in sync
        private final Color GROUP_COLOR_BACKGROUND = new Color(71,163,254);

        public QuantitationMethodGroup(int rowStart) {
            super("Quantitation Method", rowStart);
        }

        @Override
        public GroupObject getGroupNameAt(int rowIndex) {
            switch (rowIndex) {
                case ROWTYPE_QUANTI_METHOD_NAME:
                    return new GroupObject("Name", this);
                case ROWTYPE_QUANTI_METHOD_TYPE:
                    return new GroupObject("Type", this);
                case ROWTYPE_QUANTI_METHOD_ABUNDANCE_UNIT:
                    return new GroupObject("Abundance Unit", this);
            }

            return null;
        }

        @Override
        public GroupObject getGroupValueAt(int rowIndex, int columnIndex) {

            DDataset dataset = m_datasetArrayList.get(columnIndex);
            QuantitationMethod quantMethod = dataset.getQuantitationMethod();

            switch (rowIndex) {
                case ROWTYPE_QUANTI_METHOD_NAME:
                    if (quantMethod == null) {
                        return new GroupObject("", this);
                    }
                    return new GroupObject(quantMethod.getName(), this);
                case ROWTYPE_QUANTI_METHOD_TYPE:
                    if (quantMethod == null) {
                        return new GroupObject("", this);
                    }
                    return new GroupObject(quantMethod.getType(), this);
                case ROWTYPE_QUANTI_METHOD_ABUNDANCE_UNIT:
                    if (quantMethod == null) {
                        return new GroupObject("", this);
                    }
                    return new GroupObject(quantMethod.getAbundanceUnit(), this);
            }

            return null;
        }

        @Override
        public Color getGroupColor(int row) {
            return GROUP_COLOR_BACKGROUND;
        }

        @Override
        public int getRowCountImpl() {
            return ROW_COUNT;
        }

    }
 
    
    /**
     * MasterQuantitationChannelGroup
     */
    public class MasterQuantitationChannelGroup extends DataGroup {

        private static final int ROWTYPE_MASTER_QUANTICHANNEL_ID = 0;
        private static final int ROWTYPE_MASTER_QUANTICHANNEL_NAME = 1;
        private static final int ROWTYPE_MASTER_QUANTICHANNEL_IDENT_DATASET_NAME = 2;        
        private static final int ROWTYPE_MASTER_QUANTICHANNEL_NAME_SERIALIZEDPROPERTIES = 3;
        
        private static final int ROW_COUNT = 3; // get in sync
        private final Color GROUP_COLOR_BACKGROUND = new Color(254,71,163);

        private int m_number;
        
        public MasterQuantitationChannelGroup(int rowStart, int number) {
            super("Master Quantitation Channel "+(number+1), rowStart);
            m_number = number;
        }

        @Override
        public GroupObject getGroupNameAt(int rowIndex) {
            switch (rowIndex) {
                case ROWTYPE_MASTER_QUANTICHANNEL_ID:
                    return new GroupObject("Master Quantitation id", this);
                case ROWTYPE_MASTER_QUANTICHANNEL_NAME:
                    return new GroupObject("Name", this);
                case ROWTYPE_MASTER_QUANTICHANNEL_IDENT_DATASET_NAME:
                    return new GroupObject("Identification Dataset Name", this);
                case ROWTYPE_MASTER_QUANTICHANNEL_NAME_SERIALIZEDPROPERTIES:
                    return new GroupObject("Serialized Properties", this);
            }

            return null;
        }

        @Override
        public GroupObject getGroupValueAt(int rowIndex, int columnIndex) {

            DDataset dataset = m_datasetArrayList.get(columnIndex);
            List<DMasterQuantitationChannel> quantitationChannels = dataset.getMasterQuantitationChannels();
            if ((quantitationChannels == null) || (quantitationChannels.size()<=m_number)) {
                return new GroupObject("", this);
            }
            
            DMasterQuantitationChannel masterQuantitationChannel = quantitationChannels.get(m_number);

            switch (rowIndex) {
                case ROWTYPE_MASTER_QUANTICHANNEL_ID:
                    if (masterQuantitationChannel == null) {
                        return new GroupObject("", this);
                    }
                return new GroupObject(String.valueOf(masterQuantitationChannel.getId()), this);
                case ROWTYPE_MASTER_QUANTICHANNEL_NAME:
                    if (masterQuantitationChannel == null) {
                        return new GroupObject("", this);
                    }
                    return new GroupObject(masterQuantitationChannel.getName(), this);
                case ROWTYPE_MASTER_QUANTICHANNEL_IDENT_DATASET_NAME:
                    if ((masterQuantitationChannel == null) || (masterQuantitationChannel.getIdentDataset() == null)) {
                        return new GroupObject("", this);
                    }
                    return new GroupObject(masterQuantitationChannel.getIdentDataset().getName(), this);

                case ROWTYPE_MASTER_QUANTICHANNEL_NAME_SERIALIZEDPROPERTIES:
                    if (masterQuantitationChannel == null) {
                        return new GroupObject("", this);
                    }
                    return new GroupObject(masterQuantitationChannel.getSerializedProperties(), this);
            }

            return null;
        }

        @Override
        public Color getGroupColor(int row) {
            return GROUP_COLOR_BACKGROUND;
        }

        @Override
        public int getRowCountImpl() {
            return ROW_COUNT;
        }

    }
    
    
    /**
     * QuantitationChannelGroup
     */
    public class QuantitationChannelGroup extends DataGroup {

        private static final int ROWTYPE_QUANTICHANNEL_ID = 0;
        private static final int ROWTYPE_RESULT_FILE_NAME = 1;
        private static final int ROWTYPE_RAW_FILE_PATH = 2;
        private static final int ROWTYPE_MZDB_RAW_FILE_NAME = 3;
        private static final int ROWTYPE_IDENTIFICATION_RSM_ID = 4;
        
        private static final int ROW_COUNT = 5; // get in sync
        private final Color GROUP_COLOR_BACKGROUND = new Color(153,122,141);

        private final int m_numberMaster;
        private final int m_numberChannel;
        
        public QuantitationChannelGroup(int rowStart, int numberMaster, int numberChannel) {
            super("Quantitation Channel "+(numberChannel+1), rowStart);
            m_numberMaster = numberMaster;
            m_numberChannel = numberChannel;
        }

        @Override
        public GroupObject getGroupNameAt(int rowIndex) {
            switch (rowIndex) {
                case ROWTYPE_QUANTICHANNEL_ID:
                    return new GroupObject("Quantitation Channel id", this);
                case ROWTYPE_RESULT_FILE_NAME:
                    return new GroupObject("Result File Name", this);
                case ROWTYPE_RAW_FILE_PATH:
                    return new GroupObject("Raw File Path", this);
                case ROWTYPE_MZDB_RAW_FILE_NAME:
                    return new GroupObject("Mzdb Raw File Name", this);
                case ROWTYPE_IDENTIFICATION_RSM_ID : 
                    return new GroupObject("Identification Summary Id", this);
            }

            return null;
        }

        @Override
        public GroupObject getGroupValueAt(int rowIndex, int columnIndex) {

            DDataset dataset = m_datasetArrayList.get(columnIndex);
            List<DMasterQuantitationChannel> masterQuantitationChannels = dataset.getMasterQuantitationChannels();
            if ((masterQuantitationChannels == null) || (masterQuantitationChannels.size()<=m_numberMaster)) {
                return new GroupObject("", this);
            }
            
            DMasterQuantitationChannel masterQuantitationChannel = masterQuantitationChannels.get(m_numberMaster);

            List<DQuantitationChannel> quantitationChannels =  masterQuantitationChannel.getQuantitationChannels();
            
            if (quantitationChannels.size() <= m_numberChannel) {
                return new GroupObject("", this);
            }

            DQuantitationChannel quantitationChannel = quantitationChannels.get(m_numberChannel);
            if (quantitationChannel == null) {
                return new GroupObject("", this);
            }

            switch (rowIndex) {
                case ROWTYPE_QUANTICHANNEL_ID:
                    return new GroupObject(String.valueOf(quantitationChannel.getId()), this);
                case ROWTYPE_RESULT_FILE_NAME:
                    return new GroupObject(quantitationChannel.getResultFileName(), this);
                case ROWTYPE_RAW_FILE_PATH:
                    return new GroupObject(quantitationChannel.getRawFilePath(), this);
                case ROWTYPE_MZDB_RAW_FILE_NAME:
                    return new GroupObject(quantitationChannel.getMzdbFileName(), this);
                case ROWTYPE_IDENTIFICATION_RSM_ID:
                    return new GroupObject(String.valueOf(quantitationChannel.getIdentResultSummaryId()), this);
            }

            return null;
        }

        @Override
        public Color getGroupColor(int row) {
            return GROUP_COLOR_BACKGROUND;
        }

        @Override
        public int getRowCountImpl() {
            return ROW_COUNT;
        }

    }
    
    
    
    /**
     * BiologicalSampleGroup
     */
    public class BiologicalSampleGroup extends DataGroup {

        private static final int ROWTYPE_BIOLOGICAL_SAMPLE_ID = 0;
        private static final int ROWTYPE_BIOLOGICAL_SAMPLE_NAME = 1;
        
        private static final int ROW_COUNT = 2; // get in sync
        private final Color GROUP_COLOR_BACKGROUND = new Color(122, 141,153);

        private final int m_numberMaster;
        private final int m_numberChannel;
        
        public BiologicalSampleGroup(int rowStart, int numberMaster, int numberChannel) {
            super("Biological Sample "+(numberChannel+1), rowStart);
            m_numberMaster = numberMaster;
            m_numberChannel = numberChannel;
        }

        @Override
        public GroupObject getGroupNameAt(int rowIndex) {
            switch (rowIndex) {
                case ROWTYPE_BIOLOGICAL_SAMPLE_ID:
                    return new GroupObject("Biological Sample id", this);
                case ROWTYPE_BIOLOGICAL_SAMPLE_NAME:
                    return new GroupObject("Biological Sample Name", this);
            }

            return null;
        }

        @Override
        public GroupObject getGroupValueAt(int rowIndex, int columnIndex) {

            DDataset dataset = m_datasetArrayList.get(columnIndex);
            List<DMasterQuantitationChannel> masterQuantitationChannels = dataset.getMasterQuantitationChannels();
            if ((masterQuantitationChannels == null) || (masterQuantitationChannels.size()<=m_numberMaster)) {
                return new GroupObject("", this);
            }
            
            DMasterQuantitationChannel masterQuantitationChannel = masterQuantitationChannels.get(m_numberMaster);

            List<DQuantitationChannel> quantitationChannels =  masterQuantitationChannel.getQuantitationChannels();
            
            if (quantitationChannels.size() <= m_numberChannel) {
                return new GroupObject("", this);
            }

            DQuantitationChannel quantitationChannel = quantitationChannels.get(m_numberChannel);
            BiologicalSample biologicalSample = quantitationChannel.getBiologicalSample();
            if (biologicalSample == null) {
                return new GroupObject("", this);
            }

            switch (rowIndex) {
                case ROWTYPE_BIOLOGICAL_SAMPLE_ID:
                    return new GroupObject(String.valueOf(biologicalSample.getId()), this);
                case ROWTYPE_BIOLOGICAL_SAMPLE_NAME:
                    return new GroupObject(biologicalSample.getName(), this);
            }

            return null;
        }

        @Override
        public Color getGroupColor(int row) {
            return GROUP_COLOR_BACKGROUND;
        }

        @Override
        public int getRowCountImpl() {
            return ROW_COUNT;
        }

    }
}
