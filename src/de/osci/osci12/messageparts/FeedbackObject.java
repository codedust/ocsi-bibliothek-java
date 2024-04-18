package de.osci.osci12.messageparts;

/**
 * <p>Diese Klasse repräsentiert einen Feedback-Eintrag einer OSCIResponseTo-Nachricht</p>
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
public class FeedbackObject
{
  //  private static Log log = LogFactory.getLog(FeedbackObject.class);
  private String lang;
  private String code;
  private String text;

  FeedbackObject(String[] feedback)
  {
    lang = feedback[0];
    code = feedback[1];
    text = feedback[2];
  }

  /**
   * Liefert das Sprachkürzel des Feedbackeintrags (de, en...).
   * @return das Sprachkürzel
   */
  public String getLanguage()
  {
    return lang;
  }

  /**
   * Liefert den Rückmeldecode des Feedbackeintrags.
   * @return der Rückmeldecode
   */
  public String getCode()
  {
    return code;
  }

  /**
   * Liefert den Text des Feedbackeintrags.
   * @return der Text
   */
  public String getText()
  {
    return text;
  }
}
