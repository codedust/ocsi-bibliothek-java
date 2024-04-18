package de.osci.osci12.messagetypes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import de.osci.helper.Tools;
import de.osci.osci12.OSCIException;
import de.osci.osci12.common.Constants;
import de.osci.osci12.common.Constants.HeaderTags;
import de.osci.osci12.common.DialogHandler;
import de.osci.osci12.messageparts.Attachment;
import de.osci.osci12.messageparts.Body;
import de.osci.osci12.messageparts.Content;
import de.osci.osci12.messageparts.ContentContainer;
import de.osci.osci12.messageparts.EncryptedDataOSCI;
import de.osci.osci12.messageparts.Inspection;
import de.osci.osci12.messageparts.MessagePartsFactory;
import de.osci.osci12.messageparts.ProcessCardBundle;
import de.osci.osci12.messageparts.Timestamp;
import de.osci.osci12.roles.Addressee;
import de.osci.osci12.roles.Author;
import de.osci.osci12.roles.OSCIRoleException;
import de.osci.osci12.soapheader.OsciH;
import eu.osci.ws._2014._10.transport.MessageMetaData;


/**
 * <p>Dieses Klasse repräsentiert die Antwort des Intermediärs auf einen
 * Abwicklungsauftrag.
 * Clients erhalten vom Intermediär eine Instanz dieser Klasse, die eine Rückmeldung
 * über den Erfolg der Operation (getFeedback()) sowie ggf. den zum Auftrag
 * (Abwicklungs-/Bearbeitungsauftrag) gehörenden Laufzettel, den zur Antwort
 * (Bearbeitungs-/Abwicklungsantwort) gehörenden Laufzettel und
 * verschlüsselte bzw. unverschlüsselte Inhaltsdaten enthält.</p>
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
 * @see de.osci.osci12.messagetypes.MediateDelivery
 */
public class ResponseToMediateDelivery extends OSCIResponseTo implements ContentPackageI
{
  //  private static Log log = LogFactory.getLog(ResponseToMediateDelivery.class);
  public ProcessCardBundle processCardBundleRequest;
  public ProcessCardBundle processCardBundleReply;

  ResponseToMediateDelivery(DialogHandler dh) throws OSCIRoleException
  {
    super(dh);
    messageType = RESPONSE_TO_MEDIATE_DELIVERY;

    // Nun wirds bunt: Weil in diesem Szenario die Rollen wechseln (der Orginator der Anfrage wird zum Addressee der Antwort
    // und umgekehrt) werden die Rollenobjekte umgebaut.
    de.osci.osci12.extinterfaces.crypto.Signer signer = null;
    de.osci.osci12.extinterfaces.crypto.Decrypter decrypter = null;

    if (dialogHandler.getClient().hasSignaturePrivateKey())
      signer = dialogHandler.getClient().getSigner();

    if (dialogHandler.getClient().hasCipherPrivateKey())
      decrypter = dialogHandler.getClient().getDecrypter();

    addressee = new Addressee(signer, decrypter);
  }

  ResponseToMediateDelivery(MediateDelivery medDel, ResponseToProcessDelivery rspProcDel)
                     throws NoSuchAlgorithmException,
                            OSCIRoleException
  {
    super(medDel.dialogHandler);
    messageType = RESPONSE_TO_MEDIATE_DELIVERY;
    addressee = rspProcDel.addressee;
    originator = rspProcDel.originator;

    ContentContainer[] con = rspProcDel.getContentContainer();

    for (int i = 0; i < con.length; i++)
      addContentContainer(con[i]);

    EncryptedDataOSCI[] enc = rspProcDel.getEncryptedData();

    for (int i = 0; i < enc.length; i++)
      addEncryptedData(enc[i]);

    Attachment[] att = rspProcDel.getAttachments();

    for (int i = 0; i < att.length; i++)
      addAttachment(att[i]);

    Author[] auth = rspProcDel.getOtherAuthors();

    for (int i = 0; i < auth.length; i++)
      otherAutors.put(auth[i].id, auth[i]);

    de.osci.osci12.roles.Reader[] read = rspProcDel.getOtherReaders();

    for (int i = 0; i < read.length; i++)
      otherReaders.put(read[i].id, read[i]);

    dialogHandler.getControlblock().setResponse(dialogHandler.prevChallenge);
    dialogHandler.getControlblock().setChallenge(Tools.createRandom(10));
  }

