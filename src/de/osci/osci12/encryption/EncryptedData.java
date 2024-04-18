package de.osci.osci12.encryption;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.DigestOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.osci.osci12.OSCIException;
import de.osci.osci12.common.Constants;
import de.osci.osci12.common.Constants.LanguageTextEntries;
import de.osci.osci12.common.DialogHandler;
import de.osci.osci12.messageparts.ContentContainer;


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
public class EncryptedData extends EncryptedType
{
  private static Log log = LogFactory.getLog(EncryptedData.class);
  private static byte[] ns;
  public String encNS;
  public String soapNSPrefix = Constants.Namespaces.SOAP.getPrefix();
  public String osciNSPrefix = Constants.Namespaces.OSCI.getPrefix();
  public String dsNSPrefix = Constants.Namespaces.XML_SIG.getPrefix();
  public String xencNSPrefix =  Constants.Namespaces.XML_ENC.getPrefix();
  public String xsiNSPrefix = Constants.Namespaces.XML_SCHEMA.getPrefix();

  static
  {
    try
    {
      ns = (" xmlns:ds=\"http://www.w3.org/2000/09/xmldsig#\" "
            + "xmlns:osci=\"http://www.osci.de/2002/04/osci\" "
            + "xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" "
            + "xmlns:xenc=\"http://www.w3.org/2001/04/xmlenc#\" "
            + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"").getBytes(Constants.CHAR_ENCODING);
    }
    catch (UnsupportedEncodingException e)
    {
      // Jede JVM unterstützt UTF-8
    }
  }
  
  /**
   * Erstellt ein EncryptedData Objekt mit einem CipherValue und der Default-IV-Länge 
   * (siehe {@link Constants#DEFAULT_GCM_IV_LENGTH}).
   * 
   * @param coco zu verschlüsselndes ContentContainer-Objekt
   * @param encryptionMethodAlgorithm Identifier des Verschlüsselungsalgorithmus. Mögliche Werte sind
   *          Constants.SYMMETRIC_CIPHER_ALGORITHM_AES128/192/256_GCM
   * @param iD ID des EncryptedData Objektes
   * @param key symmetrischer Schlüssel für die Nutzdatenverschlüsselung
   */
  public EncryptedData(ContentContainer coco,
                       String encryptionMethodAlgorithm,
                       String iD,
                       javax.crypto.SecretKey key)
  {
    this(coco, encryptionMethodAlgorithm, Constants.DEFAULT_GCM_IV_LENGTH, iD, key);
  }

  /**
   * Erstellt ein EncryptedData Objekt mit einem CipherValue 
   * @param coco zu verschlüsselndes ContentContainer-Objekt
   * @param encryptionMethodAlgorithm Identifier des Verschlüsselungsalgorithmus.
   * Mögliche Werte sind Constants.SYMMETRIC_CIPHER_ALGORITHM_AES128/192/256_GCM
   * @param ivLength Länge des IV in Bytes (siehe {@link Constants#DEFAULT_GCM_IV_LENGTH})
   * @param iD ID des EncryptedData Objektes
   * @param key symmetrischer Schlüssel für die Nutzdatenverschlüsselung
   */
  public EncryptedData(ContentContainer coco, String encryptionMethodAlgorithm, int ivLength, String iD, javax.crypto.SecretKey key)
  {
    if (coco == null)
      throw new IllegalArgumentException(DialogHandler.text.getString(LanguageTextEntries.invalid_firstargument.name()) + " null");

    if (key == null)
      throw new IllegalArgumentException(DialogHandler.text.getString(LanguageTextEntries.invalid_firstargument.name()) + " null");

    CipherValue cipherValue = new CipherValue(coco, key, encryptionMethodAlgorithm, ivLength);
    CipherData cipherData = new CipherData(cipherValue);
    this.setEncryptionMethodAlgorithm(encryptionMethodAlgorithm);
    this.setIvLength(ivLength);
    this.setMimeType("text/xml");
    this.setId(iD);
    setCipherData(cipherData);
  }
  
  /**
   * Erstellt ein EncryptedData Objekt mit einem CipherReference und der Default-IV-Länge 
   * (siehe {@link Constants#DEFAULT_GCM_IV_LENGTH}).
   * 
   * @param cipherReference Cipher Reference Objekt Nutzdatenverschlüsselung
   * @param encryptionMethodAlgorithm Algoritmus
   * @param iD ID des EncryptedData Objektes
   */
  public EncryptedData(CipherReference cipherReference, String encryptionMethodAlgorithm, String iD)
  {
    this(cipherReference, encryptionMethodAlgorithm, Constants.DEFAULT_GCM_IV_LENGTH, iD);
  }

