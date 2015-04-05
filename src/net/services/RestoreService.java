package net.services;

import net.IPeerDataChange;
import net.chunks.BackupFile;
import net.chunks.Chunk;
import net.chunks.ChunkNo;
import net.chunks.Version;
import net.messages.*;
import net.tasks.ReceiveChunkTcpTask;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Miguel on 30-03-2015.
 */
public class RestoreService extends UserService implements Serializable
{
    private ArrayList<Chunk> m_chunkList = new ArrayList<>();
    private Chunk m_currentChunk;

    public RestoreService(BackupFile file, IPeerDataChange peer)
    {
        super(file, peer);
    }

    @Override
    public boolean wantsMessage(Message message, byte[] body)
    {
        synchronized (this)
        {
            // If the message brings the chunk I want, fill the body and add him to the list
            if (m_currentChunk != null
                    && message.getType().equals(ChunkMessage.s_TYPE)
                    && message.getFileId().equals(m_currentChunk.getFileId())
                    && ((ChunkMessage) message).getChunkNo().equals(m_currentChunk.getChunkNo()))
            {
                setBody(body);
                return true;
            } else
                return false;
        }
    }

    public void setBody(byte[] body)
    {
        synchronized (this)
        {
            m_currentChunk.setData(body);
            m_chunkList.add(m_currentChunk);
            m_currentChunk = null;

            // notify the run() that this chunk is ready!
            notify();
        }
    }

    @Override
    public void run()
    {
        synchronized (this)
        {
            for (int chunkNo = 0; chunkNo < m_file.getNumberChunks(); chunkNo++)
            {
                try
                {
                    System.out.println("RestoreService::run begin!");

                    // Create task:
                    ReceiveChunkTcpTask receiveChunkTcpTask = new ReceiveChunkTcpTask(this);
                    int serverSocketPort = receiveChunkTcpTask.getServerSocketPort();

                    // Create get chunk message:
                    m_currentChunk = new Chunk(m_file.getFileId(), new ChunkNo(chunkNo));
                    GetChunkMessage message = new GetChunkMessage(new Version('1', '0'), m_file.getFileId(), new ChunkNo(chunkNo));
                    Header header = new Header();
                    header.addMessage(message);
                    header.addMessage(new TcpAvailableMessage(serverSocketPort));
                    m_peerAccess.sendHeaderMC(header);

                    // Start task to listen for the body if the peer supports the enhanced mode:
                    Thread thread = new Thread(receiveChunkTcpTask);
                    thread.start();

                    // Wait for chunk message:
                    wait();
                    thread.interrupt();

                    System.out.println("RestoreService::run end!");
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    System.err.println("Error IO Exception in Restore Service");
                    System.exit(-3);
                }
                catch (InterruptedException e)
                {
                    System.err.println("Error waiting in RestoreService");
                }
            }

            // Recover file:
            m_file.recoverFromChunks(m_chunkList);

            System.out.println("Restore Service - A restore ended successfuly!");
        }

        // End service:
        m_peerAccess.removeUserService(this);
    }
}
