/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.gui.ptm;

import fr.proline.studio.dam.tasks.data.ptm.PTMPeptideInstance;
import fr.proline.studio.dam.tasks.data.ptm.PTMSite;
import fr.proline.studio.export.ExportButton;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxPanelInterface;
import fr.proline.studio.rsmexplorer.gui.ptm.mark.ProteinSequenceCtrl;
import fr.proline.studio.rsmexplorer.gui.ptm.mark.PTMMarkCtrl;
import fr.proline.studio.rsmexplorer.gui.ptm.pep.PeptideAreaCtrl;
import fr.proline.studio.utils.CyclicColorPalette;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import static java.awt.image.ImageObserver.HEIGHT;
import static java.awt.image.ImageObserver.WIDTH;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author VD225637
 */
public class PTMPeptidesGraphicView extends JPanel implements DataBoxPanelInterface, SplittedPanelContainer.UserActions {
    
    private static Logger LOG = LoggerFactory.getLogger("ProlineStudio.rsmexplorer.ptm");
    protected AbstractDataBox m_dataBox;
    
    private PTMPeptidesGraphicPanel m_internalPanel;
    private PTMPeptidesGraphicModel m_dataModel;

    public PTMPeptidesGraphicView() {
        super();
        m_internalPanel = new PTMPeptidesGraphicPanel();
        m_dataModel = new PTMPeptidesGraphicModel();
        m_internalPanel.setModel(m_dataModel);
        initComponents();
    }
    
      /**
     * ToolBar at left, PainArea at Center
     */
    private void initComponents() {
        setLayout(new BorderLayout());

        m_internalPanel.setSize(WIDTH - 30, HEIGHT);
        JToolBar toolbar = initToolbar();
        this.add(toolbar, BorderLayout.WEST);
        this.add(m_internalPanel, BorderLayout.CENTER);

    }

    /**
     * Export image button in ToolBar
     *
     * @return
     */
    private JToolBar initToolbar() {
        JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
        toolbar.setFloatable(false);
        ExportButton exportImageButton = new ExportButton("Graphic", this);
        toolbar.add(exportImageButton);

        return toolbar;
    }
    
    @Override
    public void addSingleValue(Object v) {
        if(PTMSite.class.isInstance(v))
            m_dataModel.setData((PTMSite)v);
    }

    @Override
    public void setDataBox(AbstractDataBox dataBox) {
        this.m_dataBox = dataBox;
    }

    @Override
    public AbstractDataBox getDataBox() {
        return m_dataBox;
    }

    @Override
    public void setLoading(int id) {
      //Nothing to do
    }

    @Override
    public void setLoading(int id, boolean calculating) {
        //Nothing to do
    }

    @Override
    public void setLoaded(int id) {
       //Nothing to do
    }

    @Override
    public ActionListener getRemoveAction(SplittedPanelContainer splittedPanel) {
        return m_dataBox.getRemoveAction(splittedPanel);
    }

    @Override
    public ActionListener getAddAction(SplittedPanelContainer splittedPanel) {
         return m_dataBox.getAddAction(splittedPanel);
    }

    @Override
    public ActionListener getSaveAction(SplittedPanelContainer splittedPanel) {
        return m_dataBox.getSaveAction(splittedPanel);
    }

    /**
     * useful between table-graphic ptm site panel
     *
     * @param i
     */
    public void setSelectedRow(int i) {
        this.m_internalPanel.setSelectedPeptideIndex(i);
    }

    public void setSelectedPTMPeptide(PTMPeptideInstance pep){
        int row = this.m_dataModel.getPeptideIndex(pep);
        this.m_internalPanel.setSelectedPeptideIndex(row);
        repaint();
    }
        
    /**
     * used to set next Data Box
     */
    public PTMPeptideInstance getSelectedPeptideInstance() {
        int selectedRowIndex = this.m_internalPanel.getSelectedPeptideIndex();
        return this.m_dataModel.getPeptideAt(selectedRowIndex);
    }
    
    public PTMSite getMainPTMSite(){
        return m_dataModel.getMainPTMSite();
    }


