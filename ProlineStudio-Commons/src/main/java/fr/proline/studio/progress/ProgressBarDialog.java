package fr.proline.studio.progress;

import fr.proline.studio.gui.DefaultDialog;
import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.Timer;

/**
 * Dialog to wait for a task to be finished, or cancel the asked action
 * @author JM235353
 */
public class ProgressBarDialog extends DefaultDialog {
    
    private static ProgressBarDialog m_dialog;
    
    private ProgressInterface m_progressInterface;
    
    private JProgressBar m_progressBar;
    private JLabel m_messageLabel;
    
    private Timer m_progressBarTimer = null;
    
    private boolean m_waitingFinished = false;
    
    public static ProgressBarDialog getDialog(Window parent, ProgressInterface progressInterface, String title, String message) {
        
        if (m_dialog == null) {
            m_dialog = new ProgressBarDialog(parent);
        }
        
        m_dialog.initDialog(progressInterface, title, message);
        
        
        return m_dialog;
    }
    
    public ProgressBarDialog(Window parent) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);
        
        setButtonVisible(BUTTON_DEFAULT, false);
        setButtonVisible(BUTTON_OK, false);
        
        setStatusVisible(false);
        
        initInternalPanel();
    }
    
    private void initDialog(ProgressInterface progressInterface, String title, String message) {
        
        m_progressInterface = progressInterface;
        setTitle(title);
        m_messageLabel.setText(message);
        
        m_waitingFinished= false;
    }
    
    private void initInternalPanel() {

        JPanel internalPanel = new JPanel();
        internalPanel.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;

        
        m_messageLabel = new JLabel();
        internalPanel.add(m_messageLabel, c);
        
        
        m_progressBar = new JProgressBar(0, 100);
        m_progressBar.setStringPainted(true);
        c.gridy++;
        internalPanel.add(m_progressBar, c);


        setInternalComponent(internalPanel);
    }

    public boolean isWaitingFinished() {
        return m_waitingFinished;
    }
    
    @Override
    public void setVisible(boolean v) {

        m_progressBar.setValue(0);
        m_progressBar.setIndeterminate(true);
        m_progressBar.setStringPainted(false);
        
        if (v) {
            m_progressBarTimer = new Timer(500, new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {

                    if (m_progressInterface.isLoaded()) {
                        m_waitingFinished = true;
                        m_progressBarTimer.stop();
                        setVisible(false);
                        return;
                    }

                    int percentage = m_progressInterface.getLoadingPercentage();
                    if (percentage>0 && (m_progressBar.isIndeterminate())) {
                        m_progressBar.setIndeterminate(false);
                        m_progressBar.setStringPainted(true);
                    }
                    m_progressBar.setValue(percentage);
                }
            });
            m_progressBarTimer.start();
        } else {
            m_progressBarTimer.stop();
        }

        super.setVisible(v);

    }

}
