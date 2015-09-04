/*
This class is Draws image of Component for global view
 */
package fr.proline.studio.rsmexplorer.adjacencymatrix.visualize;

import fr.proline.studio.dam.tasks.data.LightPeptideMatch;
import fr.proline.studio.dam.tasks.data.LightProteinMatch;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import javax.swing.JButton;


/**
 *
 * @author shivanishah
 */
public class MatrixImageButton  extends JButton{
    
    public Component componentOfImage ;
    
    private DrawVisualization m_drawVisualization;
    
    public MatrixImageButton (Component c, DrawVisualization drawVisualization) {
        this.componentOfImage = c ;
        m_drawVisualization = drawVisualization;
        
//        drawImage(c);
        int height = c.getPeptideSize();
        int width = c.getProteinSize() ;
       
        Dimension D = new Dimension(width*3, height*3);
        setMaximumSize(D);
        this.setMinimumSize(D);
    
    }
    
    private void drawImage(Component c)
    {
        int height = c.getPeptideSize();
        int width = c.getProteinSize() ;
       
        
       // JPanel image = new JPanel();
       this.setSize(new Dimension(width*3, height*3));
        
    
       int[][] flagArray = new int[height][width];
       
        DrawVisualization tempObject = new DrawVisualization() ;
        HashMap<LightPeptideMatch, ArrayList<LightProteinMatch>> m_peptideToProteinMap = new HashMap<>();

        m_peptideToProteinMap = m_drawVisualization.get_peptideToProteinMap() ;
        
        ArrayList<LightPeptideMatch> peptideList = c.peptideSet ;
        int peptIndex = -1 ;
        
        for( LightPeptideMatch temp2 : peptideList )
        {
        	 peptIndex++ ;
        	 ArrayList<LightProteinMatch> proteinList = m_peptideToProteinMap.get(temp2) ;
        	 
        	 	for(LightProteinMatch temp3 : proteinList )
        	 	{
        	 		int protIndex = c.proteinSet.indexOf(temp3) ;
        	 		//int z = [peptIndex*width+protIndex ;
        	 		flagArray[peptIndex][protIndex] = 1 ;                    
        	 	}
         }
        
        BufferedImage img = new BufferedImage(width*3, height*3, BufferedImage.TYPE_INT_RGB);
        
        // y = row, x=col
        //int colour = 28;
 /*       int r = 100;// red component 0...255
        int g = 25;// green component 0...255
        int b = 89;// blue component 0...255
        int a = 0;// alpha (transparency) component 0...255
        int colour;
        colour = (a << 24) | (r << 16) | (g << 8) | b; */
        //Color color = Color.red ;
        Color color = randomColor();
        
        int i=0, j=0;
        for(int x=0; x<height; x++)
        {
            
            for(int y=0; y<width; y++) // pept=Yaxis, length, prot=Xaxis, width fArray[][]
            {
                    i=y*3;
                    j=x*3;
                //if(flagArray[x*width+y] == 1)
                if(flagArray[x][y] == 1){
                img.setRGB(i, j, color.getRGB()); 
                img.setRGB(i, j+1, color.getRGB()); 
                img.setRGB(i, j+2, color.getRGB()); 
                img.setRGB(i+1, j, color.getRGB()); 
                img.setRGB(i+1, j+1, color.getRGB()); 
                img.setRGB(i+1, j+2, color.getRGB()); 
                img.setRGB(i+2, j, color.getRGB()); 
                img.setRGB(i+2, j+1, color.getRGB()); 
                img.setRGB(i+2, j+2, color.getRGB()); 
                }
               else
                    {
                img.setRGB(i, j, Color.BLACK.getRGB()); 
                img.setRGB(i, j+1, Color.BLACK.getRGB()); 
                img.setRGB(i, j+2, Color.BLACK.getRGB()); 
                img.setRGB(i+1, j, Color.BLACK.getRGB());
                img.setRGB(i+1, j+1, Color.BLACK.getRGB()); 
                img.setRGB(i+1, j+2, Color.BLACK.getRGB()); 
                img.setRGB(i+2, j, Color.BLACK.getRGB()); 
                img.setRGB(i+2, j+1, Color.BLACK.getRGB()); 
                img.setRGB(i+2, j+2, Color.BLACK.getRGB()); 
                
                
                
                }
               //  img.setRGB(y, x, Color.WHITE.getRGB() );
                 
                
            }
        }
    

        
   /*     Border raisedbevel = BorderFactory.createRaisedBevelBorder();
        Border loweredbevel = BorderFactory.createLoweredBevelBorder();
        Border compound = BorderFactory.createCompoundBorder(raisedbevel, loweredbevel);
        this.setBorder(compound); */
    }
    
