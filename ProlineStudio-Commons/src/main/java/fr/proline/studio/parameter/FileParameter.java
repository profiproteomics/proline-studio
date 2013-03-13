package fr.proline.studio.parameter;


import fr.proline.studio.utils.IconManager;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author jm235353
 */
public class FileParameter extends AbstractParameter {


    private String defaultValue;
    private String fileFilterName;
    private String fileFilterExtension;

    public FileParameter(String key, String name, Class graphicalType, String defaultValue, String fileFilterName, String fileFilterExtension) {
        super(key, name, String.class, graphicalType);
        this.defaultValue = defaultValue;
        this.fileFilterName = fileFilterName;
        this.fileFilterExtension = fileFilterExtension;
    }

    @Override
    public JComponent getComponent(Object value) {

        if (defaultValue == null) {
            defaultValue = "";
        }


        if (graphicalType.equals(JTextField.class)) {

            // --- Slider ---
            JPanel panel = new JPanel(new FlowLayout());
            
            
            final JTextField textField = new JTextField(30);
            textField.setText(defaultValue);

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
                
                if (fileFilterName != null) {
                    FileNameExtensionFilter filter = new FileNameExtensionFilter(fileFilterName, fileFilterExtension);
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

            parameterComponent = textField;

            return panel;
        }

        return null;
    }
    
    @Override
    public void initDefault() {
        if (defaultValue == null) {
            return; // should not happen
        }

        if (graphicalType.equals(JTextField.class)) {
            JTextField textField = (JTextField) parameterComponent;
            textField.setText(defaultValue.toString());
        }
    }
    
    @Override
    public ParameterError checkParameter() {
        
        Integer value = null;
        
        if (graphicalType.equals(JTextField.class)) {
            JTextField textField = (JTextField) parameterComponent;
            String path = textField.getText();
            File f = new File(path);
            if (! f.exists()) {
                return new ParameterError(path+" file does not exist", parameterComponent);
            } else if (f.isDirectory() ) {
                return new ParameterError(path+" is a directory", parameterComponent);
            } else if (! f.canRead() ) {
                return new ParameterError(path+" is not readable", parameterComponent);
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
        if (graphicalType.equals(JTextField.class)) {
           return ((JTextField) parameterComponent).getText();
        }
        return ""; // should not happen
    }
    
    @Override
    public void clean() {
        if (graphicalType.equals(JTextField.class)) {
            JTextField textField = (JTextField) parameterComponent;
            textField.setText("");
        }
    }
}
