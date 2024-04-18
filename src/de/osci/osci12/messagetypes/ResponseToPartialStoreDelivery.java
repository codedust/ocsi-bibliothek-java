package de.osci.osci12.messagetypes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Vector;

import de.osci.helper.Tools;
import de.osci.osci12.OSCIException;
import de.osci.osci12.common.Constants.HeaderTags;
import de.osci.osci12.common.Constants.LanguageTextEntries;
import de.osci.osci12.common.DialogHandler;
import de.osci.osci12.messageparts.Body;
import de.osci.osci12.messageparts.ChunkInformation;
import de.osci.osci12.messageparts.FeedbackObject;
import de.osci.osci12.messageparts.Inspection;
import de.osci.osci12.messageparts.MessagePartsFactory;
import de.osci.osci12.messageparts.ProcessCardBundle;
import de.osci.osci12.messageparts.Timestamp;
import de.osci.osci12.roles.OSCIRoleException;
import de.osci.osci12.roles.Originator;
import de.osci.osci12.soapheader.OsciH;


/**
 * <p>
 * Instanzen dieser Klasse werden als Antworten auf paketierte Zustellungsaufträge zurückgegeben. Das
 * Nachrichtenobjekt enthält eine Rückmeldung über den Erfolg der Operation (getFeedback()) sowie ggf. den
 * Laufzettel der paketierten Zustellung.
 * </p>
 * <p>
 * In dieser Responsenachricht wird eine Porcesscard sowie ein Feedback für die PartialStoreDelivery Nachricht
 * erwartet. Sollte dieses Objekt die letzte Nachricht einer paketierten Zustellung sein wird die Processcard
 * der gesamt Nachricht und ein Inside Feedback eingetragen.
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
 * @see de.osci.osci12.messagetypes.PartialStoreDelivery
 */
public class ResponseToPartialStoreDelivery extends OSCIResponseTo
{

  ProcessCardBundle processCardBundle;

  Vector<String[]> insideFeedBack;

  FeedbackObject[] insideFeedbackObjects;

  ChunkInformation chunkInformation = MessagePartsFactory.creatChunkInformation(ChunkInformation.CheckInstance.ResponsePartialStoreDelivery);

  ResponseToPartialStoreDelivery(DialogHandler dh) throws NoSuchAlgorithmException
  {
    this(dh, null, false);
  }

  ResponseToPartialStoreDelivery(DialogHandler dh, boolean parser) throws NoSuchAlgorithmException
  {
    this(dh, null, parser);
  }

  ResponseToPartialStoreDelivery(DialogHandler dh, ProcessCardBundle processCardBundle, boolean parser)
    throws NoSuchAlgorithmException
  {
    super(dh);
    messageType = RESPONSE_TO_PARTIAL_STORE_DELIVERY;
    originator = (Originator)dh.getClient();
    this.processCardBundle = processCardBundle;
    if (!parser)
    {
      dialogHandler.getControlblock().setResponse(dialogHandler.prevChallenge);
      dialogHandler.getControlblock().setChallenge(Tools.createRandom(10));
    }

    body = new Body("");
    body.setNSPrefixes(this);
  }

  /**
   * Diese Methode liefert den Laufzettel der paketierten Zustellung zurück oder null, wenn bei der
   * Verarbeitung der Nachricht ein Fehler aufgetreten ist. Sollte die PartialStoreDelivery Nachricht die
   * letzte zu übermittelnde Nachricht enthalten und die Nachricht ausreichend schnell verarbeitet werden,
   * befindet sich in der ProcessCard die ProcessCard zu der Gesamtnachricht (zusammengesetzte StoreDelivery
   * Nachricht). Sollten noch nicht alle Nachrichtenbestandteile übermittelt worden sein, betrifft diese
   * ProcessCard lediglich die Teilzustellung. Die Informationen im Laufzettel können auch direkt über die
   * einzelnen getter-Methoden ausgewertet werden.
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
   * Liefert den im Laufzettel enthaltenen Zeitstempel vom Zeitpunkt des Eingangs des Zustellungsauftrags beim
   * Intermediär.
   *
   * @return Zeitstempel der Einreichung beim Intermediär
   * @see #getProcessCardBundle()
   */
  public Timestamp getTimestampCreation()
  {
    return processCardBundle.getCreation();
  }

  /**
   * Liefert die Ergebnisse der Zertifikatsprüfungen in Form von Inspection-Objekten, die im
   * ProcessCardBundle-Objekt enthalten sind.
   *
   * @return inspections die Prüfergebnisse
   * @see #getProcessCardBundle()
   */
  public Inspection[] getInspections()
  {
    return processCardBundle.getInspections();
  }

