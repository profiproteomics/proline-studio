/* This class Draws the Detail Panel when the component is selected. 
 * */
package fr.proline.studio.rsmexplorer.adjacencymatrix.visualize;

import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.core.orm.msi.dto.DProteinMatch;
import fr.proline.studio.dam.tasks.data.LightPeptideMatch;
import fr.proline.studio.dam.tasks.data.LightProteinMatch;
import fr.proline.studio.export.ExportButton;
import fr.proline.studio.gui.HourglassPanel;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.parameter.BooleanParameter;
import fr.proline.studio.parameter.IntegerParameter;
import fr.proline.studio.parameter.ParameterList;
import fr.proline.studio.parameter.SettingsButton;
import fr.proline.studio.parameter.SettingsInterface;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxPanelInterface;
import fr.proline.studio.utils.CyclicColorPalette;
import java.awt.BasicStroke;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;

public class MatrixPanel extends HourglassPanel implements DataBoxPanelInterface, SettingsInterface {

    private AbstractDataBox m_dataBox;

    private static final int MIN_CELL_SIZE = 14;
    private static final int MAX_CELL_SIZE = 30;
    private static final int DELTA = 3;
    private static final int TITLE_SIZE = 20;
    private static final int INFO_SIZE = 50;
    private static final int PEPTIDE_NAME_NB_CHARS_MAX = 45;
    private static final int PROTEIN_NAME_NB_CHARS_MAX = 30;
    private static final int VISIBILITY_MARGE_SIZE = 20;

    private static final String PROTEINS = "Proteins";
    private static final String PEPTIDES = "Peptides";

    private int m_peptideNameNbCharsMax = PEPTIDE_NAME_NB_CHARS_MAX;
    private int m_proteinNameNbCharsMax = PROTEIN_NAME_NB_CHARS_MAX;
    private int m_peptideNamePixelSize;
    private int m_proteinNamePixelSize;
    
    
    private List<Cell> m_cells;
    private Cell m_overedCell = null;
    private Cell m_selectedCell = null;
    private List<PeptideCell> m_peptideCells;
    private List<ProteinCell> m_proteinCells;
    private static final ArrayList<Color> SCORE_COLOR_LIST = new ArrayList<>(6);

    private static final StringBuilder m_sb = new StringBuilder();
    
    private static final Color COLOR_GRAY = new Color(95, 87, 88);
    private static final Color COLOR_PEPTIDE_RANK1 = new Color(134, 180, 96);
    private static final Color SELECTION_COLOR = new Color(51, 153, 255);
    private static final Font FONT_HELVETICA_12_BOLD = new Font("Helvetica", Font.BOLD, 12);
    private static final Font FONT_HELVETICA_11 = new Font("Helvetica", Font.PLAIN, 11);
    private static final BasicStroke SELECTED_STROKE = new BasicStroke(2);
    private boolean m_firstPaint = true;

    private Integer[] m_peptideRank;
    private Float[] m_peptideScore;

    private int m_squareSize;
    private int m_xOffset;
    private int m_xOffsetMatrix;
    private int m_yOffsetMatrix;

    private Component m_component;
    HashMap<Long, DProteinMatch> m_proteinMap;
    HashMap<Long, DPeptideMatch> m_peptideMap;
    Long m_rsmId;
    private int[] m_flagArray;
    private DrawVisualization m_drawVisualization = null;
    private HashMap<LightProteinMatch, ArrayList<LightPeptideMatch>> m_proteinToPeptideMap = new HashMap<>();
    HashMap<LightProteinMatch, LightProteinMatch>  m_equivalentToMainProteinMap = new HashMap<>();

    private final HashMap<Long, Color> m_proteinSetColorMap = new HashMap<>();
    
    private final String[] m_info = new String[2];

    private final JPanel m_internalPanel;
    
    private ArrayList<ParameterList> m_parameterListArray = null;
    public static final String MATRIX_PARAMETERS = "Matrix Parameters";
    public static final String HIDE_EQUIVALENT_PROTEINS_KEY = "HideEquivalentProteins";
    public static final String PROTEIN_NAME_HEIGHT_KEY = "ProteinNameHeight";
    public static final String PEPTIDE_NAME_WIDTH_KEY = "ProteinNameWidth";
    private BooleanParameter m_hideEquivalentPoteinsParameter = null;
    private IntegerParameter m_proteinNameMaxCharsParameter = null;
    private IntegerParameter m_peptideNameMaxCharsParameter = null;
    
    private boolean m_showEquivalentProteins = true;
    
