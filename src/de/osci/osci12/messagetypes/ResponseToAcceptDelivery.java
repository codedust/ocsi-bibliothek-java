package de.osci.osci12.messagetypes;

import java.io.IOException;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.util.Vector;

import de.osci.helper.StoreOutputStream;
import de.osci.osci12.OSCIException;
import de.osci.osci12.common.Constants.HeaderTags;
import de.osci.osci12.common.DialogHandler;
import de.osci.osci12.messageparts.Body;
import de.osci.osci12.roles.Addressee;
import de.osci.osci12.roles.OSCIRoleException;
import de.osci.osci12.soapheader.OsciH;


/**
 * <p>Mit dieser Klasse werden Nachrichtenobjekte für Annahmeantworten
 * angelegt. Ein passiver Client, der als Supplier fungiert, muss nach Empfang
 * eines Annahmeauftrags eine Instanz dieser Klasse aufbauen und an den Intermediär
 * zurücksenden. Die Nachricht enthält inhaltlich lediglich eine Rückmeldung über
 * den Empfang der Nachricht (Feedback).</p>
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
 * @see de.osci.osci12.messagetypes.AcceptDelivery
 */
public class ResponseToAcceptDelivery extends OSCIResponseTo
{
  //  private static Log log = LogFactory.getLog(ResponseToAcceptDelivery.class);
  /**
   * Legt ein Nachrichtenobjekt für eine Annahmeantwort an.
   *
   * @param request Auftragsnachricht
   */
  public ResponseToAcceptDelivery(AcceptDelivery request)
  {
    super(request.getDialogHandler());
    dialogHandler.getControlblock().setResponse(dialogHandler.prevChallenge);
    // Hier wurde die Spec korrigiert
    dialogHandler.getControlblock().setChallenge(null);
    dialogHandler.getControlblock().setConversationID(null);
    dialogHandler.getControlblock().setSequenceNumber(-1);
    messageType = RESPONSE_TO_ACCEPT_DELIVERY;
  }

  ResponseToAcceptDelivery(DialogHandler dh)
  {
    super(dh);
    addressee = ((Addressee) dh.getSupplier());
    messageType = RESPONSE_TO_ACCEPT_DELIVERY;
  }

  /**
   * Setzt die Rückmeldungen (Fehler und Warnungen) auf Auftragsebene.
   * @param code Array mit Fehlercodes
   */
  public void setFeedback(String[] code)
  {
    super.setFeedback(code);
  }

 /**
  *
  * /**
   * Setzt die Rückmeldungen (Feedback-Einträge) mit frei wählbaren Texten und Sprachen.
   * Der erste Index des Arrays entspricht dem Index des Entry-Elementes.
   * Beim zweiten Index bezeichnet <br>
   * @param entries
   * 0 - das Sprachkürzel (z.B. "de", "en-US")<br>
   * 1 - den Code<br>
   * 2 - den Text<br>
   */
  public void setFeedback(String[][] entries)
  {
    feedBack = new Vector<String[]>();
    for (int i = 0; i < entries.length; i++)
      feedBack.add(entries[i]);
  }

  /**
   * Bringt eine Supplier-Signatur an.
   * @throws IOException bei Schreib-/Leseproblemen
   * @throws OSCIRoleException wenn dem Rollenobjekt, das als Client fungiert,
   * kein Signer-Objekt zugeordnet wurde
   * @throws de.osci.osci12.common.OSCICancelledException bei Abbruch durch den
   * Benutzer
   */
  public void sign()
            throws IOException,
                   OSCIException,
                   de.osci.osci12.common.OSCICancelledException,
                   java.security.NoSuchAlgorithmException
  {
    super.sign(dialogHandler.getSupplier());
    messageParts.set(4, nonIntermediaryCertificatesH);
  }

  /**
   * undocumented
   *
   * @throws OSCIRoleException undocumented
   */
   protected void compose() throws OSCIException,
                                  NoSuchAlgorithmException,
                               IOException
  {
    super.compose();
    osciH = new OsciH(HeaderTags.responseToAcceptDelivery.getElementName(), writeFeedBack());
    messageParts.add(osciH);
    createNonIntermediaryCertificatesH();
    messageParts.add(nonIntermediaryCertificatesH);
    if(featureDescription!=null && dialogHandler.isSendFeatureDescription())
    {
      messageParts.add(featureDescription);
    }
    messageParts.addAll(customHeaders);
    body = new Body("");
    body.setNSPrefixes(this);
    messageParts.add(body);
    stateOfMsg |= STATE_COMPOSED;
  }

  /**
   * Serialisiert und schreibt die Nachricht - ggf. verschlüsselt - in den
   * übergebenen Stream. Es wird abhängig vom isEncryption()-Flag des
   * DialogHandlers verschlüsselt oder nicht, Signaturen müssen jedoch von der
   * Anwendung selbst vor Aufruf dieser Methode (sign()) angebracht werden.
   * Die ausgehende Nachricht kann zu Debug- oder Archivierungszwecken (in jedem Fall
   * unverschlüsselt) in den zweiten übergebenen Stream geschrieben werden.
   * Dieser Parameter kann null sein.
   * @param out Stream, in den die Antwortnachricht geschrieben werden soll
   * @param storeOutput Stream, in dem die (unverschlüsselte) Antwortnachricht
   * gespeichert werden soll
   * @throws OSCIRoleException wenn erforderliche Zertifikate fehlen
   * @throws IOException bei Schreibproblemen
   * @throws java.security.NoSuchAlgorithmException wenn ein benötigter
   * Algorithmus nicht unterstützt wird
   * @see #sign()
   */
  public void writeToStream(OutputStream out, OutputStream storeOutput)
                          throws IOException,
                            de.osci.osci12.OSCIException,
                       NoSuchAlgorithmException
  {
    if (dialogHandler.isEncryption())
      new SOAPMessageEncrypted(this, storeOutput).writeXML(out);
    else if (storeOutput != null)
    {
      StoreOutputStream sos = new StoreOutputStream(out, storeOutput);
      writeXML(sos);
      sos.close();
    }
    else
      writeXML(out);
  }
}
