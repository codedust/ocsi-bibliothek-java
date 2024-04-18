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
 * <p>Mit dieser Klasse werden Nachrichtenobjekte zur Anforderung einer
 * MessageId angelegt. Clients erhalten als Antwort auf diese Nachricht
 * vom Intermediär ein Nachrichtenobjekt, welches eine Rückmeldung über den
 * Erfolg der Operation und ggf. die angeforderte MessageId enthält.</p>
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
 * @see de.osci.osci12.messagetypes.ResponseToGetMessageId
 */
public class GetMessageId extends OSCIRequest
{
  //  private static Log log = LogFactory.getLog(GetMessageId.class);
  /**
   * Legt ein Nachrichtenobjekt zur Anforderung einer MessageId an.
   *
   * @param dh DialogHandler-Objekt des Dialogs, innerhalb dessen die Nachricht
   * versendet werden soll
   *
   * @see de.osci.osci12.common.DialogHandler
   * @throws NoSuchAlgorithmException wenn der verwendete Security-Provider
   * einen erforderlichen Algorithmus nicht unterstützt (Erzeugung einer Zufallszahl)
   */
  public GetMessageId(DialogHandler dh) throws NoSuchAlgorithmException
  {
    super(dh);
    messageType = GET_MESSAGE_ID;
    originator = ((Originator) dh.getClient());

    if (!dialogHandler.isExplicitDialog())
      dialogHandler.resetControlBlock();

    dialogHandler.getControlblock().setResponse(dialogHandler.prevChallenge);
    dialogHandler.getControlblock().setChallenge(Tools.createRandom(10));
    dialogHandler.getControlblock().setSequenceNumber(dialogHandler.getControlblock().getSequenceNumber() + 1);
  }

  GetMessageId()
  {
    super();
    messageType = GET_MESSAGE_ID;
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
   * einen erforderlichen Algorithmus nicht unterstützt (Erzeugung einer Zufallszahl)
   * @return Antwortnachricht-Objekt
   */
  public ResponseToGetMessageId send() throws IOException,
                                              de.osci.osci12.OSCIException,
                                              NoSuchAlgorithmException
  {
    return (ResponseToGetMessageId) transmit(null, null);
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
    if (dialogHandler.getClient().hasCipherCertificate())
    {
      if (nonIntermediaryCertificatesH == null)
        nonIntermediaryCertificatesH = new NonIntermediaryCertificatesH();

      nonIntermediaryCertificatesH.setCipherCertificateOriginator((Originator) dialogHandler.getClient());
    }

    super.compose();
    if(featureDescription!=null && dialogHandler.isSendFeatureDescription())
    {
      messageParts.add(featureDescription);
    }
    messageParts.addAll(customHeaders);

    if (dialogHandler.getControlblock().getChallenge() == null)
      throw new IllegalStateException(DialogHandler.text.getString(LanguageTextEntries.missing_entry.name()) + ": Challenge");

    if (dialogHandler.getControlblock().getSequenceNumber() == -1)
      throw new IllegalStateException(DialogHandler.text.getString(LanguageTextEntries.missing_entry.name()) + ": SequenceNumber");

    body = new Body("<" + osciNSPrefix + ":getMessageId></" + osciNSPrefix + ":getMessageId>");
    body.setNSPrefixes(this);
    messageParts.add(body);
    stateOfMsg |= STATE_COMPOSED;
  }
}