    public void setData(List<PTMPeptideInstance> peptidesInstances ) {       
        m_dataModel.setData(peptidesInstances, m_dataBox.getProjectId());
        if (peptidesInstances == null) {
            this.m_internalPanel.clean();
        } else {
            this.m_internalPanel.updateData();            
            m_dataBox.propagateDataChanged(PTMPeptideInstance.class);  
        }
        this.repaint();
    }
    
private class PTMPeptidesGraphicPanel extends JPanel  {
     
    private static final int INITIAL_WIDTH = 1200;
    private static final int AJUSTE_GAP = 3;
    
    private PTMPeptidesGraphicModel m_dataModel = null;
    
    private JScrollPane m_scrollPane;
    private final TitlePane m_titlePane;
    private final PeptidePane m_peptidesPane;
    private final PeptideNumberPane m_peptidesNumberPane;

    int m_ajustedStartLocation;
    private int m_sequenceLength;
    private boolean m_isDataNull;//when precedent databox change order or filter, we can have non selected row, in this case, nothing to show
    
    public PTMPeptidesGraphicPanel(){
        this(new PTMMarkCtrl(), new  ProteinSequenceCtrl(), new PeptideAreaCtrl());
    }
            
            
    public PTMPeptidesGraphicPanel(PTMMarkCtrl ctrlMark, ProteinSequenceCtrl ctrlSequence, PeptideAreaCtrl ctrlPeptideArea) {
        m_titlePane = new TitlePane(ctrlMark, ctrlSequence);
        m_peptidesPane = new PeptidePane(ctrlPeptideArea);
        m_peptidesNumberPane = new PeptideNumberPane();
        m_sequenceLength = 0;
        initComponents();
    }


    private void initComponents() {
        setDoubleBuffered(false);

        m_titlePane.setMarkBeginPoint(ViewSetting.BORDER_GAP, ViewSetting.BORDER_GAP);
        m_titlePane.setSequenceBeginPoint(ViewSetting.BORDER_GAP, ViewSetting.HEIGHT_MARK);
        m_peptidesPane.setBeginPoint(ViewSetting.BORDER_GAP, ViewSetting.BORDER_GAP);
        m_peptidesNumberPane.setBeginPoint(ViewSetting.BORDER_GAP, ViewSetting.BORDER_GAP);
        //first setPreferredSize to guarantee the height
        m_titlePane.setPreferredSize(new Dimension(INITIAL_WIDTH, ViewSetting.HEIGHT_MARK + ViewSetting.HEIGHT_SEQUENCE));
        m_titlePane.setBackground(Color.WHITE);
        m_peptidesPane.setBackground(Color.WHITE);
        m_titlePane.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, CyclicColorPalette.GRAY_GRID));
        m_peptidesPane.setBorder(BorderFactory.createMatteBorder(1, 1, 0, 0, CyclicColorPalette.GRAY_GRID));
        m_scrollPane = new JScrollPane(m_peptidesPane, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        m_scrollPane.setColumnHeaderView(m_titlePane);
        m_scrollPane.setRowHeaderView(m_peptidesNumberPane);
        m_scrollPane.setBackground(Color.white);
        this.setLayout(new BorderLayout());
        this.add(m_scrollPane, BorderLayout.CENTER);
    }
    
    public void setModel(PTMPeptidesGraphicModel dataModel){
        m_dataModel = dataModel;
    }


    public void setAjustedLocation(int ajustedLocation) {
        this.m_ajustedStartLocation = ajustedLocation;
        if (ajustedLocation >= AJUSTE_GAP) {
            m_ajustedStartLocation -= AJUSTE_GAP;
        }
    }

   
    /**
     * useful for change scroll bar state
     *
     * @param g
     */
    @Override
    public void paint(Graphics g) {
        this.m_scrollPane.getViewport().revalidate();//
        super.paint(g);
    }

    protected int getSelectedPeptideIndex() {
        return this.m_peptidesPane.m_ctrlPeptideArea.getSelectedIndex();
    }

    protected void clean() {
        this.m_isDataNull = true;
    }

    protected void setSelectedPeptideIndex(int i) {
        this.m_peptidesPane.m_ctrlPeptideArea.setSelectedIndex(i);
    }

