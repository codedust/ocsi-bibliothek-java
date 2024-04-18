package de.osci.osci12.extinterfaces;

/**
 * Diese Erweiterung der OSCIDataSource-Klasse wurde mit der Version 1.2.3
 * eingeführt. Das zusätzliche Flag "confidential" informiert die Implementierung
 * darüber, ob die zu speichernden Daten ggf. durch Zugriffsbeschränkungen
 * oder Verschlüsselung geschützt werden sollten. Die OSCI-Bibliothek setzt
 * dieses Flag beim Anlegen von Content-Daten direkt nach der Instanziierung auf true.
 * Falls die Implementierung die Daten verschlüsselt, kann hierzu der 128-Bit-AES-Key
 * verwendet werden, der im DialogHandler als statische Variable bereitgestellt
 * wird. <b>Hinweis:</b> Unverschlüsslte Attachments werden als nicht sicherheitskritisch
 * betrachtet. Es steht der Implmentierung frei, diese ebenfalls zu schützen.
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
 * @see de.osci.osci12.common.DialogHandler#getTempSymKey()
 */
public abstract class OSCIDataSourceExt123 extends OSCIDataSource
{
  protected boolean confidential = false;

  /**
   * Wird bei ggf. vertraulichen Daten von der Bibliothek mit "true"
   * aufgerufen.
   *
   * @param isConfidential vertrauliche Daten
   */
  public void setConfidential(boolean isConfidential)
  {
    confidential = isConfidential;
  }
}
