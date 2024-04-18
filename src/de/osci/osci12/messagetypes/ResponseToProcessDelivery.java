package de.osci.osci12.messagetypes;

import java.io.IOException;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Vector;

import de.osci.helper.Base64;
import de.osci.helper.StoreOutputStream;
import de.osci.osci12.OSCIException;
import de.osci.osci12.common.Constants;
import de.osci.osci12.common.Constants.HeaderTags;
import de.osci.osci12.common.DialogHandler;
import de.osci.osci12.common.OSCICancelledException;
import de.osci.osci12.extinterfaces.crypto.Decrypter;
import de.osci.osci12.extinterfaces.crypto.Signer;
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
 * <p>Mit dieser Klasse werden Nachrichtenobjekte für Bearbeitungsantworten
 * angelegt. Ein passiver Client, der als Supplier fungiert, muss nach Empfang
 * eines Bearbeitungsauftrags eine Instanz dieser Klasse aufbauen und an den Intermediär
 * zurücksenden. Die Nachricht enthält eine Rückmeldung über
 * den Empfang der Nachricht (Feedback) sowie ggf. verschlüsselte bzw.
 * unverschlüsselte Inhaltsdaten.</p>
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
 * @see de.osci.osci12.messagetypes.ProcessDelivery
 */
public class ResponseToProcessDelivery extends OSCIResponseTo implements ContentPackageI
{
  //  private static Log log = LogFactory.getLog(ResponseToProcessDelivery.class);
  private String subject;

  ResponseToProcessDelivery(DialogHandler dh)
  {
    super(dh);
    messageType = RESPONSE_TO_PROCESS_DELIVERY;
  }

  /**
   * Legt ein Nachrichtenobjekt für eine Bearbeitungsantwort an.
   *
   * @see de.osci.osci12.common.DialogHandler
   * @param procDel {@link ProcessDelivery}
   */
  public ResponseToProcessDelivery(ProcessDelivery procDel)
  {
    super(procDel.dialogHandler);
    messageType = RESPONSE_TO_PROCESS_DELIVERY;

    // Nun wirds interessant: Weil in diesem Szenario die Rollen wechseln (der Orginator der Anfrage wird zum Addressee der Antwort
    // und umgekehrt) werden die Rollenobjekte umgebaut. Damit passen die Rollenobjekte des DialogHandlers nicht mehr so richtig,
    // aber in einem impliziten Dialog kann man das wohl riskieren.
    Signer signer = null;
    Decrypter decrypter = null;

    try
    {
      if (dialogHandler.getSupplier().hasSignaturePrivateKey())
        signer = dialogHandler.getSupplier().getSigner();

      if (dialogHandler.getSupplier().hasCipherPrivateKey())
        decrypter = dialogHandler.getSupplier().getDecrypter();

      originator = new Originator(signer, decrypter);

      X509Certificate signerCert = null;
      X509Certificate cipherCert = null;

      if (procDel.getOriginator().hasSignatureCertificate())
        signerCert = procDel.getOriginator().getSignatureCertificate();

      if (procDel.getOriginator().hasCipherCertificate())
        cipherCert = procDel.getOriginator().getCipherCertificate();

      addressee = new Addressee(signerCert, cipherCert);
      dialogHandler.getControlblock().setResponse(dialogHandler.prevChallenge);
      dialogHandler.getControlblock().setChallenge(null);
      dialogHandler.getControlblock().setConversationID(null);
      dialogHandler.getControlblock().setSequenceNumber(-1);
      setQualityOfTimeStampCreation(false);
      setQualityOfTimeStampReception(false);
    }
    catch (OSCIRoleException ex)
    {
      /* Nicht erforderlich */
    }
  }

  /**
   * Liefert den im Laufzettel enthaltenen Betreff-Eintrag.
   * @return den Betreff der Zustellung
   */
  public String getSubject()
  {
    return subject;
  }

  /**
   * Setzt den Betreff-Eintrag der Nachricht. Der übergebene Parameter-String
   * muß den Vorschriften für den primitiven XML-Datentyp "string" entsprechen,
   * darf also z.B. keine XML-Steuerzeichen (&lt;, &gt; o.&auml;.) enthalten.
   * @param  subject  der Betreff
   */
  public void setSubject(String subject)
  {
    this.subject = subject;
  }

  /**
   * Setzt die Rückmeldungen (Fehler und Warnungen) auf Auftragsebene.
   * @param code Array mit Fehlercodes
   */
  public void setFeedback(String[] code)
  {
    super.setFeedback(code);
  }

