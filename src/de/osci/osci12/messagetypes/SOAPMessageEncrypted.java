package de.osci.osci12.messagetypes;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

import javax.crypto.SecretKey;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.osci.helper.Base64;
import de.osci.helper.Base64OutputStream;
import de.osci.helper.StoreOutputStream;
import de.osci.helper.SymCipherOutputStream;
import de.osci.osci12.OSCIException;
import de.osci.osci12.common.Constants;
import de.osci.osci12.common.DialogHandler;
import de.osci.osci12.encryption.Crypto;
import de.osci.osci12.encryption.EncryptedData;


/**
 * Diese Klasse entspricht einer verschlüsselten OSCI-Nachricht.
 * <p>
 * Copyright © 2023 Governikus GmbH &amp; Co. KG, Germany
 * </p>
 * <p>
 * Erstellt von Governikus GmbH &amp; Co. KG
 * </p>
 * <p>
 * Diese Bibliothek kann von jedermann nach Maßgabe der European Union Public Licence genutzt werden.
 * </p>
 * Die Lizenzbestimmungen können unter der URL <a href="https://eupl.eu/">https://eupl.eu/</a> abgerufen
 * werden.
 * </p>
 *
 * @author N. Büngener
 * @version 2.4.1
 */
class SOAPMessageEncrypted extends OSCIMessage
{

  private static Log log = LogFactory.getLog(SOAPMessageEncrypted.class);

  public EncryptedData encData;

  private OSCIMessage msg;

  private String symmetricCipherAlgorithm = null;

  private int ivLength = Constants.DEFAULT_GCM_IV_LENGTH;

  private SecretKey symKey;

  private byte[] encSymKey;

  private OutputStream storeStream;

  private static byte[] xml_0;

  private static byte[] xml_1a1;

  private static byte[] xml_1a2;

  private static byte[] xml_1b;

  private static byte[] xml_2;

  private static byte[] xml_3;

  private byte[] ivLengthElement;

  private byte[] algo;

  private byte[] asymAlgo;

  private int length;

  private X509Certificate cipherCert;

  static
  {
    try
    {
      xml_0 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n\r\n<soap:Envelope xmlns:ds=\"http://www.w3.org/2000/09/xmldsig#\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xenc=\"http://www.w3.org/2001/04/xmlenc#\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://schemas.xmlsoap.org/soap/envelope/ soapMessageEncrypted.xsd http://www.w3.org/2000/09/xmldsig# oscisig.xsd http://www.w3.org/2001/04/xmlenc# oscienc.xsd\"><soap:Body><xenc:EncryptedData MimeType=\"Multipart/Related\"><xenc:EncryptionMethod Algorithm=\"".getBytes(Constants.CHAR_ENCODING);
      xml_1a1 = "\">".getBytes(Constants.CHAR_ENCODING);
      xml_1a2 = "</xenc:EncryptionMethod><ds:KeyInfo><xenc:EncryptedKey><xenc:EncryptionMethod Algorithm=\"".getBytes(Constants.CHAR_ENCODING);
      xml_1b = "</xenc:EncryptionMethod><ds:KeyInfo><ds:X509Data><ds:X509Certificate>".getBytes(Constants.CHAR_ENCODING);
      xml_2 = "</ds:X509Certificate></ds:X509Data></ds:KeyInfo><xenc:CipherData><xenc:CipherValue>".getBytes(Constants.CHAR_ENCODING);
      xml_3 = "</xenc:CipherValue></xenc:CipherData></xenc:EncryptedKey></ds:KeyInfo><xenc:CipherData><xenc:CipherReference URI=\"cid:osci_enc\"><xenc:Transforms><ds:Transform Algorithm=\"http://www.w3.org/2000/09/xmldsig#base64\"></ds:Transform></xenc:Transforms></xenc:CipherReference></xenc:CipherData></xenc:EncryptedData></soap:Body></soap:Envelope>".getBytes(Constants.CHAR_ENCODING);
    }
    catch (UnsupportedEncodingException ex)
    {
      // Kann nicht auftreten, jede JAVA-Umgebung unterstützt UTF-8
    }
  }

