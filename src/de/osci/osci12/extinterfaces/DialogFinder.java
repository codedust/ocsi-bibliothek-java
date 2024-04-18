package de.osci.osci12.extinterfaces;

import de.osci.osci12.common.DialogHandler;
import de.osci.osci12.soapheader.ControlBlockH;


/**
 * <p>Diese Schnittstelle wird nur noch intern verwendet.</p>
 * <p>Copyright © 2023 Governikus GmbH &amp; Co. KG, Germany</p>
 * <p>Erstellt von Governikus GmbH &amp; Co. KG</p>
 * <p>Diese Bibliothek kann von jedermann nach Maßgabe der European Union
 *  Public Licence genutzt werden.</p><p>Die Lizenzbestimmungen können
 * unter der URL <a href="https://eupl.eu/">https://eupl.eu/</a> abgerufen werden.
 *
 *
 * @author R. Lindemann / N. Büngener
 * @version 2.4.1
 * @see de.osci.osci12.common.DialogHandler
 */
public abstract class DialogFinder
{
  /**
   * Diese Methode muss anhand des übergebenen ControlBlockH-Objektes das
   * zugehörige DialogHandler-Objekt ermitteln und zurückgeben.
   * @param controlBlock das ControlBlock-Objekt als Identifier des gesuchten
   * DialogHandlers
   * @return gefundener DialogHandler oder null, wenn er nicht gefunden wurde
   */
  public abstract DialogHandler findDialog(ControlBlockH controlBlock);

  /**
   * Entfernt ein DialogHandler-Objekt aus der Liste der verwalteten
   * DialogHandler-Objekte. Wird nach Beendigung eines Dialogs
   * aufgerufen.
   * @param controlBlock das ControlBlock-Objekt als Identifier des gesuchten
   * DialogHandlers
   * @return true, wenn der DialogHandler erfolgreich entfernt wurde
   */
  public abstract boolean removeDialog(ControlBlockH controlBlock);

  /**
   * Fügt den verwalteten DialogHandler-Objekten ein weiteres hinzu.
   * @param dialog undocumented
   * @throws Exception undocumented
   */
  public abstract void addDialog(DialogHandler dialog)
                          throws Exception;

  /**
   * Liefert eine Versionsnummer.
   * @return Versionsnummer
   */
  public abstract String getVersion();

  /**
   * Liefert den Namen des Herstellers.
   * @return Herstellername
   */
  public abstract String getVendor();
}
