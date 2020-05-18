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
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author KX257079
 */
public class XicStatusRenderer extends DefaultTableCellRenderer {

    public static enum SelectLevel {
        DESELECTED_MANUAL(0, "Deselected Manual"),
        DESELECTED_AUTO(1, "Deselected Auto"),
        SELECTED_AUTO(2, "Selected Auto"),
        SELECTED_MANUAL(3, "Selected Manual"),
        UNKNOWN(-1, "Invalid"),
        RESET_AUTO(-2, "Reset auto");

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

    public static String getPepIonStatusText(PepIonStatus status) {
        switch (status) {
            case UNKNOWN:
                return "Unknown";
            case INVALIDATED:
                return "Invalidated";
            case VALIDATED:
                return "Validated";
            case USED_VALIDATED:
                return "Validated Used";
            case UNUSED_VALIDATED:
                return "Validated Not Used";
            default:
                return "Other";
        }
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Object status = value;
        if (status instanceof SelectLevel) {
            SelectLevel statusValue = (SelectLevel) status;
            switch (statusValue) {
                case DESELECTED_MANUAL: //0 
                case DESELECTED_AUTO: //1
                    this.setIcon(IconManager.getIcon(IconManager.IconType.INVALIDATED));
                    break;
                case SELECTED_AUTO: //2
                case SELECTED_MANUAL: //3
                    this.setIcon(IconManager.getIcon(IconManager.IconType.VALIDATED));
                    break;
                default:
                    this.setIcon(IconManager.getIcon(IconManager.IconType.TEST));

            }
            this.setToolTipText(statusValue.getDescription());

        } else if (status instanceof PepIonStatus) {
            switch ((PepIonStatus) status) {

                case INVALIDATED:
                    this.setIcon(IconManager.getIcon(IconManager.IconType.INVALIDATED));
                    break;
                case VALIDATED:
                    this.setIcon(IconManager.getIcon(IconManager.IconType.VALIDATED));
                    break;
                case USED_VALIDATED:
                    this.setIcon(IconManager.getIcon(IconManager.IconType.VALIDATED_AND_USED));
                    break;
                case UNUSED_VALIDATED:
                    this.setIcon(IconManager.getIcon(IconManager.IconType.VALIDATED_AND_NOTUSED));
                    break;
                case UNKNOWN:
                default:
                    this.setIcon(IconManager.getIcon(IconManager.IconType.TEST));
            }
            this.setToolTipText(getPepIonStatusText((PepIonStatus) status));
        }

        if (isSelected) {
            this.setBackground(javax.swing.UIManager.getDefaults().getColor("Table.selectionBackground"));
            this.setForeground(Color.WHITE);
        } else {
            this.setBackground(javax.swing.UIManager.getDefaults().getColor("Table.background"));
            this.setForeground(Color.BLACK);
        }

        return this;
    }
}
