package de.osci.osci12.samples;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import de.osci.osci12.common.Constants;
import de.osci.osci12.common.DialogHandler;
import de.osci.osci12.messageparts.Attachment;
import de.osci.osci12.messageparts.Content;
import de.osci.osci12.messageparts.ContentContainer;
import de.osci.osci12.messageparts.EncryptedDataOSCI;
import de.osci.osci12.messagetypes.ForwardDelivery;
import de.osci.osci12.messagetypes.GetMessageId;
import de.osci.osci12.messagetypes.ResponseToGetMessageId;
import de.osci.osci12.messagetypes.StoreDelivery;
import de.osci.osci12.messagetypes.StoredMessage;
import de.osci.osci12.roles.Addressee;
import de.osci.osci12.roles.Author;
import de.osci.osci12.roles.Intermed;
import de.osci.osci12.roles.Originator;
import de.osci.osci12.roles.Reader;


/**
 * This sample demonstrates the usage of the StoredMessage-class for the
 * interchange of content data between an author and an originator.
 * The main method's first parameter is the Intermediary-URL, the second one
 * a string that will be used as content data and the last one is the path to
 * a file that will be added as an attachment. The StoredMessage will be
 * written to the same directory where the attachment file resides.
 *
 * <p>Copyright © 2023 Governikus GmbH &amp; Co. KG, Germany</p>
 * <p>Erstellt von Governikus GmbH & Co. KG</p>
 * <p>Diese Bibliothek kann von jedermann nach Maßgabe der European Union
 *  Public Licence genutzt werden.</p><p>Die Lizenzbestimmungen können
 * unter der URL <a href="https://eupl.eu/">https://eupl.eu/</a> abgerufen werden.</p>
 *
 * @author N.Büngener
 * @version 1.0
 */
public class AuthorOriginatorContentInterchange
{
  //  Intermed intermed;
  public AuthorOriginatorContentInterchange()
  {
  }

  /**
   * undocumented
   *
   * @param data undocumented
   * @param attachmentFilename undocumented
   *
   * @return undocumented
   *
   * @throws Exception undocumented
   */
  public File createStoredMessageFile(String data, String attachmentFilename)
                               throws Exception
  {
    // Create an OSCI-message of any type and add the content.
    // First we need a DialogHandler, although it is not used in this case
    DialogHandler clientDialog = new DialogHandler((Originator) null, null, null);

    // Create the ForwardDelivery-message object
    ForwardDelivery forwardDel = new ForwardDelivery(clientDialog, null, "XX", "YY");

    // We have to set some dummy-parameters
    forwardDel.setQualityOfTimeStampCreation(false);
    forwardDel.setQualityOfTimeStampReception(false);

    // Create encrypted and signed Content-Data
    // Create ContentContainer
    ContentContainer encrypted_container = new ContentContainer();

    // Add plain data
    encrypted_container.addContent(new Content(data));

    // Add Attachment
    String path = attachmentFilename.substring(0, attachmentFilename.lastIndexOf('/'));
    encrypted_container.addContent(new Content(new Attachment(new FileInputStream(attachmentFilename),
                                                              attachmentFilename.substring(path.length()),
Constants.DEFAULT_SYMMETRIC_CIPHER_ALGORITHM)));

    // Sign the ContantContainer
    Author author = new Author(new de.osci.osci12.samples.impl.crypto.PKCS12Signer("/de/osci/osci12/samples/zertifikate/alice_signature_4096.p12",
                                                                                   "123456"), null);
    encrypted_container.sign(author);

    // Encrypt the signed container by adding it to an EncryptedData tag
    EncryptedDataOSCI encryptedData = new EncryptedDataOSCI(Constants.SYMMETRIC_CIPHER_ALGORITHM_AES256_GCM,
                                                            encrypted_container);
    Reader reader = new Reader(de.osci.helper.Tools.createCertificate(getClass().getResourceAsStream("/de/osci/osci12/samples/zertifikate/dave_cipher_4096.cer")));
    encryptedData.encrypt(reader);
    // Add the EncryptedData to the message
    forwardDel.addEncryptedData(encryptedData);

    // Store the message in a file
    File store = new File(path + "/StoredForwardDelivery.osci");
    StoredMessage.storeMessage(forwardDel, new FileOutputStream(store));

    return store;
  }

