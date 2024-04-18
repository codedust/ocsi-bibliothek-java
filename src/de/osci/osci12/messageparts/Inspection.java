package de.osci.osci12.messageparts;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.security.cert.X509Certificate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.osci.osci12.common.Constants;


/**
 * <p>Diese Klasse repräsentiert das OSCI-Inspection-Element. Hier werden
 * Informationen für die ausgewerteten Zertifikate der OSCI-Nachricht gehalten. </p>
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
public class Inspection extends MessagePart implements Serializable
{
  private static final long serialVersionUID = 3258130267062415451L;
  private static Log log = LogFactory.getLog(Inspection.class);
  public static final String CERT_TYPE_ADVANCED = "advanced";
  public static final String CERT_TYPE_QUALIFIED = "qualified";
  public static final String CERT_TYPE_ACCREDITED = "accredited";
  public static final String CERT_TYPE_UNKNOWN = "unknown";
  private Timestamp timeStamp;
  private String mathResult;
  private String offlineResult;
  private boolean onlineResult;
  protected boolean isOnlineChecked = false;
  private String certType;
  private String[] onlineCheckName;
  private String[] onlineCheck;
  private String x509IssuerName = null;
  private String x509SerialNumber = null;
  private String x509SubjectName = null;

  Inspection()
  {
  }

  /**
   * Dieser Konstruktur wird nur bei Offlineprüfung benutzt.
   * @param cert Certifikat für das diese Prüfergebnisse gelten
   * @param certType Art des Zertifikates Advanced, qualified oder unkonwn
   * @param timeStamp Timestamp Element zu diesem Eintrag
   * @param mathResult Ergbnis der Prüfung der Zertifikatssignatur OK oder corrupted (true or false)
   * @param offlineResult bei Offlineprüfung Ergebnis der Offline-Gültigkeitsprüfung des Zertifikates valid oder invalid (ture, false))
   */
  Inspection(X509Certificate cert, String certType, Timestamp timeStamp, String mathResult, String offlineResult)
  {
    x509IssuerName = cert.getIssuerX500Principal().getName(javax.security.auth.x500.X500Principal.RFC2253);
    x509SerialNumber = cert.getSerialNumber().toString();
    x509SubjectName = cert.getSubjectX500Principal().getName(javax.security.auth.x500.X500Principal.RFC2253);
    this.timeStamp = timeStamp;
    this.mathResult = mathResult;
    this.offlineResult = offlineResult;
    this.isOnlineChecked = false;
    this.certType = certType;
  }

  /**
   *
   * @param cert Zertifikat für das diese Prüfergebnisse gelten
   * @param certType Art des Zertifikates Advanced, qualified oder unkonwn
   * @param timeStamp Timestamp Element zu diesem Eintrag
   * @param mathResult Ergbnis der Prüfung der Zertifikatssignatur OK oder corrupted (true or false)
   * @param offlineResult bei Offlineprüfung Ergebnis der Offline-Gültigkeitsprüfung des Zertifikates valid oder invalid (tur, false))
   * @param onlineResult Ergebnis der Online Prüfung OK oder revoked
   * @param onlineCheckName art der Onlineprüfung OCSP, CRL oder LDAP
   * @param onlineCheck Ergebnis der CRL oder OCSP. Entweder base64 oder dateTime Objekt
   */
  Inspection(X509Certificate cert, String certType, Timestamp timeStamp, String mathResult, String offlineResult,
             boolean onlineResult, String[] onlineCheckName, String[] onlineCheck)
  {
    x509IssuerName = cert.getIssuerX500Principal().getName(javax.security.auth.x500.X500Principal.RFC2253);
    x509SerialNumber = cert.getSerialNumber().toString();
    x509SubjectName = cert.getSubjectX500Principal().getName(javax.security.auth.x500.X500Principal.RFC2253);
    this.timeStamp = timeStamp;
    this.mathResult = mathResult;
    this.offlineResult = offlineResult;
    this.onlineResult = onlineResult;
    this.certType = certType;
    this.onlineCheckName = onlineCheckName;
    this.onlineCheck = onlineCheck;
    this.isOnlineChecked = true;
  }

  void setTimeStamp(Timestamp timeStamp)
  {
    this.timeStamp = timeStamp;
  }

  // ok / revoked
  void setOnlineResult(boolean onlineResult)
  {
    this.isOnlineChecked = true;
    this.onlineResult = onlineResult;
  }

  void setOnlineCheckNames(String[] onlineCheckName)
  {
    //    this.isOnlineChecked = true;
    this.onlineCheckName = onlineCheckName;
  }

  // valid / invalid / indeterminate
  void setOfflineResult(String offlineResult)
  {
    if (!offlineResult.equals("valid") && !offlineResult.equals("invalid") && !offlineResult.equals("indeterminate"))
    {
      throw new IllegalArgumentException();
    }

    this.offlineResult = offlineResult.toLowerCase();
  }

  void setOnlineChecks(String[] onlineCheck)
  {
    if (onlineCheck.length > 0)
      this.isOnlineChecked = true;

    this.onlineCheck = onlineCheck;
  }

  // ok / corrupted / indeterminate
  void setMathResult(String mathResult)
  {
    if (!mathResult.equals("ok") && !mathResult.equals("corrupted") && !mathResult.equals("indeterminate"))
    {
      throw new IllegalArgumentException();
    }

    this.mathResult = mathResult.toLowerCase();
  }

  void setCertType(String certType)
  {
    if (certType.equals(CERT_TYPE_ACCREDITED) || certType.equals(CERT_TYPE_QUALIFIED) ||
            certType.equals(CERT_TYPE_ADVANCED) || certType.equals(CERT_TYPE_UNKNOWN))
      this.certType = certType;
    else
      throw new IllegalArgumentException();
  }

  void setX509IssuerName(String x509IssuerName)
  {
    this.x509IssuerName = x509IssuerName;
  }

  void setX509SerialNumber(String x509SerialNumber)
  {
    this.x509SerialNumber = x509SerialNumber;
  }

  void setX509SubjectNameNumber(String x509SubjectName)
  {
    this.x509SubjectName = x509SubjectName;
  }

  /**
   * Liefert die Seriennummer des Zertifikats.
   * @return Zertifikatsnummer
   */
  public String getX509SerialNumber()
  {
    return x509SerialNumber;
  }

  /**
   * Liefert den Ausstellernamen den Zertifikats.
   * @return Ausstellername
   */
  public String getX509IssuerName()
  {
    return x509IssuerName;
  }

  /**
   * Liefert den Inhabernamen den Zertifikats.
   * @return Inhabername
   */
  public String getX509SubjectName()
  {
    return x509SubjectName;
  }

  /**
   * Liefert den Zeitstempel der Prüfung.
   * @return Zeitstempel
   */
  public Timestamp getTimeStamp()
  {
    return timeStamp;
  }

  /**
   * Gibt an, ob das Zertifikat online (OCSP/CRL/LDAP-Prüfung) geprüft wurde.
   * @return true online-Prüfung durchgeführt
   */
  public boolean isOnlineChecked()
  {
    return isOnlineChecked;
  }

  /**
   * Liefert die Namen der online-Prüfverfahrens.
   * @return online-Prüfverfahren (OCSP/CRL/LDAP)
   */
  public String[] getOnlineCheckNames()
  {
    return onlineCheckName;
  }

  /**
   * Liefert das Ergebnis der online-Prüfung.
   * @return Ergebnis: true = ok, false = revoked.
   */
  public boolean getOnlineResult()
  {
    return onlineResult;
  }

  /**
   * Liefert die Inhalte der Prüfverfahrens-Einträge. Dies ist bei OCSP-Prüfung
   * der Base-64-codierte OCSP-Request und bei CRL-Prüfung das Datum der CRL-Liste.
   * @return Inhalt des Eintrags
   */
  public String[] getOnlineChecks()
  {
    return onlineCheck;
  }

  /**
   * Liefert das Ergebnis der mathematischen Zertifikatsprüfung.
   * @return Ergebnis: ok / corrupted / indeterminate
   */
  public String getMathResult()
  {
    return mathResult;
  }

  /**
   * Liefert das Ergebnis der offline-Prüfung.
   * @return Ergebnis: valid / invalid / indeterminate
   */
  public String getOfflineResult()
  {
    return offlineResult;
  }

  /**
   * Liefert den Typ des Zertifikats.
   * @return Typ (advanced/qualified/accredited/unknown)
   */
  public String getCertType()
  {
    return certType;
  }

  /**
   * undocumented
   *
   * @param out undocumented
   *
   * @throws IOException undocumented
   */
  protected void writeXML(OutputStream out) throws IOException
  {
    out.write(("<" + osciNSPrefix + ":Inspection>").getBytes(Constants.CHAR_ENCODING));
    timeStamp.writeXML(out);
    out.write(("<" + osciNSPrefix + ":X509SubjectName>" + ProcessCardBundle.encode(x509SubjectName) + "</" +
              osciNSPrefix + ":X509SubjectName><" + osciNSPrefix + ":X509IssuerName>" +
              ProcessCardBundle.encode(x509IssuerName) + "</" + osciNSPrefix + ":X509IssuerName><" + osciNSPrefix +
              ":X509SerialNumber>" + x509SerialNumber + "</" + osciNSPrefix + ":X509SerialNumber><" + osciNSPrefix +
              ":CertType Type=\"" + certType + "\"></" + osciNSPrefix + ":CertType><" + osciNSPrefix +
              ":MathResult Result=\"" + mathResult.toLowerCase() + "\"></" + osciNSPrefix + ":MathResult><" +
              osciNSPrefix + ":OfflineResult Result=\"" + offlineResult.toLowerCase() + "\"></" + osciNSPrefix +
              ":OfflineResult>").getBytes(Constants.CHAR_ENCODING));

    if (isOnlineChecked)
    {
      String res = "revoked";

      if (onlineResult)
      {
        res = "ok";
      }

      out.write(("<" + osciNSPrefix + ":OnlineResult Result=\"" + res + "\">").getBytes(Constants.CHAR_ENCODING));

      for (int i = 0; i < onlineCheckName.length; i++)
      {
        out.write(("<" + osciNSPrefix + ":" + onlineCheckName[i] + ">" + onlineCheck[i] + "</" + osciNSPrefix + ":" +
                  onlineCheckName[i] + ">").getBytes(Constants.CHAR_ENCODING));
      }

      out.write(("</" + osciNSPrefix + ":OnlineResult>").getBytes(Constants.CHAR_ENCODING));
    }

    out.write(("</" + osciNSPrefix + ":Inspection>").getBytes(Constants.CHAR_ENCODING));
  }
}
