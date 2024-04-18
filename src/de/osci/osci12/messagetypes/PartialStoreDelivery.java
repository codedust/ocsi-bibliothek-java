package de.osci.osci12.messagetypes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;

import eu.osci.ws._2014._10.transport.MessageMetaData;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.osci.helper.Tools;
import de.osci.osci12.OSCIException;
import de.osci.osci12.common.Constants;
import de.osci.osci12.common.Constants.HeaderTags;
import de.osci.osci12.common.Constants.LanguageTextEntries;
import de.osci.osci12.common.DialogHandler;
import de.osci.osci12.common.OSCICancelledException;
import de.osci.osci12.messageparts.Attachment;
import de.osci.osci12.messageparts.Body;
import de.osci.osci12.messageparts.ChunkInformation;
import de.osci.osci12.messageparts.Content;
import de.osci.osci12.messageparts.ContentContainer;
import de.osci.osci12.messageparts.EncryptedDataOSCI;
import de.osci.osci12.messageparts.MessagePartsFactory;
import de.osci.osci12.roles.Addressee;
import de.osci.osci12.roles.OSCIRoleException;
import de.osci.osci12.roles.Originator;
import de.osci.osci12.soapheader.OsciH;
import de.osci.osci12.soapheader.QualityOfTimestampH;


/**
 * <p>
 * Mit dieser Klasse werden Nachrichtenobjekte für paketierte Zustellungsaufträge angelegt. Bei diesem
 * Nachrichtentyp wird eine Store-Delivery Nachricht in mehreren Bestandteilen versendet. Da von diesem
 * Nachrichtentyp viele Nachrichten mit gleichen Zertifikaten gesendet werden, sollte dieser Nachrichtentyp in
 * einem explizitem Dialog durchgeführt werden.
 * </P>
 * <p>
 * In der Responsenachricht wird eine Porcesscard sowie ein Feedback für die PartialStoreDelivery Nachricht
 * erwartet. Sollte dieses Objekt die letzte Nachricht einer paketierten Zustellung sein, wird die Processcard
 * der gesamten Nachricht und ein Inside Feedback erwartet.
 * </p>
 * <p>
 * Copyright © 2023 Governikus GmbH &amp; Co. KG, Germany
 * </p>
 * <p>Erstellt von Governikus GmbH &amp; Co. KG</p>
 * <p>
 * Diese Bibliothek kann von jedermann nach Maßgabe der European Union Public Licence genutzt
 * werden.
 * </p>
 * <p>
 * Die Lizenzbestimmungen können unter der URL <a href="https://eupl.eu/">https://eupl.eu/</a> abgerufen werden.
 * </p>
 *
 * @author R. Lindemann
 * @version 2.4.1
 * @since 1.8.0
 * @see ResponseToPartialStoreDelivery
 */
public class PartialStoreDelivery extends OSCIRequest implements ContentPackageI
{

  protected static Log log = LogFactory.getLog(PartialStoreDelivery.class);

  private String subject;

  private boolean infoOnly = false;

  private ChunkInformation chunkInformation;


  PartialStoreDelivery()
  {
    super();
    messageType = PARTIAL_STORE_DELIVERY;
    setBase64Encoding(false);
  }

