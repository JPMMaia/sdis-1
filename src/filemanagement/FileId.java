package filemanagement;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by João on 25/03/2015.
 */
public class FileId
{
    private String m_value;

    public FileId(String value)
    {
        m_value = value;
    }

    public FileId(File file, byte[] fileData) throws UnsupportedEncodingException, NoSuchAlgorithmException
    {
        String bitString = file.getAbsolutePath() + file.lastModified() + new String(fileData, "UTF-8");

        // Encrypt:
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(bitString.getBytes("UTF-8"));
        m_value = new String(md.digest(), "UTF-8");
    }

    public byte[] toBytes()
    {
        return m_value.getBytes();
    }

    public String getValue()
    {
        return m_value;
    }

    public void setValue(String value)
    {
        m_value = value;
    }
}
