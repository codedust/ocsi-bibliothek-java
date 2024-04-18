package de.osci.osci12.messagetypes;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;

import de.osci.helper.Tools;
import de.osci.osci12.OSCIException;
import de.osci.osci12.common.Constants;
import de.osci.osci12.common.Constants.HeaderTags;
import de.osci.osci12.common.Constants.LanguageTextEntries;
import de.osci.osci12.common.DialogHandler;
import de.osci.osci12.common.OSCICancelledException;
import de.osci.osci12.messageparts.Body;
import de.osci.osci12.messageparts.Content;
import de.osci.osci12.messageparts.ContentContainer;
import de.osci.osci12.messageparts.EncryptedDataOSCI;
import de.osci.osci12.roles.Addressee;
import de.osci.osci12.roles.OSCIRoleException;
import de.osci.osci12.roles.Originator;
import de.osci.osci12.soapheader.OsciH;
import de.osci.osci12.soapheader.QualityOfTimestampH;
import eu.osci.ws._2014._10.transport.MessageMetaData;


/**
 * <p>Mit dieser Klasse werden Nachrichtenobjekte für Weiterleitungsaufträge
 * angelegt. Clients erhalten als Antwort auf diese Nachricht vom Intermediär
 * ein ResponseToForwardDelivery-Nachrichtenobjekt, welches eine Rückmeldung über den
 * Erfolg der Operation (getFeedback()) und ggf. den Laufzettel zur gesendeten
 * Nachricht enthält.</p>
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
 *
 * @see de.osci.osci12.messagetypes.ResponseToForwardDelivery
 */
public class ForwardDelivery extends OSCIRequest implements ContentPackageI
{

  private String subject;

  /**
   * Legt ein Nachrichtenobjekt für einen Weiterleitungsauftrag an.
   *
   * @param dh DialogHandler-Objekt des Dialogs, innerhalb dessen die Nachricht
   * versendet werden soll
   * @param addressee Rollenobjekt des Empfängers
   * @param uriReceiver URI des adressierten Empfängers
   * @param messageId Message-ID der Nachricht
   * @throws URISyntaxException bei ungültiger URI
   * @throws NoSuchAlgorithmException wenn der verwendete Security-Provider
   * einen erforderlichen Algorithmus nicht unterstützt (Erzeugung einer Zufallszahl)
   * @see de.osci.osci12.common.DialogHandler
   */
  public ForwardDelivery(DialogHandler dh, Addressee addressee, String uriReceiver, String messageId)
                  throws URISyntaxException,
                         NoSuchAlgorithmException
  {
    super(dh);

    if (!dialogHandler.isExplicitDialog())
      dialogHandler.resetControlBlock();

    dialogHandler.getControlblock().setResponse(dialogHandler.prevChallenge);
    dialogHandler.getControlblock().setChallenge(Tools.createRandom(10));
    dialogHandler.getControlblock().setSequenceNumber(dialogHandler.getControlblock().getSequenceNumber() + 1);
    messageType = FORWARD_DELIVERY;
    originator = ((Originator) dh.getClient());
    this.addressee = addressee;
    this.uriReceiver = new java.net.URI(uriReceiver);
    this.messageId = messageId;
    setQualityOfTimeStampCreation(false);
    setQualityOfTimeStampReception(false);
  }

  /**
   * Legt ein Nachrichtenobjekt für einen Weiterleitungsauftrag an.
   * Dieser Konstruktor wird nur von dem Parser benutzt.
   */
  ForwardDelivery(DialogHandler dh)
  {
    super();
    messageType = FORWARD_DELIVERY;
    this.dialogHandler = dh;
  }

  /**
   * Versendet die Nachricht und liefert die Antwortnachricht zurück.
   * Diese Methode wirft eine Exception, wenn beim Aufbau oder Versand der
   * Nachricht ein Fehler auftritt. Fehlermeldungen vom Intermediär müssen
   * dem Feedback der Antwortnachricht entnommen werden.
   *
   * @throws IOException bei Ein-/Ausgabefehlern
   * @throws OSCIException bei OSCI-Fehlern
   * @throws NoSuchAlgorithmException wenn der verwendete Security-Provider
   * einen erforderlichen Algorithmus nicht unterstützt (Erzeugung einer Zufallszahl)
   * @return Antwortnachricht-Objekt
   * @see #send(OutputStream, OutputStream)
   */
  public ResponseToForwardDelivery send() throws IOException,
                                                 OSCIException,
                                                 NoSuchAlgorithmException
  {
    return (ResponseToForwardDelivery) transmit(null, null);
  }

