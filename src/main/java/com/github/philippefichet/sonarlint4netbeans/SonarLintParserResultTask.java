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
package com.github.philippefichet.sonarlint4netbeans;

import com.github.philippefichet.sonarlint4netbeans.annotation.SonarLintAnnotationHandler;
import java.io.IOException;
import java.util.logging.Logger;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.ParserResultTask;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;

/**
 *
 * @author FICHET Philippe
 */
public class SonarLintParserResultTask extends ParserResultTask<Parser.Result> {

    private static final Logger LOG = Logger.getLogger(SonarLintParserResultTask.class.getCanonicalName());
    private final SonarLintEngine standaloneSonarLintEngineImpl;
    public SonarLintParserResultTask(SonarLintEngine standaloneSonarLintEngineImpl) {
        this.standaloneSonarLintEngineImpl = standaloneSonarLintEngineImpl;
    }

    @Override
    public void run(Parser.Result result, SchedulerEvent event) {
        try {
            SonarLintAnnotationHandler.analyze(
                standaloneSonarLintEngineImpl,
                result.getSnapshot().getSource().getFileObject(),
                result.getSnapshot().getText().toString()
            );
        } catch (IOException ex) {
            LOG.severe("Error while analyze parsing result: " + result + " => " + ex.getMessage());
        }
    }

    @Override
    public int getPriority() {
        return Integer.MAX_VALUE;
    }

    @Override
    public Class<? extends Scheduler> getSchedulerClass() {
        return Scheduler.EDITOR_SENSITIVE_TASK_SCHEDULER;
    }

    @Override
    public void cancel() {
        // Not cancelable
    }
}
