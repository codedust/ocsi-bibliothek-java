package de.osci.helper;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.SecretKey;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.osci.osci12.common.Constants;
import de.osci.osci12.common.Constants.LanguageTextEntries;
import de.osci.osci12.common.DialogHandler;
import de.osci.osci12.encryption.Crypto;


/**
 * Diese Erweiterung der Klasse java.io.FilterInputStream führt eine symmetrische Ver-/Entschlüsselung der
 * gelesenen Daten durch. Der Initialisierungsvektor wird gemäß der XML-Encryption-Spezifikation den
 * Inhaltsdaten hinzugefügt bzw. entnommen.
 * <p>
 * Copyright © 2023 Governikus GmbH &amp; Co. KG, Germany
 * </p>
 * <p>
 * Erstellt von Governikus GmbH &amp; Co. KG
 * </p>
 * <p>
 * Diese Bibliothek kann von jedermann nach Maßgabe der European Union Public Licence genutzt werden.
 * </p>
 * Die Lizenzbestimmungen können unter der URL <a href="https://eupl.eu/">https://eupl.eu/</a> abgerufen
 * werden.
 * </p>
 *
 * @author R. Lindemann, N.Büngener
 * @version 2.4.1
 * @see de.osci.helper.SymCipherOutputStream
 */
public class SymCipherInputStream extends FilterInputStream
{

  private static Log log = LogFactory.getLog(SymCipherInputStream.class);

  private Cipher cipher;

  private String symCipherAlgo = null;

  private int count = 0;

  byte[] iv;

  private boolean encrypt;

  private byte[] b = new byte[1];

  /**
   * Creates a new SymCipherInputStream object, use default algorithm for symmetric key (see
   * {@link Crypto.getCipherAlgoID}).
   *
   * @param inStream undocumented
   * @param symKey undocumented
   * @param symAlgorithm undocumented
   * @param encrypt undocumented
   * @throws IOException undocumented
   * @throws NoSuchAlgorithmException
   * @throws IllegalArgumentException undocumented
   */
  public SymCipherInputStream(InputStream inStream, String symAlgorithm, boolean encrypt)
    throws IOException, NoSuchAlgorithmException
  {
    this(inStream, Crypto.createSymKey(symAlgorithm), symAlgorithm, Constants.DEFAULT_GCM_IV_LENGTH, encrypt);
  }


  /**
   * Creates a new SymCipherInputStream object with given algorithm and IV length.
   * 
   * @param inStream
   * @param symKey
   * @param symAlgorithm
   * @param encrypt
   * @throws IOException
   */
  public SymCipherInputStream(InputStream inStream, SecretKey symKey, String symAlgorithm, boolean encrypt)
    throws IOException
  {
    this(inStream, symKey, symAlgorithm, Constants.DEFAULT_GCM_IV_LENGTH, encrypt);
  }


