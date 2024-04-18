package de.osci.osci12.messageparts;

import de.osci.helper.*;
import de.osci.osci12.common.Constants;
import de.osci.osci12.common.Constants.LanguageTextEntries;
import de.osci.osci12.common.DialogHandler;
import de.osci.osci12.encryption.Crypto;
import de.osci.osci12.encryption.OSCICipherException;
import de.osci.osci12.extinterfaces.OSCIDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.Hashtable;


/**
 * <p>
 * Die Attachment-Klasse repräsentiert einen Anhang einer OSCI-Nachricht. Attachments werden in
 * Content-Elementen mittels eines href-Attributs referenziert. Sie besitzen hierfür einen Identifier (refId),
 * der innerhalb der Nachricht, an die das Attachment gehängt wird, eindeutig sein muss.
 * </p>
 * <p>
 * Ein Attachment kann mit einem eigenen symmetrischen schlüssel versehen werden. Hierdurch kann in
 * verschiedenen verschlüsselten Inhaltsdatencontainern (EncryptedDataOSCI) dasselbe Attachment referenziert
 * werden.
 * </p>
 * <p>
 * Copyright © 2023 Governikus GmbH &amp; Co. KG, Germany
 * </p>
 * <p>Erstellt von Governikus GmbH &amp; Co. KG</p>
 * <p>
 * Diese Bibliothek kann von jedermann nach Maßgabe der European Union Public Licence genutzt
 * werden.
 * </p>
 * <p>
 * Die Lizenzbestimmungen können unter der URL <a href="https://eupl.eu/">https://eupl.eu/</a> abgerufen werden.
 * </p>
 *
 * @author R. Lindemann, N.Büngener
 * @version 2.4.1
 * @see Content
 * @see ContentContainer
 */
public class Attachment extends MessagePart
{

  private static Log log = LogFactory.getLog(Attachment.class);

  private static final int STATE_OF_ATTACHMENT_CLIENT = 0;

  public static final int STATE_OF_ATTACHMENT_PARSING = 1;

  public static final int STATE_OF_ATTACHMENT_ENCRYPTED = 2;

  SecretKey secretKey;

  String symmetricCipherAlgorithm = Constants.DEFAULT_SYMMETRIC_CIPHER_ALGORITHM;
  
  int ivLength = Constants.DEFAULT_GCM_IV_LENGTH;

  OSCIDataSource swapBuffer;

  /** Setzt den Status ob das Attachment verschlüsselt eingestellt werden soll */
  boolean encrypt;

  private String contentType = "application/octet-stream";

  private Hashtable<String, byte[]> encryptedDigestValues = new Hashtable<String, byte[]>();

  int stateOfAttachment = STATE_OF_ATTACHMENT_CLIENT;

  private String boundary_string = DialogHandler.boundary;

  protected boolean base64 = false;

  Hashtable<String, String> mimeHeaders;

  /**
   * Dieser Konstruktor wird von dem Parser aufgerufen
   *
   * @param ins
   * @param refId
   */
  Attachment(java.io.InputStream ins, String refId, long length, String transportDigestAlgorithm)
    throws IllegalArgumentException, IOException, NoSuchAlgorithmException
  {
    if (log.isDebugEnabled())
    {
      log.debug("Entry Attachment(java.io.InputStream ins, String refId, long length, String transportDigestAlgorithm) with ID " + refId);
    }
    setRefID(refId);
    this.length = length;

    if (ins != null)
    {
      this.setInputStream(ins, false, length, transportDigestAlgorithm);
    }
  }
  
  /**
   * Erzeugt ein neues Attachment-Objekt aus dem InputStream.
   *
   * @param ins der InputStream, aus dem die Daten gelesen und an die Nachricht angehängt werden
   * @param refId Identifier des Anhangs, z.B. Dateiname
   * @param symmetricCipherAlgorithm symmetrischer Verschlüsselungsalgorithmus Die möglichen Werte sind
   *          Constants.SYMMETRIC_CIPHER_ALGORITHM_AES128,-192,-256_GCM
   * @see de.osci.osci12.common.Constants
   * @throws IllegalArgumentException undocumented
   * @throws IOException undocumented
   * @throws NoSuchAlgorithmException undocumented
   */
  public Attachment(java.io.InputStream ins, String refId, String symmetricCipherAlgorithm)
    throws IllegalArgumentException, IOException, NoSuchAlgorithmException
  {
    this(ins, refId, de.osci.osci12.encryption.Crypto.createSymKey(symmetricCipherAlgorithm),
         symmetricCipherAlgorithm, Constants.DEFAULT_GCM_IV_LENGTH);
  }

