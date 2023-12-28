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

import com.github.philippefichet.sonarlint4netbeans.SonarLintDataManager;
import com.github.philippefichet.sonarlint4netbeans.git.GitUtils;
import java.util.Optional;
import java.util.prefs.Preferences;
import org.netbeans.api.project.Project;
import org.openide.util.Lookup;

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
        SonarLintDataManager sonarLintDataManager = Lookup.getDefault().lookup(SonarLintDataManager.class);
        Preferences preferences = sonarLintDataManager.getRemoteConfigurationPreferences(project);
        return new SonarLintRemoteProjectConfiguration(
            project,
            preferences.get(PROP_CONNECTION_ID, null),
            preferences.get(PROP_PROJECT_KEY, null),
            preferences.get(PROP_ORGANIZATION, null),
            null
        );
    }

    public static SonarLintRemoteProjectConfiguration fromProject(Project project, String connectionId, String projectKey, String organization) {
        return new SonarLintRemoteProjectConfiguration(
            project,
            connectionId,
            projectKey,
            organization,
            GitUtils.getProjectActiveBranch(project.getProjectDirectory()).orElse(null)
        );
    }

    public static void save(Project project, String sonarLintRemoteConnectionId, String projectKey, String organization) {
        SonarLintDataManager sonarLintDataManager = Lookup.getDefault().lookup(SonarLintDataManager.class);
        Preferences preferences = sonarLintDataManager.getRemoteConfigurationPreferences(project);
        preferences.put(PROP_CONNECTION_ID, sonarLintRemoteConnectionId);
        preferences.put(PROP_PROJECT_KEY, projectKey);
        preferences.put(PROP_ORGANIZATION, organization);
    }

    public Optional<String> getProjectActiveBranch() {
        if (activeBranch != null) {
            return Optional.of(activeBranch);
        }
        return GitUtils.getProjectActiveBranch(project.getProjectDirectory());
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
