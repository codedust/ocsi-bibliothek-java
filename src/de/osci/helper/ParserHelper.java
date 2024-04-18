package de.osci.helper;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import de.osci.osci12.common.Constants.CommonTags;
import de.osci.osci12.common.Constants.HeaderTags;
import de.osci.osci12.common.Constants.SystemProperties;


/**
 * <p>
 * Copyright © 2023 Governikus GmbH &amp; Co. KG, Germany
 * </p>
 * <p>
 * Erstellt von Governikus GmbH &amp; Co. KG
 * </p>
 * <p>
 * Diese Bibliothek kann von jedermann nach Maßgabe der European Union Public Licence genutzt
 * werden.
 * </p>
 * Die Lizenzbestimmungen können unter der URL <a href="https://eupl.eu/">https://eupl.eu/</a> abgerufen werden.
 * </p>
 * 
 * @author R. Lindemann
 * @version 2.4.1
 */
public class ParserHelper
{
  private static SAXParserFactory spf;

  static
  {
    spf = SAXParserFactory.newInstance();
    spf.setNamespaceAware(true);
    spf.setValidating(false);
  }

  private static Log log = LogFactory.getLog(ParserHelper.class);

  public static void setFeatures(XMLReader reader)
  {
    // set to false
    addFeature(reader, "http://xml.org/sax/features/external-general-entities", false);
    addFeature(reader, "http://xml.org/sax/features/external-parameter-entities", false);
    addFeature(reader, "http://xml.org/sax/features/validation", false);

    // set to true
    addFeature(reader, "http://xml.org/sax/features/namespaces", true);
    addFeature(reader, "http://xml.org/sax/features/namespace-prefixes", true);

    // neue Schalter
    addFeature(reader, "http://apache.org/xml/features/disallow-doctype-decl", true);
    addFeature(reader, "http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
    addFeature(reader, "http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
    addFeature(reader, XMLConstants.FEATURE_SECURE_PROCESSING, true);
  }

  public static SAXParser getNewSAXParser() throws ParserConfigurationException, SAXException
  {
    return spf.newSAXParser();
  }

  private static void addFeature(XMLReader reader, String feature, boolean value)
  {
    try
    {
      reader.setFeature(feature, value);

    }
    catch (SAXException e)
    {
      if (log.isDebugEnabled())
        log.debug("warning: Parser does not support feature (" + feature + ")");
    }
  }


  /**
   * Prüft ob die Secure Content parsing angestellt ist. Hierbei werden die Content RefIds auf doppelte
   * Einträge überprüft.
   *
   * @return
   */
  public static boolean isSecureContentDataCheck()
  {
    if ("false".equals(System.getProperty(SystemProperties.SecureContentDataCheck.getPropertyValue())))
    {
      return false;
    }
    return true;
  }

  /**
   * Prüft ob die Secure Transport parsing angestellt ist. Hierbei werden die sämtlich RefIds auf
   * Transportebene auf doppelte Einträge überprüft.
   *
   * @return
   */
  public static boolean isSecureTransportDataCheck()
  {
    if ("false".equals(System.getProperty(SystemProperties.SecureTransportDataCheck.getPropertyValue())))
    {
      return false;
    }
    return true;
  }


  /**
   * Prüft ob der automatische Wechsel auf GCM-Modus erlaubt ist.
   *
   * @return
   */
  public static boolean isSwitchToGCM()
  {
    if ("false".equals(System.getProperty(SystemProperties.SwitchToGCM.getPropertyValue())))
    {
      return false;
    }
    return true;
  }

  /**
   * Prüft ob nur ein symmetrischer Verschlüsselungsalgorithmus mit GCM-Modus erlaubt sein soll.
   *
   * @return
   */
  public static boolean isGCMAlgorithmOnly()
  {
    return "true".equals(System.getProperty(SystemProperties.GCMAlgorithmOnly.getPropertyValue()));
  }

  public static boolean isElement(HeaderTags expectedElement, String elementName, String uri)
  {
    if (expectedElement.getElementName().equals(elementName)
        && expectedElement.getNamespace().getUri().equals(uri))
    {
      return true;
    }
    else
    {
      return false;
    }
  }

  public static boolean isElement(CommonTags expectedElement, String elementName, String uri)
  {
    if (expectedElement.getElementName().equals(elementName)
        && expectedElement.getNamespace().getUri().equals(uri))
    {
      return true;
    }
    else
    {
      return false;
    }
  }
}
