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
import java.util.Objects;
import org.sonarsource.sonarlint.core.client.api.common.analysis.Issue;

/**
 *
 * @author FICHET Philippe &lt;philippe.fichet@laposte.net&gt;
 */
public final class IssueUtils {

    private IssueUtils() {
    }

    public static boolean containsSimilarIssue(List<Issue> issues, Issue issue)
    {
        for (Issue sue : issues) {
            if(Objects.equals(sue.getRuleKey(), issue.getRuleKey())
               && Objects.equals(sue.getStartLine(), issue.getStartLine())
               && Objects.equals(sue.getEndLine(), issue.getEndLine())
               // if the issue is less accurate
               && (issue.getStartLineOffset() == null || Objects.equals(sue.getStartLineOffset(), issue.getStartLineOffset()))
               // if the issue is less accurate
               && (issue.getEndLineOffset() == null || Objects.equals(sue.getEndLineOffset(), issue.getEndLineOffset()))
            ) {
                return true;
            }
        }
        return false;
    }
}
