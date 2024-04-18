package de.osci.osci12.messageparts;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.osci.osci12.common.Constants;


/**
 * <p>Diese Klasse repräsentiert das OSCI-Timestamp-Element, es enthält
 * Zeitstempelinformationen</p>
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
public class Timestamp extends MessagePart implements Serializable
{
  private static Log log = LogFactory.getLog(Timestamp.class);
  private static final long serialVersionUID = 3258130267062415434L;
  static final String[] TIMESTAMP_NAMES = { "Creation", "Forwarding", "Reception", "Timestamp" };
  public static final int PROCESS_CARD_CREATION = 0;
  public static final int PROCESS_CARD_FORWARDING = 1;
  public static final int PROCESS_CARD_RECEPTION = 2;
  public static final int PROCESS_CARD_TIMESTAMP = 3;
  private String algorithm;
  private int nameID;
  private String timeStamp;

  Timestamp()
  {
  }

  // Bei algorithm == null oder Leerstring ist der Typ "plain"
  public Timestamp(int nameID, String algorithm, String timeStamp)
  {
    this.nameID = nameID;
    this.algorithm = algorithm;
    this.timeStamp = timeStamp;
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
    out.write(("<" + osciNSPrefix + ":" + TIMESTAMP_NAMES[nameID] + ">").getBytes(Constants.CHAR_ENCODING));

    if ((algorithm == null) || (algorithm.length() == 0))
      out.write(("<" + osciNSPrefix + ":Plain>" + timeStamp + "</" + osciNSPrefix + ":Plain>").getBytes(Constants.CHAR_ENCODING));
    else
      out.write(("<" + osciNSPrefix + ":Cryptographic Algorithm=\"" + algorithm + "\">" +
                de.osci.helper.Base64.encode(timeStamp.getBytes(Constants.CHAR_ENCODING)) + "</" + osciNSPrefix +
                ":Cryptographic>").getBytes(Constants.CHAR_ENCODING));

    out.write(("</" + osciNSPrefix + ":" + TIMESTAMP_NAMES[nameID] + ">").getBytes(Constants.CHAR_ENCODING));
  }

  /**
   * Liefert den Algorithmus (für kryptographische Zeitstempel).
   * @return den Algorithmus
   */
  public String getAlgorithm()
  {
    return algorithm;
  }

  /**
   * Liefert den Typ des Zeitstempels als String. Mögliche Werte sind
   * "Creation", "Forwarding", "Reception" und "Timestamp".
   * @return id
   */
  public String getName()
  {
    return TIMESTAMP_NAMES[nameID];
  }

  /**
   * Liefert den Zeitstempelstring selbst.
   * @return Zeitstempel
   */
  public String getTimeStamp()
  {
    return timeStamp;
  }

  void setAlgorithm(String algorithm)
  {
    this.algorithm = algorithm;
  }

  void setTimeStamp(String timeStamp)
  {
    this.timeStamp = timeStamp;
  }

  /**
   * Liefert den Identifier des Zeitstempeltyps. Mögliche Werte sind
   * PROCESS_CARD_CREATION, PROCESS_CARD_FORWARDING, PROCESS_CARD_RECEPTION
   * und PROCESS_CARD_TIMESTAMP.
   * @return id Typ
   */
  public int getNameID()
  {
    return nameID;
  }

  void setNameID(int nameID)
  {
    this.nameID = nameID;
  }
}
