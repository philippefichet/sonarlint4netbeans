/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
