package de.osci.helper;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.osci.osci12.common.Constants;
import de.osci.osci12.common.Constants.LanguageTextEntries;
import de.osci.osci12.common.DialogHandler;
import de.osci.osci12.encryption.Crypto;


/**
 * Diese Erweiterung der Klasse java.io.FilterOutputStream führt eine symmetrische Ver-/Entschlüsselung der
 * geschriebenen Daten durch. Der Initialisierungsvektor wird gemäß der XML-Encryption-Spezifikation den
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
 * @see de.osci.helper.SymCipherInputStream
 */
public class SymCipherOutputStream extends FilterOutputStream
{

  private static Log log = LogFactory.getLog(SymCipherOutputStream.class);

  private Cipher cipher;

  private int index = 0;

  private byte[] iv;

  private SecretKey symKey;

  private boolean encrypt;

  private byte[] tmp;

  /**
   * Creates a new SymCipherOutputStream object. Detect cipher algorithm by given symmetric key.
   *
   * @deprecated better give symmetric algorithm and iv Length as parameter (see other
   *             constructors).{@link #SymCipherOutputStream(OutputStream, SecretKey, String, int, boolean)}
   * @param outStream undocumented
   * @param symKey undocumented
   * @param encrypt undocumented
   * @throws IOException undocumented
   */
  @Deprecated
  public SymCipherOutputStream(OutputStream outStream, SecretKey symKey, boolean encrypt) throws IOException
  {
    this(outStream, symKey, Crypto.getCipherAlgoID(symKey), encrypt, Constants.DEFAULT_GCM_IV_LENGTH, null);
  }


  /**
   * Creates a new SymCipherOutputStream object.
   *
   * @param outStream undocumented
   * @param symKey undocumented
   * @param symAlgorithm undocumented
   * @param ivLength undocumented
   * @param iv undocumented
   * @throws IOException undocumented
   * @since 1.7.0
   */
  public SymCipherOutputStream(OutputStream outStream,
                               SecretKey symKey,
                               String symAlgorithm,
                               int ivLength,
                               byte[] iv)
    throws IOException
  {
    this(outStream, symKey, symAlgorithm, true, ivLength, iv);
  }


  /**
   * Creates a new SymCipherOutputStream object.
   *
   * @deprecated Diese Methode wird nicht mehr lange verfügbar sein. In Zukunft lieber
   *             {@link #SymCipherOutputStream(OutputStream, SecretKey, String, int, boolean)} benutzen
   * @param outStream undocumented
   * @param symKey undocumented
   * @param encrypt undocumented
   * @throws IOException undocumented
   * @throws NoSuchAlgorithmException
   */
  @Deprecated
  public SymCipherOutputStream(OutputStream outStream, boolean encrypt)
    throws IOException, NoSuchAlgorithmException
  {
    this(outStream, DialogHandler.getTempSymKey(), Constants.DEFAULT_SYMMETRIC_CIPHER_ALGORITHM, encrypt,
         Constants.DEFAULT_GCM_IV_LENGTH, null);
  }


  /**
   * Creates a new SymCipherOutputStream object.
   * 
   * @param outStream
   * @param symKey
   * @param symAlgorithm
   * @param encrypt
   * @throws IOException
   * @since 1.7.0
   */
  public SymCipherOutputStream(OutputStream outStream, SecretKey symKey, String symAlgorithm, boolean encrypt)
    throws IOException
  {
    this(outStream, symKey, symAlgorithm, encrypt, Constants.DEFAULT_GCM_IV_LENGTH, null);
  }


  /**
   * Creates a new SymCipherOutputStream object with given IV length
   * 
   * @param outStream
   * @param symKey
   * @param symAlgorithm
   * @param ivLength
   * @param encrypt
   * @throws IOException
   * @since 1.8.4-internal-2
   */
  public SymCipherOutputStream(OutputStream outStream,
                               SecretKey symKey,
                               String symAlgorithm,
                               int ivLength,
                               boolean encrypt)
    throws IOException
  {
    this(outStream, symKey, symAlgorithm, encrypt, ivLength, null);
  }


