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

import com.github.philippefichet.sonarlint4netbeans.project.SonarLintProjectPreferenceScope;
import java.awt.Image;
import java.io.File;
import java.nio.charset.Charset;
import java.util.Optional;
import java.util.prefs.Preferences;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;


/**
 * Interface to handle data related with Netbeans API/SPI
 * @author FICHET Philippe &lt;philippe.fichet@laposte.net&gt;
 */
public interface SonarLintDataManager {
    /**
     * Retrieve project preferences data
     * @param project project to retrieve preference
     * @return project preferences data or global settings preference if project if null
     */
    public Preferences getPreferences(Project project);

    /**
     * Change scope preference of project
     * @param project project to change scope
     * @param projectPreferenceScope scope to set project
     */
    public void setPreferencesScope(Project project, SonarLintProjectPreferenceScope projectPreferenceScope);

    /**
     * Retrieve scope preference of project
     * @param project project to retrieve scope
     * @return non-null scope of project
     */
    public SonarLintProjectPreferenceScope getPreferencesScope(Project project);
    
    /**
     * Retrieve global setting preferences data
     * @return global settings preference if project if null
     */
    public Preferences getGlobalSettingsPreferences();

    /**
     * Retrieve project from fileObject
     * @param fileObject fileObject to search project
     * @return project from fileObject
     */
    public Optional<Project> getProject(FileObject fileObject);

    /**
     * Retrieve project from file
     * @param file file to search project
     * @return project from file
     */
    public Optional<Project> getProject(File file);

    /**
     * Retrieve encoding of file if can be detected
     * @param file file to detect encoding
     * @return encoding of file if can be detected
     */
    public Optional<Charset> getEncoding(File file);

    /**
     * Check if file is in test directory from project
     *
     * @param file file to check if is test
     * @return true if file is in test directory from project
     */
    public boolean isTest(File file);
    
    /**
     * Find an icon for this file.
     * @param file file to search icon
     * @param type type constants from {@link java.beans.BeanInfo}
     * @return icon for this file
     */
    public Optional<Image> getIcon(File file, int type);
}