  /**
   * Versendet die Nachricht und liefert die Antwortnachricht zurück.
   * Die aus- und eingehenden Daten werden zusätzlich in die übergebenen
   * Streams geschrieben (unverschlüsselte Auftragsdaten). Diese Parameter
   * dürfen null sein.
   *
   * @param storeOutput Stream, in den die versendete Nachricht geschrieben wird
   * @param storeInput Stream, in den die empfangene Antwortnachricht geschrieben wird
   * @throws IOException bei Ein-/Ausgabefehlern
   * @throws OSCIException bei OSCI-Fehlern
   * @throws NoSuchAlgorithmException wenn der verwendete Security-Provider
   * einen erforderlichen Algorithmus nicht unterstützt (Erzeugung einer Zufallszahl)
   * @return Antwortnachricht-Objekt
   * @see #send()
   */
  public ResponseToForwardDelivery send(OutputStream storeOutput, OutputStream storeInput)
                                 throws IOException,
                                        OSCIException,
                                        NoSuchAlgorithmException
  {
    return (ResponseToForwardDelivery) transmit(storeOutput, storeInput);
  }

  /**
   * Setzt die gewünschte Qualität des Zeitstempels, mit dem der Intermediär
   * den Eingang des Auftrags im Laufzettel protokolliert.
   *
   * @param cryptographic <b>true</b>: kryptographischer Zeitstempel von einem
   * akkreditierten Zeitstempeldienst<br><b>false</b>: Einfacher Zeitstempel
   * (lokale Rechnerzeit des Intermediärs, default)
   * @see #getQualityOfTimeStampCreation()
   */
  public void setQualityOfTimeStampCreation(boolean cryptographic)
  {
    qualityOfTimestampTypeCreation = new QualityOfTimestampH(false, cryptographic);
  }

  /**
   * Liefert die Qualität des Zeitstempels, mit dem der Intermediär den
   * Eingang des Auftrags im Laufzettel protokolliert.
   * @return Qualität des Zeitstempels: <b>true</b> - kryptographischer Zeitstempel von einem
   * akkreditierten Zeitstempeldienst<br><b>false</b> - Einfacher Zeitstempel
   * (lokale Rechnerzeit des Intermediärs)
   * @see #setQualityOfTimeStampCreation(boolean)
   */
  public boolean getQualityOfTimeStampCreation()
  {
    return qualityOfTimestampTypeCreation.isQualityCryptographic();
  }

  /**
   * Setzt die gewünschte Qualität des Zeitstempels, mit dem der Intermediär den
   * Empfang der Annahmeantwort im Laufzettel protokolliert.
   * @param cryptographic <b>true</b>: kryptographischer Zeitstempel von einem
   * akkreditierten Zeitstempeldienst<br><b>false</b>: Einfacher Zeitstempel
   * (lokale Rechnerzeit des Intermediärs, default)
   * @see #getQualityOfTimeStampReception()
   */
  public void setQualityOfTimeStampReception(boolean cryptographic)
  {
    qualityOfTimestampTypeReception = new QualityOfTimestampH(true, cryptographic);
  }

  /**
   * Liefert die geforderte Qualität des Zeitstempels, mit dem der Intermediär den
   * Empfang der Annahmeantwort im Laufzettel protokolliert.
   * @return Qualität des Zeitstempels: <b>true</b> - kryptographischer Zeitstempel von einem
   * akkreditierten Zeitstempeldienst<br><b>false</b> - Einfacher Zeitstempel
   * (lokale Rechnerzeit des Intermediärs)
   * @see #setQualityOfTimeStampReception(boolean)
   */
  public boolean getQualityOfTimeStampReception()
  {
    return qualityOfTimestampTypeReception.isQualityCryptographic();
  }

  /**
   * Liefert die Adresse des Empfängers der Inhaltsdaten.
   * @return Adresse (URI)
   */
  public String getContentReceiver()
  {
    return uriReceiver.toString();
  }

  /**
   * Setzt die Adresse des Empfängers der Inhaltsdaten.
   * @param uri Adresse
   * @throws java.net.URISyntaxException bei Syntaxfehlern
   */
  public void setContentReceiver(String uri) throws java.net.URISyntaxException
  {
    this.uriReceiver = new java.net.URI(uri);
  }

  /**
   * Liefert den Betreff der Nachricht.
   * @return den Betreff der Zustellung
   */
  public String getSubject()
  {
    return subject;
  }

  /**
   *  Setzt den Betreff der Nachricht. Der übergebene Parameter-String
   * muß den Vorschriften für den primitiven XML-Datentyp "string" entsprechen,
   * darf also z.B. keine XML-Steuerzeichen (&lt;, &gt; o.&auml;.) enthalten.
   *@param  subject Betreff
   */
  public void setSubject(String subject)
  {
    this.subject = subject;
  }

