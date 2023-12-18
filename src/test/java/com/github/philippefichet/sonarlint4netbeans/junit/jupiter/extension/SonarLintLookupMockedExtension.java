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
package com.github.philippefichet.sonarlint4netbeans.junit.jupiter.extension;

import com.github.philippefichet.sonarlint4netbeans.Predicates;
import com.github.philippefichet.sonarlint4netbeans.assertj.SonarLintArgumentMatchers;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.netbeans.modules.openide.util.GlobalLookup;
import org.openide.util.Lookup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author FICHET Philippe &lt;philippe.fichet@laposte.net&gt;
 */
public final class SonarLintLookupMockedExtension implements InvocationInterceptor {
    private static final Logger LOG = LoggerFactory.getLogger(SonarLintLookupMockedExtension.class);
    private final boolean logCall;
    private final Map<Class<?>, Object> lookupMethod = new HashMap<>();
    private final Lookup mockedLookup = Mockito.mock(Lookup.class);
    private final Map<Class<?>, Supplier<?>> lookupMethodInstance = new HashMap<>();
    private SonarLintLookupMockedExtension(Builder builder)
    {
        this.logCall = builder.logCall;
        this.lookupMethod.putAll(builder.lookupMethod);
        this.lookupMethodInstance.putAll(builder.lookupMethodInstance);
        init();
    }
    
    private void init()
    {
        Lookup defaultLookup = Lookup.getDefault();
        if (!lookupMethod.isEmpty()) {
            Predicate<Class<?>> actualPredicate = null;
            for (Map.Entry<Class<?>, Object> entry : lookupMethod.entrySet()) {
                Class<?> key = entry.getKey();
                Object lookupInstance = entry.getValue();
                Predicate<Class<?>> byClass = Predicates.byClass(key);
                if (actualPredicate == null) {
                    LOG.debug("defaultLookup.lookup(Class<?>) mock with \"{}\"", key);
                    actualPredicate = byClass;
                } else {
                    LOG.debug("defaultLookup.lookup(Class<?>) mock previous or \"{}\"", key);
                    actualPredicate = actualPredicate.or(byClass);
                }

                // Cannot use thenReturn on <? extends XXX>
                Mockito.when(mockedLookup.lookup(key))
                    .thenAnswer(
                        (InvocationOnMock iom) -> {
                        if (logCall) {
                            LOG.debug("defaultLookup.lookup(Class<?>) mocked with \"{}\"", (Class<?>)iom.getArgument(0));
                        }
                        return lookupInstance;
                    });
            }
            Mockito.when(mockedLookup.lookup(
                SonarLintArgumentMatchers.byPredicateClassBased(actualPredicate.negate())
            )).thenAnswer(
                (InvocationOnMock iom) -> {
                    Class<?> firstArgument = (Class<?>)iom.getArgument(0);
                    if (logCall) {
                        LOG.debug("defaultLookup.lookup(Class<?>) called with \"{}\"", firstArgument);
                    }
                    return defaultLookup.lookup(firstArgument);
            });
            // Allow customize later
            Mockito.when(mockedLookup.lookupResult(
                ArgumentMatchers.any(Class.class)
            )).thenAnswer(
                (InvocationOnMock iom) -> {
                    Class<?> firstArgument = (Class<?>)iom.getArgument(0);
                    if (logCall) {
                        LOG.debug("defaultLookup.lookupResult(Class<?>) called with \"{}\"", firstArgument);
                    }
                    return defaultLookup.lookupResult(firstArgument);
            });
            // Allow customize later
            Mockito.when(mockedLookup.lookup(
                ArgumentMatchers.any(Lookup.Template.class)
            )).thenAnswer(
                (InvocationOnMock iom) -> {
                    Lookup.Template<?> firstArgument = (Lookup.Template<?>)iom.getArgument(0);
                    if (logCall) {
                        LOG.debug("defaultLookup.lookup(Lookup.Template<?>) called with \"{}\"", firstArgument);
                    }
                    return defaultLookup.lookup(firstArgument);
            });
        }

        if (!lookupMethodInstance.isEmpty()) {
            Map<Class<?>, Object> instances = new HashMap<>();
            lookupMethodInstance.forEach(
                (Class<?> clazz, Supplier<?> supplier) -> {
                    Mockito.when(mockedLookup.lookup(clazz))
                    .thenAnswer(
                        (InvocationOnMock iom) -> {
                            if (logCall) {
                                LOG.debug("defaultLookup.lookup(Class<?>) for an specific instance called with \"{}\"", clazz);
                            }
                            return instances.computeIfAbsent(clazz, (Class<?> cls) -> supplier.get());
                    });
                }
            );
        }
    }

    @Override
    public void interceptTestMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        executeInMockedLookup(() -> {
            try {
                InvocationInterceptor.super.interceptTestMethod(
                    invocation,
                    invocationContext,
                    extensionContext
                );
            } catch (Throwable ex) {
                ex.printStackTrace();
                Assertions.fail(ex);
            }
        });
    }

    @Override
    public void interceptTestTemplateMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        executeInMockedLookup(() -> {
            try {
                InvocationInterceptor.super.interceptTestTemplateMethod(
                    invocation,
                    invocationContext,
                    extensionContext
                );
            } catch (Throwable ex) {
                ex.printStackTrace();
                Assertions.fail(ex);
            }
        });
    }

    public void executeInMockedLookup(Runnable r)
    {
        GlobalLookup.execute(mockedLookup, r);
    }

    /**
     * Retrieve instance if mocked like call &lt;T&gt; T lookup(Class&lt;T&gt; clazz)
     * @param <T> type of instance
     * @param clazz class of instance wanted
     * @return 
     */
    public <T> Optional<T> lookupMocked(Class<T> clazz)
    {
        return Optional.ofNullable((T)lookupMethod.get(clazz));
    }

    
    public static Builder builder()
    {
        return new Builder();
    }

    public static class Builder
    {
        private boolean logCall = false;
        private final Map<Class<?>, Object> lookupMethod = new HashMap<>();
        private final Map<Class<?>, Supplier<?>> lookupMethodInstance = new HashMap<>();
        private Builder()
        {
            
        }

        /**
         * Replace actual instance by instance in &lt;T&gt; T lookup(Class&lt;T&gt; clazz)
         * @param <T>
         * @param classForLookup
         * @param instance
         * @return this builder
         * @see org.​openide.​util.​Lookup#lookup(Class&lt;T&gt; clazz)
         * 
         */
        public <T> Builder mockLookupMethodWith(Class<T> classForLookup, T instance)
        {
            lookupMethod.put(classForLookup, instance);
            return this;
        }

        /**
         * Replace actual instance by instance in &lt;T&gt; T lookup(Class&lt;T&gt; clazz)
         * @param <T>
         * @param classForLookup
         * @param instanceFactory
         * @return this builder
         * @see org.​openide.​util.​Lookup#lookup(Class&lt;T&gt; clazz)
         * 
         */
        public <T> Builder mockLookupMethodInstanceWith(Class<T> classForLookup, Supplier<T> instanceFactory)
        {
            lookupMethodInstance.put(classForLookup, instanceFactory);
            return this;
        }

        /**
         * Alias to logCall(true)
         * @return this builder
         */
        public Builder logCall()
        {
            logCall(true);
            return this;
        }
        
        /**
         * Enable or disable log each call on Lookup
         * @param logCall
         * @return this builder
         */
        public Builder logCall(boolean logCall)
        {
            this.logCall = logCall;
            return this;
        }
        
        public SonarLintLookupMockedExtension build()
        {
            return new SonarLintLookupMockedExtension(this);
        }
    }
}