    public MatrixPanel() {
        setLoading(0);
        setBorder(BorderFactory.createLineBorder(COLOR_GRAY));
        setLayout(new BorderLayout());

        if (SCORE_COLOR_LIST.isEmpty()) {
            SCORE_COLOR_LIST.add(new Color(252, 187, 161));
            SCORE_COLOR_LIST.add(new Color(252, 146, 114));
            SCORE_COLOR_LIST.add(new Color(251, 106, 74));
            SCORE_COLOR_LIST.add(new Color(239, 59, 44));
            SCORE_COLOR_LIST.add(new Color(203, 24, 29));
            SCORE_COLOR_LIST.add(new Color(153, 0, 13));
        }

        JScrollPane scrollPane = new JScrollPane();
        m_internalPanel = new InternalPanel();
        scrollPane.setViewportView(m_internalPanel);
        
        JToolBar toolbar = initToolbar();

        add(toolbar, BorderLayout.WEST);
        add(scrollPane, BorderLayout.CENTER);

    }

    private JToolBar initToolbar() {
        JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
        toolbar.setFloatable(false);
        
        SettingsButton settingsButton = new SettingsButton( null, this);
        toolbar.add(settingsButton);
        
        ExportButton exportImageButton = new ExportButton("Protein/Peptide Matrix", m_internalPanel);
        toolbar.add(exportImageButton);
        
        return toolbar;
    }
    
    public void setData(Component c, DrawVisualization drawVisualization, HashMap<Long, DProteinMatch> proteinMap, HashMap<Long, DPeptideMatch> peptideMap, Long rsmId) {

        setLoaded(0);

        m_proteinSetColorMap.clear();
        
        if (m_cells != null) {
            m_cells.clear();
            m_overedCell = null;
            m_selectedCell = null;
        }

        m_drawVisualization = drawVisualization;
        m_rsmId = rsmId;

        m_proteinToPeptideMap = m_drawVisualization.getProteinToPeptideMap();
        m_equivalentToMainProteinMap = m_drawVisualization.getEquivalentToMainProtein();
        m_component = c;

        m_component.searchWeakPeptides(m_drawVisualization, 100); //JPM.TODO 100 is a threshold

        m_proteinMap = proteinMap;
        m_peptideMap = peptideMap;

        int rowCount = (m_component==null) ? 0 : m_component.getPeptideSize();
        int columnCount = (m_component==null) ? 0 : m_component.getProteinSize(m_showEquivalentProteins);

        m_cells = new ArrayList<>(columnCount * rowCount);
        m_peptideCells = new ArrayList<>(rowCount);	//pepide score
        m_proteinCells = new ArrayList<>(columnCount);	// protine Score

        for (int row = 0; row < rowCount; row++) {
            for (int col = 0; col < columnCount; col++) {
                Cell cell = new Cell(row, col);
                cell.setPeptideMatch(m_peptideMap.get(m_component.getPeptideArray().get(row).getId()));
                LightProteinMatch lpm = m_component.getProteinArray(m_showEquivalentProteins).get(col);
                cell.setProteinMatch(m_proteinMap.get(lpm.getId()));
                if (m_component.isWeakPeptide(row, lpm.getProteinSetId())) {
                    cell.setShowWeakNess();
                }
                m_cells.add(cell);
            }
        }

        for (int row = 0; row < rowCount; row++) {
            PeptideCell cell = new PeptideCell(row);
            cell.setPeptideMatch(m_peptideMap.get(m_component.getPeptideArray().get(row).getId()));
            m_peptideCells.add(cell);
        }

        
        for (int col = 0; col < columnCount; col++) {
            ProteinCell cell = new ProteinCell(col);
            cell.setProteinMatch(m_proteinMap.get(m_component.getProteinArray(m_showEquivalentProteins).get(col).getId()));
            m_proteinCells.add(cell);

        }
        
        HashMap<Long, Integer> m_proteinSetPeptidesNumber = new HashMap();
        for (int col = 0; col < columnCount; col++) {
            LightProteinMatch lpm = m_component.getProteinArray(m_showEquivalentProteins).get(col);
            long proteinSetId = lpm.getProteinSetId();
            DProteinMatch pm = m_proteinMap.get(lpm.getId());
            int nbPeptides = pm.getPeptideSet(m_rsmId).getPeptideCount();
            Integer curNbPeptides = m_proteinSetPeptidesNumber.get(proteinSetId);
            if ((curNbPeptides == null) || (curNbPeptides < nbPeptides)) {
                m_proteinSetPeptidesNumber.put(proteinSetId, nbPeptides);
            }
        }

        for (int col = 0; col < columnCount; col++) {
            ProteinCell cell = m_proteinCells.get(col);
            LightProteinMatch lpm = m_component.getProteinArray(m_showEquivalentProteins).get(col);
            long proteinSetId = lpm.getProteinSetId();
            DProteinMatch pm = m_proteinMap.get(lpm.getId());
            int nbPeptides = pm.getPeptideSet(m_rsmId).getPeptideCount();
            boolean sameset = nbPeptides>=m_proteinSetPeptidesNumber.get(proteinSetId);
            cell.setProteinMatch(pm);
            cell.setSameSet(sameset);

        }
        
        m_peptideRank = new Integer[rowCount];

        m_flagArray = new int[columnCount * rowCount];

        m_peptideScore = new Float[rowCount];
        int index = 0;
        if (m_component != null) {
            for (LightPeptideMatch peptideMatch : m_component.getPeptideArray()) {

                m_peptideScore[index] = peptideMatch.getScore();
                m_peptideRank[index] = peptideMatch.getCDPrettyRank();
                index++;
            }
        }


        m_firstPaint = true;

        repaint();

    }

