package de.osci.osci12.messageparts;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Vector;

import javax.crypto.SecretKey;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import de.osci.helper.Base64;
import de.osci.helper.Canonizer;
import de.osci.helper.ParserHelper;
import de.osci.helper.SymCipherInputStream;
import de.osci.helper.Tools;
import de.osci.osci12.OSCIException;
import de.osci.osci12.common.Constants;
import de.osci.osci12.common.Constants.LanguageTextEntries;
import de.osci.osci12.common.DialogHandler;
import de.osci.osci12.common.OSCICancelledException;
import de.osci.osci12.encryption.CipherReference;
import de.osci.osci12.encryption.CipherValue;
import de.osci.osci12.encryption.Crypto;
import de.osci.osci12.encryption.EncryptedData;
import de.osci.osci12.encryption.EncryptedKey;
import de.osci.osci12.encryption.OSCICipherException;
import de.osci.osci12.messagetypes.OSCIMessage;
import de.osci.osci12.roles.OSCIRoleException;
import de.osci.osci12.roles.Role;
import de.osci.osci12.signature.KeyInfo;


/**
 * <p>
 * Die EncryptedDataOSCI-Klasse stellt einen Datencontainer für verschlüsselte Daten in einer OSCI-Nachricht
 * dar. Ein EncryptedDataOSCI-Objekt wird in eine OSCI-Nachricht eingestellt oder in einen
 * Content-Container(bei Mehrfachverschlüsselung). Anwendungen können in ein EncryptedDataOSCI-Objekt nur
 * Content-Container (zum Verschlüsseln) einstellen.
 * </p>
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
 * <p>
 * Die Lizenzbestimmungen können unter der URL <a href="https://eupl.eu/">https://eupl.eu/</a> abgerufen werden.
 * </p>
 *
 * @author R. Lindemann, N.Büngener
 * @version 2.4.1
 * @see ContentContainer
 * @see Attachment
 */
public class EncryptedDataOSCI extends MessagePart
{

  private static Log log = LogFactory.getLog(EncryptedDataOSCI.class);

  private static int idNr = -1;

  private int keyIDs = 0;

  /**
   * EncryptedData-Objekt enthält eine Referenz auf ein Attachment
   */
  private static final int TYPE_OF_DATA_ATTACHMENT = 0;

  /**
   * EncryptedData Objekt enthält einen ContentContainer
   */
  private static final int TYPE_OF_DATA_CONTENTCONTAINER = 1;

  private int typeOfData = -1;

  private static int ENCRYPTEDDATA_START = 0;

  private static int ENCRYPTEDDATA_ENCRYPTED = 1;

  private int stateOfObject = ENCRYPTEDDATA_START;

  private EncryptedData encryptedDataObject = null;

  Vector<Role> roles = new Vector<Role>();

  Vector<Role> readers = new Vector<Role>();

  Vector<Attachment> attachments = new Vector<Attachment>();

  private Hashtable<Role, EncryptedKey> encryptedKeyList = new Hashtable<Role, EncryptedKey>();

  private MessagePart content = null;

  private OSCIMessage msg = null;

  private SecretKey secretKey = null;

  /**
   * Interner Konstruktor für das Hinzufügen von verschlüsselten Attachments zur Nachricht
   *
   * @param attachment
   * @throws IllegalArgumentException
   * @throws IOException
   */
  EncryptedDataOSCI(Attachment attachment) throws OSCICipherException, IOException
  {
    typeOfData = TYPE_OF_DATA_ATTACHMENT;

    if (attachment == null)
      throw new IllegalArgumentException(DialogHandler.text.getString(LanguageTextEntries.invalid_thirdargument.name())
                                         + " attachment = null");

    typeOfData = TYPE_OF_DATA_ATTACHMENT;
    this.attachments.add(attachment);

    if (!attachment.isEncrypted())
      throw new IllegalArgumentException(DialogHandler.text.getString("error_unencrypted_attachment"));

    if (attachment.getSymmetricCipherAlgorithm() == null)
    {
      if (log.isDebugEnabled())
        log.error("SymmetricCipherAlgorithm wurde nicht gesetzt.");
      throw new IllegalArgumentException(DialogHandler.text.getString("error_unencrypted_attachment"));
    }

    this.secretKey = attachment.secretKey;


    if (log.isDebugEnabled())
      log.debug("Secret-Key des Attachments wird verwendet.");

    CipherReference ref = new CipherReference("cid:" + attachment.getRefID());
    setRefID("Attachment" + attachment.getRefID());
    encryptedDataObject = new EncryptedData(ref, attachment.getSymmetricCipherAlgorithm(), attachment.getIvLength(), getRefID());
    this.stateOfObject = ENCRYPTEDDATA_ENCRYPTED;

    KeyInfo keyInfo = new KeyInfo();
    keyInfo.setMgmtData(Base64.encode(attachment.secretKey.getEncoded()));
    encryptedDataObject.setKeyInfo(keyInfo);
    this.content = attachment;
  }

