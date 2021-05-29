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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.stream.Collectors;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.sonarsource.sonarlint.core.client.api.common.Version;
import org.sonarsource.sonarlint.core.client.api.common.analysis.AnalysisResults;
import org.sonarsource.sonarlint.core.client.api.common.analysis.Issue;
import org.sonarsource.sonarlint.core.client.api.standalone.StandaloneAnalysisConfiguration;

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
                .description("SonarLintFileDemo.java with rule java:S115 but without java:S122 and java:S1118 to check CODE_SMELL CRITICAL")
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
                .description("NewClass.java with rule java:S1133 but without java:S1186, java:S1598, java:S100, java:S1134, java:S2168 and java:S115  to check CODE_SMELL INFO")
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
            Arguments.of(
                SonarLintEngineTestConfiguration.builder()
                .description("SonarLintFileDemo.java with rule java:S115 but without java:S1220 and S1118 rule with default parameters")
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
                .description("SonarLintFileDemo.java with rule java:S115 but without java:S1220 and S1118 rule with custom parameters")
                .includeRules("java:S115")
                .excludeRules("java:S1220", "java:S1118")
                .addClientInputFile(new File("./src/test/resources/SonarLintFileDemo.java"))
                .addRuleParameter("java:S115", "format", "^.+$")
                .build(),
                Collections.emptyList()
            ),
            Arguments.of(
                SonarLintEngineTestConfiguration.builder()
                .description("sonarlint-example.js with rule javascript:S108 to check javascript plugin that require nodejs")
                .includeRules("javascript:S108")
                .addClientInputFile(new File("./src/test/resources/sonarlint-example.js"))
                .build(),
                Arrays.asList(
                    new DefaultIssueTestImpl.Builder()
                    .severity("MAJOR")
                    .type("CODE_SMELL")
                    .ruleKey("javascript:S108")
                    .ruleName("Nested blocks of code should not be left empty")
                    .startLine(1)
                    .startLineOffset(33)
                    .endLine(1)
                    .endLineOffset(35)
                    .build()
                )
            ),
            Arguments.of(
                SonarLintEngineTestConfiguration.builder()
                .description("sonarlin-example.php with rule php:S101 but without php:S1105 to check php plugin")
                .includeRules("php:S101")
                .excludeRules("php:S1105")
                .addClientInputFile(new File("./src/test/resources/sonarlin-example.php"))
                .build(),
                Arrays.asList(
                    new DefaultIssueTestImpl.Builder()
                    .severity("MINOR")
                    .type("CODE_SMELL")
                    .ruleKey("php:S101")
                    .ruleName("Class names should comply with a naming convention")
                    .startLine(2)
                    .startLineOffset(10)
                    .endLine(2)
                    .endLineOffset(18)
                    .build()
                )
            ),
        };
    }

    @ParameterizedTest(name = "[{index}}] analyze({0})")
    @MethodSource("parametersForAnalyze")
    public void analyze(SonarLintEngineTestConfiguration testConfiguration, List<Issue> expectedIssue) throws MalformedURLException, BackingStoreException, IOException
    {
        SonarLintEngineImpl sonarLintEngine = new SonarLintEngineImpl();
        sonarLintEngine.waitingInitialization();
        sonarLintEngine.getPreferences().removeNode();
        testConfiguration.getRuleParameters().forEach(
            ruleParameter -> sonarLintEngine.setRuleParameter(ruleParameter.getRuleKey(), ruleParameter.getName(), ruleParameter.getValue())
        );
        String sonarLintHome = System.getProperty("user.home") + File.separator + ".sonarlint4netbeans";
        SonarLintTestUtils.installNodeJS();
        sonarLintEngine.setNodeJSPathAndVersion(
            SonarLintTestUtils.getNodeJS().getAbsolutePath(),
            Version.create(SonarLintTestUtils.getNodeJSVersion())
        );
        sonarLintEngine.waitingInitialization();

        StandaloneAnalysisConfiguration standaloneAnalysisConfiguration = 
            StandaloneAnalysisConfiguration.builder()
            .setBaseDir(new File(sonarLintHome).toPath())
            .addInputFiles(testConfiguration.getClientInputFiles())
            .addExcludedRules(testConfiguration.getExcludedRules())
            .addIncludedRules(testConfiguration.getIncludedRules())
            .addRuleParameters(sonarLintEngine.getRuleParameters())
            .build();

        List<Issue> actualIssues = new ArrayList<>();
        AnalysisResults analyze = sonarLintEngine.analyze(
            standaloneAnalysisConfiguration,
            actualIssues::add,
            null,
            null
        );

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
