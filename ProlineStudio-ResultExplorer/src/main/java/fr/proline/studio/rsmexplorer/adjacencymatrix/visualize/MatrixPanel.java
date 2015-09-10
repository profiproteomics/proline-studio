/* This class Draws the Detail Panel when the component is selected. 
 * */
package fr.proline.studio.rsmexplorer.adjacencymatrix.visualize;

import fr.proline.studio.dam.tasks.data.LightPeptideMatch;
import fr.proline.studio.dam.tasks.data.LightProteinMatch;
import fr.proline.studio.export.ExportButton;
import fr.proline.studio.export.ImageExporterInterface;
import fr.proline.studio.gui.HourglassPanel;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxPanelInterface;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

public class MatrixPanel extends HourglassPanel implements DataBoxPanelInterface {

    private AbstractDataBox m_dataBox;

    private static final int MIN_CELL_SIZE = 14;
    private static final int MAX_CELL_SIZE = 30;
    private static final int DELTA = 3;
    private static final int TITLE_SIZE = 20;
    private static final int INFO_SIZE = 50;
    private static final int PEPTIDE_NAME_SIZE = 80;
    private static final int PROTEIN_NAME_SIZE = 80;
    private static final int VISIBILITY_MARGE_SIZE = 20;

    private static final String PROTEINS = "Proteins";
    private static final String PEPTIDES = "Peptides";

    private List<Cell> m_cells;
    private Cell m_selectedCell = null;
    private List<PeptideCell> m_peptideCells;
    private List<ProteinCell> m_proteinCells;
    private static final ArrayList<Color> SCORE_COLOR_LIST = new ArrayList<>(6);

    private static final Color COLOR_GRAY = new Color(95, 87, 88);
    private static final Color COLOR_PEPTIDE_RANK1 = new Color(134, 180, 96);
    private static final Color COLOR_OVER = new Color(95, 158, 160);

    private boolean m_firstPaint = true;

    private int[] m_peptideRank;
    private Float[] m_peptideScore;
    private Float[] m_proteinScore;

    private int m_squareSize;
    private int m_xOffset;
    private int m_xOffsetMatrix;
    private int m_yOffsetMatrix;

    private Component m_component;
    private int[] m_flagArray;
    private DrawVisualization m_drawVisualization = null;
    private HashMap<LightPeptideMatch, ArrayList<LightProteinMatch>> m_peptideToProteinMap = new HashMap<>();

    private String[] m_info = new String[2];

    private JPanel m_internalPanel;
    
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
        
        ExportButton exportImageButton = new ExportButton("Protein/Peptide Matrix", m_internalPanel);
        toolbar.add(exportImageButton);
        
