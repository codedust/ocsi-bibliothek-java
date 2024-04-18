package de.osci.osci12.signature;

import java.util.Vector;


/**
 * Element xdsig:RetrievalMethod.
 *
 * <p>Copyright © 2023 Governikus GmbH &amp; Co. KG, Germany</p>
 * <p>Erstellt von Governikus GmbH &amp; Co. KG</p>
 * <p>Diese Bibliothek kann von jedermann nach Maßgabe der European Union
 *  Public Licence genutzt werden.</p><p>Die Lizenzbestimmungen können
 * unter der URL <a href="https://eupl.eu/">https://eupl.eu/</a> abgerufen werden.
 *
 * @author PPI Financial Systems GmbH
 */
public class RetrievalMethod
{
  //  private static Log log = LogFactory.getLog(RetrievalMethod.class);
  /**
   * Wert des Attributs xdsig:Type.
   */
  private static final String TYPE = "http://www.w3.org/2000/09/xmldsig#X509Data";
  /**
   * Wert des Attributs xdsig:URI.
   */
  private String uri;
  private Vector<String> transformer = new Vector<String>();

  /**
   * Standard-Konstruktor.
   */
  public RetrievalMethod()
  {
  }

  /**
   * undocumented
   *
   * @param transformer undocumented
   */
  public void addTransformer(String transformer)
  {
  }

  /**
   * undocumented
   *
   * @return undocumented
   */
  public String[] getTransformers()
  {
    return transformer.toArray(new String[] {  });
  }

  /**
   * Setzt den Wert des Attributs URI.
   * Dieses Attribut ist #REQUIRED.
   *
   * @param uri URI.
   */
  public void setURI(String uri)
  {
    this.uri = uri;
  }

  /**
   * Liefert den Wert des Attributs URI.
   *
   * @return URI.
   */
  public String getURI()
  {
    return uri;
  }

  /**
   * Liefert den Wert des Attributs xdsig:Type.
   *
   * @return String.
   */
  public String getType()
  {
    return TYPE;
  }
}
