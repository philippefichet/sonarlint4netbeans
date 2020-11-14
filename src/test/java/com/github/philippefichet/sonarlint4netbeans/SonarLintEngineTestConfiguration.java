/*
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
import java.util.List;
import org.sonarsource.sonarlint.core.client.api.common.RuleKey;
import org.sonarsource.sonarlint.core.client.api.common.analysis.ClientInputFile;

/**
 *
 * @author FICHET Philippe &lt;philippe.fichet@laposte.net&gt;
 */
public final class SonarLintEngineTestConfiguration {
    private final List<RuleKey> excludedRules;
    private final List<RuleKey> includedRules;
    private final List<ClientInputFile> clientInputFiles;

    private SonarLintEngineTestConfiguration(Builder builder) {
        this.excludedRules = builder.excludedRules;
        this.includedRules = builder.includedRules;
        this.clientInputFiles = builder.clientInputFiles;
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

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final List<RuleKey> excludedRules = new ArrayList<>();
        private final List<RuleKey> includedRules = new ArrayList<>();
        private final List<ClientInputFile> clientInputFiles = new ArrayList<>();

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
