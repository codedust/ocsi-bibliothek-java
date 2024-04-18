package de.osci.osci12.messageparts;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.osci.osci12.common.Constants;
import de.osci.osci12.common.DialogHandler;
import de.osci.osci12.common.OSCIErrorException;
import de.osci.osci12.common.OSCIExceptionCodes.OSCIErrorCodes;
import de.osci.osci12.roles.OSCIRoleException;
import de.osci.osci12.roles.Role;


/**
 * Diese Klasse bildet die Grundlage für XML-Signaturen der Bibliothek. Sie wird von Anwendungen nie direkt
 * benötigt.
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
public class OSCISignature extends MessagePart
{

  private static Log log = LogFactory.getLog(OSCISignature.class);

  private Map<String, OSCISignatureReference> refs = new HashMap<>();

  public byte[] signatureValue;

  // public byte[] signedInfoDigestValue;
  public byte[] signedInfo;

  String enclosingElement;

  public String signerId;

  String signingTime;

  String signingPropsId;

  String signingProperties;

  public String signatureAlgorithm = DialogHandler.getSignatureAlgorithm();

  de.osci.osci12.roles.Role signer = null;

  Hashtable<String, String> refsDigestMethods;

  Hashtable<String, byte[]> refsHash;

  OSCISignature()
  {}

  OSCISignature(String enclosingElement)
  {
    this.enclosingElement = enclosingElement;
  }

  void addSignatureTime(String time, String id, String digAlgo)
    throws IOException, NoSuchAlgorithmException, OSCIErrorException
  {

    if (signingPropsId != null && refs.containsKey(id))
    {
      refs.remove(id + signingPropsId);
    }
    signingPropsId = id;
    signingTime = time;
    String nsHead = "xmlns:ds=\"http://www.w3.org/2000/09/xmldsig#\" xmlns:osci=\"http://www.osci.de/2002/04/osci\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xades=\"http://uri.etsi.org/01903/v1.3.2#\" xmlns:xenc=\"http://www.w3.org/2001/04/xmlenc#\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"";
    signingProperties = "<xades:SignedProperties " + nsHead + " Id=\"" + signingPropsId
                        + "\"><xades:SignedSignatureProperties><xades:SigningTime>" + signingTime
                        + "</xades:SigningTime></xades:SignedSignatureProperties></xades:SignedProperties>";

    OSCISignatureReference osr = new OSCISignatureReference();

    MessageDigest mdg;
    if (DialogHandler.getSecurityProvider() == null)
      mdg = MessageDigest.getInstance(Constants.JCA_JCE_MAP.get(digAlgo));
    else
      mdg = MessageDigest.getInstance(Constants.JCA_JCE_MAP.get(digAlgo),
                                      DialogHandler.getSecurityProvider());

    osr.digestValue = mdg.digest(signingProperties.getBytes(Constants.CHAR_ENCODING));
    osr.digestMethodAlgorithm = digAlgo;
    osr.setNSPrefixes(soapNSPrefix, osciNSPrefix, dsNSPrefix, xencNSPrefix, xsiNSPrefix);
    osr.id = "#" + id;
    addSignatureReference(osr);
  }

  // OSCISignatureReference[] getReferences()
  // {
  // return refs.values().toArray(new OSCISignatureReference[refs.size()]);
  // }

  public Map<String, OSCISignatureReference> getReferences()
  {
    return refs;
  }


  public void setReferences(Map<String, OSCISignatureReference> refList)
  {
    refs=refList;
  }
  public void addSignatureReference(OSCISignatureReference sigReference) throws OSCIErrorException
  {
    if (refs.containsKey(sigReference.getRefID()))
    {
      log.error("Duplicated refId of signed parts!: " + sigReference.getRefID());
      throw new OSCIErrorException(OSCIErrorCodes.NotAllRelevantPartsSigned);
    }
    log.debug("Add reference with id:" + sigReference.getRefID());

    refs.put(sigReference.getRefID(), sigReference);
  }

  /**
   * Liefert das zu dieser Signatur gehörige Signer-Objekt.
   *
   * @return Signer-Rollenobjekt oder null, wenn (noch) keines gesetzt wurde.
   */
  public Role getSigner()
  {
    return signer;
  }

  /**
   * Liefert den zu dieser Signatur gehörende Signaturzeitpunkt (ISO-8601-DateTime Format).
   *
   * @return Signaturzeitpunkt
   */
  public String getSigningTime()
  {
    return signingTime;
  }

  /**
   * undocumented
   *
   * @return undocumented
   */
  public Hashtable<String, byte[]> getDigests()
  {
    if (refsHash == null)
    {
      refsHash = new Hashtable<String, byte[]>();

      for ( Entry<String, OSCISignatureReference> sigRefs : refs.entrySet() )
      {
        refsHash.put(sigRefs.getKey(), sigRefs.getValue().getDigestValue());
      }
    }

    return refsHash;
  }

  /**
   * undocumented
   *
   * @return undocumented
   */
  public Hashtable<String, String> getDigestMethods()
  {
    if (refsDigestMethods == null)
    {
      refsDigestMethods = new Hashtable<String, String>();
      for ( Entry<String, OSCISignatureReference> sigRefs : refs.entrySet() )
      {
        refsDigestMethods.put(sigRefs.getKey(), sigRefs.getValue().digestMethodAlgorithm);
      }
    }

    return refsDigestMethods;
  }

  /**
   * undocumented
   *
   * @param signer undocumented
   * @throws OSCIRoleException undocumented
   * @throws java.security.SignatureException undocumented
   * @throws de.osci.osci12.common.OSCICancelledException undocumented
   * @throws IOException undocumented
   */
  public void sign(Role signer) throws OSCIRoleException, java.security.SignatureException,
    de.osci.osci12.common.OSCICancelledException, IOException
  {
    this.signer = signer;
    this.signatureAlgorithm = signer.getSignatureAlgorithm();

    if (signedInfo == null)
      createSignedInfo();

    if (log.isDebugEnabled())
      log.debug("Algo: " + signatureAlgorithm);

    signatureValue = signer.getSigner().sign(signedInfo, signatureAlgorithm);
    signerId = "#" + signer.getSignatureCertificateId();
  }

  /**
   * undocumented
   *
   * @return undocumented
   */
  public byte[] getSignedInfoBytes()
  {
    return signedInfo;
  }

  private void createSignedInfo() throws IOException
  {
    // Das Element SignInfo aufbauen
    ByteArrayOutputStream out = new ByteArrayOutputStream();

    // out.write(("<" + dsNSPrefix + ":SignedInfo").getBytes(Constants.CHAR_ENCODING));
    // out.write(ns);
    // xsi fehlt
    out.write(("<" + dsNSPrefix + ":SignedInfo xmlns:" + dsNSPrefix
               + "=\"http://www.w3.org/2000/09/xmldsig#\" xmlns:" + osciNSPrefix
               + "=\"http://www.osci.de/2002/04/osci\" xmlns:" + soapNSPrefix
               + "=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:" + xencNSPrefix
               + "=\"http://www.w3.org/2001/04/xmlenc#\" xmlns:" + xsiNSPrefix
               + "=\"http://www.w3.org/2001/XMLSchema-instance\">\n").getBytes(Constants.CHAR_ENCODING));
    // out.write(("<" + dsNSPrefix + ":SignedInfo>\n").getBytes(Constants.CHAR_ENCODING));
    out.write(("<" + dsNSPrefix
               + ":CanonicalizationMethod Algorithm=\"http://www.w3.org/TR/2001/REC-xml-c14n-20010315\"></"
               + dsNSPrefix + ":CanonicalizationMethod>\n<" + dsNSPrefix + ":SignatureMethod Algorithm=\""
               + signatureAlgorithm + "\"></" + dsNSPrefix
               + ":SignatureMethod>\n").getBytes(Constants.CHAR_ENCODING));

    for ( Entry<String, OSCISignatureReference> sigRefs : refs.entrySet() )
    {
      if (log.isDebugEnabled())
        log.debug("################ Referenz: " + sigRefs.getKey());
      sigRefs.getValue().writeXML(out);
    }

    out.write(("</" + dsNSPrefix + ":SignedInfo>").getBytes(Constants.CHAR_ENCODING));
    signedInfo = out.toByteArray();
  }

  /**
   * undocumented
   *
   * @param out undocumented
   * @throws IOException undocumented
   */
  public void writeXML(OutputStream out) throws IOException
  {
    if (enclosingElement != null)
      out.write(enclosingElement.getBytes(Constants.CHAR_ENCODING));

    String prf = dsNSPrefix + ":";
    int start = 0;
    int stop;
    for ( start = 0 ; start < signedInfo.length ; start++ )
      if (signedInfo[start] == '<')
        break;
    byte[] prfBytes = prf.getBytes(Constants.CHAR_ENCODING);
    int j = 0;
    for ( int i = start + 1 ; i < prf.length() ; i++ , j++ )
    {
      if (prfBytes[j] != signedInfo[i])
      {
        prf = "";
        break;
      }
    }

    if (prf.length() == 0)
      out.write(("<Signature " + "xmlns=\"" + MessagePartParser.DS_XMLNS
                 + "\">").getBytes(Constants.CHAR_ENCODING));
    else
      out.write(("<" + prf + "Signature>").getBytes(Constants.CHAR_ENCODING));

    if (log.isDebugEnabled())
      log.debug("+######################## signed info" + new String(signedInfo, Constants.CHAR_ENCODING));

    for ( start = 0 ; start < signedInfo.length ; start++ )
      if (signedInfo[start] == 0x20)
        break;

    for ( stop = start ; stop < signedInfo.length ; stop++ )
      if (signedInfo[stop] == '>')
        break;

    out.write(signedInfo, 0, start);
    out.write(signedInfo, stop, signedInfo.length - stop);
    out.write(("<" + prf + "SignatureValue>").getBytes(Constants.CHAR_ENCODING));
    out.write(de.osci.helper.Base64.encode(this.signatureValue).getBytes(Constants.CHAR_ENCODING));
    // id
    out.write(("</" + prf + "SignatureValue><" + prf + "KeyInfo><" + prf
               + "RetrievalMethod URI=\"").getBytes(Constants.CHAR_ENCODING));
    out.write(signerId.getBytes(Constants.CHAR_ENCODING));
    out.write(("\"></" + prf + "RetrievalMethod></" + prf + "KeyInfo>").getBytes(Constants.CHAR_ENCODING));
    if (signingProperties != null)
    {
      out.write(("<" + dsNSPrefix + ":Object><xades:QualifyingProperties xmlns:xades=\""
                 + MessagePartParser.XADES_XMLNS + "\">").getBytes(Constants.CHAR_ENCODING));
      int nsStart = signingProperties.indexOf("<xades:SignedProperties");
      nsStart = signingProperties.indexOf(' ', nsStart);
      out.write(signingProperties.substring(0, nsStart).getBytes(Constants.CHAR_ENCODING));
      out.write(signingProperties.substring(signingProperties.indexOf(" Id=", nsStart))
                                 .getBytes(Constants.CHAR_ENCODING));
      out.write(("</xades:QualifyingProperties></" + dsNSPrefix
                 + ":Object>").getBytes(Constants.CHAR_ENCODING));
    }
    out.write(("</" + prf + "Signature>").getBytes(Constants.CHAR_ENCODING));

    if (enclosingElement != null)
      out.write(("</" + enclosingElement.substring(1, enclosingElement.indexOf(' '))
                 + ">").getBytes(Constants.CHAR_ENCODING));
  }
}
