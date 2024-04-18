package de.osci.osci12.extinterfaces.crypto;

import java.security.cert.X509Certificate;

import de.osci.osci12.common.Constants;
import de.osci.osci12.encryption.OSCICipherException;


/**
 * Diese abstrakte Klasse stellt die Schnittstelle der Bibliothek für
 * die Anbindung von Entschlüsselungs-Modulen (Crypto-Token) dar. Anwendungen, die
 * OSCI-Nachrichten entschlüsseln wollen, müssen an das entschlüsselnde Rollenobjekt
 * eine Implementation dieser Klasse übergeben.
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
 */
public abstract class Decrypter
{
  /**
   * Liefert die Versionsnummer
   * @return Versionsnummer
   */
  public abstract String getVersion();

  /**
   * Sollte den Namen des Herstellers zurückgeben.
   * @return Herstellername
   */
  public abstract String getVendor();

  /**
   * Die Implementierung dieser Methode muss das Verschlüsselungszertifikat zurückgeben.
   * @return Verschlüsselungszertifikat
   */
  public abstract X509Certificate getCertificate();

  /**
   * Die Implementierung dieser Methode muss das übergebene Byte-Array gemäß RSAES-PKCS1-v1_5 entschlüsseln.
   * @param data zu entschlüsselndes Byte-Array
   * @return entschlüsseltes Byte-Array
   * @throws OSCICipherException wenn beim Entschlüsseln ein Fehler auftritt
   * @throws de.osci.osci12.common.OSCICancelledException wenn der Vorgang vom Anwender abgebrochen wird
   */
  public abstract byte[] decrypt(byte[] data) throws OSCICipherException,
                                                     de.osci.osci12.common.OSCICancelledException;

  /**
   * Die Implementierung dieser Methode muss das übergebene Byte-Array gemäß RSAES-OAEP-ENCRYPT entschlüsseln
   * (P-Source Parameter werden nicht unterstützt).
   * Die Algorithmus-Identifier können der Klasse {@link Constants} entnommen werden.
   * <b>Diese Methode muss überschrieben werden.</b>
   * @param data zu entschlüsselndes Byte-Array
   * @param mgfAlgorithm Maskengenerierungsfunktion (OAEP), z.B. http://www.w3.org/2009/xmlenc11#mgf1sha256
   * @param digestAlgorithm Hashalgorithmus (OAEP), z.B. http://www.w3.org/2001/04/xmlenc#sha256
   * @return entschlüsseltes Byte-Array
   * @throws OSCICipherException wenn beim Entschlüsseln ein Fehler auftritt
   * @throws de.osci.osci12.common.OSCICancelledException wenn der Vorgang vom Anwender abgebrochen wird
   * @see Constants
   * @since 1.6
   */
  public abstract byte[] decrypt(byte[] data, String mgfAlgorithm, String digestAlgorithm) throws OSCICipherException,
                                                     de.osci.osci12.common.OSCICancelledException;
}
