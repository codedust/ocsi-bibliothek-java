package de.osci.osci12.messagetypes;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.osci.osci12.common.DialogHandler;
import de.osci.osci12.encryption.EncryptedDataBuilder;


/**
 * SOAPMessageEncrypted-Parser.
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
class SOAPMessageEncryptedBuilder extends OSCIMessageBuilder
{
  private static Log log = LogFactory.getLog(SOAPMessageEncryptedBuilder.class);
  private EncryptedDataBuilder edb;

  /**
   *  Constructor for the SOAPMessageEncryptedBuilder object
   *
   *@param  eb Description of Parameter
   */
  public SOAPMessageEncryptedBuilder(OSCIEnvelopeBuilder eb)
                              throws java.security.NoSuchAlgorithmException
  {
    super(eb);

    if (log.isDebugEnabled())
      log.debug("Konstruktor");

    msg = new SOAPMessageEncrypted(null, null);
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

    if (localName.equals("EncryptedData") && uri.equals(XENC_XMLNS))
    {
      edb = new EncryptedDataBuilder(parentBuilder.xmlReader, this, attributes);
      parentBuilder.xmlReader.setContentHandler(edb);
    }
    else if (localName.equals("Body") && uri.equals(SOAP_XMLNS))
    {
      //nothing to do
    }
    else
      throw new SAXException(DialogHandler.text.getString("unexpected_entry") + ": " + localName);
  }

  /**
   *  Description of the Method
   */
  public void endDocument()
  {
    if (log.isDebugEnabled())
      log.debug("End-Document SOAP");

    ((SOAPMessageEncrypted) msg).encData = edb.getEncryptedData();
  }

  /**
   * undocumented
   *
   * @param uri undocumented
   * @param localName undocumented
   * @param qName undocumented
   *
   * @throws SAXException undocumented
   */
  public void endElement(String uri, String localName, String qName)
                  throws SAXException
  {
    if (log.isDebugEnabled())
      log.debug("End-Element: " + localName);
  }
}
