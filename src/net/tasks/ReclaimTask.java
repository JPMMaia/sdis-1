package net.tasks;

import net.IPeerDataChange;
import net.Utils.RandomNumber;
import net.chunks.Chunk;
import net.chunks.Version;
import net.messages.ChunkMessage;
import net.messages.Header;
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
    public synchronized boolean wantsMessage(Message message, byte[] body)
    {
        // Check if we received a PutChunkMessage on the chunk we want to send: if so, we don't need to send it:
        if (message.getType().equals(PutChunkMessage.s_TYPE)
                && message.getFileId().equals(m_storedChunk.getFileId())
                && ((ChunkMessage) message).getChunkNo().equals(m_storedChunk.getChunkNo()))
        {
            m_initiateBackup = false;
            notify();
            return true;
        }
        else
            return false;
    }

    @Override
    public synchronized void run()
    {
        try
        { wait(RandomNumber.getInt(0, 400)); }
        catch (InterruptedException e)
        { e.printStackTrace();
            System.err.println("Error waiting in ReclaimTask"); System.exit(-2); }

        // Send the chunk he wants:
        if (m_initiateBackup)
        {
            System.out.println("Resolvi iniciar o backup após o reclaim! Boraaa!");
            new Thread(new PutChunkTask(m_storedChunk, m_peerAccess)).start();
        }
        else
            System.out.println("N vou fazer o backup depois do reclaim :(");

        m_peerAccess.removeTask(this);
    }
}
