package de.osci.osci12.messagetypes;

import java.io.IOException;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;

import de.osci.helper.Tools;
import de.osci.osci12.OSCIException;
import de.osci.osci12.common.Constants.HeaderTags;
import de.osci.osci12.common.Constants.LanguageTextEntries;
import de.osci.osci12.common.DialogHandler;
import de.osci.osci12.common.OSCICancelledException;
import de.osci.osci12.messageparts.Body;
import de.osci.osci12.roles.Originator;
import de.osci.osci12.soapheader.OsciH;


/**
 * <p>
 * Mit dieser Klasse werden Nachrichtenobjekte für Zustellungsabholaufträge angelegt. Clients können hiermit
 * maximal eine Nachricht vom Intermediär abrufen. Als Antwort auf diese Nachricht erhalten sie vom
 * Intermediär ein ResponseToFetchDelivery-Nachrichtenobjekt, welches eine Rückmeldung über den Erfolg der
 * Operation (getFeedback()) und ggf. die gewünschte Nachricht enthält.
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
 * @see de.osci.osci12.messagetypes.ResponseToFetchDelivery
 */
public class FetchDelivery extends FetchRequestAbstract
{

  FetchDelivery()
  {
    messageType = FETCH_DELIVERY;
  }

  /**
   * Legt ein Nachrichtenobjekt für einen Zustellungsabholauftrag an.
   *
   * @param dh DialogHandler-Objekt des Dialogs, innerhalb dessen die Nachricht versendet werden soll
   * @see de.osci.osci12.common.DialogHandler
   * @throws NoSuchAlgorithmException wenn der verwendete Security-Provider
   * einen erforderlichen Algorithmus nicht unterstützt (Erzeugung einer Zufallszahl)
   */
  public FetchDelivery(DialogHandler dh) throws NoSuchAlgorithmException
  {
    super(dh);
    messageType = FETCH_DELIVERY;
    originator = ((Originator)dh.getClient());
    dialogHandler.getControlblock().setResponse(dialogHandler.prevChallenge);
    dialogHandler.getControlblock().setChallenge(Tools.createRandom(10));
    dialogHandler.getControlblock()
                 .setSequenceNumber(dialogHandler.getControlblock().getSequenceNumber() + 1);
  }


  /**
   * Versendet die Nachricht und liefert die Antwortnachricht zurück. Diese Methode wirft eine Exception, wenn
   * beim Aufbau oder Versand der Nachricht ein Fehler auftritt. Fehlermeldungen vom Intermediär müssen dem
   * Feedback der Antwortnachricht entnommen werden.
   *
   * @throws IOException bei Ein-/Ausgabefehlern
   * @throws de.osci.osci12.OSCIException bei OSCI-Fehlern
   * @throws NoSuchAlgorithmException Algorithmus Fehler
   * @return Antwortnachricht-Objekt
   * @see #send(OutputStream, OutputStream)
   */
  public ResponseToFetchDelivery send() throws IOException, OSCIException, NoSuchAlgorithmException
  {
    return (ResponseToFetchDelivery)transmit(null, null);
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
   * @throws NoSuchAlgorithmException Algorithmus Fehler
   * @return Antwortnachricht-Objekt
   * @see #send()
   */
  public ResponseToFetchDelivery send(OutputStream storeOutput, OutputStream storeInput)
    throws IOException, OSCIException, NoSuchAlgorithmException
  {
    return (ResponseToFetchDelivery)transmit(storeOutput, storeInput);
  }

  void sign() throws IOException, OSCIException, OSCICancelledException, NoSuchAlgorithmException
  {
    super.sign();
    // Der NonIntemedCertHeader liegt hier weiter unten
    messageParts.set(5, nonIntermediaryCertificatesH);
    messageParts.set(3, null);
  }

  /**
   * undocumented
   *
   * @throws IOException undocumented
   * @throws OSCIException undocumented
   * @throws NoSuchAlgorithmException undocumented
   * @throws IllegalStateException undocumented
   */
  protected void compose() throws IOException, OSCIException, NoSuchAlgorithmException
  {
    super.compose();
    messageParts.set(3, null);

    if (dialogHandler.getControlblock().getChallenge() == null)
      throw new IllegalStateException(DialogHandler.text.getString(LanguageTextEntries.missing_entry.name())
                                      + ": Challenge");

    if (dialogHandler.getControlblock().getResponse() == null)
      throw new IllegalStateException(DialogHandler.text.getString(LanguageTextEntries.missing_entry.name())
                                      + ": Response");

    if (dialogHandler.getControlblock().getConversationID() == null)
      throw new IllegalStateException(DialogHandler.text.getString(LanguageTextEntries.missing_entry.name())
                                      + ": Conversation-Id");

    if (dialogHandler.getControlblock().getSequenceNumber() == -1)
      throw new IllegalStateException(DialogHandler.text.getString(LanguageTextEntries.missing_entry.name())
                                      + ": SequenceNumber");

    osciH = new OsciH(HeaderTags.fetchDelivery.getElementName(), getSelectionRuleString());
    messageParts.add(osciH);
    messageParts.add(nonIntermediaryCertificatesH);
    if (featureDescription != null && dialogHandler.isSendFeatureDescription())
    {
      messageParts.add(featureDescription);
    }
    messageParts.addAll(customHeaders);
    body = new Body("");
    // body.setNSPrefixes(this);
    messageParts.add(body);
    stateOfMsg |= STATE_COMPOSED;
  }
}
