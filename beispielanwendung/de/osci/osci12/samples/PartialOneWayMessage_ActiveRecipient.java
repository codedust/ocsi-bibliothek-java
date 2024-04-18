package de.osci.osci12.samples;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Random;

import javax.crypto.SecretKey;

import de.osci.helper.Tools;
import de.osci.osci12.OSCIException;
import de.osci.osci12.common.Constants;
import de.osci.osci12.common.DialogHandler;
import de.osci.osci12.messageparts.Attachment;
import de.osci.osci12.messageparts.ChunkInformation;
import de.osci.osci12.messageparts.Content;
import de.osci.osci12.messageparts.ContentContainer;
import de.osci.osci12.messageparts.EncryptedDataOSCI;
import de.osci.osci12.messagetypes.ExitDialog;
import de.osci.osci12.messagetypes.FetchDelivery;
import de.osci.osci12.messagetypes.GetMessageId;
import de.osci.osci12.messagetypes.InitDialog;
import de.osci.osci12.messagetypes.OSCIMessage;
import de.osci.osci12.messagetypes.PartialFetchDelivery;
import de.osci.osci12.messagetypes.PartialStoreDelivery;
import de.osci.osci12.messagetypes.ResponseToFetchAbstract;
import de.osci.osci12.messagetypes.ResponseToFetchDelivery;
import de.osci.osci12.messagetypes.ResponseToGetMessageId;
import de.osci.osci12.messagetypes.ResponseToInitDialog;
import de.osci.osci12.messagetypes.ResponseToPartialFetchDelivery;
import de.osci.osci12.messagetypes.ResponseToPartialStoreDelivery;
import de.osci.osci12.messagetypes.StoreDelivery;
import de.osci.osci12.roles.Addressee;
import de.osci.osci12.roles.Intermed;
import de.osci.osci12.roles.Originator;
import de.osci.osci12.roles.Reader;
import de.osci.osci12.samples.impl.HttpTransport;
import de.osci.osci12.samples.impl.crypto.PKCS12Decrypter;
import de.osci.osci12.samples.impl.crypto.PKCS12Signer;


/**
 * This is a demo application for an asynchronous communication scenario with partial messages according to
 * the OSCI 1.2-transport specification. The main method needs the intermediary's URL as parameter.
 * <p>
 * Copyright © 2023 Governikus GmbH &amp; Co. KG, Germany
 * </p>
 * <p>Erstellt von Governikus GmbH &amp; Co. KG</p>
 * <p>
 * Diese Bibliothek kann von jedermann nach Maßgabe der European Union Public Licence genutzt
 * werden.
 * </p>
 * Die Lizenzbestimmungen können unter der URL <a href="https://eupl.eu/">https://eupl.eu/</a> abgerufen werden.
 * </p>
 *
 * @author J. Buckelo
 * @version 1.8.0
 * @since 1.8.0
 */
public class PartialOneWayMessage_ActiveRecipient
{

  Intermed intermed;

  public static int attCounter;

  /*
   * Constructor for demo application
   * @param intermedURL Intermediary-URL
   * @throws CertificateException if the certificate cannot be parsed
   * @throws URISyntaxException URL syntax errors
   */
  public PartialOneWayMessage_ActiveRecipient(String intermedURL)
    throws CertificateException, URISyntaxException
  {
    // First we need the Intermediarys Cipher-Certificate
    X509Certificate intermedCipherCert = Tools.createCertificate(getClass().getResourceAsStream("/de/osci/osci12/samples/zertifikate/osci_manager_cipher_4096.cer"));

    // Create the Intermed-role object
    // The signature certificate (for signature checks) is always
    // enclosed in signed responses, so this parameter can be null
    intermed = new Intermed(null, intermedCipherCert, new URI(intermedURL));
  }

