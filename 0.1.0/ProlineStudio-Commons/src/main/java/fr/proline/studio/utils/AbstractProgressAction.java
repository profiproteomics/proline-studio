/*
 * Abstract Action which display a ProgressBar during action duration.
 *
 * Warnings :
 * - The action should be defined in subclass runAction method and NOT in actionPerformed nor run
 * - The closeProgressBar method Will automatically be called at the end of the run method.
 *
 */
package fr.proline.studio.utils;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.openide.util.RequestProcessor;
import org.openide.windows.WindowManager;

//@ActionID(category = "Edit",
//id = "fr.proline.studio.utils.AbstractProgressAction")
//@ActionRegistration(displayName = "#CTL_AbstractProgressAction")
//@ActionReferences({})
//@Messages("CTL_AbstractProgressAction=To Be Redefined")
public abstract class AbstractProgressAction extends AbstractAction implements  Runnable  {
    ProgressBarDialog dialog;
    ActionEvent event;
    
   /**
     * Store specified ActionEvent, Start the ProgressBarDialog
     * and call Runnable run method in another thread.
     * 
     * @param e ActionEvent 
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        this.event = e;
        dialog = new ProgressBarDialog(WindowManager.getDefault().getMainWindow(), true);
        dialog.setCommandLabel(getProgressBarMessage());
        dialog.setProgressBarIndeterminate(true);
        dialog.pack();
        RequestProcessor.getDefault().post(this);
        dialog.setVisible(true);
    }
    
    public ActionEvent getInitialActionEvent(){
        return event;
    }
    
    @Override
    /**
     * This Runnable method will call the subclasses runAction method
     * and finally close the ProgressBarDialog if not already done
     */
    public void run(){
        runAction();
        closeProgressBar();
    }
    
    /**
     * Implements the action to be performed.
     */
    public abstract void runAction();

    /**
     * Close ProgressBar dialog. This method will be called at the end of run methods.
     * 
     */
    public void closeProgressBar() {
        if (dialog.isVisible()) {
            dialog.setVisible(false);
        }
    }
    
    /**
     * Return the message that should be displayed by ProgressBar dialog
     *
     * @return
     */
    public abstract String getProgressBarMessage();
    
}
