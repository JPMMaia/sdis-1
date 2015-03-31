package net.chunks;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by Miguel on 23-03-2015.
 */
public class StoredFile
{
    private static final int s_MAX_CHUNK_SIZE = 64000;
    private long m_fileSize;
    private int m_numberOfChunks;
    private String m_filename;
    private ReplicationDeg m_replicationDegree;
    private FileId m_fileId;

    public StoredFile(String filename, ReplicationDeg replication)
    {
        m_filename = filename;
        m_replicationDegree = replication;
        FileInputStream fileStream = null;

        try
        {
            File file = new File(filename);
            fileStream = new FileInputStream(file);

            // Get file length in bytes:
            m_fileSize = file.length();

            // Read file for identifier:
            byte[] fileData = new byte[(int) m_fileSize];
            fileStream.read(fileData);
            fileStream.close();

            // Calculate file identifier:
            m_fileId = new FileId(file, fileData);
        }
        catch(Exception e)
        {
            System.err.println("StoredFile::constructor: Error while reading from file: " + e.toString());
            e.printStackTrace();
            System.exit(-1);
        }
        finally
        {
            if (fileStream != null)
                closeFileStream(fileStream);
        }
    }

    public Chunk[] divideInChunks()
    {
        // Get number of chunks + last chunk size:
        int m_numberOfChunks = (int) (m_fileSize / (float)s_MAX_CHUNK_SIZE) + 1; // floor + 1 (last chunk)
        int lastChunkSize = (int) m_fileSize % s_MAX_CHUNK_SIZE;
        Chunk[] fileChunks = new Chunk[m_numberOfChunks];

        try
        {
            FileInputStream fileStream = new FileInputStream(new File(m_filename));

            // Fill all the chunks:
            fillChunks(fileStream, fileChunks, lastChunkSize);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(-1);
        }

        return fileChunks;
    }

    private void fillChunks(FileInputStream fileStream, Chunk[] fileChunks, int lastChunkSize) throws IOException
    {
        byte[] chunkData = new byte[s_MAX_CHUNK_SIZE];
        byte[] lastChunkData = new byte[lastChunkSize];

        for(int i = 0; i < fileChunks.length; i++)
        {
            if (i == fileChunks.length - 1) // last chunk:
            {
                fileStream.read(lastChunkData);
                fileChunks[i] = new Chunk(m_fileId, new ChunkNo(i), m_replicationDegree, lastChunkData);
            }
            else // others:
            {
                fileStream.read(chunkData);
                fileChunks[i] = new Chunk(m_fileId, new ChunkNo(i), m_replicationDegree, chunkData);
            }
        }
    }

    private static void closeFileStream(FileInputStream fileStream)
    {
        try
        {
            fileStream.close();
        }
        catch (IOException e)
        {
            System.err.println("StoredFile::closeFileStream: Error closing file: " + e.toString());
            e.printStackTrace();
            System.exit(-2);
        }
    }

    public String getFilename()
    {
        return m_filename;
    }

    public FileId getFileId()
    {
        return m_fileId;
    }

    public static void main(String[] args)
    {
        new StoredFile("C:\\Users\\Miguel\\IdeaProjects\\gosto.txt", new ReplicationDeg(3));
    }
}
