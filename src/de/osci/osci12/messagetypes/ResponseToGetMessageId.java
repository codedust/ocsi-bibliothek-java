package de.osci.osci12.messagetypes;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import de.osci.helper.Base64;
import de.osci.helper.Tools;
import de.osci.osci12.OSCIException;
import de.osci.osci12.common.Constants;
import de.osci.osci12.common.Constants.LanguageTextEntries;
import de.osci.osci12.common.DialogHandler;
import de.osci.osci12.messageparts.Body;
import de.osci.osci12.roles.OSCIRoleException;
import de.osci.osci12.roles.Originator;


/**
 * <p>Dieses Klasse repräsentiert die Antwort des Intermediärs auf die
 * Anforderung einer Message-ID.
 * Clients erhalten vom Intermediär eine Instanz dieser Klasse, die eine Rückmeldung
 * über den Erfolg der Operation (getFeedback()) sowie ggf. die angeforderte
 * Message-ID.</p>
 * <p>Copyright © 2023 Governikus GmbH &amp; Co. KG, Germany</p>
 * <p>Erstellt von Governikus GmbH &amp; Co. KG</p>
 * <p>Diese Bibliothek kann von jedermann nach Maßgabe der European Union
 *  Public Licence genutzt werden.</p><p>Die Lizenzbestimmungen können
 * unter der URL <a href="https://eupl.eu/">https://eupl.eu/</a> abgerufen werden.
 *
 *
 * @author R. Lindemann, N. Büngener
 * @version 2.4.1
 * @see de.osci.osci12.messagetypes.GetMessageId
 */
public class ResponseToGetMessageId extends OSCIResponseTo
{
  //  private static Log log = LogFactory.getLog(ResponseToGetMessageId.class);
  ResponseToGetMessageId(DialogHandler dh, boolean parser)
                  throws NoSuchAlgorithmException
  {
    super(dh);
    messageType = RESPONSE_TO_GET_MESSAGE_ID;
    originator = ((Originator) dh.getClient());

    if (!parser)
    {
      dialogHandler.getControlblock().setResponse(dialogHandler.prevChallenge);
      dialogHandler.getControlblock().setChallenge(Tools.createRandom(10));
    }
  }

  ResponseToGetMessageId(DialogHandler dh) throws NoSuchAlgorithmException
  {
    this(dh, false);
  }

  /**
   * Liefert die angeforderte Message-ID. Wenn der Auftrag nicht
   * ordnungsgemäß abgewickelt wurde, wird <b>null</b> zurückgegeben.
   * In disem Fall sollte der Feedback-Eintrag ausgewertet werden.
   * @return Message-ID bzw. null im Fehlerfall
   * @see #getFeedback()
   */
  public String getMessageId()
  {
    return messageId;
  }

  void setMessageId(String messageId)
  {
    this.messageId = messageId;
  }

  /**
   * undocumented
   *
   * @throws OSCIRoleException undocumented
   * @throws IOException undocumented
   * @throws IllegalStateException undocumented
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

    if (dialogHandler.getControlblock().getSequenceNumber() == -1)
      throw new IllegalStateException(DialogHandler.text.getString(LanguageTextEntries.missing_entry.name()) + ": SequenceNumber");

    if (feedBack == null)
      throw new IllegalStateException(DialogHandler.text.getString(LanguageTextEntries.missing_entry.name()) + ": Feedback");

    StringBuffer bd = new StringBuffer("<");
    bd.append(osciNSPrefix);
    bd.append(":responseToGetMessageId>");
    bd.append(writeFeedBack());

    if ((messageId != null) && (messageId.length() > 0))
    {
      bd.append("<");
      bd.append(osciNSPrefix);
      bd.append(":MessageId>");
      bd.append(Base64.encode(messageId.getBytes(Constants.CHAR_ENCODING)));
      bd.append("</");
      bd.append(osciNSPrefix);
      bd.append(":MessageId>");
    }

    bd.append("</");
    bd.append(osciNSPrefix);
    bd.append(":responseToGetMessageId>");
    body = new Body(bd.toString());
    body.setNSPrefixes(this);
    messageParts.add(body);
    stateOfMsg |= STATE_COMPOSED;
  }
}
