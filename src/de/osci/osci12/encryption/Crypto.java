package de.osci.osci12.encryption;

import de.osci.osci12.common.Constants;
import de.osci.osci12.common.DialogHandler;
import de.osci.osci12.messagetypes.OSCIMessage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.MGF1ParameterSpec;
import java.util.Date;


/**
 * <p>
 * Copyright © 2023 Governikus GmbH &amp; Co. KG, Germany
 * </p>
 * <p>
 * Erstellt von Governikus GmbH &amp; Co. KG
 * </p>
 * <p>
 * Diese Bibliothek kann von jedermann nach Maßgabe der European Union Public Licence genutzt werden.
 * </p>
 * <p>
 * Die Lizenzbestimmungen können unter der URL <a href="https://eupl.eu/">https://eupl.eu/</a> abgerufen
 * werden.
 * </p>
 *
 * @author R. Lindemann, N.Büngener, J. Wothe
 * @version 2.4.1
 */
public class Crypto
{

  private static final Log LOG = LogFactory.getLog(OSCIMessage.class);

  // private static Log log = LogFactory.getLog(Crypto.class);
  private static String hex = "0123456789abcdef";

  /**
   * Checks generated signature is weak related to used certificates public key.
   *
   * @param date date used for check, when <code>null</code> then {@link Constants#ACTUAL_DATE} is used
   * @param x509Certificate certificate with public key
   * @return <code>true</code> when signature is weak, <code>false</code> otherwise
   */
  public static boolean isWeak(Date date, X509Certificate x509Certificate)
  {
    int keySize = Crypto.getKeySize(x509Certificate);
    return isWeak(date, x509Certificate.getPublicKey().getAlgorithm(), keySize);
  }

  /**
   * Checks generated signature is weak related to used key algorithm and size.
   *
   * @param date date used for check, when <code>null</code> then {@link Constants#ACTUAL_DATE} is used
   * @param algorithm algorithm of public key
   * @param keySize size of public key
   * @return <code>true</code> when signature is weak, <code>false</code> otherwise
   */
  public static boolean isWeak(Date date, String algorithm, int keySize)
  {
    if (date == null)
    {
      date = Constants.ACTUAL_DATE;
    }
    if (algorithm.equals("EC"))
    {
      if (keySize < 224)
      {
        LOG.info("Signature key (EC) has insufficient key size: " + keySize);
        return true;
      }
    }
    else
    {
      if (keySize < 1024)
      {
        LOG.info("Signature key (RSA) has insufficient key size: " + keySize);
        return true;
      }
      if (keySize < 2048 && !date.before(Constants.OUT_DATE_KEYSIZE_1024))
      {
        LOG.info("Signature key (RSA) has insufficient key size: " + keySize);
        return true;
      }
      if (keySize > 2048)
      {
        LOG.info("Signature key (RSA) has sufficient key size (no out date defined yet): " + keySize);
        return false;
      }
    }

    return false;
  }

  /**
   * undocumented
   *
   * @param algorithm undocumented
   * @return undocumented
   * @throws NoSuchAlgorithmException undocumented
   */
  public static javax.crypto.SecretKey createSymKey(String algorithm) throws NoSuchAlgorithmException
  {
    String algo = Constants.JCA_JCE_MAP.get(algorithm);
    KeyGenerator keyGenerator;

    if (DialogHandler.getSecurityProvider() == null)
      keyGenerator = javax.crypto.KeyGenerator.getInstance(algo.substring(0, algo.indexOf('/')));
    else
      keyGenerator = javax.crypto.KeyGenerator.getInstance(algo.substring(0, algo.indexOf('/')),
                                                           DialogHandler.getSecurityProvider());

    if (algorithm.equals(Constants.SYMMETRIC_CIPHER_ALGORITHM_AES128))
      keyGenerator.init(128);
    else if (algorithm.equals(Constants.SYMMETRIC_CIPHER_ALGORITHM_AES192))
      keyGenerator.init(192);
    else if (algorithm.equals(Constants.SYMMETRIC_CIPHER_ALGORITHM_AES256))
      keyGenerator.init(256);
    else if (algorithm.equals(Constants.SYMMETRIC_CIPHER_ALGORITHM_AES128_GCM))
      keyGenerator.init(128);
    else if (algorithm.equals(Constants.SYMMETRIC_CIPHER_ALGORITHM_AES192_GCM))
      keyGenerator.init(192);
    else if (algorithm.equals(Constants.SYMMETRIC_CIPHER_ALGORITHM_AES256_GCM))
      keyGenerator.init(256);

    return keyGenerator.generateKey();
  }

