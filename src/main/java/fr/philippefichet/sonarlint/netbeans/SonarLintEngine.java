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
package fr.philippefichet.sonarlint.netbeans;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.prefs.Preferences;
import org.sonarsource.sonarlint.core.client.api.common.LogOutput;
import org.sonarsource.sonarlint.core.client.api.common.ProgressMonitor;
import org.sonarsource.sonarlint.core.client.api.common.RuleDetails;
import org.sonarsource.sonarlint.core.client.api.common.RuleKey;
import org.sonarsource.sonarlint.core.client.api.common.analysis.AnalysisResults;
import org.sonarsource.sonarlint.core.client.api.common.analysis.IssueListener;
import org.sonarsource.sonarlint.core.client.api.connected.LoadedAnalyzer;
import org.sonarsource.sonarlint.core.client.api.standalone.StandaloneAnalysisConfiguration;

/**
 *
 * @author FICHET Philippe
 */
public interface SonarLintEngine {

    /**
     *
     * @param configuration
     * @param issueListener
     * @param logOutput
     * @param monitor
     * @return
     */
    public AnalysisResults analyze(StandaloneAnalysisConfiguration configuration, IssueListener issueListener, LogOutput logOutput, ProgressMonitor monitor);

    /**
     * Block current Thread while engine is not ready
     */
    public void waitingInitialization();

    /**
     * Return rule details of all available rules.
     *
     * @return all available rules.
     */
    public Collection<RuleDetails> getAllRuleDetails();

    /**
     * Get information about the analyzers that are currently loaded.
     *
     * @return the analyzers that are currently loaded.
     */
    public Collection<LoadedAnalyzer> getLoadedAnalyzers();

    /**
     * Return rule details.
     *
     * @param ruleKey
     * @return rule details.
     */
    public Optional<RuleDetails> getRuleDetails(String ruleKey);

    /**
     * Retrieve preferences
     *
     * @return preferences
     */
    public Preferences getPreferences();

    /**
     * Retrieve excluded rules
     *
     * @return excluded rules
     */
    public Collection<RuleKey> getExcludedRules();

    /**
     * Exclude one rule
     *
     * @param ruleKey rule to exclude
     */
    public void excludeRuleKeys(List<RuleKey> ruleKey);

    /**
     * incldue one rule
     *
     * @param ruleKey rule to include
     */
    public void includeRuleKyes(List<RuleKey> ruleKey);

    /**
     * Check if rule must be exclude
     *
     * @param ruleDetails rule details
     * @return true if rule must be exclude, false otherwise
     */
    public boolean isExcluded(RuleDetails ruleDetails);

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
}
