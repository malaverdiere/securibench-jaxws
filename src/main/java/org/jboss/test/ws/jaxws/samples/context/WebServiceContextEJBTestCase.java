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
package org.jboss.test.ws.jaxws.samples.context;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.*;


/**
 * Test JAXWS WebServiceContext
 *
 * @author Thomas.Diesler@jboss.org
 * @since 29-Apr-2005
 */
@WebServlet(name="context/WebServiceContextEJBTestCase", value="context/WebServiceContextEJBTestCase")
public class WebServiceContextEJBTestCase extends HttpServlet
{
   private Endpoint port;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            setUp();
            testGetUserPrincipal();
            testGetWebContext();
            testIsUserInRole();
            testMessageContextProperties();
        } catch (Exception e) {
            fail("Exception raised: " + e.getMessage());
        }
    }

    public void setUp() throws Exception
   {
      if (port == null)
      {
         URL wsdlURL = new URL("http://localhost:8080/jaxws-samples-context?wsdl");
         QName qname = new QName("http://org.jboss.ws/jaxws/context", "EndpointService");
         Service service = Service.create(wsdlURL, qname);
         port = (Endpoint)service.getPort(Endpoint.class);

         BindingProvider bp = (BindingProvider)port;
         bp.getRequestContext().put(BindingProvider.USERNAME_PROPERTY, "kermit");
         bp.getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, "thefrog");
      }
   }

   public void testGetWebContext() throws Exception
   {
      String retStr = port.testGetMessageContext();
      assertEquals("pass", retStr);
   }

   public void testMessageContextProperties() throws Exception
   {
      String retStr = port.testMessageContextProperties();
      assertEquals("pass", retStr);
   }
   
   public void testGetUserPrincipal() throws Exception
   {
      String retStr = port.testGetUserPrincipal();
      assertEquals("kermit", retStr);
   }

   public void testIsUserInRole() throws Exception
   {
      assertTrue("kermit is my friend", port.testIsUserInRole("friend"));
   }
}
