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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.sonarsource.sonarlint.core.client.api.common.analysis.Issue;

/**
 *
 * @author FICHET Philippe &lt;philippe.fichet@laposte.net&gt;
 */
public class SonarLintUtilsTest {
    @Test
    public void analyze() throws IOException {
        // first step, check all issue in file
        List<Issue> actualIssues = new ArrayList<>();
        List<Issue> expectedIssues = getExpectedIssueForNewClassFile();
        SonarLintUtils.analyze(
           Arrays.asList(FileUtil.normalizeFile(new File("./src/test/resources/NewClass.java"))),
            actualIssues::add,
            null,
            null
        );
        Assertions.assertThat(actualIssues)
            .extracting(DefaultIssueTestImpl::toTuple)
            .containsExactlyElementsOf(
                expectedIssues.stream()
                .map(DefaultIssueTestImpl::toTuple)
                .collect(Collectors.toList())
            );
        
        // second step, check tree
        SonarLintAnalyzerRootNode sonarLintAnalyzerRootNode = new SonarLintAnalyzerRootNode();
        SonarLintUtils.analyze(
           Arrays.asList(FileUtil.normalizeFile(new File("./src/test/resources/NewClass.java"))),
            sonarLintAnalyzerRootNode,
            null,
            null
        );
        Node[] severityNodes = sonarLintAnalyzerRootNode.getChildren().getNodes();
        Assertions.assertThat(severityNodes)
            .extracting(node -> node.getDisplayName())
            .containsExactlyElementsOf(Arrays.asList(
                "blocker (1 issue)",
                "critical (3 issues)",
                "major (1 issue)",
                "info (1 issue)",
                "minor (1 issue)"
            ));

        // Blocker
        Node[] issueBlockerRuleKeyNodes = severityNodes[0].getChildren().getNodes();
        Assertions.assertThat(issueBlockerRuleKeyNodes)
            .hasSize(1);
        Assertions.assertThat(issueBlockerRuleKeyNodes[0])
            .extracting(Node::getDisplayName)
            .describedAs("blocker display name")
            .isEqualTo("java:S2168 : Double-checked locking should not be used (1)");
        Node[] blockerFileNodes = issueBlockerRuleKeyNodes[0].getChildren().getNodes();
        Assertions.assertThat(blockerFileNodes)
            .hasSize(1);
        Assertions.assertThat(blockerFileNodes[0])
            .extracting(Node::getDisplayName)
            .describedAs("blocker file display name")
            .isEqualTo("24:12: NewClass.java");
        // Critial
        Node[] issueCriticalRuleKeyNodes = severityNodes[1].getChildren().getNodes();
        Assertions.assertThat(issueCriticalRuleKeyNodes)
            .hasSize(2);
        Assertions.assertThat(issueCriticalRuleKeyNodes[0])
            .extracting(Node::getDisplayName)
            .describedAs("java:S115 -> critical display name")
            .isEqualTo("java:S115 : Constant names should comply with a naming convention (1)");
        Assertions.assertThat(issueCriticalRuleKeyNodes[1])
            .extracting(Node::getDisplayName)
            .describedAs("java:S1186 -> critical display name")
            .isEqualTo("java:S1186 : Methods should not be empty (2)");
        Node[] critialS115FileNodes = issueCriticalRuleKeyNodes[0].getChildren().getNodes();
        Assertions.assertThat(critialS115FileNodes)
            .hasSize(1);
        Assertions.assertThat(critialS115FileNodes[0])
            .extracting(Node::getDisplayName)
            .describedAs("critial S115 file display name")
            .isEqualTo("10:31: NewClass.java");
        Node[] critialS1186FileNodes = issueCriticalRuleKeyNodes[1].getChildren().getNodes();
        Assertions.assertThat(critialS1186FileNodes)
            .hasSize(2);
        Assertions.assertThat(critialS1186FileNodes[0])
            .extracting(Node::getDisplayName)
            .describedAs("critial S1186 first file display name")
            .isEqualTo("17:16: NewClass.java");
        Assertions.assertThat(critialS1186FileNodes[1])
            .extracting(Node::getDisplayName)
            .describedAs("critial S1186 second file display name")
            .isEqualTo("19:16: NewClass.java");

        // Major
        Node[] issueMajorRuleKeyNodes = severityNodes[2].getChildren().getNodes();
        Assertions.assertThat(issueMajorRuleKeyNodes)
            .hasSize(1);
        Assertions.assertThat(issueMajorRuleKeyNodes[0])
            .extracting(Node::getDisplayName)
            .describedAs("major display name")
            .isEqualTo("java:S1134 : Track uses of \"FIXME\" tags (1)");
        Node[] majorFileNodes = issueMajorRuleKeyNodes[0].getChildren().getNodes();
        Assertions.assertThat(majorFileNodes)
            .hasSize(1);
        Assertions.assertThat(majorFileNodes[0])
            .extracting(Node::getDisplayName)
            .describedAs("major file display name")
            .isEqualTo("21:0: NewClass.java");
        // Info
        Node[] issueInfoRuleKeyNodes = severityNodes[3].getChildren().getNodes();
        Assertions.assertThat(issueInfoRuleKeyNodes)
            .hasSize(1);
        Assertions.assertThat(issueInfoRuleKeyNodes[0])
            .extracting(Node::getDisplayName)
            .describedAs("info display name")
            .isEqualTo("java:S1133 : Deprecated code should be removed (1)");
        // Minor
        Node[] issueMinorRuleKeyNodes = severityNodes[4].getChildren().getNodes();
        Assertions.assertThat(issueMinorRuleKeyNodes)
            .hasSize(1);
        Assertions.assertThat(issueMinorRuleKeyNodes[0])
            .extracting(Node::getDisplayName)
            .describedAs("minor display name")
            .isEqualTo("java:S100 : Method names should comply with a naming convention (1)");
    }

