package de.osci.osci12.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;

import javax.crypto.SecretKey;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.osci.helper.SymCipherInputStream;
import de.osci.helper.SymCipherOutputStream;
import de.osci.osci12.encryption.Crypto;
import de.osci.osci12.extinterfaces.OSCIDataSource;
import de.osci.osci12.extinterfaces.OSCIDataSourceExt123;


/**
 * Diese Klasse stellt die Standard-Implementierung der abstrakten
 * OSCIDataSource-Klasse dar. Diese Implementierung puffert Inhaltsdaten
 * bis zu einer konfigurierbaren Anzahl von Bytes im Arbeitsspeicher.
 * Wird diese Anzahl überschritten, werden die gepufferten Bytes wie alle
 * folgenden Bytes in eine temporäre Datei geschrieben.<br><br>
 * Zur Dokumentation der Methoden s. OSCIDataSource.
 *
 * <p>Copyright © 2023 Governikus GmbH &amp; Co. KG, Germany</p>
 * <p>Erstellt von Governikus GmbH &amp; Co. KG</p>
 * <p>Diese Bibliothek kann von jedermann nach Maßgabe der European Union
 *  Public Licence genutzt werden.</p>
 * <p>
 * Die Lizenzbestimmungen können
 * unter der URL <a href="https://eupl.eu/">https://eupl.eu/</a> abgerufen werden.
 *
 *
 * @author R. Lindemann, N.Büngener
 * @version 2.4.1
 * @see de.osci.osci12.extinterfaces.OSCIDataSource
 * @see de.osci.osci12.extinterfaces.OSCIDataSourceExt123
 */
public class SwapBuffer extends OSCIDataSourceExt123
{
  private static Log log = LogFactory.getLog(SwapBuffer.class);
  private InputStream is;
  private OutputStream os;
  private SwapBufferInputStream dbis;
  private SwapBufferOutputStream dbos;
  private byte[] buffer;
  private File file;
  private static SecretKey tempKey;
  private static final String SYM_CIPHER_ALGO= Constants.DEFAULT_SYMMETRIC_CIPHER_ALGORITHM;
  /**
   * Limit für die Anzahl von Bytes, die im Arbeitsspeicher gepuffert werden,
   * bevor in eine temporäre Datei geswapt wird. Als Voreinstellung wird dieser Wert
   * auf 1 % des (beim ersten Laden dieser Klasse) verfügbaren freien Arbeitsspeichers
   * gesetzt.
   */
  public static long maxBufferSize = Runtime.getRuntime().freeMemory() / 100;
  private static File tmpDir = new File(System.getProperty("java.io.tmpdir"));
  private long byteCount;

  /**
   * Creates a new SwapBuffer object.
   */
  public SwapBuffer()
  {
    os = new ByteArrayOutputStream();
    dbos = new SwapBufferOutputStream();
    byteCount = 0;
    maxBufferSize = Runtime.getRuntime().freeMemory() / 100;
  }

  /**
   * Setzt den Pfad zum Verzeichnis für temporäre Dateien.
   * Bei erhöhten Sicherheitsanforderungen kann hier z.B. ein
   * Verzeichnis mit eingeschränkten Zugriffsrechten oder
   * in einem verschlüsslten Dateisystem angegeben werden.
   * @param dir undocumented
   */
  public static void setTmpDir(String dir)
  {
    tmpDir = new File(dir);
  }

  /**
   * undocumented
   *
   * @return undocumented
   *
   * @throws IOException undocumented
   */
  public OSCIDataSource newInstance() throws IOException
  {
    return new SwapBuffer();
  }

  private void resetInputStream() throws IOException
  {
    if (dbos != null)
    {
      dbos.close();
      dbos = null;
    }

    if (is != null)
    {
      try
      {
        is.close();
      }
      catch (NegativeArraySizeException ex)
      {
        log.warn("Problem beim Schließen des InputStreams, trotzdem weiter machen");
        log.debug("Fehlermeldung: " + ex.getMessage(), ex);
      }
    }

    is = null;
    byteCount = 0;

    if (buffer != null)
      is = new ByteArrayInputStream(buffer);
    else if (confidential)
    {
      try
      {
        is = new SymCipherInputStream(new FileInputStream(file), getTempSymKey(), SYM_CIPHER_ALGO, false);
      }
      catch (NoSuchAlgorithmException nsae)
      {
        // Wurde schon beim Schreiben getestet
      }
    }
    else
      is = new FileInputStream(file);

    dbis = new SwapBufferInputStream();
  }

