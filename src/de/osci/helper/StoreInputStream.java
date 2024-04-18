package de.osci.helper;

import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
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
public class StoreInputStream extends FilterInputStream
{
  //  private static Log log = LogFactory.getLog(StoreInputStream.class);
  private OutputStream copyStream;
  private int s;
  private ByteArrayOutputStream buffer;
  private boolean closed = false;
  private boolean save = false;

  /**
   * Creates a new StoreInputStream object.
   *
   * @param inStream undocumented
   * @param copyStream undocumented
   */
  public StoreInputStream(InputStream inStream, OutputStream copyStream)
  {
    super(inStream);
    this.copyStream = copyStream;
    buffer = new ByteArrayOutputStream();
  }

  /**
   * undocumented
   *
   * @return undocumented
   *
   * @throws IOException undocumented
   */
  public int read() throws IOException
  {
    if (closed)
      return -1;

    s = in.read();

    if (s == -1)
      return -1;
    else
    {
      if (buffer != null)
        buffer.write(s);
      else if (save)
        copyStream.write(s);
    }

    return s;
  }

  /**
   * undocumented
   *
   * @param b undocumented
   * @param off undocumented
   * @param len undocumented
   *
   * @return undocumented
   *
   * @throws IOException undocumented
   */
  public int read(byte[] b, int off, int len) throws IOException
  {
    if (closed)
      return -1;

    s = in.read(b, off, len);

    if (s == -1)
      //      close();
      return -1;
    else
    {
      if (buffer != null)
        buffer.write(b, off, s);
      else if (save)
        copyStream.write(b, off, s);
    }

    return s;
  }

  /**
   * undocumented
   *
   * @param save undocumented
   *
   * @throws IOException undocumented
   */
  public void setSave(boolean save) throws IOException
  {
    synchronized (in)
    {
      this.save = save;

      if (save)
      {
        buffer.close();
        copyStream.write(buffer.toByteArray());
      }

      buffer = null;
    }
  }

  /**
   * undocumented
   *
   * @throws IOException undocumented
   */
  public void close() throws IOException
  {
    if (closed)
      return;

    in.close();

    if (buffer != null)
    {
      buffer.close();
      copyStream.write(buffer.toByteArray());
    }

    copyStream.close();
    closed = true;
  }
}
