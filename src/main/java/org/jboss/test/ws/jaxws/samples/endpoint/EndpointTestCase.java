/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.samples.endpoint;

import javax.servlet.http.HttpServlet;
import javax.xml.namespace.QName;
import javax.xml.ws.Endpoint;
import javax.xml.ws.Service;
import javax.xml.ws.soap.SOAPBinding;
import java.net.URL;



/**
 * Tests JAXWS dynamic endpoint deployment in an JSE environment.
 *
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
public class EndpointTestCase extends HttpServlet
{
   private static final int port = 8878;

   public void test() throws Exception
   {
      String publishURL1 = "http://localhost:" + port + "/jaxws-endpoint1";
      Endpoint endpoint1 = publishEndpoint(new EndpointBean(), publishURL1);

      String publishURL2 = "http://localhost:" + port + "/jaxws-endpoint2";
      Endpoint endpoint2 = publishEndpoint(new EndpointBean(), publishURL2);

      invokeEndpoint(publishURL1);
      invokeEndpoint(publishURL2);

      endpoint1.stop();
      endpoint2.stop();
   }

   private Endpoint publishEndpoint(EndpointBean epImpl, String publishURL)
   {
      Endpoint endpoint = Endpoint.create(SOAPBinding.SOAP11HTTP_BINDING, epImpl);
      endpoint.publish(publishURL);
      return endpoint;
   }

   private void invokeEndpoint(String publishURL) throws Exception
   {
      URL wsdlURL = new URL(publishURL + "?wsdl");
      QName qname = new QName("http://org.jboss.ws/jaxws/endpoint/", "EndpointService");
      Service service = Service.create(wsdlURL, qname);
      EndpointInterface port = (EndpointInterface)service.getPort(EndpointInterface.class);

      // Invoke the endpoint
      String helloWorld = "Hello world!";
      assertEquals(0, port.getCount());
      Object retObj = port.echo(helloWorld);
      assertEquals(helloWorld, retObj);
      assertEquals(1, port.getCount());
      port.echo(helloWorld);
      assertEquals(2, port.getCount());
   }
}
