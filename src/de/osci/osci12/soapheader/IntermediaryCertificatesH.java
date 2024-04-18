package de.osci.osci12.soapheader;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.osci.osci12.common.Constants;
import de.osci.osci12.roles.Intermed;
import de.osci.osci12.roles.OSCIRoleException;


/**
 * <p>Copyright © 2023 Governikus GmbH &amp; Co. KG, Germany</p>
 * <p>Erstellt von Governikus GmbH &amp; Co. KG</p>
 * <p>Diese Bibliothek kann von jedermann nach Maßgabe der European Union
 *  Public Licence genutzt werden.</p><p>Die Lizenzbestimmungen können
 * unter der URL <a href="https://eupl.eu/">https://eupl.eu/</a> abgerufen werden.
 *
 *
 * @author N. Büngener
 * @version 2.4.1
 */
public class IntermediaryCertificatesH extends CertificateH
{
  private static Log log = LogFactory.getLog(IntermediaryCertificatesH.class);
  private Intermed signaturCertificateIntermediary;
  private Intermed cipherCertificateIntermediary;

  /**
   * undocumented
   *
   * @param cipherCertificateIntermediary undocumented
   *
   * @throws OSCIRoleException undocumented
   */
  public void setCipherCertificateIntermediary(Intermed cipherCertificateIntermediary)
                                        throws OSCIRoleException
  {
    if (log.isDebugEnabled())
      log.debug("CipherCertificate Intermed wurde gesetzt");

    cipherCertificateIntermediary.getCipherCertificate();
    this.cipherCertificateIntermediary = cipherCertificateIntermediary;
  }

  /**
   * undocumented
   *
   * @param signaturCertificateIntermediary undocumented
   *
   * @throws OSCIRoleException undocumented
   */
  public void setSignatureCertificateIntermediary(Intermed signaturCertificateIntermediary)
                                           throws OSCIRoleException
  {
    signaturCertificateIntermediary.getSignatureCertificate();
    this.signaturCertificateIntermediary = signaturCertificateIntermediary;
  }

  /**
   * undocumented
   *
   * @param out undocumented
   *
   * @throws IOException undocumented
   * @throws OSCIRoleException undocumented
   */
  public void writeXML(OutputStream out) throws IOException,
                                                OSCIRoleException
  {
    out.write(("<" + osciNSPrefix + ":IntermediaryCertificates").getBytes(Constants.CHAR_ENCODING));
    out.write(ns);
    out.write((" Id=\"intermediarycertificates\" " + soapNSPrefix +
              ":actor=\"http://www.w3.org/2001/12/soap-envelope/actor/none\" " + soapNSPrefix +
              ":mustUnderstand=\"1\">").getBytes(Constants.CHAR_ENCODING));

    if (cipherCertificateIntermediary != null)
      addCipherCertificate(cipherCertificateIntermediary, out);

    if (signaturCertificateIntermediary != null)
      addSignatureCertificate(signaturCertificateIntermediary, out);

    out.write(("</" + osciNSPrefix + ":IntermediaryCertificates>").getBytes(Constants.CHAR_ENCODING));
  }

  /**
   * undocumented
   *
   * @return undocumented
   */
  public Intermed getCipherCertificateIntermediary()
  {
    return cipherCertificateIntermediary;
  }

  /**
   * undocumented
   *
   * @return undocumented
   */
  public Intermed getSignatureCertificateIntermediary()
  {
    return signaturCertificateIntermediary;
  }
}