    @Override
protected void paintComponent(Graphics g) {
    
    Component c = componentOfImage; 
    
            int height = c.getPeptideSize();
        int width = c.getProteinSize() ;
       
        
       // JPanel image = new JPanel();
       this.setSize(new Dimension(width*3, height*3));
        
    
       int[][] flagArray = new int[height][width];

        HashMap<LightPeptideMatch, ArrayList<LightProteinMatch>> m_peptideToProteinMap = m_drawVisualization.get_peptideToProteinMap() ;
        
        ArrayList<LightPeptideMatch> peptideList = c.peptideSet ;
        int peptIndex = -1 ;
        
        for( LightPeptideMatch temp2 : peptideList )
        {
        	 peptIndex++ ;
        	 ArrayList<LightProteinMatch> proteinList = m_peptideToProteinMap.get(temp2) ;
        	 
        	 	for(LightProteinMatch temp3 : proteinList )
        	 	{
        	 		int protIndex = c.proteinSet.indexOf(temp3) ;
        	 		//int z = [peptIndex*width+protIndex ;
        	 		flagArray[peptIndex][protIndex] = 1 ;                    
        	 	}
         }
        
        BufferedImage img = new BufferedImage(width*3, height*3, BufferedImage.TYPE_INT_RGB);
        
        // y = row, x=col
        //int colour = 28;
 /*       int r = 100;// red component 0...255
        int g = 25;// green component 0...255
        int b = 89;// blue component 0...255
        int a = 0;// alpha (transparency) component 0...255
        int colour;
        colour = (a << 24) | (r << 16) | (g << 8) | b; */
        //Color color = Color.red ;
        Color color = randomColor();
        
        int i=0, j=0;
        for(int x=0; x<height; x++)
        {
            
            for(int y=0; y<width; y++) // pept=Yaxis, length, prot=Xaxis, width fArray[][]
            {
                    i=y*3;
                    j=x*3;
                //if(flagArray[x*width+y] == 1)
                if(flagArray[x][y] == 1){
                img.setRGB(i, j, color.getRGB()); 
                img.setRGB(i, j+1, color.getRGB()); 
                img.setRGB(i, j+2, color.getRGB()); 
                img.setRGB(i+1, j, color.getRGB()); 
                img.setRGB(i+1, j+1, color.getRGB()); 
                img.setRGB(i+1, j+2, color.getRGB()); 
                img.setRGB(i+2, j, color.getRGB()); 
                img.setRGB(i+2, j+1, color.getRGB()); 
                img.setRGB(i+2, j+2, color.getRGB()); 
                }
               else
                    {
                img.setRGB(i, j, Color.BLACK.getRGB()); 
                img.setRGB(i, j+1, Color.BLACK.getRGB()); 
                img.setRGB(i, j+2, Color.BLACK.getRGB()); 
                img.setRGB(i+1, j, Color.BLACK.getRGB());
                img.setRGB(i+1, j+1, Color.BLACK.getRGB()); 
                img.setRGB(i+1, j+2, Color.BLACK.getRGB()); 
                img.setRGB(i+2, j, Color.BLACK.getRGB()); 
                img.setRGB(i+2, j+1, Color.BLACK.getRGB()); 
                img.setRGB(i+2, j+2, Color.BLACK.getRGB()); 
                
                
                
                }
               //  img.setRGB(y, x, Color.WHITE.getRGB() );
                 
                
            }
        }
    

    
    super.paintComponent(g);
    g.drawImage(img, 0, 0, null); // see javadoc for more info on the parameters            
}

private Color randomColor() {
     final Random random = new Random();
     return new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
    }


@Override
public Dimension getPreferredSize() {
        return new Dimension(componentOfImage.getPeptideSize() * 3, componentOfImage.getProteinSize() * 3);
    } 

   
}


