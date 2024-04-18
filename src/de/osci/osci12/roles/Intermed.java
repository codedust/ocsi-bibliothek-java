package de.osci.osci12.roles;

import java.security.cert.X509Certificate;

import de.osci.osci12.extinterfaces.crypto.Decrypter;
import de.osci.osci12.extinterfaces.crypto.Signer;


/**
 *  Diese Klasse stellt einen OSCI-Intermediär dar.
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
public class Intermed extends Role
{
  //    private static final String idName = "intermed";
  private java.net.URI uri = null;

  /**
   * Konstruktor für ein Intermediärs-Objekt (intermediärsseitig).
   *
   * @param signer Signer-Objekt, welches die Signaturen der Nachrichten
   *        erstellen soll (null, wenn keine Signaturen gewünscht).
   * @param decrypter Decrypter-Objekt, welches den Inhalt der Nachrichten
   *        entschlüsseln soll.
   */
  public Intermed(Signer signer, Decrypter decrypter)
  {
    super(signer, decrypter);
  }

  /**
   * Konstruktor für ein Intermediärs-Objekt (clientseitig).
   * Das Signaturzertifikat des Intermediärs muss nicht übergeben werden.
   * Die verwendete URL muss durch die Transportimplementierung aufgelöst werden können.
   *
   * @param signatureCertificate Zertifikat, mit dem die Signatur einer Antwort
   * geprüft werden kann
   * @param cipherCertificate Zertifikat, mit dem die Nachricht verschlüsselt
   *        werden soll
   * @param uri URL des Intermediärs.
   */
  public Intermed(X509Certificate signatureCertificate, X509Certificate cipherCertificate, java.net.URI uri)
  {
    super(signatureCertificate, cipherCertificate);
    this.uri = uri;
  }

  /**
   * Liefert die URI des Intermediärs.
   *
   * @return URI
   */
  public java.net.URI getUri()
  {
    return uri;
  }

  /**
   * Setzt die URI des Intermediärs.
   * @param uri die URI
   */
  public void setUri(java.net.URI uri)
  {
    this.uri = uri;
  }
}
