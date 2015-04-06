package net.tasks;

import net.IPeerDataChange;
import net.utils.RandomNumber;
import net.chunks.Chunk;
import net.messages.Message;
import net.messages.PutChunkMessage;

/**
 * Created by Miguel on 04-04-2015.
 */
public class ReclaimTask extends Task
{
    Chunk m_storedChunk;
    private boolean m_initiateBackup = true;

    public ReclaimTask(Chunk storedChunk, IPeerDataChange peer)
    {
        super(peer);
        m_storedChunk = storedChunk;
    }

    @Override
    public boolean wantsMessage(Message message, byte[] body)
    {
        synchronized (this)
        {
            // Check if we received a PutChunkMessage on the chunk we want to send: if so, we don't need to send it:
            if (message.getType().equals(PutChunkMessage.s_TYPE)
                    && message.getFileId().equals(m_storedChunk.getFileId())
                    && ((PutChunkMessage) message).getChunkNo().equals(m_storedChunk.getChunkNo()))
            {
                m_initiateBackup = false;
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
                System.err.println("Error waiting in ReclaimTask");
                System.exit(-2);
            }

            // Send the chunk he wants:
            if (m_initiateBackup)
            {
                System.out.println("Let's start reclaim!");
                new Thread(new PutChunkTask(m_storedChunk, m_peerAccess)).start();
            } else
                System.out.println("We are not going to start reclaim!");

        }

        m_peerAccess.removeTask(this);
    }
}