  /**
   * Fügt der Nachricht einen Inhaltsdatencontainer hinzu. Diese Methode
   * sollte erst aufgerufen werden, wenn der Container vollständig erstellt wurde.
   * <b>Hinweis: </b>ContentContainer-Objekte mit Attachments, die durch Entschlüsselung
   * von EncryptedDataOSCI-Objekten gewonnen wurden, können hier nicht ohne weiteres
   * hinzugefügt werden. In diesem Fall müssen die Attachments zunächst ausgelesen und
   * die Daten in neu angelegten Attachment-Objekten wieder hinzugefügt werden.
   * @param container Inhaltsdatencontainer
   * @see de.osci.osci12.messageparts.ContentContainer
   */
  public void addContentContainer(ContentContainer container)
                           throws OSCIRoleException
  {
    super.addContentContainer(container);
  }

  /**
   * Entfernt einen Inhaltsdatencontainer aus der Nachricht.
   * @param container Inhaltsdatencontainer
   * @see #addContentContainer
   */
  public void removeContentContainer(ContentContainer container)
  {
    super.removeContentContainer(container);
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
   * Fügt der Nachricht einen EncryptedData-Eintrag mit verschlüsselten
   * Inhaltsdaten hinzu.
   * @param encData verschlüsselte Daten
   * @see EncryptedDataOSCI
   */
  public void addEncryptedData(EncryptedDataOSCI encData)
                        throws OSCIRoleException
  {
    super.addEncryptedData(encData);
  }

  /**
   * Entfernt einen EncryptedData-Eintrag mit verschlüsselten Daten
   * aus der Nachricht.
   * @param encData verschlüsselte Daten
   * @see #addEncryptedData
   * @see EncryptedDataOSCI
   */
  public void removeEncryptedData(EncryptedDataOSCI encData)
  {
    super.removeEncryptedData(encData);
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

  /**
   * undocumented
   *
   * @return undocumented
   */
  public String getMessageId()
  {
    return messageId;
  }

  void sign()
     throws IOException,
            OSCIException,
            OSCICancelledException,
            NoSuchAlgorithmException
  {
    super.sign();
    // Der NonIntemedCertHeader liegt hier weiter unten
    messageParts.set(3, null);
    messageParts.set(7, nonIntermediaryCertificatesH);
  }

  /**
   * undocumented
   *
   * @throws OSCIRoleException undocumented
   * @throws IOException undocumented
   * @throws IllegalStateException undocumented
   */
  protected void compose() throws OSCIException,
                                  IOException,
                                  NoSuchAlgorithmException
  {
    super.compose();
    messageParts.set(3, null);
    messageParts.add(qualityOfTimestampTypeCreation);
    messageParts.add(qualityOfTimestampTypeReception);

    if (dialogHandler.getControlblock().getChallenge() == null)
      throw new IllegalStateException(DialogHandler.text.getString(LanguageTextEntries.missing_entry.name()) + ": Challenge");

    if (dialogHandler.getControlblock().getSequenceNumber() == -1)
      throw new IllegalStateException(DialogHandler.text.getString(LanguageTextEntries.missing_entry.name()) + ": SequenceNumber");

    importAllCertificates();

    if (nonIntermediaryCertificatesH == null)
      createNonIntermediaryCertificatesH();

    if (uriReceiver == null)
      throw new IllegalStateException(DialogHandler.text.getString(LanguageTextEntries.missing_entry.name()) + ": URI - contentReceiver");

    if (messageId == null)
      throw new IllegalStateException(DialogHandler.text.getString(LanguageTextEntries.missing_entry.name()) + ": Message-Id");

    String head = "<" + osciNSPrefix + ":ContentReceiver URI=\"" + uriReceiver + "\"></" + osciNSPrefix +
                  ":ContentReceiver><" + osciNSPrefix + ":MessageId>" +
                  de.osci.helper.Base64.encode(messageId.getBytes(Constants.CHAR_ENCODING)) + "</" + osciNSPrefix +
                  ":MessageId>";

    if (subject != null)
      head += ("<" + osciNSPrefix + ":Subject>" + subject + "</" + osciNSPrefix + ":Subject>");

    osciH = new OsciH(HeaderTags.forwardDelivery.getElementName(), head);
    messageParts.add(osciH);
    messageParts.add(nonIntermediaryCertificatesH);
    if(featureDescription!=null && dialogHandler.isSendFeatureDescription())
    {
      messageParts.add(featureDescription);
    }
    messageParts.addAll(customHeaders);
    body = new Body(getContentContainer(), getEncryptedData());
    //    body.setNSPrefixes(this);
    messageParts.add(body);
    stateOfMsg |= STATE_COMPOSED;
  }
}