  /**
   * Legt ein Nachrichtenobjekt für einen paketierten Zustellungsauftrag an.
   *
   * @param dh DialogHandler-Objekt des Dialogs, innerhalb dessen die Nachricht versendet werden soll
   * @param addressee Rollenobjekt des Empfängers
   * @param chunkInforamtion Informationen zu dem aktuellem Chunk
   * @param messageId Message-ID der originalen StoreDelivery Nachricht. Diese Klasse hängt selbstständig den
   *          Postfix "_Partial" an.
   * @throws OSCIRoleException wenn das Verschlüsselungszertifikat des Empfängers fehlt
   * @throws NoSuchAlgorithmException wenn der verwendete Security-Provider einen erforderlichen Algorithmus
   *           nicht unterstützt (Erzeugung einer Zufallszahl)
   * @see de.osci.osci12.common.DialogHandler
   */
  public PartialStoreDelivery(DialogHandler dh,
                              Addressee addressee,
                              ChunkInformation chunkInforamtion,
                              String messageId)
    throws OSCIRoleException, NoSuchAlgorithmException
  {
    super(dh);
    messageType = PARTIAL_STORE_DELIVERY;
    originator = (Originator)dh.getClient();
    this.addressee = addressee;
    // Check, ob ein Cipherzert eingestellt wurde
    addressee.getCipherCertificate();
    setChunkInformation(chunkInforamtion);
    if (!messageId.endsWith("_Partial"))
    {
      this.messageId = messageId.concat("_Partial");
    }
    else
    {
      this.messageId = messageId;
    }
    setBase64Encoding(false);
    if (!dialogHandler.isExplicitDialog())
      dialogHandler.resetControlBlock();
    setQualityOfTimeStampCreation(false);
    dialogHandler.getControlblock().setResponse(dialogHandler.getControlblock().getChallenge());
    dialogHandler.getControlblock().setChallenge(Tools.createRandom(10));
    dialogHandler.getControlblock()
                 .setSequenceNumber(dialogHandler.getControlblock().getSequenceNumber() + 1);
  }

  /**
   * Legt ein Nachrichtenobjekt für einen paketierten Zustellungsauftrag an. Es wird lediglich eine PartialStoreDelivery Nachricht mit 'InfoOnly' aufgebaut!
   *
   * @param dh DialogHandler-Objekt des Dialogs, innerhalb dessen die Nachricht versendet werden soll
   * @param addressee Rollenobjekt des Empfängers
   * @param infoOnly OnlyInfo wird angefordert
   * @param messageId Message-ID der originalen StoreDelivery Nachricht. Diese Klasse hängt selbstständig den
   *          Postfix "_Partial" an.
   * @throws OSCIRoleException wenn das Verschlüsselungszertifikat des Empfängers fehlt
   * @throws NoSuchAlgorithmException wenn der verwendete Security-Provider einen erforderlichen Algorithmus
   *           nicht unterstützt (Erzeugung einer Zufallszahl)
   * @see de.osci.osci12.common.DialogHandler
   */
  public PartialStoreDelivery(DialogHandler dh,
                              Addressee addressee,
                             boolean infoOnly,
                              String messageId)
    throws OSCIRoleException, NoSuchAlgorithmException
  {
    this(dh,addressee,null,messageId);
    if(!infoOnly)
    {
      log.error("Error create constructor with infoOnly set to false");
      throw new IllegalArgumentException(DialogHandler.text.getString(LanguageTextEntries.invalid_thirdargument.name())
                                         + " InfoOnly");
    }
    setInfoOnly(true);
  }

  /**
   * @return Liefert die eingestellte ChunkInformation
   * @see ChunkInformation
   * @see #PartialStoreDelivery(DialogHandler, Addressee, ChunkInformation, String)
   * @see #setChunkInformation(ChunkInformation)
   */
  public ChunkInformation getChunkInformation()
  {
    return chunkInformation;
  }

  /**
   * @param chunkInformation Setzt die aktuelle ChunkInformation. Die ChunkInformation sollte schon mit dem
   *          Konstrukor gesetzt werden
   * @see #PartialStoreDelivery(DialogHandler, Addressee, ChunkInformation, String)
   * @see #getChunkInformation()
   */
  public void setChunkInformation(ChunkInformation chunkInformation)
  {
    if(infoOnly)
    throw new IllegalArgumentException(DialogHandler.text.getString(LanguageTextEntries.invalid_firstargument.name())
                                       + " InfoOnly");
    this.chunkInformation = chunkInformation;
  }

