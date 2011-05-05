package cider.client.gui;

public class CiderApplication
{
    public static boolean debugApp;
    LoginUI ui;
    
    public CiderApplication()
    {
        
    }
    
    public void startCider()
    {
        try
        {
            this.ui.displayLogin();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    

    public void restarted()
    {
        this.ui = new LoginUI(this);
        this.startCider();
    }
    
    
    public static void main(String[] args)
    {
        debugApp = true;
        for(String arg : args)
            if(arg.equals("debugapp=true"))
                debugApp = false;
        
        CiderApplication app = new CiderApplication();
        app.ui = new LoginUI(app);
        app.startCider();
    }
}
