package de.osci.osci12.messagetypes;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import de.osci.helper.Tools;
import de.osci.osci12.OSCIException;
import de.osci.osci12.common.Constants.HeaderTags;
import de.osci.osci12.common.Constants.LanguageTextEntries;
import de.osci.osci12.common.DialogHandler;
import de.osci.osci12.common.OSCICancelledException;
import de.osci.osci12.messageparts.Body;
import de.osci.osci12.messageparts.Inspection;
import de.osci.osci12.messageparts.ProcessCardBundle;
import de.osci.osci12.messageparts.Timestamp;
import de.osci.osci12.roles.OSCIRoleException;
import de.osci.osci12.roles.Originator;
import de.osci.osci12.soapheader.OsciH;


/**
 * <p>Instanzen dieser Klasse werden als Antworten auf Zustellungsaufträge
 * zurückgegeben. Das Nachrichtenobjekt enthält eine Rückmeldung über den Erfolg
 * der Operation (getFeedback()) sowie ggf. den Laufzettel der
 * Zustellung.</p>
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
 * @see de.osci.osci12.messagetypes.StoreDelivery
 * @see #getProcessCardBundle()
 */
public class ResponseToStoreDelivery extends OSCIResponseTo
{
  //  private static Log log = LogFactory.getLog(ResponseToStoreDelivery.class);
  ProcessCardBundle processCardBundle;

  ResponseToStoreDelivery(DialogHandler dh) throws Exception
  {
    this(dh, false);
  }

  ResponseToStoreDelivery(DialogHandler dh, boolean parser)
                   throws NoSuchAlgorithmException
  {
    super(dh);
    messageType = RESPONSE_TO_STORE_DELIVERY;
    originator = ((Originator) dh.getClient());

    if (!parser)
    {
      dialogHandler.getControlblock().setResponse(dialogHandler.prevChallenge);
      dialogHandler.getControlblock().setChallenge(Tools.createRandom(10));
    }

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
   * des Zustellungsauftrags beim Intermediär.
   * @return Zeitstempel der Einreichung beim Intermediär
   * @see #getProcessCardBundle()
   */
  public Timestamp getTimestampCreation()
  {
    return processCardBundle.getCreation();
  }

  /**
   * Liefert die Ergebnisse der Zertifikatsprüfungen in Form von Inspection-Objekten,
   * die im ProcessCardBundle-Objekt enthalten sind.
   * @return inspections die Prüfergebnisse
   * @see #getProcessCardBundle()
   *
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

  void sign()
     throws IOException,
            OSCIException,
            OSCICancelledException,
            NoSuchAlgorithmException
  {
    super.sign();
    // Der IntemedCertHeader liegt hier weiter unten
    messageParts.set(4, intermediaryCertificatesH);
    messageParts.set(2, null);
  }

  /**
   * undocumented
   *
   * @throws IOException undocumented
   * @throws OSCIRoleException undocumented
   * @throws IllegalStateException undocumented
   */
  protected void compose() throws IOException,
                                  OSCIException,
                                  NoSuchAlgorithmException
  {
    super.compose();
    messageParts.set(2, null);

    if (dialogHandler.getControlblock().getResponse() == null)
      throw new IllegalStateException(DialogHandler.text.getString(LanguageTextEntries.missing_entry.name()) + ": Response");

    if (dialogHandler.getControlblock().getConversationID() == null)
      throw new IllegalStateException(DialogHandler.text.getString(LanguageTextEntries.missing_entry.name()) + ": Conversation-Id");

    if (dialogHandler.getControlblock().getSequenceNumber() == -1)
      throw new IllegalStateException(DialogHandler.text.getString(LanguageTextEntries.missing_entry.name()) + ": SequenceNumber");

    if (feedBack == null)
      throw new IllegalStateException(DialogHandler.text.getString(LanguageTextEntries.missing_entry.name()) + ": Feedback");

    if (processCardBundle == null)
      osciH = new OsciH(HeaderTags.responseToStoreDelivery.getElementName(), writeFeedBack());
    else
      osciH = new OsciH(HeaderTags.responseToStoreDelivery.getElementName(), writeFeedBack() + processCardBundle.writeToString());

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
