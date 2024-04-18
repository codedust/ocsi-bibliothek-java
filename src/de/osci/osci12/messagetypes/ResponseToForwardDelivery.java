package de.osci.osci12.messagetypes;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import de.osci.helper.Tools;
import de.osci.osci12.OSCIException;
import de.osci.osci12.common.Constants.HeaderTags;
import de.osci.osci12.common.DialogHandler;
import de.osci.osci12.common.OSCICancelledException;
import de.osci.osci12.messageparts.Body;
import de.osci.osci12.messageparts.Inspection;
import de.osci.osci12.messageparts.ProcessCardBundle;
import de.osci.osci12.messageparts.Timestamp;
import de.osci.osci12.soapheader.OsciH;


/**
 * <p>Dieses Klasse repräsentiert die Antwort des Intermediärs auf einen
 * Weiterleitungsauftrag.
 * Clients erhalten vom Intermediär eine Instanz dieser Klasse, die eine Rückmeldung
 * über den Erfolg der Operation (getFeedback()) sowie ggf. den zugehörigen
 * Laufzettel enthält.</p>
 * <p>Copyright © 2023 Governikus GmbH &amp; Co. KG, Germany</p>
 * <p>Erstellt von Governikus GmbH &amp; Co. KG</p>
 * <p>Diese Bibliothek kann von jedermann nach Maßgabe der European Union
 *  Public Licence genutzt werden.</p><p>Die Lizenzbestimmungen können
 * unter der URL <a href="https://eupl.eu/">https://eupl.eu/</a> abgerufen werden.
 *
 *
 * @author R. Lindemann, N. Büngener
 * @version 2.4.1
 * @see de.osci.osci12.messagetypes.ForwardDelivery
 */
public class ResponseToForwardDelivery extends OSCIResponseTo
{

  ProcessCardBundle processCardBundle;

  /**
   * Dieser Konstruktor ist für den Intermediär
   * @param accDel
   * @param rspAccDel
   */
  ResponseToForwardDelivery(AcceptDelivery accDel, ResponseToAcceptDelivery rspAccDel)
                     throws NoSuchAlgorithmException
  {
    super(accDel.fwdDH);
    dialogHandler.getControlblock().setResponse(dialogHandler.prevChallenge);
    dialogHandler.getControlblock().setChallenge(Tools.createRandom(10));

    if (dialogHandler.getControlblock().getSequenceNumber() == -1)
      dialogHandler.getControlblock().setSequenceNumber(0);

    this.feedBack = rspAccDel.feedBack;
    body = new Body("");
    body.setNSPrefixes(this);
  }

  ResponseToForwardDelivery(DialogHandler dh)
  {
    super(dh);
    messageType = RESPONSE_TO_FORWARD_DELIVERY;
    body = new Body("");
    body.setNSPrefixes(this);
  }

  /**
   * Diese Methode liefert den Laufzettel der Zustellung zurück oder null,
   * wenn bei der Verarbeitung der Nachricht ein Fehler aufgetreten ist.
   * Die Informationen im Laufzettel können auch direkt über die einzelnen
   * getX()-Methoden ausgewertet werden.
   *
   * @return den Laufzettel als ProcessCardBundle-Objekt, im Fehlerfall null
   * @see #getTimestampCreation()
   * @see #getTimestampForwarding()
   * @see #getTimestampReception()
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
   * Liefert den im Laufzettel enthaltenen Zeitstempel vom Zeitpunkt des Eingangs
   * des Weiterleitungsauftrags beim Intermediär.
   * @return Zeitstempel der Einreichung beim Intermediär
   * @see #getProcessCardBundle()
   */
  public Timestamp getTimestampCreation()
  {
    return processCardBundle.getCreation();
  }

  /**
   * Liefert den im Laufzettel enthaltenen Zeitstempel vom Zeitpunkt des
   * vollständigen Aufbaus des Annahmeauftrags vom Intermediär für den Empfänger.
   * @return Zeitstempel der Erstellung des Annahmeauftrags durch den Intermediär
   * @see #getProcessCardBundle()
   */
  public Timestamp getTimestampForwarding()
  {
    return processCardBundle.getForwarding();
  }

  /**
   * Liefert den im Laufzettel enthaltenen Zeitstempel vom Zeitpunkt des
   * Eingangs einer positiven Annahmeantwort vom Empfänger beim Intermediär.
   * @return Zeitstempel der Registrierung einer Empfangsbestätigung (Annahmeantwort)
   * durch den Intermediär
   * @see #getProcessCardBundle()
   */
  public Timestamp getTimestampReception()
  {
    return processCardBundle.getReception();
  }

  /**
   * Liefert die Ergebnisse der Zertifikatsprüfungen in Form von Inspection-Objekten,
   * die im ProcessCardBundle-Objekt enthalten sind.
   * @return inspections die Prüfergebnisse
   * @see #getProcessCardBundle()
   */
  public Inspection[] getInspections()
  {
    return processCardBundle.getInspections();
  }

  /**
   * Liefert den im Laufzettel enthaltenen Betreff-Eintrag.
   * @return den Betreff der Zustellung
   * @see #getProcessCardBundle()
   */
  public String getSubject()
  {
    return processCardBundle.getSubject();
  }

  /**
   * Liefert das Datum der letzten Änderung des Laufzettels. Das Format
   * entspricht dem XML-Schema nach http://www.w3.org/TR/xmlschema-2/#dateTime
   * @return Datum der letzten Änderung
   * @see #getProcessCardBundle()
   */
  public String getRecentModification()
  {
    return processCardBundle.getRecentModification();
  }

  /**
   * Liefert die Message-ID der Nachricht.
   * @return Message-ID
   * @see #getProcessCardBundle()
   */
  public String getMessageId()
  {
    return processCardBundle.getMessageId();
  }

  /**
   * undocumented
   *
   * @param processCardBundle undocumented
   */
  protected void setProcessCardBundle(ProcessCardBundle processCardBundle)
  {
    this.processCardBundle = processCardBundle;
  }

  void sign()
     throws IOException,
            OSCIException,
            OSCICancelledException,
            NoSuchAlgorithmException
  {
    super.sign();
    // Der NonIntemedCertHeader liegt hier weiter unten
    messageParts.set(4, intermediaryCertificatesH);
    messageParts.set(2, null);
  }

  /**
   * undocumented
   *
   * @throws OSCIException undocumented
   * @throws IOException undocumented
   * @throws NoSuchAlgorithmException undocumented
   */
  protected void compose() throws OSCIException,
                                  IOException,
                                  NoSuchAlgorithmException
  {
    super.compose();
    messageParts.set(2, null);

    if (processCardBundle == null)
      osciH = new OsciH(HeaderTags.responseToForwardDelivery.getElementName(), writeFeedBack());
    else
      osciH = new OsciH(HeaderTags.responseToForwardDelivery.getElementName(), writeFeedBack() + processCardBundle.writeToString());

    messageParts.add(osciH);
    messageParts.add(intermediaryCertificatesH);
    if(featureDescription!=null && dialogHandler.isSendFeatureDescription())
    {
      messageParts.add(featureDescription);
    }
    messageParts.addAll(customHeaders);
    messageParts.add(body);
    stateOfMsg |= STATE_COMPOSED;
  }
}
