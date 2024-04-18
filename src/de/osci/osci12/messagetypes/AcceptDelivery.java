package de.osci.osci12.messagetypes;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import de.osci.helper.Tools;
import de.osci.osci12.OSCIException;
import de.osci.osci12.common.Constants.HeaderTags;
import de.osci.osci12.common.Constants.LanguageTextEntries;
import de.osci.osci12.common.DialogHandler;
import de.osci.osci12.messageparts.Attachment;
import de.osci.osci12.messageparts.Body;
import de.osci.osci12.messageparts.Content;
import de.osci.osci12.messageparts.ContentContainer;
import de.osci.osci12.messageparts.EncryptedDataOSCI;
import de.osci.osci12.messageparts.Inspection;
import de.osci.osci12.messageparts.ProcessCardBundle;
import de.osci.osci12.messageparts.Timestamp;
import de.osci.osci12.roles.Addressee;
import de.osci.osci12.roles.Intermed;
import de.osci.osci12.roles.OSCIRoleException;
import de.osci.osci12.soapheader.DesiredLanguagesH;
import de.osci.osci12.soapheader.IntermediaryCertificatesH;
import de.osci.osci12.soapheader.OsciH;
import eu.osci.ws._2014._10.transport.MessageMetaData;


/**
 * <p>
 * Diese Klasse repräsentiert Nachrichtenobjekte für Annahmeaufträge. Der Intermediär erzeugt nach dem Erhalt
 * eines Weiterleitungsauftrags eine Instanz dieser Klasse und sendet die Nachricht an den Empfänger (hier als
 * Supplier) . Als Antwort auf diese Nachricht muss der Empfänger ein
 * ResponseToAcceptDelivery-Nachrichtenobjekt mit einer Rückmeldung (Feedback) aufbauen und an den Intermediär
 * zurücksenden.
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
 * @author R. Lindemann, N. Büngener
 * @version 2.4.1
 * @see de.osci.osci12.messagetypes.ResponseToAcceptDelivery
 */
public class AcceptDelivery extends OSCIRequest implements ContentPackageI
{

  // private static Log log = LogFactory.getLog(AcceptDelivery.class);
  public ProcessCardBundle processCardBundle = null;

  public DialogHandler fwdDH;

  /**
   * Konstruktor für den Supplier Parser
   *
   * @param dh
   */
  AcceptDelivery(DialogHandler dh)
  {
    super(dh);
    messageType = ACCEPT_DELIVERY;
    addressee = ((Addressee)dh.getSupplier());
  }

  AcceptDelivery()
  {
    messageType = ACCEPT_DELIVERY;
  }

  /**
   * Konstruktor für den Intermediär
   *
   * @param fd
   */
  AcceptDelivery(ForwardDelivery fd) throws OSCIRoleException, NoSuchAlgorithmException
  {
    super(new DialogHandler((Intermed)fd.dialogHandler.getSupplier(), fd.addressee, null));
    messageType = ACCEPT_DELIVERY;
    uriReceiver = fd.uriReceiver;
    addressee = fd.addressee;
    originator = fd.originator;
    desiredLanguagesH = new DesiredLanguagesH(fd.desiredLanguagesH.getLanguageList());

    ContentContainer[] con = fd.getContentContainer();

    for ( int i = 0 ; i < con.length ; i++ )
      addContentContainer(con[i]);

    EncryptedDataOSCI[] enc = fd.getEncryptedData();

    for ( int i = 0 ; i < enc.length ; i++ )
      addEncryptedData(enc[i]);

    Attachment[] att = fd.getAttachments();

    for ( int i = 0 ; i < att.length ; i++ )
      addAttachment(att[i]);

    for ( int i = 0 ; i < fd.getOtherAuthors().length ; i++ )
      otherAutors.put(fd.getOtherAuthors()[i].id, fd.getOtherAuthors()[i]);

    for ( int i = 0 ; i < fd.getOtherReaders().length ; i++ )
      otherReaders.put(fd.getOtherReaders()[i].id, fd.getOtherReaders()[i]);

    dialogHandler.setEncryption(fd.getDialogHandler().isEncryption());
    dialogHandler.getControlblock().setResponse(null);
    dialogHandler.getControlblock().setChallenge(Tools.createRandom(10));
    dialogHandler.getControlblock().setConversationID(null);
    dialogHandler.getControlblock().setSequenceNumber(-1);
    fwdDH = fd.getDialogHandler();
  }

  /**
   * Versendet die Nachricht und liefert die Antwortnachricht zurück. Diese Methode wirft eine Exception, wenn
   * beim Aufbau oder Versand der Nachricht ein Fehler auftritt. Fehlermeldungen vom Intermediär müssen dem
   * Feedback der Antwortnachricht entnommen werden.
   *
   * @throws IOException bei Ein-/Ausgabefehlern
   * @throws de.osci.osci12.OSCIException bei OSCI-Fehlern
   * @return Antwortnachricht-Objekt
   */
  ResponseToAcceptDelivery send() throws IOException, de.osci.osci12.OSCIException, NoSuchAlgorithmException
  {
    return (ResponseToAcceptDelivery)transmit(null, null);
  }

  /**
   * Diese Methode liefert den Laufzettel der Zustellung zurück oder null, wenn bei der Verarbeitung der
   * Nachricht ein Fehler aufgetreten ist. Die Informationen im Laufzettel können auch direkt über die
   * einzelnen getX()-Methoden ausgewertet werden.
   *
   * @return den Laufzettel als ProcessCardBundle-Objekt, im Fehlerfall null
   * @see #getTimestampCreation()
   * @see #getTimestampForwarding()
   * @see #getInspections()
   * @see #getSubject()
   * @see #getRecentModification()
   * @see #getMessageId()
   */
  public ProcessCardBundle getProcessCardBundle()
  {
    return processCardBundle;
  }

