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
package com.github.philippefichet.sonarlint4netbeans.remote.listener;

import org.netbeans.api.progress.ProgressHandle;
import org.openide.util.Cancellable;
import org.openide.util.Exceptions;

/**
 *
 * @author FICHET Philippe &lt;philippe.fichet@laposte.net&gt;
 */
public class SonarLintRemoteSyncListenerProgressHandle implements SonarLintRemoteEnginSyncListener {
    private final ProgressHandle handle;

    public SonarLintRemoteSyncListenerProgressHandle(String projectKey, Cancellable cancellable) {
        handle = ProgressHandle.createHandle("SonarLint Sync (init)", cancellable);
        handle.setDisplayName("SonarLint " + projectKey);
        handle.start();
        handle.switchToIndeterminate();
        handle.progress("Initialization");
    }

    @Override
    public void consume(Status status, String message) {
        if(status == Status.FINISH) {
            handle.close();
            return;
        }

        if(status == Status.CANCELLED) {
            handle.setDisplayName("SonarLint Sync (canceled)");
            handle.progress("Canceled");
            handle.close();
            return;
        }

        handle.progress(message);
        try {
            Thread.sleep(1);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

}
