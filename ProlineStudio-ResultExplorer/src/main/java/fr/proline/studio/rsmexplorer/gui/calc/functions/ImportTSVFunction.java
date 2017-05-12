package fr.proline.studio.rsmexplorer.gui.calc.functions;



import au.com.bytecode.opencsv.CSVReader;
import fr.proline.studio.comparedata.ExtraDataType;
import fr.proline.studio.export.ExportModelUtilities;
import fr.proline.studio.export.ExportSubStringFont;
import fr.proline.studio.filter.DoubleFilter;
import fr.proline.studio.filter.Filter;
import fr.proline.studio.filter.LongFilter;
import fr.proline.studio.filter.StringDiffFilter;
import fr.proline.studio.parameter.DisplayStubParameter;
import fr.proline.studio.graphics.PlotInformation;
import fr.proline.studio.graphics.PlotType;
import fr.proline.studio.parameter.AbstractLinkedParameters;
import fr.proline.studio.parameter.AbstractParameter;
import fr.proline.studio.parameter.FileParameter;
import fr.proline.studio.parameter.ParameterError;
import fr.proline.studio.parameter.ParameterList;
import fr.proline.studio.parameter.StringParameter;
import fr.proline.studio.pattern.WindowBox;
import fr.proline.studio.python.interpreter.CalcError;
import fr.proline.studio.rsmexplorer.gui.calc.GraphPanel;
import fr.proline.studio.rsmexplorer.gui.calc.ProcessCallbackInterface;
import fr.proline.studio.rsmexplorer.gui.calc.graph.AbstractConnectedGraphObject;
import fr.proline.studio.rsmexplorer.gui.calc.graph.FunctionGraphNode;
import fr.proline.studio.table.DecoratedTable;
import fr.proline.studio.table.renderer.DefaultLeftAlignRenderer;
import fr.proline.studio.table.renderer.DefaultRightAlignRenderer;
import fr.proline.studio.table.renderer.DoubleRenderer;
import fr.proline.studio.table.DecoratedTableModel;
import fr.proline.studio.table.GlobalTableModelInterface;
import fr.proline.studio.table.LazyData;
import fr.proline.studio.table.TableDefaultRendererManager;
import fr.proline.studio.table.TablePopupMenu;
import fr.proline.studio.utils.IconManager;
import java.awt.Color;
import java.awt.Dimension;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.ImageIcon;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author JM235353
 */
public class ImportTSVFunction extends AbstractFunction {

    private static final String KEY_FILE_PARAMETER = "FILE_KEY";
    private static final String SEPARATOR_PARAMETER = "SEPARATOR_KEY";
    
    private ParameterList m_parameterList;
    private FileParameter m_fileParameter;

    private StringParameter m_separatorParameter;
    private DisplayStubParameter m_displayTableParameter;
    
    private LoadedDataModel m_displayModel = new LoadedDataModel();
    
    private String m_modelName = null;
    
    private HashMap<String, String[]> m_prioritySeparatorMap = new HashMap<>();
    
    private static final Color FRAME_COLOR = new Color(51,128,200);
    
    
    
    public ImportTSVFunction(GraphPanel panel) {
        super(panel);
    }
    
    @Override
    public Color getFrameColor() {
        return FRAME_COLOR;
    }
    
    @Override
    public String getDataName() {
        if (m_fileParameter != null) {
            String path = m_fileParameter.getStringValue();
            File f = new File(path);
            return f.getName();
        }
        return null;
    }

    @Override
    public ImageIcon getIcon() {
        return IconManager.getIcon(IconManager.IconType.TABLE_IMPORT);
    }
    
    @Override
    public void inLinkDeleted() {
        super.inLinkDeleted();
        m_parameterList= null;
        m_fileParameter = null;
    }
    
    @Override
    public String getName() {
        return "CSV/TSV Import";
    }

    @Override
    public int getNumberOfInParameters() {
        return 0;
    }

    @Override
    public boolean settingsDone() {
        return (m_fileParameter != null);
    }
    
    @Override
    public boolean calculationDone() {
        return (m_globalTableModelInterface != null);
    }
    
    @Override
    public AbstractFunction cloneFunction(GraphPanel p) {
        AbstractFunction clone = new ImportTSVFunction(p);
        clone.cloneInfo(this);
        return clone;
    }
    
