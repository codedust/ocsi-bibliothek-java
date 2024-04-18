package de.osci.osci12.messageparts;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.osci.helper.ISO8601DateTimeFormat;
import de.osci.helper.ParserHelper;
import de.osci.helper.SymCipherOutputStream;
import de.osci.osci12.OSCIException;
import de.osci.osci12.common.Constants;
import de.osci.osci12.common.DialogHandler;
import de.osci.osci12.encryption.Crypto;
import de.osci.osci12.extinterfaces.crypto.Signer;
import de.osci.osci12.roles.Author;
import de.osci.osci12.roles.OSCIRoleException;
import de.osci.osci12.roles.Originator;
import de.osci.osci12.roles.Role;
import de.osci.osci12.signature.OSCISignatureException;


/**
 * <p>
 * Die ContentContainer-Klasse stellt einen OSCI-Inhaltsdatenscontainer dar. Ein ContentContainer kann einen
 * oder mehrere Content- oder EncryptedData-Objekte enthalten. Attachments werden als Contents eingestellt,
 * die eine Referenz auf das Attachment enthalten.
 * </p>
 * <p>
 * Ein Content-Container wird als eine Einheit signiert und / oder verschlüsselt.
 * </p>
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
public class ContentContainer extends MessagePart implements Serializable
{

  private static final long serialVersionUID = 4443521943513857170L;

  private static Log log = LogFactory.getLog(ContentContainer.class);

  static boolean STATE_OF_OBJECT_CONSTRUCTION = true;

  static boolean STATE_OF_OBJECT_PARSING = false;

  boolean stateOfObject = STATE_OF_OBJECT_CONSTRUCTION;

  protected static final int INNER_CONTAINER = 1;

  protected static final int SIGNED_CONTAINER = 2;

  protected static final int ENCRYPTED_CONTAINER = 3;

  // lfdNr für die ContentContainer
  private static int idNr = -1;

  // lfdNr für die signed signature properties Ids
  int signedSigPropNr = 0;

  // Vector für die Signer dieses ContentContainers
  Vector<OSCISignature> signerList = new Vector<OSCISignature>();

  // Vector für alle Roles des ContentContainer. Beinhalten auch die Rollen Objekte eventueller Encrypted Data
  // Childs
  Vector<Role> roles = new Vector<Role>();

  // Vector für die Attachments
  Hashtable<String, Attachment> attachments = new Hashtable<String, Attachment>();

  // Vector für die Contents
  private Vector<Content> contentList = new Vector<Content>();

  // Vector für die EncryptedData Objekte
  private Vector<EncryptedDataOSCI> encryptedDataList = new Vector<EncryptedDataOSCI>();

  /**
   * Legt ein ContentContainer-Objekt an.
   */
  public ContentContainer()
  {
    id = typ + (++idNr);
  }

  /**
   * Überprüft die Signatur zu dem übergebenen Role Objekt. Zur Installation von ggf. erforderlichen
   * Transformern s. checkAllSignatures(). Bevor eine Signaturprüfung an dem ContentContainer-Objekt
   * durchgeführt werden kann, müssen denjenigen Content-Objekten, die unter Anwendung von Transformationen
   * signiert wurden, die transformierten Daten übergeben werden. Welche Transformationen erforderlich sind,
   * kann (bei Content-Objekten mit Inhaltsdaten) mit Hilfe der Methode getTransformerForSignature() abgefragt
   * werden.
   *
   * @param signatureRole Rollen-Objekt mit dem Zertifikat zur Signatur
   * @return true, wenn die Prüfung positiv ausgefallen ist
   * @see #checkAllSignatures()
   * @see Content#setTransformedData(InputStream transformedData)
   * @see Content#getTransformerForSignature()
   * @throws OSCIRoleException undocumented
   * @throws OSCISignatureException undocumented
   */
  public boolean checkSignature(Role signatureRole) throws OSCISignatureException, OSCIRoleException
  {
    if (log.isDebugEnabled())
      log.debug("(start) checkSignature (...) ");

    boolean check;
    check = checkContainsSigner(signatureRole);

    if (check)
    {
      try
      {
        OSCISignature[] signatures = findSignatureObjects(signatureRole);

        for ( int j = 0 ; j < signatures.length ; j++ )
        {
          if (log.isDebugEnabled())
            log.debug("Signature Objekt: " + signatures[j]);

          if (signatures[j].getDigestMethods().containsValue(Constants.DIGEST_ALGORITHM_SHA1))
            log.info("SHA-1 used as digest algorithm for content signature.");

          if (signatures[j].signatureAlgorithm.equals(Constants.SIGNATURE_ALGORITHM_RSA_SHA1))
            log.info("SHA-1 with RSA used as signature algorithm for content signature.");

          Hashtable<String, MessagePart> newHashes = new Hashtable<String, MessagePart>();

          // id
          for ( int i = 0 ; i < contentList.size() ; i++ )
          {
            Content co = contentList.get(i);
            newHashes.put("#" + co.getRefID(), co);
          }

          for ( int i = 0 ; i < encryptedDataList.size() ; i++ )
          {
            EncryptedDataOSCI encData = encryptedDataList.get(i);
            newHashes.put("#" + encData.getRefID(), encData);
          }

          Enumeration<Attachment> e = attachments.elements();

          while (e.hasMoreElements())
          {
            Attachment att = e.nextElement();
            newHashes.put("cid:" + att.getRefID(), att);
          }

          Map<String, OSCISignatureReference> sigRefs = signatures[j].getReferences();

          if (log.isDebugEnabled())
          {
            for ( String refId : sigRefs.keySet() )
            {
              log.debug("Reference: " + refId);

            }
            log.debug("Anzahl contents und encData: " + newHashes.size() + " OSCISIGReferenzen: "
                      + sigRefs.size());
          }

          Vector<String> checked = new Vector<String>();
          MessagePart mp;

          byte[] digest, newDigest;
          int sigRefCount = sigRefs.size();
          if (signatures[j].signingTime != null)
          {
            sigRefCount--;
            log.info("Add signing time to count list");
          }

          if (sigRefCount != newHashes.size())
          {
            log.error("The number of references and hashed parts are not equal");
            return false;
          }
          // sind die Hashwerte gleich?
          for ( OSCISignatureReference signatureRef : sigRefs.values() )
          {
            String id = signatureRef.getRefID();

            if (log.isDebugEnabled())
              log.debug("ID die kontrolliert wird: " + id + ":" + signatures[j].signingPropsId);

            if (id.equals("#" + signatures[j].signingPropsId))
            {
              MessageDigest mdg;
              if (DialogHandler.getSecurityProvider() == null)
                mdg = MessageDigest.getInstance(Constants.JCA_JCE_MAP.get(signatureRef.getDigestMethodAlgorithm()));
              else
                mdg = MessageDigest.getInstance(Constants.JCA_JCE_MAP.get(signatureRef.getDigestMethodAlgorithm()),
                                                DialogHandler.getSecurityProvider());
              newDigest = mdg.digest(signatures[j].signingProperties.getBytes(Constants.CHAR_ENCODING));
            }
            else
            {
              mp = newHashes.get(id);
              newDigest = mp.getDigestValue(signatureRef.getDigestMethodAlgorithm());
            }

            digest = signatureRef.digestValue;

            if (newDigest == null)
            {
              if (log.isDebugEnabled())
                log.debug("Der aktuelle Digest für die RefID: " + id + " konnte nicht gefunden werden");

              return false;
            }

            if (!MessageDigest.isEqual(digest, newDigest))
            {
              log.error("Der Digest für die RefID: " + id + " ist falsch!");

              return false;
            }
            else if (log.isDebugEnabled())
              log.debug("Der Digest ist richtig.");

            checked.add(id);
          }

          String key;

          for ( Enumeration<String> en = newHashes.keys() ; en.hasMoreElements() ; )
          {
            key = en.nextElement();

            if (!checked.contains(key))
            {
              log.error("Unsigniertes Containerelement gefunden: " + id);

              return false;
            }
          }

          X509Certificate c = signatureRole.getSignatureCertificate();

          if ((c.getKeyUsage() != null) && !c.getKeyUsage()[0] && !c.getKeyUsage()[1])
          {
            log.error("Signature certificate has wrong key usage.");
            return false;
          }

          // nun noch das Signed Info testen
          java.security.Signature sg;

          if (DialogHandler.getSecurityProvider() == null)
            sg = java.security.Signature.getInstance(Constants.JCA_JCE_MAP.get(signatures[j].signatureAlgorithm));
          else
            sg = java.security.Signature.getInstance(Constants.JCA_JCE_MAP.get(signatures[j].signatureAlgorithm),
                                                     DialogHandler.getSecurityProvider());

          // sg.initVerify(signatureRole.getSignatureCertificate());
          sg.initVerify(c.getPublicKey());
          sg.update(signatures[j].getSignedInfoBytes());

          if (!sg.verify(signatures[j].signatureValue))
          {
            log.error("Signatur falsch !" + new String(signatures[j].getSignedInfoBytes()));

            return false;
          }
          else if (log.isDebugEnabled())
            log.debug("Die Signaturprüfung wurde erfolgreich abgeschlossen.");
        }
      }
      catch (IllegalStateException ex)
      {
        throw ex;
      }
      catch (Exception ex)
      {
        log.error("", ex);
        throw new OSCISignatureException("signature_check_error");
      }
    }
    else
    {
      log.warn("Content-Signatur konnte nicht überprüft werden (Falsches Role Objekt).");
      throw new OSCIRoleException("no_signature_for_role");
    }

    if (log.isDebugEnabled())
      log.debug("(ende) findSignatureObject");

    return true;
  }

  /**
   * Liefert ein Array mit den Signatur-Objekten. Diesen Objekten können die verwendeten Hash- und
   * Signaturalgorithmen entnommen werden.
   *
   * @return Signatur-Objekte
   * @see OSCISignature#signatureAlgorithm
   * @see OSCISignature#getDigestMethods()
   */
  public OSCISignature[] getSignatures()
  {
    return signerList.toArray(new OSCISignature[signerList.size()]);
  }

  /*
   * Liefert die zum Role-Objekt gehörigen XML-Signature Objekte.
   * @param roleToCheck Rollenobjekt für das die Signatur-Objekt übergeben werden soll
   * @return OSCISignature Objekt oder null sobald kein korospondierendes Objekt exitstiert
   * @throws OSCIRoleException
   */
  private OSCISignature[] findSignatureObjects(Role roleToCheck) throws OSCIRoleException
  {
    if (log.isDebugEnabled())
      log.debug("(start) findSignatureObject (...) ");

    Enumeration<OSCISignature> e = this.signerList.elements();
    Vector<OSCISignature> sigs = new Vector<OSCISignature>();

    if (log.isDebugEnabled())
      log.debug("Anzahl SignerList: " + signerList.size());

    while (e.hasMoreElements())
    {
      OSCISignature signature = e.nextElement();

      if (signature.signer.getSignatureCertificate().equals(roleToCheck.getSignatureCertificate()))
        sigs.add(signature);
    }

    return sigs.toArray(new OSCISignature[0]);
  }

  /**
   * Fügt dem Objekt ein XML-Signature Element hinzu (Wird vom Parser aufgerufen)
   *
   * @param signature OSCISignature Objekt
   */
  void addSignature(OSCISignature signature)
  {
    signerList.add(signature);
    roles.add(signature.signer);
  }

  /**
   * Überprüft ob das Role Objekt für die Signaturprüfung verwendet werden kann
   *
   * @param roleToCheck Role Objekt
   * @return true sobald das Zertifikat des Role Objektes zur Signaturprüfung benutzt werden kann
   * @throws OSCIRoleException
   */
  private boolean checkContainsSigner(Role roleToCheck) throws OSCIRoleException
  {
    if (log.isDebugEnabled())
      log.debug("(start) checkContainsRole (...) ");

    Role[] signer = this.getSigners();

    for ( int i = 0 ; i < signer.length ; i++ )
    {
      if (log.isDebugEnabled())
        log.debug("Role Object: " + signer[i].id);

      if (signer[i].getSignatureCertificate().equals(roleToCheck.getSignatureCertificate()))
        return true;
    }

    return false;
  }

  /**
   * Durchsucht Inhaltsdatensignaturen nach den verwendeten Algorithmen. Es wird true zurückgegeben, wenn
   * Referenzen der XML-Signatur oder die Signatur selbst mit Algorithmen erzeugt wurden, die zu dem
   * übergebenen Prüfzeitpunkt als unsicher eingestuft wurden. Wird als Prüfzeitpunkt null übergeben, wird
   * (unabhängig von Fristen) auf die Verwendung von Algorithmen geprüft, die zum Zeitpunkt der
   * Veröffentlichung dieser Implementierung als unsicher eingestuft wurden.
   *
   * @param date Prüfzeitpunkt
   * @param signer Signer
   * @return true, wenn unsichere Algorithmen zur Signatur verwendet wurden, andernfalls false
   * @throws IllegalStateException falls der ContentContainer nicht von dem übergebenen Rollenobjekt signiert
   *           ist
   * @throws OSCIRoleException wenn beim Zugriff auf das Signatur-Rollenobjekt (z.B. Zertifikat) ein Problem
   *           auftritt
   */
  public boolean hasWeakSignature(Role signer, Date date) throws OSCIRoleException
  {
    if (checkContainsSigner(signer) == false)
      throw new IllegalStateException("Message is not signed by given role object " + signer.id + ".");

    OSCISignature[] signatures = findSignatureObjects(signer);

    if (date == null)
      date = Constants.ACTUAL_DATE;

    if (Crypto.isWeak(date, signer.getSignatureCertificate()))
    {
      return true;
    }


    for ( int j = 0 ; j < signatures.length ; j++ )
    {
      if (Constants.OUT_DATES.containsKey(signatures[j].signatureAlgorithm)
          && !date.before(Constants.OUT_DATES.get(signatures[j].signatureAlgorithm)))
        return true;
      String[] digMeths = signatures[j].getDigestMethods().values().toArray(new String[0]);
      for ( int i = 0 ; i < digMeths.length ; i++ )
        if (Constants.OUT_DATES.containsKey(digMeths[i])
            && !date.before(Constants.OUT_DATES.get(digMeths[i])))
          return true;
    }
    return false;
  }


  /**
   * Überprüft alle Signaturen in dem ContentContainer. Die Hinweise zu transformierten Daten (s.
   * checkSignature(Role)) sind zu beachten.
   *
   * @return true, wenn alle Prüfungen positiv ausgefallen sind
   * @throws OSCIRoleException wenn z.B. erforderliche Zertifikate fehlen
   * @throws OSCISignatureException wenn bei der Prüfung der Signatur ein Fehler auftritt <b>oder der
   *           Inhaltsdatencontainer überhaupt nicht signiert wurde.</b>
   * @see #checkSignature(Role)
   */
  public boolean checkAllSignatures() throws OSCIRoleException, OSCISignatureException
  {
    Role[] signer = this.getSigners();

    if (signer.length == 0)
      throw new OSCISignatureException("no_signature");

    for ( int i = 0 ; i < signer.length ; i++ )
    {
      if (!checkSignature(signer[i]))
      {
        return false;
      }
    }

    return true;
  }

  /**
   * Liefert die eingestellten Attachment-Objekte des ContentContainer.
   *
   * @return Array der referenzierten Attachments
   */
  public Attachment[] getAttachments()
  {
    if (log.isDebugEnabled())
      log.debug("Anzahl der Attachments: " + attachments.size());

    Attachment[] atts = null;

    if (attachments.size() > 0)
    {
      atts = new Attachment[attachments.size()];

      Enumeration<Attachment> e = attachments.elements();
      int count = 0;

      while (e.hasMoreElements())
      {
        atts[count] = e.nextElement();
        count++;
      }
    }

    return atts;
  }

  /**
   * Liefert die eingestellten Rollen-Objekte des ContentContainer, welche für die Signatur sowie
   * untergeordnete Verschlüsselungen und Signaturen verwendet wurden.
   *
   * @return Array der Rollenobjekte
   */
  public Role[] getRoles()
  {
    return roles.toArray(new Role[]{});
  }

  /**
   * Diese Methode signiert mit dem angegebenen Rollen-Objekt den kompletten ContentContainer. Der
   * Signaturalgorithmus wird von der verwendeten {@link Signer}-Implementierung festgelegt. <b>Hinweis:</b>
   * Wenn der ContentContainer einen verschlüsselten ContentContainer (EncryptedDataOSCI) enthält, so werden
   * die (ebenfalls verschlüsselten) Attachments, die ggf. in diesem Container referenziert sind, nicht mit
   * signiert. Der Grund ist, dass bei einer Signaturprüfung Refenzen auf Attachments im verschlüsselten
   * ContentContainer nicht überprüft werden können.
   *
   * @param signer Role-Objekt mit dem Signer-Objekt
   * @param digestAlgorithm Hashalgorithmus, der für die Berechnung der Hashwerte im SignedInfo-Element
   *          verwendet werden soll.
   * @throws IOException bei Schreib-/Lesefehlern
   * @throws NoSuchAlgorithmException wenn für ein enthaltenes Content-, EncryptedData- oder Attachment-Objekt
   *           ein nicht unterstützter Hashalgorithmus gesetzt wurde oder wenn für das übergebene Role-Objekt
   *           ein nicht unterstützter Signieralgorithmus gesetzt wurde
   * @throws OSCIRoleException wenn für das übergebene Rollenobjekt kein Signer-Objekt gesetzt wurde oder
   *           diesem das erforderliche Signaturzertifikat fehlt
   * @throws SignatureException wenn beim Signaturvorgang ein Fehler aufgetreten ist
   * @see Signer#getAlgorithm()
   * @see ISO8601DateTimeFormat
   */
  public void sign(Role signer, String digestAlgorithm)
    throws OSCIException, NoSuchAlgorithmException, SignatureException, IOException
  {
    sign(signer, digestAlgorithm, null);
  }


  /**
   * Signiert den Container mit dem im {@link DialogHandler} festgelegten Standardhashalgorithmus (zur
   * Berechnung der Hashwerte der Nachrichtenbestandteile, die in das SignedInfo-Element eingetragen werden).
   * Der Signaturalgorithmus wird von der verwendeten Signer-Implementierung festgelegt.
   *
   * @param signer Role-Objekt mit dem Signer-Objekt
   * @throws IOException bei Schreib-/Lesefehlern
   * @throws NoSuchAlgorithmException wenn für ein enthaltenes Content-, EncryptedData- oder Attachment-Objekt
   *           ein nicht unterstützter Hashalgorithmus gesetzt wurde oder wenn für das übergebene Role-Objekt
   *           ein nicht unterstützter Signieralgorithmus gesetzt wurde
   * @throws OSCIRoleException wenn für das übergebene Rollenobjekt kein Signer-Objekt gesetzt wurde oder
   *           diesem das erforderliche Signaturzertifikat fehlt
   * @throws SignatureException wenn beim Signaturvorgang ein Fehler aufgetreten ist
   * @see #sign(Role, String)
   * @see DialogHandler#setDigestAlgorithm(String)
   * @see Signer#getAlgorithm()
   */
  public void sign(Role signer)
    throws OSCIException, NoSuchAlgorithmException, SignatureException, IOException
  {
    sign(signer, DialogHandler.getDigestAlgorithm(), null);
  }

  /**
   * Diese Methode signiert mit dem angegebenen Rollen-Objekt den kompletten ContentContainer. Der
   * Signaturalgorithmus wird von der verwendeten {@link Signer}-Implementierung festgelegt. Der
   * Signaturzeitpunkt kann im ISO-8601-Format übergeben werden. <b>Hinweis:</b> Wenn der ContentContainer
   * einen verschlüsselten ContentContainer (EncryptedDataOSCI) enthält, so werden die (ebenfalls
   * verschlüsselten) Attachments, die ggf. in diesem Container referenziert sind, nicht mit signiert. Der
   * Grund ist, dass bei einer Signaturprüfung Refenzen auf Attachments im verschlüsselten ContentContainer
   * nicht überprüft werden können.
   *
   * @param signer Role-Objekt mit dem Signer-Objekt
   * @param digestAlgorithm Hashalgorithmus, der für die Berechnung der Hashwerte im SignedInfo-Element
   *          verwendet werden soll.
   * @param time Signaturzeitpunkt im ISO 8601-Format
   * @throws IOException bei Schreib-/Lesefehlern
   * @throws NoSuchAlgorithmException wenn für ein enthaltenes Content-, EncryptedData- oder Attachment-Objekt
   *           ein nicht unterstützter Hashalgorithmus gesetzt wurde oder wenn für das übergebene Role-Objekt
   *           ein nicht unterstützter Signieralgorithmus gesetzt wurde
   * @throws OSCIRoleException wenn für das übergebene Rollenobjekt kein Signer-Objekt gesetzt wurde oder
   *           diesem das erforderliche Signaturzertifikat fehlt
   * @throws SignatureException wenn beim Signaturvorgang ein Fehler aufgetreten ist
   * @see Signer#getAlgorithm()
   * @see ISO8601DateTimeFormat
   * @deprecated Die Verwendung dieser Methode führt zur Inkompatibilität der erzeugten Nachrichten mit
   *             älteren Versionen der OSCI 1.2-Transportbibliothek. Sie sollte nur in Szenarien eingesetzt
   *             werden, in denen sichergestellt ist, dass alle beteiligten Kommunikationspartner aktuelle
   *             Implementierungen verwenden.
   * @since 1.4
   */
  @Deprecated
  public void sign(Role signer, String digestAlgorithm, String time)
    throws OSCIException, NoSuchAlgorithmException, SignatureException, IOException
  {
    if (!roles.contains(signer))
      roles.add(signer);

    if (!(signer instanceof Author) && !(signer instanceof Originator))
      throw new OSCIRoleException("wrong_role_sign_cont");

    OSCISignature sig = new OSCISignature();

    if (signerList.size() == 0)
    {
      if (log.isDebugEnabled())
        log.debug("Anzahl der Contents" + contentList.size());

      Content cnt;

      for ( int i = 0 ; i < contentList.size() ; i++ )
      {
        // Contents und EncryptedData müssen dieselben NS-Prefixes verwenden
        cnt = contentList.get(i);
        setNSPrefixes(cnt.soapNSPrefix, cnt.osciNSPrefix, cnt.dsNSPrefix, cnt.xencNSPrefix, cnt.xsiNSPrefix);
        addSignatureReference(sig, cnt, digestAlgorithm);
      }

      addAttachmentSigRefs(sig, this, digestAlgorithm);

      EncryptedDataOSCI enc;

      for ( int i = 0 ; i < encryptedDataList.size() ; i++ )
      {
        enc = encryptedDataList.get(i);
        setNSPrefixes(enc.soapNSPrefix, enc.osciNSPrefix, enc.dsNSPrefix, enc.xencNSPrefix, enc.xsiNSPrefix);
        addSignatureReference(sig, enc, digestAlgorithm);
      }
    }
    else
    {
      for ( OSCISignatureReference sigRef : signerList.get(0).getReferences().values() )
      {
        log.debug("RefId: " + sigRef.getRefID());
        // Hinzufügen der signature references nur sobald es nicht die Timestamp reference ist
        if (!sigRef.getRefID().startsWith("#" + getRefID() + "TS"))
        {
          sig.addSignatureReference(sigRef);
        }
      }
    }

    if (time != null)
      sig.addSignatureTime(time, this.id + "TS" + (signedSigPropNr++), digestAlgorithm);


    sig.sign(signer);
    signerList.add(sig);
  }

  /**
   * Fügt Attachment-Signaturreferenzen rekursiv hinzu.
   *
   * @param sig undocumented
   * @param coco undocumented
   * @throws OSCIException undocumented
   * @throws IOException undocumented
   * @throws NoSuchAlgorithmException undocumented
   */
  private void addAttachmentSigRefs(OSCISignature sig, ContentContainer coco, String digestAlgorithm)
    throws OSCIException, IOException, NoSuchAlgorithmException
  {
    Content cnt;

    for ( int i = 0 ; i < coco.contentList.size() ; i++ )
    {
      cnt = coco.contentList.get(i);

      if (cnt.getContentType() == Content.ATTACHMENT_REFERENCE)
      {
        // bereits vorhandene Referenzen auf gleiches Attachment suchen

        if (!sig.getReferences().containsKey("cid:" + cnt.getAttachment().getRefID()))
          addSignatureReference(sig, cnt.getAttachment(), digestAlgorithm);
      }
      else if (cnt.getContentType() == Content.CONTENT_CONTAINER)
        addAttachmentSigRefs(sig, cnt.getContentContainer(), digestAlgorithm);
    }
  }

  /**
   * @param sig undocumented
   * @param mp undocumented
   * @throws NoSuchAlgorithmException undocumented
   * @throws IOException undocumented
   * @throws OSCIRoleException undocumented
   */
  private void addSignatureReference(OSCISignature sig, MessagePart mp, String digestAlgorithm)
    throws NoSuchAlgorithmException, IOException, OSCIException
  {
    sig.addSignatureReference(new OSCISignatureReference(mp, digestAlgorithm));
  }

  /**
   * Liefert die Rollenobjekte, von denen die Signaturen angebracht wurden.
   *
   * @return Array der Rollenobjekte
   */
  public Role[] getSigners()
  {
    Role[] roles = new Role[signerList.size()];

    for ( int i = 0 ; i < roles.length ; i++ )
      roles[i] = signerList.get(i).signer;

    return roles;
  }

  private boolean listContainsRefid(String refId)
  {
    if (refId == null)
    {
      return false;
    }
    for ( Content content : contentList )
    {
      String refIdInList = content.getRefID();
      if (refId.equals(refIdInList))
      {
        return true;
      }
    }
    return false;
  }


  /**
   * Fügt dem ContentContainer ein Content-Objekt hinzu.
   *
   * @param content das hinzuzufügende Content-Objekt
   * @see Content
   */
  public void addContent(Content content)
  {
    addContentInternal(content, true);
  }


  /**
   * Interne Methode zum Hinzufügen von Contents.
   *
   * @param content das hinzuzufügende Content-Objekt
   * @param checkForDuplicateId true, wenn nach doppelten Ref-IDs geprüft werden soll
   */
  void addContentInternal(Content content, boolean checkForDuplicateId)
  {
    if ((signerList.size() > 0) && (stateOfObject == STATE_OF_OBJECT_CONSTRUCTION))
      throw new IllegalStateException(DialogHandler.text.getString("signature_violation"));

    boolean containsElement = contentList.contains(content);

    if (ParserHelper.isSecureContentDataCheck() && this.getRefID() != null
        && this.getRefID().equals(content.getRefID()))
    {
      throw new IllegalArgumentException("refId " + content.getRefID() + " equals ContentContainer ID "
                                         + this.getRefID());
    }

    if (!containsElement)
    {
      if (checkForDuplicateId && listContainsRefid(content.getRefID()))
      {
        throw new IllegalArgumentException("refId " + content.getRefID() + " is already in ContentContainer");
      }

      contentList.add(content);

      if (content.getAttachment() != null)
      {
        attachments.put(content.getAttachment().getRefID(), content.getAttachment());
      }

      if (content.getContentContainer() != null)
      {
        Role[] rls = content.getContentContainer().getRoles();

        for ( int i = 0 ; i < rls.length ; i++ )
          roles.add(rls[i]);

        Attachment[] atts = content.getContentContainer().getAttachments();

        if (atts != null)
        {
          for ( int i = 0 ; i < atts.length ; i++ )
            attachments.put(atts[i].getRefID(), atts[i]);
        }
      }
    }
  }

  /**
   * Liefert die im ContentContainer enthaltenen Content-Objekte.
   *
   * @return content Array der enthaltenen Content-Objekt
   * @see Content
   */
  public Content[] getContents()
  {
    return contentList.toArray(new Content[contentList.size()]);
  }

  /**
   * Entfernt ein Content-Objekt aus dem ContentContainer.
   *
   * @param content das zu entfernende Content-Objekt
   * @throws IllegalStateException wenn der ContentContainer signiert ist
   * @see Content
   */
  public void removeContent(Content content) throws java.lang.IllegalArgumentException
  {
    if ((signerList.size() > 0) && (stateOfObject == STATE_OF_OBJECT_CONSTRUCTION))
      throw new IllegalStateException(DialogHandler.text.getString("signature_violation"));

    if (log.isDebugEnabled())
      log.debug("start remove");

    boolean erg = contentList.remove(content);

    if (!erg)
      throw new java.lang.IllegalArgumentException();

    if (content.getAttachment() != null)
      attachments.remove(content.getAttachment().getRefID());
  }

  /**
   * Fügt dem ContentContainer ein EncryptedData-Objekt hinzu.
   *
   * @param encryptedDataElement das hinzuzufügende EncryptedData-Objekt
   * @see EncryptedDataOSCI
   */
  public void addEncryptedData(EncryptedDataOSCI encryptedDataElement)
  {
    if ((signerList.size() > 0) && (stateOfObject == STATE_OF_OBJECT_CONSTRUCTION))
      throw new IllegalStateException(DialogHandler.text.getString("signature_violation"));

    if (!encryptedDataList.contains(encryptedDataElement))
    {
      if (log.isDebugEnabled())
        log.debug("Encrypted-Data Element wird hinzugefügt.");

      encryptedDataList.add(encryptedDataElement);
    }

    Attachment[] atts = encryptedDataElement.getAttachments();

    for ( int i = 0 ; i < atts.length ; i++ )
    {
      if (!attachments.contains(atts[i]))
        attachments.put(atts[i].getRefID(), atts[i]);
    }

    Role[] rls = encryptedDataElement.getRoles();

    for ( int i = 0 ; i < rls.length ; i++ )
      roles.add(rls[i]);

    if (log.isDebugEnabled())
      log.debug("Anzahl der neuen Roles: " + rls.length);
  }

  /**
   * Entfernt ein EncryptedData-Objekt aus dem ContentContainer. Der zweite Parameter gibt an, ob bei
   * EncryptedData-Objekten, die ein verschlüsseltes Attachment referenzieren, dieses aus dem ContentContainer
   * entfernt wird. Dies ist von Bedeutung, wenn ein Attachment in mehreren EncryptedData-Objekten
   * referenziert wird.
   *
   * @param encryptedDataElement das zu entfernende EncryptedDataOSCI-Objekt
   * @param removeAttachment <b>true</b>, Attachments, welche im EncryptedData-Objekt referenziert sind,
   *          werden ebenfalls aus dem ContentContainer entfernt
   * @throws IllegalStateException wenn der ContentContainer signiert ist
   * @see EncryptedDataOSCI
   */
  public void removeEncryptedData(EncryptedDataOSCI encryptedDataElement, boolean removeAttachment)
    throws IllegalStateException
  {
    if ((signerList.size() > 0) && (stateOfObject == STATE_OF_OBJECT_CONSTRUCTION))
      throw new IllegalStateException(DialogHandler.text.getString("signature_violation"));

    encryptedDataList.remove(encryptedDataElement);

    if (removeAttachment)
    {
      for ( final Attachment attachment : encryptedDataElement.attachments )
      {
        attachments.remove(attachment.getRefID());
      }
    }
  }

  /**
   * Liefert die im ContentContainer enthaltenen verschlüsselten Daten als EncryptedData-Objekte.
   *
   * @return Array der enthaltenen EncryptedData-Objekte
   * @see EncryptedDataOSCI
   */
  public EncryptedDataOSCI[] getEncryptedData()
  {
    return encryptedDataList.toArray(new EncryptedDataOSCI[encryptedDataList.size()]);
  }

  /**
   * undocumented
   *
   * @param out undocumented
   * @throws IOException undocumented
   * @throws OSCIException undocumented
   */
  @Override
  protected void writeXML(OutputStream out) throws IOException, OSCIException
  {
    writeXML(out, true);
  }

  /**
   * Interne Methode, wird von Anwendungen normalerweise nicht aufgerufen.
   *
   * @param out Stream, in den geschrieben werden soll
   * @exception IOException im Fehlerfall
   */
  protected void writeXML(OutputStream out, boolean inner) throws IOException, OSCIException
  {
    int i;
    out.write(("<" + osciNSPrefix + ":ContentContainer").getBytes(Constants.CHAR_ENCODING));

    // Namespaces bei Verschlüsselung erhalten
    if ((out instanceof SymCipherOutputStream) || (!(out instanceof DigestOutputStream) && !inner))
      // if (out instanceof SymCipherOutputStream)
      out.write(ns);

    if ((getRefID() != null) && (getRefID().length() > 0))
      out.write((" Id=\"" + getRefID() + "\"").getBytes(Constants.CHAR_ENCODING));

    out.write(0x3e);

    if (signerList.size() > 0)
    {
      for ( i = 0 ; i < signerList.size() ; i++ )
      {
        signerList.get(i).writeXML(out);
      }
    }

    for ( i = 0 ; i < contentList.size() ; i++ )
    {
      contentList.get(i).writeXML(out, true);
    }

    for ( i = 0 ; i < encryptedDataList.size() ; i++ )
    {
      encryptedDataList.get(i).writeXML(out, inner);
    }

    out.write(("</" + osciNSPrefix + ":ContentContainer>").getBytes(Constants.CHAR_ENCODING));
  }

  /**
   * undocumented
   *
   * @return undocumented
   */
  @Override
  public String toString()
  {
    ByteArrayOutputStream out = new ByteArrayOutputStream();

    try
    {
      writeXML(out);
    }
    catch (Exception ex1)
    {
      log.error("Error", ex1);
    }

    try
    {
      return out.toString(Constants.CHAR_ENCODING);
    }
    catch (UnsupportedEncodingException ex)
    {
      // kann nicht vorkommen
      return "";
    }
  }
}