    public DProteinMatch getSelectedProteinMatch() {
        if (m_selectedCell == null) {
            return null;
        }
        return m_selectedCell.getProteinMatch();
    }
    
    public DPeptideMatch getSelectedPeptideMatch() {
        if (m_selectedCell == null) {
            return null;
        }
        return m_selectedCell.getPeptideMatch();
    }

    @Override
    public ArrayList<ParameterList> getParameters() {
        if (m_parameterListArray == null) {
            initParameters();
        }

        return m_parameterListArray;
    }

    @Override
    public void parametersChanged() {
        
        if (m_parameterListArray == null) {
            initParameters();
        }

        Boolean hideEquivalentProteins = (Boolean) m_hideEquivalentPoteinsParameter.getObjectValue();
        m_showEquivalentProteins = !hideEquivalentProteins;
        
        m_peptideNameNbCharsMax = ((Integer) m_peptideNameMaxCharsParameter.getObjectValue()).intValue();
        m_proteinNameNbCharsMax = ((Integer) m_proteinNameMaxCharsParameter.getObjectValue()).intValue();
        
        setData(m_component, m_drawVisualization, m_proteinMap, m_peptideMap, m_rsmId);

    }
    
    private void initParameters() {


        ParameterList parameterTableList = new ParameterList(MATRIX_PARAMETERS);

        m_hideEquivalentPoteinsParameter = new BooleanParameter(HIDE_EQUIVALENT_PROTEINS_KEY,"Hide Proteins with same peptides",JCheckBox.class, Boolean.FALSE);
        m_proteinNameMaxCharsParameter = new IntegerParameter(PROTEIN_NAME_HEIGHT_KEY, "Maximum Protein Name Length", JTextField.class, PROTEIN_NAME_NB_CHARS_MAX, 10, PROTEIN_NAME_NB_CHARS_MAX*5);
        m_peptideNameMaxCharsParameter = new IntegerParameter(PEPTIDE_NAME_WIDTH_KEY, "Maximum Peptide Name Length", JTextField.class, PEPTIDE_NAME_NB_CHARS_MAX, 10, PEPTIDE_NAME_NB_CHARS_MAX*5);

        
        parameterTableList.add(m_hideEquivalentPoteinsParameter);
        parameterTableList.add(m_proteinNameMaxCharsParameter);
        parameterTableList.add(m_peptideNameMaxCharsParameter);
        

        parameterTableList.getPanel(); // generate panel at once

        m_parameterListArray = new ArrayList<>();
        m_parameterListArray.add(parameterTableList);
    }
    
    public class InternalPanel extends JPanel {

