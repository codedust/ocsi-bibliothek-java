package de.osci.osci12.samples;

import de.osci.helper.CustomHeaderHelper;
import de.osci.helper.Tools;
import de.osci.osci12.common.DialogHandler;
import de.osci.osci12.extinterfaces.crypto.Decrypter;
import de.osci.osci12.extinterfaces.crypto.Signer;
import de.osci.osci12.messagetypes.ExitDialog;
import de.osci.osci12.messagetypes.FetchDelivery;
import de.osci.osci12.messagetypes.GetMessageId;
import de.osci.osci12.messagetypes.InitDialog;
import de.osci.osci12.messagetypes.OSCIMessage;
import de.osci.osci12.messagetypes.ResponseToFetchDelivery;
import de.osci.osci12.messagetypes.ResponseToGetMessageId;
import de.osci.osci12.messagetypes.StoreDelivery;
import de.osci.osci12.roles.Addressee;
import de.osci.osci12.roles.Intermed;
import de.osci.osci12.roles.Originator;
import de.osci.osci12.samples.impl.HttpTransport;
import de.osci.osci12.samples.impl.crypto.PKCS12Decrypter;
import de.osci.osci12.samples.impl.crypto.PKCS12Signer;
import eu.osci.ws._2014._10.transport.DeliveryAttributesType;
import eu.osci.ws._2014._10.transport.DestinationsType;
import eu.osci.ws._2014._10.transport.KeyCodeType;
import eu.osci.ws._2014._10.transport.MessageMetaData;
import eu.osci.ws._2014._10.transport.MsgIdentificationType;
import eu.osci.ws._2014._10.transport.OriginatorsType;
import eu.osci.ws._2014._10.transport.PartyIdentifierType;
import eu.osci.ws._2014._10.transport.PartyType;
import eu.osci.ws._2014._10.transport.QualifierType;
import eu.osci.ws._2014._10.transport.QualifierType.BusinessScenario;
import eu.osci.ws._2014._10.transport.QualifierType.MessageType;
import org.apache.cxf.ws.addressing.AttributedURIType;

import java.io.InputStream;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;


/**
 * Dieses Sample zeigt exemplarisch das Zusammenbauen und Versenden einer StoreDelivery-Nachricht, die ein
 * serialisiertes {@link eu.osci.ws._2014._10.transport.MessageMetaData}-XML-Element als Custom-SOAP-Header
 * einbindet. Das MessageMetaData-Element kann dabei z.B. aus einem XTA Transport oder aus dem Dateisystem
 * stammen.
 * <br><br>
 * Der Ablauf ist drei Kontexte unterteilt:
 * <br><br>
 * 1. es wird ein beispielhaftes MessageMetaData-Element aufgebaut<br>
 * 2. es wird eine StoreDelivery-Nachricht erstellt, das MessageMetaData-Objekt als CustomHeader
 * hinzugefügt und die Nachricht an einen Intermediär gesendet<br>
 * 3. die Nachricht wird vom Intermediär wieder abgeholt und das enthaltene MessageMetaData-Element
 * kontrolliert <br><br>
 * <p>Copyright © 2023 Governikus GmbH &amp; Co. KG, Germany</p>
 * <p>Erstellt von Governikus GmbH &amp; Co. KG</p>
 * <p>Diese Bibliothek kann von jedermann nach Maßgabe der European Union
 * Public Licence genutzt werden.</p><p>Die Lizenzbestimmungen können
 * unter der URL <a href="https://eupl.eu/">https://eupl.eu/</a> abgerufen werden.</p>
 */
public class SendMessageWithMessageMetaData
{

  private static final String PIN = "123456";

  /**
   * Startpunkt des Samples.
   */
  public static void main(String[] args) throws Exception
  {
    // 1. Beispiel-MessageMetaData-Element wird aufgebaut und serialisiert
    MessageMetaData mmd = createSampleMMD();
    // in vielen Fällen liegt das MessageMetaData-Element als Datei/Datenstrom vor, daher wird dies hier simuliert
    String mmdString = CustomHeaderHelper.getMessageMetaDataAsString(mmd);

    // 2. Übernahme des MessageMetaData in eine neue OSCI-Nachricht und versenden dieser
    String messageId = sendStoreDeliveryWithMessageMetaData(mmdString);

    // 3. Abholung der Nachricht mit der Nachrichten-ID und Prüfung des MessageMetaData-Elements
    fetchMessageAndCheckMessageMetaData(messageId);
  }

