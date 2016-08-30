package Server; /**
    Copyright 2014-2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.

    Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance with the License. A copy of the License is located at

        http://aws.amazon.com/apache2.0/

    or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

import CodeHelper.CodeHelperSpeechlet;
import com.amazon.speech.speechlet.Speechlet;
import com.amazon.speech.speechlet.servlet.SpeechletServlet;
import org.apache.log4j.BasicConfigurator;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * Shared launcher for executing all sample skills within a single servlet container.
 */
public final class Launcher {
    /**
     * port number for the jetty server.
     */
    private static final int PORT = 8080;

    /**
     * Security scheme to use.
     */
    private static final String HTTPS_SCHEME = "http";

    /**
     * default constructor.
     */
    private Launcher() {
    }

    /**
     * Main entry point. Starts a Jetty server.
     *
     * @param args
     *            ignored.
     * @throws Exception
     *             if anything goes wrong.
     */
    public static void main(final String[] args) throws Exception {

        // Configure logging to output to the console with default level of INFO
        BasicConfigurator.configure();

        // Configure server and its associated servlets
        Server server = new Server();

        HttpConfiguration httpConf = new HttpConfiguration();
        httpConf.setSecurePort(PORT);
        httpConf.addCustomizer(new SecureRequestCustomizer());
        HttpConnectionFactory httpConnectionFactory = new HttpConnectionFactory(httpConf);

        ServerConnector serverConnector = new ServerConnector(server, httpConnectionFactory);
        serverConnector.setPort(PORT);

        Connector[] connectors = new Connector[1];
        connectors[0] = serverConnector;
        server.setConnectors(connectors);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);
        context.addServlet(new ServletHolder(createServlet(new CodeHelperSpeechlet())), "/codehelper");

        server.start();
        server.join();
    }

    private static SpeechletServlet createServlet(final Speechlet speechlet) {
        SpeechletServlet servlet = new SpeechletServlet();
        servlet.setSpeechlet(speechlet);
        return servlet;
    }

}
