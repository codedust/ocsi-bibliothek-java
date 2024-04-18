package de.osci.osci12.messageparts;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.osci.osci12.OSCIException;
import de.osci.osci12.common.Constants;
import de.osci.osci12.common.Constants.LanguageTextEntries;
import de.osci.osci12.common.DialogHandler;


/**
 * Diese Klasse repräsentiert die Struktur der ChunkInformation. Werte können gesetzt werden und werden später
 * in die OSCI-Nachricht serialisiert.
 * <p>
 * Copyright © 2023 Governikus GmbH &amp; Co. KG, Germany
 * </p>
 * <p>Erstellt von Governikus GmbH &amp; Co. KG</p>
 * <p>
 * Diese Bibliothek kann von jedermann nach Maßgabe der European Union Public Licence genutzt
 * werden.
 * </p>
 * <p>
 * Die Lizenzbestimmungen können unter der URL <a href="https://eupl.eu/">https://eupl.eu/</a> abgerufen werden.
 * </p>
 *
 * @author R. Lindemann
 * @version 2.4.1
 * @since 1.8.0
 */
public class ChunkInformation extends MessagePart implements Serializable
{

  private static final long serialVersionUID = -4517673667797670551L;

  private int chunkNumber = 0;

  private long totalMessageSize = 0;

  private long chunkSize = 0;

  private CheckInstance checkInstance;

  private List<Integer> receivedChunks = new ArrayList<>();

  private int totalChunkNumbers = 0;

  ChunkInformation(CheckInstance checkInstance)
  {
    setCheckInstance(checkInstance);
  }

  /**
   * Konstruktor für Partial Store Delivery
   *
   * @param chunkSize Groesse eines Chunks in KB
   * @param chunkNumber Aktuelle Chunk Nummer
   * @param totalMessageSize Gesamtgroesse (Store Delivery Nachricht) in KB
   * @param totalChunkNumbers Gesamtzahl der Chunks
   */
  public ChunkInformation(long chunkSize, int chunkNumber, long totalMessageSize, int totalChunkNumbers)
  {
    setCheckInstance(CheckInstance.PartialStoreDelivery);
    setTotalMessageSize(totalMessageSize);
    setTotalChunkNumbers(totalChunkNumbers);
    setChunkSize(chunkSize);
    setChunkNumber(chunkNumber);

  }

  /**
   * Konstruktor für Partial Fetch Delivery
   *
   * @param chunkSize Groesse eines Chunks in KB
   * @param chunkNumber Aktuelle Chunk Nummer
   * @param receivedChunks Liste bereits empfangener Chunks. Sofern noch keine Chunks empfangen wurden, kann
   *          hier null übergeben werden.
   */
  public ChunkInformation(long chunkSize, int chunkNumber, List<Integer> receivedChunks)
  {
    setCheckInstance(CheckInstance.PartialFetchDelivery);
    setChunkSize(chunkSize);
    setChunkNumber(chunkNumber);
    if (receivedChunks == null || receivedChunks.size() == 0)
    {
      setReceivedChunks(new Integer[]{-1});
    }
    else
    {
      setReceivedChunks(receivedChunks);
    }
  }

  /**
   * Konstruktor für Partial Fetch Delivery
   *
   * @param chunkSize Groesse eines Chunks in KB
   * @param chunkNumber Aktuelle Chunk Nummer
   * @param receivedChunks Liste bereits empfangener Chunks. Sofern noch keine Chunks empfangen wurden, braucht
   *          dieser Parameter nicht gesetzt werden
   */
  public ChunkInformation(long chunkSize, int chunkNumber, Integer... receivedChunks)
  {
    setCheckInstance(CheckInstance.PartialFetchDelivery);
    setChunkSize(chunkSize);
    setChunkNumber(chunkNumber);
    if (receivedChunks == null || receivedChunks.length == 0)
    {
      setReceivedChunks(new Integer[]{-1});
    }
    else
    {
      setReceivedChunks(Arrays.asList(receivedChunks));
    }
  }

