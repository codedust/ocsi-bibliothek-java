package de.osci.osci12.samples;

import java.io.*;
import java.util.ArrayList;


/**
 * This is a part of the demo application for an asynchronous communication scenario with partial messages
 * according to the OSCI 1.2-transport specification. The main method needs the intermediary's URL as
 * parameter.
 * <p>
 * Copyright © 2023 Governikus GmbH &amp; Co. KG, Germany
 * </p>
 * <p>Erstellt von Governikus GmbH &amp; Co. KG</p>
 * <p>
 * Diese Bibliothek kann von jedermann nach Maßgabe der European Union Public Licence genutzt
 * werden.
 * </p>
 * Die Lizenzbestimmungen können unter der URL <a href="https://eupl.eu/">https://eupl.eu/</a> abgerufen werden..
 * </p>
 *
 * @author J. Buckelo
 * @version 0.9
 * @since 1.8.0
 */
public class ChunkHelper
{

  public enum Mode
  {
    CONSTANTSIZE, OPTIMIZEDSTORE
  }

  private ArrayList<File> chunks;

  private boolean chunksDeleted = false;

  private long chunkSizeBytes;

  /**
   * @param in InputStream that will be chunked
   * @param chunkDirectory Directoriy for the chunks
   * @param chunkName Name of the chunk files
   * @param chunkSize Desired size of the chunk files in KB
   * @throws IOException
   */
  public ChunkHelper(InputStream in, String chunkDirectory, String chunkName, long chunkSize)
    throws IOException
  {
    chunkSizeBytes = chunkSize * 1024;
    chunks = new ArrayList<File>((int)((in.available() / chunkSizeBytes) + 1));
    writeToFiles(in, chunkDirectory + File.separator + chunkName, chunkSizeBytes);
  }

  /**
   * @param in InputStream that will be chunked
   * @param chunkDirectory Directoriy for the chunks
   * @param chunkName Name of the chunk files
   * @param totalSize Size of the whole input stream in KB
   * @param mode The mode decides how the input stream should be chunked
   * @throws IOException
   */
  public ChunkHelper(InputStream in,
                     String chunkDirectory,
                     String chunkName,
                     int chunkNumber,
                     long totalSize,
                     Mode mode)
    throws IOException
  {
    chunks = new ArrayList<File>(chunkNumber);
    writeToFiles(in, chunkDirectory + File.separator + chunkName, chunkNumber, totalSize * 1024, mode);
  }

  /**
   * @return Number of produced chunks
   */
  public int getNumberOfChunks()
  {
    return chunks.size();
  }

  /**
   * @param num Number of the chunk file. Starting at 1
   * @return the chunk file
   */
  public File getChunkFile(int num)
  {
    if (!chunksDeleted && num <= chunks.size() && num > 0)
    {
      return chunks.get(num - 1);
    }
    return null;
  }

  /**
   * @return List of all produced chunks
   */
  public ArrayList<File> getChunkFiles()
  {
    return chunks;
  }

  /**
   * @param num Number of the chunk file. Starting at 1
   * @return InputStream of the chunk
   * @throws FileNotFoundException
   */
  public FileInputStream getChunk(int num) throws FileNotFoundException
  {
    if (!chunksDeleted && num <= chunks.size() && num > 0)
    {
      return new FileInputStream(chunks.get(num - 1));
    }
    return null;
  }

  /**
   * @return Desired chunk size
   */
  public long getChunkSize()
  {
    return chunkSizeBytes / 1024;
  }

  /**
   * Deltes all chunk files
   */
  public void deleteFiles()
  {
    if (!chunksDeleted)
    {
      for ( File file : chunks )
      {
        file.delete();
      }
      chunksDeleted = true;
      chunks = null;
    }
  }

  private void writeToFiles(InputStream in, String filePath, long chunkSize) throws IOException
  {
    try (StreamChunker ss = new StreamChunker(in))
    {
      String fileName = filePath + ".chunk";
      for ( int i = 1 ; ss.chunkRemaining() ; i++ )
      {
        File file = new File(fileName + i);
        try (FileOutputStream out = new FileOutputStream(file))
        {
          ss.writeChunk(out, chunkSize);
        }
        chunks.add(file);
      }
    }
  }

  private long writeToFiles(InputStream in, String filePath, int chunkNumber, long totalSize, Mode mode)
    throws IOException
  {
    chunkSizeBytes = (totalSize / chunkNumber) + 128;

    if (mode == Mode.OPTIMIZEDSTORE)
    {
      chunkSizeBytes = (totalSize - (chunkSizeBytes / 3)) / (chunkNumber - 1);
    }

    writeToFiles(in, filePath, chunkSizeBytes);

    return chunkSizeBytes;
  }

}
