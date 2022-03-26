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
package com.github.philippefichet.sonarlint4netbeans;

import com.github.philippefichet.sonarlint4netbeans.ui.SonarLintRuleSettings;
import com.github.philippefichet.sonarlint4netbeans.ui.renderer.SonarLintListCellRenderer;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Optional;
import javax.swing.JList;
import org.netbeans.api.project.Project;
import org.sonarsource.sonarlint.core.client.api.common.RuleKey;
import org.sonarsource.sonarlint.core.client.api.standalone.StandaloneRuleDetails;

/**
 *
 * @author FICHET Philippe &lt;philippe.fichet@laposte.net&gt;
 */
public class SonarLintListMouseAdapter extends MouseAdapter {
    private final JList<String> sonarLintAllRules;
    private final SonarLintEngine sonarLintEngine;
    private final SonarLintOptions sonarLintOptions;
    private final Project project;

    public SonarLintListMouseAdapter(
        JList<String> sonarLintAllRules,
        SonarLintOptions sonarLintOptions,
        SonarLintEngine sonarLintEngine,
        Project project
    ) {
        this.sonarLintAllRules = sonarLintAllRules;
        this.sonarLintEngine = sonarLintEngine;
        this.sonarLintOptions = sonarLintOptions;
        this.project = project;
    }
    
    @Override
    public void mouseClicked(MouseEvent e) {
        int locationToIndex = sonarLintAllRules.locationToIndex(e.getPoint());
        if (locationToIndex != -1 && 
            sonarLintAllRules.getCellBounds(locationToIndex,locationToIndex).contains(e.getPoint())
        ) {
            SonarLintListCellRenderer cellRenderer = (SonarLintListCellRenderer)sonarLintAllRules.getCellRenderer();
            if (cellRenderer.clickOnCkeckBox(e.getPoint())) {
                Optional<StandaloneRuleDetails> ruleDetails = sonarLintEngine.getRuleDetails(sonarLintAllRules.getSelectedValue());
                ruleDetails.ifPresent(rule -> {
                    RuleKey ruleKey = RuleKey.parse(rule.getKey());
                    if (sonarLintEngine.isExcluded(rule, project)) {
                        sonarLintEngine.includeRuleKey(ruleKey, project);
                    } else {
                        sonarLintEngine.excludeRuleKey(ruleKey, project);
                    }
                    sonarLintAllRules.repaint();
                });
            }
            if (cellRenderer.clickOnSettings(e.getPoint())) {
                Optional<StandaloneRuleDetails> ruleDetails = sonarLintEngine.getRuleDetails(sonarLintAllRules.getSelectedValue());
                ruleDetails.ifPresent(standaloneRule -> {
                    if (!standaloneRule.paramDetails().isEmpty()) {
                        SonarLintRuleSettings sonarLintRuleParameters = new SonarLintRuleSettings(
                            sonarLintOptions,
                            sonarLintEngine,
                            sonarLintAllRules.getSelectedValue(),
                            (String ruleKey, String parameterName, String parameterValue) -> {
                                SonarLintUtils.changeRuleParameterValue(sonarLintEngine, project, ruleKey, parameterName, parameterValue);
                            },
                            project
                        );
                        sonarLintRuleParameters.setVisible(true);
                    }
                });
            }
        }
    }
}
