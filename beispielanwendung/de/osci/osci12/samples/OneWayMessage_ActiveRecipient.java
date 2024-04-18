package de.osci.osci12.samples;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;

import de.osci.osci12.OSCIException;
import de.osci.osci12.common.Constants;
import de.osci.osci12.common.DialogHandler;
import de.osci.osci12.messageparts.Attachment;
import de.osci.osci12.messageparts.Content;
import de.osci.osci12.messageparts.ContentContainer;
import de.osci.osci12.messageparts.EncryptedDataOSCI;
import de.osci.osci12.messagetypes.ExitDialog;
import de.osci.osci12.messagetypes.FetchDelivery;
import de.osci.osci12.messagetypes.FetchProcessCard;
import de.osci.osci12.messagetypes.GetMessageId;
import de.osci.osci12.messagetypes.InitDialog;
import de.osci.osci12.messagetypes.OSCIMessage;
import de.osci.osci12.messagetypes.ResponseToExitDialog;
import de.osci.osci12.messagetypes.ResponseToFetchDelivery;
import de.osci.osci12.messagetypes.ResponseToFetchProcessCard;
import de.osci.osci12.messagetypes.ResponseToGetMessageId;
import de.osci.osci12.messagetypes.ResponseToInitDialog;
import de.osci.osci12.messagetypes.ResponseToStoreDelivery;
import de.osci.osci12.messagetypes.StoreDelivery;
import de.osci.osci12.roles.Addressee;
import de.osci.osci12.roles.Intermed;
import de.osci.osci12.roles.Originator;
import de.osci.osci12.roles.Reader;


