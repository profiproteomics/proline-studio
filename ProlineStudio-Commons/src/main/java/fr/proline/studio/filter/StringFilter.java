package fr.proline.studio.filter;

import static fr.proline.studio.filter.StringDiffFilter.SEARCH_TEXT_AREA;
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
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.AbstractAction;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.text.JTextComponent;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Filter on String values with wildcards : * and ?
 *
 * @author JM235353
 */
public class StringFilter extends Filter {

    protected static final Logger logger = LoggerFactory.getLogger("ProlineStudio.Commons");
    protected static final Integer SEARCH_TEXT = 0;
    protected static final Integer SEARCH_TEXT_AREA = 1;
    protected static final String OPTION_EQUAL = "=";
    protected static final String OPTION_NOT = "!=";
    protected static final String OPTION_IN = "IN";
    protected String m_filterText;
    protected Pattern m_searchPattern;
    protected String m_selectItem;
    protected JComboBox m_cbOp;
    protected JTextField m_field;
    protected JScrollPane m_area;
    protected String m_filterAreaText;
    protected ArrayList<Pattern> m_searchPatternList;
    protected String[] m_optionList;

    public StringFilter(String variableName, ConvertValueInterface convertValueInterface, int modelColumn) {
        super(variableName, convertValueInterface, modelColumn);
        m_optionList = new String[2];
        m_optionList[0] = OPTION_EQUAL;
        m_optionList[1] = OPTION_IN;
        m_selectItem = OPTION_EQUAL;
        m_cbOp = null;
        m_field = null;
        m_area = null;
        m_filterText = "";
        m_filterAreaText = "";
        m_searchPattern = null;
        m_searchPatternList = null;
    }

    /**
     * clone is used for search the "IN" option is not include.
     *
     * @return
     */
    @Override
    public Filter cloneFilter4Search() {
        StringFilter clone = new StringFilter(m_variableName, m_convertValueInterface, m_modelColumn);
        clone.m_optionList = new String[1];
        clone.m_optionList[0] = OPTION_EQUAL;
        //clone.m_optionList[1] = OPTION_IN; search has not multi chose
        //next attributs  have not been created
        clone.reset();
        setValuesForClone(clone);
        clone.m_filterText = m_filterText;
        return clone;
    }

    @Override
    public boolean filter(Object v1, Object v2) {
        String value = (String) v1;
        boolean found = false;
        switch (m_selectItem) {
            case OPTION_EQUAL:
                if (m_filterText.length() == 0) {
                    return true;
                }
                Matcher matcher = m_searchPattern.matcher(value);
                found = matcher.matches();
                return found;

            case OPTION_IN:
                if (m_filterAreaText.length() == 0) {
                    return true;
                }
                found = false;
                for (Pattern rx : m_searchPatternList) {
                    if (rx.matcher(value).matches()) {
                        return true; //out at the first matche
                    }
                }
                return false;
        }
        return found;
    }

    @Override
    public FilterStatus checkValues() {
        String selectItem = m_cbOp.getSelectedItem().toString();
        String stringF = m_field.getText().trim();
        String stringA = ((JTextArea) this.m_area.getViewport().getView()).getText();
        try {
            if (!selectItem.equals(OPTION_IN)) {
                if (stringF.length() == 0) {
                    return null;
                }
                m_searchPattern = StringUtils.compileRegex(stringF);
            } else {
                if (stringA.length() == 0) {
                    return null;
                }
                String delims = "[\n]+";//each line separator 
                String[] filterAList = stringA.split(delims);
                for (String s : filterAList) {
                    StringUtils.compileRegex(s);
                }
            }
        } catch (Exception e) {
            return new FilterStatus("Regex Pattern Error", getComponent(SEARCH_TEXT));
        }

        return null;
    }

    /**
     * register values from GUI, transfer it as regular expression then pack it
     * as Pattern
     *
     * @return
     */
    @Override
    public boolean registerValues() {
        boolean hasChanged = false;
        if (isDefined()) {
            String lastValue;
            String lastSelItem = m_selectItem;
            m_selectItem = m_cbOp.getSelectedItem().toString();
            if (!m_selectItem.equals(OPTION_IN)) {
                lastValue = m_filterText;
                m_filterText = m_field.getText().trim();
                m_searchPattern = StringUtils.compileRegex(m_filterText);
                hasChanged = (lastValue.compareTo(m_filterText) != 0) || (lastSelItem != m_selectItem);
            } else {
                lastValue = m_filterAreaText;
                m_filterAreaText = ((JTextArea) this.m_area.getViewport().getView()).getText().trim();
                String delims = "[\n]+";//each line separator 
                String[] m_filterStringList = m_filterAreaText.split(delims);
                m_searchPatternList = new ArrayList<Pattern>();
                for (String m_filterStringList1 : m_filterStringList) {
                    m_searchPatternList.add(StringUtils.compileRegex(m_filterStringList1));
                }
                hasChanged = (lastValue.compareTo(m_filterAreaText) != 0) || (lastSelItem != m_selectItem);
            }
        }

        registerDefinedAsUsed();

        return hasChanged;
    }

