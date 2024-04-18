package de.osci.osci12.common;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.crypto.SecretKey;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.osci.osci12.common.OSCIExceptionCodes.OSCIErrorCodes;
import de.osci.osci12.encryption.Crypto;
import de.osci.osci12.extinterfaces.DialogFinder;
import de.osci.osci12.extinterfaces.OSCIDataSource;
import de.osci.osci12.extinterfaces.ProgressEventHandlerI;
import de.osci.osci12.extinterfaces.TransportI;
import de.osci.osci12.messageparts.ProcessCardBundle;
import de.osci.osci12.messagetypes.OSCIMessage;
import de.osci.osci12.roles.Addressee;
import de.osci.osci12.roles.Intermed;
import de.osci.osci12.roles.Originator;
import de.osci.osci12.roles.Role;
import de.osci.osci12.soapheader.ControlBlockH;


/**
 * Der DialogHandler steuert die Kommunikation mit dem Intermediär. Für die Kommunikation mit dem Intermediär
 * müssen eine Reihe von Rahmenparametern gesetzt werden. Daher ist diese Klasse <b>zentral</b> fuer jede
 * Kommunikation. Ein DialogHandler-Objekt ist für jede Nachricht erforderlich, unabhängig davon, ob diese
 * innerhalb eines impliziten oder expliziten Dialogs verarbeitet wird.
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
 */
public class DialogHandler
{

  private static Log log = LogFactory.getLog(DialogHandler.class);

  private static Provider securityProvider;

  /**
   * ResourceBundle-Objekt, welches bibliotheksweit für die Textausgaben verwendet wird.
   */
  public static final ResourceBundle text = ResourceBundle.getBundle("de.osci.osci12.extinterfaces.language.Text",
                                                                     java.util.Locale.getDefault());

  static
  {
    try
    {
      securityProvider = (Provider)Class.forName("org.bouncycastle.jce.provider.BouncyCastleProvider")
                                        .newInstance();
      // Workaround: Eigentlich sollte es nicht notwendig sein, den Provider zu registrieren,
      // aber die Erfahrung zeigt etwas anderes!
      Security.addProvider(securityProvider);
    }
    catch (Exception ex)
    {
      // Falls Provider nicht vorhanden, muss ein eigener installiert
      // werden, ab Java 1.5 funktionieren die JDK-Provider
      log.info(text.getString("no_secprov") + " org.bouncycastle.jce.provider.BouncyCastleProvider");
    }
  }

  /** Hashalgortihmus (Voreinstellung SHA-256) */
  private static String digestAlgorithm = Constants.DIGEST_ALGORITHM_SHA256;

  // private static String digestAlgorithm = de.osci.osci12.common.Constants.DIGEST_ALGORITHM_SHA1;
  /** Symmetrischer Verschlüsselungsalgorithmus (Nachrichtenverschlüsselung) */
  private String symmetricCipherAlgorithm = Constants.DEFAULT_SYMMETRIC_CIPHER_ALGORITHM;
  
  /** Länge des Initialisierungsvektors (in Bytes) für die Transportverschlüsselung bei Nutzung von AES-GCM */
  private int ivLength = Constants.DEFAULT_GCM_IV_LENGTH;

  /** Asymmetrischer Verschlüsselungsalgorithmus (Verschlüsselung des Sitzungsschlüssels) */
  private String asymmetricCipherAlgorithm = Constants.DEFAULT_ASYMMETRIC_CIPHER_ALGORITHM;

  // private static String asymmetricCipherAlgorithm =
  // de.osci.osci12.common.Constants.ASYMMETRIC_CIPHER_ALGORITHM_RSA_OAEP;
  /** Zufallsgenerator-Algorithmus */
  private static String secureRandomAlgorithm = Constants.SECURE_RANDOM_ALGORITHM_SHA1;

  /** Signaturalgorithmus (Nachrichtensignatur) */
  private static String signatureAlgorithm = Constants.SIGNATURE_ALGORITHM_RSA_SHA256;

  // private static String signatureAlgorithm = Constants.SIGNATURE_ALGORITHM_RSA_SHA1;
  /** Signatur des Suppliers prüfen. */
  private boolean checkSignatures = true;

  /** Der Controlblock. */
  private ControlBlockH controlblock = new ControlBlockH();

