/*
 * Abstract CookieAction which display a ProgressBar during action duration.
 *
 * Warnings :
 * - The action should be defined in subclass runAction method and NOT in performAction nor run
 * - The closeProgressBar method Will automatically be called at the end of the run method.
 *
 */
package fr.proline.studio.utils;

import java.util.ArrayList;
import java.util.List;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.CookieAction;
import org.openide.windows.WindowManager;

/**
 *
 * @author VD225637
 */
public abstract class AbstractProgressCookieAction extends CookieAction implements Runnable {

    ProgressBarDialog dialog;
    private Lookup nodeLookup;
    private List<Lookup> allNodeLookup;

    /**
     * Return the message that should be displayed by ProgressBar dialog
     *
     * @return
     */
    public abstract String getProgressBarMessage();

    /**
     * Store currently active nodes, Start the ProgressBarDialog
     * and call Runnable run method in another thread
     * 
     * @param activatedNodes 
     */
    @Override
    protected void performAction(Node[] activatedNodes) {

        nodeLookup = activatedNodes[0].getLookup();
        allNodeLookup = new ArrayList<Lookup>();
        for (Node node : activatedNodes) {
            allNodeLookup.add(node.getLookup());
        }

        dialog = new ProgressBarDialog(WindowManager.getDefault().getMainWindow(), true);
        dialog.setCommandLabel(getProgressBarMessage());
        dialog.setProgressBarIndeterminate(true);
        dialog.pack();
        RequestProcessor.getDefault().post(this);
        dialog.setVisible(true);
    }

    @Override
    protected int mode() {
        return CookieAction.MODE_EXACTLY_ONE;
    }

    protected Lookup getFirstLookup() {
        return nodeLookup;
    }

    protected List<Lookup> getAllLookups() {
        return allNodeLookup;
    }

    @Override
    protected void initialize() {
        super.initialize();
        // see org.openide.util.actions.SystemAction.iconResource() Javadoc for more details
        putValue("noIconInMenu", Boolean.TRUE);
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous() {
        return false;
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
}
