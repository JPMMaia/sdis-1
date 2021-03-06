package net.messages;

import net.chunks.ChunkNo;
import net.chunks.FileId;
import net.chunks.ReplicationDeg;
import net.chunks.Version;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Jo�o on 26/03/2015.
 */
public class MessagesTester
{
    private static final String s_CRLF = "\r\n";
    private static final String s_MESSAGE_1 = "PUTCHUNK <Version> <FileId> <ChunkNo> <ReplicationDeg>";
    private static final String s_MESSAGE_2 = "PUTCHUNK2   <Version2>   <FileId2>   <ChunkNo2>   <ReplicationDeg2>  ";
    private static final String s_MESSAGE_3 = "PUTCHUNK 2.4 File_2_4 4 7";
    private static final String s_BODY_1 = "Ola Bifes!";
    private static final String s_HEADER_1 = s_MESSAGE_1 + s_CRLF + s_CRLF;
    private static final String s_HEADER_2 = s_MESSAGE_1 + s_CRLF + s_MESSAGE_2 + s_CRLF + s_CRLF;
    private static final String s_HEADER_3 = s_MESSAGE_3 + s_CRLF + s_CRLF + s_BODY_1;

    @Test
    public void testHeaderSplit()
    {
        String[] split1 = Header.splitMessages(s_HEADER_1);
        Assert.assertEquals(1, split1.length);
        Assert.assertEquals(s_MESSAGE_1, split1[0]);

        String[] split2 = Header.splitMessages(s_HEADER_2);
        Assert.assertEquals(2, split2.length);
        Assert.assertEquals(s_MESSAGE_1, split2[0]);
        Assert.assertEquals(s_MESSAGE_2, split2[1]);
    }

    @Test
    public void testMessageSplit()
    {
        String[] split1 = Message.splitMessage(s_MESSAGE_1);
        Assert.assertEquals("PUTCHUNK", split1[0]);
        Assert.assertEquals("<Version>", split1[1]);
        Assert.assertEquals("<FileId>", split1[2]);
        Assert.assertEquals("<ChunkNo>", split1[3]);
        Assert.assertEquals("<ReplicationDeg>", split1[4]);

        String[] split2 = Message.splitMessage(s_MESSAGE_2);
        Assert.assertEquals("PUTCHUNK2", split2[0]);
        Assert.assertEquals("<Version2>", split2[1]);
        Assert.assertEquals("<FileId2>", split2[2]);
        Assert.assertEquals("<ChunkNo2>", split2[3]);
        Assert.assertEquals("<ReplicationDeg2>", split2[4]);
    }

    @Test
    public void testMessageParsing()
    {
        String[] split3 = Message.splitMessage(s_MESSAGE_3);
        PutChunkMessage message = PutChunkMessage.createMessage(split3);

        Version version = message.getVersion();
        Assert.assertEquals('2', version.getVersion());
        Assert.assertEquals('4', version.getSubVersion());

        FileId fileId = message.getFileId();
        Assert.assertEquals("File_2_4", fileId.getValue());

        ChunkNo chunkNo = message.getChunkNo();
        Assert.assertEquals(4, chunkNo.getValue());

        ReplicationDeg replicationDeg = message.getReplicationDeg();
        Assert.assertEquals(7, replicationDeg.getValue());
    }

    @Test
    public void testHeaderParsing()
    {
        byte[] data = s_HEADER_3.getBytes(Header.s_STANDARD_CHARSET);
        Header header = new Header(data, data.length);

        // Check if message is a PUTCHUNK message:
        Message message = (Message) header.getMessage(0);
        Assert.assertEquals(PutChunkMessage.s_TYPE, message.getType());

        // Test message:
        PutChunkMessage putChunkMessage = (PutChunkMessage) message;
        Assert.assertEquals(s_MESSAGE_3, putChunkMessage.toString());

        // Test body:
        Assert.assertArrayEquals(s_BODY_1.getBytes(Header.s_STANDARD_CHARSET), header.getBody());
    }
}
