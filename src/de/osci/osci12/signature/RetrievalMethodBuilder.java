package de.osci.osci12.signature;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import de.osci.osci12.common.Constants.LanguageTextEntries;


/**
 * Builder, der ein Element ds:RetrievalMethod bearbeitet.
 *
 * <p>Copyright © 2023 Governikus GmbH &amp; Co. KG, Germany</p>
 * <p>Erstellt von Governikus GmbH &amp; Co. KG</p>
 * <p>Diese Bibliothek kann von jedermann nach Maßgabe der European Union
 *  Public Licence genutzt werden.</p><p>Die Lizenzbestimmungen können
 * unter der URL <a href="https://eupl.eu/">https://eupl.eu/</a> abgerufen werden.
 *
 * @author PPI Financial Systems GmbH
 */
class RetrievalMethodBuilder extends DefaultHandler
{
  DefaultHandler parentHandler = null;
  XMLReader xmlReader = null;
  protected static Log log = LogFactory.getLog(RetrievalMethodBuilder.class);
  /** Aufgebaute RetrievalMethod. */
  private final RetrievalMethod retrievalObject = new RetrievalMethod();
  protected static final String DS_XMLNS = "http://www.w3.org/2000/09/xmldsig#";

  /**
   * Creates a new RetrievalMethodBuilder object.
   *
   * @param xmlReader undocumented
   * @param parentHandler undocumented
   * @param attributes undocumented
   */
  public RetrievalMethodBuilder(XMLReader xmlReader, DefaultHandler parentHandler, Attributes attributes)
  {
    this.xmlReader = xmlReader;
    this.parentHandler = parentHandler;

    if (attributes == null)
      throw new IllegalArgumentException(de.osci.osci12.common.DialogHandler.text.getString(LanguageTextEntries.invalid_thirdargument.name()) +
                                         " null");

    String uri = attributes.getValue("URI");
    retrievalObject.setURI(uri);
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

    if (localName.equals("RetrievalMethod") && uri.equals(DS_XMLNS))
    {
      if (parentHandler instanceof KeyInfoBuilder)
      {
        try
        {
          ((KeyInfoBuilder) parentHandler).keyInfo.setRetrievalMethod(retrievalObject);
        }
        catch (IllegalStateException ex)
        {
          throw new SAXException(de.osci.osci12.common.DialogHandler.text.getString("sax_exception"));
        }
      }

      xmlReader.setContentHandler(parentHandler);
    }
    else if (localName.equals("Transforms") && uri.equals(DS_XMLNS))
      ; //nothing to do
    else if (localName.equals("Transform") && uri.equals(DS_XMLNS))
      ; //nothing to do
    else
      throw new SAXException(de.osci.osci12.common.DialogHandler.text.getString("unexpected_entry") + ": " + localName);
  }

  /**
   * Liefert die aufgebaute RetrievalMethod.
   *
   * @return RetrievalMethod.
   */
  public RetrievalMethod getRetrievalMethod()
  {
    return retrievalObject;
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
      log.debug("Start Element: " + localName);

    if (localName.equals("Transforms") && uri.equals(DS_XMLNS))
      ; //nothing to do
    else if (localName.equals("Transform") && uri.equals(DS_XMLNS))
      retrievalObject.addTransformer(attributes.getValue("Algorithm"));
    else
      throw new SAXException(de.osci.osci12.common.DialogHandler.text.getString("unexpected_entry") + ": " + localName);
  }
}
