/* This class Draws the Detail Panel when the component is selected. 
 * */

package fr.proline.studio.rsmexplorer.adjacencymatrix.visualize;

import javax.swing.JPanel;

import fr.proline.studio.dam.tasks.data.LightPeptideMatch;
import fr.proline.studio.dam.tasks.data.LightProteinMatch;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JTextField;

public class MatrixPanel extends JPanel{
	 private int columnCount = 1;
     private int rowCount = 1;
     private List<Rectangle> cells;
     private List<Rectangle> rows;
     private List<Rectangle> columns;
     private List<JButton> buttonCheckList;
     private int[] buttonState ;
     private List<Color> colorList;
     private int[] peptRank ;
     private Float[] peptScoreLog, protScoreLog, peptScore, protScore;
    // private List<Rectangle> rowRectangle, columnRectangle ;
     private Point selectedCell, selectedLabel;
     private Point ProtBound1, ProtBound2, PeptBound1, PeptBound2 ;
     String lStr ;
     int groupNum = 0 ;
     JTextField idDisplay;
     Component pComponent ;
     int[] flagArray ;
     DrawVisualization tempObject = null;
     HashMap<LightPeptideMatch, ArrayList<LightProteinMatch>> m_peptideToProteinMap = new HashMap<>();
     int finalCellHeight, finalCellWidth, finalXOffset, finalYOffset ;
     Long showProtid = (long) 0 , showPeptid = (long) 0 ;
     // use new Color(95,87,88) insted of black
   
     public MatrixPanel(Component c, JPanel cardPanel, JTextField idDisplay, DrawVisualization drawVisualization) {   
        this.setBorder(BorderFactory.createLineBorder(new Color(95,87,88)));
        tempObject = drawVisualization;
    
        m_peptideToProteinMap = tempObject.get_peptideToProteinMap();
            this.pComponent = c ;
        
    	rowCount = pComponent.getPeptideSize() ;
    	columnCount = pComponent.getProteinSize() ;
    	 
    	cells = new ArrayList<>(columnCount * rowCount);
    	rows = new ArrayList<>(rowCount);	//pepide score
    	columns = new ArrayList<>(columnCount);	// protine Score
    //	rowRectangle = new  ArrayList<>(rowCount);
    	buttonCheckList = new  ArrayList<>(columnCount);
    	buttonState = new int[columnCount];
    	for(int z=0; z<columnCount; z++)
    	{ JButton j = new JButton(); buttonCheckList.add(j); }
    	
    	peptRank = new int[rowCount];
    	
    	flagArray = new int[columnCount * rowCount];
    	
    	peptScoreLog = new Float[rowCount];
    	peptScore = new Float[rowCount];
    	//-10LogP is probability
    	int index=0;
    	for(LightPeptideMatch TempPept : pComponent.peptideSet)
    	{
    		
    		//peptScoreLog[index] = (float) java.lang.Math.log(TempPept.getScore());
    		peptScoreLog[index] = Math.min(1 ,(TempPept.getScore()/100));
    		peptScore[index] = TempPept.getScore();
    		peptRank[index] = TempPept.getCDPrettyRank();
    		index++;
    	//	System.out.println("Pept  "+TempPept.getScore());
    	}
    	
    	protScoreLog = new Float[columnCount];
    	protScore = new Float[columnCount];
    	index=0;
    	for(LightProteinMatch TempProt : pComponent.proteinSet)
    	{
    		protScoreLog[index] = Math.min(1,(TempProt.getScore()/100));
    		protScore[index] = TempProt.getScore();
//    		protScoreLog[index] = (float) java.lang.Math.log(TempProt.getScore());
    		index++;
    		//System.out.println("Prot:  "+TempProt.getScore());
    	}
    	
    	this.idDisplay = idDisplay ;
    	this.setLayout(new BorderLayout());
    	//this.add(textPane, BorderLayout.SOUTH);
    	
                
    /*	254 229 217  seq_Reds_7_01 - Not used
     	252 187 161  seq_Reds_7_02 - [0, 10]
    	252 146 114  seq_Reds_7_03 - (10, 25]
    	251 106  74  seq_Reds_7_04	(25, 40)
    	239  59  44  seq_Reds_7_05	[40,55)
    	203  24  29  seq_Reds_7_06	[55, 70)
    	153   0  13  seq_Reds_7_07 	[70, 100)
    	BLack - more than 100 
    	*/
    	
      colorList = new ArrayList<Color>(6) ;
      
      colorList.add(new Color(252,187,161));
      colorList.add(new Color(252,146,114));
      colorList.add(new Color(251,106 ,74));
      colorList.add(new Color(239, 59, 44));
      colorList.add(new Color(203, 24,  29));
      colorList.add(new Color(153,0 , 13));
     
    	
    	
     }
     