  private static String sendStoreDeliveryWithMessageMetaData(String mmd) throws Exception
  {
    // Transportkonfiguration für StoreDelivery-Versenden vorbereiten
    System.out.println("Bereite OSCI-StoreDelivery-Transport vor");
    DialogHandler senderDialog = createSendDialogHandler();
    Addressee addressee = createAddressee();
    String msgId = getMsgId(senderDialog);

    // Neue StoreDelivery-Nachricht erzeugen
    StoreDelivery storeDelivery = new StoreDelivery(senderDialog, addressee, msgId);

    // MessageMetaData-Element hinzufügen
    System.out.println("Füge der Nachricht das MessageMetaData-Element als CustomHeader hinzu");
    storeDelivery.addMessageMetaDataXTA2V3(mmd);

    // Nachricht versenden
    System.out.println("Versende OSCI-StoreDelivery-Nachricht an Intermediär");
    return storeDelivery.send().getMessageId();
  }

  private static void fetchMessageAndCheckMessageMetaData(String messageId) throws Exception
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

    // MessageMetaData-Element aus der Antwort holen
    MessageMetaData mmd = rsp2FetchDel.getMessageMetaDataXTA2V3();

    if (mmd.getMsgIdentification()
           .getMessageID()
           .getValue()
           .equals("urn:de:xta:messageid:TestXTAgovernikus:8dfasdf-67c1-44ad-dfss-46da9b73b0eb"))
    {
      System.out.println("Abgeholte Nachricht enthält MessageMetaData-Element mit der korrekten ID");
    }
    else
    {
      System.err.println("Abgeholte Nachricht enthält MessageMetaData-Element mit der falschen ID!");
    }

    // OSCI-Dialog beenden
    new ExitDialog(fetchDialog).send();

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
      PIN), new PKCS12Decrypter("/de/osci/osci12/samples/zertifikate/bob_cipher_4096.p12", PIN));
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
    return SendMessageWithMessageMetaData.class.getResourceAsStream(resource);
  }

  private static URI createIntermedUri() throws URISyntaxException
  {
    final String testEntry = "http://gov.current.tf.bos-test.de:8080/osci-manager-entry/externalentry";
    return new URI(testEntry);
  }

  private static Originator createOriginator() throws Exception
  {
    Signer signer = createSigner();
    Decrypter decrypter = new PKCS12Decrypter("/de/osci/osci12/samples/zertifikate/alice_cipher_4096.p12",
                                              PIN);
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

  private static String getMsgId(DialogHandler dh) throws Exception
  {
    final GetMessageId getMsgId = new GetMessageId(dh);
    final ResponseToGetMessageId rsp2GetMsgID = getMsgId.send();
    return rsp2GetMsgID.getMessageId();
  }

  /**
   * Erzeuge ein realitätsnahes Beispiel für ein {@link MessageMetaData}-Objekt
   *
   * @return
   */
  private static MessageMetaData createSampleMMD()
  {
    MessageMetaData mmd = new MessageMetaData();
    mmd.setDeliveryAttributes(new DeliveryAttributesType());
    mmd.getDeliveryAttributes().setServiceQuality("meldewesen");
    mmd.setOriginators(new OriginatorsType());

    PartyIdentifierType type = new PartyIdentifierType();
    type.setName("Author");
    type.setType("xoev");
    type.setValue("ags:01010101010");
    PartyType party = new PartyType();
    party.setIdentifier(type);
    mmd.getOriginators().setAuthor(party);

    type = new PartyIdentifierType();
    type.setName("Reader");
    type.setType("xoev");
    type.setValue("ags:01010101010");
    party = new PartyType();
    party.setIdentifier(type);
    mmd.setDestinations(new DestinationsType());
    mmd.getDestinations().setReader(party);

    AttributedURIType msgId = new AttributedURIType();
    msgId.setValue("urn:de:xta:messageid:TestXTAgovernikus:8dfasdf-67c1-44ad-dfss-46da9b73b0eb");
    mmd.setMsgIdentification(new MsgIdentificationType());
    mmd.getMsgIdentification().setMessageID(msgId);

    BusinessScenario bs = new BusinessScenario();
    KeyCodeType kct = new KeyCodeType();
    kct.setListURI("urn:de:xta:codeliste:business.scenario");
    kct.setListVersionID("2");
    kct.setCode("XINNERES_DATA");
    bs.setDefined(kct);

    MessageType msgType = new MessageType();
    msgType.setListURI("urn:de:codeliste:nachrichtentyp");
    msgType.setListVersionID("1");
    msgType.setPayloadSchema("http://www.osci.de/xinneres/rueckweisung/3/xinneres-rueckweisung.xsd");
    msgType.setCode("rueckweisung.asynchron.0010");
    msgType.setName("XInneres-Rueckweisung");
    QualifierType qualifier = new QualifierType();
    qualifier.setService("http://www.osci.de/xinneres/rueckweisung/3/xinneresrueckweisungv3.wsdl");
    qualifier.setBusinessScenario(bs);
    mmd.setMsgSize(BigInteger.valueOf(12345));
    return mmd;
  }


}
