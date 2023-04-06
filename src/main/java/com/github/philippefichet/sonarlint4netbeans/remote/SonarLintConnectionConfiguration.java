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
package com.github.philippefichet.sonarlint4netbeans.remote;

/**
 *
 * @author FICHET Philippe &lt;philippe.fichet@laposte.net&gt;
 */
public class SonarLintConnectionConfiguration {
    private final String connectionId;
    private final String projectKey;
    private final String baseURL;
    private final boolean isSonarCloud;
    private final String organization;

    public SonarLintConnectionConfiguration(String connectionId, String projectKey, String baseURL, boolean isSonarCloud, String organization) {
        this.connectionId = connectionId;
        this.projectKey = projectKey;
        this.baseURL = baseURL;
        this.isSonarCloud = isSonarCloud;
        this.organization = organization;
    }

    public String getConnectionId() {
        return connectionId;
    }

    public String getProjectKey() {
        return projectKey;
    }

    public String getBaseURL() {
        return baseURL;
    }

    public boolean isIsSonarCloud() {
        return isSonarCloud;
    }

    public String getOrganization() {
        return organization;
    }

}
