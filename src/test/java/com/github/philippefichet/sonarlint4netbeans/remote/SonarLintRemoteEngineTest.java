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
package com.github.philippefichet.sonarlint4netbeans.remote;

import com.github.philippefichet.sonarlint4netbeans.ProjectMockedBuilder;
import com.github.philippefichet.sonarlint4netbeans.SonarLintDataManager;
import com.github.philippefichet.sonarlint4netbeans.SonarLintDataManagerMockedBuilder;
import com.github.philippefichet.sonarlint4netbeans.SonarLintUtils;
import com.github.philippefichet.sonarlint4netbeans.issue.DefaultIssueTestImpl;
import com.github.philippefichet.sonarlint4netbeans.issue.IssueTestFactory;
import com.github.philippefichet.sonarlint4netbeans.junit.jupiter.extension.SonarLintLookupMockedExtension;
import com.github.philippefichet.sonarlint4netbeans.project.SonarLintProjectPreferenceScope;
import com.github.philippefichet.sonarlint4netbeans.remote.configuration.SonarLintRemoteConnectionConfiguration;
import com.github.philippefichet.sonarlint4netbeans.remote.configuration.SonarLintRemoteConnectionConfigurationManagement;
import com.github.philippefichet.sonarlint4netbeans.remote.configuration.SonarLintRemoteProjectConfiguration;
import com.github.philippefichet.sonarlint4netbeans.remote.synchronization.TaskWrapper;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mockito;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.sonarsource.sonarlint.core.client.api.common.analysis.Issue;
import org.sonarsource.sonarlint.core.commons.Language;

/**
 *
 * @author FICHET Philippe &lt;philippe.fichet@laposte.net&gt;
 */
@EnabledIfSystemProperty(
    named = "sonarlint4netbeans.test.remote.project-maven1.token",
    matches = "[a-f0-9]+",
    disabledReason = "Disabled because require token to check remote issue on sample project"
)
public class SonarLintRemoteEngineTest {

    private final Project sonarlint4netbeansSampleMavenProject = new ProjectMockedBuilder()
        .projectDirectory(new File("./src/test/samples/sonarlint4netbeans-sample-mavenproject/branch-master"))
        .build();

    private final File sonarlint4netbeansSampleMavenProjectNewClass =
        new File("./src/test/samples/sonarlint4netbeans-sample-mavenproject/branch-master/src/main/java/com/mycompany/mavenproject1/NewClass.java");

    private final Project sonarlint4netbeansSampleMavenProjectWithLocalChange = new ProjectMockedBuilder()
        .projectDirectory(new File("./src/test/samples/sonarlint4netbeans-sample-mavenproject/branch-master-with-local-change"))
        .build();

    private final File sonarlint4netbeansSampleMavenProjectNewClassWithLocalChange =
        new File("./src/test/samples/sonarlint4netbeans-sample-mavenproject/branch-master-with-local-change/src/main/java/com/mycompany/mavenproject1/NewClass.java");

    @RegisterExtension
    SonarLintLookupMockedExtension sonarLintLookupMockedExtension = SonarLintLookupMockedExtension.builder()
        .logCall()
        .mockLookupMethodWith(
            SonarLintDataManager.class,
            new SonarLintDataManagerMockedBuilder()
            // sonarlint4netbeans-sample-mavenproject / master branch
            .createPreferences(sonarlint4netbeansSampleMavenProject)
            .preferencesScope(sonarlint4netbeansSampleMavenProject, SonarLintProjectPreferenceScope.REMOTE)
            .addFileToProject(sonarlint4netbeansSampleMavenProject, sonarlint4netbeansSampleMavenProjectNewClass)
            // sonarlint4netbeans-sample-mavenproject / master branch / local change
            .createPreferences(sonarlint4netbeansSampleMavenProjectWithLocalChange)
            .preferencesScope(sonarlint4netbeansSampleMavenProjectWithLocalChange, SonarLintProjectPreferenceScope.REMOTE)
            .addFileToProject(sonarlint4netbeansSampleMavenProjectWithLocalChange, sonarlint4netbeansSampleMavenProjectNewClassWithLocalChange)
            .build()
        ).mockLookupMethodWith(Project.class, Mockito.mock(Project.class))
        .mockLookupMethodWith(SonarLintRemoteConnectionConfigurationManagement.class, Mockito.mock(SonarLintRemoteConnectionConfigurationManagement.class))
        .build();

