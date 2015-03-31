package net.services;

import net.IPeerDataChange;
import net.chunks.Chunk;
import net.chunks.ReplicationDeg;
import net.chunks.StoredFile;
import net.tasks.PutChunkTask;
import net.tasks.Task;

/**
 * Created by Miguel on 30-03-2015.
 */
public class BackupService extends UserService
{
    StoredFile m_file;
    ReplicationDeg m_replicationDegField;
    IPeerDataChange m_peerAccess;

    public BackupService(String filename, int replicationDeg, IPeerDataChange peerAccess)
    {
        m_replicationDegField = new ReplicationDeg(replicationDeg);
        m_file = new StoredFile(filename, m_replicationDegField);
        m_peerAccess = peerAccess;
    }

    @Override
    public void run()
    {
        // For each file chunk:
        Chunk[] chunks = m_file.divideInChunks();
        for (int chunkNo = 0; chunkNo < chunks.length; chunkNo++)
        {
            Chunk chunk = chunks[chunkNo];

            PutChunkTask putChunk = new PutChunkTask(chunk, m_peerAccess);
            Thread putChunkThread = new Thread(putChunk);
            putChunkThread.start();

            try
            {
                putChunkThread.join();
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }

            if (putChunk.getReturn() != PutChunkTask.SENT)
            {
                System.err.println("A chunk was not sucessfully sent after 5 times, aborting backup!");
                return;
            }
        }

        // Store file in the list:
        m_peerAccess.addStoredFile(m_file);
    }
}