    private void updateData() {
        m_titlePane.updateData();
        m_peptidesPane.updateData();

        int ajustedLocation = m_dataModel.getLowerStartInProtSeq();
        m_isDataNull = false;
        m_sequenceLength = m_dataModel.getProteinSequence().length();
        setAjustedLocation(ajustedLocation);
    }

    private class TitlePane extends JPanel {

        private PTMMarkCtrl m_ctrlMark;
        private ProteinSequenceCtrl m_ctrlSequence;

        private TitlePane(PTMMarkCtrl ctrlMark, ProteinSequenceCtrl ctrlSequence) {
            super();
            m_ctrlMark = ctrlMark;
            m_ctrlSequence = ctrlSequence;
            this.addMouseMotionListener(new MouseMotionListener() {

                @Override
                public void mouseMoved(MouseEvent e) {//for tooltips
                    int x = e.getX();
                    int y = e.getY();
                    String tips = m_ctrlMark.getToolTipText(x, y, m_ajustedStartLocation);
                    setToolTipText(tips);//null will turn off ToolTip

                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    // TODO Auto-generated method stub

                }
            });
        }
        
        public void updateData(){
            m_ctrlSequence.setData(m_dataModel.getProteinSequence());
            if(m_dataModel.getMainPTMSite() != null)
                m_ctrlSequence.setPTMSequencePosition(m_dataModel.getMainPTMSite().getPositionOnProtein());
            else {
//                //Take first PTMSite (??) or all ??? VDS FIXME
//                if(m_dataModel.getPeptideAt(0)!=null && m_dataModel.getPeptideAt(0).getSites().size()>0)
                    m_ctrlSequence.setPTMSequencePosition(-1);
            }
            m_ctrlMark.setData(m_dataModel.getAllPtmMarks());            
        }

        public void setMarkBeginPoint(int x, int y) {
            m_ctrlMark.setBeginPoint(x, y);
        }

        public void setSequenceBeginPoint(int x, int y) {
            m_ctrlSequence.setBeginPoint(x, y);
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension((int) ((m_sequenceLength + AJUSTE_GAP - m_ajustedStartLocation) * ViewSetting.WIDTH_AA), ViewSetting.HEIGHT_MARK + ViewSetting.HEIGHT_SEQUENCE);
        }

        @Override
        protected void paintComponent(Graphics g) {
            this.isPaintingOrigin();
            super.paintComponent(g);
            if (!m_isDataNull) {
                ViewContext viewContext = new ViewContext();
                viewContext.setAjustedStartLocation(m_ajustedStartLocation);
                int adjustedEndLoc = m_dataModel.getHigherEndInProtSeq();                
                if(m_sequenceLength > (adjustedEndLoc+AJUSTE_GAP))
                    adjustedEndLoc = m_dataModel.getHigherEndInProtSeq()+AJUSTE_GAP;
                    viewContext.setAjustedEndLocation(adjustedEndLoc).setShowNCtermIndex(false);
                    Graphics2D g2 = (Graphics2D) g;
                    m_ctrlMark.paint(g2, viewContext);
                    m_ctrlSequence.paint(g2, viewContext);
            }
        }

    }

    private class PeptidePane extends JPanel {

        private PeptideAreaCtrl m_ctrlPeptideArea;

