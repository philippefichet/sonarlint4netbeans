/*
 * sonarlint4netbeans: SonarLint integration for Apache Netbeans
 * Copyright (C) 2020 Philippe FICHET.
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
package com.github.philippefichet.sonarlint4netbeans;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.api.project.Project;
import org.sonarsource.sonarlint.core.client.api.common.RuleKey;
import org.sonarsource.sonarlint.core.client.api.common.analysis.ClientInputFile;

/**
 *
 * @author FICHET Philippe &lt;philippe.fichet@laposte.net&gt;
 */
public final class SonarLintEngineTestConfiguration {
    private final String description;
    private final List<RuleKey> excludedRules;
    private final List<RuleKey> includedRules;
    private final List<ClientInputFile> clientInputFiles;
    private final List<RuleParameter> ruleParameters;
    private final Map<Project, Map<String, String>> extraProperties;

    private final boolean requireNodeJS;

    private SonarLintEngineTestConfiguration(Builder builder) {
        this.description = builder.description;
        this.excludedRules = builder.excludedRules;
        this.includedRules = builder.includedRules;
        this.clientInputFiles = builder.clientInputFiles;
        this.ruleParameters = builder.ruleParameters;
        this.requireNodeJS = builder.requireNodeJS;
        this.extraProperties = builder.extraProperties;
    }

    public List<RuleKey> getExcludedRules() {
        return excludedRules;
    }

    public List<RuleKey> getIncludedRules() {
        return includedRules;
    }

    public List<ClientInputFile> getClientInputFiles() {
        return clientInputFiles;
    }

    public List<RuleParameter> getRuleParameters() {
        return ruleParameters;
    }

    public boolean isRequireNodeJS() {
        return requireNodeJS;
    }

    public Map<Project, Map<String, String>> getExtraProperties() {
        return extraProperties;
    }

    @Override
    public String toString() {
        return description;
    }

    public static Builder builder() {
        return new Builder();
    }
    
    public static final class RuleParameter {
        private final String ruleKey;
        private final String name;
        private final String value;

        public RuleParameter(String ruleKey, String name, String value) {
            this.ruleKey = ruleKey;
            this.name = name;
            this.value = value;
        }

        public String getRuleKey() {
            return ruleKey;
        }

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }
    }

    public static final class Builder {
        private String description;
        private final List<RuleKey> excludedRules = new ArrayList<>();
        private final List<RuleKey> includedRules = new ArrayList<>();
        private final List<ClientInputFile> clientInputFiles = new ArrayList<>();
        private final List<RuleParameter> ruleParameters = new ArrayList<>();
        private final Map<Project, Map<String, String>> extraProperties = new HashMap<>();
        private boolean requireNodeJS = false;

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder requireNodeJS() {
            this.requireNodeJS = true;
            return this;
        }

        public Builder addClientInputFile(File file) throws IOException {
            Path path = file.toPath();
            clientInputFiles.add(new FSClientInputFile(
                new String(Files.readAllBytes(path)),
                path.toAbsolutePath(),
                file.getName(),
                false,
                StandardCharsets.UTF_8
            ));
            return this;
        }

        public Builder addExtraProperty(String name, String value, Project project) {
            extraProperties.computeIfAbsent(project, p -> new HashMap<>()).put(name, value);
            return this;
        }

        public Builder addRuleParameter(String ruleKey, String name, String value)
        {
            ruleParameters.add(new RuleParameter(ruleKey, name, value));
            return this;
        }

        public Builder includeRules(String... ruleKeys) {
            for (String ruleKey : ruleKeys) {
                includedRules.add(RuleKey.parse(ruleKey));
            }
            return this;
        }

        public Builder excludeRules(String... ruleKeys) {
            for (String ruleKey : ruleKeys) {
                excludedRules.add(RuleKey.parse(ruleKey));
            }
            return this;
        }

        public SonarLintEngineTestConfiguration build() {
            return new SonarLintEngineTestConfiguration(this);
        }
    }
}
