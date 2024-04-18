package de.osci.osci12.messageparts;

import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import de.osci.osci12.common.Constants;
import de.osci.osci12.common.DialogHandler;
import de.osci.osci12.messagetypes.OSCIMessage;
import de.osci.osci12.messagetypes.OSCIMessageBuilder;


/**
 * Interne Klasse, wird von Anwendungen nicht direkt benötigt.
 *
 * <p>Copyright © 2023 Governikus GmbH &amp; Co. KG, Germany</p>
 * <p>Erstellt von Governikus GmbH &amp; Co. KG</p>
 * <p>Diese Bibliothek kann von jedermann nach Maßgabe der European Union
 *  Public Licence genutzt werden.</p><p>Die Lizenzbestimmungen können
 * unter der URL <a href="https://eupl.eu/">https://eupl.eu/</a> abgerufen werden.
 *
 *
 * @author R. Lindemann, N. Büngener
 * @version 2.4.1
 */
public class MessagePartParser extends DefaultHandler
{
  //  private static Log log = LogFactory.getLog(MessagePartParser.class);
  protected DefaultHandler parentHandler = null;
  protected XMLReader xmlReader = null;
  protected StringBuffer currentElement = null;
  protected OSCIMessage msg = null;
  protected static final String SOAP_XMLNS = Constants.Namespaces.SOAP.getUri();
  protected static final String OSCI_XMLNS = Constants.Namespaces.OSCI.getUri();
  protected static final String OSCI2019_XMLNS = Constants.Namespaces.OSCI128.getUri();
  protected static final String DS_XMLNS = Constants.Namespaces.XML_SIG.getUri();
  protected static final String XENC_XMLNS = Constants.Namespaces.XML_ENC.getUri();
  protected static final String XSI_XMLNS = Constants.Namespaces.XML_SCHEMA.getUri();
  protected static final String XADES_XMLNS = "http://uri.etsi.org/01903/v1.3.2#";

  /**
   * Creates a new MessagePartParser object.
   *
   * @param parentHandler undocumented
   */
  protected MessagePartParser(OSCIMessageBuilder parentHandler)
  {
    this.parentHandler = parentHandler;
    msg = parentHandler.getOSCIMessage();
    this.xmlReader = parentHandler.getXMLReader();
  }

  /**
   * Creates a new MessagePartParser object.
   *
   * @param xmlReader undocumented
   * @param parentHandler undocumented
   */
  protected MessagePartParser(XMLReader xmlReader, DefaultHandler parentHandler)
  {
    this.parentHandler = parentHandler;
    this.xmlReader = xmlReader;
  }

  /**
   * undocumented
   *
   * @param ch undocumented
   * @param start undocumented
   * @param length undocumented
   *
   * @throws SAXException undocumented
   */
  public void characters(char[] ch, int start, int length)
                  throws SAXException
  {
    if (currentElement == null)
    {
      for (int i = 0; i < length; i++)
      {
        if (ch[start + i] > ' ')
          throw new SAXException(DialogHandler.text.getString("unexpected_char"));
      }
    }
    else
      currentElement.append(ch, start, length);
  }
}