  /**
   * Erzeugt ein neues Attachment-Objekt aus dem InputStream.
   *
   * @param ins der InputStream, aus dem die Daten gelesen und an die Nachricht angehängt werden
   * @param refId Identifier des Anhangs, z.B. Dateiname
   * @param symmetricCipherAlgorithm symmetrischer Verschlüsselungsalgorithmus Die möglichen Werte sind
   *          Constants.SYMMETRIC_CIPHER_ALGORITHM_AES128,-192,-256_GCM
   * @param ivLength Länge des IV in Bytes (16 für Abwärtskompatibilität, 12 empfohlen)
   * @see de.osci.osci12.common.Constants
   * @throws IllegalArgumentException undocumented
   * @throws IOException undocumented
   * @throws NoSuchAlgorithmException undocumented
   */
  public Attachment(java.io.InputStream ins, String refId, String symmetricCipherAlgorithm, int ivLength)
    throws IllegalArgumentException, IOException, NoSuchAlgorithmException
  {
    this(ins, refId, de.osci.osci12.encryption.Crypto.createSymKey(symmetricCipherAlgorithm),
         symmetricCipherAlgorithm, ivLength);
  }

  /**
   * @deprecated Erzeugt ein neues Attachment-Objekt aus dem InputStream. Der geheime schlüssel wird für die
   *             Verschlüsselung des Attachments benutzt.In Zukunft bitte
   *             {@link #Attachment(InputStream, String, SecretKey, String)} benutzen
   * @param ins der InputStream, aus dem die Daten gelesen und an die Nachricht angehängt werden
   * @param refId Identifier des Anhangs, z.B. Dateiname. Dieser Identifier muss innerhalb der Nachricht, an
   *          die das Attachment gehängt wird, eindeutig sein
   * @param secretKey der Secret-Key mit dem verschlüsselt werden soll Der übergebene schlüssel muss daher für
   *          diesem Algorithmus anwendbar sein Wird dieser Parameter mit 'null' übergeben, wird ein neuer
   *          AES-256-schlüssel erzeugt
   * @throws IllegalArgumentException undocumented
   * @throws IOException undocumented
   * @throws NoSuchAlgorithmException undocumented
   */
  @Deprecated
  public Attachment(java.io.InputStream ins, String refId, SecretKey secretKey)
    throws IllegalArgumentException, IOException, NoSuchAlgorithmException
  {
    this(ins, refId, secretKey, Crypto.getCipherAlgoID(secretKey), Constants.DEFAULT_GCM_IV_LENGTH);
  }
  
  /**
   * Erzeugt ein neues Attachment-Objekt aus dem InputStream. Der geheime schlüssel wird für die
   * Verschlüsselung des Attachments benutzt.
   *
   * @param ins der InputStream, aus dem die Daten gelesen und an die Nachricht angehängt werden
   * @param refId Identifier des Anhangs, z.B. Dateiname. Dieser Identifier muss innerhalb der Nachricht, an
   *          die das Attachment gehängt wird, eindeutig sein
   * @param secretKey der Secret-Key mit dem verschlüsselt werden soll
   * @param symmetricCipherAlgorithm der symetrische Verschlüsselungs-Algorithmus passend zu dem Secret Key
   *          Der übergebene schlüssel muss daher für diesem Algorithmus anwendbar sein Wird dieser Parameter
   *          mit 'null' übergeben, wird ein neuer AES-256-schlüssel erzeugt
   * @throws IllegalArgumentException undocumented
   * @throws IOException undocumented
   * @throws NoSuchAlgorithmException undocumented
   * @since 1.7.0
   */
  public Attachment(java.io.InputStream ins,
                    String refId,
                    SecretKey secretKey,
                    String symmetricCipherAlgorithm)
    throws IllegalArgumentException, IOException, NoSuchAlgorithmException
  {
    this(ins, refId, secretKey, symmetricCipherAlgorithm, Constants.DEFAULT_GCM_IV_LENGTH);
  }

