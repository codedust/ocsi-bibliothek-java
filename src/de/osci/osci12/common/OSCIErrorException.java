package de.osci.osci12.common;

import de.osci.osci12.OSCIException;
import de.osci.osci12.common.OSCIExceptionCodes.OSCIExceptionCodesI;
import de.osci.osci12.messagetypes.OSCIMessage;


/**
 * Diese Exception wird für die in der OSCI 1.2 Transport-Spezifikation definierten Fehlermeldungen auf
 * Auftragsebene verwendet.
 * <p>
 * Copyright © 2023 Governikus GmbH &amp; Co. KG, Germany
 * </p>
 * <p>Erstellt von Governikus GmbH &amp; Co. KG</p>
 * <p>
 * Diese Bibliothek kann von jedermann nach Maßgabe der European Union Public Licence genutzt
 * werden.
 * </p>
 * <p>
 * Die Lizenzbestimmungen können unter der URL <a href="https://eupl.eu/">https://eupl.eu/</a> abgerufen werden.
 * </p>
 *
 * @author R. Lindemann, N.Büngener
 * @version 2.4.1
 */
public class OSCIErrorException extends OSCIException
{

  private static final long serialVersionUID = 1L;

  private OSCIMessage message;

  /**
   * Legt ein neues OSCIErrorException Objekt an.
   *
   * @deprecated Bitte den Konstruktor mit OSCIExceptionCodesI benutzen
   * @param errorCode Fehlercode
   */
  @Deprecated
  public OSCIErrorException(String errorCode)
  {
    super(errorCode);
  }

  /**
   * Legt ein neues OSCIErrorException Objekt an.
   *
   * @deprecated Bitte den Konstruktor mit OSCIExceptionCodesI benutzen
   * @param errorCode Fehlercode
   * @param message OSCI-Nachrichtenobjekt, dessen Verarbeitung den Fehler ausgelöst hat.
   */
  @Deprecated
  public OSCIErrorException(String errorCode, OSCIMessage message)
  {
    super(errorCode);
    this.message = message;
  }

  /**
   * Legt ein neues OSCIErrorException Objekt an.
   *
   * @param errorCode Fehlercode Objekt
   */
  public OSCIErrorException(OSCIExceptionCodesI errorCode)
  {
    super(errorCode);
  }

  /**
   * Legt ein neues OSCIErrorException Objekt an.
   *
   * @param errorCode Fehlercode Objekt
   * @param message OSCI-Nachrichtenobjekt, dessen Verarbeitung den Fehler ausgelöst hat.
   */
  public OSCIErrorException(OSCIExceptionCodesI errorCode, OSCIMessage message)
  {
    super(errorCode);
    this.message = message;
  }

  /**
   * Liefert das OSCI-Nachrichtenobjekt, dessen Verarbeitung den Fehler ausgelöst hat. Wenn beim Auftreten des
   * Fehlers noch kein Nachrichtenobjekt angelegt werden konnte, wir null zurückgegeben. Das Objekt ist in dem
   * Zustand zum Zeitpunkt des Fehlers, also ggf. unvollständig.
   *
   * @return OSCI-Nachrichtenobjekt, ggf. null
   */
  public OSCIMessage getOSCIMessage()
  {
    return message;
  }

}
