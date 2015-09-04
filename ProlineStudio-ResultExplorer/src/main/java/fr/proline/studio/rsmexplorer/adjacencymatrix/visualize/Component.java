/*	Defines structure of each component
 * */

package fr.proline.studio.rsmexplorer.adjacencymatrix.visualize;

import java.util.ArrayList;

import fr.proline.studio.dam.tasks.data.LightPeptideMatch;
import fr.proline.studio.dam.tasks.data.LightProteinMatch;

public class Component {
	
	public ArrayList<LightProteinMatch> proteinSet = new ArrayList<>();
	public ArrayList<LightPeptideMatch> peptideSet = new ArrayList<>();
	
	public int getProteinSize() {
        return proteinSet.size();
    }

	public int getPeptideSize() {
        return peptideSet.size();
    }

}
