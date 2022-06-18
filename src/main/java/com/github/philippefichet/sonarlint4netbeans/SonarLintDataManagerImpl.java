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

import com.github.philippefichet.sonarlint4netbeans.project.SonarLintProjectPreferenceScope;
import java.awt.Image;
import java.io.File;
import java.nio.charset.Charset;
import java.util.Optional;
import java.util.prefs.Preferences;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.queries.FileEncodingQuery;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;

/**
 *
 * @author FICHET Philippe &lt;philippe.fichet@laposte.net&gt;
 */
public class SonarLintDataManagerImpl implements SonarLintDataManager {
    private static final String PREFERENCE_SCOPE_KEY = "sonarlint-preference-scope";
    @Override
    public Preferences getPreferences(Project project) {
        if (project == null) {
            return getGlobalSettingsPreferences();
        }
        return ProjectUtils.getPreferences(project, SonarLintEngineImpl.class, true);
    }

    @Override
    public Optional<Project> getProject(FileObject fileObject) {
        if (fileObject == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(FileOwnerQuery.getOwner(fileObject));
    }

    @Override
    public Optional<Project> getProject(File file) {
        if (file == null) {
            return Optional.empty();
        }
        return getProject(FileUtil.toFileObject(file));
    }

    @Override
    public Preferences getGlobalSettingsPreferences() {
        return NbPreferences.forModule(SonarLintEngineImpl.class);
    }

    @Override
    public void setPreferencesScope(Project project, SonarLintProjectPreferenceScope projectPreferenceScope) {
        Preferences preferences = getPreferences(project);
        preferences.put(PREFERENCE_SCOPE_KEY, projectPreferenceScope.name().toLowerCase());
    }

    @Override
    public SonarLintProjectPreferenceScope getPreferencesScope(Project project) {
        if (project == null) {
            return SonarLintProjectPreferenceScope.GLOBAL;
        }
        Preferences preferences = getPreferences(project);
        String scopeLowerCase = preferences.get(PREFERENCE_SCOPE_KEY, null);
        if (scopeLowerCase == null) {
            return SonarLintProjectPreferenceScope.GLOBAL;
        } else {
            try {
                return SonarLintProjectPreferenceScope.valueOf(scopeLowerCase.toUpperCase());
            } catch(IllegalArgumentException ex) {
                return SonarLintProjectPreferenceScope.GLOBAL;
            }
        }
    }

    @Override
    public Optional<Charset> getEncoding(File file) {
        FileObject fileObject = FileUtil.toFileObject(file);
        // ignore null FileObject (e.g. .nbattr) as getEncoding throws IllegalArgumentException
        if (fileObject == null)
        {
            return Optional.empty();
        }
        return Optional.ofNullable(FileEncodingQuery.getEncoding(fileObject));
    }

    @Override
    public boolean isTest(File file) {
        Optional<Project> project = getProject(file);
        if (project.isPresent()) {
            File projectFile = FileUtil.toFile(project.get().getProjectDirectory());
            if (file.getAbsolutePath().startsWith(projectFile.getAbsolutePath())) {
                String relativeProjectPath = file.getAbsolutePath().replace(projectFile.getAbsolutePath(), "");
                if (relativeProjectPath.contains(File.separator + "test" + File.separator)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public Optional<Image> getIcon(File file, int type) {
        FileObject fileObject = FileUtil.toFileObject(file);
        if (fileObject != null) {
            try {
                DataObject dataObject = DataObject.find(fileObject);
                if (dataObject != null) {
                    return Optional.ofNullable(dataObject.getNodeDelegate().getIcon(type));
                }
            } catch (DataObjectNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return Optional.empty();
    }

    @Override
    public File getInstalledFile(String path) {
        return InstalledFileLocator.getDefault().locate(path, "com.github.philippefichet.sonarlint4netbeans", false);
    }
}