  /**
   * Setzt den Betreff-Eintrag im Laufzettel. Der übergebene Parameter-String muss den Vorschriften für den
   * primitiven XML-Datentyp "string" entsprechen, darf also z.B. keine XML-Steuerzeichen (&lt;, &gt; o.&auml;.)
   * enthalten. Hier sollte der gleiche Betreff wie in der Store Delivery Nachricht eingetragen werden
   *
   * @param subject der Betreff
   */
  public void setSubject(String subject)
  {
    this.subject = subject;
  }

  /**
   * @return Liefert True sobald Info Only angefordert wurde.
   * @see #setInfoOnly(boolean)
   */
  public boolean isInfoOnly()
  {
    return infoOnly;
  }

  /**
   * @param infoOnly Mit diesem Schalter kann die PartialStoreDelivery Nachricht in den Modus Info-Only
   *          überführt werden. Die ChunkInformation wird dann ignoriert und es sollte kein ChunkBlob
   *          eingetragen werden
   * @see #isInfoOnly()
   */
  public void setInfoOnly(boolean infoOnly)
  {
    this.infoOnly = infoOnly;
  }

  /**
   * Setzt den aktuellen zu übertragen Chunk der kompletten StoreDelivery Nachricht. Dieser Stream wird in die
   * PartialStoreDelivery Nachricht als Attachment eingefügt.
   *
   * @param chunkBlob InputStream von einem Teil(Chunk) der großen Store Delivery Nachricht.
   * @throws OSCIRoleException bei OSCI-Fehlern
   * @throws NoSuchAlgorithmException Bei falschen Algorithmen
   * @throws IOException bei Ein-/Ausgabefehlern
   */
  public void setChunkBlob(InputStream chunkBlob)
    throws OSCIRoleException, NoSuchAlgorithmException, IOException
  {
    ContentContainer container = new ContentContainer();
    container.setRefID("ChunkContentContainer");
    Attachment atta = new Attachment(chunkBlob, "ChunkBlobStoreDelivery");
    atta.setBase64Encoding(false);
    Content content = new Content(atta);
    content.setRefID("ChunkContent");
    container.addContent(content);
    super.addContentContainer(container);
  }


  /**
   * Versendet die Nachricht und liefert die Antwortnachricht zurück. Diese Methode wirft eine Exception, wenn
   * beim Aufbau oder Versand der Nachricht ein Fehler auftritt. Fehlermeldungen vom Intermediär müssen dem
   * Feedback der Antwortnachricht entnommen werden.
   *
   * @throws IOException bei Ein-/Ausgabefehlern
   * @throws de.osci.osci12.OSCIException bei OSCI-Fehlern
   * @throws NoSuchAlgorithmException undocumented
   * @return Antwortnachricht-Objekt
   * @see #send(OutputStream, OutputStream)
   */
  public ResponseToPartialStoreDelivery send() throws IOException, OSCIException, NoSuchAlgorithmException
  {
    if (base64)
      throw new IllegalStateException("Base64 should be disabled!");
    return (ResponseToPartialStoreDelivery)transmit(null, null);
  }

  /**
   * Versendet die Nachricht und liefert die Antwortnachricht zurück. Die aus- und eingehenden Daten werden
   * zusätzlich in die übergebenen Streams geschrieben (unverschlüsselte Auftragsdaten). Diese Parameter
   * dürfen null sein.
   *
   * @param storeOutput Stream, in den die versendete Nachricht geschrieben wird
   * @param storeInput Stream, in den die empfangene Antwortnachricht geschrieben wird
   * @throws IOException bei Ein-/Ausgabefehlern
   * @throws de.osci.osci12.OSCIException bei OSCI-Fehlern
   * @throws NoSuchAlgorithmException undocumented
   * @return Antwortnachricht-Objekt
   * @see #send()
   */
  public ResponseToPartialStoreDelivery send(OutputStream storeOutput, OutputStream storeInput)
    throws IOException, OSCIException, NoSuchAlgorithmException
  {
    if (base64)
      throw new IllegalStateException("Base64 should be disabled!");
    return (ResponseToPartialStoreDelivery)transmit(storeOutput, storeInput);
  }

