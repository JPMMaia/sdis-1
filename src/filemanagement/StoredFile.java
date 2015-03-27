package filemanagement;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by Miguel on 23-03-2015.
 */
public class StoredFile
{
    private static final int s_MAX_SIZE = 64000;
    private String m_path;
    private Chunk[] m_chunks;
    private Version m_version;
    private FileId m_fileId;

    public StoredFile(String filePath)
    {
        FileInputStream fileStream = null;

        try
        {
            File file = new File(filePath);
            fileStream = new FileInputStream(file);

            // Get file length in bytes:
            long fileSize = file.length();

            // Get number of chunks + last chunk size:
            int numberOfChunks = (int) (fileSize / (float)s_MAX_SIZE) + 1; // floor + 1 (last chunk)
            int lastChunkSize = (int) fileSize % s_MAX_SIZE;
            m_chunks = new Chunk[numberOfChunks];

            // Read file for identifier:
            byte[] fileData = new byte[(int) fileSize];
            fileStream.read(fileData);
            fileStream.close();
            fileStream = new FileInputStream(file);
            m_fileId = new FileId(file, fileData);
            m_version = new Version('1', '2');

            // Fill all the chunks:
            fillChunks(fileStream, numberOfChunks, lastChunkSize);
        }
        catch(Exception e)
        {
            System.out.println("Error while reading from file: " + e.toString());
            e.printStackTrace();
            System.exit(-1);
        }
        finally
        {
            if (fileStream != null)
                closeFileStream(fileStream);
        }
    }

    private void fillChunks(FileInputStream fileStream, int numberOfChunks, int lastChunkSize) throws IOException
    {
        byte[] chunkData = new byte[s_MAX_SIZE];
        byte[] lastChunkData = new byte[lastChunkSize];

        for(int i = 0; i < numberOfChunks; i++)
        {
            if (i == numberOfChunks - 1) // last chunk:
            {
                fileStream.read(lastChunkData);
                m_chunks[i] = new Chunk(m_version, m_fileId, new ChunkNo(i), lastChunkData);
            }
            else // others:
            {
                fileStream.read(chunkData);
                m_chunks[i] = new Chunk(m_version, m_fileId, new ChunkNo(i), chunkData);
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
            System.out.println("Error closing file: " + e.toString());
            e.printStackTrace();
            System.exit(-2);
        }
    }

    public static void main(String[] args)
    {
        new StoredFile("C:\\Users\\Miguel\\IdeaProjects\\gosto.txt");
    }
}
