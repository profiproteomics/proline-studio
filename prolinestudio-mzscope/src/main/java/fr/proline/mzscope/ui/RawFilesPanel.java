package fr.proline.mzscope.ui;

import fr.proline.mzscope.ui.model.RawFileListModel;
import fr.proline.mzscope.model.IRawFile;
import fr.proline.mzscope.utils.IPopupMenuDelegate;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * list which contains the mzdb filenames
 *
 * @author MB243701
 */
public class RawFilesPanel extends JPanel {

   final private static Logger logger = LoggerFactory.getLogger(RawFilesPanel.class);

   private final RawFileListModel rawFilesListModel;
   private JList<IRawFile> rawFilesList;
   private JPopupMenu popupMenu;
   private JScrollPane scrollPane;
   private JLabel openedRawFilesLabel;
   private final IPopupMenuDelegate popupMenuDelegate;


   /**
    * Creates new form RawFilesPanel
    * 
    * @param popupDelegate that will be responsible for popupmenu item creation and popupmenu items update
    */
   public RawFilesPanel(IPopupMenuDelegate popupDelegate) {
      this.popupMenuDelegate = popupDelegate;
      initComponents();
      rawFilesListModel = (RawFileListModel) rawFilesList.getModel();
   }

   private void initComponents() {
      openedRawFilesLabel = new JLabel();
      scrollPane = new JScrollPane();
      rawFilesList = new JList();
      popupMenu = new JPopupMenu();
      popupMenuDelegate.initPopupMenu(popupMenu);
      openedRawFilesLabel.setText("mzDB files");

      rawFilesList.setModel(new RawFileListModel());

      rawFilesList.setComponentPopupMenu(popupMenu);
      rawFilesList.setLayoutOrientation(JList.VERTICAL);
      rawFilesList.addMouseListener(new java.awt.event.MouseAdapter() {
         @Override
         public void mouseClicked(java.awt.event.MouseEvent evt) {
            rawFilesListMouseClicked(evt);
         }

         @Override
         public void mousePressed(java.awt.event.MouseEvent evt) {
            rawFilesListMousePressed(evt);
         }
      });
      scrollPane.setViewportView(rawFilesList);

      javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
      this.setLayout(layout);
      layout.setHorizontalGroup(
              layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addGroup(layout.createSequentialGroup()
                      .addGap(5, 5, 5)
                      .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                              .addGroup(layout.createSequentialGroup()
                                      .addComponent(scrollPane)
                                      .addGap(5, 5, 5))
                              .addGroup(layout.createSequentialGroup()
                                      .addComponent(openedRawFilesLabel)
                                      .addContainerGap(156, Short.MAX_VALUE))))
      );
      layout.setVerticalGroup(
              layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addGroup(layout.createSequentialGroup()
                      .addGap(5, 5, 5)
                      .addComponent(openedRawFilesLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                      .addGap(5, 5, 5)
                      .addComponent(scrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 192, Short.MAX_VALUE)
                      .addGap(5, 5, 5))
      );

      openedRawFilesLabel.getAccessibleContext().setAccessibleName("Raw files");
   }

   private void rawFilesListMouseClicked(MouseEvent evt) {
      if (evt.getClickCount() == 2) {
         if (rawFilesList.getSelectedIndex() > -1) {
            popupMenuDelegate.getDefaultAction().actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "default"));
         }
      }
   }

   private void rawFilesListMousePressed(MouseEvent evt) {
      popupMenuDelegate.updatePopupMenu();
      if (rawFilesList.getSelectedIndex() > -1 && SwingUtilities.isRightMouseButton(evt)) {
         popupMenu.show((JComponent) evt.getSource(), evt.getX(), evt.getY());
      }
   }

   public JPopupMenu getPopupMenu() {
      return popupMenu;
   }

   public List<IRawFile> getSelectedValues() {
      return rawFilesList.getSelectedValuesList();
   }

   public void addFile(IRawFile file) {
      rawFilesListModel.add(file);
   }

   public void removeFile(IRawFile file) {
      rawFilesListModel.removeFile(file);
   }

   public void removeAllFiles() {
      rawFilesListModel.removeAllFiles();
   }

}
