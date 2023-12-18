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

import com.github.philippefichet.sonarlint4netbeans.SonarLintDataManager;
import com.github.philippefichet.sonarlint4netbeans.SonarLintEngine;
import com.github.philippefichet.sonarlint4netbeans.SonarLintUtils;
import com.github.philippefichet.sonarlint4netbeans.remote.configuration.SonarLintRemoteProjectConfiguration;
import com.github.philippefichet.sonarlint4netbeans.remote.synchronization.SonarLintRemoteSynchronizationTask;
import com.github.philippefichet.sonarlint4netbeans.remote.synchronization.TaskWrapper;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.prefs.Preferences;
import org.netbeans.api.project.Project;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.openide.util.lookup.ServiceProvider;
import org.sonarsource.sonarlint.core.ConnectedSonarLintEngineImpl;
import org.sonarsource.sonarlint.core.analysis.api.AnalysisResults;
import org.sonarsource.sonarlint.core.client.api.common.AbstractGlobalConfiguration;
import org.sonarsource.sonarlint.core.client.api.common.analysis.IssueListener;
import org.sonarsource.sonarlint.core.client.api.connected.ConnectedAnalysisConfiguration;
import org.sonarsource.sonarlint.core.client.api.connected.ConnectedGlobalConfiguration;
import org.sonarsource.sonarlint.core.client.api.connected.ProjectBranches;
import org.sonarsource.sonarlint.core.commons.Language;
import org.sonarsource.sonarlint.core.commons.Version;
import org.sonarsource.sonarlint.core.commons.log.ClientLogOutput;
import org.sonarsource.sonarlint.core.commons.progress.ClientProgressMonitor;
import org.sonarsource.sonarlint.core.serverconnection.ProjectBinding;
import org.sonarsource.sonarlint.core.serverconnection.issues.ServerIssue;

/**
 *
 * @author FICHET Philippe &lt;philippe.fichet@laposte.net&gt;
 */
@ServiceProvider(service = SonarLintRemoteEngine.class)
public final class SonarLintRemoteEngine {

    private static final RequestProcessor RP = new RequestProcessor(SonarLintRemoteEngine.class);
    private static final Map<String, TaskWrapper> ALL_CURRENT_SYNC_TASKS = Collections.synchronizedMap(new HashMap<>());
    private static final Map<String, Long> LAST_SYNC = Collections.synchronizedMap(new HashMap<>());
    private static final String PREFIX_RUNTIME_PREFERENCE = "runtime.";
    private static final String RUNTIME_NODE_JS_PATH_PREFERENCE = "nodejs.path";
    private static final String RUNTIME_NODE_JS_VERSION_PREFERENCE = "nodejs.version";
    private final Clock clock = Clock.systemUTC();
    private final Lookup lookup = Lookup.getDefault();

    private final Map<String, ConnectedSonarLintEngineImpl> connectedSonarLintEngineImpls = Collections.synchronizedMap(new HashMap<>());

    /**
     * Default constructor for Lookup
     */
    public SonarLintRemoteEngine() {
    }
    
    private String toKey(SonarLintRemoteProjectConfiguration sonarLintRemoteProjectConfiguration) {
        return sonarLintRemoteProjectConfiguration.getProjectKey() + "-" +
            sonarLintRemoteProjectConfiguration.getOrganization() + "-" +
            sonarLintRemoteProjectConfiguration.getConnectionId() + "-" +
            sonarLintRemoteProjectConfiguration.getProjectActiveBranch();
    }

    public Optional<TaskWrapper> getLaunchedResyncTask(
        SonarLintRemoteProjectConfiguration sonarLintRemoteProjectConfiguration
    ) {
        String key = toKey(sonarLintRemoteProjectConfiguration);
        TaskWrapper currentTask = ALL_CURRENT_SYNC_TASKS.get(key);
        return Optional.ofNullable(currentTask);
    }

