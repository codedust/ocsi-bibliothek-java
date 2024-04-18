package de.osci.osci12.messagetypes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import de.osci.helper.Tools;
import de.osci.osci12.OSCIException;
import de.osci.osci12.common.Constants;
import de.osci.osci12.common.Constants.LanguageTextEntries;
import de.osci.osci12.common.DialogHandler;
import de.osci.osci12.messageparts.Body;
import de.osci.osci12.messageparts.MessagePartsFactory;
import de.osci.osci12.messageparts.ProcessCardBundle;
import de.osci.osci12.roles.Originator;


/**
 * <p>Dieses Klasse repräsentiert die Antwort des Intermediärs auf einen
 * Laufzettelabholauftrag.
 * Clients erhalten vom Intermediär eine Instanz dieser Klasse, die eine Rückmeldung
 * über den Erfolg der Operation (getFeedback()) sowie ggf. die
 * angeforderten Laufzettel enthält.</p>
 * <p>Copyright © 2023 Governikus GmbH &amp; Co. KG, Germany</p>
 * <p>Erstellt von Governikus GmbH &amp; Co. KG</p>
 * <p>Diese Bibliothek kann von jedermann nach Maßgabe der European Union
 *  Public Licence genutzt werden.</p><p>Die Lizenzbestimmungen können
 * unter der URL <a href="https://eupl.eu/">https://eupl.eu/</a> abgerufen werden.
 *
 *
 * @author R. Lindemann, N. Büngener
 * @version 2.4.1
 * @see de.osci.osci12.messagetypes.FetchProcessCard
 */
public class ResponseToFetchProcessCard extends OSCIResponseTo
{
  //  private static Log log = LogFactory.getLog(ResponseToFetchProcessCard.class);
  private String selectionRule;
  private int selectionMode = -1;
  private int selectionRole = -1;
  private boolean selectNoReceptionOnly = false;
  private long quantityLimit = -1;
  ProcessCardBundle[] processCardBundles = new ProcessCardBundle[0];

  ResponseToFetchProcessCard(DialogHandler dh)
  {
    super(dh);
    messageType = RESPONSE_TO_FETCH_PROCESS_CARD;
    originator = ((Originator) dialogHandler.getClient());
  }

  ResponseToFetchProcessCard(FetchProcessCard fpc) throws java.security.NoSuchAlgorithmException
  {
    super(fpc.dialogHandler);
    originator = ((Originator) dialogHandler.getClient());
    messageType = RESPONSE_TO_FETCH_PROCESS_CARD;
    setSelectionMode(fpc.getSelectionMode());
    setSelectionRule(fpc.getSelectionRule());
    setRoleForSelection(fpc.getRoleForSelection());
    setSelectNoReceptionOnly(fpc.isSelectNoReception());

    if (fpc.getQuantityLimit() > 0)
      setQuantityLimit(fpc.getQuantityLimit());

    dialogHandler.getControlblock().setResponse(dialogHandler.prevChallenge);
    dialogHandler.getControlblock().setChallenge(Tools.createRandom(10));
  }

  /**
   * Liefert die angeforderten Laufzettel als Array von ProcessCardBundle-Objekten.
   * @return Laufzettel
   * @see de.osci.osci12.messageparts.ProcessCardBundle
   */
  public ProcessCardBundle[] getProcessCardBundles()
  {
    return processCardBundles;
  }

  void setSelectionRule(String selectionRule)
  {
    this.selectionRule = selectionRule;
  }

  /**
   * Liefert die gesetzte Auswahlregel. Der Inhalt des zurückgegebenen
   * Strings hängt vom gesetzten Auswahlmodus ab und kann
   * entweder in einer oder mehrerer Message-ID oder einem Datum bestehen.
   * Mehrere Message-IDs werden aneinandergereiht durch ";" getrennt zurückgegeben.
   * Das Format eines Datums entspricht dem XML-Schema nach
   * <a href="http://www.w3.org/TR/xmlschema-2/#dateTime">
   * http://www.w3.org/TR/xmlschema-2/#dateTime</a>.
   * Wurde keine Regel gesetzt,
   * wird als default null zurückgegeben.
   * @return Auswahlregel (Message-ID oder Datum)
   * @see FetchProcessCard#getSelectionRule()
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
   * @return den Auswahlmodus SELECT_BY_MESSAGE_ID, SELECT_BY_DATE_OF_RECEPTION,
   * SELECT_BY_RECENT_MODIFICATION oder NO_SELECTION_RULE
   * @see FetchProcessCard#getSelectionMode()
   */
  public int getSelectionMode()
  {
    return selectionMode;
  }

