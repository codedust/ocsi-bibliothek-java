package de.osci.osci12.messagetypes;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import de.osci.osci12.common.DialogHandler;


/**
 * StoredEnvelope-Parser.
 *
 * <p>Copyright © 2023 Governikus GmbH &amp; Co. KG, Germany</p>
 * <p>Erstellt von Governikus GmbH &amp; Co. KG</p>
 * <p>Diese Bibliothek kann von jedermann nach Maßgabe der European Union
 *  Public Licence genutzt werden.</p><p>Die Lizenzbestimmungen können
 * unter der URL <a href="https://eupl.eu/">https://eupl.eu/</a> abgerufen werden.
 *
 *
 * @author R. Lindemann / N. Büngener
 * @version 2.4.1
 */
class StoredEnvelopeBuilder extends OSCIEnvelopeBuilder
{
  private static Log log = LogFactory.getLog(StoredEnvelopeBuilder.class);

  /**
   * Creates a new StoredEnvelopeBuilder object.
   *
   * @param xmlReader undocumented
   */
  public StoredEnvelopeBuilder(XMLReader xmlReader)
  {
    super(xmlReader, null);
  }

  /**
   *  Description of the Method
   *
   *@param  uri                           Description of Parameter
   *@param  localName                     Description of Parameter
   *@param  qName                         Description of Parameter
   *@param  attributes                    Description of Parameter
   *@exception  SAXException  Description of Exception
   */
  public void startElement(String uri, String localName, String qName, Attributes attributes)
                    throws SAXException
  {
    if (log.isDebugEnabled())
      log.debug("Start-Element: " + localName);

    // SOAP-Namespacedefinition suchen
    if ((soapNSPrefix == null) || !localName.equals("Envelope"))
      throw new SAXException(DialogHandler.text.getString("msg_format_error") + ": " + localName);

    String schemaLocation = attributes.getValue("xsi:schemaLocation");

    if (log.isDebugEnabled())
      log.debug("Nachrichtentyp: " + schemaLocation);

    if (schemaLocation == null)
    {
      childBuilder = new SOAPFaultBuilder(this);
      this.xmlReader.setContentHandler(childBuilder);
    }
    else if (schemaLocation.equals(XSD_ENCRYPTED_DATA))
    {
      throw new SAXException(new IllegalArgumentException(DialogHandler.text.getString("encrypted_message")));
    }
    else
    {
      int msgType;

      if (schemaLocation.equals(XSD_STORE_DELIVERY))
        msgType = OSCIMessage.STORE_DELIVERY;
      else if (schemaLocation.equals(XSD_RSP_STORE_DELIVERY))
        msgType = OSCIMessage.RESPONSE_TO_STORE_DELIVERY;
      else if (schemaLocation.equals(XSD_FETCH_DELIVERY))
        msgType = OSCIMessage.FETCH_DELIVERY;
      else if (schemaLocation.equals(XSD_RSP_FETCH_DELIVERY))
        msgType = OSCIMessage.RESPONSE_TO_FETCH_DELIVERY;
      else if (schemaLocation.equals(XSD_FORWARD_DELIVERY))
        msgType = OSCIMessage.FORWARD_DELIVERY;
      else if (schemaLocation.equals(XSD_ACCEPT_DELIVERY))
        msgType = OSCIMessage.ACCEPT_DELIVERY;
      else if (schemaLocation.equals(XSD_RSP_ACCEPT_DELIVERY))
        msgType = OSCIMessage.RESPONSE_TO_ACCEPT_DELIVERY;
      else if (schemaLocation.equals(XSD_RSP_FORWARD_DELIVERY))
        msgType = OSCIMessage.RESPONSE_TO_FORWARD_DELIVERY;
      else if (schemaLocation.equals(XSD_MEDIATE_DELIVERY))
        msgType = OSCIMessage.MEDIATE_DELIVERY;
      else if (schemaLocation.equals(XSD_PROCESS_DELIVERY))
        msgType = OSCIMessage.PROCESS_DELIVERY;
      else if (schemaLocation.equals(XSD_RSP_PROCESS_DELIVERY))
        msgType = OSCIMessage.RESPONSE_TO_PROCESS_DELIVERY;
      else if (schemaLocation.equals(XSD_RSP_MEDIATE_DELIVERY))
        msgType = OSCIMessage.RESPONSE_TO_MEDIATE_DELIVERY;
      else if (schemaLocation.equals(XSD_FETCH_PROCESS_CARD))
        msgType = OSCIMessage.FETCH_PROCESS_CARD;
      else if (schemaLocation.equals(XSD_RSP_FETCH_PROCESS_CARD))
        msgType = OSCIMessage.RESPONSE_TO_FETCH_PROCESS_CARD;
      else if (schemaLocation.equals(XSD_PARTIAL_STORE_DELIVERY))
        msgType = OSCIMessage.PARTIAL_STORE_DELIVERY;
      else if (schemaLocation.equals(XSD_PARTIAL_FETCH_DELIVERY))
        msgType = OSCIMessage.PARTIAL_FETCH_DELIVERY;
      else if (schemaLocation.equals(XSD_RSP_PARTIAL_STORE_DELIVERY))
        msgType = OSCIMessage.RESPONSE_TO_PARTIAL_STORE_DELIVERY;
      else if (schemaLocation.equals(XSD_RSP_PARTIAL_FETCH_DELIVERY))
        msgType = OSCIMessage.RESPONSE_TO_PARTIAL_FETCH_DELIVERY;
      else
      {
        log.error("Nicht erlaubter Nachrichtentyp ! Nachrichtentypt ist:\n" + schemaLocation + "\n" +
                  (XSD_RSP_FETCH_PROCESS_CARD));
        throw new SAXException(DialogHandler.text.getString("sax_exception_msgtype"));
      }

      childBuilder = new StoredMessageBuilder(this, msgType);
      this.xmlReader.setContentHandler(childBuilder);
    }

    childBuilder.getOSCIMessage().soapNSPrefix = soapNSPrefix;
    childBuilder.getOSCIMessage().osciNSPrefix = osciNSPrefix;
    childBuilder.getOSCIMessage().osci2017NSPrefix = osci2017NSPrefix;
    childBuilder.getOSCIMessage().osci2019NSPrefix = osci2019NSPrefix;
    childBuilder.getOSCIMessage().dsNSPrefix = dsNSPrefix;
    childBuilder.getOSCIMessage().xencNSPrefix = xencNSPrefix;
    childBuilder.getOSCIMessage().ns = ns.toString();
  }
}
