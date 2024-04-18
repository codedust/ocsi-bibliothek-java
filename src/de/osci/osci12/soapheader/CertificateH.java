package de.osci.osci12.soapheader;

import java.io.IOException;
import java.io.OutputStream;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Hashtable;

import de.osci.helper.Base64OutputStream;
import de.osci.osci12.common.Constants;
import de.osci.osci12.common.Constants.LanguageTextEntries;
import de.osci.osci12.common.DialogHandler;
import de.osci.osci12.roles.Addressee;
import de.osci.osci12.roles.Author;
import de.osci.osci12.roles.Intermed;
import de.osci.osci12.roles.OSCIRoleException;
import de.osci.osci12.roles.Originator;
import de.osci.osci12.roles.Reader;
import de.osci.osci12.roles.Role;


/**
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
public abstract class CertificateH extends HeaderEntry
{
  //  private static Log log = LogFactory.getLog(CertificateH.class);
  protected Hashtable<String, X509Certificate> certificates = new Hashtable<String, X509Certificate>();
  protected byte[] tmpBuffer;

  /**
   * undocumented
   *
   * @param role undocumented
   * @param out undocumented
   *
   * @throws OSCIRoleException undocumented
   * @throws IOException undocumented
   * @throws IllegalArgumentException undocumented
   */
  protected void addCipherCertificate(Role role, OutputStream out)
                               throws OSCIRoleException,
                                      IOException
  {
    if (role == null)
      throw new IllegalArgumentException(DialogHandler.text.getString(LanguageTextEntries.invalid_firstargument.name()) + " null");

    String name = ""; // = role.getClass().getName().substring(role.getClass().getName().lastIndexOf('.') + 1);
    role.getCipherCertificate(); // Test, ob vorhanden

    if (role instanceof Intermed)
      name = "Intermediary";
    else if (role instanceof Originator)
      name = "Originator";
    else if (role instanceof Addressee)
      name = "Addressee";
    else if (role instanceof de.osci.osci12.roles.Reader)
      name = "OtherReader";
    else if (role instanceof Author)
      name = "OtherAuthor";
    else
      throw new IllegalArgumentException(DialogHandler.text.getString(LanguageTextEntries.invalid_firstargument.name()) + role);

    /*
       if (name.equals("Reader") || name.equals("Author"))
         name = "Other" + name;
       else if (name.equals("Intermed"))
         name = "Intermediary";
     */
    out.write(("<" + osciNSPrefix + ":CipherCertificate" + name + " Id=\"" + role.getCipherCertificateId() + "\"><" +
              dsNSPrefix + ":X509Data><" + dsNSPrefix + ":X509Certificate>").getBytes(Constants.CHAR_ENCODING));

    try
    {
      Base64OutputStream base64Out = new Base64OutputStream(out, true);
      base64Out.write(role.getCipherCertificate().getEncoded());
      base64Out.flush();
    }
    catch (CertificateEncodingException ex)
    {
      throw new IOException(de.osci.osci12.common.DialogHandler.text.getString("cert_gen_error") + " - " +
                            ex.getClass() + ": " + ex.getMessage());
    }

    out.write(("</" + dsNSPrefix + ":X509Certificate></" + dsNSPrefix + ":X509Data></" + osciNSPrefix +
              ":CipherCertificate" + name + ">").getBytes(Constants.CHAR_ENCODING));
    //    out.flush();
  }

  /**
   * undocumented
   *
   * @return undocumented
   */
  public Hashtable<String, X509Certificate> getCertificates()
  {
    return certificates;
  }

  /**
   * undocumented
   *
   * @param role undocumented
   * @param out undocumented
   *
   * @throws OSCIRoleException undocumented
   * @throws IOException undocumented
   */
  protected void addSignatureCertificate(Role role, OutputStream out)
                                  throws OSCIRoleException,
                                         IOException
  {
    String name = ""; // = role.getClass().getName().substring(role.getClass().getName().lastIndexOf('.') + 1);
    role.getSignatureCertificate(); // Test, ob vorhanden

    if (role instanceof Intermed)
      name = "Intermediary";
    else if (role instanceof Originator)
      name = "Originator";
    else if (role instanceof Addressee)
      name = "Addressee";
    else if (role instanceof Reader)
      name = "OtherReader";
    else if (role instanceof Author)
      name = "OtherAuthor";
    else
      throw new IllegalArgumentException(DialogHandler.text.getString(LanguageTextEntries.invalid_firstargument.name()) + role);

    /*
       if (name.equals("Reader") || name.equals("Author"))
         name = "Other" + name;
       else if (name.equals("Intermed"))
         name = "Intermediary";
     */
    out.write(("<" + osciNSPrefix + ":SignatureCertificate" + name + " Id=\"" + role.getSignatureCertificateId() +
              "\"><" + dsNSPrefix + ":X509Data><" + dsNSPrefix + ":X509Certificate>").getBytes(Constants.CHAR_ENCODING));

    try
    {
      Base64OutputStream base64Out = new Base64OutputStream(out, true);
      base64Out.write(role.getSignatureCertificate().getEncoded());
      base64Out.flush();
    }
    catch (CertificateEncodingException ex)
    {
      throw new IOException(de.osci.osci12.common.DialogHandler.text.getString("cert_gen_error") + " - " +
                            ex.getClass() + ": " + ex.getMessage());
    }

    out.write(("</" + dsNSPrefix + ":X509Certificate></" + dsNSPrefix + ":X509Data></" + osciNSPrefix +
              ":SignatureCertificate" + name + ">").getBytes(Constants.CHAR_ENCODING));
  }
}
