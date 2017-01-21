// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 

import java.applet.*;
import java.awt.*;
import java.io.*;
import java.net.URL;
import java.util.Hashtable;

public class ptstub
    implements AppletStub
{

    public ptstub(ptviewer ptviewer1, String s)
    {
        f3 = null;
        f4 = null;
        f5 = null;
        f6 = null;
        f4 = ptviewer1;
        f2 = s;
    }

    public ptstub(Applet applet, Frame frame)
    {
        f3 = null;
        f4 = null;
        f5 = null;
        f6 = null;
        f3 = applet;
        f1 = new Hashtable();
        f5 = new ptcontext(frame);
    }

    public ptstub(String s, Frame frame)
    {
    	
    	
        f3 = null;
        f4 = null;
        f5 = null;
        f6 = null;
        f1 = new Hashtable();
        f5 = new ptcontext(frame);
        try
        {
            String s1 = s;
            int i;
            if((i = s.lastIndexOf(File.separatorChar)) >= 0)
                s1 = s.substring(i + 1);
            Class class1;
            (class1 = Class.forName("ptviewer")).getResource(s1);
            f6 = System.getProperty("user.dir");
            if(s1.toLowerCase().endsWith(".jpeg") || s1.toLowerCase().endsWith(".jpg") || s1.toLowerCase().endsWith(".jpa") || s1.toLowerCase().endsWith(".jpb") || s1.toLowerCase().endsWith(".jpc") || s1.toLowerCase().endsWith(".mov"))
            {
                f1.put("file", s1);
                return;
            } else
            {
                InputStream inputstream = class1.getResourceAsStream(s1);
                byte abyte0[] = readFile(inputstream, 0);
                inputstream.close();
                m1(new String(abyte0));
                return;
            }
        }
        catch(Exception _ex) { }
        File file;
        if(!(file = new File(s)).exists())
        {
            FileDialog filedialog;
            (filedialog = new FileDialog(frame, "Load Panorama...")).show();
            if((s = filedialog.getFile()) != null)
                file = new File(filedialog.getDirectory() + s);
            else
                System.exit(0);
        }
        if(!file.exists())
            System.exit(0);
        try
        {
            f6 = (new File(file.getCanonicalPath())).getParent();
        }
        catch(Exception _ex) { }
        if(file.getName().toLowerCase().endsWith(".jpeg") || file.getName().toLowerCase().endsWith(".jpg") || file.getName().toLowerCase().endsWith(".jpa") || file.getName().toLowerCase().endsWith(".jpb") || file.getName().toLowerCase().endsWith(".jpc") || file.getName().toLowerCase().endsWith(".mov"))
        {
        	
            f1.put("file", file.getName());
            return;
        }
        try
        {
            FileReader filereader = new FileReader(file);
            char ac[] = new char[(int)file.length()];
            filereader.read(ac, 0, (int)file.length());
            filereader.close();
            m1(new String(ac));
            return;
        }
        catch(Exception _ex)
        {
            System.exit(0);
        }
    }

    public void appletResize(int i, int j)
    {
        if(f3 != null)
            f3.resize(i, j);
    }

    public AppletContext getAppletContext()
    {
        return f5;
    }

    public URL getCodeBase()
    {
        if(f4 != null)
            return f4.getCodeBase();
        URL url = null;
        try
        {
            String s = "file:" + f6;
            StringBuffer stringbuffer = new StringBuffer();
            for(int i = 0; i < s.length(); i++)
                if(s.charAt(i) != ' ')
                    stringbuffer.append(s.charAt(i));
                else
                    stringbuffer.append("%20");

            stringbuffer.append("/");
            url = new URL(stringbuffer.toString());
        }
        catch(Exception _ex) { }
        return url;
    }

    public URL getDocumentBase()
    {
        if(f4 != null)
            return f4.getDocumentBase();
        else
            return getCodeBase();
    }

    public String getParameter(String s)
    {
        if(f4 == null)
            return (String)f1.get(s);
        else
            return f4.myGetParameter(f2, s);
    }

    public boolean isActive()
    {
        return true;
    }

    private void m1(String s)
    {
    	
        int k1 = 0;
        String s5 = null;
        for(int i = 0; i < s.length() && s5 == null;)
            if(s.regionMatches(true, i, "<applet", 0, 7))
            {
                for(k1 = i += 7; k1 < s.length() && s.charAt(k1) != '>'; k1++);
                s5 = s.substring(i, k1);
            } else
            {
                i++;
            }

        if(s5 != null)
        {
            for(int i1 = 0; i1 < s5.length();)
            {
                while(Character.isWhitespace(s5.charAt(i1)) && i1 < s5.length()) 
                    i1++;
                if(i1 < s5.length())
                {
                    String s3 = s5.substring(i1, s5.substring(i1).indexOf('=') + i1);
                    i1 += s5.substring(i1).indexOf('=') + 1;
                    int j;
                    String s1;
                    if(s5.charAt(i1) == '"')
                    {
                        i1++;
                        s1 = s5.substring(i1, s5.substring(i1).indexOf('"') + i1);
                        i1 += s5.substring(i1).indexOf('"') + 1;
                    } else
                    if((j = s5.substring(i1).indexOf(' ')) >= 0)
                    {
                        s1 = s5.substring(i1, j + i1);
                        i1 += j + 1;
                    } else
                    {
                        s1 = s5.substring(i1);
                        i1 = s5.length();
                    }
                    f1.put(s3, s1);
                }
            }

            String s6 = null;
            int k = ++k1;
            while(k1 < s.length() && s6 == null) 
                if(s.regionMatches(true, k1, "</applet>", 0, 9))
                    s6 = s.substring(k, k1);
                else
                    k1++;
            if(s6 != null)
            {
                String s2;
                String s4;
                for(int j1 = 0; j1 < s6.length(); f1.put(s4, s2))
                {
                    while(j1 < s6.length() && !s6.regionMatches(true, j1, "<param", 0, 6)) 
                        j1++;
                    if(j1 >= s6.length())
                        break;
                    int l1;
                    for(l1 = j1 += 6; l1 < s6.length() && s6.charAt(l1) != '>'; l1++);
                    String s7;
                    if(s6.charAt(l1) == '>')
                    {
                        s7 = s6.substring(j1, l1);
                        j1 = l1 + 1;
                    } else
                    {
                        return;
                    }
                    int l;
                    for(l = 0; l < s7.length() && !s7.regionMatches(true, l, "name=", 0, 5); l++);
                    if(l >= s7.length())
                    {
                        System.out.println("Error in parameter tag: " + s7 + "  No name tag");
                        return;
                    }
                    l1 = l += 5;
                    for(; l < s7.length() && !Character.isWhitespace(s7.charAt(l)); l++);
                    if(l >= s7.length())
                    {
                        System.out.println("Error in parameter tag: " + s7 + "  No value tag");
                        return;
                    }
                    s4 = s7.substring(l1, l);
                    for(; l < s7.length() && !s7.regionMatches(true, l, "value=", 0, 6); l++);
                    if(l >= s7.length())
                    {
                        System.out.println("Error in parameter tag: " + s7 + "  No value tag");
                        return;
                    }
                    l += 6;
                    if(s7.charAt(l) == '"')
                    {
                        l++;
                        s2 = s7.substring(l, s7.substring(l).indexOf('"') + l);
                        int _tmp = l;
                        s7.substring(l).indexOf('"');
                    } else
                    if((l1 = s7.substring(l).indexOf(' ')) >= 0)
                        s2 = s7.substring(l, l1 + l);
                    else
                        s2 = s7.substring(l);
                }

            }
        }
    }

    byte[] readFile(InputStream inputstream, int i)
    {
        int j = 0;
        int l = 0;
        int i1 = i <= 0 ? 50000 : i / 10 + 1;
        byte abyte0[] = new byte[i <= 0 ? 50000 : i];
        try
        {
            while(l != -1) 
            {
                int k = 0;
                if(abyte0.length < j + i1)
                {
                    byte abyte1[] = new byte[j + i1];
                    System.arraycopy(abyte0, 0, abyte1, 0, j);
                    abyte0 = abyte1;
                }
                while(k < i1 && (l = inputstream.read(abyte0, j, i1 - k)) != -1) 
                {
                    k += l;
                    j += l;
                }
            }
            if(abyte0.length > j)
            {
                byte abyte2[] = new byte[j];
                System.arraycopy(abyte0, 0, abyte2, 0, j);
                abyte0 = abyte2;
            }
        }
        catch(Exception _ex)
        {
            return null;
        }
        return abyte0;
    }

    private Hashtable f1;
    private String f2;
    private Applet f3;
    private ptviewer f4;
    private AppletContext f5;
    private String f6;
}