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

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.FieldValue;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import org.openide.util.Lookup;
import org.sonarsource.sonarlint.core.container.standalone.rule.StandaloneRule;

/**
 *
 * @author FICHET Philippe &lt;philippe.fichet@laposte.net&gt;
 */
public class StandaloneActiveRuleAdapterInterceptor {

    private StandaloneActiveRuleAdapterInterceptor() {
    }

    @RuntimeType
    public static Object intercept(
        @SuperCall Callable<?> callable,
        @Origin Method method,
        @AllArguments Object[] arguments,
        @FieldValue("rule") StandaloneRule rule
    ) throws Exception {
        String methodName = method.getName();
        if (methodName.equals("param")) {
            SonarLintEngine sonarLintEngine = Lookup.getDefault().lookup(SonarLintEngine.class);
            Optional<String> ruleParameter = sonarLintEngine.getRuleParameter(rule.getKey(), (String)arguments[0]);
            if(ruleParameter.isPresent()) {
                return ruleParameter.get();
            } else {
                return callable.call();
            }
        } else {
            Map<String, String> params = (Map<String, String>) callable.call();
            if (!params.isEmpty()) {
                SonarLintEngine sonarLintEngine = Lookup.getDefault().lookup(SonarLintEngine.class);
                for (String parametersKey : params.keySet()) {
                    Optional<String> ruleParameter = sonarLintEngine.getRuleParameter(rule.getKey(), parametersKey);
                    if(ruleParameter.isPresent()) {
                        params.put(parametersKey, ruleParameter.get());
                    }
                }
            }
            return params;
        }
    }

}