  /**
   * undocumented
   *
   * @param data undocumented
   * @param algorithm undocumented
   * @return undocumented
   */
  public static javax.crypto.SecretKey createSymKey(byte[] data, String algorithm)
  {
    String algo = Constants.JCA_JCE_MAP.get(algorithm);

    return new javax.crypto.spec.SecretKeySpec(data, algo.substring(0, algo.indexOf('/')));
  }

  /**
   * undocumented
   *
   * @param b undocumented
   * @return undocumented
   */
  public static String toHex(byte[] b)
  {
    StringBuffer s = new StringBuffer();
    int j;

    for ( int i = 0 ; i < b.length ; i++ )
    {
      // oberes nibble
      j = (b[i] >> 4) & 0xf;
      s.append(hex.charAt(j));
      // unteres nibble
      j = b[i] & 0xf;
      s.append(hex.charAt(j));
    }

    return s.toString();
  }

  /**
   * undocumented
   *
   * @param key undocumented
   * @return undocumented
   * @throws OSCICipherException undocumented
   * @throws NoSuchAlgorithmException undocumented
   */
  public static byte[] doRSAEncryption(java.security.cert.X509Certificate encryptionCert, Key key)
    throws OSCICipherException, NoSuchAlgorithmException
  {
    return doRSAEncryption(encryptionCert, key, Constants.DEFAULT_ASYMMETRIC_CIPHER_ALGORITHM);
  }

  /**
   * undocumented
   *
   * @param key undocumented
   * @return undocumented
   * @throws OSCICipherException undocumented
   * @throws NoSuchAlgorithmException undocumented
   */
  public static byte[] doRSAEncryption(java.security.cert.X509Certificate encryptionCert,
                                       Key key,
                                       String algorithm)
    throws OSCICipherException, NoSuchAlgorithmException
  {
    try
    {
      // byte[] oaepParams = new byte[0];

      Cipher cipher;

      if (DialogHandler.getSecurityProvider() == null)
        cipher = javax.crypto.Cipher.getInstance(Constants.JCA_JCE_MAP.get(algorithm));
      else
        cipher = javax.crypto.Cipher.getInstance(Constants.JCA_JCE_MAP.get(algorithm),
                                                 DialogHandler.getSecurityProvider());

      if (Constants.ASYMMETRIC_CIPHER_ALGORITHM_RSA_OAEP.equals(algorithm))
      {
        PSource.PSpecified pSource = PSource.PSpecified.DEFAULT;
        // if (oaepParams != null) currently no use of optional OAEP-Parameters for encryption
        // pSource = new PSource.PSpecified(oaepParams);

        // default
        String digestAlgorithm = Constants.JCA_JCE_MAP.get(Constants.DIGEST_ALGORITHM_SHA256);

        if (!Constants.DIGEST_ALGORITHM_RIPEMD160.equals(DialogHandler.getDigestAlgorithm())
            && !Constants.DIGEST_ALGORITHM_SHA1.equals(DialogHandler.getDigestAlgorithm()))
        {
          // special handling for SHA-3 (for now)
          if (Constants.DIGEST_ALGORITHM_SHA3_256.equals(DialogHandler.getDigestAlgorithm()))
          {
            digestAlgorithm = Constants.JCA_JCE_MAP.get(Constants.DIGEST_ALGORITHM_SHA256);
          }
          else if (Constants.DIGEST_ALGORITHM_SHA3_384.equals(DialogHandler.getDigestAlgorithm())
                   || Constants.DIGEST_ALGORITHM_SHA3_512.equals(DialogHandler.getDigestAlgorithm()))
          {
            digestAlgorithm = Constants.JCA_JCE_MAP.get(Constants.DIGEST_ALGORITHM_SHA512);
          }
          else
          {
            digestAlgorithm = Constants.JCA_JCE_MAP.get(DialogHandler.getDigestAlgorithm());
          }
        }

        MGF1ParameterSpec mgfParameterSpec = new MGF1ParameterSpec(digestAlgorithm);

        OAEPParameterSpec oaepParameters = new OAEPParameterSpec(digestAlgorithm, "MGF1", mgfParameterSpec,
                                                                 pSource);

        cipher.init(Cipher.WRAP_MODE, encryptionCert.getPublicKey(), oaepParameters);
      }
      else
        cipher.init(Cipher.WRAP_MODE, encryptionCert);

      return cipher.wrap(key);
    }
    catch (NoSuchAlgorithmException ex)
    {
      throw ex;
    }
    catch (Exception ex)
    {
      LOG.warn("Error: ", ex);
      throw new OSCICipherException("encryption_error");
    }
  }