  /**
   * Liefert den im Laufzettel enthaltenen Betreff-Eintrag.
   *
   * @return den Betreff der Zustellung
   * @see #getProcessCardBundle()
   */
  public String getSubject()
  {
    return processCardBundle.getSubject();
  }

  /**
   * @return Liefert die ChunkInformation zur Nachricht
   * @see #setChunkInformation(ChunkInformation)
   */
  public ChunkInformation getChunkInformation()
  {
    return chunkInformation;
  }

  /**
   * Setzt die aktuelle ChunkInformation
   *
   * @param chunkInformation ChunkInformation
   * @see #getChunkInformation()
   */
  public void setChunkInformation(ChunkInformation chunkInformation)
  {
    this.chunkInformation = chunkInformation;
  }

  /**
   * Setzt die Inside Feedbacks
   *
   * @param code
   */
  void setInsideFeedback(String[] code)
  {
    insideFeedBack = new Vector<>();

    String[] fb;

    for ( int i = 0 ; i < code.length ; i++ )
    {
      fb = new String[3];
      fb[0] = dialogHandler.getLanguageList();
      fb[1] = code[i];
      fb[2] = DialogHandler.text.getString(code[i]);
      insideFeedBack.add(fb);
    }
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
   * Liefert die Message-ID der Nachricht.
   *
   * @return Message-ID
   * @see #getProcessCardBundle()
   */
  public String getMessageId()
  {
    return processCardBundle.getMessageId();
  }

  @Override
  void sign()
    throws IOException, OSCIException, java.security.NoSuchAlgorithmException
  {
    super.sign();
    // Der IntemedCertHeader liegt hier weiter unten
    messageParts.set(4, intermediaryCertificatesH);
    messageParts.set(2, null);
  }



  /**
   * Setzt die Nachricht zusammen
   *
   * @throws IOException Im Fehlerfall
   * @throws OSCIRoleException Im Fehlerfall
   * @throws IllegalStateException Im Fehlerfall
   */
  @Override
  protected void compose() throws IOException, OSCIException, NoSuchAlgorithmException
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

    ByteArrayOutputStream chunkInformationXml = new ByteArrayOutputStream();

    MessagePartsFactory.writeXML(chunkInformation, chunkInformationXml);
    String msgElement = "";
    if (insideFeedBack != null && !insideFeedBack.isEmpty())
    {
      msgElement = msgElement.concat(writeInsideFeedBack());
    }

    // RESPONSE_TO_PARTIAL_FETCH_DELIVERY
    if (processCardBundle == null)
    {
      osciH = new OsciH(HeaderTags.responseToPartialStoreDelivery.getElementName(),
                        msgElement + writeFeedBack() + chunkInformationXml.toString(), osci2017NSPrefix);
    }
    else
    {
      osciH = new OsciH(HeaderTags.responseToPartialStoreDelivery.getElementName(),
                        msgElement + writeFeedBack() + processCardBundle.writeToString()
                                                                                    + chunkInformationXml.toString(),
                        osci2017NSPrefix);
    }

    messageParts.add(osciH);
    messageParts.add(intermediaryCertificatesH);
    if (featureDescription != null && dialogHandler.isSendFeatureDescription())
    {
      messageParts.add(featureDescription);
    }
    messageParts.addAll(customHeaders);
    messageParts.add(body);
    stateOfMsg |= STATE_COMPOSED;
  }

  String writeInsideFeedBack()
  {
    StringBuilder sb = new StringBuilder("<");
    String namespace = osciNSPrefix;
    sb.append(osci2017NSPrefix);
    sb.append(":InsideFeedback>");

    for ( int i = 0 ; i < insideFeedBack.size() ; i++ )
    {
      sb.append("<");
      sb.append(namespace);
      sb.append(":Entry xml:lang=\"");
      sb.append(((String[])insideFeedBack.get(i))[0]);
      sb.append("\"><");
      sb.append(namespace);
      sb.append(":Code>");
      sb.append(((String[])insideFeedBack.get(i))[1]);
      sb.append("</");
      sb.append(namespace);
      sb.append(":Code><");
      sb.append(namespace);
      sb.append(":Text>");
      sb.append(((String[])insideFeedBack.get(i))[2]);
      sb.append("</");
      sb.append(namespace);
      sb.append(":Text></");
      sb.append(namespace);
      sb.append(":Entry>");
    }

    sb.append("</");
    sb.append(osci2017NSPrefix);
    sb.append(":InsideFeedback>");

    return sb.toString();
  }

}