    public TaskWrapper launchResyncTask(
        SonarLintRemoteProjectConfiguration sonarLintRemoteProjectConfiguration
    ) {
        String key = sonarLintRemoteProjectConfiguration.getProjectKey() + "-" +
            sonarLintRemoteProjectConfiguration.getOrganization() + "-" +
            sonarLintRemoteProjectConfiguration.getConnectionId() + "-" +
            sonarLintRemoteProjectConfiguration.getProjectActiveBranch();
        TaskWrapper currentTask = ALL_CURRENT_SYNC_TASKS.get(key);

        if (currentTask != null && !currentTask.getTask().isFinished()) {
            return currentTask;
        }
        SonarLintRemoteSynchronizationTask sync = createSyncTask(
            sonarLintRemoteProjectConfiguration,
            EnumSet.of(SonarLintRemoteSynchronizationTask.SyncPolicies.DOWNLOAD_ALL_SERVER_ISSUES,
                SonarLintRemoteSynchronizationTask.SyncPolicies.UPDATE_PROJECT,
                SonarLintRemoteSynchronizationTask.SyncPolicies.SYNC
            )
        );
        RequestProcessor.Task create = RP.post(sync);
        create.addTaskListener((Task task) -> {
            ALL_CURRENT_SYNC_TASKS.remove(key);
            LAST_SYNC.put(key, clock.millis());
        });
        TaskWrapper taskWrapper = new TaskWrapper(create, sync);
        ALL_CURRENT_SYNC_TASKS.put(key, taskWrapper);
        return taskWrapper;
    }

    protected SonarLintRemoteSynchronizationTask createSyncTask(
        SonarLintRemoteProjectConfiguration sonarLintRemoteProjectConfiguration,
        EnumSet<SonarLintRemoteSynchronizationTask.SyncPolicies> enumSet
    ) {
        return new SonarLintRemoteSynchronizationTask(
            sonarLintRemoteProjectConfiguration,
            () -> getConnectedSonarLintEngineImpl(sonarLintRemoteProjectConfiguration),
            enumSet
        );
    }

    private ConnectedSonarLintEngineImpl getConnectedSonarLintEngineImpl(SonarLintRemoteProjectConfiguration sonarLintRemoteProjectConfiguration)
    {
        String sonarLintHome = System.getProperty("user.home") + File.separator + ".sonarlint4netbeans";
        return connectedSonarLintEngineImpls.computeIfAbsent(
            sonarLintRemoteProjectConfiguration.getConnectionId(),
            c ->
                new ConnectedSonarLintEngineImpl(
                    createBuilder()
                    .setConnectionId(c)
                    .setStorageRoot(Paths.get(sonarLintHome, "storage"))
                    .setWorkDir(Paths.get(sonarLintHome, "work"))
                    .addEnabledLanguages(Language.values())
                    .build()
                )
        );
    }
    
    private ConnectedGlobalConfiguration.Builder createBuilder()
    {
        ConnectedGlobalConfiguration.Builder builder = ConnectedGlobalConfiguration.sonarCloudBuilder();
        // TODO à vérifier
        Optional<String> nodeJSPathOptional = getNodeJSPath();
        Optional<Version> nodeJSVersionOptional = getNodeJSVersion();
        if (nodeJSPathOptional.isPresent() && nodeJSVersionOptional.isPresent()) {
            String nodeJSPath = nodeJSPathOptional.get();
            Version nodeJSVersion = nodeJSVersionOptional.get();
            Path nodeJS = Paths.get(nodeJSPath);
            builder.setNodeJs(nodeJS, nodeJSVersion);
        } else {
            tryToSetDefaultNodeJS(builder);
        }
        return builder;
    }

    public AnalysisResults analyze(
        SonarLintRemoteProjectConfiguration sonarLintRemoteProjectConfiguration,
        ConnectedAnalysisConfiguration configuration,
        IssueListener issueListener,
        ClientLogOutput logOutput,
        ClientProgressMonitor monitor
    ) {
        waitingSync(sonarLintRemoteProjectConfiguration);
        ConnectedSonarLintEngineImpl connectedSonarLintEngineImpl = getConnectedSonarLintEngineImpl(sonarLintRemoteProjectConfiguration);
        return connectedSonarLintEngineImpl.analyze(
            configuration,
            issueListener,
            logOutput,
            monitor
        );
    }

