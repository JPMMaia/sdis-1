package net.tasks;

import net.IPeerDataChange;
import net.Utils.RandomNumber;
import net.chunks.Chunk;
import net.chunks.Version;
import net.messages.Header;
import net.messages.PutChunkMessage;
import net.messages.StoredMessage;

import java.util.Random;

/**
 * Created by Miguel on 31-03-2015.
 */
public class StoreTask extends Task
{
    private PutChunkMessage m_msg;
    private byte[] m_body;
    String m_peerAddress;

    public StoreTask(PutChunkMessage message, byte[] body, String peerAddress, IPeerDataChange peer)
    {
        super(peer);
        m_msg = message;
        m_body = body;
        m_peerAddress = peerAddress;
    }

    @Override
    public void run()
    {
        Chunk storedChunk = new Chunk(m_msg.getFileId(), m_msg.getChunkNo(), m_msg.getReplicationDeg(), m_body);

        // Only save chunks that are not mine:
        if (!m_peerAccess.isHomeChunk(storedChunk))
        {
            // If I'm not storing this chunk:
            if (!m_peerAccess.isStoredChunk(storedChunk))
            {
                // Only if I have space remaining:
                long remainingSpace = m_peerAccess.getFreeSpace() - m_body.length;
                if (remainingSpace >= 0)
                {
                    m_peerAccess.addStoredChunk(storedChunk);

                    try
                    { Thread.sleep(RandomNumber.getInt(0, 400)); }
                    catch (InterruptedException e)
                    { e.printStackTrace(); }

                    // Send Stored Message:
                    StoredMessage message = new StoredMessage(new Version('1','0'), storedChunk.getFileId(), storedChunk.getChunkNo());
                    Header header = new Header();
                    header.addMessage(message);
                    m_peerAccess.sendHeaderMC(header);
                }

                // TODO: do stuff if no space available
                if (remainingSpace <= 0)
                {

                }
            }

            // If I'm already storing this chunk: ignore it
        }

        // If it's MY chunk from one of my files: ignore it
    }
}
