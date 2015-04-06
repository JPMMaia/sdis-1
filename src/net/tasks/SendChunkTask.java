package net.tasks;

import net.IPeerDataChange;
import net.utils.RandomNumber;
import net.chunks.Chunk;
import net.chunks.Version;
import net.messages.*;

/**
 * Created by Miguel on 01-04-2015.
 */
public class SendChunkTask extends Task
{
    private GetChunkMessage m_msg;
    private Chunk m_chunkToSend;
    private boolean m_sendChunk = true;
    private String m_peerAddress;

    public SendChunkTask(GetChunkMessage message, Chunk chuckToSend, String peerAddress, IPeerDataChange peer)
    {
        super(peer);
        m_msg = message;
        m_chunkToSend = chuckToSend;
        m_peerAddress = peerAddress;
    }

    @Override
    public boolean wantsMessage(Message message, byte[] body)
    {
        synchronized (this)
        {
            // Chunk response to the get chunk message - check if we received a chunk message:
            if (message.getType().equals(ChunkMessage.s_TYPE)
                    && message.getFileId().equals(m_msg.getFileId())
                    && ((ChunkMessage) message).getChunkNo().equals(m_msg.getChunkNo()))
            {
                m_sendChunk = false;
                notify();
                return true;
            } else
                return false;
        }
    }

    @Override
    public void run()
    {
        synchronized (this)
        {
            try
            {
                wait(RandomNumber.getInt(0, 400));
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
                System.err.println("Error waiting in GetChunkTask");
                System.exit(-2);
            }

            // Send the chunk he wants:
            if (m_sendChunk)
            {
                ChunkMessage message = new ChunkMessage(new Version('1', '0'), m_msg.getFileId(), m_msg.getChunkNo());
                Header header = new Header();
                header.addMessage(message);
                header.setBody(m_chunkToSend.getData());

                m_peerAccess.sendHeaderMDR(header);
            }
        }

        m_peerAccess.removeTask(this);
    }
}