  /**
   * Legt ein EncryptedData-Objekt an, welches als zu verschlüsselnden Inhalt das übergebenen
   * ContentContainer-Objekt enthält. Es wird für die symmetrische Verschlüsselung ein AES-256-GCM-Schlüssel
   * erzeugt.
   *
   * @param coco Inhaltsdatencontainer mit den zu verschlüsselnden Daten
   * @throws NoSuchAlgorithmException wenn ein nicht unterstützter Algorothmus übergeben wurde
   * @throws IOException undocumented
   */
  public EncryptedDataOSCI(ContentContainer coco) throws NoSuchAlgorithmException, IOException
  {
    this(null, Constants.SYMMETRIC_CIPHER_ALGORITHM_AES256_GCM, Constants.DEFAULT_GCM_IV_LENGTH, coco);
  }
  
  

  /**
   * @deprecated Legt ein EncryptedData-Objekt mit dem übergebenen symmetrischen Schlüssel an, welches als zu
   *             verschlüsselnden Inhalt das übergebenen ContentContainer-Objekt enthält. In Zukunft besser
   *             {@link #EncryptedDataOSCI(javax.crypto.SecretKey, String, ContentContainer)} benutzen
   * @param secretKey geheimer Schlüssel zum Verschlüsseln der Daten
   * @param coco Inhaltsdatencontainer mit den zu verschlüsselnden Daten
   * @throws NoSuchAlgorithmException wenn ein nicht unterstützter Algorothmus übergeben wurde
   * @throws IOException undocumented
   */
  @Deprecated
  public EncryptedDataOSCI(javax.crypto.SecretKey secretKey, ContentContainer coco)
    throws NoSuchAlgorithmException, IOException
  {
    this(secretKey, Crypto.getCipherAlgoID(secretKey), Constants.DEFAULT_GCM_IV_LENGTH, coco);
  }
  
  /**
   * Legt ein EncryptedData-Objekt für den angegebenen symmetrischen Verschlüsselungsalgorithmus an, welches
   * als zu verschlüsselnden Inhalt das übergebene ContentContainer-Objekt enthält. Es wird für die
   * symmetrische Verschlüsselung ein Schlüssel für den gewählten Algorithmus erzeugt.
   *
   * @param symmetricCipherAlgorithm Verschlüsselungsalgorithmus, erlaubte Werte sind
   *          Constants.SYMMETRIC_CIPHER_ALGORITHM_TDES_CBC und Constants.SYMMETRIC_CIPHER_ALGORITHM_AES128,
   *          -192, -265
   * @param ivLength Länge des IV in Bytes (16 für Abwärtskompatibilität, 12 empfohlen)
   * @param coco Inhaltsdatencontainer mit den zu verschlüsselnden Daten
   * @throws NoSuchAlgorithmException wenn ein nicht unterstützter Algorithmus übergeben wurde
   * @throws IOException undocumented
   */
  public EncryptedDataOSCI(String symmetricCipherAlgorithm, int ivLength, ContentContainer coco)
    throws NoSuchAlgorithmException, IOException
  {
    this(null, symmetricCipherAlgorithm, ivLength, coco);
  }

