package de.osci.osci12.encryption;

import java.io.IOException;
import java.io.OutputStream;

import de.osci.osci12.OSCIException;
import de.osci.osci12.common.Constants;
import de.osci.osci12.common.DialogHandler;


/**
 * <p>Copyright © 2023 Governikus GmbH &amp; Co. KG, Germany</p>
 * <p>Erstellt von Governikus GmbH &amp; Co. KG</p>
 * <p>Diese Bibliothek kann von jedermann nach Maßgabe der European Union
 *  Public Licence genutzt werden.</p>
 * <p>
 * Die Lizenzbestimmungen können unter der URL <a href="https://eupl.eu/">https://eupl.eu/</a> abgerufen werden.
 *
 *
 * @author R. Lindemann, N.Büngener
 * @version 2.4.1
 */
public class EncryptedKey extends EncryptedType
{
  //  private static Log log = LogFactory.getLog(EncryptedKey.class);
  /** Empfänger der Nachricht */
  private String recipient;
  /** Name dieses Schlüssels. Kann durch KeyName referenziert werden. */
  private String carriedKeyName;
  /**
   * Referenz auf alle mit diesem Schluessel verschluesselten EncryptedType
   * Elemente.
   */
  private String referenceList;

  public String mgfAlgorithm;

  public String digestAlgorithm;

  private static final String XENC11 = "http://www.w3.org/2009/xmlenc11#";

  String xenc11NSPrefix = "xenc11";

/**
   * Für den Parser
   * @param encryptionMethodAlgorithm
   */
  EncryptedKey()
  {
  }

  /**
   * Creates a new EncryptedKey object.
   *
   * @param encryptionMethodAlgorithm undocumented
   * @param cipherValue undocumented
   */
  public EncryptedKey(String encryptionMethodAlgorithm, CipherValue cipherValue)
  {
    this.setEncryptionMethodAlgorithm(encryptionMethodAlgorithm);
    if (encryptionMethodAlgorithm.equals(Constants.ASYMMETRIC_CIPHER_ALGORITHM_RSA_OAEP))
    {
      // MGF und Hashfunktionen nach DialogHandler
      if (Constants.DIGEST_ALGORITHM_SHA512.equals(DialogHandler.getDigestAlgorithm())
          ||Constants.DIGEST_ALGORITHM_SHA3_384.equals(DialogHandler.getDigestAlgorithm())
          || Constants.DIGEST_ALGORITHM_SHA3_512.equals(DialogHandler.getDigestAlgorithm()))
      {
        mgfAlgorithm = Constants.MASK_GENERATION_FUNCTION_1_SHA512;
        digestAlgorithm = Constants.DIGEST_ALGORITHM_SHA512;
      }
      else if (Constants.DIGEST_ALGORITHM_SHA256.equals(DialogHandler.getDigestAlgorithm())
               || Constants.DIGEST_ALGORITHM_SHA3_256.equals(DialogHandler.getDigestAlgorithm()))
      {
        mgfAlgorithm = Constants.MASK_GENERATION_FUNCTION_1_SHA256;
        digestAlgorithm = Constants.DIGEST_ALGORITHM_SHA256;
      }
      else // default
      {
        mgfAlgorithm = Constants.MASK_GENERATION_FUNCTION_1_SHA256;
        digestAlgorithm = Constants.DIGEST_ALGORITHM_SHA256;
      }
    }
    CipherData cipherData = new CipherData(cipherValue);
    this.setCipherData(cipherData);
  }

  /**
   * undocumented
   *
   * @param recipient undocumented
   */
  public void setRecipient(String recipient)
  {
    this.recipient = recipient;
  }

  /**
   * undocumented
   *
   * @return undocumented
   */
  public String getRecipient()
  {
    return recipient;
  }

  /**
   * undocumented
   *
   * @param carriedKeyName undocumented
   */
  public void setCarriedKeyName(String carriedKeyName)
  {
    this.carriedKeyName = carriedKeyName;
  }

  /**
   * undocumented
   *
   * @return undocumented
   */
  public String getCarriedKeyName()
  {
    return carriedKeyName;
  }

  /**
   * undocumented
   *
   * @param referenceList undocumented
   */
  public void setReferenceList(String referenceList)
  {
    this.referenceList = referenceList;
  }

  /**
   * undocumented
   *
   * @return undocumented
   */
  public String getReferenceList()
  {
    return referenceList;
  }

  /**
   * undocumented
   *
   * @param uRI undocumented
   */
  public void addDataReference(String uRI)
  {
  }

  /**
   * undocumented
   *
   * @param uRI undocumented
   */
  public void addKeyReference(String uRI)
  {
  }

  /**
   * undocumented
   *
   * @param out undocumented
   *
   * @throws IOException undocumented
   * @throws OSCIException undocumented
   */
  public void writeXML(OutputStream out, String ds, String xenc)
                throws IOException,
                       OSCIException
  {
    out.write(("<" + xenc + ":EncryptedKey>").getBytes(Constants.CHAR_ENCODING));
    out.write(("<" + xenc + ":EncryptionMethod Algorithm=\"" + this.getEncryptionMethodAlgorithm() + "\">").getBytes(Constants.CHAR_ENCODING));
    if (this.getEncryptionMethodAlgorithm().equals(Constants.ASYMMETRIC_CIPHER_ALGORITHM_RSA_OAEP))
    {
        out.write(("<" + xenc11NSPrefix + ":MGF xmlns:" + xenc11NSPrefix + "=\"" + XENC11 + "\" Algorithm=\"" + mgfAlgorithm + "\"></" + xenc11NSPrefix + ":MGF>").getBytes(Constants.CHAR_ENCODING));
        out.write(("<" + ds + ":DigestMethod Algorithm=\"" + digestAlgorithm + "\"></" + ds + ":DigestMethod>").getBytes(Constants.CHAR_ENCODING));
    }
    out.write(("</" + xenc + ":EncryptionMethod>").getBytes(Constants.CHAR_ENCODING));
    this.getKeyInfo().writeXML(out, ds, xenc);
    this.getCipherData().writeXML(out, ds, xenc);
    out.write(("</" + xenc + ":EncryptedKey>").getBytes(Constants.CHAR_ENCODING));
  }
}
