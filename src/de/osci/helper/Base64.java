package de.osci.helper;

import de.osci.osci12.common.Constants;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;


/**
 * Wrapper zum Base64-Ver-/Entschlüsseln kleinerer Datenmengen.
 *
 * <p>Copyright © 2023 Governikus GmbH &amp; Co. KG, Germany</p>
 * <p>Erstellt von Governikus GmbH &amp; Co. KG</p>
 * <p>Diese Bibliothek kann von jedermann nach Maßgabe der European Union
 *  Public Licence genutzt werden.</p><p>Die Lizenzbestimmungen können
 * unter der URL <a href="https://eupl.eu/">https://eupl.eu/</a> abgerufen werden.
 *
 *
 * @author R. Lindemann, N.Büngener
 * @version 2.4.1
 */
public class Base64
{
  //  private static Log log = LogFactory.getLog(Base64.class);
  /**
   * undocumented
   *
   * @param data undocumented
   *
   * @return undocumented
   *
   * @throws IOException undocumented
   */
  public static String encode(byte[] data) throws IOException
  {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    Base64OutputStream b64out = new Base64OutputStream(bos, false);
    b64out.write(data);
    b64out.close();

    return bos.toString(Constants.CHAR_ENCODING);
  }

  /**
   * undocumented
   *
   * @param data undocumented
   *
   * @return undocumented
   *
   * @throws IOException undocumented
   */
  public static byte[] decode(String data) throws IOException
  {
    ByteArrayInputStream bis = new ByteArrayInputStream(data.getBytes(Constants.CHAR_ENCODING));
    Base64InputStream b64in = new Base64InputStream(bis);
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    byte[] d = new byte[Constants.DEFAULT_BUFFER_BLOCKSIZE];
    int i;

    while ((i = b64in.read(d)) > -1)
      bos.write(d, 0, i);

    bos.close();
    b64in.close();

    return bos.toByteArray();
  }

  /**
   * undocumented
   *
   * @param len undocumented
   *
   * @return undocumented
   */
  public static long calcB64Length(long len)
  {
    if (len == 0)
      return 0;

    long l = ((len - 1) / 3 * 4) + 4;

    return l + (l / 76);
  }
}
