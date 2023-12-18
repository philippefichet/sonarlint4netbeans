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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.List;
import java.util.Optional;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.netbeans.api.keyring.Keyring;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author FICHET Philippe &lt;philippe.fichet@laposte.net&gt;
 */
@ServiceProvider(service = SonarLintRemoteConnectionConfigurationManagement.class)
public class SonarLintRemoteConnectionConfigurationManagement {

    private final Gson gson = new Gson();

    /**
     * Default constructor for Lookup
     */
    public SonarLintRemoteConnectionConfigurationManagement()
    {
/*
        Project project = Lookup.getDefault().lookup(Project.class);
        PropertyEvaluator evaluator = project.getLookup().lookup(PropertyEvaluator.class);
        String branch = evaluator.getProperty("branch");
        System.out.println("Current branch: " + branch);

        Project project = FileUtil.toFileObject(FileUtil.toFile(project)).getLookup().lookup(Project.class);
        Project mainProject = ProjectUtils.getMainProject(project);
        System.out.println("Main project: " + mainProject.getProjectDirectory().getName());

        */
    }

    public Optional<String> getAuthTokenFromConnectionId(String connectionId) {
        return Optional.ofNullable(
        Keyring.read(SonarLintRemoteConnectionConfiguration.class.getCanonicalName() + ".auth-token." + connectionId)
        ).map(String::new);
    }

    public void saveAuthTokenFromConnectionId(String connectionId, String token) {
        Keyring.save(
            SonarLintRemoteConnectionConfiguration.class.getCanonicalName() + ".auth-token." + connectionId,
            token.toCharArray(),
            "Auth token for SonarLint Remote connectionId \"" + connectionId + "\""
        );
    }

    public void deleteAuthTokenFromConnectionId(String connectionId) {
        Keyring.delete(
            SonarLintRemoteConnectionConfiguration.class.getCanonicalName() + ".auth-token." + connectionId
        );
    }

    public Optional<SonarLintRemoteConnectionConfiguration> getSonarLintConnectionConfigurationFromConnectionId(String connectionId) {
        return getAllSonarLintConnectionConfigurations().stream()
            .filter(c -> c.getConnectionId().equals(connectionId))
            .findFirst();
    }

    public void deleteSonarLintConnectionConfigurationFromConnectionId(String connectionId) {
        Preferences preferences = NbPreferences.forModule(SonarLintRemoteConnectionConfiguration.class);
        preferences.put(
            "connections",
            gson.toJson(
                    getAllSonarLintConnectionConfigurations()
                        .stream()
                        .filter(c -> !c.getConnectionId().equals(connectionId)
                ).collect(Collectors.toList())
            )
        );
    }

    public List<SonarLintRemoteConnectionConfiguration> getAllSonarLintConnectionConfigurations() {
        Preferences preferences = NbPreferences.forModule(SonarLintRemoteConnectionConfiguration.class);
        return gson.fromJson(
            preferences.get("connections", "[]"),
            new TypeToken<List<SonarLintRemoteConnectionConfiguration>>() {}.getType()
        );
    }

    public void saveSonarLintConnectionConfiguration(SonarLintRemoteConnectionConfiguration sonarLintConnectionConfiguration) {
        Preferences preferences = NbPreferences.forModule(SonarLintRemoteConnectionConfiguration.class);
        preferences.put(
            "connections",
            gson.toJson(
                Stream.concat(
                    getAllSonarLintConnectionConfigurations()
                        .stream()
                        .filter(c -> !c.getConnectionId().equals(sonarLintConnectionConfiguration.getConnectionId())),
                    Stream.of(sonarLintConnectionConfiguration)
                ).collect(Collectors.toList())
            )
        );
    }
}
