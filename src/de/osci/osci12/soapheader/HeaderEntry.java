package de.osci.osci12.soapheader;

/**
 * <p>Copyright © 2023 Governikus GmbH &amp; Co. KG, Germany</p>
 * <p>Erstellt von Governikus GmbH &amp; Co. KG</p>
 * <p>Diese Bibliothek kann von jedermann nach Maßgabe der European Union
 *  Public Licence genutzt werden.</p><p>Die Lizenzbestimmungen können
 * unter der URL <a href="https://eupl.eu/">https://eupl.eu/</a> abgerufen werden.
 *
 *
 * @author N. Büngener
 * @version 2.4.1
 */
public abstract class HeaderEntry extends de.osci.osci12.messageparts.MessagePart
{
  //  private static Log log = LogFactory.getLog(HeaderEntry.class);
  protected HeaderEntry()
  {
    // TODO: why do not use getClass().getSimpleName()?
    // TODO: why omit the last character?
    id = getClass().getName().substring(getClass().getName().lastIndexOf('.') + 1, getClass().getName().length() - 1)
         .toLowerCase();
    transformers.add(can);
  }
}
