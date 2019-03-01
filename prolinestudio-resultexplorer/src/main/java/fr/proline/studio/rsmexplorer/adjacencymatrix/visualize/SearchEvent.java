package fr.proline.studio.rsmexplorer.adjacencymatrix.visualize;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JComboBox;

public class SearchEvent implements ItemListener {

	String updateLabel ;
	
	
	@Override
	public void itemStateChanged(ItemEvent e) {
		// TODO Auto-generated method stub
		@SuppressWarnings("unchecked")
		JComboBox<String> cb = (JComboBox<String>)e.getSource();
        String searchName = (String)cb.getSelectedItem();
        updateLabel = searchName;
	}


	public String getItem() {
		// TODO Auto-generated method stub
		return updateLabel;
	}

}
