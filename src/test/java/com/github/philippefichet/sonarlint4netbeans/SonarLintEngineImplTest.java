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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.prefs.BackingStoreException;
import java.util.stream.Collectors;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.sonar.api.batch.rule.RuleParam;
import org.sonarsource.sonarlint.core.client.api.common.RuleDetails;
import org.sonarsource.sonarlint.core.client.api.common.RuleKey;
import org.sonarsource.sonarlint.core.client.api.common.analysis.AnalysisResults;
import org.sonarsource.sonarlint.core.client.api.common.analysis.ClientInputFile;
import org.sonarsource.sonarlint.core.client.api.common.analysis.Issue;
import org.sonarsource.sonarlint.core.client.api.standalone.StandaloneAnalysisConfiguration;
import org.sonarsource.sonarlint.core.container.standalone.rule.StandaloneRule;
import org.sonarsource.sonarlint.core.container.standalone.rule.StandaloneRuleParam;

/**
 *
 * @author FICHET Philippe &lt;philippe.fichet@laposte.net&gt;
 */
public class SonarLintEngineImplTest {

    public static Arguments[] parametersForAnalyze() throws IOException
    {
        return new Arguments[] {
            Arguments.of(
                SonarLintEngineTestConfiguration.builder()
                .includeRules("java:S115")
                .excludeRules("java:S1220", "java:S1118")
                .addClientInputFile(new File("./src/test/resources/SonarLintFileDemo.java"))
                .build(),
                Arrays.asList(
                    new DefaultIssueTestImpl.Builder()
                    .severity("CRITICAL")
                    .type("CODE_SMELL")
                    .ruleKey("java:S115")
                    .ruleName("Constant names should comply with a naming convention")
                    .startLine(25)
                    .startLineOffset(31)
                    .endLine(25)
                    .endLineOffset(56)
                    .build()
                )
            ),
            Arguments.of(
                SonarLintEngineTestConfiguration.builder()
                .includeRules("java:S1133")
                .excludeRules("java:S1186", "java:S1598", "java:S100", "java:S1134", "java:S2168", "java:S115")
                .addClientInputFile(new File("./src/test/resources/NewClass.java"))
                .build(),
                Arrays.asList(
                    new DefaultIssueTestImpl.Builder()
                    .severity("INFO")
                    .type("CODE_SMELL")
                    .ruleKey("java:S1133")
                    .ruleName("Deprecated code should be removed")
                    .startLine(17)
                    .startLineOffset(16)
                    .endLine(17)
                    .endLineOffset(32)
                    .build()
                )
            ),
        };
    }

    @ParameterizedTest(name = "[{index}}] check analyze")
    @MethodSource("parametersForAnalyze")
    public void analyze(SonarLintEngineTestConfiguration testConfiguration, List<Issue> expectedIssue) throws MalformedURLException, BackingStoreException
    {
        SonarLintEngineImpl sonarLintEngine = new SonarLintEngineImpl();
        sonarLintEngine.getPreferences().removeNode();
        String sonarLintHome = System.getProperty("user.home") + File.separator + ".sonarlint4netbeans";

        StandaloneAnalysisConfiguration standaloneAnalysisConfiguration =
            StandaloneAnalysisConfiguration.builder()
            .setBaseDir(new File(sonarLintHome).toPath())
            .addInputFiles(testConfiguration.getClientInputFiles())
            .addExcludedRules(testConfiguration.getExcludedRules())
            .addIncludedRules(testConfiguration.getIncludedRules())
            .build();

        List<Issue> actualIssues = new ArrayList<>();
        AnalysisResults analyze = sonarLintEngine.analyze(
            standaloneAnalysisConfiguration,
            actualIssues::add,
            null,
            null
        );

        Assertions.assertThat(actualIssues)
            .extracting(DefaultIssueTestImpl::toTuple)
            .containsExactlyElementsOf(
                expectedIssue.stream()
                .map(DefaultIssueTestImpl::toTuple)
                .collect(Collectors.toList())
            );
    }

    @Test
    public void ruleParameterChanged() throws MalformedURLException, IOException, BackingStoreException {
        SonarLintEngineImpl sonarLintEngine = new SonarLintEngineImpl();
        sonarLintEngine.getPreferences().removeNode();
        File sonarlintFileDemo = new File("./src/test/resources/SonarLintFileDemo.java").getAbsoluteFile();
        String sonarLintHome = System.getProperty("user.home") + File.separator + ".sonarlint4netbeans";
        List<Issue> issues = new ArrayList<>();
        String rileKeyString = "java:S115";
        Optional<RuleDetails> ruleDetails = sonarLintEngine.getRuleDetails(rileKeyString);
        RuleKey ruleKey = RuleKey.parse(rileKeyString);
        List<RuleKey> excludedRules = new ArrayList<>();
        List<RuleKey> includedRules = new ArrayList<>();
        includedRules.add(ruleKey);
        excludedRules.add(RuleKey.parse("java:S1220"));
        excludedRules.add(RuleKey.parse("java:S1118"));
        
        Path sonarlintFileDemoPath = sonarlintFileDemo.toPath();
        List<ClientInputFile> files = new ArrayList<>();
        files.add(new FSClientInputFile(
            new String(Files.readAllBytes(sonarlintFileDemoPath)),
            sonarlintFileDemoPath.toAbsolutePath(),
            sonarlintFileDemo.getName(),
            false,
            StandardCharsets.UTF_8
        )
        );
        
        StandaloneRule rule = (StandaloneRule)ruleDetails.get();
        for (RuleParam param : rule.params()) {
            System.out.println("ruleDetails = " + param.key() + " / " + param.description());
            System.out.println("ruleDetails = " + param.key() + " / " + ((StandaloneRuleParam)param).defaultValue());
        }

        StandaloneAnalysisConfiguration standaloneAnalysisConfiguration =
            StandaloneAnalysisConfiguration.builder()
            .setBaseDir(new File(sonarLintHome).toPath())
            .addInputFiles(files)
            .addExcludedRules(excludedRules)
            .addIncludedRules(includedRules)
            .build();

        sonarLintEngine.setRuleParameter("java:S115", "format", "^.+$");
        AnalysisResults analyze = sonarLintEngine.analyze(
            standaloneAnalysisConfiguration,
            issues::add,
            null,
            null
        );

        Assertions.assertThat(issues).isEqualTo(Collections.emptyList());
    }
}
