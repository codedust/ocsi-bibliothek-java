package de.osci.helper;

import de.osci.osci12.common.Constants;
import de.osci.osci12.common.DialogHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;


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
public class Tools
{
  private static Log log = LogFactory.getLog(Tools.class);
  private static SecureRandom random;
  private static CertificateFactory factory;

  /**
   * undocumented
   *
   * @param a undocumented
   * @param b undocumented
   *
   * @return undocumented
   */
  public static boolean compareByteArrays(byte[] a, byte[] b)
  {
    if (a.length != b.length)
      return false;

    for (int i = 0; i < a.length; i++)
    {
      if (a[i] != b[i])
        return false;
    }

    return true;
  }

  /**
   * undocumented
   *
   * @param certificate undocumented
   *
   * @return undocumented
   *
   * @throws CertificateException undocumented
   */
  public static X509Certificate createCertificate(InputStream certificate)
                                           throws CertificateException
  {
    try
    {
      if (certificate == null)
      {
        if (log.isDebugEnabled())
          log.debug("Kein Input Stream");
      }

      if (factory == null)
      {
        if (DialogHandler.getSecurityProvider() == null)
          factory = CertificateFactory.getInstance("X.509");
        else
          factory = CertificateFactory.getInstance("X.509", DialogHandler.getSecurityProvider());
      }

      X509Certificate ret = (java.security.cert.X509Certificate)factory.generateCertificate(certificate);

      if (ret == null)
        throw new CertificateException(DialogHandler.text.getString("cert_gen_error"));

      return ret;
    }
    catch (CertificateException ex)
    {
      throw ex;
    }
    catch (Exception ex)
    {
      log.error("Fehler: ", ex);
      throw new CertificateException(DialogHandler.text.getString("cert_gen_error"));
    }
  }

  /**
   * undocumented
   *
   * @param in undocumented
   *
   * @return undocumented
   *
   * @throws IOException undocumented
   */
  public static byte[] readBytes(InputStream in) throws IOException
  {
    try
    {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      byte[] bytes = new byte[Constants.DEFAULT_BUFFER_BLOCKSIZE];
      int count = 0;

      while ((count = in.read(bytes)) > -1)
      {
        bos.write(bytes, 0, count);
      }

      in.close();

      return bos.toByteArray();
    }
    catch (IOException ex)
    {
      log.error("Fehler beim einlesen des Streams.");
      throw ex;
    }
  }

  /**
   * undocumented
   *
   * @param certificate undocumented
   *
   * @return undocumented
   *
   * @throws CertificateException undocumented
   */
  public static X509Certificate createCertificate(byte[] certificate)
                                           throws CertificateException
  {
    try
    {
      if (factory == null)
      {
        if (DialogHandler.getSecurityProvider() == null)
          factory = CertificateFactory.getInstance("X.509");
        else
          factory = CertificateFactory.getInstance("X.509", DialogHandler.getSecurityProvider());
      }

      return (java.security.cert.X509Certificate) factory.generateCertificate(new java.io.ByteArrayInputStream(certificate));
    }
    catch (CertificateException ex)
    {
      log.error("Fehler Certifikate konnte nicht erstellt werden. Bytes: " + new String(certificate));
      throw ex;
    }
    catch (Exception ex)
    {
      throw new CertificateException(DialogHandler.text.getString("cert_gen_error"));
    }
  }

  /**
   * undocumented
   *
   * @param length undocumented
   *
   * @return undocumented
   *
   * @throws NoSuchAlgorithmException undocumented
   */
  public static String createRandom(int length) throws NoSuchAlgorithmException
  {
    try
    {
      return Base64.encode(createRawRandom(length));
    }
    catch (IOException e)
    {
      throw new RuntimeException(e);
    }
  }

  /**
   * undocumented
   *
   * @param length undocumented
   *
   * @return undocumented
   *
   * @throws NoSuchAlgorithmException undocumented
   */
  public static byte[] createRawRandom(int length) throws NoSuchAlgorithmException
  {
    if (random == null)
    {
      try
      {
        random = SecureRandom.getInstance(DialogHandler.getSecureRandomAlgorithm(), DialogHandler.getSecurityProvider());
      }
      catch (NoSuchAlgorithmException nsae)
      {
        // in case the configured provider does not support the algorithm, fallback
        random = SecureRandom.getInstance(DialogHandler.getSecureRandomAlgorithm());
      }
    }

    byte[] bytes = new byte[length];
    random.nextBytes(bytes);

    return bytes;

  }
}