  /**
   * Sends an OSCI-StoreDelivery message to the intermediary.
   *
   * @return the OSCI-message response object
   * @throws GeneralSecurityException in case of cryptographic / security errors
   * @throws IOException write / read errors
   * @throws OSCIException any errors concerning the OSCI-message processing
   */
  public String sendPartialStoreDelivery() throws GeneralSecurityException, IOException, OSCIException
  {
    // Create the Originator-object. Since we have the private key(s), we use this
    // Constructor. We need a Decrypter-object for decryption of response
    Originator user1 = new Originator(new PKCS12Signer("/de/osci/osci12/samples/zertifikate/alice_signature_4096.p12",
                                                       "123456"),
                                      new PKCS12Decrypter("/de/osci/osci12/samples/zertifikate/carol_cipher_4096.p12",
                                                          "123456"));

    // Create a DialogHandler
    DialogHandler clientDialog = new DialogHandler(user1, intermed, new HttpTransport());

    // Default configuration is transport encryption and signature activated
    // clientDialog.setCreateSignatures(false);
    // Create the GetMessageId-Request
    InitDialog id = new InitDialog(clientDialog);
    id.send();
    GetMessageId getMsgID = new GetMessageId(clientDialog);

    // Transmit the request and receive the response
    ResponseToGetMessageId rsp2GetMsgID = getMsgID.send();

    // Error handling
    if (!rsp2GetMsgID.getFeedback()[0][1].startsWith("0"))
    {
      // do error handling
    }

    // Now we have a message-Id, we are ready to send the PartialStoreDelivery-requests.
    // First we need to create the Addressee-object representing the user 2.
    // The signature certificate is not needed since we do not
    // expect a signed response from the addressee in this secenario
    // (it would be included in a signed response anyway).
    Addressee user2 = new Addressee(null,
                                    Tools.createCertificate(getClass().getResourceAsStream("/de/osci/osci12/samples/zertifikate/bob_cipher_4096.cer")));

    // Create the StoreDelivery-message object that will be splitted and
    // send in the PartilaStoreDelivery-messages.
    DialogHandler strDelDialog = new DialogHandler(user1, intermed, new HttpTransport());
    StoreDelivery storeDel = new StoreDelivery(strDelDialog, user2, rsp2GetMsgID.getMessageId());

    storeDel.setSubject("Subject");
    // We trust the intermediary's time stamp (default)
    storeDel.setQualityOfTimeStampCreation(false);
    storeDel.setQualityOfTimeStampReception(false);

    // Add not encrypted Content-Data
    ContentContainer notEncryptedContainer = new ContentContainer();
    // Create 2MB attachment
    addAttachments(notEncryptedContainer, 2000,null);
    notEncryptedContainer.sign(user1);
    storeDel.addContentContainer(notEncryptedContainer);

    // Add encrypted Content-Data
    Reader reader = new Reader(Tools.createCertificate(getClass().getResourceAsStream("/de/osci/osci12/samples/zertifikate/dave_cipher_4096.cer")));
    ContentContainer encryptedContainer = new ContentContainer();
    SecretKey secKey = de.osci.osci12.encryption.Crypto.createSymKey(Constants.SYMMETRIC_CIPHER_ALGORITHM_AES256_GCM);
    addAttachments(encryptedContainer, 2000,secKey);
    EncryptedDataOSCI encryptedData = new EncryptedDataOSCI(Constants.SYMMETRIC_CIPHER_ALGORITHM_AES256_GCM,
                                                            encryptedContainer);
    encryptedData.encrypt(reader);
    storeDel.addEncryptedData(encryptedData);

    // Create 1000KB chunks
    long chunkSize = 1000;
    ByteArrayOutputStream storeDelOut = new ByteArrayOutputStream();
    storeDel.writeMessage(storeDelOut);
    byte[] storeDelBytes = storeDelOut.toByteArray();
    long storeDelSize = storeDelBytes.length / 1024;
    ByteArrayInputStream storeDelIn = new ByteArrayInputStream(storeDelBytes);
    Path tempDir = Files.createTempDirectory("partial_store_delivery");
    // Splits the StoreDeliveryMessage into chunk files
    ChunkHelper ch = new ChunkHelper(storeDelIn, tempDir.toString(), "storeDel.osci", chunkSize);

    // Error handling
    if (!rsp2GetMsgID.getFeedback()[0][1].startsWith("0"))
    {
      // do error handling
    }

    // Iterate through all chunks
    ResponseToPartialStoreDelivery rsp2PartialStoreDel = null;
    int chunkNumber = ch.getNumberOfChunks();
    for ( int i = 1 ; i <= chunkNumber ; i++ )
    {
      // Create the ChunkInformation for the current chunk
      ChunkInformation chunkInfo = new ChunkInformation(chunkSize, i, storeDelSize, chunkNumber);
      // Create the PartialStoreDelivery-message with the same message id as
      // the StoreDelivery-message
      PartialStoreDelivery parStoreDelivery = new PartialStoreDelivery(clientDialog, user2, chunkInfo,
                                                                       rsp2GetMsgID.getMessageId());
      // Set the ChunkBlob in the PartialStoreDelivery-message
      parStoreDelivery.setChunkBlob(ch.getChunk(i));
      rsp2PartialStoreDel = parStoreDelivery.send();

      // Error handling
      if (rsp2PartialStoreDel == null || !rsp2PartialStoreDel.getFeedback()[0][1].startsWith("0"))
      {
        // do error handling
      }
    }
    // Bei der letzten ResponseToPartialStoreDelivery Nachricht sollte ein Inside Feedback endhalten sein
    if (rsp2PartialStoreDel == null || rsp2PartialStoreDel.getInsideFeedback() == null
        || !rsp2PartialStoreDel.getFeedback()[0][1].startsWith("0"))
    {
      // do error handling
    }
    // Auch die Processcard der letzten Nachricht enthält die Processcard der gesamt Nachricht
    rsp2PartialStoreDel.getProcessCardBundle();
    // Optional: Delete all chunks after they have been sent
    ch.deleteFiles();
    tempDir.toFile().delete();
    ExitDialog ed = new ExitDialog(clientDialog);

    // Transmit the request and receive the response
    ed.send();
    return rsp2GetMsgID.getMessageId();
  }