  /**
   * Erzeugt ein neues Attachment-Objekt aus dem InputStream. Der geheime schlüssel wird für die
   * Verschlüsselung des Attachments benutzt.
   *
   * @param ins der InputStream, aus dem die Daten gelesen und an die Nachricht angehängt werden
   * @param refId Identifier des Anhangs, z.B. Dateiname. Dieser Identifier muss innerhalb der Nachricht, an
   *          die das Attachment gehängt wird, eindeutig sein
   * @param secretKey der Secret-Key mit dem verschlüsselt werden soll
   * @param symmetricCipherAlgorithm der symetrische Verschlüsselungs-Algorithmus passend zu dem Secret Key
   *          Der übergebene schlüssel muss daher für diesem Algorithmus anwendbar sein Wird dieser Parameter
   *          mit 'null' übergeben, wird ein neuer AES-256-schlüssel erzeugt
   * @param ivLength Länge des IV in Bytes (16 für Abwärtskompatibilität, 12 empfohlen)
   * @throws IllegalArgumentException undocumented
   * @throws IOException undocumented
   * @throws NoSuchAlgorithmException undocumented
   * @since 1.7.0
   */
  public Attachment(java.io.InputStream ins,
                    String refId,
                    SecretKey secretKey,
                    String symmetricCipherAlgorithm, int ivLength)
    throws IllegalArgumentException, IOException, NoSuchAlgorithmException
  {
    if (log.isDebugEnabled())
    {
      log.debug(
        "Entry Attachment(java.io.InputStream ins, String refId, SecretKey secretKey, String symmetricCipherAlgorithm, int ivLength) with ID "
        + refId);
    }
    if (ins == null)
    {
      throw new IllegalArgumentException(DialogHandler.text.getString(LanguageTextEntries.invalid_firstargument.name()) + " ins");
    }

    if (refId == null)
    {
      throw new IllegalArgumentException(DialogHandler.text.getString(LanguageTextEntries.invalid_firstargument.name()) + " refId");
    }
    if (symmetricCipherAlgorithm == null)
    {
      throw new IllegalArgumentException(DialogHandler.text.getString("invalid_fourthargument")
                                         + " symmetricCipherAlgorithm");
    }
    this.ivLength = ivLength;

    this.symmetricCipherAlgorithm = symmetricCipherAlgorithm;
    if (secretKey == null)
    {
      this.secretKey = de.osci.osci12.encryption.Crypto.createSymKey(symmetricCipherAlgorithm);
    }
    else
    {
      if (!secretKey.getAlgorithm().equals("DESede") && !secretKey.getAlgorithm().equals("AES"))
        throw new IllegalArgumentException(DialogHandler.text.getString("encryption_algorithm_not_supported"));

      this.secretKey = secretKey;
    }

    this.encrypt = true;
    setRefID(refId);

    if (log.isDebugEnabled())
      log.debug("RefId Name: " + refId);

    this.makeTempFile(ins);
  }

  /**
   * Erzeugt ein neues Attachment-Objekt aus dem InputStream. Das Attachment wird unverschlüsselt übertragen.
   *
   * @param ins der InputStream, aus dem die Daten gelesen und an die Nachricht angehängt werden.
   * @param refId Identifier des Anhangs, z.B. Dateiname. Dieser Identifier muss innerhalb der Nachricht, an
   *          die das Attachment gehängt wird, eindeutig sein.
   * @throws IllegalArgumentException undocumented
   * @throws IOException undocumented
   * @throws NoSuchAlgorithmException undocumented
   */
  public Attachment(java.io.InputStream ins, String refId)
    throws IllegalArgumentException, IOException, NoSuchAlgorithmException
  {
    if (log.isDebugEnabled())
    {
      log.debug("Entry Attachment(java.io.InputStream ins, String refId) with ID " + refId);
    }

    if (ins == null)
      throw new IllegalArgumentException(DialogHandler.text.getString(LanguageTextEntries.invalid_firstargument.name()) + " null");

    if (refId == null)
      throw new IllegalArgumentException(DialogHandler.text.getString(LanguageTextEntries.invalid_firstargument.name()) + " null");

    this.encrypt = false;
    setRefID(refId);

    if (log.isDebugEnabled())
      log.debug("RefId Name: " + refId);

    this.makeTempFile(ins);
  }

