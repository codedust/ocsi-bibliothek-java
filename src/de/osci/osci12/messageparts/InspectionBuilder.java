package de.osci.osci12.messageparts;

import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import de.osci.osci12.common.Constants.LanguageTextEntries;
import de.osci.osci12.common.DialogHandler;


/**
 * Inspection-Parser.
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
class InspectionBuilder extends MessagePartParser
{
  private static Log log = LogFactory.getLog(InspectionBuilder.class);
  Inspection inspection = null;
  private TimestampBuilder timestampBuilder = null;
  private boolean isInsideOnlineResult = false;
  private Vector<String> onlineChecks = new Vector<String>();
  private Vector<String> onlineCheckNames = new Vector<String>();

  /**
   * Creates a new InspectionBuilder object.
   *
   * @param xmlReader undocumented
   * @param parentHandler undocumented
   */
  public InspectionBuilder(XMLReader xmlReader, DefaultHandler parentHandler)
  {
    super(xmlReader, parentHandler);
    inspection = new Inspection();

    ProcessCardBundle pcb = ((ProcessCardBundleBuilder) parentHandler).processCard;
    inspection.setNSPrefixes(pcb.soapNSPrefix, pcb.osciNSPrefix, pcb.dsNSPrefix, pcb.xencNSPrefix, pcb.xsiNSPrefix);
  }

  /**
   * undocumented
   *
   * @param uri undocumented
   * @param localName undocumented
   * @param qName undocumented
   * @param attributes undocumented
   *
   * @throws org.xml.sax.SAXException undocumented
   * @throws SAXException undocumented
   */
  public void startElement(String uri, String localName, String qName, Attributes attributes)
                    throws org.xml.sax.SAXException
  {
    if (log.isDebugEnabled())
      log.debug("Start-Element: " + localName);

    if (localName.equals("Timestamp") && uri.equals(OSCI_XMLNS))
    {
      timestampBuilder = new TimestampBuilder(xmlReader, this);
      xmlReader.setContentHandler(timestampBuilder);
    }
    else if (localName.equals("X509IssuerName") && uri.equals(OSCI_XMLNS))
    {
      currentElement = new StringBuffer();
    }
    else if (localName.equals("X509SerialNumber") && uri.equals(OSCI_XMLNS))
    {
      currentElement = new StringBuffer();
    }
    else if (localName.equals("X509SubjectName") && uri.equals(OSCI_XMLNS))
    {
      currentElement = new StringBuffer();
    }
    else if (localName.equals("CertType") && uri.equals(OSCI_XMLNS))
    {
      inspection.setCertType(attributes.getValue("Type"));
    }
    else if (localName.equals("MathResult") && uri.equals(OSCI_XMLNS))
    {
      inspection.setMathResult(attributes.getValue("Result"));
    }
    else if (localName.equals("OfflineResult") && uri.equals(OSCI_XMLNS))
    {
      inspection.setOfflineResult(attributes.getValue("Result"));
    }
    else if (localName.equals("OnlineResult") && uri.equals(OSCI_XMLNS))
    {
      isInsideOnlineResult = true;
      inspection.setOnlineResult(attributes.getValue("Result").equals("ok"));
    }
    else if (isInsideOnlineResult)
    {
      if (localName.equals("OCSP") && uri.equals(OSCI_XMLNS))
      {
        currentElement = new StringBuffer();
        onlineCheckNames.add("OCSP");
      }
      else if (localName.equals("CRL") && uri.equals(OSCI_XMLNS))
      {
        currentElement = new StringBuffer();
        onlineCheckNames.add("CRL");
      }
      else if (localName.equals("LDAP") && uri.equals(OSCI_XMLNS))
      {
        currentElement = new StringBuffer();
        onlineCheckNames.add("LDAP");
      }
    }
    else
    {
      throw new SAXException(DialogHandler.text.getString("unexpected_entry") + ": " + localName);
    }
  }

  /**
   * undocumented
   *
   * @return undocumented
   */
  public Inspection getInspectionObject()
  {
    return inspection;
  }

  /**
   * undocumented
   *
   * @param uri undocumented
   * @param localName undocumented
   * @param qName undocumented
   *
   * @throws org.xml.sax.SAXException undocumented
   * @throws SAXException undocumented
   */
  public void endElement(String uri, String localName, String qName)
                  throws org.xml.sax.SAXException
  {
    if (log.isDebugEnabled())
      log.debug("End-Element: " + localName);

    if (localName.equals("Inspection") && uri.equals(OSCI_XMLNS))
    {
      if ((inspection.getMathResult() == null) || (inspection.getOfflineResult() == null))
        throw new SAXException(DialogHandler.text.getString(LanguageTextEntries.missing_entry.name()) + ".");

      inspection.setOnlineCheckNames((String[]) onlineCheckNames.toArray(new String[0]));
      inspection.setOnlineChecks((String[]) onlineChecks.toArray(new String[0]));
      parentHandler.endElement(uri, localName, qName);
      xmlReader.setContentHandler(parentHandler);
    }
    else if (localName.equals("Timestamp") && uri.equals(OSCI_XMLNS))
      inspection.setTimeStamp(timestampBuilder.getTimestampObject());
    else if (localName.equals("X509IssuerName") && uri.equals(OSCI_XMLNS))
    {
      inspection.setX509IssuerName(currentElement.toString());
    }
    else if (localName.equals("X509SerialNumber") && uri.equals(OSCI_XMLNS))
    {
      inspection.setX509SerialNumber(currentElement.toString());
    }
    else if (localName.equals("X509SubjectName") && uri.equals(OSCI_XMLNS))
    {
      inspection.setX509SubjectNameNumber(currentElement.toString());
    }
    else if (localName.equals("CertType") && uri.equals(OSCI_XMLNS))
      ; //nothing to do
    else if (localName.equals("MathResult") && uri.equals(OSCI_XMLNS))
      ; //nothing to do
    else if (localName.equals("OfflineResult") && uri.equals(OSCI_XMLNS))
      ; //nothing to do
    else if (localName.equals("OnlineResult") && uri.equals(OSCI_XMLNS))
    {
      isInsideOnlineResult = false;
    }
    else if (isInsideOnlineResult && ((localName.equals("OCSP") && uri.equals(OSCI_XMLNS)) ||
                 (localName.equals("CRL") && uri.equals(OSCI_XMLNS)) ||
                 (localName.equals("LDAP") && uri.equals(OSCI_XMLNS))))
    {
      onlineChecks.add(currentElement.toString());
    }
    else
    {
      throw new SAXException(DialogHandler.text.getString("unexpected_entry") + ": " + localName);
    }

    currentElement = null;
  }
}
