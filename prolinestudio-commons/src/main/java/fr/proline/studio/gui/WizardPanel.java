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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

/**
 *
 * @author CB205360
 */
public class WizardPanel extends JPanel {

    public WizardPanel(String title) {
        this(title, null);
    }

    public WizardPanel(String title, String helpText) {
        super();
        if (helpText != null && !helpText.isEmpty()) {
            htmlWizard(title, helpText);

        } else {
            setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.anchor = GridBagConstraints.NORTHWEST;
            c.fill = GridBagConstraints.BOTH;
            c.insets = new java.awt.Insets(5, 15, 5, 15);

            JLabel wizardLabel = new JLabel(title);
            wizardLabel.setIcon(IconManager.getIcon(IconManager.IconType.WAND_HAT));

            c.gridx = 0;
            c.gridy = 0;
            c.weightx = 1;
            c.weighty = 1;
            add(wizardLabel, c);
        }
    }

    /**
     * When we have helpText, show Html suppted JTextPane
     *
     * @param title
     * @param helpText
     */
    public void htmlWizard(String title, String helpText) {
        setLayout(new BorderLayout());
        /**
         * right icon image
         */
        JLabel wizardLabel = new JLabel(IconManager.getIcon(IconManager.IconType.WAND_HAT));
        wizardLabel.setPreferredSize(new Dimension(30, 30));
        add(wizardLabel, BorderLayout.LINE_START);

        JTextPane wizardPane = new JTextPane();
        wizardPane.setEditable(false);
        wizardPane.setContentType("text/html");
        String newTitle;
        if (title.contains("<html>") || title.contains("</html>")) {
            newTitle = title.replaceAll("<html>", "");
            title = newTitle.replaceAll("</html>", "");
        }
        String htmlText = "<!DOCTYPE html><html><head><style>div {"
                + "padding-top: 0;"
                + "padding-bottom: 12px;"
                + "}</style></head><body>";
        htmlText += "<div style=\"color:blue;\"><b>" + title + "</b></div>";
        htmlText += "<div style=\"color:black;\">" + helpText + "</div></body></html>";
        wizardPane.setText(htmlText);
        add(wizardPane, BorderLayout.CENTER);
        this.setBorder(BorderFactory.createMatteBorder(5, 5, 5, 5, Color.LIGHT_GRAY));
    }
}