  /**
   * undocumented
   *
   * @throws Throwable undocumented
   */
  protected void finalize() throws Throwable
  {
    try
    {
      deleteTempFile();
    }
    catch (IOException e)
    {
      log.error("Failed to delete tmp-file: ", e);
    }

    super.finalize();
  }

  protected void deleteTempFile() throws IOException
  {
    if (file != null)
    {
        Files.delete(file.toPath());
    }
  }
  /**
   * undocumented
   *
   * @return undocumented
   *
   * @throws IOException undocumented
   */
  public InputStream getInputStream() throws IOException
  {
    if (is == null)
      resetInputStream();

    return dbis;
  }

  /**
   * undocumented
   *
   * @return undocumented
   *
   * @throws IOException undocumented
   * @throws IllegalStateException undocumented
   */
  public OutputStream getOutputStream() throws IOException
  {
    if (os == null)
    {
      log.error("SwapBuffer ist bereits im Lesemodus, kein OutputStream verfügbar.");
      throw new IllegalStateException();
    }

    return dbos;
  }

  class SwapBufferOutputStream extends OutputStream
  {
    private FileOutputStream fos;

    public void write(byte[] b, int off, int len) throws IOException
    {
      if (is != null)
      {
        log.error("DataBuffer ist bereits im Lesemodus, kann nicht beschrieben werden.");
        throw new IllegalStateException();
      }

      if ((os instanceof ByteArrayOutputStream) && ((byteCount + len) > maxBufferSize))
      {
        if (log.isDebugEnabled())
          log.debug("SWAPPE AUF PLATTE");

        String time = Long.toString(System.currentTimeMillis());
        time = time.substring(time.length() - 4);
        file = java.io.File.createTempFile(time, null, tmpDir);
        file.deleteOnExit();
        flush();
        buffer = ((ByteArrayOutputStream) os).toByteArray();

        if (confidential)
        {
          try
          {
            fos = new FileOutputStream(file);
            os = new SymCipherOutputStream(fos,getTempSymKey(),SYM_CIPHER_ALGO, true);
          }
          catch (NoSuchAlgorithmException nsae)
          {
            log.warn("Verwendeter security provider unterstützt nicht den erforderlichen Algoritmus, " +
                     "Daten werden unverschlüsselt gespeichert !\n" + nsae.getMessage(),nsae);
            confidential = false;
            os = new FileOutputStream(file);
          }
        }
        else
          os = new FileOutputStream(file);

        os.write(buffer);
        buffer = null;
      }

      os.write(b, off, len);
      byteCount += len;
    }

    public void write(int b) throws IOException
    {
      this.write(new byte[] { (byte) b });
    }

    public void flush() throws IOException
    {
      os.flush();
    }

    public void close() throws IOException
    {
      if (os == null)
        return;

      os.flush();
      os.close();

      if (fos != null)
      {
        fos.close();
        fos = null;
      }

      if (os instanceof ByteArrayOutputStream)
        buffer = ((ByteArrayOutputStream) os).toByteArray();

      os = null;
    }
  }

  class SwapBufferInputStream extends InputStream
  {
    public int read(byte[] b, int off, int len) throws IOException
    {
      if (os != null)
      {
        log.error("DataBuffer ist im Schreibmodus, kann nicht gelesen werden.");
        throw new IllegalStateException();
      }

      return is.read(b, off, len);
    }

    public int read() throws IOException
    {
      byte[] b = new byte[1];

      if (read(b, 0, 1) == -1)
        return -1;

      return ((int) b[0]) & 0xff;
    }

    public void reset() throws IOException
    {
      resetInputStream();
    }

    public void close() throws IOException
    {
      is.close();
    }

    public int available() throws IOException
    {
      return is.available();
    }
  }

  /**
   * Liefert die Anzahl der (momentan) gespeicherten Bytes.
   * @return Anzahl der Bytes
   */
  public long getLength()
  {
    return byteCount;
  }

  /**
   * Liefert eine Versionsnummer.
   * @return Versionsnummer
   */
  public String getVersion()
  {
    return "0.1";
  }

  /**
   * Liefert den Namen des Herstellers.
   * @return Herstellername
   */
  public String getVendor()
  {
    return "BOS";
  }
  private static SecretKey getTempSymKey() throws NoSuchAlgorithmException
  {
    if (tempKey == null)
      tempKey = Crypto.createSymKey(SYM_CIPHER_ALGO);

    return tempKey;
  }
}
