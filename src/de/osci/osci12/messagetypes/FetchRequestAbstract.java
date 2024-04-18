package de.osci.osci12.messagetypes;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import de.osci.helper.Base64;
import de.osci.osci12.common.Constants;
import de.osci.osci12.common.DialogHandler;
import de.osci.osci12.common.Constants.LanguageTextEntries;

/**
 * <p>Die Klasse ist die Superklasse der AbholAuftrags-Objekte.</p>
 *
 * <p>Copyright © 2023 Governikus GmbH &amp; Co. KG, Germany</p>
 * <p>Erstellt von Governikus GmbH &amp; Co. KG</p>
 * <p>Diese Bibliothek kann von jedermann nach Maßgabe der European Union
 *  Public Licence genutzt werden.</p><p>Die Lizenzbestimmungen können
 * unter der URL <a href="https://eupl.eu/">https://eupl.eu/</a> abgerufen werden.
 *
 *
 * @author A. Mergenthal
 * @version 2.4.1
 *
 * @see de.osci.osci12.messagetypes.ResponseToFetchDelivery
 */
public class FetchRequestAbstract extends OSCIRequest
{
  private String selectionRule = null;
  private int selectionMode = -1;

  FetchRequestAbstract(){}


  public FetchRequestAbstract(DialogHandler dh)
  {
    super(dh);
  }


  /**
   * Setzt die Auswahlregel für die abzuholende Nachricht. Der Inhalt des
   * übergebenen Strings hängt vom gewählten Auswahlmodus ab und kann
   * entweder aus einer Message-ID oder einem Datum bestehen.
   * Das Format eines Datums muss dem XML-Schema nach
   * <a href="http://www.w3.org/TR/xmlschema-2/#dateTime">
   * http://www.w3.org/TR/xmlschema-2/#dateTime</a> (ISO 8601-Format) entsprechen.
   *
   * @param selectionRule Message-ID oder Datum
   * @see #setSelectionMode(int)
   * @see #getSelectionRule()
   * @see #formatISO8601(java.util.Date)
   */
  public void setSelectionRule(String selectionRule)
  {
    this.selectionRule = selectionRule;
  }

  /**
   * Liefert die gesetzte Auswahlregel. Wurde keine Regel gesetzt,
   * wird als default null zurückgegeben.
   * @return Auswahlregel (Message-ID oder Datum)
   * @see #setSelectionRule(String)
   * @see #setSelectionMode(int)
   */
  public String getSelectionRule()
  {
    return selectionRule;
  }

  /**
   * Setzt den Auswahlmodus. Mögliche Werte sind
   * SELECT_BY_MESSAGE_ID, SELECT_BY_DATE_OF_RECEPTION oder NO_SELECTION_RULE
   * (default).
   *
   * @param selectionMode Auswahlmodus
   * @see #SELECT_BY_MESSAGE_ID
   * @see #SELECT_BY_DATE_OF_RECEPTION
   * @see #NO_SELECTION_RULE
   * @see #setSelectionRule(String)
   */
  public void setSelectionMode(int selectionMode)
  {
    if ((selectionMode != SELECT_BY_MESSAGE_ID) && (selectionMode != SELECT_BY_DATE_OF_RECEPTION) &&
            (selectionMode != NO_SELECTION_RULE))
      throw new IllegalArgumentException(DialogHandler.text.getString(LanguageTextEntries.invalid_firstargument.name()) + selectionMode);

    this.selectionMode = selectionMode;
  }

  /**
   * Liefert den gesetzten Auswahlmodus.
   * @return Auswahlmodus SELECT_BY_MESSAGE_ID, SELECT_BY_DATE_OF_RECEPTION
   * oder NO_SELECTION_RULE
   * @see #setSelectionMode(int)
   * @see #setSelectionRule(String)
   */
  public int getSelectionMode()
  {
    return selectionMode;
  }

  protected String getSelectionRuleString() throws UnsupportedEncodingException, IOException
  {
    // todo: Evtl. Plausis Selection-Mode / Selection-Rule-Format checken (Datum/MsgID)
    if (getSelectionMode() == SELECT_BY_MESSAGE_ID || getSelectionMode() == SELECT_BY_DATE_OF_RECEPTION)
    {
      if (getSelectionRule() == null)
      {
        throw new IllegalStateException(DialogHandler.text.getString(LanguageTextEntries.missing_entry.name())
                                        + ": SelectionRule");
      }

      StringBuilder selectionBuilder = new StringBuilder("<");
      selectionBuilder.append(osciNSPrefix);
      selectionBuilder.append(":SelectionRule><");
      selectionBuilder.append(osciNSPrefix);

      if (getSelectionMode() == SELECT_BY_MESSAGE_ID)
      {
        selectionBuilder.append(":MessageId>");
        selectionBuilder.append(Base64.encode(getSelectionRule().getBytes(Constants.CHAR_ENCODING)));
        selectionBuilder.append("</");
        selectionBuilder.append(osciNSPrefix);
        selectionBuilder.append(":MessageId></");
      }
      else // getSelectionMode() == SELECT_BY_DATE_OF_RECEPTION
      {
        selectionBuilder.append(":ReceptionOfDelivery>");
        selectionBuilder.append(getSelectionRule());
        selectionBuilder.append("</");
        selectionBuilder.append(osciNSPrefix);
        selectionBuilder.append(":ReceptionOfDelivery></");
      }

      selectionBuilder.append(osciNSPrefix);
      selectionBuilder.append(":SelectionRule>");
      return selectionBuilder.toString();
    }
    else
    {
      return "";
    }
  }

}
