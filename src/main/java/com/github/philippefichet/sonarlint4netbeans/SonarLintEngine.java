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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.prefs.Preferences;
import org.netbeans.api.project.Project;
import org.sonarsource.sonarlint.core.client.api.common.LogOutput;
import org.sonarsource.sonarlint.core.client.api.common.PluginDetails;
import org.sonarsource.sonarlint.core.client.api.common.ProgressMonitor;
import org.sonarsource.sonarlint.core.client.api.common.RuleDetails;
import org.sonarsource.sonarlint.core.client.api.common.RuleKey;
import org.sonarsource.sonarlint.core.client.api.common.Version;
import org.sonarsource.sonarlint.core.client.api.common.analysis.AnalysisResults;
import org.sonarsource.sonarlint.core.client.api.common.analysis.IssueListener;
import org.sonarsource.sonarlint.core.client.api.standalone.StandaloneAnalysisConfiguration;
import org.sonarsource.sonarlint.core.client.api.standalone.StandaloneRuleDetails;

/**
 *
 * @author FICHET Philippe
 */
public interface SonarLintEngine {
    public static final Project GLOBAL_SETTINGS_PROJECT = null;
    /**
     *
     * @param configuration analysis configuration
     * @param issueListener listener of issues
     * @param logOutput batch logs to a custom output
     * @param monitor monitor
     * @return result of analyze
     */
    public AnalysisResults analyze(StandaloneAnalysisConfiguration configuration, IssueListener issueListener, LogOutput logOutput, ProgressMonitor monitor);

    /**
     * Block current Thread while engine is not ready
     */
    public void waitingInitialization();

    /**
     * Retrieve NodeJS path if customized
     * @return NodeJS path
     */
    public Optional<String> getNodeJSPath();

    /**
     * Retrieve NodeJS path if customized
     * @return NodeJS path
     */
    public Optional<Version> getNodeJSVersion();

    /**
     * Custom NodeJS Path
     * @param nodeJSPath new NodeJS path
     * @param nodeJSversion Version of NodeJS used
     */
    public void setNodeJSPathAndVersion(String nodeJSPath, Version nodeJSversion);

    /**
     * Return rule details of all available rules.
     *
     * @return all available rules.
     */
    public Collection<StandaloneRuleDetails> getAllRuleDetails();

    /**
     * Get information about the analyzers that are currently loaded.
     *
     * @return the analyzers that are currently loaded.
     */
    public Collection<PluginDetails> getPluginDetails();

    /**
     * Return rule details.
     *
     * @param ruleKey rule key (ex: java:S108)
     * @return rule details.
     */
    public Optional<StandaloneRuleDetails> getRuleDetails(String ruleKey);

    /**
     * Retrieve preferences
     *
     * @return preferences
     */
    public Preferences getPreferences(Project project);

    /**
     * Retrieve excluded rules
     *
     * @return excluded rules
     */
    public Collection<RuleKey> getExcludedRules(Project project);

    /**
     * Exclude one rule
     *
     * @param ruleKey rule to exclude
     */
    public void excludeRuleKeys(List<RuleKey> ruleKey, Project project);

    /**
     * set rules to include
     *
     * @param ruleKeys rules to include
     */
    public void includeRuleKeys(List<RuleKey> ruleKeys, Project project);

    /**
     * include one rule
     *
     * @param ruleKey rule to include
     */
    public void includeRuleKey(RuleKey ruleKey, Project project);

    /**
     * exclude one rule
     *
     * @param ruleKey rule to exclude
     */
    public void excludeRuleKey(RuleKey ruleKey, Project project);

    /**
     * Check if rule must be exclude
     *
     * @param ruleDetails rule details
     * @return true if rule must be exclude, false otherwise
     */
    public boolean isExcluded(RuleDetails ruleDetails, Project project);

    /**
     * Call consumer when engine is initialized. If already initialized,
     * consumer is call immediatly
     *
     * @param consumer consumer to call when engine is initialized.
     */
    public void whenInitialized(Consumer<SonarLintEngine> consumer);

    /**
     * Call consumer when configuration on engine is changed.
     *
     * @param consumer consumer to call when configuration engine is changed.
     */
    public void whenConfigurationChanged(Consumer<SonarLintEngine> consumer);

    /**
     * Add parameter value on rule by parameter name
     * @param ruleKey rule key (ex: java:S108)
     * @param parameterName paramater name of rule
     * @param parameterValue paramater value
     */
    public void setRuleParameter(String ruleKey, String parameterName, String parameterValue, Project project);

    /**
     * Retrieve all rules with customized parameter value
     * @return All rules with customized parameter value
     */
    public Map<RuleKey, Map<String, String>> getRuleParameters(Project project);
    
    /**
     * Remove parameter value on rule by parameter name
     * @param ruleKey rule key (ex: java:S108)
     * @param parameterName paramater name of rule
     */
    public void removeRuleParameter(String ruleKey, String parameterName, Project project);

    /**
     * Retrieve parameter value on rule only if changed
     * @param ruleKey rule key (ex: java:S108)
     * @return value of rule parameter or empty if not value changed
     */
    public Optional<String> getRuleParameter(String ruleKey, String parameterName, Project project);

    /**
     * Retrieve all extra properties for a project
     * @param project Project to retrieve all extra properties
     * @return all extra properties for a project
     */
    public Map<String, String> getAllExtraProperties(Project project);

    /**
     * Change all extra properties for a project
     * @param extraProperties all extra properties to set for a project
     * @param project Project to change all extra properties
     */
    public void setAllExtraProperties(Map<String, String> extraProperties, Project project);
}
