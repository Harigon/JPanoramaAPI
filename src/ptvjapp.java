import java.applet.Applet;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Window;
import java.io.PrintStream;

public class ptvjapp extends Frame
{
  public static void main(String[] paramArrayOfString)
  {
    System.runFinalizersOnExit(true);
    String str;
    if (paramArrayOfString.length == 0) {
      str = "default.html";
    } else {
      str = paramArrayOfString[0];
    }
    if (str != null) {
      new ptvjapp(str);
    }
  }
  
  public ptvjapp(String paramString)
  {
    int i = 800;
    int j = 800;
    ptviewer localptviewer = new ptviewer();
    add("Center", localptviewer);
    localptviewer.setStub(new ptstub(paramString, this));
    String str;
    if ((str = localptviewer.getParameter("width")) != null)
    {
      System.out.println("width" + 320);
      i = Integer.parseInt(str);
    }
    if ((str = localptviewer.getParameter("height")) != null)
    {
      System.out.println("height" + 200);
      j = Integer.parseInt(str);
    }
    resize(i, j);
    if ((str = localptviewer.getParameter("name")) != null) {
      setTitle(str);
    }
    setVisible(true);
    Dimension localDimension;
    if (((localDimension = localptviewer.getSize()).width != i) || (localDimension.height != j))
    {
      int tmp203_202 = i;
      localDimension.width = (tmp203_202 + tmp203_202 - localDimension.width);
      int tmp218_216 = j;
      localDimension.height = (tmp218_216 + tmp218_216 - localDimension.height);
      resize(localDimension);
      localptviewer.resize(i, j);
    }
    localptviewer.init();
    localptviewer.start();
  }
}
