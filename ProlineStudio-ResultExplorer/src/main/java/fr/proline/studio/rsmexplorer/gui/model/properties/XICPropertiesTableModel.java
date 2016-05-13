package fr.proline.studio.rsmexplorer.gui.model.properties;

import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.uds.BiologicalSample;
import fr.proline.core.orm.uds.QuantitationMethod;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.core.orm.uds.dto.DMasterQuantitationChannel;
import fr.proline.core.orm.uds.dto.DQuantitationChannel;
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
    
    public XICPropertiesTableModel() {

    }

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
            m_dataGroupMap = new HashMap<>();
            
            int startRow = 0;
            DataGroup group = new GeneralInformationGroup(startRow); m_dataGroupList.add(group); startRow += group.getRowCount();
            group = new IdentificationSummaryGroup(startRow); m_dataGroupList.add(group); startRow += group.getRowCount();
            group = new QuantProcessingConfigGroup(startRow, datasetArrayList); m_dataGroupList.add(group); startRow+=group.getRowCount();
            group = new QuantitationMethodGroup(startRow); m_dataGroupList.add(group); startRow+=group.getRowCount();
            for (int i=0;i<maxMasterQuantitationChannels;i++) {
                group = new MasterQuantitationChannelGroup(startRow, i); m_dataGroupList.add(group); startRow+=group.getRowCount();
                for (int j=0;j<maxQuantitationChannels[i];j++) {
                    group = new QuantitationChannelGroup(startRow, i, j); m_dataGroupList.add(group); startRow+=group.getRowCount();
                    group = new BiologicalSampleGroup(startRow, i, j); m_dataGroupList.add(group); startRow+=group.getRowCount();
                }
            }


        }
        
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
        public String getGroupNameAt(int rowIndex) {
            switch (rowIndex) {
                case ROWTYPE_XIC_ID:
                    return "XIC id";
                case ROWTYPE_XIC_DESCRIPTION:
                    return "XIC Description";
            }

            return null;
        }

        @Override
        public String getGroupValueAt(int rowIndex, int columnIndex) {

            DDataset dataset = m_datasetArrayList.get(columnIndex);

            switch (rowIndex) {
                case ROWTYPE_XIC_ID:
                    return String.valueOf(dataset.getId());
                case ROWTYPE_XIC_DESCRIPTION:
                    return dataset.getDescription();
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

        public IdentificationSummaryGroup(int rowStart) {
            super("Identification Summary", rowStart);
        }

        @Override
        public String getGroupNameAt(int rowIndex) {
            switch (rowIndex) {
                case ROWTYPE_IDENTIFICATION_SUMMARY_ID:
                    return "Identification Summary id";
                case ROWTYPE_DESCRIPTION:
                    return "Description";
                case ROWTYPE_DATE:
                    return "Date";
            }

            return null;
        }

        @Override
        public String getGroupValueAt(int rowIndex, int columnIndex) {

            DDataset dataset = m_datasetArrayList.get(columnIndex);
            ResultSummary rsm = dataset.getResultSummary();
            if (rsm == null) {
                return "";
            }

            switch (rowIndex) {
                case ROWTYPE_IDENTIFICATION_SUMMARY_ID:
                    return String.valueOf(rsm.getId());
                case ROWTYPE_DESCRIPTION:
                    return rsm.getDescription();
                case ROWTYPE_DATE:
                    Timestamp timeStamp = rsm.getModificationTimestamp();
                    DateFormat df = DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault());
                    return df.format(timeStamp);
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
     * QuantProcessingConfigGroup
     */
    public class QuantProcessingConfigGroup extends DataGroup {

        private final Color GROUP_COLOR_BACKGROUND = new Color(76,166,107);
        
        private ArrayList<String> m_valuesName = new ArrayList<>();
        private HashMap<Integer, HashMap<String, String>> m_valuesMap = new HashMap<>();
        
        public QuantProcessingConfigGroup(int rowStart, ArrayList<DDataset> datasetArrayList) {
            super("Quantitation Processing Config", rowStart);

            // retrieve information on maps
            TreeSet<String> keysSet = new TreeSet<>();
            int nbDataset = datasetArrayList.size();

            try {
                for (int i = 0; i < nbDataset; i++) {
                    DDataset dataset = datasetArrayList.get(i);
                    
                    //fr.proline.core.orm.uds.ObjectTree objectTree = dataset.getQuantProcessingConfig();
                    Map<String, Object> objectTreeAsMap = dataset.getQuantProcessingConfigAsMap();

                    HashMap<String, String> propertiesList = new HashMap<>();
                    SerializedPropertiesUtil.getProperties(propertiesList, "Quantitation Processing Config", objectTreeAsMap);

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
        public String getGroupNameAt(int rowIndex) {
            return m_valuesName.get(rowIndex);

        }
        
        @Override
        public String getGroupValueAt(int rowIndex, int columnIndex) {

            String key = m_valuesName.get(rowIndex);
            String value = m_valuesMap.get(columnIndex).get(key);
            if (value == null) {
                value = "";
            }
            return value;

        }

        @Override
        public Color getGroupColor(int row) {
            return GROUP_COLOR_BACKGROUND;
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
        public String getGroupNameAt(int rowIndex) {
            switch (rowIndex) {
                case ROWTYPE_QUANTI_METHOD_NAME:
                    return "Name";
                case ROWTYPE_QUANTI_METHOD_TYPE:
                    return "Type";
                case ROWTYPE_QUANTI_METHOD_ABUNDANCE_UNIT:
                    return "Abundance Unit";
            }

            return null;
        }

        @Override
        public String getGroupValueAt(int rowIndex, int columnIndex) {

            DDataset dataset = m_datasetArrayList.get(columnIndex);
            QuantitationMethod quantMethod = dataset.getQuantitationMethod();


            switch (rowIndex) {
                case ROWTYPE_QUANTI_METHOD_NAME:
                    if (quantMethod == null) {
                        return "";
                    }
                    return quantMethod.getName();
                case ROWTYPE_QUANTI_METHOD_TYPE:
                    if (quantMethod == null) {
                        return "";
                    }
                    return quantMethod.getType();
                case ROWTYPE_QUANTI_METHOD_ABUNDANCE_UNIT:
                    if (quantMethod == null) {
                        return "";
                    }
                    return quantMethod.getAbundanceUnit();
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
        private static final int ROWTYPE_MASTER_QUANTICHANNEL_NAME_SERIALIZEDPROPERTIES = 2;
        
        private static final int ROW_COUNT = 3; // get in sync
        private final Color GROUP_COLOR_BACKGROUND = new Color(254,71,163);

        private int m_number;
        
        public MasterQuantitationChannelGroup(int rowStart, int number) {
            super("Master Quantitation Channel "+(number+1), rowStart);
            m_number = number;
        }

        @Override
        public String getGroupNameAt(int rowIndex) {
            switch (rowIndex) {
                case ROWTYPE_MASTER_QUANTICHANNEL_ID:
                    return "Master Quantitation id";
                case ROWTYPE_MASTER_QUANTICHANNEL_NAME:
                    return "Name";
                case ROWTYPE_MASTER_QUANTICHANNEL_NAME_SERIALIZEDPROPERTIES:
                    return "Serialized Properties";
            }

            return null;
        }

        @Override
        public String getGroupValueAt(int rowIndex, int columnIndex) {

            DDataset dataset = m_datasetArrayList.get(columnIndex);
            List<DMasterQuantitationChannel> quantitationChannels = dataset.getMasterQuantitationChannels();
            if ((quantitationChannels == null) || (quantitationChannels.size()<=m_number)) {
                return "";
            }
            
            DMasterQuantitationChannel masterQuantitationChannel = quantitationChannels.get(m_number);

            switch (rowIndex) {
                case ROWTYPE_MASTER_QUANTICHANNEL_ID:
                    if (masterQuantitationChannel == null) {
                        return "";
                    }
                return String.valueOf(masterQuantitationChannel.getId());
                case ROWTYPE_MASTER_QUANTICHANNEL_NAME:
                    if (masterQuantitationChannel == null) {
                        return "";
                    }
                    return masterQuantitationChannel.getName();
                case ROWTYPE_MASTER_QUANTICHANNEL_NAME_SERIALIZEDPROPERTIES:
                    if (masterQuantitationChannel == null) {
                        return "";
                    }
                    return masterQuantitationChannel.getSerializedProperties();
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
        public String getGroupNameAt(int rowIndex) {
            switch (rowIndex) {
                case ROWTYPE_QUANTICHANNEL_ID:
                    return "Quantitation Channel id";
                case ROWTYPE_RESULT_FILE_NAME:
                    return "Result File Name";
                case ROWTYPE_RAW_FILE_PATH:
                    return "Raw File Path";
                case ROWTYPE_MZDB_RAW_FILE_NAME:
                    return "Mzdb Raw File Name";
                case ROWTYPE_IDENTIFICATION_RSM_ID : 
                    return "Identification Summary Id";
            }

            return null;
        }

        @Override
        public String getGroupValueAt(int rowIndex, int columnIndex) {

            DDataset dataset = m_datasetArrayList.get(columnIndex);
            List<DMasterQuantitationChannel> masterQuantitationChannels = dataset.getMasterQuantitationChannels();
            if ((masterQuantitationChannels == null) || (masterQuantitationChannels.size()<=m_numberMaster)) {
                return "";
            }
            
            DMasterQuantitationChannel masterQuantitationChannel = masterQuantitationChannels.get(m_numberMaster);

            List<DQuantitationChannel> quantitationChannels =  masterQuantitationChannel.getQuantitationChannels();
            
            if (quantitationChannels.size() <= m_numberChannel) {
                return "";
            }

            DQuantitationChannel quantitationChannel = quantitationChannels.get(m_numberChannel);
            if (quantitationChannel == null) {
                return "";
            }

            switch (rowIndex) {
                case ROWTYPE_QUANTICHANNEL_ID:
                    return String.valueOf(quantitationChannel.getId());
                case ROWTYPE_RESULT_FILE_NAME:
                    return quantitationChannel.getResultFileName();
                case ROWTYPE_RAW_FILE_PATH:
                    return quantitationChannel.getRawFilePath();
                case ROWTYPE_MZDB_RAW_FILE_NAME:
                    return quantitationChannel.getMzdbFileName();
                case ROWTYPE_IDENTIFICATION_RSM_ID:
                    return String.valueOf(quantitationChannel.getIdentResultSummaryId());
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
        public String getGroupNameAt(int rowIndex) {
            switch (rowIndex) {
                case ROWTYPE_BIOLOGICAL_SAMPLE_ID:
                    return "Biological Sample id";
                case ROWTYPE_BIOLOGICAL_SAMPLE_NAME:
                    return "Biological Sample Name";
            }

            return null;
        }

        @Override
        public String getGroupValueAt(int rowIndex, int columnIndex) {

            DDataset dataset = m_datasetArrayList.get(columnIndex);
            List<DMasterQuantitationChannel> masterQuantitationChannels = dataset.getMasterQuantitationChannels();
            if ((masterQuantitationChannels == null) || (masterQuantitationChannels.size()<=m_numberMaster)) {
                return "";
            }
            
            DMasterQuantitationChannel masterQuantitationChannel = masterQuantitationChannels.get(m_numberMaster);

            List<DQuantitationChannel> quantitationChannels =  masterQuantitationChannel.getQuantitationChannels();
            
            if (quantitationChannels.size() <= m_numberChannel) {
                return "";
            }

            DQuantitationChannel quantitationChannel = quantitationChannels.get(m_numberChannel);
            BiologicalSample biologicalSample = quantitationChannel.getBiologicalSample();
            if (biologicalSample == null) {
                return "";
            }

            switch (rowIndex) {
                case ROWTYPE_BIOLOGICAL_SAMPLE_ID:
                    return String.valueOf(biologicalSample.getId());
                case ROWTYPE_BIOLOGICAL_SAMPLE_NAME:
                    return biologicalSample.getName();
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
