package de.osci.osci12.messagetypes;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import de.osci.helper.Tools;
import de.osci.osci12.OSCIException;
import de.osci.osci12.common.Constants.LanguageTextEntries;
import de.osci.osci12.common.DialogHandler;
import de.osci.osci12.messageparts.Body;
import de.osci.osci12.roles.OSCIRoleException;


/**
 * <p>Dieses Klasse repräsentiert die Antwortnachricht auf die Initialisierung
 * eines expliziten Dialogs.
 * Clients erhalten vom Intermediär eine Instanz dieser Klasse als Antwort auf
 * ein an den Intermediär gesendetes InitDialog-Objekt. Da der an den Konstruktor
 * des InitDialog-Objektes übergebene DialogHandler bereits beim Empfang der
 * Antwortnachricht mit den empfangenen Parametern aktualisiert wird, ist dieses
 * Objekt für Client-Anwendungen normalerweise ohne Bedeutung.</p>
 * <p>Copyright © 2023 Governikus GmbH &amp; Co. KG, Germany</p>
 * <p>Erstellt von Governikus GmbH &amp; Co. KG</p>
 * <p>Diese Bibliothek kann von jedermann nach Maßgabe der European Union
 *  Public Licence genutzt werden.</p><p>Die Lizenzbestimmungen können
 * unter der URL <a href="https://eupl.eu/">https://eupl.eu/</a> abgerufen werden.
 *
 *
 * @author R. Lindemann, N. Büngener
 * @version 2.4.1
 * @see de.osci.osci12.messagetypes.InitDialog
 * @see de.osci.osci12.common.DialogHandler
 * @see #getDialogHandler()
 */
public class ResponseToInitDialog extends OSCIResponseTo
{
  //  private static Log log = LogFactory.getLog(ResponseToInitDialog.class);
  /**
   * Creates a new ResponseToInitDialog object.
   *
   * @param dh undocumented
   */
  protected ResponseToInitDialog(DialogHandler dh) // throws OSCIRoleException
  {
    super(dh);
    messageType = RESPONSE_TO_INIT_DIALOG;
    dialogHandler.explicitDialog = true;
  }

  /**
   * Creates a new ResponseToInitDialog object.
   *
   * @param iD undocumented
   *
   * @throws NoSuchAlgorithmException undocumented
   */
  protected ResponseToInitDialog(InitDialog iD) throws NoSuchAlgorithmException
  {
    super(iD.dialogHandler);
    messageType = RESPONSE_TO_INIT_DIALOG;
    dialogHandler.getControlblock().setResponse(dialogHandler.prevChallenge);
    dialogHandler.getControlblock().setChallenge(Tools.createRandom(10));
    dialogHandler.getControlblock().setSequenceNumber(-1);
    setFeedback(new String[] { "0801" });
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

    if (feedBack == null)
      throw new IllegalStateException(DialogHandler.text.getString(LanguageTextEntries.missing_entry.name()) + ": Feedback");

    StringBuffer bd = new StringBuffer("<");
    bd.append(osciNSPrefix);
    bd.append(":responseToInitDialog>");
    bd.append(writeFeedBack());
    bd.append("</");
    bd.append(osciNSPrefix);
    bd.append(":responseToInitDialog>");
    body = new Body(bd.toString());
    body.setNSPrefixes(this);
    dialogHandler.explicitDialog = true;
    messageParts.add(body);
    stateOfMsg |= STATE_COMPOSED;
  }
}
