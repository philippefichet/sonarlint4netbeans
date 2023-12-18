/*
 * sonarlint4netbeans: SonarLint integration for Apache Netbeans
 * Copyright (C) 2023 Philippe FICHET.
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

import com.github.philippefichet.sonarlint4netbeans.junit.jupiter.extension.SonarLintLookupMockedExtension;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.List;
import java.util.prefs.BackingStoreException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.sonarsource.sonarlint.core.client.api.common.analysis.Issue;
import org.sonarsource.sonarlint.core.commons.IssueSeverity;
import org.sonarsource.sonarlint.core.commons.RuleType;

/**
 *
 * @author FICHET Philippe &lt;philippe.fichet@laposte.net&gt;
 */
@Tag("javascript")
@DisabledIfSystemProperty(
    named = "sonarlint4netbeans.test.skip.javascript",
    matches = "true",
    disabledReason = "Disabled because BundleUtils.extractFromClasspath(bundle, deployLocation); in BundleImpl  is too slow (~60s.)"
)
public class SonarLintEngineImplJavascriptPluginTest {

    @RegisterExtension
    SonarLintLookupMockedExtension lookupExtension = SonarLintLookupMockedExtension.builder()
        .logCall()
        .mockLookupMethodWith(
            SonarLintDataManager.class,
            new SonarLintDataManagerMockedBuilder().build()
        ).build();
    