  /**
   * Sends an OSCI-FetchDelivery message to the intermediary.
   *
   * @return the OSCI-message response object
   * @throws GeneralSecurityException in case of cryptographic / security errors
   * @throws IOException write / read errors
   * @throws OSCIException any errors concerning the OSCI-message processing
   */
  public ResponseToFetchDelivery sendPartialFetchDelivery(String messageId)
    throws GeneralSecurityException, IOException, OSCIException
  {
    // Create the Originator-object. Since we have the private key(s), we use this
    // Constructor. We need a Decrypter-object for decryption of response
    Originator user2 = new Originator(new PKCS12Signer("/de/osci/osci12/samples/zertifikate/bob_signature_4096.p12",
                                                       "123456"),
                                      new PKCS12Decrypter("/de/osci/osci12/samples/zertifikate/bob_cipher_4096.p12",
                                                          "123456"));

    // Create a DialogHandler
    DialogHandler clientDialog = new DialogHandler(user2, intermed, new HttpTransport());

    // Default configuration is transport encryption and signature activated
    // clientDialog.setCreateSignatures(false);
    // clientDialog.setEncryption(false);
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
    // returning the challenge, so we are ready to send the PartialFetchDelivery-request.
    // Create the ChunkInformation with the desired chunk size
    ChunkInformation chunkInfo = new ChunkInformation(1000, 1);
    // Create the FetchDelivery-message object
    PartialFetchDelivery parFetchDel = new PartialFetchDelivery(clientDialog, chunkInfo);
    parFetchDel.setSelectionMode(OSCIMessage.SELECT_BY_MESSAGE_ID);
    parFetchDel.setSelectionRule(messageId);

    // Transmit the request and receive the response
    ResponseToFetchAbstract rsp2ParFetchDelAbstract = parFetchDel.send();

    if (!rsp2ParFetchDelAbstract.getFeedback()[0][1].startsWith("0"))
    {
      // Error handling
    }

    ResponseToFetchDelivery rsp2FetchDel;

    // If the stored message is smaller than the chunk size a ResponseToFetchDelivery
    // will be returned.
    if (rsp2ParFetchDelAbstract instanceof ResponseToFetchDelivery)
    {
      rsp2FetchDel = (ResponseToFetchDelivery)rsp2ParFetchDelAbstract;
    }
    // If the stored message is bigger than the chunk size a ResponseToPartialFetchDelivery
    // will be returned with the number of chunks in the ChunkInformation.
    else
    {
      ResponseToPartialFetchDelivery rsp2PartialFetchDel = (ResponseToPartialFetchDelivery)rsp2ParFetchDelAbstract;

      try (ByteArrayOutputStream fetchDelOut = new ByteArrayOutputStream())
      {
        // Write the chunk blob to a File
        byte[] buf = new byte[1024];
        int bytesRead;
        InputStream in = rsp2PartialFetchDel.getChunkBlob();
        while ((bytesRead = in.read(buf)) >= 0)
        {
          fetchDelOut.write(buf, 0, bytesRead);
        }

        // Keep track of received chunks
        ArrayList<Integer> receivedChunks = new ArrayList<>();
        receivedChunks.add(1);

        for ( int i = 2 ; i <= rsp2PartialFetchDel.getChunkInformation().getTotalChunkNumbers() ; i++ )
        {
          // Set the number of the current chunk
          chunkInfo.setChunkNumber(i);
          // Set the chunk that were already successfully received
          chunkInfo.setReceivedChunks(receivedChunks);
          // Create new PartialFetchDelivery-message
          parFetchDel = new PartialFetchDelivery(clientDialog, chunkInfo);
          parFetchDel.setSelectionRule(messageId);
          parFetchDel.setSelectionMode(FetchDelivery.SELECT_BY_MESSAGE_ID);
          // Send PartialFetchDelivery-message
          rsp2PartialFetchDel = (ResponseToPartialFetchDelivery)parFetchDel.send();

          // Error handling
          if (!rsp2PartialFetchDel.getFeedback()[0][1].startsWith("0"))
          {
            // do error handling
          }

          // Append chunk to file
          in = rsp2PartialFetchDel.getChunkBlob();
          while ((bytesRead = in.read(buf)) >= 0)
          {
            fetchDelOut.write(buf, 0, bytesRead);
          }

          // Add chunk to received chunks
          receivedChunks.add(i);
        }

        // Read the ResponseToFetchDelivery
        try (ByteArrayInputStream fetchDelIn = new ByteArrayInputStream(fetchDelOut.toByteArray()))
        {
          rsp2FetchDel = ResponseToFetchDelivery.parseResponseToFetchDelivery(fetchDelIn);
        }

      }
    }

    // Optional: Check process card bundle (result of certificate check etc.)
    // rsp2FetchDel.getInspections();
    // rsp2FetchDel.getTimestampCreation();
    // rsp2FetchDel.getRecentModification();
    // rsp2FetchDel.getContentContainer();
    // rsp2FetchDel.getAttachments();
    // ....
    // Cleanup - Create the ExitDialog-Request
    ExitDialog ed = new ExitDialog(clientDialog);

    // Transmit the request and receive the response
    ed.send();

    return rsp2FetchDel;
  }

