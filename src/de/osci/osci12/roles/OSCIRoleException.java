package de.osci.osci12.roles;

import de.osci.osci12.OSCIException;


/**
 * Diese Exception zeigt an, dass ein Rollenobjekt für eine unzulässige Operation
 * verwendet wurde. Beispiel: An die Methode ContentContainer.sign(Role) wird ein
 * Role-Objekt übergeben, welches kein Signer-Objekt enthält.
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
public class OSCIRoleException extends OSCIException
{
  /**
   *
   */
  private static final long serialVersionUID = 1L;

  /**
   * Creates a new OSCIRoleException object.
   *
   * @param message undocumented
   */
  public OSCIRoleException(String message)
  {
    super(message);
  }

  /**
   * Creates a new OSCISignatureException object.
   *
   * @param addInfo undocumented
   * @param errorCode undocumented
   */
  public OSCIRoleException(String errorCode, String addInfo)
  {
    super(addInfo, errorCode);
  }

  /**
   * Liefert die Exception-Nachricht in der jeweiligen Sprache (Default-Locale).
   * @return lokale Nachricht
   */
  public String getLocalizedMessage()
  {
    return super.getLocalizedMessage() + " '" + super.getMessage() + "'";
  }

}
