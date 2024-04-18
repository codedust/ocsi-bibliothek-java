package de.osci.osci12.messagetypes;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.osci.osci12.common.DialogHandler;
import de.osci.osci12.common.OSCIExceptionCodes.OSCIErrorCodes;
import de.osci.osci12.common.SoapClientException;
import de.osci.osci12.common.SoapServerException;


/**
 * SOAPFault-Parser.
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
class SOAPFaultBuilder extends OSCIMessageBuilder
{
  private static Log log = LogFactory.getLog(SOAPFaultBuilder.class);
  private String faultcode;
  private String faultstring;
  private String oscicode;

  /**
   *  Constructor for the SOAPFaultBuilder object
   *
   *@param  parentHandler  Description of Parameter
   */
  public SOAPFaultBuilder(OSCIEnvelopeBuilder parentHandler)
  {
    super(parentHandler);
    // Dummy-Nachrichtenobjekt
    msg = new SOAPFault("");
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
      log.debug("Start Element SOAPFaultBuilder: " + localName + uri);

    if (localName.equals("Body"))
    {
      insideBody = true;
    }
    else if (insideBody)
    {
      // todo: Keine uri ?
      if ((localName.equals("faultcode") || localName.equals("faultstring")) ||
              (localName.equals("Code") && uri.equals(OSCI_XMLNS)))
        currentElement = new StringBuffer();
    }
    else if (!(localName.equals("detail") /* && uri.equals(SOAP_XMLNS)*/))
      throw new SAXException(DialogHandler.text.getString("unexpected_entry") + ": " + localName);
  }

  /**
   *  Description of the Method
   *
   *@param  uri                           Description of Parameter
   *@param  localName                     Description of Parameter
   *@param  qName                         Description of Parameter
   *@exception  SAXException  Description of Exception
   */
  public void endElement(String uri, String localName, String qName)
                  throws SAXException
  {
    if (log.isDebugEnabled())
      log.debug("End-Element: " + localName);

    if (localName.equals("faultcode") /* && uri.equals(SOAP_XMLNS)*/)
      faultcode = currentElement.toString();
    else if (localName.equals("faultstring") /* && uri.equals(SOAP_XMLNS)*/)
      faultstring = currentElement.toString();
    else if (localName.equals("Code") && uri.equals(OSCI_XMLNS))
      oscicode = currentElement.toString();
    else if (localName.equals("Envelope") && uri.equals(SOAP_XMLNS))
    {
      log.debug("SOAP-Error " + oscicode + "-" + faultstring);
      OSCIErrorCodes errorCode=null;
      errorCode=OSCIErrorCodes.fromErrorCode(oscicode);
      if(errorCode==null)
      {
         log.error("Could not find error Code: "+oscicode);
         errorCode=OSCIErrorCodes.InternalErrorSupplier;
         faultstring=faultstring.concat("Error code not found in Enumeration. Code: "+oscicode);
      }
      if (faultcode.equals(getOSCIMessage().soapNSPrefix + ":Server"))
        throw new SAXException(new SoapServerException(errorCode, faultstring));
      else
        throw new SAXException(new SoapClientException(errorCode, faultstring));
    }

    currentElement = null;
  }
}
