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
import javax.swing.border.EtchedBorder;

/**
 *
 * @author CB205360
 */
public class WizardPanel extends JPanel {

    String m_title;
    String m_helpText;
    Icon m_icon;

    public WizardPanel() {
        this(null, null);
    }

    public WizardPanel(String title) {
        this(title, null);
    }

    public WizardPanel(String title, String helpText) {
        this(IconManager.getIcon(IconManager.IconType.WAND_HAT), title, helpText);
    }

    public WizardPanel(Icon icon, String title, String helpText) {
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
        String title = htmlSupportedTitle;
        String helpText = htmlSupportedHelpText;
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
        String htmlTitle = formatString(title, "black");
        String htmlHelp = formatString(helpText, "gray");

        String fontfamily = this.getFont().getFamily();
        int fontSize = this.getFont().getSize();
        String htmlText = "<!DOCTYPE html><html><head><style>div {"
                + "font-family: " + fontfamily + ";"
                + "font-size: " + fontSize + ";"
                + "padding-top: 0;"
                + "padding-bottom: 12px;"
                + "}</style></head><body>";
        htmlText += htmlTitle;
        htmlText += htmlHelp + "</body></html>";
        wizardPane.setText(htmlText);
        add(wizardPane, BorderLayout.CENTER);
        //this.setBorder(BorderFactory.createMatteBorder(5, 5, 5, 5, Color.LIGHT_GRAY));
        Border raisedbevel = BorderFactory.createRaisedBevelBorder();
        Border loweredbevel = BorderFactory.createLoweredBevelBorder();
        this.setBorder(BorderFactory.createCompoundBorder(raisedbevel, loweredbevel));
    }

    private String formatString(String txt, String color) {
        if (txt != null && !txt.isEmpty()) {
            String newTxt;
            if (txt.contains("<html>") || txt.contains("</html>")) {
                newTxt = txt.replaceAll("<html>", "");
                txt = newTxt.replaceAll("</html>", "");
            }
            return "<div style=\"color:" + color + ";\"><b>" + txt + "</b></div>";
        }
        return "";

    }
}
