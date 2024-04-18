package de.osci.osci12.messageparts;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import de.osci.osci12.common.Constants.LanguageTextEntries;
import de.osci.osci12.common.DialogHandler;


/**
 * Zeitstempelparser, wird von Anwendungen nie direkt benötigt.
 *
 * <p>Copyright © 2023 Governikus GmbH &amp; Co. KG, Germany</p>
 * <p>Erstellt von Governikus GmbH &amp; Co. KG</p>
 * <p>Diese Bibliothek kann von jedermann nach Maßgabe der European Union
 *  Public Licence genutzt werden.</p><p>Die Lizenzbestimmungen können
 * unter der URL <a href="https://eupl.eu/">https://eupl.eu/</a> abgerufen werden.
 *
 *
 * @author R. Lindemann, N.Büngener
 * @version 2.4.1
 */
class TimestampBuilder extends MessagePartParser
{
  private static Log log = LogFactory.getLog(TimestampBuilder.class);
  private Timestamp timestamp = null;

  /**
   * Creates a new TimestampBuilder object.
   *
   * @param xmlReader undocumented
   * @param parentHandler undocumented
   */
  public TimestampBuilder(XMLReader xmlReader, DefaultHandler parentHandler)
  {
    super(xmlReader, parentHandler);
    timestamp = new Timestamp();

    MessagePart mp;

    if (parentHandler instanceof InspectionBuilder)
      mp = ((InspectionBuilder) parentHandler).inspection;
    else
      mp = ((ProcessCardBundleBuilder) parentHandler).processCard;

    timestamp.setNSPrefixes(mp.soapNSPrefix, mp.osciNSPrefix, mp.dsNSPrefix, mp.xencNSPrefix, mp.xsiNSPrefix);
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

    try
    {
      if (localName.equals("Plain") && uri.equals(OSCI_XMLNS))
        currentElement = new StringBuffer();
      else if (localName.equals("Cryptographic") && uri.equals(OSCI_XMLNS))
      {
        currentElement = new StringBuffer();

        String algo = attributes.getValue("Algorithm");

        if ((algo == null) || algo.equals(""))
          throw new SAXException(DialogHandler.text.getString(LanguageTextEntries.missing_entry.name()) + ": Algorithm");

        timestamp.setAlgorithm(algo);
      }
      else
        throw new SAXException(DialogHandler.text.getString("unexpected_entry") + ": " + localName);
    }
    catch (SAXException e)
    {
      throw e;
    }
    catch (Exception ex)
    {
      log.error("Fehler im Start-Element", ex);
      throw new SAXException(ex);
    }
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
      if (localName.equals("Plain") && uri.equals(OSCI_XMLNS))
        timestamp.setTimeStamp(currentElement.toString());
      else if (localName.equals("Cryptographic") && uri.equals(OSCI_XMLNS))
        timestamp.setTimeStamp(new String(de.osci.helper.Base64.decode(currentElement.toString()),
                                          de.osci.osci12.common.Constants.CHAR_ENCODING));
      else if (localName.equals("Timestamp") && uri.equals(OSCI_XMLNS))
      {
        timestamp.setNameID(Timestamp.PROCESS_CARD_TIMESTAMP);
        parentHandler.endElement(uri, localName, qName);
        xmlReader.setContentHandler(parentHandler);
      }
      else if (localName.equals("Creation") && uri.equals(OSCI_XMLNS))
      {
        timestamp.setNameID(Timestamp.PROCESS_CARD_CREATION);
        parentHandler.endElement(uri, localName, qName);
        xmlReader.setContentHandler(parentHandler);
      }
      else if (localName.equals("Forwarding") && uri.equals(OSCI_XMLNS))
      {
        timestamp.setNameID(Timestamp.PROCESS_CARD_FORWARDING);
        parentHandler.endElement(uri, localName, qName);
        xmlReader.setContentHandler(parentHandler);
      }
      else if (localName.equals("Reception") && uri.equals(OSCI_XMLNS))
      {
        timestamp.setNameID(Timestamp.PROCESS_CARD_RECEPTION);
        parentHandler.endElement(uri, localName, qName);
        xmlReader.setContentHandler(parentHandler);
      }
      else
        throw new SAXException(DialogHandler.text.getString("unexpected_entry") + ": " + localName);
    }
    catch (SAXException ex)
    {
      throw ex;
    }
    catch (Exception ex)
    {
      log.error("Fehler im End-Element!", ex);
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

  Timestamp getTimestampObject()
  {
    return timestamp;
  }
}
