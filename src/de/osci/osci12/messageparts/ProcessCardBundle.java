package de.osci.osci12.messageparts;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.osci.osci12.common.Constants;


/**
 * Diese Klasse bildet den OSCI-Laufzettel ab.
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
public class ProcessCardBundle extends MessagePart
{
  private static Log log = LogFactory.getLog(ProcessCardBundle.class);
  public String name;
  private String recentModification;
  private String subject;
  private String messageId;
  Timestamp creation;
  Timestamp forwarding;
  Timestamp reception;
  private Inspection[] inspections;

  ProcessCardBundle(String name)
  {
    this.name = name;
  }

  ProcessCardBundle(String name, String messageId, String recentModification, Timestamp creation, Timestamp forwarding,
                    Timestamp reception, String subject, Inspection[] inspections)
  {
    this.name = name;
    this.messageId = messageId;
    this.recentModification = recentModification;
    this.creation = creation;
    this.forwarding = forwarding;
    this.reception = reception;
    this.subject = subject;
    this.inspections = inspections;
  }

  /**
   * Liefert den Zeitstempel der Erstellung.
   * @return Zeitstempel
   */
  public Timestamp getCreation()
  {
    return creation;
  }

  /**
   * Liefert den Zeitstempel der Weiterleitung.
   * @return Zeitstempel
   */
  public Timestamp getForwarding()
  {
    return forwarding;
  }

  /**
   * Liefert den Zeitstempel des Empfangs.
   * @return Zeitstempel
   */
  public Timestamp getReception()
  {
    return reception;
  }

  /**
   * Liefert die Ergebnisse der Zertifikatsprüfungen in Form von Inspection-Objekten,
   * die im ProcessCardBundle-Objekt enthalten sind.
   * @return inspections die Prüfergebnisse
   */
  public Inspection[] getInspections()
  {
    return inspections;
  }

  /**
   * Liefert die Message-ID der Nachricht.
   * @return Message-ID
   */
  public String getMessageId()
  {
    return messageId;
  }

  /**
   * Liefert das Datum der letzten Änderung des Laufzettels. Das Format
   * entspricht dem XML-Schema nach <a href="http://www.w3.org/TR/xmlschema-2/#dateTime">
   * http://www.w3.org/TR/xmlschema-2/#dateTime</a>.
   * @return Datum der letzten Änderung
   */
  public String getRecentModification()
  {
    return recentModification;
  }

  /**
   * Liefert den im Laufzettel enthaltenen Betreff-Eintrag.
   * @return den Betreff der Zustellung
   */
  public String getSubject()
  {
    return subject;
  }

  /**
   * Interne Methode.
   * @return ProcessCardBundle als String
   * @throws IOException undocumented
   */
  public String writeToString() throws IOException
  {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    writeXML(out);
    out.close();

    return out.toString(Constants.CHAR_ENCODING);
  }

  void setCreation(Timestamp creation)
  {
    this.creation = creation;
  }

  void setForwarding(Timestamp forwarding)
  {
    this.forwarding = forwarding;
  }

  void setInspections(Inspection[] inspections)
  {
    this.inspections = inspections;
  }

  void setMessageId(String messageId)
  {
    this.messageId = messageId;
  }

  void setName(String name)
  {
    this.name = name;
  }

  void setReception(Timestamp reception)
  {
    this.reception = reception;
  }

  void setSubject(String subject)
  {
    this.subject = subject;
  }

  void setRecentModification(String recentModification)
  {
    this.recentModification = recentModification;
  }

  static String encode(String text)
  {
    text = text.replaceAll("&", "&amp;");
    text = text.replaceAll("<", "&lt;");
    text = text.replaceAll(">", "&gt;");

    return text.replaceAll("\r", "&#xD;");
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
    writeXML(out, false);
  }

  /** Interne Methode */
  protected void writeXML(OutputStream out, boolean writeObj)
                   throws IOException
  {
    if (log.isDebugEnabled())
      log.debug("Name: " + name);

    if (log.isDebugEnabled())
      log.debug("MessageID: " + messageId);

    if (log.isDebugEnabled())
      log.debug("recentModi: " + recentModification);

    out.write(("<" + osciNSPrefix + ":" + name).getBytes(Constants.CHAR_ENCODING));

    if (writeObj)
      out.write((" xmlns:" + osciNSPrefix + "=\"http://www.osci.de/2002/04/osci\"").getBytes(Constants.CHAR_ENCODING));

    out.write(("><" + osciNSPrefix + ":MessageId>" +
              de.osci.helper.Base64.encode(messageId.getBytes(Constants.CHAR_ENCODING)) + "</" + osciNSPrefix +
              ":MessageId><" + osciNSPrefix + ":ProcessCard RecentModification=\"" + recentModification + "\">").getBytes(Constants.CHAR_ENCODING));

    if (creation != null)
      creation.writeXML(out);

    if (forwarding != null)
      forwarding.writeXML(out);

    if (reception != null)
      reception.writeXML(out);

    if (subject != null)
      out.write(("<" + osciNSPrefix + ":Subject>" + subject + "</" + osciNSPrefix + ":Subject>").getBytes(Constants.CHAR_ENCODING));

    out.write(("</" + osciNSPrefix + ":ProcessCard><" + osciNSPrefix + ":InspectionReport>").getBytes(Constants.CHAR_ENCODING));

    if (inspections != null)
    {
      for (int i = 0; i < inspections.length; i++)
        inspections[i].writeXML(out);
    }

    out.write(("</" + osciNSPrefix + ":InspectionReport></" + osciNSPrefix + ":" + name + ">").getBytes(Constants.CHAR_ENCODING));
  }
}
