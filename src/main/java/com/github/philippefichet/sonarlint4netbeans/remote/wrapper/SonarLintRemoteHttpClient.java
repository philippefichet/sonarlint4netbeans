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
package com.github.philippefichet.sonarlint4netbeans.remote.wrapper;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.WebSocket;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.sonarsource.sonarlint.core.http.HttpClient;
import org.sonarsource.sonarlint.core.http.HttpClient.AsyncRequest;
import org.sonarsource.sonarlint.core.http.HttpClient.Response;
import org.sonarsource.sonarlint.core.http.HttpConnectionListener;

/**
 *
 * @author FICHET Philippe &lt;philippe.fichet@laposte.net&gt;
 */
public class SonarLintRemoteHttpClient implements HttpClient {
    private static final String AUTHORIZATION_HEADER_NAME = "Authorization";
    private final java.net.http.HttpClient client = java.net.http.HttpClient.newBuilder().build();
    private final Consumer<Long> responseReadByteConsumer;
    private final String login;

    public enum Authentification {
        BASIC,
        BEARER;
    }

    public SonarLintRemoteHttpClient(String login, Authentification authentification, Consumer<Long> responseReadByteConsumer) {
        this.responseReadByteConsumer = responseReadByteConsumer;
        this.login = toHttpHeader(login, authentification);
    }

    private String toHttpHeader(String login, SonarLintRemoteHttpClient.Authentification authentification) {
        switch(authentification) {
            case BEARER:
                return "Bearer " + login;
            case BASIC:
                return "Basic " + Base64.getEncoder().encodeToString((login + ":").getBytes());
            default:
                throw new UnsupportedOperationException("Authentification \"" + authentification.name() + "\" not supported");
        }
    }

    @Override
    public Response get(String url) {
        try {
            HttpResponse<InputStream> response = client.send(
                HttpRequest.newBuilder(URI.create(url)).GET()
                    .header(AUTHORIZATION_HEADER_NAME, login)
                    .build(),
                HttpResponse.BodyHandlers.ofInputStream()
            );
            return new SonarLintRemoteHttpResponse(url, response, responseReadByteConsumer);
        } catch (IOException ex) {
            Logger.getLogger(SonarLintRemoteHttpClient.class.getName()).log(Level.SEVERE, null, ex);
            // TODO cumtomize exception
            throw new RuntimeException(ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(SonarLintRemoteHttpClient.class.getName()).log(Level.SEVERE, null, ex);
            Thread.currentThread().interrupt();
            // TODO cumtomize exception
            throw new RuntimeException(ex);
        }
    }

    @Override
    public CompletableFuture<Response> getAsync(String url) {
        return client.sendAsync(
                HttpRequest.newBuilder(URI.create(url)).GET()
                    .header(AUTHORIZATION_HEADER_NAME, login)
                    .build(),
                HttpResponse.BodyHandlers.ofInputStream()
            ).thenApply((HttpResponse<InputStream> send) -> new SonarLintRemoteHttpResponse(url, send, responseReadByteConsumer));
    }

    @Override
    public WebSocket createWebSocketConnection(String string, Consumer<String> cnsmr, Runnable r) {
        // TODO ?
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public AsyncRequest getEventStream(String url, HttpConnectionListener connectionListener, Consumer<String> messageConsumer) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Response post(String url, String contentType, String body) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Response delete(String url, String contentType, String body) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

}