/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.actions;


import java.awt.event.ActionEvent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.NodeAction;

/**
 *
 * @author JM235353
 */
public class DisplayAction extends NodeAction implements Presenter.Popup {

   private static DisplayAction instance = null;

   private DisplayAction() {
   }

   public static DisplayAction getInstance() {
      if (instance == null) {
         instance = new DisplayAction();
      }
      
      return instance;
   }
   

   

   @Override
   public JMenuItem getPopupPresenter() {

         if (menu == null) {
             menu = new JMenu(NbBundle.getMessage(DisplayAction.class, "CTL_DisplayAction"));
             proteinMenuItem = new JMenuItem(ProteinGroupsAction.getInstance());
             menu.add(proteinMenuItem);
         /*menu.add(new JMenuItem(PeptidsGroupsAction.getInstance()));
         menu.add(new JMenuItem(QueriesGroupsAction.getInstance()));
         menu.add(new JMenuItem(PropertiesAction.getInstance()));*/
             //JPM.TODO
         }
         
         menu.setEnabled(isEnabled());
         proteinMenuItem.setEnabled(ProteinGroupsAction.getInstance().isEnabled());
         return menu;
   }
   private JMenu menu = null;
   private JMenuItem proteinMenuItem = null;
   
   
    /*   @Override
    public void updateEnabled(Node[] selectedNodes) {

    }*/

    @Override
    protected void performAction(Node[] nodes) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected boolean enable(Node[] nodes) {

        
        boolean actionEnabled = ((nodes != null) && (nodes.length == 1));
        
        if (actionEnabled) {
            //JPM.hack : update enable of sub actions
            ProteinGroupsAction.getInstance().enable(nodes);
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