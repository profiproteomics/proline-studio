package fr.proline.studio.filter;

import fr.proline.studio.utils.StringUtils;
import java.awt.GridBagConstraints;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.AbstractAction;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.text.JTextComponent;
import org.openide.util.Exceptions;

/**
 * Filter on String values with wildcards : * and ?
 *
 * @author JM235353
 */
public class StringFilter extends Filter {

    protected static final Integer SEARCH_TEXT = 0;
    protected String m_filterText = null;
    protected Pattern m_searchPattern;

    public StringFilter(String variableName, ConvertValueInterface convertValueInterface, int modelColumn) {
        super(variableName, convertValueInterface, modelColumn);
    }

    @Override
    public Filter cloneFilter() {
        StringFilter clone = new StringFilter(m_variableName, m_convertValueInterface, m_modelColumn);
        clone.m_filterText = m_filterText;
        clone.m_searchPattern = m_searchPattern;
        setValuesForClone(clone);
        return clone;
    }

    @Override
    public boolean filter(Object v1, Object v2) {
        if (m_filterText == null) {
            return true;
        }

        String value = (String) v1;

        Matcher matcher = m_searchPattern.matcher(value);

        return matcher.matches();
    }

    @Override
    public FilterStatus checkValues() {
        if (m_filterText == null) {
            return null;
        }

        try {
            StringUtils.compileRegex(m_filterText);
        } catch (Exception e) {
            return new FilterStatus("Regex Pattern Error", getComponent(SEARCH_TEXT));
        }

        return null;
    }

    @Override
    public boolean registerValues() {

        boolean hasChanged = false;

        if (isDefined()) {

            String lastValue = m_filterText;

            m_filterText = ((JTextField) getComponent(SEARCH_TEXT)).getText().trim();
            if (m_filterText.isEmpty()) {
                m_filterText = null;
            }

            m_searchPattern = StringUtils.compileRegex(m_filterText);

            hasChanged = (lastValue == null) || (m_filterText == null) || (lastValue.compareTo(m_filterText) != 0);
        }

        registerDefinedAsUsed();

        return hasChanged;
    }

    @Override
    public void createComponents(JPanel p, GridBagConstraints c) {
        c.gridx++;
        c.gridwidth = 3;
        c.weightx = 1;
        JLabel nameLabel = new JLabel(getName());

        p.add(nameLabel, c);

        c.gridx += 3;
        c.gridwidth = 1;
        c.weightx = 0;
        p.add(new JLabel("="), c);
        c.gridx++;
        c.gridwidth = 3;
        c.weightx = 1;
        p.add(createTextField(), c);

        c.gridx += 2;

    }

    protected JTextField createTextField() {
        JTextField vTextField = ((JTextField) getComponent(SEARCH_TEXT));
        if (vTextField == null) {
            vTextField = createPasteTextField(vTextField, m_filterText);
            registerComponent(SEARCH_TEXT, vTextField);
        }
        return vTextField;
    }

    @Override
    public void reset() {
        m_filterText = null;
    }

    /**
     * create a JTextField, which listen ctrl+V action, convert any html text to
     * plain text
     *
     * @param vTextField
     * @param filterText
     * @return
     */
    protected JTextField createPasteTextField(JTextField vTextField, String filterText) {
        //System.out.println("debug################### createPasteTextField in" );
        vTextField = new JTextField(8);
        vTextField.setToolTipText("<html>Search is based on wildcards:<br>  '*' : can replace all characters<br>  '?' : can replace one character<br><br>Use 'FOO*' to search a string starting with FOO. </html>");
        if (filterText != null) {
            vTextField.setText(filterText);
        }

        KeyStroke pasteKeyStrock = KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_MASK, false);

        vTextField.getInputMap().put(pasteKeyStrock, "paste");

        PasteAction pasteAction = new PasteAction(vTextField);
        vTextField.getActionMap().put("paste", pasteAction);
        //System.out.println("debug################### createPasteTextField out" );
        return vTextField;
    }

    protected class PasteAction extends AbstractAction {

        JTextComponent textComponent;

        public PasteAction(JTextComponent textComponent) {
            super();
            this.textComponent = textComponent;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            //System.out.println("debug################### createPasteTextField action in" );
            String msg = "";
            try {
                Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
                boolean b = c.isDataFlavorAvailable(DataFlavor.stringFlavor);
                if (b) {
                    msg = StringUtils.extractTextFromHtml(c.getData(DataFlavor.stringFlavor).toString());
                } else {
                    Transferable t = c.getContents(null);
                    msg = t.getTransferData(t.getTransferDataFlavors()[0]).toString();
                }

            } catch (UnsupportedFlavorException ex) {
                msg = "UnsupportedFlavorException";
                Exceptions.printStackTrace(ex);
            } catch (IOException ex) {
                msg = "IOException";
                Exceptions.printStackTrace(ex);
            }

            this.textComponent.setText(msg);
        }

    }

}
