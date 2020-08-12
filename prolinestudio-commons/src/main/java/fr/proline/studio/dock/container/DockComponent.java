package fr.proline.studio.dock.container;


import javax.swing.*;


public class DockComponent extends DockContainer {

    final private String m_windowKey;
    final private String m_title;
    final private Icon m_icon;

    private int m_properties = 0;

    public static final int PROP_NONE = 0;
    public static final int PROP_CLOSE = 0b01;
    public static final int PROP_MINIMIZE = 0b10;


    public DockComponent(String windowKey, String title, Icon icon, JComponent component, int properties) {
        m_windowKey = windowKey;
        m_title = title;
        m_icon = icon;
        m_component = component;
        m_properties = properties;
    }

    @Override
    public DockContainer search(String windowKey) {

        if (m_windowKey == null) {
            return null;
        }

        if (m_windowKey.equals(windowKey)) {
            return this;
        }

        return null;
    }

    @Override
    public void toFront() {
        getParent().toFront(this);
    }


    public void addProperties(int properties) {
        m_properties |= properties;
    }

    public boolean canClose() {
        return (PROP_CLOSE & m_properties) != 0;
    }

    public boolean canMinimize() {
        return (PROP_MINIMIZE & m_properties) != 0;
    }

    @Override
    public String getTitle() {
        return m_title;
    }

    public Icon getIcon()  {
        return null;
    }


    public  void check() {

    }

}