        public InternalPanel() {
            MouseAdapter mouseHandler = new MouseAdapter() {
                
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (m_component == null) {
                        return;
                    }

                    boolean selectionChanged = false;

                    // reinit overred cell if needed
                    if (m_selectedCell != null) {
                        m_selectedCell.setSelected(false);
                        m_selectedCell = null;
                        selectionChanged = true;
                    }

                    // look for newly selected cell
                    int mouseX = e.getX();
                    int mouseY = e.getY();
                    m_selectedCell = findCell(mouseX, mouseY);
                    if (m_selectedCell != null) {
                        m_selectedCell.setSelected(true);
                        selectionChanged = true;
                    }

                    if (selectionChanged) {
                        m_dataBox.propagateDataChanged(DProteinMatch.class);
                        m_dataBox.propagateDataChanged(DPeptideMatch.class);
                        repaint();
                    }

                }
                
                @Override
                public void mouseDragged(MouseEvent e) {
                    mouseMoved(e);
                }
                
                @Override
                public void mouseMoved(MouseEvent e) {

                    if (m_component == null) {
                        return;
                    }
                    
                    boolean needRepaint = false;

                    // reinit overred cell if needed
                    if (m_overedCell != null) {
                        m_overedCell.setFlewOver(false);
                        m_overedCell = null;
                        needRepaint = true;
                    }

                    // look for newly selected cell
                    int mouseX = e.getX();
                    int mouseY = e.getY();
                    m_overedCell = findCell(mouseX, mouseY); 
                    if (m_overedCell != null) {
                        m_overedCell.setFlewOver(true);
                        needRepaint = true;
                    }

                    if (needRepaint) {
                        repaint();
                    }

                }
            };
            addMouseListener(mouseHandler);
            addMouseMotionListener(mouseHandler);
        }
        
        
        private Cell findCell(int mouseX, int mouseY) {
            int columnCount = m_component.getProteinSize(m_showEquivalentProteins);

            int matrixX1 = m_cells.get(0).getPixelX();
            int matrixX2 = m_cells.get(m_cells.size() - 1).getPixelX() + m_squareSize;
            int matrixY1 = m_cells.get(0).getPixelY();
            int matrixY2 = m_cells.get(m_cells.size() - 1).getPixelY() + m_squareSize;
            int proteinY1 = m_proteinCells.get(0).getPixelY();
            int proteinY2 = proteinY1 + m_squareSize;
            int peptideX1 = m_peptideCells.get(0).getPixelX();
            int peptideX2 = peptideX1 + m_squareSize;

            // check matrix mouse over
            if ((mouseX >= matrixX1)
                    && (mouseX < matrixX2)
                    && (mouseY >= matrixY1)
                    && (mouseY < matrixY2)) {
                // we are over the matrix
                int column = (mouseX - matrixX1) / m_squareSize;
                int row = (mouseY - matrixY1) / m_squareSize;
                int indexCell = row * columnCount + column;
                if (indexCell < m_cells.size()) {
                    return m_cells.get(row * columnCount + column);
                }
            } else if ((mouseX >= matrixX1)
                    && (mouseX < matrixX2)
                    && (mouseY >= proteinY1)
                    && (mouseY < proteinY2)) {
                int column = (mouseX - matrixX1) / m_squareSize;
                return m_proteinCells.get(column);
            } else if ((mouseY >= matrixY1)
                    && (mouseY < matrixY2)
                    && (mouseX >= peptideX1)
                    && (mouseX < peptideX2)) {
                int row = (mouseY - matrixY1) / m_squareSize;
                return m_peptideCells.get(row);
            }
            
            return null;
        }
        
