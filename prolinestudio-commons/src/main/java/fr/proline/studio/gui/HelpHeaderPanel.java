/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.gui;

import fr.proline.studio.utils.IconManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.border.Border;

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
        setLayout(new BorderLayout());
        setBackground(Color.white);
        /**
         * right icon image
         */
        JLabel wizardLabel = new JLabel(m_icon);
        wizardLabel.setPreferredSize(new Dimension(30, 30));
        add(wizardLabel, BorderLayout.LINE_START);

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
        add(wizardPane, BorderLayout.CENTER);
        Border raisedbevel = BorderFactory.createRaisedBevelBorder();
        Border loweredbevel = BorderFactory.createLoweredBevelBorder();
        this.setBorder(BorderFactory.createCompoundBorder(raisedbevel, loweredbevel));
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
