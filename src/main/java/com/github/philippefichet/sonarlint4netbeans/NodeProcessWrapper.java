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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.sonarsource.nodejs.ProcessWrapper;
import org.sonarsource.nodejs.ProcessWrapperImpl;

/**
 * Used to intercept nodejs path in NodeCommandBuilderImpl
 * @author FICHET Philippe &lt;philippe.fichet@laposte.net&gt;
 */
public class NodeProcessWrapper implements ProcessWrapper {

    private final ProcessWrapperImpl processWrapper = new ProcessWrapperImpl();
    private List<String> commandLineUsed = null;

    @Override
    public Process startProcess(List<String> commandLine, Map<String, String> env, Consumer<String> outputConsumer, Consumer<String> errorConsumer) throws IOException {
        commandLineUsed = new ArrayList<>(commandLine);
        return processWrapper.startProcess(commandLine, env, outputConsumer, errorConsumer);
    }

    @Override
    public boolean waitFor(Process process, long timeout, TimeUnit unit) throws InterruptedException {
        return processWrapper.waitFor(process, timeout, unit);
    }

    @Override
    public void interrupt() {
        processWrapper.interrupt();
    }

    @Override
    public void destroyForcibly(Process process) {
        processWrapper.destroyForcibly(process);
    }

    @Override
    public boolean isMac() {
        return processWrapper.isMac();
    }

    @Override
    public boolean isWindows() {
        return processWrapper.isWindows();
    }

    @Override
    public String getenv(String name) {
        return processWrapper.getenv(name);
    }

    @Override
    public int exitValue(Process process) {
        return processWrapper.exitValue(process);
    }

    public Optional<List<String>> getCommandLineUsed() {
        return Optional.ofNullable(commandLineUsed);
    }
}
