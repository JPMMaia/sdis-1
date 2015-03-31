package net.services;

import net.IPeerDataChange;
import net.chunks.Chunk;
import net.chunks.ReplicationDeg;
import net.chunks.BackupFile;
import net.tasks.PutChunkTask;

/**
 * Created by Miguel on 30-03-2015.
 */
public class BackupService extends UserService
{
    BackupFile m_file;
    IPeerDataChange m_peerAccess;

    public BackupService(String filename, int replicationDeg, IPeerDataChange peerAccess)
    {
        ReplicationDeg replicationDegField = new ReplicationDeg(replicationDeg);

        m_file = new BackupFile(filename, replicationDegField);
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

            // Add chunk to MY chunk list:
            m_peerAccess.addHomeChunk(chunk.getIdentifier());

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
                // TODO: aqui tenho de remover todos os chunks daquele file
                return;
            }
        }

        // Store file in the list:
        m_peerAccess.addHomeFile(m_file);
    }
}
