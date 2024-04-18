package de.osci.osci12.extinterfaces;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * Implementierungen dieser Klasse können mit Hilfe der Methode
 * setDataBuffer(OSCIDataSource buffer) des DialogHandlers installiert
 * werden, falls Inhaltsdaten nicht durch die default-Implementierung
 * SwapBuffer im Arbeitsspeicher bzw. in temporären Dateien gepuffert werden
 * sollen, sondern beispielsweise in einer Datenbank.<br>
 * Dieser Puffer-Mechanismus wird von den Klassen EncryptedData, Content und Attachment
 * genutzt.
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
 * @see de.osci.osci12.common.SwapBuffer
 */
public abstract class OSCIDataSource
{
  /**
   * Die Implementierung dieser statischen Methode muss eine neue Instanz der Klasse
   * zurückgeben.
   * @return neue Instanz der implementierenden Klasse
   * @throws IOException wenn ein Fehler auftritt
   */
  public abstract OSCIDataSource newInstance() throws IOException;

  /**
   * Die Implementierung dieser Methode muss einen OutputStream liefern, in den
   * die zu puffernden Daten geschrieben werden können.
   * @return den OutputStream
   * @throws IOException wenn ein Fehler auftritt
   */
  public abstract OutputStream getOutputStream() throws IOException;

  /**
   * Die Implementierung dieser Methode muss einen InputStream liefern, aus dem
   * die gepufferten Daten gelesen werden können. Der erste Aufruf dieser
   * Methode beendet den Schreibvorgang in diesen Puffer.
   *
   * <b>Achtung:</b> Der zurückgegebene InputStream muss die reset()-Methode
   * in der Weise implementieren, dass nach deren Aufruf wieder von vorn ab dem
   * ersten Byte gelesen wird. Die markSupported()-Methode muss
   * <b>false</b> zurückliefern.
   * @return den InputStream
   * @throws IOException wenn ein Fehler auftritt
   * @see java.io.InputStream#reset()
   * @see java.io.InputStream#markSupported()
   */
  public abstract InputStream getInputStream() throws IOException;

  /**
   * Diese Methode muss die Anzahl der in den Puffer geschriebenen Bytes
   * zurückgeben.
   * @return Anzahl der Bytes
   */
  public abstract long getLength();

  /**
   * Liefert eine Versionsnummer.
   * @return Versionsnummer
   */
  public abstract String getVersion();

  /**
   * Liefert den Namen des Herstellers.
   * @return Herstellername
   */
  public abstract String getVendor();
}
