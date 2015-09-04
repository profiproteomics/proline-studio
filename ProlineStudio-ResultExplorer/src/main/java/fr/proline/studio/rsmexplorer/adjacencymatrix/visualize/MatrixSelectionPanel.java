package fr.proline.studio.rsmexplorer.adjacencymatrix.visualize;

import fr.proline.studio.dam.tasks.data.AdjacencyMatrixData;
import fr.proline.studio.gui.HourglassPanel;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxPanelInterface;
import fr.proline.studio.dam.tasks.data.LightPeptideMatch;
import fr.proline.studio.dam.tasks.data.LightProteinMatch;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;

/**
 *
 * @author JM235353
 */
public class MatrixSelectionPanel extends HourglassPanel implements DataBoxPanelInterface {
    
    private AbstractDataBox m_dataBox;
    
    private DrawVisualization m_drawVisualization = null; 
    
    private navigationData nData;
    
    private static HashMap<LightPeptideMatch, ArrayList<LightProteinMatch>> m_peptideToProteinMap ;
    private ArrayList<Component> cList1;
    
    private SearchEvent comboEvent;
    
    private JPanel cardPanel;
    private JTextField idDisplay;
    private JTextField idEntry;
    
    private static final String GLOBALPANEL = "Main Panel";
    
    public MatrixSelectionPanel() {
        setLoading(0);
        
        
    }
    
    private void initPanel() {
        
        nData = new navigationData();
        
        // search filter order
        m_peptideToProteinMap = m_drawVisualization.get_peptideToProteinMap();
        cList1 = filterComponents(m_drawVisualization.get_ComponentList());

        //Sort clist
        Collections.sort(cList1, new CustomComparator());
        
        
        
        setLayout(new BorderLayout());
        
        // create objects
        idDisplay = new JTextField();
        idDisplay.setHorizontalAlignment(SwingConstants.CENTER);
        idDisplay.setBackground(Color.white);
        idDisplay.setEditable(false);
        idDisplay.setVisible(false);
        
        
        JButton previousB = new JButton("Previous Frame");
        previousB.setEnabled(false);
        
        JButton nextB = new JButton("Next Frame");
        nextB.setEnabled(false);
        
        JButton mainPageButton = new JButton("Global view");

        cardPanel = new JPanel(new CardLayout());
        
        JPanel buttonPane = new JPanel();
        GroupLayout layout = new GroupLayout(buttonPane);
        buttonPane.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        JLabel label1 = new JLabel("Search:");
        String[] searchStrings = {"  -----", "Proteins", "Peptides"};
        
        ArrayList<Integer> layoutArray = new ArrayList<>() ;
        
        //Indices start at 0, so 4 specifies the pig.
        JComboBox<String> searchList = new JComboBox<>(searchStrings);
        searchList.setPrototypeDisplayValue("XXXXXXXXXX");
        searchList.setEditable(false);
        comboEvent = new SearchEvent();
        searchList.addItemListener(comboEvent);
        searchList.setAlignmentY(java.awt.Component.CENTER_ALIGNMENT);

        JLabel label2 = new JLabel("     ID :");
        idEntry = new JTextField(10);
        
        
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
                bTemp.addActionListener(new ZoomEvent(cList1.get(k), cardPanel, str, nData, previousB, nextB, idDisplay, m_drawVisualization));
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
        scroll.setName(GLOBALPANEL);
        cardPanel.add(scroll, GLOBALPANEL);

        
        
        
        
        java.awt.Component rigidArea = Box.createRigidArea(new Dimension(10, 10));
        layout.setHorizontalGroup(layout.createSequentialGroup()
                .addComponent(previousB)
                .addComponent(nextB)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED,
                        GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(idDisplay)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED,
                        GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(label1)
                        .addComponent(label2))
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(searchList, GroupLayout.PREFERRED_SIZE, 130,
                                GroupLayout.PREFERRED_SIZE)
                        .addComponent(idEntry, GroupLayout.PREFERRED_SIZE, 130,
                                GroupLayout.PREFERRED_SIZE))
                .addComponent(rigidArea)
                .addComponent(mainPageButton)
        );

        layout.setVerticalGroup(
                layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(previousB)
                        .addComponent(nextB)
                        .addComponent(idDisplay)
                        .addComponent(label1)
                        .addComponent(searchList, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                                GroupLayout.PREFERRED_SIZE)
                        .addComponent(rigidArea)
                        .addComponent(mainPageButton)
                )
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(label2)
                        .addComponent(idEntry, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                                GroupLayout.PREFERRED_SIZE)
                )
        );

        JPanel topPane = new JPanel();
        topPane.setLayout(new BoxLayout(topPane, BoxLayout.Y_AXIS));
        topPane.add(buttonPane);
        
        
        // add panels to the MatrixSelectionPanel
        add(cardPanel, BorderLayout.CENTER);
        add(topPane, BorderLayout.NORTH);
        
        
        // Actions
        mainPageButton.addActionListener(new GlobalViewEvent(cardPanel, nData, previousB, nextB, idDisplay));
        previousB.addActionListener(new navigateCards("previous", cardPanel, nData, previousB, nextB, idDisplay));
        nextB.addActionListener(new navigateCards("next", cardPanel, nData, previousB, nextB, idDisplay));

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

                        MatrixPanel cCard = new MatrixPanel(currentComponent, cardPanel, idDisplay, m_drawVisualization);

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

                        nData.previous.push(nData.currentCard);
                        nData.currentCard = str;
                    } else {
	            	 //"Item not found!");

                    }
                }
            }
        };
        idEntry.addActionListener(action);
        
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

}

