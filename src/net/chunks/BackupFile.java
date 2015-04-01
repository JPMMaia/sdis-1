package net.chunks;

import java.io.*;
import java.text.SimpleDateFormat;

/**
 * Created by Miguel on 23-03-2015.
 */
public class BackupFile
{
    private static final int s_MAX_CHUNK_SIZE = 64000;
    private static final String s_RECOVER_DIRECTORY = "recover/";
    private long m_fileSize;
    private int m_numberOfChunks;
    private String m_filePath;
    private String m_fileName;
    private String m_lastModified;
    private ReplicationDeg m_replicationDegree;
    private FileId m_fileId;

    public BackupFile(String filePath, ReplicationDeg replication)
    {
        m_filePath = filePath;
        m_replicationDegree = replication;
        FileInputStream fileStream = null;

        try
        {
            File file = new File(filePath);
            fileStream = new FileInputStream(file);

            // Get file info (length/last modified):
            m_fileName = file.getName();
            m_fileSize = file.length();
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
            m_lastModified = sdf.format(file.lastModified());

            // Read file for identifier:
            byte[] fileData = new byte[(int) m_fileSize];
            fileStream.read(fileData);
            fileStream.close();

            // Calculate file identifier:
            m_fileId = new FileId(file, fileData);
        }
        catch(Exception e)
        {
            System.err.println("BackupFile::constructor: not found! Backup aborted. Filename: " + e.toString());
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
        m_numberOfChunks = (int) (m_fileSize / (float)s_MAX_CHUNK_SIZE) + 1; // floor + 1 (last chunk)
        int lastChunkSize = (int) m_fileSize % s_MAX_CHUNK_SIZE;
        Chunk[] fileChunks = new Chunk[m_numberOfChunks];

        try
        {
            FileInputStream fileStream = new FileInputStream(new File(m_filePath));

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

    public void recoverFromChunks(Chunk[] chunksArray)
    {
        if (chunksArray.length != m_numberOfChunks)
        {
            System.err.println("BackupFile::recover: Recover not possible, different chunk number!");
            return;
        }

        try
        {
            File recoverFile = new File(s_RECOVER_DIRECTORY + m_fileName);
            FileOutputStream recoverStream = new FileOutputStream(recoverFile);

            for (Chunk chunk: chunksArray)
                recoverStream.write(chunk.getData());

            recoverStream.close();
        }
        catch (FileNotFoundException e)
        {
            System.err.println("BackupFile::recoverFromChunks: Folder recover not found");
            System.exit(-1);
        }
        catch (IOException e)
        {
            System.err.println("BackupFile::recoverFromChunks: Error writing in recover file");
            System.exit(-1);
        }
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
            System.err.println("BackupFile::closeFileStream: Error closing file: " + e.toString());
            e.printStackTrace();
            System.exit(-2);
        }
    }

    public String getFilePath()
    {
        return m_filePath;
    }

    public FileId getFileId()
    {
        return m_fileId;
    }

    public int getNumberChunks()
    {
        return m_numberOfChunks;
    }

    public String getLastModified()
    {
        return m_lastModified;
    }
}