/**
 * This is a demo application for an asynchronous communication scenario
 * according to the OSCI 1.2-transport specification. The main method needs
 * the intermediary's URL as parameter.
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
public class OneWayMessage_ActiveRecipient
{
  Intermed intermed;

  /*
   * Constructor for demo application
   * @param intermedURL Intermediary-URL
   * @throws CertificateException if the certificate cannot be parsed
   * @throws URISyntaxException URL syntax errors
   */
  public OneWayMessage_ActiveRecipient(String intermedURL)
                                throws CertificateException,
                                       URISyntaxException
  {
    // First we need the Intermediarys Cipher-Certificate
    java.security.cert.X509Certificate intermedCipherCert = de.osci.helper.Tools.createCertificate(getClass()
                                                                                                   .getResourceAsStream("/de/osci/osci12/samples/zertifikate/osci_manager_cipher_4096.cer"));

    // Create the Intermed-role object
    // The signature certificate (for signature checks) is always
    // enclosed in signed responses, so this parameter can be null
    intermed = new Intermed(null, intermedCipherCert, new java.net.URI(intermedURL));
  }

  /**
   * Sends an OSCI-StoreDelivery message to the intermediary.
   * @return the OSCI-message response object
   * @throws GeneralSecurityException in case of cryptographic / security errors
   * @throws IOException write / read errors
   * @throws OSCIException any errors concerning the OSCI-message processing
   */
  public ResponseToStoreDelivery sendStoreDelivery() throws GeneralSecurityException,
                                                            IOException,
                                                            OSCIException
  {
    // Create the Originator-object. Since we have the private key(s), we use this
    // Constructor. We need a Decrypter-object for decryption of response
    Originator user_1 = new Originator(new de.osci.osci12.samples.impl.crypto.PKCS12Signer("/de/osci/osci12/samples/zertifikate/alice_signature_4096.p12",
                                                                                           "123456"),
                                       new de.osci.osci12.samples.impl.crypto.PKCS12Decrypter("/de/osci/osci12/samples/zertifikate/carol_cipher_4096.p12",
                                                                                              "123456"));

    // Create a DialogHandler
    DialogHandler clientDialog = new DialogHandler(user_1, intermed, new de.osci.osci12.samples.impl.HttpTransport());

    // Default configuration is transport encryption and signature activated
    //clientDialog.setCreateSignatures(false);
    // Create the GetMessageId-Request
    GetMessageId getMsgID = new GetMessageId(clientDialog);

    // Transmit the request and receive the response
    ResponseToGetMessageId rsp2GetMsgID = getMsgID.send();

    // Error handling
    if (!rsp2GetMsgID.getFeedback()[0][1].startsWith("0"))
    {
      // do error handling
    }

    // Now we have a message-Id, we are ready to send the StoreDelivery-request
    // First we need to create the Addressee-object representing the user 2.
    // The signature certificate is not needed since we do not
    // expect a signed response from the addressee in this secenario
    // (it would be included in a signed response anyway).
    Addressee user_2 = new Addressee(null,
                                     de.osci.helper.Tools.createCertificate(getClass().getResourceAsStream("/de/osci/osci12/samples/zertifikate/bob_cipher_4096.cer")));

    // Create the StoreDelivery-message object
    StoreDelivery storeDel = new StoreDelivery(clientDialog, user_2, rsp2GetMsgID.getMessageId());
    storeDel.setSubject("Subject");
    // We trust the intermediary's time stamp (default)
    storeDel.setQualityOfTimeStampCreation(false);
    storeDel.setQualityOfTimeStampReception(false);

    // Add not encrypted Content-Data
    ContentContainer not_encrypted_container = new ContentContainer();
    not_encrypted_container.addContent(new Content("Any content data."));
    not_encrypted_container.addContent(new Content(new Attachment(new ByteArrayInputStream("Any unencrypted attachment data.".getBytes()),
                                                                  "test.txt")));
    not_encrypted_container.sign(user_1);
    storeDel.addContentContainer(not_encrypted_container);

    // Add encrypted Content-Data
    Reader reader = new Reader(de.osci.helper.Tools.createCertificate(getClass().getResourceAsStream("/de/osci/osci12/samples/zertifikate/dave_cipher_4096.cer")));
    ContentContainer encrypted_container = new ContentContainer();
    encrypted_container.addContent(new Content("Any encrypted content data."));
    encrypted_container.addContent(new Content(new Attachment(new ByteArrayInputStream("Any encrypted attachment data.".getBytes()),
                                                              "enc_test.txt",
                                                              Constants.SYMMETRIC_CIPHER_ALGORITHM_AES256_GCM)));

    EncryptedDataOSCI encryptedData = new EncryptedDataOSCI(Constants.SYMMETRIC_CIPHER_ALGORITHM_AES256_GCM,
                                                            encrypted_container);
    encryptedData.encrypt(reader);
    storeDel.addEncryptedData(encryptedData);

    // Transmit the request and receive the response
    ResponseToStoreDelivery rsp2StoreDel = storeDel.send();

    if (!rsp2StoreDel.getFeedback()[0][1].startsWith("0"))
    {
      // Error handling
    }

    // Optional: Check process card bundle (result of certificate check etc.)
    rsp2StoreDel.getInspections();
    rsp2StoreDel.getTimestampCreation();
    rsp2StoreDel.getRecentModification();

    // ....
    return rsp2StoreDel;
  }

  /**
   * Sends an OSCI-FetchDelivery message to the intermediary.
   * @return the OSCI-message response object
   * @throws GeneralSecurityException in case of cryptographic / security errors
   * @throws IOException write / read errors
   * @throws OSCIException any errors concerning the OSCI-message processing
   */
  public ResponseToFetchDelivery sendFetchDelivery(String messageId)
                                            throws GeneralSecurityException,
                                                   IOException,
                                                   OSCIException
  {
    // Create the Originator-object. Since we have the private key(s), we use this
    // Constructor. We need a Decrypter-object for decryption of response
    Originator user_2 = new Originator(new de.osci.osci12.samples.impl.crypto.PKCS12Signer("/de/osci/osci12/samples/zertifikate/bob_signature_4096.p12",
                                                                                           "123456"),
                                       new de.osci.osci12.samples.impl.crypto.PKCS12Decrypter("/de/osci/osci12/samples/zertifikate/bob_cipher_4096.p12",
                                                                                              "123456"));

    // Create a DialogHandler
    DialogHandler clientDialog = new DialogHandler(user_2, intermed, new de.osci.osci12.samples.impl.HttpTransport());

    // Default configuration is transport encryption and signature activated
    //    clientDialog.setCreateSignatures(false);
    //    clientDialog.setEncryption(false);
    // Create the InitDialog-Request
    InitDialog id = new InitDialog(clientDialog);

    // Transmit the request and receive the response
    ResponseToInitDialog rsp2InitDialog = id.send();

    // Error handling
    if (!rsp2InitDialog.getFeedback()[0][1].startsWith("0"))
    {
      // Error handling
    }

    // Now the dialog is initialized. We can authenticate ourselves by
    // returning the challenge, so we are ready to send the FetchDelivery-request
    // Create the FetchDelivery-message object
    FetchDelivery fetchDel = new FetchDelivery(clientDialog);
    fetchDel.setSelectionMode(OSCIMessage.SELECT_BY_MESSAGE_ID);
    fetchDel.setSelectionRule(messageId);

    // Transmit the request and receive the response
    ResponseToFetchDelivery rsp2FetchDel = fetchDel.send();

    if (!rsp2FetchDel.getFeedback()[0][1].startsWith("0"))
    {
      // Error handling
    }

    // Optional: Check process card bundle (result of certificate check etc.)
    //    rsp2FetchDel.getInspections();
    //    rsp2FetchDel.getTimestampCreation();
    //    rsp2FetchDel.getRecentModification();
    //    rsp2FetchDel.getContentContainer();
    //    rsp2FetchDel.getAttachments();
    // ....
    // Cleanup - Create the ExitDialog-Request
    ExitDialog ed = new ExitDialog(clientDialog);

    // Transmit the request and receive the response
    ResponseToExitDialog rsp2ExitDialog = ed.send();

    return rsp2FetchDel;
  }

  /**
   * Sends an OSCI-FetchProcessCard message to the intermediary.
   * @return the OSCI-message response object
   * @throws GeneralSecurityException in case of cryptographic / security errors
   * @throws IOException write / read errors
   * @throws OSCIException any errors concerning the OSCI-message processing
   */
  public ResponseToFetchProcessCard sendFetchProcessCard(String messageId)
                                                  throws GeneralSecurityException,
                                                         IOException,
                                                         OSCIException
  {
    // Create the Originator-object.
    Originator user_1 = new Originator(new de.osci.osci12.samples.impl.crypto.PKCS12Signer("/de/osci/osci12/samples/zertifikate/alice_signature_4096.p12",
                                                                                           "123456"),
                                       new de.osci.osci12.samples.impl.crypto.PKCS12Decrypter("/de/osci/osci12/samples/zertifikate/carol_cipher_4096.p12",
                                                                                              "123456"));

    // Create a DialogHandler
    DialogHandler clientDialog = new DialogHandler(user_1, intermed, new de.osci.osci12.samples.impl.HttpTransport());

    // Create the InitDialog-Request
    InitDialog id = new InitDialog(clientDialog);

    // Transmit the request and receive the response
    ResponseToInitDialog rsp2InitDialog = id.send();

    // Error handling
    if (!rsp2InitDialog.getFeedback()[0][1].startsWith("0"))
    {
      // do error handling
    }

    // Now the dialog is initialized. We can authenticate ourselves by
    // returning the challenge, so we are ready to send the FetchProcessCard-request
    // Create the FetchProcessCard-message object
    FetchProcessCard fetchProcCard = new FetchProcessCard(clientDialog);
    fetchProcCard.setSelectionMode(OSCIMessage.SELECT_BY_MESSAGE_ID);
    fetchProcCard.setSelectionRule(messageId);

    // Transmit the request and receive the response
    ResponseToFetchProcessCard rsp2FetchProcCard = fetchProcCard.send();

    if (!rsp2FetchProcCard.getFeedback()[0][1].startsWith("0"))
    {
      // Error handling
    }

    // Optional: Check process card bundle (result of certificate check etc.)
    rsp2FetchProcCard.getProcessCardBundles();

    // ....
    // Cleanup - Create the ExitDialog-Request
    ExitDialog ed = new ExitDialog(clientDialog);

    // Transmit the request and receive the response
    ResponseToExitDialog rsp2ExitDialog = ed.send();

    return rsp2FetchProcCard;
  }

  /**
   * Main entry.
   * @param args must contain one string with the intermediary URL
   */
  public static void main(String[] args)
  {
    try
    {
      OneWayMessage_ActiveRecipient scenario_1 = new OneWayMessage_ActiveRecipient(args[0]);
      ResponseToStoreDelivery responseStore = scenario_1.sendStoreDelivery();
      System.out.println("\nResponseToStoreDelivery:\n" + responseStore.toString());

      ResponseToFetchDelivery responseFetchDel = scenario_1.sendFetchDelivery(responseStore.getMessageId());
      Reader reader = new Reader(new de.osci.osci12.samples.impl.crypto.PKCS12Decrypter("/de/osci/osci12/samples/zertifikate/dave_cipher_4096.p12",
                                                                                        "123456"));
      System.out.println("\nResponseToFetchDelivery:\n" + responseFetchDel.toString());

      ResponseToFetchProcessCard responseFetchProcCard = scenario_1.sendFetchProcessCard(responseStore.getMessageId());
      System.out.println("\nResponseToFetchProcessCard:\n" + responseFetchProcCard.toString());
      // When a message of unknown content structure is received, the content types must
      // be analyzed (e.g. using Content.getContentType())
      System.out.println("\nCONTENT DATA:\n" +
                         responseFetchDel.getContentContainer()[0].getContents()[0].getContentData());

      InputStream in = responseFetchDel.getContentContainer()[0].getContents()[1].getAttachment().getStream();
      StringBuffer attachment = new StringBuffer();
      int i;

      while ((i = in.read()) > -1)
        attachment.append((char) i);

      in.close();
      System.out.println("\nATTACHMENT:\n" + attachment);
      System.out.println("\nRESULT OF SIGNATURE CHECK: " +
                         responseFetchDel.getContentContainer()[0].checkAllSignatures());
      System.out.println("\nENCRYPTED CONTENT DATA:\n" +
                         responseFetchDel.getEncryptedData()[0].decrypt(reader).getContents()[0].getContentData());
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
}
