package fr.proline.studio.rsmexplorer.actions;

import fr.proline.core.orm.uds.UserAccount;
import fr.proline.studio.dam.UDSDataManager;
import fr.proline.studio.dam.data.ProjectData;
import fr.proline.studio.dpm.AccessServiceThread;
import fr.proline.studio.dpm.ServerConnectionManager;
import fr.proline.studio.dpm.task.AbstractServiceCallback;
import fr.proline.studio.dpm.task.CreateProjectTask;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.rsmexplorer.gui.dialog.AddProjectDialog;
import fr.proline.studio.rsmexplorer.node.RSMHourGlassNode;
import fr.proline.studio.rsmexplorer.node.RSMNode;
import fr.proline.studio.rsmexplorer.node.RSMProjectNode;
import fr.proline.studio.rsmexplorer.node.RSMTree;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.tree.DefaultTreeModel;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/**
 * To Add a Project in the UDS DB
 * @author jm235353
 */
public class AddProjectAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {




    }

    public void updateEnabled() {
        boolean b = ServerConnectionManager.getServerConnectionManager().isConnectionDone();
    }
    
}
