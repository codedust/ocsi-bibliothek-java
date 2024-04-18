package de.osci.osci12.soapheader;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import de.osci.osci12.OSCIException;
import de.osci.osci12.common.Constants;
import de.osci.osci12.common.Constants.HeaderTags;
import de.osci.osci12.common.Constants.OSCIFeatures;


/**
 * <p>
 * Mit dieser Klasse wird der FeatureDescription Header bestückt. Die Chunk-Eigenschaften sowie weitere
 * Features können gesetzt werden. Dieser Header kann dann auf den Nachrichten-Objekten gesetzt werden.
 * </p>
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
 * @author R. Lindemann
 * @version 2.4.1
 * @since 1.8.0
 */
public class FeatureDescriptionH extends HeaderEntry
{

  private List<OSCIFeatures> supportedFeatures = new ArrayList<OSCIFeatures>();

  /*
   * Initial mit dem neutralem Wert -1 instanziert
   */
  private long maxMessageSize = -1;

  private long maxChunkSize = -1;

  private long minChunkSize = -1;

  private long chunkMessageTimeout = -1;


  /**
   * @return Liefert die Liste der eingetragenen unterstützten Features
   * @see OSCIFeatures
   * @see #setSupportedFeatures(List)
   */
  public List<OSCIFeatures> getSupportedFeatures()
  {
    return supportedFeatures;
  }

  /**
   * @param supportedFeatures Setzt die Liste der unterstützten Features. Wird zumeist vom
   *          Supplier (Intermediär) gesetzt.
   * @see OSCIFeatures
   * @see #getSupportedFeatures()
   */
  public void setSupportedFeatures(List<OSCIFeatures> supportedFeatures)
  {
    this.supportedFeatures = supportedFeatures;
  }

  /**
   * @return Liefert den eingestellten Wert der maximalen Nachrichtengröße
   * @see #setMaxMessageSize(long)
   */
  public long getMaxMessageSize()
  {
    return maxMessageSize;
  }

  /**
   * @param maxMessageSize Setzt den Wert der maximalen Nachrichtengröße. Wird zumeist vom
   *          Supplier (Intermediär) gesetzt.
   * @see #getMaxMessageSize()
   */
  public void setMaxMessageSize(long maxMessageSize)
  {
    this.maxMessageSize = maxMessageSize;
  }


  /**
   * @return Liefert die maximale akzeptierte Chunk Größe
   * @see #setMaxChunkSize(long)
   */
  public long getMaxChunkSize()
  {
    return maxChunkSize;
  }

  /**
   * @param maxChunkSize Setzt die maximale Chunk Größe. Wird zumeist vom Supplier (Intermediär) gesetzt.
   * @see #getMaxChunkSize()
   */
  public void setMaxChunkSize(long maxChunkSize)
  {
    this.maxChunkSize = maxChunkSize;
  }

  /**
   * @return Liefert die minimale Chunk Größe
   * @see #setMinChunkSize(long)
   */
  public long getMinChunkSize()
  {
    return minChunkSize;
  }

  /**
   * @param minChunkSize Setzt die minimale Chunk Größe. Wird zumeist vom Supplier (Intermediär) gesetzt.
   * @see #getMinChunkSize()
   */
  public void setMinChunkSize(long minChunkSize)
  {
    this.minChunkSize = minChunkSize;
  }

  /**
   * @return Liefert die garantierte Timeout-Zeit vom Supplier, nach der eine Rückantwort generiert wird. Sollte
   *         die Verarbeitung beim Supplier das Timeout überschreiten, wird eine vorläufige Rückantwort
   *         generiert.
   * @see #setChunkMessageTimeout(long)
   */
  public long getChunkMessageTimeout()
  {
    return chunkMessageTimeout;
  }

  /**
   * @param chunkMessageTimeout Setzt die Timeout-Zeit. Wird zumeist vom Supplier (Intermediär) gesetzt.
   * @see #getChunkMessageTimeout()
   */
  public void setChunkMessageTimeout(long chunkMessageTimeout)
  {
    this.chunkMessageTimeout = chunkMessageTimeout;
  }

  @Override
  protected void writeXML(OutputStream out) throws IOException, OSCIException
  {
    String namespacePre = osci2017NSPrefix;
    out.write(("<" + namespacePre + ":"
               + HeaderTags.FeatureDescription.getElementName()).getBytes(Constants.CHAR_ENCODING));
    out.write(ns2017);

    // lexikografische Reihenfolge der Attribute ist entscheidend fuer die Signaturpruefung!

    if (chunkMessageTimeout != -1)
      out.write((" ChunkMessageTimeout=\"" + chunkMessageTimeout + "\"").getBytes(Constants.CHAR_ENCODING));

    out.write((" Id=\"" + id + "\"").getBytes(Constants.CHAR_ENCODING));

    if (maxChunkSize != -1)
      out.write((" MaxChunkSize=\"" + maxChunkSize + "\"").getBytes(Constants.CHAR_ENCODING));


    if (maxMessageSize != -1)
      out.write((" MaxMessageSize=\"" + maxMessageSize + "\"").getBytes(Constants.CHAR_ENCODING));

    if (minChunkSize != -1)
      out.write((" MinChunkSize=\"" + minChunkSize + "\"").getBytes(Constants.CHAR_ENCODING));


    out.write(">".getBytes());
    if (supportedFeatures != null && !supportedFeatures.isEmpty())
    {
      StringBuilder sb = new StringBuilder("<");
      sb.append(namespacePre);
      sb.append(":SupportedFeatures>");

      for ( OSCIFeatures feature : supportedFeatures )
      {
        sb.append("<");
        sb.append(namespacePre);
        sb.append(":Feature Key=\"" + feature.name() + "\"");
        sb.append(" Version=\"" + feature.getVersion() + "\">");
        sb.append("</");
        sb.append(namespacePre);
        sb.append(":Feature>");
      }
      sb.append("</");
      sb.append(namespacePre);
      sb.append(":SupportedFeatures>");
      out.write(sb.toString().getBytes());
    }
    out.write(("</" + namespacePre + ":" + HeaderTags.FeatureDescription.getElementName()
               + ">").getBytes(Constants.CHAR_ENCODING));
  }

}
