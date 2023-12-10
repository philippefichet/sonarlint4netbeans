/*
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
package com.github.philippefichet.sonarlint4netbeans.remote.synchronization;

import com.github.philippefichet.sonarlint4netbeans.remote.configuration.SonarLintRemoteConnectionConfiguration;
import com.github.philippefichet.sonarlint4netbeans.remote.configuration.SonarLintRemoteConnectionConfigurationManagement;
import com.github.philippefichet.sonarlint4netbeans.remote.configuration.SonarLintRemoteProjectConfiguration;
import com.github.philippefichet.sonarlint4netbeans.remote.listener.SonarLintRemoteEnginSyncListener;
import com.github.philippefichet.sonarlint4netbeans.remote.listener.SonarLintRemoteSyncListenerProgressHandle;
import com.github.philippefichet.sonarlint4netbeans.remote.wrapper.SonarLintRemoteHttpClient;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.logging.Logger;
import org.openide.util.Cancellable;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.sonarsource.sonarlint.core.ConnectedSonarLintEngineImpl;
import org.sonarsource.sonarlint.core.commons.progress.ClientProgressMonitor;
import org.sonarsource.sonarlint.core.serverapi.EndpointParams;
import org.sonarsource.sonarlint.core.serverapi.exception.NotFoundException;
import org.sonarsource.sonarlint.core.serverapi.exception.ServerErrorException;

/**
 *
 * @author FICHET Philippe &lt;philippe.fichet@laposte.net&gt;
 */
public class SonarLintRemoteSynchronizationTask implements Cancellable, Runnable {

    private static final Logger LOG = Logger.getLogger(SonarLintRemoteSynchronizationTask.class.getName());
    
    public enum SyncPolicies {
        SYNC,
        UPDATE_PROJECT,
        DOWNLOAD_ALL_SERVER_ISSUES,
        DOWNLOAD_ALL_SERVER_ISSUES_FOR_FILE,
    }
    
    private final AtomicBoolean forceStop = new AtomicBoolean(false);
    private final SonarLintRemoteProjectConfiguration sonarLintRemoteProjectConfiguration;
    private final SonarLintRemoteEnginSyncListener sonarLintRemoteEnginSyncListener;
    private final Supplier<ConnectedSonarLintEngineImpl> connectedSonarLintEngineImplSupplier;
    private final EnumSet<SyncPolicies> sonarLintRemoteSyncPolicies;

    public SonarLintRemoteSynchronizationTask(
        SonarLintRemoteProjectConfiguration sonarLintRemoteProjectConfiguration,
        Supplier<ConnectedSonarLintEngineImpl> connectedSonarLintEngineImplSupplier,
        EnumSet<SyncPolicies> sonarLintRemoteSyncPolicies
    ) {
        this.sonarLintRemoteProjectConfiguration = sonarLintRemoteProjectConfiguration;
        this.sonarLintRemoteEnginSyncListener = new SonarLintRemoteSyncListenerProgressHandle(
            sonarLintRemoteProjectConfiguration.getProjectKey(),
            this::cancel
        );
        this.connectedSonarLintEngineImplSupplier = connectedSonarLintEngineImplSupplier;
        this.sonarLintRemoteSyncPolicies = sonarLintRemoteSyncPolicies;
    }

    @Override
    public boolean cancel() {
        forceStop.set(true);
        return true;
    }

