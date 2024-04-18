package de.osci.osci12.soapheader;

import java.io.IOException;
import java.io.OutputStream;

import org.xml.sax.SAXException;

import de.osci.osci12.common.Constants;
import de.osci.osci12.common.Constants.HeaderTags;
import de.osci.osci12.messagetypes.OSCIMessageBuilder;


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
public class DesiredLanguagesH extends HeaderEntry
{

  // private static Log log = LogFactory.getLog(DesiredLanguagesH.class);
  /** Liste der Sprachen im Format de,fr */
  private String languageList;

  /**
   * Creates a new DesiredLanguageH object.
   *
   * @param languageList undocumented
   * @param parentHandler Description of Parameter
   * @param atts Description of Parameter
   * @throws SAXException
   */
  public DesiredLanguagesH(OSCIMessageBuilder parentHandler, String id, String languageList)
    throws SAXException
  {
    parentHandler.addFoundMsgPartIds(id, HeaderTags.DesiredLanguages.getNamespace().getUri() +":"+HeaderTags.DesiredLanguages.getElementName());
    this.languageList = languageList;
  }

  /**
   * Creates a new DesiredLanguageH object.
   *
   * @param languageList undocumented
   */
  public DesiredLanguagesH(String languageList)
  {
    this.languageList = languageList;
  }

  /**
   * undocumented
   *
   * @return undocumented
   */
  public String getLanguageList()
  {
    return languageList;
  }

  /**
   * undocumented
   *
   * @param out undocumented
   * @throws IOException undocumented
   */
  public void writeXML(OutputStream out) throws IOException
  {
    out.write(("<" + osciNSPrefix + ":DesiredLanguages").getBytes(Constants.CHAR_ENCODING));
    out.write(ns);
    out.write((" Id=\"desiredlanguages\" LanguagesList=\"" + languageList + "\" " + soapNSPrefix
               + ":actor=\"http://schemas.xmlsoap.org/soap/actor/next\" " + soapNSPrefix
               + ":mustUnderstand=\"1\"></" + osciNSPrefix
               + ":DesiredLanguages>").getBytes(Constants.CHAR_ENCODING));
  }
}
