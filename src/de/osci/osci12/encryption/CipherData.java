package de.osci.osci12.encryption;

import java.io.IOException;
import java.io.OutputStream;

import de.osci.osci12.OSCIException;
import de.osci.osci12.common.Constants;


/**
 * <p>Copyright © 2023 Governikus GmbH &amp; Co. KG, Germany</p>
 * <p>Erstellt von Governikus GmbH &amp; Co. KG</p>
 * <p>Diese Bibliothek kann von jedermann nach Maßgabe der European Union
 *  Public Licence genutzt werden.</p>
 * <p>
 * Die Lizenzbestimmungen können unter der URL <a href="https://eupl.eu/">https://eupl.eu/</a> abgerufen werden.
 *
 *
 * @author R. Lindemann, N.Büngener
 * @version 2.4.1
 */
public class CipherData
{
  //  private static Log log = LogFactory.getLog(CipherData.class);
  private CipherReference cipherReference = null;
  private CipherValue cipherValue = null;
  private boolean referencedData = false;

  /**
   * Creates a new CipherData object.
   */
  public CipherData()
  {
  }

  /**
   * Creates a new CipherData object.
   *
   * @param cipherValue undocumented
   */
  public CipherData(CipherValue cipherValue)
  {
    referencedData = false;
    this.cipherValue = cipherValue;
  }

  /**
   * Creates a new CipherData object.
   *
   * @param cipherRef undocumented
   */
  public CipherData(CipherReference cipherRef)
  {
    referencedData = true;
    this.cipherReference = cipherRef;
  }

  /**
   * undocumented
   *
   * @return undocumented
   */
  public CipherReference getCipherReference()
  {
    return cipherReference;
  }

  /**
   * undocumented
   *
   * @return undocumented
   */
  public CipherValue getCipherValue()
  {
    return cipherValue;
  }

  /**
   * undocumented
   *
   * @return undocumented
   */
  public boolean isReferencedData()
  {
    return referencedData;
  }

  /**
   * undocumented
   *
   * @param out undocumented
   *
   * @throws IOException undocumented
   * @throws OSCIException undocumented
   */
  public void writeXML(OutputStream out, String ds, String xenc)
                throws IOException,
                       OSCIException
  {
    out.write(("<" + xenc + ":CipherData>").getBytes(Constants.CHAR_ENCODING));

    if (referencedData)
    {
      cipherReference.writeXML(out, ds, xenc);
    }
    else
    {
      cipherValue.writeXML(out, xenc);
    }

    out.write(("</" + xenc + ":CipherData>").getBytes(Constants.CHAR_ENCODING));
  }
}
