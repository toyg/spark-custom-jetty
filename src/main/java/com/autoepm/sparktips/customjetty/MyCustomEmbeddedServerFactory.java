
package com.autoepm.sparktips.customjetty;

import org.eclipse.jetty.util.thread.ThreadPool;
import spark.ExceptionMapper;
import spark.embeddedserver.EmbeddedServer;
import spark.embeddedserver.EmbeddedServerFactory;
import spark.embeddedserver.jetty.JettyHandler;
import spark.http.matching.MatcherFilter;
import spark.route.Routes;
import spark.staticfiles.StaticFilesConfiguration;

import javax.servlet.FilterConfig;


/** This is the main entry point for customization operations **/
public class MyCustomEmbeddedServerFactory implements EmbeddedServerFactory {
    private ThreadPool threadPool;
    private boolean httpOnly = true;

    public MyCustomEmbeddedServerFactory() {
    }

    @Override
    public EmbeddedServer create(Routes routeMatcher, StaticFilesConfiguration staticFilesConfiguration, ExceptionMapper exceptionMapper, boolean hasMultipleHandler) {
        MatcherFilter matcherFilter = new MatcherFilter(routeMatcher, staticFilesConfiguration, exceptionMapper, false, hasMultipleHandler);
        matcherFilter.init((FilterConfig) null);
        /* ********* CUSTOMIZATION POINT ***************
         * If you want to provide custom handlers, to do things like header manipulation and so on, this is the place.
         */
        JettyHandler handler = new JettyHandler(matcherFilter);
        handler.getSessionCookieConfig().setHttpOnly(this.httpOnly);

        // return an instance of your custom EmbeddedServer implementation.
        return (new MyCustomEmbeddedServer(new MyCustomJettyThreadPoolConfigFactory(), handler)).withThreadPool(this.threadPool);
    }

    public MyCustomEmbeddedServerFactory withThreadPool(ThreadPool threadPool) {
        this.threadPool = threadPool;
        return this;
    }

    public MyCustomEmbeddedServerFactory withHttpOnly(boolean httpOnly) {
        this.httpOnly = httpOnly;
        return this;
    }


}
