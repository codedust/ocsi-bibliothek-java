package de.osci.osci12.extinterfaces.crypto;

import java.security.SignatureException;
import java.security.cert.X509Certificate;

import de.osci.osci12.common.Constants;
import de.osci.osci12.common.DialogHandler;


/**
 * Diese abstrakte Klasse stellt die Schnittstelle der Bibliothek für
 * die Anbindung von Signier-Modulen (Crypto-Token) dar. Anwendungen, die
 * OSCI-Nachrichten signieren wollen, müssen an das signierende Rollenobjekt
 * eine Implementation dieser Klasse übergeben.
 * <p>Copyright © 2023 Governikus GmbH &amp; Co. KG, Germany</p>
 * <p>Erstellt von Governikus GmbH &amp; Co. KG</p>
 * <p>Diese Bibliothek kann von jedermann nach Maßgabe der European Union
 *  Public Licence genutzt werden.</p><p>Die Lizenzbestimmungen können
 * unter der URL <a href="https://eupl.eu/">https://eupl.eu/</a> abgerufen werden.
 *
 *
 * @author R. Lindemann, N.Büngener
 * @version 2.4.1
 */
public abstract class Signer
{
  /**
   * Liefert die Versionsnummer.
   * @return Versionsnummer
   */
  public abstract String getVersion();

  /**
   * Sollte den Namen des Herstellers zurückgeben.
   * @return Herstellername
   */
  public abstract String getVendor();

  /**
   * Die Implementierung dieser Methode muss das Signaturzertifikat zurückgeben.
   * @return Signaturzertifikat
   */
  public abstract X509Certificate getCertificate();

  /**
   * Diese Methode sollte den Signaturalgorithmus als XML-Identifier zurückgeben,
   * den die Implementierung bei der Erzeugung einer Signatur verwendet.
   * Gibt diese Methode null zurück, wird der im DialogHandler gesetzte
   * Default-Algorithmus verwendet.
   * @return Signaturalgorithmus
   * @see Constants#SIGNATURE_ALGORITHM_RSA_SHA256
   * @see Constants#SIGNATURE_ALGORITHM_RSA_SHA512
   * @see Constants#SIGNATURE_ALGORITHM_RSA_RIPEMD160
   * @see Constants#SIGNATURE_ALGORITHM_ECDSA_SHA256
   * @see Constants#SIGNATURE_ALGORITHM_ECDSA_SHA512
   * @see DialogHandler#setSignatureAlgorithm(String)
   */
  public abstract String getAlgorithm();

  /**
   * Die Implementierung dieser Methode muss das übergebene Byte-Array signieren.
   * Der algorithm-Parameter hat keine weitere Bedeutung mehr, da ab Version 1.3 die
   * Signer-Implemetierung über die Methode getAlgorithm() den Algorithmus festlegt.
   * Die OSCI-Bibliothek fragt vor dem Aufruf von sign(byte[], String) den Signaturalgorithmus
   * ab und übergibt den String wieder an die sign-Methode.
   * @param hash zu signierendes Byte-Array
   * @param algorithm Signaturalgorithmus
   * @return Signatur-Wert
   * @throws SignatureException wenn beim Erstellen der Signatur ein Fehler auftritt
   * @throws de.osci.osci12.common.OSCICancelledException wenn der Vorgang vom Anwender abgebrochen wird
   */
  public abstract byte[] sign(byte[] hash, String algorithm)
                       throws SignatureException,
                              de.osci.osci12.common.OSCICancelledException;
}
