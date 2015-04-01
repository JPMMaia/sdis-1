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
    public BackupService(BackupFile file, IPeerDataChange peerAccess)
    {
        super(file, peerAccess);
    }

    @Override
    public void run()
    {
        // For each file chunk:
        Chunk[] chunks = m_file.divideInChunks();
        for (int chunkNo = 0; chunkNo < chunks.length; chunkNo++)
        {
            Chunk chunk = chunks[chunkNo];

            // Add chunk to MY chunk list: TODO: apagar o data destes chunks?
            m_peerAccess.addHomeChunk(chunk);

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
                System.err.println("A chunk was not successfully sent after 5 times, aborting backup!");
                // TODO: aqui tenho de remover todos os chunks daquele file
                return;
            }
        }

        // Store file in the list:
        m_peerAccess.addHomeFile(m_file);
    }
}
