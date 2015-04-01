package net.tasks;

import net.IPeerDataChange;
import net.chunks.Chunk;
import net.messages.StoredMessage;

/**
 * Created by Miguel on 31-03-2015.
 */
public class ProcessStoredTask extends Task
{
    private StoredMessage m_msg;
    String m_peerAddress;

    public ProcessStoredTask(StoredMessage message, String peerAddress, IPeerDataChange peer)
    {
        super(peer);
        m_msg = message;
        m_peerAddress = peerAddress;
    }

    @Override
    public void run()
    {
        Chunk chunkKey = new Chunk(m_msg.getFileId(), m_msg.getChunkNo());

        if (m_peerAccess.isHomeChunk(chunkKey))
        {
            m_peerAccess.addHomeChunkIP(chunkKey, m_peerAddress);
        }
        else if (m_peerAccess.isStoredChunk(chunkKey))
        {
            m_peerAccess.addStoredChunkIP(chunkKey, m_peerAddress);
        }

        // If I'm not related to this chunk, ignore it
    }
}
