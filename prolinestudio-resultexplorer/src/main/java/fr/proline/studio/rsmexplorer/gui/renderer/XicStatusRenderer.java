/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.gui.renderer;

import fr.proline.core.orm.msi.dto.DMasterQuantPeptideIon.PepIonStatus;
import fr.proline.studio.utils.IconManager;
import java.util.HashMap;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author KX257079
 */
public class XicStatusRenderer extends DefaultTableCellRenderer {

    public static enum SelectLevel {
        DESELECTED_MANUAL(0, "DESELECTED MANUAL"),
        DESELECTED_AUTO(1, "DESELECTED AUTO"),
        SELECTED_AUTO(2, "SELECTED AUTO"),
        SELECTED_MANUAL(3, "SELECTED MANUAL"),
        RESET_AUTO(-1, "RESET AUTO");

        private int _intValue;
        private String _description;
        private static HashMap map = new HashMap<>();

        private SelectLevel(int value, String description) {
            this._intValue = value;
            this._description = description;
        }

        static {
            for (SelectLevel status : SelectLevel.values()) {
                map.put(status._intValue, status);
            }
        }

        public int getIntValue() {
            return _intValue;
        }

        public String getDescription() {
            return _description;
        }

        public static SelectLevel valueOf(int status) {
            return (SelectLevel) map.get(status);
        }

    }

    private JPanel getRenderer(ImageIcon[] iconArray) {
        JPanel result = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        for (ImageIcon icon : iconArray) {
            JLabel label = new JLabel(icon);
            result.add(label);
        }
        return result;
    }

    public static String getPepIonStatusText(PepIonStatus status) {
        switch (status) {
            case UNKNOWN:
                return "UNKNOWN";
            case INVALIDATED:
                return "INVALIDATED";
            case VALIDATED:
                return "VALIDATED";
            case USED_VALIDATED:
                return "USED VALIDATED";
            case UNUSED_VALIDATED:
                return "UNUSED VALIDATED";
            default:
                return "OTHER";
        }
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Object status = value;
        ImageIcon yesIcon = IconManager.getIcon(IconManager.IconType.TICK_SMALL);
        ImageIcon noIcon = IconManager.getIcon(IconManager.IconType.CROSS_SMALL16);
        ImageIcon manualIcon = new ImageIcon(IconManager.getIcon(IconManager.IconType.NAVIGATE).getImage().getScaledInstance(16, 16, Image.SCALE_DEFAULT));
        ImageIcon unKnowIcon = IconManager.getIcon(IconManager.IconType.BOOK_QUESTION);
        ImageIcon usedIcon = IconManager.getIcon(IconManager.IconType.PIN);
        JPanel renderer = null;
        if (status instanceof SelectLevel) {
            SelectLevel statusValue = (SelectLevel) status;
            switch (statusValue) {
                case DESELECTED_MANUAL: //0 
                    renderer = getRenderer(new ImageIcon[]{noIcon, manualIcon});
                    break;
                case DESELECTED_AUTO: //1
                    renderer = getRenderer(new ImageIcon[]{noIcon});
                    break;
                case SELECTED_AUTO: //2
                    renderer = getRenderer(new ImageIcon[]{yesIcon});
                    break;
                case SELECTED_MANUAL: //3
                    renderer = getRenderer(new ImageIcon[]{yesIcon, manualIcon});
                    break;
                default:
                    renderer = getRenderer(new ImageIcon[]{unKnowIcon});

            }
            renderer.setToolTipText(statusValue.getDescription());

        } else if (status instanceof PepIonStatus) {
            switch ((PepIonStatus) status) {

                case INVALIDATED:
                    renderer = getRenderer(new ImageIcon[]{noIcon});
                    //this.setIcon(IconManager.getIcon(IconManager.IconType.CROSS_SMALL16));
                    break;
                case VALIDATED:
                    renderer = getRenderer(new ImageIcon[]{yesIcon, unKnowIcon});
                    //this.setIcon(IconManager.getIcon(IconManager.IconType.TICK_SMALL));
                    break;
                case USED_VALIDATED:
                    renderer = getRenderer(new ImageIcon[]{yesIcon, usedIcon});
                    //this.setIcon(IconManager.getIcon(IconManager.IconType.TICK_SMALL));
                    break;
                case UNUSED_VALIDATED:
                    renderer = getRenderer(new ImageIcon[]{yesIcon});
                    //this.setIcon(IconManager.getIcon(IconManager.IconType.TICK_SMALL));
                    break;
                case UNKNOWN:
                default:
                    renderer = getRenderer(new ImageIcon[]{unKnowIcon});
                //this.setIcon(IconManager.getIcon(IconManager.IconType.TEST));
            }
            renderer.setToolTipText(getPepIonStatusText((PepIonStatus) status));
        }

        if (isSelected) {
            renderer.setBackground(javax.swing.UIManager.getDefaults().getColor("Table.selectionBackground"));
            renderer.setForeground(Color.WHITE);
        } else {
            renderer.setBackground(javax.swing.UIManager.getDefaults().getColor("Table.background"));
            renderer.setForeground(Color.BLACK);
        }

        return renderer;
    }
}
