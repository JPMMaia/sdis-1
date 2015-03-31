package net.tasks;

import net.IPeerDataChange;
import net.chunks.Chunk;
import net.messages.PutChunkMessage;

/**
 * Created by Miguel on 31-03-2015.
 */
public class StoreTask extends Task
{
    private Chunk m_chunk;

    // TODO adapatar como em baixo
    public StoreTask(IPeerDataChange peer)
    {
        super(peer);
    }

    /*
    public PutChunkTask(PutChunkMessage message, byte[] body, String peerAddress, IPeerDataChange peer)
    {
        super(peer);
        m_chunk = chunk;
    }
    */

    @Override
    public void run()
    {

    }
}
