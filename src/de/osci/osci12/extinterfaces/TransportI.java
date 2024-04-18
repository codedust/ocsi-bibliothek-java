package de.osci.osci12.extinterfaces;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;


/**
 * Interface-Klasse für das Übermitteln von OSCI-Nachrichten
 * Die OSCI-Bibliothek sieht nicht vor, den Nutzer auf ein Transportprotokoll
 * festzulegen. Aus diesem Grund wird dieses Transport-Interface zur Verfügung
 * gestellt, welches es der Anwendung ermöglicht, die erstellten OSCI-Nachrichten
 * mit dem gewünschtem Protokoll oder auf die gewünschte Art zu übermitteln oder
 * zu speichern. Vorstellbare Implementierungen sind z.B. http, https, ftp,
 * smtp/pop, Filesystem oder jms.
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
 */
public interface TransportI
{
  /**
   * Liefert die Versionsnummer.
   * @return Versionsnummer
   */
  public String getVersion();

  /**
   * Sollte den Namen des Herstellers zurückgeben.
   * @return Herstellername
   */
  public String getVendor();

  /**
   * Die Implementierung dieser statischen Methode muss eine neue Instanz der Klasse
   * zurückgeben.
   * @return neue Instanz der implementierenden Klasse
   * @throws IOException wenn ein Fehler auftritt
   */
  public TransportI newInstance() throws IOException;

  /**
   * Liefert den Response-Stream.
   *
   * @return den InputStream der eingehenden Antwortdaten
   * @throws IOException InputStream
   */
  public InputStream getResponseStream() throws IOException;

  /**
   * Methode kann zur Überprüfung der Erreichbarkeit einer URL implementiert
   * und verwendet werden. Die Bibliothek selbst ruft diese Methode nicht auf.
   *
   *@param  uri              URI des Kommunikationspartners
   *@return                  true, wenn der Kommunikationspartner erreichbar ist
   *@exception  IOException  im Fehlerfall
   */
  public boolean isOnline(URI uri) throws IOException;

  /**
   *  Sollte die Länge des Response Streams liefern. Auch diese Methode wird
   * von der Bibliothek z.Zt. nicht benötigt.
   *
   *@return Anzahl der empfangenen Bytes oder <code>-1</code>, falls die Länge nicht bekannt ist.
   */
  public long getContentLength();

  /**
   *  Liefert eine konkrete Verbindung zum Versenden eines Streams.
   *  Die Methode konnektet zu der übergebenen URI und liefert als Ergebnis
   *  einen Outputstream, in den die Bibliothek dann die serialisierte OSCI-Nachricht
   *  schreibt.
   *
   *@param  uri              URI des Kommunikationspartners
   *@param  length           Länge der Übertragungsdaten (Anz d. Bytes)
   *@return                  Output-Stream, in den die Daten geschrieben werden können
   *@exception  IOException  im Fehlerfall
   */
  public OutputStream getConnection(URI uri, long length)
                             throws IOException;
}
