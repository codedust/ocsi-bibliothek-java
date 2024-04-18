package de.osci.osci12.messageparts;

import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import de.osci.helper.ParserHelper;
import de.osci.osci12.common.Constants;
import de.osci.osci12.common.Constants.CommonTags;
import de.osci.osci12.common.Constants.LanguageTextEntries;
import de.osci.osci12.common.DialogHandler;
import de.osci.osci12.messagetypes.OSCIMessage;
import de.osci.osci12.messagetypes.OSCIMessageBuilder;


/**
 * Laufzettelparser, wird von Anwendungen nie direkt benötigt.
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
public class ProcessCardBundleBuilder extends MessagePartParser
{
  private static Log log = LogFactory.getLog(ProcessCardBundleBuilder.class);
  ProcessCardBundle processCard = null;
  TimestampBuilder timeBuilder = null;
  private InspectionBuilder inspectionBuilder = null;
  private int[] check;
  private Vector<Inspection> inspections = new Vector<Inspection>();

  /**
   * Creates a new ProcessCardBundleBuilder object.
   *
   * @param name undocumented
   * @param xmlReader undocumented
   * @param parentHandler undocumented
   * @param check undocumented
   */
  public ProcessCardBundleBuilder(CommonTags name, XMLReader xmlReader, DefaultHandler parentHandler, int[] check)
  {
    super(xmlReader, parentHandler);
    processCard = new ProcessCardBundle(name.getElementName());
    this.check = check;

    OSCIMessage msg = ((OSCIMessageBuilder) parentHandler).getOSCIMessage();
    processCard.setNSPrefixes(msg);
  }

  ProcessCardBundleBuilder(XMLReader xmlReader, ProcessCardBundle proBundle, int[] check)
  {
    super(xmlReader, null);
    processCard = proBundle;
    this.check = check;
  }
  /**
   * Creates a new ProcessCardBundleBuilder object.
   *
   * @param name undocumented
   * @param xmlReader undocumented
   * @param parentHandler undocumented
   * @param check undocumented
   */

  public ProcessCardBundleBuilder(String name, XMLReader xmlReader, DefaultHandler parentHandler, int[] check)
  {
    super(xmlReader, parentHandler);
    processCard = new ProcessCardBundle(name);
    this.check = check;

    OSCIMessage msg = ((OSCIMessageBuilder) parentHandler).getOSCIMessage();
    processCard.setNSPrefixes(msg);
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

    if (ParserHelper.isElement(CommonTags.MessageId,localName,uri))
      currentElement = new StringBuffer();
    else if (ParserHelper.isElement(CommonTags.ProcessCardBundle, localName,uri))
      ; // nothing to do
    else if (localName.equals("ProcessCard") && uri.equals(OSCI_XMLNS))
    {
      processCard.setRecentModification(attributes.getValue("RecentModification"));
    }
    else if ((localName.equals("Creation") && uri.equals(OSCI_XMLNS)) ||
                 (localName.equals("Forwarding") && uri.equals(OSCI_XMLNS)) ||
                 (localName.equals("Reception") && uri.equals(OSCI_XMLNS)))
    {
      timeBuilder = new TimestampBuilder(this.xmlReader, this);
      xmlReader.setContentHandler(timeBuilder);
    }
    else if (localName.equals("Subject") && uri.equals(OSCI_XMLNS))
      currentElement = new StringBuffer();
    else if (localName.equals("InspectionReport") && uri.equals(OSCI_XMLNS))
      ; //nothing to do
    else if (localName.equals("Inspection") && uri.equals(OSCI_XMLNS))
    {
      inspectionBuilder = new InspectionBuilder(xmlReader, this);
      xmlReader.setContentHandler(inspectionBuilder);
    }
    else
      throw new SAXException(DialogHandler.text.getString("unexpected_entry") + " " + localName);
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
    try
    {
      if (log.isDebugEnabled())
        log.debug("End-Element: " + localName);

      if (localName.equals(processCard.name) && uri.equals(OSCI_XMLNS))
      {
        if (processCard.getMessageId() == null)
          throw new SAXException(DialogHandler.text.getString(LanguageTextEntries.missing_entry.name()) + ": " + localName);

        if (inspections.toArray().length > 0)
          processCard.setInspections(inspections.toArray(new Inspection[0]));

        if (parentHandler != null)
          xmlReader.setContentHandler(parentHandler);
      }
      else if (ParserHelper.isElement(CommonTags.MessageId,localName,uri))
      {
        processCard.setMessageId(new String(de.osci.helper.Base64.decode(currentElement.toString()),
                                            Constants.CHAR_ENCODING));
      }
      else if (localName.equals("Creation") && uri.equals(OSCI_XMLNS))
      {
        if (check[0] != 0)
        {
          processCard.setCreation(timeBuilder.getTimestampObject());
          check[0]--;
        }
        else
          throw new SAXException(DialogHandler.text.getString("unexpected_entry") + ": " + localName);
      }
      else if (localName.equals("Forwarding") && uri.equals(OSCI_XMLNS))
      {
        if (check[1] != 0)
        {
          processCard.setForwarding(timeBuilder.getTimestampObject());
          check[1]--;
        }
        else
          throw new SAXException(DialogHandler.text.getString("unexpected_entry") + ": " + localName);
      }
      else if (localName.equals("Reception") && uri.equals(OSCI_XMLNS))
      {
        if (check[2] != 0)
        {
          processCard.setReception(timeBuilder.getTimestampObject());
          check[2]--;
        }
        else
          throw new SAXException(DialogHandler.text.getString("unexpected_entry") + ": " + localName);
      }
      else if (localName.equals("Subject") && uri.equals(OSCI_XMLNS))
      {
        if (check[3] != 0)
        {
          processCard.setSubject(currentElement.toString());
          check[3]--;
        }
        else
          throw new SAXException(DialogHandler.text.getString("unexpected_entry") + ": " + localName);
      }
      else if (localName.equals("InspectionReport") && uri.equals(OSCI_XMLNS))
        ; //nothing to do
      else if (localName.equals("Inspection") && uri.equals(OSCI_XMLNS))
        inspections.add(inspectionBuilder.getInspectionObject());
      else if (localName.equals("ProcessCard") && uri.equals(OSCI_XMLNS))
        ; // nothing to do
      else
        throw new SAXException(DialogHandler.text.getString("unexpected_entry") + ": " + localName);
    }
    catch (SAXException e)
    {
      throw e;
    }
    catch (Exception ex)
    {
      log.error("Fehler Start-Element: " + localName, ex);
      throw new SAXException(ex);
    }

    currentElement = null;
  }

  /**
   * undocumented
   *
   * @return undocumented
   */
  public ProcessCardBundle getProcessCardBundleObject()
  {
    return processCard;
  }
}
