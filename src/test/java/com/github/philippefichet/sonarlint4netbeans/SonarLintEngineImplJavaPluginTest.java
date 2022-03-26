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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.prefs.BackingStoreException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.sonarsource.sonarlint.core.client.api.common.analysis.Issue;

/**
 *
 * @author FICHET Philippe &lt;philippe.fichet@laposte.net&gt;
 */
public class SonarLintEngineImplJavaPluginTest {

    public static Arguments[] parametersForAnalyze() throws IOException
    {
        return new Arguments[] {
            Arguments.of(
                SonarLintEngineTestConfiguration.builder()
                .requirePlugin("java")
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
                .requirePlugin("java")
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
                .requirePlugin("java")
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
                .requirePlugin("java")
                .description("SonarLintFileDemo.java with rule java:S115 but without java:S1220 and S1118 rule with custom parameters")
                .includeRules("java:S115")
                .excludeRules("java:S1220", "java:S1118")
                .addClientInputFile(new File("./src/test/resources/SonarLintFileDemo.java"))
                .addRuleParameter("java:S115", "format", "^.+$")
                .build(),
                Collections.emptyList()
            ),
        };
    }

    @ParameterizedTest(name = "[{index}}] analyze({0})")
    @MethodSource("parametersForAnalyze")
    public void analyze(SonarLintEngineTestConfiguration testConfiguration, List<Issue> expectedIssue) throws MalformedURLException, BackingStoreException, IOException {
        SonarLintEngineImplTestUtils.analyzeTesting(testConfiguration, expectedIssue);
    }
}