  void setRoleForSelection(int role)
  {
    this.selectionRole = role;
  }

  /**
   * Liefert den Identifier für das Auswahlkriterium, ob nur Laufzettel von
   * Nachrichten zurückgegeben werden sollen, die an den oder vom Absender des
   * Laufzettelabholauftrags geschickt wurden.
   *
   * @see FetchProcessCard#setRoleForSelection(int)
   * @return Modus
   */
  public int getRoleForSelection()
  {
    return selectionRole;
  }

  void setSelectNoReceptionOnly(boolean noReceptionOnly)
  {
    this.selectNoReceptionOnly = noReceptionOnly;
  }

  /**
   * Gibt an, ob Liefert den Identifier für das Auswahlkriterium, ob nur Laufzettel von
   * Nachrichten zurückgegeben werden sollen, die an den oder vom Absender des
   * Laufzettelabholauftrags geschickt wurden.
   *
   * @see FetchProcessCard#setRoleForSelection(int)
   * @return Modus
   */
  public boolean isSelectNoReception()
  {
    return selectNoReceptionOnly;
  }

  /**
   * Liefert die maximale Anzahl angeforderter Laufzettel.
   * @return maximale Anzahl
   */
  public long getQuantityLimit()
  {
    return quantityLimit;
  }

  void setQuantityLimit(long quantityLimit)
  {
    if (quantityLimit <= 0)
      throw new IllegalArgumentException(DialogHandler.text.getString(LanguageTextEntries.invalid_firstargument.name()) + ": " + quantityLimit);

    this.quantityLimit = quantityLimit;
  }

  /**
   * undocumented
   *
   * @throws OSCIException undocumented
   * @throws IOException undocumented
   */
  protected void compose() throws OSCIException,
                                  IOException,
                                  NoSuchAlgorithmException
  {
    super.compose();
    if(featureDescription!=null && dialogHandler.isSendFeatureDescription())
    {
      messageParts.add(featureDescription);
    }
    messageParts.addAll(customHeaders);

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    bos.write(0x3c);
    bos.write(osciNSPrefix.getBytes(Constants.CHAR_ENCODING));
    bos.write(":responseToFetchProcessCard>".getBytes(Constants.CHAR_ENCODING));
    bos.write(writeFeedBack().getBytes(Constants.CHAR_ENCODING));
    bos.write(0x3c);
    bos.write(osciNSPrefix.getBytes(Constants.CHAR_ENCODING));
    bos.write(":fetchProcessCard>".getBytes(Constants.CHAR_ENCODING));

    String selectionAttributes = "";

    if (selectNoReceptionOnly)
      selectionAttributes += " NoReception=\"true\"";

    if (selectionRole == SELECT_ADDRESSEE)
      selectionAttributes += " Role=\"Addressee\"";
    else if (selectionRole == SELECT_ORIGINATOR)
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

    bos.write(selection.toString().getBytes(Constants.CHAR_ENCODING));
    bos.write(("</" + osciNSPrefix + ":fetchProcessCard>").getBytes(Constants.CHAR_ENCODING));

    for (int i = 0; i < processCardBundles.length; i++)
    {
      MessagePartsFactory.writeXML(processCardBundles[i], bos);
    }

    bos.write(("</" + osciNSPrefix + ":responseToFetchProcessCard>").getBytes(Constants.CHAR_ENCODING));
    body = new Body(bos.toString(Constants.CHAR_ENCODING));
    body.setNSPrefixes(this);
    messageParts.add(body);
    stateOfMsg |= STATE_COMPOSED;
  }
}
