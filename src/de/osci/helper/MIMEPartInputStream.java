package de.osci.helper;

import de.osci.osci12.common.Constants;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Hashtable;


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
public class MIMEPartInputStream extends FilterInputStream
{
  private String contentType;
  private String encoding;
  private String contentID;
  private int s;
  private long contentLength;
  private boolean closed = false;
  private byte[] end;
  private MIMEParser parser;
  public Hashtable<String, String> mime_headers;

  /**
   * Creates a new MIMEPartInputStream object.
   *
   * @param parser undocumented
   * @param contentType undocumented
   * @param encoding undocumented
   * @param contentID undocumented
   * @param contentLength undocumented
   *
   * @throws UnsupportedEncodingException undocumented
   */
  public MIMEPartInputStream(MIMEParser parser, String contentType, String encoding, String contentID, long contentLength)
                      throws UnsupportedEncodingException
  {
    super(parser.in);
    this.parser = parser;
    this.end = ("\r\n--" + parser.boundary).getBytes(Constants.CHAR_ENCODING);
    this.contentType = contentType;
    this.encoding = encoding;
    this.contentID = contentID;
    this.contentLength = contentLength;
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
    byte[] tmp = new byte[1];
    int i = read(tmp, 0, 1);

    if (i == -1)
      return -1;

    return ((int) tmp[0]) & 0xff;
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

    if (parser.buffer != null)
    {
      if ((parser.bufferPointer + len) <= parser.buffer.length)
      {
        System.arraycopy(parser.buffer, parser.bufferPointer, b, off, len);
        parser.bufferPointer += len;
        s = len;
      }
      else
      {
        int bufBytes = parser.buffer.length - parser.bufferPointer;
        System.arraycopy(parser.buffer, parser.bufferPointer, b, off, bufBytes);
        s = in.read(b, off + bufBytes, len - bufBytes);

        if (s > -1)
          s += bufBytes;
        else
          s = bufBytes;

        parser.buffer = null;
        parser.bufferPointer = 0;
      }
    }
    else
      s = in.read(b, off, len);

    if (s == -1)
      throw new IOException("EOS before marker.");

    int i = off;
    int patternIndex = -1;
    int tmp;

    for (i = off; i < (off + s); i++)
    {
      if ((b[i] == end[0]))
      {
        for (tmp = i, patternIndex = 0; ((patternIndex < end.length) && (tmp < (off + s))); patternIndex++, tmp++)
        {
          if (b[tmp] != end[patternIndex])
            break;
        }

        if (patternIndex == end.length)
        {
          // Falls keine Daten im Buffer waren, restliche Daten in neuem Buffer speichern,
          // sonst den bufferPointer zurücksetzen.
          if (parser.buffer == null)
          {
            parser.buffer = new byte[s - i + off];
            System.arraycopy(b, i, parser.buffer, 0, parser.buffer.length);
            parser.bufferPointer = 0;
          }
          else
            parser.bufferPointer -= (len - (i - off));

          s = i - off;
          closed = true;

          break;
        }

        // Prüfen, ob weitere Daten Rest eines Trenners enthalten
        else if (tmp >= (off + s))
        {
          int rest = end.length - patternIndex;

          if ((parser.buffer == null) || ((parser.buffer.length - parser.bufferPointer) < rest))
          {
            int bufferIndex = 0;

            if (parser.buffer == null)
              parser.buffer = new byte[rest];
            else
            {
              // Buffer erweitern, falls notwendig
              bufferIndex = parser.buffer.length - parser.bufferPointer;

              byte[] newBuffer = new byte[rest];
              System.arraycopy(parser.buffer, parser.bufferPointer, newBuffer, 0,
                               parser.buffer.length - parser.bufferPointer);
              parser.buffer = newBuffer;
            }

            int p = 0;

            while (bufferIndex < parser.buffer.length)
            {
              if ((p = in.read(parser.buffer, bufferIndex, 1)) > 0)
                bufferIndex++;
              else if (p < 0)
                throw new IOException("EOS before marker.");
            }

            parser.bufferPointer = 0;
          }

          for (tmp = parser.bufferPointer; patternIndex < end.length; patternIndex++, tmp++)
          {
            if (parser.buffer[tmp] != end[patternIndex])
              break;
          }

          if (patternIndex == end.length)
          {
            s = i - off;

            // etwas umständlich, tritt aber auch nicht oft ein
            byte[] newBuffer = new byte[(end.length + parser.buffer.length) - tmp];
            System.arraycopy(end, 0, newBuffer, 0, end.length);
            System.arraycopy(parser.buffer, tmp, newBuffer, end.length, parser.buffer.length - tmp);
            parser.buffer = newBuffer;
            parser.bufferPointer = 0;
            closed = true;
          }
        }
      }

      patternIndex = 0;
    }

    if (s == 0)
    {
      close();

      return -1;
    }

    return s;
  }

  /**
   * undocumented
   *
   * @throws IOException undocumented
   */
  public void close() throws IOException
  {
    // Bis zum Ende lesen....
    byte[] tmp = new byte[Constants.DEFAULT_BUFFER_BLOCKSIZE];

    while (read(tmp) > -1)
    {
    }

    closed = true;
  }

  /**
   * undocumented
   *
   * @return undocumented
   */
  public String getContentID()
  {
    return contentID;
  }

  /**
   * undocumented
   *
   * @return undocumented
   */
  public String getEncoding()
  {
    return encoding;
  }

  /**
   * undocumented
   *
   * @return undocumented
   */
  public long getLength()
  {
    return contentLength;
  }

  /**
   * undocumented
   *
   * @return undocumented
   */
  public String getContentType()
  {
    return contentType;
  }
}
