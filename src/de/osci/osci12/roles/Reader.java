package de.osci.osci12.roles;

import java.security.cert.X509Certificate;

import de.osci.osci12.extinterfaces.crypto.Decrypter;


/**
 *  Diese Klasse stellt einen OSCI-Leser dar.
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
public class Reader extends Role
{
  //  private static Log log = LogFactory.getLog(Reader.class);
  private static int idNr = -1;

  /**
   *  Konstruktor für den Empfang einer Nachricht. Wird als Parameter
   *  null übergeben, weil keine Verschlüsselung der Rückantwort gewünscht wird,
   *  muss trotzdem mit setCipherCertificate(X509Certificate) ein
   *  Verschlüsselungszertifikat gesetzt werden, weil dies Voraussetzung für
   *  die Teilnahme an OSCI ist.
   *
   *  @param  decrypter Decrypter-Objekt, welches den Inhalt der Nachricht entschlüsseln
   *  soll
   */
  public Reader(Decrypter decrypter)
  {
    super(null, decrypter);
    idNr++;
    id += idNr;
  }

  /**
   *  Konstruktor für das Versenden einer Nachricht.
   *
   *  @param  cipherCertificate Zertifikat, mit dem die Nachricht verschlüsselt werden soll
   */
  public Reader(X509Certificate cipherCertificate)
  {
    super(null, cipherCertificate);
    idNr++;
    id += idNr;
  }

  //    public String getId() {return id + " " + idNr;}
  //    public String getId() {return String.valueOf(idNr);}
  public void setSignatureCertificate(X509Certificate signatureCertificate)
  {
    throw new UnsupportedOperationException();
  }
}
