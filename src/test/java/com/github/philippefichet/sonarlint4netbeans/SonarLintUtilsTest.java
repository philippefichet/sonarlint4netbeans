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

import com.github.philippefichet.sonarlint4netbeans.project.SonarLintProjectPreferenceScope;
import com.github.philippefichet.sonarlint4netbeans.treenode.SonarLintAnalyzerRootNode;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.prefs.BackingStoreException;
import java.util.stream.Collectors;
import org.assertj.core.api.Assertions;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ProjectManagerImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.Utilities;
import org.sonarsource.sonarlint.core.client.api.common.RuleKey;
import org.sonarsource.sonarlint.core.client.api.common.Version;
import org.sonarsource.sonarlint.core.client.api.common.analysis.Issue;
import org.sonarsource.sonarlint.core.client.api.standalone.StandaloneRuleDetails;
import org.sonarsource.sonarlint.core.client.api.standalone.StandaloneRuleParam;

/**
 *
 * @author FICHET Philippe &lt;philippe.fichet@laposte.net&gt;
 */
public class SonarLintUtilsTest {
    private static final Project mockedProjectWithProjectScope = Mockito.mock(Project.class);
    private static final Project mockedProjectWithGlobalScope = Mockito.mock(Project.class);

    @RegisterExtension
    private final SonarLintLookupMockedExtension lookupExtension = SonarLintLookupMockedExtension.builder()
        .logCall()
        .mockLookupMethodWith(ProjectManagerImplementation.class, Mockito.mock(ProjectManagerImplementation.class))
        .mockLookupMethodWith(SonarLintOptions.class, Mockito.mock(SonarLintOptions.class))
        .mockLookupMethodWith(
            SonarLintDataManager.class,
            new SonarLintDataManagerMockedBuilder()
                .createPreferences(mockedProjectWithProjectScope)
                .preferencesScope(mockedProjectWithProjectScope, SonarLintProjectPreferenceScope.PROJECT)
                .createPreferences(mockedProjectWithGlobalScope)
                .preferencesScope(mockedProjectWithGlobalScope, SonarLintProjectPreferenceScope.GLOBAL)
                .preferencesScope(SonarLintEngine.GLOBAL_SETTINGS_PROJECT, SonarLintProjectPreferenceScope.GLOBAL)
                .build()
        ).build();

    @Test
    @DisplayName("Analyze hierarchical tree used in \"Analyze with Sonarlint\"")
    public void likeAnalyzeWithSonarlint() throws IOException, BackingStoreException {
        // first step, check all issue in file
        List<Issue> actualIssues = new ArrayList<>();
        List<Issue> expectedIssues = getExpectedIssueForNewClassFile();
        // Clean engine before use
        SonarLintTestUtils.cleanSonarLintEngine();
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

    @Test
    @DisplayName("Analyze hierarchical tree used in \"Analyze with Sonarlint\" with a custom rule parameter value")
    public void likeAnalyzeWithSonarlintWithParameter() throws BackingStoreException, IOException {
        SonarLintEngine sonarLintEngine = SonarLintTestUtils.getCleanSonarLintEngine();
        sonarLintEngine.getAllRuleDetails().forEach(ruleKey -> sonarLintEngine.excludeRuleKey(RuleKey.parse(ruleKey.getKey()), SonarLintEngine.GLOBAL_SETTINGS_PROJECT));
        sonarLintEngine.includeRuleKey(RuleKey.parse("java:S100"), SonarLintEngine.GLOBAL_SETTINGS_PROJECT);
        sonarLintEngine.includeRuleKey(RuleKey.parse("java:S1186"), SonarLintEngine.GLOBAL_SETTINGS_PROJECT);
        sonarLintEngine.setRuleParameter("java:S100", "format", "^.+$", SonarLintEngine.GLOBAL_SETTINGS_PROJECT);

        // first step, check all issue in file
        List<Issue> actualIssues = new ArrayList<>();
        List<Issue> expectedIssues = Arrays.asList(
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
            .build()
        );
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
                "critical (2 issues)"
            ));

