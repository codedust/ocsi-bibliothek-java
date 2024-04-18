package de.osci.osci12.samples.impl.crypto;

import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.osci.osci12.common.Constants;
import de.osci.osci12.common.DialogHandler;
import de.osci.osci12.encryption.OSCICipherException;


/**
 * Diese Klasse ist eine Beispiel-Implementierung der abstrakten Decrypter-Klasse.
 * Für die Verwendung wird ein PKCS#12-Keystore in Form einer *.p12-Datei benötigt.
 * Die Implementierung ist für Testzwecke bestimmt, sie greift auf den ersten
 * verfügbaren Alias zu. Die PIN für dessen Privatschlüssel muss die gleiche sein
 * wie die des Keystores.
 *
 * Diese einfache Implementierung hält die PIN des Keystores als Character-Array
 * im Arbeitsspeicher, sie wird als String übergeben. Es wird Anwendern empfohlen,
 * eigene Implementierungen zu schreiben, die die PIN in der Methode decrypt(...)
 * abfragen und nach Gebrauch wieder löschen oder anderweitig für ein sicheres
 * Pin-Cashing zu sorgen.
 *
 * <p>Copyright © 2023 Governikus GmbH &amp; Co. KG, Germany</p>
 * <p>Erstellt von Governikus GmbH &amp; Co. KG</p>
 * <p>Diese Bibliothek kann von jedermann nach Maßgabe der European Union
 *  Public Licence genutzt werden.</p><p>Die Lizenzbestimmungen können
 * unter der URL <a href="https://eupl.eu/">https://eupl.eu/</a> abgerufen werden.
 *
 *
 * @author R. Lindemann, N.Büngener
 * @version 2.4.1
 * @see de.osci.osci12.extinterfaces.crypto.Decrypter
 */
public class PKCS12Decrypter extends de.osci.osci12.extinterfaces.crypto.Decrypter
{
  private static Log log = LogFactory.getLog(PKCS12Decrypter.class);
  private X509Certificate cert;
  private String alias;
  private char[] pin = null;
  private KeyStore keyStore = null;
  private Key key;

  /**
   * Legt ein neues PKCS12Decrypter Objekt an.
   *
   * @param p12_fileName Pfad zur PKSC#12-Keystore Datei
   * @param pin PIN
   *
   * @throws KeyStoreException wenn Probleme beim Laden des Keystores auftreten
   * @throws CertificateException bei Problemen bei der Verarbeitung der Zertifikate
   * @throws NoSuchAlgorithmException falls nicht unterstützte Algotrihmen erforderlich sind
   * @throws IOException bei Schreib-/Leseproblemen
   * @throws UnrecoverableKeyException Fehler beim Einlesen der Schlüssel
   */
  public PKCS12Decrypter(String p12_fileName, String pin)
                  throws KeyStoreException,
                         CertificateException,
                         NoSuchAlgorithmException,
                         IOException,
                         UnrecoverableKeyException
  {
    init(PKCS12Decrypter.class.getResourceAsStream(p12_fileName), pin);
  }

  /**
   * Legt ein neues PKCS12Decrypter Objekt an.
   *
   * @param in InputStream des PKSC#12-Keystores
   * @param pin PIN
   *
   * @throws KeyStoreException wenn Probleme beim Laden des Keystores auftreten
   * @throws CertificateException bei Problemen bei der Verarbeitung der Zertifikate
   * @throws NoSuchAlgorithmException falls nicht unterstützte Algotrihmen erforderlich sind
   * @throws IOException bei Schreib-/Leseproblemen
   * @throws UnrecoverableKeyException Fehler beim Einlesen der Schlüssel
   */
  public PKCS12Decrypter(InputStream in, String pin)
                  throws KeyStoreException,
                         CertificateException,
                         NoSuchAlgorithmException,
                         IOException,
                         UnrecoverableKeyException
  {
    init(in, pin);
  }

  private void init(InputStream in, String pin)
             throws KeyStoreException,
                    CertificateException,
                    NoSuchAlgorithmException,
                    IOException,
                    UnrecoverableKeyException
  {
    this.pin = pin.toCharArray();

    if (DialogHandler.getSecurityProvider() == null)
      keyStore = java.security.KeyStore.getInstance("PKCS12");
    else
      keyStore = java.security.KeyStore.getInstance("PKCS12", DialogHandler.getSecurityProvider());

    keyStore.load(in, this.pin);

    String al = null;
    Enumeration<String> e = keyStore.aliases();

    while (e.hasMoreElements())

      if (keyStore.isKeyEntry(al = e.nextElement()))
        break;

    if (al == null)
      throw new NullPointerException("No private key found in keystore.");

    this.alias = al;
    cert = (X509Certificate) keyStore.getCertificate(alias);
    key = keyStore.getKey(alias, this.pin);
  }

  /**
   * Liefert den Namen des Herstellers.
   * @return Herstellername
   */
  public String getVendor()
  {
    return "BOS";
  }

  /**
   * Liefert die Versionsnummer.
   * @return Versionsnummer
   */
  public String getVersion()
  {
    return "1.6";
  }

  /**
   *  Liefert das Verschlüsselungszertifikat.
   *
   *@return  das Verschlüsselungszertifikat
   */
  public X509Certificate getCertificate()
  {
    return cert;
  }

  /**
   *  Entschlüsselt das übergebene Byte-Array gemäß RSAES-PKCS1-v1_5
   *
   *@param  data die zu entschlüsselnden Daten
   *@return verschlüsselten Daten
   */
  public byte[] decrypt(byte[] data) throws de.osci.osci12.encryption.OSCICipherException
  {
    try
    {
      return de.osci.osci12.encryption.Crypto.doRSADecryption(key, data);
    }
    catch (Exception ex)
    {
      throw new de.osci.osci12.encryption.OSCICipherException("decryption_error");
    }
  }

  /**
   *  Entschlüsselt das übergebene Byte-Array gemäß RSAES-OAEP
   *
   *@param  data die zu entschlüsselnden Daten
   *@return verschlüsselten Daten
   */
  @Override
  public byte[] decrypt(byte[] data, String mgfAlgorithm, String digestAlgorithm) throws OSCICipherException,
                                                                    de.osci.osci12.common.OSCICancelledException
  {
    try
    {
      return de.osci.osci12.encryption.Crypto.doRSADecryption(key, data, Constants.ASYMMETRIC_CIPHER_ALGORITHM_RSA_OAEP, mgfAlgorithm, digestAlgorithm, null);
    }
    catch (Exception ex)
    {
      throw new de.osci.osci12.encryption.OSCICipherException("decryption_error");
    }
  }

}
