package fr.proline.studio.gui;

import fr.proline.studio.utils.IconManager;
import fr.proline.studio.utils.MiscellaneousUtils;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.*;
import org.slf4j.LoggerFactory;

/**
 * All Dialogs of this application must extend this class
 * which offers a common behaviour
 * @author JM235353
 */
public class DefaultDialog extends javax.swing.JDialog {

    public static final int BUTTON_OK = 0;
    public static final int BUTTON_CANCEL = 1;
    public static final int BUTTON_DEFAULT = 2;
    public static final int BUTTON_LOAD = 3;
    public static final int BUTTON_SAVE = 4;
    public static final int BUTTON_HELP = 5;
    public static final int BUTTON_BACK = 6;
    private static final int BUTTONS_NUMBER = 7; // ---- get in sync
    
    private JPanel m_internalPanel;
    private ImagePanel m_imageInfoPanel;
    
    private JButton[] m_buttons;


    private JPanel m_statusPanel;
    private JLabel m_statusLabel;
    
    private BusyGlassPane m_busyGlassPane = null;
    private HighlightGlassPane m_highlightGlassPane = null;

    private DefaultDialog m_dialog;
    
    private boolean m_firstDisplay = true;
    
    protected int m_buttonClicked = BUTTON_CANCEL;
    
    private String m_helpURL = null;
     