  /**
   * Setzt das refID-Attribut des Attachments. Da der hier verwendete String auch als Referenz im XML der
   * OSCI-Nachricht verwendet wird, empfiehlt sich die Anwendung von URL-encoding.
   *
   * @param id refID-String
   * @see #getRefID()
   * @see java.net.URLEncoder
   */
  public void setRefID(String id)
  {
    super.setRefID(id);
  }

  /**
   * Liefert das refID-Attribut des Attachments. Da für die verwendeten Strings URL-encoding empfohlen wird,
   * sollte ggf. URL-decoding angewendet werden.
   *
   * @see #setRefID(String)
   * @see java.net.URLDecoder
   */
  public String getRefID()
  {
    return super.getRefID();
  }


  /**
   * Diese Methode liefert die Länge des Attachments in Byte.
   *
   * @return Länge des Attachments
   */
  public long getLength()
  {
    if (base64)
      return Base64.calcB64Length(this.length);
    else

      return this.length;
  }

  /**
   * Liefert den InputStream der Daten zuRück, wenn das Attachment einer empfangenen Nachricht entnommen
   * wurde.
   *
   * @return InputStream der Anhangsdaten
   * @throws OSCICipherException undocumented
   * @throws IOException undocumented
   *
   */
  public InputStream getStream() throws OSCICipherException, IOException
  {
    if (log.isDebugEnabled())
    {
      log.debug("entry getStream for attachment " + getRefID());
    }

    if (swapBuffer == null)
    {
      if (log.isDebugEnabled())
      {
        log.debug("Swapbuffer is null, return null for attachment " + getRefID());
      }
      return null;
    }

    InputStream is = null;
    is = swapBuffer.getInputStream();
    is.reset();

    if (encrypt || (stateOfAttachment == STATE_OF_ATTACHMENT_ENCRYPTED))
    {
      if ((secretKey == null) && (stateOfAttachment != STATE_OF_ATTACHMENT_PARSING))
      {
        throw new OSCICipherException("no_secret_key");
      }

      if (log.isDebugEnabled())
        log.debug("Attachment ist Base64 codiert.");

      if (log.isDebugEnabled())
        log.debug("Attachment ist verschlüsselt codiert.");

      return new SymCipherInputStream(is, secretKey, symmetricCipherAlgorithm, ivLength, false);
    }
    else
    {
      if (log.isDebugEnabled())
      {
        log.debug("Daten sind nicht verschlüsselt");
      }
      return is;
    }
  }

  /**
   * Diese Methode liefert den Content der Binärdaten im Mime-Format.
   *
   * @return Content-Type der Daten #setContentType(String)
   */
  public String getContentType()
  {
    return contentType;
  }

  /**
   * Diese Methode setzt den Content-Type (MIME-Content-Type) der Daten
   *
   * @param contentType Content-Type der Daten, sollte ein gültiges Mime-Format sein (z.B. 'text/html' oder
   *          'image/gif') #getContentType()
   */
  public void setContentType(String contentType)
  {
    this.contentType = contentType;
  }

  /**
   * Diese Methode wird nur vom Parser aufgerufen
   *
   * @param ins Inputstream des Attachment
   * @param encrypted Status des Attachments (sind die Daten Verschlüsselt)
   * @param length Laenge des Stream
   */
  void setInputStream(InputStream ins, boolean encrypted, long length, String transportDigestAlgorithm)
    throws IOException, NoSuchAlgorithmException
  {
    if (log.isDebugEnabled())
      log.debug("entry SetStream for attachment " + getRefID());

    this.length = length;
    this.encrypt = encrypted;

    if (ins != null)
    {
      if (log.isDebugEnabled())
      {
        log.debug("InputStream found for attachment " + getRefID());
      }
      swapBuffer = DialogHandler.getNewDataBuffer();
      OutputStream out = swapBuffer.getOutputStream();
      MessageDigest encMsgDigest = null;

      if (transportDigestAlgorithm != null)
      {

        if (DialogHandler.getSecurityProvider() == null)
          encMsgDigest = MessageDigest.getInstance(Constants.JCA_JCE_MAP.get(transportDigestAlgorithm));
        else
          encMsgDigest = MessageDigest.getInstance(Constants.JCA_JCE_MAP.get(transportDigestAlgorithm),
                                                   DialogHandler.getSecurityProvider());
        if (log.isDebugEnabled())
        {
          log.debug("Create signed output stream for attachment " + getRefID());
        }
        out = new DigestOutputStream(out, encMsgDigest);
      }

      java.io.BufferedInputStream bufferdIn = new java.io.BufferedInputStream(ins, Constants.DEFAULT_BUFFER_BLOCKSIZE);
      int count = 0;
      byte[] bytesIn = new byte[Constants.DEFAULT_BUFFER_BLOCKSIZE];

      while ((count = bufferdIn.read(bytesIn)) > -1)
      {
        out.write(bytesIn, 0, count);
      }

      if (log.isDebugEnabled())
      {
        log.debug("Written " + count + " bytes into stream for attachment " + getRefID());
      }

      ins.close();
      out.close();
      bufferdIn.close();
      if (transportDigestAlgorithm != null)
        encryptedDigestValues.put(transportDigestAlgorithm, encMsgDigest.digest());
      this.length = swapBuffer.getLength();
    }
    else
    {
      log.error("No Attachment InputStream found for attachment " + getRefID());
      throw new IllegalArgumentException(DialogHandler.text.getString(LanguageTextEntries.invalid_firstargument.name()) + " null");
    }
  }

