package net.services;

import net.IPeerDataChange;
import net.chunks.BackupFile;
import net.chunks.ChunkNo;
import net.chunks.Version;
import net.messages.GetChunkMessage;
import net.messages.Header;

/**
 * Created by Miguel on 30-03-2015.
 */
public class RestoreService extends UserService
{
    private Object chunkReceived = new Object();

    public RestoreService(BackupFile file, IPeerDataChange peer)
    {
        super(file, peer);
    }

    @Override
    public void run()
    {
        for (int chunkNo = 0; chunkNo < m_file.getNumberChunks(); chunkNo++)
        {
            // Create get chunk message:
            GetChunkMessage message = new GetChunkMessage(new Version('1','0'), m_file.getFileId(), new ChunkNo(chunkNo));
            Header header = new Header();
            header.addMessage(message);
            m_peerAccess.sendHeaderMC(header);


        }
    }
}
