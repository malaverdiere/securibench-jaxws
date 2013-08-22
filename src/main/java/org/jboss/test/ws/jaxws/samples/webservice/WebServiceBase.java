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
package org.jboss.test.ws.jaxws.samples.webservice;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.Assert.*;


/**
 * Base testing class for @WebService
 *
 * @author <a href="mailto:jason.greene@jboss.com">Jason T. Greene</a>
 */
public class WebServiceBase extends HttpServlet
{

   private EndpointInterface getPort(String endpointURI) throws MalformedURLException
   {
      QName serviceName = new QName("http://www.openuri.org/2004/04/HelloWorld", "EndpointService");
      URL wsdlURL = new URL("http://localhost:8080/" + endpointURI + "?wsdl");

      Service service = Service.create(wsdlURL, serviceName);
      return service.getPort(EndpointInterface.class);
   }

   private EndpointInterface03 getPort03(String endpointURI) throws MalformedURLException
   {
      QName serviceName = new QName("http://www.openuri.org/2004/04/HelloWorld", "EndpointService");
      URL wsdlURL = new URL("http://localhost:8080/" + endpointURI + "?wsdl");

      Service service = Service.create(wsdlURL, serviceName);
      return service.getPort(EndpointInterface03.class);
   }

    @Override
    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        try{
            webServiceTest("abc");
            webServiceWsdlLocationTest("abc");
            webServiceEndpointInterfaceTest("abc");
        } catch (Exception e){

        }
    }

    public void webServiceTest(String endpointURI) throws Exception
   {
      String helloWorld = "Hello world!";
      Object retObj = getPort(endpointURI).echo(helloWorld);
      assertEquals(helloWorld, retObj);
   }

   public void webServiceWsdlLocationTest(String endpointURI) throws Exception
   {
      String helloWorld = "Hello world!";
      Object retObj = getPort(endpointURI).echo(helloWorld);
      assertEquals(helloWorld, retObj);
   }

   public void webServiceEndpointInterfaceTest(String endpointURI) throws Exception
   {
      String helloWorld = "Hello Interface!";
      Object retObj = getPort03(endpointURI).echo(helloWorld);
      assertEquals(helloWorld, retObj);
   }
}
