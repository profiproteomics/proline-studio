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
package fr.proline.studio.rsmexplorer.gui.calc;

import fr.proline.studio.graphics.PlotType;
import fr.proline.studio.rsmexplorer.gui.calc.functions.AbstractFunction;
import fr.proline.studio.rsmexplorer.gui.calc.functions.AdjustPFunction;
import fr.proline.studio.rsmexplorer.gui.calc.functions.ColumnFilterFunction;
import fr.proline.studio.rsmexplorer.gui.calc.functions.ComputeFDRFunction;
import fr.proline.studio.rsmexplorer.gui.calc.functions.DiffAnalysisFunction;
import fr.proline.studio.rsmexplorer.gui.calc.functions.DiffFunction;
import fr.proline.studio.rsmexplorer.gui.calc.functions.ExpressionFunction;
import fr.proline.studio.rsmexplorer.gui.calc.functions.FilterFunction;
import fr.proline.studio.rsmexplorer.gui.calc.functions.ImportTSVFunction;
import fr.proline.studio.rsmexplorer.gui.calc.functions.JoinFunction;
import fr.proline.studio.rsmexplorer.gui.calc.functions.LogFunction;
import fr.proline.studio.rsmexplorer.gui.calc.functions.MissingValuesImputationFunction;
import fr.proline.studio.rsmexplorer.gui.calc.functions.NormalizationFunction;
import fr.proline.studio.rsmexplorer.gui.calc.functions.PValueFunction;
import fr.proline.studio.rsmexplorer.gui.calc.functions.QuantiFilterFunction;
import fr.proline.studio.rsmexplorer.gui.calc.functions.SCDiffAnalysisFunction;
import fr.proline.studio.rsmexplorer.gui.calc.functions.TtdFunction;
import fr.proline.studio.rsmexplorer.gui.calc.graph.FunctionGraphNode;
import fr.proline.studio.rsmexplorer.gui.calc.graph.GraphGroup;
import fr.proline.studio.rsmexplorer.gui.calc.graph.GraphNode;
import fr.proline.studio.rsmexplorer.gui.calc.graph.GraphicGraphNode;
import fr.proline.studio.rsmexplorer.gui.calc.graphics.AbstractGraphic;
import fr.proline.studio.rsmexplorer.gui.calc.graphics.BoxPlotGraphic;
import fr.proline.studio.rsmexplorer.gui.calc.graphics.CalibrationPlotGraphic;
import fr.proline.studio.rsmexplorer.gui.calc.graphics.DensityPlotGraphic;
import fr.proline.studio.rsmexplorer.gui.calc.graphics.ParallelCoordinatesGraphic;
import fr.proline.studio.rsmexplorer.gui.calc.graphics.ScatterOrHistogramGraphic;
import fr.proline.studio.rsmexplorer.gui.calc.graphics.VarianceDistPlotGraphic;
import fr.proline.studio.rsmexplorer.gui.calc.graphics.VennDiagramGraphic;
import java.awt.Point;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.InputSource;

/**
 *
 * Parse an XML string to construct a Graph in the Data Analyzer
 * 
 * @author JM235353
 */
public class UserMacroParser {
    
    private static UserMacroParser m_singleton = null;
    
    public GraphPanel m_graphPanel = null;
    
    public static UserMacroParser getGraphFileManager() {
        if (m_singleton == null) {
            m_singleton = new UserMacroParser();
        }

        
        return m_singleton;
    }
    
    private UserMacroParser() {
        
    }
    
    public void setGraphPanel(GraphPanel graphPanel) {
        m_graphPanel = graphPanel;
    }
    