  private void addAttachments(ContentContainer coco, int sizeKB, SecretKey secKey)
    throws IOException, IllegalArgumentException, NoSuchAlgorithmException
  {
    byte[] randomBytes = new byte[sizeKB * 1024 / 3];
    new Random().nextBytes(randomBytes);
    coco.addContent(new Content("Any content data."));
    if (secKey == null)
    {
      coco.addContent(new Content(new Attachment(new ByteArrayInputStream(randomBytes),
                                                 "random" + (attCounter++) + ".txt")));
      coco.addContent(new Content(new Attachment(new ByteArrayInputStream(randomBytes),
                                                 "random" + (attCounter++) + ".txt")));
      coco.addContent(new Content(new Attachment(new ByteArrayInputStream(randomBytes),
                                                 "random" + (attCounter++) + ".txt")));
    }
    else
    {
      coco.addContent(new Content(new Attachment(new ByteArrayInputStream(randomBytes),
                                                 "random" + (attCounter++) + ".txt", secKey,
                                                 Constants.SYMMETRIC_CIPHER_ALGORITHM_AES256_GCM)));
      coco.addContent(new Content(new Attachment(new ByteArrayInputStream(randomBytes),
                                                 "random" + (attCounter++) + ".txt", secKey,
                                                 Constants.SYMMETRIC_CIPHER_ALGORITHM_AES256_GCM)));
      coco.addContent(new Content(new Attachment(new ByteArrayInputStream(randomBytes),
                                                 "random" + (attCounter++) + ".txt", secKey,
                                                 Constants.SYMMETRIC_CIPHER_ALGORITHM_AES256_GCM)));
    }
  }

  /**
   * Main entry.
   *
   * @param args must contain one string with the intermediary URL
   */
  public static void main(String[] args)
  {
    try
    {
      PartialOneWayMessage_ActiveRecipient scenario1 = new PartialOneWayMessage_ActiveRecipient(args[0]);
      String storeDelMsgId = scenario1.sendPartialStoreDelivery();

      ResponseToFetchDelivery responseFetchDel = scenario1.sendPartialFetchDelivery(storeDelMsgId);
      // Hier kann jetzt die Empfange Nachricht ausgewertet weren
      responseFetchDel.getContentContainer();
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
}