  /** Nachrichtensignatur anbringen. */
  private boolean createSignatures = true;

  /** Sobald dieser Wert true ist ein FeatureDescription Header gesendet. */
  private boolean sendFeatureDescription = true;

  /** Ein Objekt welches das Interface TranportI implementiert */
  private TransportI transportModule;

  /** Nachrichtenverschluesselung. */
  private boolean encryption = true;

  /** Sprachliste. */
  private String languageList = Locale.getDefault().getLanguage();

  /** Implementierung des Interfaces ProgressEventHandlerI. */
  private ProgressEventHandlerI progressEventHandler = null;

  /** Client */
  public Role client = null;

  /** Supplier */
  public Role supplier = null;

  boolean disableControlBlockCheck = false;

  /** For internal use only */
  static DialogFinder dialogFinder;

  /** For internal use only */
  static Role[] defaultSupplier;

  /**
   * Status impliziter/expliziter Dialog. Sollte von Anwendungen nicht gesetzt werden.
   */
  public boolean explicitDialog = false;

  /**
   * Der Trenner für die einzelnen MIME boundaries
   */
  public static String boundary = "MIME_boundary";

  private static OSCIDataSource dataBuffer = new SwapBuffer();

  ProcessCardBundle processCardForTimestampReception;

  private static SecretKey tempKey;

  /**
   * Konstruktor für alle Aufträge/Auftragsantworten, ausgenommen Annahmeauftrag/-antwort und
   * Bearbeitungsauftrag/-antwort.
   *
   * @param client Originator als Client. Sollen Aufträge signiert versendet werden oder werden verschlüsselte
   *          Rückantworten erwartet, so muss für dieses Objekt ein Decrypter- bzw. Signer-Objekt gesetzt
   *          sein. Die gilt für die Verschlüsselung bzw. Signatur der Nachricht wie für die der Inhaltsdaten.
   * @param supplier Intermediär als Supplier.
   * @param transportModule zu verwendende Implementierung des TransportInterfaces
   * @see de.osci.osci12.roles.Originator
   */
  public DialogHandler(Originator client, Intermed supplier, TransportI transportModule)
  {
    this.supplier = supplier;

    if ((supplier == null) && (defaultSupplier != null))
      this.supplier = defaultSupplier[0];

    this.transportModule = transportModule;
    this.client = client;
  }

  /**
   * DialogHandler für Annahmeauftrag/-antwort und Bearbeitungsauftrag/-antwort. Dieser Konstruktor wird vom
   * Anwender nicht benötigt.
   *
   * @param client Intermediär als Client.
   * @param supplier Addressee als Supplier.
   * @param transportModule Implementierung des TransportInterfaces
   * @see de.osci.osci12.roles.Addressee
   */
  public DialogHandler(Intermed client, Addressee supplier, TransportI transportModule)
  {
    this.supplier = supplier;

    if ((supplier == null) && (defaultSupplier != null))
    {
      this.supplier = defaultSupplier[0];
    }

    this.transportModule = transportModule;
    this.client = client;
  }

  /**
   * Liefert einen 128-Bit-AES-Schlüssel für beliebige interne Verschlüsselungsaufgaben. Es wird innerhalb der
   * JVM-Instanz immer derselbe, einmal angelegte Schlüssel zurückgegeben.
   *
   * @return symmetrischen 128-Bit-AES-Schlüssel
   * @throws NoSuchAlgorithmException undocumented
   */
  public static SecretKey getTempSymKey() throws NoSuchAlgorithmException
  {
    if (tempKey == null)
      tempKey = Crypto.createSymKey(Constants.DEFAULT_SYMMETRIC_CIPHER_ALGORITHM);

    return tempKey;
  }

  /**
   * Liefert <b>true</b>, wenn die Nachrichtensignaturen (Client-/Suppliersignatur) beim Empfang überprüft
   * werden, sonst <b>false</b>.
   *
   * @return Wert des Attributs
   * @see #setCheckSignatures(boolean)
   */
  public boolean isCheckSignatures()
  {
    return checkSignatures;
  }

