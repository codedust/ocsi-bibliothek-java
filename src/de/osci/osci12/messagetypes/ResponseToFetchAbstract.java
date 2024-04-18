package de.osci.osci12.messagetypes;


import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import de.osci.osci12.OSCIException;
import de.osci.osci12.common.DialogHandler;
import de.osci.osci12.common.OSCICancelledException;
import de.osci.osci12.messageparts.Content;
import de.osci.osci12.messageparts.ContentContainer;
import de.osci.osci12.messageparts.EncryptedDataOSCI;
import de.osci.osci12.messageparts.Inspection;
import de.osci.osci12.messageparts.ProcessCardBundle;
import de.osci.osci12.messageparts.Timestamp;
import eu.osci.ws._2014._10.transport.MessageMetaData;


/**
 * <p>
 * Dieses Klasse repräsentiert die Antwort des Intermediärs auf einen Zustellungsabholauftrag. Clients
 * erhalten vom Intermediär eine Instanz dieser Klasse, die eine Rückmeldung über den Erfolg der Operation
 * (getFeedback()) sowie ggf. die angeforderten verschlüsselten und/oder unverschlüsselten Inhaltsdaten
 * einschl. des zugehörigen Laufzettels enthält.
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
 * @see de.osci.osci12.messagetypes.FetchDelivery
 */
public abstract class ResponseToFetchAbstract extends OSCIResponseTo implements ContentPackageI
{

  // private static Log log = LogFactory.getLog(ResponseToFetchDelivery.class);
  String selectionRule;

  int selectionMode = -1;

  protected ProcessCardBundle processCardBundle;

  ResponseToFetchAbstract(DialogHandler dh)
  {
    super(dh);
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

  void setSelectionRule(String selectionRule)
  {
    this.selectionRule = selectionRule;
  }

  /**
   * Liefert die gesetzte Auswahlregel. Der Inhalt des zurückgegebenen Strings hängt vom gesetzten
   * Auswahlmodus ab und kann entweder in einer Base64-codierten Message-ID oder einem Datum bestehen. Das
   * Format eines Datums entspricht dem XML-Schema nach <a href="http://www.w3.org/TR/xmlschema-2/#dateTime">
   * http://www.w3.org/TR/xmlschema-2/#dateTime</a>. Wurde keine Regel gesetzt, wird als default null
   * zurückgegeben.
   *
   * @return Auswahlregel (Message-ID oder Datum)
   * @see #getSelectionRule()
   */
  public String getSelectionRule()
  {
    return selectionRule;
  }

  void setSelectionMode(int selectionMode)
  {
    this.selectionMode = selectionMode;
  }

  /**
   * Liefert den gesetzten Auswahlmodus.
   *
   * @return den Auswahlmodus SELECT_BY_MESSAGE_ID, SELECT_BY_DATE_OF_RECEPTION oder NO_SELECTION_RULE
   */
  public int getSelectionMode()
  {
    return selectionMode;
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
   * Liefert den im Laufzettel enthaltenen Zeitstempel vom Zeitpunkt des Eingangs des Zustellungsauftrags beim
   * Intermediär.
   *
   * @return Zeitstempel der Einreichung beim Intermediär
   * @see #getProcessCardBundle()
   */
  public Timestamp getTimestampCreation()
  {
    if (processCardBundle == null)
      return null;

    return processCardBundle.getCreation();
  }

  /**
   * Liefert den im Laufzettel enthaltenen Zeitstempel vom Zeitpunkt des vollständigen Aufbaus der
   * Abholantwort vom Intermediär für den Empfänger.
   *
   * @return Zeitstempel der Erstellung der Abholantwort durch den Intermediär
   * @see #getProcessCardBundle()
   */
  public Timestamp getTimestampForwarding()
  {
    if (processCardBundle == null)
      return null;

    return processCardBundle.getForwarding();
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
    if (processCardBundle == null)
      return null;

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
    if (processCardBundle == null)
      return null;

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
    if (processCardBundle == null)
      return null;

    return processCardBundle.getRecentModification();
  }

  /**
   * Liefert die Message-ID der Nachricht.
   *
   * @return Message-ID
   */
  public String getMessageId()
  {
    if (processCardBundle == null)
      return null;

    return processCardBundle.getMessageId();
  }

  void sign() throws IOException, OSCIException, OSCICancelledException, NoSuchAlgorithmException
  {
    super.sign();
    // Der IntemedCertHeader liegt hier weiter unten
    messageParts.set(4, intermediaryCertificatesH);
    messageParts.set(2, null);
  }


}