        @Override
        public void paint(Graphics g) {

            int width = getWidth();
            int height = getHeight();      

            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2d.setColor(Color.white);
            g2d.fillRect(0, 0, width, height);

            
            if (m_component == null) {
                return;
            } else if (m_component!=null && isLoading()) {
                                
                if (m_displayFont == null) {
                    m_displayFont = new Font("SansSerif", Font.BOLD, 12);
                    m_metrics = g.getFontMetrics(m_displayFont);
                }
                g.setFont(m_displayFont);
                

                final int PAD = 10;
                final int INTERNAL_PAD = 5;
                final int BOX_HEIGHT = INTERNAL_PAD * 2 + 16;

                String text = "No Cluster Selected";
                int stringWidth = m_metrics.stringWidth(text);
                int fontAscent = m_metrics.getAscent();
                
                int visibleHeight = getVisibleRect().height;

                g.setColor(Color.white);
                g.fillRect(PAD, visibleHeight - BOX_HEIGHT - PAD, stringWidth + INTERNAL_PAD * 4, BOX_HEIGHT);
                g.setColor(Color.gray);
                g.drawRect(PAD + 2, visibleHeight - BOX_HEIGHT - PAD + 2, stringWidth + INTERNAL_PAD * 4 - 4, BOX_HEIGHT - 4);

                g.setColor(Color.black);
                g.drawString(text, PAD+INTERNAL_PAD*2, visibleHeight-BOX_HEIGHT-PAD+INTERNAL_PAD+fontAscent);
                
                return;
            }

            if (m_firstPaint) {
                computeNecessarySize(g);
            }
            
            
            int rowCount = m_component.getPeptideSize();
            int columnCount = m_component.getProteinSize(m_showEquivalentProteins);

            // calculate square size :
            int squareSizeX = (int) (((width - TITLE_SIZE - m_peptideNamePixelSize - DELTA) / (columnCount + 1)));
            int squareSizeY = (int) (((height - INFO_SIZE - TITLE_SIZE - m_proteinNamePixelSize - DELTA) / (rowCount + 1)));
            m_squareSize = Math.min(squareSizeX, squareSizeY);
            if (m_squareSize > MAX_CELL_SIZE) {
                m_squareSize = MAX_CELL_SIZE;
            } else if (m_squareSize < MIN_CELL_SIZE) {
                m_squareSize = MIN_CELL_SIZE;
            }

            int matrixWidth = (columnCount + 1) * m_squareSize + DELTA;
            int matrixTotalWidth = matrixWidth + TITLE_SIZE + m_peptideNamePixelSize;
            int matrixHeight = (rowCount + 1) * m_squareSize + DELTA;
            int matrixTotalHeight = matrixHeight + INFO_SIZE + TITLE_SIZE + m_proteinNamePixelSize;

            m_xOffset = (width - matrixTotalWidth) / 2;
            if (m_xOffset < 0) {
                m_xOffset = 0;
            }
            m_xOffsetMatrix = m_xOffset + TITLE_SIZE + m_peptideNamePixelSize + DELTA;
            m_yOffsetMatrix = INFO_SIZE + TITLE_SIZE + m_proteinNamePixelSize + DELTA;

            g2d.setColor(COLOR_GRAY);
            g2d.setFont(FONT_HELVETICA_12_BOLD);
            FontMetrics fontMetricsBold = g.getFontMetrics(g.getFont());

            int proteinsWidth = fontMetricsBold.stringWidth(PROTEINS);
            int peptidesWidth = fontMetricsBold.stringWidth(PEPTIDES);

            g2d.drawString(PROTEINS, m_xOffset + (matrixTotalWidth - proteinsWidth + m_peptideNamePixelSize)/2, INFO_SIZE + TITLE_SIZE / 2);

            drawRotatedLine(g2d, (double) (m_xOffset + TITLE_SIZE/2), (matrixTotalHeight + peptidesWidth )/2+m_proteinNamePixelSize, (double) 270 * java.lang.Math.PI / 180, PEPTIDES);

            g2d.setFont(FONT_HELVETICA_11);
            FontMetrics fontMetricsPlain = g.getFontMetrics(FONT_HELVETICA_11);


            ArrayList<LightProteinMatch> proteinList = m_component.getProteinArray(m_showEquivalentProteins);
            int proteinIndex = -1;
            for (LightProteinMatch proteinMatch : proteinList) {
               proteinIndex++;
               ArrayList<LightPeptideMatch> peptideList = m_proteinToPeptideMap.get(proteinMatch);
               if (peptideList == null) {
                   peptideList = m_proteinToPeptideMap.get(m_equivalentToMainProteinMap.get(proteinMatch));
               }
               for (LightPeptideMatch peptideMatch : peptideList) {
                   int peptIndex = m_component.getPeptideArray().indexOf(peptideMatch);
                   int z = peptIndex * columnCount + proteinIndex;
                   m_flagArray[z] = 1;
               }
            }
 
            // --- Unselected Cells
            
            // draw matrix
            for (Cell cell : m_cells) {
                if (!cell.isSelectedOrOver()) {
                    cell.draw(g2d);
                }
            }
            for (Cell cell : m_cells) {
                if (cell.isSelectedOrOver()) {
                    cell.draw(g2d);
                }
            }

            // draw peptide column
            for (PeptideCell cell : m_peptideCells) {
                if (!cell.isSelectedOrOver()) {
                    cell.draw(g2d);
                }
            }

            // draw protein column
            for (ProteinCell cell : m_proteinCells) {
                if (!cell.isSelectedOrOver()) {
                    cell.draw(g2d);
                }
            }

            // --- Selected of over flew cells
            
            Stroke s = g2d.getStroke();
            g2d.setStroke(SELECTED_STROKE);
                    
            for (PeptideCell cell : m_peptideCells) {
                if (cell.isSelectedOrOver()) {
                    cell.draw(g2d);
                    g2d.setColor(SELECTION_COLOR);
                    Cell cell1 = m_cells.get(cell.m_row * columnCount);
                    Cell cell2 = m_cells.get(cell.m_row * columnCount + columnCount-1);
                    int x = cell1.getPixelX();
                    int y = cell1.getPixelY();
                    int x2 = cell2.getPixelX()+m_squareSize;
                    int y2 = cell2.getPixelY()+m_squareSize;
                    g.drawRect(x, y, x2-x, y2-y);
                }
            }
            

            for (ProteinCell cell : m_proteinCells) {
                if (cell.isSelectedOrOver()) {
                    cell.draw(g2d);
                    g2d.setColor(SELECTION_COLOR);
                    Cell cell1 = m_cells.get(cell.m_col);
                    Cell cell2 = m_cells.get((rowCount-1)*columnCount+cell.m_col);
                    int x = cell1.getPixelX();
                    int y = cell1.getPixelY();
                    int x2 = cell2.getPixelX()+m_squareSize;
                    int y2 = cell2.getPixelY()+m_squareSize;
                    g.drawRect(x, y, x2-x, y2-y);
                }
            }
            g2d.setStroke(s);

            /*Drawing Labels*/
            int ascent = fontMetricsPlain.getAscent();
            int descent = fontMetricsPlain.getDescent();
            

            
            long id1 = (m_selectedCell==null) ? -1 : m_selectedCell.getPeptideMatchId();
            long id2 = (m_overedCell==null) ? -1 : m_overedCell.getPeptideMatchId();
            int dIndex = 0;
            for (LightPeptideMatch peptideMatch : m_component.getPeptideArray()) {
                
                long id = peptideMatch.getId();
                boolean highlight = ((id == id1) || (id == id2));
                if (highlight) {
                    g2d.setColor(SELECTION_COLOR);
                } else {
                    g2d.setColor(COLOR_GRAY);
                }
                
                String sequence = peptideMatch.getSequence();
                int stringWidth = 0;
                int i=0;
                for (;i<sequence.length();i++) {
                    stringWidth +=  fontMetricsPlain.charWidth(sequence.charAt(i));
                    if (stringWidth>m_peptideNamePixelSize) {
                        i--;
                        break;
                    }
                }
                if ((i>0) && (i<sequence.length()-1)) {
                    sequence = sequence.substring(0, i-1)+".";
                }


                Cell cell = m_cells.get(dIndex * columnCount);
  
                int top = cell.getPixelY();
                int bottom = top+m_squareSize;
                int baseline=(top+((bottom+1-top)/2) - ((ascent + descent)/2) + ascent);
                
                g2d.drawString(sequence, m_xOffset + TITLE_SIZE, baseline);

                dIndex++;
            }

            id1 = (m_selectedCell==null) ? -1 : m_selectedCell.getProteinMatchId();
            id2 = (m_overedCell==null) ? -1 : m_overedCell.getProteinMatchId();
            dIndex = 0;
            for (LightProteinMatch proteinMatch : m_component.getProteinArray(m_showEquivalentProteins)) {
                String proteinName = proteinMatch.getAccession();
                long id = proteinMatch.getId();
                boolean highlight = ((id == id1) || (id == id2));
                if (highlight) {
                    g2d.setColor(SELECTION_COLOR);
                } else {
                    g2d.setColor(COLOR_GRAY);
                }
                int stringWidth = 0;
                int i=0;
                for (;i<proteinName.length();i++) {
                    stringWidth +=  fontMetricsPlain.charWidth(proteinName.charAt(i));
                    if (stringWidth>m_proteinNamePixelSize) {
                        i--;
                        break;
                    }
                }
                if ((i>0) && (i<proteinName.length()-1)) {
                    proteinName = proteinName.substring(0, i-1)+".";
                }

                Cell cell = m_cells.get(dIndex);
                int x = cell.getPixelX();
                int y = cell.getPixelY();
                
                int top = x;
                int bottom = x+m_squareSize;
                int baseline=(top+((bottom+1-top)/2) - ((ascent + descent)/2) + ascent);
                drawRotatedLine(g2d, baseline, (double) (y - 5 - m_squareSize), (double) 270 * java.lang.Math.PI / 180, proteinName);

                dIndex++;

            }

            Cell displayCell = (m_overedCell!=null) ? m_overedCell : m_selectedCell;
            if (displayCell != null) {
                displayCell.getInfo(m_info);
                
                g2d.setFont(new Font("Helvetica", Font.BOLD, 12));
                g2d.setColor(COLOR_GRAY);
                g2d.drawString(m_info[0], 10, INFO_SIZE/3);
                g2d.drawString(m_info[1], 10, 2*(INFO_SIZE/3));
            }
 


            if (m_firstPaint) {
                setPreferredSize(new Dimension(matrixTotalWidth+VISIBILITY_MARGE_SIZE, matrixTotalHeight+VISIBILITY_MARGE_SIZE));
                m_firstPaint = false;
                revalidate();
                repaint();
            }

        }
        private Font m_displayFont = null;
        private FontMetrics m_metrics = null;


