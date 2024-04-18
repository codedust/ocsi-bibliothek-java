package de.osci.osci12.samples;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;

import de.osci.helper.Tools;
import de.osci.osci12.common.DialogHandler;
import de.osci.osci12.encryption.EncryptedData;
import de.osci.osci12.encryption.EncryptedDataBuilder;
import de.osci.osci12.extinterfaces.crypto.Decrypter;
import de.osci.osci12.extinterfaces.crypto.Signer;
import de.osci.osci12.messageparts.Content;
import de.osci.osci12.messageparts.ContentContainer;
import de.osci.osci12.messageparts.EncryptedDataOSCI;
import de.osci.osci12.messagetypes.ExitDialog;
import de.osci.osci12.messagetypes.FetchDelivery;
import de.osci.osci12.messagetypes.GetMessageId;
import de.osci.osci12.messagetypes.InitDialog;
import de.osci.osci12.messagetypes.OSCIMessage;
import de.osci.osci12.messagetypes.ResponseToFetchDelivery;
import de.osci.osci12.messagetypes.ResponseToGetMessageId;
import de.osci.osci12.messagetypes.StoreDelivery;
import de.osci.osci12.roles.Addressee;
import de.osci.osci12.roles.Author;
import de.osci.osci12.roles.Intermed;
import de.osci.osci12.roles.Originator;
import de.osci.osci12.roles.Reader;
import de.osci.osci12.samples.impl.HttpTransport;
import de.osci.osci12.samples.impl.crypto.PKCS12Decrypter;
import de.osci.osci12.samples.impl.crypto.PKCS12Signer;
import de.osci.osci12.soapheader.NonIntermediaryCertificatesH;
import de.osci.osci12.soapheader.NonIntermediaryCertificatesHBuilder;


/**
 * Dieses Sample zeigt exemplarisch das Zusammenbauen und Versenden einer StoreDelivery-Nachricht, die ein
 * serialisiertes EncryptedData-XML-Element mit Zertifikatsreferenzen und das dazu passende serialisierte
 * NonIntermediaryCertificates-Element einliest.
 * Beide Elemente können dabei z.B. aus einem XTA Transport oder aus dem Dateisystem stammen.
 * <br><br>
 * Der Ablauf ist fünf Kontexte unterteilt:
 * <br><br>
 * 1. es werden die Zertifikate zum Verschlüsseln/Signieren in zwei OSCI-Rollen-Objekte eingebaut<br>
 * 2. es wird ein EncryptedData-Element mit den verschlüsselten Inhaltsdaten (als XML-Bytes) erzeugt<br>
 * 3. es wird ein NonIntermediaryCertificates-Element mit den benutzten Zertifikaten (als XML-Bytes)
 * erzeugt<br>
 * 4. es wird eine StoreDelivery-Nachricht erstellt, die beiden serialisierten ELemente werden eingelesen
 * und der Nachricht hinzugefügt, und die Nachricht wird an einen Intermediär gesendet<br>
 * 5. die Nachricht wird vom Intermediär wieder abgeholt und der Inhalt wird entschlüsselt und geprüft
 * <br><br>
 * <p>Copyright © 2023 Governikus GmbH &amp; Co. KG, Germany</p>
 * <p>Erstellt von Governikus GmbH &amp; Co. KG</p>
 * <p>Diese Bibliothek kann von jedermann nach Maßgabe der European Union
 * Public Licence genutzt werden.</p><p>Die Lizenzbestimmungen können
 * unter der URL <a href="https://eupl.eu/">https://eupl.eu/</a> abgerufen werden.</p>
 */
@SuppressWarnings({"LocalCanBeFinal", "LongLine", "UseOfSystemOutOrSystemErr"})
public class SendParsedEncryptedData
{

  private static final String PIN = "123456";

  private static final String DAVE_CIPHER_REF = "reader_dave_cipher_ref";

  private static final String ALICE_SIGN_REF = "author_alice_sign_ref";

