package fr.proline.studio.rsmexplorer.actions;


import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.openide.util.NbBundle;

/**
 *
 * @author JM235353
 */
public class ChildParentAction extends AbstractRSMAction {

    //private static ChildParentAction instance = null;

    public ChildParentAction() {
        super(NbBundle.getMessage(DisplayAction.class, "CTL_ChildParentAction"));
    }

    @Override
    public JMenuItem getPopupPresenter() {
        JMenu menu = new JMenu((String) getValue(NAME));
        
        JMenuItem newFractionMenuItem = new JMenuItem(new NewIdentificationFractionAction());
        menu.add(newFractionMenuItem);
        
        JMenuItem detachMenuItem = new JMenuItem(new DetachAction());
        menu.add(detachMenuItem);

        return menu;
    }
    
    
    /*public static ChildParentAction getInstance() {
        if (instance == null) {
            instance = new ChildParentAction();
        }
        return instance;
    }*/

    /*
    @Override
    public JMenuItem getPopupPresenter() {
        if (menu == null) {
            menu = new JMenu(NbBundle.getMessage(DisplayAction.class, "CTL_ChildParentAction"));
            newFractionMenuItem = new JMenuItem(NewIdentificationFractionAction.getInstance());
            menu.add(newFractionMenuItem);
            detachMenuItem = new JMenuItem(DetachAction.getInstance());
            menu.add(detachMenuItem);
        }
        menu.setEnabled(isEnabled());
        newFractionMenuItem.setEnabled(NewIdentificationFractionAction.getInstance().isEnabled());
        detachMenuItem.setEnabled(DetachAction.getInstance().isEnabled());

        return menu;
    }
    private JMenu menu = null;
    private JMenuItem newFractionMenuItem = null;
    private JMenuItem detachMenuItem = null;



    @Override
    protected boolean enable(Node[] nodes) {
        boolean actionEnabled = ((nodes != null) && (nodes.length == 1));

        if (actionEnabled) {
            //JPM.hack : update enable of sub actions
            NewIdentificationFractionAction.getInstance().enable(nodes);
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
    }*/
}