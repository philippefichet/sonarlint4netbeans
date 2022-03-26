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
package com.github.philippefichet.sonarlint4netbeans;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.sonarsource.sonarlint.core.client.api.common.Language;
import org.sonarsource.sonarlint.core.client.api.common.analysis.AnalysisResults;
import org.sonarsource.sonarlint.core.client.api.common.analysis.ClientInputFile;

/**
 * implementation of AnalysisResults to merge multiple analyze from different configuration
 * @author FICHET Philippe &lt;philippe.fichet@laposte.net&gt;
 */
public final class AnalysisResultsMergerable implements AnalysisResults {
    private int indexedFileCount = 0;
    private final List<ClientInputFile> failedAnalysisFiles = new ArrayList<>();
    private final Map<ClientInputFile, Language> languagePerFile = new HashMap<>();
    
    public AnalysisResultsMergerable merge(AnalysisResults analysisResults)
    {
        indexedFileCount += analysisResults.indexedFileCount();
        failedAnalysisFiles.addAll(analysisResults.failedAnalysisFiles());
        languagePerFile.putAll(languagePerFile);
        return this;
    }

    @Override
    public int indexedFileCount() {
        return indexedFileCount;
    }

    @Override
    public Collection<ClientInputFile> failedAnalysisFiles() {
        return failedAnalysisFiles;
    }

    @Override
    public Map<ClientInputFile, Language> languagePerFile() {
        return languagePerFile;
    }
    
}
