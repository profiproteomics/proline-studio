package fr.proline.studio.gui;

import fr.proline.studio.utils.IconManager;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * All Dialogs of this application must extend this class
 * which offers a common behaviour
 * @author JM235353
 */
public class DefaultDialog extends javax.swing.JDialog {

    public static final int BUTTON_OK = 0;
    public static final int BUTTON_CANCEL = 1;
    public static final int BUTTON_DEFAULT = 2;
    
    private JPanel m_internalPanel;
    
    private JButton m_okButton;
    private JButton m_cancelButton;
    private JButton m_defaultButton;

    private JPanel m_statusPanel;
    private JLabel m_statusLabel;
    
    private BusyGlassPane m_busyGlassPane = null;
    private HighlightGlassPane m_highlightGlassPane = null;
    
    
    private boolean m_firstDisplay = true;
    
    protected int m_buttonClicked = BUTTON_CANCEL;
    
     
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
        
        addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent we) {
                cancelButtonActionPerformed();
            }
        });
        
        initComponents(); 

    }

    @Override
    public void setVisible(boolean v) {
        pack();

        if (v) {
            // reinit button when dialog is opened
            m_buttonClicked = BUTTON_CANCEL;
        }
        
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

    public void setButtonVisible(int buttonId, boolean visible) {
        switch (buttonId) {
            case BUTTON_OK:
                m_okButton.setVisible(visible);
                break;
            case BUTTON_CANCEL:
                m_cancelButton.setVisible(visible);
                break;
            case BUTTON_DEFAULT:
                m_defaultButton.setVisible(visible);
                break;
                
        }
    }
    
    protected void setButtonName(int buttonId, String name) {
        switch (buttonId) {
            case BUTTON_OK:
                m_okButton.setText(name);
                break;
            case BUTTON_CANCEL:
                m_cancelButton.setText(name);
                break;
            case BUTTON_DEFAULT:
                m_defaultButton.setText(name);
                break;
        }
    }
    
    protected void setButtonIcon(int buttonId, Icon icon) {
        switch (buttonId) {
            case BUTTON_OK:
                m_okButton.setIcon(icon);
                break;
            case BUTTON_CANCEL:
                m_cancelButton.setIcon(icon);
                break;
            case BUTTON_DEFAULT:
                m_defaultButton.setIcon(icon);
                break;
        }
    }
    
    protected void doClick(int buttonId) {
        switch (buttonId) {
            case BUTTON_OK:
                m_okButton.doClick();
                break;
            case BUTTON_CANCEL:
                m_cancelButton.doClick();
                break;
            case BUTTON_DEFAULT:
                m_defaultButton.doClick();
                break;
        }
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
        
        m_okButton = new JButton(IconManager.getIcon(IconManager.IconType.OK));
        m_okButton.setDefaultCapable(true);
        getRootPane().setDefaultButton(m_okButton);
        m_cancelButton = new JButton(IconManager.getIcon(IconManager.IconType.CANCEL));
        m_defaultButton = new JButton(IconManager.getIcon(IconManager.IconType.DEFAULT));
        
        buttonPanel.add(m_defaultButton, c);
        
        c.gridx++;
        c.weightx = 1;
        buttonPanel.add(Box.createHorizontalGlue(), c);
        
        c.gridx++;
        c.weightx = 0;
        buttonPanel.add(m_okButton, c);
        
        c.gridx++;
        buttonPanel.add(m_cancelButton, c);
        
        m_okButton.setText(org.openide.util.NbBundle.getMessage(DefaultDialog.class, "DefaultDialog.okButton.text"));
        m_okButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed();
            }
        });

        m_cancelButton.setText(org.openide.util.NbBundle.getMessage(DefaultDialog.class, "DefaultDialog.cancelButton.text"));
        m_cancelButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed();
            }
        });

        m_defaultButton.setText(org.openide.util.NbBundle.getMessage(DefaultDialog.class, "DefaultDialog.defaultButton.text"));
        m_defaultButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                defaultButtonActionPerformed();
            }
        });


        return buttonPanel;
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
    
    
    public void centerToFrame(Frame f) {
        
        // pack must have been done beforehand
        pack();
        
        int width = getWidth();
        int height = getHeight();
        
        int frameX = f.getX();
        int frameY = f.getX();
        int frameWidth = f.getWidth();
        int frameHeight = f.getHeight();
      
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
    
    public void highlight(Component c) {
        
        if (m_highlightGlassPane == null) {
            m_highlightGlassPane = new HighlightGlassPane();
        }
        m_highlightGlassPane.setComponent(c);
                
        setGlassPane(m_highlightGlassPane);
        
        
        m_highlightGlassPane.highlight();
        

    }
    
    
    
    /**
     * Glass Pane to set the dialog as busy
     */
    private class BusyGlassPane extends JComponent {

        public BusyGlassPane() {

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
        }
    }
    
    /**
     * Glass Pane to highlight a component of the dialog
     */
    private class HighlightGlassPane extends JComponent {

        private final int ANIMATION_DELAY = 400;
        private final int DISPLAY_DELAY = 1300;
        
        private long timeStart = 0;
        private Component comp;
        private int x,y,width,height;
        
        public HighlightGlassPane() {
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            
            Point p = SwingUtilities.convertPoint(comp, 0, 0, this); 
            
            
            g.setColor(new Color(240,0,0));
            ((Graphics2D)g).setStroke(new BasicStroke(2));
            
            long timeCur = System.currentTimeMillis();
            
            
            final int START_ANGLE = 30;
            final int DELTA_ANGLE = 380;
            int deltaAngle;
            if (timeCur-timeStart<=ANIMATION_DELAY) {
                deltaAngle = START_ANGLE + (int) ((DELTA_ANGLE-START_ANGLE)*(((double)(timeCur-timeStart))/ANIMATION_DELAY));
            } else {
                deltaAngle = DELTA_ANGLE;
            }
            for (int angle=START_ANGLE;angle<=deltaAngle+START_ANGLE;angle+=3) {
                int delta =  (int) (((double)(angle-START_ANGLE)/((double)DELTA_ANGLE))*10.0); //(DELTA_ANGLE-angle-START_ANGLE)/20;

                g.drawArc(p.x+x-delta, p.y+y-delta, width+delta*2, height+delta*2,angle, 3);
            }
            

        }

        public void setComponent(Component c) {
            
            final int PAD = 10;
            
            comp = c;
            
            x = -PAD;
            y = -PAD;
            width = c.getWidth()+PAD*2;
            height = c.getHeight()+PAD*2;
        }
        
        public void highlight() {
            
            m_highlightGlassPane.setVisible(true);
            
            timeStart = System.currentTimeMillis();

            
            ActionListener timerAction = new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    
                    long timeCur = System.currentTimeMillis();
                    if ((timeCur-timeStart)>ANIMATION_DELAY+DISPLAY_DELAY) {
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

    
}
