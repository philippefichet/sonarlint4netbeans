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

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Optional;
import javax.swing.JList;
import org.sonarsource.sonarlint.core.client.api.common.RuleDetails;
import org.sonarsource.sonarlint.core.client.api.common.RuleKey;
import org.sonarsource.sonarlint.core.container.standalone.rule.StandaloneRule;

/**
 *
 * @author FICHET Philippe &lt;philippe.fichet@laposte.net&gt;
 */
public class SonarLintListMouseAdapter extends MouseAdapter {
    private final JList<String> sonarLintAllRules;
    private final SonarLintEngine sonarLintEngine;

    public SonarLintListMouseAdapter(JList<String> sonarLintAllRules, SonarLintEngine sonarLintEngine) {
        this.sonarLintAllRules = sonarLintAllRules;
        this.sonarLintEngine = sonarLintEngine;
    }
    
    @Override
    public void mouseClicked(MouseEvent e) {
        int locationToIndex = sonarLintAllRules.locationToIndex(e.getPoint());
        if (locationToIndex != -1 && 
            sonarLintAllRules.getCellBounds(locationToIndex,locationToIndex).contains(e.getPoint())
        ) {
            SonarLintListCellRenderer cellRenderer = (SonarLintListCellRenderer)sonarLintAllRules.getCellRenderer();
            if (cellRenderer.clickOnCkeckBox(e.getPoint())) {
                Optional<RuleDetails> ruleDetails = sonarLintEngine.getRuleDetails(sonarLintAllRules.getSelectedValue());
                ruleDetails.ifPresent(rule -> {
                    RuleKey ruleKey = RuleKey.parse(rule.getKey());
                    if (sonarLintEngine.isExcluded(rule)) {
                        sonarLintEngine.includeRuleKey(ruleKey);
                    } else {
                        sonarLintEngine.excludeRuleKey(ruleKey);
                    }
                    sonarLintAllRules.repaint();
                });
            }
            if (cellRenderer.clickOnSettings(e.getPoint())) {
                Optional<RuleDetails> ruleDetails = sonarLintEngine.getRuleDetails(sonarLintAllRules.getSelectedValue());
                ruleDetails.ifPresent(rule -> {
                    if (rule instanceof StandaloneRule) {
                        StandaloneRule standaloneRule = (StandaloneRule)rule;
                        if (!standaloneRule.params().isEmpty()) {
                            SonarLintRuleSettings sonarLintRuleParameters = new SonarLintRuleSettings(sonarLintEngine, sonarLintAllRules.getSelectedValue());
                            sonarLintRuleParameters.setVisible(true);
                        }
                    }
                });
            }
        }
    }
}
