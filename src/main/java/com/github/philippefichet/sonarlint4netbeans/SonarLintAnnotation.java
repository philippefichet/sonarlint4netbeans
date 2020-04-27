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

import org.openide.text.Annotation;
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
    private final String severity;

    public SonarLintAnnotation(String ruleKey, String ruleName, String severity, long startOffest, int length) {
        super();
        this.startOffest = startOffest;
        this.length = length;
        this.ruleKey = ruleKey;
        this.ruleName = ruleName;
        this.severity = severity;
        this.shortDescription = ruleKey + "\n" + ruleName + "\nClick to show details";
    }

    public long getStartOffest() {
        return startOffest;
    }

    public int getLength() {
        return length;
    }

    @Override
    public String getAnnotationType() {
        switch(severity) {
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
    
    public String getSeverity() {
        return severity;
    }
}
