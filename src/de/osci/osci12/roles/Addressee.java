package de.osci.osci12.roles;

import java.security.cert.X509Certificate;

import de.osci.osci12.extinterfaces.crypto.Decrypter;
import de.osci.osci12.extinterfaces.crypto.Signer;


/**
 *  Diese Klasse stellt einen OSCI-Empfaenger dar.
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
public class Addressee extends Role
{
  //  private static final String idName = "addressee";
  /**
   *  Konstruktor für den Empfang einer Nachricht.
   *
   *  @param  signer Signer-Objekt, welches die Signatur der Rückantwort erstellen
   *  soll (kann null sein, wenn keine Signatur gewünscht).
   *  @param  decrypter Decrypter-Objekt, welches den Inhalt der Nachricht entschlüsseln
   *  soll.
   */
  public Addressee(Signer signer, Decrypter decrypter)
  {
    super(signer, decrypter);
  }

  /**
   *  Konstruktor für das Versenden einer Nachricht.
   *
   *  @param  signatureCertificate Zertifikat, mit dem die Signatur der Rückantwort geprüft wird
   *  @param  cipherCertificate Zertifikat, mit dem die Nachricht verschlüsselt werden soll
   */
  public Addressee(X509Certificate signatureCertificate, X509Certificate cipherCertificate)
  {
    super(signatureCertificate, cipherCertificate);
  }
}
