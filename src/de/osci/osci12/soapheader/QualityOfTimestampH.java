package de.osci.osci12.soapheader;

import java.io.IOException;
import java.io.OutputStream;


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
public class QualityOfTimestampH extends HeaderEntry
{
  //  private static Log log = LogFactory.getLog(QualityOfTimestampH.class);
  private boolean service;
  private boolean quality;

  /**
   * Creates a new QualityOfTimestampH object.
   *
   * @param serviceReception undocumented
   * @param qualityCryptographic undocumented
   */
  public QualityOfTimestampH(boolean serviceReception, boolean qualityCryptographic)
  {
    this.service = serviceReception;
    this.quality = qualityCryptographic;
    id = "qualityoftimestamp_" + (service ? "reception" : "creation");
  }

  /**
   * undocumented
   *
   * @return undocumented
   */
  public boolean isQualityCryptographic()
  {
    return quality;
  }

  /**
   * undocumented
   *
   * @return undocumented
   */
  public boolean isServiceReception()
  {
    return service;
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
    out.write(("<" + osciNSPrefix + ":QualityOfTimestamp").getBytes(de.osci.osci12.common.Constants.CHAR_ENCODING));
    out.write(ns);
    out.write((" Id=\"" + getRefID() + "\" Quality=\"" + (quality ? "cryptographic" : "plain") + "\" Service=\"" +
              (service ? "reception" : "creation") + "\" " + soapNSPrefix +
              ":actor=\"http://schemas.xmlsoap.org/soap/actor/next\" " + soapNSPrefix + ":mustUnderstand=\"1\"" +
              "></" + osciNSPrefix + ":QualityOfTimestamp>").getBytes(de.osci.osci12.common.Constants.CHAR_ENCODING));
  }
}