        private void computeNecessarySize(Graphics g) {

            FontMetrics fontMetrics = g.getFontMetrics(FONT_HELVETICA_11);
            
            // Calculation for Proteins
            int maxStringLength = 0;
       
            for (LightProteinMatch proteinMatch : m_component.getProteinArray(m_showEquivalentProteins)) {
                String proteinName = proteinMatch.getAccession();
                int nb = proteinName.length();
                if (nb>maxStringLength) {
                    maxStringLength = nb;
                }
            }
            
            int stringLength = Math.min(m_proteinNameNbCharsMax, maxStringLength);
            
            int maxPixelWidth = 0;
            for (LightProteinMatch proteinMatch : m_component.getProteinArray(m_showEquivalentProteins)) {
                String proteinName = proteinMatch.getAccession();
                int nb = proteinName.length();
                if (nb>stringLength) {
                    proteinName = proteinName.substring(0, stringLength)+".";
                }
                int stringWidth = fontMetrics.stringWidth(proteinName);
                if (stringWidth>maxPixelWidth) {
                    maxPixelWidth = stringWidth;
                }
            }
            m_proteinNamePixelSize = maxPixelWidth;
            
            // Calculation for Peptides
            maxStringLength = 0;
       
            for (LightPeptideMatch peptideMatch : m_component.getPeptideArray()) {
                String peptideName = peptideMatch.getSequence();
                int nb = peptideName.length();
                if (nb>maxStringLength) {
                    maxStringLength = nb;
                }
            }
            
            stringLength = Math.min(m_peptideNameNbCharsMax, maxStringLength);
            
            maxPixelWidth = 0;
            for (LightPeptideMatch peptideMatch : m_component.getPeptideArray()) {
                String peptideName = peptideMatch.getSequence();
                int nb = peptideName.length();
                if (nb>stringLength) {
                    peptideName = peptideName.substring(0, stringLength)+".";
                }
                int stringWidth = fontMetrics.stringWidth(peptideName);
                if (stringWidth>maxPixelWidth) {
                    maxPixelWidth = stringWidth;
                }
            }
            m_peptideNamePixelSize = maxPixelWidth;
        }
        

