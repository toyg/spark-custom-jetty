package com.autoepm.sparktips.customjetty;

import spark.embeddedserver.EmbeddedServer;
import spark.embeddedserver.EmbeddedServers;

import static spark.Spark.*;
public class Example {

        public static void main(String[] args) {

            // We add our custom factory to the list.
            // By overriding the JETTY identifier, we ensure the default one is not called.
            EmbeddedServers.add(
                    EmbeddedServers.Identifiers.JETTY,
                    new MyCustomEmbeddedServerFactory());

            // do you SSL? You should.
            secure("myKeystoreFilePath.jks",
                    "myKeystoreFilePassword",
                    "miRootTrustKeystoreFilePath.jks",
                    "myRootTrustFileKeystorePassword");

            get("/hello", (req, res) -> "Hello World");
        }
}
