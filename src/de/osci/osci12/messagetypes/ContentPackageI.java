package de.osci.osci12.messagetypes;

import de.osci.osci12.messageparts.Content;
import de.osci.osci12.messageparts.ContentContainer;
import de.osci.osci12.messageparts.EncryptedDataOSCI;
import eu.osci.ws._2014._10.transport.MessageMetaData;


/**
 * Dieses Interface wird von allen Nachrichtentypen implementiert, die
 * Inhaltsdaten enthalten.
 *
 * <p>Copyright © 2023 Governikus GmbH &amp; Co. KG, Germany</p>
 * <p>Erstellt von Governikus GmbH &amp; Co. KG</p>
 * <p>Diese Bibliothek kann von jedermann nach Maßgabe der European Union
 *  Public Licence genutzt werden.</p><p>Die Lizenzbestimmungen können
 * unter der URL <a href="https://eupl.eu/">https://eupl.eu/</a> abgerufen werden.
 *
 *
 * @author R. Lindemann / N. Büngener
 * @version 2.4.1
 */
public interface ContentPackageI
{
  /**
   * Liefert die Message-ID der Nachricht (Antwort).
   * @return Message-ID
   * @since 1.0.3
   */
  public String getMessageId();

  /**
   * Liefert den im Antwortlaufzettel enthaltenen Betreff-Eintrag.
   * @return den Betreff der Antwortnachricht
   * @since 1.0.3
   */
  public String getSubject();

  /**
   * Liefert die in die Nachricht eingestellten (unverschlüsselten) Inhaltsdaten als ContentContainer-Objekte.
   * @return enthaltene ContentContainer mit Inhaltsdaten
   * @see de.osci.osci12.messageparts.ContentContainer
   */
  public ContentContainer[] getContentContainer();

  /**
   * Durchsucht <b>die unverschlüsselten</b> Inhaltsdaten nach dem ContentContainer
   * mit der übergebenen RefID.
   * @param refID zu suchende RefID
   * @return den zugehörigen ContentContainer oder null, wenn die Referenz
   * nicht gefunden wurde.
   */
  public ContentContainer getContentContainerByRefID(String refID);

  /**
   * Durchsucht <b>die unverschlüsselten</b> ContentContainer nach dem Content
   * mit der übergebenen RefID.
   * @param refID zu suchende RefID
   * @return den zugehörigen Content oder null, wenn die Referenz
   * nicht gefunden wurde.
   */
  public Content getContentByRefID(String refID);

  /**
   * Liefert die in die Nachricht eingestellten verschlüsselten Inhaltsdaten
   * als EncryptedData-Objekte.
   * @return enthaltene EncryptedData-Objekt mit verschlüsselten Inhaltsdaten
   * @see EncryptedDataOSCI
   */
  public EncryptedDataOSCI[] getEncryptedData();

  /**
   * Liefert den in die Nachricht eingestellten MessageMetaData-Header (MMD gemäß XTA2 V3). Dieser enthält
   * den XTA-Transportauftrag mit allen Metadaten, die benötigt werden, um den XTA-Nachrichtentransport
   * zu beauftragen und durchzuführen.
   * @return enthaltenes {@link MessageMetaData}-Objekt
   * @see OSCIMessage#addMessageMetaDataXTA2V3(MessageMetaData)
   */
  public MessageMetaData getMessageMetaDataXTA2V3();
}