    @Test
    public void remote() throws InterruptedException, IOException {
        SonarLintRemoteConnectionConfigurationManagement sonarLintRemoteConnectionConfigurationManagement = Lookup.getDefault().lookup(SonarLintRemoteConnectionConfigurationManagement.class);
        String connectionId = "testing-connection-id-sonarcloud";
        String projectKey = "philippefichet_sonarlint4netbeans-sample-mavenproject";
        String branchName = "master";
        String baseURL = "https://sonarcloud.io";
        boolean isSonarCloud = true;
        String organization = "philippefichet";
        String login = System.getProperty("sonarlint4netbeans.test.remote.project-maven1.token");
        SonarLintRemoteConnectionConfiguration sonarLintRemoteConnectionConfiguration = new SonarLintRemoteConnectionConfiguration(
            connectionId,
            baseURL,
            isSonarCloud
        );
        Mockito.when(sonarLintRemoteConnectionConfigurationManagement.getAuthTokenFromConnectionId(connectionId))
            .thenReturn(Optional.of(login));
        Mockito.when(sonarLintRemoteConnectionConfigurationManagement.getSonarLintConnectionConfigurationFromConnectionId(connectionId))
            .thenReturn(Optional.of(sonarLintRemoteConnectionConfiguration));
        
        SonarLintRemoteEngine remoteEngine = new SonarLintRemoteEngine(new Language[] {Language.JAVA});
        SonarLintRemoteProjectConfiguration sonarLintRemoteProjectConfiguration = new SonarLintRemoteProjectConfiguration(
            sonarlint4netbeansSampleMavenProject,
            connectionId,
            projectKey,
            organization,
            branchName
        );
        SonarLintRemoteProjectConfiguration.save(sonarlint4netbeansSampleMavenProject, connectionId, projectKey, organization);
        TaskWrapper sync = remoteEngine.launchResyncTask(
            sonarLintRemoteProjectConfiguration
        );
        sync.getTask().waitFinished(30_000);
        List<Issue> issues = SonarLintUtils.analyze(FileUtil.toFileObject(sonarlint4netbeansSampleMavenProjectNewClass), null);
        Assertions.assertThat(issues)
        .extracting(DefaultIssueTestImpl::toTuple)
        .containsExactlyInAnyOrder(
            // Local issue
            IssueTestFactory.javaS115(26, 31, 56).buildTuple(),
            IssueTestFactory.javaS1133(33, 16, 32).buildTuple(),
            IssueTestFactory.javaS1186(33, 16, 32).buildTuple(),
            IssueTestFactory.javaS100(35, 16, 47).buildTuple(),
            IssueTestFactory.javaS1186(35, 16, 47).buildTuple(),
            IssueTestFactory.javaS1134(38, 0, 17).buildTuple(),
            IssueTestFactory.javaS2168(41, 12, 24).buildTuple(),
            IssueTestFactory.javaS106(54, 16, 26).buildTuple(),
            IssueTestFactory.javaS1172(60, 44, 50).buildTuple(),
            IssueTestFactory.javaS107(60, 23, 39).buildTuple(),
            IssueTestFactory.javaS106(61, 8, 18).buildTuple(),
            // Server issue
            IssueTestFactory.javaS2629(80).buildTuple(),
            IssueTestFactory.javaS3457(80).buildTuple()
        );
    }

    @Test
    public void remoteWithLocalChange() throws InterruptedException, IOException {
        SonarLintRemoteConnectionConfigurationManagement sonarLintRemoteConnectionConfigurationManagement = Lookup.getDefault().lookup(SonarLintRemoteConnectionConfigurationManagement.class);
        String connectionId = "testing-connection-id-sonarcloud";
        String projectKey = "philippefichet_sonarlint4netbeans-sample-mavenproject";
        String branchName = "master";
        String baseURL = "https://sonarcloud.io";
        boolean isSonarCloud = true;
        String organization = "philippefichet";
        String login = System.getProperty("sonarlint4netbeans.test.remote.project-maven1.token");
        SonarLintRemoteConnectionConfiguration sonarLintRemoteConnectionConfiguration = new SonarLintRemoteConnectionConfiguration(
            connectionId,
            baseURL,
            isSonarCloud
        );
        Mockito.when(sonarLintRemoteConnectionConfigurationManagement.getAuthTokenFromConnectionId(connectionId))
            .thenReturn(Optional.of(login));
        Mockito.when(sonarLintRemoteConnectionConfigurationManagement.getSonarLintConnectionConfigurationFromConnectionId(connectionId))
            .thenReturn(Optional.of(sonarLintRemoteConnectionConfiguration));
        
        SonarLintRemoteEngine remoteEngine = new SonarLintRemoteEngine(new Language[] {Language.JAVA});
        SonarLintRemoteProjectConfiguration sonarLintRemoteProjectConfiguration = new SonarLintRemoteProjectConfiguration(
            sonarlint4netbeansSampleMavenProjectWithLocalChange,
            connectionId,
            projectKey,
            organization,
            branchName
        );
        SonarLintRemoteProjectConfiguration.save(sonarlint4netbeansSampleMavenProjectWithLocalChange, connectionId, projectKey, organization);
        TaskWrapper sync = remoteEngine.launchResyncTask(
            sonarLintRemoteProjectConfiguration
        );
        sync.getTask().waitFinished(30_000);
        List<Issue> issues = SonarLintUtils.analyze(FileUtil.toFileObject(sonarlint4netbeansSampleMavenProjectNewClassWithLocalChange), null);
        Assertions.assertThat(issues)
        .filteredOn("ruleKey", "java:S2629")
        .extracting(DefaultIssueTestImpl::toTuple)
        .containsAnyOf(IssueTestFactory.javaS2629(82).buildTuple());
    }
}
