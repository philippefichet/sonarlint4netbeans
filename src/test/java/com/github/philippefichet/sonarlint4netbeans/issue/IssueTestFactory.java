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

import org.sonarsource.sonarlint.core.commons.IssueSeverity;
import org.sonarsource.sonarlint.core.commons.RuleType;

/**
 *
 * @author FICHET Philippe &lt;philippe.fichet@laposte.net&gt;
 */
public final class IssueTestFactory {

    private IssueTestFactory() {
    }

    public static DefaultIssueTestImpl.Builder javaS100(Integer line, Integer startLineOffset, Integer endLineOffset) {
        return new DefaultIssueTestImpl.Builder()
            .severity(IssueSeverity.MINOR)
            .type(RuleType.CODE_SMELL)
            .ruleKey("java:S100")
            .startLine(line)
            .startLineOffset(startLineOffset)
            .endLine(line)
            .endLineOffset(endLineOffset);
    }

    public static DefaultIssueTestImpl.Builder javaS106(Integer line, Integer startLineOffset, Integer endLineOffset) {
        return new DefaultIssueTestImpl.Builder()
            .severity(IssueSeverity.MAJOR)
            .type(RuleType.CODE_SMELL)
            .ruleKey("java:S106")
            .startLine(line)
            .startLineOffset(startLineOffset)
            .endLine(line)
            .endLineOffset(endLineOffset);
    }

    public static DefaultIssueTestImpl.Builder javaS107(Integer line, Integer startLineOffset, Integer endLineOffset) {
        return new DefaultIssueTestImpl.Builder()
            .severity(IssueSeverity.MAJOR)
            .type(RuleType.CODE_SMELL)
            .ruleKey("java:S107")
            .startLine(line)
            .startLineOffset(startLineOffset)
            .endLine(line)
            .endLineOffset(endLineOffset);
    }

    public static DefaultIssueTestImpl.Builder javaS115(Integer line, Integer startLineOffset, Integer endLineOffset) {
        return new DefaultIssueTestImpl.Builder()
            .severity(IssueSeverity.CRITICAL)
            .type(RuleType.CODE_SMELL)
            .ruleKey("java:S115")
            .startLine(line)
            .startLineOffset(startLineOffset)
            .endLine(line)
            .endLineOffset(endLineOffset);
    }

    public static DefaultIssueTestImpl.Builder javaS1133(Integer line, Integer startLineOffset, Integer endLineOffset) {
        return new DefaultIssueTestImpl.Builder()
            .severity(IssueSeverity.INFO)
            .type(RuleType.CODE_SMELL)
            .ruleKey("java:S1133")
            .startLine(line)
            .startLineOffset(startLineOffset)
            .endLine(line)
            .endLineOffset(endLineOffset);
    }

    public static DefaultIssueTestImpl.Builder javaS1134(Integer line, Integer startLineOffset, Integer endLineOffset) {
        return new DefaultIssueTestImpl.Builder()
            .severity(IssueSeverity.MAJOR)
            .type(RuleType.CODE_SMELL)
            .ruleKey("java:S1134")
            .startLine(line)
            .startLineOffset(startLineOffset)
            .endLine(line)
            .endLineOffset(endLineOffset);
    }

    public static DefaultIssueTestImpl.Builder javaS1172(Integer line, Integer startLineOffset, Integer endLineOffset) {
        return new DefaultIssueTestImpl.Builder()
            .severity(IssueSeverity.MAJOR)
            .type(RuleType.CODE_SMELL)
            .ruleKey("java:S1172")
            .startLine(line)
            .startLineOffset(startLineOffset)
            .endLine(line)
            .endLineOffset(endLineOffset);
    }

    public static DefaultIssueTestImpl.Builder javaS1186(Integer line, Integer startLineOffset, Integer endLineOffset) {
        return new DefaultIssueTestImpl.Builder()
            .severity(IssueSeverity.CRITICAL)
            .type(RuleType.CODE_SMELL)
            .ruleKey("java:S1186")
            .startLine(line)
            .startLineOffset(startLineOffset)
            .endLine(line)
            .endLineOffset(endLineOffset);
    }

    public static DefaultIssueTestImpl.Builder javaS2168(Integer line, Integer startLineOffset, Integer endLineOffset) {
        return new DefaultIssueTestImpl.Builder()
            .severity(IssueSeverity.BLOCKER)
            .type(RuleType.BUG)
            .ruleKey("java:S2168")
            .startLine(line)
            .startLineOffset(startLineOffset)
            .endLine(line)
            .endLineOffset(endLineOffset);
    }

    public static DefaultIssueTestImpl.Builder javaS2629(Integer line) {
        return new DefaultIssueTestImpl.Builder()
            .severity(IssueSeverity.MAJOR)
            .type(RuleType.CODE_SMELL)
            .ruleKey("java:S2629")
            .startLine(line)
            .endLine(line);
    }
    public static DefaultIssueTestImpl.Builder javaS3457(Integer line) {
        return new DefaultIssueTestImpl.Builder()
            .severity(IssueSeverity.MAJOR)
            .type(RuleType.CODE_SMELL)
            .ruleKey("java:S3457")
            .startLine(line)
            .endLine(line);
    }

}