  /**
   * Liefert den im Laufzettel enthaltenen Zeitstempel vom Zeitpunkt des Eingangs des Weiterleitungsauftrags
   * beim Intermediär.
   *
   * @return Zeitstempel der Einreichung beim Intermediär
   * @see #getProcessCardBundle()
   */
  public Timestamp getTimestampCreation()
  {
    return processCardBundle.getCreation();
  }

  /**
   * Liefert den im Laufzettel enthaltenen Zeitstempel vom Zeitpunkt des vollständigen Aufbaus des
   * Annahmeauftrags vom Intermediär für den Empfänger.
   *
   * @return Zeitstempel der Erstellung des Annahmeauftrags durch den Intermediär
   * @see #getProcessCardBundle()
   */
  public Timestamp getTimestampForwarding()
  {
    return processCardBundle.getForwarding();
  }

  /**
   * Liefert die Ergebnisse der Zertifikatsprüfungen in Form von Inspection-Objekten, die im
   * ProcessCardBundle-Objekt enthalten sind.
   *
   * @return inspections Prüfergebnisse
   * @see #getProcessCardBundle()
   */
  public Inspection[] getInspections()
  {
    return processCardBundle.getInspections();
  }

  /**
   * Liefert den Betreff der Nachricht.
   *
   * @return den Betreff der Zustellung
   */
  public String getSubject()
  {
    return processCardBundle.getSubject();
  }

  /**
   * Liefert das Datum der letzten Änderung des Laufzettels. Das Format entspricht dem XML-Schema nach
   * http://www.w3.org/TR/xmlschema-2/#dateTime
   *
   * @return Datum der letzten Änderung
   * @see #getProcessCardBundle()
   */
  public String getRecentModification()
  {
    return processCardBundle.getRecentModification();
  }

  /**
   * Liefert die in die Nachricht eingestellten (unverschlüsselten) Inhaltsdaten als ContentContainer-Objekte.
   *
   * @return enthaltene ContentContainer mit Inhaltsdaten
   * @see de.osci.osci12.messageparts.ContentContainer
   */
  public ContentContainer[] getContentContainer()
  {
    return super.getContentContainer();
  }

  /**
   * Durchsucht <b>die unverschlüsselten</b> Inhaltsdaten nach dem ContentContainer mit der übergebenen RefID.
   *
   * @param refID zu suchende RefID
   * @return den zugehörigen ContentContainer oder null, wenn die Referenz nicht gefunden wurde.
   */
  public ContentContainer getContentContainerByRefID(String refID)
  {
    return super.getContentContainerByRefID(refID);
  }

  /**
   * Durchsucht <b>die unverschlüsselten</b> ContentContainer nach dem Content mit der übergebenen RefID.
   *
   * @param refID zu suchende RefID
   * @return den zugehörigen Content oder null, wenn die Referenz nicht gefunden wurde.
   */
  public Content getContentByRefID(String refID)
  {
    return super.getContentByRefID(refID);
  }

  /**
   * Liefert die in die Nachricht eingestellten verschlüsselten Inhaltsdaten als EncryptedData-Objekte.
   *
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

  /**
   * Liefert die Message-ID der Nachricht.
   *
   * @return Message-ID
   */
  public String getMessageId()
  {
    return processCardBundle.getMessageId();
  }

  void setProcessCardBundle(ProcessCardBundle processCardBundle)
  {
    this.processCardBundle = processCardBundle;
  }

  void sign() throws IOException, OSCIException, de.osci.osci12.common.OSCICancelledException,
    java.security.NoSuchAlgorithmException
  {
    super.sign();
    // Der IntemedCertHeader liegt hier weiter unten
    messageParts.set(5, intermediaryCertificatesH);
    messageParts.set(3, null);
  }

  /**
   * undocumented
   *
   * @throws IOException undocumented
   * @throws OSCIRoleException undocumented
   * @throws IllegalStateException undocumented
   */
  protected void compose() throws IOException, OSCIException, NoSuchAlgorithmException
  {
    super.compose();
    messageParts.set(3, null);

    if (processCardBundle == null)
      throw new IllegalStateException(DialogHandler.text.getString(LanguageTextEntries.missing_entry.name()) + ": ProcessCardBundle");

    osciH = new OsciH(HeaderTags.acceptDelivery.getElementName(), processCardBundle.writeToString());
    messageParts.add(osciH);

    if (dialogHandler.getClient().hasCipherCertificate())
    {
      if (intermediaryCertificatesH == null)
        intermediaryCertificatesH = new IntermediaryCertificatesH();

      intermediaryCertificatesH.setCipherCertificateIntermediary((Intermed)dialogHandler.getClient());
    }

    messageParts.add(intermediaryCertificatesH);
    createNonIntermediaryCertificatesH();
    messageParts.add(nonIntermediaryCertificatesH);
    if(featureDescription!=null && dialogHandler.isSendFeatureDescription())
    {
      messageParts.add(featureDescription);
    }
    messageParts.addAll(customHeaders);

    if (body == null)
      body = new Body(getContentContainer(), getEncryptedData());

    messageParts.add(body);
    stateOfMsg |= STATE_COMPOSED;
  }
}
