package de.osci.osci12.messagetypes;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.util.Vector;

import de.osci.osci12.OSCIException;
import de.osci.osci12.common.OSCIExceptionCodes.OSCIErrorCodes;
import de.osci.osci12.messageparts.Attachment;
import de.osci.osci12.messageparts.ChunkInformation;
import de.osci.osci12.messageparts.Content;
import de.osci.osci12.messageparts.ContentContainer;
import de.osci.osci12.messageparts.EncryptedDataOSCI;
import de.osci.osci12.messageparts.FeedbackObject;
import de.osci.osci12.messageparts.MessagePartsFactory;
import de.osci.osci12.messageparts.ProcessCardBundle;
import de.osci.osci12.roles.Intermed;
import eu.osci.ws._2014._10.transport.MessageMetaData;


/**
 * <p>Eine Instanz dieser Klasse wird beim Einlesen einer serialisierten
 * OSCI-Nachricht beliebigen Typs (Nachrichten mit Inhaltsdaten) angelegt.</p>
 * Die Klasse dient folgenden Zwecken:<ul><li>Abspeichern und Wiedereinlesen von
 * Nachrichten mit Inhaltsdaten</li><li>Austausch von Inhaltsdaten zwischen
 * Autoren und Sendern bzw. Empfänger und Lesern.</li></ul>
 * <p>
 * Leser können ihre Inhaltsdaten in Nachrichten beliebigen Typs ablegen, diese
 * speichern und z.B. als Datei weiterreichen. Absender können eine solche Datei
 * mit der Methode loadMessage(InputStream) laden, die Inhaltdatencontainer
 * entnehmen und anderen Nachrichten hinzufügen.
 * </p><p>
 * Ein generelles Problem ist, dass in verschlüsselten Inhaltsdaten einer
 * OSCI-Nachricht die Informationen über die enthaltenen Referenzen auf
 * Zertifikate und Attachments ohne Entschlüsselung nicht verfügbar sind.
 * Im Zweifel müssen daher alle Zertifikate (z.B. mit den Methoden
 * OSCIMessage.getOtherAuthors(), OSCIMessage.getOtherReaders() und
 * OSCIMessage.addRole(Role)) und Attachments (Methoden
 * exportAttachment(OSCIMessage, Attachment) und exportAttachments(OSCIMessage))
 * entnommen und der neuen Nachricht hinzugefügt werden.
 * </p><p>
 * Anwendungen sollten dies berücksichtigen und für den Inhaltsdatenaustausch
 * möglichst mehrere einzelne Nachrichten statt einer komplexen verwenden.
 * Besonders problematisch ist in diesem Zusammenhang die Signatur durch
 * Originator- bzw. Verschlüsselung für Addressee-Rollenobjekte, weil diese
 * i.d.R. nicht in eine neue Nachricht übernommen werden können. Hier sollten
 * grundsätzlich Author- und Reader-Objekte verwendet werden.</p>
 * <p>Weiter ist zu beachten, dass es beim Zusammensetzen einer neuen Nachricht aus
 * Inhaltdatencontainern, die anderen Nachrichten entnommen wurden, zu Konflikten
 * mit den Ref-Ids der Content-Einträge kommen kann. Da die Bibliothek wegen der
 * ggf. vorhandenen Signatur diese Ids nicht selbst anpassen kann, sollten
 * Anwendungen eindeutige Ref-Ids setzen. Diese können z.B. aus Message-IDs
 * und laufenden Nummern oder Zertfikats-Ids (z.B. IssuerDN und SerialNumber)
 * und Datum/Uhrzeit generiert werden.</p>
 *
 *
 * <p>Copyright © 2023 Governikus GmbH &amp; Co. KG, Germany</p>
 * <p>Erstellt von Governikus GmbH &amp; Co. KG</p>
 * <p>Diese Bibliothek kann von jedermann nach Maßgabe der European Union
 *  Public Licence genutzt werden.</p><p>Die Lizenzbestimmungen können
 * unter der URL <a href="https://eupl.eu/">https://eupl.eu/</a> abgerufen werden.
 *
 *
 * @author R. Lindemann, N. Büngener
 * @version 2.4.1
 */
