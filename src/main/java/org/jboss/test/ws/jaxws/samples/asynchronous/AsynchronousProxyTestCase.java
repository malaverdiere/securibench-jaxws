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
package org.jboss.test.ws.jaxws.samples.asynchronous;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Response;
import javax.xml.ws.Service;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;


import static org.junit.Assert.*;


/**
 * Test JAXWS asynchrous proxy
 *
 * @author Thomas.Diesler@jboss.org
 * @since 12-Aug-2006
 */
@WebServlet(name="asynchronous/AsynchronousProxyTestCase", value="asynchronous/AsynchronousProxyTestCase")
public class AsynchronousProxyTestCase extends HttpServlet
{
   private String targetNS = "http://org.jboss.ws/jaxws/asynchronous";
   private Exception handlerException;
   private boolean asyncHandlerCalled;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        final String param = req.getParameter("abc");

        try {
            testInvokeAsync(param);
            testInvokeSync(param);
            testInvokeAsyncHandler(param);
        } catch (Exception e) {
            fail("Exception occured: "+ e.getMessage());
        }
    }

    public void testInvokeSync(final String param) throws Exception
   {
      Endpoint port = createProxy();
      String retStr = port.echo(param);
      assertEquals(param, retStr);
   }

   public void testInvokeAsync(final String param) throws Exception
   {
      Endpoint port = createProxy();
      Response response = port.echoAsync(param);

      // access future
      String retStr = (String) response.get();
      assertEquals(param, retStr);
   }

   public void testInvokeAsyncHandler(final String param) throws Exception
   {
      AsyncHandler<String> handler = new AsyncHandler<String>()
      {
         public void handleResponse(Response response)
         {
            try
            {
               String retStr = (String) response.get(5000, TimeUnit.MILLISECONDS);
               assertEquals(param, retStr);
               asyncHandlerCalled = true;
            }
            catch (Exception ex)
            {
               handlerException = ex;
            }
         }
      };

      Endpoint port = createProxy();
      Future future = port.echoAsync(param, handler);
      future.get(5000, TimeUnit.MILLISECONDS);

      if (handlerException != null)
         throw handlerException;

      assertTrue("Async handler called", asyncHandlerCalled);
   }

   private Endpoint createProxy() throws MalformedURLException
   {
      URL wsdlURL = new URL("http://localhost:8080/jaxws-samples-asynchronous?wsdl");
      QName serviceName = new QName(targetNS, "EndpointBeanService");
      Service service = Service.create(wsdlURL, serviceName);
      return (Endpoint)service.getPort(Endpoint.class);
   }
}
