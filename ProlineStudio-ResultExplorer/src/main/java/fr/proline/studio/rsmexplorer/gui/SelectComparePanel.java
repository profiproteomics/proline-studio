package fr.proline.studio.rsmexplorer.gui;

import fr.proline.studio.comparedata.AbstractJoinDataModel;
import fr.proline.studio.comparedata.DiffDataModel;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxPanelInterface;
import java.awt.event.ActionListener;
import javax.swing.JPanel;
import fr.proline.studio.comparedata.CompareDataInterface;
import fr.proline.studio.comparedata.CompareDataProviderInterface;
import fr.proline.studio.comparedata.JoinDataModel;
import fr.proline.studio.graphics.CrossSelectionInterface;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

/**
 *
 * @author JM235353
 */
public class SelectComparePanel extends JPanel implements DataBoxPanelInterface, CompareDataProviderInterface {

    private AbstractDataBox m_dataBox;

    private AbstractJoinDataModel m_result = null;
    private CompareDataInterface m_compareDataInterface1;
    private CompareDataInterface m_compareDataInterface2;
    
    private JScrollPane m_dataScrollPane;
    private JComboBox m_algorithmCombobox;
    
    private DataRepresentation m_dataRepresentation1 = null;
    private DataRepresentation m_dataRepresentation2 = null;
    private KeyLinkRepresentation m_keyLinkRepresentation = null;
    private ResultLinkRepresentation m_resultLinkRepresentation = null;
    
    private final static String[] ALGOS = { "Difference", "Join" };
    private final static int ALGO_DIFF = 0;
    private final static int ALGO_JOIN = 1;
    
    public SelectComparePanel() {
        setLayout(new BorderLayout());
        setBounds(0, 0, 500, 400);
        setBackground(Color.white);


        JPanel internalPanel = initComponents();
        add(internalPanel, BorderLayout.CENTER);

        JToolBar toolbar = initToolbar();
        add(toolbar, BorderLayout.WEST);
    }
    
    
    private JPanel initComponents() {


        JPanel internalPanel = new JPanel();
        internalPanel.setBackground(Color.white);

        internalPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);


        // create objects
        m_dataScrollPane = new JScrollPane();

        JPanel graphPanel = createGraphPanel();

        m_dataScrollPane.setViewportView(graphPanel);
        m_dataScrollPane.setBackground(Color.white);


        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.gridwidth = 3;
        internalPanel.add(m_dataScrollPane, c);