  /**
   * Diese Methode liefert den Laufzettel des Auftrags zurück oder null,
   * wenn bei der Verarbeitung der Nachricht ein Fehler aufgetreten ist.
   * Die Informationen im Laufzettel können auch direkt über die einzelnen
   * getX()-Methoden ausgewertet werden.
   *
   * @return den Laufzettel des Auftrags als ProcessCardBundle-Objekt, im Fehlerfall null
   * @see #getProcessCardBundleReply()
   * @see #getTimestampCreationRequest()
   * @see #getTimestampForwardingRequest()
   * @see #getInspectionsRequest()
   * @see #getSubjectRequest()
   * @see #getRecentModificationRequest()
   * @see #getRecentModificationRequest()
   */
  public ProcessCardBundle getProcessCardBundleRequest()
  {
    return processCardBundleRequest;
  }

  /**
   * Diese Methode liefert den Laufzettel der Antwort zurück oder null,
   * wenn bei der Verarbeitung der Nachricht ein Fehler aufgetreten ist.
   * Die Informationen im Laufzettel können auch direkt über die einzelnen
   * getX()-Methoden ausgewertet werden.
   *
   * @return den Laufzettel der Antwort als ProcessCardBundle-Objekt, im Fehlerfall null
   * @see #getProcessCardBundleRequest()
   * @see #getTimestampCreationReply()
   * @see #getTimestampForwardingReply()
   * @see #getInspectionsReply()
   * @see #getRecentModificationReply()
   * @see #getMessageIdRequest()
   * @see #getRecentModificationReply()
   */
  public ProcessCardBundle getProcessCardBundleReply()
  {
    return processCardBundleReply;
  }

  /**
   * Liefert den im Auftragslaufzettel enthaltenen Zeitstempel vom Zeitpunkt des Eingangs
   * des Abwicklungsauftrags beim Intermediär.
   * @return Timestamp Zeitstempel der Einreichung beim Intermediär
   * @see #getProcessCardBundleReply()
   * @see #getTimestampCreationReply()
   */
  public Timestamp getTimestampCreationRequest()
  {
    if (processCardBundleRequest == null)
      return null;

    return processCardBundleRequest.getCreation();
  }

  /**
   * Liefert den im Antwortlaufzettel enthaltenen Zeitstempel vom Zeitpunkt des Eingangs
   * der Bearbeitungsantwort beim Intermediär.
   * @return Zeitstempel der Einreichung beim Intermediär
   * @see #getProcessCardBundleReply()
   * @see #getTimestampCreationRequest()
   */
  public Timestamp getTimestampCreationReply()
  {
    if (processCardBundleReply == null)
      return null;

    return processCardBundleReply.getCreation();
  }

  /**
   * Liefert den im Laufzettel enthaltenen Zeitstempel vom Zeitpunkt des
   * vollständigen Aufbaus des Bearbeitungsauftrags vom Intermediär für den Empfänger.
   * @return Zeitstempel der Erstellung des Bearbeitungsauftrags durch den Intermediär
   * @see #getProcessCardBundleRequest()
   * @see #getTimestampForwardingReply()
   */
  public Timestamp getTimestampForwardingRequest()
  {
    if (processCardBundleRequest == null)
      return null;

    return processCardBundleRequest.getForwarding();
  }

  /**
   * Liefert den im Laufzettel enthaltenen Zeitstempel vom Zeitpunkt des
   * vollständigen Aufbaus der Abwicklungsantwort vom Intermediär für den Sender.
   * @return Zeitstempel der Erstellung der Abwicklungsantwort durch den Intermediär
   * @see #getProcessCardBundleReply()
   * @see #getTimestampForwardingRequest()
   */
  public Timestamp getTimestampForwardingReply()
  {
    if (processCardBundleReply == null)
      return null;

    return processCardBundleReply.getForwarding();
  }

  /**
   * Liefert den im Auftragslaufzettel enthaltenen Zeitstempel vom Zeitpunkt des
   * Eingangs einer positiven Bearbeitungsantwort vom Empfänger
   * beim Intermediär.
   * @return Zeitstempel der Registrierung einer Empfangsbestätigung (Bearbeitungsantwort)
   * durch den Intermediär
   * @see #getProcessCardBundleRequest()
   * @see #getTimestampForwardingRequest()
   */
  public Timestamp getTimestampReceptionRequest()
  {
    if (processCardBundleRequest == null)
      return null;

    return processCardBundleRequest.getReception();
  }

  /**
   * Liefert die Ergebnisse der Zertifikatsprüfungen des Abwicklungsauftrags in
   * Form von Inspection-Objekten, die im Laufzettel des Auftrags enthalten sind.
   * @return inspections die Prüfergebnisse
   * @see #getProcessCardBundleRequest()
   * @see #getInspectionsReply()
   */
  public Inspection[] getInspectionsRequest()
  {
    if (processCardBundleRequest == null)
      return null;

    return processCardBundleRequest.getInspections();
  }