public class StoredMessage extends OSCIResponseTo implements ContentPackageI
{
  //  private static Log log = LogFactory.getLog(StoredMessage.class);
  ProcessCardBundle[] processCardBundles;
  ProcessCardBundle processCardBundleReply;
  String subject;
  java.net.URI uriReceiver;
  String selectionRule;
  int selectionMode = -1;
  long quantityLimit = -1;
  Vector<String[]> insideFeedBack;

  FeedbackObject[] insideFeedbackObjects;

  StoredMessage(int messageType)
  {
    this.messageType = messageType;
  }

  /**
   * Serialisiert die übergebene Nachricht und schreibt die Daten in den übergebenen Stream.
   * @param msg zu speichernde Nachricht; z.Zt. können StoredMessage-Objekte selbst nicht
   * gespeichert werden. Der Stream wird von der Methode <b>nicht</b> geschlossen.
   * @param output Ausgabestream
   * @throws IOException bei Schreibfehlern
   * @throws OSCIException bei Problemen beim Aufbau der Nachricht
   * @throws NoSuchAlgorithmException wenn der installierte Security-Provider einen
   * benötigten Algorithmus nicht unterstützt
   * @see #loadMessage(InputStream)
   */
  public static void storeMessage(OSCIMessage msg, OutputStream output)
                           throws IOException,
                                  OSCIException,
                                  NoSuchAlgorithmException
  {
    if (msg instanceof StoredMessage)
      throw new UnsupportedOperationException();

    msg.writeXML(output);
  }

  /**
   * Liest eine (unverschlüsselte) Nachricht aus dem übergebenen Stream. Die in
   * der Nachricht enthaltenen Inhalte können dem zurückgegebenen
   * Nachrichtenobjekt entnommen werden. <b>Die Methode prüft keine Signaturen.</b>
   * @param input zu lesender Stream
   * @return StoredMessage-Instanz
   * @throws IOException bei Schreibfehlern
   * @throws OSCIException bei Problemen beim Aufbau der Nachricht
   * @throws NoSuchAlgorithmException wenn der installierte Security-Provider einen
   * benötigten Algorithmus nicht unterstützt
   * @see #storeMessage(OSCIMessage, OutputStream)
   * @see #loadMessageCheckingSignature(InputStream)
   */
  public static StoredMessage loadMessage(InputStream input)
                                   throws IOException,
                                          OSCIException,
                                          NoSuchAlgorithmException
  {
    return new StoredMessageParser().parseStream(input);
  }

  /**
   * Liest eine (unverschlüsselte) Nachricht aus dem übergebenen Stream. Die in
   * der Nachricht enthaltenen Inhalte können dem zurückgegebenen
   * Nachrichtenobjekt entnommen werden. Diese Methode prüft außerdem
   * eine ggf. vorhandene Nachrichtensignatur. Liefert die Prüfung ein
   * negatives Ergebnis, wird eine OSCIException (Code 9601) geworfen.
   * <b>Die Methode prüft keine Inhaltsdatensignaturen. Ebenso wird
   * keine Exception ausgelöst, wenn die Nachricht unsigniert ist.</b>
   * @param input zu lesender Stream
   * @return StoredMessage-Instanz
   * @throws IOException bei Schreibfehlern
   * @throws OSCIException bei Problemen beim Aufbau der Nachricht oder ungültiger Nachrichtensignatur
   * @throws NoSuchAlgorithmException wenn der installierte Security-Provider einen
   * benötigten Algorithmus nicht unterstützt
   * @see #storeMessage(OSCIMessage, OutputStream)
   * @see #loadMessage(InputStream)
   */
  public static StoredMessage loadMessageCheckingSignature(InputStream input)
                                                    throws IOException,
                                                           OSCIException,
                                                           NoSuchAlgorithmException
  {
    StoredMessageParser smp = new StoredMessageParser();
    StoredMessage sm = smp.parseStream(input);

    if (sm.isSigned())
    {
      if (sm.getMessageType() <= 0x10)
        sm.dialogHandler.supplier.setSignatureCertificate(sm.dialogHandler.client.getSignatureCertificate());

      if (!smp.checkMsgHashes(sm))
        throw new OSCIException(OSCIErrorCodes.SignatureInvalid);
    }

    return sm;
  }