  /**
   * Erstellt ein EncryptedData Objekt mit einem CipherReference
   * @param cipherReference Cipher Reference Objekt Nutzdatenverschlüsselung
   * @param encryptionMethodAlgorithm Algoritmus
   * @param ivLength Länge des IV in Bytes (16 für Abwärtskompatibilität, 12 empfohlen)
   * @param iD ID des EncryptedData Objektes
   */
  public EncryptedData(CipherReference cipherReference, String encryptionMethodAlgorithm, int ivLength, String iD)
  {
    CipherData cipherData = new CipherData(cipherReference);
    this.setEncryptionMethodAlgorithm(encryptionMethodAlgorithm);
    this.setIvLength(ivLength);
    this.setMimeType("text/xml");
    this.setId(iD);
    setCipherData(cipherData);
  }

  /**
   * Ein Konstruktor für den Parser
   * @param iD ID des EncryptedData Objektes
   */
  EncryptedData(String iD)
  {
    this.setMimeType("text/xml");
    this.setId(iD);
  }

  /**
   * undocumented
   *
   * @param refId undocumented
   *
   * @return undocumented
   */
  public EncryptedKey findEncrypedKey(String refId)
  {
    EncryptedKey[] keys = this.getKeyInfo().getEncryptedKeys();

    for (int i = 0; i < keys.length; i++)
    {
      if (log.isDebugEnabled())
        log.debug("KeyInfo Eintrag:" + keys[i].getKeyInfo().getRetrievalMethod().getURI());

      if (log.isDebugEnabled())
        log.debug("Vergleiche refID:\n" + refId + "\nmit:\n" + keys[i].getKeyInfo().getRetrievalMethod().getURI() +
                  "\n" + keys[i].getKeyInfo().getRetrievalMethod().getURI().endsWith(refId));

      //id prefix # wird ingnoriert
      if (keys[i].getKeyInfo().getRetrievalMethod().getURI().endsWith(refId))
        return keys[i];
    }

    return null;
  }

  /**
   * undocumented
   *
   * @param out undocumented
   *
   * @throws IOException undocumented
   * @throws OSCIException undocumented
   */
  public void writeXML(OutputStream out) throws IOException,
                                                OSCIException
  {
    writeXML(out, true);
  }

  /**
   * undocumented
   *
   * @param out undocumented
   * @param inner undocumented
   * @throws IOException undocumented
   * @throws OSCIException undocumented
   */
  public void writeXML(OutputStream out, boolean inner) throws IOException, OSCIException
  {
    out.write(("<" + xencNSPrefix + ":EncryptedData").getBytes(Constants.CHAR_ENCODING));

    if (!(out instanceof DigestOutputStream) && !inner)
    {
      if (encNS == null)
        out.write(ns);
      else
        out.write(encNS.getBytes(Constants.CHAR_ENCODING));
    }

    if ((getId() != null) && !getId().equals(""))
    {
      out.write((" Id=\"" + this.getId() + "\"").getBytes(Constants.CHAR_ENCODING));
    }

    out.write((" MimeType=\"" + this.getMimeType() + "\"><" + xencNSPrefix + ":EncryptionMethod Algorithm=\""
               + this.getEncryptionMethodAlgorithm() + "\">").getBytes(Constants.CHAR_ENCODING));

    // Element nur schreiben, wenn ungleich altem Default-Wert (16), um Abwärtskompatibilität zu wahren
    if (getIvLength() != 16)
    {
      out.write(("<" + Constants.Namespaces.OSCI128.getPrefix() + ":IvLength xmlns:"
                 + Constants.Namespaces.OSCI128.getPrefix() + "=\"" + Constants.Namespaces.OSCI128.getUri()
                 + "\" Value=\"" + getIvLength() + "\"></" + Constants.Namespaces.OSCI128.getPrefix()
                 + ":IvLength>").getBytes(Constants.CHAR_ENCODING));
    }
    out.write(("</" + xencNSPrefix + ":EncryptionMethod>").getBytes(Constants.CHAR_ENCODING));
    
    this.getKeyInfo().writeXML(out, dsNSPrefix, xencNSPrefix);
    this.getCipherData().writeXML(out, dsNSPrefix, xencNSPrefix);
    out.write(("</" + xencNSPrefix + ":EncryptedData>").getBytes(Constants.CHAR_ENCODING));
  }
}