        return toolbar;
    }
    
    public void setData(Component c, DrawVisualization drawVisualization) {

        setLoaded(0);

        if (m_cells != null) {
            m_cells.clear();
        }

        m_drawVisualization = drawVisualization;

        m_peptideToProteinMap = m_drawVisualization.get_peptideToProteinMap();
        m_component = c;

        int rowCount = m_component.getPeptideSize();
        int columnCount = m_component.getProteinSize();

        m_cells = new ArrayList<>(columnCount * rowCount);
        m_peptideCells = new ArrayList<>(rowCount);	//pepide score
        m_proteinCells = new ArrayList<>(columnCount);	// protine Score

        m_peptideRank = new int[rowCount];

        m_flagArray = new int[columnCount * rowCount];

        m_peptideScore = new Float[rowCount];
        int index = 0;
        for (LightPeptideMatch peptideMatch : m_component.peptideSet) {

            m_peptideScore[index] = peptideMatch.getScore();
            m_peptideRank[index] = peptideMatch.getCDPrettyRank();
            index++;
        }

        m_proteinScore = new Float[columnCount];
        index = 0;
        for (LightProteinMatch proteinMatch : m_component.proteinSet) {
            m_proteinScore[index] = proteinMatch.getScore();
            index++;
        }

        m_firstPaint = true;

        repaint();

    }

    public class InternalPanel extends JPanel {

        public InternalPanel() {
            MouseAdapter mouseHandler = new MouseAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {

                    if (m_component == null) {
                        return;
                    }
                    
                    boolean needRepaint = false;

                    // reinit selection if needed
                    if (m_selectedCell != null) {
                        m_selectedCell.setSelected(false);
                        m_selectedCell = null;
                        needRepaint = true;
                    }

                    int mouseX = e.getX();
                    int mouseY = e.getY();

                    int columnCount = m_component.getProteinSize();

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
                            m_selectedCell = m_cells.get(row * columnCount + column);
                            m_selectedCell.setSelected(true);
                            needRepaint = true;
                        }
                    } else if ((mouseX >= matrixX1)
                            && (mouseX < matrixX2)
                            && (mouseY >= proteinY1)
                            && (mouseY < proteinY2)) {
                        int column = (mouseX - matrixX1) / m_squareSize;
                        m_selectedCell = m_proteinCells.get(column);
                        m_selectedCell.setSelected(true);
                        needRepaint = true;
                    } else if ((mouseY >= matrixY1)
                            && (mouseY < matrixY2)
                            && (mouseX >= peptideX1)
                            && (mouseX < peptideX2)) {
                        int row = (mouseY - matrixY1) / m_squareSize;
                        m_selectedCell = m_peptideCells.get(row);
                        m_selectedCell.setSelected(true);
                        needRepaint = true;
                    }

                    if (needRepaint) {
                        repaint();
                    }

                }
            };
            addMouseMotionListener(mouseHandler);
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
            }

            int rowCount = m_component.getPeptideSize();
            int columnCount = m_component.getProteinSize();

            // calculate square size :
            int squareSizeX = (int) (((width - TITLE_SIZE - PEPTIDE_NAME_SIZE - DELTA) / (columnCount + 1)));
            int squareSizeY = (int) (((height - INFO_SIZE - TITLE_SIZE - PROTEIN_NAME_SIZE - DELTA) / (rowCount + 1)));
            m_squareSize = Math.min(squareSizeX, squareSizeY);
            if (m_squareSize > MAX_CELL_SIZE) {
                m_squareSize = MAX_CELL_SIZE;
            } else if (m_squareSize < MIN_CELL_SIZE) {
                m_squareSize = MIN_CELL_SIZE;
            }

            int matrixWidth = (columnCount + 1) * m_squareSize + DELTA;
            int matrixTotalWidth = matrixWidth + TITLE_SIZE + PEPTIDE_NAME_SIZE;
            int matrixHeight = (rowCount + 1) * m_squareSize + DELTA;
            int matrixTotalHeight = matrixHeight + INFO_SIZE + TITLE_SIZE + PROTEIN_NAME_SIZE;

            m_xOffset = (width - matrixTotalWidth) / 2;
            if (m_xOffset < 0) {
                m_xOffset = 0;
            }
            m_xOffsetMatrix = m_xOffset + TITLE_SIZE + PEPTIDE_NAME_SIZE + DELTA;
            m_yOffsetMatrix = INFO_SIZE + TITLE_SIZE + PROTEIN_NAME_SIZE + DELTA;

            g2d.setColor(COLOR_GRAY);
            g2d.setFont(new Font("Helvetica", Font.BOLD, 12));
            FontMetrics fontMetricsBold = g.getFontMetrics(g.getFont());

            int proteinsWidth = fontMetricsBold.stringWidth(PROTEINS);
            int peptidesWidth = fontMetricsBold.stringWidth(PEPTIDES);

            g2d.drawString(PROTEINS, m_xOffset + matrixTotalWidth / 2 - proteinsWidth / 2, INFO_SIZE + TITLE_SIZE / 2);

            label_line(g2d, (double) (m_xOffset + TITLE_SIZE / 2), matrixTotalHeight / 2 + peptidesWidth / 2, (double) 270 * java.lang.Math.PI / 180, PEPTIDES);

            g2d.setFont(new Font("Helvetica", Font.PLAIN, 11));
            FontMetrics fontMetricsPlain = g.getFontMetrics(g.getFont());


            if (m_cells.isEmpty()) {
                for (int row = 0; row < rowCount; row++) {
                    for (int col = 0; col < columnCount; col++) {
                        m_cells.add(new Cell(row, col));
                    }
                }
            }

            if (m_peptideCells.isEmpty()) {
                for (int row = 0; row < rowCount; row++) {
                    PeptideCell cell = new PeptideCell(row);
                    m_peptideCells.add(cell);
                }
            }

            if (m_proteinCells.isEmpty()) {
                for (int col = 0; col < columnCount; col++) {
                    ProteinCell cell = new ProteinCell(col);
                    m_proteinCells.add(cell);

                }
            }

            ArrayList<LightPeptideMatch> peptideList = m_component.peptideSet;
            int peptIndex = -1;

            for (LightPeptideMatch peptideMatch : peptideList) {
                peptIndex++;
                ArrayList<LightProteinMatch> proteinList = m_peptideToProteinMap.get(peptideMatch);

                for (LightProteinMatch proteinMatch : proteinList) {
                    int protIndex = m_component.proteinSet.indexOf(proteinMatch);
                    int z = peptIndex * columnCount + protIndex;
                    m_flagArray[z] = 1;
                }
            }

            // draw matrix
            for (Cell cell : m_cells) {
                cell.draw(g2d);
            }
            
            // draw peptide column
            for (PeptideCell cell : m_peptideCells) {
                cell.draw(g2d);
            }
            
            // draw protein column
            for (ProteinCell cell : m_proteinCells) {
                cell.draw(g2d);
            }


            /*Drawing Labels*/
            Color c = g2d.getColor();
            g2d.setColor(new Color(212, 91, 91));

            int dIndex = 0;
            for (LightPeptideMatch peptideMatch : m_component.peptideSet) {
                String sequence = peptideMatch.getSequence();
                int stringWidth = 0;
                int i=0;
                for (;i<sequence.length();i++) {
                    stringWidth +=  fontMetricsPlain.charWidth(sequence.charAt(i));
                    if (stringWidth>PEPTIDE_NAME_SIZE) {
                        i--;
                        break;
                    }
                }
                sequence = sequence.substring(0, i);


                Cell cell = m_cells.get(dIndex * columnCount);
                double y = cell.getPixelY();

                g2d.drawString(sequence, m_xOffset + TITLE_SIZE, (int) (y + (m_squareSize / 2)));



                dIndex++;
            }

            dIndex = 0;
            for (LightProteinMatch proteinMatch : m_component.proteinSet) {
                String proteinName = proteinMatch.getAccession();
                int stringWidth = 0;
                int i=0;
                for (;i<proteinName.length();i++) {
                    stringWidth +=  fontMetricsPlain.charWidth(proteinName.charAt(i));
                    if (stringWidth>PROTEIN_NAME_SIZE) {
                        i--;
                        break;
                    }
                }
                proteinName = proteinName.substring(0, i);

                Cell cell = m_cells.get(dIndex);
                int x = cell.getPixelX();
                int y = cell.getPixelY();
                label_line(g2d, (double) (x + m_squareSize / 2), (double) (y - 5 - m_squareSize), (double) 270 * java.lang.Math.PI / 180, proteinName);

                dIndex++;

            }

            if (m_selectedCell != null) {
                m_selectedCell.getInfo(m_info);
                
                g2d.setFont(new Font("Helvetica", Font.BOLD, 12));
                g2d.setColor(Color.black);
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


        private void label_line(Graphics g, double x, double y, double theta, String label) {

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
    public ActionListener getRemoveAction(SplittedPanelContainer splittedPanel) {
        //JPM.TODO
        return null;
    }

    @Override
    public ActionListener getAddAction(SplittedPanelContainer splittedPanel) {
        //JPM.TODO
        return null;
    }

    @Override
    public ActionListener getSaveAction(SplittedPanelContainer splittedPanel) {
        //JPM.TODO
        return null;
    }
    
    
    public class Cell {
        
        protected int m_row;
        protected int m_col;
        
        protected boolean m_selected = false;
        
        public Cell(int row, int col) {
            m_row = row;
            m_col = col;
        }
        
        public void draw(Graphics g) {
            g.setColor(getFillColor());
            g.fillRect(getPixelX(), getPixelY(), m_squareSize, m_squareSize);
            
            g.setColor(COLOR_GRAY);
            g.drawRect(getPixelX(), getPixelY(), m_squareSize, m_squareSize);
        }
        
        public void setSelected(boolean v) {
            m_selected = v;
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

            if (m_selected) {
                return COLOR_OVER;
            }
            
            int index = m_row * m_component.getProteinSize() + m_col;
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
            LightProteinMatch proteinMatch = (m_col>=0) ? m_component.proteinSet.get(m_col) : null;
            LightPeptideMatch peptideMatch = (m_row>=0) ? m_component.peptideSet.get(m_row) : null;
            
            if (proteinMatch == null) {
                info[0] = "";
            } else {
                m_sb.append("Protein: ");
                m_sb.append(proteinMatch.getAccession());
                m_sb.append("    Score: ");
                m_sb.append(proteinMatch.getScore());
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
                m_sb.append(peptideMatch.getCDPrettyRank());
                info[1] = m_sb.toString();
                m_sb.setLength(0);
            }
            
        }
        private StringBuilder m_sb = new StringBuilder();
        
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
        public void setSelected(boolean v) {
            super.setSelected(v);
            int columnCount = m_component.getProteinSize();
            for (int i = 0; i < columnCount; i++) {
                m_cells.get(m_row *columnCount + i).setSelected(v);
            }
        }
        
        @Override
        public Color getFillColor() {
            if (m_selected) {
                return COLOR_OVER;
            }
            
            if (m_peptideRank[m_row] == 1) {
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
        public void setSelected(boolean v) {
            super.setSelected(v);
            int rowCount = m_component.getPeptideSize();
            int columnCount = m_component.getProteinSize();
            for (int i=0;i<rowCount;i++) {
                m_cells.get(i*columnCount+m_col).setSelected(v);
            }
        }
        
        @Override
        public Color getFillColor() {
            
            if (m_selected) {
                return COLOR_OVER;
            }
            
            return new Color(134, 180, 96);  //JPM.TODO

        }
        
    }
    
}