  /**
   * Diese Methode liest die Daten aus dem InputStream und verschlüsselt sie und ermittelt den Digest des
   * Attachments. Das Verschlüsselte Ergebnis wird in einen OSCIDataSource geschrieben.
   *
   * @throws IOException
   * @throws NoSuchAlgorithmException
   */
  private void makeTempFile(InputStream ins) throws IOException, NoSuchAlgorithmException
  {
    if (swapBuffer != null)
    {
      return;
    }

    if (log.isDebugEnabled())
      log.debug("MakeTempFile wurde aufgerufen.");

    swapBuffer = DialogHandler.getNewDataBuffer();

    OutputStream out = swapBuffer.getOutputStream();
    MessageDigest encMsgDigest = null;
    MessageDigest msgDigest = null;

    if (DialogHandler.getSecurityProvider() == null)
      msgDigest = MessageDigest.getInstance(Constants.JCA_JCE_MAP.get(DialogHandler.getDigestAlgorithm()));
    else
      msgDigest = MessageDigest.getInstance(Constants.JCA_JCE_MAP.get(DialogHandler.getDigestAlgorithm()),
                                            DialogHandler.getSecurityProvider());

    if (encrypt)
    {
      if (DialogHandler.getSecurityProvider() == null)
        encMsgDigest = MessageDigest.getInstance(Constants.JCA_JCE_MAP.get(DialogHandler.getDigestAlgorithm()));
      else
        encMsgDigest = MessageDigest.getInstance(Constants.JCA_JCE_MAP.get(DialogHandler.getDigestAlgorithm()),
                                                 DialogHandler.getSecurityProvider());

      if (log.isDebugEnabled())
        log.debug("Verschlüsseltes Attachment wird erstellt");

      out = new DigestOutputStream(out, encMsgDigest);
      out = new SymCipherOutputStream(out, secretKey, symmetricCipherAlgorithm, ivLength, true);
    }
    else if (log.isDebugEnabled())
      log.debug("Unverschlüsseltes Attachment wird erstellt");

    out = new DigestOutputStream(out, msgDigest);

    java.io.BufferedInputStream bufferdIn = new java.io.BufferedInputStream(ins, Constants.DEFAULT_BUFFER_BLOCKSIZE);
    int count = 0;
    byte[] bytesIn = new byte[Constants.DEFAULT_BUFFER_BLOCKSIZE];
    length = 0;

    while ((count = bufferdIn.read(bytesIn)) > -1)
      out.write(bytesIn, 0, count);

    out.flush();
    swapBuffer.getOutputStream().flush();
    out.close();
    bufferdIn.close();
    digestValues.put(DialogHandler.getDigestAlgorithm(), msgDigest.digest());

    if (encrypt)
      encryptedDigestValues.put(DialogHandler.getDigestAlgorithm(), encMsgDigest.digest());
    else
      encryptedDigestValues.put(DialogHandler.getDigestAlgorithm(),
                                digestValues.get(DialogHandler.getDigestAlgorithm()));

    length = swapBuffer.getLength();
  }

