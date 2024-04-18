package de.osci.osci12.samples.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URLConnection;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.osci.osci12.extinterfaces.TransportI;


/**
 * Beispiel-Implementierung eines Transport-Interfaces.
 *
 * <p>Copyright © 2023 Governikus GmbH &amp; Co. KG, Germany</p>
 * <p>Erstellt von Governikus GmbH &amp; Co. KG</p>
 * <p>Diese Bibliothek kann von jedermann nach Maßgabe der European Union
 *  Public Licence genutzt werden.</p><p>Die Lizenzbestimmungen können
 * unter der URL <a href="https://eupl.eu/">https://eupl.eu/</a> abgerufen werden.
 *
 *
 * @author R. Lindemann, N. Büngener
 * @version 2.4.1
 * @see de.osci.osci12.extinterfaces.TransportI
 */
public class HttpTransport implements TransportI
{
  private static Log log = LogFactory.getLog(HttpTransport.class);
  URLConnection con;
  ResourceBundle text = ResourceBundle.getBundle("de.osci.osci12.extinterfaces.language.Text", Locale.getDefault());

  /**
   * Creates a new HttpTransport object.
   */
  public HttpTransport()
  {
  }

  /**
   * Liefert die Versionsnummer
   * @return Versionsnummer
   */
  public String getVendor()
  {
    return "BOS";
  }

  /**
   * undocumented
   *
   * @return undocumented
   */
  public String getVersion()
  {
    return "0.9";
  }

  /**
   * undocumented
   *
   * @return undocumented
   */
  public TransportI newInstance()
  {
    return new HttpTransport();
  }

  /**
   * undocumented
   *
   * @return undocumented
   *
   * @throws IOException undocumented
   */
  public InputStream getResponseStream() throws IOException
  {
    return con.getInputStream();
  }

  /**
   * undocumented
   *
   * @param uri undocumented
   *
   * @return undocumented
   *
   * @throws IOException undocumented
   */
  public boolean isOnline(java.net.URI uri) throws IOException
  {
    try
    {
      con = uri.toURL().openConnection();
      con.connect();

      return true;
    }
    catch (MalformedURLException ex)
    {
      throw new IOException(text.getString("invalid_url") + ": " + ex.getLocalizedMessage());
    }
    catch (IOException ex)
    {
      return false;
    }
  }

  /**
   * undocumented
   *
   * @return undocumented
   */
  public long getContentLength()
  {
    /**@todo Diese de.osci.osci12.extinterfaces.transport.TransportI-Methode implementieren*/
    throw new java.lang.UnsupportedOperationException("Methode getContentLength() noch nicht implementiert.");
  }

  /**
   * undocumented
   *
   * @param uri undocumented
   * @param laenge undocumented
   *
   * @return undocumented
   *
   * @throws IOException undocumented
   */
  public OutputStream getConnection(java.net.URI uri, long laenge) throws IOException {
      try
      {
    	con = uri.toURL().openConnection();
        HttpURLConnection httpCon = (HttpURLConnection) con;

        httpCon.setInstanceFollowRedirects(false);
        httpCon.setRequestMethod("POST");
        httpCon.setRequestProperty("Content-Type", "text/xml");
        httpCon.setRequestProperty("charset", "utf-8");
        httpCon.setRequestProperty("Content-Length", Long.toString(laenge));
        httpCon.setUseCaches(false);
        httpCon.setDoOutput(true);

        OutputStream s = httpCon.getOutputStream();

        return s;
      }
      catch (MalformedURLException ex) {
        throw new IOException(text.getString("invalid_url") + ": " + ex.getLocalizedMessage());
      }
  }
}