  /**
   * Bringt eine Client-Signatur an.
   *
   * @throws IOException bei Schreib-/Leseproblemen
   * @throws OSCIException wenn Zusammenstellen der Daten ein Problem auftritt.
   * @throws de.osci.osci12.common.OSCICancelledException bei Abbruch durch den Benutzer
   */
  void sign()
    throws IOException, OSCIException, OSCICancelledException, java.security.NoSuchAlgorithmException
  {
    super.sign();
    // Der NonIntemedCertHeader liegt hier weiter unten
    messageParts.set(3, null);
    messageParts.set(7, nonIntermediaryCertificatesH);
  }

  /**
   * Setzt die Nachricht zusammen
   *
   * @throws OSCIRoleException Im Fehlerfall
   * @throws IOException Im Fehlerfall
   * @throws IllegalStateException Im Fehlerfall
   */
  @Override
  protected void compose() throws OSCIException, IOException, NoSuchAlgorithmException
  {
    super.compose();
    messageParts.set(3, null);
    messageParts.add(qualityOfTimestampTypeCreation);
    messageParts.add(null);
    if (dialogHandler.getControlblock().getChallenge() == null)
      throw new IllegalStateException(DialogHandler.text.getString(LanguageTextEntries.missing_entry.name()) + ": Challenge");

    if (dialogHandler.getControlblock().getSequenceNumber() == -1)
      throw new IllegalStateException(DialogHandler.text.getString(LanguageTextEntries.missing_entry.name()) + ": SequenceNumber");

    importAllCertificates();
    ByteArrayOutputStream parStrDelHeader = new ByteArrayOutputStream();
    String msgIdElement = "<" + osciNSPrefix + ":MessageId>"
                          + de.osci.helper.Base64.encode(messageId.getBytes(Constants.CHAR_ENCODING)) + "</"
                          + osciNSPrefix + ":MessageId>";
    parStrDelHeader.write(msgIdElement.getBytes());
    if (subject != null)
      parStrDelHeader.write(("<" + osciNSPrefix + ":Subject>" + subject + "</" + osciNSPrefix
                             + ":Subject>").getBytes());
    if (infoOnly)
    {
      parStrDelHeader.write(("<" + osci2017NSPrefix + ":InfoOnly></" + osci2017NSPrefix
                             + ":InfoOnly>").getBytes());
    }
    else
    {
      MessagePartsFactory.writeXML(chunkInformation,parStrDelHeader);
    }
    osciH = new OsciH(HeaderTags.partialStoreDelivery.getElementName(),
                      new String(parStrDelHeader.toByteArray(), "UTF-8"), osci2017NSPrefix);
    messageParts.add(osciH);
    createNonIntermediaryCertificatesH();
    nonIntermediaryCertificatesH.getCipherCertificateAddressee();
    messageParts.add(nonIntermediaryCertificatesH);
    if (featureDescription != null && dialogHandler.isSendFeatureDescription())
    {
      messageParts.add(featureDescription);
    }
    messageParts.addAll(customHeaders);
    if (infoOnly)
    {
      body = new Body(new ContentContainer[]{}, new EncryptedDataOSCI[]{});
    }
    else
    {
      if(contentContainer== null || contentContainer.size()!= 1)
      {
        throw new IllegalStateException("Wrong count of ContentContainer objects.");
      }
      if(encryptedData!= null && !encryptedData.isEmpty())
      {
        throw new IllegalStateException("Wrong count of EncryptedData objects.");
      }
      body = new Body(getContentContainer(), getEncryptedData());
    }
    messageParts.add(body);
    stateOfMsg |= STATE_COMPOSED;
  }