  /**
   * Legt ein EncryptedData-Objekt für den angegebenen symmetrischen Verschlüsselungsalgorithmus an, welches
   * als zu verschlüsselnden Inhalt das übergebene ContentContainer-Objekt enthält. Es wird für die
   * symmetrische Verschlüsselung ein Schlüssel für den gewählten Algorithmus erzeugt.
   *
   * @param symmetricCipherAlgorithm Verschlüsselungsalgorithmus, erlaubte Werte sind
   *          Constants.SYMMETRIC_CIPHER_ALGORITHM_TDES_CBC und Constants.SYMMETRIC_CIPHER_ALGORITHM_AES128,
   *          -192, -265
   * @param coco Inhaltsdatencontainer mit den zu verschlüsselnden Daten
   * @throws NoSuchAlgorithmException wenn ein nicht unterstützter Algorithmus übergeben wurde
   * @throws IOException undocumented
   */
  public EncryptedDataOSCI(String symmetricCipherAlgorithm, ContentContainer coco)
    throws NoSuchAlgorithmException, IOException
  {
    this(null, symmetricCipherAlgorithm, Constants.DEFAULT_GCM_IV_LENGTH, coco);
  }
  
  /**
   * Legt ein EncryptedData-Objekt mit dem übergebenen symmetrischen Schlüssel an, welches als zu
   * verschlüsselnden Inhalt das übergebene ContentContainer-Objekt enthält.
   *
   * @param secretKey geheimer Schlüssel zum Verschlüsseln der Daten
   * @param algo symmetrischer Algorithmus
   * @param coco Inhaltsdatencontainer mit den zu verschlüsselnden Daten
   * @throws NoSuchAlgorithmException wenn ein nicht unterstützter Algorithmus übergeben wurde
   * @since 1.7.0
   */
  public EncryptedDataOSCI(javax.crypto.SecretKey secretKey, String algo, ContentContainer coco)
    throws NoSuchAlgorithmException
  {
    this(secretKey, algo, Constants.DEFAULT_GCM_IV_LENGTH, coco);
  }

  /**
   * Legt ein EncryptedData-Objekt mit dem übergebenen symmetrischen Schlüssel an, welches als zu
   * verschlüsselnden Inhalt das übergebene ContentContainer-Objekt enthält.
   *
   * @param secretKey geheimer Schlüssel zum Verschlüsseln der Daten
   * @param algo symmetrischer Algorithmus
   * @param ivLength Länge des IV in Bytes (16 für Abwärtskompatibilität, 12 empfohlen)
   * @param coco Inhaltsdatencontainer mit den zu verschlüsselnden Daten
   * @throws NoSuchAlgorithmException wenn ein nicht unterstützter Algorithmus übergeben wurde
   * @since 1.7.0
   */
  public EncryptedDataOSCI(javax.crypto.SecretKey secretKey, String algo, int ivLength, ContentContainer coco)
    throws NoSuchAlgorithmException
  {
    transformers.add(can);
    id = typ + (++idNr);
    if (algo == null)
      throw new IllegalArgumentException(DialogHandler.text.getString(LanguageTextEntries.invalid_firstargument.name())
                                         + " algo = null");
    if (secretKey == null)
      secretKey = Crypto.createSymKey(algo);

    String secKeyAlgo = secretKey.getAlgorithm();

    if (!secKeyAlgo.equals("DESede") && !secKeyAlgo.equals("AES"))
      throw new NoSuchAlgorithmException(DialogHandler.text.getString("encryption_algorithm_not_supported")
                                         + secKeyAlgo);

    if (secKeyAlgo.equals("AES"))
    {
      int len = secretKey.getEncoded().length;

      if ((len != 16) && (len != 24) && (len != 32))
        throw new NoSuchAlgorithmException(DialogHandler.text.getString("encryption_algorithm_not_supported"
                                                                        + " AES-" + (len * 8)));
    }

    if (coco == null)
      throw new IllegalArgumentException(DialogHandler.text.getString(LanguageTextEntries.invalid_thirdargument.name())
                                         + " coco = null");

    typeOfData = TYPE_OF_DATA_CONTENTCONTAINER;
    id = "encdata_" + idNr++;
    this.secretKey = secretKey;
    this.content = coco;
    encryptedDataObject = new EncryptedData(coco, algo, ivLength, id, this.secretKey);

    Attachment[] atts = coco.getAttachments();

    if ((atts != null) && (atts.length > 0))
    {
      for ( int i = 0 ; i < atts.length ; i++ )
        this.attachments.add(atts[i]);
    }

    for ( int i = 0 ; i < coco.roles.size() ; i++ )
      roles.add(coco.roles.get(i));

    setNSPrefixes(coco.soapNSPrefix, coco.osciNSPrefix, coco.dsNSPrefix, coco.xencNSPrefix, coco.xsiNSPrefix);

    try
    {
      setNS(new String(coco.ns, Constants.CHAR_ENCODING));
    }
    catch (UnsupportedEncodingException ex)
    {}
  }

