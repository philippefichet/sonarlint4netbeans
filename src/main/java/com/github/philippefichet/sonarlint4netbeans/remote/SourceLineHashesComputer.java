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
package com.github.philippefichet.sonarlint4netbeans.remote;

import static java.nio.charset.StandardCharsets.UTF_8;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.sonarsource.sonarlint.shaded.org.apache.commons.codec.binary.Hex;
import org.sonarsource.sonarlint.shaded.org.apache.commons.codec.digest.DigestUtils;
import org.sonarsource.sonarlint.shaded.org.apache.commons.lang.StringUtils;

/**
 * Copied from
 * https://github.com/SonarSource/sonarqube/blob/master/sonar-core/src/main/java/org/sonar/core/hash/SourceLineHashesComputer.java
 *
 * @author FICHET Philippe &lt;philippe.fichet@laposte.net&gt;
 */
public class SourceLineHashesComputer {

    private final MessageDigest md5Digest = DigestUtils.getMd5Digest();
    private final List<String> lineHashes;

    public SourceLineHashesComputer() {
        this.lineHashes = new ArrayList<>();
    }

    public SourceLineHashesComputer(int expectedLineCount) {
        this.lineHashes = new ArrayList<>(expectedLineCount);
    }

    public void addLine(String line) {
        Objects.requireNonNull(line, "line can not be null");
        lineHashes.add(computeHash(line));
    }

    public List<String> getLineHashes() {
        return Collections.unmodifiableList(lineHashes);
    }

    private String computeHash(String line) {
        String reducedLine = StringUtils.replaceChars(line, "\t ", "");
        if (reducedLine.isEmpty()) {
            return "";
        }
        return Hex.encodeHexString(md5Digest.digest(reducedLine.getBytes(UTF_8)));
    }
}