    public static Arguments[] parametersForAnalyze() throws IOException
    {
        return new Arguments[] {
            Arguments.of(
                SonarLintEngineTestConfiguration.builder()
                .description("sonarlint-example.js with rule javascript:S108 to check javascript plugin that require nodejs")
                .requirePlugin("javascript")
                .requireNodeJS()
                .excludeRules("javascript:S3504", "javascript:S3827", "javascript:S3504")
                .includeRules("javascript:S108")
                .addClientInputFile(new File("./src/test/resources/sonarlint-example.js"))
                .build(),
                Arrays.asList(
                    new DefaultIssueTestImpl.Builder()
                    .severity(IssueSeverity.MAJOR)
                    .type(RuleType.CODE_SMELL)
                    // "Nested blocks of code should not be left empty")
                    .ruleKey("javascript:S108")
                    .startLine(1)
                    .startLineOffset(33)
                    .endLine(1)
                    .endLineOffset(35)
                    .build()
                )
            ),
            Arguments.of(
                SonarLintEngineTestConfiguration.builder()
                .description("sonarlint-example-with-global-variables.js with rule javascript:S3827 to check 3 issues from global variable declaring without extra properties \"sonar.javascript.globals\"")
                .requirePlugin("javascript")
                .requireNodeJS()
                .excludeRules("javascript:S3504")
                .includeRules("javascript:S3827")
                .addClientInputFile(new File("./src/test/resources/sonarlint-example-with-global-variables.js"))
                .build(),
                Arrays.asList(
                      // api variable
                    new DefaultIssueTestImpl.Builder()
                    .severity(IssueSeverity.BLOCKER)
                    .type(RuleType.BUG)
                    // "Variables should be defined before being used"
                    .ruleKey("javascript:S3827")
                    .startLine(3)
                    .startLineOffset(11)
                    .endLine(3)
                    .endLineOffset(14)
                    .build(),
                    // AapiI variable
                    new DefaultIssueTestImpl.Builder()
                    .severity(IssueSeverity.BLOCKER)
                    .type(RuleType.BUG)
                    // "Variables should be defined before being used"
                    .ruleKey("javascript:S3827")
                    .startLine(4)
                    .startLineOffset(11)
                    .endLine(4)
                    .endLineOffset(16)
                    .build(),
                    // globalVariables variable
                    new DefaultIssueTestImpl.Builder()
                    .severity(IssueSeverity.BLOCKER)
                    .type(RuleType.BUG)
                    // "Variables should be defined before being used")
                    .ruleKey("javascript:S3827")
                    .startLine(5)
                    .startLineOffset(11)
                    .endLine(5)
                    .endLineOffset(26)
                    .build()
                )
            ),
            Arguments.of(
                SonarLintEngineTestConfiguration.builder()
                .description("sonarlint-example-with-global-variables.js with rule javascript:S3827 to check one issue from global variable declaring in extra properties \"sonar.javascript.globals\" for \"globalVariables,api\"")
                .requirePlugin("javascript")
                .requireNodeJS()
                    .excludeRules("javascript:S3504")
                .includeRules("javascript:S3827")
                .addClientInputFile(new File("./src/test/resources/sonarlint-example-with-global-variables.js"))
                .addExtraProperty("sonar.javascript.globals", "globalVariables,api", SonarLintEngine.GLOBAL_SETTINGS_PROJECT)
                .build(),
                Arrays.asList(
                    // AapiI variable
                    new DefaultIssueTestImpl.Builder()
                    .severity(IssueSeverity.BLOCKER)
                    .type(RuleType.BUG)
                    // "Variables should be defined before being used"
                    .ruleKey("javascript:S3827")
                    .startLine(4)
                    .startLineOffset(11)
                    .endLine(4)
                    .endLineOffset(16)
                    .build()
                )
            ),
            Arguments.of(
                SonarLintEngineTestConfiguration.builder()
                .description("sonarlint-example-with-global-variables.js with rule javascript:S3827 to check two issues from global variable declaring with extra properties \"sonar.javascript.globals\" for \"AapiI\"")
                .requirePlugin("javascript")
                .requireNodeJS()
                .excludeRules("javascript:S3504")
                .includeRules("javascript:S3827")
                .addClientInputFile(new File("./src/test/resources/sonarlint-example-with-global-variables.js"))
                .addExtraProperty("sonar.javascript.globals", "AapiI", SonarLintEngine.GLOBAL_SETTINGS_PROJECT)
                .build(),
                Arrays.asList(
                    // api variable
                    new DefaultIssueTestImpl.Builder()
                    .severity(IssueSeverity.BLOCKER)
                    .type(RuleType.BUG)
                    // "Variables should be defined before being used"
                    .ruleKey("javascript:S3827")
                    .startLine(3)
                    .startLineOffset(11)
                    .endLine(3)
                    .endLineOffset(14)
                    .build(),
                    // globalVariables variable
                    new DefaultIssueTestImpl.Builder()
                    .severity(IssueSeverity.BLOCKER)
                    .type(RuleType.BUG)
                    // "Variables should be defined before being used"
                    .ruleKey("javascript:S3827")
                    .startLine(5)
                    .startLineOffset(11)
                    .endLine(5)
                    .endLineOffset(26)
                    .build()
                )
            ),
            Arguments.of(
                SonarLintEngineTestConfiguration.builder()
                .description("sonarlint-example.css with rule css:S1116 to check one issue on line \"4\"")
                .requirePlugin("javascript")
                .requireNodeJS()
                .includeRules("css:S1116")
                .addClientInputFile(new File("./src/test/resources/sonarlint-example.css"))
                .build(),
                Arrays.asList(
                    // api variable
                    new DefaultIssueTestImpl.Builder()
                    .severity(IssueSeverity.MINOR)
                    .type(RuleType.CODE_SMELL)
                    // "Extra semicolons should be removed"
                    .ruleKey("css:S1116")
                    .startLine(4)
                    .startLineOffset(0)
                    .endLine(4)
                    .endLineOffset(20)
                    .build()
                )
            ),
        };
    }

    @ParameterizedTest(name = "[{index}}] analyze({0})")
    @MethodSource("parametersForAnalyze")
    public void analyze(SonarLintEngineTestConfiguration testConfiguration, List<Issue> expectedIssue) throws MalformedURLException, BackingStoreException, IOException
    {
        SonarLintEngineImplTestUtils.analyzeTesting(testConfiguration, expectedIssue);
    }
}