  /**
   * undocumented
   *
   * @param key undocumented
   * @param data undocumented
   * @return undocumented
   * @throws OSCICipherException undocumented
   * @throws NoSuchAlgorithmException undocumented
   */
  public static byte[] doRSADecryption(Key key, byte[] data)
    throws OSCICipherException, NoSuchAlgorithmException
  {
    return doRSADecryption(key, data, Constants.ASYMMETRIC_CIPHER_ALGORITHM_RSA_1_5, null, null, null);
  }

  /**
   * undocumented
   *
   * @param key undocumented
   * @param data undocumented
   * @return undocumented
   * @throws OSCICipherException undocumented
   * @throws NoSuchAlgorithmException undocumented
   */
  public static byte[] doRSADecryption(Key key,
                                       byte[] data,
                                       String algorithm,
                                       String mgfAlgorithm,
                                       String digestAlgorithm,
                                       byte[] oaepParams)
    throws OSCICipherException, NoSuchAlgorithmException
  {
    try
    {
      Cipher cipher;

      if (DialogHandler.getSecurityProvider() == null)
        cipher = javax.crypto.Cipher.getInstance(Constants.JCA_JCE_MAP.get(algorithm));
      else
        cipher = javax.crypto.Cipher.getInstance(Constants.JCA_JCE_MAP.get(algorithm),
                                                 DialogHandler.getSecurityProvider());

      if (algorithm.equals(Constants.ASYMMETRIC_CIPHER_ALGORITHM_RSA_OAEP))
      {
        PSource.PSpecified pSource = PSource.PSpecified.DEFAULT;
        if (oaepParams != null)
        {
          pSource = new PSource.PSpecified(oaepParams);
        }

        MGF1ParameterSpec mgfParameterSpec;

        if (mgfAlgorithm.equals(Constants.MASK_GENERATION_FUNCTION_1_SHA256))
        {
          mgfParameterSpec = new MGF1ParameterSpec("SHA-256");
        }
        else if (mgfAlgorithm.equals(Constants.MASK_GENERATION_FUNCTION_1_SHA384))
        {
          mgfParameterSpec = new MGF1ParameterSpec("SHA-384");
        }
        else if (mgfAlgorithm.equals(Constants.MASK_GENERATION_FUNCTION_1_SHA512))
        {
          mgfParameterSpec = new MGF1ParameterSpec("SHA-512");
        }
        else
        {
          throw new IllegalArgumentException("Unsupported mask generation function " + mgfAlgorithm);
        }

        OAEPParameterSpec oaepParameters = new OAEPParameterSpec(Constants.JCA_JCE_MAP.get(digestAlgorithm),
                                                                 "MGF1", mgfParameterSpec, pSource);

        cipher.init(Cipher.DECRYPT_MODE, key, oaepParameters);
      }
      else
      {
        cipher.init(Cipher.DECRYPT_MODE, key);
      }

      return cipher.doFinal(data);

    }
    catch (NoSuchAlgorithmException ex)
    {
      throw ex;
    }
    catch (Exception ex)
    {
      // ex.printStackTrace();
      LOG.warn("Error: ", ex);
      throw new OSCICipherException("decryption_error");
    }
  }

