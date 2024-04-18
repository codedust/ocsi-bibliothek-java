package de.osci.osci12.samples.impl.crypto;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.osci.osci12.common.Constants;
import de.osci.osci12.common.DialogHandler;


/**
 * Diese Klasse ist eine Beispiel-Implementierung der abstrakten Signer-Klasse. Für die Verwendung wird ein
 * PKCS#12-Keystore in Form einer *.p12-Datei benötigt. Die Implementierung ist für Testzwecke bestimmt, sie
 * greift auf den ersten verfügbaren Alias zu. Die PIN für dessen Privatschlüssel muss die gleiche sein wie
 * die des Keystores. Diese einfache Implementierung hält die PIN des Keystores als Character-Array im
 * Arbeitsspeicher, sie wird als String übergeben. Es wird Anwendern empfohlen, eigene Implementierungen zu
 * schreiben, die die PIN in der Methode sign(...) abfragen und nach Gebrauch wieder löschen oder anderweitig
 * für ein sicheres Pin-Cashing zu sorgen.
 * <p>
 * Copyright © 2023 Governikus GmbH &amp; Co. KG, Germany
 * </p>
 * <p>Erstellt von Governikus GmbH &amp; Co. KG</p>
 * <p>
 * Diese Bibliothek kann von jedermann nach Maßgabe der European Union Public Licence genutzt
 * werden.
 * </p>
 * Die Lizenzbestimmungen können unter der URL <a href="https://eupl.eu/">https://eupl.eu/</a> abgerufen werden.</p>
 *
 * @author R. Lindemann, N.Büngener, J. Wothe
 * @version 2.4.1
 * @see de.osci.osci12.extinterfaces.crypto.Signer
 */
public class PKCS12Signer extends de.osci.osci12.extinterfaces.crypto.Signer
{

  private static Log log = LogFactory.getLog(PKCS12Signer.class);

  private X509Certificate cert;

  private String alias;

  private char[] pin = null;

  private KeyStore keyStore = null;

  private boolean usePSSforRSAkey = false;

  /**
   * Legt ein neues PKCS12Signer Objekt an.
   *
   * @param p12_fileName Pfad zur PKSC#12-Keystore Datei
   * @param pin PIN
   * @throws KeyStoreException wenn Probleme beim Laden des Keystores auftreten
   * @throws CertificateException bei Problemen bei der Verarbeitung der Zertifikate
   * @throws NoSuchAlgorithmException falls nicht unterstützte Algotrihmen erforderlich sind
   * @throws IOException bei Schreib-/Leseproblemen
   */
  public PKCS12Signer(String p12_fileName, String pin)
    throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException
  {
    this(PKCS12Signer.class.getResourceAsStream(p12_fileName), pin, false);
  }

  /**
   * Legt ein neues PKCS12Signer Objekt an.
   *
   * @param in InputStream des PKSC#12-Keystores
   * @param pin PIN
   * @throws KeyStoreException wenn Probleme beim Laden des Keystores auftreten
   * @throws CertificateException bei Problemen bei der Verarbeitung der Zertifikate
   * @throws NoSuchAlgorithmException falls nicht unterstützte Algotrihmen erforderlich sind
   * @throws IOException bei Schreib-/Leseproblemen
   * @throws UnrecoverableKeyException Fehler beim Einlesen der Schlüssel
   */
  public PKCS12Signer(InputStream in, String pin)
    throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException
  {
    this(in, pin, false);
  }

  /**
   * Legt ein neues PKCS12Signer Objekt an.
   *
   * @param p12_fileName Pfad zur PKSC#12-Keystore Datei
   * @param pin PIN
   * @param usePSSforRSAkey <code>true</code> for using PSS paddding in case of a RSA key
   * @throws KeyStoreException wenn Probleme beim Laden des Keystores auftreten
   * @throws CertificateException bei Problemen bei der Verarbeitung der Zertifikate
   * @throws NoSuchAlgorithmException falls nicht unterstützte Algotrihmen erforderlich sind
   * @throws IOException bei Schreib-/Leseproblemen
   */
  public PKCS12Signer(String p12_fileName, String pin, boolean usePSSforRSAkey)
    throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException
  {
    this(PKCS12Signer.class.getResourceAsStream(p12_fileName), pin, usePSSforRSAkey);
  }