  SymCipherOutputStream(OutputStream outStream,
                        SecretKey symKey,
                        String symAlgorithm,
                        boolean encrypt,
                        int ivLength,
                        byte[] iv)
    throws IOException
  {
    super(outStream);
    this.symKey = symKey;
    this.encrypt = encrypt;

    if (symAlgorithm.endsWith("-cbc"))
    {
      if (ParserHelper.isGCMAlgorithmOnly())
      {
        throw new IllegalArgumentException(DialogHandler.text.getString(LanguageTextEntries.invalid_thirdargument.name())
                                           + " Wrong algorithm! only GCM supported! Given: " + symAlgorithm);
      }
      else
      {
        log.warn("CBC will not be supported in the future and is not allowed for transport encryption any longer!");
      }
    }

    try
    {
      if (DialogHandler.getSecurityProvider() == null)
      {
        cipher = Cipher.getInstance(Constants.JCA_JCE_MAP.get(symAlgorithm));
      }
      else
      {
        cipher = Cipher.getInstance(Constants.JCA_JCE_MAP.get(symAlgorithm),
                                    DialogHandler.getSecurityProvider());
      }

      if (symAlgorithm.endsWith("tripledes-cbc"))
      {
        this.iv = new byte[8];
      }
      else if (symAlgorithm.endsWith("-gcm"))
      {
        if (ivLength == 16 || ivLength == 12)
        {
          log.debug("Use IV length for outgoing AES-GCM: " + ivLength);
          this.iv = new byte[ivLength];

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
        this.iv = new byte[16];
      }

      if (encrypt)
      {
        if (iv == null)
          this.iv = Tools.createRawRandom(this.iv.length);
        else
          System.arraycopy(iv, 0, this.iv, 0, this.iv.length);

        IvParameterSpec algoParamSpec = new javax.crypto.spec.IvParameterSpec(this.iv);
        cipher.init(Cipher.ENCRYPT_MODE, symKey, algoParamSpec);

        // this.iv = cipher.getIV();
        outStream.write(this.iv);
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
  }

  /**
   * undocumented
   *
   * @param b undocumented
   * @throws IOException undocumented
   */
  @Override
  public void write(int b) throws IOException
  {
    // out.write(new byte[] { (byte) b }, 0, 1);
    write(new byte[]{(byte)b}, 0, 1);
  }

  /**
   * undocumented
   *
   * @param b undocumented
   * @param off undocumented
   * @param len undocumented
   * @throws IOException undocumented
   * @throws IndexOutOfBoundsException undocumented
   */
  @Override
  public void write(byte[] b, int off, int len) throws IOException
  {
    if ((off | len | (b.length - (len + off)) | (off + len)) < 0)
      throw new IndexOutOfBoundsException();

    if ((!encrypt) && (index < iv.length))
    {
      if (len < (iv.length - index))
      {
        for ( int i = 0 ; i < len ; i++ )
          iv[index++] = b[off + i];
      }
      else
      {
        int i;

        for ( i = 0 ; i < (iv.length - index) ; i++ )
          iv[i + index] = b[off + i];

        index += i;

        try
        {
          IvParameterSpec algoParamSpec = new javax.crypto.spec.IvParameterSpec(iv);
          cipher.init(Cipher.DECRYPT_MODE, symKey, algoParamSpec);
          tmp = cipher.update(b, off + i, len - i);

          if (tmp != null)
            out.write(tmp);
        }
        catch (Exception ex)
        {
          throw new IOException(DialogHandler.text.getString("decryption_error"));
        }
      }
    }
    else
    {
      // out.write(b, off, len);
      if (b.length > 0)
      {
        tmp = cipher.update(b, off, len);

        if (tmp != null)
          out.write(tmp);
      }
    }
  }

  /**
   * undocumented
   *
   * @throws IOException undocumented
   */
  @Override
  public void close() throws IOException
  {
    try
    {
      out.write(cipher.doFinal());
    }
    catch (Exception ex)
    {
      if (encrypt)
        throw new IOException(DialogHandler.text.getString("encryption_error"));
      else
        throw new IOException(DialogHandler.text.getString("decryption_error"));
    }

    out.flush();
  }

  /**
   * undocumented
   *
   * @return undocumented
   */
  public byte[] getIv()
  {
    return iv;
  }
}
