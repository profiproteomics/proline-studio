package fr.proline.studio.rsmexplorer;


import fr.proline.studio.pattern.ParameterList;
import fr.proline.studio.pattern.WindowBox;
import fr.proline.studio.table.TableInfo;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class MultiDataBoxViewerTopPanel extends DataBoxViewerTopPanel {

    private WindowBox[] m_windowBoxes = null;

    public MultiDataBoxViewerTopPanel(WindowBox[] windowBoxes, String name) {
        super(windowBoxes[0]);
        m_windowBoxes = windowBoxes;
        removeAll();
        setLayout(new GridLayout());
        add(createPanel());
        setName(name);
    }


    private JPanel createPanel() {
        JPanel internalPanel = new JPanel(new GridLayout());

        JTabbedPane tabbedPane = new JTabbedPane();
        for (WindowBox wBox : m_windowBoxes) {
            ImageIcon boxIcon = null;
            if(wBox.getIcon() != null)
                boxIcon = new ImageIcon(wBox.getIcon());
            tabbedPane.addTab(wBox.getName(), boxIcon, wBox.getPanel(), null);
        }

        internalPanel.add(tabbedPane);

        return internalPanel;
    }

    @Override
    public void retrieveTableModels(ArrayList<TableInfo> list) {
        for (WindowBox wBox : m_windowBoxes) {
            wBox.retrieveTableModels(list);
        }
    }

    /*

    JPM.DOCK

    @Override
    protected void componentOpened() {
        for (WindowBox wBox : m_windowBoxes) {
            wBox.windowOpened();
        }
    }

    @Override
    protected void componentClosed() {
        for (WindowBox wBox : m_windowBoxes) {
            wBox.windowClosed();
        }
    }*/

    @Override
    public ParameterList getInParameters(){
        ParameterList inParameters = new ParameterList();
        for (WindowBox wBox : m_windowBoxes) {
            ParameterList windowInParameters = wBox.getEntryBox().getInParameters();
            inParameters.addParameter(windowInParameters);
        }
        return inParameters;
    }

    @Override
    public ParameterList getOutParameters(){
        ParameterList outParameters = new ParameterList();
        for (WindowBox wBox : m_windowBoxes) {
            ParameterList windowOutParameters = wBox.getEntryBox().getOutParameters();
            outParameters.addParameter(windowOutParameters);
        }
        return outParameters;
    }



}
