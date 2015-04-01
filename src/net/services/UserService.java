package net.services;

import net.IPeerDataChange;
import net.chunks.BackupFile;
import net.chunks.FileId;
import net.messages.Header;
import net.messages.Message;

/**
 * Created by Miguel on 30-03-2015.
 */
public abstract class UserService implements Runnable
{
    protected BackupFile m_file;
    protected IPeerDataChange m_peerAccess;

    public UserService(BackupFile file, IPeerDataChange peer)
    {
        m_file = file;
        m_peerAccess = peer;
    }

    public FileId getFileId()
    {
        return m_file.getFileId();
    }

    public boolean wantsMessage(Message message, byte[] body)
    {
        return false;
    }
}
