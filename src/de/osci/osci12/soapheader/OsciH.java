package de.osci.osci12.soapheader;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.osci.osci12.common.Constants;


/**
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
public class OsciH extends HeaderEntry
{

  private static Log log = LogFactory.getLog(OsciH.class);

  String name;

  String data;

  String namespace = osciNSPrefix;

  /**
   * Constructor for the OsciH object
   *
   * @param name Description of Parameter
   * @param data Description of Parameter
   */
  public OsciH(String name, String data)
  {
    this.name = name;
    this.data = data;
  }

  /**
   * Constructor for the OsciH object
   *
   * @param name Description of Parameter
   * @param data Description of Parameter
   */
  public OsciH(String name, String data, String namespace)
  {
    this.name = name;
    this.data = data;
    this.namespace = namespace;
  }

  /**
   * Gets the refID attribute of the OsciH object
   *
   * @return The refID value
   */
  public String getRefID()
  {
    return name.toLowerCase();
  }

  /**
   * Description of the Method
   *
   * @param out Description of Parameter
   * @exception IOException Description of Exception
   */
  public void writeXML(OutputStream out) throws IOException
  {
    if (log.isDebugEnabled())
      log.debug("RefID: " + getRefID());

    out.write(("<" + namespace + ":" + name).getBytes(Constants.CHAR_ENCODING));
   if (namespace.equals(osciNSPrefix))
    {
      out.write(ns);
    }
    else
    {
      out.write(ns2017);
    }
    out.write((" Id=\"" + getRefID() + "\" " + soapNSPrefix
               + ":actor=\"http://schemas.xmlsoap.org/soap/actor/next\" " + soapNSPrefix
               + ":mustUnderstand=\"1\">").getBytes(Constants.CHAR_ENCODING));
    out.write(data.getBytes(Constants.CHAR_ENCODING));
    out.write(("</" + namespace + ":" + name + ">").getBytes(Constants.CHAR_ENCODING));
  }
}
