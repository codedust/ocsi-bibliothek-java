package de.osci.osci12.signature;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import de.osci.helper.Base64InputStream;
import de.osci.osci12.common.Constants.LanguageTextEntries;
import de.osci.osci12.common.DialogHandler;


/**
 * Builder, der ein Element xdsig:X509Data bearbeitet.
 *
 * <p>Copyright © 2023 Governikus GmbH &amp; Co. KG, Germany</p>
 * <p>Erstellt von Governikus GmbH &amp; Co. KG</p>
 * <p>Diese Bibliothek kann von jedermann nach Maßgabe der European Union
 *  Public Licence genutzt werden. Die Lizenzbestimmungen können
 * unter der URL <a href="https://eupl.eu/">https://eupl.eu/</a> abgerufen werden.
 *
 * @author PPI Financial Systems GmbH
 * @version 2.4.1
 */
class X509DataBuilder extends DefaultHandler
{
  DefaultHandler parentHandler = null;
  private StringBuffer currentElement = null;
  private X509Data x509Data;
  XMLReader xmlReader = null;
  protected static Log log = LogFactory.getLog(X509DataBuilder.class);
  private final StringBuffer buffer = new StringBuffer();
  protected static final String DS_XMLNS = "http://www.w3.org/2000/09/xmldsig#";
  protected static final String XENC_XMLNS = "http://www.w3.org/2001/04/xmlenc#";

  /**
   * Creates a new X509DataBuilder object.
   *
   * @param xmlReader undocumented
   * @param parentHandler undocumented
   * @param attributes undocumented
   */
  public X509DataBuilder(XMLReader xmlReader, DefaultHandler parentHandler, Attributes attributes)
  {
    this.xmlReader = xmlReader;
    this.parentHandler = parentHandler;

    if (attributes == null)
      throw new IllegalArgumentException(de.osci.osci12.common.DialogHandler.text.getString(LanguageTextEntries.invalid_thirdargument.name()) +
                                         " null");
  }

  /**
   * undocumented
   *
   * @param uri undocumented
   * @param localName undocumented
   * @param qName undocumented
   * @param attributes undocumented
   *
   * @throws SAXException undocumented
   */
  public void startElement(String uri, String localName, String qName, Attributes attributes)
                    throws SAXException
  {
    if (log.isDebugEnabled())
      log.debug("Start-Element: " + localName + "localName: " + localName);

    if (localName.equals("X509Certificate") && uri.equals(DS_XMLNS))
      currentElement = new StringBuffer();
    else if (localName.equals("X509IssuerSerial") && uri.equals(DS_XMLNS))
      throw new SAXException(DialogHandler.text.getString("unsupported_entry") + ": " + localName);
    else if (localName.equals("X509SKI") && uri.equals(DS_XMLNS))
      currentElement = new StringBuffer();
    else if (localName.equals("X509SubjectName") && uri.equals(DS_XMLNS))
      currentElement = new StringBuffer();
    else if (localName.equals("X509CRL") && uri.equals(DS_XMLNS))
      currentElement = new StringBuffer();
    else
      throw new SAXException(de.osci.osci12.common.DialogHandler.text.getString("unexpected_entry") + ": " + localName);
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

    try
    {
      if (localName.equals("X509Data") && uri.equals(DS_XMLNS))
      {
        if (parentHandler instanceof KeyInfoBuilder)
        {
          ((KeyInfoBuilder) parentHandler).getKeyInfo().x509Data = x509Data;

          if (log.isDebugEnabled())
            log.debug("Parent ist KeyInfo");
        }

        xmlReader.setContentHandler(parentHandler);
      }
      else if (localName.equals("X509Certificate") && uri.equals(DS_XMLNS))
      {
        java.io.ByteArrayInputStream bytesStream = new java.io.ByteArrayInputStream(currentElement.toString().getBytes(de.osci.osci12.common.Constants.CHAR_ENCODING));
        Base64InputStream base64In = new Base64InputStream(bytesStream);
        x509Data = new X509Data(de.osci.helper.Tools.createCertificate(base64In));
      }
      else if (localName.equals("X509IssuerSerial") && uri.equals(DS_XMLNS))
      {
        throw new SAXException(DialogHandler.text.getString("unsupported_entry") + ": " + localName);
      }
      else if (localName.equals("X509SKI") && uri.equals(DS_XMLNS))
      {
        x509Data.setX509SKI(currentElement.toString());
      }
      else if (localName.equals("X509SubjectName") && uri.equals(DS_XMLNS))
      {
        x509Data.setX509SubjectName(currentElement.toString());
      }
      else if (localName.equals("X509CRL") && uri.equals(DS_XMLNS))
      {
        x509Data.setX509CRL(currentElement.toString());
      }
      else
      {
        throw new SAXException(de.osci.osci12.common.DialogHandler.text.getString("unexpected_entry") + ": " +
                               localName);
      }
    }
    catch (SAXException ex)
    {
      throw ex;
    }
    catch (Exception ex)
    {
      log.error("Fehler im End-Element X509Data.", ex);
      throw new SAXException(de.osci.osci12.common.DialogHandler.text.getString("sax_exception"), ex);
    }

    currentElement = null;
  }

  /**
   * Liefert den Inhalt des inneren Elements xdsig:X509Certificate.
   *
   * @return StringBuffer.
   */
  public StringBuffer getX509DataBuffer()
  {
    return buffer;
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
