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
package org.jboss.test.ws.jaxws.samples.provider;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.Service.Mode;
import javax.xml.ws.soap.SOAPBinding;

import junit.framework.Test;




import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Test a Provider<SOAPMessage>
 *
 * @author Thomas.Diesler@jboss.org
 * @since 29-Jun-2006
 */
public class ProviderPayloadTestCase extends JBossWSTest
{
   private String reqString =
      "<ns1:somePayload xmlns:ns1='http://org.jboss.ws/provider'>Hello</ns1:somePayload>";

   private String resString =
      "<ns1:somePayload xmlns:ns1='http://org.jboss.ws/provider'>Hello:Inbound:LogicalSourceHandler:Outbound:LogicalSourceHandler</ns1:somePayload>";

   public static Test suite()
   {
      return new JBossWSTestSetup(ProviderPayloadTestCase.class, "jaxws-samples-provider-payload.war");
   }

   public void testProviderMessage() throws Exception
   {
      String reqEnvStr =
         "<env:Envelope xmlns:env='http://schemas.xmlsoap.org/soap/envelope/'>" +
         "  <env:Body>" + reqString + "</env:Body>" +
         "</env:Envelope>";

      MessageFactory msgFactory = MessageFactory.newInstance();
      SOAPConnection con = SOAPConnectionFactory.newInstance().createConnection();
      SOAPMessage reqMsg = msgFactory.createMessage(null, new ByteArrayInputStream(reqEnvStr.getBytes()));

      URL epURL = new URL("http://" + getServerHost() + ":8080/jaxws-samples-provider-payload");
      SOAPMessage resMsg = con.call(reqMsg, epURL);
      SOAPEnvelope resEnv = resMsg.getSOAPPart().getEnvelope();

      SOAPHeader soapHeader = resEnv.getHeader();
      if (soapHeader != null)
         soapHeader.detachNode();

      Node responseBody = DOMUtils.getFirstChildElement(resEnv.getBody());
      assertEquals("wrong namespace: " + responseBody.getNamespaceURI(), "http://org.jboss.ws/provider", responseBody.getNamespaceURI());
      assertEquals("wrong localPart: " + responseBody.getLocalName(), "somePayload", responseBody.getLocalName());
      String responseString = DOMUtils.getTextContent(responseBody);
      assertEquals("wrong content: " + responseString, "Hello:Inbound:LogicalSourceHandler:Outbound:LogicalSourceHandler", responseString);
   }

   private Dispatch<Source> createDispatch(String target) throws MalformedURLException, JAXBException
   {
      String targetNS = "http://org.jboss.ws/provider";
      QName serviceName = new QName(targetNS, "ProviderService");
      QName portName = new QName(targetNS, "ProviderPort");
      URL endpointAddress = new URL("http://" + getServerHost() + ":8080/jaxws-samples-provider-payload/" + target);

      Service service = Service.create(serviceName);
      service.addPort(portName, SOAPBinding.SOAP11HTTP_BINDING, endpointAddress.toExternalForm());
      
      Dispatch<Source> dispatch = service.createDispatch(portName, Source.class, Mode.PAYLOAD);
      return dispatch;
   }
}