  /**
   * Diese Methode exportiert alle Attachments der Nachricht in eine andere
   * OSCI-Nachricht beliebigen Typs.
   * @param destinationMessage Zielnachricht
   * @see #exportAttachment(OSCIMessage, Attachment)
   */
  public void exportAttachments(OSCIMessage destinationMessage)
  {
    Attachment[] atts = getAttachments();

    for (int i = 0; i < atts.length; i++)
      destinationMessage.addAttachment(atts[i]);
  }

  /**
   * Diese Methode exportiert ein Attachment der Nachricht in eine andere
   * OSCI-Nachricht beliebigen Typs.
   * @param destinationMessage Zielnachricht
   * @param att zu exportierendes Attachment
   * @see #exportAttachments(OSCIMessage)
   */
  public void exportAttachment(OSCIMessage destinationMessage, Attachment att)
  {
    Attachment[] atts = getAttachments();

    for (int i = 0; i < atts.length; i++)
    {
      if (att.equals(atts[i]))
      {
        destinationMessage.addAttachment(atts[i]);

        return;
      }
    }
  }

  /**
   * Diese Methode liefert den Laufzettel der Nachricht zurück.
   * Im Falle einer Abwicklungsantwort wird der Laufzettel des Requests
   * zurückgegeben.
   *
   * @return den Laufzettel des Auftrags als ProcessCardBundle-Objekt oder null,
   * wenn der Nachrichtentyp keinen Laufzettel enthält
   * @see #getProcessCardBundleReply()
   * @see de.osci.osci12.messagetypes.ResponseToMediateDelivery#getProcessCardBundleRequest()
   */
  public ProcessCardBundle getProcessCardBundle()
  {
    if (processCardBundles == null)
      return null;

    return processCardBundles[0];
  }

  /**
   * Diese Methode liefert die Laufzettel einer Laufzettelabholantwort zurück.
   *
   * @return den Laufzettel des Auftrags als ProcessCardBundle-Objekt
   * @throws UnsupportedOperationException wenn die Nachricht nicht vom Typ
   * ResponseToFetchProcessCard ist
   * @see #getProcessCardBundle()
   * @see de.osci.osci12.messagetypes.ResponseToFetchProcessCard#getProcessCardBundles()
   */
  public ProcessCardBundle[] getProcessCardBundles()
  {
    if (messageType != OSCIMessage.RESPONSE_TO_FETCH_PROCESS_CARD)
      throw new UnsupportedOperationException();

    return processCardBundles;
  }

  /**
   * Diese Methode liefert den Antwort-Laufzettel einer Abwicklungsantwort zurück.
   *
   * @return den Laufzettel der Nachricht als ProcessCardBundle-Objekt
   * @throws UnsupportedOperationException wenn die Nachricht nicht vom Typ
   * ResponseToMediateDelivery ist.
   * @see #getProcessCardBundle()
   */
  public ProcessCardBundle getProcessCardBundleReply()
  {
    if (messageType != OSCIMessage.RESPONSE_TO_MEDIATE_DELIVERY)
      throw new UnsupportedOperationException();

    return processCardBundleReply;
  }

  /**
   * Liefert die Message-ID der Nachricht.
   * Im Falle einer Abwicklungsantwort wird die Message-ID des Requests
   * zurückgegeben.
   * @return Message-ID
   */
  public String getMessageId()
  {
    return messageId;
  }

  void setMessageId(String messageId)
  {
    this.messageId = messageId;
  }