  /**
   * Dieser Konstruktor wird beim Parsen aufgerufen
   *
   * @param encryptedData {@link EncryptedData}
   * @param osciMsg {@link OSCIMessage}
   */
  public EncryptedDataOSCI(EncryptedData encryptedData, OSCIMessage osciMsg) throws SAXException
  {
    this.encryptedDataObject = encryptedData;
    stateOfObject = ENCRYPTEDDATA_ENCRYPTED;
    this.id = encryptedData.getId();
    msg = osciMsg;

    de.osci.osci12.encryption.EncryptedKey[] encKeys = encryptedData.getKeyInfo().getEncryptedKeys();

    for ( int i = 0 ; i < encKeys.length ; i++ )
    {
      String uri = (encKeys[i]).getKeyInfo().getRetrievalMethod().getURI();

      // id
      if (uri.startsWith("#"))
        uri = uri.substring(1);

      Role role = osciMsg.getRoleForRefID(uri);

      if (role == null)
        throw new SAXException(DialogHandler.text.getString("no_cipher_cert") + uri);

      roles.add(role);
      readers.add(role);
    }
  }

  void setNS(String ns)
  {
    encryptedDataObject.encNS = ns;
  }

  /**
   * undocumented
   *
   * @param soap undocumented
   * @param osci undocumented
   * @param ds undocumented
   * @param xenc undocumented
   * @param xsi undocumented
   */
  public void setNSPrefixes(String soap, String osci, String ds, String xenc, String xsi)
  {
    encryptedDataObject.soapNSPrefix = soap;
    encryptedDataObject.osciNSPrefix = osci;
    encryptedDataObject.dsNSPrefix = ds;
    encryptedDataObject.xencNSPrefix = xenc;
    encryptedDataObject.xsiNSPrefix = xsi;
  }

  /**
   * <p>
   * Diese Methode entschlüsselt den Inhalt des EncryptedDataOSCI-Objektes mit dem übergebenem Role-Objekt.
   * </p>
   * <p>
   * Die Verschlüsselung wird aufgehoben, die entschlüsselten Informationen geparst und ein
   * ContentContainer-Objekt aufgebaut und zurückgegeben.
   * </p>
   *
   * @param reader enthält die Entschlüsselungsinformationen. Das Role-Objekt muss auf jeden Fall ein
   *          Decrypter-Objekt zur Aufhebung der Verschlüsselung enthalten.
   * @return den entschlüsselten ContentContainer
   * @throws NoSuchAlgorithmException undocumented
   * @throws SAXException undocumented
   * @throws OSCICipherException undocumented
   * @throws OSCIRoleException undocumented
   * @throws IOException undocumented
   * @throws OSCICancelledException undocumented
   */
  public ContentContainer decrypt(Role reader) throws OSCICipherException, OSCIRoleException, IOException,
    OSCICancelledException, SAXException, NoSuchAlgorithmException
  {
    EncryptedKey encKey = this.encryptedDataObject.findEncrypedKey(getCertificatIdForRole(reader));

    if (log.isDebugEnabled())
      log.debug("Anzahl der Empfänger: " + encryptedKeyList.size());

    if ((encKey == null) || !reader.hasCipherPrivateKey())
      throw new IllegalArgumentException(DialogHandler.text.getString("no_encryption_for_role"));

    if (log.isDebugEnabled())
      log.debug("key: " + encKey.getKeyInfo().getRetrievalMethod().getURI());

    // es wurde ein EncryptedKey gefunden
    // Entschlüsseln des sym. Schlüssels
    InputStream keyIn = encKey.getCipherData().getCipherValue().getCipherValueStream();
    keyIn.reset();

    byte[] dt = Tools.readBytes(keyIn);
    byte[] decryptedKey;
    if (encKey.getEncryptionMethodAlgorithm().equals(Constants.ASYMMETRIC_CIPHER_ALGORITHM_RSA_OAEP))
      decryptedKey = reader.getDecrypter().decrypt(dt, encKey.mgfAlgorithm, encKey.digestAlgorithm);
    else
      decryptedKey = reader.getDecrypter().decrypt(dt);

    // Unterscheidung ob es sich um eine CipherRef oder Cipher Value handelt
    if (encryptedDataObject.getCipherData().getCipherReference() != null)
    {
      if (log.isDebugEnabled())
        log.debug("Es handelt sich um eine CipherReference unbekannter herkunft.");

      throw new OSCICipherException("invalid_reference");
    }
    else
    {
      if (log.isDebugEnabled())
        log.debug("Es handelt sich um ein CipherValue.");

      InputStream in = encryptedDataObject.getCipherData().getCipherValue().getCipherValueStream();
      in.reset();
      
      if(!encryptedDataObject.isIvLengthParsed())
      {
        // kein IV-Length-Element geparsed? Dann benutze alten IV-Standard 16 Byte /128 Bit
        encryptedDataObject.setIvLength(16);
      }
      
      SymCipherInputStream cin = new SymCipherInputStream(in,
                                     de.osci.osci12.encryption.Crypto.createSymKey(decryptedKey,
                                                                                   encryptedDataObject.getEncryptionMethodAlgorithm()),
                                     encryptedDataObject.getEncryptionMethodAlgorithm(),
                                     encryptedDataObject.getIvLength(), false);

      return parseInputStream(cin);
    }
  }


