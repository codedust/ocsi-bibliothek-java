package de.osci.osci12.messagetypes;

import java.io.IOException;
import java.io.OutputStream;

import de.osci.osci12.common.Constants;
import de.osci.osci12.common.DialogHandler;
import de.osci.osci12.common.OSCIExceptionCodes.OSCIErrorCodes;


/**
 * <p>Dieses Klasse repräsentiert eine SOAP-Fehlermeldung auf Nachrichtenebene.</p>
 * <p>Copyright © 2023 Governikus GmbH &amp; Co. KG, Germany</p>
 * <p>Erstellt von Governikus GmbH &amp; Co. KG</p>
 * <p>Diese Bibliothek kann von jedermann nach Maßgabe der European Union
 *  Public Licence genutzt werden.</p><p>Die Lizenzbestimmungen können
 * unter der URL <a href="https://eupl.eu/">https://eupl.eu/</a> abgerufen werden.
 *
 *
 * @author R. Lindemann, N. Büngener
 * @version 2.4.1
 */
public class SOAPFault extends OSCIResponseTo
{
  //  private static Log log = LogFactory.getLog(ResponseToExitDialog.class);
  private static final String soapFaultIntro = "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:osci=\"http://www.osci.de/2002/04/osci\"><soap:Body><soap:Fault><faultcode>soap:";
  private static final String soapFaultExtro = "</osci:Code></detail></soap:Fault></soap:Body></soap:Envelope>";
  private String oscicode;
  private String soapfault;

  /**
   * Legt ein SOAP-Fehlerobjekt für den genannten OSCI-Code an.
   * @param oscicode OSCI-Fehlercode (s. Spezifikation)
   */
  public SOAPFault(String oscicode)
  {
    super();
    this.oscicode = oscicode;
    messageType = RESPONSE_TO_EXIT_DIALOG;
  }

  /**
   * undocumented
   */
  protected void compose()
  {
    soapfault = soapFaultIntro;

    if (oscicode.equals(OSCIErrorCodes.SoapServerInternalErrorSupplier.getOSCICode()) || oscicode.equals(OSCIErrorCodes.SoapServerInternalErrorSupplierOnEncCertClient.getOSCICode()))
      soapfault += "Server";
    else
      soapfault += "Client";

    soapfault += ("</faultcode><faultstring>" + DialogHandler.text.getString(oscicode) +
    "</faultstring><detail><osci:Code>" + oscicode + soapFaultExtro);
    stateOfMsg |= STATE_COMPOSED;
  }

  /**
   * undocumented
   *
   * @param out undocumented
   *
   * @throws IOException undocumented
   */
  protected void writeXML(OutputStream out) throws IOException
  {
    compose();
    out.write(("\r\nMIME-Version: 1.0\r\nContent-Type: Multipart/Related; boundary=" + DialogHandler.boundary
               +
               "; type=text/xml\r\n").getBytes(Constants.CHAR_ENCODING));
    out.write(("\r\n--" + DialogHandler.boundary
               + "\r\nContent-Type: text/xml; charset=UTF-8\r\n").getBytes(Constants.CHAR_ENCODING));
    out.write(("Content-Transfer-Encoding: 8bit\r\nContent-ID: <" + contentID + ">\r\n\r\n").getBytes(Constants.CHAR_ENCODING));
    out.write(("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n\r\n").getBytes(Constants.CHAR_ENCODING));
    out.write(soapfault.getBytes(Constants.CHAR_ENCODING));
    out.write(("\r\n--" + DialogHandler.boundary + "--\r\n").getBytes(Constants.CHAR_ENCODING));
  }

  /**
   * Mit dieser Methode können passive Empfänger SOAP-Fehlernachrichten an
   * den Intermediär zurückschicken.
   * @param out OutputStream
   * @throws IOException bei Schreibfehlern
   */
  public void writeToStream(OutputStream out) throws IOException
  {
    writeXML(out);
  }
}
