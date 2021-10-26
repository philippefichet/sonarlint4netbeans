/*
 * sonarlint4netbeans: SonarLint integration for Apache Netbeans
 * Copyright (C) 2020 Philippe FICHET.
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
import java.util.Optional;
import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author FICHET Philippe &lt;philippe.fichet@laposte.net&gt;
 */
public class SonarLintSeverityTableCellRenderer implements TableCellRenderer {
    private final DefaultTableCellRenderer defaultTableCellRenderer = new DefaultTableCellRenderer();

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        String severity = String.valueOf(value);
        Optional<ImageIcon> toImageIcon = SonarLintUtils.ruleSeverityToImageIcon(severity);
        if (toImageIcon.isPresent()) {
            defaultTableCellRenderer.setIcon(toImageIcon.get());
        }
        return defaultTableCellRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    }
}