        private PeptidePane(PeptideAreaCtrl ctrlPeptideArea) {
            super();
            m_ctrlPeptideArea = ctrlPeptideArea;
            this.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    requestFocusInWindow();
                    //PtmSitePeptide selectedItem = _ctrlPeptideArea.getSelectedItem(e.getX(), e.getY());
                    int oldSelected = m_ctrlPeptideArea.getSelectedIndex();
                    int selectedIndex = m_ctrlPeptideArea.getSelectedIndex(e.getX(), e.getY());
                    if (selectedIndex != oldSelected && (selectedIndex != -1) && (m_dataBox != null)) {
                        repaint(); //VDS : Should Graphic view repaint ?! m_dataBox DataBoxPanelInterface repaint ? 
                        m_dataBox.propagateDataChanged(PTMPeptideInstance.class);
                    }
                }
            });
            this.addMouseMotionListener(new MouseMotionListener() {

                @Override
                public void mouseMoved(MouseEvent e) {//for tooltips
                    int x = e.getX();
                    int y = e.getY();
                    String tips = m_ctrlPeptideArea.getToolTipText(x, y);
                    //if (tips != null) {
                    setToolTipText(tips);//null will turn off tooltip

                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    // TODO Auto-generated method stub

                }
            });
            this.addKeyListener(
                    new java.awt.event.KeyListener() {
                public void keyPressed(java.awt.event.KeyEvent evt) {
                    int oldSelected = m_ctrlPeptideArea.getSelectedIndex();
                    if (evt.getKeyCode() == KeyEvent.VK_UP) {
                        m_ctrlPeptideArea.setRelativeSelected(-1);
                    } else if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
                        m_ctrlPeptideArea.setRelativeSelected(1);
                    }
                    int selectedIndex = m_ctrlPeptideArea.getSelectedIndex();
                    if (oldSelected != selectedIndex) {
                        m_dataBox.propagateDataChanged(PTMPeptideInstance.class);
                        repaint();
                    }
                }

                @Override
                public void keyTyped(KeyEvent e) {
                    // TODO Auto-generated method stub
                }

                @Override
                public void keyReleased(KeyEvent e) {
                    // TODO Auto-generated method stub
                }
            });
        }

        public void updateData(){     
            m_ctrlPeptideArea.setPTMPepInstanceData(m_dataModel.getPTMPeptideInstance());
            this.m_ctrlPeptideArea.setSelectedIndex(0);
        }
        
        public void setBeginPoint(int x, int y) {
            m_ctrlPeptideArea.setBeginPoint(x, y);
        }

        @Override
        public Dimension getPreferredSize() {
            int width = (int) ((m_sequenceLength + AJUSTE_GAP - m_ajustedStartLocation) * ViewSetting.WIDTH_AA);
            int height = m_dataModel.getRowCount() * (ViewSetting.HEIGHT_AA * 2 - ViewSetting.HEIGHT_AA / 2);
            if (height == 0) {
                height = 5 * ViewSetting.HEIGHT_AA;
            }
            //logger.debug("PanelPtmDraw PeptidePane getPreferredSize(" + width + "," + height + ")");
            return new Dimension(width, height); //must be the all paint dimension in order to be visible in scroll panel

        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (!m_isDataNull) {
                ViewContext viewContext = new ViewContext();
                viewContext.setAjustedStartLocation(m_ajustedStartLocation).setAreaWidth(this.getWidth());
                Graphics2D g2 = (Graphics2D) g;
                m_ctrlPeptideArea.paint(g2, viewContext);
            }
        }
    }

    private class PeptideNumberPane extends JPanel {

        int m_x0, m_y0;

        private PeptideNumberPane() {
            super();
            this.setBackground(Color.WHITE);
        }

        public void setBeginPoint(int x, int y) {
            m_x0 = x;
            m_y0 = y;
        }

        @Override
        public Dimension getPreferredSize() {
            int width = ViewSetting.WIDTH_AA + 2 * ViewSetting.BORDER_GAP;
            int height = m_dataModel.getRowCount() * (ViewSetting.HEIGHT_AA * 2 - ViewSetting.HEIGHT_AA / 2);
            if (height == 0) {
                height = 5 * ViewSetting.HEIGHT_AA;
            }
            return new Dimension(width, height);

        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (!m_isDataNull) {
                Graphics2D g2 = (Graphics2D) g;
                FontMetrics f = g2.getFontMetrics(ViewSetting.FONT_NUMBER);
                int ascend = f.getAscent();
                int y0 = m_y0 + ascend;
                int x0;
                g2.setColor(Color.black);
                g2.setFont(ViewSetting.FONT_NUMBER);
//                    g2.setColor(Color.WHITE);
//                    g2.setFont(ViewSetting.FONT_PTM);
                String number;
                int stringWidth;
                int rowCount = m_dataModel.getRowCount();
                for (int i = 1; i < rowCount + 1; i++) {
                    number = String.valueOf(i);
                    stringWidth = f.stringWidth(number);
                    x0 = m_x0 + ViewSetting.WIDTH_AA - stringWidth;
                    g2.drawString("" + i, x0, y0);
                    y0 += (int) ViewSetting.HEIGHT_AA * 1.5;
                }

            }
        }

    }

}

    
}
