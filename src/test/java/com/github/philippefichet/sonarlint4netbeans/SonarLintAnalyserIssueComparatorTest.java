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

import java.io.File;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.sonarsource.sonarlint.core.client.api.common.analysis.Issue;

/**
 *
 * @author FICHET Philippe &lt;philippe.fichet@laposte.net&gt;
 */
public class SonarLintAnalyserIssueComparatorTest {

    public enum CompareOrder {
        FIRST,
        SECOND,
        TIED;
    }
    
    public static Arguments[] parametersForCompare()
    {
        File firstFile = new File("./src/test/resources/NewClass.java");
        return new Arguments[] {
            Arguments.of(
                new DefaultIssueTestImpl.Builder()
                    .clientInputFile(new FSClientInputFile(null, firstFile.toPath(), null, true, null))
                    .startLine(19)
                    .startLineOffset(15)
                    .build(),
                new DefaultIssueTestImpl.Builder()
                    .clientInputFile(new FSClientInputFile(null, firstFile.toPath(), null, true, null))
                    .startLine(20)
                    .startLineOffset(30)
                    .build(),
                    CompareOrder.FIRST
            ),
            Arguments.of(
                new DefaultIssueTestImpl.Builder()
                    .clientInputFile(new FSClientInputFile(null, firstFile.toPath(), null, true, null))
                    .startLine(20)
                    .startLineOffset(30)
                    .build(),
                new DefaultIssueTestImpl.Builder()
                    .clientInputFile(new FSClientInputFile(null, firstFile.toPath(), null, true, null))
                    .startLine(19)
                    .startLineOffset(15)
                    .build(),
                    CompareOrder.SECOND
            ),
        };
    }
    
    @ParameterizedTest
    @MethodSource("parametersForCompare")
    public void compare(Issue o1, Issue o2, CompareOrder compareOrder)
    {
        SonarLintAnalyserIssueComparator sonarLintAnalyserIssueComparator = new SonarLintAnalyserIssueComparator();
        int compare = sonarLintAnalyserIssueComparator.compare(o1, o2);
        switch (compareOrder) {
            case FIRST:
                Assertions.assertThat(compare)
                    .isLessThan(0);
                break;
            case SECOND:
                Assertions.assertThat(compare)
                    .isGreaterThan(0);
                break;
            case TIED:
                Assertions.assertThat(compare)
                    .isEqualTo(0);
                break;
            default:
                Assertions.fail("Comparator Order \"" + compareOrder + "\" is not supported");
        }
    }
}