    /**
     *
     * @return
     */
     
    
     
     @Override
     public Dimension getPreferredSize() {
       //   Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
         
          int fWidth = 1346 - 20;
          int fHight = 768-100;
         
         return new Dimension(fWidth, fHight);
     }

     @Override
     public void invalidate() {
         cells.clear();
         super.invalidate();
     }

     @Override
     public void paint(Graphics g) {
         //super.paintComponent(g);
         Graphics2D g2d = (Graphics2D) g;

         Dimension panelSize = getSize();

         idDisplay.setVisible(false);
       
         g2d.setColor(Color.WHITE);
         int width = panelSize.width;
         int height = panelSize.height;
         g.fillRect(0, 0, width, height);
         
         g2d.setColor(new Color(95,87,88));

         
         
         if(rowCount > 49 || columnCount > 99)
        	 groupNum = 1 ;
         else
        	 groupNum = 2;
         
        // System.out.println("Group Num : "+groupNum);
         
         int xOffset, yOffset, cellWidth, cellHeight ;
         
         if(groupNum == 1)
         {
        	  g2d.setFont(new Font("Helvetica", Font.BOLD, 12));
        	 
        	 xOffset = 25; //Labels
             yOffset = 45; //Labels
             
             cellWidth = (width-xOffset) / (columnCount+1);
             cellHeight = (height-yOffset) /( rowCount + 2);

             //Put Max size
             if(cellWidth >30)
            	 cellWidth = 30;
             if(cellHeight >30)
            	 cellHeight = 30;
             
             //MAke them square
             if(cellHeight<cellWidth)
            	 cellWidth = cellHeight;
             else
            	 cellHeight = cellWidth; 

             xOffset = (width - (columnCount * cellWidth))/2 + 20  ;
             yOffset = (height - (rowCount * cellHeight))/2 + 20;
             
             if(columnCount> 8)
                 g2d.drawString("Proteins", xOffset+(cellWidth*(columnCount/2))-(cellWidth/2)-5, yOffset-36-cellHeight);
                 else
                 g2d.drawString("Proteins", xOffset, yOffset-36-cellHeight);
              
                 
                 label_line(g2d,(double) (xOffset-44-cellWidth-3), (double)yOffset+(cellHeight*(rowCount/2))+(cellHeight/2),
                		(double) 270 * java.lang.Math.PI/180, "Peptides");
             
                 g2d.setFont(new Font("Helvetica", Font.PLAIN, 7));

         }
         
         else
         {
        	 g2d.setFont(new Font("Helvetica", Font.BOLD, 13));
             xOffset = 10 + 70; //Labels
             yOffset = 10 + 65; //Labels
             
             // xOffset does not contain score row or column 
             cellWidth = (width-xOffset) / (columnCount+1);
             cellHeight = (height-yOffset) / (rowCount+2);

             //Put Max size
             if(cellWidth >30)
            	 cellWidth = 30;
             if(cellHeight >30)
            	 cellHeight = 30;
             
             //MAke them square
             if(cellHeight<cellWidth)
            	 cellWidth = cellHeight;
             else
            	 cellHeight = cellWidth; 
                          
             xOffset = (width - (columnCount * cellWidth))/2 + 30 ;
             yOffset = (height - (rowCount * cellHeight))/2 + 23 ;
             
             
           
             if(columnCount%2 == 0)
                 g2d.drawString("Proteins", xOffset+(cellWidth*(columnCount/2))-(cellWidth/2)-5, yOffset-(cellHeight+3)-50);
                 else
                 g2d.drawString("Proteins", xOffset+(cellWidth*(columnCount/2)), yOffset-50-(cellHeight+3));
              
                 
                 label_line(g2d,(double) (xOffset-60-(cellWidth+3)), (double)yOffset+(cellHeight*(rowCount/2))+(cellHeight/2),
                		(double) 270 * java.lang.Math.PI/180, "Peptides");
        
                 //"Verdana" "Helvetica"
                 g2d.setFont(new Font("Helvetica", Font.PLAIN, 11));
            
         }
         
        
         finalCellHeight = cellHeight;
         finalCellWidth = cellWidth;
         finalXOffset = xOffset;
         finalYOffset = yOffset;
       
        
         if (cells.isEmpty()) {
             for (int row = 0; row < rowCount; row++) {
                 for (int col = 0; col < columnCount; col++) {
                     Rectangle cell = new Rectangle(
                             xOffset + (col * cellWidth),
                             yOffset + (row * cellHeight),
                             cellWidth,
                             cellHeight);
                     cells.add(cell);
                    }
             }
         }
       
         int indexTemp = 0 ;
         if(rows.isEmpty())
         {
        	 for(int row = 0; row < rowCount; row++ ){
        		 Rectangle cell = new Rectangle(
                         xOffset - cellWidth - 3,
                         yOffset +(row * cellHeight),
                         cellWidth,
                         cellHeight);
        		rows.add(cell);
        	/*	Rectangle score = new Rectangle((xOffset - cellWidth - 3)+(int)(cellWidth*(1-peptScoreLog[indexTemp])),
                        yOffset +(row * cellHeight),
                       (int) (cellWidth * peptScoreLog[indexTemp]),
                        cellHeight); 
        		 rowRectangle.add(score);*/
                indexTemp++ ;
         	}
         }
         
         indexTemp = 0 ;
         
         if(columns.isEmpty())
         {
        	 for(int col = 0; col < columnCount; col++ ){
        		 Rectangle cell = new Rectangle(
                         xOffset +(cellWidth *col),
                         yOffset - cellHeight - 3,
                         cellWidth,
                         cellHeight);
                 columns.add(cell);
                 
              
                 
              /*   Rectangle score = new Rectangle(
                         xOffset +(cellWidth *col),
                         (yOffset - cellHeight - 3)+(int)(cellHeight*(1 - protScoreLog[indexTemp])),
                         cellWidth,
                         (int)(cellHeight*protScoreLog[indexTemp]));
         
                 columnRectangle.add(score); */
                 indexTemp++ ;
         	}
         }
         
         ArrayList<LightPeptideMatch> peptideList = pComponent.peptideSet ;
         int peptIndex = -1 ;
        
         for( LightPeptideMatch temp2 : peptideList )
         {
        	 peptIndex++ ;
        	 ArrayList<LightProteinMatch> proteinList = m_peptideToProteinMap.get(temp2) ;
        	 
        	 	for(LightProteinMatch temp3 : proteinList )
        	 	{
        	 		int protIndex = pComponent.proteinSet.indexOf(temp3) ;
        	 		int z = peptIndex*columnCount+protIndex ;
        	 		flagArray[z] = 1 ;                    
        	 	}
         }
         
        
         final  BasicStroke graph = new BasicStroke();
         
         g2d.setStroke(graph);
         /*  float dash1[] = {10.0f};
         //final  BasicStroke dashed =
            new BasicStroke(1.0f,
                             BasicStroke.CAP_BUTT,
                             BasicStroke.JOIN_MITER,
                             10.0f, dash1, 0.0f);
     g2d.setStroke(dashed); */
         
        // g2d.setColor(Color.GRAY);
         for (Rectangle cell : cells)
         {
          	g2d.draw(cell) ;        
         }
         
         
         g2d.setColor(new Color(95,87,88));
         for (Rectangle cell : rows)
         {
        	 //g2d.setColor(new Color(206,189,78));
        	 g2d.fill(cell) ;   
        	// g2d.setColor(new Color(95,87,88));
          	//g2d.draw(cell) ;        
         }
         
         for (Rectangle cell : columns)
         {
        	// g2d.setColor(new Color(206,189,78));
    	 g2d.fill(cell) ;   
        	// g2d.setColor(new Color(95,87,88));
          	//g2d.draw(cell) ;      
         }
        
     /*    g2d.setColor(new Color(206,189,78));
       
        
         for (Rectangle cell :  columnRectangle)
       {
    	   g2d.fill(cell) ;      
        	
       } */
       
         int rankIndex = 0 ;    
         g2d.setColor(new Color(134,180,96));
       for (Rectangle cell :  rows)
       {
        
    	   if(peptRank[rankIndex] == 1)   		   
    		   g2d.fill(cell) ;        
       
        	rankIndex ++ ;
       }
       
         
         
       g2d.setColor(new Color(95,87,88));
       for (Rectangle cell : rows)
       {
      	 //g2d.setColor(new Color(206,189,78));
      	 g2d.draw(cell) ;   
      	// g2d.setColor(new Color(95,87,88));
        	//g2d.draw(cell) ;        
       }
       
      
       
       
       /*	254 229 217  seq_Reds_7_01 - Not used
    	252 187 161  seq_Reds_7_02 - [0, 10]
   	252 146 114  seq_Reds_7_03 - (10, 30]
   	251 106  74  seq_Reds_7_04	(30, 45)
   	239  59  44  seq_Reds_7_05	[45,70)
   	203  24  29  seq_Reds_7_06	[70, 100)
   	153   0  13  seq_Reds_7_07 	[100)
   
   	*/
   	   
       
       Color c1 ;
         for(Rectangle cell : cells)
         {    
        	 //	g2d.setColor(new Color(241, 98, 69));
        		//g2d.setColor(Color.black);
        		 //g2d.setColor(new Color(212,91,91));
        	 
        	 int index = cells.indexOf(cell) ;
        	 
        	 int protNum = index%columnCount , peptNum = index/columnCount ;
    		 
     		 if(protNum == 0)
     		 {
     			 float score = peptScore[peptNum];
     			 
     			 if(score>=0 && score<10)
     				 g2d.setColor( colorList.get(0));
     			 else if (score>=10 && score<30)
     				g2d.setColor( colorList.get(1));
     			 else if(score>=30  && score<45)
     				g2d.setColor( colorList.get(2));
     			else if(score>=45  && score<70)
     				g2d.setColor( colorList.get(3));
     			else if(score>=70  && score<100 )
     				g2d.setColor( colorList.get(4));
     			else if(score >=100  )
     				g2d.setColor( colorList.get(5));
     				
     		 }
     			
        	 if(flagArray[index] == 1)          	 
             { 
        		 
        		 c1 = g2d.getColor() ;
        		 g2d.fill(cell);
             	g2d.setColor(new Color(95,87,88));
             	g2d.draw(cell) ;
                g2d.setColor(c1);
             	
             	double x = cell.getCenterX();
            	double y = cell.getCenterY();
            	
             }
          }  
         
         /*Drawing Labels*/
         Color c = g2d.getColor();
         //g2d.setColor(new Color(95,87,88));
         g2d.setColor(new Color(212,91,91));
         
        if(groupNum == 1) 
        {
        	 int dIndex = 0;
             for(LightPeptideMatch tempPept : pComponent.peptideSet)
             {
            	 long peptId = tempPept.getId() ;
            	 String s = String.valueOf(peptId);
            	 
            	 Rectangle r = cells.get(dIndex*columnCount);
            	 Point p = r.getLocation() ;
            	
            	 double x = r.getCenterX();
             	double y = r.getCenterY();
             	
            	 //g2d.drawString(s,(int) x-(cellWidth)-30,(int) y);
            	 g2d.drawString(s,(int)( x-40-cellWidth-3),(int)(y+ (cellHeight /2 )));
            
            	 if(dIndex == 0)
            		 PeptBound1 = new Point((int) x-40,(int)(y+(cellHeight /2 )));
            	 else if(dIndex == rowCount-1) 
            		 PeptBound2 = new Point((int) x-40,(int)(y+ (cellHeight /2 )));
            		 
            	 dIndex++;
             }
             
            
             
             
             
             dIndex = 0;
             for(LightProteinMatch tempProt : pComponent.proteinSet)
             {
            	 long protId = tempProt.getId() ;
            	 String s = String.valueOf(protId);
            	 
            	 Rectangle r = cells.get(dIndex);
            	 Point p = r.getLocation() ;
            	 label_line(g2d,(double)(p.x + cellWidth/2), (double)(p.y-5-cellHeight),
                 		(double) 270 * java.lang.Math.PI/180, s);
            	 
            	 if(dIndex == 0)
            		 ProtBound1 = new Point(((int)p.x + cellWidth/2), (int)p.y-2);
            	 else if(dIndex == columnCount-1) 
            		 ProtBound2 = new Point(((int)p.x + cellWidth/2), (int)p.y-2);
            	
            	 dIndex++;
            	 
             }
        }
        else
        {
        	 int dIndex = 0;
             for(LightPeptideMatch tempPept : pComponent.peptideSet)
             {
            	 long peptId = tempPept.getId() ;
            	 String s = String.valueOf(peptId);
            	 
            	 Rectangle r = cells.get(dIndex*columnCount);
            	 Point p = r.getLocation() ;
            	 
            	 g2d.drawString(s,(int) p.x-50-3-cellWidth,(int)( p.y+(cellHeight/2)+5));
            	 
            	 if(dIndex == 0)
            		 PeptBound1 = new Point((int) p.x-50,(int)( p.y+(cellHeight/2)+5));
            	 else if(dIndex == rowCount-1) 
            		 PeptBound2 = new Point((int) p.x-50,(int)( p.y+(cellHeight/2)+5));
            	 
            	 dIndex++;
             }
             
             dIndex = 0;
             for(LightProteinMatch tempProt : pComponent.proteinSet)
             {
            	 long protId = tempProt.getId() ;
            	 String s = String.valueOf(protId);
            	 
            	 Rectangle r = cells.get(dIndex);
            	 Point p = r.getLocation() ;
            	 
            	 label_line(g2d,(double)(p.x+(cellWidth/2)+4), (double)(p.y-cellHeight-7),
                 		(double) 270 * java.lang.Math.PI/180, s);
            	 
            	 if(dIndex == 0)
            		 ProtBound1 = new Point((int)(p.x+(cellWidth/2)+4), (int)p.y-2);
            	 else if(dIndex == columnCount-1) 
            		 ProtBound2 = new Point((int)(p.x+(cellWidth/2)+4), (int)p.y-2);
            	
            	 dIndex++;
            	 
             }
        }
        
        g2d.setColor(c);
        
         MouseAdapter mouseHandler;
         mouseHandler = new MouseAdapter() {
             @Override
             public void mouseMoved(MouseEvent e) {
                 //Point point = e.getPoint();

                 int cellWidth = finalCellWidth;
                 int cellHeight = finalCellHeight;

                 Rectangle lastCell = cells.get(cells.size() - 1);
                 Rectangle firstCell = cells.get(0);
                 
                 selectedLabel = null;
        		 lStr = null;
                 
                 if( e.getX() <= firstCell.x || e.getY()<= firstCell.y)
                {
                	 selectedCell = null;
                	 idDisplay.setVisible(false);
               	 }
                 else if( e.getX() >= (lastCell.x+finalCellWidth) || e.getY() >= (lastCell.y+finalCellHeight) )
                 {
                	 selectedCell = null;
                	 idDisplay.setVisible(false);
                 }
                 else
                 {
                     int column = (e.getX()-finalXOffset) / cellWidth;
                     int row = (e.getY()-finalYOffset) / cellHeight;

                     selectedCell = new Point(column, row);
                                    	 
                 }
                 
             if(groupNum == 2)
             {
                 if(selectedCell == null)
                 {
                	 
                	 if( (e.getX() <= finalXOffset && e.getX() >= (PeptBound1.x - cellWidth - 3)) && ( e.getY()<= PeptBound2.y && e.getY()>= finalYOffset))
                     {
                     	 selectedLabel = new Point(e.getX(), e.getY());
                     	 lStr = "Pept place";
                     } 
                	 else if
                	 ( (e.getX() <= (finalXOffset+(cellWidth*columnCount)) && e.getX() >= finalXOffset) && ( e.getY()<= finalYOffset && e.getY()>= (finalYOffset-40-3-cellHeight)))
                     {
                     	 selectedLabel = new Point(e.getX(), e.getY());
                     	 lStr = "Prot place";
                     }
                	 
                 }
             }
             else if (groupNum == 1)
             {
            	 
            	 if(selectedCell == null)
                 {
                	 
                	 if( (e.getX() <= finalXOffset && e.getX() >= (PeptBound1.x - cellWidth - 3)) && ( e.getY()<= PeptBound2.y-2 && e.getY()>= finalYOffset))
                     {
                     	 selectedLabel = new Point(e.getX(), e.getY());
                     	 lStr = "Pept place";
                     } 
                	 else if
                	 ( (e.getX() <= (finalXOffset+(cellWidth*columnCount)-3) && e.getX() >= finalXOffset) && ( e.getY()<= finalYOffset && e.getY()>= (finalYOffset-40-3-cellHeight)))
                     {
                     	 selectedLabel = new Point(e.getX(), e.getY());
                     	 lStr = "Prot place";
                     }
                	 
                 }
            	 
             }
                 repaint();

                 
             }
         };
         addMouseMotionListener(mouseHandler);
         
         if(selectedLabel != null || showPeptid != 0 || showProtid != 0)
         {	
        
        	 
        	 if(showPeptid != 0)
        	 {
        		 System.out.println("Heyyyyyyyyyy"+showProtid);
        		  int Num = 0;
        		  int z =0;
        		 for(LightPeptideMatch p : pComponent.peptideSet)
       		  	{
        			  if(p.getId() == showPeptid){
       				 Num = z ;
       				  break ; }
        			  z++;
       		  	}
        		
             		
             		if(Num < rowCount)
             		{
             			for(int i = (int) (Num*columnCount); i < (Num+1)*columnCount; i++)
                     	{
                     		if(flagArray[i] == 1)  
                     			g2d.setColor(new Color(67,168,170));
                     		else
                     			g2d.setColor(Color.black);
                     		g2d.fill(cells.get(i));
                     		g2d.setColor(new Color(95,87,88));
                     		g2d.draw(cells.get(i));
                     		
                     	}
                     	
                     	 
                          g2d.setColor(new Color(95,158,160));
                          g2d.fill(rows.get(Num));
                          showPeptid = (long) 0;
             		}
                
        	 }
        	 else if(showProtid != 0)
        	 {
        		 System.out.println("Heyyyyyyyyyy"+showProtid+"ghjgfh");
        		 int Num1 = 0;
       		  	int z1 =0;
       		  	for(LightProteinMatch p : pComponent.proteinSet)
      		  	{
       			  if(p.getId() == showProtid){
      				 Num1 = z1 ;
      				  break ; }
       			  z1++;
      		  	}
       		
            		
            			for(int i = Num1; i < cells.size(); i=i+columnCount  )
                    	{
                    		if(flagArray[i] == 1)  
                    			g2d.setColor(new Color(67,168,170));
                    		else
                    			g2d.setColor(Color.black);
                    		g2d.fill(cells.get(i));
                    		g2d.setColor(new Color(95,87,88));
                    		g2d.draw(cells.get(i));
                    		
                    	}
                    	
                    	
                    	 
                         g2d.setColor(new Color(95,158,160));
                         g2d.fill(columns.get(Num1));
                         showProtid = (long) 0;
             
        	 }
        	 else
        	 {
        	 
        	String  id = "" ;
        	StringBuilder sb = new StringBuilder();
        	int labelNum = -1; 
        	
        	if(lStr == "Pept place")
        	{
        		
        		labelNum = (selectedLabel.y-finalYOffset)/finalCellHeight  ;
        		
        		if(labelNum < rowCount)
        		{
        			id = String.valueOf(pComponent.peptideSet.get(labelNum).getId());
        		
        			//g2d.setColor(new Color(134,180,96));
        			//g2d.setColor(new Color(67,168,170));
                    
                	for(int i = labelNum*columnCount; i < (labelNum+1)*columnCount; i++)
                	{
                		if(flagArray[i] == 1)  
                			g2d.setColor(new Color(67,168,170));
                		else
                			g2d.setColor(Color.black);
                		g2d.fill(cells.get(i));
                		g2d.setColor(new Color(95,87,88));
                		g2d.draw(cells.get(i));
                		
                	}
                	
                	 
                     g2d.setColor(new Color(95,158,160));
                     g2d.fill(rows.get(labelNum));
                     
        		}
        		else
        			labelNum = -1 ;
        	
        		
                sb.append("Peptide: ");
                sb.append(id);
                sb.append("   Score: ");
                sb.append(peptScore[labelNum]);
                sb.append("   Rank: ");
                sb.append(peptRank[labelNum]);
               
        	}
        	else if(lStr == "Prot place")
        	{
        		
        		labelNum = (selectedLabel.x-finalXOffset)/finalCellWidth  ;
        		
        		if(labelNum < columnCount)
        		{
        			id = String.valueOf(pComponent.proteinSet.get(labelNum).getId());
        			
        			//g2d.setColor(new Color(67,168,170));
                    
                	for(int i = labelNum; i < cells.size(); i=i+columnCount  )
                	{
                		if(flagArray[i] == 1)  
                			g2d.setColor(new Color(67,168,170));
                		else
                			g2d.setColor(Color.black);
                		g2d.fill(cells.get(i));
                		g2d.setColor(new Color(95,87,88));
                		g2d.draw(cells.get(i));
                		
                	}
                	
                	
  // imp##                   g2d.getFontMetrics().stringWidth(str)
                     g2d.setColor(new Color(95,158,160));
                     g2d.fill(columns.get(labelNum));
                     
                
        		}
        		else
        			System.out.println("Error getting index");
        		
                sb.append("Protein : ");
                sb.append(id);
                sb.append("   Score: ");
                sb.append(protScore[labelNum]);
        	}
        	else
        	{
        		//System.out.println("Error in label hover");
        	}
        	
        	idDisplay.setVisible(true);
        	idDisplay.setText(sb.toString());
             
         }
         }
        	
         if (selectedCell != null) {

             int index = selectedCell.x + (selectedCell.y * columnCount);
             
             
             Rectangle cell = cells.get(index);
             g2d.setColor(new Color(95,158,160));
             g2d.fill(cell);
             

             int protNum = index%columnCount , peptNum = index/columnCount ;
             String p1 = "Protein: ";
             String p2 = String.valueOf(pComponent.proteinSet.get(protNum).getId());
            // String p5 = " Score: ";
            // String p6 = String.valueOf(protScore[protNum]);
             String p3 ="   Peptide: ";
             String p4 = String.valueOf(pComponent.peptideSet.get(peptNum).getId()) ;
            // String p7 = " Score: ";
            // String p8 = String.valueOf(peptScore[peptNum]);
            
             
             g2d.setColor(new Color(95,158,160));
             g2d.fill(rows.get(peptNum));
             g2d.fill(columns.get(protNum));
             
             
             
             StringBuilder sb = new StringBuilder();
             sb.append("");
             sb.append(p1);
             sb.append(p2);
            // sb.append(p5);
            // sb.append(p6);
             sb.append(p3);
             sb.append(p4);
            // sb.append(p7);
            // sb.append(p8);
             String str = sb.toString();
             //);
             //text.concat("Peptide : ");
             //text.concat(String.valueOf(pComponent.peptideSet.get(peptNum).getId()));
             
             idDisplay.setVisible(true);
             
             idDisplay.setText(str);
             
          
         }
         
             
            g2d.dispose();
     }
     
     public void heighlightPeptide(Long id)
     {
    	showPeptid = id ;
   	 
     }
     public void heighlightProtein(Long id)
     {
    	 showProtid = id ;
    	
     }
     private void label_line(Graphics g, double x, double y, double theta, String label) {

         Graphics2D g2D = (Graphics2D)g;

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
        g2D.drawString(label, (int)x, (int)y);

        // put the original font back
        g2D.setFont(theFont);
    }}

 /*private Component getGlobalMatrix(ArrayList<Component> cList)
 {
     Component tempGlobal = new Component();
     
     int i = 0;
     
     for(Component temp : cList)
     {
         i++ ;
         tempGlobal.peptideSet.addAll(temp.peptideSet);
         tempGlobal.proteinSet.addAll(temp.proteinSet);
     
         if(i>15)
             break;
     }
     
     return tempGlobal ;
 }
 */

