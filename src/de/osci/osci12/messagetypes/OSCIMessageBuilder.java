package de.osci.osci12.messagetypes;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import de.osci.helper.CanParser;
import de.osci.helper.Canonizer;
import de.osci.osci12.common.Constants;
import de.osci.osci12.common.Constants.Namespaces;
import de.osci.osci12.messageparts.MessagePartsFactory;
import de.osci.osci12.common.DialogHandler;


/**
 * Superklasse der Nachrichtenparser. Wird vom Anwender nicht benötigt.
 * <p>
 * Copyright © 2023 Governikus GmbH &amp; Co. KG, Germany
 * </p>
 * <p>
 * Erstellt von Governikus GmbH &amp; Co. KG
 * </p>
 * <p>
 * Diese Bibliothek kann von jedermann nach Maßgabe der European Union Public Licence 
 * genutzt werden.
 * </p>
 * <p>
 * Die Lizenzbestimmungen können unter der URL <a href="https://eupl.eu/">https://eupl.eu/</a>
 * abgerufen werden.
 * </p>
 *
 * @author R. Lindemann, N.Büngener
 * @version 2.4.1
 */
public class OSCIMessageBuilder extends DefaultHandler
{

  public static final String SOAP_XMLNS = Constants.Namespaces.SOAP.getUri();

  public static final String OSCI_XMLNS = Constants.Namespaces.OSCI.getUri();
  
  public static final String OSCI_2019_XMLNS = Constants.Namespaces.OSCI128.getUri();

  public static final String DS_XMLNS = Constants.Namespaces.XML_SIG.getUri();

  public static final String XENC_XMLNS = Constants.Namespaces.XML_ENC.getUri();

  public static final String XSI_XMLNS = Constants.Namespaces.XML_SCHEMA.getUri();

  private static Log log = LogFactory.getLog(OSCIMessageBuilder.class);

  protected StringBuffer currentElement = null;

  /**
   * Description of the Field
   */
  protected OSCIMessage msg = null;

  protected boolean contentPackageAlreadySet = false;

  protected boolean insideHeader = false;

  protected boolean insideBody = false;

  private HashSet<String> parsedMsgPartsElementNames = new HashSet<>();

  protected Hashtable<String, String> parsedMsgPartsIds = new Hashtable<>();

  public OSCIEnvelopeBuilder parentBuilder = null;

  protected Vector<String> customSoapHeader = new Vector<>();

  /**
   * Creates a new OSCIMessageBuilder object.
   *
   * @param eb undocumented
   */
  public OSCIMessageBuilder(OSCIEnvelopeBuilder eb)
  {
    this.parentBuilder = eb;
  }

  /**
   * undocumented
   *
   * @return undocumented
   */
  public OSCIMessage getOSCIMessage()
  {
    return msg;
  }

  /**
   * undocumented
   *
   * @return undocumented
   */
  public XMLReader getXMLReader()
  {
    return parentBuilder.xmlReader;
  }

  /**
   * undocumented
   *
   * @param uri undocumented
   * @param localName undocumented
   * @param qName undocumented
   * @param attributes undocumented
   * @throws SAXException undocumented
   */
  @Override
  public void startElement(String uri, String localName, String qName, Attributes attributes)
    throws SAXException
  {
    log.debug("ENV-Builder startElement: " + localName + " " + uri.length());

    if (localName.equals("Header") && uri.equals(SOAP_XMLNS))
    {
      if (insideHeader)
        throw new SAXException(DialogHandler.text.getString("unexpected_entry") + ": " + localName);
      insideHeader = true;
    }
    else if (localName.equals("Body") && uri.equals(SOAP_XMLNS))
    {
      if (insideBody)
        throw new SAXException(DialogHandler.text.getString("unexpected_entry") + ": " + localName);
      insideBody = true;
      addFoundMsgPartIds(attributes.getValue("Id"), SOAP_XMLNS + ":Body");
      // msg.bodyId = attributes.getValue("Id");
    }
    else if (!(localName.equals("Envelope") && uri.equals(SOAP_XMLNS)))
      throw new SAXException(DialogHandler.text.getString("unexpected_entry") + ": " + localName);
  }

  /**
   * undocumented
   *
   * @param uri undocumented
   * @param localName undocumented
   * @param qName undocumented
   * @param attributes undocumented
   * @throws SAXException undocumented
   */
  protected void startCustomSoapHeader(String uri, String localName, String qName, Attributes attributes)
    throws SAXException
  {
    try
    {
      CanParser cp = new CanParser(customSoapHeader, parentBuilder.xmlReader,
                                   parentBuilder.xmlReader.getContentHandler(), qName);
      parentBuilder.xmlReader.setContentHandler(cp);
      cp.startDocument();
      cp.startElement(uri, localName, qName, attributes);
      addFoundMsgPartIds(attributes.getValue("Id"), uri + ":" + localName);
    }
    catch (SAXException ex)
    {
      throw ex;
    }
    catch (Exception ex)
    {
      throw new SAXException(DialogHandler.text.getString("sax_exception_customheader"), ex);
    }
  }