        // Critial
        Node[] issueCriticalRuleKeyNodes = severityNodes[0].getChildren().getNodes();
        Assertions.assertThat(issueCriticalRuleKeyNodes)
            .hasSize(1);
        Assertions.assertThat(issueCriticalRuleKeyNodes[0])
            .extracting(Node::getDisplayName)
            .describedAs("java:S1186 -> critical display name")
            .isEqualTo("java:S1186 : Methods should not be empty (2)");
        Node[] critialS1186FileNodes = issueCriticalRuleKeyNodes[0].getChildren().getNodes();
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
            .ruleKey("java:S115")
            .ruleName("Constant names should comply with a naming convention")
            .startLine(10)
            .startLineOffset(31)
            .endLine(10)
            .endLineOffset(56)
            .build(),
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

    @Test
    @DisplayName("Analyze with custom rule parameter value")
    public void ruleParameterChanged() throws MalformedURLException, IOException, BackingStoreException {
        SonarLintEngine sonarLintEngine = SonarLintTestUtils.getCleanSonarLintEngine();
        String ruleKeyString = "java:S115";
        sonarLintEngine.getAllRuleDetails().forEach(ruleKey -> sonarLintEngine.excludeRuleKey(RuleKey.parse(ruleKey.getKey()), SonarLintEngine.GLOBAL_SETTINGS_PROJECT));
        sonarLintEngine.includeRuleKey(RuleKey.parse(ruleKeyString), SonarLintEngine.GLOBAL_SETTINGS_PROJECT);
        File sonarlintFileDemo = FileUtil.normalizeFile(new File("./src/test/resources/SonarLintFileDemo.java").getAbsoluteFile());
        Path sonarlintFileDemoPath = sonarlintFileDemo.toPath();
        sonarLintEngine.setRuleParameter(ruleKeyString, "format", "^.+$", SonarLintEngine.GLOBAL_SETTINGS_PROJECT);
        List<Issue> issues = SonarLintUtils.analyze(
            FileUtil.toFileObject(sonarlintFileDemo),
            new String(Files.readAllBytes(sonarlintFileDemoPath))
        );
        Assertions.assertThat(issues).isEqualTo(Collections.emptyList());
    }

    @Test
    @DisplayName("Analyze with custom rule parameter value on project with global scope")
    public void ruleParameterChangedOnProjectWithGlobalScope() throws MalformedURLException, IOException, BackingStoreException {
        SonarLintDataManager sonarlintDataMangerMocked = lookupExtension.lookupMocked(SonarLintDataManager.class).get();
        SonarLintEngine sonarLintEngine = SonarLintTestUtils.getCleanSonarLintEngine();
        String ruleKeyString = "java:S115";
        Project project = mockedProjectWithGlobalScope;
        sonarLintEngine.getAllRuleDetails().forEach(ruleKey -> sonarLintEngine.excludeRuleKey(RuleKey.parse(ruleKey.getKey()), SonarLintEngine.GLOBAL_SETTINGS_PROJECT));
        sonarLintEngine.includeRuleKey(RuleKey.parse(ruleKeyString), SonarLintEngine.GLOBAL_SETTINGS_PROJECT);
        sonarLintEngine.getAllRuleDetails().forEach(ruleKey -> sonarLintEngine.excludeRuleKey(RuleKey.parse(ruleKey.getKey()), project));
        sonarLintEngine.includeRuleKey(RuleKey.parse(ruleKeyString), project);
        File sonarlintFileDemo = FileUtil.normalizeFile(new File("./src/test/resources/SonarLintFileDemo.java").getAbsoluteFile());
        File sonarlintFileDemoOnFakeProject = FileUtil.normalizeFile(new File("./src/test/resources/fakeproject/SonarLintFileDemo.java").getAbsoluteFile());
        FileObject toFileObject = FileUtil.toFileObject(sonarlintFileDemo);
        FileObject toFileObjectOnFakeProject = FileUtil.toFileObject(sonarlintFileDemoOnFakeProject);
        Mockito.when(sonarlintDataMangerMocked.getProject(toFileObjectOnFakeProject))
            .thenReturn(Optional.of(project));
        List<Issue> issues = new ArrayList<>();
        List<Issue> issuesOnFakeProject = new ArrayList<>();
        sonarLintEngine.setRuleParameter(ruleKeyString, "format", "^.+$", project);
        // When
        issues.addAll(SonarLintUtils.analyze(
            toFileObject,
            new String(Files.readAllBytes(sonarlintFileDemo.toPath()))
        ));
        issuesOnFakeProject.addAll(SonarLintUtils.analyze(
            toFileObjectOnFakeProject,
            new String(Files.readAllBytes(sonarlintFileDemoOnFakeProject.toPath()))
        ));
        // Then
        Assertions.assertThat(issues)
            .hasSize(1);
        Assertions.assertThat(issuesOnFakeProject)
            .hasSize(1);
    }