  /**
   * Legt ein neues PKCS12Signer Objekt an.
   *
   * @param in InputStream des PKSC#12-Keystores
   * @param pin PIN
   * @param usePSSforRSAkey <code>true</code> for using PSS paddding in case of a RSA key
   * @throws KeyStoreException wenn Probleme beim Laden des Keystores auftreten
   * @throws CertificateException bei Problemen bei der Verarbeitung der Zertifikate
   * @throws NoSuchAlgorithmException falls nicht unterstützte Algotrihmen erforderlich sind
   * @throws IOException bei Schreib-/Leseproblemen
   * @throws UnrecoverableKeyException Fehler beim Einlesen der Schlüssel
   */
  public PKCS12Signer(InputStream in, String pin, boolean usePSSforRSAkey)
    throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException
  {
    this.pin = pin.toCharArray();

    if (DialogHandler.getSecurityProvider() == null)
      keyStore = java.security.KeyStore.getInstance("PKCS12");
    else
    {
      keyStore = java.security.KeyStore.getInstance("PKCS12", DialogHandler.getSecurityProvider());
    }

    keyStore.load(in, this.pin);

    String al = null;
    Enumeration<String> e = keyStore.aliases();

    while (e.hasMoreElements())

      if (keyStore.isKeyEntry(al = e.nextElement()))
        break;

    if (al == null)
      throw new NullPointerException("No private key found in keystore.");

    this.alias = al;
    cert = (X509Certificate)keyStore.getCertificate(alias);
    this.usePSSforRSAkey = usePSSforRSAkey;
  }

  /**
   * Sollte den Namen des Herstellers zurückgeben.
   *
   * @return Herstellername
   */
  public String getVendor()
  {
    return "BOS";
  }

  /**
   * Liefert die Versionsnummer
   *
   * @return Versionsnummer
   */
  public String getVersion()
  {
    return "1.5";
  }

  /**
   * Liefert den im {@link DialogHandler} gesetzten Default-Signaturalgorithmus.
   * Falls dieser Algorithmus nicht zu dem Privatschlüssel (RSA/ECDSA) oder dem
   * gesetzten usePSSforRSAkey-Flag passt, wird der Algorithmus entsprechend
   * angepasst.
   *
   * @return Signaturalgorithmus-Identifier
   * @see DialogHandler#getSignatureAlgorithm()
   */
  public String getAlgorithm()
  {
    String algo = DialogHandler.getSignatureAlgorithm();
    try
    {
      String synKeyType = keyStore.getKey(alias, this.pin).getAlgorithm();
      if (algo.endsWith("sha256") && synKeyType.equals("EC"))
        algo = Constants.SIGNATURE_ALGORITHM_ECDSA_SHA256;
      else if (algo.endsWith("sha512") && synKeyType.equals("EC"))
        algo = Constants.SIGNATURE_ALGORITHM_ECDSA_SHA512;
      else if (synKeyType.equals("RSA"))
      {
        if (this.usePSSforRSAkey)
        {
          // RSA-PKCS#1, PSS padding
          if (algo.contains("sha256"))
            algo = Constants.SIGNATURE_ALGORITHM_RSA_SHA256_PSS;
          else if (algo.contains("sha512"))
            algo = Constants.SIGNATURE_ALGORITHM_RSA_SHA512_PSS;
        }
        else
        {
          // RSA-PKCS#1, v1.5 padding
          if (algo.endsWith("sha256"))
            algo = Constants.SIGNATURE_ALGORITHM_RSA_SHA256;
          else if (algo.endsWith("sha512"))
            algo = Constants.SIGNATURE_ALGORITHM_RSA_SHA512;
        }
      }
    }
    catch (Exception e)
    {
      log.error("Problem getting private key type, return to default", e);
    }
    return algo;
  }

  /**
   * Liefert das Signaturzertifikat.
   *
   * @return das Signaturzertifikat
   */
  public X509Certificate getCertificate()
  {
    return cert;
  }

  /**
   * Signiert das übergebene Byte-Array
   *
   * @param data die zu signierenden Daten
   * @param algorithm Signaturalgorithmus
   * @return die Signatur
   * @throws SignatureException
   */
  public byte[] sign(byte[] data, String algorithm) throws SignatureException
  {
    try
    {
      java.security.Signature sigengine;
      if (log.isDebugEnabled())
        log.debug("Algorithm: " + algorithm);

      if (DialogHandler.getSecurityProvider() == null)
        sigengine = java.security.Signature.getInstance((String)Constants.JCA_JCE_MAP.get(algorithm));
      else
        sigengine = java.security.Signature.getInstance((String)Constants.JCA_JCE_MAP.get(algorithm),
                                                        DialogHandler.getSecurityProvider());

      sigengine.initSign(((java.security.PrivateKey)keyStore.getKey(alias, pin)));
      sigengine.update(data);

      if (log.isDebugEnabled())
        log.debug("Fertig mit dem Signieren");

      return sigengine.sign();
    }
    catch (Exception ex)
    {
      throw new SignatureException(DialogHandler.text.getString("signature_creation_error") + " - "
                                   + ex.getClass() + ": " + ex.getLocalizedMessage());
    }
  }
}
