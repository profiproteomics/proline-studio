package fr.proline.studio.rserver.dialog;

import fr.proline.studio.export.ExportButton;
import fr.proline.studio.gui.HourglassPanel;
import fr.proline.studio.utils.IconManager;
import java.awt.*;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import org.openide.windows.TopComponent;

/**
 *
 * Top Component to view an image which is a result comming from R Server
 * 
 * @author JM235353
 */
public class ImageViewerTopComponent extends TopComponent {

    public ImageViewerTopComponent(String name, Image img) {

        // Set Name
        setName(name);

        // Set Tooltip
        setToolTipText(name);

        setLayout(new GridLayout());
        ImagePanel imagePanel = new ImagePanel();
        imagePanel.setImage(img);
        add(imagePanel);
    }

    @Override
    public Image getIcon() {
        return IconManager.getImage(IconManager.IconType.WAVE);
    }

    public class ImagePanel extends HourglassPanel {

        private Dimension m_dimension = null;
        private Image m_img = null;

        private ImageDisplayPanel m_displayPanel;

        public ImagePanel() {

            setLayout(new BorderLayout());

            m_dimension = new Dimension(0, 0);
            m_img = null;

            m_displayPanel = new ImageDisplayPanel();
            JScrollPane scrollPane = new JScrollPane();
            scrollPane.setBackground(Color.white);
            scrollPane.setViewportView(m_displayPanel);

            add(scrollPane, BorderLayout.CENTER);

            JToolBar toolbar = initToolbar();
            add(toolbar, BorderLayout.WEST);
        }

        public final JToolBar initToolbar() {

            JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
            toolbar.setFloatable(false);

            ExportButton exportImageButton = new ExportButton("Graphic", m_displayPanel);
            toolbar.add(exportImageButton);

            return toolbar;
        }

        public void setImage(Image img) {
            m_img = img;
            m_dimension.width = img.getWidth(null);
            m_dimension.height = img.getHeight(null);

            revalidate();
            repaint();
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
}
