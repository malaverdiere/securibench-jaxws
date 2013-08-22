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
package org.jboss.test.ws.jaxws.samples.eardeployment;

import javax.servlet.http.HttpServlet;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import static org.junit.Assert.*;


/**
 * Test the wsdl is published to local filesystem; this test assumes
 * client and server share the filesystem.
 * 
 * @author alessio.soldano@jboss.com
 */
public class WSDLPublishTestCase extends HttpServlet
{
   private File wsdlFileDir;
   private static long testStart;

   public void testEJB3Endpoint() throws Exception
   {
      String soapAddress = "http://localhost:8080/earejb3/EndpointService/Endpoint";
      QName serviceName = new QName("http://eardeployment.jaxws/", "EndpointService");
      
      File file = new File(getWsdlFileDir().getAbsolutePath() + File.separator + "jaxws-samples-eardeployment.ear" + File.separator
            + "jaxws-samples-eardeployment-ejb3.jar" + File.separator + "Endpoint.wsdl");
      
      assertTrue("Wsdl file not found", file.exists());
      assertTrue("Stale wsdl file found", file.lastModified() > testStart - 1000);
      
      URL wsdlUrl = file.toURI().toURL();
      
      Service service = Service.create(wsdlUrl, serviceName);
      Endpoint port = service.getPort(Endpoint.class);

      BindingProvider bp = (BindingProvider)port;
      bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, soapAddress);

      String helloWorld = "Hello world!";
      String retObj = port.echo(helloWorld);
      assertEquals(helloWorld, retObj);
   }

   public void testJSEEndpoint() throws Exception
   {
      String soapAddress = "http://localhost:8080/earjse/JSEBean";
      QName serviceName = new QName("http://eardeployment.jaxws/", "EndpointService");
      
      File file = new File(getWsdlFileDir().getAbsolutePath() + File.separator + "jaxws-samples-eardeployment.ear" + File.separator
            + "jaxws-samples-eardeployment-pojo.war" + File.separator + "Endpoint.wsdl");
      
      assertTrue("Wsdl file not found", file.exists());
      assertTrue("Stale wsdl file found", file.lastModified() > testStart - 1000);
      
      URL wsdlUrl = file.toURI().toURL();

      Service service = Service.create(wsdlUrl, serviceName);
      Endpoint port = service.getPort(Endpoint.class);

      BindingProvider bp = (BindingProvider)port;
      bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, soapAddress);

      String helloWorld = "Hello world!";
      String retObj = port.echo(helloWorld);
      assertEquals(helloWorld, retObj);
   }
   
   private File getWsdlFileDir() throws IOException
   {
      if (wsdlFileDir == null)
      {
         URL url = new URL("http://localhost:8080/earjse/support");
         BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
         wsdlFileDir = new File(br.readLine(), "wsdl");
      }
      return wsdlFileDir;
   }
}
