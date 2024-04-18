package de.osci.osci12.encryption;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Enumeration;
import java.util.Vector;

import de.osci.helper.ParserHelper;
import de.osci.osci12.soapheader.NonIntermediaryCertificatesH;
import de.osci.osci12.soapheader.NonIntermediaryCertificatesHBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import de.osci.osci12.common.Constants;
import de.osci.osci12.common.DialogHandler;
import de.osci.osci12.common.Constants.LanguageTextEntries;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;


/**
 * <p>Copyright © 2023 Governikus GmbH &amp; Co. KG, Germany</p>
 *
 * <p>Erstellt von Governikus GmbH &amp; Co. KG</p>
 * <p>Diese Bibliothek kann von jedermann nach Maßgabe der European Union
 *  Public Licence genutzt werden.</p>
 * <p>
 * Die Lizenzbestimmungen können unter der URL <a href="https://eupl.eu/">https://eupl.eu/</a> abgerufen werden.
 *
 *
 * @author R. Lindemann, N.Büngener
 * @version 2.4.1
 */
public class EncryptedDataBuilder extends DefaultHandler
{
  private static Log log = LogFactory.getLog(EncryptedDataBuilder.class);
  DefaultHandler parentHandler;
  private Vector<String> transformer = new Vector<String>();
  XMLReader xmlReader;
  private EncryptedData encrytedDataObject;
  private String edId = "";
  private String cipherRefId = "";
  private OutputStreamWriter valueWriter;
  private CipherValue cv;
  protected static final String DS_XMLNS = Constants.Namespaces.XML_SIG.getUri();
  protected static final String XENC_XMLNS = Constants.Namespaces.XML_ENC.getUri();

  /**
   * Creates a new EncryptedDataBuilder object.
   *
   * @param xmlReader undocumented
   * @param parentHandler undocumented
   * @param attributes undocumented
   */
  public EncryptedDataBuilder(XMLReader xmlReader, DefaultHandler parentHandler, Attributes attributes)
  {
    this.xmlReader = xmlReader;
    this.parentHandler = parentHandler;
    encrytedDataObject = new EncryptedData("");
   
    if ((attributes != null) && (attributes.getValue("Id") != null))
      encrytedDataObject.setId(attributes.getValue("Id"));
  }

  /**
   * For parsing without surrounding message: creates a new EncryptedDataBuilder object without parent handler
   *
   * @param xmlReader
   */
  public EncryptedDataBuilder(XMLReader xmlReader)
  {
    this.xmlReader = xmlReader;
    encrytedDataObject = new EncryptedData("");
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
    try
    {
      if (log.isDebugEnabled())
        log.debug("Start-Element: " + localName);

      if (localName.equals("EncryptedData") && uri.equals(XENC_XMLNS))
      {
        if (log.isDebugEnabled())
          log.debug("encryptedData");

        if (encrytedDataObject == null)
          encrytedDataObject = new EncryptedData(edId);

        if (attributes.getValue("Id") != null)
          encrytedDataObject.setId(attributes.getValue("Id"));
      }
      else if (localName.equals("EncryptionMethod") && uri.equals(XENC_XMLNS))
      {
        encrytedDataObject.setEncryptionMethodAlgorithm(attributes.getValue("Algorithm"));
      }
      else if (localName.equals("IvLength") && uri.equals(Constants.Namespaces.OSCI128.getUri()))
      {       
        encrytedDataObject.setIvLength(Integer.valueOf(attributes.getValue("Value")));
        encrytedDataObject.setIvLengthParsed(true);
      }
      else if (localName.equals("KeyInfo") && uri.equals(DS_XMLNS))
      {
        DefaultHandler childBuilder = new de.osci.osci12.signature.KeyInfoBuilder(xmlReader, this, attributes);
        xmlReader.setContentHandler(childBuilder);
      }
      else if (localName.equals("CipherData") && uri.equals(XENC_XMLNS))
        ; //nothing to do
      else if (localName.equals("CipherReference") && uri.equals(XENC_XMLNS))
      {
        cipherRefId = attributes.getValue("URI");
      }
      else if (localName.equals("Transforms") && uri.equals(XENC_XMLNS))
        ; // nothing to do
      else if (localName.equals("Transform") && uri.equals(DS_XMLNS))
        transformer.add(attributes.getValue("Algorithm"));
      else if (localName.equals("CipherValue") && uri.equals(XENC_XMLNS))
      {
        cv = new CipherValue();
        valueWriter = cv.getStreamToWrite();
      }
      else
        throw new SAXException(DialogHandler.text.getString("unexpected_entry") + ": " + localName);
    }
    catch (SAXException ex)
    {
      throw ex;
    }
    catch (Exception ex1)
    {
      log.error("Allgemeiner Fehler im Start-Element: " + localName);
      throw new SAXException(ex1);
    }
  }

