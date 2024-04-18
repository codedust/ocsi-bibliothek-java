package de.osci.osci12.samples;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class StreamChunker implements AutoCloseable
{

  private InputStream in;

  private byte[] buffer;
  
  private static final int bufferSize = 1024;

  /**
   * Takes an input stream and writes it chunk for chunk to output streams.
   * 
   * @param in the input stream
   */
  public StreamChunker(InputStream in)
  {
    this.in = in;
    this.buffer = new byte[bufferSize];
  }

  /**
   * @return true if there are still bytes in the input stream available to be written to a new chunk.
   * @throws IOException
   */
  public boolean chunkRemaining() throws IOException
  {
    return in.available() > 0;
  }

  /**
   * Writes bytes to the output stream till the chunk size is reached or no bytes are available anymore in the
   * input stream. There could be more bytes written to the output stream than set in chunk size since there
   * are always 8192 bytes blocks read from the input stream.
   * 
   * @param out the output stream
   * @return the number of bytes written to the output stream
   * @throws IOException
   */
  public long writeChunk(OutputStream out, long chunkSize) throws IOException
  {
    long bytesWritten = 0;
    int b;
    while (bytesWritten < chunkSize)
    {
      if ((b = in.read(buffer)) > -1)
      {
        bytesWritten += b;
        out.write(buffer, 0, b);
      }
      else
      {
        break;
      }
    }
    return bytesWritten;
  }

  /**
   * Closes the input stream.
   */
  public void close() throws IOException
  {
    in.close();
  }

}