  /**
   * Liefert die Ergebnisse der Zertifikatsprüfungen des Abwicklungsauftrags in
   * Form von Inspection-Objekten, die im Laufzettel der Antwort enthalten sind.
   * @return inspections die Prüfergebnisse
   * @see #getProcessCardBundleReply()
   * @see #getInspectionsRequest()
   */
  public Inspection[] getInspectionsReply()
  {
    if (processCardBundleReply == null)
      return null;

    return processCardBundleReply.getInspections();
  }

  /**
   * Liefert den im Auftragslaufzettel enthaltenen Betreff-Eintrag.
   * @return den Betreff des Auftragsnachricht
   * @see #getProcessCardBundleRequest()
   */
  public String getSubjectRequest()
  {
    if (processCardBundleRequest == null)
      return null;

    return processCardBundleRequest.getSubject();
  }

  /**
   * Liefert den im Antwortlaufzettel enthaltenen Betreff-Eintrag.
   * @return den Betreff der Antwortnachricht
   * @see #getProcessCardBundleRequest()
   * @see #getSubjectRequest()
   * @since 1.0.3
   */
  public String getSubject()
  {
    if (processCardBundleReply == null)
      return null;

    return processCardBundleReply.getSubject();
  }

  /**
   * Liefert das Datum der letzten Änderung des Auftragslaufzettels. Das Format
   * entspricht dem XML-Schema nach <a href="http://www.w3.org/TR/xmlschema-2/#dateTime">
   * http://www.w3.org/TR/xmlschema-2/#dateTime</a>.
   * @return Datum der letzten Änderung
   * @see #getProcessCardBundleRequest()
   * @see #getRecentModificationReply()
   */
  public String getRecentModificationRequest()
  {
    if (processCardBundleRequest == null)
      return null;

    return processCardBundleRequest.getRecentModification();
  }

  /**
   * Liefert das Datum der letzten Änderung des Antwortlaufzettels. Das Format
   * entspricht dem XML-Schema nach <a href="http://www.w3.org/TR/xmlschema-2/#dateTime">
   * http://www.w3.org/TR/xmlschema-2/#dateTime</a>.
   * @return Datum der letzten Änderung
   * @see #getProcessCardBundleRequest()
   * @see #getRecentModificationRequest()
   */
  public String getRecentModificationReply()
  {
    if (processCardBundleReply == null)
      return null;

    return processCardBundleReply.getRecentModification();
  }

  /**
   * Liefert die Message-ID der Nachricht (Auftrag).
   * @return Message-ID
   */
  public String getMessageIdRequest()
  {
    return messageId;
  }

  /**
   * Liefert die Message-ID der Nachricht (Antwort).
   * @return Message-ID
   * @since 1.0.3
   */
  public String getMessageId()
  {
    if (processCardBundleReply == null)
      return null;

    return processCardBundleReply.getMessageId();
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

  void sign()
     throws IOException,
            OSCIException,
            de.osci.osci12.common.OSCICancelledException,
            java.security.NoSuchAlgorithmException
  {
    super.sign();
    // Der IntemedCertHeader liegt hier weiter unten
    messageParts.set(4, intermediaryCertificatesH);
    messageParts.set(2, null);
  }

  /**
   * undocumented
   *
   * @throws OSCIException undocumented
   * @throws IOException undocumented
   */
  protected void compose() throws OSCIException,
                                  IOException,
                                  NoSuchAlgorithmException
  {
    super.compose();
    messageParts.set(2, null);
    importAllCertificates();

    String head = writeFeedBack();

    if (processCardBundleRequest != null)
    {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      MessagePartsFactory.writeXML(processCardBundleRequest, out);

      if (processCardBundleReply != null)
      {
        MessagePartsFactory.writeXML(processCardBundleReply, out);
      }

      head += out.toString(Constants.CHAR_ENCODING);
    }

    osciH = new OsciH(HeaderTags.responseToMediateDelivery.getElementName(), head);
    messageParts.add(osciH);
    messageParts.add(intermediaryCertificatesH);
    createNonIntermediaryCertificatesH();
    messageParts.add(nonIntermediaryCertificatesH);
    if(featureDescription!=null && dialogHandler.isSendFeatureDescription())
    {
      messageParts.add(featureDescription);
    }
    messageParts.addAll(customHeaders);
    body = new Body(getContentContainer(), getEncryptedData());
    messageParts.add(body);
    stateOfMsg |= STATE_COMPOSED;
  }
}