    private void waitingSync(SonarLintRemoteProjectConfiguration sonarLintRemoteProjectConfiguration)
    {
        Long get = LAST_SYNC.get(toKey(sonarLintRemoteProjectConfiguration));
        if (get == null) {
            launchResyncTask(sonarLintRemoteProjectConfiguration)
            .getTask()
            .waitFinished();
        }
    }
    
    public List<ServerIssue> getServerIssues(
        SonarLintRemoteProjectConfiguration sonarLintRemoteProjectConfiguration,
        String relativizePathToAnalyze
    )
    {
        waitingSync(sonarLintRemoteProjectConfiguration);
        ProjectBinding projectBinding = new ProjectBinding(
            sonarLintRemoteProjectConfiguration.getProjectKey(),
            "",
            ""
        );
        
        ConnectedSonarLintEngineImpl connectedSonarLintEngineImpl = getConnectedSonarLintEngineImpl(sonarLintRemoteProjectConfiguration);
//        connectedSonarLintEngineImpl.getActiveRuleDetails(endpoint, client, relativizePathToAnalyze, relativizePathToAnalyze);
//        connectedSonarLintEngineImpl.downloadAllServerIssuesForFile(endpoint, client, projectBinding, relativizePathToAnalyze, relativizePathToAnalyze, monitor);AllServerIssues(endpoint, client, relativizePathToAnalyze, relativizePathToAnalyze, monitor);
        return connectedSonarLintEngineImpl.getServerIssues(
            projectBinding,
            findBestBranch(connectedSonarLintEngineImpl, sonarLintRemoteProjectConfiguration),
            relativizePathToAnalyze
        );
    }

    private String findBestBranch(ConnectedSonarLintEngineImpl connectedSonarLintEngineImpl, SonarLintRemoteProjectConfiguration sonarLintRemoteProjectConfiguration) {
        ProjectBranches serverBranches = connectedSonarLintEngineImpl.getServerBranches(sonarLintRemoteProjectConfiguration.getProjectKey());
        if (sonarLintRemoteProjectConfiguration.getProjectActiveBranch().isPresent()) {
            String branchProject = sonarLintRemoteProjectConfiguration.getProjectActiveBranch().get();
            return serverBranches.getBranchNames()
                .stream()
                .filter(b -> b.equals(branchProject))
                .findFirst().orElseGet(serverBranches::getMainBranchName);
        }
        return serverBranches.getMainBranchName();
    }

    /************************/
    private SonarLintDataManager getSonarLintDataManager()
    {
        return lookup.lookup(SonarLintDataManager.class);
    }

    public Preferences getPreferences(Project project) {
        SonarLintDataManager dataManager = getSonarLintDataManager();
        if (project == SonarLintEngine.GLOBAL_SETTINGS_PROJECT) {
            return dataManager.getGlobalSettingsPreferences();
        } else {
            return dataManager.getPreferences(project);
        }
    }

    private void tryToSetDefaultNodeJS(AbstractGlobalConfiguration.AbstractBuilder configBuilder) {
        SonarLintUtils.tryToSearchDefaultNodeJS(
            () -> SonarLintUtils.searchPathEnvVar().orElse(""),
            configBuilder::setNodeJs
        );
    }

    public Optional<String> getNodeJSPath() {
        return Optional.ofNullable(getPreferences(SonarLintEngine.GLOBAL_SETTINGS_PROJECT).get(PREFIX_RUNTIME_PREFERENCE + RUNTIME_NODE_JS_PATH_PREFERENCE, null));
    }

    public Optional<Version> getNodeJSVersion() {
        return Optional.ofNullable(
            getPreferences(SonarLintEngine.GLOBAL_SETTINGS_PROJECT).get(PREFIX_RUNTIME_PREFERENCE + RUNTIME_NODE_JS_VERSION_PREFERENCE, null)
        ).map(Version::create);
    }
}
