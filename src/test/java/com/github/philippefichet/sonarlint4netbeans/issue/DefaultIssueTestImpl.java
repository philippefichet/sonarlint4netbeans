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
package com.github.philippefichet.sonarlint4netbeans.issue;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.assertj.core.groups.Tuple;
import org.sonarsource.sonarlint.core.analysis.api.ClientInputFile;
import org.sonarsource.sonarlint.core.analysis.api.Flow;
import org.sonarsource.sonarlint.core.analysis.api.QuickFix;
import org.sonarsource.sonarlint.core.client.api.common.analysis.Issue;
import org.sonarsource.sonarlint.core.commons.CleanCodeAttribute;
import org.sonarsource.sonarlint.core.commons.ImpactSeverity;
import org.sonarsource.sonarlint.core.commons.IssueSeverity;
import org.sonarsource.sonarlint.core.commons.RuleType;
import org.sonarsource.sonarlint.core.commons.SoftwareQuality;
import org.sonarsource.sonarlint.core.commons.TextRange;
import org.sonarsource.sonarlint.core.commons.VulnerabilityProbability;

/**
 *
 * @author FICHET Philippe &lt;philippe.fichet@laposte.net&gt;
 */
public class DefaultIssueTestImpl implements Issue {

    private final IssueSeverity severity;
    private final RuleType type;
    private final String ruleKey;
    private final Integer startLine;
    private final Integer endLine;
    private final Integer startLineOffset;
    private final Integer endLineOffset;
    private final ClientInputFile clientInputFile;

    public DefaultIssueTestImpl(IssueSeverity severity, RuleType type, String ruleKey, Integer startLine, Integer endLine, Integer startLineOffset, Integer endLineOffset, ClientInputFile clientInputFile) {
        this.severity = severity;
        this.type = type;
        this.ruleKey = ruleKey;
        this.startLine = startLine;
        this.endLine = endLine;
        this.startLineOffset = startLineOffset;
        this.endLineOffset = endLineOffset;
        this.clientInputFile = clientInputFile;
    }

    @Override
    public IssueSeverity getSeverity() {
        return severity;
    }

    @Override
    public RuleType getType() {
        return type;
    }

    @Override
    public String getRuleKey() {
        return ruleKey;
    }

    @Override
    public List<Flow> flows() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ClientInputFile getInputFile() {
        return clientInputFile;
    }

    @Override
    public Integer getStartLine() {
        return startLine;
    }

    @Override
    public Integer getEndLine() {
        return endLine;
    }

    @Override
    public Integer getStartLineOffset() {
        return startLineOffset;
    }

    @Override
    public Integer getEndLineOffset() {
        return endLineOffset;
    }

    @Override
    public String getMessage() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public Tuple toTuple()
    {
        return toTuple(this);
    }

    public static Tuple toTuple(Issue issue)
    {
        return Tuple.tuple(
            issue.getSeverity(),
            issue.getType(),
            issue.getRuleKey(),
            issue.getStartLine(),
            issue.getEndLine(),
            issue.getStartLineOffset(),
            issue.getEndLineOffset()
        );
    };

    @Override
    public TextRange getTextRange() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String toString() {
        return "DefaultIssueTestImpl{" + "severity=" + severity + ", type=" + type + ", ruleKey=" + ruleKey + ", startLine=" + startLine + ", endLine=" + endLine + ", startLineOffset=" + startLineOffset + ", endLineOffset=" + endLineOffset + ", clientInputFile=" + clientInputFile + '}';
    }

    @Override
    public List<QuickFix> quickFixes() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Optional<CleanCodeAttribute> getCleanCodeAttribute() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Map<SoftwareQuality, ImpactSeverity> getImpacts() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Optional<String> getRuleDescriptionContextKey() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Optional<VulnerabilityProbability> getVulnerabilityProbability() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    public static class Builder
    {
        private IssueSeverity severity;
        private RuleType type;
        private String ruleKey;
        private Integer startLine;
        private Integer endLine;
        private Integer startLineOffset;
        private Integer endLineOffset;
        private ClientInputFile clientInputFile;

        public Builder severity(IssueSeverity severity)
        {
            this.severity = severity;
            return this;
        }

        public Builder type(RuleType type)
        {
            this.type = type;
            return this;
        }
        
        public Builder ruleKey(String ruleKey)
        {
            this.ruleKey = ruleKey;
            return this;
        }

        public Builder startLine(Integer startLine)
        {
            this.startLine = startLine;
            return this;
        }
        
        public Builder endLine(Integer endLine)
        {
            this.endLine = endLine;
            return this;
        }
        
        public Builder startLineOffset(Integer startLineOffset)
        {
            this.startLineOffset = startLineOffset;
            return this;
        }
        
        public Builder endLineOffset(Integer endLineOffset)
        {
            this.endLineOffset = endLineOffset;
            return this;
        }
        
        public Builder clientInputFile(ClientInputFile clientInputFile)
        {
            this.clientInputFile = clientInputFile;
            return this;
        }

        public Issue build() {
            return new DefaultIssueTestImpl(
                severity,
                type,
                ruleKey,
                startLine,
                endLine,
                startLineOffset,
                endLineOffset,
                clientInputFile
            );
        }
        public Tuple buildTuple() {
            return toTuple(new DefaultIssueTestImpl(
                severity,
                type,
                ruleKey,
                startLine,
                endLine,
                startLineOffset,
                endLineOffset,
                null
            ));
        }
    }
}