  private static final List<String> SAMPLE_CONTENT_NAMES =
    Arrays.asList("messageText", "form.xml", "printout.pdf", "protocol.pdf", "protocol.json",
                  "xsozialxslt.xml");

  /**
   * Startpunkt des Samples.
   */
  public static void main(String[] args) throws Exception
  {
    // 1. OSCI-Rollen mit entsprechenden Schlüsseln erstellen

    // Leser-Rolle mit öffentlichem Schlüssel zum Verschlüsseln erzeugen
    Reader readerEncrypt = createReaderEncrypt();

    // Autor-Rolle mit privatem Schlüssel zum Signieren erzeugen
    Author authorSign = createAuthorSign();


    // 2. Erstellung eines EncryptedData-Elements mit den verschlüsselten Inhaltsdaten (als XML-Bytes)
    byte[] encryptedDataBytes = createEncryptedData(readerEncrypt, authorSign);


    // 3. Erstellung eines NonIntermediaryCertificates-Elements mit den Zertifikaten für die Inhaltsdaten (als XML-Bytes)
    byte[] nonIntermediaryCertificatesBytes = createNonIntermediaryCertificates(readerEncrypt, authorSign);


    // 4. Übernahme des verschlüsselten Inhalts und der Zertifikate in eine neue OSCI-Nachricht und versenden dieser
    String messageId =
      sendStoreDeliveryWithEncryptedData(encryptedDataBytes, nonIntermediaryCertificatesBytes);


    // 5. Abholung der Nachricht mit der Nachrichten-ID und Entschlüsselung / Prüfung des Inhalts
    fetchMessageAndCheckContents(messageId);
  }


  private static byte[] createEncryptedData(Reader readerEncrypt, Author authorSign) throws Exception
  {
    // #### 1. Content-Container mit einigen Beispieldaten erzeugen und signieren ####

    System.out.println("Erzeuge und signiere Beispiel-Container");

    ContentContainer cocoWithAllContents = new ContentContainer();
    cocoWithAllContents.setRefID("XSOZIAL_DATA");

    SAMPLE_CONTENT_NAMES.forEach(c -> cocoWithAllContents.addContent(createSampleContent(c)));

    // Content-Container signieren mit Autor
    cocoWithAllContents.sign(authorSign);


    // #### 2. EncryptedDataOSCI-Objekt erzeugen, verschlüsseln und serialisieren ####

    System.out.println("Erzeuge und serialisiere EncryptedData-Element");

    EncryptedDataOSCI encryptedData = new EncryptedDataOSCI(cocoWithAllContents);

    // Verschlüsseln mit Leser-Zertifikat
    encryptedData.encrypt(readerEncrypt);

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    encryptedData.writeXML(outputStream, false);

    return outputStream.toByteArray();
  }

  private static byte[] createNonIntermediaryCertificates(Reader readerEncrypt, Author authorSign)
    throws Exception
  {
    // #### 1. NonIntermediaryCertificates-Instanz erzeugen ####

    System.out.println("Erzeuge NonIntermediaryCertificates-Instanz mit gegebenen Schlüsseln");

    NonIntermediaryCertificatesH nih = new NonIntermediaryCertificatesH();

    // Setzen der Schlüssel für die passenden OSCI-Rollen Leser und Autor
    nih.setCipherCertificatesOtherReaders(new Reader[]{readerEncrypt});
    nih.setSignatureCertificatesOtherAuthors(new Author[]{authorSign});

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    nih.writeXML(outputStream);
    return outputStream.toByteArray();
  }

  private static Content createSampleContent(String name)
  {
    final Content msgText;
    try
    {
      // hier können auch OSCI-Attachments oder InputStreams eingebunden werden
      msgText = new Content("some random bytes");
    }
    catch (IOException e)
    {
      throw new RuntimeException(e);
    }
    msgText.setRefID(name);
    return msgText;
  }

