package filemanagement;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by Miguel on 23-03-2015.
 */
public class StoredFile
{
    private String m_path;
    private Chunk[] m_chunks;

    public StoredFile(String filePath)
    {
        File file = null;
        FileInputStream fileStream = null;

        try
        {
            file = new File(filePath);
            fileStream = new FileInputStream(file);
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

        // Get file length in bytes:
        long fileSize = file.length();


        // Get number of chunks + last chunk size:
        int numberOfChunks = (int) (fileSize / 64000.0) + 1; // floor + 1 (last chunk)
        int lastChunkSize = (int) fileSize % 64000;
        m_chunks = new Chunk[numberOfChunks];

        // Fill all the chunks:
        //fillChunks(fileSize, numberOfChunks, lastChunkSize);
    }

    private void fillChunks(byte[] fileData, int numberOfChunks, int lastChunkSize)
    {
/*        for(int i = 0; i < numberOfChunks; i++)
        {
            if (i == numberOfChunks - 1) // last chunk:
            {

                m_chunks[i] = new Chunk("NAO XEI", i);
            }
            else // others:
            {
                m_chunks[i] = new Chunk("NAO XEI", i, lastChunkSize);
            }
        }*/
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
}
