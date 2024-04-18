package de.osci.osci12;

import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.osci.osci12.common.OSCIExceptionCodes;
import de.osci.osci12.common.OSCIExceptionCodes.OSCIExceptionCodesI;


/**
 * Diese Klasse stellt die Superklasse aller bibliothekseigenen Exceptions dar.
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
public class OSCIException extends Exception
{

  private static Log log = LogFactory.getLog(OSCIException.class);

  private static final long serialVersionUID = 1L;

  protected String errorCode = "null";

  protected OSCIExceptionCodesI errorCodeObject = null;

  public static final ResourceBundle us_text = ResourceBundle.getBundle("de.osci.osci12.extinterfaces.language.Text",
                                                                        java.util.Locale.US);

  // protected String message;
  public OSCIException()
  {}

  /**
   * Erzeugt ein OSCIException-Objekt mit einem erklärenden String und einem Fehlercode. Als Fehlercode kann
   * jeder String verwendet werden, für den ein entsprechender Eintrag in der zum gesetzten default-Locale
   * gehörenden Sprachdatei (s. Package de.osci.osci12.extinterfaces.language) vorhanden ist.
   *
   * @param errorCode undocumented
   */
  public OSCIException(String errorCode)
  {
    super();
    setErrorCode(errorCode);
  }
  /**
   * Erzeugt ein OSCIException-Objekt mit einem erklärenden String und einem Fehlercode. Als Fehlercode kann
   * jeder String verwendet werden, für den ein entsprechender Eintrag in der zum gesetzten default-Locale
   * gehörenden Sprachdatei (s. Package de.osci.osci12.extinterfaces.language) vorhanden ist.
   *
   * @param errorCodeobject {@link OSCIException}
   */
  public OSCIException(OSCIExceptionCodesI errorCodeobject)
  {
    super();
    setErrorCodeObject(errorCodeobject);
  }


  /**
   * Creates a new OSCIException object.
   *
   * @param message undocumented
   * @param errorCode undocumented
   */
  protected OSCIException(String message, String errorCode)
  {
    super(message);
    setErrorCode(errorCode);


  }

  /**
   * Erzeugt ein SoapClientException-Objekt mit einem erklärenden String als Message und einem Fehlercode.
   *
   * @param oscicode {@link OSCIExceptionCodes}
   * @param message erklärenden String als Message und einem Fehlercode
   */
  public OSCIException(OSCIExceptionCodesI oscicode, String message)
  {
    super(message);
    setErrorCodeObject(oscicode);
  }

  /**
   * Liefert den OSCI-Fehlercode.
   *
   * @return Code
   */
  public String getErrorCode()
  {
    return errorCode;
  }

  /**
   * Liefert den OSCI-Fehlercode, als enumuartion Objekt.
   *
   * @return Code
   */
  public OSCIExceptionCodesI getErrorCodeObject()
  {
    return errorCodeObject;
  }

  protected void setErrorCodeObject(OSCIExceptionCodesI errorCodeObject)
  {
    this.errorCodeObject = errorCodeObject;
    if(errorCodeObject!=null)
    {
      errorCode= errorCodeObject.getOSCICode();
    }
  }

  protected void setErrorCode(String errorCode)
  {
    this.errorCode = errorCode;
  }

  /**
   * Liefert die Exception-Nachricht in der jeweiligen Sprache (Default-Locale).
   *
   * @return lokale Nachricht
   */
  @Override
  public String getLocalizedMessage()
  {
    try
    {
      if (errorCodeObject != null)
      {
        return de.osci.osci12.common.DialogHandler.text.getString(errorCodeObject.getOSCICode());
      }
      else
      {
        return de.osci.osci12.common.DialogHandler.text.getString(errorCode);
      }
    }
    catch (Exception ex)
    {
      return getMessage();
    }
  }
}
