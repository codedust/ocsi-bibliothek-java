package de.osci.osci12.samples;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;

import de.osci.osci12.OSCIException;
import de.osci.osci12.common.DialogHandler;
import de.osci.osci12.messageparts.Content;
import de.osci.osci12.messageparts.ContentContainer;
import de.osci.osci12.messagetypes.ExitDialog;
import de.osci.osci12.messagetypes.GetMessageId;
import de.osci.osci12.messagetypes.InitDialog;
import de.osci.osci12.messagetypes.MediateDelivery;
import de.osci.osci12.messagetypes.ResponseToGetMessageId;
import de.osci.osci12.messagetypes.ResponseToMediateDelivery;
import de.osci.osci12.roles.Addressee;
import de.osci.osci12.roles.Intermed;
import de.osci.osci12.roles.Originator;


/**
 * This is a demo application for a synchronous communication scenario
 * (request-response) according to the OSCI 1.2-trensport specification.
 * The main method's first parameter is the intermediary's URL, the second one
 * is the recipient's URL.
 *
 * <p>Copyright © 2023 Governikus GmbH &amp; Co. KG, Germany</p>
 * <p>Erstellt von Governikus GmbH &amp; Co. KG</p>
 * <p>Diese Bibliothek kann von jedermann nach Maßgabe der European Union
 *  Public Licence genutzt werden.</p><p>Die Lizenzbestimmungen können
 * unter der URL <a href="https://eupl.eu/">https://eupl.eu/</a> abgerufen werden.</p>
 *
 * @author N.Büngener
 * @version 0.9
 */
public class RequestResponse
{
  Intermed intermed;

  /*
   * Constructor for demo application
   * @param intermedURL Intermediary-URL
   * @throws CertificateException if the certificate cannot be parsed
   * @throws URISyntaxException URL syntax errors
   */
  public RequestResponse(String intermedURL) throws CertificateException,
                                                    URISyntaxException
  {
    // First we need the Intermediarys Cipher-Certificate
    java.security.cert.X509Certificate intermedCipherCert = de.osci.helper.Tools.createCertificate(getClass()
                                                                                                   .getResourceAsStream("/de/osci/osci12/samples/zertifikate/osci_manager_cipher_4096.cer"));

    // Create the Intermed-role object
    intermed = new Intermed(null, intermedCipherCert, new java.net.URI(intermedURL));
  }

  /**
   * Sends an OSCI-MediateDelivery message to the intermediary.
   * @return the OSCI-message response object
   * @throws GeneralSecurityException in case of cryptographic / security errors
   * @throws IOException write / read errors
   * @throws OSCIException any errors concerning the OSCI-message processing
   */
  public ResponseToMediateDelivery sendMediateDelivery(String urlRecipient)
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

    new InitDialog(clientDialog).send();

    // Now we have a message-Id, we are ready to send the MediateDelivery-request
    // First we create the Addressee-object representing the user 2.
    // The signature certificate is not needed since we do not
    // expect a signed response from the addressee in this secenario
    // (it would be included in a signed response anyway).
    Addressee user_2 = new Addressee(null,
                                     de.osci.helper.Tools.createCertificate(getClass().getResourceAsStream("/de/osci/osci12/samples/zertifikate/bob_cipher_4096.cer")));

    // Create the MediateDelivery-message object
    // The constructor's second parameter (Addressee-object) may be null.
    // In this case you have to disable transport encryption calling
    // clientDialog.setEncryption(false);
    // This is usually not recommended. As a side effect, the recipient will not
    // be able to fetch the process card later from the intermediary.
    MediateDelivery mediateDel = new MediateDelivery(clientDialog, user_2, urlRecipient);

    // If the next lines are removed, we will have no recording
    mediateDel.setMessageId(rsp2GetMsgID.getMessageId());
    mediateDel.setSubject("Subject");
    // We trust the intermediary's time stamp
    mediateDel.setQualityOfTimeStampCreation(false);
    mediateDel.setQualityOfTimeStampReception(false);

    // Add Content-Data
    ContentContainer data = new ContentContainer();
    data.addContent(new Content("Any content data."));
    mediateDel.addContentContainer(data);

    // Transmit the request and receive the response
    ResponseToMediateDelivery rsp2MedDel = mediateDel.send();

    if (!rsp2MedDel.getFeedback()[0][1].startsWith("0"))
    {
      // Error handling
    }

    // Optional: Check process card bundle (result of certificate check etc.)
    //    rsp2MedDel.getProcessCardBundleRequest();
    //    rsp2MedDel.getProcessCardBundleReply();
    // ....
    // cleanup
    new ExitDialog(clientDialog).send();

    return rsp2MedDel;
  }

  /**
   * Main entry.
   * @param args two strings are required:<ul><li>intermediary URL</li><li>recipient URL</li></ul>
   */
  public static void main(String[] args)
  {
    try
    {
      RequestResponse scenario_3 = new RequestResponse(args[0]);
      ResponseToMediateDelivery responseMediate = scenario_3.sendMediateDelivery(args[1]);
      System.out.println("\nResponseToStoreDelivery:\n" + responseMediate.toString());
      System.out.println("\nInhaltsdaten:\n" +
                         responseMediate.getContentContainer()[0].getContents()[0].getContentData());
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
}