  /**
   * Gibt die ID des Verschlüsselungszertifikates für die angegebene Rolle zurück.
   * 
   * @param role
   * @return
   * @throws OSCIRoleException
   */
  private String getCertificatIdForRole(Role role) throws OSCIRoleException
  {
    if (role == null)
    {
      throw new IllegalArgumentException(DialogHandler.text.getString(LanguageTextEntries.invalid_firstargument.name())
                                         + " reader = null");
    }
    String rdId = null;

    for ( int i = 0 ; i < roles.size() ; i++ )
    {
      if (role.getCipherCertificate().equals(roles.get(i).getCipherCertificate()))
      {
        rdId = roles.get(i).getCipherCertificateId();

        break;
      }
    }

    if (rdId == null)
    {
      throw new IllegalArgumentException(DialogHandler.text.getString("no_encryption_for_role"));
    }

    return rdId;
  }


  /**
   * Diese Methode gibt den asymmetrischen Verschlüsselungsalgorithmus für den {@link EncryptedKey} des
   * angegebenen Lesers zurück.
   * 
   * @param reader
   * @return asymmetrischen Verschlüsselungsalgorithmus
   * @throws OSCICipherException
   * @throws OSCIRoleException
   * @throws IOException
   * @throws OSCICancelledException
   * @throws SAXException
   * @throws NoSuchAlgorithmException
   */
  public String getAsymEncryptionAlgorithm(Role reader) throws OSCICipherException, OSCIRoleException,
    IOException, OSCICancelledException, SAXException, NoSuchAlgorithmException
  {
    EncryptedKey encKey = this.encryptedDataObject.findEncrypedKey(getCertificatIdForRole(reader));

    if (encKey != null)
    {
      return encKey.getEncryptionMethodAlgorithm();
    }
    return null;
  }

  
  /**
   * Diese Methode erstellt die EncryptedData-Strukturen ohne den symmetrischen Schlüssel noch einmal zu
   * verschlüsseln. Dies kann bei vielen Verschlüsselungen zu einer schnelleren Verarbeitung mit weniger
   * PIN-Eingaben führen.
   *
   * @param encryptedSymKey verschlüsselter symmetrischer Schlüssel
   * @param reader Role-Objekt, welches den Leser repräsentiert
   * @throws OSCIRoleException undocumented
   * @throws IOException undocumented
   * @throws OSCICipherException undocumented
   */
  public void encrypt(byte[] encryptedSymKey, Role reader)
    throws OSCIRoleException, IOException, OSCICipherException
  {
    encrypt(encryptedSymKey, reader, Constants.DEFAULT_ASYMMETRIC_CIPHER_ALGORITHM);
  }

  
  /**
   * Diese Methode erstellt die EncryptedData-Strukturen ohne den symmetrischen Schlüssel noch einmal zu
   * verschlüsseln. Dies kann bei vielen Verschlüsselungen zu einer schnelleren Verarbeitung mit weniger
   * PIN-Eingaben führen.
   *
   * @param encryptedSymKey verschlüsselter symmetrischer Schlüssel
   * @param reader Role-Objekt, welches den Leser repräsentiert
   * @param algorithm Asymmetrischer Verschlüsselungsalgorithmus
   * @throws OSCIRoleException undocumented
   * @throws IOException undocumented
   * @throws OSCICipherException undocumented
   */
  public void encrypt(byte[] encryptedSymKey, Role reader, String algorithm)
    throws OSCIRoleException, IOException, OSCICipherException
  {
    if (!readers.contains(reader))
    {
      roles.add(reader);
      this.readers.add(reader);
    }

    this.stateOfObject = ENCRYPTEDDATA_ENCRYPTED;

    if (log.isDebugEnabled())
      log.debug("Encrypted-Data Methode encrypt mit :" + reader.getCipherCertificateId()); // + "
                                                                                           // encryptedSymKey:
                                                                                           // " +

    KeyInfo keyInfo = null;

    if (content == null)
      keyInfo = new KeyInfo("#" + reader.getCipherCertificate());
    else
      keyInfo = new KeyInfo("#" + reader.getCipherCertificateId());

    CipherValue cipherValue = new CipherValue(encryptedSymKey);
    EncryptedKey encryptedKey = new EncryptedKey(algorithm, cipherValue);
    encryptedKey.setId("EncData_" + id + "_" + keyIDs);
    encryptedKey.setKeyInfo(keyInfo);

    if (encryptedDataObject.getKeyInfo() == null)
      encryptedDataObject.setKeyInfo(new KeyInfo());

    encryptedDataObject.getKeyInfo().addEncryptedKey(encryptedKey);
    encryptedKeyList.put(reader, encryptedKey);

    if (content instanceof ContentContainer)
      this.createEncryptedAttachments((ContentContainer)content, encryptedSymKey, reader);

    if (log.isDebugEnabled())
      log.debug("Fertig mit Encrypted-Data Methode encrypt mit.");
  }

