/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.wsf.test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.*;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.ws.Service;
import javax.xml.ws.Service.Mode;
import javax.xml.ws.soap.SOAPBinding;


/**
 * A JBossWS test helper that deals with test deployment/undeployment, etc.
 *
 * @author Thomas.Diesler@jboss.org
 * @author ropalka@redhat.com
 * @author alessio.soldano@jboss.com
 */
public class JBossWSTestHelper
{
    private static final String SYSPROP_JBOSSWS_INTEGRATION_TARGET = "jbossws.integration.target";
    private static final String SYSPROP_JBOSS_BIND_ADDRESS = "jboss.bind.address";
    private static final String SYSPROP_TEST_ARCHIVE_DIRECTORY = "test.archive.directory";
    private static final String SYSPROP_TEST_RESOURCES_DIRECTORY = "test.resources.directory";
    private static final String SYSPROP_AS_SERVER_CONN_RETRIEVAL_ATTEMPTS = "test.as.server.connection.retrieval.attempts";
    private static final String TEST_USERNAME = "test.username";
    private static final String TEST_PASSWORD = "test.password";
    private static final boolean DEPLOY_PROCESS_ENABLED = !Boolean.getBoolean("disable.test.archive.deployment");
    private static final int AS_SERVER_CONN_RETRIEVAL_ATTEMPTS = Integer.getInteger(SYSPROP_AS_SERVER_CONN_RETRIEVAL_ATTEMPTS, 5);

    private static MBeanServerConnection server;
    private static String integrationTarget;
    private static String implInfo;
    private static String testArchiveDir;
    private static String testResourcesDir;


    public static boolean isTargetJBoss7()
    {
        String target = getIntegrationTarget();
        return target.startsWith("jboss7");
    }

    public static boolean isTargetJBoss71()
    {
        String target = getIntegrationTarget();
        return target.startsWith("jboss71");
    }

    public static boolean isTargetJBoss72()
    {
        String target = getIntegrationTarget();
        return target.startsWith("jboss72");
    }

    public static boolean isIntegrationNative()
    {
        String vendor = getImplementationInfo();
        return vendor.toLowerCase().indexOf("jboss") != -1;
    }

    public static boolean isIntegrationCXF()
    {
        String vendor = getImplementationInfo();
        return vendor.toLowerCase().indexOf("apache") != -1;
    }

    private static String getImplementationInfo()
    {
        if (implInfo == null)
        {
            Object obj = getImplementationObject();
            implInfo = obj.getClass().getPackage().getName();
        }
        return implInfo;
    }

    private static Object getImplementationObject()
    {
        Service service = Service.create(new QName("dummyService"));
        Object obj = service.getHandlerResolver();
        if (obj == null)
        {
            service.addPort(new QName("dummyPort"), SOAPBinding.SOAP11HTTP_BINDING, "http://dummy-address");
            obj = service.createDispatch(new QName("dummyPort"), Source.class, Mode.PAYLOAD);
        }
        return obj;
    }

    private static String getImplementationPackage()
    {
        return getImplementationObject().getClass().getPackage().getName();
    }

    /**
     * Get the JBoss server host from system property "jboss.bind.address"
     * This defaults to "localhost"
     */
    public static String getServerHost()
    {
        final String host = System.getProperty(SYSPROP_JBOSS_BIND_ADDRESS, "localhost");
        return toIPv6URLFormat(host);
    }

    private static String toIPv6URLFormat(final String host)
    {
        try
        {
            if (host.startsWith(":"))
            {
                throw new IllegalArgumentException("JBossWS test suite requires IPv6 addresses to be wrapped with [] brackets. Expected format is: [" + host + "]");
            }
            if (host.startsWith("["))
            {
                if (System.getProperty("java.net.preferIPv4Stack") == null)
                {
                    throw new IllegalStateException("always provide java.net.preferIPv4Stack JVM property when using IPv6 address format");
                }
                if (System.getProperty("java.net.preferIPv6Addresses") == null)
                {
                    throw new IllegalStateException("always provide java.net.preferIPv6Addresses JVM property when using IPv6 address format");
                }
            }
            final boolean isIPv6Address = InetAddress.getByName(host) instanceof Inet6Address;
            final boolean isIPv6Formatted = isIPv6Address && host.startsWith("[");
            return isIPv6Address && !isIPv6Formatted ? "[" + host + "]" : host;
        }
        catch (final UnknownHostException e)
        {
            throw new RuntimeException(e);
        }
    }