  /**
   * Creates a new SOAPMessageEncrypted object.
   *
   * @param msg undocumented
   * @param storeStream undocumented
   * @throws NoSuchAlgorithmException undocumented
   */
  public SOAPMessageEncrypted(OSCIMessage msg, OutputStream storeStream) throws NoSuchAlgorithmException
  {
    messageType = SOAP_MESSAGE_ENCRYPTED;
    this.msg = msg;


    if (this.msg != null)
    {
      this.symmetricCipherAlgorithm = msg.dialogHandler.getSymmetricCipherAlgorithm();
      this.ivLength = msg.dialogHandler.getIvLength();
      symKey = Crypto.createSymKey(symmetricCipherAlgorithm);
    }
    this.storeStream = storeStream;
  }


  /**
   * Creates a new SOAPMessageEncrypted object.
   *
   * @param msg undocumented
   * @param storeStream undocumented
   * @param symmetricCipherAlgorithm undocumented
   * @throws NoSuchAlgorithmException undocumented
   */
  public SOAPMessageEncrypted(OSCIMessage msg, OutputStream storeStream, String symmetricCipherAlgorithm)
    throws NoSuchAlgorithmException
  {
    this(msg, storeStream, symmetricCipherAlgorithm, Constants.DEFAULT_GCM_IV_LENGTH);
  }

  /**
   * Creates a new SOAPMessageEncrypted object.
   *
   * @param msg undocumented
   * @param storeStream undocumented
   * @param symmetricCipherAlgorithm undocumented
   * @param ivLength undocumented
   * @throws NoSuchAlgorithmException undocumented
   */
  public SOAPMessageEncrypted(OSCIMessage msg,
                              OutputStream storeStream,
                              String symmetricCipherAlgorithm,
                              int ivLength)
    throws NoSuchAlgorithmException
  {
    messageType = SOAP_MESSAGE_ENCRYPTED;
    this.msg = msg;

    this.symmetricCipherAlgorithm = symmetricCipherAlgorithm;
    this.ivLength = ivLength;

    if (this.msg != null)
      symKey = Crypto.createSymKey(symmetricCipherAlgorithm);

    this.storeStream = storeStream;
  }

  /**
   * undocumented
   *
   * @return undocumented
   * @throws IOException undocumented
   * @throws OSCIException undocumented
   * @throws NoSuchAlgorithmException undocumented
   */
  @Override
  protected long calcLength() throws IOException, OSCIException, NoSuchAlgorithmException
  {
    if ((stateOfMsg & STATE_COMPOSED) == 0)
      compose();

    long len = 288;
    len += length;
    len += Integer.toString(length).getBytes(Constants.CHAR_ENCODING).length;
    len += (4 * msg.boundary_string.getBytes(Constants.CHAR_ENCODING).length);
    len += contentID.getBytes(Constants.CHAR_ENCODING).length;

    try
    {
      len += Base64.calcB64Length(cipherCert.getEncoded().length);
    }
    catch (CertificateEncodingException ex)
    {
      throw new IOException(DialogHandler.text.getString("cert_gen_error") + ": " + ex.getClass() + ": "
                            + ex.getMessage());
    }

    if (msg.base64)
    {
      len += 6;
      len += Base64.calcB64Length(Crypto.calcSymEncLength(msg.calcLength(),
                                                          symmetricCipherAlgorithm,
                                                          ivLength));
    }
    else
    {
      len += 21;
      len += Crypto.calcSymEncLength(msg.calcLength(), symmetricCipherAlgorithm, ivLength);
    }

    log.debug("Laenge der Nachricht: " + len);

    return len;
  }

