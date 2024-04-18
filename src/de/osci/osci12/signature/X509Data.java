package de.osci.osci12.signature;

import java.io.IOException;
import java.io.OutputStream;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

import de.osci.helper.Base64OutputStream;
import de.osci.osci12.common.Constants;


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
 *
 */
public class X509Data
{
  //  private static Log log = LogFactory.getLog(X509Data.class);
  private X509Certificate x509Cert = null;
  private String x509SKI = null;
  private String x509SubjectName = null;
  private String x509CRL = null;
  private IssuerSerial issuerSerial = null;
  private int dataState = -1;
  private static final int STATE_ISSUER_SERIAL = 0;
  private static final int STATE_SKI = 1;
  private static final int STATE_SUBJECT_NAME = 2;
  private static final int STATE_CERTIFICATE = 3;
  private static final int STATE_CRL = 4;

  /**
   * Creates a new X509Data object.
   *
   * @param cert undocumented
   */
  public X509Data(X509Certificate cert)
  {
    this.x509Cert = cert;
  }

  /**
   * undocumented
   *
   * @return undocumented
   */
  public X509Certificate getX509Certificate()
  {
    return x509Cert;
  }

  /**
   * undocumented
   *
   * @return undocumented
   */
  public String getX509CRL()
  {
    return x509CRL;
  }

  /**
   * undocumented
   *
   * @return undocumented
   */
  public String getX509SKI()
  {
    return x509SKI;
  }

  /**
   * undocumented
   *
   * @return undocumented
   */
  public String getX509SubjectName()
  {
    return x509SubjectName;
  }

  /**
   * undocumented
   *
   * @param x509SubjectName undocumented
   *
   * @throws Exception undocumented
   * @throws IllegalStateException undocumented
   */
  public void setX509SubjectName(String x509SubjectName)
                          throws Exception
  {
    if (dataState > -1)
      throw new IllegalStateException();
    else
      dataState = STATE_SUBJECT_NAME;

    this.x509SubjectName = x509SubjectName;
  }

  /**
   * undocumented
   *
   * @param x509CRL undocumented
   */
  public void setX509CRL(String x509CRL)
  {
    this.x509CRL = x509CRL;
  }

  /**
   * undocumented
   *
   * @param x509SKI undocumented
   *
   * @throws Exception undocumented
   * @throws IllegalStateException undocumented
   */
  public void setX509SKI(String x509SKI) throws Exception
  {
    if (dataState > -1)
      throw new IllegalStateException();
    else
      dataState = STATE_SKI;

    this.x509SKI = x509SKI;
  }

  /**
   * undocumented
   *
   * @param issuerName undocumented
   *
   * @throws Exception undocumented
   * @throws IllegalStateException undocumented
   */
  public void setIssuerName(String issuerName) throws Exception
  {
    if ((dataState > -1) && (dataState != STATE_ISSUER_SERIAL))
      throw new IllegalStateException();
    else
      dataState = STATE_ISSUER_SERIAL;

    if (issuerSerial == null)
      issuerSerial = new IssuerSerial();

    issuerSerial.x509IssuerName = issuerName;
  }

  /**
   * undocumented
   *
   * @param serialNumber undocumented
   *
   * @throws Exception undocumented
   * @throws IllegalStateException undocumented
   */
  public void setSerialNumber(String serialNumber) throws Exception
  {
    if ((dataState > -1) && (dataState != STATE_ISSUER_SERIAL))
      throw new IllegalStateException();
    else
      dataState = STATE_ISSUER_SERIAL;

    if (issuerSerial == null)
      issuerSerial = new IssuerSerial();

    issuerSerial.x509SerialNumber = serialNumber;
  }

  public class IssuerSerial
  {
    String x509IssuerName = null;
    String x509SerialNumber = null;

    IssuerSerial()
    {
    }

    public String getX509IssuerName()
    {
      return x509IssuerName;
    }

    public String getX509SerialNumber()
    {
      return x509SerialNumber;
    }
  }

  /**
   * undocumented
   *
   * @param out undocumented
   *
   * @throws IOException undocumented
   */
  public void writeXML(OutputStream out, String ds) throws IOException
  {
    out.write(("<" + ds + ":X509Data>").getBytes(Constants.CHAR_ENCODING));

    if (dataState == STATE_CERTIFICATE)
    {
      out.write(("<" + ds + ":X509Certificate>").getBytes(Constants.CHAR_ENCODING));

      Base64OutputStream myOut = new Base64OutputStream(out, true);

      try
      {
        myOut.write(x509Cert.getEncoded());
      }
      catch (CertificateEncodingException ex)
      {
        throw new IOException(de.osci.osci12.common.DialogHandler.text.getString("cert_gen_error") + " - " +
                              ex.getClass() + ": " + ex.getMessage());
      }

      myOut.flush();
      //               myOut.close();
      out.write(("</" + ds + ":X509Certificate>").getBytes(Constants.CHAR_ENCODING));
    }
    else if (dataState == STATE_CRL)
    {
      out.write(("<" + ds + ":X509CRL>" + x509CRL + "</" + ds + ":X509CRL>").getBytes(Constants.CHAR_ENCODING));
    }
    else if (dataState == STATE_ISSUER_SERIAL)
    {
      out.write(("<" + ds + ":X509IssuerSerial><" + ds + ":X509IssuerName>" + issuerSerial.getX509IssuerName() + "</" +
                ds + ":X509IssuerName><" + ds + ":X509SerisalNumber>" + issuerSerial.getX509SerialNumber() + "</" + ds +
                ":X509SerisalNumber></" + ds + ":X509IssuerSerial>").getBytes(Constants.CHAR_ENCODING));
    }
    else if (dataState == STATE_SKI)
    {
      out.write(("<" + ds + ":X509SKI>" + x509SKI + "</" + ds + ":X509SKI>").getBytes(Constants.CHAR_ENCODING));
    }
    else if (dataState == STATE_SUBJECT_NAME)
    {
      out.write(("<" + ds + ":X509SubjectName>" + x509SubjectName + "</" + ds + ":X509SubjectName>").getBytes(Constants.CHAR_ENCODING));
    }

    out.write(("</" + ds + ":X509Data>").getBytes(Constants.CHAR_ENCODING));
  }
}
