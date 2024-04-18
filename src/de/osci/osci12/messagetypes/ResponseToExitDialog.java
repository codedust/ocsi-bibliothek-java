package de.osci.osci12.messagetypes;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import de.osci.osci12.OSCIException;
import de.osci.osci12.common.Constants.LanguageTextEntries;
import de.osci.osci12.common.DialogHandler;
import de.osci.osci12.messageparts.Body;
import de.osci.osci12.roles.OSCIRoleException;
import de.osci.osci12.roles.Originator;


/**
 * <p>Diese Klasse repräsentiert die Antwortnachricht auf die Beendigung
 * eines expliziten Dialogs.
 * Clients erhalten vom Intermediär eine Instanz dieser Klasse als Antwort auf
 * ein an den Intermediär gesendetes ExitDialog-Objekt.</p>
 * <p>Copyright © 2023 Governikus GmbH &amp; Co. KG, Germany</p>
 * <p>Erstellt von Governikus GmbH &amp; Co. KG</p>
 * <p>Diese Bibliothek kann von jedermann nach Maßgabe der European Union
 *  Public Licence genutzt werden.</p><p>Die Lizenzbestimmungen können
 * unter der URL <a href="https://eupl.eu/">https://eupl.eu/</a> abgerufen werden.
 *
 *
 * @author R. Lindemann, N. Büngener
 * @version 2.4.1
 * @see de.osci.osci12.messagetypes.ExitDialog
 * @see de.osci.osci12.common.DialogHandler
 */
public class ResponseToExitDialog extends OSCIResponseTo
{
  //  private static Log log = LogFactory.getLog(ResponseToExitDialog.class);
  ResponseToExitDialog(DialogHandler dh)
  {
    super(dh);
    messageType = RESPONSE_TO_EXIT_DIALOG;
    originator = ((Originator) dh.getClient());
    // Challenge wird in compose() zu null gesetzt.
    dialogHandler.getControlblock().setResponse(dialogHandler.prevChallenge);
  }

  ResponseToExitDialog()
  {
    super();
    messageType = RESPONSE_TO_EXIT_DIALOG;
    dialogHandler.explicitDialog = false;
  }

  /**
   * undocumented
   *
   * @throws OSCIRoleException undocumented
   * @throws IllegalStateException undocumented
   */
  protected void compose() throws OSCIException,
                                  NoSuchAlgorithmException,
                                  IOException
  {
    super.compose();
    if(featureDescription!=null && dialogHandler.isSendFeatureDescription())
    {
      messageParts.add(featureDescription);
    }
    messageParts.addAll(customHeaders);

    if (dialogHandler.getControlblock().getResponse() == null)
      throw new IllegalStateException(DialogHandler.text.getString(LanguageTextEntries.missing_entry.name()) + ": Response");

    if (dialogHandler.getControlblock().getConversationID() == null)
      throw new IllegalStateException(DialogHandler.text.getString(LanguageTextEntries.missing_entry.name()) + ": Conversation-Id");

    if (dialogHandler.getControlblock().getSequenceNumber() == -1)
      throw new IllegalStateException(DialogHandler.text.getString(LanguageTextEntries.missing_entry.name()) + ": SequenceNumber");

    dialogHandler.getControlblock().setChallenge(null);
    dialogHandler.explicitDialog = false;

    if (feedBack == null)
      throw new IllegalStateException(DialogHandler.text.getString(LanguageTextEntries.missing_entry.name()) + ": Feedback");

    body = new Body("<" + osciNSPrefix + ":responseToExitDialog>" + writeFeedBack() + "</" + osciNSPrefix +
                    ":responseToExitDialog>");
    body.setNSPrefixes(this);
    messageParts.add(body);
    stateOfMsg |= STATE_COMPOSED;
  }
}
