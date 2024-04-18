package de.osci.osci12.messageparts;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.osci.helper.NullOutputStream;
import de.osci.osci12.OSCIException;
import de.osci.osci12.common.Constants;
import de.osci.osci12.common.Constants.Namespaces;
import de.osci.osci12.common.DialogHandler;
import de.osci.osci12.encryption.Crypto;
import de.osci.osci12.messagetypes.OSCIMessage;


/**
 * <p>Diese Klasse ist die Basisklasse für sämtliche Header-Element und alle weiteren
 * Elemente welche in einer OSCI-Nachricht signiert werden (Attachment, ContentContainer,
 * Content... ).</p>
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
public abstract class MessagePart
{
  private static Log log = LogFactory.getLog(MessagePart.class);
  protected Hashtable<String, String> xmlns = new Hashtable<String, String>();
  protected String id = "";
  protected String typ;
  protected Hashtable<String, byte[]> digestValues = new Hashtable<String, byte[]>();
  protected long length = -1;
  protected Vector<String> transformers = new Vector<String>();
  protected static final String b64 = "<ds:Transform Algorithm=\"" + Constants.TRANSFORM_BASE64 + "\"></ds:Transform>";
  protected static final String can = "<ds:Transform Algorithm=\"" + Constants.TRANSFORM_CANONICALIZATION +
                                      "\"></ds:Transform>";
  public byte[] ns;
  public byte[] ns2017;
  protected MessageDigest md;
  protected String soapNSPrefix = Constants.Namespaces.SOAP.getPrefix();  
  protected String osciNSPrefix = Constants.Namespaces.OSCI.getPrefix();  
  protected String osci2017NSPrefix = Constants.Namespaces.OSCI2017.getPrefix();  
  protected String osci2019NSPrefix = Constants.Namespaces.OSCI128.getPrefix(); // optional 
  protected String dsNSPrefix = Constants.Namespaces.XML_SIG.getPrefix();  
  protected String xencNSPrefix =  Constants.Namespaces.XML_ENC.getPrefix();  
  protected String xsiNSPrefix = Constants.Namespaces.XML_SCHEMA.getPrefix();

  /**
   * Creates a new MessagePart object.
   */
  protected MessagePart()
  {
    typ = getClass().getName().substring(getClass().getName().lastIndexOf('.') + 1).toLowerCase();

    try
    {
      ns = " xmlns:ds=\"http://www.w3.org/2000/09/xmldsig#\" xmlns:osci=\"http://www.osci.de/2002/04/osci\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xenc=\"http://www.w3.org/2001/04/xmlenc#\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"".getBytes(Constants.CHAR_ENCODING);
      ns2017 = (" xmlns:ds=\"http://www.w3.org/2000/09/xmldsig#\" xmlns:osci=\"http://www.osci.de/2002/04/osci\" xmlns:osci2017=\""+Namespaces.OSCI2017.getUri()+"\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xenc=\"http://www.w3.org/2001/04/xmlenc#\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"").getBytes(Constants.CHAR_ENCODING);
    }
    catch (UnsupportedEncodingException ex)
    {
      log.debug(ex.getMessage());
      // Jede JVM unterstützt UTF-8
    }
  }

  /**
   * Setzt die Namespace-Prefixe und die NS-Deklaration, die bei der Serialisierung verwendet werden.
   * Sollte vom Anwender normalerweise nicht aufgerufen werden.
   * @param msg die Nachricht, aus der die Prefixes übernommen werden sollen
   */
  public void setNSPrefixes(OSCIMessage msg)
  {
    soapNSPrefix = msg.soapNSPrefix;
    osciNSPrefix = msg.osciNSPrefix;
    osci2017NSPrefix= msg.osci2017NSPrefix;
    osci2019NSPrefix= msg.osci2019NSPrefix;
    dsNSPrefix = msg.dsNSPrefix;
    xencNSPrefix = msg.xencNSPrefix;
    xsiNSPrefix = msg.xsiNSPrefix;

    try
    {
      ns = msg.ns.getBytes(Constants.CHAR_ENCODING);
    }
    catch (UnsupportedEncodingException ex)
    {
      log.debug(ex.getMessage());
      // Tritt nicht auf
    }
  }

  /**
   * Setzt die Namespace-Prefixe, die bei der Serialisierung verwendet werden.
   * Sollte vom Anwender normalerweise nicht aufgerufen werden.
   * @param soap SOAP-Prefix
   * @param osci OSCI-Prefix
   * @param ds Digital Signature-Prefix
   * @param xenc XML Encryption-Prefix
   * @param xsi XML Schema-Prefix
   */
  public void setNSPrefixes(String soap, String osci, String ds, String xenc, String xsi)
  {
    soapNSPrefix = soap;
    osciNSPrefix = osci;
    dsNSPrefix = ds;
    xencNSPrefix = xenc;
    xsiNSPrefix = xsi;
  }

  /**
   * Setzt das refID-Attribut des MessagePart-Objekts. Diese Eigenschaft wird
   * in Subklassen für unterschiedliche Zwecke verwendet, z.B. als Referenz-Id
   * für die Signaturen. Das Attribut muss mindestens innerhalb der OSCI-Nachricht
   * eindeutig sein.
   *
   * @param  id  refID-String
   * @see #getRefID()
   */
  public void setRefID(String id)
  {
    this.id = id;
  }

  /**
   * Liefert das refID-Attribut.
   * @return refID
   * @see #setRefID(String)
   */
  public String getRefID()
  {
    return id;
  }

  /*
   *  Liefert den Hashwert des Message-Parts.
   *
   *@return    The digestValue value
   */
  protected byte[] getDigestValue(String digestAlgorithm) throws java.security.NoSuchAlgorithmException,
                                           IOException,
                                           OSCIException
  {
    if (digestValues.get(digestAlgorithm) != null)
      return (byte[])digestValues.get(digestAlgorithm);

    if (DialogHandler.getSecurityProvider() == null)
      md = MessageDigest.getInstance(Constants.JCA_JCE_MAP.get(digestAlgorithm));
    else
      md = MessageDigest.getInstance(Constants.JCA_JCE_MAP.get(digestAlgorithm),
                                     DialogHandler.getSecurityProvider());

    NullOutputStream nos = new NullOutputStream();
//    {
//      public void write(int b) throws IOException
//      {
//        log.debug("\n\nSINGLE SIGN: "+new String(new byte[]{(byte)b}));
//        super.write(b);
//      }
//
//      public void write(byte[] data, int off, int len) throws IOException
//      {
//        log.debug("SIGN: "+new String(data, off, len));
//        super.write(data, off, len);
//      }
//
//      public void flush() throws IOException
//      {
//        log.debug("FLUSH SIGN", new Exception());
//        super.flush();
//      }
//     };

    DigestOutputStream digestOut = new DigestOutputStream(nos, md);
    writeXML(digestOut);
    digestOut.close();
    digestValues.put(digestAlgorithm, md.digest());

    if (log.isDebugEnabled())
      log.debug("enter getDigestValue" + Crypto.toHex((byte[])digestValues.get(digestAlgorithm)));

    length = nos.getLength();

    return (byte[])digestValues.get(digestAlgorithm);
  }

  /**
   * Berechnet die Länge des XML-Tags.
   * @return Länge
   * @throws IOException undocumented
   * @throws de.osci.osci12.roles.OSCIRoleException undocumented
   */
  public long getLength() throws IOException,
                                 OSCIException
  {
    if (length > -1)
      return length;

    de.osci.helper.NullOutputStream out = new de.osci.helper.NullOutputStream();
    writeXML(out);
    out.close();
    length = out.getLength();

    return length;
  }

  /**
   * undocumented
   *
   * @param out undocumented
   *
   * @throws IOException undocumented
   * @throws de.osci.osci12.OSCIException undocumented
   */
  protected abstract void writeXML(OutputStream out) throws IOException,
                                                            de.osci.osci12.OSCIException;
}