  /**
   * Legt fest, ob Nachrichtensignaturen (Client-/Suppliersignaturen) beim Empfang geprüft werden sollen.
   * Voreinstellung ist <b>true</b>. <b>Achtung: </b>Diese Eigenschaft legt lediglich fest, ob vorhandene
   * Signaturen eingehender Nachrichten mathematisch geprüft werden. Fehlt eine Signatur ganz, wird keine
   * Exception o.ä. ausgelöst. Es sollte daher zusätzlich OSCIMessage.isSigned() geprüft werden.
   *
   * @param check <b>true</b> - Signaturprüfung wird durchgeführt.
   * @see #isCheckSignatures()
   * @see de.osci.osci12.messagetypes.OSCIMessage#isSigned()
   */
  public void setCheckSignatures(boolean check)
  {
    this.checkSignatures = check;
  }

  /**
   * Liefert <b>true</b>, wenn an den Nachrichten eine Signatur (Client- bzw. Suppliersignatur) angebracht
   * wird, sonst <b>false</b>.
   *
   * @return Wert des Attributs
   * @see #setCreateSignatures(boolean)
   */
  public boolean isCreateSignatures()
  {
    return createSignatures;
  }

  /**
   * Legt fest, ob Nachrichtensignaturen (Client-/Suppliersignaturen) beim Versand angebracht werden sollen.
   * Voreinstellung ist <b>true</b>.
   *
   * @param sign true ausgehende Nachrichten werden signiert
   * @see #isCreateSignatures()
   */
  public void setCreateSignatures(boolean sign)
  {
    this.createSignatures = sign;
  }

  /**
   * Liefert den eingestellten Wert, ob eine FeatureDescription gesendet werden soll oder nicht. Es ist
   * ebenfalls möglich das Versenden der FeatureDescription mit einem System Property
   * "de.osci.SendFeatureDescription" zu steuern. Sobald diese System Property gesetzt ist, ist sie vorrangig.
   *
   * @return True sobald eine FeatureDescription versendet werden soll.
   * @see #setSendFeatureDescription(boolean)
   */
  public boolean isSendFeatureDescription()
  {
    if ("false".equals(System.getProperty("de.osci.SendFeatureDescription")))
    {
      return false;
    }
    else if ("true".equals(System.getProperty("de.osci.SendFeatureDescription")))
    {
      return true;
    }
    return sendFeatureDescription;
  }


  /**
   * setSendFeatureDescription
   * 
   * @param sendFeatureDescription <code>true/false</code>
   */
  public void setSendFeatureDescription(boolean sendFeatureDescription)
  {
    this.sendFeatureDescription = sendFeatureDescription;
  }

  /**
   * Speichert den Challenge-Wert einer vorangegangenen Response-Nachricht. Wird von Anwednungen normalerweise
   * nicht benötigt und sollte auch nicht gesetzt werden.
   */
  public String prevChallenge;

  /**
   * Interne Methode, sollte von Anwendungen nicht aufgerufen werden.
   * 
   * @param cb {@link ControlBlockH}
   * @throws SoapClientException undocumented
   */
  public void checkControlBlock(ControlBlockH cb /* , boolean update */) throws SoapClientException
  {
    if (disableControlBlockCheck)
      return;
    if (log.isDebugEnabled())
      log.debug("\nRSP: " + controlblock.getResponse() + " : " + cb.getResponse());

    if (log.isDebugEnabled())
      log.debug("\nCHALL: " + controlblock.getChallenge() + " : " + cb.getChallenge());

    if (log.isDebugEnabled())
      log.debug("\nConvID: " + controlblock.getConversationID() + " : " + cb.getConversationID());

    if (log.isDebugEnabled())
      log.debug("\nSQU: " + controlblock.getSequenceNumber() + " : " + cb.getSequenceNumber());

    if (controlblock.getChallenge() != null)
    {
      if (!cb.getResponse().equals(controlblock.getChallenge()))
        throw new SoapClientException(OSCIErrorCodes.WrongControlBlock);
    }

    prevChallenge = cb.getChallenge();

    if (controlblock.getConversationID() != null)
    {
      // der folgende Fall kann eigentlich nicht eintreten, weil der
      // ControlBlock anhand der ConvId gesucht wurde
      if (!controlblock.getConversationID().equals(cb.getConversationID()))
        throw new SoapClientException(OSCIErrorCodes.WrongControlBlock);
    }
    else
      controlblock.setConversationID(cb.getConversationID());

    if ((cb.getSequenceNumber() > -1) && (cb.getSequenceNumber() != controlblock.getSequenceNumber()))
      throw new SoapClientException(OSCIErrorCodes.WrongControlBlock);

    controlblock.setSequenceNumber(cb.getSequenceNumber());
    controlblock.setChallenge(cb.getChallenge());
    controlblock.setResponse(cb.getResponse());
  }

