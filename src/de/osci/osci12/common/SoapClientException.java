package de.osci.osci12.common;

import de.osci.osci12.OSCIException;
import de.osci.osci12.common.OSCIExceptionCodes.OSCIExceptionCodesI;


/**
 * Diese Exception zeigt eine der in der OSCI-Spezifikation
 * definierten Fehlermeldungen auf Nachrichtenebene (faultcode soap:Client) an.
 *
 * <p>Copyright © 2023 Governikus GmbH &amp; Co. KG, Germany</p>
 * <p>Erstellt von Governikus GmbH &amp; Co. KG</p>
 * <p>Diese Bibliothek kann von jedermann nach Maßgabe der European Union
 *  Public Licence genutzt werden.</p>
 * <p>
 * Die Lizenzbestimmungen können
 * unter der URL <a href="https://eupl.eu/">https://eupl.eu/</a> abgerufen werden.
 * </p>
 *
 *
 * @author R. Lindemann, N.Büngener
 * @version 2.4.1
 */
public class SoapClientException extends OSCIException
{
  /**
   *
   */
  private static final long serialVersionUID = 1L;

  /**
   * Erzeugt ein SoapClientException-Objekt mit einem erklärenden String als
   * Message und einem Fehlercode.
   * @deprecated Bitte den Konstruktor mit OSCIExceptionCodesI benutzen
   * @param oscicode undocumented
   * @param faultstring undocumented
   */
  @Deprecated
  public SoapClientException(String oscicode, String faultstring)
  {
    super(faultstring, oscicode);
  }

  /**
   * Erzeugt ein SoapClientException-Objekt mit einem Fehlercode und erklärendem
   * String als Message. Als Message-String wird der dem Code entsprechende Eintrag
   * in der zum aktuellen default-Locale gehörenden Sprachdatei (s. Package
   * de.osci.osci12.extinterfaces.language) gesetzt.
   * @deprecated Bitte den Konstruktor mit OSCIExceptionCodesI benutzen
   * @param oscicode undocumented
   */
  @Deprecated
  public SoapClientException(String oscicode)
  {
    this(oscicode, DialogHandler.text.getString(oscicode));
  }
  /**
   * Erzeugt ein SoapClientException-Objekt mit einem erklärenden String als
   * Message und einem Fehlercode.
   * @param oscicode undocumented
   * @param faultstring undocumented
   */
  public SoapClientException(OSCIExceptionCodesI oscicode, String faultstring)
  {
    super(oscicode,faultstring);
  }

  /**
   * Erzeugt ein SoapClientException-Objekt mit einem Fehlercode und erklärendem
   * String als Message. Als Message-String wird der dem Code entsprechende Eintrag
   * in der zum aktuellen default-Locale gehörenden Sprachdatei (s. Package
   * de.osci.osci12.extinterfaces.language) gesetzt.
   * @param oscicode undocumented
   */
  public SoapClientException(OSCIExceptionCodesI oscicode)
  {
    this(oscicode, DialogHandler.text.getString(oscicode.getOSCICode()));
  }
}
