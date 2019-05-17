/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.gui;

import fr.proline.studio.utils.IconManager;

import java.awt.*;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;

/**
 *
 * @author CB205360
 */
public class HelpHeaderPanel extends JPanel {

    String m_title;
    String m_helpText;
    Icon m_icon;

    public HelpHeaderPanel() {
        this(null, null);
    }

    public HelpHeaderPanel(String title) {
        this(title, null);
    }

    public HelpHeaderPanel(String title, String helpText) {
        this(IconManager.getIcon(IconManager.IconType.WAND_HAT), title, helpText);
    }

    public HelpHeaderPanel(Icon icon, String title, String helpText) {
        super();
        m_title = title;
        m_helpText = helpText;
        m_icon = icon;
        initComponent();
    }

    private void initComponent() {
        if (m_title == null && m_helpText == null) {
            setVisible(false);
            return;
        }

        htmlWizard(m_title, m_helpText);
        setVisible(true);
    }

    /**
     * When we have helpText, show Html suppted JTextPane
     *
     * @param htmlSupportedTitle
     * @param htmlSupportedHelpText
     */
    public void htmlWizard(String htmlSupportedTitle, String htmlSupportedHelpText) {
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.NORTH, GridBagConstraints.NONE, null, 0,0);
        c.insets = new Insets(10,15,10,15);

        setBackground(Color.white);
        /**
         * right icon image
         */
        JLabel wizardLabel = new JLabel(m_icon);
        wizardLabel.setPreferredSize(new Dimension(30, 30));
        add(wizardLabel, c);

        c.gridx = 1;
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.weightx = 1;
        c.weighty = 1;
        c.insets = new Insets(5,5,10,5);

        JTextPane wizardPane = new JTextPane();
        wizardPane.setEditable(false);
        wizardPane.setContentType("text/html");
        String title = removeHtmlTag(htmlSupportedTitle);
        if (!title.isEmpty()) {
            title = "<div id=\"label\">" + title + "</div>";
        }
        String help = removeHtmlTag(htmlSupportedHelpText);
        if (!help.isEmpty()) {
            help = "<div id=\"help\">" + help + "</div>";
        }
        String fontFamily = this.getFont().getFamily();
        int fontSize = this.getFont().getSize();

        String html = String.format(htmlModel(), fontFamily, fontSize, fontFamily, fontSize, title, help);
        wizardPane.setText(html);
        add(wizardPane, c);
//        Border raisedbevel = BorderFactory.createRaisedBevelBorder();
//        Border loweredbevel = BorderFactory.createLoweredBevelBorder();
//        this.setBorder(BorderFactory.createCompoundBorder(raisedbevel, loweredbevel));
        this.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
    }

    private String htmlModel() {
        return "<html> \n"
                + "   <head>\n"
                + "      <style type=\"text/css\">  \n"
                + "         #label{\n"
                + "         color: black; \n"
                + "         font-family: %s;\n"
                + "         font-weight: bold;\n"
                + "         font-size: %d;\n"
                + "         padding-top: 3px;\n"
                + "         padding-bottom: 3px; \n"
                + "         } \n"
                + "         #help{\n"
                + "         color: Gray; \n"
                + "         font-family: %s;\n"
                + "         font-size: %d;\n"
                + "         padding-top: 6px;\n"
                + "         padding-bottom: 6px;\n"
                + "         } \n"
                + "      </style> \n"
                + "   </head> \n"
                + "   <body>\n"
                + "	%s <!-- title --> \n"               
                + "	%s <!-- help text -->\n"
                + "   </body> \n"
                + "</html>";
    }

    private String removeHtmlTag(String txt) {
        if (txt != null && !txt.isEmpty()) {
            if (txt.contains("<html>") || txt.contains("</html>")) {
                String newTxt = txt.replaceAll("<html>", "").replaceAll("</html>", "");
                return newTxt;
            }
            return txt;
        }
        return "";
    }

}
