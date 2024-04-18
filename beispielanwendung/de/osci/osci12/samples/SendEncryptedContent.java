package de.osci.osci12.samples;

import de.osci.helper.Tools;
import de.osci.osci12.OSCIException;
import de.osci.osci12.common.DialogHandler;
import de.osci.osci12.extinterfaces.crypto.Decrypter;
import de.osci.osci12.extinterfaces.crypto.Signer;
import de.osci.osci12.messageparts.Attachment;
import de.osci.osci12.messageparts.Content;
import de.osci.osci12.messageparts.ContentContainer;
import de.osci.osci12.messageparts.EncryptedDataOSCI;
import de.osci.osci12.messagetypes.GetMessageId;
import de.osci.osci12.messagetypes.ResponseToGetMessageId;
import de.osci.osci12.messagetypes.StoreDelivery;
import de.osci.osci12.messagetypes.StoredMessage;
import de.osci.osci12.roles.Addressee;
import de.osci.osci12.roles.Intermed;
import de.osci.osci12.roles.Originator;
import de.osci.osci12.roles.Reader;
import de.osci.osci12.roles.Role;
import de.osci.osci12.samples.impl.HttpTransport;
import de.osci.osci12.samples.impl.crypto.PKCS12Decrypter;
import de.osci.osci12.samples.impl.crypto.PKCS12Signer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import static de.osci.osci12.common.Constants.SYMMETRIC_CIPHER_ALGORITHM_AES256_GCM;


/**
 * Dieses Sample zeigt exemplarisch das Versenden einer StoreDelivery Nachricht unter
 * vorheriger Übernahme des verschlüsselten Inhalts und weiterer Daten aus einer geladenen
 * StoredMessage. Die geladene Nachricht kann dabei z.B. aus einem XTA Transport oder dem Dateisystem stammen.
 *
 * Dieser Ablauf ist zwei Kontexte unterteilt:
 * 1. Auf der einen Seite wird zunächst ein verschlüsselter Inhalt erstellt und per StoredMessage abgelegt.
 * 2. Auf der anderen Seite wird diese StoredMessage geladen, die dazugehörigen Daten in einer StoreDelivery
 * übernommen und dann versendet
 *
 * <p>Copyright © 2023 Governikus GmbH &amp; Co. KG, Germany</p>
 * <p>Erstellt von Governikus GmbH &amp; Co. KG</p>
 * <p>Diese Bibliothek kann von jedermann nach Maßgabe der European Union
 *  Public Licence genutzt werden.</p><p>Die Lizenzbestimmungen können
 * unter der URL <a href="https://eupl.eu/">https://eupl.eu/</a> abgerufen werden.</p>
 */
public class SendEncryptedContent
{

  private static final String PIN = "123456";

  /**
   * Startpunkt des Samples.
   */
  public static void main(String[] args)
    throws CertificateException, NoSuchAlgorithmException, OSCIException, IOException,
    UnrecoverableKeyException, KeyStoreException, URISyntaxException
  {
    //1. Erstellung einer Nachricht mit verschlüsseltem Inhalt
    ByteArrayOutputStream msgStream = createEncryptedContent();

    //2. Übernahme dieses verschlüsselten Inhalts in eine neue Nachricht und versenden dieser
    sendStoreDelivery(msgStream);
  }

  private static ByteArrayOutputStream createEncryptedContent()
    throws IOException, NoSuchAlgorithmException, CertificateException, OSCIException
  {
    //Verschlüsselten Content Container für die Nachricht erstellen
    ContentContainer coco = createContentContainer();
    EncryptedDataOSCI encryptedData = new EncryptedDataOSCI(SYMMETRIC_CIPHER_ALGORITHM_AES256_GCM, coco);
    Reader reader = createReader();
    encryptedData.encrypt(reader);
     

    //StoreDelivery Nachricht erstellen und den verschlüsselten Content Container hinzufügen
    //Diese kann in der Praxis z.B. aus einem XTA Transport oder dem Dateisystem stammen.
    DialogHandler dhstore = new DialogHandler((Originator)null, null, new HttpTransport());
    StoreDelivery storeDelivery = new StoreDelivery(dhstore, createAddressee(), "MyMsgId");
    storeDelivery.addEncryptedData(encryptedData);

    //Nachricht in einem Outputstream speichern.
    //Dieser simuliert nun die eingelesene Nachricht aus z.B. einem XTA Transport oder dem Dateisystem
    ByteArrayOutputStream outstrDel = new ByteArrayOutputStream();
    StoredMessage.storeMessage(storeDelivery, outstrDel);

    return outstrDel;
  }