    public static MBeanServerConnection getServer()
    {
        if (server == null)
        {
            String integrationTarget = getIntegrationTarget();
            if (integrationTarget.startsWith("jboss7"))
            {
                server = getAS7ServerConnection(integrationTarget);
            }
            else
            {
                throw new IllegalStateException("Unsupported target container: " + integrationTarget);
            }
        }
        return server;
    }

    private static MBeanServerConnection getAS7ServerConnection(String integrationTarget)
    {
        final String urlString = System.getProperty("jmx.service.url", "service:jmx:remoting-jmx://" + getServerHost() + ":" + 9999);
        JMXServiceURL serviceURL = null;
        JMXConnector connector = null;
        try
        {
            serviceURL = new JMXServiceURL(urlString);
        }
        catch (MalformedURLException e1)
        {
            throw new IllegalStateException(e1);
        }
        //add more tries to get the connection. Workaround to fix some test failures caused by connection is not established in 5 seconds
        for (int i = 0; i < AS_SERVER_CONN_RETRIEVAL_ATTEMPTS && connector == null; i++)
        {
            try
            {
                connector = JMXConnectorFactory.connect(serviceURL, null);
            }
            catch (IOException ex)
            {
                throw new IllegalStateException("Cannot obtain MBeanServerConnection to: " + urlString, ex);
            }
            catch (RuntimeException e)
            {
                if (e.getMessage().contains("WAITING") && i < AS_SERVER_CONN_RETRIEVAL_ATTEMPTS - 1)
                {
                    continue;
                }
                else
                {
                    throw e;
                }
            }
        }

        try
        {
            return connector.getMBeanServerConnection();
        }
        catch (Exception e)
        {
            throw new IllegalStateException("Cannot obtain MBeanServerConnection to: " + urlString, e);
        }
    }

    public static String getIntegrationTarget()
    {
        if (integrationTarget == null)
        {
            integrationTarget = System.getProperty(SYSPROP_JBOSSWS_INTEGRATION_TARGET);

            if (integrationTarget == null)
                throw new IllegalStateException("Cannot obtain system property: " + SYSPROP_JBOSSWS_INTEGRATION_TARGET);
        }

        return integrationTarget;
    }

    /** Try to discover the URL for the deployment archive */
    public static URL getArchiveURL(String archive) throws MalformedURLException
    {
        return getArchiveFile(archive).toURI().toURL();
    }

    /** Try to discover the File for the deployment archive */
    public static File getArchiveFile(String archive)
    {
        File file = new File(archive);
        if (file.exists())
            return file;

        file = new File(getTestArchiveDir() + "/" + archive);
        if (file.exists())
            return file;

        String notSet = (getTestArchiveDir() == null ? " System property '" + SYSPROP_TEST_ARCHIVE_DIRECTORY + "' not set." : "");
        throw new IllegalArgumentException("Cannot obtain '" + getTestArchiveDir() + "/" + archive + "'." + notSet);
    }

    /** Try to discover the URL for the test resource */
    public static URL getResourceURL(String resource) throws MalformedURLException
    {
        return getResourceFile(resource).toURI().toURL();
    }

    /** Try to discover the File for the test resource */
    public static File getResourceFile(String resource)
    {
        File file = new File(resource);
        if (file.exists())
            return file;

        file = new File(getTestResourcesDir() + "/" + resource);
        if (file.exists())
            return file;

        String notSet = (getTestResourcesDir() == null ? " System property '" + SYSPROP_TEST_RESOURCES_DIRECTORY + "' not set." : "");
        throw new IllegalArgumentException("Cannot obtain '" + getTestResourcesDir() + "/" + resource + "'." + notSet);
    }

    public static String getTestArchiveDir()
    {
        if (testArchiveDir == null)
            testArchiveDir = System.getProperty(SYSPROP_TEST_ARCHIVE_DIRECTORY);

        return testArchiveDir;
    }

    public static String getTestResourcesDir()
    {
        if (testResourcesDir == null)
            testResourcesDir = System.getProperty(SYSPROP_TEST_RESOURCES_DIRECTORY);

        return testResourcesDir;
    }

