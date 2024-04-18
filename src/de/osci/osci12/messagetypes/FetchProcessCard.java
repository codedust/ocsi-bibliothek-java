package de.osci.osci12.messagetypes;

import java.io.IOException;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;

import de.osci.helper.Tools;
import de.osci.osci12.OSCIException;
import de.osci.osci12.common.Constants;
import de.osci.osci12.common.Constants.LanguageTextEntries;
import de.osci.osci12.common.DialogHandler;
import de.osci.osci12.messageparts.Body;
import de.osci.osci12.roles.OSCIRoleException;
import de.osci.osci12.roles.Originator;


/**
 * <p>Mit dieser Klasse werden Nachrichtenobjekte für Laufzettelabholaufträge
 * angelegt. Clients können hiermit Laufzettel eingegangener Nachrichten vom Intermediär
 * abrufen.  Als Antwort auf diese Nachricht erhalten sie vom Intermediär
 * ein ResponseToFetchProcessCard-Nachrichtenobjekt, welches eine Rückmeldung über den
 * Erfolg der Operation und ggf. die gewünschten Laufzettel enthält.</p>
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
 * @see de.osci.osci12.messagetypes.ResponseToFetchProcessCard
 */
public class FetchProcessCard extends OSCIRequest
{
  //  private static Log log = LogFactory.getLog(FetchProcessCard.class);
  private String selectionRule;
  private int selectionMode = -1;
  private int roleForSelection = -1;
  private boolean selectNoReceptionOnly = false;
  private long quantityLimit = -1;

  /**
   * Legt ein Nachrichtenobjekt für einen Laufzettelabholauftrag an.
   *
   * @param dh DialogHandler-Objekt des Dialogs, innerhalb dessen die Nachricht
   * versendet werden soll
   *
   * @see de.osci.osci12.common.DialogHandler
   * @throws NoSuchAlgorithmException Algorithmus Fehler
   *
   */
  public FetchProcessCard(DialogHandler dh) throws NoSuchAlgorithmException
  {
    super(dh);
    originator = ((Originator) dh.getClient());
    messageType = FETCH_PROCESS_CARD;
    dialogHandler.getControlblock().setResponse(dialogHandler.prevChallenge);
    dialogHandler.getControlblock().setChallenge(Tools.createRandom(10));
    dialogHandler.getControlblock().setSequenceNumber(dialogHandler.getControlblock().getSequenceNumber() + 1);
  }

  FetchProcessCard()
  {
    super();
    messageType = FETCH_PROCESS_CARD;
  }

