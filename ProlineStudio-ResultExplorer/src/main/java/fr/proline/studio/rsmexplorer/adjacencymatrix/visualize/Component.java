/*	Defines structure of each component
 * */
package fr.proline.studio.rsmexplorer.adjacencymatrix.visualize;

import java.util.ArrayList;

import fr.proline.studio.dam.tasks.data.LightPeptideMatch;
import fr.proline.studio.dam.tasks.data.LightProteinMatch;

public class Component {

    public ArrayList<LightProteinMatch> proteinMatchArray = new ArrayList<>();
    public ArrayList<LightPeptideMatch> peptideArray = new ArrayList<>();

    public Component() {
        
    }
    
    
    public int getProteinSize() {
        return proteinMatchArray.size();
    }

    public int getPeptideSize() {
        return peptideArray.size();
    }

}