  /**
   * Interne Methode, sollte von Anwendungen nicht aufgerufen werden.
   * 
   * @param controlBlock {@link ControlBlockH}
   * @return DialogHandler {@link DialogHandler}
   * @throws SoapClientException undocumented
   */
  public static DialogHandler findDialog(ControlBlockH controlBlock) throws SoapClientException
  {
    DialogHandler dh = null;

    if (dialogFinder != null)
    {
      dh = dialogFinder.findDialog(controlBlock);
    }

    if (dh == null)
      throw new SoapClientException(OSCIErrorCodes.WrongControlBlock);
    else

      return dh;
  }

  /**
   * Liefert die installierte Implementierung des Transportinterfaces.
   *
   * @return Transportinterface-Implementierung.
   * @see de.osci.osci12.extinterfaces.TransportI
   */
  public TransportI getTransportModule()
  {
    return transportModule;
  }

  /**
   * Liefert <b>true</b>, wenn die Nachrichten als verschlüsselte Auftragsdaten versendet werden.
   *
   * @return Wert des Attributs
   * @see #setEncryption(boolean)
   */
  public boolean isEncryption()
  {
    return encryption;
  }

  /**
   * Legt fest, ob die Nachrichten als verschlüsselte Auftragsdaten versendet werden. Voreinstellung ist
   * <b>true</b>.<br>
   * <b>Hinweis: </b>Das Abschalten der Nachrichtenverschlüsselung ist nur in Ausnahmefällen sinnvoll (z.B.
   * bei der Verwendung von SSL), weil hierdurch die Sicherheitsmechanismen auf Nachrichtenebene wirkungslos
   * werden. Die OSCI-Bibliothek setzt das encryption-Flag für einen Dialog auf true, wenn innerhalb des
   * Dialoges eine verschlüsselte Nachricht empfangen wird. Damit kann jeder Kommunikationspartner das höhere
   * Sicherheitsniveau für den Dialog erzwingen. Da insbesondere Dialoginitialisierungsantworten vom
   * Intermediär verschlüsselt werden müssen, kann es erfoderlich sein, dieses Flag nach dem Empfang einer
   * Nachricht erneut auf false zu setzen, wenn keine Verschlüsselung gewünscht wird.
   *
   * @param doEncryption ausgehende Nachrichten werden verschlüsselt
   * @see #isEncryption()
   */
  public void setEncryption(boolean doEncryption)
  {
    this.encryption = doEncryption;
  }

  /**
   * Liefert den aktuellen Controlblock. Ein Controlblock beinhaltet Challenge, Response, ConversationID und
   * Sequenznummer.
   *
   * @return ControlBlock dieses Dialogs
   */
  public ControlBlockH getControlblock()
  {
    return controlblock;
  }

  /**
   * Liefert die aktuell registrierte Implementierung des ProgressI-Interfaces.
   *
   * @return ProgressEventHandlerI-Implementierung
   * @see de.osci.osci12.extinterfaces.ProgressEventHandlerI
   */
  public ProgressEventHandlerI getProgressEventHandler()
  {
    return progressEventHandler;
  }

  /**
   * Registriert eine Instanz (einer Implementierung) des ProgressEventHandlerI- Interfaces. Das registrierte
   * Objekt wird im Verlauf der Verarbeitung der Nachricht durch Aufruf seiner Methode event(int,String,int)
   * über Fortschrittsereignisse informiert.
   *
   * @param progressEventHandler ProgressEventHandlerI-Implementierung
   * @see de.osci.osci12.extinterfaces.ProgressEventHandlerI
   */
  public void setProgressEventHandler(ProgressEventHandlerI progressEventHandler)
  {
    this.progressEventHandler = progressEventHandler;
  }

  /**
   * Setzt den ControlBlock zuRück. Erlaubt die Wiederverwendung dieses Objekts in einem neuen Dialog.
   */
  public void resetControlBlock()
  {
    this.controlblock = new ControlBlockH();
  }

