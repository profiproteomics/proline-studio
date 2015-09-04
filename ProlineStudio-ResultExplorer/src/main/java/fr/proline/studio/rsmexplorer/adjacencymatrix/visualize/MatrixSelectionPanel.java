package fr.proline.studio.rsmexplorer.adjacencymatrix.visualize;

import fr.proline.studio.dam.tasks.data.AdjacencyMatrixData;
import fr.proline.studio.gui.HourglassPanel;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxPanelInterface;
import fr.proline.studio.dam.tasks.data.LightPeptideMatch;
import fr.proline.studio.dam.tasks.data.LightProteinMatch;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 *
 * @author JM235353
 */
public class MatrixSelectionPanel extends HourglassPanel implements DataBoxPanelInterface {
    
    private AbstractDataBox m_dataBox;
    
    private DrawVisualization m_drawVisualization = null; 

    
    private static HashMap<LightPeptideMatch, ArrayList<LightProteinMatch>> m_peptideToProteinMap ;
    private ArrayList<Component> cList1;
    
    private SearchEvent comboEvent;

    
    private Component m_currentComponent = null;

    public MatrixSelectionPanel() {
        setLoading(0);
        
        
    }
    
    private void initPanel() {

        // search filter order
        m_peptideToProteinMap = m_drawVisualization.get_peptideToProteinMap();
        cList1 = filterComponents(m_drawVisualization.get_ComponentList());

        //Sort clist
        Collections.sort(cList1, new CustomComparator());
        
        
        
        setLayout(new BorderLayout());
        
        // create objects

        ArrayList<Integer> layoutArray = new ArrayList<>() ;
        
        //Indices start at 0, so 4 specifies the pig.



        
        
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int fWidth = screenSize.width - 20;
        int fHight = screenSize.height;
        
        int tWidth = 0;
        layoutArray.add(0);
        for (int i = 0; i < cList1.size(); i++) {
            tWidth = tWidth + cList1.get(i).proteinSet.size() * 3 + 10;

            if (tWidth < fWidth) {
                int index = layoutArray.size() - 1;
                int tempValue = layoutArray.get(index);
                tempValue++;
                layoutArray.set(index, tempValue);
            } else {
                tWidth = cList1.get(i).getPeptideSize() + 10;
                layoutArray.add(1);
            }

        }

        // Drawing layout  
        JPanel jCol = new JPanel();
        jCol.setLayout(new BoxLayout(jCol, BoxLayout.Y_AXIS));

        jCol.add(Box.createRigidArea(new Dimension(0, 10)));

        int k = 0;
        int length = 0;
        for (int i = 0; i < layoutArray.size(); i++) {
            JPanel jRow = new JPanel();
            jRow.setLayout(new BoxLayout(jRow, BoxLayout.X_AXIS));
            jRow.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
            Dimension d = new Dimension(fWidth + 10, ((cList1.get(k).peptideSet.size() * 3) + 10));
            jRow.setMinimumSize(d);

            jRow.add(Box.createVerticalGlue());
            length = length + d.height + 10;

            for (int j = 0; j < layoutArray.get(i); j++) {
                //    jRow.add(pList.get(k));
                JButton bTemp = new MatrixImageButton(j, cList1.get(k), m_drawVisualization);
                StringBuilder sb = new StringBuilder();
                sb.append("");
                sb.append(k);
                String str = sb.toString();
                final Component _c = cList1.get(k);
                bTemp.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        setCurrentComponent(_c);
                        m_dataBox.propagateDataChanged(Component.class);
                    }
                    
                });

                jRow.add(bTemp);

