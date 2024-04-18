package de.osci.osci12.encryption;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import de.osci.osci12.common.DialogHandler;
import de.osci.osci12.signature.KeyInfoBuilder;


/**
 * <p>Copyright © 2023 Governikus GmbH &amp; Co. KG, Germany</p>
 * <p>Erstellt von Governikus GmbH &amp; Co. KG</p>
 * <p>Diese Bibliothek kann von jedermann nach Maßgabe der European Union
 *  Public Licence genutzt werden.</p>
 * <p>
 * Die Lizenzbestimmungen können unter der URL <a href="https://eupl.eu/">https://eupl.eu/</a> abgerufen werden.
 *
 *
 * @author R. Lindemann, N.Büngener
 * @version 2.4.1
 *
 */
public class EncryptedKeyBuilder extends DefaultHandler
{
  private static Log log = LogFactory.getLog(EncryptedKeyBuilder.class);
  EncryptedKey encKey = null;
  private Vector<String> transformer = new Vector<String>();
  private CipherReference cipherRef = null;
  private CipherValue cipherValue = null;
  private CipherData cipherData = null;
  private StringBuffer currentElement = null;
  private de.osci.osci12.signature.KeyInfoBuilder parentHandler = null;
  private XMLReader xmlReader = null;
  protected static final String DS_XMLNS = "http://www.w3.org/2000/09/xmldsig#";
  protected static final String XENC_XMLNS = "http://www.w3.org/2001/04/xmlenc#";
  protected static final String XENC11_XMLNS = "http://www.w3.org/2009/xmlenc11#";

  /**
   * Creates a new EncryptedKeyBuilder object.
   *
   * @param xmlReader undocumented
   * @param parentHandler undocumented
   * @param attributes undocumented
   *
   * @throws SAXException undocumented
   */
  public EncryptedKeyBuilder(XMLReader xmlReader, DefaultHandler parentHandler, Attributes attributes)
                      throws SAXException
  {
    if (parentHandler instanceof KeyInfoBuilder)
      this.parentHandler = ((KeyInfoBuilder) parentHandler);
    else
      throw new SAXException(DialogHandler.text.getString("sax_exception"));

    this.xmlReader = xmlReader;
    encKey = new EncryptedKey();

    if (attributes != null)
    {
      if (attributes.getValue("Recipient") != null)
        encKey.setRecipient(attributes.getValue("Recipient"));

      if (attributes.getValue("Id") != null)
        encKey.setId(attributes.getValue("Id"));

      if (attributes.getValue("MimeType") != null)
        encKey.setMimeType(attributes.getValue("MimeType"));

      if (attributes.getValue("Encoding") != null)
        encKey.setEncoding(attributes.getValue("Encoding"));
    }
  }

