/*
 * Copyright 2019 Giacomo Lacava
 * Modified from Per Wendel's original SocketConnectorFactory.java.
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

import org.eclipse.jetty.server.*;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.embeddedserver.jetty.SocketConnectorFactory;
import spark.ssl.SslStores;
import spark.utils.Assert;

import java.util.concurrent.TimeUnit;

/**
 * ServerConnector factory, where you can tweak ssl parameters etc
 */
public class MyCustomSocketConnectorFactory extends SocketConnectorFactory {
    private final static Logger logger = LoggerFactory.getLogger(MyCustomSocketConnectorFactory.class);

    // plain http
    public static ServerConnector createSocketConnector(Server server, String host, int port) {
        Assert.notNull(server, "'server' must not be null");
        Assert.notNull(host, "'host' must not be null");

        HttpConnectionFactory httpConnectionFactory = createHttpConnectionFactory();
        ServerConnector connector = new ServerConnector(server, httpConnectionFactory);
        initializeConnector(connector, host, port);
        /* ********* CUSTOMIZATION POINT ***************
         * If you need any extra option on your regular connector, this is where to add it
         */
        logger.info("HEY MY CUSTOM THINGY WORKS!! come say thanks at https://www.linkedin.com/in/glacava/");

        return connector;
    }

    // ssl / tls https.
    public static ServerConnector createSecureSocketConnector(Server server, String host, int port, SslStores sslStores) {
        Assert.notNull(server, "'server' must not be null");
        Assert.notNull(host, "'host' must not be null");
        Assert.notNull(sslStores, "'sslStores' must not be null");

        SslContextFactory sslContextFactory = new SslContextFactory(sslStores.keystoreFile());

        /* ********* CUSTOMIZATION POINT ***************
        this is where you can set any custom SSL-related option.
        In this case, as an example, I'm overriding the default choice of algorithms and ciphers.
        Note how I don't touch anything to do with sslStores, so the secure() spark API still works the same.
         */
        // first we need to clear existing exclusions
        sslContextFactory.setExcludeProtocols(new String[]{});
        sslContextFactory.setExcludeCipherSuites(new String[]{});
        // then we re-add what we want
        sslContextFactory.setExcludeProtocols(SSLOptions.PROTOCOLS);
        sslContextFactory.setIncludeCipherSuites(SSLOptions.CIPHERS);


        if (sslStores.keystorePassword() != null) {
            sslContextFactory.setKeyStorePassword(sslStores.keystorePassword());
        }

        if (sslStores.certAlias() != null) {
            sslContextFactory.setCertAlias(sslStores.certAlias());
        }

        if (sslStores.trustStoreFile() != null) {
            sslContextFactory.setTrustStorePath(sslStores.trustStoreFile());
        }

        if (sslStores.trustStorePassword() != null) {
            sslContextFactory.setTrustStorePassword(sslStores.trustStorePassword());
        }

        if (sslStores.needsClientCert()) {
            sslContextFactory.setNeedClientAuth(true);
            sslContextFactory.setWantClientAuth(true);
        }

        HttpConnectionFactory httpConnectionFactory = createHttpConnectionFactory();
        ServerConnector connector = new ServerConnector(server, sslContextFactory, httpConnectionFactory);
        initializeConnector(connector, host, port);

        logger.info("HEY MY CUSTOM THINGY WORKS!! come say thanks at https://www.linkedin.com/in/glacava/");

        return connector;
    }

    private static void initializeConnector(ServerConnector connector, String host, int port) {
        connector.setIdleTimeout(TimeUnit.HOURS.toMillis(1L));
        connector.setHost(host);
        connector.setPort(port);
    }

    private static HttpConnectionFactory createHttpConnectionFactory() {
        HttpConfiguration httpConfig = new HttpConfiguration();
        httpConfig.setSecureScheme("https");
        httpConfig.addCustomizer(new ForwardedRequestCustomizer());
        return new HttpConnectionFactory(httpConfig);
    }
}
