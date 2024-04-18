package de.osci.osci12.roles;

import java.security.cert.X509Certificate;

import de.osci.osci12.extinterfaces.crypto.Decrypter;
import de.osci.osci12.extinterfaces.crypto.Signer;


/**
 *  Diese Klasse stellt einen OSCI-Autor dar.
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
public class Author extends Role
{
  private static int idNr = -1;

  /**
   *  Konstruktor für den Versand einer Nachricht. Wird als zweiter Parameter
   *  null übergeben, weil keine Verschlüsselung der Rückantwort gewünscht wird,
   *  muss trotzdem mit setCipherCertificate(X509Certificate) ein
   *  Verschlüsselungszertifikat gesetzt werden, weil dies Voraussetzung für
   *  die Teilnahme an OSCI ist.
   *
   *  @param  signer Signer-Objekt, welches die Signatur der Nachricht erstellen
   *  soll (kann null sein, wenn keine Signatur gewünscht)
   *  @param  decrypter Decrypter-Objekt, welches den Inhalt der Rückantwort entschlüsseln
   *  soll (null, wenn die Nachricht nicht verschlüsselt wird)
   */
  public Author(Signer signer, Decrypter decrypter)
  {
    super(signer, decrypter);
    idNr++;
    id += idNr;
  }

  /**
   *  Konstruktor für den Empfang einer Nachricht.
   *
   *  @param  signatureCertificate Zertifikat, mit dem die Signatur der Nachricht geprüft wird
   *  @param  cipherCertificate Zertifikat, mit dem die Rückantwort verschlüsselt werden soll
   */
  public Author(X509Certificate signatureCertificate, X509Certificate cipherCertificate)
  {
    super(signatureCertificate, cipherCertificate);
    idNr++;
    id += idNr;
  }
}