  /**
   * Liefert den mit diesem DialogHandler verbundenen Client.
   *
   * @return Rollenobjekt, welches als Client fungiert
   */
  public Role getClient()
  {
    return this.client;
  }

  /**
   * Liefert den mit diesem DialogHandler verbundenen Supplier.
   *
   * @return Rollenobjekt, welches als Supplier fungiert
   */
  public Role getSupplier()
  {
    return supplier;
  }

  /**
   * Liefert die Liste der Sprachkürzel, die in den DesiredLanguages-Elementen eingetragen wird.
   *
   * @return Liste der Sprachkürzel, getrennt durch Leerzeichen, z.B. "de en-US fr"
   * @see #setLanguageList(String)
   */
  public String getLanguageList()
  {
    return languageList;
  }

  /**
   * Setzt die Liste der Sprachkürzel, die in den DesiredLanguages-Elementen eingetragen wird. Voreingestellt
   * ist das kürzel der im default-Locale eingetragenen Sprache.
   *
   * @param languageList die Liste der Sprachkürzel, getrennt durch Leerzeichen, z.B. "de en-US fr"
   * @see #getLanguageList()
   */
  public void setLanguageList(String languageList)
  {
    this.languageList = languageList;
  }

  /**
   * Liefert den eingestellten Algorithmus für den verwendeten Zufallszahlengenerator.
   *
   * @return String-Identifier des Algorithmus
   * @see #setSecureRandomAlgorithm(String)
   */
  public static String getSecureRandomAlgorithm()
  {
    return secureRandomAlgorithm;
  }

  /**
   * Mit Hilfe dieser Methode kann ein Algorithmus für den verwendeten Zufallszahlengenerator gesetzt werden.
   * Dieser String wird von der Bibliothek bei der Initialisierung an die Methode
   * java.security.SecureRandom#getInstance(String) übergeben. Voreingestellt ist "SHA1PRNG".
   *
   * @param algorithm ein String-Identifier für den Algorithmus, der von dem installierten Provider
   *          unterstützt werden muss.
   * @see java.security.SecureRandom
   */
  public static void setSecureRandomAlgorithm(String algorithm)
  {
    secureRandomAlgorithm = algorithm;
  }

  /**
   * Liefert den Identifier des Signaturalgorithmus, der für die Verschlüsselung der Nachrichten
   * (verschlüsselte Auftragsdaten) verwendet wird.
   *
   * @return Identifier
   */
  public static String getSignatureAlgorithm()
  {
    return signatureAlgorithm;
  }

  /**
   * Setzt die Voreinstellung des Signaturalgorithmus, der für die Verschlüsselung der Nachrichten
   * (verschlüsselte Auftragsdaten) verwendet wird. Diese Default-Einstellung wird verwendet, wenn die
   * getAlgorithm()- Methode der Signer-Implementierung null zurückliefert.
   * 
   * @param newSignatureAlgorithm Algorithmus
   */
  public static void setSignatureAlgorithm(String newSignatureAlgorithm)
  {
    if (Constants.JCA_JCE_MAP.get(newSignatureAlgorithm) == null)
      throw new IllegalArgumentException(DialogHandler.text.getString("invalid_signature_algorithm") + " "
                                         + newSignatureAlgorithm);
    if (newSignatureAlgorithm.equals(Constants.SIGNATURE_ALGORITHM_RSA_SHA1))
      log.info("Using SHA-1 with RSA as default signature algorithm for signing OSCI messages.");

    signatureAlgorithm = newSignatureAlgorithm;
  }

  /**
   * Hiermit kann eine Implementierung der abstrakten Klasse OSCIDataSource installiert werden, falls
   * Inhaltsdaten nicht durch die default-Implementierung SwapBuffer im Arbeitsspeicher bzw. in temporären
   * Dateien gepuffert werden sollen, sondern beispielsweise in einer Datenbank.<br>
   * Dieser Puffer-Mechanismus wird von den Klassen EncryptedData, Content und Attachment genutzt. Zur
   * Implementierung eigener Klassen sind die Hinweise in der Dokumentation von OSCIDataSource zu beachten.
   *
   * @see OSCIDataSource
   * @see SwapBuffer
   * @param buffer die OSCIDataSource-Implementierung
   */
  public static void setDataBuffer(OSCIDataSource buffer)
  {
    dataBuffer = buffer;
  }

