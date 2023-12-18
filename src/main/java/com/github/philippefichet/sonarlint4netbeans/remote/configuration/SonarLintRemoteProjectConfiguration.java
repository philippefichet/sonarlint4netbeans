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
package com.github.philippefichet.sonarlint4netbeans.remote.configuration;

import java.io.File;
import java.util.Map;
import java.util.Optional;
import java.util.prefs.Preferences;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.libs.git.GitBranch;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitRepository;
import org.netbeans.libs.git.progress.ProgressMonitor;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author FICHET Philippe &lt;philippe.fichet@laposte.net&gt;
 */
public class SonarLintRemoteProjectConfiguration {
    private static final String PROP_CONNECTION_ID = "sonarlint-remote-connection-id";
    private static final String PROP_PROJECT_KEY = "sonarlint-remote-project-key";
    private static final String PROP_ORGANIZATION = "sonarlint-remote-oorganization";
    private final Project project;
    private final String connectionId;
    private final String projectKey;
    private final String organization;
    private final String activeBranch;

    public SonarLintRemoteProjectConfiguration(Project project, String connectionId, String projectKey, String organization, String activeBranch) {
        this.project = project;
        this.connectionId = connectionId;
        this.projectKey = projectKey;
        this.organization = organization;
        this.activeBranch = activeBranch;
    }

    public static SonarLintRemoteProjectConfiguration fromProject(Project project) {
        Preferences preferences = ProjectUtils.getPreferences(project, SonarLintRemoteProjectConfiguration.class, true);
        return new SonarLintRemoteProjectConfiguration(
            project,
            preferences.get(PROP_CONNECTION_ID, null),
            preferences.get(PROP_PROJECT_KEY, null),
            preferences.get(PROP_ORGANIZATION, null),
            null
        );
    }

    public static SonarLintRemoteProjectConfiguration fromProject(Project project, String connectionId, String projectKey, String organization) {
        File projectDir = FileUtil.toFile(project.getProjectDirectory());
        GitRepository instance = GitRepository.getInstance(projectDir);
        Map<String, GitBranch> branches = null;
        try {
            branches = instance.createClient().getBranches(false, new ProgressMonitor.DefaultProgressMonitor());
        } catch (GitException ex) {
        }
        return new SonarLintRemoteProjectConfiguration(
            project,
            connectionId,
            projectKey,
            organization,
            branches == null ? null : branches.values().iterator().next().getName()
        );
    }

    public static void save(Project project, String sonarLintRemoteConnectionId, String projectKey, String organization) {
        Preferences preferences = ProjectUtils.getPreferences(project, SonarLintRemoteProjectConfiguration.class, true);
        preferences.put(PROP_CONNECTION_ID, sonarLintRemoteConnectionId);
        preferences.put(PROP_PROJECT_KEY, projectKey);
        preferences.put(PROP_ORGANIZATION, organization);
    }

    public Optional<String> getProjectActiveBranch() {
        if (activeBranch != null) {
            return Optional.of(activeBranch);
        }
        File projectDir = FileUtil.toFile(project.getProjectDirectory());

        GitRepository instance = GitRepository.getInstance(projectDir);
        Map<String, GitBranch> branches = null;
        try {
            branches = instance.createClient().getBranches(false, new ProgressMonitor.DefaultProgressMonitor());
        } catch (GitException ex) {
            // Exceptions.printStackTrace(ex);
        }
        if (branches != null) {
            return Optional.of(branches.values().iterator().next().getName());
        }
        return Optional.empty();
    }

    public String getConnectionId() {
        return connectionId;
    }

    public String getProjectKey() {
        return projectKey;
    }

    public String getOrganization() {
        return organization;
    }

    public Project getProject() {
        return project;
    }
}
