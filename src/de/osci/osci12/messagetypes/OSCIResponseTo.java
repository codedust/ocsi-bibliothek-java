package de.osci.osci12.messagetypes;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Vector;

import de.osci.osci12.OSCIException;
import de.osci.osci12.common.DialogHandler;
import de.osci.osci12.messageparts.FeedbackObject;
import de.osci.osci12.messageparts.MessagePartsFactory;


/**
 * Die Klasse ist die Superklasse aller OSCI-Antwortnachrichtenobjekte.
 *
 * <p>Copyright © 2023 Governikus GmbH &amp; Co. KG, Germany</p>
 * <p>Erstellt von Governikus GmbH &amp; Co. KG</p>
 * <p>Diese Bibliothek kann von jedermann nach Maßgabe der European Union
 *  Public Licence genutzt werden.</p><p>Die Lizenzbestimmungen können
 * unter der URL <a href="https://eupl.eu/">https://eupl.eu/</a> abgerufen werden.
 *
 *
 * @author R. Lindemann, N.Büngener
 * @version 2.4.1
 */
public abstract class OSCIResponseTo extends OSCIMessage
{
  //  private static Log log = LogFactory.getLog(OSCIResponseTo.class);
  Vector<String[]> feedBack;
  FeedbackObject[] feedbackObjects;

  OSCIResponseTo()
  {
  }

  OSCIResponseTo(DialogHandler dh)
  {
    super(dh);
  }

  String writeFeedBack()
  {
    StringBuffer sb = new StringBuffer("<");
    sb.append(osciNSPrefix);
    sb.append(":Feedback>");

    for (int i = 0; i < feedBack.size(); i++)
    {
      sb.append("<");
      sb.append(osciNSPrefix);
      sb.append(":Entry xml:lang=\"");
      sb.append(((String[]) feedBack.get(i))[0]);
      sb.append("\"><");
      sb.append(osciNSPrefix);
      sb.append(":Code>");
      sb.append(((String[]) feedBack.get(i))[1]);
      sb.append("</");
      sb.append(osciNSPrefix);
      sb.append(":Code><");
      sb.append(osciNSPrefix);
      sb.append(":Text>");
      sb.append(((String[]) feedBack.get(i))[2]);
      sb.append("</");
      sb.append(osciNSPrefix);
      sb.append(":Text></");
      sb.append(osciNSPrefix);
      sb.append(":Entry>");
    }

    sb.append("</");
    sb.append(osciNSPrefix);
    sb.append(":Feedback>");

    return sb.toString();
  }

  void setFeedback(String[] code)
  {
    feedBack = new Vector<String[]>();

    String[] fb;

    for (int i = 0; i < code.length; i++)
    {
      fb = new String[3];
      fb[0] = dialogHandler.getLanguageList();
      fb[1] = code[i];
      fb[2] = DialogHandler.text.getString(code[i]);
      feedBack.add(fb);
    }
  }

  /**
   * Liefert die Rückmeldung (Feedback-Eintrag) als String-Array zurück.
   * Der erste Index des Arrays entspricht dem Index des Entry-Elementes.
   * Beim zweiten Index bezeichnet <br>
   * 0 - das Sprachkürzel (z.B. "de", "en-US", optional)<br>
   * 1 - den Code<br>
   * 2 - den Text<br>
   *
   * @return Rückmeldung
   */
  public String[][] getFeedback()
  {
    if (feedBack == null)
      return null;

    return feedBack.toArray(new String[feedBack.size()][3]);
  }

  /**
   * undocumented
   *
   * @return undocumented
   */
  public FeedbackObject[] getFeedbackObjects()
  {
    if (feedBack == null)
      return null;

    if (feedbackObjects == null)
    {
      feedbackObjects = new FeedbackObject[feedBack.size()];

      for (int i = 0; i < feedBack.size(); i++)
        feedbackObjects[i] = MessagePartsFactory.createFeedbackObject(feedBack.get(i));
    }

    return feedbackObjects;
  }

  /**
   * Bringt eine Supplier-Signatur an.
   * @throws IOException bei Schreib-/Leseproblemen
   * @throws OSCIException wenn beim Zusammenstellen der Datein ein Problem auftritt
   * @throws de.osci.osci12.common.OSCICancelledException bei Abbruch durch den
   * @throws NoSuchAlgorithmException undocumented
   * Benutzer
   */
  void sign()
     throws IOException,
            OSCIException,
            java.security.NoSuchAlgorithmException
  {
    super.sign(dialogHandler.getSupplier());
    messageParts.set(2, intermediaryCertificatesH);
  }

  /**
   * undocumented
   *
   * @throws OSCIException undocumented
   * @throws NoSuchAlgorithmException undocumented
   * @throws IOException undocumented
   */
  @Override
  protected void compose() throws OSCIException,
                                  NoSuchAlgorithmException,
                                  IOException
  {
    super.compose();
    messageParts.add(intermediaryCertificatesH); // maybe null
  }
}
