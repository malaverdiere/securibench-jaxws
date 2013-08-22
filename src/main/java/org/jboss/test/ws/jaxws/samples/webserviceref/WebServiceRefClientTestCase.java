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
package org.jboss.test.ws.jaxws.samples.webserviceref;

import javax.servlet.http.HttpServlet;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.URL;
/**
 * Test the JAXWS annotation: javax.xml.ws.WebServiceref
 *
 * @author Thomas.Diesler@jboss.com
 * @since 23-Oct-2005
 */
public class WebServiceRefClientTestCase extends HttpServlet
{
   public final String TARGET_ENDPOINT_ADDRESS = "http://localhost:8080/jaxws-samples-webserviceref";


   public void testGeneratedService() throws Exception
   {
      URL wsdlURL = new URL(TARGET_ENDPOINT_ADDRESS + "?wsdl");
      QName serviceQName = new QName("http://org.jboss.ws/wsref", "EndpointService");

      EndpointService service = new EndpointService(wsdlURL, serviceQName);
      Endpoint port = service.getEndpointPort();

      String helloWorld = "Hello World!";
      Object retObj = port.echo(helloWorld);
      assertEquals(helloWorld, retObj);
   }

   public void testDynamicProxy() throws Exception
   {
      URL wsdlURL = new URL(TARGET_ENDPOINT_ADDRESS + "?wsdl");
      QName serviceQName = new QName("http://org.jboss.ws/wsref", "EndpointService");
      Service service = Service.create(wsdlURL, serviceQName);
      Endpoint port = (Endpoint)service.getPort(Endpoint.class);

      String helloWorld = "Hello World!";
      Object retObj = port.echo(helloWorld);
      assertEquals(helloWorld, retObj);
   }

   public void testApplicationClient() throws Throwable
   {
      final String appclientArg = "Hello World!";
      final OutputStream appclientOS = new ByteArrayOutputStream();
      final Process appclientProcess = JBossWSTestHelper.deployAppclient("jaxws-samples-webserviceref-appclient.ear#jaxws-samples-webserviceref-appclient.jar", appclientOS, appclientArg);
      // wait till appclient stops
      String appclientLog = appclientOS.toString();
      while (!appclientLog.contains("stopped in")) {
         Thread.sleep(100);
         appclientLog = appclientOS.toString();
      }
      // assert appclient logs
      assertTrue(!appclientLog.contains("Invalid echo return"));
      assertTrue(appclientLog.contains("TEST START"));
      assertTrue(appclientLog.contains("TEST END"));
   }
}