  /**
   * Diese Methode parst den soeben entschlüsselten Xml-Strom und Liefert das Ergebnisobjekt zurück
   *
   * @param in Stream der geparst wird
   * @return Erstelltes Object (derzeit nur coco)
   * @throws OSCICipherException undocumented
   */
  private ContentContainer parseInputStream(InputStream in)
    throws OSCICipherException, IOException, SAXException, NoSuchAlgorithmException
  {
    ContentPackageBuilder copaBuilder = null;

    try
    {
      Canonizer can = null;
      if (ParserHelper.isSecureContentDataCheck())
      {
        can = new Canonizer(in, null, true);
      }
      else
      {
        can = new Canonizer(in, null, false);
      }
      InputStreamReader isr = new InputStreamReader(can, Constants.CHAR_ENCODING);
      SAXParserFactory saxFactory = SAXParserFactory.newInstance();
      saxFactory.setNamespaceAware(true);

      SAXParser parser = saxFactory.newSAXParser();
      XMLReader rd = parser.getXMLReader();
      ParserHelper.setFeatures(rd);
      copaBuilder = new ContentPackageBuilder(rd, msg, can);
      rd.setContentHandler(copaBuilder);
      rd.parse(new InputSource(isr));
    }
    catch (ParserConfigurationException ex1)
    {
      throw new SAXException(ex1);
    }
    finally
    {
      in.close();
    }

    if (copaBuilder.getLastCreatedObject() instanceof ContentContainer)
    {
      ContentContainer coco = (ContentContainer)copaBuilder.getLastCreatedObject();
      decryptAttachments(coco);

      return coco;
    }
    else
      throw new OSCICipherException("sax_exception");
  }

