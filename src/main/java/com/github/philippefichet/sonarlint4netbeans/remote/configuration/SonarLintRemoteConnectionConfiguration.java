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

/**
 *
 * @author FICHET Philippe &lt;philippe.fichet@laposte.net&gt;
 */
public class SonarLintRemoteConnectionConfiguration {
    private final String connectionId;
    private final String baseURL;
    private final boolean isSonarCloud;

    public SonarLintRemoteConnectionConfiguration(String connectionId, String baseURL, boolean isSonarCloud) {
        this.connectionId = connectionId;
        this.baseURL = baseURL;
        this.isSonarCloud = isSonarCloud;
    }

    public String getConnectionId() {
        return connectionId;
    }

    public String getBaseURL() {
        return baseURL;
    }

    public boolean isIsSonarCloud() {
        return isSonarCloud;
    }

}
