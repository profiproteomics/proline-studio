/*
 This class is Draws image of Component for global view
 */
package fr.proline.studio.rsmexplorer.adjacencymatrix.visualize;

import fr.proline.studio.dam.tasks.data.LightPeptideMatch;
import fr.proline.studio.dam.tasks.data.LightProteinMatch;
import fr.proline.studio.utils.CyclicColorPalette;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JButton;

/**
 *
 * @author shivanishah
 */
public class MatrixImageButton extends JButton {

    private static final int SQUARE_SIZE = 3;
    private static final int DELTA = 1;
    private static final int FRAME = 2;
    
    private static final BasicStroke SELECT_STROKE = new BasicStroke(3.0f);
    
    private BufferedImage m_image = null;

    private boolean m_isSelected = false;
    
    private static final Color EMPTY_CASE_COLOR = new Color(224, 224, 224);
    
    public MatrixImageButton(int index, Component componentOfImage, DrawVisualization drawVisualization) {
        int nbPeptides = componentOfImage.getPeptideSize();
        int nbProteins = componentOfImage.getProteinSize();

        int width = (nbProteins * (SQUARE_SIZE+DELTA))+DELTA+FRAME*2;
        int height = (nbPeptides * (SQUARE_SIZE+DELTA))+DELTA+FRAME*2;
        
        Dimension dimension = new Dimension(width, height);
        setMaximumSize(dimension);
        setMinimumSize(dimension);
        setPreferredSize(dimension);

        createImage(index, componentOfImage, drawVisualization);
    }
    
    private void createImage(int index, Component componentOfImage, DrawVisualization drawVisualization) {

        int nbPeptides = componentOfImage.getPeptideSize();
        int nbProteins = componentOfImage.getProteinSize();

        boolean[][] flagArray = new boolean[nbPeptides][nbProteins];
        for (int i=0;i<nbPeptides;i++) {
            for (int j=0;j<nbProteins;j++) {
                flagArray[i][j] = false;
            }
        }

        HashMap<LightPeptideMatch, ArrayList<LightProteinMatch>> m_peptideToProteinMap = drawVisualization.getPeptideToProteinMap();

        ArrayList<LightPeptideMatch> peptideList = componentOfImage.m_peptideArray;
        int peptIndex = -1;

        for (LightPeptideMatch temp2 : peptideList) {
            peptIndex++;
            ArrayList<LightProteinMatch> proteinList = m_peptideToProteinMap.get(temp2);

            for (LightProteinMatch temp3 : proteinList) {
                int protIndex = componentOfImage.m_proteinMatchArray.indexOf(temp3);
                flagArray[peptIndex][protIndex] = true;
            }
        }

        Dimension d = getPreferredSize();
        m_image = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_RGB);
        Graphics g = m_image.getGraphics();

        
        g.setColor(Color.white);
        g.fillRect(0, 0, d.width, d.height);
        
        Color color = CyclicColorPalette.getColor(index);

        for (int x = 0; x < nbPeptides; x++) {

            for (int y = 0; y < nbProteins; y++) // pept=Yaxis, length, prot=Xaxis, width fArray[][]
            {
                int i = (y * (SQUARE_SIZE+DELTA))+DELTA+FRAME;
                int j = (x * (SQUARE_SIZE+DELTA))+DELTA+FRAME;
                //if(flagArray[x*width+y] == 1)
                if (flagArray[x][y]) {
                    g.setColor(color);
                    g.fillRect(i,j, SQUARE_SIZE, SQUARE_SIZE);
                } else {
                    g.setColor(EMPTY_CASE_COLOR);
                    g.fillRect(i,j, SQUARE_SIZE, SQUARE_SIZE);
                }

            }
        }
        
        g.setColor(Color.black);
        g.drawRect(0, 0, d.width-1, d.height-1);
        
    }

    public void setSelection(boolean isSelected) {
        m_isSelected = isSelected;
    }

    @Override
    protected void paintComponent(Graphics g) {
        
        Graphics2D g2d = (Graphics2D) g;
        
        g2d.drawImage(m_image, 0, 0, null); 
        
        if (m_isSelected) {
            Dimension d = getPreferredSize();
            g2d.setColor(Color.black);
            Stroke s = g2d.getStroke();
            g2d.setStroke(SELECT_STROKE);
            g2d.drawRect(1, 1, d.width-3, d.height-3);
            g2d.setStroke(s);
        }
        
    }

}
