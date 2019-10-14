/* 
 * Copyright (C) 2019 VD225637
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1 
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License 
 * along with this program; If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
 */
package fr.proline.mzscope.utils;

import fr.proline.studio.utils.IconManager;
import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.event.*;
import java.util.EventListener;
import javax.swing.event.EventListenerList;

/**
 * Component to be used as tabComponent; Contains a JLabel to show the text and
 * a JButton to close the tab it belongs to
 */
public class ButtonTabComponent extends JPanel{

    //events
    private EventListenerList closeListenerList = new EventListenerList();
    
    private JButton waitingButton = null;
    private JButton closeButton = null;

    public ButtonTabComponent(String text) {
        //unset default FlowLayout' gaps
        super(new FlowLayout(FlowLayout.LEFT, 0, 0));
        setOpaque(false);
        final String tabTile = text;

        //make JLabel read titles from JTabbedPane
        JLabel label = new JLabel() {
            @Override
            public String getText() {
                return tabTile;
            }
        };

        add(label);
        //add more space between the label and the button
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
        //tab button
        closeButton = new TabButton();
        add(closeButton);
        //add more space to the top of the component
        setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
    }
    
    /* display an icon for waiting process */
    public void setWaitingState(boolean waitingState){
        if (waitingState){
            if( closeButton != null){
                this.closeButton.setEnabled(false);
            }
            this.add(getWaitingButton(), 0);
        }else if (this.waitingButton != null){
            this.remove(0);
            this.waitingButton = null;
            if( closeButton != null){
                this.closeButton.setEnabled(true);
            }
        }
        repaint();
    }
    
    private JButton getWaitingButton(){
        if (this.waitingButton == null){
            this.waitingButton = new JButton(IconManager.getIcon(IconManager.IconType.HOUR_GLASS_MINI9));
            this.waitingButton.setToolTipText("loading data");
            //Make the button looks the same for all Laf's
            this.waitingButton.setUI(new BasicButtonUI());
            //Make it transparent
            this.waitingButton.setContentAreaFilled(false);
            //No need to be focusable
            this.waitingButton.setFocusable(false);
            this.waitingButton.setBorder(BorderFactory.createEtchedBorder());
            this.waitingButton.setBorderPainted(false);
        }
        return this.waitingButton;
    }

    /**
     * event register
     *
     * @param listener
     */
    public void addCloseTabListener(CloseTabListener listener) {
        closeListenerList.add(CloseTabListener.class, listener);
    }

    public void removeCloseTabListener(CloseTabListener listener) {
        closeListenerList.remove(CloseTabListener.class, listener);
    }

    private void fireCloseTab() {
        Object[] listeners = closeListenerList.getListenerList();
        for (int i = 0; i < listeners.length; i = i + 2) {
            if (listeners[i] == CloseTabListener.class) {
                ((CloseTabListener) listeners[i + 1]).closeTab(this);
            }
        }
    }

    private class TabButton extends JButton implements ActionListener {

        private final BasicStroke stroke = new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

        public TabButton() {
            init();
        }

        private void init() {
            int size = 17;
            setPreferredSize(new Dimension(size, size));
            setToolTipText("close this tab");
            //Make the button looks the same for all Laf's
            setUI(new BasicButtonUI());
            //Make it transparent
            setContentAreaFilled(false);
            //No need to be focusable
            setFocusable(false);
            setBorder(BorderFactory.createEtchedBorder());
            setBorderPainted(false);
            //Making nice rollover effect
            //we use the same listener for all buttons
            addMouseListener(buttonMouseListener);
            setRolloverEnabled(true);
            //Close the proper tab by clicking the button
            addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            fireCloseTab();
        }

        //we don't want to update UI for this button
        @Override
        public void updateUI() {
        }

        //paint the cross
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            //shift the image for pressed buttons
            if (getModel().isPressed()) {
                g2.translate(1, 1);
            }
            g2.setStroke(stroke);
            g2.setColor(Color.DARK_GRAY);
            if (getModel().isRollover()) {
                g2.setColor(Color.LIGHT_GRAY);
            }
            int delta = 6;
            g2.drawLine(delta, delta, getWidth() - delta - 1, getHeight() - delta - 1);
            g2.drawLine(getWidth() - delta - 1, delta, delta, getHeight() - delta - 1);
            g2.dispose();
        }
    }

    private final static MouseListener buttonMouseListener = new MouseAdapter() {
        @Override
        public void mouseEntered(MouseEvent e) {
            Component component = e.getComponent();
            if (component instanceof AbstractButton) {
                AbstractButton button = (AbstractButton) component;
                button.setBorderPainted(true);
            }
        }

        @Override
        public void mouseExited(MouseEvent e) {
            Component component = e.getComponent();
            if (component instanceof AbstractButton) {
                AbstractButton button = (AbstractButton) component;
                button.setBorderPainted(false);
            }
        }
    };

    public interface CloseTabListener extends EventListener {

        public void closeTab(ButtonTabComponent buttonTabComponent);
    }
}
