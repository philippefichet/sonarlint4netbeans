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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.stream.Collectors;
import org.assertj.core.api.Assertions;
import org.netbeans.api.project.Project;
import org.sonarsource.sonarlint.core.client.api.common.LogOutput;
import org.sonarsource.sonarlint.core.client.api.common.PluginDetails;
import org.sonarsource.sonarlint.core.client.api.common.Version;
import org.sonarsource.sonarlint.core.client.api.common.analysis.AnalysisResults;
import org.sonarsource.sonarlint.core.client.api.common.analysis.Issue;
import org.sonarsource.sonarlint.core.client.api.standalone.StandaloneAnalysisConfiguration;

/**
 *
 * @author FICHET Philippe &lt;philippe.fichet@laposte.net&gt;
 */
public final class SonarLintEngineImplTestUtils {

    private static final Logger LOG = Logger.getLogger(SonarLintEngineImplTestUtils.class.getName());

    private SonarLintEngineImplTestUtils() {
    }

    public static void analyzeTesting(SonarLintEngineTestConfiguration testConfiguration, List<Issue> expectedIssue) throws MalformedURLException, BackingStoreException, IOException
    {
        SonarLintEngineImpl sonarLintEngine = new SonarLintEngineImpl();
        sonarLintEngine.waitingInitialization();
        Collection<PluginDetails> pluginDetails = sonarLintEngine.getPluginDetails();
        List<String> requirePlugin = testConfiguration.getRequirePlugin();
        pluginDetails.forEach(d -> {
            d.skipReason().ifPresent(s -> {
                LOG.info("Plugin \"" + d.key() + ":" + d.name() + "\" skipped : " + s.toString());
                if (requirePlugin.contains(d.key())) {
                    Assertions.fail("Plugin %s is required but disabled : %s", d.key(), s.toString());
                }
            });
        });
        sonarLintEngine.getPreferences(SonarLintEngine.GLOBAL_SETTINGS_PROJECT).removeNode();
        testConfiguration.getExtraProperties().forEach(
            (Project project, Map<String, String> extraProperties) ->
            sonarLintEngine.setAllExtraProperties(extraProperties, project)
        );
        testConfiguration.getRuleParameters().forEach(
            ruleParameter -> sonarLintEngine.setRuleParameter(ruleParameter.getRuleKey(), ruleParameter.getName(), ruleParameter.getValue(), SonarLintEngine.GLOBAL_SETTINGS_PROJECT)
        );
        String sonarLintHome = System.getProperty("user.home") + File.separator + ".sonarlint4netbeans";
        if (testConfiguration.isRequireNodeJS()) {
            SonarLintTestUtils.installNodeJS();
            sonarLintEngine.setNodeJSPathAndVersion(
                SonarLintTestUtils.getNodeJS().getAbsolutePath(),
                Version.create(SonarLintTestUtils.getNodeJSVersion())
            );
        }

        StandaloneAnalysisConfiguration standaloneAnalysisConfiguration = 
            StandaloneAnalysisConfiguration.builder()
            .setBaseDir(new File(sonarLintHome).toPath())
            .addInputFiles(testConfiguration.getClientInputFiles())
            .addExcludedRules(testConfiguration.getExcludedRules())
            .addIncludedRules(testConfiguration.getIncludedRules())
            .addRuleParameters(sonarLintEngine.getRuleParameters(SonarLintEngine.GLOBAL_SETTINGS_PROJECT))
            .putAllExtraProperties(sonarLintEngine.getAllExtraProperties(SonarLintEngine.GLOBAL_SETTINGS_PROJECT))
            .build();

        List<Issue> actualIssues = new ArrayList<>();
        AnalysisResults analyze = sonarLintEngine.analyze(
            standaloneAnalysisConfiguration,
            actualIssues::add,
            new LogOutput() {
            @Override
            public void log(String formattedMessage, LogOutput.Level level) {
                LOG.info("[" + level + "] " + formattedMessage);
            }
            },
            null
        );
        sonarLintEngine.stop();

        Assertions.assertThat(actualIssues)
            .hasSameSizeAs(expectedIssue);
        Assertions.assertThat(actualIssues)
            .extracting(DefaultIssueTestImpl::toTuple)
            .containsExactlyElementsOf(
                expectedIssue.stream()
                .map(DefaultIssueTestImpl::toTuple)
                .collect(Collectors.toList())
            );
    }
}