    /**
     * Creates new form AbstractDialog
     */
    public DefaultDialog() {
        this(null, Dialog.ModalityType.MODELESS);
    }
    public DefaultDialog(Window parent) {
        this(parent, Dialog.ModalityType.APPLICATION_MODAL);
    }
    public DefaultDialog(Window parent, Dialog.ModalityType modalityType) {
        super(parent, modalityType);
        
        m_buttons = new JButton[BUTTONS_NUMBER];
        
        // Action when the user press on the dialog cross
        addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent we) {
                cancelButtonActionPerformed();
            }
        });
        
        // Escape Key Action
        KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        Action actionListener = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                cancelButtonActionPerformed();
            }
        };
        InputMap inputMap = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        inputMap.put(stroke, "ESCAPE");
        getRootPane().getActionMap().put("ESCAPE", actionListener);

        // Enter Action
        stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
        actionListener = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                doClick(BUTTON_OK);
            }
        };
        inputMap.put(stroke, "ENTER");
        getRootPane().getActionMap().put("ENTER", actionListener);
        
        
        initComponents(); 

    }

    public void setHelpURL(String helpURL) {
        m_helpURL = helpURL;
    }
    
    public void setImageInfo(JPanel sourcePanel, int x, int y, int width, int height) {
        m_imageInfoPanel.setVisible(true);
        m_imageInfoPanel.setImageInfo(sourcePanel, x, y, width, height);
    }
    
    public void hideInfoPanel() {
        m_imageInfoPanel.setVisible(false);
        revalidate();
    }
    
    @Override
    public void setVisible(boolean v) {
        pack();

        if (v) {
            // reinit button when dialog is opened
            m_buttonClicked = BUTTON_CANCEL;
        }
        
        m_dialog = this;
        
        super.setVisible(v);
        

    }
    
    @Override
     public void pack() {
         if (m_firstDisplay) {
            super.pack();
            m_firstDisplay = false;
         }
     }
    
    public void repack() {
        super.pack();
    }

    public int getButtonClicked() {
        return m_buttonClicked;
    }
    
    
    protected void setInternalComponent(Component component) {

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.weightx = 1;
        c.weighty = 1;

        m_internalPanel.add(component, c);
 
    }
    
    protected void replaceInternaleComponent(Component component) {
        m_internalPanel.removeAll();
        setInternalComponent(component);
    }

    public void setButtonVisible(int buttonId, boolean visible) {
        m_buttons[buttonId].setVisible(visible);

    }
    
    public void setButtonEnabled(int buttonId, boolean enabled) {
        m_buttons[buttonId].setEnabled(enabled);
    }
    
    public void setButtonName(int buttonId, String name) {
        m_buttons[buttonId].setText(name);
    }
    
    protected void setButtonIcon(int buttonId, Icon icon) {
        m_buttons[buttonId].setIcon(icon);
    }
    
    protected void doClick(int buttonId) {
        m_buttons[buttonId].doClick();
    }
    
    protected void setStatusVisible(boolean visible) {
        m_statusPanel.setVisible(visible);
    }
    
    
    public void setStatus(boolean error, String text) {
        if (error) {
            m_statusLabel.setIcon(IconManager.getIcon(IconManager.IconType.EXCLAMATION));
            
            if (statusStimer == null) {
                final int DELAY_TO_ERASE_STATUS = 7000;
                statusStimer = new Timer(DELAY_TO_ERASE_STATUS, new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        m_statusLabel.setIcon(IconManager.getIcon(IconManager.IconType.EMPTY));
                        m_statusLabel.setText("");
                    }
                    
                });
                statusStimer.setRepeats(false);
            }
            if (!statusStimer.isRunning()) {
                statusStimer.start();
            } else {
                statusStimer.restart();
            }
            
        } else {
            m_statusLabel.setIcon(IconManager.getIcon(IconManager.IconType.EMPTY));
        }
        m_statusLabel.setText(text);
        

    }
    private Timer statusStimer = null;
    
    
    
    
    protected boolean cancelCalled() {
        return true;
    }
    
    protected boolean okCalled() {
        return true;
    }
        
    protected boolean defaultCalled() {
        return false;
    }
    
    protected boolean backCalled() {
        return false;
    }
    
    protected boolean saveCalled() {
        return false;
    }
    
    protected boolean loadCalled() {
        return false;
    }
    
    
    private void initComponents() {

        setResizable(false);
        
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        
        setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.weightx = 1;

        m_imageInfoPanel = new ImagePanel();
        m_imageInfoPanel.setVisible(false);
        add(m_imageInfoPanel, c);
        
        c.gridy++;
        c.weighty = 1;
        m_internalPanel = new JPanel();
        m_internalPanel.setLayout(new GridBagLayout());
        add(m_internalPanel, c);
        
        JPanel buttonPanel = createButtonPanel();
        c.gridy++;
        c.weighty = 0;
        add(buttonPanel, c);
        
        m_statusPanel = createStatusPanel();
        c.gridy++;
        c.weightx = 1;
        add(m_statusPanel, c);
    }
  
    private JPanel createButtonPanel() {
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridBagLayout());
        
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5,5,5,5);
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.weightx = 0;
        c.weighty = 0;
        
        m_buttons[BUTTON_OK] = new JButton(IconManager.getIcon(IconManager.IconType.OK));
        m_buttons[BUTTON_CANCEL] = new JButton(IconManager.getIcon(IconManager.IconType.CANCEL));
        m_buttons[BUTTON_DEFAULT] = new JButton(IconManager.getIcon(IconManager.IconType.DEFAULT));
        m_buttons[BUTTON_BACK] = new JButton(IconManager.getIcon(IconManager.IconType.BACK));
        m_buttons[BUTTON_LOAD] = new JButton(IconManager.getIcon(IconManager.IconType.LOAD_SETTINGS));
        m_buttons[BUTTON_SAVE] = new JButton(IconManager.getIcon(IconManager.IconType.SAVE_SETTINGS));
        m_buttons[BUTTON_HELP] = new JButton(IconManager.getIcon(IconManager.IconType.QUESTION));
        Insets margin = m_buttons[BUTTON_HELP].getMargin();
        margin.left=3;
        margin.right=3;
        m_buttons[BUTTON_HELP].setMargin(margin);
        
        
        buttonPanel.add(m_buttons[BUTTON_SAVE], c);
        
        c.gridx++;
        buttonPanel.add(m_buttons[BUTTON_LOAD], c);
        
        c.gridx++;
        buttonPanel.add(m_buttons[BUTTON_DEFAULT], c);
        
        c.gridx++;
        c.weightx = 1;
        buttonPanel.add(Box.createHorizontalGlue(), c);
        
        c.gridx++;
        c.weightx = 0;
        buttonPanel.add(m_buttons[BUTTON_BACK], c);
        
        c.gridx++;
        buttonPanel.add(m_buttons[BUTTON_OK], c);
        
        c.gridx++;
        buttonPanel.add(m_buttons[BUTTON_CANCEL], c);
        
        c.gridx++;
        buttonPanel.add(m_buttons[BUTTON_HELP], c);
        
        m_buttons[BUTTON_SAVE].setText(org.openide.util.NbBundle.getMessage(DefaultDialog.class, "DefaultDialog.saveButton.text"));
        setButtonVisible(BUTTON_SAVE, false);
        m_buttons[BUTTON_SAVE].addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed();
            }
        });
        
        m_buttons[BUTTON_LOAD].setText(org.openide.util.NbBundle.getMessage(DefaultDialog.class, "DefaultDialog.loadButton.text"));
        setButtonVisible(BUTTON_LOAD, false);
        m_buttons[BUTTON_LOAD].addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadButtonActionPerformed();
            }
        });
        
        m_buttons[BUTTON_OK].setText(org.openide.util.NbBundle.getMessage(DefaultDialog.class, "DefaultDialog.okButton.text"));
        m_buttons[BUTTON_OK].addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed();
            }
        });

        m_buttons[BUTTON_CANCEL].setText(org.openide.util.NbBundle.getMessage(DefaultDialog.class, "DefaultDialog.cancelButton.text"));
        m_buttons[BUTTON_CANCEL].addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed();
            }
        });

        m_buttons[BUTTON_DEFAULT].setText(org.openide.util.NbBundle.getMessage(DefaultDialog.class, "DefaultDialog.defaultButton.text"));
        setButtonVisible(BUTTON_DEFAULT, false);
        m_buttons[BUTTON_DEFAULT].addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                defaultButtonActionPerformed();
            }
        });
        
        m_buttons[BUTTON_BACK].setText(org.openide.util.NbBundle.getMessage(DefaultDialog.class, "DefaultDialog.backButton.text"));
        setButtonVisible(BUTTON_BACK, false);
        m_buttons[BUTTON_BACK].addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backButtonActionPerformed();
            }
        });
        
        //m_helpButton.setText(org.openide.util.NbBundle.getMessage(DefaultDialog.class, "DefaultDialog.helpButton.text"));
        m_buttons[BUTTON_HELP].addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                helpButtonActionPerformed();
            }
        });


        return buttonPanel;
    }

    
    private void saveButtonActionPerformed() {
        if (saveCalled()) {
            m_buttonClicked = BUTTON_SAVE;
        }
    }

    private void loadButtonActionPerformed() {
        if (loadCalled()) {
            m_buttonClicked = BUTTON_LOAD;
        }
    }

     
    private void cancelButtonActionPerformed() {                                             
        if (cancelCalled()) {
            m_buttonClicked = BUTTON_CANCEL;
            setVisible(false);
        }
    }                                            

    private void okButtonActionPerformed() {                                         
        if (okCalled()) {
            m_buttonClicked = BUTTON_OK;
            setVisible(false);
        }
    }                                        

    private void defaultButtonActionPerformed() {                                         
        if (defaultCalled()) {
            m_buttonClicked = BUTTON_DEFAULT;
            setVisible(false);
        }
    } 
    
    private void backButtonActionPerformed() {
        if (backCalled()) {
            m_buttonClicked = BUTTON_BACK;
        }
    }
    
    
    
    private void helpButtonActionPerformed() {
        // Show help
        if (m_helpURL == null) {
            LoggerFactory.getLogger("ProlineStudio.Commons").error(getClass().getSimpleName() + " failed");
            return; // should not happen
        }
        if (Desktop.isDesktopSupported()) { // JDK 1.6.0

            try {
                //Desktop.getDesktop().browse(new URL(MiscellaneousUtils.convertURLToCurrentHelp(m_helpURL)).toURI());
                Desktop.getDesktop().browse(MiscellaneousUtils.createRedirectTempFile(m_helpURL));
            } catch (Exception ex) {
                LoggerFactory.getLogger("ProlineStudio.Commons").error(getClass().getSimpleName() + " failed", ex);
            }
        }
    }
    

    private JPanel createStatusPanel() {
        
        JPanel statusPanel = new JPanel();
        statusPanel.setLayout(new GridBagLayout());
        statusPanel.setBorder(BorderFactory.createLoweredSoftBevelBorder());
        
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(1,1,1,1);
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.weightx = 1;
        c.weighty = 0;
        
        m_statusLabel = new JLabel(" ");
        m_statusLabel.setIcon(IconManager.getIcon(IconManager.IconType.EMPTY));
        
        statusPanel.add(m_statusLabel, c);
        
        
        return statusPanel;
    }
    
    @Override
    public void setLocation(int x, int y) {
        
        // pack must have been done beforehand
        pack();
        
        // we do not allow the dialog to be partially out of the screen
        
        // top left corner check
        if (x<0) {
            x = 0;
        }
        if (y<0) {
            y = 0;
        }
        
        // bottom right corner check
        int width = getWidth(); 
        int height = getHeight()+30; // +30 is for window task bar
        
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        if (x+width>screenSize.width) {
            x = screenSize.width-width;
        }
        if (y+height>screenSize.height) {
            y = screenSize.height-height;
        }

        super.setLocation(x, y);
    }
    
    
    public void centerToWindow(Window w) {
        
        // pack must have been done beforehand
        pack();
        
        int width = getWidth();
        int height = getHeight();
        
        int frameX = w.getX();
        int frameY = w.getY();
        int frameWidth = w.getWidth();
        int frameHeight = w.getHeight();
      
        int x = frameX+(frameWidth-width)/2;
        int y = frameY+(frameHeight-height)/2;
        
        setLocation(x, y);
         
    }


    public void centerToScreen() {
        
        // pack must have been done beforehand
        pack();
        
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        int width = getWidth();
        int height = getHeight();

        int x = (screenSize.width-width)/2;
        int y = (screenSize.height-height)/2;
        
        super.setLocation(x, y);

    }
    
    public void setBusy(boolean busy) {
        
        if (m_busyGlassPane == null) {
            m_busyGlassPane = new BusyGlassPane();
        }
        
        setGlassPane(m_busyGlassPane);
        
        if (busy) {
            m_busyGlassPane.setVisible(true);
            
        } else {
            m_busyGlassPane.setVisible(false);
        }
    }
    
    public void startTask(ProgressTask task) {
        setBusy(true);
        m_busyGlassPane.setProgressBar(task);
        
    }
    
    
    public void highlight(Component c) {
        
        highlight(c, null);
    }
    
    public void highlight(Component c, Rectangle zone) {

        if (m_highlightGlassPane == null) {
            m_highlightGlassPane = new HighlightGlassPane();
        }
        m_highlightGlassPane.setComponent(c, zone);

        setGlassPane(m_highlightGlassPane);


        m_highlightGlassPane.highlight();


    }
    
    
    
    /**
     * Glass Pane to set the dialog as busy
     */
    private class BusyGlassPane extends JComponent implements PropertyChangeListener {

        private boolean m_firstProgressDisplay = true;
        private JPanel m_progressPanel;
        private JProgressBar m_progressBar;
        
        
        public BusyGlassPane() {

            setLayout(null);
            
            // get rid of all mouse events
            MouseAdapter mouseAdapter = new MouseAdapter() {
            };
            addMouseListener(mouseAdapter);
            addMouseMotionListener(mouseAdapter);

            // set wait mouse cursor
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            
            
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            if (m_progressBar != null) {

                if (m_firstProgressDisplay) {
                    m_progressPanel.setBounds(30, getHeight()/2-16, getWidth()-60, 32);
                    m_progressBar.setBounds(6, 6, m_progressPanel.getWidth()-12, m_progressPanel.getHeight()-12);

                    m_firstProgressDisplay = false;
                }
                
                Color ppColor = new Color(140, 140, 140, 70);
                g.setColor(ppColor);
                g.fillRect(0, 0, getWidth()-1, getHeight()-1);
            }
        }
        
        protected void setProgressBar(ProgressTask task) {
            
            removeAll();
            
            m_firstProgressDisplay = true;
            
            m_progressPanel = new JPanel(null) {
                
                @Override
                public void paint(Graphics g) {
                    super.paint(g);
                    
                    g.setColor(Color.darkGray);
                    g.drawRect(1, 1, getWidth()-3, getHeight()-3);
                }
            };

            m_progressPanel.setOpaque(true);
            m_progressPanel.setBackground(Color.white);

            
            m_progressBar = new JProgressBar(task.getMinValue(), task.getMaxValue());
            m_progressBar.setIndeterminate(true);
            m_progressBar.setStringPainted(false);

            m_progressPanel.add(m_progressBar);
            
            
            
            add(m_progressPanel);

            task.addPropertyChangeListener(this);
            task.execute();
            
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if ("progress".equals(evt.getPropertyName())) {
                int progress = (Integer) evt.getNewValue();
                if ((progress>0) && (m_progressBar.isIndeterminate())) {
                    m_progressBar.setIndeterminate(false);
                    m_progressBar.setStringPainted(true);
                }
                m_progressBar.setValue(progress);
                if (progress >= m_progressBar.getMaximum()) {
                    setBusy(false);
                    m_dialog.setVisible(false);
                }
            }
        }
        
        
    }
    
    /**
     * Glass Pane to highlight a component of the dialog
     */
    private class HighlightGlassPane extends JComponent {

        private final int ANIMATION_DELAY = 400;
        private final int DISPLAY_DELAY = 1300;
        
        private long m_timeStart = 0;
        private Component m_comp;
        private int m_x, m_y, m_width, m_height;
        
        public HighlightGlassPane() {
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            
            Point p = SwingUtilities.convertPoint(m_comp, 0, 0, this); 
            
            
            g.setColor(new Color(240,0,0));
            ((Graphics2D)g).setStroke(new BasicStroke(2));
            
            long timeCur = System.currentTimeMillis();
            
            
            final int START_ANGLE = 30;
            final int DELTA_ANGLE = 380;
            int deltaAngle;
            if (timeCur-m_timeStart<=ANIMATION_DELAY) {
                deltaAngle = START_ANGLE + (int) ((DELTA_ANGLE-START_ANGLE)*(((double)(timeCur-m_timeStart))/ANIMATION_DELAY));
            } else {
                deltaAngle = DELTA_ANGLE;
            }
            for (int angle=START_ANGLE;angle<=deltaAngle+START_ANGLE;angle+=3) {
                int delta =  (int) (((double)(angle-START_ANGLE)/((double)DELTA_ANGLE))*10.0); //(DELTA_ANGLE-angle-START_ANGLE)/20;

                g.drawArc(p.x+m_x-delta, p.y+m_y-delta, m_width+delta*2, m_height+delta*2,angle, 3);
            }
            

        }

        private void setComponent(Component c) {

            m_comp = c;
            
            m_x = -PAD;
            m_y = -PAD;
            m_width = c.getWidth()+PAD*2;
            m_height = c.getHeight()+PAD*2;
        }
        
        public void setComponent(Component c, Rectangle r) {

            if (r == null) {
                setComponent(c);
                return;
            }

            m_comp = c;

            m_x = -PAD+r.x;
            m_y = -PAD+r.y;
            m_width = r.width + PAD * 2;
            m_height = r.height + PAD * 2;
        }
        private static final int PAD = 10;
        
        public void highlight() {
            
            m_highlightGlassPane.setVisible(true);
            
            m_timeStart = System.currentTimeMillis();

            
            ActionListener timerAction = new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    
                    long timeCur = System.currentTimeMillis();
                    if ((timeCur-m_timeStart)>ANIMATION_DELAY+DISPLAY_DELAY) {
                        m_highlightGlassPane.setVisible(false);
                        animationTimer.setRepeats(false);
                    } else {
                        repaint();
                    }
                }
            };
            
            animationTimer = new Timer(20, timerAction);
            animationTimer.setRepeats(true);
            animationTimer.start();
            
        }
        private Timer animationTimer = null;
        
        
        
        @Override
        protected void processMouseEvent(MouseEvent e) {
            if (e.getID() == MouseEvent.MOUSE_CLICKED) {
                m_statusLabel.setIcon(IconManager.getIcon(IconManager.IconType.EMPTY));
                m_statusLabel.setText("");
            }
            super.processMouseEvent(e);

        }
    }

    
    public static abstract class ProgressTask extends SwingWorker {
        
        public abstract int getMinValue();
        public abstract int getMaxValue();
        
    }
    
    private class ImagePanel extends JPanel {

        private BufferedImage m_bi = null;
        private final Dimension m_dimension = new Dimension(0, 0);

        public ImagePanel() {
        }

        @Override
        public Dimension getPreferredSize() {
            return m_dimension;
        }
        
        public void paint(Graphics g) {
            if (m_bi == null) {
                return;
            }
            
            g.setColor(Color.white);
            g.fillRect(0, 0, getWidth(), getHeight());
            
            
            int xImage = (getWidth()-m_bi.getWidth())/2;
            int yImage = 1;
            g.drawImage(m_bi, xImage, yImage, null);
            
            g.setColor(Color.darkGray);
            g.drawRect(xImage-1,yImage-1,m_bi.getWidth()+1,m_bi.getHeight()+1);
            
        }

        public void setImageInfo(JPanel sourcePanel, int x, int y, int width, int height) {
            m_bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            m_dimension.width = width+2;
            m_dimension.height = height+2;
            Graphics g = m_bi.getGraphics();
            g.translate(-x, -y);
            sourcePanel.paint(g);
        }

    }
    
}