  private static void sendStoreDelivery(ByteArrayOutputStream msgStream)
    throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException,
    URISyntaxException, IOException, OSCIException
  {
    //Neue StoreDelivery Nachricht unter Berücksichtigung der Transportkonfiguration (Originator, Adressat, Nachricht ID Erzeugung usw.) erstellen.
    //Diese wird den Inhalt der zuvor geladenen Nachricht übernehmen.
    DialogHandler senderDialog = createDialogHandler();
    Addressee addressee = createAddressee();
    String msgId = getMsgId(senderDialog);
    StoreDelivery storeDeliveryNext = new StoreDelivery(senderDialog, addressee, msgId);

    //Laden der Nachricht und übernahme der dazugehörigen Daten aus dem Outputstream in die neue StoreDelivery Nachricht
    StoredMessage loadedMsg = StoredMessage.loadMessage(new ByteArrayInputStream(msgStream.toByteArray()));

    for ( EncryptedDataOSCI encData : loadedMsg.encryptedData.values() )
    {
      storeDeliveryNext.addEncryptedData(encData);
    }
    for ( Role role : loadedMsg.getOtherAuthors() )
    {
      storeDeliveryNext.addRole(role);
    }
    for ( Role role : loadedMsg.getOtherReaders() )
    {
      storeDeliveryNext.addRole(role);
    }
    storeDeliveryNext.attachments = loadedMsg.attachments;

    //Neue StoreDelivery Nachricht mit übernommenem Inhalt neu versenden
    storeDeliveryNext.send(msgStream, null);
  }

  private static DialogHandler createDialogHandler()
    throws UnrecoverableKeyException, CertificateException, NoSuchAlgorithmException, KeyStoreException,
    IOException, URISyntaxException
  {
    final Intermed intermed = createIntermed();
    final Originator originator = createOriginator();
    return new DialogHandler(originator, intermed, new HttpTransport());
  }

  private static Intermed createIntermed() throws CertificateException, URISyntaxException
  {
    final X509Certificate cipherCert = createIntermedCipherCert();
    final URI uri = createIntermedUri();
    return new Intermed(null, cipherCert, uri);
  }

  private static X509Certificate createIntermedCipherCert() throws CertificateException
  {
    final String testCypher = "/de/osci/osci12/samples/zertifikate/osci_manager_cipher_4096.cer";
    final InputStream resource = getResource(testCypher);
    return Tools.createCertificate(resource);
  }

  private static InputStream getResource(String resource)
  {
    return SendEncryptedContent.class.getResourceAsStream(resource);
  }

  private static URI createIntermedUri() throws URISyntaxException
  {
    final String testEntry = "http://gov.current.tf.bos-test.de:8080/osci-manager-entry/externalentry";
    return new URI(testEntry);
  }

  private static Originator createOriginator()
    throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException,
    UnrecoverableKeyException
  {
    Signer signer = createSigner();
    Decrypter decrypter = createDecrypter();
    return new Originator(signer, decrypter);
  }

  private static Signer createSigner()
    throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException
  {
    final String cert = "/de/osci/osci12/samples/zertifikate/alice_signature_4096.p12";
    return new PKCS12Signer(cert, PIN);
  }

  private static Decrypter createDecrypter()
    throws UnrecoverableKeyException, CertificateException, NoSuchAlgorithmException, KeyStoreException,
    IOException
  {
    final String cert = "/de/osci/osci12/samples/zertifikate/carol_cipher_4096.p12";
    return new PKCS12Decrypter(cert, PIN);
  }

  private static Addressee createAddressee() throws CertificateException
  {
    final String cert = "/de/osci/osci12/samples/zertifikate/bob_cipher_4096.cer";
    final InputStream resource = getResource(cert);
    return new Addressee(null, Tools.createCertificate(resource));
  }

  private static ContentContainer createContentContainer() throws IOException, NoSuchAlgorithmException
  {
    final ContentContainer coco = new ContentContainer();
    coco.addContent(new Content("Any encrypted content data."));
    coco.addContent(new Content(anyAttachment()));
    return coco;
  }

  private static Attachment anyAttachment() throws IOException, NoSuchAlgorithmException
  {
    final InputStream inputStream = new ByteArrayInputStream("Any encrypted attachment data.".getBytes());
    final String refId = "enc_test.txt";
    return new Attachment(inputStream, refId, SYMMETRIC_CIPHER_ALGORITHM_AES256_GCM);
  }

  private static Reader createReader() throws CertificateException
  {
    final String cert = "/de/osci/osci12/samples/zertifikate/dave_cipher_4096.cer";
    final InputStream resource = getResource(cert);
    return new Reader(Tools.createCertificate(resource));
  }

  private static String getMsgId(DialogHandler dh) throws OSCIException, NoSuchAlgorithmException, IOException
  {
    final GetMessageId getMsgId = new GetMessageId(dh);
    final ResponseToGetMessageId rsp2GetMsgID = getMsgId.send();
    return rsp2GetMsgID.getMessageId();
  }

}