                jRow.add(Box.createRigidArea(new Dimension(8, 0)));
                k++;
            }
            jRow.setBackground(Color.WHITE);
            jCol.add(jRow);
            jCol.add(Box.createRigidArea(new Dimension(0, 10)));

        }

        Dimension dCol = new Dimension(screenSize.width, length + 30);
        jCol.setMinimumSize(dCol);
        jCol.setPreferredSize(dCol);

        jCol.setBackground(Color.WHITE);

        JScrollPane scroll = new JScrollPane(jCol);

  
        
        // add panels to the MatrixSelectionPanel
        add(scroll, BorderLayout.CENTER);





    }
    
    public DrawVisualization getDrawVisualization() {
        return m_drawVisualization;
    }
    
    public void setData(AdjacencyMatrixData matrixData) {
        
        m_drawVisualization = new DrawVisualization();
        
        m_drawVisualization.setData(matrixData);

        initPanel();
        
        setLoaded(0);
        
        revalidate(); 
        repaint();
    }
    
    private ArrayList<Component> filterComponents(ArrayList<Component> cList) {
        ArrayList<Component> subCList = new ArrayList<>();
        for (Component temp : cList) {
            if (temp.peptideSet.size() > 1 && temp.proteinSet.size() > 1) {
                if (!fullMatch(temp)) {
                    subCList.add(temp);
                }
            }
        }

        return subCList;
    }
    
    private boolean fullMatch(Component temp) {
        for (LightPeptideMatch peptTemp : temp.peptideSet) {
            ArrayList<LightProteinMatch> protList = m_peptideToProteinMap.get(peptTemp);
            for (LightProteinMatch protTemp : temp.proteinSet) {
                if (!protList.contains(protTemp)) {
                    return false;
                }
            }

        }
        return true;
    }
    
    public void setCurrentComponent(Component c) {
        m_currentComponent = c;
    }
    
    public Component getCurrentComponent() {
        return m_currentComponent;
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

    


    public class CustomComparator implements Comparator<Component> {

        @Override
        public int compare(Component o1, Component o2) {

            if (o1 == null || o2 == null) {
                throw new NullPointerException();
            }

            return (o2.getPeptideSize() * o2.getProteinSize()) - (o1.getPeptideSize() * o1.getProteinSize());
        }
    }

    
    /* OLD SEARCH EVENT 
    
            Action action = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Component currentComponent = null;
                String searchItem = comboEvent.getItem();
                long id;

              //System.out.println("some action"+searchItem);
                if (searchItem == null) {
                } else {
                    if (searchItem.compareToIgnoreCase("Peptides")==0) {
                        String idPept = idEntry.getText();

                        try {
                            id = Long.valueOf(idPept);
                            for (Component c : cList1) {
                                for (LightPeptideMatch p : c.peptideSet) {
                                    if (p.getId() == id) {
                                        currentComponent = c;
                                        break;
                                    } else {
                                    }
                                }
                            }

                        } catch (NumberFormatException ex) {

                        }

                        if (currentComponent == null) {
                            idEntry.setText("Peptide not found !");
		            	 // idEntry.sett

                        }
                    } else if (searchItem.compareToIgnoreCase("Proteins")==0) {
                        String idProt = idEntry.getText();

                        try {
                            id = Long.valueOf(idProt);

                            for (Component c : cList1) {
                                for (LightProteinMatch p : c.proteinSet) {
                                    if (p.getId() == id) {
                                        currentComponent = c;

                                        break;
                                    }
                                }
                            }
                        } catch (NumberFormatException ex) {
                        }

                        if (currentComponent == null) {
                            idEntry.setText("Protine not found !");
                        }
                    }

                    if (currentComponent != null) {
                        int componentSearchIndex = cList1.indexOf(currentComponent);

                        StringBuilder sb = new StringBuilder();
                        sb.append("");
                        sb.append(componentSearchIndex);
                        String str = sb.toString();

                        MatrixPanel cCard = new MatrixPanel();
                        cCard.setData(currentComponent, idDisplay, m_drawVisualization);

                        if (searchItem.compareToIgnoreCase("Proteins")==0) {
                            String idProt = idEntry.getText();
                            id = Long.valueOf(idProt);
                            cCard.heighlightProtein(id);

                        } else if (searchItem.compareToIgnoreCase("Peptides")==0) {
                            String idPept = idEntry.getText();
                            id = Long.valueOf(idPept);
                            cCard.heighlightPeptide(id);
                        }

                        cardPanel.add(cCard, str);
                        CardLayout cl = (CardLayout) (cardPanel.getLayout());
                        cl.show(cardPanel, str);

                    } else {
	            	 //"Item not found!");

                    }
                }
            }
        };
    */
    
}

