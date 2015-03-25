package net.messages.header;

import filemanagement.StoredFile;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by João on 25/03/2015.
 */
public class FileIdField
{
    private String m_value;

    public FileIdField(String value)
    {

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
