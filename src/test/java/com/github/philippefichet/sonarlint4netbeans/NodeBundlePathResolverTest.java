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

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author FICHET Philippe &lt;philippe.fichet@laposte.net&gt;
 */
public class NodeBundlePathResolverTest {
    
    @Test
    public void testSomeMethod() {
        String pathToSearch = "/Users/runner/hostedtoolcache/node/14.19.0/x64/bin:"
            + "/Users/runner/hostedtoolcache/jdk/8.0.322/x64/bin:"
            + "/usr/local/lib/ruby/gems/2.7.0/bin:"
            + "/usr/local/opt/ruby@2.7/bin:"
            + "/usr/local/opt/pipx_bin:"
            + "/Users/runner/.cargo/bin:"
            + "/usr/local/opt/curl/bin:"
            + "/usr/local/bin:"
            + "/usr/local/sbin:"
            + "/Users/runner/bin:"
            + "/Users/runner/.yarn/bin:"
            + "/Users/runner/Library/Android/sdk/tools:"
            + "/Users/runner/Library/Android/sdk/platform-tools:"
            + "/Users/runner/Library/Android/sdk/ndk-bundle:"
            + "/Library/Frameworks/Mono.framework/Versions/Current/Commands:"
            + "/usr/bin:/bin:/usr/sbin:"
            + "/sbin:"
            + "/Users/runner/.dotnet/tools:"
            + "/Users/runner/.ghcup/bin:"
            + "/Users/runner/hostedtoolcache/stack/2.7.3/x64";
        // /Users/runner/hostedtoolcache/node/14.19.0/x64
        String pathSeparator = ":";
        String relativePath = "package/node_modules/run-node/run-node";
        
        NodeBundlePathResolver nodeBundlePathResolver = new NodeBundlePathResolver(
            pathToSearch,
            pathSeparator,
            (String basePath, String search) -> {
                if (basePath.equals("/Users/runner/hostedtoolcache/node/14.19.0/x64/bin")
                    && search.equals(relativePath)) {
                    return basePath + "/" + relativePath;
                }
                return null;
            }
        );
        String resolve = nodeBundlePathResolver.resolve(relativePath);
        Assertions.assertThat(resolve)
            .isEqualTo("/Users/runner/hostedtoolcache/node/14.19.0/x64/bin/package/node_modules/run-node/run-node");
    }
    
}
