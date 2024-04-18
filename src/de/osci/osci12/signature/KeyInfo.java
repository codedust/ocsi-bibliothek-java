package de.osci.osci12.signature;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.osci.osci12.OSCIException;
import de.osci.osci12.common.Constants;
import de.osci.osci12.encryption.EncryptedKey;


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
public class KeyInfo
{
  private static Log log = LogFactory.getLog(KeyInfo.class);
  private String keyName;
  /*
     private static int KEY_IS_ENCRYPTEDKEY = 0;
     private static int KEY_IS_RETRIEVALMETHOD = 1;
     private static int KEY_IS_X509DATA = 2;
   */
  private int typeOfKey = -1;
  private RetrievalMethod retrievalMethod;
  X509Data x509Data = null;
  private String mgmtData = null;
  private String keyValue;
  /** Liste enthaltener Encryptedkey Elemente. */
  private Vector<EncryptedKey> encryptedKey = new Vector<EncryptedKey>();
  private String agreementMethod;
  private String id;

  /**
   * Creates a new KeyInfo object.
   *
   * @param id
   * @param uRI
   */
  public KeyInfo(String uRI, String id)
  {
    this.id = id;
    this.typeOfKey = 1;

    if (log.isDebugEnabled())
      log.debug("Konstruktortype: RetrievelMethod");

    RetrievalMethod retrievalMethod = new RetrievalMethod();
    retrievalMethod.setURI(uRI);
    this.retrievalMethod = retrievalMethod;
  }

  /**
   * Creates a new KeyInfo object.
   *
   * @param uRI undocumented
   */
  public KeyInfo(String uRI)
  {
    this.typeOfKey = 1;

    if (log.isDebugEnabled())
      log.debug("Konstruktortype: RetrievelMethod");

    RetrievalMethod retrievalMethod = new RetrievalMethod();
    retrievalMethod.setURI(uRI);
    this.retrievalMethod = retrievalMethod;
  }

  /**
   * Creates a new KeyInfo object.
   */
  public KeyInfo()
  {
    if (log.isDebugEnabled())
      log.debug("KeyInfo Konstruktortype noch nicht definiert.");
  }

  /**
   * Creates a new KeyInfo object.
   *
   * @param cert undocumented
   */
  public KeyInfo(java.security.cert.X509Certificate cert)
  {
    typeOfKey = 2;

    if (log.isDebugEnabled())
      log.debug("Konstruktortype: X509Certificate Data");

    x509Data = new X509Data(cert);
  }

  /**
   * undocumented
   *
   * @param keyName undocumented
   */
  public void setKeyName(String keyName)
  {
    this.keyName = keyName;
  }

  /**
   * undocumented
   *
   * @return undocumented
   */
  public String getKeyName()
  {
    return keyName;
  }

  /**
   * undocumented
   *
   * @param retrievalMethod undocumented
   */
  public void setRetrievalMethod(RetrievalMethod retrievalMethod)
  {
    if (typeOfKey > -1)
      throw new IllegalStateException();

    typeOfKey = 1;
    this.retrievalMethod = retrievalMethod;
  }

  /**
   * undocumented
   *
   * @return undocumented
   */
  public RetrievalMethod getRetrievalMethod()
  {
    return retrievalMethod;
  }

  /**
   * undocumented
   *
   * @param keyValue undocumented
   */
  public void setKeyValue(String keyValue)
  {
    this.keyValue = keyValue;
  }

  /**
   * undocumented
   *
   * @return undocumented
   */
  public String getKeyValue()
  {
    return keyValue;
  }

  /**
   * undocumented
   *
   * @param encryptedKey undocumented
   */
  public void addEncryptedKey(EncryptedKey encryptedKey)
  {
    this.encryptedKey.add(encryptedKey);
  }

  /**
   * undocumented
   *
   * @return undocumented
   */
  public EncryptedKey[] getEncryptedKeys()
  {
    return encryptedKey.toArray(new EncryptedKey[0]);
  }

  /**
   * undocumented
   *
   * @param agreementMethod undocumented
   */
  public void setAgreementMethod(String agreementMethod)
  {
    this.agreementMethod = agreementMethod;
  }

  /**
   * undocumented
   *
   * @return undocumented
   */
  public String getAgreementMethod()
  {
    return agreementMethod;
  }

  /**
   * undocumented
   *
   * @param id undocumented
   */
  public void setId(String id)
  {
    this.id = id;
  }

  /**
   * undocumented
   *
   * @return undocumented
   */
  public String getId()
  {
    return id;
  }

  /**
   * undocumented
   *
   * @param out undocumented
   *
   * @throws IOException undocumented
   * @throws OSCIException undocumented
   */
  public void writeXML(OutputStream out, String ds, String xenc)
                throws IOException,
                       OSCIException
  {
    out.write(("<" + ds + ":KeyInfo").getBytes(Constants.CHAR_ENCODING));

    if (this.id != null)
      out.write((" Id=\"" + this.id + "\">").getBytes(Constants.CHAR_ENCODING));
    else
      out.write(0x3e);

    if (this.encryptedKey.size() > 0)
    {
      for (int i = 0; i < this.encryptedKey.size(); i++)
        this.encryptedKey.get(i).writeXML(out, ds, xenc);
    }

    if (this.retrievalMethod != null)
    {
      //id
      out.write(("<" + ds + ":RetrievalMethod Type=\"" + this.getRetrievalMethod().getType() + "\" URI=\"" +
                this.getRetrievalMethod().getURI() + "\"></" + ds + ":RetrievalMethod>").getBytes(Constants.CHAR_ENCODING));
    }

    if (x509Data != null)
    {
      x509Data.writeXML(out, ds);
    }
    else if (mgmtData != null)
    {
      out.write(("<" + ds + ":MgmtData>" + mgmtData + "</" + ds + ":MgmtData>").getBytes(Constants.CHAR_ENCODING));
    }

    out.write(("</" + ds + ":KeyInfo>").getBytes(Constants.CHAR_ENCODING));
  }

  /**
   * undocumented
   *
   * @return undocumented
   */
  public int getTypeOfKey()
  {
    return typeOfKey;
  }

  /**
   * undocumented
   *
   * @return undocumented
   */
  public X509Data getX509Data()
  {
    return x509Data;
  }

  /**
   * undocumented
   *
   * @return undocumented
   */
  public String getMgmtData()
  {
    return mgmtData;
  }

  /**
   * undocumented
   *
   * @param mgmtData undocumented
   */
  public void setMgmtData(String mgmtData)
  {
    this.mgmtData = mgmtData;
  }
}
