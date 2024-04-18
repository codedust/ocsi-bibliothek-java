package de.osci.osci12.common;

import de.osci.osci12.OSCIException;


/**
 * Diese Exception zeigt den Abbruch einer Aktion (Signier- oder Entschlüsselungsvorgänge)
 * durch den Benutzer an.
 *
 * <p>Copyright © 2023 Governikus GmbH &amp; Co. KG, Germany</p>
 * <p>Erstellt von Governikus GmbH &amp; Co. KG</p>
 * <p>Diese Bibliothek kann von jedermann nach Maßgabe der European Union
 *  Public Licence genutzt werden.</p>
 * <p>
 * Die Lizenzbestimmungen können unter der URL <a href="https://eupl.eu/">https://eupl.eu/</a> abgerufen werden.
 * </p>
 *
 *
 * @author R. Lindemann, N.Büngener
 * @version 2.4.1
 */
public class OSCICancelledException extends OSCIException
{
  private static final long serialVersionUID = 1L;
}
