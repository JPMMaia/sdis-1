package net.services;

import net.IPeerDataChange;
import net.chunks.BackupFile;
import net.chunks.Chunk;
import net.chunks.ChunkNo;
import net.chunks.Version;
import net.messages.ChunkMessage;
import net.messages.GetChunkMessage;
import net.messages.Header;
import net.messages.Message;

import java.util.ArrayList;

/**
 * Created by Miguel on 30-03-2015.
 */
public class RestoreService extends UserService
{
    private ArrayList<Chunk> m_chunkList = new ArrayList<>();
    private Chunk m_currentChunk;

    public RestoreService(BackupFile file, IPeerDataChange peer)
    {
        super(file, peer);
    }

    @Override
    public synchronized boolean wantsMessage(Message message, byte[] body)
    {
        // If the message brings the chunk I want, fill the body and add him to the list
        if (m_currentChunk != null
                && message.getType().equals(ChunkMessage.s_TYPE)
                && message.getFileId().equals(m_currentChunk.getFileId())
                && ((ChunkMessage) message).getChunkNo().equals(m_currentChunk.getChunkNo()))
        {
            m_currentChunk.setData(body);
            m_chunkList.add(m_currentChunk);
            m_currentChunk = null;

            // notify the run() that this chunk is ready!
            notify();
            return true;
        }
        else
            return false;
    }

    @Override
    public synchronized void run()
    {
        for (int chunkNo = 0; chunkNo < m_file.getNumberChunks(); chunkNo++)
        {
            // Create get chunk message:
            m_currentChunk = new Chunk(m_file.getFileId(), new ChunkNo(chunkNo));
            GetChunkMessage message = new GetChunkMessage(new Version('1','0'), m_file.getFileId(), new ChunkNo(chunkNo));
            Header header = new Header();
            header.addMessage(message);
            m_peerAccess.sendHeaderMC(header);

            // Wait for chunk message:
            try
            { wait(); }
            catch (InterruptedException e)
            { e.printStackTrace();
                System.err.println("Error waiting in RestoreService"); System.exit(-2); }
        }

        // Recover file:
        m_file.recoverFromChunks(m_chunkList);

        System.out.println("RecoverService - A backup ended successfuly!");

        // End service:
        m_peerAccess.removeUserService(this);
    }


}
