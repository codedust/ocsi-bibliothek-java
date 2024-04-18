package de.osci.osci12.samples;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;

import de.osci.osci12.OSCIException;
import de.osci.osci12.common.DialogHandler;
import de.osci.osci12.messageparts.Attachment;
import de.osci.osci12.messageparts.Content;
import de.osci.osci12.messageparts.ContentContainer;
import de.osci.osci12.messagetypes.ForwardDelivery;
import de.osci.osci12.messagetypes.GetMessageId;
import de.osci.osci12.messagetypes.ResponseToForwardDelivery;
import de.osci.osci12.messagetypes.ResponseToGetMessageId;
import de.osci.osci12.roles.Addressee;
import de.osci.osci12.roles.Intermed;
import de.osci.osci12.roles.Originator;


/**
 * This is a demo application for a synchronous communication scenario
 * (one-way-message, passive recipient) according to the OSCI 1.2-trensport
 * specification.
 * The main method's first parameter is the intermediary's URL, the second one
 * is the recipient's URL.
 *
 * <p>Copyright © 2023 Governikus GmbH &amp; Co. KG, Germany</p>
 * <p>Erstellt von Governikus GmbH &amp; Co. KG</p>
 * <p>Diese Bibliothek kann von jedermnn nach Maßgabe der European Union
 *  Public Licence genutzt werden.</p><p>Die Lizenzbestimmungen können
 * unter der URL <a href="https://eupl.eu/">https://eupl.eu/</a> abgerufen werden.</p>
 *
 * @author N.Büngener
 * @version 0.9
 */
public class OneWayMessage_PassiveRecipient
{
  Intermed intermed;

  /*
   * Constructor for demo application
   * @param intermedURL Intermediary-URL
   * @throws CertificateException if the certificate cannot be parsed
   * @throws URISyntaxException URL syntax errors
   */
  public OneWayMessage_PassiveRecipient(String intermedURL)
                                 throws Exception
  {
    // First we need the Intermediarys Cipher-Certificate
    java.security.cert.X509Certificate intermedCipherCert = de.osci.helper.Tools.createCertificate(getClass()
                                                                                                   .getResourceAsStream("/de/osci/osci12/samples/zertifikate/osci_manager_cipher_4096.cer"));

    // Create the Intermed-role object
    intermed = new Intermed(null, intermedCipherCert, new java.net.URI(intermedURL));
  }

  /**
   * Sends an OSCI-ForwardDelivery message to the intermediary.
   * @return the OSCI-message response object
   * @throws GeneralSecurityException in case of cryptographic / security errors
   * @throws IOException write / read errors
   * @throws OSCIException any errors concerning the OSCI-message processing
   * @throws URISyntaxException URL syntax errors
   */
  public ResponseToForwardDelivery sendForwardDelivery(String urlRecipient)
                                                throws GeneralSecurityException,
                                                       IOException,
                                                       OSCIException,
                                                       URISyntaxException
  {
    // Create the Originator-object. Since we have the private key(s), we use this
    // Constructor. We need a Decrypter-object for decryption of response
    Originator user_1 = new Originator(new de.osci.osci12.samples.impl.crypto.PKCS12Signer("/de/osci/osci12/samples/zertifikate/alice_signature_4096.p12",
                                                                                           "123456"),
                                       new de.osci.osci12.samples.impl.crypto.PKCS12Decrypter("/de/osci/osci12/samples/zertifikate/carol_cipher_4096.p12",
                                                                                              "123456"));

    // Create a DialogHandler
    // Default configuration is transport encryption and signature activated
    DialogHandler clientDialog = new DialogHandler(user_1, intermed, new de.osci.osci12.samples.impl.HttpTransport());

    // Create the GetMessageId-Request
    GetMessageId getMsgID = new GetMessageId(clientDialog);

    // Transmit the request and receive the response
    ResponseToGetMessageId rsp2GetMsgID = getMsgID.send();

    // Error handling
    if (!rsp2GetMsgID.getFeedback()[0][1].startsWith("0"))
    {
      // do error handling
    }

    // Now we have a message-Id, we are ready to send the MediateDelivery-request
    // First we create the Addressee-object representing the user 2.
    // The signature certificate is not needed since we do not
    // expect a signed response from the addressee in this secenario
    // (it would be included in a signed response anyway).
    Addressee user_2 = new Addressee(null,
                                     de.osci.helper.Tools.createCertificate(getClass().getResourceAsStream("/de/osci/osci12/samples/zertifikate/bob_cipher_4096.cer")));

    // Create the ForwardDelivery-message object
    // The constructor's second parameter (Addressee-object) may be null.
    // In this case transport encryption must be disabled by calling
    // clientDialog.setEncryption(false);
    // This is usually not recommended. As a side effect, the recipient will not
    // be able to fetch the process card later from the intermediary.
    ForwardDelivery forwardDel = new ForwardDelivery(clientDialog, user_2, urlRecipient, rsp2GetMsgID.getMessageId());
    forwardDel.setSubject("Subject");
    // We trust the intermediary's time stamp
    forwardDel.setQualityOfTimeStampCreation(false);
    forwardDel.setQualityOfTimeStampReception(false);

    // Add Content-Data
    ContentContainer data = new ContentContainer();
    data.addContent(new Content("Any content data."));
    data.addContent(new Content(new Attachment(new ByteArrayInputStream("Any attached data".getBytes()), "attachment_id")));
    forwardDel.addContentContainer(data);

    // Transmit the request and receive the response
    ResponseToForwardDelivery rsp2FwdDel = forwardDel.send();

    if (!rsp2FwdDel.getFeedback()[0][1].startsWith("0"))
    {
      // Error handling
    }

    // Optional: Check process card bundle (result of certificate check etc.)
    //    rsp2FwdDel.getProcessCardBundle();
    // ....
    return rsp2FwdDel;
  }

  /**
   * Main entry.
   * @param args two strings are required:<ul><li>intermediary URL</li><li>recipient URL</li></ul>
   */
  public static void main(String[] args)
  {
    try
    {
      OneWayMessage_PassiveRecipient scenario_2 = new OneWayMessage_PassiveRecipient(args[0]);
      ResponseToForwardDelivery responseForward = scenario_2.sendForwardDelivery(args[1]);
      System.out.println("\nResponseToForwardDelivery:\n" + responseForward.toString());
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
}