  /**
   * hasDigestValue
   * @param digestAlgorithm Digest Algorithmus
   * @return undocumented
   */
  public boolean hasDigestValue(String digestAlgorithm)
  {
    return digestValues.get(digestAlgorithm) != null;
  }

  /**
   * Liefert den Hashwert des Attachments.
   *
   * @return Hashwert des Attachments
   */
  public byte[] getDigestValue(String digestAlgorithm) throws IOException, NoSuchAlgorithmException
  {
    if (log.isDebugEnabled())
      log.debug("Digest vorhanden? " + digestValues.get(digestAlgorithm) + " stateOfObjekt:"
                + stateOfAttachment);

    if (digestValues.get(digestAlgorithm) == null)
    {
      digestValues.put(digestAlgorithm, createDigest(digestAlgorithm, encrypt));
    }

    if (log.isDebugEnabled())
      log.debug("Der Digest Value des Attachment ist: "
                + new String((byte[])digestValues.get(digestAlgorithm)));

    return (byte[])digestValues.get(digestAlgorithm);
  }

  private byte[] createDigest(String digestAlgorithm, boolean enc)
    throws IOException, NoSuchAlgorithmException
  {
    InputStream in = null;
    in = swapBuffer.getInputStream();

    if (in == null)
    {
      throw new IOException(DialogHandler.text.getString("io_exception"));
    }

    in.reset();

    MessageDigest msgDigest;

    if (DialogHandler.getSecurityProvider() == null)
      msgDigest = MessageDigest.getInstance(Constants.JCA_JCE_MAP.get(digestAlgorithm));
    else
      msgDigest = MessageDigest.getInstance(Constants.JCA_JCE_MAP.get(digestAlgorithm),
                                            DialogHandler.getSecurityProvider());

    if (log.isDebugEnabled())
      log.debug("State of Attachment: " + stateOfAttachment + " Verschlüsselt: " + encrypt);

    InputStream inStream = in;

    if (!(stateOfAttachment == STATE_OF_ATTACHMENT_PARSING) && enc)
    {
      if (log.isDebugEnabled())
        log.debug("Das Attachment ist verschlüsselt.");

      inStream = new SymCipherInputStream(in, secretKey, symmetricCipherAlgorithm, ivLength, false);
    }

    DigestOutputStream out = new DigestOutputStream(new NullOutputStream(), msgDigest);
    byte[] bytes = new byte[Constants.DEFAULT_BUFFER_BLOCKSIZE];
    int count = Constants.DEFAULT_BUFFER_BLOCKSIZE;

    while ((count = inStream.read(bytes)) > -1)
      out.write(bytes, 0, count);

    inStream.close();
    out.close();
    return msgDigest.digest();
  }

  /**
   * Setzt den MIME-Trennstring. Wird von Anwendungen normalerweise nicht aufgerufen.
   *
   * @param boundary Trenner
   */
  public void setBoundary(String boundary)
  {
    boundary_string = boundary;
  }

  /**
   * Setzt das Encoding dea MIME-Boundary-Abschnitts auf Base64.
   *
   * @param b64 true, Daten werden Base64 codiert.
   */
  public void setBase64Encoding(boolean b64)
  {
    base64 = b64;
  }

  /**
   * Interne Methode, wird von Anwendungen normalerweise nicht aufgerufen. Sie serialisiert das MIME-Boundary
   * des Attachment.
   *
   * @param out Outputstream, in den das Attachment serialisiert wird
   * @exception IOException
   */
  protected void writeXML(java.io.OutputStream out) throws java.io.IOException
  {
    out.write(("\r\n--" + boundary_string + "\r\nContent-Type: " + contentType
               + "\r\nContent-Transfer-Encoding: ").getBytes(Constants.CHAR_ENCODING));
    out.write((base64 ? "base64" : "8-bit").getBytes(Constants.CHAR_ENCODING));
    out.write(("\r\nContent-ID: <" + getRefID() + ">\r\nContent-Length: " + getLength()
               + "\r\n").getBytes(Constants.CHAR_ENCODING));

    if (mimeHeaders != null)
    {
      String key;
      Hashtable<String, String> mh = (Hashtable<String, String>)mimeHeaders.clone();
      removeHeaders(mh);

      for ( Enumeration<String> e = mh.keys() ; e.hasMoreElements() ; )
      {
        key = e.nextElement();
        out.write((key.trim() + ": " + (mh.get(key)).trim() + "\r\n").getBytes(Constants.CHAR_ENCODING));
      }
    }

    out.write(("\r\n").getBytes(Constants.CHAR_ENCODING));

    if (log.isDebugEnabled())
      log.debug("######### SwapBuffero#### " + swapBuffer);

    InputStream in = swapBuffer.getInputStream();
    in.reset();

    if (base64)
      out = new Base64OutputStream(out, true);

    int count = 0;
    byte[] bytesIn = new byte[Constants.DEFAULT_BUFFER_BLOCKSIZE];

    while ((count = in.read(bytesIn)) > -1)
      out.write(bytesIn, 0, count);

    in.close();

    if (base64)
      out.flush();
  }