    @Test
    @DisplayName("Analyze with custom rule parameter value on project")
    public void ruleParameterChangedOnProject() throws MalformedURLException, IOException, BackingStoreException {
        SonarLintDataManager sonarlintDataMangerMocked = lookupExtension.lookupMocked(SonarLintDataManager.class).get();
        SonarLintEngine sonarLintEngine = SonarLintTestUtils.getCleanSonarLintEngine();
        String ruleKeyString = "java:S115";
        sonarLintEngine.getAllRuleDetails().forEach(ruleKey -> sonarLintEngine.excludeRuleKey(RuleKey.parse(ruleKey.getKey()), SonarLintEngine.GLOBAL_SETTINGS_PROJECT));
        sonarLintEngine.includeRuleKey(RuleKey.parse(ruleKeyString), SonarLintEngine.GLOBAL_SETTINGS_PROJECT);
        sonarLintEngine.getAllRuleDetails().forEach(ruleKey -> sonarLintEngine.excludeRuleKey(RuleKey.parse(ruleKey.getKey()), mockedProjectWithProjectScope));
        sonarLintEngine.includeRuleKey(RuleKey.parse(ruleKeyString), mockedProjectWithProjectScope);
        File sonarlintFileDemo = FileUtil.normalizeFile(new File("./src/test/resources/SonarLintFileDemo.java").getAbsoluteFile());
        File sonarlintFileDemoOnFakeProject = FileUtil.normalizeFile(new File("./src/test/resources/fakeproject/SonarLintFileDemo.java").getAbsoluteFile());
        FileObject toFileObject = FileUtil.toFileObject(sonarlintFileDemo);
        FileObject toFileObjectOnFakeProject = FileUtil.toFileObject(sonarlintFileDemoOnFakeProject);
        Mockito.when(sonarlintDataMangerMocked.getProject(toFileObjectOnFakeProject))
            .thenReturn(Optional.of(mockedProjectWithProjectScope));
        List<Issue> issues = new ArrayList<>();
        List<Issue> issuesOnFakeProject = new ArrayList<>();
        sonarLintEngine.setRuleParameter(ruleKeyString, "format", "^.+$", mockedProjectWithProjectScope);
        // When
        issues.addAll(SonarLintUtils.analyze(
            toFileObject,
            new String(Files.readAllBytes(sonarlintFileDemo.toPath()))
        ));
        issuesOnFakeProject.addAll(SonarLintUtils.analyze(
            toFileObjectOnFakeProject,
            new String(Files.readAllBytes(sonarlintFileDemoOnFakeProject.toPath()))
        ));
        // Then
        Assertions.assertThat(issues)
            .hasSize(1);
        Assertions.assertThat(issuesOnFakeProject)
            .isEqualTo(Collections.emptyList());
    }

