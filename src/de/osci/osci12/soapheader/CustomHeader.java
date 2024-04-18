package de.osci.osci12.soapheader;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.osci.osci12.common.Constants;
import de.osci.osci12.common.Constants.LanguageTextEntries;


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
public class CustomHeader extends HeaderEntry
{
  private static Log log = LogFactory.getLog(CustomHeader.class);
  private String data;

  /**
   *  Constructor for the CustomHeader object
   *
   *@param  data  Description of Parameter
   */
  public CustomHeader(String data)
  {
    int i = data.indexOf(" Id=");

    if (i == -1)
      i = data.indexOf(" Id =");

    if ((i == -1) || (i > data.indexOf('>')))
      throw new IllegalArgumentException(de.osci.osci12.common.DialogHandler.text.getString(LanguageTextEntries.missing_entry.name()) + ": Id");
    else
      i = data.indexOf('\"', i) + 1;

    setRefID(data.substring(i, data.indexOf('\"', i)));
    this.data = data;
    //    setRefID(name.toLowerCase());
  }

  /**
   * undocumented
   *
   * @return undocumented
   */
  public String getData()
  {
    return data;
  }

  /**
   *  Description of the Method
   *
   *@param  out              Description of Parameter
   *@exception  IOException  Description of Exception
   */
  protected void writeXML(OutputStream out) throws IOException
  {
    if (log.isDebugEnabled())
      log.debug("Custom RefID: " + getRefID());

    out.write(data.getBytes(Constants.CHAR_ENCODING));
  }
}