  private void decryptAttachments(ContentContainer cc) throws IOException
  {
    EncryptedDataOSCI[] encDatas = cc.getEncryptedData();

    for ( int i = 0 ; i < encDatas.length ; i++ )
    {
      if (encDatas[i].encryptedDataObject.getCipherData().getCipherReference() != null) // getRefID().startsWith("Attachment"))
      {
        String uri = encDatas[i].encryptedDataObject.getCipherData().getCipherReference().getURI();

        if (uri.startsWith("cid:"))
          uri = uri.substring(4);

        encDatas[i].setRefID(uri);

        Hashtable<String, Attachment> msgAtts = msg.attachments;
        Attachment att = msgAtts.get(uri);
        att.stateOfAttachment = Attachment.STATE_OF_ATTACHMENT_ENCRYPTED;
        att.encrypt = true;

        if (log.isDebugEnabled())
          log.debug("das Attachment: " + att + " URI: " + uri);

        att.secretKey = Crypto.createSymKey(Base64.decode(encDatas[i].encryptedDataObject.getKeyInfo()
                                                                                         .getMgmtData()),
                                            encDatas[i].encryptedDataObject.getEncryptionMethodAlgorithm());
        att.symmetricCipherAlgorithm = encDatas[i].encryptedDataObject.getEncryptionMethodAlgorithm();
        att.ivLength = encDatas[i].encryptedDataObject.getIvLength();
        cc.attachments.put(att.getRefID(), att);
        cc.removeEncryptedData(encDatas[i], false);
      }
    }

    Content[] cnts = cc.getContents();

    for ( int i = 0 ; i < cnts.length ; i++ )
    {
      if (cnts[i].getContentType() == Content.CONTENT_CONTAINER)
        decryptAttachments(cnts[i].getContentContainer());
    }
  }

  /**
   * Fügt beim Verschlüsseln die EncryptedData-Elemente der Attachments hinzu
   *
   * @param encryptedSymKey undocumented
   * @param reader undocumented
   * @throws OSCICipherException undocumented
   * @throws IOException undocumented
   */
  private void createEncryptedAttachments(ContentContainer cc, byte[] encryptedSymKey, Role reader)
    throws OSCICipherException, IOException
  {
    Attachment[] atts = cc.getAttachments();

    if (atts != null)
    {
      ContentContainer coco = (ContentContainer)content;
      coco.stateOfObject = ContentContainer.STATE_OF_OBJECT_PARSING;
      HashSet<String> encDataRefIds = new HashSet<String>();
      EncryptedDataOSCI[] edo = coco.getEncryptedData();
      for ( int i = 0 ; i < edo.length ; i++ )
        encDataRefIds.add(edo[i].getRefID());

      for ( int i = 0 ; i < atts.length ; i++ )
      {
        if (log.isDebugEnabled())
          log.debug("Ein weiteres Attachment." + atts[i].getRefID());
        if (!encDataRefIds.add("Attachment" + atts[i].getRefID()))
          continue;

        EncryptedDataOSCI encData = new EncryptedDataOSCI(atts[i]);
        encData.setNSPrefixes(coco.soapNSPrefix,
                              coco.osciNSPrefix,
                              coco.dsNSPrefix,
                              coco.xencNSPrefix,
                              coco.xsiNSPrefix);
        encData.setNS(new String(coco.ns, Constants.CHAR_ENCODING));
        coco.addEncryptedData(encData);
      }

      Role[] roles = ((ContentContainer)content).getRoles();

      for ( int i = 0 ; i < roles.length ; i++ )
        if (!this.roles.contains(roles[i]))
          this.roles.add(roles[i]);
    }

    Content[] cnts = cc.getContents();

    for ( int i = 0 ; i < cnts.length ; i++ )
    {
      if (cnts[i].getContentType() == Content.CONTENT_CONTAINER)
        createEncryptedAttachments(cnts[i].getContentContainer(), encryptedSymKey, reader);
    }
  }

  /**
   * Verschlüsselt den geheimen Schlüssel und fügt ihn als EncryptedKey-Element dem EncyptedData-Element
   * hinzu. Außerdem wird das Rollen-Objekt der Parent-Nachricht (des MessagePart-Objektes) hinzugefügt, so
   * dass das Verschlüsselungszertifikat in der Nachricht enthalten ist.
   *
   * @param reader Rollen-Objekt, für welches verschlüsselt werden soll
   * @throws OSCIRoleException wenn dem Rollen-Objekt das erforderliche Verschlüsselungszertifikat fehlt
   * @throws OSCICipherException undocumented
   * @throws IOException undocumented
   * @throws NoSuchAlgorithmException undocumented
   */
  public void encrypt(Role reader)
    throws OSCICipherException, OSCIRoleException, IOException, NoSuchAlgorithmException
  {
    encrypt(reader, Constants.DEFAULT_ASYMMETRIC_CIPHER_ALGORITHM);
  }