  /**
   * @deprecated Diese Methode wird bald nicht mehr vorhanden sein. Besser die Methoden mit Angabe des
   *             Algorithmus benutzen.
   * @param sk undocumented
   * @return undocumented
   */
  @Deprecated
  public static String getCipherAlgoID(SecretKey sk)
  {
    String algo = null;

    if (sk.getAlgorithm().equals("DESede"))
      algo = Constants.SYMMETRIC_CIPHER_ALGORITHM_TDES_CBC;
    else if (sk.getAlgorithm().equals("AES"))
    {
      if ((sk.getEncoded().length * 8) == 128)
        algo = Constants.SYMMETRIC_CIPHER_ALGORITHM_AES128_GCM;
      else if ((sk.getEncoded().length * 8) == 192)
        algo = Constants.SYMMETRIC_CIPHER_ALGORITHM_AES192_GCM;
      else if ((sk.getEncoded().length * 8) == 256)
        algo = Constants.SYMMETRIC_CIPHER_ALGORITHM_AES256_GCM;
    }

    return algo;
  }

  /**
   * Calculate length of encrypted stream based on length of original stream and algorithm. Uses default IV
   * length.
   *
   * @param len length of original stream
   * @param algorithm symmetric cipher algorithm
   * @return length of encrypted stream
   */
  public static long calcSymEncLength(long len, String algorithm)
  {
    return calcSymEncLength(len, algorithm, Constants.DEFAULT_GCM_IV_LENGTH);
  }

  /**
   * Calculate length of encrypted stream based on length of original stream, algorithm and IV length (if
   * necessary, e.g. for AES-GCM).
   *
   * @param len length of original stream
   * @param algorithm symmetric cipher algorithm
   * @param ivLength IV length (use default if not known)
   * @return length of encrypted stream
   */
  public static long calcSymEncLength(long len, String algorithm, int ivLength)
  {
    if (algorithm.equals(Constants.SYMMETRIC_CIPHER_ALGORITHM_TDES_CBC))
    {
      return 16 + ((len / 8) * 8);
    }
    else if (algorithm.endsWith("cbc"))
    {
      return 32 + ((len / 16) * 16);
    }
    else if (algorithm.endsWith("-gcm") && ivLength == 12)
    {
      return 28 + len;
    }
    else
    {
      return 32 + len;
    }
  }

  public static int getKeySize(X509Certificate cert)
  {
    if (cert.getPublicKey().getAlgorithm().equals("EC"))
      return ((ECPublicKey)cert.getPublicKey()).getParams().getOrder().bitLength();
    else
      return ((RSAPublicKey)cert.getPublicKey()).getModulus().bitLength();
  }

  /**
   * Diese Methode prüft, ob mit der verwendeten Schlüssellänge und dem im DialogHandler eingestellten
   * Hashalgorithmus eine Verschlüsselung möglich ist und liefert ggf. den Identifier für SHA-256 statt für
   * SHA-512. Da das Problem z.Zt. (Version 1.6) nur mit den nicht mehr zulässigen 1024-Bit-Schlüsseln in
   * Verbindung mit SHA-512 auftritt, wird diese Methode von der Bibliothek selbst nicht verwendet.
   *
   * @param encryptionCert Verschlüsselungszertifikat
   * @param symmetricCipherAlgo Symmetrischer Verschlüsselungsalgorithmus
   * @return
   */
  public static String getDigestMethodForOAEP(java.security.cert.X509Certificate encryptionCert,
                                              String symmetricCipherAlgo)
  {
    int data_len = 24;

    if (symmetricCipherAlgo.contains("aes"))
      data_len = Integer.parseInt(symmetricCipherAlgo.substring(symmetricCipherAlgo.indexOf("#aes") + 4,
                                                                symmetricCipherAlgo.indexOf("-cbc")));

    int keyLength = ((RSAPublicKey)encryptionCert.getPublicKey()).getModulus().bitLength();

    String digAlgo = DialogHandler.getDigestAlgorithm();

    if (keyLength < data_len + 2 + 2 * (Integer.parseInt(digAlgo.substring(digAlgo.indexOf("#sha") + 4))))
      digAlgo = Constants.DIGEST_ALGORITHM_SHA256;

    return digAlgo;
  }

}
