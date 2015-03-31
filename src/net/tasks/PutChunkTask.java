package net.tasks;

import net.IPeerDataChange;
import net.chunks.Chunk;
import net.chunks.Version;
import net.messages.Header;
import net.messages.PutChunkMessage;

/**
 * Created by Miguel on 31-03-2015.
 */
public class PutChunkTask extends Task
{
    public static final int SENT = 0;
    public static final int NOT_SENT = 1;
    private Chunk m_chunk;

    public PutChunkTask(Chunk chunk, IPeerDataChange peer)
    {
        super(peer);
        m_chunk = chunk;
    }

    @Override
    public void run()
    {
        try
        {
            // Create put chunk message:
            PutChunkMessage message = new PutChunkMessage(new Version('1', '0'), m_chunk.getFileId(), m_chunk.getChunkNo(), m_chunk.getOptimalReplicationDeg());
            Header header = new Header();
            header.addMessage(message);
            header.setBody(m_chunk.getData());

            // do 1 try + 5 repeats
            int numTries = 0;
            int delayTime = 500;

            while (numTries < 6)
            {
                // Send header to MDB channel:
                m_peerAccess.sendHeaderMDB(header);

                Thread.sleep(delayTime);

                // Backup success:
                if (m_peerAccess.getRealReplicationDeg(m_chunk) >= m_chunk.getOptimalReplicationDeg().getValue())
                {
                    setReturn(SENT);
                    return;
                }

                // Needs extra replication:
                numTries++;
                delayTime *= 2;
            }

            setReturn(NOT_SENT);
        }
        catch (InterruptedException e)
        {
            System.out.println("The putChunkThread was interrupted!");
            setReturn(NOT_SENT);
        }
    }
}
