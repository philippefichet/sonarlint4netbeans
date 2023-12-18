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
package com.github.philippefichet.sonarlint4netbeans.annotation;

import java.util.Map;
import org.apache.commons.text.StringEscapeUtils;
import org.openide.text.Annotation;
import org.sonarsource.sonarlint.core.client.api.standalone.StandaloneRuleParam;
import org.sonarsource.sonarlint.core.commons.IssueSeverity;
/**
 *
 * @author FICHET Philippe
 */
public class SonarLintAnnotation extends Annotation {

    public static final String ANNOTATION_TYPE_GENERIC = "com-github-philippefichet-sonarlint4netbeans-annotation-generic";
    public static final String ANNOTATION_TYPE_INFO = "com-github-philippefichet-sonarlint4netbeans-annotation-info";
    public static final String ANNOTATION_TYPE_MINOR = "com-github-philippefichet-sonarlint4netbeans-annotation-minor";
    public static final String ANNOTATION_TYPE_MAJOR = "com-github-philippefichet-sonarlint4netbeans-annotation-major";
    public static final String ANNOTATION_TYPE_CRITIAL = "com-github-philippefichet-sonarlint4netbeans-annotation-critical";
    public static final String ANNOTATION_TYPE_BLOCKER = "com-github-philippefichet-sonarlint4netbeans-annotation-blocker";
    private final long startOffest;
    private final int length;
    private final String shortDescription;
    private final String ruleKey;
    private final String ruleName;
    private final IssueSeverity severity;

    public SonarLintAnnotation(String ruleKey, String ruleName, Map<StandaloneRuleParam, String> ruleParams, IssueSeverity severity, long startOffest, int length) {
        super();
        this.startOffest = startOffest;
        this.length = length;
        this.ruleKey = ruleKey;
        this.ruleName = ruleName;
        this.severity = severity;
        StringBuilder sb = new StringBuilder("<html>");
        sb.append("<strong>");
        sb.append(ruleKey);
        sb.append(": ")
            .append(StringEscapeUtils.escapeHtml4(ruleName))
            .append("</strong>")
            .append("<br/>Click to show details");
        if (!ruleParams.isEmpty()) {
            sb.append("<br/><br/><strong>Parameters:</strong><br/>");
            for (Map.Entry<StandaloneRuleParam, String> ruleParam : ruleParams.entrySet()) {
                StandaloneRuleParam standaloneRuleParam = ruleParam.getKey();
                sb.append("<div>")
                    .append(standaloneRuleParam.name())
                    .append(": ")
                    .append(standaloneRuleParam.description())
                    .append(". (value: ")
                    .append(ruleParam.getValue())
                    .append(", default: ")
                    .append(standaloneRuleParam.defaultValue())
                    .append(")</div>");
            }
        }
        this.shortDescription = sb.append("</html>")
            .toString();
    }

    public long getStartOffest() {
        return startOffest;
    }

    public int getLength() {
        return length;
    }

    @Override
    public String getAnnotationType() {
        switch(severity.name()) {
            case "INFO":
                return ANNOTATION_TYPE_INFO;
            case "MINOR":
                return ANNOTATION_TYPE_MINOR;
            case "MAJOR":
                return ANNOTATION_TYPE_MAJOR;
            case "CRITICAL":
                return ANNOTATION_TYPE_CRITIAL;
            case "BLOCKER":
                return ANNOTATION_TYPE_BLOCKER;
            default:
                return ANNOTATION_TYPE_GENERIC;
        }
    }

    @Override
    public String getShortDescription() {
        return shortDescription;
    }

    public String getRuleKey() {
        return ruleKey;
    }

    public String getRuleName() {
        return ruleName;
    }
    
    public IssueSeverity getSeverity() {
        return severity;
    }
}