    public FunctionGraphNode createFunctionGraphNode(int subtype) {
        
        AbstractFunction function = null;
        AbstractFunction.FUNCTION_TYPE functionType = AbstractFunction.FUNCTION_TYPE.values()[subtype];
        switch (functionType) {
            case AdjustPFunction: {
                function = new AdjustPFunction(m_graphPanel);
                break;
            }
            case ColumnFilterFunction: {
                function = new ColumnFilterFunction(m_graphPanel);
                break;
            }
            case ComputeFDRFunction: {
                function = new ComputeFDRFunction(m_graphPanel);
                break;
            }
            case DiffAnalysisFunction: {
                function = new DiffAnalysisFunction(m_graphPanel);
                break;
            }
            case DiffFunction: {
                function = new DiffFunction(m_graphPanel);
                break;
            }
            case ExpressionFunction: {
                function = new ExpressionFunction(m_graphPanel);
                break;
            }
            case FilterFunction: {
                function = new FilterFunction(m_graphPanel);
                break;
            }
            case ImportTSVFunction: {
                function = new ImportTSVFunction(m_graphPanel);
                break;
            }
            case JoinFunction: {
                function = new JoinFunction(m_graphPanel);
                break;
            }
            case Log2Function: {
                function = new LogFunction(m_graphPanel, false);
                break;
            }
            case Log10Function: {
                function = new LogFunction(m_graphPanel, true);
                break;
            }
            case MissingValuesImputationFunction: {
                function = new MissingValuesImputationFunction(m_graphPanel);
                break;
            }
            case NormalizationFunction: {
                function = new NormalizationFunction(m_graphPanel);
                break;
            }
            case PValueFunction: {
                function = new PValueFunction(m_graphPanel);
                break;
            }
            case QuantiFilterFunction: {
                function = new QuantiFilterFunction(m_graphPanel);
                break;
            }
            case SCDiffAnalysisFunction: {
                function = new SCDiffAnalysisFunction(m_graphPanel);
                break;
            }
            case TtdFunction: {
                function = new TtdFunction(m_graphPanel);
                break;
            }
        }
        return new FunctionGraphNode(function, m_graphPanel);
    }
    
    public GraphicGraphNode createGraphicGraphNode(int subtype) {
    
        AbstractGraphic graphic = null;
        AbstractGraphic.GRAPHIC_TYPE functionType = AbstractGraphic.GRAPHIC_TYPE.values()[subtype];

        switch (functionType) {
            case CalibrationPlotGraphic: {
                graphic = new CalibrationPlotGraphic(m_graphPanel);
                break;
            }
            case ParallelCoordinatesGraphic: {
                graphic = new ParallelCoordinatesGraphic(m_graphPanel);
                break;
            }
            case ScatterGraphic: {
                graphic = new ScatterOrHistogramGraphic(m_graphPanel, PlotType.SCATTER_PLOT);
                break;
            }
            case HistogramGraphic: {
                graphic = new ScatterOrHistogramGraphic(m_graphPanel, PlotType.HISTOGRAM_PLOT);
                break;
            }
            case VennDiagramGraphic: {
                graphic = new VennDiagramGraphic(m_graphPanel);
                break;
            }
            case BoxPlotGraphic: {
                graphic = new BoxPlotGraphic(m_graphPanel);
                break;
            }
            case DensityPlotGraphic: {
                graphic = new DensityPlotGraphic(m_graphPanel);
                break;
            }
            case VarianceDistPlotGraphic: {
                graphic = new VarianceDistPlotGraphic(m_graphPanel);
                break;
            }

        }
        return new GraphicGraphNode(m_graphPanel, graphic);
    }
    
    public void parseFile(String xml, GraphPanel graphPanel) throws Exception {
        Point position = graphPanel.getNextGraphNodePosition(null);
        parseFile(xml, graphPanel, position.x, position.y);
        
    }
    public void parseFile(String xml, GraphPanel graphPanel, int x, int y) throws Exception {
        
        if (x < GraphNode.WIDTH * 1.4) {
            x = (int) (GraphNode.WIDTH * 1.4);
        }
        if (y < GraphNode.HEIGHT_MIN * 1.4) {
            y = (int) (GraphNode.HEIGHT_MIN * 1.4);
        }
        
        m_graphPanel = graphPanel;
        
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser parser = factory.newSAXParser();
        GraphXMLHandler handler = new GraphXMLHandler(m_graphPanel, x, y);

        parser.parse(new InputSource(new ByteArrayInputStream(xml.getBytes("utf-8"))), handler);
        handler.doConnections();
        
        m_graphPanel.repaint();
        
        m_graphPanel = null;
    }
    
    public class GraphXMLHandler extends DefaultHandler {
        
        private GraphPanel m_graphPanel;
        
        private HashMap<Integer, GraphNode> m_graphNodeMap = new HashMap<>();
        private ArrayList<int[]> m_connections = new ArrayList<>();
        
        private Point m_firstPosition = null;
        private Point m_deltaPosition = null;
        
        private GraphGroup m_group = null;
        