  /**
   * undocumented
   *
   * @param ch undocumented
   * @param start undocumented
   * @param length undocumented
   * @throws SAXException undocumented
   */
  public void characters(char[] ch, int start, int length) throws SAXException
  {
    // if (log.isDebugEnabled()) log.debug("Character: " + new String(ch, start, length));
    if (valueWriter == null)
    {
      for ( int i = 0 ; i < length ; i++ )
      {
        if (ch[start + i] > ' ' && !Character.isDigit(ch[start + i]))
        {
          throw new SAXException(DialogHandler.text.getString("unexpected_char"));
        }
      }
    }
    else
    {
      try
      {
        valueWriter.write(ch, start, length);
      }
      catch (IOException ex)
      {
        throw new SAXException(ex);
      }
    }
  }

  
  public void endElement(String uri, String localName, String qName)
                  throws SAXException
  {
    if (log.isDebugEnabled())
      log.debug("Ende-Element: " + localName);

    if (localName.equals("EncryptedData") && uri.equals(XENC_XMLNS))
    {
      if(parentHandler != null)
      {
        xmlReader.setContentHandler(parentHandler);
        parentHandler.endElement(uri, localName, qName);
      }
    }
    else if (localName.equals("EncryptionMethod") && uri.equals(XENC_XMLNS))
      ; //nothing  to do
    else if (localName.equals("CipherData") && uri.equals(XENC_XMLNS))
      ; //nothing to do
    else if (localName.equals("EncryptedKey") && uri.equals(XENC_XMLNS))
      ;
    // nothing to do
    else if (localName.equals("CipherReference") && uri.equals(XENC_XMLNS))
    {
      try
      {
        CipherReference cr = new CipherReference(cipherRefId);
        Enumeration<String> transformers = transformer.elements();

        while (transformers.hasMoreElements())
          cr.addTransform(transformers.nextElement());

        encrytedDataObject.setCipherData(new CipherData(cr));
      }
      catch (Exception ex1)
      {
        log.error("Fehler beim setzen der CipherReference.", ex1);
        throw new SAXException(ex1);
      }
    }
    else if (localName.equals("Transforms") && uri.equals(XENC_XMLNS))
      ; // nothing to do
    else if (localName.equals("Transform") && uri.equals(DS_XMLNS))
      ; //nothing to do
    else if (localName.equals("CipherValue") && uri.equals(XENC_XMLNS))
    {
      try
      {
        valueWriter.close();
      }
      catch (IOException ex)
      {
        throw new SAXException(ex);
      }

      encrytedDataObject.setCipherData(new CipherData(cv));
    }
    else if (localName.equals("IvLength") && uri.equals(Constants.Namespaces.OSCI128.getUri()))
    {
      //nothing to do
    }
    else
      throw new SAXException(DialogHandler.text.getString("unexpected_entry") + ": " + localName);

    // Abwärtskompatibilität mit alten Nachrichten (OSCI-Bibliothek < 1.9.0)
    if(!encrytedDataObject.isIvLengthParsed())
    {
      encrytedDataObject.setIvLength(16);
    }
    
    valueWriter = null;
  }

  /**
   * undocumented
   *
   * @return undocumented
   */
  public EncryptedData getEncryptedData()
  {
    return encrytedDataObject;
  }


  /**
   * Parse {@link EncryptedData} element directly from XML representation given as byte array.
   *
   * @param xmlBytes
   * @return
   * @throws SAXException
   */
  public static EncryptedData createFromXmlBytes(byte[] xmlBytes) throws SAXException
  {
    try
    {
      SAXParser parser = ParserHelper.getNewSAXParser();
      ParserHelper.setFeatures(parser.getXMLReader());

      EncryptedDataBuilder encBuilder = new EncryptedDataBuilder(parser.getXMLReader());

      parser.parse(new InputSource(new ByteArrayInputStream(xmlBytes)), encBuilder);
      return encBuilder.getEncryptedData();
    }
    catch (IOException | ParserConfigurationException ex)
    {
      log.error("Allgemeiner Fehler beim Parsen des eingelesenen EncryptedData-Elements");
      throw new SAXException(ex);
    }
  }
}
