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

import com.github.philippefichet.sonarlint4netbeans.SonarLintEngine;
import java.awt.Component;
import java.awt.Font;
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import org.netbeans.api.project.Project;
import org.sonarsource.sonarlint.core.client.api.standalone.StandaloneRuleDetails;
import org.sonarsource.sonarlint.core.client.api.standalone.StandaloneRuleParam;

/**
 *
 * @author FICHET Philippe &lt;philippe.fichet@laposte.net&gt;
 */
public class SonarLintRuleKeyTableCellRenderer implements TableCellRenderer {
    private final DefaultTableCellRenderer defaultTableCellRenderer = new DefaultTableCellRenderer();
    private final SonarLintEngine sonarLintEngine;
    private final Project project;

    public SonarLintRuleKeyTableCellRenderer(SonarLintEngine sonarLintEngine, Project project) {
        this.sonarLintEngine = sonarLintEngine;
        this.project = project;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        JLabel cell = (JLabel)defaultTableCellRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        Optional<StandaloneRuleDetails> optionalRuleDetails = sonarLintEngine.getRuleDetails((String)value);
        if (optionalRuleDetails.isPresent()) {
            StandaloneRuleDetails standaloneRule = optionalRuleDetails.get();
            boolean hasCustomParamValue = false;
            for (StandaloneRuleParam param : standaloneRule.paramDetails()) {
                if (sonarLintEngine.getRuleParameter(standaloneRule.getKey(), param.key(), project).isPresent()) {
                    hasCustomParamValue = true;
                    break;
                }
            }

            Font font = cell.getFont();
            @SuppressWarnings({
                "java:S6411" // "Types used as keys in Maps should implement Comparable" disabled because legacy Swing code
            })
            Map<TextAttribute, Object> attributes = new HashMap<>();
            if (hasCustomParamValue) {
                attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
                attributes.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
            }
            cell.setFont(font.deriveFont(attributes));
        }
        return cell;
    }
}