  /**
   * Verschlüsselt den geheimen Schlüssel mit dem angegebenen Algorithmus und fügt ihn als
   * EncryptedKey-Element dem EncyptedData-Element hinzu. Außerdem wird das Rollen-Objekt der Parent-Nachricht
   * (des MessagePart- Objektes) hinzugefügt, so dass das Verschlüsselungszertifikat in der Nachricht
   * enthalten ist.
   *
   * @param reader Rollen-Objekt, für welches verschlüsselt werden soll
   * @param algorithm Asymmetrischer Verschlüsselungsalgorithmus (http://www.w3.org/2001/04/xmlenc#rsa-1_5
   *          oder http://www.w3.org/2009/xmlenc11#rsa-oaep)
   * @throws OSCIRoleException wenn dem Rollen-Objekt das erforderliche Verschlüsselungszertifikat fehlt
   * @throws OSCICipherException undocumented
   * @throws NoSuchAlgorithmException undocumented
   * @throws IOException undocumented
   */
  public void encrypt(Role reader, String algorithm)
    throws OSCICipherException, OSCIRoleException, IOException, NoSuchAlgorithmException
  {
    if (!readers.contains(reader))
    {
      roles.add(reader);
      readers.add(reader);
    }

    this.stateOfObject = ENCRYPTEDDATA_ENCRYPTED;

    if (log.isDebugEnabled())
      log.debug("Encrypted-Data Methode encrypt mit :" + reader.getCipherCertificateId()
                + " Es handelt sich um ein EncryptedData Objekt vom Typ:" + this.typeOfData);

    if (secretKey == null)
      throw new OSCICipherException("no_secret_key");

    byte[] encryptedSymKey = Crypto.doRSAEncryption(reader.getCipherCertificate(), secretKey, algorithm);
    encrypt(encryptedSymKey, reader, algorithm);
  }

  /**
   * Interne Methode, wird von Anwendungen normalerweise nicht aufgerufen.
   *
   * @param out
   * @exception IOException
   */
  public void writeXML(OutputStream out) throws IOException, OSCIException
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
   * @throws IllegalStateException undocumented
   */
  public void writeXML(OutputStream out, boolean inner) throws IOException, OSCIException
  {
    if (stateOfObject < ENCRYPTEDDATA_ENCRYPTED)
      throw new IllegalStateException(DialogHandler.text.getString("invalid_stateofobject")
                                      + " not encrypted.");

    encryptedDataObject.setId(getRefID());
    encryptedDataObject.writeXML(out, inner);
  }

  /**
   * Liefert den Identifier des symmetrischen Verschlüsselungsalgorithmus.
   *
   * @return Algorithmus
   */
  public String getSymEncryptionMethod()
  {
    return encryptedDataObject.getEncryptionMethodAlgorithm();
  }

  /**
   * Liefert die eingestellten Attachment-Objekte des ContentContainer.
   *
   * @return Array der zugehörigen Attachments
   */
  public Attachment[] getAttachments()
  {
    return (Attachment[])this.attachments.toArray(new Attachment[0]);
  }

  /**
   * Liefert die eingestellten Role-Objekte des EncryptedData-Objektes, welche für die Signatur und/oder
   * Verschlüsselung verwendet wurden.
   *
   * @return Array der verwendeten Role-Objekte
   */
  public Role[] getRoles()
  {
    return roles.toArray(new Role[0]);
  }

  /**
   * Liefert die eingestellten Role-Objekte des EncryptedData-Objektes, mit dem die Daten verschlüsselt
   * wurden.
   *
   * @return Array der Verschlüsselungs-Role-Objekte
   */
  public Role[] getReaders()
  {
    return readers.toArray(new Role[]{});
  }
  
  /**
   * Ändert die Länge des Initialisierungsvektors in Byte, der bei der Verschlüsselung mit AES-GCM genutzt wird
   * 
   * @param ivLength Länge des IV in Bytes (16 für Abwärtskompatibilität, 12 empfohlen)
   */
  public void setIvLength(int ivLength)
  {
    if (encryptedDataObject != null)
    {
      encryptedDataObject.setIvLength(ivLength);
    }
    else
    {
      log.warn("Could not set IV, encryptedDataObject is null");
    }
  }
}
