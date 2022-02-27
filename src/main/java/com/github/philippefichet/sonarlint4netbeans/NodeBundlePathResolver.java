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

import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.logging.Logger;
import org.sonarsource.nodejs.BundlePathResolver;

/**
 *
 * @author FICHET Philippe &lt;philippe.fichet@laposte.net&gt;
 */
public class NodeBundlePathResolver implements BundlePathResolver
{
    private static final Logger LOG = Logger.getLogger(NodeBundlePathResolver.class.getCanonicalName());
    private static final String NODE_COMMAND_NAME = "node";
    private static final String REDIRECT_NODE_RELATIVE_PATH = "package/node_modules/run-node/run-node";
    private final Supplier<String> pathToSearchSupplier;
    private String[] pathToSearch;
    private final String pathSeparator;
    private final BiFunction<String, String, String> checkFileExist;

    /**
     * 
     * @param pathToSearch Like $PATH environement variable
     */
    public NodeBundlePathResolver(Supplier<String> pathToSearchSupplier, String pathSeparator, BiFunction<String, String, String> checkFileExist) {
        this.pathToSearchSupplier = pathToSearchSupplier;
        this.pathSeparator = pathSeparator;
        this.checkFileExist = checkFileExist;
        LOG.fine("NodeBundlePathResolver pathSeparator \"" + pathSeparator + "\"");
    }

    @Override
    public String resolve(String relativePath) {
        if (pathToSearch == null) {
            pathToSearch = pathToSearchSupplier.get().split(pathSeparator);
            LOG.fine("NodeBundlePathResolver pathToSearch \"" + Arrays.toString(pathToSearch) + "\"");
        }
        String node = relativePath.equals(REDIRECT_NODE_RELATIVE_PATH) ? NODE_COMMAND_NAME : relativePath;
        for (String pathSearch : pathToSearch) {
            String pathIfExist = checkFileExist.apply(pathSearch, node);
            if (pathIfExist != null) {
                return pathIfExist;
            }
        }
        return null;
    }
}
