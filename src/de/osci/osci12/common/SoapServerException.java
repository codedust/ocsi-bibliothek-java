package de.osci.osci12.common;

import de.osci.osci12.OSCIException;
import de.osci.osci12.common.OSCIExceptionCodes.OSCIExceptionCodesI;


/**
 * Diese Exception zeigt eine der in der OSCI-Spezifikation
 * definierten Fehlermeldungen auf Nachrichtenebene (faultcode soap:Server) an.
 *
 * <p>Copyright © 2023 Governikus GmbH &amp; Co. KG, Germany</p>
 * <p>Erstellt von Governikus GmbH &amp; Co. KG</p>
 * <p>Diese Bibliothek kann von jedermann nach Maßgabe der European Union
 *  Public Licence genutzt werden.</p>
 * <p>
 * Die Lizenzbestimmungen können
 * unter der URL <a href="https://eupl.eu/">https://eupl.eu/</a> abgerufen werden.
 *
 *
 * @author R. Lindemann, N.Büngener
 * @version 2.4.1
 */
public class SoapServerException extends OSCIException
{
  /**
   *
   */
  private static final long serialVersionUID = 1L;
  /**
   * Erzeugt ein SoapServerException-Objekt mit einem erklärenden String und
   * einem Fehlercode. Als Fehlercode kann jeder String verwendet werden, für den
   * ein entsprechender Eintrag in der zum gesetzten default-Locale gehörenden
   * Sprachdatei (s. Package de.osci.osci12.extinterfaces.language) vorhanden ist.
   * @deprecated Bitte den Konstruktor mit OSCIExceptionCodesI benutzen
   * @param oscicode OSCI-Fehlercode
   * @param faultstring Fehlertext
   */
  @Deprecated
  public SoapServerException(String oscicode, String faultstring)
  {
    super(faultstring, oscicode);
  }
  /**
   * Erzeugt ein SoapServerException-Objekt mit einem erklärenden String und
   * einem Fehlercode. Als Fehlercode kann jeder String verwendet werden, für den
   * ein entsprechender Eintrag in der zum gesetzten default-Locale gehörenden
   * Sprachdatei (s. Package de.osci.osci12.extinterfaces.language) vorhanden ist.
   * @param oscicode OSCI-Fehlercode Objekt
   * @param faultstring Fehlertext
   */
  public SoapServerException(OSCIExceptionCodesI oscicode, String faultstring)
  {
    super(oscicode,faultstring);
  }
}