  /**
   * Interne Methode, sollte von Anwendungen nicht aufgerufen werden.
   * 
   * @return OSCIDataSource {@link OSCIDataSource}
   * @throws IOException undocumented
   */
  public static synchronized OSCIDataSource getNewDataBuffer() throws java.io.IOException
  {
    return dataBuffer.newInstance();
  }

  /**
   * Mit dieser Methode kann ein Security-Provider gesetzt werden, der von der Bibliothek für die
   * kryptographischen Operationen verwendet wird. Beim Laden der Klasse wird versucht, den
   * BouncyCastle-Provider vorzuinstallieren. Ist dieser nicht vorhanden, muss der Anwender mit dieser Methode
   * einen eigenen Provider setzen. Wird der Provider zu <b>null</b> gesetzt, werden über die JCE-Mechanismen
   * jeweils die ersten verfügbaren Provider verwendet.
   *
   * @param provider Security-Provider
   * @see #getSecurityProvider()
   * @see java.security.Provider
   */
  public static void setSecurityProvider(Provider provider)
  {
    securityProvider = provider;
  }

  /**
   * Liefert den registrierten Security-Provider für die kryptographischen Operationen.
   *
   * @see #setSecurityProvider(Provider)
   * @return securityProvider
   */
  public static Provider getSecurityProvider()
  {
    return securityProvider;
  }

  /**
   * Mit dieser Methode können passive Empfänger ein Addressee-Objekt als Default-Supplier setzen, das für den
   * Empfang einer Nachricht das richtige Decrypter- und gegebenenfalls Signer-Objekt zur Verfügung stellt.
   * Falls eingehende Nachrichten für verschiedene Privatschlüssel verschlüsselt sein können, muss die Methode
   * setDefaultSuppliers(Addressee[]) verwendet werden.
   *
   * @param supplier Rollenobjekt
   * @see #getDefaultSupplier()
   * @see #setDefaultSuppliers(Addressee[])
   * @see #getDefaultSuppliers()
   */
  public static void setDefaultSupplier(Addressee supplier)
  {
    defaultSupplier = new Role[1];
    defaultSupplier[0] = supplier;
  }

  /**
   * Liefert das als Default-Supplier gesetzte Addressee-Objekt. Wurden mehrere Default-Supplier konfiguriert,
   * wird der erste (Postion 0) zuRückgegeben.
   *
   * @return Default-Supplier
   * @see #setDefaultSupplier(Addressee)
   * @see #setDefaultSuppliers(Addressee[])
   * @see #getDefaultSuppliers()
   */
  public static Addressee getDefaultSupplier()
  {
    if (defaultSupplier == null)
      throw new IllegalStateException("No default supplier configured.");

    return (Addressee)defaultSupplier[0];
  }

  /**
   * Mit dieser Methode können passive Empfänger Addressee-Objekte als Default-Supplier setzen, die für den
   * Empfang von Nachrichten das Decrypter- und gegebenenfalls Signer-Objekte zur Verfügung stellen.
   *
   * @param supplier Rollenobjekt
   * @see #getDefaultSupplier()
   * @see #setDefaultSupplier(Addressee)
   * @see #getDefaultSuppliers()
   */
  public static void setDefaultSuppliers(Addressee[] supplier)
  {
    defaultSupplier = supplier;
  }

  /**
   * Liefert die als Default-Supplier gesetzten Addressee-Objekte.
   *
   * @return Default-Supplier
   * @see #getDefaultSupplier()
   * @see #setDefaultSupplier(Addressee)
   * @see #setDefaultSuppliers(Addressee[]) return Supplier-Objekte
   */
  public static Role[] getDefaultSuppliers()
  {
    return defaultSupplier;
  }

  /**
   * Liefert den symmetrischen Verschlüsselungs-Algorithmus.
   *
   * @return Identifier des Algorithmus
   */
  public String getSymmetricCipherAlgorithm()
  {
    return symmetricCipherAlgorithm;
  }

  /**
   * Setzen des symmetrischen Verschlüsselungs-Algorithmus (Nachrichtenverschlüsselung)
   *
   * @param symmetricCipherAlgorithm undocumented
   */
  public void setSymmetricCipherAlgorithm(String symmetricCipherAlgorithm)
  {
    this.symmetricCipherAlgorithm = symmetricCipherAlgorithm;
  }
  
  