    @Override
    public void process(AbstractConnectedGraphObject[] graphObjects, FunctionGraphNode functionGraphNode, ProcessCallbackInterface callback) {

        try {

            // check if we have already processed
            if (m_globalTableModelInterface != null) {
                return;
            }

            setCalculating(true);
            setInError(false, null);

            //JPM.TODO : should be done in a thread *****************************************************
            String filePath = ((String) m_fileParameter.getObjectValue()).trim();

            char separator = getSeparator();

            LoadedDataModel loadedModel = new LoadedDataModel();

            Exception e = loadFile(loadedModel, filePath, separator, true, false);
            if (e != null) {
                setInError(new CalcError(e, null, -1));
            } else {
                addModel(loadedModel);
            }


            setCalculating(false);

        } finally {
            callback.finished(functionGraphNode);
        }

    }
    
    private Exception loadFile(LoadedDataModel model, String filePath, char separator, boolean header, boolean testFile) {
        
        File f = new File(filePath);
        if (!f.exists()) {
            model.setData(null, null, null);
        }
        
        try {
            CSVReader reader = new CSVReader(new FileReader(filePath), separator);

            
            
        // read column headers
        String[] headerLine = null;
        int nbColumns = 0;
        int line = 1;
        if (header) {
            headerLine = reader.readNext();
            if (headerLine == null) {
                model.setData(null, null, null);
                throw new IOException("No Data found in File");
            }
            nbColumns = headerLine.length;
            line = 2;
        }
        
        if (headerLine == null) {
            headerLine = new String[nbColumns];
            for (int i = 0; i < nbColumns; i++) {
                headerLine[i] = new String(String.valueOf(i + 1));
            }
        }

        // read lines, skip empty ones and check the number of columns
        ArrayList<String[]> allDataLines = new ArrayList<>();
        String[] dataLine;
        while ((dataLine = reader.readNext()) != null) {

            int nbColumsCur = dataLine.length;
            if (nbColumsCur == 0) {
                // continue : empty line
            } else if (nbColumns == 0) {
                nbColumns = nbColumsCur;
            }

            if (nbColumns != nbColumsCur) {
                model.setData(null, null, null);
                return new IOException("Number of columns differ in file at line " + line);
            }

            allDataLines.add(dataLine);

            line++;

            if ((testFile) && (line >= 7)) {
                // we read only the first lines of the file
                break;
                
            }
        }



        // check the type of columns
        int nbRows = allDataLines.size();
        Class[] columTypes = new Class[nbColumns];
        for (int col = 0; col < nbColumns; col++) {

            boolean canBeLong = true;
            boolean canBeDouble = true;
            for (int row = 0; row < nbRows; row++) {
                String data = allDataLines.get(row)[col];
                if (canBeLong) {
                    try {
                        Long.parseLong(data);
                    } catch (NumberFormatException nfe) {
                        canBeLong = false;
                    }
                }
                if ((!canBeLong) && (canBeDouble)) {
                    try {
                        Double.parseDouble(data);
                    } catch (NumberFormatException nfe) {
                        canBeDouble = false;
                        break;
                    }
                }
            }
            if (canBeLong) {
                columTypes[col] = Long.class;
            } else if (canBeDouble) {
                columTypes[col] = Double.class;
            } else {
                columTypes[col] = String.class;
            }
        }

        // create the model
        Object[][] data = new Object[nbRows][nbColumns];
        for (int row = 0; row < nbRows; row++) {
            for (int col = 0; col < nbColumns; col++) {
                String value = allDataLines.get(row)[col];
                if (columTypes[col].equals(Long.class)) {
                    data[row][col] = Long.parseLong(value);
                } else if (columTypes[col].equals(Double.class)) {
                    data[row][col] = Double.parseDouble(value);
                } else {
                    data[row][col] = value;
                }

            }
        }

        
        model.setData(headerLine, columTypes, data);

        } catch (Exception e) {
            model.setData(null, null, null);
            return e;
        }
        
        return null;
    }
    
    @Override
    public void askDisplay(FunctionGraphNode functionGraphNode) {
        display(m_fileParameter.getStringValue(), getName());
    }
    
    @Override
    public ArrayList<WindowBox> getDisplayWindowBox(FunctionGraphNode functionGraphNode) {
        return getDisplayWindowBox(functionGraphNode.getPreviousDataName(), getName());
    }
    
