package de.osci.helper;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.security.NoSuchAlgorithmException;
import java.util.Hashtable;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;


/**
 * Diese Klasse führt die Kanonisierung gemäß der Spezifikation
 * http://www.w3.org/TR/xml-c14n durch. Die Funktion beschränkt
 * sich auf die Anforderungen der OSCI 1.2 Transportbibliothek.
 *
 * <p>Copyright © 2023 Governikus GmbH &amp; Co. KG, Germany</p>
 * <p>Erstellt von Governikus GmbH &amp; Co. KG</p>
 * <p>Diese Bibliothek kann von jedermann nach Maßgabe der European Union
 *  Public Licence genutzt werden.</p><p>Die Lizenzbestimmungen können
 * unter der URL <a href="https://eupl.eu/">https://eupl.eu/</a> abgerufen werden.
 *
 *
 * @author H. Tabrizi / N. Büngener
 * @version 2.4.1
 */
public class Canonizer extends FilterInputStream
{
  //  private static Log log = LogFactory.getLog(Canonizer.class);
  // Constants
  /** Namespaces feature id (http://xml.org/sax/features/namespaces). */
  protected static final String NAMESPACES_FEATURE_ID = "http://xml.org/sax/features/namespaces";
  /** Namespace-Prefix feature id (http://xml.org/sax/features/namespaces). */
  protected static final String NAMESPACE_PREFIXES_FEATURE_ID = "http://xml.org/sax/features/namespace-prefixes";
  /** Validation feature id (http://xml.org/sax/features/validation). */
  protected static final String VALIDATION_FEATURE_ID = "http://xml.org/sax/features/validation";
  /** Schema validation feature id (http://apache.org/xml/features/validation/schema). */
  protected static final String SCHEMA_VALIDATION_FEATURE_ID = "http://apache.org/xml/features/validation/schema";
  /** Schema full checking feature id (http://apache.org/xml/features/validation/schema-full-checking). */
  protected static final String SCHEMA_FULL_CHECKING_FEATURE_ID = "http://apache.org/xml/features/validation/schema-full-checking";
  /** Entity External General checking feature id (http://apache.org/xml/features/validation/schema-full-checking). */
  protected static final String EXTERNAL_GENERAL_ENTITIES_FEATURE_ID = "http://xml.org/sax/features/external-general-entities";
  /** Entity External Parameter checking feature id (http://apache.org/xml/features/validation/schema-full-checking). */
  protected static final String EXTERNAL_PARAMETER_ENTITIES_FEATURE_ID = "http://xml.org/sax/features/external-parameter-entities";

  // Data
  private PipedInputStream pis;
  private PipedOutputStream pos;
  private Exception canException;
  private InputStream input;
  private CanParser cp;

  /**
   * Creates a new Canonizer object.
   *
   * @param in undocumented
   * @param sis undocumented
   *
   * @throws IOException undocumented
   * @throws SAXException undocumented
   * @throws ParserConfigurationException undocumented
   * @throws NoSuchAlgorithmException undocumented
   */
  public Canonizer(InputStream in, StoreInputStream sis)
            throws IOException,
                   SAXException,
                   ParserConfigurationException,
                   NoSuchAlgorithmException
  {
    this(in,sis,true);
  }
  /**
   * Creates a new Canonizer object.
   *
   * @param in undocumented
   * @param sis undocumented
   *
   * @throws IOException undocumented
   * @throws SAXException undocumented
   * @throws ParserConfigurationException undocumented
   * @throws NoSuchAlgorithmException undocumented
   */
  public Canonizer(InputStream in, StoreInputStream sis,boolean checkIds)
            throws IOException,
                   SAXException,
                   ParserConfigurationException,
                   NoSuchAlgorithmException
  {
    super(in);
    input = in;
    pis = new PipedInputStream();
    pos = new PipedOutputStream(pis);
    cp = new CanParser(pos, sis,checkIds);
    cp.signedInfos = new Vector<byte[]>();
    cp.signedProperties = new Vector<String>();
    cp.cocoNS = new Vector<String>();
    canException = null;
    new Thread()
      {
        public void run()
        {
          try
          {
            cp.startCanonicalization(input, false);
            pos.close();
          }
          catch (SAXException e)
          {
            canException = e.getException();
          }
          catch (Exception e)
          {
              canException = e;
          }
          finally
          {
            try
            {
              pos.close();
            }
            catch (IOException ex)
            {
              // Keine Aktion, alles zu spät....
            }
          }
        }
      }.start();
  }

  /**
   * undocumented
   *
   * @return undocumented
   *
   * @throws java.io.IOException undocumented
   */
  public int read() throws java.io.IOException
  {
    /*
       byte[] d = new byte[1];
       if (read(d, 0, 1) == -1)
         return -1;
       return ((int)d[0]) & 0xff;
     */
    return pis.read();
  }

  /**
   * undocumented
   *
   * @param b undocumented
   * @param off undocumented
   * @param len undocumented
   *
   * @return undocumented
   *
   * @throws IOException undocumented
   */
  public int read(byte[] b, int off, int len) throws IOException
  {
    return pis.read(b, off, len);
  }

  /**
   * undocumented
   *
   * @return undocumented
   */
  public Hashtable<String, byte[]> getDigestValues()
  {
    return cp.digestValues;
  }

  /**
   * undocumented
   *
   * @return undocumented
   */
  public Vector<byte[]> getSignedInfos()
  {
    return cp.signedInfos;
  }

  /**
   * undocumented
   *
   * @return undocumented
   */
  public Vector<String> getSignedProperties()
  {
    return cp.signedProperties;
  }

  /**
   * undocumented
   *
   * @return undocumented
   */
  public Vector<String> getContainerNS()
  {
    return cp.cocoNS;
  }

  /**
   * undocumented
   *
   * @return undocumented
   */
  public Exception getCanException()
  {
    return canException;
  }

}
