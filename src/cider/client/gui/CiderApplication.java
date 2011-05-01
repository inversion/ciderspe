package cider.client.gui;

public class CiderApplication
{
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
        CiderApplication app = new CiderApplication();
        app.ui = new LoginUI(app);
        app.startCider();
    }
}