    @Override
    public void generateDefaultParameters(AbstractConnectedGraphObject[] graphObjects) {

        String[] separatorsCSV = {",", ";", "\\t", " " };
        m_prioritySeparatorMap.put("csv", separatorsCSV);
        
        String[] separatorsTSV = {"\\t", ",", ";", " " };
        m_prioritySeparatorMap.put("tsv", separatorsTSV);
        
        
        final String[] fileFilterNames = { "TSV File", "CSV File" };
        final String[] fileFilterExtensions = { "tsv", "csv" };
        
        m_fileParameter = new FileParameter(null, KEY_FILE_PARAMETER, "CSV/TSV File", JTextField.class, "", fileFilterNames, fileFilterExtensions);
        
        String[] possibilitiesName = { "Tab", "Comma", "Semicolon", "Space" };
        String[] possibilities = { "\\t", ",", ";", " " };
        m_separatorParameter = new StringParameter(SEPARATOR_PARAMETER, "Separator", ";",1,2, possibilitiesName, possibilities);
        m_separatorParameter.forceShowLabel(AbstractParameter.LabelVisibility.AS_BORDER_TITLE);
        
        
        DecoratedTable previewTable = new PreviewTable();
        previewTable.setModel(m_displayModel);
        JScrollPane scrollPane = new JScrollPane() {

            private final Dimension preferredSize = new Dimension(360, 140);

            @Override
            public Dimension getPreferredSize() {
                return preferredSize;
            }
        };
        scrollPane.setViewportView(previewTable);
        previewTable.setFillsViewportHeight(true);
        m_displayTableParameter = new DisplayStubParameter("Preview", scrollPane);
        
        
        m_parameterList = new ParameterList("ImportTSV");
        m_parameters = new ParameterList[1];
        m_parameters[0] = m_parameterList;
        
        m_parameterList.add(m_fileParameter);
        m_parameterList.add(m_separatorParameter);
        m_parameterList.add(m_displayTableParameter);
        

        AbstractLinkedParameters fileLinkedParameter = new AbstractLinkedParameters(m_parameterList) {
            @Override
            public void valueChanged(String value, Object associatedValue) {
                String filePath = m_fileParameter.getStringValue();
                String[] separators = m_prioritySeparatorMap.get("csv"); // for CSV or other extensions like txt
                if (filePath.endsWith("tsv")) {
                    separators = m_prioritySeparatorMap.get("tsv");
                }

                boolean found = false;
                for (int i=0;i<separators.length;i++) {
                    Exception e = loadFile(m_displayModel, m_fileParameter.getStringValue(), separatorToChar(separators[i]), true, true);
                    if (e == null) {
                        if (m_displayModel.getColumnCount() > 1) {
                            m_separatorParameter.setValue(separators[i]);
                            found = true;
                            break;
                        }
                    }
                    
                }
                if (!found) {
                    loadFile(m_displayModel, m_fileParameter.getStringValue(), separatorToChar(separators[0]), true, true);
                    m_separatorParameter.setValue(separators[0]);
                }
                
            }
            
        };
        
        AbstractLinkedParameters separatorLinkedParameter = new AbstractLinkedParameters(m_parameterList) {
            @Override
            public void valueChanged(String value, Object associatedValue) {
                loadFile(m_displayModel, m_fileParameter.getStringValue(), getSeparator(), true, true);
            }
            
        };

        m_parameterList.getPanel(); // generate panel at once


        m_fileParameter.addLinkedParameters(fileLinkedParameter); // link parameter, it will modify the panel
        m_separatorParameter.addLinkedParameters(separatorLinkedParameter); // link parameter, it will modify the panel
    }
    
    @Override
    public ParameterError checkParameters(AbstractConnectedGraphObject[] graphObjects) {

        ParameterError error =  m_fileParameter.checkParameter();

        return error;
    }
    
    @Override
    public void userParametersChanged() {
        // need to recalculate model
        m_globalTableModelInterface = null;
    }
    
    private char getSeparator() {
        char separator;
        String separatorString = m_separatorParameter.getStringValue();
        if (separatorString.length() == 1) {
            separator = separatorString.charAt(0);
        } else if (separatorString.compareTo("\\t") == 0) {
            separator = '\t';
        } else {
            // we try with tab for the moment 
            separator = '\t';
        }
        
        return separator;
    }
    
    private char separatorToChar(String separatorString) {
        char separator;
        if (separatorString.compareTo("\\t") == 0) {
            separator = '\t';
        } else {
            separator = separatorString.charAt(0);
        }
        return separator;
    }
    
    public class LoadedDataModel extends DecoratedTableModel implements GlobalTableModelInterface {

        private String[] m_columnNames = { "No Data" };
        private Class[] m_columTypes = { String.class };
        private Object[][] m_data = null;
        
        public LoadedDataModel() {
            m_data = new Object[1][1];
            m_data[0][0] = "";
        }
        public LoadedDataModel(String[] columnNames, Class[] columTypes, Object[][] data) {
            setData(columnNames, columTypes, data);
        }
        