    @Test
    @DisplayName("Detect nodejs version")
    public void detectNodeJSVersion() throws IOException
    {
        SonarLintTestUtils.installNodeJS();
        File nodeJSDirectory = SonarLintTestUtils.getNodeJSDirectory();
        String node = Utilities.isWindows() ? "node.exe" : "bin/node";
        Optional<Version> detectNodeJSVersion = SonarLintUtils.detectNodeJSVersion(nodeJSDirectory.getAbsolutePath() + File.separator + node);
        Assertions.assertThat(detectNodeJSVersion).isPresent().get().isEqualTo(Version.create(SonarLintTestUtils.getNodeJSVersion()));
    }

    @Test
    @DisplayName("Check default rule parameter value from extractRuleParameters")
    public void extractRuleParametersWithDefaultValue() throws BackingStoreException
    {
        SonarLintEngine sonarLintEngine = SonarLintTestUtils.getCleanSonarLintEngine();
        String ruleKeyString = "java:S115";
        sonarLintEngine.getAllRuleDetails().forEach(ruleKey -> sonarLintEngine.excludeRuleKey(RuleKey.parse(ruleKey.getKey()), SonarLintEngine.GLOBAL_SETTINGS_PROJECT));
        sonarLintEngine.includeRuleKey(RuleKey.parse(ruleKeyString), SonarLintEngine.GLOBAL_SETTINGS_PROJECT);
        Map<StandaloneRuleParam, String> extractRuleParameters = SonarLintUtils.extractRuleParameters(sonarLintEngine, ruleKeyString, SonarLintEngine.GLOBAL_SETTINGS_PROJECT);
        Assertions.assertThat(extractRuleParameters)
            .hasSize(1)
            .containsValue("^[A-Z][A-Z0-9]*(_[A-Z0-9]+)*$");
    }
    
    @Test
    @DisplayName("Check custom rule parameter value from extractRuleParameters")
    public void extractRuleParametersWithCustomValue() throws BackingStoreException
    {
        SonarLintEngine sonarLintEngine = SonarLintTestUtils.getCleanSonarLintEngine();
        String ruleKeyString = "java:S115";
        sonarLintEngine.getAllRuleDetails().forEach(ruleKey -> sonarLintEngine.excludeRuleKey(RuleKey.parse(ruleKey.getKey()), SonarLintEngine.GLOBAL_SETTINGS_PROJECT));
        sonarLintEngine.includeRuleKey(RuleKey.parse(ruleKeyString), SonarLintEngine.GLOBAL_SETTINGS_PROJECT);
        sonarLintEngine.setRuleParameter(ruleKeyString, "format", "^[A-Z]*$", SonarLintEngine.GLOBAL_SETTINGS_PROJECT);
        Map<StandaloneRuleParam, String> extractRuleParameters = SonarLintUtils.extractRuleParameters(sonarLintEngine, ruleKeyString, SonarLintEngine.GLOBAL_SETTINGS_PROJECT);
        Assertions.assertThat(extractRuleParameters)
            .hasSize(1)
            .containsValue("^[A-Z]*$");
    }

