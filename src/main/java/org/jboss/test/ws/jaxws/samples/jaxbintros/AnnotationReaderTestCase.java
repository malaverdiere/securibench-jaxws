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
package org.jboss.test.ws.jaxws.samples.jaxbintros;

import javax.servlet.http.HttpServlet;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.Map;


import static org.junit.Assert.*;

/**
 * @author Heiko.Braun@jboss.com
 * @author alessio.soldano@jboss.com
 */
public class AnnotationReaderTestCase extends HttpServlet
{
   public void testUnmarshal() throws Exception
   {
      String reqString = 
         "   <ns1:user xmlns:ns1='http://org.jboss.ws/provider' string='Kermit'>" + 
         "      <qname>The Frog</qname>" + 
         "    </ns1:user>";

      Map<String, Object> jaxbConfig = Collections.EMPTY_MAP;
      
      JAXBContext jaxbContext = JAXBContext.newInstance(new Class[] { UserType.class }, jaxbConfig);
      Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

      JAXBElement jbe = unmarshaller.unmarshal(new StreamSource(new ByteArrayInputStream(reqString.getBytes())

      ), UserType.class);

      UserType ut = (UserType)jbe.getValue();
      assertEquals("Kermit", ut.getString());
      assertEquals("The Frog", ut.getQname().getLocalPart());

   }
}
