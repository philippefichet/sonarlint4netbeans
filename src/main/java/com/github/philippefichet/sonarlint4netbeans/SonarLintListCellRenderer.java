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
package com.github.philippefichet.sonarlint4netbeans;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Point;
import java.awt.font.TextAttribute;
import java.util.Map;
import java.util.Optional;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import org.sonar.api.batch.rule.RuleParam;
import org.sonarsource.sonarlint.core.client.api.common.RuleDetails;
import org.sonarsource.sonarlint.core.container.standalone.rule.StandaloneRule;

/**
 *
 * @author FICHET Philippe &lt;philippe.fichet@laposte.net&gt;
 */
public final class SonarLintListCellRenderer extends JPanel implements ListCellRenderer<String> {
    private final SonarLintEngine sonarLintEngine;
    private final JCheckBox enableOrDisable;
    private final JLabel modifyParameters;
    private final ImageIcon iconModifyParameters;
    private final ImageIcon iconNoParameters;
    private final DefaultListCellRenderer defaultListCellRenderer = new DefaultListCellRenderer();

    public SonarLintListCellRenderer(SonarLintEngine sonarLintEngine) {
        this.sonarLintEngine = sonarLintEngine;
        FlowLayout flowLayout = new FlowLayout();
        flowLayout.setAlignment(FlowLayout.LEFT);
        flowLayout.setHgap(0);
        flowLayout.setVgap(0);
        setLayout(flowLayout);
        enableOrDisable = new JCheckBox();
        enableOrDisable.setEnabled(true);

        iconModifyParameters = new ImageIcon(SonarLintUtils.class.getClassLoader().getResource("com/github/philippefichet/sonarlint4netbeans/resources/settings.png"), "Edit parameters rule");
        iconNoParameters = new ImageIcon(SonarLintUtils.class.getClassLoader().getResource("com/github/philippefichet/sonarlint4netbeans/resources/settings-empty.png"), "No parameters rule");
        modifyParameters = new JLabel(iconModifyParameters);
        add(enableOrDisable);
        add(modifyParameters);
        add(defaultListCellRenderer);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends String> list, String value, int index, boolean isSelected, boolean cellHasFocus) {
        Optional<RuleDetails> optionalRuleDetails = sonarLintEngine.getRuleDetails(value);
        defaultListCellRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (optionalRuleDetails.isPresent()) {
            RuleDetails ruleDetails = optionalRuleDetails.get();
            if (ruleDetails instanceof StandaloneRule && !((StandaloneRule)ruleDetails).params().isEmpty()) {
                modifyParameters.setIcon(iconModifyParameters);
            } else {
                modifyParameters.setIcon(iconNoParameters);
            }
            Optional<ImageIcon> toImageIcon = SonarLintUtils.toImageIcon(optionalRuleDetails.get().getSeverity());
            if (toImageIcon.isPresent()) {
                defaultListCellRenderer.setIcon(toImageIcon.get());
            }
            enableOrDisable.setSelected(!sonarLintEngine.isExcluded(optionalRuleDetails.get()));
            if (ruleDetails instanceof StandaloneRule) {
                StandaloneRule standaloneRule = (StandaloneRule)ruleDetails;
                boolean hasCustomParamValue = false;
                for (RuleParam param : standaloneRule.params()) {
                    if (sonarLintEngine.getRuleParameter(standaloneRule.getKey(), param.key()).isPresent()) {
                        hasCustomParamValue = true;
                        break;
                    }
                }

                Font font = defaultListCellRenderer.getFont();
                Map attributes = font.getAttributes();
                if (hasCustomParamValue) {
                    attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
                    attributes.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
                } else {
                    attributes.remove(TextAttribute.UNDERLINE);
                    attributes.remove(TextAttribute.WEIGHT);
                }
                defaultListCellRenderer.setFont(font.deriveFont(attributes));
            }
        }
        setBackground(defaultListCellRenderer.getBackground());
        enableOrDisable.setBackground(defaultListCellRenderer.getBackground());
        return this;
    }

    public boolean clickOnCkeckBox(Point point) {
        return enableOrDisable.getBounds().getX() < point.getX()
            && enableOrDisable.getBounds().getX() + enableOrDisable.getBounds().getWidth() > point.getX();
    }

    public boolean clickOnSettings(Point point) {
        return modifyParameters.getBounds().getX() < point.getX()
            && modifyParameters.getBounds().getX() + modifyParameters.getBounds().getWidth() > point.getX();
    }
}
