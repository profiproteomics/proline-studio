/* Computes the Connected Components from the data.
 * Using Peptide->protein EdgeSet to find Connected Components
*/
package fr.proline.studio.rsmexplorer.adjacencymatrix.visualize;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import fr.proline.studio.dam.tasks.data.LightPeptideMatch;
import fr.proline.studio.dam.tasks.data.LightProteinMatch;

public class ConnectedComponents {


	public ArrayList<LightProteinMatch> m_proteins = new ArrayList<>() ;
	public ArrayList<LightPeptideMatch> m_peptides = new ArrayList<>() ;
	public HashMap<LightProteinMatch, ArrayList<LightPeptideMatch>> m_proteinToPeptideMap = new HashMap<>() ;
	public HashMap<LightPeptideMatch, ArrayList<LightProteinMatch>> m_peptideToProteinMap = new HashMap<>() ;

	public ArrayList<fr.proline.studio.rsmexplorer.adjacencymatrix.visualize.Component> componentSet = new ArrayList<>() ; 
	int numberOfComponents = 0 ;
	int maxIndex = 0, maxSize = 0 ;
	
	
	public ConnectedComponents(ArrayList<LightProteinMatch> proteins, ArrayList<LightPeptideMatch> peptides, HashMap<LightProteinMatch, ArrayList<LightPeptideMatch>> proteinToPeptideMap, HashMap<LightPeptideMatch, ArrayList<LightProteinMatch>> peptideToProteinMap)
	{
		m_proteins = proteins ;
		m_peptides = peptides ;
		m_proteinToPeptideMap = proteinToPeptideMap ;
		m_peptideToProteinMap = peptideToProteinMap ;
	}
	
	public int getLargestComponent()
	{
		return maxIndex ;
	}
		
	public ArrayList<Component> getConnectedComponents()
	{		
	
		int proteinIndex = 0, peptideIndex = 0, componentIndex = 0 ;
		int check1=0 , check2=0 ;	
		
		int proteinSize = m_proteins.size() ;
		int peptideSize = m_peptides.size();
		
		int[] proteinFlag = new int[proteinSize];
		int[] peptideFlag = new int[peptideSize];

		Queue<LightProteinMatch> proteinQueue = new LinkedList<LightProteinMatch>();
		Queue<LightPeptideMatch> peptideQueue = new LinkedList<LightPeptideMatch>();
		
		//System.out.println("ABC" + m_proteinToPeptideMap.size());
		
		if(m_proteins == null && m_peptides == null)
		{
			System.out.println("Empty data");
			componentSet = null;
			
			return componentSet ;
		}
		
		
			LightProteinMatch proteinTemp = m_proteins.get(0) ;
			LightPeptideMatch peptideTemp = m_peptides.get(0);
		
			//System.out.println("---AB---");
			
		proteinQueue.add(m_proteins.get(0)) ;
		proteinFlag[0] = 1 ;
		
		componentIndex = -1 ;
		
		while((proteinIndex < proteinSize) && (peptideIndex < peptideSize))
		{
			componentIndex ++ ;
			Component componentNull = new Component();
			componentSet.add(componentNull) ;
			
			while(!(proteinQueue.isEmpty() && peptideQueue.isEmpty()))
			{
				
				while(!proteinQueue.isEmpty())
				{
					
					proteinTemp = proteinQueue.remove(); 
					ArrayList<LightPeptideMatch> peptideList = new ArrayList<>() ;
					peptideList = m_proteinToPeptideMap.get(proteinTemp) ;
					
					for( LightPeptideMatch temp1 : peptideList)
					{
							int tempIndex = m_peptides.indexOf(temp1);
							if(peptideFlag[tempIndex] == 0)
							{
								peptideQueue.add(temp1);
								peptideFlag[tempIndex] = 1 ;
							}				
					}
	
					componentSet.get(componentIndex).proteinSet.add(proteinTemp) ;
				
				}
				
				while(!peptideQueue.isEmpty())
				{
					peptideTemp = peptideQueue.remove();
					 ArrayList<LightProteinMatch> proteinList = new ArrayList<>() ;
					proteinList = m_peptideToProteinMap.get(peptideTemp) ;
					
					for( LightProteinMatch temp2 : proteinList)
					{
							int tempIndex = m_proteins.indexOf(temp2);
							if(proteinFlag[tempIndex] == 0)
							{
								proteinQueue.add(temp2);
								proteinFlag[tempIndex] = 1 ;
							}				
					}
					
					componentSet.get(componentIndex).peptideSet.add(peptideTemp) ;				
				
				 }
						
		}
			
			int tempCheck1  = componentSet.get(componentIndex).getProteinSize() ;
			int tempCheck2 = componentSet.get(componentIndex).getPeptideSize() ;
			System.out.println("ComponentIndex:"+componentIndex+"   Protein array size: " + componentSet.get(componentIndex).getProteinSize()+" Peptide array size: " + componentSet.get(componentIndex).getPeptideSize() );
			check1 = check1 + componentSet.get(componentIndex).getProteinSize() ;
			check2 = check2 + componentSet.get(componentIndex).getPeptideSize() ;
	
			if(tempCheck1 + tempCheck2 > maxSize)
			{
				maxIndex = componentIndex ;
				maxSize = tempCheck1 + tempCheck2 ;
			}	
			
			
			if(proteinIndex < proteinSize)
			{	
				int i;
				for( i = proteinIndex; i < proteinSize ; i++)
				{
					if(proteinFlag[i] == 0)
					{
						proteinQueue.add(m_proteins.get(i)) ;
						proteinFlag[i] = 1 ;
						break;
					}	
				}
				
				proteinIndex = i ;
			}
			else
			{
				int j ;
				for( j = peptideIndex; j < peptideSize ; j++)
				{
					if(peptideFlag[j] == 0)
					{
						peptideQueue.add(m_peptides.get(j)) ;
						peptideFlag[j] = 1 ;
						break;
					}	
				}
				
				peptideIndex = j ;
			}
		
		

		}
		
		System.out.println("Protine total: "+check1+" Peptide total: "+check2);
		return componentSet ;	
	}
	}
