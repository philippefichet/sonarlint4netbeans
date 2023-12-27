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
package com.github.philippefichet.sonarlint4netbeans.git;

import java.io.IOException;
import java.util.Optional;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author FICHET Philippe &lt;philippe.fichet@laposte.net&gt;
 */
public final class GitUtils {

    private GitUtils() {
    }

    /**
     * Search git active branch for this file
     * @param file file to search git active branch
     * @return branch name if found
     */
    public static Optional<String> getProjectActiveBranch(FileObject file) {
        if (file == null) {
            return Optional.empty();
        }
        Optional<FileObject> dotGitFolder = searchDotGitFolder(file);
        if (dotGitFolder.isPresent()) {
            try {
                return Optional.of(
                    dotGitFolder.get()
                        .getFileObject("HEAD")
                        .asText()
                        .split("\n")[0]
                        .substring("ref: refs/heads/".length())
                );
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return Optional.empty();
    }

    /**
     * Search .git folder from directory
     * @param file directory to search
     * @return ".git" FileObject if found
     */
    public static Optional<FileObject> searchDotGitFolder(FileObject file)
    {
        FileObject fileObject = file;
        while (fileObject.getFileObject(".git") == null && fileObject.getParent() != null) {
            fileObject = fileObject.getParent();
        }
        return Optional.ofNullable(fileObject.getFileObject(".git"));
    }

}
