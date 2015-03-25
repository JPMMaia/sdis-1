package filemanagement;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Miguel on 23-03-2015.
 */
public class StoredFile
{
    private static final int s_MAX_SIZE = 64000;
    private String m_path;
    private Chunk[] m_chunks;

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
            String fileId = createFileIdentifier(file, fileData);

            // Fill all the chunks:
            fillChunks(fileStream, fileId, numberOfChunks, lastChunkSize);
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

    private static String createFileIdentifier(File file, byte[] fileData) throws NoSuchAlgorithmException, UnsupportedEncodingException
    {
        String bitString = file.getAbsolutePath() + file.lastModified() + new String(fileData, "UTF-8");

        // Encrypt:
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(bitString.getBytes("UTF-8"));
        return new String(md.digest(), "UTF-8");
    }

    private void fillChunks(FileInputStream fileStream, String fileId, int numberOfChunks, int lastChunkSize) throws IOException
    {
        byte[] chunkData = new byte[s_MAX_SIZE];
        byte[] lastChunkData = new byte[lastChunkSize];

        for(int i = 0; i < numberOfChunks; i++)
        {
            if (i == numberOfChunks - 1) // last chunk:
            {
                fileStream.read(lastChunkData);
                m_chunks[i] = new Chunk(fileId, i, lastChunkData);
            }
            else // others:
            {
                fileStream.read(chunkData);
                m_chunks[i] = new Chunk(fileId, i, chunkData);
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