  /**
   * @return Liefert die eingestellte / geparste CheckInstance (vom entsprechenden Nachrichtentyp)
   */
  public CheckInstance getCheckInstance()
  {
    return checkInstance;
  }


  /**
   * @param checkInstance Setzt die CheckInstance. Wird eigentlich durch den Konstruktor gesetzt
   * @see #getCheckInstance()
   */
  public void setCheckInstance(CheckInstance checkInstance)
  {
    if (checkInstance == null)
      throw new IllegalArgumentException(DialogHandler.text.getString(LanguageTextEntries.invalid_firstargument.name())
                                         + " checkInstance");
    this.checkInstance = checkInstance;
  }

  /**
   * @return Liefert die eingetragene / geparste TotalChunkNumber
   * @see #setTotalChunkNumbers(int)
   */
  public int getTotalChunkNumbers()
  {
    return totalChunkNumbers;
  }

  /**
   * @param totalChunkNumbers Setzt die TotalChunkNumbers
   * @see #getTotalChunkNumbers()
   */
  public void setTotalChunkNumbers(int totalChunkNumbers)
  {
    if (!checkInstance.totalChunkNumberCheck)
      throw new IllegalArgumentException(DialogHandler.text.getString(LanguageTextEntries.invalid_firstargument.name())
                                         + " totalChunkNumbers");
    this.totalChunkNumbers = totalChunkNumbers;
  }

  /**
   * @return Liefert die eingetragene / geparste ChunkNumber
   * @see #setChunkNumber(int)
   */
  public int getChunkNumber()
  {
    return chunkNumber;
  }

  /**
   * @param chunkNumber Setzt die ChunkNumber
   * @see #getChunkNumber()
   */
  public void setChunkNumber(int chunkNumber)
  {
    if (!checkInstance.chunkNumberCheck)
      throw new IllegalArgumentException(DialogHandler.text.getString(LanguageTextEntries.invalid_firstargument.name())
                                         + " chunkNumber");
    this.chunkNumber = chunkNumber;
  }

  /**
   * @return Liefert die eingetragene / geparste TotalMessageSize in KB
   * @see #setTotalMessageSize(long)
   */
  public long getTotalMessageSize()
  {
    return totalMessageSize;
  }

  /**
   * @param totalMessageSize Setzt die TotalMessageSize in KB
   * @see #getTotalMessageSize()
   */
  public void setTotalMessageSize(long totalMessageSize)
  {
    this.totalMessageSize = totalMessageSize;
  }

  /**
   * @return Liefert die eingetragene / geparste ChunkSize in KB
   * @see #setChunkSize(long)
   */
  public long getChunkSize()
  {
    return chunkSize;
  }

  /**
   * @param chunkSize Setzt die ChunkSize in KB
   * @see #getChunkSize()
   */
  public void setChunkSize(long chunkSize)
  {
    if (!checkInstance.chunkSizeCheck)
      throw new IllegalArgumentException(DialogHandler.text.getString(LanguageTextEntries.invalid_firstargument.name())
                                         + " chunkSize");
    this.chunkSize = chunkSize;
  }

  /**
   * @return Liefert die eingetragene / geparste ReceivedChunks
   * @see #setReceivedChunks(Integer[])
   */
  public List<Integer> getReceivedChunks()
  {
    return receivedChunks;
  }

  /**
   * @param receivedChunks Setzt die bereits empfangenen Chunks
   * @see #getReceivedChunks()
   */
  public void setReceivedChunks(List<Integer> receivedChunks)
  {
    if (!checkInstance.receivedChunksCheck)
      throw new IllegalArgumentException(DialogHandler.text.getString(LanguageTextEntries.invalid_firstargument.name())
                                         + " receivedChunks");
    this.receivedChunks = receivedChunks;
  }