  private static String sendStoreDeliveryWithEncryptedData(byte[] encryptedDataBytes,
                                                           byte[] nonIntermediaryHeaderBytes) throws Exception
  {
    // Transportkonfiguration für StoreDelivery-Versenden vorbereiten
    System.out.println("Bereite OSCI-StoreDelivery-Transport vor");
    DialogHandler senderDialog = createSendDialogHandler();
    Addressee addressee = createAddressee();
    String msgId = getMsgId(senderDialog);

    // Neue StoreDelivery-Nachricht erzeugen
    StoreDelivery storeDelivery = new StoreDelivery(senderDialog, addressee, msgId);

    // Laden der OSCI-Rollen mit den Zertifikaten aus dem NonIntermediaryCertificates-Element
    // (könnte z.B. aus der XTA-Nachricht sein oder eine Datei)
    System.out.println("Lade OSCI-Rollen aus NonIntermediaryCertificates-Element");
    NonIntermediaryCertificatesH nonIntermediaryCertificates =
      NonIntermediaryCertificatesHBuilder.createFromXmlBytes(nonIntermediaryHeaderBytes);

    // Hinzufügen der Leser/Autoren zur StoreDelivery-Nachricht
    storeDelivery.addReadersAndAuthors(nonIntermediaryCertificates);

    // Laden des EncryptedData-Elements aus dem Stream (könnte z.B. aus der XTA-Nachricht sein oder eine Datei)
    System.out.println("Lese EncryptedData-Element ein und füge es der Nachricht hinzu");
    EncryptedData encryptedData = EncryptedDataBuilder.createFromXmlBytes(encryptedDataBytes);

    // Hinzufügen des EncryptedData-Elements zur Nachricht als EncryptedDataOSCI-Objekt
    storeDelivery.addEncryptedData(new EncryptedDataOSCI(encryptedData, storeDelivery));

    System.out.println("Versende OSCI-StoreDelivery-Nachricht an Intermediär");
    return storeDelivery.send().getMessageId();
  }

  private static void fetchMessageAndCheckContents(String messageId) throws Exception
  {
    // Transportkonfiguration für FetchDelivery-Versenden vorbereiten
    System.out.println("Bereite OSCI-FetchDelivery-Transport vor");
    DialogHandler fetchDialog = createFetchDialogHandler();
    new InitDialog(fetchDialog).send();

    // FetchDelivery-Nachricht erzeugen (Abholen anhand der Nachrichten-ID)
    System.out.println("Erzeuge und versende FetchDelivery-Nachricht");
    FetchDelivery fetchDel = new FetchDelivery(fetchDialog);
    fetchDel.setSelectionMode(OSCIMessage.SELECT_BY_MESSAGE_ID);
    fetchDel.setSelectionRule(messageId);

    // Nachricht versenden
    ResponseToFetchDelivery rsp2FetchDel = fetchDel.send();

    // OSCI-Dialog beenden
    new ExitDialog(fetchDialog).send();

    // EncryptedData-Objekt aus der Nachricht holen
    System.out.println("Überprüfe und entschlüssele abgeholte Nachricht");
    EncryptedDataOSCI[] fetchedEncryptedData = rsp2FetchDel.getEncryptedData();

    // es darf nur genau ein Element sein
    if (fetchedEncryptedData.length != 1)
    {
      throw new Exception("Nicht genau ein EncryptedData-Element!");
    }

    // ContentContainer entschlüsseln mit dem privaten Leser-Schlüssel
    ContentContainer enc_coco = fetchedEncryptedData[0].decrypt(createReaderDecrypt());

    if (!rsp2FetchDel.getOtherAuthors()[0].getSignatureCertificateId().equals(ALICE_SIGN_REF))
    {
      throw new Exception(
        "Falsche Autoren-ID " + rsp2FetchDel.getOtherAuthors()[0].getSignatureCertificateId());
    }

    // Signatur prüfen mit der übergebenen Autoren-Rolle
    enc_coco.checkSignature(rsp2FetchDel.getOtherAuthors()[0]);

    // Inhalte prüfen
    if (!enc_coco.getRefID().equals("XSOZIAL_DATA"))
    {
      throw new Exception("Falscher Content-Container(-Name)");
    }

    if (enc_coco.getContents().length != 6)
    {
      throw new Exception("Nicht genau 6 Contents im entschlüsselten ContentContainer!");
    }

    if (!Arrays.stream(enc_coco.getContents())
               .allMatch(content -> SAMPLE_CONTENT_NAMES.contains(content.getRefID())))
    {
      throw new Exception("Nicht alle Contents im entschlüsselten ContentContainer!");
    }
  }

