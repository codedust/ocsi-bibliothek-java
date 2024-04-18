package de.osci.helper;

import de.osci.osci12.common.Constants;
import de.osci.osci12.common.DialogHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.StringTokenizer;


/**
 * Einfacher MIME-Parser. Die Funktion beschränkt sich auf die Anforderungen der OSCI 1.2 Transportbibliothek.
 * <p>
 * Copyright © 2023 Governikus GmbH &amp; Co. KG, Germany
 * </p>
 * <p>
 * Erstellt von Governikus GmbH &amp; Co. KG
 * </p>
 * <p>
 * Diese Bibliothek kann von jedermann nach Maßgabe der European Union Public Licence genutzt
 * werden.
 * </p>
 * Die Lizenzbestimmungen können unter der URL <a href="https://eupl.eu/">https://eupl.eu/</a> abgerufen werden.
 * </p>
 *
 * @author N. Büngener
 * @version 2.4.1
 */
public class MIMEParser
{

  private static Log log = LogFactory.getLog(MIMEParser.class);

  public static final int MAX_LINE_SIZE = 1024 * 1024; // 1 MB

  InputStream in;

  private MIMEPartInputStream currentStream;

  public String boundary;

  byte[] buffer;

  int bufferPointer;

  /**
   * Creates a new MIMEParser object.
   *
   * @param in undocumented
   * @throws IOException undocumented
   */
  public MIMEParser(InputStream in) throws IOException
  {
    this.in = in;

    Hashtable<String, String> header = readHeaders();
    
    if (header == null || (header.get("mime-version") != null && !header.get("mime-version").equals("1.0")))
      throw new IOException(DialogHandler.text.getString("msg_format_error"));

    Hashtable<String, String> contentType = parseHeader("content-type", header.get("content-type"));

    if ((boundary = contentType.get("boundary")) == null)
      throw new IOException(DialogHandler.text.getString("msg_format_error"));
  }

  /**
   * undocumented
   *
   * @return undocumented
   * @throws IOException undocumented
   * @throws IllegalArgumentException undocumented
   */
  public MIMEPartInputStream getNextStream() throws IOException
  {
    String contentType;
    String charset;
    String encoding;
    String contentID;
    String contentLength;
    long length = -1;
    String bound;

    do
    {
      bound = readLine();
    }
    while (bound != null && bound.trim().equals(""));

    if (("--" + boundary + "--").equals(bound))
    {
      // Bis zum Ende lesen....
      byte[] tmp = new byte[Constants.DEFAULT_BUFFER_BLOCKSIZE];

      while (in.read(tmp) > -1)
      {}
      in.close();

      return null;
    }
    else if (!("--" + boundary).equals(bound))
      throw new IOException(DialogHandler.text.getString("msg_format_error"));

    Hashtable<String, String> header = readHeaders();

    if (header == null)
    {
      in.close();

      return null;
    }

    if (log.isDebugEnabled())
      log.debug("header: " + header);

    if (header.get("content-type") == null)
      throw new IOException(DialogHandler.text.getString("msg_format_error"));

    Hashtable<String, String> cntTyp = parseHeader("content-type", header.get("content-type"));
    contentType = cntTyp.get("content-type");

    if (cntTyp.get("charset") != null)
    {
      charset = cntTyp.get("charset");

      if (!charset.equalsIgnoreCase(Constants.CHAR_ENCODING))
        throw new IllegalArgumentException(DialogHandler.text.getString("invalid_charset") + charset);
    }

    encoding = header.get("content-transfer-encoding");
    contentID = header.get("content-id");

    if (contentID.startsWith("<"))
      contentID = contentID.substring(1, contentID.length() - 1);

    contentLength = header.get("content-length");

    if (contentLength != null)
      length = Long.parseLong(contentLength);

    currentStream = new MIMEPartInputStream(this, contentType, encoding, contentID, length);
    currentStream.mime_headers = header;

    return currentStream;
  }

  private Hashtable<String, String> readHeaders() throws IOException
  {
    Hashtable<String, String> headers = new Hashtable<>();
    String header;
    String nextHeader = null;
    try
    {
      // header = readLine();
      while ((header = readLine().trim()).equals(""))
      {}

      while (!header.equals(""))
      {
        try
        {
          nextHeader = readLine();

          while ((nextHeader.length() > 0) && Character.isWhitespace(nextHeader.charAt(0)))
          {
            header = header + nextHeader;
            nextHeader = readLine();
          }

          headers.put(header.substring(0, header.indexOf(':')).toLowerCase().trim(),
                      header.substring(header.indexOf(':') + 1).trim());
          header = nextHeader.trim();

        }
        catch (StringIndexOutOfBoundsException ex)
        {
          // do nothing
          log.warn("unexpected mime structure!");
          if (nextHeader != null)
            header = nextHeader.trim();
        }
      }
    }
    catch (NullPointerException ex)
    {
      // do nothing
      log.warn("unexpected mime structure!");
      return null;
    }
    if (headers.size() == 0)
    {
      log.debug("\n\nKEIN HEADER !!");

      return null;
    }

    return headers;
  }

  private Hashtable<String, String> parseHeader(String field_name, String header)
  {
    Hashtable<String, String> params = new Hashtable<>();
    StringTokenizer ct = new StringTokenizer(header, "; ");
    params.put(field_name, ct.nextToken().trim());

    String[] tmp;

    while (ct.hasMoreTokens())
    {
      tmp = ct.nextToken().split("=", 2);

      if (tmp.length > 1)
      {
        if (tmp[1].startsWith("\"") && tmp[1].endsWith("\""))
          tmp[1] = tmp[1].substring(1, tmp[1].length() - 1);

        params.put(tmp[0].trim(), tmp[1].trim());
      }
    }

    return params;
  }

  private String readLine() throws IOException
  {
    int b = 0;
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    boolean end = false;
    long counter = 0;

    while (!end)
    {
      b = readFromInput();

      if (b == -1)
        return null;

      if (b == 0x0D)
      {
        b = readFromInput();

        if (b == 0x0A)
          end = true;
        else if (Character.isWhitespace((char)(b)))
          continue;
      }

      if (!end)
      {
        baos.write(b);
      }

      counter++;
      if (counter >= MAX_LINE_SIZE)
      {
        log.warn("Zeile hat Laenge von " + MAX_LINE_SIZE + " erreicht, Lesen der Zeile abbrechen");
        throw new IOException(DialogHandler.text.getString("msg_format_error"));
      }
    }

    if (log.isDebugEnabled())
    {
      log.debug("ZEILE mit Laenge " + counter + ": " + baos.toString(Constants.CHAR_ENCODING));
    }

    return baos.toString(Constants.CHAR_ENCODING);
  }

  private int readFromInput() throws IOException
  {
    int j = 0;

    if ((buffer != null) && (bufferPointer < buffer.length))
    {
      j = buffer[bufferPointer++];

      if (bufferPointer >= buffer.length)
      {
        buffer = null;
        bufferPointer = 0;
      }
    }
    else
      j = in.read();

    return j;
  }
}