        private void drawRotatedLine(Graphics g, double x, double y, double theta, String label) {

            Graphics2D g2D = (Graphics2D) g;

            // Create a rotation transformation for the font.
            AffineTransform fontAT = new AffineTransform();

            // get the current font
            Font theFont = g2D.getFont();

            // Derive a new font using a rotatation transform
            fontAT.rotate(theta);
            Font theDerivedFont = theFont.deriveFont(fontAT);

            // set the derived font in the Graphics2D context
            g2D.setFont(theDerivedFont);

            // Render a string using the derived font
            g2D.drawString(label, (int) x, (int) y);

            // put the original font back
            g2D.setFont(theFont);
        }
    }

    @Override
    public void setDataBox(AbstractDataBox dataBox) {
        m_dataBox = dataBox;
    }

    @Override
    public AbstractDataBox getDataBox() {
        return m_dataBox;
    }
    
    @Override
    public void addSingleValue(Object v) {
        // not used for the moment JPM.TODO ?
    }

    @Override
    public ActionListener getRemoveAction(SplittedPanelContainer splittedPanel) {
        return m_dataBox.getRemoveAction(splittedPanel);
    }

    @Override
    public ActionListener getAddAction(SplittedPanelContainer splittedPanel) {
        return m_dataBox.getAddAction(splittedPanel);
    }

    @Override
    public ActionListener getSaveAction(SplittedPanelContainer splittedPanel) {
        return m_dataBox.getSaveAction(splittedPanel);
    }
    
    
    public class Cell {
        
        protected DPeptideMatch m_peptideMatch = null;
        protected DProteinMatch m_proteinMatch = null;
        
        protected boolean m_showHalfFilled = false;
        
        protected int m_row;
        protected int m_col;
        
        protected boolean m_flewOver = false;
        protected boolean m_selected = false;
        
        protected boolean m_showWeakness = false;
        
        public Cell(int row, int col) {
            m_row = row;
            m_col = col;
        }
        
        public void setShowWeakNess() {
            m_showWeakness = true;
        }
        
        public boolean isSelectedOrOver() {
            return ((m_selected) || (m_flewOver));
        }
        
        public void setPeptideMatch(DPeptideMatch peptideMatch) {
            m_peptideMatch = peptideMatch;
        }
        
        public void setProteinMatch(DProteinMatch proteinMatch) {
            m_proteinMatch = proteinMatch;
        }
        
        public DPeptideMatch getPeptideMatch() {
            return m_peptideMatch;
        }
        
        public DProteinMatch getProteinMatch() {
            return m_proteinMatch;
        }
        
        public long getPeptideMatchId() {
            if (m_peptideMatch == null) {
                return -1l;
            }
            return m_peptideMatch.getId();
        }

        public long getProteinMatchId() {
            if (m_proteinMatch == null) {
                return -1l;
            }
            return m_proteinMatch.getId();
        }

        public void draw(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            
            g.setColor(getFillColor());
            if (m_showHalfFilled) {
                if (m_xPoints == null) {
                    m_xPoints = new int[3];
                    m_yPoints = new int[3];
                }
                m_xPoints[0] = getPixelX();
                m_xPoints[1] = getPixelX()+m_squareSize;
                m_xPoints[2] = getPixelX();
                m_yPoints[0] = getPixelY();
                m_yPoints[1] = getPixelY();
                m_yPoints[2] = getPixelY()+m_squareSize;
                g.fillPolygon(m_xPoints, m_yPoints, 3);
            } else {
                g.fillRect(getPixelX(), getPixelY(), m_squareSize, m_squareSize);
            }

            if (m_selected || m_flewOver) {
                g.setColor(SELECTION_COLOR);
            } /*else if (m_showWeakness) {  //JPM.TODO : show weakness not done for the moment
                g.setColor(Color.red);
            }*/ else {
                g.setColor(Color.lightGray);
            }

            Stroke s = g2d.getStroke();
            if (m_selected || m_flewOver) {
                g2d.setStroke(SELECTED_STROKE);
                
            }
            
            g.drawRect(getPixelX(), getPixelY(), m_squareSize, m_squareSize);
            g2d.setStroke(s);
        }
        private int m_xPoints[];
        private int m_yPoints[];

        
        public void setSelected(boolean v) {
            m_selected = v;
        }
        
        public void setFlewOver(boolean v) {
            m_flewOver = v;
        }
        
        public int getPixelX() {
            return m_xOffsetMatrix + ((m_col+1) * m_squareSize)+DELTA;
        }
        
        public int getPixelY() {
            return m_yOffsetMatrix + ((m_row+1) * m_squareSize)+DELTA;
        }
        
        public boolean inside(int x, int y) {
            int pixelX = getPixelX();
            int pixelY = getPixelY();
            return ((x>=pixelX) && (x<=pixelX+m_squareSize) && (y>=pixelY) && (y<=pixelY+m_squareSize));
        }
        
        public Color getFillColor() {

            int index = m_row * m_component.getProteinSize(m_showEquivalentProteins) + m_col;
            if (m_flagArray[index] == 1) {

                
                
                float score = m_peptideScore[m_row];

                int colorIndex = (int) score / 10;
                if (colorIndex < 0) {
                    // should not happen
                    colorIndex = 0;
                } else if (colorIndex > 5) {
                    colorIndex = 5;
                }
                return SCORE_COLOR_LIST.get(colorIndex);
            } else {
                return Color.white;
            }
        }
        
        public void getInfo(String[] info) {
            LightProteinMatch proteinMatch = (m_col>=0) ? m_component.getProteinArray(m_showEquivalentProteins).get(m_col) : null;
            LightPeptideMatch peptideMatch = (m_row>=0) ? m_component.getPeptideArray().get(m_row) : null;
            
            if (proteinMatch == null) {
                info[0] = "";
            } else {
                m_sb.append("Protein: ");
                m_sb.append(proteinMatch.getAccession());
                m_sb.append("    Score: ");
                m_sb.append(m_proteinMap.get(proteinMatch.getId()).getPeptideSet(m_rsmId).getScore());
                info[0] = m_sb.toString();
                m_sb.setLength(0);
            }
            
            if (peptideMatch == null) {
                info[1] = "";
            } else {
                m_sb.append("Peptide: ");
                m_sb.append(peptideMatch.getSequence());
                m_sb.append("    Score: ");
                m_sb.append(peptideMatch.getScore());
                m_sb.append("    Rank: ");
                m_sb.append(peptideMatch.getCDPrettyRank() == null ? "-": peptideMatch.getCDPrettyRank());
                info[1] = m_sb.toString();
                m_sb.setLength(0);
            }
            
        }
        
        
    }
    
    public class PeptideCell extends Cell {

        public PeptideCell(int row) {
            super(row, -1);
        }

        @Override
        public int getPixelX() {
            return super.getPixelX()-DELTA;
        }

        
        @Override
        public Color getFillColor() {

            if (m_peptideRank[m_row] != null && m_peptideRank[m_row] == 1) {
                return COLOR_PEPTIDE_RANK1;
            }
            
            return Color.lightGray;
       
        }
        
    }
    
    public class ProteinCell extends Cell {
        
        
        
        public ProteinCell(int col) {
            super(-1, col);
        }

        @Override
        public int getPixelY() {
            return super.getPixelY()-DELTA;
        }

        
        @Override
        public Color getFillColor() {

            Long proteinSetId = m_component.getProteinArray(m_showEquivalentProteins).get(m_col).getProteinSetId();
            Color c = m_proteinSetColorMap.get(proteinSetId);
            if (c == null) {
                c = CyclicColorPalette.getColor(m_proteinSetColorMap.size());
                m_proteinSetColorMap.put(proteinSetId, c);
            }
            
            return c;

        }
        
        public void setSameSet(boolean sameset) {
            m_showHalfFilled = !sameset;
        }
        
    }
    

    
}
