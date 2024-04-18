package de.osci.osci12.messageparts;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Vector;

import de.osci.osci12.OSCIException;
import de.osci.osci12.common.Constants;
import de.osci.osci12.common.DialogHandler;


/**
 * Diese Klasse bildet eine XML-Signature-Referenz in der OSCI-Bibliothek ab.
 * <p>
 * Copyright © 2023 Governikus GmbH &amp; Co. KG, Germany
 * </p>
 * <p>Erstellt von Governikus GmbH &amp; Co. KG</p>
 * <p>
 * Diese Bibliothek kann von jedermann nach Maßgabe der European Union Public Licence genutzt
 * werden.
 * </p>
 * Die Lizenzbestimmungen können unter der URL <a href="https://eupl.eu/">https://eupl.eu/</a> abgerufen werden.
 * </p>
 *
 * @author R. Lindemann, N.Büngener
 * @version 2.4.1
 */
class OSCISignatureReference extends MessagePart
{

  // private static Log log = LogFactory.getLog(OSCISignatureReference.class);
  Vector<String> transformerAlgorithms = new Vector<String>();

  String digestMethodAlgorithm;

  byte[] digestValue;

  OSCISignatureReference(MessagePart messagePart, String digestAlgorithm)
    throws java.security.NoSuchAlgorithmException, IOException, OSCIException
  {
    if (messagePart instanceof Attachment)
      id = "cid:" + messagePart.getRefID();
    else
      id = "#" + messagePart.getRefID();

    setNSPrefixes(messagePart.soapNSPrefix,
                  messagePart.osciNSPrefix,
                  messagePart.dsNSPrefix,
                  messagePart.xencNSPrefix,
                  messagePart.xsiNSPrefix);
    digestMethodAlgorithm = digestAlgorithm;
    digestValue = messagePart.getDigestValue(digestMethodAlgorithm);
    transformerAlgorithms = messagePart.transformers;
  }

  // Konstruktor für den Parser
  OSCISignatureReference()
  {}

  /**
   * Liefert den Hashalgorithmus.
   *
   * @return String-Id des Hashalgorithmus
   * @see de.osci.osci12.common.Constants
   */
  public String getDigestMethodAlgorithm()
  {
    return digestMethodAlgorithm;
  }

  /**
   * Liefert den Hashwert.
   *
   * @return den Hashwert oder null, falls dieser noch nicht ermittelt wurde.
   */
  public byte[] getDigestValue()
  {
    return digestValue;
  }

  void setDigestValue(byte[] digestValue)
  {
    this.digestValue = digestValue;
  }

  /**
   * undocumented
   *
   * @return undocumented
   */
  public String[] getTransformerAlgorithms()
  {
    return (String[])transformerAlgorithms.toArray(new String[0]);
  }

  /**
   * undocumented
   *
   * @param digestMethodAlgorithm undocumented
   */
  public void setDigestMethodAlgorithm(String digestMethodAlgorithm)
  {
    if (Constants.JCA_JCE_MAP.get(digestMethodAlgorithm) == null)
      throw new IllegalArgumentException(DialogHandler.text.getString("invalid_hash_algorithm") + " "
                                         + digestMethodAlgorithm);
    this.digestMethodAlgorithm = digestMethodAlgorithm;
  }


  /**
   * undocumented
   *
   * @param out undocumented
   * @throws java.io.IOException undocumented
   */
  protected void writeXML(java.io.OutputStream out) throws java.io.IOException
  {
    // id
    // out.write(("<ds:Reference URI=\"" + prefix + id + "\">").getBytes(Constants.CHAR_ENCODING));
    out.write(("<" + dsNSPrefix + ":Reference URI=\"" + id + "\">\n").getBytes(Constants.CHAR_ENCODING));

    if (transformerAlgorithms.size() > 0)
    {
      out.write(("<" + dsNSPrefix + ":Transforms>\n").getBytes(Constants.CHAR_ENCODING));

      for ( int j = 0 ; j < transformerAlgorithms.size() ; j++ )
        out.write(transformerAlgorithms.get(j).getBytes(Constants.CHAR_ENCODING));

      out.write(("\n</" + dsNSPrefix + ":Transforms>\n").getBytes(Constants.CHAR_ENCODING));
    }

    out.write(("<" + dsNSPrefix + ":DigestMethod Algorithm=\"" + digestMethodAlgorithm + "\"></" + dsNSPrefix
               + ":DigestMethod>\n<" + dsNSPrefix + ":DigestValue>"
               + de.osci.helper.Base64.encode(digestValue) + "</" + dsNSPrefix + ":DigestValue>\n</"
               + dsNSPrefix + ":Reference>\n").getBytes(Constants.CHAR_ENCODING));
  }

  /**
   * undocumented
   *
   * @return undocumented
   */
  public String toString()
  {
    try
    {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      this.writeXML(out);

      return out.toString();
    }
    catch (IOException ex)
    {
      // kann nicht vorkommen
      return "";
    }
  }
}
