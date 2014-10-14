package fr.proline.studio.rsmexplorer.gui.dialog;

import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.utils.IconManager;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.net.URL;
import java.util.prefs.Preferences;
import javax.swing.*;
import javax.swing.tree.*;
import org.slf4j.LoggerFactory;
import org.openide.util.NbPreferences;

/**
 * Help Dialog with links to the how to sections
 *
 * @author JM235353
 */
public class HelpDialog extends DefaultDialog implements MouseListener, MouseMotionListener {

    private static HelpDialog m_singletonDialog = null;
    
    private JTree m_tree = null;
    private JCheckBox m_checkBox = null;
    
    private String[][] HELP_CONTENT = {
        { "Create a Project", "http://biodev.extra.cea.fr/docs/proline/doku.php?id=how_to:studio:createproject" },
        { "Create a Dataset", "http://biodev.extra.cea.fr/docs/proline/doku.php?id=how_to:studio:createaggregate" },
        { "Import a Search Result", "http://biodev.extra.cea.fr/docs/proline/doku.php?id=how_to:studio:importmascot" },
        { "Validate a Search Result", "http://biodev.extra.cea.fr/docs/proline/doku.php?id=how_to:studio:rsvalidation" },
        { "Display Search Result Data", "http://biodev.extra.cea.fr/docs/proline/doku.php?id=how_to:studio:viewpsm" },
        { "Display Identification Summary Data", "http://biodev.extra.cea.fr/docs/proline/doku.php?id=how_to:studio:viewprotset" },
        { "Create a Spectral Count", "http://biodev.extra.cea.fr/docs/proline/doku.php?id=how_to:studio:spectralcount" },
        { "Create a XIC", "http://biodev.extra.cea.fr/docs/proline/doku.php?id=how_to:studio:xic" }
  
    };
    
    public static HelpDialog getDialog(Window parent) {
        if (m_singletonDialog == null) {
            m_singletonDialog = new HelpDialog(parent);
        }

        return m_singletonDialog;
    }

    public static boolean showAtStart() {
        Preferences preferences = NbPreferences.root();
        return ! preferences.getBoolean("Hide_Getting_Started_Dialog", Boolean.FALSE);
    }
    
    public HelpDialog(Window parent) {
        super(parent, Dialog.ModalityType.MODELESS);

        setTitle("Help");

        setSize(new Dimension(500,340));
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

        // Checkbox
        m_checkBox = new JCheckBox("Do not show this help again at start");
        m_checkBox.setBackground(Color.white);
        Preferences preferences = NbPreferences.root();
        boolean selected = preferences.getBoolean("Hide_Getting_Started_Dialog", Boolean.FALSE);
        m_checkBox.setSelected(selected);
        
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
        internalPanel.add(m_checkBox, c);
        
        
        setInternalComponent(internalPanel);

    }



    
    
    @Override
    public void pack() {
        // forbid pack by overloading the method
    }
    
    private DefaultMutableTreeNode createTree() {
        DefaultMutableTreeNode top = new DefaultMutableTreeNode("Getting Started");

        for (int i=0;i<HELP_CONTENT.length;i++) {
            top.add(new DefaultMutableTreeNode(new HelpInfo(HELP_CONTENT[i][0], HELP_CONTENT[i][1])));
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
        preferences.putBoolean("Hide_Getting_Started_Dialog", m_checkBox.isSelected());
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
            
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) (DefaultMutableTreeNode)path.getLastPathComponent();

            Object useObject = node.getUserObject();
            if (useObject == null) {
                return false;
            }
            return (useObject instanceof HelpInfo);

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
        if (useObject instanceof HelpInfo) {
            HelpInfo helpInfo = (HelpInfo) useObject;
            String url = helpInfo.getUrl();
            if (Desktop.isDesktopSupported()) { // JDK 1.6.0

                try {
                    Desktop.getDesktop().browse(new URL(url).toURI());
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

            JLabel l = (JLabel) super.getTreeCellRendererComponent(tree, value, false, expanded, leaf, row,  hasFocus);


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
    
    
    private class HelpInfo {
        
        private String m_text;
        private String m_url;
        
        public HelpInfo(String text, String url) {
            m_text = text;
            m_url = url;
        }
        
        public String getUrl() {
            return m_url;
        }
        
        @Override
        public String toString() {
            return m_text;
        }
    }
    
    
    
}
