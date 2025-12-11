package com.hastane.view;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class StatusCellRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        String status = (String) value;

        if (!isSelected) {
            if ("ONAYLANDI".equalsIgnoreCase(status) || "TAMAMLANDI".equalsIgnoreCase(status)) {
                c.setForeground(new Color(0, 128, 0)); // Koyu Yeşil
                c.setFont(c.getFont().deriveFont(Font.BOLD));
            } else if ("İPTAL EDİLDİ".equalsIgnoreCase(status) || "GELMEDİ".equalsIgnoreCase(status)) {
                c.setForeground(Color.RED);
                c.setFont(c.getFont().deriveFont(Font.BOLD));
            } else {
                c.setForeground(Color.BLACK); // Beklemede vs.
            }
        }
        return c;
    }
}