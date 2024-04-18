package de.osci.helper;

import java.io.IOException;
import java.io.OutputStream;


/**
 * <p>Copyright © 2023 Governikus GmbH &amp; Co. KG, Germany</p>
 * <p>Erstellt von Governikus GmbH &amp; Co. KG</p>
 * <p>Diese Bibliothek kann von jedermann nach Maßgabe der European Union
 *  Public Licence genutzt werden.</p><p>Die Lizenzbestimmungen können
 * unter der URL <a href="https://eupl.eu/">https://eupl.eu/</a> abgerufen werden.
 *
 *
 * @author N. Büngener
 * @version 2.4.1
 */
public class NullOutputStream extends OutputStream
{
  //  private static Log log = LogFactory.getLog(NullOutputStream.class);
  private long count;

  /**
   * Creates a new NullOutputStream object.
   */
  public NullOutputStream()
  {
  }

  /**
   * undocumented
   *
   * @param b undocumented
   *
   * @throws IOException undocumented
   */
  public void write(int b) throws IOException
  {
    count++;
  }

  /**
   * undocumented
   *
   * @param b undocumented
   * @param off undocumented
   * @param len undocumented
   *
   * @throws IOException undocumented
   */
  public void write(byte[] b, int off, int len) throws IOException
  {
    count += len;
  }

  /**
   * undocumented
   *
   * @return undocumented
   */
  public long getLength()
  {
    return count;
  }
}
