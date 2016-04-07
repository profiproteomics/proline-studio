package fr.proline.studio.rsmexplorer.gui;

import fr.proline.studio.gui.HourglassPanel;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxPanelInterface;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionListener;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 *
 * @author JM235353
 */
public class ImagePanel extends HourglassPanel implements DataBoxPanelInterface {

    private AbstractDataBox m_dataBox;

    private Dimension m_dimension = null;
    private Image m_img = null;

    public ImagePanel() {

        setLayout(new BorderLayout());

        m_dimension = new Dimension(0, 0);
        m_img = null;

        ImageDisplayPanel displayPanel = new ImageDisplayPanel();
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBackground(Color.white);
        scrollPane.setViewportView(displayPanel);

        add(scrollPane, BorderLayout.CENTER);
    }

    public void setImage(Image img) {
        m_img = img;
        m_dimension.width = img.getWidth(null);
        m_dimension.height = img.getHeight(null);

        revalidate();
        repaint();
    }

    @Override
    public void addSingleValue(Object v) {
        // nothing to do
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

    private class ImageDisplayPanel extends JPanel {

        @Override
        public Dimension getPreferredSize() {
            return m_dimension;
        }

        @Override
        public void paint(Graphics g) {

            g.setColor(Color.white);
            g.fillRect(0, 0, getWidth(), getHeight());

            if (m_img == null) {
                return;
            }

            g.drawImage(m_img, 0, 0, this);
        }
    }

}
