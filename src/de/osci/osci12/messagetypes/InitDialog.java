package de.osci.osci12.messagetypes;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import de.osci.helper.Tools;
import de.osci.osci12.OSCIException;
import de.osci.osci12.common.Constants.LanguageTextEntries;
import de.osci.osci12.common.DialogHandler;
import de.osci.osci12.messageparts.Body;
import de.osci.osci12.roles.OSCIRoleException;
import de.osci.osci12.roles.Originator;
import de.osci.osci12.soapheader.NonIntermediaryCertificatesH;


/**
 * <p>
 *  Diese Klasse dient der Initialisierung eines expliziten Dialogs. Clients
 *  erhalten als Antwort auf diese Nachricht vom Intermediär ein
 *  Nachrichtenobjekt, welches in seinem ControlBlock die angeforderte
 *  ConversationId enthält. Diese Id wird beim Empfang der Antwort an das
 *  verwendete DialogHandler-Objekt übergeben. Der Client muss lediglich dieses
 *  DialogHandler-Objekt für alle weiteren Nachrichten verwenden, die innerhalb
 *  dieses Dialogs behandelt werden sollen.</p>
 *
 * <p>Copyright © 2023 Governikus GmbH &amp; Co. KG, Germany</p>
 * <p>Erstellt von Governikus GmbH &amp; Co. KG</p>
 * <p>Diese Bibliothek kann von jedermann nach Maßgabe der European Union
 *  Public Licence genutzt werden.</p><p>Die Lizenzbestimmungen können
 * unter der URL <a href="https://eupl.eu/">https://eupl.eu/</a> abgerufen werden.
 *
 *
 * @author     R. Lindemann, N. Büngener
 * @version 2.4.1
 * @see        de.osci.osci12.common.DialogHandler
 */
public class InitDialog extends OSCIRequest
{
  //  private static Log log = LogFactory.getLog(InitDialog.class);
  /**
   *  Legt ein Nachrichtenobjekt zur Dialoginitialisierung an.
   *
   *@param  dh DialogHandler-Objekt, welches für die
   *      folgenden Nachrichten initialisiert werden soll.
   * @throws NoSuchAlgorithmException wenn der verwendete Security-Provider
   * einen erforderlichen Algorithmus nicht unterstützt
   *@see de.osci.osci12.common.DialogHandler
   */
  public InitDialog(DialogHandler dh) throws NoSuchAlgorithmException
  {
    super(dh);
    dh.getControlblock().setChallenge(Tools.createRandom(10));
    dh.getControlblock().setConversationID(null);
    dh.getControlblock().setSequenceNumber(-1);
    dh.getControlblock().setResponse(null);
    messageType = INIT_DIALOG;
    originator = ((Originator) dh.getClient());
  }

  /*
   *  Diser Konstruktor wird nur für die parser benötigt
   */
  InitDialog()
  {
    messageType = INIT_DIALOG;
  }

  /**
   * Versendet die Nachricht und liefert die Antwortnachricht zurück.
   * Diese Methode wirft eine Exception, wenn beim Aufbau oder Versand der
   * Nachricht ein Fehler auftritt. Fehlermeldungen vom Intermediär müssen
   * dem Feedback der Antwortnachricht entnommen werden.
   *
   * @throws IOException bei Ein-/Ausgabefehlern
   * @throws de.osci.osci12.OSCIException bei OSCI-Fehlern
   * @throws NoSuchAlgorithmException wenn der verwendete Security-Provider
   * einen erforderlichen Algorithmus nicht unterstützt
   * @return Antwortnachricht-Objekt
   */
  public ResponseToInitDialog send() throws IOException,
                                            de.osci.osci12.OSCIException,
                                            NoSuchAlgorithmException
  {
    return (ResponseToInitDialog) transmit(null, null);
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
    if (nonIntermediaryCertificatesH == null)
      nonIntermediaryCertificatesH = new NonIntermediaryCertificatesH();

    super.compose();
    if(featureDescription!=null && dialogHandler.isSendFeatureDescription())
    {
      messageParts.add(featureDescription);
    }
    messageParts.addAll(customHeaders);
    nonIntermediaryCertificatesH.setCipherCertificateOriginator((Originator) dialogHandler.getClient());
    body = new Body("<" + osciNSPrefix + ":initDialog></" + osciNSPrefix + ":initDialog>");
    body.setNSPrefixes(this);
    messageParts.add(body);

    // Zur Sicherheit nochmal aufräumen
    if (dialogHandler.getControlblock().getChallenge() == null)
    {
      throw new IllegalStateException(DialogHandler.text.getString(LanguageTextEntries.missing_entry.name()) + ": Challenge");
    }

    stateOfMsg |= STATE_COMPOSED;
  }
}