  /**
   * Liefert die gewünschte Länge des Initialisierungsvektors für die Transportverschlüsselung bei Nutzung von AES-GCM.
   * 
   * @return
   */
  public int getIvLength()
  {
    return ivLength;
  }

  /**
   * Setzt die gewünschte Länge des Initialisierungsvektors für die Transportverschlüsselung bei Nutzung von AES-GCM.
   * 
   * @return
   */
  public void setIvLength(int ivLength)
  {
    this.ivLength = ivLength;
  }
  

  /**
   * Liefert den gesetzten Hash-Algorithmus.
   *
   * @return Identifier des Hash-Algorithmus
   * @see #setDigestAlgorithm(String)
   */
  public static String getDigestAlgorithm()
  {
    return digestAlgorithm;
  }

  /**
   * Setzt den Hash-Algorithmus für die Signatur der Nachrichten (Voreinstellung
   * http://www.w3.org/2000/09/xmldsig#sha256). Dieser Algorithmus wird verwendet, sofern auf der jeweiligen
   * OSCIMessage nichts anderes gesetzt wird. Der hier gesetzte Algorithmus wird bei RSA-OAEP-Verschlüsselung
   * ebenfalls für die Hashwert- und Maskenerzeugung verwendet.
   *
   * @param newDigestAlgorithm Hash-Algorithmus-Identifier
   * @see #getDigestAlgorithm()
   * @see OSCIMessage#setDigestAlgorithm(String)
   */
  public static void setDigestAlgorithm(String newDigestAlgorithm)
  {
    if (Constants.JCA_JCE_MAP.get(newDigestAlgorithm) == null) 
    {
      throw new IllegalArgumentException(DialogHandler.text.getString("invalid_hash_algorithm") + " "
        + newDigestAlgorithm);
    }
    
    if (Constants.DIGEST_ALGORITHM_SHA1.equals(newDigestAlgorithm))  
    {
      log.info("Using SHA-1 as default digest algorithm for signing OSCI messages.");
    }

    digestAlgorithm = newDigestAlgorithm;
  }

  /**
   * Liefert den gesetzten asymmetrischen Verschlüsselungsalgorithmus.
   *
   * @return Identifier des Verschlüsselungs-Algorithmus
   * @see #setAsymmetricCipherAlgorithm(String)
   */
  public String getAsymmetricCipherAlgorithm()
  {
    return asymmetricCipherAlgorithm;
  }

  /**
   * Setzt den asymmetrischen Verschlüsselungsalgorithmus (Verschlüsselung des Sitzungsschlüssels). Dieser
   * Algorithmus kann nur global gesetzt, Änderungen sind nicht Thread-safe.
   *
   * @param newAsymmetricCipherAlgorithm Algorithmus
   * @see #getAsymmetricCipherAlgorithm()
   */
  public void setAsymmetricCipherAlgorithm(String newAsymmetricCipherAlgorithm)
  {
    if (newAsymmetricCipherAlgorithm == null
        || !(newAsymmetricCipherAlgorithm.equals(Constants.ASYMMETRIC_CIPHER_ALGORITHM_RSA_1_5)
             || newAsymmetricCipherAlgorithm.equals(Constants.ASYMMETRIC_CIPHER_ALGORITHM_RSA_OAEP)))
      throw new IllegalArgumentException(DialogHandler.text.getString("encryption_algorithm_not_supported")
                                         + " " + newAsymmetricCipherAlgorithm);

    asymmetricCipherAlgorithm = newAsymmetricCipherAlgorithm;
  }

  /**
   * Wird von der Bibliothek aufgerufen, um Ereignisse an den registrierten ProgressEventHandlerI
   * weiterzugeben. Kann selbstverständlich auch von Anwendungen zu diesem Zweck verwendet werden.
   *
   * @param type Identifier des Events
   * @see Constants
   */
  public void fireEvent(int type)
  {
    if (progressEventHandler != null)
      progressEventHandler.event(type, DialogHandler.text.getString("event_" + type), -1);
  }

  /**
   * Status des Dialogs: explizit/implizit
   *
   * @return true explizit, false, implizit
   */
  public boolean isExplicitDialog()
  {
    return explicitDialog;
  }

}
