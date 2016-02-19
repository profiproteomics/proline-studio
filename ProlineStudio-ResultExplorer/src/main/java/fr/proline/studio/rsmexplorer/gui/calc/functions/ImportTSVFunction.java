package fr.proline.studio.rsmexplorer.gui.calc.functions;



import au.com.bytecode.opencsv.CSVReader;
import fr.proline.studio.comparedata.ExtraDataType;
import fr.proline.studio.filter.DoubleFilter;
import fr.proline.studio.filter.Filter;
import fr.proline.studio.filter.LongFilter;
import fr.proline.studio.filter.StringFilter;
import fr.proline.studio.graphics.PlotInformation;
import fr.proline.studio.graphics.PlotType;
import fr.proline.studio.parameter.AbstractLinkedParameters;
import fr.proline.studio.parameter.BooleanParameter;
import fr.proline.studio.parameter.FileParameter;
import fr.proline.studio.parameter.ParameterError;
import fr.proline.studio.parameter.ParameterList;
import fr.proline.studio.parameter.StringParameter;
import fr.proline.studio.python.interpreter.CalcError;
import fr.proline.studio.rsmexplorer.gui.calc.GraphPanel;
import fr.proline.studio.rsmexplorer.gui.calc.ProcessCallbackInterface;
import fr.proline.studio.rsmexplorer.gui.calc.graph.AbstractGraphObject;
import fr.proline.studio.rsmexplorer.gui.calc.graph.FunctionGraphNode;
import fr.proline.studio.table.renderer.DefaultLeftAlignRenderer;
import fr.proline.studio.table.renderer.DefaultRightAlignRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.DoubleRenderer;
import fr.proline.studio.table.DecoratedTableModel;
import fr.proline.studio.table.GlobalTableModelInterface;
import fr.proline.studio.table.LazyData;
import fr.proline.studio.table.TableDefaultRendererManager;
import fr.proline.studio.utils.IconManager;
import java.awt.Color;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JTextField;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author JM235353
 */
public class ImportTSVFunction extends AbstractFunction {

    private static final String KEY_FILE_PARAMETER = "FILE_KEY";
    private static final String SEPARATOR_AUTO_PARAMETER = "SEPARATOR_AUTO_KEY";
    private static final String SEPARATOR_PARAMETER = "SEPARATOR_KEY";
    
    private ParameterList m_parameterList;
    private FileParameter m_fileParameter;
    private BooleanParameter m_automaticSeparatorParameter;
    private StringParameter m_separatorParameter;
    
    private String m_modelName = null;
    
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
        return "Import CSV/TSV";
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
    public AbstractFunction cloneFunction(GraphPanel panel) {
        return new ImportTSVFunction(panel);
    }
    
    @Override
    public void process(AbstractGraphObject[] graphObjects, FunctionGraphNode functionGraphNode, ProcessCallbackInterface callback) {

        try {

            // check if we have already processed
            if (m_globalTableModelInterface != null) {
                return;
            }

            try {

                setCalculating(true);
                setInError(false, null);

        //JPM.TODO : should be done in a thread *****************************************************
                String filePath = ((String) m_fileParameter.getObjectValue()).trim();

                CSVReader reader;
                boolean automaticSeparator = (Boolean) m_automaticSeparatorParameter.getObjectValue();
                if (automaticSeparator) {
                    if (filePath.endsWith("csv")) {
                        reader = new CSVReader(new FileReader(filePath), ',');
                    } else {
                        reader = new CSVReader(new FileReader(filePath), '\t');
                    }
                } else {
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
                    reader = new CSVReader(new FileReader(filePath), separator);
                }

                // read column headers
                String[] headerLine = reader.readNext();
                if (headerLine == null) {
                    throw new IOException("No Data found in File");
                }

                int nbColumns = headerLine.length;

                // read lines, skip empty ones and check the number of columns
                ArrayList<String[]> allDataLines = new ArrayList<>();
                String[] dataLine;
                int line = 2;
                while ((dataLine = reader.readNext()) != null) {

                    int nbColumsCur = dataLine.length;
                    if (nbColumsCur == 0) {
                        // continue : empty line
                    }

                    if (nbColumns != nbColumsCur) {
                        throw new IOException("Number of columns differ in file at line " + line);
                    }

                    allDataLines.add(dataLine);

                    line++;

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
                m_globalTableModelInterface = new LoadedDataModel(headerLine, columTypes, data);

            } catch (Exception e) {
                setInError(new CalcError(e, null, -1));
            }
            setCalculating(false);

        } finally {
            callback.finished(functionGraphNode);
        }

    }
    
    @Override
    public void askDisplay(FunctionGraphNode functionGraphNode) {
        display(m_fileParameter.getStringValue(), getName());
    }
    
    @Override
    public void generateDefaultParameters(AbstractGraphObject[] graphObjects) {

        final String[] fileFilterNames = { "TSV File", "CSV File" };
        final String[] fileFilterExtensions = { "tsv", "csv" };
        
        m_fileParameter = new FileParameter(null, KEY_FILE_PARAMETER, "CSV/TSV File", JTextField.class, "", fileFilterNames, fileFilterExtensions);
        m_automaticSeparatorParameter = new BooleanParameter(SEPARATOR_AUTO_PARAMETER, "Separator according to file extension", JCheckBox.class, true);
        m_separatorParameter = new StringParameter(SEPARATOR_PARAMETER, "Separator", JTextField.class, "\\t",1,2);
        
        m_parameterList = new ParameterList("ImportTSV");
        m_parameters = new ParameterList[1];
        m_parameters[0] = m_parameterList;
        
        m_parameterList.add(m_fileParameter);
        m_parameterList.add(m_automaticSeparatorParameter);
        m_parameterList.add(m_separatorParameter);
        
        AbstractLinkedParameters linkedParameters = new AbstractLinkedParameters(m_parameterList) {
            @Override
            public void valueChanged(String value, Object associatedValue) {
                showParameter(m_separatorParameter, (value.compareTo("false") == 0));

                updataParameterListPanel();
            }
            
        };
        
        m_parameterList.getPanel(); // generate panel at once
        linkedParameters.showParameter(m_separatorParameter, false); // separator parameter not visible by default
        m_automaticSeparatorParameter.addLinkedParameters(linkedParameters); // link parameter, it will modify the panel
        
    }
    
    @Override
    public ParameterError checkParameters(AbstractGraphObject[] graphObjects) {

        ParameterError error =  m_fileParameter.checkParameter();

        return error;
    }
    
    @Override
    public void userParametersChanged() {
        // need to recalculate model
        m_globalTableModelInterface = null;
    }
    
    
    public class LoadedDataModel extends DecoratedTableModel implements GlobalTableModelInterface {

        private final String[] m_columnNames;
        private final Class[] m_columTypes;
        private final Object[][] m_data;
        
        public LoadedDataModel(String[] columnNames, Class[] columTypes, Object[][] data) {
            m_columnNames = columnNames;
            m_columTypes = columTypes;
            m_data = data;
        }
        
        
        @Override
        public int getRowCount() {
            return m_data.length;
        }

        @Override
        public int getColumnCount() {
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
                    filtersMap.put(i, new StringFilter(getColumnName(i), null, i));
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
}