    public static String getTestUsername() {
        String prop = System.getProperty(TEST_USERNAME);
        if (prop == null || "".equals(prop) || ("${" + TEST_USERNAME + "}").equals(prop)) {
            prop = "kermit";
        }
        return prop;
    }

    public static String getTestPassword() {
        String prop = System.getProperty(TEST_PASSWORD);
        if (prop == null || "".equals(prop) || ("${" + TEST_PASSWORD + "}").equals(prop)) {
            prop = "thefrog";
        }
        return prop;
    }

    // ----------------------- Merged from JBossWSTest

    /** Get the server remote env context
     * Every test calling this method have to ensure InitialContext.close()
     * method is called at end of test to clean up all associated caches.
     */
    public static InitialContext getServerInitialContext() throws NamingException, IOException
    {
        final Hashtable<String, String> env = getEnvironment("server.jndi.properties");
        return new InitialContext(env);
    }

    private static Hashtable<String, String> getEnvironment(final String resourceName) throws IOException {
        final Hashtable<String, String> env = new Hashtable<String, String>();
        final InputStream is = JBossWSTestHelper.class.getClassLoader().getResourceAsStream(resourceName);
        if (is != null) {
            final Properties props = new Properties();
            props.load(is);
            Map.Entry<Object, Object> entry;
            final Iterator<Map.Entry<Object, Object>> entries = props.entrySet().iterator();
            while (entries.hasNext()) {
                entry = entries.next();
                env.put((String)entry.getKey(), (String)entry.getValue());
            }
        }
        return env;
    }

    /**
     * Execute <b>command</b> in separate process.
     * @param command command to execute
     * @throws IOException if I/O error occurs
     */
    public static void executeCommand(String command) throws IOException
    {
        executeCommand(command, null, null, null);
    }

    /**
     * Execute <b>command</b> in separate process. If process will fail, display custom <b>message</b> in assertion.
     * @param command command to execute
     * @param message message to display if assertion fails
     * @throws IOException if I/O error occurs
     */
    public static void executeCommand(String command, String message) throws IOException
    {
        executeCommand(command, null, message, null);
    }

    /**
     * Execute <b>command</b> in separate process, copy process input to <b>os</b>.
     * @param command command to execute
     * @param os output stream to copy process input to. If null, <b>System.out</b> will be used
     * @throws IOException if I/O error occurs
     */
    public static void executeCommand(String command, OutputStream os) throws IOException
    {
        executeCommand(command, os, null, null);
    }

    /**
     * Execute <b>command</b> in separate process, copy process input to <b>os</b>. If process will fail, display custom <b>message</b> in assertion.
     * @param command command to execute
     * @param os output stream to copy process input to. If null, <b>System.out</b> will be used
     * @param message message to display if assertion fails
     * @throws IOException if I/O error occurs
     */
    public static void executeCommand(String command, OutputStream os, String message) throws IOException
    {
        executeCommand(command, os, message, null);
    }

    /**
     * Execute <b>command</b> in separate process, copy process input to <b>os</b>. If process will fail, display custom <b>message</b> in assertion.
     * @param command command to execute
     * @param os output stream to copy process input to. If null, <b>System.out</b> will be used
     * @param message message to display if assertion fails
     * @param env environment
     * @throws IOException if I/O error occurs
     */
    public static void executeCommand(String command, OutputStream os, String message, Map<String, String> env) throws IOException
    {
        if (command == null)
            throw new NullPointerException( "Command cannot be null" );

        StringTokenizer st = new StringTokenizer(command, " \t\r");
        List<String> tokenizedCommand = new LinkedList<String>();
        while (st.hasMoreTokens())
        {
            // PRECONDITION: command doesn't contain whitespaces in the paths
            tokenizedCommand.add(st.nextToken());
        }

        try
        {
            ProcessBuilder pb = new ProcessBuilder(command);
            if (env != null)
            {
                for (String variable : env.keySet())
                {
                    pb.environment().put(variable, env.get(variable));
                }
            }
            Process p = pb.start();
            p.waitFor();
            byte[] buf = new byte[2048];
            InputStream is = p.getInputStream(); //standard out
            while(is.available() > 0){
                int read = is.read(buf);
                if (read > 0)
                    os.write(buf,0,read);
            }
            //executeCommand(tokenizedCommand, os, message, env);
        }
        catch (InterruptedException e){

        }
        catch (IOException e)
        {
            throw e;
        }
    }

}
