/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.samples.webservicerefsec;

import javax.servlet.http.HttpServlet;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import static org.junit.Assert.*;

/**
 * Test multiple webserviceref fro the same endpoint with different security credentials
 *
 * @author alessio.soldano@jboss.com
 * @since 12-May-2010
 */
public class WebServiceRefSecTestCase extends HttpServlet
{
   public final String TARGET_ENDPOINT_ADDRESS = "http://localhost:8080/jaxws-samples-webservicerefsec";


   public void testServletClient() throws Exception
   {

     URL url = new URL(TARGET_ENDPOINT_ADDRESS + "-servlet-client?echo=HelloWorld");
     BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
     String retStr = br.readLine();
     assertEquals("HelloWorld", retStr);

   }
}
