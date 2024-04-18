package de.osci.helper;

import java.io.FilterOutputStream;
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
public class StoreOutputStream extends FilterOutputStream
{
  //  private static Log log = LogFactory.getLog(StoreOutputStream.class);
  private OutputStream copyStream;

  /**
   * Creates a new StoreOutputStream object.
   *
   * @param outStream undocumented
   * @param copyStream undocumented
   *
   * @throws IOException undocumented
   */
  public StoreOutputStream(OutputStream outStream, OutputStream copyStream)
                    throws IOException
  {
    super(outStream);
    this.copyStream = copyStream;
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
    //    write(new byte[] { (byte) b }, 0, 1);
    out.write(b);
    copyStream.write(b);
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
    out.write(b, off, len);
    copyStream.write(b, off, len);
  }

  /**
   * undocumented
   *
   * @throws IOException undocumented
   */
  public void flush() throws IOException
  {
    out.flush();
    copyStream.flush();
  }

  /**
   * undocumented
   *
   * @throws IOException undocumented
   */
  public void close() throws IOException
  {
    flush();
    out.close();
    copyStream.close();
  }
}
