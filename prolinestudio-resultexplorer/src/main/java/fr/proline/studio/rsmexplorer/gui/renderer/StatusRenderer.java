/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.gui.renderer;

import fr.proline.core.orm.msi.dto.DMasterQuantPeptideIon;
import static fr.proline.studio.rsmexplorer.gui.renderer.StatusRenderer.STATUS_VALIDATED;
import fr.proline.studio.utils.IconManager;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author KX257079
 */
public class StatusRenderer extends DefaultTableCellRenderer {

    public static String STATUS_VALIDATED = "validated";
    public static String STATUS_INVALIDATED = "invalidated";
    public static String STATUS_OTHER = "other";
    public static int PREFERRED_WIDTH = 30;
    public static Class STATUS;

    public enum Status {
        VALIDATED, INVALIDATED, OTHER
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Object status = value;
        if (status instanceof Boolean) {
            if (status.equals(Boolean.TRUE)) {
                this.setIcon(IconManager.getIcon(IconManager.IconType.TICK_SMALL));
                this.setToolTipText(STATUS_VALIDATED);
            } else {
                this.setIcon(IconManager.getIcon(IconManager.IconType.CROSS_SMALL16));
                this.setToolTipText(STATUS_INVALIDATED);
            }
        } else if (status instanceof Status) {
            if (status.equals(Status.VALIDATED)) {
                this.setIcon(IconManager.getIcon(IconManager.IconType.TICK_SMALL));
                this.setToolTipText(STATUS_VALIDATED);
            } else if (status.equals(Status.INVALIDATED)) {
                this.setIcon(IconManager.getIcon(IconManager.IconType.CROSS_SMALL16));
                this.setToolTipText(STATUS_INVALIDATED);
            } else {
                this.setIcon(IconManager.getIcon(IconManager.IconType.TEST));
                this.setToolTipText(STATUS_OTHER);
            }
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
