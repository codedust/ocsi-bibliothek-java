package de.osci.osci12.encryption;

import de.osci.osci12.OSCIException;


/**
 * Diese Exception zeigt ein Problem bei Ver- oder Entschlüsselungsvorgängen
 * an. Die Bibliothek faßt auch einige JCE/JCA-Exceptions hiermit zusammen, um (aus
 * Sicherheitsgründen) keine detaillierten Informationen über fehlgeschlagene
 * Entschlüsselungsversuche zu liefern.
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
public class OSCICipherException extends OSCIException
{
  /**
   * Creates a new OSCICipherException object.
   *
   * @param errorCode undocumented
   */
  public OSCICipherException(String errorCode)
  {
    super(errorCode);
  }

  /**
   * Creates a new OSCICipherException object.
   *
   * @param message undocumented
   * @param errorCode undocumented
   */
  public OSCICipherException(String message, String errorCode)
  {
    super(message, errorCode);
  }
}
