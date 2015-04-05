package net.services;

import net.IPeerDataChange;
import net.chunks.BackupFile;
import net.chunks.Chunk;
import net.chunks.Version;
import net.messages.DeleteMessage;
import net.messages.Header;
import net.tasks.PutChunkTask;

import java.io.Serializable;

/**
 * Created by Miguel on 30-03-2015.
 */
public class BackupService extends UserService implements Serializable
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

            // Add chunk to MY chunk list:
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
                System.err.println("Error waiting for putchunk thread");
                e.printStackTrace();
                System.exit(-3);
            }

            if (putChunk.getReturn() != PutChunkTask.SENT)
            {
                System.err.println("BackupService::run A chunk was not successfully sent after 5 times, aborting backup!");
                m_peerAccess.deleteHomeFile(chunk.getFileId());

                // Send deleteFile:
                DeleteMessage message = new DeleteMessage(new Version('1', '0'), chunk.getFileId());
                Header header = new Header();
                header.addMessage(message);
                m_peerAccess.sendHeaderMC(header);

                // End service:
                m_peerAccess.removeUserService(this);
                return;
            }
        }

        // Store file in the list:
        m_peerAccess.addHomeFile(m_file);

        System.out.println("BackupService - A backup ended successfuly!");

        // End service:
        m_peerAccess.removeUserService(this);
    }
}
