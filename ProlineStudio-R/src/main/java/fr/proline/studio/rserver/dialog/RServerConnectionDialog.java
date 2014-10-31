package fr.proline.studio.rserver.dialog;


import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.dpm.AccessServiceThread;
import fr.proline.studio.dpm.task.AbstractServiceCallback;
import fr.proline.studio.dpm.task.AbstractServiceTask;
import fr.proline.studio.gui.ConnectionDialog;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.rserver.RServerManager;
import java.awt.Window;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.*;
import org.openide.util.NbPreferences;
import org.slf4j.LoggerFactory;

/**
 * Dialog to Connect to the R Server
 * @author jm235353
 */
public class RServerConnectionDialog extends ConnectionDialog {

    private static final String KEY_R_SERVER_URL = "RServerURL";
    private static final String KEY_R_USER = "RServerUser";
    private static final String KEY_R_PASSWORD = "RServerPassword";
    private static final String KEY_R_SERVER_PASSWORD_NEEDED = "RServerPasswordNeeded";
    
    
    private static RServerConnectionDialog m_singletonDialog = null;
    
    public static RServerConnectionDialog getDialog(Window parent) {
        if (m_singletonDialog == null) {
            m_singletonDialog = new RServerConnectionDialog(parent);
        }

        return m_singletonDialog;
    }
    
    private RServerConnectionDialog(Window parent) {
        super(parent, "R-Server Connection", " R-Server Parameter ", "R-Server Host :");

        setButtonVisible(DefaultDialog.BUTTON_HELP, false);
        setButtonVisible(BUTTON_DEFAULT, true);
        
        initDefaults();
        
        
    }

    private void initDefaults() {

        Preferences preferences = NbPreferences.root();

        m_serverURLTextField.setText(preferences.get(KEY_R_SERVER_URL, ""));
        m_userTextField.setText(preferences.get(KEY_R_USER, ""));

        String password = preferences.get(KEY_R_PASSWORD, "");
        m_passwordField.setText(password);
        m_rememberPasswordCheckBox.setSelected(!password.isEmpty());

    }
    
    @Override
    protected boolean okCalled() {
        if (!checkParameters()) {
            return false;
        }
        
        connect();

        
        // dialog will be closed if the connection is established
        return false;
    }


    @Override
    protected boolean defaultCalled() {
        initDefaults();

        return false;
    }


    
    private void connect() {
    
        setBusy(true);
        
        String serverURL = m_serverURLTextField.getText();
        int indexOfPort = serverURL.lastIndexOf(":");
        int port = 6311;  //JPM.TODO : check default r server port 
        if (indexOfPort !=-1) {
            try {
                port = Integer.parseInt(serverURL.substring(indexOfPort+1));
            } catch (NumberFormatException nfe) {
                
            }
            serverURL = serverURL.substring(0, indexOfPort);
        }
        
        String user  = m_userTextField.getText();
        String password = new String(m_passwordField.getPassword());
        
        AbstractServiceCallback callback = new AbstractServiceCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success) {
                
                setBusy(false);
                
                if (success) {
                    storeDefaults();
                    setVisible(false);
                } else {
                    TaskError error = getTaskError();
                    if (error != null) {
                        setStatus(true, error.getErrorTitle());
                        JOptionPane.showMessageDialog(m_singletonDialog, error.getErrorTitle(), "R-Server Connection Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
            
        };
        
        RServerConnectionTask task = new RServerConnectionTask(callback, serverURL, port, user, password);
        AccessServiceThread.getAccessServiceThread().addTask(task); 
        
        
    }
    
    private void storeDefaults() {
        Preferences preferences = NbPreferences.root();
        
        preferences.put(KEY_R_SERVER_URL, m_serverURLTextField.getText());
        preferences.put(KEY_R_USER, m_userTextField.getText());
        
        if (m_rememberPasswordCheckBox.isSelected()) {
            preferences.put(KEY_R_PASSWORD, new String(m_passwordField.getPassword()));
            preferences.putBoolean(KEY_R_SERVER_PASSWORD_NEEDED, true);
        } else {
            preferences.put(KEY_R_PASSWORD, "");
            preferences.putBoolean(KEY_R_SERVER_PASSWORD_NEEDED, false);
        }
        
        try {
            preferences.flush();
        } catch (BackingStoreException e) {
            LoggerFactory.getLogger("ProlineStudio.DPM").error("Saving Server Connection Parameters Failed", e);
        }
    }
    
    public class RServerConnectionTask extends AbstractServiceTask {

        private String m_serverURL;
        private int m_port;
        private String m_user;
        private String m_password;

        public RServerConnectionTask(AbstractServiceCallback callback, String serverURL, int port, String user, String password) {
            super(callback, true /*
                     * synchronous
                     */, new TaskInfo("Connection to R-Server " + serverURL + ":" + port, false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_HIGH));

            m_serverURL = serverURL;
            m_port = port;
            m_user = user;
            m_password = password;
        }

        @Override
        public boolean askService() {

            RServerManager serverR = RServerManager.getRServerManager();

            try {
                serverR.connect(m_serverURL, m_port, m_user, m_password);
            } catch (RServerManager.RServerException e) {
                m_taskError = new TaskError(e);
                //setStatus(true, e.getMessage());
                //JOptionPane.showMessageDialog(m_singletonDialog, e.getMessage(), "Database Connection Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }


            return true;
        }

        @Override
        public AbstractServiceTask.ServiceState getServiceState() {
            // always returns STATE_DONE because it is a synchronous service
            return AbstractServiceTask.ServiceState.STATE_DONE;
        }
    }

    
}
