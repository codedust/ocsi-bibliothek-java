package de.osci.osci12.signature;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import de.osci.osci12.common.DialogHandler;
import de.osci.osci12.encryption.EncryptedDataBuilder;
import de.osci.osci12.encryption.EncryptedKeyBuilder;


/**
 * Builder, der ein Element ds:KeyInfo bearbeitet.
 *
 * <p>Copyright © 2023 Governikus GmbH &amp; Co. KG, Germany</p>
 * <p>Erstellt von Governikus GmbH &amp; Co. KG</p>
 * <p>Diese Bibliothek kann von jedermann nach Maßgabe der European Union
 *  Public Licence genutzt werden.</p><p>Die Lizenzbestimmungen können
 * unter der URL <a href="https://eupl.eu/">https://eupl.eu/</a> abgerufen werden.
 *
 * @author PPI Financial Systems GmbH
 */
public class KeyInfoBuilder extends DefaultHandler
{
  DefaultHandler parentHandler = null;
  XMLReader xmlReader = null;
  private static Log log = LogFactory.getLog(KeyInfoBuilder.class);
  protected static final String DS_XMLNS = "http://www.w3.org/2000/09/xmldsig#";
  protected static final String XENC_XMLNS = "http://www.w3.org/2001/04/xmlenc#";
  /**
   * Wird gesetzt, sobald bekannt ist, dass eine Instanz
   * von KeyInfoX509 aufzubauen ist.
   */
  private StringBuffer currentElement = null;
  /**
   * X509DataBuilder.
   */
  private X509DataBuilder x509Builder = null;
  /**
   * MgmtData.
   */
  /**
   * ID.
   */
  private String keyId = "";
  /** Aufgebautes KeyInfo. */
  KeyInfo keyInfo = null;

  /**
   * Konstruktor.
   *
   * @param xmlReader
   * @param parentHandler
   * @param attributes
   */
  public KeyInfoBuilder(XMLReader xmlReader, DefaultHandler parentHandler, Attributes attributes)
  {
    this.xmlReader = xmlReader;
    this.parentHandler = parentHandler;
    keyInfo = new KeyInfo();

    if ((attributes != null) && (attributes.getValue("Id") != null))
    {
      keyId = attributes.getValue("Id");
      keyInfo.setId(keyId);
    }
  }

  /**
   * undocumented
   *
   * @param uri undocumented
   * @param localName undocumented
   * @param qName undocumented
   * @param attributes undocumented
   *
   * @throws SAXParseException undocumented
   * @throws SAXException undocumented
   */
  public void startElement(String uri, String localName, String qName, Attributes attributes)
                    throws SAXParseException,
                           SAXException
  {
    if (log.isDebugEnabled())
      log.debug("Start-Element: " + localName);

    if (localName.equals("KeyName") && uri.equals(DS_XMLNS))
      currentElement = new StringBuffer();
    else if (localName.equals("KeyValue") && uri.equals(DS_XMLNS))
      throw new SAXException(DialogHandler.text.getString("unsupported_entry") + ": " + localName);
    else if (localName.equals("RetrievalMethod") && uri.equals(DS_XMLNS))
    {
      RetrievalMethodBuilder retrievalMethodBuilder = new RetrievalMethodBuilder(xmlReader, this, attributes);
      xmlReader.setContentHandler(retrievalMethodBuilder);
    }
    else if (localName.equals("X509Data") && uri.equals(DS_XMLNS))
    {
      x509Builder = new X509DataBuilder(xmlReader, this, attributes);
      xmlReader.setContentHandler(x509Builder);
    }
    else if (localName.equals("PGPData") && uri.equals(DS_XMLNS))
      throw new SAXException(DialogHandler.text.getString("unsupported_entry") + ": " + localName);
    else if (localName.equals("SPKIData") && uri.equals(DS_XMLNS))
      throw new SAXException(DialogHandler.text.getString("unsupported_entry") + ": " + localName);
    else if (localName.equals("MgmtData") && uri.equals(DS_XMLNS))
    {
      currentElement = new StringBuffer();
    }
    else if (localName.equals("EncryptedKey") && uri.equals(XENC_XMLNS))
    {
      xmlReader.setContentHandler(new EncryptedKeyBuilder(xmlReader, this, attributes));
    }

    else if (localName.equals("AgreementMethod") && uri.equals(XENC_XMLNS))
      throw new SAXException(DialogHandler.text.getString("unsupported_entry") + ": " + localName);
    else
      throw new SAXException(DialogHandler.text.getString("unexpected_entry") + ": " + localName, null);
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
      log.debug("Start-Element :" + localName);

    if (localName.equals("KeyInfo") && uri.equals(DS_XMLNS))
    {
      if (parentHandler instanceof EncryptedDataBuilder)
        ((EncryptedDataBuilder) parentHandler).getEncryptedData().setKeyInfo(keyInfo);
      else if (parentHandler instanceof EncryptedKeyBuilder)
        ((EncryptedKeyBuilder) parentHandler).getEncKey().setKeyInfo(keyInfo);

      xmlReader.setContentHandler(parentHandler);
    }
    else if (localName.equals("KeyName") && uri.equals(DS_XMLNS))
      keyInfo.setKeyName(currentElement.toString());
    else if (localName.equals("RetrievalMethod") && uri.equals(DS_XMLNS))
    {
    }
    else if (localName.equals("X509Data") && uri.equals(DS_XMLNS))
    {
    }
    else if (localName.equals("MgmtData") && uri.equals(DS_XMLNS))
    {
      keyInfo.setMgmtData(currentElement.toString());
    }
    else if (localName.equals("EncryptedData") && uri.equals(XENC_XMLNS))
    {
    }
    else
      throw new SAXException(DialogHandler.text.getString("unexpected_entry") + ": " + localName);

    currentElement = null;
  }

  /**
   * Liefert das aufgebaute KeyInfo.
   *
   * @return KeyInfo.
   * @throws IllegalStateException Die Methode
   * {@link org.xml.sax.ContentHandler#endElement(String, String, String)}
   * wurde noch nicht aufgerufen.
   */
  public KeyInfo getKeyInfo()
  {
    return keyInfo;
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
    if (log.isDebugEnabled())
      log.debug("Character: " + new String(ch, start, length));

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