  /**
   * undocumented
   *
   * @param intermedURL undocumented
   * @param storedFile undocumented
   *
   * @return undocumented
   *
   * @throws Exception undocumented
   */
  public StoreDelivery importToStoreDelivery(String intermedURL, File storedFile)
                                      throws Exception
  {
    // We construct a new StoreDelivery-message and import the encrypted content from
    // the stored message.
    // First we need the Intermediarys Cipher-Certificate
    java.security.cert.X509Certificate intermedCipherCert = de.osci.helper.Tools.createCertificate(getClass()
                                                                                                   .getResourceAsStream("/de/osci/osci12/samples/zertifikate/osci_manager_cipher_4096.cer"));

    // Create the Intermed-role object
    Intermed intermed = new Intermed(null, intermedCipherCert, new java.net.URI(intermedURL));

    // Create the Originator-object. Since we have the private key(s), we use this
    // Constructor. We need a Decrypter-object for decryption of response
    Originator user_1 = new Originator(new de.osci.osci12.samples.impl.crypto.PKCS12Signer("/de/osci/osci12/samples/zertifikate/alice_signature_4096.p12",
                                                                                           "123456"),
                                       new de.osci.osci12.samples.impl.crypto.PKCS12Decrypter("/de/osci/osci12/samples/zertifikate/carol_cipher_4096.p12",
                                                                                              "123456"));

    // Create a DialogHandler
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

    Addressee user_2 = new Addressee(null,
                                     de.osci.helper.Tools.createCertificate(getClass().getResourceAsStream("/de/osci/osci12/samples/zertifikate/bob_cipher_4096.cer")));

    // Create the StoreDelivery-message object
    StoreDelivery storeDel = new StoreDelivery(clientDialog, user_2, rsp2GetMsgID.getMessageId());
    storeDel.setSubject("Subject");
    // Required quality of timestamps applied by the intermediary
    storeDel.setQualityOfTimeStampCreation(false);
    storeDel.setQualityOfTimeStampReception(false);

    // Now we can load the StoredMessage, extract the encrypted data and
    // add it to the new message
    StoredMessage storedMsg = StoredMessage.loadMessage(new FileInputStream(storedFile));
    storeDel.addEncryptedData(storedMsg.getEncryptedData()[0]);

    // We do not know, which certificates may be referenced in the encrypted data.
    // To make sure that all necessary certificates will be available, we simply
    // add all certificates included in the stored message.
    // CAUTION: If the originator has signed contents of StoredMessage,
    // it's  signature certificate must be included by constructing an new Author
    // object using the originator certificate. The same applies to addressees
    // and encryption.
    // This should be avoided by applications.
    Reader[] readers = storedMsg.getOtherReaders();

    for (int i = 0; i < readers.length; i++)
      storeDel.addRole(readers[i]);

    Author[] authors = storedMsg.getOtherAuthors();

    for (int i = 0; i < authors.length; i++)
      storeDel.addRole(authors[i]);

    // The problem mentioned above applies to the attachments too. The
    // StoredMessage.export(OSCIMessage)-method adds all attachments.
    storedMsg.exportAttachments(storeDel);

    // Add not encrypted Content-Data
    ContentContainer not_encrypted_container = new ContentContainer();
    not_encrypted_container.addContent(new Content("Any content data."));
    storeDel.addContentContainer(not_encrypted_container);

    // Add encrypted Content-Data
    Reader reader = new Reader(de.osci.helper.Tools.createCertificate(getClass().getResourceAsStream("/de/osci/osci12/samples/zertifikate/dave_cipher_4096.cer")));
    ContentContainer encrypted_container = new ContentContainer();
    encrypted_container.addContent(new Content("Any encrypted content data."));

    EncryptedDataOSCI encryptedData = new EncryptedDataOSCI(Constants.SYMMETRIC_CIPHER_ALGORITHM_AES256_GCM,
                                                            encrypted_container);
    encryptedData.encrypt(reader);
    storeDel.addEncryptedData(encryptedData);

    return storeDel;
  }

  /**
   * undocumented
   *
   * @param args undocumented
   */
  public static void main(String[] args)
  {
    try
    {
      AuthorOriginatorContentInterchange authOrigSample = new AuthorOriginatorContentInterchange();
      File stored = authOrigSample.createStoredMessageFile(args[1], args[2]);
      StoreDelivery storeDel = authOrigSample.importToStoreDelivery(args[0], stored);
      System.out.println("\nStoreDelivery-message with imported data:\n" + storeDel.toString());
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
}
