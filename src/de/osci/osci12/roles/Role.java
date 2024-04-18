package de.osci.osci12.roles;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.osci.osci12.common.Constants;
import de.osci.osci12.common.DialogHandler;
import de.osci.osci12.encryption.Crypto;
import de.osci.osci12.extinterfaces.crypto.Decrypter;
import de.osci.osci12.extinterfaces.crypto.Signer;


/**
 * Diese Klasse ist die Superklasse aller OSCI-Rollenobjekte.
 * <p>
 * Copyright © 2023 Governikus GmbH &amp; Co. KG, Germany
 * </p>
 * <p>Erstellt von Governikus GmbH &amp; Co. KG</p>
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
public abstract class Role
{

  private static Log log = LogFactory.getLog(Role.class);

  private static MessageDigest md;

  protected Signer signer = null;

  protected Decrypter decrypter = null;

  protected X509Certificate signatureCertificate = null;

  protected X509Certificate cipherCertificate = null;

  /** Interne Id, sollte von Anwendungen nicht gesetzt werden. */
  public String id;

  /** Referenz-Ids, sollten von Anwendungen nicht gesetzt werden. */
  public String cipherRefId;

  /** Referenz-Ids, sollten von Anwendungen nicht gesetzt werden. */
  public String signatureRefId;

  private String cipherHash;

  private String signatureHash;

  // Signaturalgoritmus
  protected String signatureAlgorithm;

  /**
   * Creates a new Role object.
   */
  protected Role()
  {
    id = getClass().getName().substring(getClass().getName().lastIndexOf('.') + 1).toLowerCase().replace('$',
                                                                                                         '_');
  }

  /**
   * Creates a new Role object.
   */
  protected Role(Signer signer, Decrypter decrypter)
  {
    this();
    setSigner(signer);
    setDecrypter(decrypter);
  }

  /**
   * Creates a new Role object.
   */
  protected Role(X509Certificate signatureCertificate, X509Certificate cipherCertificate)
  {
    this();

    if (signatureCertificate != null)
      setSignatureCertificate(signatureCertificate);

    setCipherCertificate(cipherCertificate);
  }

  /**
   * Liefert die Id, mit der in der OSCI-Nachricht das Verschlüsselungszertifikat referenziert wird. Die Id
   * ist ein String, der aus dem Rollennamen, bei Author- und Reader-Objekten einer lfd. Nummer, der
   * Funktionbezeichnung (cipher) und dem SHA-1-Hashwert über den Bytes des Zertifikats besteht.
   *
   * @return Id des CipherCertificate-Elementes
   * @see #getSignatureCertificateId()
   */
  public String getCipherCertificateId()
  {
    try
    {
      if (!hasCipherCertificate())
        return id;

      if (cipherRefId != null)
        return cipherRefId;
      else
      {
        if (cipherHash == null)
          cipherHash = hashCertificate(getCipherCertificate());

        return (id + "_cipher_" + cipherHash);
      }
    }
    catch (Exception ex) // OSCIRole-, NoSuchAlgorithm-, CertificateEncodingException// oder
                         // UnsupportedEncodingException
    {
      return id;
    }
  }

  /**
   * Liefert die Id, mit der in der OSCI-Nachricht das Signaturzertifikat referenziert wird. Die Id ist ein
   * String, der aus dem Rollennamen, bei Author- und Reader-Objekten einer lfd. Nummer, der
   * Funktionbezeichnung (signature) und dem SHA-1-Hashwert über den Bytes des Zertifikats besteht.
   *
   * @return Id des SignatureCertificate-Elementes
   * @see #getCipherCertificateId()
   */
  public String getSignatureCertificateId()
  {
    try
    {
      if (!hasSignatureCertificate())
        return id;

      if (signatureRefId != null)
        return signatureRefId;
      else
      {
        if (signatureHash == null)
          signatureHash = hashCertificate(getSignatureCertificate());

        return (id + "_signature_" + signatureHash);
      }
    }
    catch (Exception ex) // OSCIRole-, NoSuchAlgorithm-, CertificateEncodingException// oder
                         // UnsupportedEncodingException
    {
      return id;
    }
  }

  /**
   * Helfermethode für die Berechnung des Hashwertes von Zertfifkaten. Wird vom Anwender normalerweise nicht
   * benötigt.
   *
   * @param cert zu hashendes Zertifkat
   * @return hexadezimaler Hashwert
   * @throws NoSuchAlgorithmException wenn Hashalgorithmus nicht verfügbar
   * @throws CertificateEncodingException bei Codierungsfehlern
   */
  private static synchronized String hashCertificate(X509Certificate cert)
  {
    if (cert == null)
      return "no_hash_available";

    try
    {
      if (md == null)
      {
        if (DialogHandler.getSecurityProvider() == null)
          md = MessageDigest.getInstance(Constants.JCA_JCE_MAP.get(Constants.DIGEST_ALGORITHM_SHA256));
        else
          md = MessageDigest.getInstance(Constants.JCA_JCE_MAP.get(Constants.DIGEST_ALGORITHM_SHA256),
                                         DialogHandler.getSecurityProvider());
      }

      md.reset();

      return Crypto.toHex(md.digest(cert.getEncoded()));
    }
    catch (Exception ex)
    {
      return "no_hash_available";
    }
  }

  /**
   * Liefert das eingestellte Signer-Objekt oder null, wenn kein Objekt an den Konstruktor übergeben wurde.
   *
   * @return Signer-Objekt
   * @throws OSCIRoleException wenn kein Signer gesetzt wurde
   */
  public Signer getSigner() throws OSCIRoleException
  {
    if (signer == null)
      throw new OSCIRoleException("no_signer");

    return signer;
  }

  /**
   * Stellt fest, ob ein Signatur-Privatschlüssel (Signer) verfügbar ist.
   *
   * @return true Signer wurde gesetzt
   */
  public boolean hasSignaturePrivateKey()
  {
    return (signer != null);
  }

  /**
   * Liefert das eingestellte Decrypter-Objekt oder null, wenn kein Objekt an den Konstruktor übergeben wurde.
   *
   * @return Decrypter-Objekt
   * @throws OSCIRoleException wenn kein Decrypter gesetzt wurde
   */
  public Decrypter getDecrypter() throws OSCIRoleException
  {
    if (decrypter == null)
      throw new OSCIRoleException("no_decrypter");

    return decrypter;
  }

  /**
   * Stellt fest, ob ein Verschlüsselungs-Privatschlüssel (Decrypter) verfügbar ist.
   *
   * @return true Decrypter wurde gesetzt
   */
  public boolean hasCipherPrivateKey()
  {
    return (decrypter != null);
  }

  /**
   * Setzt das zu verwendende Signer-Objekt. Falls bereits ein Signaturzertifikat gesetzt wurde, wird dieses
   * nach Aufruf dieser Methode ignoriert. Alle weiteren Aufrufe von getSignatureCertificate() liefern dann
   * das Zertifkat des Signer-Objektes zurück. setSigner(null) löscht das Signer-Objekt.
   *
   * @param signer das Signer-Objekt
   * @see #getSigner()
   */
  public void setSigner(Signer signer)
  {
    this.signer = signer;
    signatureHash = null;
  }

  /**
   * Setzt das zu verwendende Decrypter-Objekt. Falls bereits ein Signaturzertifikat gesetzt wurde, wird
   * dieses nach Aufruf dieser Methode ignoriert. Alle weiteren Aufrufe von getCipherCertificate() liefern
   * dann das Zertifkat des Decrypter-Objektes zurück. setDecrypter(null) löscht das Decrypter-Objekt.
   *
   * @param decrypter das Decrypter-Objekt
   * @see #getDecrypter()
   */
  public void setDecrypter(Decrypter decrypter)
  {
    this.decrypter = decrypter;
    cipherHash = null;
  }

  /**
   * Setzt das Signaturzertifikat des Rollenobjektes.
   *
   * @param signatureCertificate das Signaturzertifikat
   */
  public void setSignatureCertificate(X509Certificate signatureCertificate)
  {
    this.signatureCertificate = signatureCertificate;
    signatureHash = hashCertificate(signatureCertificate);
  }

  /**
   * Liefert das eingestellte Signaturzertifikat.
   *
   * @return des Signaturzertifikat
   * @throws OSCIRoleException wenn kein Signaturzertifikat eingestellt ist
   */
  public X509Certificate getSignatureCertificate() throws OSCIRoleException
  {
    if ((signer == null) && (signatureCertificate == null))
      throw new OSCIRoleException("no_signature_cert", id);
    else if (signer == null)
      return signatureCertificate;
    else

      return signer.getCertificate();
  }

  /**
   * Stellt fest, ob ein Signaturzertifikat (bzw. Signer) verfügbar ist.
   *
   * @return true Signaturzertifikat wurde gesetzt
   */
  public boolean hasSignatureCertificate()
  {
    return ((signer != null) || (signatureCertificate != null));
  }

  /**
   * Setzt das Verschlüsselungszertifikat des Rollenobjektes. Sollte nur in Ausnahmefällen von Anwendungen
   * aufgreufen werden, wenn z.B. kein Decrypter zur Verfügung steht.
   *
   * @param cipherCertificate das Signaturzertifikat
   */
  public void setCipherCertificate(X509Certificate cipherCertificate)
  {
    this.cipherCertificate = cipherCertificate;
    cipherHash = hashCertificate(cipherCertificate);
  }

  /**
   * Liefert das eingestellte Verschlüsselungszertifikat.
   *
   * @return des Verschlüsselungszertifikat
   * @throws OSCIRoleException wenn kein Verschlüsselungszertifikat eingestellt ist
   */
  public X509Certificate getCipherCertificate() throws OSCIRoleException
  {
    if ((decrypter == null) && (cipherCertificate == null))
      throw new OSCIRoleException("no_cipher_cert", id);
    else if (decrypter == null)
      return cipherCertificate;
    else
    {
      return decrypter.getCertificate();
    }
  }

  /**
   * Stellt fest, ob ein Verschlüsselungszertifikat (bzw. Decrypter) verfügbar ist.
   *
   * @return true Verschlüsselungszertifikat wurde gesetzt
   */
  public boolean hasCipherCertificate()
  {
    return ((decrypter != null) || (cipherCertificate != null));
  }

  /**
   * Liefert (ab Version 1.3) den Signaturalgorithmus, der von der Methode {@link Signer#getAlgorithm()}
   * zurückgeliefert wird. Liefert diese Methode null oder ist sie nicht implementiert, wird der im
   * {@link DialogHandler} eingestellte Default-Signaturalgorithmus zurückgegeben.
   *
   * @return den Identifier des Algorithmus
   * @throws IllegalStateException wenn kein Signer gesetzt ist
   * @see DialogHandler#getSignatureAlgorithm()
   * @see Signer#getAlgorithm()
   */
  public String getSignatureAlgorithm()
  {
    if (!hasSignaturePrivateKey())
      throw new IllegalStateException("Kein Signer-Objekt gesetzt.");

    try
    {
      signatureAlgorithm = signer.getAlgorithm();
      if (signatureAlgorithm == null)
        signatureAlgorithm = DialogHandler.getSignatureAlgorithm();
    }
    catch (AbstractMethodError ame)
    {
      log.warn("No implementaion of Signer.getAlgorithm() found in Role '" + id
               + "', probably old Signer implementaion (< 1.3) in use. Defaulting to "
               + DialogHandler.getSignatureAlgorithm());
      signatureAlgorithm = DialogHandler.getSignatureAlgorithm();
    }

    return signatureAlgorithm;
  }
}