  /**
   * undocumented
   *
   * @param uri undocumented
   * @param localName undocumented
   * @param qName undocumented
   * @throws SAXException undocumented
   */
  @Override
  public void endElement(String uri, String localName, String qName) throws SAXException
  {
    if (localName.equals("Header") && uri.equals(SOAP_XMLNS))
      insideHeader = false;
    else if (localName.equals("Body") && uri.equals(SOAP_XMLNS))
      insideBody = false;
    else if (localName.equals("Envelope") && uri.equals(SOAP_XMLNS))
    {
      parentBuilder.xmlReader.setContentHandler(parentBuilder);

      for ( int i = 0 ; i < customSoapHeader.size() ; i++ )
        msg.addCustomHeader(customSoapHeader.get(i));
    }

    currentElement = null;
  }

  /**
   * undocumented
   *
   * @param ch undocumented
   * @param start undocumented
   * @param length undocumented
   * @throws SAXException undocumented
   */
  @Override
  public void characters(char[] ch, int start, int length) throws SAXException
  {
    if (currentElement == null)
    {
      for ( int i = 0 ; i < length ; i++ )
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
  public Canonizer getCanStream()
  {
    return parentBuilder.hashNCanStream;
  }

  /**
   * F?gt ein Element in die Liste der bereits gefundenen Elemente
   * 
   * @param qName Elementname
   * @throws SAXException In Fehlerf?llen z.B. doppeltes Element
   */
  private void addFoundMsgPart(String qName) throws SAXException
  {
    if (!parsedMsgPartsElementNames.add(qName))
    {
      log.error("MessagePart already found: " + qName);
      throw new SAXException(DialogHandler.text.getString("unexpected_entry"));
    }
  }

  /**
   * undocumented
   *
   * @param prefix undocumented
   * @param uri undocumented
   */
  @Override
  public void startPrefixMapping(String prefix, String uri)
  {
    if (uri.equals(Namespaces.OSCI2017.getUri()))
      msg.osci2017NSPrefix = prefix;
  }

  /**
   * F?gt ein Element in die Liste der bereits gefundenen Elemente
   * 
   * @param id Id des gefundenen Elements
   * @param qName Elementenname mit Namespace
   * @throws SAXException In Fehlerf?llen z.B. doppeltes Element
   */
  public void addFoundMsgPartIds(String id, String qName) throws SAXException
  {
    addFoundMsgPart(qName);
    if (id == null)
    {
      log.error("No Id attribute found: " + qName);
      throw new SAXException(DialogHandler.text.getString("unexpected_entry"));
    }
    id = "#".concat(id);
    if (parsedMsgPartsIds.containsKey(id))
    {
      log.error("Id already found: " + qName);
      throw new SAXException(DialogHandler.text.getString("unexpected_entry"));
    }
    parsedMsgPartsIds.put(id, qName);

  }
  
  public void setContentPackageHandler(String localName) throws SAXException {
    if(contentPackageAlreadySet)
    {
      throw new SAXException(DialogHandler.text.getString("unsupported_entry") + ": " + localName);
    }
    contentPackageAlreadySet = true;
    parentBuilder.xmlReader.setContentHandler(MessagePartsFactory.createContentPackageBuilder(this));
  }

  // public void checkDigestAndIdold(String id, String removeQName) throws SAXException
  // {
  // if (id == null)
  // {
  // log.error("ID element do not exist!");
  // throw new SAXException(DialogHandler.text.getString("unexpected_entry"));
  // }
  // id = "#".concat(id);
  // String qName = parentBuilder.hashNCanStream.getAvaliableMsgParts().get(id);
  // if (qName == null)
  // {
  // if (parentBuilder.hashNCanStream.getAvaliableMsgParts().containsKey(id))
  // {
  // log.error("Element with Id: " + id + " already found");
  // }
  // else
  // {
  // log.error("Element with Id: " + id + " not avalibale");
  // }
  // throw new SAXException(DialogHandler.text.getString("unexpected_entry"));
  // }
  // if (!qName.equals(removeQName))
  // {
  // log.error("Element with qName: " + removeQName + " was not the expected element!");
  // throw new SAXException(DialogHandler.text.getString("unexpected_entry"));
  // }
  // // now check that this part was really hashed
  // if (parentBuilder.hashNCanStream.getDigestValues().get(id) == null)
  // {
  // log.error("Element with Id: " + id + " was not hashed");
  // throw new SAXException(DialogHandler.text.getString("unexpected_entry"));
  // }
  // parentBuilder.hashNCanStream.getAvaliableMsgParts().put(id, null);
  // }
}
