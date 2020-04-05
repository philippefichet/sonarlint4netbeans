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
package fr.philippefichet.sonarlint.netbeans;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Path;
import org.sonarsource.sonarlint.core.client.api.common.analysis.ClientInputFile;

/**
 *
 * @author FICHET Philippe
 */
public class FSClientInputFile implements ClientInputFile {

    private final Path path;
    private final String relativePath;
    private final boolean isTest;
    private final Charset encoding;
    private final String content;

    public FSClientInputFile(String content, Path path, String relativePath, boolean isTest, Charset encoding) {
        this.content = content;
        this.path = path;
        this.relativePath = relativePath;
        this.isTest = isTest;
        this.encoding = encoding;
    }

    @Override
    public String getPath() {
        return path.toString();
    }

    @Override
    public boolean isTest() {
        return isTest;
    }

    @Override
    public Charset getCharset() {
        return encoding;
    }

    @Override
    public <G> G getClientObject() {
        return null;
    }

    @Override
    public InputStream inputStream() throws IOException {
        return new ByteArrayInputStream(content.getBytes());
    }

    @Override
    public String contents() throws IOException {
        return content;
    }

    @Override
    public String relativePath() {
        return relativePath;
    }

    @Override
    public URI uri() {
        return path.toUri();
    }
}