    @Test
    @DisplayName("Check custom rule parameter value from extractRuleParameters on project with global scope")
    public void extractRuleParametersWithCustomValueOnProjectWithGlobalScope() throws BackingStoreException, MalformedURLException
    {
        SonarLintEngine sonarLintEngine = new SonarLintEngineImpl();
        SonarLintTestUtils.cleanSonarLintEngine(sonarLintEngine);
        String ruleKey = "java:S115";
        String parameterName = "format";
        String defaultFormatValue = "^[A-Z][A-Z0-9]*(_[A-Z0-9]+)*$";
        String newFormatValue = "^[_A-Z][A-Z0-9]*(_[A-Z0-9]+)*$";
        String newFormatValueOnGlobalScope = "^[_0-9A-Z][A-Z0-9]*(_[A-Z0-9]+)*$";
        sonarLintEngine.setRuleParameter(ruleKey, parameterName, newFormatValue, mockedProjectWithProjectScope);
        sonarLintEngine.setRuleParameter(ruleKey, parameterName, newFormatValueOnGlobalScope, mockedProjectWithGlobalScope);
        Map<StandaloneRuleParam, String> extractRuleParametersProjectScope = SonarLintUtils.extractRuleParameters(sonarLintEngine, ruleKey, mockedProjectWithProjectScope);
        Map<StandaloneRuleParam, String> extractRuleParametersGlobalScope = SonarLintUtils.extractRuleParameters(sonarLintEngine, ruleKey, mockedProjectWithGlobalScope);
        Map<StandaloneRuleParam, String> extractRuleParametersGlobalSetting = SonarLintUtils.extractRuleParameters(sonarLintEngine, ruleKey, SonarLintEngine.GLOBAL_SETTINGS_PROJECT);
        Assertions.assertThat(extractRuleParametersProjectScope)
            .describedAs("rule parameter on project with project scope")
            .extractingFromEntries(SonarLintUtilsTest::toTuple)
            .containsExactlyInAnyOrder(
                Assertions.tuple("format", defaultFormatValue, newFormatValue)
            );
        Assertions.assertThat(extractRuleParametersGlobalScope)
            .describedAs("rule parameter on project with global scope")
            .extractingFromEntries(SonarLintUtilsTest::toTuple)
            .containsExactlyInAnyOrder(
                Assertions.tuple("format", defaultFormatValue, newFormatValueOnGlobalScope)
            );
        Assertions.assertThat(extractRuleParametersGlobalSetting)
            .describedAs("rule parameter on global settings")
            .extractingFromEntries(SonarLintUtilsTest::toTuple)
            .containsExactlyInAnyOrder(
                Assertions.tuple("format", defaultFormatValue, defaultFormatValue)
            );
    }