    @Override
    public void createComponents(JPanel p, GridBagConstraints c) {
        FilterDialog ctrlDialog = FilterDialog.getDialog(WindowManager.getDefault().getMainWindow());

        JLabel nameLabel = new JLabel(getName());

        m_cbOp = new JComboBox(m_optionList);
        m_cbOp.setSelectedItem(m_selectItem);
        m_cbOp.addActionListener(new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                JComboBox<String> combo = (JComboBox<String>) e.getSource();
                String selectItem = (String) combo.getSelectedItem();
                if (selectItem.equals(OPTION_IN) & !m_selectItem.equals(OPTION_IN)) {
                    m_field.setVisible(false);
                    m_area.setVisible(true);
                    m_selectItem = selectItem;//must do before dialog.repack();
                    if (ctrlDialog != null) {
                        ctrlDialog.repack();
                    }

                } else if (m_selectItem.equals(OPTION_IN) & !selectItem.equals(OPTION_IN)) {
                    m_field.setVisible(true);
                    m_area.setVisible(false);
                    m_selectItem = selectItem;//must do before dialog.repack();
                    if (ctrlDialog != null) {
                        ctrlDialog.repack();
                    }
                }

            }
        });

        m_field = ((JTextField) getComponent(SEARCH_TEXT));
        if (m_field == null) {
            m_field = createPasteTextField(m_filterText);
            registerComponent(SEARCH_TEXT, m_field);
        }
        m_area = ((JScrollPane) getComponent(SEARCH_TEXT_AREA));
        if (m_area == null) {
            JTextArea textArea = createPasteArea(m_filterAreaText);
            m_area = new JScrollPane(textArea);
            registerComponent(SEARCH_TEXT_AREA, m_area);
        }

        c.gridx++;
        JPanel textPane = new JPanel();
        GridBagConstraints c1 = new GridBagConstraints();
        c1.anchor = GridBagConstraints.NORTHWEST;
        c1.fill = GridBagConstraints.BOTH;
        c1.gridx = 0;
        c1.gridy = 0;
        c1.weightx = 1;
        c1.gridwidth = 2;
        textPane.add(nameLabel, c);

        c1.gridx += 2;
        c1.gridwidth = 1;
        c1.weightx = 0;
        textPane.add(m_cbOp, c1);
        c1.gridx++;

        c1.weightx = 1;
        c1.gridwidth = 6;
        textPane.add(m_field, c1);
        textPane.add(m_area, c1);
        c.gridwidth = 9;
        c.weightx = 1;
        p.add(textPane, c);
        c.gridx += 9;

        if (m_selectItem.equals(OPTION_IN)) {
            m_field.setVisible(false);
            m_area.setVisible(true);
        } else {
            m_field.setVisible(true);
            m_area.setVisible(false);
        }

    }
    
    @Override
    public void reset() {
        m_selectItem = OPTION_EQUAL;
        m_cbOp = null;
        m_filterText = "";
        m_filterAreaText = "";
        m_field = null;
        m_area = null;
        m_searchPattern = null;
        m_searchPatternList = null;
    }

    @Override
    public void clearComponents() {
        if (m_components == null) {
            return;
        }
        m_components.clear();
    }

    /**
     * create a JTextField, which listen ctrl+V action, convert any html text to
     * plain text
     *
     * @param filterText
     * @return
     */
    protected JTextField createPasteTextField(String filterText) {
        JTextField vTextField = new JTextField(12);
        String msg = NbBundle.getMessage(this.getClass(),"Filter_String_RegularExpression.tooltip.text");
        vTextField.setToolTipText(msg);
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

    protected JTextArea createPasteArea(String filterAreaText) {
        JTextArea vArea = new JTextArea(5, 20);
        String msg = NbBundle.getMessage(this.getClass(),"Filter_String_RegularExpression.tooltip.text");
        vArea.setToolTipText(msg);
        if (filterAreaText != null) {
            vArea.setText(filterAreaText);
        }
        KeyStroke pasteKeyStrock = KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_MASK, false);
        vArea.getInputMap().put(pasteKeyStrock, "paste");
        PasteAction pasteAction = new PasteAction(vArea);
        vArea.getActionMap().put("paste", pasteAction);
        return vArea;
    }

    protected class PasteAction extends AbstractAction {

        JTextComponent textComponent;

        public PasteAction(JTextComponent textComponent) {
            super();
            this.textComponent = textComponent;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String msg = "";
            try {
                Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
                boolean b = c.isDataFlavorAvailable(DataFlavor.stringFlavor);

                if (b) {
                    String text = c.getData(DataFlavor.stringFlavor).toString().trim();
                    //remove html tags
                    String content = StringUtils.extractTextFromHtml(text).trim();
                    //one line string to multi line string, used for TextArea if necessary
                    //msg = content.replaceAll("[\n]+", "\r\n");
                    if (content.length() == text.length()) {
                        msg = text;
                    } else {
                        msg = content;
                    }
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