  /**
   * Versendet die Nachricht und liefert die Antwortnachricht zurück.
   * Diese Methode wirft eine Exception, wenn beim Aufbau oder Versand der
   * Nachricht ein Fehler auftritt. Fehlermeldungen vom Intermediär müssen
   * dem Feedback der Antwortnachricht entnommen werden.
   *
   * @throws IOException bei Ein-/Ausgabefehlern
   * @throws OSCIException bei OSCI-Fehlern
   * @throws NoSuchAlgorithmException Algorithmus Fehler
   * @return Antwortnachricht-Objekt
   * @see #send(OutputStream, OutputStream)
   */
  public ResponseToFetchProcessCard send() throws IOException,
                                                  OSCIException,
                                                  NoSuchAlgorithmException
  {
    return (ResponseToFetchProcessCard) transmit(null, null);
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
  public ResponseToFetchProcessCard send(OutputStream storeOutput, OutputStream storeInput)
                                  throws IOException,
                                         OSCIException,
                                         NoSuchAlgorithmException
  {
    return (ResponseToFetchProcessCard) transmit(storeOutput, storeInput);
  }

  /**
   * Setzt die Auswahlregel für die abzuholende Nachricht. Der Inhalt des
   * übergebenen Strings hängt vom gewählten Auswahlmodus ab und kann
   * entweder in einer Message-ID oder einem Datum bestehen.
   * Mehrere Message-IDs können durch ";" getrennt aneinandergereiht werden.
   * Das Format eines Datums muss dem XML-Schema nach
   * <a href="http://www.w3.org/TR/xmlschema-2/#dateTime">
   * http://www.w3.org/TR/xmlschema-2/#dateTime</a> (ISO 8601-Format) entsprechen.
   *
   * @param selectionRule eine oder mehrere Message-IDs oder Datum
   * @see #setSelectionMode(int)
   * @see #getSelectionRule()
   * @see #formatISO8601(java.util.Date)
   */
  public void setSelectionRule(String selectionRule)
  {
    this.selectionRule = selectionRule;
  }

  /**
   * Liefert die gesetzte Auswahlregel. Der Inhalt des zurückgegebenen
   * Strings hängt vom gesetzten Auswahlmodus ab und kann
   * entweder in einer oder mehrerer Message-ID oder einem Datum bestehen.
   * Mehrere Message-IDs werden aneinandergereiht durch ";" getrennt zurückgegeben.
   * Wurde keine Regel gesetzt, wird als default null zurückgegeben.
   * @return Auswahlregel (Message-IDs oder Datum)
   * @see #setSelectionRule(String)
   * @see #setSelectionMode(int)
   */
  public String getSelectionRule()
  {
    return selectionRule;
  }

  /**
   * Setzt den Auswahlmodus. Mögliche Werte sind
   * SELECT_BY_MESSAGE_ID, SELECT_BY_DATE_OF_RECEPTION,
   * SELECT_BY_RECENT_MODIFICATION oder NO_SELECTION_RULE (default).
   *
   * @param selectionMode Auswahlmodus
   * @see #SELECT_BY_MESSAGE_ID
   * @see #SELECT_BY_DATE_OF_RECEPTION
   * @see #SELECT_BY_RECENT_MODIFICATION
   * @see #NO_SELECTION_RULE
   * @see #setSelectionRule(String)
   */
  public void setSelectionMode(int selectionMode)
  {
    if ((selectionMode != SELECT_BY_MESSAGE_ID) && (selectionMode != SELECT_BY_DATE_OF_RECEPTION) &&
            (selectionMode != SELECT_BY_RECENT_MODIFICATION) && (selectionMode != NO_SELECTION_RULE))
      throw new IllegalArgumentException(DialogHandler.text.getString(LanguageTextEntries.invalid_firstargument.name()) + selectionMode);

    this.selectionMode = selectionMode;
  }

  /**
   * Legt fest, ob nur Laufzettel von Nachrichten zurückgegeben werden sollen,
   * die an den Absender des Laufzettelabholauftrags geschickt wurden oder
   * solche von Nachrichten, die von dem Absender geschickt wurden. Mögliche Werte sind
   * <ul><li>SELECT_ORIGINATOR - für Nachrichten, die vom Absender dieses
   * Auftrags gesendet wurden.</li><li>SELECT_ADDRESSEE - für Nachrichten, die
   * an den Absender dieses Auftrags gesendet wurden.</li><li>SELECT_ALL -
   * für alle Nachrichten (default).</li></ul> <b>Diese Einstellung ist nur in
   * den Selection-Modes SELECT_BY_DATE_OF_RECEPTION und
   * SELECT_BY_RECENT_MODIFICATION wirksam.</b>
   *
   * @param role Modus
   * @see #SELECT_ORIGINATOR
   * @see #SELECT_ADDRESSEE
   * @see #SELECT_ALL
   * @see #setSelectionRule(String)
   * @see #setSelectionMode(int)
   */
  public void setRoleForSelection(int role)
  {
    if ((role != SELECT_ORIGINATOR) && (role != SELECT_ADDRESSEE) && (role != SELECT_ALL))
      throw new IllegalArgumentException(DialogHandler.text.getString(LanguageTextEntries.invalid_firstargument.name()) + role);

    this.roleForSelection = role;
  }

  /**
   * Liefert den Identifier für das Auswahlkriterium, ob nur Laufzettel von
   * Nachrichten zurückgegeben werden sollen, die an den oder vom Absender des
   * Laufzettelabholauftrags geschickt wurden.
   *
   * @see #setRoleForSelection(int)
   * @return Modus
   */
  public int getRoleForSelection()
  {
    return roleForSelection;
  }

  /**
   * Legt fest, ob nur Laufzettel von Nachrichten zurückgegeben werden sollen,
   * für die keine Empfangsbestätigung vom Empfänger vorliegt.
   *
   * @param noReceptionOnly true, es werden nur Laufzettel für nicht zugestellte
   * Nachrichten zurückgegeben. false, es werden alle Laufzettel zurückgegeben
   * (default). <b>Diese Einstellung ist nur in den Selection-Modes
   * SELECT_BY_DATE_OF_RECEPTION und SELECT_BY_RECENT_MODIFICATION wirksam.</b>
   *
   * @see #setSelectionRule(String)
   * @see #setSelectionMode(int)
   * @see #setRoleForSelection(int)
   */
  public void setSelectNoReceptionOnly(boolean noReceptionOnly)
  {
    this.selectNoReceptionOnly = noReceptionOnly;
  }

  /**
   * Gibt an, ob Liefert den Identifier für das Auswahlkriterium, ob nur Laufzettel von
   * Nachrichten zurückgegeben werden sollen, die an den oder vom Absender des
   * Laufzettelabholauftrags geschickt wurden.
   *
   * @see #setRoleForSelection(int)
   * @return Modus
   */
  public boolean isSelectNoReception()
  {
    return selectNoReceptionOnly;
  }

  /**
   * Liefert den gesetzten Auswahlmodus.
   * @return den Auswahlmodus SELECT_BY_MESSAGE_ID, SELECT_BY_DATE_OF_RECEPTION,
   * SELECT_BY_RECENT_MODIFICATION oder NO_SELECTION_RULE
   * @see #setSelectionRule(String)
   * @see #setSelectionMode(int)
   */
  public int getSelectionMode()
  {
    return selectionMode;
  }

  /**
   * Liefert die maximale Anzahl zurückzugebender Laufzettel.
   * @return gesetzte maximale Anzahl
   */
  public long getQuantityLimit()
  {
    return quantityLimit;
  }

  /**
   * Legt die maximale Anzahl zurückzugebender Laufzettel fest.
   * @param quantityLimit gewünschte maximale Anzahl
   */
  public void setQuantityLimit(long quantityLimit)
  {
    if (quantityLimit <= 0)
      throw new IllegalArgumentException("Limit für die Zahl der Laufzettel muss größer 0 sein.");

    this.quantityLimit = quantityLimit;
  }

  /**
   * undocumented
   *
   * @throws IOException undocumented
   * @throws OSCIRoleException undocumented
   * @throws NoSuchAlgorithmException Algorithmus Fehler
   */
  protected void compose() throws IOException,
                                  OSCIException,
                                  NoSuchAlgorithmException
  {
    super.compose();
    if(featureDescription!=null && dialogHandler.isSendFeatureDescription())
    {
      messageParts.add(featureDescription);
    }
    messageParts.addAll(customHeaders);

    if (dialogHandler.getControlblock().getChallenge() == null)
      throw new IllegalStateException(DialogHandler.text.getString(LanguageTextEntries.missing_entry.name()) + ": Challenge");

    if (dialogHandler.getControlblock().getResponse() == null)
      throw new IllegalStateException(DialogHandler.text.getString(LanguageTextEntries.missing_entry.name()) + ": Response");

    if (dialogHandler.getControlblock().getConversationID() == null)
      throw new IllegalStateException(DialogHandler.text.getString(LanguageTextEntries.missing_entry.name()) + ": Conversation-Id");

    if (dialogHandler.getControlblock().getSequenceNumber() == -1)
      throw new IllegalStateException(DialogHandler.text.getString(LanguageTextEntries.missing_entry.name()) + ": SequenceNumber");

    String selectionAttributes = "";

    if (selectNoReceptionOnly)
      selectionAttributes += " NoReception=\"true\"";

    if (roleForSelection == SELECT_ADDRESSEE)
      selectionAttributes += " Role=\"Addressee\"";
    else if (roleForSelection == SELECT_ORIGINATOR)
      selectionAttributes += " Role=\"Originator\"";

    StringBuffer selection = new StringBuffer("<");
    selection.append(osciNSPrefix);
    selection.append(":SelectionRule>");

    if (selectionMode == SELECT_BY_MESSAGE_ID)
    {
      String[] msgIds = selectionRule.split(";");

      for (int i = 0; i < msgIds.length; i++)
      {
        selection.append("<");
        selection.append(osciNSPrefix);
        selection.append(":MessageId>");
        selection.append(de.osci.helper.Base64.encode(msgIds[i].getBytes(Constants.CHAR_ENCODING)));
        selection.append("</");
        selection.append(osciNSPrefix);
        selection.append(":MessageId>");
      }

      selection.append("</");
    }
    else if (selectionMode == SELECT_BY_DATE_OF_RECEPTION)
    {
      selection.append("<");
      selection.append(osciNSPrefix);
      selection.append(":ReceptionOfDelivery" + selectionAttributes + ">" + selectionRule + "</");
      selection.append(osciNSPrefix);
      selection.append(":ReceptionOfDelivery></");
    }
    else if (selectionMode == SELECT_BY_RECENT_MODIFICATION)
    {
      selection.append("<");
      selection.append(osciNSPrefix);
      selection.append(":RecentModification" + selectionAttributes + ">" + selectionRule + "</");
      selection.append(osciNSPrefix);
      selection.append(":RecentModification></");
    }
    else
      selection.delete(0, selection.length());

    if (selection.length() > 0)
    {
      selection.append(osciNSPrefix);
      selection.append(":SelectionRule>");
    }

    if (quantityLimit >= 0)
    {
      selection.append("<");
      selection.append(osciNSPrefix);
      selection.append(":Quantity Limit=\"" + quantityLimit + "\"></");
      selection.append(osciNSPrefix);
      selection.append(":Quantity>");
    }

    body = new Body("<" + osciNSPrefix + ":fetchProcessCard>" + selection + "</" + osciNSPrefix + ":fetchProcessCard>");
    body.setNSPrefixes(this);
    messageParts.add(body);
    stateOfMsg |= STATE_COMPOSED;
  }
}
