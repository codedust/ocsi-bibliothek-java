package de.osci.osci12.common;

import java.util.*;


/**
 * In dieser Klasse sind Konstanten der Bibliothek definiert.
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
public class Constants
{

  /**
   * Enumeration fuer neue System-Properties.
   *
   * @author Lindemann
   */
  public enum SystemProperties
  {

    GCMAlgorithmOnly("de.osci.GCMAlgorithmOnly"),
    SecureContentDataCheck("de.osci.SecureContentDataCheck"),
    SecureTransportDataCheck("de.osci.SecureTransportDataCheck"),
    SwitchToGCM("de.osci.SwitchToGCM");

    private String property;

    SystemProperties(String property)
    {
      this.property = property;
    }

    /**
     * @return System-Property Wert.
     */
    public String getPropertyValue()
    {
      return property;
    }


  }

  /**
   * Enumeration für neue OSCI Features.
   *
   * @author Lindemann
   */
  public enum OSCIFeatures
  {

    DisableBase64("1.2.0"),
    OAEPEncryption("1.6.0"),
    SHA3HashAlgo("1.6.3"),
    GCMPaddingModus("1.7.0"),
    PartialMessageTransmission("1.8.0"),
    Support96Bit12ByteIV("1.9.0");

    private String version;

    OSCIFeatures(String version)
    {
      this.version = version;
    }

    /**
     * @return Version der OSCI Bibliothek ab der dieses Feature unterstützt wird.
     */
    public String getVersion()
    {
      return version;
    }
  }

  public enum CommonTags
  {

    ContentPackage("ContentPackage", Namespaces.OSCI),
    ProcessCardBundle("ProcessCardBundle", Namespaces.OSCI),
    ReplyProcessCardBundle("ReplyProcessCardBundle", Namespaces.OSCI),
    RequestProcessCardBundle("RequestProcessCardBundle", Namespaces.OSCI),
    Feedback("Feedback", Namespaces.OSCI),
    MessageId("MessageId", Namespaces.OSCI),
    ChunkInformation("ChunkInformation", Namespaces.OSCI2017),
    InsideFeedback("InsideFeedback", Namespaces.OSCI2017);

    private String elementName;

    private Namespaces namespace;

    CommonTags(String elementName, Namespaces namespace)
    {
      this.elementName = elementName;
      this.namespace = namespace;
    }

    public String getElementName()
    {
      return elementName;
    }

    public Namespaces getNamespace()
    {
      return namespace;
    }
  }

  public enum HeaderTags
  {

    ControlBlock("ControlBlock", Namespaces.OSCI),
    ClientSignature("ClientSignature", Namespaces.OSCI),
    SupplierSignature("SupplierSignature", Namespaces.OSCI),
    DesiredLanguages("DesiredLanguages", Namespaces.OSCI),
    IntermediaryCertificates("IntermediaryCertificates", Namespaces.OSCI),
    QualityOfTimestamp("QualityOfTimestamp", Namespaces.OSCI),
    NonIntermediaryCertificates("NonIntermediaryCertificates", Namespaces.OSCI),
    // msg Types
    acceptDelivery("acceptDelivery", Namespaces.OSCI),
    fetchDelivery("fetchDelivery", Namespaces.OSCI),
    processDelivery("processDelivery", Namespaces.OSCI),
    responseToExitDialog("responseToExitDialog", Namespaces.OSCI),
    responseToFetchDelivery("responseToFetchDelivery", Namespaces.OSCI),
    mediateDelivery("mediateDelivery", Namespaces.OSCI),
    responseToMediateDelivery("responseToMediateDelivery", Namespaces.OSCI),
    storeDelivery("storeDelivery", Namespaces.OSCI),
    responseToStoreDelivery("responseToStoreDelivery", Namespaces.OSCI),
    fetchProcessCard("fetchProcessCard", Namespaces.OSCI),
    responseToFetchProcessCard("responseToFetchProcessCard", Namespaces.OSCI),
    forwardDelivery("forwardDelivery", Namespaces.OSCI),
    responseToForwardDelivery("responseToForwardDelivery", Namespaces.OSCI),
    responseToAcceptDelivery("responseToAcceptDelivery", Namespaces.OSCI),
    responseToProcessDelivery("responseToProcessDelivery", Namespaces.OSCI),


    FeatureDescription("FeatureDescription", Namespaces.OSCI2017),
    partialStoreDelivery("partialStoreDelivery", Namespaces.OSCI2017),
    responseToPartialStoreDelivery("responseToPartialStoreDelivery", Namespaces.OSCI2017),
    partialFetchDelivery("partialFetchDelivery", Namespaces.OSCI2017),
    responseToPartialFetchDelivery("responseToPartialFetchDelivery", Namespaces.OSCI2017);

    private String elementName;

    private Namespaces namespace;

    HeaderTags(String elementName, Namespaces namespace)
    {
      this.elementName = elementName;
      this.namespace = namespace;
    }

    public String getElementName()
    {
      return elementName;
    }

    public Namespaces getNamespace()
    {
      return namespace;
    }
  }

  public enum Namespaces
  {

    SOAP("http://schemas.xmlsoap.org/soap/envelope/", "soap"),
    OSCI("http://www.osci.de/2002/04/osci", "osci"),
    OSCI2017("http://xoev.de/transport/osci12/7", "osci2017"),
    OSCI128("http://xoev.de/transport/osci12/8", "osci128"),
    XML_SIG("http://www.w3.org/2000/09/xmldsig#", "ds"),
    XML_ENC("http://www.w3.org/2001/04/xmlenc#", "xenc"),
    XML_SCHEMA("http://www.w3.org/2001/XMLSchema-instance", "xsi");

    private String uri;

    private String prefix;

    Namespaces(String uri, String prefix)
    {
      this.uri = uri;
      this.prefix = prefix;
    }

    public String getUri()
    {
      return uri;
    }

    public String getPrefix()
    {
      return prefix;
    }
  }

  public enum LanguageTextEntries
  {
    missing_entry,
    invalid_firstargument,
    invalid_secondargument,
    invalid_thirdargument,
    invalid_fourthargument,
    warning_iv_length
  }

  /**
   * Hashtable mit den benötigten JCA/JCE-Identifiern (not modifiable)
   */
  public static final Hashtable<String, String> JCA_JCE_MAP;

  /**
   * Character-Encoding UTF-8
   */
  public static final String CHAR_ENCODING = "UTF-8";

  /**
   * Algorithmus für Zufallszahlenerstellung wird auf SHA1 Pseudo Random Number Generation gestellt,
   * JCA/JCE-Identifier
   */
  public static final String SECURE_RANDOM_ALGORITHM_SHA1 = "SHA1PRNG";

  /**
   * Algorithmus für Signaturerstellung, Konstante für SHA1withRSA
   */
  public static final String SIGNATURE_ALGORITHM_RSA_SHA1 = "http://www.w3.org/2000/09/xmldsig#rsa-sha1";

  /**
   * Algorithmus für Signaturerstellung, Konstante für SHA1withRSA, JCA/JCE-Identifier
   */
  private static final String SIGNATURE_ALGORITHM_RSA_SHA1_JCA_JCE = "SHA1withRSA";

  /**
   * Hashtable mit Ablaufdaten der Algorithmen und Schlüssellängen (not modifiable)
   */
  public static final Hashtable<String, Date> OUT_DATES;

  /**
   * Ablaufdatum für 1024 Bit RSA-Schlüssellänge (AlgKat2008)
   */
  public static final Date OUT_DATE_KEYSIZE_1024;

  /**
   * Default-Gültigkeitsdatum für Algorithmus-Prüfungen
   */
  public static final Date ACTUAL_DATE;

  /**
   * Default-Länge in Byte des Initialisierungsvektors (IV) für die Ver- und Entschlüsselung mit AES-GCM.
   */
  public static final int DEFAULT_GCM_IV_LENGTH = 12;

  /**
   * Algorithmus für Signaturerstellung, Konstante für SHA256withRSA
   */
  public static final String SIGNATURE_ALGORITHM_RSA_SHA256 = "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256";

  /**
   * Algorithmus für Signaturerstellung, Konstante für SHA256withRSA, JCA/JCE-Identifier
   */
  private static final String SIGNATURE_ALGORITHM_RSA_SHA256_JCA_JCE = "SHA256withRSA";


  /**
   * Algorithmus für Signaturerstellung, Konstante für SHA512withRSA
   */
  public static final String SIGNATURE_ALGORITHM_RSA_SHA512 = "http://www.w3.org/2001/04/xmldsig-more#rsa-sha512";

  /**
   * Algorithmus für Signaturerstellung, Konstante für SHA512withRSA, JCA/JCE-Identifier
   */
  private static final String SIGNATURE_ALGORITHM_RSA_SHA512_JCA_JCE = "SHA512withRSA";

  /**
   * Algorithmus für Signaturerstellung, Konstante für SHA256withRSAandMGF1 (PSS)
   */
  public static final String SIGNATURE_ALGORITHM_RSA_SHA256_PSS = "http://www.w3.org/2007/05/xmldsig-more#sha256-rsa-MGF1";

  /**
   * Algorithmus für Signaturerstellung, Konstante für SHA256withRSAandMGF1 (PSS), JCA/JCE-Identifier
   */
  private static final String SIGNATURE_ALGORITHM_RSA_SHA256_PSS_JCA_JCE = "SHA256withRSAandMGF1";

  /**
   * Algorithmus für Signaturerstellung, Konstante für SHA512withRSAandMGF1 (PSS)
   */
  public static final String SIGNATURE_ALGORITHM_RSA_SHA512_PSS = "http://www.w3.org/2007/05/xmldsig-more#sha512-rsa-MGF1";

  /**
   * Algorithmus für Signaturerstellung, Konstante für SHA512withRSAandMGF1 (PSS), JCA/JCE-Identifier
   */
  private static final String SIGNATURE_ALGORITHM_RSA_SHA512_PSS_JCA_JCE = "SHA512withRSAandMGF1";

  /**
   * Algorithmus für Signaturerstellung, Konstante für RIPEMD160withRSA
   */
  public static final String SIGNATURE_ALGORITHM_RSA_RIPEMD160_RFC4051 = "http://www.w3.org/2001/04/xmldsig-more/rsa-ripemd160";

  /**
   * Konstante für Abwärtskompatibilität nach Korrektur der RIPEMD160withRSA-Konstante (Version 1.5, Typo in
   * RFC 4051)
   */
  public static final String SIGNATURE_ALGORITHM_RSA_RIPEMD160 = "http://www.w3.org/2001/04/xmldsig-more#rsa-ripemd160";

  /**
   * Algorithmus für Signaturerstellung, Konstante für RIPEMD160withRSA, JCA/JCE-Identifier
   */
  private static final String SIGNATURE_ALGORITHM_RSA_RIPEMD160_JCA_JCE = "RIPEMD160withRSA";

  /**
   * Algorithmus für Signaturerstellung, Konstante für SHA256withECDSA
   */
  public static final String SIGNATURE_ALGORITHM_ECDSA_SHA256 = "http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha256";

  /**
   * Algorithmus für Signaturerstellung, Konstante für SHA256withECDSA, JCA/JCE-Identifier
   */
  private static final String SIGNATURE_ALGORITHM_ECDSA_SHA256_JCA_JCE = "SHA256withCVC-ECDSA";

  /**
   * Algorithmus für Signaturerstellung, Konstante für SHA512withECDSA
   */
  public static final String SIGNATURE_ALGORITHM_ECDSA_SHA512 = "http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha512";

  /**
   * Algorithmus für Signaturerstellung, Konstante für SHA512withECDSA, JCA/JCE-Identifier
   */
  private static final String SIGNATURE_ALGORITHM_ECDSA_SHA512_JCA_JCE = "SHA512withCVC-ECDSA";

  /**
   * Algorithmus für Signaturerstellung, Konstante für SHA3_256withRSAandMGF1
   */
  public static final String SIGNATURE_ALGORITHM_RSA_SHA3_256 = "http://www.w3.org/2007/05/xmldsig-more#sha3-256-rsa-MGF1";

  /**
   * Algorithmus für Signaturerstellung, Konstante für SHA3_256withRSAandMGF1, JCA/JCE-Identifier
   */
  private static final String SIGNATURE_ALGORITHM_RSA_SHA3_256_JCA_JCE = "SHA3-256withRSAandMGF1";

  /**
   * Algorithmus für Signaturerstellung, Konstante für SHA3_384withRSAandMGF1
   */
  public static final String SIGNATURE_ALGORITHM_RSA_SHA3_384 = "http://www.w3.org/2007/05/xmldsig-more#sha3-384-rsa-MGF1";

  /**
   * Algorithmus für Signaturerstellung, Konstante für SHA3_384withRSAandMGF1, JCA/JCE-Identifier
   */
  private static final String SIGNATURE_ALGORITHM_RSA_SHA3_384_JCA_JCE = "SHA3-384withRSAandMGF1";

  /**
   * Algorithmus für Signaturerstellung, Konstante für SHA3_512withRSAandMGF1
   */
  public static final String SIGNATURE_ALGORITHM_RSA_SHA3_512 = "http://www.w3.org/2007/05/xmldsig-more#sha3-512-rsa-MGF1";

  /**
   * Algorithmus für Signaturerstellung, Konstante für SHA3_512withRSAandMGF1, JCA/JCE-Identifier
   */
  private static final String SIGNATURE_ALGORITHM_RSA_SHA3_512_JCA_JCE = "SHA3-512withRSAandMGF1";



  /**
   * Algorithmus für Hashwerterstellung, Konstante für SHA1
   */
  public static final String DIGEST_ALGORITHM_SHA1 = "http://www.w3.org/2000/09/xmldsig#sha1";

  /**
   * Algorithmus für Hashwerterstellung, Konstante für SHA1, JCA/JCE-Identifier
   */
  private static final String DIGEST_ALGORITHM_SHA1_JCA_JCE = "SHA-1";

  /**
   * Algorithmus für Hashwerterstellung, Konstante für SHA256
   */
  public static final String DIGEST_ALGORITHM_SHA256 = "http://www.w3.org/2001/04/xmlenc#sha256";

  /**
   * Algorithmus für Hashwerterstellung, Konstante für SHA256, JCA/JCE-Identifier
   */
  private static final String DIGEST_ALGORITHM_SHA256_JCA_JCE = "SHA-256";

  /**
   * Algorithmus für Hashwerterstellung, Konstante für SHA512
   */
  public static final String DIGEST_ALGORITHM_SHA512 = "http://www.w3.org/2001/04/xmlenc#sha512";

  /**
   * Algorithmus für Hashwerterstellung, Konstante für SHA512, JCA/JCE-Identifier
   */
  private static final String DIGEST_ALGORITHM_SHA512_JCA_JCE = "SHA-512";

  /**
   * Algorithmus für Hashwerterstellung, Konstante für RIPEMD160
   */
  public static final String DIGEST_ALGORITHM_RIPEMD160 = "http://www.w3.org/2001/04/xmlenc#ripemd160";

  /**
   * Algorithmus für Hashwerterstellung, Konstante für RIPEMD160, JCA/JCE-Identifier
   */
  private static final String DIGEST_ALGORITHM_RIPEMD160_JCA_JCE = "RIPEMD160";

  /**
   * Algorithmus für Hashwerterstellung, Konstante für SHA3-256
   */
  public static final String DIGEST_ALGORITHM_SHA3_256 = "http://www.w3.org/2007/05/xmldsig-more#sha3-256";

  /**
   * Algorithmus für Hashwerterstellung, Konstante für SHA3-256, JCA/JCE-Identifier
   */
  private static final String DIGEST_ALGORITHM_SHA3_256_JCA_JCE = "SHA3-256";

  /**
   * Algorithmus für Hashwerterstellung, Konstante für SHA3-384
   */
  public static final String DIGEST_ALGORITHM_SHA3_384 = "http://www.w3.org/2007/05/xmldsig-more#sha3-384";

  /**
   * Algorithmus für Hashwerterstellung, Konstante für SHA3-384, JCA/JCE-Identifier
   */
  private static final String DIGEST_ALGORITHM_SHA3_384_JCA_JCE = "SHA3-384";

  /**
   * Algorithmus für Hashwerterstellung, Konstante für SHA3-512
   */
  public static final String DIGEST_ALGORITHM_SHA3_512 = "http://www.w3.org/2007/05/xmldsig-more#sha3-512";

  /**
   * Algorithmus für Hashwerterstellung, Konstante für SHA3-512, JCA/JCE-Identifier
   */
  private static final String DIGEST_ALGORITHM_SHA3_512_JCA_JCE = "SHA3-512";



  /**
   * Algorithmus für symmetrische Verschlüsselung Two-Key-Triple-DES (CBC-Mode) - Warnung: veraltet!
   *
   * @deprecated Diese Algorithmen werden in Zukunft nicht mehr zu Verfügung stehen. In Zukunft sollen die
   *             Algorithmen mit GCM Modus benutzt werden
   */
  @Deprecated
  public static final String SYMMETRIC_CIPHER_ALGORITHM_TDES_CBC = "http://www.w3.org/2001/04/xmlenc#tripledes-cbc";

  /**
   * Algorithmus für symmetrische Verschlüsselung Two-Key-Triple-DES (CBC-Mode), JCA/JCE-Identifier - Warnung:
   * veraltet!
   *
   * @deprecated Diese Algorithmen werden in Zukunft nicht mehr zu Verfügung stehen. In Zukunft sollen die
   *             Algorithmen mit GCM Modus benutzt werden
   */
  @Deprecated
  private static final String SYMMETRIC_CIPHER_ALGORITHM_TDES_CBC_JCA_JCE = "DESede/CBC/PKCS5Padding";

  /**
   * Algorithmus für symmetrische Verschlüsselung AES-128-CBC - Warnung: veraltet!
   *
   * @deprecated Diese Algorithmen werden in Zukunft nicht mehr zu Verfügung stehen. In Zukunft sollen die
   *             Algorithmen mit GCM Modus benutzt werden
   */
  @Deprecated
  public static final String SYMMETRIC_CIPHER_ALGORITHM_AES128 = "http://www.w3.org/2001/04/xmlenc#aes128-cbc";

  /**
   * Algorithmus für symmetrische Verschlüsselung AES (CBC-Mode), JCA/JCE-Identifier - Warnung: veraltet!
   *
   * @deprecated Diese Algorithmen werden in Zukunft nicht mehr zu Verfügung stehen. In Zukunft sollen die
   *             Algorithmen mit GCM Modus benutzt werden
   */
  @Deprecated
  private static final String SYMMETRIC_CIPHER_ALGORITHM_AES_CBC_JCA_JCE = "AES/CBC/PKCS5Padding";

  /**
   * Algorithmus für symmetrische Verschlüsselung AES-192-CBC - Warnung: veraltet!
   *
   * @deprecated Diese Algorithmen werden in Zukunft nicht mehr zu Verfügung stehen. In Zukunft sollen die
   *             Algorithmen mit GCM Modus benutzt werden
   */
  @Deprecated
  public static final String SYMMETRIC_CIPHER_ALGORITHM_AES192 = "http://www.w3.org/2001/04/xmlenc#aes192-cbc";

  /**
   * Algorithmus für symmetrische Verschlüsselung AES-256-CBC - Warnung: veraltet!
   *
   * @deprecated Diese Algorithmen werden in Zukunft nicht mehr zu Verfügung stehen. In Zukunft sollen die
   *             Algorithmen mit GCM Modus benutzt werden
   */
  @Deprecated
  public static final String SYMMETRIC_CIPHER_ALGORITHM_AES256 = "http://www.w3.org/2001/04/xmlenc#aes256-cbc";

  /**
   * Algorithmus für symmetrische Verschlüsselung AES-128-GCM
   *
   * @since 1.8.0
   */
  public static final String SYMMETRIC_CIPHER_ALGORITHM_AES128_GCM = "http://www.w3.org/2009/xmlenc11#aes128-gcm";

  /**
   * Standardwert für den symmetrischen Verschlüsselungsalgorithmus
   */
  public static final String DEFAULT_SYMMETRIC_CIPHER_ALGORITHM = Constants.SYMMETRIC_CIPHER_ALGORITHM_AES256_GCM;

  /**
   * Algorithmus für symmetrische Verschlüsselung AES (GCM-Mode), JCA/JCE-Identifier
   *
   * @since 1.8.0
   */
  private static final String SYMMETRIC_CIPHER_ALGORITHM_AES_GCM_JCA_JCE = "AES/GCM/NoPadding";

  /**
   * Algorithmus für symmetrische Verschlüsselung AES-192-GCM
   *
   * @since 1.8.0
   */
  public static final String SYMMETRIC_CIPHER_ALGORITHM_AES192_GCM = "http://www.w3.org/2009/xmlenc11#aes192-gcm";

  /**
   * Algorithmus für symmetrische Verschlüsselung AES-256-GCM
   *
   * @since 1.8.0
   */
  public static final String SYMMETRIC_CIPHER_ALGORITHM_AES256_GCM = "http://www.w3.org/2009/xmlenc11#aes256-gcm";

  /**
   * Algorithmus für asymmetrische Verschlüsselung RSAES-PKCS1-v1_5
   *
   * @deprecated Dieser Algorithmus wird in Zukunft nicht mehr zu Verfügung stehen. In Zukunft soll der
   *             Algorithmus ASYMMETRIC_CIPHER_ALGORITHM_RSA_OAEP benutzt werden
   */
  @Deprecated
  public static final String ASYMMETRIC_CIPHER_ALGORITHM_RSA_1_5 = "http://www.w3.org/2001/04/xmlenc#rsa-1_5";

  /**
   * Algorithmus für asymmetrische Verschlüsselung RSA-OAEP
   */
  public static final String ASYMMETRIC_CIPHER_ALGORITHM_RSA_OAEP = "http://www.w3.org/2009/xmlenc11#rsa-oaep";

  /**
   * Default-Algorithmus für asymmetrische Verschlüsselung
   */
  public static final String DEFAULT_ASYMMETRIC_CIPHER_ALGORITHM = ASYMMETRIC_CIPHER_ALGORITHM_RSA_OAEP;

  /**
   * Algorithmus für Maskengenerierung (RSA-OAEP) mit MGF1 und SHA-256
   */
  public static final String MASK_GENERATION_FUNCTION_1_SHA256 = "http://www.w3.org/2009/xmlenc11#mgf1sha256";

  /**
   * Algorithmus für Maskengenerierung (RSA-OAEP) mit MGF1 und SHA-384
   */
  public static final String MASK_GENERATION_FUNCTION_1_SHA384 = "http://www.w3.org/2009/xmlenc11#mgf1sha384";

  /**
   * Algorithmus für Maskengenerierung (RSA-OAEP) mit MGF1 und SHA-512
   */
  public static final String MASK_GENERATION_FUNCTION_1_SHA512 = "http://www.w3.org/2009/xmlenc11#mgf1sha512";

  /**
   * Algorithmus für asymmetrische Verschlüsselung RSAES-PKCS1-v1_5, JCA/JCE-Identifier
   */
  @Deprecated
  private static final String ASYMMETRIC_CIPHER_ALGORITHM_RSA_1_5_JCA_JCE = "RSA/ECB/PKCS1Padding";

  /**
   * Algorithmus für asymmetrische Verschlüsselung RSAES-OAEP, JCA/JCE-Identifier
   */
  private static final String ASYMMETRIC_CIPHER_ALGORITHM_RSA_OAEP_JCA_JCE = "RSA/ECB/OAEPPadding";

  /**
   * Event-Id 'Verbindung zum Intermediär wird aufgebaut'
   */
  public static final int EVENT_CONNECT = 0;

  /**
   * Event-Id 'Auftragsnachricht wird signiert'
   */
  public static final int EVENT_SIGN_MSG = 1;

  /**
   * Event-Id 'Auftragsnachricht wird versendet'
   */
  public static final int EVENT_SEND_MSG = 2;

  /**
   * Event-Id 'Antwortnachricht wird empfangen'
   */
  public static final int EVENT_RECEIVE_MSG = 3;

  /**
   * Event-Id 'Vorgang abgeschlossen'
   */
  public static final int EVENT_ACTION_COMPLETE = 4;

  public static final int DEFAULT_BUFFER_BLOCKSIZE = Integer.parseInt(System.getProperty("osci.default.blocksize", "32768"));

  // Transforms
  /**
   * XSLT-Transformation
   */
  public static final String TRANSFORM_XSLT = "http://www.w3.org/TR/1999/REC-xslt-19991116";

  /**
   * Kanonisierung
   */
  public static final String TRANSFORM_CANONICALIZATION = "http://www.w3.org/TR/2001/REC-xml-c14n-20010315";

  /**
   * Tranformer für Base64-Decoder
   */
  public static final String TRANSFORM_BASE64 = "http://www.w3.org/2000/09/xmldsig#base64";

  static
  {

    Hashtable<String, String> tmpJcaJce = new Hashtable<String, String>();
    tmpJcaJce.put(ASYMMETRIC_CIPHER_ALGORITHM_RSA_1_5, ASYMMETRIC_CIPHER_ALGORITHM_RSA_1_5_JCA_JCE);
    tmpJcaJce.put(ASYMMETRIC_CIPHER_ALGORITHM_RSA_OAEP, ASYMMETRIC_CIPHER_ALGORITHM_RSA_OAEP_JCA_JCE);

    tmpJcaJce.put(DIGEST_ALGORITHM_SHA1, DIGEST_ALGORITHM_SHA1_JCA_JCE);
    tmpJcaJce.put(DIGEST_ALGORITHM_SHA256, DIGEST_ALGORITHM_SHA256_JCA_JCE);
    tmpJcaJce.put(DIGEST_ALGORITHM_SHA512, DIGEST_ALGORITHM_SHA512_JCA_JCE);
    tmpJcaJce.put(DIGEST_ALGORITHM_RIPEMD160, DIGEST_ALGORITHM_RIPEMD160_JCA_JCE);
    tmpJcaJce.put(DIGEST_ALGORITHM_SHA3_256, DIGEST_ALGORITHM_SHA3_256_JCA_JCE);
    tmpJcaJce.put(DIGEST_ALGORITHM_SHA3_384, DIGEST_ALGORITHM_SHA3_384_JCA_JCE);
    tmpJcaJce.put(DIGEST_ALGORITHM_SHA3_512, DIGEST_ALGORITHM_SHA3_512_JCA_JCE);

    tmpJcaJce.put(SIGNATURE_ALGORITHM_RSA_SHA1, SIGNATURE_ALGORITHM_RSA_SHA1_JCA_JCE);
    tmpJcaJce.put(SIGNATURE_ALGORITHM_RSA_SHA256, SIGNATURE_ALGORITHM_RSA_SHA256_JCA_JCE);
    tmpJcaJce.put(SIGNATURE_ALGORITHM_RSA_SHA512, SIGNATURE_ALGORITHM_RSA_SHA512_JCA_JCE);
    tmpJcaJce.put(SIGNATURE_ALGORITHM_RSA_SHA256_PSS, SIGNATURE_ALGORITHM_RSA_SHA256_PSS_JCA_JCE);
    tmpJcaJce.put(SIGNATURE_ALGORITHM_RSA_SHA512_PSS, SIGNATURE_ALGORITHM_RSA_SHA512_PSS_JCA_JCE);
    tmpJcaJce.put(SIGNATURE_ALGORITHM_RSA_RIPEMD160, SIGNATURE_ALGORITHM_RSA_RIPEMD160_JCA_JCE);
    tmpJcaJce.put(SIGNATURE_ALGORITHM_RSA_RIPEMD160_RFC4051, SIGNATURE_ALGORITHM_RSA_RIPEMD160_JCA_JCE);
    tmpJcaJce.put(SIGNATURE_ALGORITHM_ECDSA_SHA256, SIGNATURE_ALGORITHM_ECDSA_SHA256_JCA_JCE);
    tmpJcaJce.put(SIGNATURE_ALGORITHM_ECDSA_SHA512, SIGNATURE_ALGORITHM_ECDSA_SHA512_JCA_JCE);
    tmpJcaJce.put(SIGNATURE_ALGORITHM_RSA_SHA3_256, SIGNATURE_ALGORITHM_RSA_SHA3_256_JCA_JCE);
    tmpJcaJce.put(SIGNATURE_ALGORITHM_RSA_SHA3_384, SIGNATURE_ALGORITHM_RSA_SHA3_384_JCA_JCE);
    tmpJcaJce.put(SIGNATURE_ALGORITHM_RSA_SHA3_512, SIGNATURE_ALGORITHM_RSA_SHA3_512_JCA_JCE);

    tmpJcaJce.put(SYMMETRIC_CIPHER_ALGORITHM_TDES_CBC, SYMMETRIC_CIPHER_ALGORITHM_TDES_CBC_JCA_JCE);
    tmpJcaJce.put(SYMMETRIC_CIPHER_ALGORITHM_AES128, SYMMETRIC_CIPHER_ALGORITHM_AES_CBC_JCA_JCE);
    tmpJcaJce.put(SYMMETRIC_CIPHER_ALGORITHM_AES192, SYMMETRIC_CIPHER_ALGORITHM_AES_CBC_JCA_JCE);
    tmpJcaJce.put(SYMMETRIC_CIPHER_ALGORITHM_AES256, SYMMETRIC_CIPHER_ALGORITHM_AES_CBC_JCA_JCE);

    tmpJcaJce.put(SYMMETRIC_CIPHER_ALGORITHM_AES128_GCM, SYMMETRIC_CIPHER_ALGORITHM_AES_GCM_JCA_JCE);
    tmpJcaJce.put(SYMMETRIC_CIPHER_ALGORITHM_AES192_GCM, SYMMETRIC_CIPHER_ALGORITHM_AES_GCM_JCA_JCE);
    tmpJcaJce.put(SYMMETRIC_CIPHER_ALGORITHM_AES256_GCM, SYMMETRIC_CIPHER_ALGORITHM_AES_GCM_JCA_JCE);

    JCA_JCE_MAP = new UnmodifiableHashtable<String, String>(tmpJcaJce);

    // Note: (Calendar month based on 0)
    Calendar c = new GregorianCalendar(TimeZone.getTimeZone("Europe/Berlin"));
    c.clear();
    c.set(2014, 2, 1);
    ACTUAL_DATE = new UnmodifiableDate(c.getTime());

    c.clear();
    c.set(2008, 3, 1);
    OUT_DATE_KEYSIZE_1024 = new UnmodifiableDate(c.getTime());

    Hashtable<String, Date> tmpOutDates = new Hashtable<String, Date>();

    c.clear();
    c.set(2008, 6, 1); // BSI-Empfehlung
    tmpOutDates.put(DIGEST_ALGORITHM_SHA1, new UnmodifiableDate(c.getTime()));
    tmpOutDates.put(SIGNATURE_ALGORITHM_RSA_SHA1, new UnmodifiableDate(c.getTime()));

    c.clear();
    c.set(2011, 0, 1);
    tmpOutDates.put(DIGEST_ALGORITHM_RIPEMD160, new UnmodifiableDate(c.getTime()));
    tmpOutDates.put(SIGNATURE_ALGORITHM_RSA_RIPEMD160, new UnmodifiableDate(c.getTime()));
    tmpOutDates.put(SIGNATURE_ALGORITHM_RSA_RIPEMD160_RFC4051, new UnmodifiableDate(c.getTime()));

    OUT_DATES = new UnmodifiableHashtable<String, Date>(tmpOutDates);

  }


  /**
   * Helper class to make some final static Date constants unmodifiable
   *
   * TODO: find better solution in next major version of OSCI-Lib!
   * (Don't use Date as part of the public interface)
   */
  @SuppressWarnings("deprecation")
  private static class UnmodifiableDate extends Date
  {

    private UnmodifiableDate(final Date origin) {
      super(origin.getTime());
    }

    @Override
    public final void setYear(final int year)
    {
      throw new UnsupportedOperationException("modification not supported");
    }

    @Override
    public final void setMonth(final int month)
    {
      throw new UnsupportedOperationException("modification not supported");
    }

    @Override
    public final void setDate(final int date)
    {
      throw new UnsupportedOperationException("modification not supported");
    }

    @Override
    public final void setHours(final int hours)
    {
      throw new UnsupportedOperationException("modification not supported");
    }

    @Override
    public final void setMinutes(final int minutes)
    {
      throw new UnsupportedOperationException("modification not supported");
    }

    @Override
    public final void setSeconds(final int seconds)
    {
      throw new UnsupportedOperationException("modification not supported");
    }

    @Override
    public final void setTime(final long time)
    {
      throw new UnsupportedOperationException("modification not supported");
    }
  }


  /**
   * Helper class to make some final static Hashtable constants unmodifiable
   *
   * TODO: find better solution in next major version of OSCI-Lib!
   * (Don't use Hashtable as part of the public interface)
   */
  private static class UnmodifiableHashtable<K extends Object, V extends Object> extends Hashtable<K, V>
  {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private final boolean initialized;

    /**
     * Constructor.
     *
     * @param m the map whose mappings are to be placed in this table.
     */
    public UnmodifiableHashtable(Map<? extends K, ? extends V> m)
    {
      super(m);
      this.initialized = true;
    }

    @Override
    public synchronized V put(K key, V value)
    {
      if (this.initialized)
      {
        throw new UnsupportedOperationException("modifications not supported");
      }
      return super.put(key, value);
    }

    @Override
    public synchronized void putAll(Map<? extends K, ? extends V> t)
    {
      if (this.initialized)
      {
        throw new UnsupportedOperationException("modifications not supported");
      }
      super.putAll(t);
    }

    @Override
    public synchronized V remove(Object key)
    {
      if (this.initialized)
      {
        throw new UnsupportedOperationException("modifications not supported");
      }
      return super.remove(key);
    }

    @Override
    public synchronized void clear()
    {
      if (this.initialized)
      {
        throw new UnsupportedOperationException("modifications not supported");
      }
      super.clear();
    }

  }
}