  /**
   * @param receivedChunks Setzt die bereits empfangenen Chunks
   * @see #getReceivedChunks()
   */
  public void setReceivedChunks(Integer[] receivedChunks)
  {
    setReceivedChunks(Arrays.asList(receivedChunks));
  }

  @Override
  protected void writeXML(OutputStream out) throws IOException, OSCIException
  {
    out.write(("<" + osci2017NSPrefix + ":ChunkInformation").getBytes(Constants.CHAR_ENCODING));
    if (getChunkNumber() != 0)
    {
      out.write((" ChunkNumber=\"" + getChunkNumber() + "\"").getBytes(Constants.CHAR_ENCODING));
    }
    if (getChunkSize() != 0)
    {
      out.write((" ChunkSize=\"" + getChunkSize() + "\"").getBytes(Constants.CHAR_ENCODING));
    }
    if (getReceivedChunks() != null && !getReceivedChunks().isEmpty())
    {
      String reciverChunksString = " ReceivedChunks=\"";
      for ( int chunkNumberRec : getReceivedChunks() )
      {
        reciverChunksString = reciverChunksString.concat(Integer.toString(chunkNumberRec) + " ");
      }
      reciverChunksString = reciverChunksString.substring(0, reciverChunksString.length() - 1);
      reciverChunksString = reciverChunksString.concat("\"");
      out.write((reciverChunksString).getBytes(Constants.CHAR_ENCODING));
    }
    if (getTotalChunkNumbers() != 0)
    {
      out.write((" TotalChunkNumbers=\"" + getTotalChunkNumbers() + "\"").getBytes(Constants.CHAR_ENCODING));
    }
    if (getTotalMessageSize() != 0)
    {
      out.write((" TotalMessageSize=\"" + getTotalMessageSize() + "\"").getBytes(Constants.CHAR_ENCODING));
    }
    out.write(("></" + osci2017NSPrefix + ":ChunkInformation>").getBytes(Constants.CHAR_ENCODING));
  }

  /**
   * Definition der Kardinalitäten für die Chunk-Information
   */
  public enum CheckInstance
  {
    PartialStoreDelivery(true, true, true, false, true),
    ResponsePartialStoreDelivery(false, true, false, true, true),
    PartialFetchDelivery(true, true, false, true, false),
    ResponsePartialFetchDelivery(false, true, true, false, true);

    private boolean chunkSizeCheck;

    private boolean chunkNumberCheck;

    private boolean totalMessageSizeCheck;

    private boolean receivedChunksCheck;

    private boolean totalChunkNumberCheck;

    CheckInstance(boolean chunkSizeCheck,
                  boolean chunkNumberCheck,
                  boolean totalMessageSizeCheck,
                  boolean receivedChunksCheck,
                  boolean totalChunkNumberCheck)
    {
      this.chunkSizeCheck = chunkSizeCheck;
      this.chunkNumberCheck = chunkNumberCheck;
      this.totalMessageSizeCheck = totalMessageSizeCheck;
      this.receivedChunksCheck = receivedChunksCheck;
      this.totalChunkNumberCheck = totalChunkNumberCheck;
    }

    /**
     * @return True sobald das Attribut ChunkSize Pflicht ist.
     */
    public boolean isChunkSizeCheck()
    {
      return chunkSizeCheck;
    }

    /**
     * @return True sobald das Attribut ChunkNumber Pflicht ist.
     */
    public boolean isChunkNumberCheck()
    {
      return chunkNumberCheck;
    }

    /**
     * @return True sobald das Attribut TotalMessageSize Pflicht ist.
     */
    public boolean isTotalMessageSizeCheck()
    {
      return totalMessageSizeCheck;
    }

    /**
     * @return True sobald das Attribut ReceivedChunks Pflicht ist.
     */
    public boolean isReceivedChunksCheck()
    {
      return receivedChunksCheck;
    }

    /**
     * @return True sobald das Attribut TotalChunkNumber Pflicht ist.
     */
    public boolean isTotalChunkNumberCheck()
    {
      return totalChunkNumberCheck;
    }
  }
}


