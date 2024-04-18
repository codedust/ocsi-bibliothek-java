package de.osci.osci12.soapheader;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.osci.osci12.OSCIException;
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
 */
public class ControlBlockH extends HeaderEntry
{
  private static Log log = LogFactory.getLog(ControlBlockH.class);
  /** Challenge der Nachricht. */
  private String challenge = null;
  /** Response der Nachricht. */
  private String response = null;
  /** Sequence Nummer der Nachricht. Sequenz beginnt mit dem Value 0 */
  private int sequenceNumber = -1;
  /** Die durch den Supplier vergebene DialogID. */
  private String conversationID = null;

  //    public ElementQuantity elementQuantity = new ElementQuantity();
  /**
   * Creates a new ControlBlock object.
   */
  public ControlBlockH()
  {
  }

  /**
   * undocumented
   *
   * @return undocumented
   */
  public String getChallenge()
  {
    if (log.isDebugEnabled())
      log.debug("challenge:" + challenge);

    return challenge;
  }

  /**
   * undocumented
   *
   * @return undocumented
   */
  public String getConversationID()
  {
    return conversationID;
  }

  /**
   * undocumented
   *
   * @return undocumented
   */
  public String getResponse()
  {
    return response;
  }

  /**
   * undocumented
   *
   * @return undocumented
   */
  public int getSequenceNumber()
  {
    return sequenceNumber;
  }

  /**
   * undocumented
   *
   * @param sequenceNumber undocumented
   */
  public void setSequenceNumber(int sequenceNumber)
  {
    this.sequenceNumber = sequenceNumber;
  }

  /**
   * undocumented
   *
   * @param conversationID undocumented
   */
  public void setConversationID(String conversationID)
  {
    this.conversationID = conversationID;
  }

  /**
   * undocumented
   *
   * @param challenge undocumented
   */
  public void setChallenge(String challenge)
  {
    this.challenge = challenge;
  }

  /**
   * undocumented
   *
   * @param response undocumented
   */
  public void setResponse(String response)
  {
    this.response = response;
  }

  // Im ControlBlockH dürfen keine temporüren Buffer verwendet werden, weil dieses
  // Objekt als Bestandteil des DialogHandlers immer wieder veründert wird.
  public byte[] getDigestValue(String digestAlgorithm) throws java.security.NoSuchAlgorithmException,
                                        IOException,
                                        OSCIException
  {
    digestValues.remove(digestAlgorithm);

    return super.getDigestValue(digestAlgorithm);
  }

  /**
   * undocumented
   *
   * @return undocumented
   *
   * @throws IOException undocumented
   * @throws OSCIException undocumented
   */
  public long getLength() throws IOException,
                                 OSCIException
  {
    length = -1;

    return super.getLength();
  }

  /**
   * undocumented
   *
   * @param out undocumented
   *
   * @throws IOException undocumented
   */
  protected void writeXML(OutputStream out) throws IOException //, OSCIRoleException
  {
    // Weil dieses Objekt im DialogHandler referenziert ist, darf es nicht aus dem tmpBuffer
    // geholt werden
    out.write(("<" + osciNSPrefix + ":ControlBlock").getBytes(Constants.CHAR_ENCODING));
    out.write(ns);

    if (conversationID != null)
      out.write((" ConversationId=\"" + conversationID + "\"").getBytes(Constants.CHAR_ENCODING));

    out.write(" Id=\"controlblock\"".getBytes(Constants.CHAR_ENCODING));

    if (sequenceNumber != -1)
      out.write((" SequenceNumber=\"" + sequenceNumber + "\"").getBytes(Constants.CHAR_ENCODING));

    out.write((" " + soapNSPrefix + ":actor=\"http://schemas.xmlsoap.org/soap/actor/next\" " + soapNSPrefix +
              ":mustUnderstand=\"1\">").getBytes(Constants.CHAR_ENCODING));

    if (response != null)
      out.write(("<" + osciNSPrefix + ":Response>" + response + "</" + osciNSPrefix + ":Response>").getBytes(Constants.CHAR_ENCODING));

    if (challenge != null)
      out.write(("<" + osciNSPrefix + ":Challenge>" + challenge + "</" + osciNSPrefix + ":Challenge>").getBytes(Constants.CHAR_ENCODING));

    out.write(("</" + osciNSPrefix + ":ControlBlock>").getBytes(Constants.CHAR_ENCODING));
  }
}
