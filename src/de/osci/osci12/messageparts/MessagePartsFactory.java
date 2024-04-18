package de.osci.osci12.messageparts;

import java.io.IOException;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import de.osci.osci12.OSCIException;


/**
 * Die Methoden dieser Factory-Klasse werden von Anwendungen nicht benötigt und
 * sind deshalb nicht dokumentiert.
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
public class MessagePartsFactory
{
  /**
   * undocumented
   *
   * @param omb undocumented
   *
   * @return undocumented
   */
  public static ContentPackageBuilder createContentPackageBuilder(de.osci.osci12.messagetypes.OSCIMessageBuilder omb /*, String soap, String osci, String ds, String xenc*/)
  {
    ContentPackageBuilder cpb = new ContentPackageBuilder(omb);

    return cpb;
  }

  public static ChunkInformation creatChunkInformation(ChunkInformation.CheckInstance chunkInstance)
  {
    return new ChunkInformation(chunkInstance);
  }
  /**
   * undocumented
   *
   * @param xmlReader undocumented
   * @param parentHandler undocumented
   * @param atts undocumented
   *
   * @return undocumented
   * @throws SAXException undocumented
   */
  public static OSCISignatureBuilder createOsciSignatureBuilder(XMLReader xmlReader, DefaultHandler parentHandler,
                                                                Attributes atts) throws SAXException
  {
    return new OSCISignatureBuilder(xmlReader, parentHandler, atts,false);
  }

  /**
   * undocumented
   *
   * @param name undocumented
   * @param messageId undocumented
   * @param recentModification undocumented
   * @param creation undocumented
   * @param forwarding undocumented
   * @param reception undocumented
   * @param subject undocumented
   * @param inspections undocumented
   *
   * @return undocumented
   */
  public static ProcessCardBundle createProcessCardBundle(String name, String messageId, String recentModification,
                                                          Timestamp creation, Timestamp forwarding,
                                                          Timestamp reception, String subject, Inspection[] inspections)
  {
    return new ProcessCardBundle(name, messageId, recentModification, creation, forwarding, reception, subject,
                                 inspections);
  }

  /**
   * undocumented
   *
   * @param enclosingElement undocumented
   *
   * @return undocumented
   */
  public static OSCISignature createOSCISignature(String enclosingElement)
  {
    return new OSCISignature(enclosingElement);
  }

  /**
   * undocumented
   *
   * @param mp undocumented
   * @param digestAlgorithm undocumented
   * @return undocumented
   *
   * @throws IOException undocumented
   * @throws java.security.NoSuchAlgorithmException undocumented
   * @throws OSCIException undocumented
   */
  public static OSCISignatureReference createOSCISignatureReference(MessagePart mp, String digestAlgorithm)
    throws IOException,
           java.security.NoSuchAlgorithmException,
           OSCIException
  {
    return new OSCISignatureReference(mp, digestAlgorithm);
  }

  /**
   * undocumented
   *
   * @param feedback undocumented
   *
   * @return undocumented
   */
  public static FeedbackObject createFeedbackObject(String[] feedback)
  {
    return new FeedbackObject(feedback);
  }

  /**
   * attachment
   *
   * @param ins undocumented
   * @param refId undocumented
   * @param length undocumented
   * @param transportDigestAlgorithm undocumented
   * @return undocumented
   *
   * @throws IOException undocumented
   * @throws NoSuchAlgorithmException undocumented
   */
  public static Attachment attachment(java.io.InputStream ins, String refId, long length, String transportDigestAlgorithm)
                               throws IOException,
                                      NoSuchAlgorithmException
  {
    return new Attachment(ins, refId, length, transportDigestAlgorithm);
  }

  /**
   * undocumented
   *
   * @param attachment undocumented
   * @param ins undocumented
   * @param encrypt undocumented
   * @param length undocumented undocumented
   * @param transportDigestAlgorithm undocumented
   * @throws IOException undocumented
   * @throws NoSuchAlgorithmException undocumented
   */
  public static void attachmentSetStream(Attachment attachment, java.io.InputStream ins, boolean encrypt, long length, String transportDigestAlgorithm)
                                  throws IOException,
                                         NoSuchAlgorithmException
  {
    attachment.setInputStream(ins, encrypt, length, transportDigestAlgorithm);
  }

  /**
   * undocumented
   *
   * @param att undocumented
   * @param newState undocumented
   * @param encrypted undocumented
   */
  public static void attachmentSetState(Attachment att, int newState, boolean encrypted)
  {
    att.stateOfAttachment = newState;
    att.encrypt = encrypted;
  }

  /**
   * attachmentIsBase64
   *
   * @param att undocumented
   * @return Base64, true
   */
  public static boolean attachmentIsBase64(Attachment att)
  {
    return att.base64;
  }

  /**
   * setDigestValue
   * @param c {@link MessagePart}
   * @param digest undocumented
   * @param digestAlgorithm Digest Algorithmus
   */
  public static void setDigestValue(MessagePart c, byte[] digest, String digestAlgorithm)
  {
    c.digestValues.put(digestAlgorithm, digest);
  }

  /**
   * Returns the message part's digest value.
   * @param c {@link MessagePart}
   * @param digestAlgorithm Digest Algorithmus
   * @return digest value
   * @throws OSCIException undocumented
   * @throws IOException undocumented
   * @throws NoSuchAlgorithmException undocumented
   */
  public static byte[] getDigestValue(MessagePart c, String digestAlgorithm) throws NoSuchAlgorithmException, IOException, OSCIException
  {
    return c.getDigestValue(digestAlgorithm);
  }

  /**
   * undocumented
   *
   * @param mp undocumented
   * @param out undocumented
   *
   * @throws IOException undocumented
   * @throws OSCIException undocumented
   */
  public static void writeXML(MessagePart mp, OutputStream out)
                       throws IOException,
                              OSCIException
  {
    mp.writeXML(out);
  }
}