  /**
   * Creates a new SymCipherInputStream object with given algorithm.
   *
   * @param inStream undocumented
   * @param symKey undocumented
   * @param symAlgorithm undocumented
   * @param ivLength undocumented
   * @param encrypt undocumented
   * @throws IOException undocumented
   * @throws IllegalArgumentException undocumented
   */
  public SymCipherInputStream(InputStream inStream,
                              SecretKey symKey,
                              String symAlgorithm,
                              int ivLength,
                              boolean encrypt)
    throws IOException
  {
    super(null);
    symCipherAlgo = symAlgorithm;
    this.encrypt = encrypt;

    if (inStream == null)
      throw new IllegalArgumentException(DialogHandler.text.getString(LanguageTextEntries.invalid_firstargument.name())
                                         + " null");

    if (symKey == null)
      throw new IllegalArgumentException(DialogHandler.text.getString(LanguageTextEntries.invalid_secondargument.name())
                                         + " null");
    try
    {
      if (symAlgorithm == null)
      {
        throw new IllegalArgumentException(DialogHandler.text.getString(LanguageTextEntries.invalid_thirdargument.name())
                                           + " null");
      }

      if (symAlgorithm.endsWith("-cbc"))
      {
        if (ParserHelper.isGCMAlgorithmOnly())
        {
          throw new IllegalArgumentException(DialogHandler.text.getString(LanguageTextEntries.invalid_thirdargument.name())
                                             + " Wrong algorithm! only GCM supported! Given: "
                                             + symAlgorithm);
        }
        else
        {
          log.warn("CBC will not be supported in the future and is not allowed for transport encryption any longer!");
        }
      }

      if (DialogHandler.getSecurityProvider() == null)
        cipher = Cipher.getInstance(Constants.JCA_JCE_MAP.get(symAlgorithm));
      else
        cipher = Cipher.getInstance(Constants.JCA_JCE_MAP.get(symAlgorithm),
                                    DialogHandler.getSecurityProvider());

      if (encrypt)
      {
        cipher.init(Cipher.ENCRYPT_MODE, symKey);
        iv = cipher.getIV();
      }
      else
      {
        if (symCipherAlgo.endsWith("tripledes-cbc"))
        {
          iv = new byte[8];
        }
        else if (symCipherAlgo.endsWith("-gcm"))
        {
          if (ivLength == 16 || ivLength == 12)
          {
            log.debug("Use IV length for incoming AES-GCM-encrypted stream: " + ivLength);
            iv = new byte[ivLength];

            if (ivLength == 16)
            {
              log.warn(DialogHandler.text.getString(LanguageTextEntries.warning_iv_length.name()));
            }
          }
          else
          {
            throw new IllegalArgumentException(DialogHandler.text.getString(LanguageTextEntries.invalid_thirdargument.name())
                                               + " Wrong IV length " + ivLength + " given for algorithm "
                                               + symAlgorithm);
          }
        }
        else
        {
          iv = new byte[16];
        }

        int tmp;

        for ( int i = 0 ; i < iv.length ; i++ )
        {
          tmp = inStream.read();

          if (tmp == -1)
            throw new IOException("Unexpected EOS");

          iv[i] = (byte)tmp;
        }

        javax.crypto.spec.IvParameterSpec algoParamSpec = new javax.crypto.spec.IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, symKey, algoParamSpec);
      }
    }
    catch (GeneralSecurityException ex)
    {
      if (encrypt)
      {
        log.warn("Exception at encryption", ex);
        throw new IOException(DialogHandler.text.getString("encryption_error"));
      }
      else
      {
        log.warn("Exception at decryption", ex);
        throw new IOException(DialogHandler.text.getString("decryption_error"));
      }
    }

    super.in = new CipherInputStream(inStream, cipher);
  }

  /**
   * undocumented
   *
   * @return undocumented
   * @throws IOException undocumented
   */
  @Override
  public int read() throws IOException
  {
    if (read(b, 0, 1) == -1)
      return -1;

    return (b[0]) & 0xff;
  }

  /**
   * undocumented
   *
   * @param b undocumented
   * @param off undocumented
   * @param len undocumented
   * @return undocumented
   * @throws IOException undocumented
   */
  @Override
  public int read(byte[] b, int off, int len) throws IOException
  {
    if (encrypt && (count < iv.length))
    {
      if (len < (iv.length - count))
      {
        System.arraycopy(iv, count, b, off, len);
        count += len;

        return len;
      }
      else
      {
        System.arraycopy(iv, count, b, off, iv.length - count);

        try
        {
          int ret = in.read(b, (off + iv.length) - count, len - iv.length + count);
          ret += (iv.length - count);
          count = iv.length;
          return ret;
        }
        catch (IOException ex)
        {
          if (findException(ex, BadPaddingException.class))
          {
            log.error("Bad padding exception. Return end of stream!!!", ex);
            return -1;
          }
          else
          {
            throw ex;
          }
        }

      }
    }
    else
    {
      try
      {

        return in.read(b, off, len);
      }
      catch (IOException ex)
      {
        if (findException(ex, BadPaddingException.class))
        {
          log.error("Bad padding exception. Return end of stream!!!", ex);
          return -1;
        }
        else
        {
          throw ex;
        }
      }
    }
  }


  /**
   * Get initialization vector
   * 
   * @return
   */
  public byte[] getIv()
  {
    return iv;
  }


  private boolean findException(Throwable ex, Class<? extends Throwable> findEx)
  {
    // nur bei CBC kann es wirklich eine BadPadding Exception geben
    if (symCipherAlgo.endsWith("-gcm"))
    {
      return false;
    }
    while (ex.getCause() != null)
    {
      if (findEx.isInstance(ex.getCause()))
        return true;
      else
        ex = ex.getCause();
    }
    return false;
  }
}
