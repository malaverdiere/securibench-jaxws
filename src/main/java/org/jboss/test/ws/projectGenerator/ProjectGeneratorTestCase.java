/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.projectGenerator;

import javax.servlet.http.HttpServlet;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import static org.junit.Assert.*;

/**
 * A test case for the user project generator:
 * - creates a user-project.properties file with the configuration (i.e. the output users get with the interactive setup-new-project Ant target)
 * - calls the project generator (create-new-project Ant target)
 * - writes a simple endpoint implementation in the project workspace
 * - calls the deploy target of the generated build file
 * - invokes the deployed simple endpoint
 * 
 * @author alessio.soldano@jboss.com
 * @since 28-Apr-2008
 */
public class ProjectGeneratorTestCase extends HttpServlet
{
   private static final String FS = System.getProperty("file.separator"); // '/' on unix, '\' on windows
   private static final String PS = System.getProperty("path.separator"); // ':' on unix, ';' on windows
   private static final String ANT_SHELL = ":".equals( PS ) ? "ant" : "ant.bat";
   private String jbossHome;
   private File binDistroDir;
   private String projectName = "GeneratorTestProject";
   
   private String endpointURL = "http://localhost:8080/" + projectName + "/EndpointService/EndpointImpl";
   private String targetNS = "http://projectGenerator.ws.test.jboss.org/";
   
   protected void setUp() throws Exception
   {
      super.setUp();
      jbossHome = System.getProperty("jboss.home").replace('\\', '/');
      binDistroDir = new File(System.getProperty("user.dir"), "..");
   }
   
   public void testGenerator() throws Exception
   {
      if (!isDistroTest())
      {
         System.out.println("Skipping this test since it is meant to be run on binary distribution only.");
         return;
      }
      File userPrjProp = new File(binDistroDir, "user-project.properties");
      writeUserProjectProperties(userPrjProp);
      File distroBuild = new File(binDistroDir, "build.xml");
      String integrationTarget = System.getProperty("jbossws.integration.target");
      String env = " -D" + integrationTarget + ".home=" + jbossHome + " -Djbossws.integration.target=" + integrationTarget;
      executeCommand(ANT_SHELL + " -f " + distroBuild.getAbsolutePath() + env + " create-project", "Error while creating the user project!");
      File projectHomeDir = new File(binDistroDir, projectName);
      File packageDir = new File(projectHomeDir.getCanonicalPath() + FS + "src" + FS + "main" + FS + "java" + FS + "org" + FS + "jboss" + FS + "test" + FS + "ws" + FS + "projectGenerator");
      packageDir.mkdirs();
      File endpointImpl = new File(packageDir, "EndpointImpl.java");
      writeEndpointImpl(endpointImpl);
      File endpointInterface = new File(packageDir, "Endpoint.java");
      writeEndpointInterface(endpointInterface);
      File projectBuild = new File(projectHomeDir.getCanonicalPath(), "build.xml");
      try
      {
         executeCommand(ANT_SHELL + " -f " + projectBuild + " deploy", "Error while compiling / deploying the user project!");
         System.out.println("Sleeping 15 sec to let the deployment scanner pick up the user project jar");
         Thread.sleep(15000);
         //Running the actual test
         URL wsdlURL = new URL(endpointURL + "?wsdl");
         QName serviceName = new QName(targetNS, "EndpointService");
         Service service = Service.create(wsdlURL, serviceName);
         Endpoint port = (Endpoint)service.getPort(Endpoint.class);
         Object retObj = port.echo("Hello");
         assertEquals("Hello", retObj);
      }
      finally
      {
         executeCommand(ANT_SHELL + " -f " + projectBuild + " undeploy", "Error while undeploying the user project");
         executeCommand(ANT_SHELL + " -f " + distroBuild.getCanonicalPath() + env + " delete-project", "Error while deleting the user project");
         userPrjProp.delete();
      }
   }
   
   private void writeEndpointImpl(File file) throws Exception
   {
      StringBuffer sb = new StringBuffer();
      sb.append("package org.jboss.test.ws.projectGenerator;\n");
      sb.append("import javax.jws.WebService;\n");
      sb.append("import javax.ejb.Stateless;\n");
      sb.append("@Stateless\n");
      sb.append("@WebService(serviceName=\"EndpointService\", endpointInterface = \"org.jboss.test.ws.projectGenerator.Endpoint\")\n");
      sb.append("public class EndpointImpl {\n");
      sb.append("  public String echo(String input) {\n");
      sb.append("    return input;\n");
      sb.append("  }\n");
      sb.append("}\n");
      BufferedWriter out = new BufferedWriter(new FileWriter(file));
      out.write(sb.toString());
      out.close();
   }
   
   private void writeEndpointInterface(File file) throws Exception
   {
      StringBuffer sb = new StringBuffer();
      sb.append("package org.jboss.test.ws.projectGenerator;\n");
      sb.append("import javax.jws.WebMethod;\n");
      sb.append("import javax.jws.WebService;\n");
      sb.append("import javax.jws.soap.SOAPBinding;\n");
      sb.append("@WebService (name=\"Endpoint\")\n");
      sb.append("@SOAPBinding(style = SOAPBinding.Style.RPC)\n");
      sb.append("public interface Endpoint {\n");
      sb.append("  @WebMethod(operationName = \"echoString\", action = \"urn:EchoString\")\n");
      sb.append("  String echo(String input);\n");
      sb.append("}\n");
      BufferedWriter out = new BufferedWriter(new FileWriter(file));
      out.write(sb.toString());
      out.close();
   }
   
   private void writeUserProjectProperties(File file) throws Exception
   {
      StringBuffer sb = new StringBuffer();
      sb.append("#JBossWS user project generator test\n");
      sb.append("project.name=" + projectName + "\n");
      sb.append("project.jboss.home=" + jbossHome.replace('\\', '/') + "\n");
      sb.append("project.type=jar\n");
      sb.append("project.jboss.conf=standalone\n");
      sb.append("workspace.home=" + binDistroDir.getAbsolutePath().replace('\\', '/') + "\n");
      BufferedWriter out = new BufferedWriter(new FileWriter(file));
      out.write(sb.toString());
      out.close();
   }
   
   private boolean isDistroTest() throws Exception
   {
      return Boolean.getBoolean("binary.distribution");
   }
}