    public static Arguments[] parametersForToTruncateURI() throws URISyntaxException
    {
        return new Arguments[] {
            Arguments.of(
                new URI("file:/source-code/my-project/src/main/java/com/example/Main.java"),
                100,
                "file:/source-code/my-project/src/main/java/com/example/Main.java"
            ),
            Arguments.of(
                new URI("file:/source-code/my-project/src/main/java/com/example/Main.java"),
                11,
                ".../Main.java"
            ),
            Arguments.of(
                new URI("file:/source-code/my-project/src/main/java/com/example/Main.java"),
                12,
                ".../example/Main.java"
            ),
            Arguments.of(
                new URI("file:/source-code/my-project/src/main/java/com/example/Main.java"),
                40,
                ".../my-project/src/main/java/com/example/Main.java"
            ),
            Arguments.of(
                new URI("file:///C:/Users/Main.java"),
                12,
                ".../Users/Main.java"
            ),
            Arguments.of(
                new URI("file:///C:/Users/Main.java"),
                100,
                "file:///C:/Users/Main.java"
            ),
        };
    }
    
    @ParameterizedTest(name = "[{index}] Given URI {0} When truncate to {1} characters Then path must be {2}.")
    @MethodSource("parametersForToTruncateURI")
    public void toTruncateURI(URI uri, int maximalLength, String expectedTruncateURI)
    {
        String actualTruncateURI = SonarLintUtils.toTruncateURI(uri, maximalLength);
        Assertions.assertThat(actualTruncateURI)
            .isEqualTo(expectedTruncateURI);
    }

    private static List<Issue> getExpectedIssueForNewClassFile()
    {
        return Arrays.asList(
            new DefaultIssueTestImpl.Builder()
            .severity("CRITICAL")
            .type("CODE_SMELL")
            .ruleKey("java:S1186")
            .ruleName("Methods should not be empty")
            .startLine(17)
            .startLineOffset(16)
            .endLine(17)
            .endLineOffset(32)
            .build(),
            new DefaultIssueTestImpl.Builder()
            .severity("CRITICAL")
            .type("CODE_SMELL")
            .ruleKey("java:S1186")
            .ruleName("Methods should not be empty")
            .startLine(19)
            .startLineOffset(16)
            .endLine(19)
            .endLineOffset(47)
            .build(),
            new DefaultIssueTestImpl.Builder()
            .severity("CRITICAL")
            .type("CODE_SMELL")
            .ruleKey("java:S115")
            .ruleName("Constant names should comply with a naming convention")
            .startLine(10)
            .startLineOffset(31)
            .endLine(10)
            .endLineOffset(56)
            .build(),
            new DefaultIssueTestImpl.Builder()
            .severity("INFO")
            .type("CODE_SMELL")
            .ruleKey("java:S1133")
            .ruleName("Deprecated code should be removed")
            .startLine(17)
            .startLineOffset(16)
            .endLine(17)
            .endLineOffset(32)
            .build(),
            new DefaultIssueTestImpl.Builder()
            .severity("MINOR")
            .type("CODE_SMELL")
            .ruleKey("java:S100")
            .ruleName("Method names should comply with a naming convention")
            .startLine(19)
            .startLineOffset(16)
            .endLine(19)
            .endLineOffset(47)
            .build(),
            new DefaultIssueTestImpl.Builder()
            .severity("MAJOR")
            .type("CODE_SMELL")
            .ruleKey("java:S1134")
            .ruleName("Track uses of \"FIXME\" tags")
            .startLine(21)
            .startLineOffset(0)
            .endLine(21)
            .endLineOffset(18)
            .build(),
            new DefaultIssueTestImpl.Builder()
            .severity("BLOCKER")
            .type("BUG")
            .ruleKey("java:S2168")
            .ruleName("Double-checked locking should not be used")
            .startLine(24)
            .startLineOffset(12)
            .endLine(24)
            .endLineOffset(24)
            .build()
        );
    }
}