  /**
   * Liefert das Intermediärsobjekt oder null, wenn keine Zertifikate in der
   * Nachricht enthalten sind.
   * @return Intermediär
   */
  public Intermed getIntermediary()
  {
    if (dialogHandler.getSupplier() instanceof Intermed)
      return (Intermed) dialogHandler.getSupplier();
    else

      return (Intermed) dialogHandler.getClient();
  }

  /**
   * Liefert die in die Nachricht eingestellten (unverschlüsselten) Inhaltsdaten als ContentContainer-Objekte.
   * @return enthaltene ContentContainer mit Inhaltsdaten
   * @see de.osci.osci12.messageparts.ContentContainer
   */
  public ContentContainer[] getContentContainer()
  {
    return super.getContentContainer();
  }

  /**
   * Liefert die in die Nachricht eingestellten verschlüsselten Inhaltsdaten
   * als EncryptedData-Objekte.
   * @return enthaltene EncryptedData-Objekt mit verschlüsselten Inhaltsdaten
   * @see EncryptedDataOSCI
   */
  public EncryptedDataOSCI[] getEncryptedData()
  {
    return super.getEncryptedData();
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
   * Liefert den Betreff der Nachricht oder null, wenn kein Betreff in der
   * Nachricht enthalten ist.
   * @return den Betreff
   */
  public String getSubject()
  {
    return subject;
  }

  /**
   * Liefert die URI des Nachrichtenempfängers oder null, wenn keine Empfänger-URI
   * in der Nachricht enthalten ist.
   * @return URI
   */
  public java.net.URI getUriReceiver()
  {
    return uriReceiver;
  }

  /**
   * Liefert die Qualität des Zeitstempels, mit dem der Intermediär den
   * Eingang des Auftrags im Laufzettel protokolliert.
   * @return Qualität des Zeitstempels: <b>true</b> - kryptographischer Zeitstempel von einem
   * akkreditierten Zeitstempeldienst<br><b>false</b> - Einfacher Zeitstempel
   * (lokale Rechnerzeit des Intermediärs)
   * @throws UnsupportedOperationException wenn der repräsentierte Nachrichtentyp
   * keine Zeitstempelanforderung enthält
   * @see #getQualityOfTimeStampReception()
   */
  public boolean getQualityOfTimeStampCreation()
  {
    if ((messageType != OSCIMessage.STORE_DELIVERY) && (messageType != OSCIMessage.FORWARD_DELIVERY) &&
            (messageType != OSCIMessage.MEDIATE_DELIVERY) && (messageType != OSCIMessage.RESPONSE_TO_PROCESS_DELIVERY) && (messageType != OSCIMessage.PARTIAL_STORE_DELIVERY))
      throw new UnsupportedOperationException();

    return qualityOfTimestampTypeCreation.isQualityCryptographic();
  }

  /**
   * Liefert die geforderte Qualität des Zeitstempels, mit dem der Intermediär den
   * Empfang der Annahmeantwort im Laufzettel protokolliert.
   * @return Qualität des Zeitstempels: <b>true</b> - kryptographischer Zeitstempel von einem
   * akkreditierten Zeitstempeldienst<br><b>false</b> - Einfacher Zeitstempel
   * (lokale Rechnerzeit des Intermediärs)
   * @throws UnsupportedOperationException wenn der repräsentierte Nachrichtentyp
   * keine Zeitstempelanforderung enthält
   * @see #getQualityOfTimeStampCreation()
   */
  public boolean getQualityOfTimeStampReception()
  {
    if ((messageType != OSCIMessage.STORE_DELIVERY) && (messageType != OSCIMessage.FORWARD_DELIVERY) &&
            (messageType != OSCIMessage.MEDIATE_DELIVERY) && (messageType != OSCIMessage.RESPONSE_TO_PROCESS_DELIVERY) && (messageType != OSCIMessage.PARTIAL_STORE_DELIVERY))
      throw new UnsupportedOperationException();

    return qualityOfTimestampTypeReception.isQualityCryptographic();
  }

  /**
   * Liefert den gesetzten Auswahlmodus für Nachrichten oder Laufzettel.
   * @return den Auswahlmodus SELECT_BY_MESSAGE_ID, SELECT_BY_DATE_OF_RECEPTION
   * oder NO_SELECTION_RULE
   * @throws UnsupportedOperationException wenn der repräsentierte Nachrichtentyp
   * keine Auswahlmöglichkeit enthält
   * @see #getSelectionRule()
   */
  public int getSelectionMode()
  {
    if ((messageType != OSCIMessage.FETCH_DELIVERY) && (messageType != OSCIMessage.FETCH_PROCESS_CARD) &&
            (messageType != OSCIMessage.RESPONSE_TO_FETCH_DELIVERY) &&
            (messageType != OSCIMessage.RESPONSE_TO_FETCH_PROCESS_CARD))
      throw new UnsupportedOperationException();

    return selectionMode;
  }

  /**
   * Liefert die gesetzte Auswahlregel für Nachrichten oder Laufzettel.
   * Wurde keine Regel gesetzt, wird als default null zurückgegeben.
   * @return Auswahlregel (Message-ID oder Datum)
   * @throws UnsupportedOperationException wenn der repräsentierte Nachrichtentyp
   * keine Auswahlmöglichkeit enthält
   * @see #getSelectionMode()
   */
  public String getSelectionRule()
  {
    if ((messageType != OSCIMessage.FETCH_DELIVERY) && (messageType != OSCIMessage.FETCH_PROCESS_CARD) &&
            (messageType != OSCIMessage.RESPONSE_TO_FETCH_DELIVERY) &&
            (messageType != OSCIMessage.RESPONSE_TO_FETCH_PROCESS_CARD))
      throw new UnsupportedOperationException();

    return selectionRule;
  }

  /**
   * Liefert die maximale Anzahl zurückzugebender Laufzettel.
   * @return gesetzte maximale Anzahl
   * @throws UnsupportedOperationException wenn die repräsentierte Nachricht
   * kein Laufzettelabholauftrag oder -antwort ist.
   */
  public long getQuantityLimit()
  {
    if ((messageType != OSCIMessage.FETCH_PROCESS_CARD) && (messageType != OSCIMessage.RESPONSE_TO_FETCH_PROCESS_CARD))
      throw new UnsupportedOperationException();

    return quantityLimit;
  }
  /**
   * Diese Feedback-Einträge sind Fehlermeldungen und Warnings aus der zusammengebauten StoreDelivery
   * Nachricht. Liefert die Rückmeldung (Feedback-Eintrag) als String-Array zurück. Dieses Feedback wird nur
   * gefüllt, sobald die letzte paketierte Nachricht übertragen wurde. Der erste Index des Arrays entspricht dem
   * Index des Entry-Elementes. Beim zweiten Index bezeichnet <br>
   * 0 - das Sprachkürzel (z.B. "de", "en-US", optional)<br>
   * 1 - den Code<br>
   * 2 - den Text<br>
   *
   * @return Rückmeldung
   */
  public String[][] getInsideFeedback()
  {
    if (insideFeedBack == null)
      return null;

    return insideFeedBack.toArray(new String[insideFeedBack.size()][3]);
  }

  /**
   * Diese Feedback-Einträge sind Fehlermeldungen und Warnings aus der zusammengebauten StoreDelivery Nachricht.
   * Es wird ein Objekt-Array (FeedbackObject[]) zurück gegeben.
   *
   * @return Array von Feedback-Objekten
   * @see #getInsideFeedback()
   */
  public FeedbackObject[] getInsideFeedbackObjects()
  {
    if (insideFeedBack == null)
      return null;

    if (insideFeedbackObjects == null)
    {
      insideFeedbackObjects = new FeedbackObject[insideFeedBack.size()];

      for ( int i = 0 ; i < insideFeedBack.size() ; i++ )
        insideFeedbackObjects[i] = MessagePartsFactory.createFeedbackObject(insideFeedBack.get(i));
    }

    return insideFeedbackObjects;
  }
}