        public GraphXMLHandler(GraphPanel p, int x, int y) {
            m_graphPanel = p;

            m_firstPosition = new Point(x, y);

        }

        
        @Override
        public void startElement(String namespaceURI, String lname, String qname, Attributes attrs) throws SAXException {

                //
            
            int nbAttributes = attrs.getLength();
            
            if (qname.compareTo("dataanalyzer") == 0) {
                for (int i = 0; i < nbAttributes; i++) {
                    String attributeName = attrs.getQName(i);
                    String attributevalue = attrs.getValue(i);
                    if (attributeName.compareTo("name") == 0) {
                        m_group = new GraphGroup(attributevalue);
                    }
                }
            } else if (qname.compareTo("graphnode") == 0) {
                int id = -1;
                int x = 0;
                int y = 0;
                String type = null;
                int subtype = -1;
                for (int i = 0; i < nbAttributes; i++) {
                    String attributeName = attrs.getQName(i);
                    String attributevalue = attrs.getValue(i);
                    
                    if (attributeName.compareTo("id") == 0) {
                        id = Integer.parseInt(attributevalue);
                    } else if (attributeName.compareTo("x") == 0) {
                        x = Integer.parseInt(attributevalue);
                    } else if (attributeName.compareTo("y") == 0) {
                        y = Integer.parseInt(attributevalue);
                    } else if (attributeName.compareTo("type") == 0) {
                        type = attributevalue;
                    } else if (attributeName.compareTo("subtype") == 0) {
                        subtype = Integer.parseInt(attributevalue);
                    }
                }
                
                if (m_firstPosition != null) {
                    m_deltaPosition = new Point(x-m_firstPosition.x, y-m_firstPosition.y);
                    x = m_firstPosition.x;
                    y = m_firstPosition.y;
                    m_firstPosition = null;
                } else {
                    x-= m_deltaPosition.x;
                    y -= m_deltaPosition.y;
                }
                
                if (type.compareTo("Function") == 0) {
                    FunctionGraphNode graphNode = createFunctionGraphNode(subtype);
                    m_graphPanel.addGraphNode(graphNode, x, y);
                    if (m_group != null) {
                        m_group.addObject(graphNode);
                    }
                    
                    m_graphNodeMap.put(id, graphNode);
                } else if (type.compareTo("Graphic") == 0) {
                    GraphicGraphNode graphNode = createGraphicGraphNode(subtype);
                    m_graphPanel.addGraphNode(graphNode, x, y);
                    if (m_group != null) {
                        m_group.addObject(graphNode);
                    }

                    m_graphNodeMap.put(id, graphNode);
                }

            } else if (qname.compareTo("connector") == 0) {
                int id1 = -1;
                int id2 = -1;
                int index = -1;
                
                for (int i = 0; i < nbAttributes; i++) {
                    String attributeName = attrs.getQName(i);
                    String attributevalue = attrs.getValue(i);

                    if (attributeName.compareTo("id1") == 0) {
                        id1 = Integer.parseInt(attributevalue);
                    } else if (attributeName.compareTo("id2") == 0) {
                        id2 = Integer.parseInt(attributevalue);
                    } else if (attributeName.compareTo("index") == 0) {
                        index = Integer.parseInt(attributevalue);
                    }
                }
                
                if ((id1 != -1) && (id2 != -1) && (index != -1)) {
                    int[] connection = new int[3];
                    connection[0] = id1;
                    connection[1] = id2;
                    connection[2] = index;
                    m_connections.add(connection);
                }
                

            }

        }
        
        public void doConnections() {
            for (int[] connection : m_connections) {
                int id1 = connection[0];
                int id2 = connection[1];
                int outConnectorIndex = connection[2]; // index of out connector
                
                GraphNode nodeOut = m_graphNodeMap.get(id1);
                GraphNode nodeIn = m_graphNodeMap.get(id2);
                
                if ((nodeOut != null) && (nodeIn != null)) {
                    nodeOut.connectTo(nodeIn, outConnectorIndex);
                    if ((nodeIn.getFirstFreeConnector() == null) && (nodeIn.canAddConnector())) {
                        // all connectors are connected, but we can add a new connector
                        nodeIn.addFreeConnector();
                    }
                }
            }
        }
        
        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
        }
        
        @Override
        public void characters(char[] data, int start, int end) {
        }
    }
}
