package de.osci.osci12.extinterfaces;

/**
 * Anwendungen können eine Implementierung dieser Schnittstelle im DialogHandler-Objekt
 * einer Kommunikation setzen, über das die Bibliothek Informationen über die Durchführung
 * von Operationen liefert. Z.Zt. bietet die Bibliothek nur eine minimale Unterstützung
 * dieser Schnittstelle, beispielsweise ohne Informationen über den prozentualen Fortschritt
 * einer Operation.
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
 * @see de.osci.osci12.common.DialogHandler#setProgressEventHandler(ProgressEventHandlerI)
 */
public interface ProgressEventHandlerI
{
  public static final int SEND_MESSAGE = 0;
  public static final int RECEIVE_MESSAGE = 1;

  /**
   * Liefert eine Versionsnummer.
   * @return Versionsnummer
   */
  public String getVersion();

  /**
   * Liefert den Namen des Herstellers.
   * @return Herstellername
   */
  public String getVendor();

  /**
   * Wird aufgerufen, sobald langwierige Vorgänge in der Bibliothek der
   * Applikation mitgeteilt werden sollen.
   *
   * @param type Art der Nachricht
   * @param param Text des Fortschrittsevents
   * @param percent Fortschritt einer Operation in Prozent oder -1,
   * wenn keine Angabe
   * @see de.osci.osci12.common.Constants
   */
  public void event(int type, String param, int percent);
}