  /**
   * Setzt die gewünschte Qualität des Zeitstempels, mit dem der Intermediär den Eingang des Auftrags im
   * Laufzettel protokolliert.
   *
   * @param cryptographic <b>true</b>: kryptographischer Zeitstempel von einem akkreditierten
   *          Zeitstempeldienst<br>
   *          <b>false</b>: Einfacher Zeitstempel (lokale Rechnerzeit des Intermediärs, default)
   * @see #getQualityOfTimeStampCreation()
   */
  public void setQualityOfTimeStampCreation(boolean cryptographic)
  {
    qualityOfTimestampTypeCreation = new QualityOfTimestampH(false, cryptographic);
  }

  /**
   * Liefert die Qualität des Zeitstempels, mit dem der Intermediär den Eingang des Auftrags im Laufzettel
   * protokolliert.
   *
   * @return Qualität des Zeitstempels: <b>true</b> - kryptographischer Zeitstempel von einem akkreditierten
   *         Zeitstempeldienst<br>
   *         <b>false</b> - Einfacher Zeitstempel (lokale Rechnerzeit des Intermediärs)
   * @see #setQualityOfTimeStampCreation(boolean)
   */
  public boolean getQualityOfTimeStampCreation()
  {
    return qualityOfTimestampTypeCreation.isQualityCryptographic();
  }

  /**
   * @return Der eingestellte Betreff. Sollte dem Betreff der Store Delivery Nachtricht entsprechen.
   */
  @Override
  public String getSubject()
  {
    return subject;
  }

  /**
   * Durchsucht <b>die unverschlüsselten</b> Inhaltsdaten nach dem ContentContainer
   * mit der übergebenen RefID.
   * @param refID zu suchende RefID
   * @return den zugehörigen ContentContainer oder null, wenn die Referenz
   * nicht gefunden wurde.
   */
  public ContentContainer getContentContainerByRefID(String refID)
  {
    return super.getContentContainerByRefID(refID);
  }

  /**
   * Durchsucht <b>die unverschlüsselten</b> ContentContainer nach dem Content
   * mit der übergebenen RefID.
   * @param refID zu suchende RefID
   * @return den zugehörigen Content oder null, wenn die Referenz
   * nicht gefunden wurde.
   */
  public Content getContentByRefID(String refID)
  {
  return super.getContentByRefID(refID);
  }

  /**
   * Diese Methode sollte nicht benutzt werden! Es sollten keine weiteren Nachrichtenbestandteile hinzugefügt werden.
   * Liefert die in der Nachricht eingestellten (unverschlüsselten) Inhaltsdaten als ContentContainer-Objekte.
   *
   * @return enthaltene ContentContainer mit Inhaltsdaten
   * @see de.osci.osci12.messageparts.ContentContainer
   */
  @Override
  public ContentContainer[] getContentContainer()
  {
    return super.getContentContainer();
  }

  /**
   * Diese Methode sollte nicht benutzt werden! Es sollten keine weiteren Nachrichtenbestandteile hinzugefügt werden.
   * Liefert die in der Nachricht eingestellten verschlüsselten Inhaltsdaten als EncryptedData-Objekte.
   *
   * @return enthaltene EncryptedData-Objekt mit verschlüsselten Inhaltsdaten
   * @see EncryptedDataOSCI
   */
  @Override
  public EncryptedDataOSCI[] getEncryptedData()
  {
    return super.getEncryptedData();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addMessageMetaDataXTA2V3(MessageMetaData mmd)
  {
    super.addMessageMetaDataXTA2V3(mmd);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addMessageMetaDataXTA2V3(String mmd)
  {
    super.addMessageMetaDataXTA2V3(mmd);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public MessageMetaData getMessageMetaDataXTA2V3()
  {
    return super.getMessageMetaDataXTA2V3();
  }

 /**
  * @return Liefert die eingetragene MessageId.
  */
  @Override
  public String getMessageId()
  {
    return this.messageId;
  }


}
