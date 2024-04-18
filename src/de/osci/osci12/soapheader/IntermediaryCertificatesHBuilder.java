package de.osci.osci12.soapheader;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.osci.helper.Base64InputStream;
import de.osci.helper.ParserHelper;
import de.osci.osci12.common.Constants.HeaderTags;
import de.osci.osci12.common.DialogHandler;
import de.osci.osci12.messagetypes.OSCIMessage;
import de.osci.osci12.messagetypes.OSCIMessageBuilder;
import de.osci.osci12.roles.Intermed;


/**
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
public class IntermediaryCertificatesHBuilder extends de.osci.osci12.messageparts.MessagePartParser
{
  private static Log log = LogFactory.getLog(IntermediaryCertificatesHBuilder.class);
  private static final int CIPHER_CERTIFICATE_INTERMEDIARY = 0;
  private static final int SIGNATURE_CERTIFICATE_INTERMEDIARY = 1;
  private int[] check;
  private IntermediaryCertificatesH ich;
  private int typ;
  private String refId;
  private Intermed intermed;

  //  private String tmpId;
  /**
   *  Constructor for the IntermediaryCertificatesHBuilder object
   *
   *@param  parentHandler             Description of Parameter
   *@param  atts                      Description of Parameter
   *@param  check                     Description of Parameter
   * @throws SAXException
   */
  public IntermediaryCertificatesHBuilder(OSCIMessageBuilder parentHandler, Attributes atts, int[] check) throws SAXException
  {
    super(parentHandler);
    parentHandler.addFoundMsgPartIds(atts.getValue("Id"),HeaderTags.IntermediaryCertificates.getNamespace().getUri() +":"+HeaderTags.IntermediaryCertificates.getElementName());
    this.check = check;
    ich = new IntermediaryCertificatesH();

    OSCIMessage msg = parentHandler.getOSCIMessage();
    ich.setNSPrefixes(msg);

    int msgTyp = msg.getMessageType();

    if ((msgTyp == OSCIMessage.ACCEPT_DELIVERY) || (msgTyp == OSCIMessage.RESPONSE_TO_ACCEPT_DELIVERY) ||
            (msgTyp == OSCIMessage.PROCESS_DELIVERY) || (msgTyp == OSCIMessage.RESPONSE_TO_PROCESS_DELIVERY))
    {
      intermed = (Intermed) msg.dialogHandler.getClient();

      if (intermed == null)
      {
        intermed = new Intermed((X509Certificate) null, (X509Certificate) null, null);
        msg.dialogHandler.client = intermed;
      }
    }
    else
      intermed = (Intermed) msg.dialogHandler.getSupplier();

    if (atts.getValue("Id") != null)
      ich.setRefID(atts.getValue("Id"));
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
      log.debug("Start-Element: " + localName);

    if (localName.equals("CipherCertificateIntermediary") && uri.equals(OSCI_XMLNS))
    {
      typ = CIPHER_CERTIFICATE_INTERMEDIARY;
    }
    else if (localName.equals("SignatureCertificateIntermediary") && uri.equals(OSCI_XMLNS))
    {
      typ = SIGNATURE_CERTIFICATE_INTERMEDIARY;
    }
    else if (localName.equals("X509Certificate") && uri.equals(DS_XMLNS))
    {
      currentElement = new StringBuffer();
      //      tmpId = attributes.getValue("Id");
    }
    else if (!(localName.equals("X509Data") && uri.equals(DS_XMLNS)))
    {
      throw new SAXException(DialogHandler.text.getString("unexpected_entry") + ": " + localName);
    }

    if (check[typ] == 0)
    {
      throw new SAXException(DialogHandler.text.getString("unexpected_entry") + ": IntermediaryCertificate");
    }

    if (attributes.getValue("Id") != null)
      refId = attributes.getValue("Id");
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
    try
    {
      if (log.isDebugEnabled())
        log.debug("End-Element: " + localName);

      if (ParserHelper.isElement(HeaderTags.IntermediaryCertificates,localName,uri))
      {
        if (log.isDebugEnabled())
          log.debug("Setze IntermedCertificates");
        msg.intermediaryCertificatesH = ich;
        xmlReader.setContentHandler(parentHandler);
      }
      else if (localName.equals("X509Certificate") && uri.equals(DS_XMLNS))
      {
        X509Certificate cert;

        try
        {
          ByteArrayInputStream byteStream = new ByteArrayInputStream(currentElement.toString().getBytes(de.osci.osci12.common.Constants.CHAR_ENCODING));
          Base64InputStream base64In = new Base64InputStream(byteStream);
          cert = de.osci.helper.Tools.createCertificate(base64In);
        }
        catch (CertificateException ex)
        {
          throw new SAXException(DialogHandler.text.getString("cert_gen_error"), ex);
        }

        if (typ == CIPHER_CERTIFICATE_INTERMEDIARY)
        {
          if (intermed.hasCipherCertificate())
          {
            if (!intermed.getCipherCertificate().equals(cert))
              throw new IllegalStateException(DialogHandler.text.getString("not_matching_cipher_certs") +
                                              " Intermediary");
          }
          else
            intermed.setCipherCertificate(cert);

          intermed.cipherRefId = refId;
          ich.setCipherCertificateIntermediary(intermed);
        }
        else if (typ == SIGNATURE_CERTIFICATE_INTERMEDIARY)
        {
          if (intermed.hasSignatureCertificate())
          {
            if (!intermed.getSignatureCertificate().equals(cert))
              throw new IllegalStateException(DialogHandler.text.getString("not_matching_sig_certs") + " Intermediary");
          }
          else
            intermed.setSignatureCertificate(cert);

          intermed.signatureRefId = refId;
          ich.setSignatureCertificateIntermediary(intermed);
        }
      }
    }
    catch (SAXException ex)
    {
      throw ex;
    }
    catch (Exception ex)
    {
      if (log.isDebugEnabled())
        log.debug("Fehler im End-Element.", ex);

      throw new SAXException(ex);
    }

    typ = -1;
    currentElement = null;
  }
}