  /**
   * undocumented
   *
   * @throws OSCIException undocumented
   * @throws NoSuchAlgorithmException undocumented
   * @throws IOException undocumented
   */
  @Override
  protected void compose() throws OSCIException, NoSuchAlgorithmException, IOException
  {
    if ((msg.stateOfMsg & STATE_COMPOSED) == 0)
      msg.compose();

    if (msg instanceof OSCIRequest)
      cipherCert = msg.getDialogHandler().getSupplier().getCipherCertificate();
    else
      cipherCert = msg.getDialogHandler().getClient().getCipherCertificate();

    encSymKey = Base64.encode(Crypto.doRSAEncryption(cipherCert,
                                                     symKey,
                                                     msg.getDialogHandler().getAsymmetricCipherAlgorithm()))
                      .getBytes(Constants.CHAR_ENCODING);
    algo = symmetricCipherAlgorithm.getBytes(Constants.CHAR_ENCODING);
    asymAlgo = constructEncryptionAlgo();
    length = xml_0.length + algo.length + xml_1a1.length + xml_1a2.length + asymAlgo.length + xml_1b.length
             + xml_2.length + encSymKey.length + xml_3.length;

    // nur einsetzen, wenn ungleich altem Default-Wert (16), um Abwärtskompatibilität zu wahren
    if (ivLength != 16)
    {
      ivLengthElement = ("<" + Constants.Namespaces.OSCI128.getPrefix() + ":IvLength xmlns:"
                         + Constants.Namespaces.OSCI128.getPrefix() + "=\""
                         + Constants.Namespaces.OSCI128.getUri() + "\" Value=\"" + ivLength + "\"></"
                         + Constants.Namespaces.OSCI128.getPrefix()
                         + ":IvLength>").getBytes(Constants.CHAR_ENCODING);
      length += ivLengthElement.length;
    }

    stateOfMsg |= STATE_COMPOSED;
  }

  private byte[] constructEncryptionAlgo() throws UnsupportedEncodingException
  {
    String ret;
    if (Constants.ASYMMETRIC_CIPHER_ALGORITHM_RSA_OAEP.equals(msg.getDialogHandler()
                                                                 .getAsymmetricCipherAlgorithm()))
    {
      if (Constants.DIGEST_ALGORITHM_SHA512.equals(DialogHandler.getDigestAlgorithm())
          || Constants.DIGEST_ALGORITHM_SHA3_384.equals(DialogHandler.getDigestAlgorithm())
          || Constants.DIGEST_ALGORITHM_SHA3_512.equals(DialogHandler.getDigestAlgorithm()))
      {
        ret = Constants.ASYMMETRIC_CIPHER_ALGORITHM_RSA_OAEP
              + "\"><xenc11:MGF xmlns:xenc11=\"http://www.w3.org/2009/xmlenc11#\" Algorithm=\""
              + Constants.MASK_GENERATION_FUNCTION_1_SHA512 + "\"></xenc11:MGF>";
        ret += "<ds:DigestMethod Algorithm=\"" + Constants.DIGEST_ALGORITHM_SHA512 + "\"></ds:DigestMethod>";
      }
      else if (Constants.DIGEST_ALGORITHM_SHA256.equals(DialogHandler.getDigestAlgorithm())
               || Constants.DIGEST_ALGORITHM_SHA3_256.equals(DialogHandler.getDigestAlgorithm()))
      {
        ret = Constants.ASYMMETRIC_CIPHER_ALGORITHM_RSA_OAEP
              + "\"><xenc11:MGF xmlns:xenc11=\"http://www.w3.org/2009/xmlenc11#\" Algorithm=\""
              + Constants.MASK_GENERATION_FUNCTION_1_SHA256 + "\"></xenc11:MGF>";
        ret += "<ds:DigestMethod Algorithm=\"" + Constants.DIGEST_ALGORITHM_SHA256 + "\"></ds:DigestMethod>";
      }
      else // default
      {
        ret = Constants.ASYMMETRIC_CIPHER_ALGORITHM_RSA_OAEP
              + "\"><xenc11:MGF xmlns:xenc11=\"http://www.w3.org/2009/xmlenc11#\" Algorithm=\""
              + Constants.MASK_GENERATION_FUNCTION_1_SHA256 + "\"></xenc11:MGF>";
        ret += "<ds:DigestMethod Algorithm=\"" + Constants.DIGEST_ALGORITHM_SHA256 + "\"></ds:DigestMethod>";
      }
    }
    else
    {
      ret = Constants.ASYMMETRIC_CIPHER_ALGORITHM_RSA_1_5 + "\">";
    }

    return ret.getBytes(Constants.CHAR_ENCODING);

  }

