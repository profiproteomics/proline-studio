package fr.proline.studio.parameter;

import fr.proline.studio.utils.IconManager;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;

/**
 * Parameter of type File, displayed as a Textfield with a button to open a file
 * browser.
 *
 * @author jm235353
 */
public class FileParameter extends AbstractParameter {

    private ArrayList<AbstractLinkedParameters> m_linkedParametersList = null;

    private final FileSystemView m_fsv;
    private String m_defaultValue, m_startValue;
    private final String[] m_fileFilterName;
    private final String[] m_fileFilterExtension;
    private boolean m_edited = false;

    private int m_selectionMode = JFileChooser.FILES_AND_DIRECTORIES;
    private boolean m_allFiles = true;
    private File m_defaultDirectory = null;

    public FileParameter(FileSystemView fsv, String key, String name, Class graphicalType, String defaultValue, String[] fileFilterName, String[] fileFilterExtension) {
        super(key, name, String.class, graphicalType);
        m_fsv = fsv;
        m_defaultValue = defaultValue;
        m_fileFilterName = fileFilterName;
        m_fileFilterExtension = fileFilterExtension;
    }

    @Override
    public JComponent getComponent(Object value) {

        m_startValue = null;
        if (value != null) {
            m_startValue = value.toString();
        }
        if (m_startValue == null) {
            m_startValue = (m_defaultValue != null) ? m_defaultValue : "";
        }

        if (m_parameterComponent == null) {
            if (m_graphicalType.equals(JTextField.class)) {

                // --- Slider ---
                m_panel = new JPanel(new FlowLayout());

                final JTextField textField = new JTextField(30);
                textField.setText(m_startValue);
                textField.addKeyListener(new KeyListener() {

                    @Override
                    public void keyTyped(KeyEvent ke) {
                        if (!textField.getText().equalsIgnoreCase(m_startValue)) {
                            m_edited = true;
                        } else {
                            m_edited = false;
                        }
                    }

                    @Override
                    public void keyPressed(KeyEvent ke) {
                        ;
                    }

                    @Override
                    public void keyReleased(KeyEvent ke) {
                        ;
                    }
                });

                //Check if this works-solves the problem!
                m_parameterComponent = textField;

                final JButton addFileButton = new JButton(IconManager.getIcon(IconManager.IconType.OPEN_FILE));
                addFileButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
                addFileButton.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {

                        JFileChooser fchooser = (m_fsv != null) ? new JFileChooser(m_fsv) : new JFileChooser();

                        fchooser.setMultiSelectionEnabled(false);

                        if (m_defaultDirectory != null) {
                            fchooser.setCurrentDirectory(m_defaultDirectory);
                        }
                        fchooser.setFileSelectionMode(m_selectionMode);
                        fchooser.setAcceptAllFileFilterUsed(m_allFiles);

                        if (m_fileFilterName != null) {
                            for (int i = 0; i < m_fileFilterName.length; i++) {
                                FileNameExtensionFilter filter = new FileNameExtensionFilter(m_fileFilterName[i], m_fileFilterExtension[i]);
                                fchooser.addChoosableFileFilter(filter);
                            }
                        }

                        int result = fchooser.showOpenDialog(addFileButton);
                        if (result == JFileChooser.APPROVE_OPTION) {
                            File file = fchooser.getSelectedFile();
                            textField.setText(file.getPath());
                            if (!textField.getText().equalsIgnoreCase(m_startValue)) {
                                m_edited = true;
                            } else {
                                m_edited = false;
                            }
                        }
                    }
                });

                m_panel.add(textField);
                m_panel.add(addFileButton);

                m_parameterComponent = textField;

                return m_panel;
            }
        }

        return m_panel;
    }
    private JPanel m_panel = null;

    @Override
    public void initDefault() {
        if (m_defaultValue == null) {
            return; // should not happen
        }

        if (m_graphicalType.equals(JTextField.class)) {
            JTextField textField = (JTextField) m_parameterComponent;
            textField.setText(m_defaultValue.toString());
        }
    }

    @Override
    public ParameterError checkParameter() {

        if (m_graphicalType.equals(JTextField.class)) {
            JTextField textField = (JTextField) m_parameterComponent;
            String path = textField.getText().trim();
            if (path.isEmpty()) {
                return new ParameterError(path + " file is missing", m_parameterComponent);
            }

        }

        return null;
    }

    @Override
    public void setValue(String v) {
        if ((m_graphicalType.equals(JTextField.class)) && (m_parameterComponent != null)) {
            ((JTextField) m_parameterComponent).setText(v);
        }
    }

    @Override
    public String getStringValue() {
        return getObjectValue().toString();
    }

    @Override
    public Object getObjectValue() {
        if (m_graphicalType.equals(JTextField.class)) {
            if (m_parameterComponent != null) {
                return ((JTextField) m_parameterComponent).getText().trim();
            }
        }
        return ""; // should not happen
    }

    @Override
    public void clean() {
        if (m_graphicalType.equals(JTextField.class)) {
            JTextField textField = (JTextField) m_parameterComponent;
            textField.setText("");
        }
    }

    public void setSelectionMode(int selectionMode) {
        m_selectionMode = selectionMode;
    }

    public void setAllFiles(boolean allFiles) {
        m_allFiles = allFiles;
    }

    public void setDefaultDirectory(File defaultDirectory) {
        m_defaultDirectory = defaultDirectory;
    }

    public void addLinkedParameters(final AbstractLinkedParameters linkedParameters) {

        // create parameterComponent if needed
        getComponent(null);

        if (m_linkedParametersList == null) {
            m_linkedParametersList = new ArrayList<>(1);
        }
        m_linkedParametersList.add(linkedParameters);

        if (m_parameterComponent instanceof JTextField) {

            ((JTextField) m_parameterComponent).getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void changedUpdate(DocumentEvent e) {
                    textChanged();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    textChanged();
                }

                @Override
                public void insertUpdate(DocumentEvent e) {
                    textChanged();
                }

                public void textChanged() {
                    linkedParameters.valueChanged(getStringValue(), getObjectValue());
                }
            });

        }
    }

    @Override
    public boolean isEdited() {
        return m_edited;
    }
}
