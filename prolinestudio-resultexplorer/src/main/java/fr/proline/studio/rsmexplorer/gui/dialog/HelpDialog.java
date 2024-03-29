/* 
 * Copyright (C) 2019
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1 
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License 
 * along with this program; If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
 */
package fr.proline.studio.rsmexplorer.gui.dialog;

import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.parameter.BooleanParameter;
import fr.proline.studio.parameter.ParameterList;
import fr.proline.studio.utils.IconManager;
import fr.proline.studio.utils.HelpUtils;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.prefs.Preferences;
import javax.swing.*;
import javax.swing.tree.*;
import org.slf4j.LoggerFactory;
import fr.proline.studio.NbPreferences;

/**
 * Help Dialog with links to the how to sections
 *
 * @author JM235353
 */
public class HelpDialog extends DefaultDialog implements MouseListener, MouseMotionListener {

    private static HelpDialog m_singletonDialog = null;

    private JTree m_tree = null;
    private JCheckBox m_checkBox = null;

    private ParameterList m_parameterList;
    private static final String PARAMETER_LIST_NAME = "General Application Settings";
    private BooleanParameter m_parameter;

    private String[][] HELP_CONTENT = {
        {"Create a Project", "id.1302m92"},
        {"Create a Dataset", "id.3mzq4wv"},
        {"Import a Search Result", "id.319y80a"},
        {"Validate a Search Result", "id.42ddq1a"},
        {"Display Search Result Data", "id.184mhaj"},
        {"Display Identification Summary Data", "id.279ka65"},
        {"Create a Spectral Count", "id.2mn7vak"},
        {"Create a XIC", "id.3ls5o66"}

    };

    public static HelpDialog getDialog(Window parent) {
        if (m_singletonDialog == null) {
            m_singletonDialog = new HelpDialog(parent);
        }

        return m_singletonDialog;
    }

    public static boolean showAtStart() {
        Preferences preferences = NbPreferences.root();
        return !preferences.getBoolean("General_Application_Settings.Hide_Getting_Started_Dialog", Boolean.FALSE);
    }

    public HelpDialog(Window parent) {
        super(parent, Dialog.ModalityType.MODELESS);

        setTitle("Help");

        setSize(new Dimension(500, 340));
        setResizable(true);

        // hide default and ol button
        setButtonVisible(BUTTON_HELP, false);
        setButtonVisible(BUTTON_CANCEL, false);
        setButtonName(BUTTON_OK, "Close");
        setStatusVisible(false);

        // tree
        m_tree = new JTree(createTree());
        m_tree.setRowHeight(20);
        m_tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        m_tree.setCellRenderer(new HelpCellRenderer());
        m_tree.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        m_tree.addMouseListener(this);
        m_tree.addMouseMotionListener(this);

        JScrollPane scrollPane = new JScrollPane(m_tree);

        // Image at left
        JLabel leftImageLabel = new JLabel(IconManager.getIcon(IconManager.IconType.BIG_HELP));
        leftImageLabel.setBackground(Color.white);

        JPanel internalPanel = new JPanel(new GridBagLayout());
        internalPanel.setBackground(Color.white);
        internalPanel.setOpaque(true);
        internalPanel.setBorder(BorderFactory.createTitledBorder(""));

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.weighty = 1;
        c.gridx = 0;
        c.gridy = 0;
        internalPanel.add(leftImageLabel, c);

        c.weightx = 1;
        c.gridx++;
        internalPanel.add(scrollPane, c);

        c.gridy++;
        c.weighty = 0;
        internalPanel.add(this.createParameterPanel(), c);

        setInternalComponent(internalPanel);

    }

    private JPanel createParameterPanel() {
        JPanel panel = new JPanel();
        m_parameterList = new ParameterList(PARAMETER_LIST_NAME);
        JCheckBox gettingStartedCheckBox = new JCheckBox("Hide Getting Started Dialog On Startup");
        gettingStartedCheckBox.setBackground(Color.WHITE);
        m_parameter = new BooleanParameter("Hide_Getting_Started_Dialog", "Hide Getting Started Dialog On Startup", gettingStartedCheckBox, false);
        m_parameterList.add(m_parameter);
        m_parameterList.loadParameters(NbPreferences.root());
        panel = m_parameterList.getPanel();
        panel.setBackground(Color.WHITE);
        return panel;
    }

