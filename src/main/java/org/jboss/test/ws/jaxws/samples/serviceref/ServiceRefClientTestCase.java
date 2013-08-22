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
package org.jboss.test.ws.jaxws.samples.serviceref;

import org.jboss.wsf.test.JBossWSTestHelper;

import javax.servlet.http.HttpServlet;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import static org.junit.Assert.*;
/**
 * Test the JAXWS <service-ref>
 *
 * @author Thomas.Diesler@jboss.com
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
public class ServiceRefClientTestCase extends HttpServlet
{
   public final String TARGET_ENDPOINT_ADDRESS = "http://localhost:8080/jaxws-samples-serviceref";

   public void testWSDLAccess() throws Exception
   {
      URL wsdlURL = new URL(TARGET_ENDPOINT_ADDRESS + "?wsdl");
      InputStream inputStream = wsdlURL.openStream();
      assertNotNull(inputStream);
      inputStream.close();
   }

   public void testDynamicProxy() throws Exception
   {
      URL wsdlURL = getResourceURL("jaxws/samples/serviceref/META-INF/wsdl/Endpoint.wsdl");
      QName qname = new QName("http://serviceref.samples.jaxws.ws.test.jboss.org/", "EndpointService");
      Service service = Service.create(wsdlURL, qname);
      Endpoint port = (Endpoint)service.getPort(Endpoint.class);

      String request = "DynamicProxy";
      String response = port.echo(request);
      assertEquals(request, response);
   }

   public void testApplicationClient() throws Exception
   {
      final OutputStream appclientOS = new ByteArrayOutputStream();
      JBossWSTestHelper.deployAppclient("jaxws-samples-serviceref-appclient.ear#jaxws-samples-serviceref-appclient.jar", appclientOS, "Hello World!");
      // wait till appclient stops
      String appclientLog = appclientOS.toString();
      while (!appclientLog.contains("stopped in")) {
         Thread.sleep(100);
         appclientLog = appclientOS.toString();
      }
      // assert appclient logs
      assertTrue(appclientLog.contains("TEST START"));
      assertTrue(appclientLog.contains("TEST END"));
      assertFalse(appclientLog.contains("not overridden through service-ref"));
      assertFalse(appclientLog.contains("Invalid echo return"));
   }
}
