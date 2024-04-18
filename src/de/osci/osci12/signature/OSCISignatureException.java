package de.osci.osci12.signature;

import de.osci.osci12.OSCIException;


/**
 * Diese Exception zeigt ein Problem bei Signaturvorgängen
 * an. Die Bibliothek faßt auch einige JCE/JCA-Exceptions hiermit zusammen, um (aus
 * Sicherheitsgründen) keine detaillierten Informationen über fehlgeschlagene
 * Signierversuche zu liefern.
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
public class OSCISignatureException extends OSCIException
{
  /**
   * Creates a new OSCISignatureException object.
   *
   * @param errorCode undocumented
   */
  public OSCISignatureException(String errorCode)
  {
    super(errorCode);
  }

  /**
   * Creates a new OSCISignatureException object.
   *
   * @param errorCode undocumented
   */
  public OSCISignatureException(String errorCode, String addInfo)
  {
    super(addInfo, errorCode);
  }

  /**
   * Liefert die Exception-Nachricht in der jeweiligen Sprache (Default-Locale).
   * @return lokale Nachricht
   */
  public String getLocalizedMessage()
  {
    if (super.getMessage() == null)
      return super.getLocalizedMessage();
    else
      return super.getLocalizedMessage() + " '" + super.getMessage() + "'";
  }
}
