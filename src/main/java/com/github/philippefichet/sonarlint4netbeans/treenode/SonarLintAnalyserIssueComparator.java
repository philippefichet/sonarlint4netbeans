/*
 * sonarlint4netbeans: SonarLint integration for Apache Netbeans
 * Copyright (C) 2022 Philippe FICHET.
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
package com.github.philippefichet.sonarlint4netbeans.treenode;

import java.util.Comparator;
import org.sonarsource.sonarlint.core.client.api.common.analysis.Issue;

/**
 *
 * @author FICHET Philippe &lt;philippe.fichet@laposte.net&gt;
 */
public class SonarLintAnalyserIssueComparator implements Comparator<Issue> {

    @Override
    public int compare(Issue o1, Issue o2) {
        String[] splitO1 = o1.getInputFile().uri().getPath().split("/");
        String[] splitO2 = o2.getInputFile().uri().getPath().split("/");
        if (splitO1.length == 0) {
            return 1;
        } else if (splitO2.length == 0) {
            return -1;
        } else {
            return compare(o1, splitO1, o2, splitO2);
        }
    }

    private int compare(Issue o1, String[] splitO1, Issue o2, String[] splitO2) {
        int compare = splitO1[splitO1.length-1].compareTo(splitO2[splitO2.length-1]);
        if (compare == 0) {
            int compareLine = (o1.getStartLine() == null ? 0 : o1.getStartLine()) - (o2.getStartLine() == null ? 0 : o2.getStartLine());
            if (compareLine == 0) {
                return (o1.getStartLineOffset() == null ? 0 : o1.getStartLineOffset()) - (o2.getStartLineOffset() == null ? 0 : o2.getStartLineOffset());
            } else {
                return compareLine;
            }
        } else {
            return compare;
        }
    }
}
