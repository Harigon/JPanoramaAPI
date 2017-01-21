// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 

import java.applet.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;

public class ptcontext
    implements AppletContext
{

    public ptcontext(Frame frame)
    {
        f1 = frame;
    }

    public void showStatus(String s)
    {
        if(s != null && s.length() > 0)
        {
            Applet applet;
            Dimension dimension = (applet = (Applet)f1.getComponent(0)).getSize();
            Graphics g = applet.getGraphics();
            applet.setBackground(Color.lightGray);
            g.clearRect(0, dimension.height - 14, dimension.width, 14);
            g.drawString(s, 10, dimension.height - 2);
        }
    }

    public AudioClip getAudioClip(URL url)
    {
        return null;
    }

    public void showDocument(URL url)
    {
        new ptvjapp(url.getFile());
    }

    public void showDocument(URL url, String s)
    {
        showDocument(url);
    }

    public Applet getApplet(String s)
    {
        return null;
    }

    public Enumeration getApplets()
    {
        return null;
    }

    public Image getImage(URL url)
    {
        return Toolkit.getDefaultToolkit().getImage(url);
    }

    private Frame f1;

	@Override
	public InputStream getStream(String key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterator<String> getStreamKeys() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setStream(String key, InputStream stream) throws IOException {
		// TODO Auto-generated method stub
		
	}
}