    @Override
    public void run() {
        try {
            sync();
        } catch (RuntimeException ex) {
            cancel();
            sonarLintRemoteEnginSyncListener.consume(
                SonarLintRemoteEnginSyncListener.Status.CANCELLED,
                "Error while sync: " + ex.getMessage()
            );
            Exceptions.printStackTrace(ex);
            throw ex;
        }
    }
    private void sync() {
        sonarLintRemoteEnginSyncListener.consume(SonarLintRemoteEnginSyncListener.Status.RUNNING, "Initialization ...");
        String connectionId = sonarLintRemoteProjectConfiguration.getConnectionId();
        String projectKey = sonarLintRemoteProjectConfiguration.getProjectKey();
        SonarLintRemoteConnectionConfigurationManagement sonarLintRemoteConnectionConfigurationManagement = Lookup.getDefault().lookup(SonarLintRemoteConnectionConfigurationManagement.class);
        Optional<SonarLintRemoteConnectionConfiguration> sonarLintConnectionConfigurationFromConnectionId = sonarLintRemoteConnectionConfigurationManagement.getSonarLintConnectionConfigurationFromConnectionId(connectionId);
        Optional<String> authTokenFromConnectionId = sonarLintRemoteConnectionConfigurationManagement.getAuthTokenFromConnectionId(connectionId);
        if (sonarLintConnectionConfigurationFromConnectionId.isEmpty()) {
            sonarLintRemoteEnginSyncListener.consume(
                SonarLintRemoteEnginSyncListener.Status.FINISH,
                "Connection not found"
            );
            return;
        }
        if (authTokenFromConnectionId.isEmpty()) {
            sonarLintRemoteEnginSyncListener.consume(
                SonarLintRemoteEnginSyncListener.Status.FINISH,
                "Auth token not found"
            );
            return;
        }

        ConnectedSonarLintEngineImpl connectedSonarLintEngineImpl = connectedSonarLintEngineImplSupplier.get();
        AtomicReference<String> prefixStep = new AtomicReference<>("Initialization");
        AtomicReference<String> prefixMessage = new AtomicReference<>();
        AtomicInteger currentStep = new AtomicInteger(0);
        SonarLintRemoteHttpClient sonarLintRemoteHttpClient = new SonarLintRemoteHttpClient(
            authTokenFromConnectionId.get(),
            sonarLintConnectionConfigurationFromConnectionId.get().isIsSonarCloud()
            ? SonarLintRemoteHttpClient.Authentification.BEARER
            : SonarLintRemoteHttpClient.Authentification.BASIC,
            l -> {
                sonarLintRemoteEnginSyncListener.consume(
                    SonarLintRemoteEnginSyncListener.Status.RUNNING,
                    "[" + prefixStep.get() + "] " + prefixMessage.get() + " (" + l + " bytes)"
                );
                if (Thread.currentThread().isInterrupted() || forceStop.get()) {
                    throw new IllegalStateException("Thread " + Thread.currentThread().getName() + " is interrupted");
                }
            }
        );
        EndpointParams endpointParams = new EndpointParams(
            sonarLintConnectionConfigurationFromConnectionId.get().getBaseURL(),
            sonarLintConnectionConfigurationFromConnectionId.get().isIsSonarCloud(),
            sonarLintRemoteProjectConfiguration.getOrganization()
        );
        ClientProgressMonitor clientProgressMonitor = new ClientProgressMonitor() {
            @Override
            public void setMessage(String msg) {
                prefixMessage.set(msg);
                sonarLintRemoteEnginSyncListener.consume(
                    SonarLintRemoteEnginSyncListener.Status.RUNNING,
                    "[" + prefixStep.get() + "] " + msg
                );
                LOG.info(msg);
                //if (Thread.currentThread().isInterrupted()) {
                if (Thread.currentThread().isInterrupted()) {
                    throw new IllegalStateException("Thread " + Thread.currentThread().getName() + " is interrupted");
                }
            }

            @Override
            public void setFraction(float fraction) {
                if (fraction < 1.0 && !Float.isNaN(fraction))
                {
                    System.out.println("fraction = " + fraction);
                }
            }

            @Override
            public void setIndeterminate(boolean indeterminate) {
            }
        };


        if (sonarLintRemoteSyncPolicies.contains(SyncPolicies.UPDATE_PROJECT)) {
            prefixStep.set("Update project");
            currentStep.incrementAndGet();
            connectedSonarLintEngineImpl.updateProject(endpointParams, sonarLintRemoteHttpClient, projectKey, clientProgressMonitor);
        }
        // TODO "-J--add-exports java.base/sun.nio.ch=ALL-UNNAMED"
        if (sonarLintRemoteSyncPolicies.contains(SyncPolicies.DOWNLOAD_ALL_SERVER_ISSUES)) {
            prefixStep.set("Download server issues");
            currentStep.incrementAndGet();
            connectedSonarLintEngineImpl.downloadAllServerIssues(endpointParams, sonarLintRemoteHttpClient, projectKey, sonarLintRemoteProjectConfiguration.getProjectActiveBranch().orElse("main"), clientProgressMonitor);
        }

        if (sonarLintRemoteSyncPolicies.contains(SyncPolicies.SYNC))
        {
            prefixStep.set("Syncing ... ");
            currentStep.incrementAndGet();
            try {
                connectedSonarLintEngineImpl.sync(
                    endpointParams,
                    sonarLintRemoteHttpClient,
                    Collections.singleton(projectKey),
                    clientProgressMonitor
                );
                sonarLintRemoteEnginSyncListener.consume(
                    SonarLintRemoteEnginSyncListener.Status.FINISH,
                    "Done"
                );
            } catch (IllegalStateException | NotFoundException | ServerErrorException ex) {
                sonarLintRemoteEnginSyncListener.consume(
                    SonarLintRemoteEnginSyncListener.Status.FINISH,
                    "End with error: " + ex.getLocalizedMessage()
                );
            }
        }
    }
}
