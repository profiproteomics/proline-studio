package fr.proline.studio.parameter;


import fr.proline.studio.utils.IconManager;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Parameter of type File, displayed as a Textfield with a button to open a file browser.
 * @author jm235353
 */
public class FileParameter extends AbstractParameter {


    private String m_defaultValue;
    private String m_fileFilterName;
    private String m_fileFilterExtension;

    public FileParameter(String key, String name, Class graphicalType, String defaultValue, String fileFilterName, String fileFilterExtension) {
        super(key, name, String.class, graphicalType);
        m_defaultValue = defaultValue;
        m_fileFilterName = fileFilterName;
        m_fileFilterExtension = fileFilterExtension;
    }

    @Override
    public JComponent getComponent(Object value) {

        if (m_defaultValue == null) {
            m_defaultValue = "";
        }


        if (m_graphicalType.equals(JTextField.class)) {

            // --- Slider ---
            JPanel panel = new JPanel(new FlowLayout());
            
            
            final JTextField textField = new JTextField(30);
            textField.setText(m_defaultValue);

            final JButton addFileButton = new JButton(IconManager.getIcon(IconManager.IconType.OPEN_FILE));
            addFileButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
            addFileButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                
                JFileChooser fchooser = new JFileChooser();
                /*if ((defaultDirectory!=null) && (defaultDirectory.isDirectory())) {
                    fchooser.setCurrentDirectory(defaultDirectory);
                }*/
                fchooser.setMultiSelectionEnabled(false);
                
                if (m_fileFilterName != null) {
                    FileNameExtensionFilter filter = new FileNameExtensionFilter(m_fileFilterName, m_fileFilterExtension);
                    fchooser.addChoosableFileFilter(filter);
                }
                //fchooser.setFileFilter(defaultFilter);
                int result = fchooser.showOpenDialog(addFileButton);
                if (result == JFileChooser.APPROVE_OPTION) {

                    File file = fchooser.getSelectedFile();
                    textField.setText(file.getAbsolutePath());
                }
            }
        });
            
            
            panel.add(textField);
            panel.add(addFileButton);

            m_parameterComponent = textField;

            return panel;
        }

        return null;
    }
    
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
        
        Integer value = null;
        
        if (m_graphicalType.equals(JTextField.class)) {
            JTextField textField = (JTextField) m_parameterComponent;
            String path = textField.getText();
            File f = new File(path);
            if (! f.exists()) {
                return new ParameterError(path+" file does not exist", m_parameterComponent);
            } else if (f.isDirectory() ) {
                return new ParameterError(path+" is a directory", m_parameterComponent);
            } else if (! f.canRead() ) {
                return new ParameterError(path+" is not readable", m_parameterComponent);
            }

        }
        

        
        return null;
    }

    @Override
    public String getStringValue() {
        return getObjectValue().toString();
    }

    @Override
    public Object getObjectValue() {
        if (m_graphicalType.equals(JTextField.class)) {
           return ((JTextField) m_parameterComponent).getText();
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
}
