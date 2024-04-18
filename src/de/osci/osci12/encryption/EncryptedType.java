package de.osci.osci12.encryption;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.osci.osci12.common.Constants;
import de.osci.osci12.signature.KeyInfo;


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
 *
 * @author R. Lindemann, N.Büngener
 * @version 2.4.1
 */
public class EncryptedType
{

  private static Log log = LogFactory.getLog(EncryptedType.class);

  /** ID Dieses Elements. */
  private String id;

  /** Encoding. */
  private String encoding;

  /** MimeType z.B. text/xml. */
  private String mimeType;

  /** Algorithmus der Verschluesselung. Z.B. DES fuer Nachricht oder RSA fuer KEY. */
  private String encryptionMethodAlgorithm;

  /** Laenge des Initialisierungsvektors (in Byte) fuer die Verschluesselung mit AES-GCM */
  private int ivLength = Constants.DEFAULT_GCM_IV_LENGTH;

  /** Notwendiger Status, solange noch Nachrichten ohne IvLength-Element versendet werden duerfen */
  private boolean ivLengthParsed = false;

  /** Weitere Eigenschaften der Verschluesselung. Noch nicht Impl. */

  // private EncryptionProperties encryptionProperties;
  /** CipherData Element. */
  private CipherData cipherData;

  /** Das KeyInfo Element. */
  private KeyInfo keyInfo;

  /**
   * Creates a new EncryptedType object.
   */
  public EncryptedType()
  {}

  /**
   * undocumented
   *
   * @return undocumented
   */
  public String getId()
  {
    return id;
  }

  /**
   * undocumented
   *
   * @param id undocumented
   */
  public void setId(String id)
  {
    this.id = id;
  }

  /**
   * undocumented
   *
   * @param encoding undocumented
   */
  public void setEncoding(String encoding)
  {
    this.encoding = encoding;
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
   * @param mimeType undocumented
   */
  public void setMimeType(String mimeType)
  {
    this.mimeType = mimeType;
  }

  /**
   * undocumented
   *
   * @return undocumented
   */
  public String getMimeType()
  {
    return mimeType;
  }

  /**
   * undocumented
   *
   * @param encryptionMethodAlgorithm undocumented
   */
  public void setEncryptionMethodAlgorithm(String encryptionMethodAlgorithm)
  {
    this.encryptionMethodAlgorithm = encryptionMethodAlgorithm;
  }

  /**
   * undocumented
   *
   * @return undocumented
   */
  public String getEncryptionMethodAlgorithm()
  {
    return encryptionMethodAlgorithm;
  }

  /**
   * undocumented
   *
   * @return undocumented
   */
  public int getIvLength()
  {
    return ivLength;
  }

  /**
   * undocumented
   *
   * @return undocumented
   */
  public void setIvLength(int ivLength)
  {
    this.ivLength = ivLength;
  }
  
  /**
   * undocumented
   *
   * @return undocumented
   */
  public boolean isIvLengthParsed()
  {
    return ivLengthParsed;
  }
  
  /**
   * undocumented
   *
   * @return undocumented
   */
  public void setIvLengthParsed(boolean ivLengthParsed)
  {
    this.ivLengthParsed = ivLengthParsed;
  }

  /**
   * undocumented
   *
   * @param cipherData undocumented
   */
  public void setCipherData(CipherData cipherData)
  {
    this.cipherData = cipherData;
  }

  /**
   * undocumented
   *
   * @return undocumented
   */
  public CipherData getCipherData()
  {
    return cipherData;
  }

  /**
   * undocumented
   *
   * @param keyInfo undocumented
   */
  public void setKeyInfo(KeyInfo keyInfo)
  {
    if (keyInfo == null)
      log.warn("KeyInfo durf nicht null sein.");

    this.keyInfo = keyInfo;
  }

  /**
   * undocumented
   *
   * @return undocumented
   */
  public KeyInfo getKeyInfo()
  {
    return keyInfo;
  }
}