  /**
   * Setzt die Rückmeldungen (Feedback-Einträge) mit frei wählbaren Texten und Sprachen.
   * Der erste Index des Arrays entspricht dem Index des Entry-Elementes.
   * Beim zweiten Index bezeichnet <br>
   * @param entries,
   * 0 - das Sprachkürzel (z.B. "de", "en-US")<br>
   * 1 - den Code<br>
   * 2 - den Text<br>
   */
  public void setFeedback(String[][] entries)
  {
    feedBack = new Vector<String[]>();
    for (int i = 0; i < entries.length; i++)
      feedBack.add(entries[i]);
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
   * Setzt die gewünschte Qualität des Zeitstempels, mit dem der Intermediär die
   * Empfangsbestätigung der Zustellung durch den Empfänger im Laufzettel protokolliert.
   * Die Empfangsbestätigung besteht in einem weiteren Auftrag, den der Empfänger
   * nach Erhalt der Bearbeitungsantwort innerhalb desselben expliziten Dialogs
   * an den Intermediär schickt.
   * @param cryptographic <b>true</b>: kryptographischer Zeitstempel von einem
   * akkreditierten Zeitstempeldienst<br><b>false</b>: Einfacher Zeitstempel
   * (lokale Rechnerzeit des Intermediärs)
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
   * (lokale Rechnerzeit des Intermediärs, default)
   * @see #setQualityOfTimeStampReception(boolean)
   */
  public boolean getQualityOfTimeStampReception()
  {
    return qualityOfTimestampTypeReception.isQualityCryptographic();
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
   * Liefert ein Array mit den Inhaltsdatencontainern der Nachricht.
   * @see #addContentContainer
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
   * Liefert die Message-ID der Nachricht.
   * @return Message-ID
   */
  public String getMessageId()
  {
    return messageId;
  }

  /**
   * Setzt die Message-ID der Nachricht.
   * @param messageId Message Id
   */
  public void setMessageId(String messageId)
  {
    this.messageId = messageId;
  }

  /**
   * Bringt eine Supplier-Signatur an.
   * @throws IOException bei Schreib-/Leseproblemen
   * @throws OSCIRoleException wenn dem Rollenobjekt, das als Client fungiert,
   * kein Signer-Objekt zugeordnet wurde
   * @throws OSCICancelledException bei Abbruch durch den
   * @throws NoSuchAlgorithmException
   * Benutzer
   */
  public void sign()
            throws IOException,
                   OSCIException,
                   OSCICancelledException,
                   NoSuchAlgorithmException
  {
    super.sign(originator);
    //    messageParts.set(1, signatureHeader);
    messageParts.set(2, null);
    messageParts.set(6, nonIntermediaryCertificatesH);
  }

  /**
   * undocumented
   *
   * @throws OSCIRoleException undocumented
   * @throws IOException undocumented
   * @throws NoSuchAlgorithmException
   */
  protected void compose() throws OSCIException,
                                  IOException,
                                  NoSuchAlgorithmException
  {
    super.compose();
    messageParts.set(2, null);

    String head = writeFeedBack();

    if (messageId != null)
    {
      head += ("<" + osciNSPrefix + ":MessageId>" +
      Base64.encode(messageId.getBytes(Constants.CHAR_ENCODING)) + "</" + osciNSPrefix + ":MessageId>");

      if (subject != null)
        head += ("<" + osciNSPrefix + ":Subject>" + subject + "</" + osciNSPrefix + ":Subject>");
    }
    else
    {
      qualityOfTimestampTypeCreation = null;
      qualityOfTimestampTypeReception = null;
    }

    messageParts.add(qualityOfTimestampTypeCreation);
    messageParts.add(qualityOfTimestampTypeReception);
    osciH = new OsciH(HeaderTags.responseToProcessDelivery.getElementName(), head);
    messageParts.add(osciH);
    createNonIntermediaryCertificatesH();
    messageParts.add(nonIntermediaryCertificatesH);
    body = new Body(getContentContainer(), getEncryptedData());
    if(featureDescription!=null && dialogHandler.isSendFeatureDescription())
    {
      messageParts.add(featureDescription);
    }
    messageParts.addAll(customHeaders);
    messageParts.add(body);
    stateOfMsg |= STATE_COMPOSED;
  }

  /**
   * Serialisiert und schreibt die Nachricht - ggf. verschlüsselt - in den
   * übergebenen Stream. Es wird abhängig vom isEncryption()-Flag des
   * DialogHandlers verschlüsselt oder nicht, Signaturen müssen jedoch von der
   * Anwendung selbst vor Aufruf dieser Methode (sign()) angebracht werden.
   * Die ausgehende Nachricht kann zu Debug- oder Archivierungszwecken (in jedem Fall
   * unverschlüsselt) in den zweiten übergebenen Stream geschrieben werden.
   * Dieser Parameter kann null sein.
   * @param out Stream, in den die Antwortnachricht geschrieben werden soll
   * @param storeOutput Stream, in dem die (unverschlüsselte) Antwortnachricht
   * gespeichert werden soll
   * @throws OSCIRoleException wenn erforderliche Zertifikate fehlen
   * @throws IOException bei Schreibproblemen
   * @throws NoSuchAlgorithmException wenn ein benötigter
   * Algorithmus nicht unterstützt wird
   * @see #sign()
   */
  public void writeToStream(OutputStream out, OutputStream storeOutput)
                     throws IOException,
                            OSCIException,
                            NoSuchAlgorithmException
  {
    if (dialogHandler.isEncryption())
      new SOAPMessageEncrypted(this, storeOutput).writeXML(out);
    else if (storeOutput != null)
    {
      StoreOutputStream sos = new StoreOutputStream(out, storeOutput);
      writeXML(sos);
      sos.close();
    }
    else
      writeXML(out);
  }
}