        return internalPanel;

    }
    
    private JPanel createGraphPanel() {
        JPanel p = new JPanel(null) {
            @Override
            public void paint(Graphics g) {
                
                if (m_dataRepresentation1 != null) {
                    m_dataRepresentation1.draw(g);
                }
                if (m_dataRepresentation2 != null) {
                    m_dataRepresentation2.draw(g);
                }
                if (m_keyLinkRepresentation != null) {
                    m_keyLinkRepresentation.draw(g);
                }
                if (m_resultLinkRepresentation != null) {
                    m_resultLinkRepresentation.draw(g);
                
                    Dimension d = m_algorithmCombobox.getPreferredSize();
                    int x = m_resultLinkRepresentation.getCenterX()-d.width/2;
                    int y = m_resultLinkRepresentation.getCenterY()-d.height/2;
                    m_algorithmCombobox.setBounds(x, y, d.width, d.height);
                    
                    paintComponents(g);
                }
                
                
            }
        };
        p.setBackground(Color.white);
        
        m_algorithmCombobox = new JComboBox(ALGOS);
        m_algorithmCombobox.setSelectedIndex(0);
        m_algorithmCombobox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                applyAlgorithm();
            }

        });
        
        p.add(m_algorithmCombobox);

        return p;

    }
    
    private JToolBar initToolbar() {
        JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
        toolbar.setFloatable(false);

        //JPM.TODO

        return toolbar;
    }
    
    public void applyAlgorithm() {
        int algo = m_algorithmCombobox.getSelectedIndex();
        if (algo == ALGO_DIFF) {
            DiffDataModel diffModel = new DiffDataModel();
            diffModel.setData(m_compareDataInterface1, m_compareDataInterface2);

            m_result = diffModel;
        } else {  // ALGO_JOIN
            JoinDataModel joinDataModel = new JoinDataModel();
            joinDataModel.setData(m_compareDataInterface1, m_compareDataInterface2);
            
            m_result = joinDataModel;
        }
        
        m_dataBox.propagateDataChanged(CompareDataInterface.class);
    }
    
    public void setData(CompareDataInterface compareDataInterface1, CompareDataInterface compareDataInterface2) {
        if (compareDataInterface2 == null) {
            m_dataRepresentation1 = new DataRepresentation(compareDataInterface1, true);
            m_dataRepresentation1.setPosition(20, 20);
        } else {
            m_compareDataInterface1 = compareDataInterface1;
            m_compareDataInterface2 = compareDataInterface2;

            applyAlgorithm();
            
            m_dataRepresentation2 = new DataRepresentation(compareDataInterface2, false);
            m_dataRepresentation2.setPosition(m_dataRepresentation1.getX()+m_dataRepresentation1.getWidth()+40, 20);
            m_keyLinkRepresentation = new KeyLinkRepresentation(m_dataRepresentation1, m_dataRepresentation2, m_result);
            m_resultLinkRepresentation = new ResultLinkRepresentation(m_dataRepresentation1, m_dataRepresentation2);
        }

        
    }
           
    @Override
    public CompareDataInterface getCompareDataInterface() {
        return m_result;
    }

    @Override
    public void setDataBox(AbstractDataBox dataBox) {
        m_dataBox = dataBox;
    }
    @Override
    public AbstractDataBox getDataBox() {
        return m_dataBox;
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

    @Override
    public void setLoading(int id) {}

    @Override
    public void setLoading(int id, boolean calculating) {}

    @Override
    public void setLoaded(int id) {}

    @Override
    public CrossSelectionInterface getCrossSelectionInterface() {
        return null;
    }

    
    public static class DataRepresentation {
        
        private final CompareDataInterface m_dataInterface;
        private final boolean m_keysAtRight;
        
        private int m_x;
        private int m_y;
        private int m_height;
        private int m_width;
        
        private Font m_font = null;
        private Font m_fontBold = null;
        private int m_hgtBold;
        private int m_hgtPlain;
        private int m_ascentBold;
        //private int m_ascentPlain;
        
        private static final int MARGIN_X = 5;
        private static final int MARGIN_Y = 5;
        
        public DataRepresentation(CompareDataInterface dataInterface, boolean keysAtRight) {
            m_dataInterface = dataInterface;
            m_keysAtRight = keysAtRight;
        }
        
        public void setPosition(int x, int y) {
            m_x = x;
            m_y = y;
        }
        
        public int getX() {
            return m_x;
        }
        public int getY() {
            return m_y;
        }
        public int getWidth() {
            return m_width;
        }
        public int getHeight() {
            return m_height;
        }
        
        public void draw(Graphics g) {
            if (m_font == null) {
                m_font = new Font(" TimesRoman ", Font.PLAIN, 11);
                m_fontBold = m_font.deriveFont(Font.BOLD);
                
                FontMetrics metricsBold = g.getFontMetrics(m_fontBold);
                FontMetrics metricsPlain = g.getFontMetrics(m_font);

                m_hgtBold = metricsBold.getHeight();
                m_ascentBold = metricsBold.getAscent();
                
                
                m_hgtPlain = metricsPlain.getHeight();
                //m_ascentPlain = metricsPlain.getAscent();
                
                
                int nbData = m_dataInterface.getColumnCount();
                
                String name = m_dataInterface.getName();
                int maxSize = metricsBold.stringWidth(name);
                
                for (int i=0;i<nbData;i++) {
                    String text = m_dataInterface.getDataColumnIdentifier(i);
                    int size = metricsPlain.stringWidth(text);
                    if (size > maxSize) {
                        maxSize = size;
                    }
                }
                
                m_width = MARGIN_X*2+maxSize;
                m_height = m_hgtBold+nbData*m_hgtPlain+(nbData+2)*MARGIN_Y;

            }
            
            g.setColor(new Color(255,204,99));
            g.fillRect(m_x, m_y, m_width, m_hgtBold+MARGIN_Y+MARGIN_Y/2);

            g.setColor(Color.black);
            g.drawRect(m_x, m_y, m_width, m_hgtBold+MARGIN_Y+MARGIN_Y/2);
            g.drawRect(m_x, m_y, m_width, m_height);

            
            
            g.setFont(m_fontBold);
            g.drawString( m_dataInterface.getName(), m_x+MARGIN_X, m_y+MARGIN_Y+m_ascentBold);
            
            g.setFont(m_font);
            int nbData = m_dataInterface.getColumnCount();
            for (int i = 0; i < nbData; i++) {
                String text = m_dataInterface.getDataColumnIdentifier(i);
                g.drawString( text, m_x+MARGIN_X, m_y+MARGIN_Y+m_ascentBold+m_hgtBold+(MARGIN_Y+m_hgtPlain)*i);
            }
            
            int[] keysColumn = m_dataInterface.getKeysColumn();
            for (int i=0;i<keysColumn.length;i++) {
                int columnIndex = keysColumn[i];
                int diameter = m_hgtPlain/3;
                if (m_keysAtRight) {
                    g.drawRect(m_x+m_width, m_y+MARGIN_Y+m_ascentBold+m_hgtBold+(MARGIN_Y+m_hgtPlain)*columnIndex-(m_hgtPlain-diameter)/2, diameter, diameter);
                    //g.drawArc(m_x+m_width-diameter/2+1,m_y+MARGIN_Y+m_ascentBold+m_hgtBold+(MARGIN_Y+m_hgtPlain)*columnIndex-m_hgtPlain/2 , diameter, diameter, 270, 180);
                } else {
                    g.drawRect(m_x-diameter, m_y+MARGIN_Y+m_ascentBold+m_hgtBold+(MARGIN_Y+m_hgtPlain)*columnIndex-(m_hgtPlain-diameter)/2, diameter, diameter);
                    //g.drawArc(m_x-diameter+1,m_y+MARGIN_Y+m_ascentBold+m_hgtBold+(MARGIN_Y+m_hgtPlain)*columnIndex-m_hgtPlain/2 , diameter, diameter, 90, 180);
                }
            }
            
            
        }
        
        public void getKeyPosition(int columnIndex, Point p) {
            int diameter = m_hgtPlain/3;
            if (m_keysAtRight) {
                p.x = m_x+m_width+diameter/2;
                p.y = m_y+MARGIN_Y+m_ascentBold+m_hgtBold+(MARGIN_Y+m_hgtPlain)*columnIndex-(m_hgtPlain-diameter)/2+diameter/2;
            } else {
                p.x = m_x-diameter+diameter/2;
                p.y = m_y+MARGIN_Y+m_ascentBold+m_hgtBold+(MARGIN_Y+m_hgtPlain)*columnIndex-(m_hgtPlain-diameter)/2 +diameter/2;
            }
        }
        
    }
    
    public class KeyLinkRepresentation {
        
        private final DataRepresentation m_dataRepresentation1;
        private final DataRepresentation m_dataRepresentation2;
        
        private final Point m_p1 = new Point();
        private final Point m_p2 = new Point();
        
        private final AbstractJoinDataModel m_joinModel;
        
         public KeyLinkRepresentation(DataRepresentation dataRepresentation1, DataRepresentation dataRepresentation2, AbstractJoinDataModel joinModel) {
            m_dataRepresentation1 = dataRepresentation1;
            m_dataRepresentation2 = dataRepresentation2;
            m_joinModel = joinModel;
        }
         
         public void draw(Graphics g) {
             g.setColor(Color.blue);
             
             m_dataRepresentation1.getKeyPosition(m_joinModel.getSelectedKey1(), m_p1);
             m_dataRepresentation2.getKeyPosition(m_joinModel.getSelectedKey1(), m_p2);
             
             g.drawLine(m_p1.x, m_p1.y, m_p2.x, m_p2.y);
         }
    }
    
    public class ResultLinkRepresentation {
        
        private final DataRepresentation m_dataRepresentation1;
        private final DataRepresentation m_dataRepresentation2;
        
        private final int HEIGHT_START_SEGMENT = 10;
        private final int HEIGHT_RESULT_SEGMENT = 40;
        
        private int m_centerX;
        private int m_centerY;
        
        public ResultLinkRepresentation(DataRepresentation dataRepresentation1, DataRepresentation dataRepresentation2) {
            m_dataRepresentation1 = dataRepresentation1;
            m_dataRepresentation2 = dataRepresentation2;
        }
        
        public int getCenterX() {
            return m_centerX;
        }
        
        public int getCenterY() {
            return m_centerY;
        }
        
        public void draw(Graphics g) {
             g.setColor(Color.black);
             
             int x1 = m_dataRepresentation1.getX()+ m_dataRepresentation1.getWidth()/2;
             int x2 = m_dataRepresentation2.getX()+ m_dataRepresentation2.getWidth()/2;
             int y1 = m_dataRepresentation1.getY()+ m_dataRepresentation1.getHeight();
             int y2 = m_dataRepresentation2.getY()+ m_dataRepresentation2.getHeight();
             
             int yHorizontalLine = Math.max(y1, y2)+HEIGHT_START_SEGMENT;
             
             m_centerX = (x1+x2)/2;
             
             m_centerY = yHorizontalLine+HEIGHT_RESULT_SEGMENT/2;
             
             
             g.drawLine(x1, y1, x1, yHorizontalLine);
             g.drawLine(x2, y2, x2, yHorizontalLine);
             g.drawLine(x1, yHorizontalLine, x2, yHorizontalLine);
             g.drawLine(m_centerX, yHorizontalLine, m_centerX, yHorizontalLine+HEIGHT_RESULT_SEGMENT);
         }
    }
    
}
