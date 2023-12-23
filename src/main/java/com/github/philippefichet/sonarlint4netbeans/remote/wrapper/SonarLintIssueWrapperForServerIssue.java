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
package com.github.philippefichet.sonarlint4netbeans.remote.wrapper;

import com.github.philippefichet.sonarlint4netbeans.FSClientInputFile;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.sonarsource.sonarlint.core.analysis.api.ClientInputFile;
import org.sonarsource.sonarlint.core.analysis.api.Flow;
import org.sonarsource.sonarlint.core.analysis.api.QuickFix;
import org.sonarsource.sonarlint.core.client.api.common.analysis.Issue;
import org.sonarsource.sonarlint.core.client.api.standalone.StandaloneRuleDetails;
import org.sonarsource.sonarlint.core.commons.CleanCodeAttribute;
import org.sonarsource.sonarlint.core.commons.ImpactSeverity;
import org.sonarsource.sonarlint.core.commons.IssueSeverity;
import org.sonarsource.sonarlint.core.commons.RuleType;
import org.sonarsource.sonarlint.core.commons.SoftwareQuality;
import org.sonarsource.sonarlint.core.commons.TextRange;
import org.sonarsource.sonarlint.core.commons.TextRangeWithHash;
import org.sonarsource.sonarlint.core.commons.VulnerabilityProbability;
import org.sonarsource.sonarlint.core.serverconnection.issues.FileLevelServerIssue;
import org.sonarsource.sonarlint.core.serverconnection.issues.LineLevelServerIssue;
import org.sonarsource.sonarlint.core.serverconnection.issues.RangeLevelServerIssue;
import org.sonarsource.sonarlint.core.serverconnection.issues.ServerIssue;

/**
 *
 * @author FICHET Philippe &lt;philippe.fichet@laposte.net&gt;
 */
public class SonarLintIssueWrapperForServerIssue implements Issue {

    private final ServerIssue serverIssue;
    private final FSClientInputFile clientInputFile;
    private final Optional<StandaloneRuleDetails> ruleDetails;
    private final Integer startLine;
    private final Integer endLine;

    public SonarLintIssueWrapperForServerIssue(FSClientInputFile clientInputFile, ServerIssue serverIssue, Optional<StandaloneRuleDetails> ruleDetails) {
        this.clientInputFile = clientInputFile;
        this.serverIssue = serverIssue;
        this.ruleDetails = ruleDetails;

        if (serverIssue instanceof FileLevelServerIssue) {
            FileLevelServerIssue flsi = (FileLevelServerIssue)serverIssue;
            startLine = null;
            endLine = null;
        } else if (serverIssue instanceof LineLevelServerIssue) {
            LineLevelServerIssue llsi = (LineLevelServerIssue)serverIssue;
            List<String> lineHashes = clientInputFile.getLineHashes();
            // Naive implementation because of possible collision
            // TODO improve search if possible (nearest line?)
            int indexOf = lineHashes.indexOf(llsi.getLineHash()) + 1;
            startLine = indexOf;
            endLine = indexOf;
        } else if (serverIssue instanceof RangeLevelServerIssue) {
            RangeLevelServerIssue rlsi = (RangeLevelServerIssue)serverIssue;
            TextRangeWithHash textRange = rlsi.getTextRange();
            startLine = textRange.getStartLine();
            endLine = textRange.getStartLine();
        } else {
            startLine = null;
            endLine = null;
        }
    }

    @Override
    public IssueSeverity getSeverity() {
        // TODO to fix NPE. Find real user severity later
        if (serverIssue.getUserSeverity() == null) {
            if (ruleDetails.isPresent()) {
                return ruleDetails.get().getDefaultSeverity();
            } else {
                return IssueSeverity.BLOCKER;
            }
        }
        return serverIssue.getUserSeverity();
    }

    @Override
    public RuleType getType() {
        return serverIssue.getType();
    }

    @Override
    public String getRuleKey() {
        return serverIssue.getRuleKey();
    }

    @Override
    public List<Flow> flows() {
        return Collections.emptyList();
    }

    @Override
    public List<QuickFix> quickFixes() {
        return Collections.emptyList();
    }

    @Override
    public Optional<String> getRuleDescriptionContextKey() {
        return Optional.empty();
    }

    @Override
    public Optional<VulnerabilityProbability> getVulnerabilityProbability() {
        return Optional.empty();
    }

    @Override
    public String getMessage() {
        return serverIssue.getMessage();
    }

    @Override
    public ClientInputFile getInputFile() {
        return clientInputFile;
    }

    @Override
    public TextRange getTextRange() {
        if (serverIssue instanceof RangeLevelServerIssue) {
            return ((RangeLevelServerIssue)serverIssue).getTextRange();
        }
        return null;
    }

    @Override
    public Integer getStartLine() {
        return startLine;
    }

    @Override
    public Integer getStartLineOffset() {
        if (serverIssue instanceof RangeLevelServerIssue) {
            return ((RangeLevelServerIssue)serverIssue).getTextRange().getStartLineOffset();
        }
        return null;
    }

    @Override
    public Integer getEndLine() {
        return endLine;
    }

    @Override
    public Integer getEndLineOffset() {
        if (serverIssue instanceof RangeLevelServerIssue) {
            return ((RangeLevelServerIssue)serverIssue).getTextRange().getEndLineOffset();
        }
        return null;
    }

    @Override
    public Optional<CleanCodeAttribute> getCleanCodeAttribute() {
        // TODO
        return Optional.empty();
    }

    @Override
    public Map<SoftwareQuality, ImpactSeverity> getImpacts() {
        // TODO
        return Collections.emptyMap();
    }
}
