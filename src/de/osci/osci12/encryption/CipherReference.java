package de.osci.osci12.encryption;

import de.osci.osci12.common.Constants;
import de.osci.osci12.common.DialogHandler;
import de.osci.osci12.extinterfaces.OSCIDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;


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
public class CipherReference
{
  // Transform ist optinal und wird zunächst nicht unterstützt.
  private static Log log = LogFactory.getLog(CipherReference.class);
  private Vector<String> transformList = new Vector<String>();
  private String uri;
  private OSCIDataSource swapBuffer = null;
  private boolean encryptedStream = false;

  /**
   * Creates a new CipherReference object.
   *
   * @param uri undocumented
   *
   * @throws IOException undocumented
   */
  public CipherReference(String uri) throws IOException
  {
    this.uri = uri;

    if (log.isDebugEnabled())
      log.debug("Konstruktor");

    swapBuffer = DialogHandler.getNewDataBuffer();
  }

  /**
   * undocumented
   *
   * @param refStream undocumented
   *
   * @throws IOException undocumented
   */
  public void setReferencedStream(InputStream refStream)
                           throws IOException
  {
    if (log.isDebugEnabled())
      log.debug("Der Referenced Stream wurde gesetzt.");

    OutputStream out = swapBuffer.getOutputStream();
    byte[] bytes = new byte[Constants.DEFAULT_BUFFER_BLOCKSIZE];
    int anz = 0;

    while ((anz = refStream.read(bytes)) > -1)
    {
      out.write(bytes, 0, anz);
    }
  }

  /**
   * undocumented
   *
   * @param transform undocumented
   */
  public void addTransform(String transform)
  {
    transformList.add(transform);
  }

  /**
   * undocumented
   *
   * @param i undocumented
   *
   * @return undocumented
   */
  public String getTransform(int i)
  {
    return transformList.get(i);
  }

  /**
   * undocumented
   *
   * @param uri undocumented
   */
  public void setURI(String uri)
  {
    this.uri = uri;
  }

  /**
   * undocumented
   *
   * @return undocumented
   */
  public String getURI()
  {
    return uri;
  }

  /**
   * undocumented
   *
   * @return undocumented
   *
   * @throws IOException undocumented
   */
  public InputStream getReferencedStream() throws IOException
  {
    return swapBuffer.getInputStream();
  }

  /**
   * undocumented
   *
   * @param out undocumented
   *
   * @throws IOException undocumented
   */
  public void writeXML(OutputStream out, String ds, String xenc)
                throws IOException
  {
    //id
    out.write(("<" + xenc + ":CipherReference URI=\"" + this.getURI() + "\">").getBytes(Constants.CHAR_ENCODING));
    out.write(("<" + xenc + ":Transforms><" + ds +
              ":Transform Algorithm=\"http://www.w3.org/2000/09/xmldsig#base64\"></" + ds + ":Transform></" + xenc +
              ":Transforms></" + xenc).getBytes(Constants.CHAR_ENCODING));
    // Transformer auflisten
    out.write(":CipherReference>".getBytes(Constants.CHAR_ENCODING));
  }

  /**
   * undocumented
   *
   * @return undocumented
   */
  public boolean isEncryptedStream()
  {
    return encryptedStream;
  }

  /**
   * undocumented
   *
   * @param encryptedStream undocumented
   */
  public void setEncryptedStream(boolean encryptedStream)
  {
    this.encryptedStream = encryptedStream;
  }
}
