package de.osci.osci12.messageparts;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.osci.osci12.OSCIException;
import de.osci.osci12.common.Constants;
import de.osci.osci12.soapheader.HeaderEntry;


/**
 * Diese Klasse stellt einen OSCI-Body (soap:Body) dar.
 * Sie muss von Anwendungen nie direkt verwendet werden.
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

// Body ist nicht wirklich ein soapheader, aber es ist ganz praktisch...
public class Body extends HeaderEntry
{
  private static Log log = LogFactory.getLog(Body.class);
  String data;
  private ContentContainer[] cc;
  private EncryptedDataOSCI[] encData;

  /**
   * Setzt den Body initial mit einem vorbereiteten String.
   * @param data Inhalt
   */
  public Body(String data)
  {
    this.data = data;
    id = "body";
  }

  /**
   * Legt ein Body-Objekt mit Inhaltsdaten an.
   * @param cc ContentContainer mit unverschlüsselten Inhaltsdaten
   * @param encData EncryptedData mit unverschlüsselten Inhaltsdaten
   */
  public Body(ContentContainer[] cc, EncryptedDataOSCI[] encData)
  {
    this.cc = cc;

    Hashtable<String, String> nsp = new Hashtable<String, String>();
    nsp.put(osciNSPrefix, MessagePartParser.OSCI_XMLNS);
    nsp.put(soapNSPrefix, MessagePartParser.SOAP_XMLNS);
    nsp.put(dsNSPrefix, MessagePartParser.DS_XMLNS);
    nsp.put(xencNSPrefix, MessagePartParser.XENC_XMLNS);
    nsp.put(xsiNSPrefix, MessagePartParser.XSI_XMLNS);

    for (int i = 0; i < cc.length; i++)
    {
      nsp.put(cc[i].osciNSPrefix, MessagePartParser.OSCI_XMLNS);
      nsp.put(cc[i].soapNSPrefix, MessagePartParser.SOAP_XMLNS);
      nsp.put(cc[i].dsNSPrefix, MessagePartParser.DS_XMLNS);
      nsp.put(cc[i].xencNSPrefix, MessagePartParser.XENC_XMLNS);
      nsp.put(cc[i].xsiNSPrefix, MessagePartParser.XSI_XMLNS);
    }

    for (int i = 0; i < encData.length; i++)
    {
      nsp.put(encData[i].osciNSPrefix, MessagePartParser.OSCI_XMLNS);
      nsp.put(encData[i].soapNSPrefix, MessagePartParser.SOAP_XMLNS);
      nsp.put(encData[i].dsNSPrefix, MessagePartParser.DS_XMLNS);
      nsp.put(encData[i].xencNSPrefix, MessagePartParser.XENC_XMLNS);
      nsp.put(encData[i].xsiNSPrefix, MessagePartParser.XSI_XMLNS);
    }

    Vector<String> keySet = new Vector<String>(nsp.keySet());
    Collections.sort(keySet);

    StringBuffer sb = new StringBuffer("");

    for (int i = 0; i < keySet.size(); i++)
    {
      sb.append(" xmlns:");
      sb.append(keySet.get(i));
      sb.append("=\"");
      sb.append(nsp.get(keySet.get(i)));
      sb.append("\"");
    }

    try
    {
      ns = sb.toString().getBytes(Constants.CHAR_ENCODING);
      log.debug("NS: " + new String(ns));
    }
    catch (UnsupportedEncodingException ex)
    {
    }

    this.encData = encData;
    id = "body";
  }

  /**
   * Liefert den Hashwert des Message-Parts. Diese Methode muss für Body überschrieben werden,
   * weil die Länge dieses Tags in der Nachricht von der des signierten Tags abweicht.
   *
   *@return The digestValue value
   */
  protected byte[] getDigestValue(String digestAlgorithm) throws java.security.NoSuchAlgorithmException,
                                           IOException,
                                           OSCIException
  {
    byte[] ret = super.getDigestValue(digestAlgorithm);
    length = -1;

    return ret;
  }

  /**
   * Diese Methode wird intern aufgerufen.
   * Es wird der eingestellte Body serialisiert.
   * @param out Outputstream, in den geschrieben werden soll
   * @throws IOException im Fehlerfall
   */
  public void writeXML(OutputStream out) throws IOException,
                                                OSCIException
  {
    out.write(0x3c);
    out.write(soapNSPrefix.getBytes(Constants.CHAR_ENCODING));
    out.write((":Body").getBytes(Constants.CHAR_ENCODING));
    out.write(ns);
    out.write((" Id=\"" + id + "\">").getBytes(Constants.CHAR_ENCODING));

    if (data != null)
      out.write(data.getBytes(Constants.CHAR_ENCODING));
    else
    {
      out.write(0x3c);
      out.write(osciNSPrefix.getBytes(Constants.CHAR_ENCODING));
      out.write(":ContentPackage>".getBytes(Constants.CHAR_ENCODING));

      for (int i = 0; i < encData.length; i++)
        encData[i].writeXML(out, false);

      for (int i = 0; i < cc.length; i++)
        cc[i].writeXML(out, false);

      out.write("</".getBytes(Constants.CHAR_ENCODING));
      out.write(osciNSPrefix.getBytes(Constants.CHAR_ENCODING));
      out.write(":ContentPackage>".getBytes(Constants.CHAR_ENCODING));
    }

    out.write("</".getBytes(Constants.CHAR_ENCODING));
    out.write(soapNSPrefix.getBytes(Constants.CHAR_ENCODING));
    out.write(":Body>".getBytes(Constants.CHAR_ENCODING));
  }
}
