/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.actions;

import java.awt.event.ActionEvent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;
import org.openide.util.actions.Presenter;

/**
 *
 * @author JM235353
 */
public class ChildParentAction extends NodeAction implements Presenter.Popup {

    private static ChildParentAction instance = null;

    private ChildParentAction() {
    }

    public static ChildParentAction getInstance() {
        if (instance == null) {
            instance = new ChildParentAction();
        }
        return instance;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public JMenuItem getPopupPresenter() {
        if (menu == null) {
            menu = new JMenu(NbBundle.getMessage(DisplayAction.class, "CTL_ChildParentAction"));
            newContextMenuItem = new JMenuItem(NewContextAction.getInstance());
            menu.add(newContextMenuItem);
            detachMenuItem = new JMenuItem(DetachAction.getInstance());
            menu.add(detachMenuItem);
        }
        menu.setEnabled(isEnabled());
        newContextMenuItem.setEnabled(NewContextAction.getInstance().isEnabled());
        detachMenuItem.setEnabled(DetachAction.getInstance().isEnabled());

        return menu;
    }
    private JMenu menu = null;
    private JMenuItem newContextMenuItem = null;
    private JMenuItem detachMenuItem = null;

    @Override
    protected void performAction(Node[] nodes) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected boolean enable(Node[] nodes) {
        boolean actionEnabled = ((nodes != null) && (nodes.length == 1));

        if (actionEnabled) {
            //JPM.hack : update enable of sub actions
            NewContextAction.getInstance().enable(nodes);
            DetachAction.getInstance().enable(nodes);
        }

        return actionEnabled;

    }

    @Override
    public String getName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public HelpCtx getHelpCtx() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}