  /**
   * undocumented
   *
   * @param out undocumented
   * @throws IOException undocumented
   * @throws OSCIException undocumented
   * @throws NoSuchAlgorithmException undocumented
   * @throws IOException undocumented
   */
  @Override
  protected void writeXML(OutputStream out) throws IOException, OSCIException, NoSuchAlgorithmException
  {
    if ((stateOfMsg & STATE_COMPOSED) == 0)
      compose();

    out.write(("MIME-Version: 1.0\r\nContent-Type: Multipart/Related; boundary=" + msg.boundary_string
               + "; type=text/xml\r\n").getBytes(Constants.CHAR_ENCODING));
    out.write(("\r\n--" + msg.boundary_string
               + "\r\nContent-Type: text/xml; charset=UTF-8\r\n").getBytes(Constants.CHAR_ENCODING));
    out.write(("Content-Transfer-Encoding: 8bit\r\nContent-ID: <" + contentID
               + ">\r\n").getBytes(Constants.CHAR_ENCODING));
    out.write(("Content-Length: " + length + "\r\n\r\n").getBytes(Constants.CHAR_ENCODING));
    out.write(xml_0);
    out.write(algo);
    out.write(xml_1a1);
    if (ivLengthElement != null)
    {
      out.write(ivLengthElement);
    }
    out.write(xml_1a2);
    out.write(asymAlgo);
    out.write(xml_1b);

    try
    {
      out.write(Base64.encode(cipherCert.getEncoded()).getBytes(Constants.CHAR_ENCODING));
    }
    catch (CertificateEncodingException ex)
    {
      throw new IOException(DialogHandler.text.getString("cert_gen_error") + ": " + ex.getClass() + ": "
                            + ex.getMessage());
    }

    out.write(xml_2);
    out.write(encSymKey);
    out.write(xml_3);
    out.write(("\r\n\r\n--" + msg.boundary_string + "\r\nContent-Type: ").getBytes(Constants.CHAR_ENCODING));
    out.write((msg.base64 ? "text/base64" : "application/octet-stream").getBytes(Constants.CHAR_ENCODING));
    out.write(("\r\nContent-Transfer-Encoding: ").getBytes(Constants.CHAR_ENCODING));
    out.write((msg.base64 ? "7bit" : "binary").getBytes(Constants.CHAR_ENCODING));
    out.write(("\r\nContent-ID: <osci_enc>\r\n\r\n").getBytes(Constants.CHAR_ENCODING));

    Base64OutputStream b64out = null;
    SymCipherOutputStream tdesOut;

    if (msg.base64)
    {
      b64out = new Base64OutputStream(out, false);
      tdesOut = new SymCipherOutputStream(b64out, symKey, symmetricCipherAlgorithm, ivLength, true);
    }
    else
      tdesOut = new SymCipherOutputStream(out, symKey, symmetricCipherAlgorithm, ivLength, true);

    if (storeStream == null)
    {
      msg.writeXML(tdesOut);
      tdesOut.close();
    }
    else
    {
      StoreOutputStream sos = new StoreOutputStream(tdesOut, storeStream);
      msg.writeXML(sos);
      sos.close();
    }

    if (msg.base64)
      b64out.flush(true);

    out.write(("\r\n--" + msg.boundary_string + "--\r\n").getBytes(Constants.CHAR_ENCODING));
  }
  /*
   * public static void main(String[] args) { try { StoreDelivery sd = TestStoreDelivery.createTest();
   * SOAPMessageEncrypted sme = new SOAPMessageEncrypted(sd); System.out.println(sme.toString());
   * FileOutputStream fos = new
   * FileOutputStream("/"+sme.getClass().getName().substring(sd.getClass().getName().lastIndexOf('.')+1)+
   * ".xml"); sme.writeXML(fos); fos.close(); System.exit(0); } catch (Exception ex) { ex.getMessage();
   * ex.getLocalizedMessage(); ex.printStackTrace(); } }
   */
}