  /**
   * undocumented
   *
   * @param prefix undocumented
   * @param uri undocumented
   */
  public void startPrefixMapping(String prefix, String uri)
  {
    if (uri.equals(XENC11_XMLNS))
       encKey.xenc11NSPrefix = prefix;
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
      log.debug("Start-Element: " + localName);

    if (localName.equals("EncryptedKey") && uri.equals(XENC_XMLNS))
      ; //nothing to do
    else if (localName.equals("KeyInfo") && uri.equals(DS_XMLNS))
    {
      xmlReader.setContentHandler(new KeyInfoBuilder(xmlReader, this, attributes));
    }
    else if (localName.equals("EncryptionMethod") && uri.equals(XENC_XMLNS))
        encKey.setEncryptionMethodAlgorithm(attributes.getValue("Algorithm"));
    else if (localName.equals("MGF") && uri.equals(XENC11_XMLNS))
        encKey.mgfAlgorithm = attributes.getValue("Algorithm");
    else if (localName.equals("DigestMethod") && uri.equals(DS_XMLNS))
        encKey.digestAlgorithm = attributes.getValue("Algorithm");
    else if (localName.equals("CipherData") && uri.equals(XENC_XMLNS))
      ; //nothing to do
    else if (localName.equals("CipherValue") && uri.equals(XENC_XMLNS))
    {
      currentElement = new StringBuffer();
    }
    else if (localName.equals("CipherReference") && uri.equals(XENC_XMLNS))
    {
      try
      {
        String cp = attributes.getValue("URI");

        //        if (cp.startsWith("#"))
        //          cp = cp.substring(1);
        cipherRef = new CipherReference(cp);
      }
      catch (IOException ex)
      {
        throw new SAXException(ex);
      }
    }
    else if (localName.equals("Transforms") && uri.equals(XENC_XMLNS))
      ; //nothing to do
    else if (localName.equals("Transform") && uri.equals(DS_XMLNS))
    {
      if (attributes.getValue("Algorithm") == null)
        throw new SAXException(DialogHandler.text.getString("sax_exception"));

      transformer.add(attributes.getValue("Algorithm"));
    }
    else if (localName.equals("EncryptionProperties") && uri.equals(XENC_XMLNS))
      throw new SAXException(de.osci.osci12.common.DialogHandler.text.getString("sax_exception"));
    else if (localName.equals("RefernceList") && uri.equals(XENC_XMLNS))
      throw new SAXException(DialogHandler.text.getString("sax_exception"));
    else if (localName.equals("CarriedKeyName") && uri.equals(XENC_XMLNS))
      currentElement = new StringBuffer();
    else
      throw new SAXException(DialogHandler.text.getString("unexpected_entry") + ": " + localName);
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
      if (localName.equals("EncryptedKey") && uri.equals(XENC_XMLNS))
      {
        parentHandler.getKeyInfo().addEncryptedKey(encKey);
        xmlReader.setContentHandler(parentHandler);
      }
      else if (localName.equals("KeyInfo") && uri.equals(DS_XMLNS))
      {
        ;
      }
      else if (localName.equals("CipherData") && uri.equals(XENC_XMLNS))
      {
        if (cipherRef != null)
          cipherData = new CipherData(cipherRef);
        else
        {
          cipherData = new CipherData(cipherValue);
        }

        encKey.setCipherData(cipherData);
      }
      else if (localName.equals("CipherReference") && uri.equals(XENC_XMLNS))
      {
        Enumeration<String> transformers = transformer.elements();

        while (transformers.hasMoreElements())
        {
          cipherRef.addTransform(transformers.nextElement());
        }

        transformer.clear();
      }
      else if (localName.equals("Transforms") && uri.equals(XENC_XMLNS))
        ; //nothing to do
      else if (localName.equals("Transform") && uri.equals(DS_XMLNS))
        ; //nothing to do
      else if (localName.equals("CipherValue") && uri.equals(XENC_XMLNS))
      {
        cipherValue = new CipherValue(currentElement.toString());
      }
      else if (localName.equals("EncryptionMethod") && uri.equals(XENC_XMLNS))
        ; // not supported
      else if (localName.equals("MGF") && uri.equals(XENC11_XMLNS))
        ; // not supported
      else if (localName.equals("DigestMethod") && uri.equals(DS_XMLNS))
        ; // not supported
      else if (localName.equals("EncryptionProperties") && uri.equals(XENC_XMLNS))
        ; // not supported
      else if (localName.equals("RefernceList") && uri.equals(XENC_XMLNS))
        ; // not supported
      else if (localName.equals("CarriedKeyName") && uri.equals(XENC_XMLNS))
        encKey.setCarriedKeyName(currentElement.toString());
      else
      {
        throw new SAXException(DialogHandler.text.getString("unexpected_entry") + ": " + localName);
      }
    }
    catch (SAXException ex)
    {
      throw ex;
    }
    catch (Exception ex)
    {
      log.error("Fehler im Encrypted-Key Builder.", ex);
      throw new SAXException(ex);
    }

    currentElement = null;
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

  /**
   * undocumented
   *
   * @return undocumented
   */
  public EncryptedKey getEncKey()
  {
    return encKey;
  }
}
