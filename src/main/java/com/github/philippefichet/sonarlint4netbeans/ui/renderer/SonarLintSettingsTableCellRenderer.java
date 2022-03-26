/*
 * sonarlint4netbeans: SonarLint integration for Apache Netbeans
 * Copyright (C) 2022 Philippe FICHET.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package com.github.philippefichet.sonarlint4netbeans.ui.renderer;

import com.github.philippefichet.sonarlint4netbeans.SonarLintUtils;
import java.awt.Component;
import java.awt.FlowLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author FICHET Philippe &lt;philippe.fichet@laposte.net&gt;
 */
public class SonarLintSettingsTableCellRenderer implements TableCellRenderer {
    private final FlowLayout rootLayout = new FlowLayout();
    private final JPanel rootPanel = new JPanel(rootLayout);
    private final DefaultTableCellRenderer defaultTableCellRenderer = new DefaultTableCellRenderer();
    private final ImageIcon iconModifyParameters = new ImageIcon(SonarLintUtils.class.getClassLoader().getResource("com/github/philippefichet/sonarlint4netbeans/resources/settings.png"), "Edit parameters rule");
    private final ImageIcon iconNoParameters = new ImageIcon(SonarLintUtils.class.getClassLoader().getResource("com/github/philippefichet/sonarlint4netbeans/resources/settings-empty.png"), "No parameters rule");

    public SonarLintSettingsTableCellRenderer() {
        rootLayout.setHgap(0);
        rootLayout.setVgap(0);
        rootLayout.setAlignOnBaseline(false);
        rootPanel.add(defaultTableCellRenderer);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        JLabel cell = (JLabel)defaultTableCellRenderer.getTableCellRendererComponent(table, "", isSelected, hasFocus, row, column);
        if (Boolean.TRUE.equals(value)) {
            cell.setIcon(iconModifyParameters);
        } else {
            cell.setIcon(iconNoParameters);
        }
        rootPanel.setBackground(cell.getBackground());
        return rootPanel;
    }
}
