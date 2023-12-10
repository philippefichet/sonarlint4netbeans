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

import java.util.Objects;
import org.openide.util.RequestProcessor;

/**
 *
 * @author FICHET Philippe &lt;philippe.fichet@laposte.net&gt;
 */
public final class TaskWrapper {
    private final RequestProcessor.Task task;
    private final SonarLintRemoteSynchronizationTask sonarLintRemoteSynchronizationTask;

    public TaskWrapper(RequestProcessor.Task task, SonarLintRemoteSynchronizationTask sonarLintRemoteSynchronizationTask) {
        this.task = task;
        this.sonarLintRemoteSynchronizationTask = sonarLintRemoteSynchronizationTask;
    }

    public RequestProcessor.Task getTask() {
        return task;
    }

    public SonarLintRemoteSynchronizationTask getSonarLintRemoteSynchronizationTask() {
        return sonarLintRemoteSynchronizationTask;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + Objects.hashCode(this.task);
        hash = 79 * hash + Objects.hashCode(this.sonarLintRemoteSynchronizationTask);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TaskWrapper other = (TaskWrapper) obj;
        if (!Objects.equals(this.task, other.task)) {
            return false;
        }
        return Objects.equals(this.sonarLintRemoteSynchronizationTask, other.sonarLintRemoteSynchronizationTask);
    }
    
    
}
