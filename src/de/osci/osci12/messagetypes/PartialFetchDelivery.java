package de.osci.osci12.messagetypes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;

import de.osci.helper.Tools;
import de.osci.osci12.OSCIException;
import de.osci.osci12.common.Constants.HeaderTags;
import de.osci.osci12.common.Constants.LanguageTextEntries;
import de.osci.osci12.common.DialogHandler;
import de.osci.osci12.messageparts.Body;
import de.osci.osci12.messageparts.ChunkInformation;
import de.osci.osci12.messageparts.MessagePartsFactory;
import de.osci.osci12.roles.Originator;
import de.osci.osci12.soapheader.OsciH;


/**
 * <p>
 * Mit dieser Klasse werden Nachrichtenobjekte für paketierte Zustellungsabholaufträge angelegt. Clients
 * können hiermit maximal eine Nachricht vom Intermediär abrufen. Als Antwort auf diese Nachricht erhalten sie
 * vom Intermediär ein ResponseToPartialFetchDelivery-Nachrichtenobjekt bzw. eine ResponseToFetchDelivery,
 * welches eine Rückmeldung über den Erfolg der Operation (getFeedback()) und ggf. die gewünschte Nachricht
 * enthält.
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
 * @see de.osci.osci12.messagetypes.ResponseToPartialFetchDelivery
 */
public class PartialFetchDelivery extends FetchRequestAbstract
{

  private ChunkInformation chunkInformation;

  PartialFetchDelivery()
  {
    messageType = PARTIAL_FETCH_DELIVERY;
    setBase64Encoding(false);
  }

  /**
   * Legt ein Nachrichtenobjekt für einen paketierte Zustellungsabholauftrag an.
   *
   * @param dh DialogHandler-Objekt des Dialogs, innerhalb dessen die Nachricht versendet werden soll
   * @param chunkInforamtion Setzt das aktelle ChunkInformation Objekt.
   * @see de.osci.osci12.common.DialogHandler
   * @throws NoSuchAlgorithmException undocumented
   */
  public PartialFetchDelivery(DialogHandler dh, ChunkInformation chunkInforamtion)
    throws NoSuchAlgorithmException
  {
    super(dh);
    messageType = PARTIAL_FETCH_DELIVERY;
    originator = (Originator)dh.getClient();
    setChunkInformation(chunkInforamtion);
    setBase64Encoding(false);
    dialogHandler.getControlblock().setResponse(dialogHandler.prevChallenge);
    dialogHandler.getControlblock().setChallenge(Tools.createRandom(10));
    dialogHandler.getControlblock()
                 .setSequenceNumber(dialogHandler.getControlblock().getSequenceNumber() + 1);
  }

  /**
   * @return Liefert das eingetragene ChunkInformation Objekt
   * @see #PartialFetchDelivery(DialogHandler, ChunkInformation)
   * @see #setChunkInformation(ChunkInformation)
   */
  public ChunkInformation getChunkInformation()
  {
    return chunkInformation;
  }

  /**
   * Die ChunkInformation sollte bereits mit dem Konstruktor gesetzt werden
   *
   * @param chunkInformation Setzt die ChunkInformation
   * @see #PartialFetchDelivery(DialogHandler, ChunkInformation)
   * @see #getChunkInformation()
   */
  public void setChunkInformation(ChunkInformation chunkInformation)
  {
    this.chunkInformation = chunkInformation;
  }

  /**
   * Versendet die Nachricht und liefert die Antwortnachricht zurück. Diese Methode wirft eine Exception, wenn
   * beim Aufbau oder Versand der Nachricht ein Fehler auftritt. Fehlermeldungen vom Intermediär müssen dem
   * Feedback der Antwortnachricht entnommen werden.
   * <p>
   * Als Antwort wird ein Teil einer ResponseToFetchDelivery Nachricht als Payload der
   * ResponseToPartialFetchDelivery Nachricht übergeben. Sollte die Nachricht kleiner als die eingestellte
   * Chunk-Size sein, wird sofort die ResponseToFetchDelivery Nachricht als Ergebnis zurück gegeben.
   * </p>
   *
   * @throws IOException bei Ein-/Ausgabefehlern
   * @throws de.osci.osci12.OSCIException bei OSCI-Fehlern
   * @throws NoSuchAlgorithmException undocumented
   * @return Antwortnachricht-Objekt
   * @see #send(OutputStream, OutputStream)
   */
  public ResponseToFetchAbstract send() throws IOException, OSCIException, NoSuchAlgorithmException
  {
    return (ResponseToFetchAbstract)transmit(null, null);
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
   * @throws NoSuchAlgorithmException undocumented
   * @return Antwortnachricht-Objekt
   * @see #send()
   */
  public ResponseToFetchAbstract send(OutputStream storeOutput, OutputStream storeInput)
    throws IOException, OSCIException, NoSuchAlgorithmException
  {
    return (ResponseToFetchAbstract)transmit(storeOutput, storeInput);
  }

  @Override
  void sign() throws IOException, OSCIException, NoSuchAlgorithmException
  {
    super.sign();
    // Der NonIntemedCertHeader liegt hier weiter unten
    messageParts.set(5, nonIntermediaryCertificatesH);
    messageParts.set(3, null);
  }

  /**
   * Setzt die Nachricht zusammen
   *
   * @throws IOException Im Fehlerfall
   * @throws OSCIException Im Fehlerfall
   * @throws NoSuchAlgorithmException Im Fehlerfall
   */
  @Override
  protected void compose() throws IOException, OSCIException, NoSuchAlgorithmException
  {
    super.compose();
    messageParts.set(3, null);

    if (dialogHandler.getControlblock().getChallenge() == null)
      throw new IllegalStateException(DialogHandler.text.getString(LanguageTextEntries.missing_entry.name()) + ": Challenge");

    if (dialogHandler.getControlblock().getResponse() == null)
      throw new IllegalStateException(DialogHandler.text.getString(LanguageTextEntries.missing_entry.name()) + ": Response");

    if (dialogHandler.getControlblock().getConversationID() == null)
      throw new IllegalStateException(DialogHandler.text.getString(LanguageTextEntries.missing_entry.name()) + ": Conversation-Id");

    if (dialogHandler.getControlblock().getSequenceNumber() == -1)
      throw new IllegalStateException(DialogHandler.text.getString(LanguageTextEntries.missing_entry.name()) + ": SequenceNumber");

    StringBuilder selection = new StringBuilder(getSelectionRuleString());
    ByteArrayOutputStream chunkInformationXml = new ByteArrayOutputStream();
    MessagePartsFactory.writeXML(chunkInformation, chunkInformationXml);
    selection.append(chunkInformationXml);

    osciH = new OsciH(HeaderTags.partialFetchDelivery.getElementName(), selection.toString(),
                      osci2017NSPrefix);
    messageParts.add(osciH);
    messageParts.add(nonIntermediaryCertificatesH);
    if (featureDescription != null && dialogHandler.isSendFeatureDescription())
    {
      messageParts.add(featureDescription);
    }
    messageParts.addAll(customHeaders);
    body = new Body("");
    messageParts.add(body);
    stateOfMsg |= STATE_COMPOSED;
  }
}
