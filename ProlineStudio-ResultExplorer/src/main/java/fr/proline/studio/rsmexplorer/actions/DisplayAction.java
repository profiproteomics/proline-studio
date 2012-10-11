/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.actions;


import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.openide.util.NbBundle;

/**
 *
 * @author JM235353
 */
public class DisplayAction extends AbstractRSMAction {

   ///private static DisplayAction instance = null;

   public DisplayAction() {
       super(NbBundle.getMessage(DisplayAction.class, "CTL_DisplayAction"));
   }
/*
   public static DisplayAction getInstance() {
      if (instance == null) {
         instance = new DisplayAction();
      }
      
      return instance;
   }
   */

   

    @Override
    public JMenuItem getPopupPresenter() {
        JMenu menu = new JMenu((String) getValue(NAME));

        JMenuItem proteinMenuItem = new JMenuItem(new ProteinGroupsAction());
        menu.add(proteinMenuItem);
        /*
         * menu.add(new JMenuItem(PeptidsGroupsAction.getInstance()));
         * menu.add(new JMenuItem(QueriesGroupsAction.getInstance()));
         * menu.add(new JMenuItem(PropertiesAction.getInstance()));
         */
        //JPM.TODO

        return menu;
    }
    
    

   
   
    /*   @Override
    public void updateEnabled(Node[] selectedNodes) {

    }*/
/*
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
    }*/
   
   
}