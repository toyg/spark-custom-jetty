/*
 * Copyright 2019 Giacomo Lacava
 * Modified from Per Wendel's original EmbeddedJettyServer.java
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package com.autoepm.sparktips.customjetty;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.embeddedserver.EmbeddedServer;
import spark.embeddedserver.jetty.websocket.WebSocketHandlerWrapper;
import spark.embeddedserver.jetty.websocket.WebSocketServletContextHandlerFactory;
import spark.ssl.SslStores;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * your custom EmbeddedServer implementation. Most of the methods are compulsory.
 */
public class MyCustomEmbeddedServer implements EmbeddedServer {

    private static final int SPARK_DEFAULT_PORT = 4567;
    private static final String NAME = "Spark";
    private final Handler handler;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final MyCustomJettyThreadPoolConfigFactory serverFactory;
    private Server server;
    private Map<String, WebSocketHandlerWrapper> webSocketHandlers;
    private Optional<Integer> webSocketIdleTimeoutMillis;
    private ThreadPool threadPool = null;


    public MyCustomEmbeddedServer(MyCustomJettyThreadPoolConfigFactory serverFactory, Handler handler) {
        this.serverFactory = serverFactory;
        this.handler = handler;
    }

    @Override
    public int ignite(String host, int port, SslStores sslStores, int maxThreads, int minThreads, int threadIdleTimeoutMillis) throws Exception {
        boolean hasCustomizedConnectors = false;
        if (port == 0) {
            try (ServerSocket s = new ServerSocket(0)) {
                port = s.getLocalPort();
            } catch (IOException e) {
                logger.error("Could not get first available port (port set to 0), using default: {}", SPARK_DEFAULT_PORT);
                port = SPARK_DEFAULT_PORT;
            }
        }

        // Create instance of jetty server with either default or supplied queued thread pool
        if (threadPool == null) {
            server = serverFactory.create(maxThreads, minThreads, threadIdleTimeoutMillis);
        } else {
            server = serverFactory.create(threadPool);
        }


        /* ********* CUSTOMIZATION POINT ***************
         * This is where we buid the necessary Jetty connector.
         * By default we handle http and https, if you need anything else, this is the place to add it.
         * Note that SSL options are tweaked further down in MyCustomSocketConnectorFactory,
         * so if that's what you're looking after, go there.
         */
        ServerConnector connector;
        if (sslStores == null) {
            connector = MyCustomSocketConnectorFactory.createSocketConnector(this.server, host, port);
        } else {
            connector = MyCustomSocketConnectorFactory.createSecureSocketConnector(this.server, host, port, sslStores);
        }

        Connector[] previousConnectors = this.server.getConnectors();
        this.server = connector.getServer();
        if (previousConnectors.length != 0) {
            this.server.setConnectors(previousConnectors);
            hasCustomizedConnectors = true;
        } else {
            this.server.setConnectors(new Connector[]{connector});
        }

        /* ********* CUSTOMIZATION POINT ***************
        If you need stuff specific to WebSockets, have a look here.
         */
        ServletContextHandler webSocketServletContextHandler = WebSocketServletContextHandlerFactory.create(this.webSocketHandlers, this.webSocketIdleTimeoutMillis);
        if (webSocketServletContextHandler == null) {
            this.server.setHandler(this.handler);
        } else {
            List<Handler> handlersInList = new ArrayList();
            handlersInList.add(this.handler);
            if (webSocketServletContextHandler != null) {
                handlersInList.add(webSocketServletContextHandler);
            }

            HandlerList handlers = new HandlerList();
            handlers.setHandlers((Handler[]) handlersInList.toArray(new Handler[handlersInList.size()]));
            this.server.setHandler(handlers);
        }

        this.logger.info("== {} has ignited ...", "Spark");
        if (hasCustomizedConnectors) {
            this.logger.info(">> Listening on Custom Server ports!");
        } else {
            this.logger.info(">> Listening on {}:{}", host, port);
        }

        this.server.start();
        return port;
    }

    @Override
    public void join() throws InterruptedException {
        this.server.join();
    }

    @Override
    public void extinguish() {
        this.logger.info(">>> {} shutting down ...", "Spark");

        try {
            if (this.server != null) {
                this.server.stop();
            }
        } catch (Exception var2) {
            this.logger.error("stop failed", var2);
            System.exit(100);
        }

        this.logger.info("done");
    }

    @Override
    public int activeThreadCount() {
        return this.server == null ? 0 : this.server.getThreadPool().getThreads() - this.server.getThreadPool().getIdleThreads();
    }

    public void configureWebSockets(Map<String, WebSocketHandlerWrapper> webSocketHandlers, Optional<Integer> webSocketIdleTimeoutMillis) {
        /* ********* CUSTOMIZATION POINT ***************
        If you need stuff specific to WebSockets, have a look here.
         */
        this.webSocketHandlers = webSocketHandlers;
        this.webSocketIdleTimeoutMillis = webSocketIdleTimeoutMillis;
    }

    public MyCustomEmbeddedServer withThreadPool(ThreadPool threadPool) {
        this.threadPool = threadPool;
        return this;
    }
}