  private static DialogHandler createSendDialogHandler() throws Exception
  {
    final Intermed intermed = createIntermed();
    final Originator originator = createOriginator();
    return new DialogHandler(originator, intermed, new HttpTransport());
  }

  private static DialogHandler createFetchDialogHandler() throws Exception
  {
    final Intermed intermed = createIntermed();
    final Originator originator = new Originator(new PKCS12Signer(
      "/de/osci/osci12/samples/zertifikate/bob_signature_4096.p12",
      "123456"), new PKCS12Decrypter("/de/osci/osci12/samples/zertifikate/bob_cipher_4096.p12", "123456"));
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
    return SendParsedEncryptedData.class.getResourceAsStream(resource);
  }

  private static URI createIntermedUri() throws URISyntaxException
  {
    final String testEntry = "http://gov.current.tf.bos-test.de:8080/osci-manager-entry/externalentry";
    return new URI(testEntry);
  }

  private static Originator createOriginator() throws Exception
  {
    Signer signer = createSigner();
    Decrypter decrypter =
      new PKCS12Decrypter("/de/osci/osci12/samples/zertifikate/alice_cipher_4096.p12", PIN);
    return new Originator(signer, decrypter);
  }

  private static Signer createSigner() throws Exception
  {
    final String cert = "/de/osci/osci12/samples/zertifikate/alice_signature_4096.p12";
    return new PKCS12Signer(cert, PIN);
  }

  private static Addressee createAddressee() throws CertificateException
  {
    final String cert = "/de/osci/osci12/samples/zertifikate/bob_cipher_4096.cer";
    final InputStream resource = getResource(cert);
    return new Addressee(null, Tools.createCertificate(resource));
  }

  private static Reader createReaderEncrypt() throws CertificateException
  {
    System.out.println("Erzeuge OSCI-Rolle Leser");
    final String cert = "/de/osci/osci12/samples/zertifikate/dave_cipher_4096.cer";
    final InputStream resource = getResource(cert);
    Reader reader = new Reader(Tools.createCertificate(resource));

    // eine einheitliche ID sollte als Referenz z.B. beim Transport der Zertifikate in XTA Transport benutzt werden
    reader.cipherRefId = DAVE_CIPHER_REF;
    return reader;
  }

  private static Reader createReaderDecrypt() throws Exception
  {
    Reader reader = new Reader(new de.osci.osci12.samples.impl.crypto.PKCS12Decrypter(
      "/de/osci/osci12/samples/zertifikate/dave_cipher_4096.p12", PIN));
    // eine einheitliche ID sollte als Referenz z.B. beim Transport der Zertifikate in XTA Transport benutzt werden
    reader.cipherRefId = DAVE_CIPHER_REF;
    return reader;
  }

  private static Author createAuthorSign() throws Exception
  {
    Author author = new Author(createSigner(), null);

    // eine einheitliche ID sollte als Referenz z.B. beim Transport der Zertifikate in XTA Transport benutzt werden
    author.signatureRefId = ALICE_SIGN_REF;
    return author;
  }

  private static String getMsgId(DialogHandler dh) throws Exception
  {
    final GetMessageId getMsgId = new GetMessageId(dh);
    final ResponseToGetMessageId rsp2GetMsgID = getMsgId.send();
    return rsp2GetMsgID.getMessageId();
  }

}