        public final void setData(String[] columnNames, Class[] columTypes, Object[][] data) {
            m_columnNames = columnNames;
            m_columTypes = columTypes;
            m_data = data;
            fireTableStructureChanged();
        }
        
        @Override
        public int getRowCount() {
            if (m_data == null) {
                return 0;
            }
            return m_data.length;
        }

        @Override
        public int getColumnCount() {
            if (m_columnNames == null) {
                return 0;
            }
           return m_columnNames.length;
        }
        
        @Override
        public Class getColumnClass(int column) {
            return m_columTypes[column];
        }

        @Override
        public String getColumnName(int column) {
            return m_columnNames[column];
        }
        
        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            return m_data[rowIndex][columnIndex];
        }

        @Override
        public String getToolTipForHeader(int col) {
            return null;
        }

        @Override
        public String getTootlTipValue(int row, int col) {
            return null;
        }

        @Override
        public TableCellRenderer getRenderer(int row, int col) {
            if (m_columTypes[col] == String.class) {
                return new DefaultLeftAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class));
            } else if  (m_columTypes[col] == Long.class) {
                return TableDefaultRendererManager.getDefaultRenderer(Long.class);
            } else if  (m_columTypes[col] == Double.class) {
                return new DoubleRenderer( new DefaultRightAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class)), 4, true, true);
            }  
            return null;
        }

        @Override
        public GlobalTableModelInterface getFrozzenModel() {
            return this;
        }

        @Override
        public Long getTaskId() {
            return -1l;
        }

        @Override
        public LazyData getLazyData(int row, int col) {
            return null;
        }

        @Override
        public void givePriorityTo(Long taskId, int row, int col) {
        }

        @Override
        public void sortingChanged(int col) {
        }

        @Override
        public int getSubTaskId(int col) {
            return -1;
        }

        @Override
        public String getDataColumnIdentifier(int columnIndex) {
            return m_columnNames[columnIndex];
        }

        @Override
        public Class getDataColumnClass(int columnIndex) {
            return m_columTypes[columnIndex];
        }

        @Override
        public Object getDataValueAt(int rowIndex, int columnIndex) {
            return getValueAt(rowIndex, columnIndex);
        }

        @Override
        public int[] getKeysColumn() {
            return null;
        }

        @Override
        public void setName(String name) {
            m_modelName = name;
        }

        @Override
        public String getName() {
            return m_modelName;
        }

        @Override
        public Map<String, Object> getExternalData() {
            return null;
        }

        @Override
        public PlotInformation getPlotInformation() {
            return null;
        }

        @Override
        public void addFilters(LinkedHashMap<Integer, Filter> filtersMap) {
            
            int nbCols = getColumnCount();
            for (int i = 0; i < nbCols; i++) {
                Class c = getColumnClass(i);
                if (c.equals(Double.class)) {
                    filtersMap.put(i, new DoubleFilter(getColumnName(i), null,i ));
                } else if (c.equals(Long.class)) {
                    filtersMap.put(i, new LongFilter(getColumnName(i), null, i));
                } else if (c.equals(String.class)) {
                    filtersMap.put(i, new StringDiffFilter(getColumnName(i), null, i));
                }
            }
        }

        @Override
        public boolean isLoaded() {
            return true;
        }

        @Override
        public int getLoadingPercentage() {
            return 100;
        }

        @Override
        public PlotType getBestPlotType() {
            return PlotType.SCATTER_PLOT;
        }

        @Override
        public int getBestXAxisColIndex(PlotType plotType) {
            return -1;
        }

        @Override
        public int getBestYAxisColIndex(PlotType plotType) {
            return -1;
        }

        @Override
        public String getExportRowCell(int row, int col) {
            return ExportModelUtilities.getExportRowCell(this, row, col);
        }

        @Override
        public ArrayList<ExportSubStringFont> getSubStringFonts(int row, int col) {
            return null;
        }
        
        @Override
        public String getExportColumnName(int col) {
            return m_columnNames[col];
        }

        @Override
        public int getInfoColumn() {
            return 0;
        }

        @Override
        public ArrayList<ExtraDataType> getExtraDataTypes() {
            return null;
        }

        @Override
        public Object getValue(Class c) {
            return getSingleValue(c);
        }

        @Override
        public Object getRowValue(Class c, int row) {
            return null;
        }
        
        @Override
        public Object getColValue(Class c, int col) {
            return null;
        }
        
    }
    
    public class PreviewTable extends DecoratedTable {

        public PreviewTable() {
            
        }
        
        @Override
        public TablePopupMenu initPopupMenu() {
            return null;
        }

        @Override
        public void prepostPopupMenu() {
            // nothing to do
        }
    
    }
}
