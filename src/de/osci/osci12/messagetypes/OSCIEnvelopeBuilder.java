package de.osci.osci12.messagetypes;

import java.security.NoSuchAlgorithmException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import de.osci.helper.Canonizer;
import de.osci.osci12.common.Constants.Namespaces;
import de.osci.osci12.common.DialogHandler;
import de.osci.osci12.roles.OSCIRoleException;


/**
 * Nachrichtenparser.
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
class OSCIEnvelopeBuilder extends DefaultHandler
{

  static final String XSD_ENC_SIG = "http://www.w3.org/2000/09/xmldsig# oscisig.xsd http://www.w3.org/2001/04/xmlenc# oscienc.xsd";

  // Deklaration der XSD für die Nachrichtentypen
  static final String XSD_INIT_DIALOG = "http://schemas.xmlsoap.org/soap/envelope/ soapInitDialog.xsd " + XSD_ENC_SIG;
  static final String XSD_RSP_INIT_DIALOG = "http://schemas.xmlsoap.org/soap/envelope/ soapResponseToInitDialog.xsd " +
                                            XSD_ENC_SIG;
  static final String XSD_EXIT_DIALOG = "http://schemas.xmlsoap.org/soap/envelope/ soapExitDialog.xsd " + XSD_ENC_SIG;
  static final String XSD_RSP_EXIT_DIALOG = "http://schemas.xmlsoap.org/soap/envelope/ soapResponseToExitDialog.xsd " +
                                            XSD_ENC_SIG;
  static final String XSD_GET_MSG_ID = "http://schemas.xmlsoap.org/soap/envelope/ soapGetMessageId.xsd " + XSD_ENC_SIG;
  static final String XSD_RSP_GET_MSG_ID = "http://schemas.xmlsoap.org/soap/envelope/ soapResponseToGetMessageId.xsd " +
                                           XSD_ENC_SIG;
  static final String XSD_STORE_DELIVERY = "http://schemas.xmlsoap.org/soap/envelope/ soapStoreDelivery.xsd " +
                                           XSD_ENC_SIG;
  static final String XSD_RSP_STORE_DELIVERY =
    "http://schemas.xmlsoap.org/soap/envelope/ soapResponseToStoreDelivery.xsd " + XSD_ENC_SIG;
  static final String XSD_FETCH_DELIVERY = "http://schemas.xmlsoap.org/soap/envelope/ soapFetchDelivery.xsd " +
                                           XSD_ENC_SIG;
  static final String XSD_RSP_FETCH_DELIVERY =
    "http://schemas.xmlsoap.org/soap/envelope/ soapResponseToFetchDelivery.xsd " + XSD_ENC_SIG;
  static final String XSD_FETCH_PROCESS_CARD = "http://schemas.xmlsoap.org/soap/envelope/ soapFetchProcessCard.xsd " +
                                               XSD_ENC_SIG;
  static final String XSD_RSP_FETCH_PROCESS_CARD =
        "http://schemas.xmlsoap.org/soap/envelope/ soapResponseToFetchProcessCard.xsd " + XSD_ENC_SIG;
  static final String XSD_FORWARD_DELIVERY = "http://schemas.xmlsoap.org/soap/envelope/ soapForwardDelivery.xsd " +
                                             XSD_ENC_SIG;
  static final String XSD_RSP_FORWARD_DELIVERY =
        "http://schemas.xmlsoap.org/soap/envelope/ soapResponseToForwardDelivery.xsd " + XSD_ENC_SIG;
  static final String XSD_ACCEPT_DELIVERY = "http://schemas.xmlsoap.org/soap/envelope/ soapAcceptDelivery.xsd " +
                                            XSD_ENC_SIG;
  static final String XSD_RSP_ACCEPT_DELIVERY =
        "http://schemas.xmlsoap.org/soap/envelope/ soapResponseToAcceptDelivery.xsd " + XSD_ENC_SIG;
  static final String XSD_MEDIATE_DELIVERY = "http://schemas.xmlsoap.org/soap/envelope/ soapMediateDelivery.xsd " +
                                             XSD_ENC_SIG;
  static final String XSD_RSP_MEDIATE_DELIVERY =
        "http://schemas.xmlsoap.org/soap/envelope/ soapResponseToMediateDelivery.xsd " + XSD_ENC_SIG;
  static final String XSD_PROCESS_DELIVERY = "http://schemas.xmlsoap.org/soap/envelope/ soapProcessDelivery.xsd " +
                                             XSD_ENC_SIG;
  static final String XSD_RSP_PROCESS_DELIVERY =
        "http://schemas.xmlsoap.org/soap/envelope/ soapResponseToProcessDelivery.xsd " + XSD_ENC_SIG;
  static final String XSD_MESSAGE_FAULT = "http://schemas.xmlsoap.org/soap/envelope/ soapMessageFault.xsd " +
                                          XSD_ENC_SIG;
  static final String XSD_ENCRYPTED_DATA = "http://schemas.xmlsoap.org/soap/envelope/ soapMessageEncrypted.xsd " +
                                           XSD_ENC_SIG;
  // EFFI message types
  static final String XSD_PARTIAL_STORE_DELIVERY = "http://schemas.xmlsoap.org/soap/envelope/ soapPartialStoreDelivery.xsd " +
    XSD_ENC_SIG;
  static final String XSD_RSP_PARTIAL_STORE_DELIVERY = "http://schemas.xmlsoap.org/soap/envelope/ soapResponseToPartialStoreDelivery.xsd " +
    XSD_ENC_SIG;
  static final String XSD_PARTIAL_FETCH_DELIVERY = "http://schemas.xmlsoap.org/soap/envelope/ soapPartialFetchDelivery.xsd " +
    XSD_ENC_SIG;
  static final String XSD_RSP_PARTIAL_FETCH_DELIVERY = "http://schemas.xmlsoap.org/soap/envelope/ soapResponseToPartialFetchDelivery.xsd " +
    XSD_ENC_SIG;


  //Allgemeines
  private static Log log = LogFactory.getLog(OSCIEnvelopeBuilder.class);
  protected OSCIMessage msg = null;
  XMLReader xmlReader = null;
  /** Für internen Gebrauch */
  Canonizer hashNCanStream = null;
  protected DialogHandler dhHandler = null;
  /**
   * SOAP-Namespace Identifier
   */
  String soapNSPrefix;
  /**
   * OSCI-Namespace Identifier
   */
  String osciNSPrefix;

  /**
   * OSCI-Namespace Identifier OSCI Version 126 ; 2017 Namespace
   */
  String osci2017NSPrefix;
  /**
   * OSCI-Namespace Identifier OSCI Version 128 ; 2019 Namespace
   */
  String osci2019NSPrefix;
  /**
   * XML Signature-Namespace Identifier
   */
  String dsNSPrefix;
  /**
   * XML Encryption-Namespace Identifier
   */
  String xencNSPrefix;
  StringBuffer ns;
  OSCIMessageBuilder childBuilder = null;

  /**
   *  Konstruktor für das OSCIMessageBuilder-Objekt
   *
   *@param  xmlReader
   *@param  dh
   */
  public OSCIEnvelopeBuilder(XMLReader xmlReader, DialogHandler dh)
  {
    this.xmlReader = xmlReader;
    this.dhHandler = dh;
    ns = new StringBuffer();
  }

  /**
   * undocumented
   *
   * @param prefix undocumented
   * @param uri undocumented
   */
  public void startPrefixMapping(String prefix, String uri)
  {
    if (uri.equals(OSCIMessageBuilder.SOAP_XMLNS))
      soapNSPrefix = prefix;
    else if (uri.equals(OSCIMessageBuilder.OSCI_XMLNS))
      osciNSPrefix = prefix;
    else if (uri.equals(OSCIMessageBuilder.OSCI_2019_XMLNS))
      osci2019NSPrefix = prefix;
    else if (uri.equals(Namespaces.OSCI2017.getUri()))
      osci2017NSPrefix= prefix;
    else if (uri.equals(OSCIMessageBuilder.DS_XMLNS))
      dsNSPrefix = prefix;
    else if (uri.equals(OSCIMessageBuilder.XENC_XMLNS))
      xencNSPrefix = prefix;

    ns.append(" xmlns:" + prefix + "=\"" + uri + "\"");
  }

  /**
   *  Start element.
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
      log.debug("Start-Element: " + localName + "\n" + uri + "\n" + qName + attributes.getLength());

    // SOAP-Namespacedefinition suchen
    if ((soapNSPrefix == null) || !localName.equals("Envelope"))
      throw new SAXException(DialogHandler.text.getString("msg_format_error") + ": " + localName);

    String schemaLocation = attributes.getValue("xsi:schemaLocation");

    if (log.isDebugEnabled())
      log.debug("Nachrichtentyp: " + schemaLocation);

    try
    {
      if (schemaLocation == null)
      {
        childBuilder = new SOAPFaultBuilder(this);
      }
      else if (schemaLocation.equals(XSD_ENCRYPTED_DATA))
      {
        try
        {
          childBuilder = new SOAPMessageEncryptedBuilder(this);
        }
        catch (Exception ex)
        {
          throw new SAXException(ex);
        }
      }
      else if (schemaLocation.equals(XSD_ACCEPT_DELIVERY))
      {
        childBuilder = new AcceptDeliveryBuilder(this);
      }
      else if (schemaLocation.equals(XSD_RSP_FORWARD_DELIVERY))
      {
        childBuilder = new ResponseToForwardDeliveryBuilder(this);
      }
      else if (schemaLocation.equals(XSD_RSP_INIT_DIALOG))
      {
        childBuilder = new ResponseToInitDialogBuilder(this);
      }
      else if (schemaLocation.equals(XSD_PROCESS_DELIVERY))
      {
        childBuilder = new ProcessDeliveryBuilder(this);
      }
      else if (schemaLocation.equals(XSD_RSP_MEDIATE_DELIVERY))
      {
        childBuilder = new ResponseToMediateDeliveryBuilder(this);
      }
      else if (schemaLocation.equals(XSD_RSP_GET_MSG_ID))
      {
        childBuilder = new ResponseToGetMessageIdBuilder(this);
      }
      else if (schemaLocation.equals(XSD_RSP_EXIT_DIALOG))
      {
        childBuilder = new ResponseToExitDialogBuilder(this);
      }
      else if (schemaLocation.equals(XSD_RSP_STORE_DELIVERY))
      {
        childBuilder = new ResponseToStoreDeliveryBuilder(this);
      }
      else if (schemaLocation.equals(XSD_RSP_FETCH_DELIVERY))
      {
        childBuilder = new ResponseToFetchDeliveryBuilder(this);
      }
      else if (schemaLocation.equals(XSD_RSP_FETCH_PROCESS_CARD))
      {
        childBuilder = new ResponseToFetchProcessCardBuilder(this);
      }
      else if (schemaLocation.equals(XSD_RSP_PARTIAL_STORE_DELIVERY))
      {
        childBuilder = new ResponseToPartialStoreDeliveryBuilder(this);
      }
      else if (schemaLocation.equals(XSD_RSP_PARTIAL_FETCH_DELIVERY))
      {
        childBuilder = new ResponseToPartialFetchDeliveryBuilder(this);
      }
      else
      {
        log.error("Falsche OSCI-Nachricht. Nachrichtentyp nicht bekannt! Nachrichtentypt ist:\n" + schemaLocation +
                  "\n" + (XSD_RSP_FETCH_PROCESS_CARD));
        throw new SAXException(DialogHandler.text.getString("sax_exception_msgtype"));
      }

      this.xmlReader.setContentHandler(childBuilder);
      childBuilder.getOSCIMessage().soapNSPrefix = soapNSPrefix;
      childBuilder.getOSCIMessage().osciNSPrefix = osciNSPrefix;
      childBuilder.getOSCIMessage().osci2017NSPrefix = osci2017NSPrefix;
      childBuilder.getOSCIMessage().osci2019NSPrefix = osci2019NSPrefix;
      childBuilder.getOSCIMessage().dsNSPrefix = dsNSPrefix;
      childBuilder.getOSCIMessage().xencNSPrefix = xencNSPrefix;
      childBuilder.getOSCIMessage().ns = ns.toString();
   // hier werden beide Variablen des Namespace gesetzt
//      childBuilder.getOSCIMessage().ns2017 = ns.toString();
    }
    catch (NoSuchAlgorithmException ex)
    {
      throw new SAXException(ex);
    }
    catch (OSCIRoleException ex)
    {
      throw new SAXException(ex);
    }
  }

  /**
   * undocumented
   */
  public void endDocument()
  {
    childBuilder.getOSCIMessage().hashableMsgPart = hashNCanStream.getDigestValues();
    childBuilder.getOSCIMessage().parsedMsgPartsIds= childBuilder.parsedMsgPartsIds;
    if (childBuilder.getOSCIMessage().signatureHeader != null)
      childBuilder.getOSCIMessage().signatureHeader.signedInfo = (byte[]) hashNCanStream.getSignedInfos().remove(0);

    childBuilder.getOSCIMessage().stateOfMsg |= OSCIMessage.STATE_PARSED;
  }

  /**
   * undocumented
   *
   * @return undocumented
   */
  public XMLReader getXmlReader()
  {
    return xmlReader;
  }
}