    @Override
    public void pack() {
        // forbid pack by overloading the method
    }

    private DefaultMutableTreeNode createTree() {
        DefaultMutableTreeNode top = new DefaultMutableTreeNode("Getting Started");

        for (int i = 0; i < HELP_CONTENT.length; i++) {
            top.add(new DefaultMutableTreeNode(new DocumentationInfo(HELP_CONTENT[i][0], HELP_CONTENT[i][1])));
        }
        return top;
    }

    @Override
    protected boolean okCalled() {

        saveCheckbox();

        return true;
    }

    @Override
    protected boolean cancelCalled() {

        saveCheckbox();

        return true;
    }

    private void saveCheckbox() {
        Preferences preferences = NbPreferences.root();
        m_parameterList.saveParameters(preferences);
    }

    private void checkMouseCursor(MouseEvent e) {
        if (isOverLink(e)) {
            m_tree.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        } else {
            m_tree.setCursor(Cursor.getDefaultCursor());
        }
    }

    private boolean isOverLink(MouseEvent e) {
        Point p = e.getPoint();
        int selRow = m_tree.getRowForLocation(p.x, p.y);

        if (selRow != -1) {
            TreePath path = m_tree.getPathForRow(selRow);
            Rectangle pathBounds = m_tree.getPathBounds(path);

            if (!pathBounds.contains(p)) {
                return false;
            }

            DefaultMutableTreeNode node = (DefaultMutableTreeNode) (DefaultMutableTreeNode) path.getLastPathComponent();

            Object useObject = node.getUserObject();
            if (useObject == null) {
                return false;
            }
            return (useObject instanceof DocumentationInfo);

        }
        return false;
    }

    @Override
    public void mouseClicked(MouseEvent e) {

        if (!isOverLink(e)) {
            return;
        }

        DefaultMutableTreeNode node = (DefaultMutableTreeNode) m_tree.getLastSelectedPathComponent();
        Object useObject = node.getUserObject();
        if (useObject instanceof DocumentationInfo) {
            DocumentationInfo helpInfo = (DocumentationInfo) useObject;
            String suffix = helpInfo.getSuffix();
            if (Desktop.isDesktopSupported()) { // JDK 1.6.0
                try {
                    Desktop.getDesktop().browse(HelpUtils.createRedirectTempFile(suffix));
                } catch (Exception ex) {
                    LoggerFactory.getLogger("ProlineStudio.ResultExplorer").error(getClass().getSimpleName() + " failed", ex);
                }
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        checkMouseCursor(e);
    }

    @Override
    public void mouseExited(MouseEvent e) {
        checkMouseCursor(e);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        checkMouseCursor(e);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        checkMouseCursor(e);
    }

    private class HelpCellRenderer extends DefaultTreeCellRenderer {

        private Font m_font = null;
        private Font m_fontBold = null;

        public HelpCellRenderer() {

        }

        @Override
        public Component getTreeCellRendererComponent(
                JTree tree,
                Object value,
                boolean sel,
                boolean expanded,
                boolean leaf,
                int row,
                boolean hasFocus) {

            JLabel l = (JLabel) super.getTreeCellRendererComponent(tree, value, false, expanded, leaf, row, hasFocus);

            if (m_font == null) {
                m_font = l.getFont().deriveFont(14);
                m_fontBold = m_font.deriveFont(Font.BOLD);
            }

            l.setBackground(Color.white); // discard selection visibility

            if (leaf) {
                l.setFont(m_font);
                l.setForeground(Color.blue);
                setIcon(IconManager.getIcon(IconManager.IconType.BOOK_QUESTION));
            } else {
                l.setFont(m_fontBold);
                l.setForeground(Color.black);

                if (expanded) {
                    setIcon(IconManager.getIcon(IconManager.IconType.BOOK_OPEN));
                } else {
                    setIcon(IconManager.getIcon(IconManager.IconType.BOOK));
                }
            }

            return this;
        }
    }

    private class DocumentationInfo {

        private String m_text;
        private String m_documentationSuffix;

        public DocumentationInfo(String text, String url) {
            m_text = text;
            m_documentationSuffix = url;
        }

        public String getSuffix() {
            return m_documentationSuffix;
        }

        @Override
        public String toString() {
            return m_text;
        }
    }

}