  /**
   * Liefert true, wenn es sich um ein verschlüsseltes Attachment handelt.
   *
   * @return verschlüsselt = true, unverschlüsselt = false
   */
  public boolean isEncrypted()
  {
    return encrypt;
  }

  /**
   * Liefert den SymmetricCipherAlgorithm des secret keys
   *
   * @return Algorithmus
   */
  public String getSymmetricCipherAlgorithm()
  {
    return symmetricCipherAlgorithm;
  }

  /**
   * Setzt den SymmetricCipherAlgorithm des secret keys
   *
   * @param symmetricCipherAlgorithm Algo
   */
  public void setSymmetricCipherAlgorithm(String symmetricCipherAlgorithm)
  {
    this.symmetricCipherAlgorithm = symmetricCipherAlgorithm;
  }

  /**
   * Liefert den Hashwert nach der Verschlüsselung für die Nachrichtensignatur.
   *
   * @param digestAlgorithm Digest Algorithmus
   * @return byte[] Hashwert
   * @throws IOException undocumented
   * @throws NoSuchAlgorithmException undocumented
   */
  public byte[] getEncryptedDigestValue(String digestAlgorithm) throws IOException, NoSuchAlgorithmException
  {
    if (encryptedDigestValues.get(digestAlgorithm) == null)
      encryptedDigestValues.put(digestAlgorithm, createDigest(digestAlgorithm, false));

    if (log.isDebugEnabled())
      log.debug(new String((byte[])encryptedDigestValues.get(digestAlgorithm)));

    return (byte[])encryptedDigestValues.get(digestAlgorithm);
  }

  /**
   * Liefert eine Hashtable mit den MIME-Headereinträgen des MIME-boundary. Nicht enthalten sind die
   * Headereinträge "Content-Transfer-Encoding", "Content-ID", "Content-Length" und "Content-Type".
   * Abschnitts.
   *
   * @return Header
   */
  public Hashtable<String, String> getMimeHeaders()
  {
    return mimeHeaders;
  }

  /**
   * Setzt zusätzliche MIME-Headereinträge für den MIME-boundary Abschnitt des Attachments. Die key- und
   * value-Strings der Hashtable müssen den MIME-Spezifikationen genügen. Die Header
   * Content-Transfer-Encoding, Content-ID, Content-Length und Content-Type werden von der Implementierung
   * gesetzt und werden ignoriert.
   *
   * @param headers MIME-Header als key-value-Paare
   * @see #setContentType(String)
   */
  public void setMimeHeaders(Hashtable<String, String> headers)
  {
    removeHeaders(headers);
    mimeHeaders = headers;
  }

  private void removeHeaders(Hashtable<String, String> ht)
  {
    String[] hd = new String[]{"Content-Transfer-Encoding", "Content-ID", "Content-Length", "Content-Type"};
    String key;

    for ( Enumeration<String> e = ht.keys() ; e.hasMoreElements() ; )
    {
      key = e.nextElement();

      for ( int i = 0 ; i < hd.length ; i++ )
      {
        if (key.equalsIgnoreCase(hd[i]))
          ht.remove(key);
      }
    }
  }
  
  /**
   * IV-Länge für Ver/Entschlüsselung mit AES-GCM.
   *
   */
  public int getIvLength()
  {
    return ivLength;
  }
  
  
  /**
   * Setze nachträglich IV-Länge (für AES-GCM, z.B. wenn Verschlüsselung erst im Nachhinein erfolgt).
   * 
   * @param ivLength
   */
  public void setIvLength(int ivLength)
  {
    this.ivLength = ivLength;
  }
}