    @Test
    @DisplayName("Analyze with extra properties on project")
    public void extraPropertiesOnProject() throws IOException, BackingStoreException {
        String commonExtraPropertyName = "sonar.java.source";
        SonarLintDataManager sonarlintDataMangerMocked = lookupExtension.lookupMocked(SonarLintDataManager.class).get();
        SonarLintEngine sonarLintEngine = SonarLintTestUtils.getCleanSonarLintEngine();
        sonarLintEngine.waitingInitialization();
        String ruleKeyString = "java:S3725";
        RuleKey ruleKey = RuleKey.parse(ruleKeyString);
        StandaloneRuleDetails ruleDetails = sonarLintEngine.getRuleDetails(ruleKeyString).get();
        List<RuleKey> allRuleKey = sonarLintEngine.getAllRuleDetails()
            .stream()
            .map( (StandaloneRuleDetails standaloneRuleDetails) -> RuleKey.parse(standaloneRuleDetails.getKey()))
            .collect(Collectors.toList());
        sonarLintEngine.excludeRuleKeys(allRuleKey, SonarLintEngine.GLOBAL_SETTINGS_PROJECT);
        sonarLintEngine.includeRuleKey(ruleKey, SonarLintEngine.GLOBAL_SETTINGS_PROJECT);
        Assertions.assertThat(sonarLintEngine.isExcluded(ruleDetails, SonarLintEngine.GLOBAL_SETTINGS_PROJECT))
            .describedAs("Check no excluded rule \"" + ruleKeyString + "\" on global settings")
            .isFalse();

        sonarLintEngine.excludeRuleKeys(allRuleKey, mockedProjectWithProjectScope);
        sonarLintEngine.includeRuleKey(ruleKey, mockedProjectWithProjectScope);
        Assertions.assertThat(sonarLintEngine.isExcluded(ruleDetails, mockedProjectWithProjectScope))
            .describedAs("Check no excluded rule " + ruleKeyString + " on project scope")
            .isFalse();

        sonarLintEngine.excludeRuleKeys(allRuleKey, mockedProjectWithGlobalScope);
        sonarLintEngine.includeRuleKey(ruleKey, mockedProjectWithGlobalScope);
        Assertions.assertThat(sonarLintEngine.isExcluded(ruleDetails, mockedProjectWithGlobalScope))
            .describedAs("Check no excluded rule " + ruleKeyString + " on global scope")
            .isFalse();

        File sonarlintFileDemo = FileUtil.normalizeFile(new File("./src/test/resources/CheckS3725WithExtraProperties.java").getAbsoluteFile());
        File sonarlintFileDemoOnFakeProject = FileUtil.normalizeFile(new File("./src/test/resources/fakeproject/CheckS3725WithExtraProperties.java").getAbsoluteFile());
        File sonarlintFileDemoOnFakeProject2 = FileUtil.normalizeFile(new File("./src/test/resources/fakeproject2/CheckS3725WithExtraProperties.java").getAbsoluteFile());

        FileObject toFileObject = FileUtil.toFileObject(sonarlintFileDemo);
        FileObject toFileObjectOnFakeProject = FileUtil.toFileObject(sonarlintFileDemoOnFakeProject);
        FileObject toFileObjectOnFakeProject2 = FileUtil.toFileObject(sonarlintFileDemoOnFakeProject2);
        Mockito.when(sonarlintDataMangerMocked.getProject(toFileObjectOnFakeProject))
            .thenReturn(Optional.of(mockedProjectWithProjectScope));
        Mockito.when(sonarlintDataMangerMocked.getProject(toFileObjectOnFakeProject2))
            .thenReturn(Optional.of(mockedProjectWithGlobalScope));
        List<Issue> issues = new ArrayList<>();
        List<Issue> issuesOnFakeProject = new ArrayList<>();
        List<Issue> issuesOnFakeProject2 = new ArrayList<>();
        Map<String, String> extraPropertiesForGlobal = new HashMap<>();
        extraPropertiesForGlobal.put(commonExtraPropertyName, "11");
        Map<String, String> extraPropertiesForProjectScope = new HashMap<>();
        extraPropertiesForProjectScope.put(commonExtraPropertyName, "8");
        Map<String, String> extraPropertiesForGlobalScope = new HashMap<>();
        extraPropertiesForGlobalScope.put(commonExtraPropertyName, "8");
        sonarLintEngine.setAllExtraProperties(extraPropertiesForGlobal, SonarLintEngine.GLOBAL_SETTINGS_PROJECT);
        sonarLintEngine.setAllExtraProperties(extraPropertiesForProjectScope, mockedProjectWithProjectScope);
        sonarLintEngine.setAllExtraProperties(extraPropertiesForGlobalScope, mockedProjectWithGlobalScope);
        
        issues.addAll(SonarLintUtils.analyze(
            toFileObject,
            new String(Files.readAllBytes(sonarlintFileDemo.toPath()))
        ));
        Assertions.assertThat(issues)
            .describedAs("Global property")
            .hasSize(0);

        issuesOnFakeProject.addAll(SonarLintUtils.analyze(
            toFileObjectOnFakeProject,
            new String(Files.readAllBytes(sonarlintFileDemoOnFakeProject.toPath()))
        ));
        Assertions.assertThat(issuesOnFakeProject)
            .describedAs("project scope")
            .hasSize(1);

        issuesOnFakeProject.addAll(SonarLintUtils.analyze(
            toFileObjectOnFakeProject2,
            new String(Files.readAllBytes(sonarlintFileDemoOnFakeProject2.toPath()))
        ));
        Assertions.assertThat(issuesOnFakeProject2)
            .describedAs("global scope")
            .hasSize(0);
    }

    /**
     * Transform entry into tuple with name, default value and actual value
     * @param entry
     * @return 
     */
    private static Tuple toTuple(Map.Entry<StandaloneRuleParam, String> entry)
    {
        StandaloneRuleParam key = entry.getKey();
        return Assertions.tuple(
            key.name(),
            key.defaultValue(),
            entry.getValue()
        );
    }
}
