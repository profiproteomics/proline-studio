/*
 This class is Draws image of Component for global view
 */
package fr.proline.studio.rsmexplorer.adjacencymatrix.visualize;

import fr.proline.studio.dam.tasks.data.LightPeptideMatch;
import fr.proline.studio.dam.tasks.data.LightProteinMatch;
import fr.proline.studio.utils.CyclicColorPalette;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JButton;

/**
 *
 * @author shivanishah
 */
public class MatrixImageButton extends JButton {

    
    private BufferedImage m_image = null;
    
    private static final int SQUARE_SIZE = 3;

    public MatrixImageButton(int index, Component componentOfImage, DrawVisualization drawVisualization) {
        int height = componentOfImage.getPeptideSize();
        int width = componentOfImage.getProteinSize();

        Dimension dimension = new Dimension(width * SQUARE_SIZE, height * SQUARE_SIZE);
        setMaximumSize(dimension);
        setMinimumSize(dimension);
        setPreferredSize(dimension);

        createImage(index, componentOfImage, drawVisualization);
    }
    
    private void createImage(int index, Component componentOfImage, DrawVisualization drawVisualization) {

        int height = componentOfImage.getPeptideSize();
        int width = componentOfImage.getProteinSize();

        boolean[][] flagArray = new boolean[height][width];
        for (int i=0;i<height;i++) {
            for (int j=0;j<width;j++) {
                flagArray[i][j] = false;
            }
        }

        HashMap<LightPeptideMatch, ArrayList<LightProteinMatch>> m_peptideToProteinMap = drawVisualization.get_peptideToProteinMap();

        ArrayList<LightPeptideMatch> peptideList = componentOfImage.peptideSet;
        int peptIndex = -1;

        for (LightPeptideMatch temp2 : peptideList) {
            peptIndex++;
            ArrayList<LightProteinMatch> proteinList = m_peptideToProteinMap.get(temp2);

            for (LightProteinMatch temp3 : proteinList) {
                int protIndex = componentOfImage.proteinSet.indexOf(temp3);
                //int z = [peptIndex*width+protIndex ;
                flagArray[peptIndex][protIndex] = true;
            }
        }

        m_image = new BufferedImage(width * SQUARE_SIZE, height * SQUARE_SIZE, BufferedImage.TYPE_INT_RGB);
        Graphics g = m_image.getGraphics();

        Color color = CyclicColorPalette.getColor(index);

        for (int x = 0; x < height; x++) {

            for (int y = 0; y < width; y++) // pept=Yaxis, length, prot=Xaxis, width fArray[][]
            {
                int i = y * SQUARE_SIZE;
                int j = x * SQUARE_SIZE;
                //if(flagArray[x*width+y] == 1)
                if (flagArray[x][y]) {
                    g.setColor(color);
                    g.fillRect(i,j,SQUARE_SIZE,SQUARE_SIZE);
                } else {
                    g.setColor(Color.black);
                    g.fillRect(i,j,SQUARE_SIZE,SQUARE_SIZE);
                }

            }
        }
    }


    @Override
    protected void paintComponent(Graphics g) {
        g.drawImage(m_image, 0, 0, null); // see javadoc for more info on the parameters            
    }


    /*@Override
    public Dimension getPreferredSize() {
        return new Dimension(m_componentOfImage.getPeptideSize() * 3, m_componentOfImage.getProteinSize() * 3);
    }*/

}
