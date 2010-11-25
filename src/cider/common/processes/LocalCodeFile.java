package cider.common.processes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

/**
 * TBI
 * 
 * @author Lawrence
 * 
 */
public class LocalCodeFile implements ICodeLocation
{
    File file;
    private BufferedReader in;

    public LocalCodeFile(File file)
    {
        this.file = file;
    }

    @Override
    public void push(Queue<TypingEvent> typingEvents)
    {

    }

    @Override
    public Queue<TypingEvent> update()
    {
        Queue<TypingEvent> inBox = new LinkedList<TypingEvent>();
        long currentTime = System.currentTimeMillis();
        inBox.add(new TypingEvent(currentTime, TypingEventMode.deleteAll, 0, ""));
        inBox.add(new TypingEvent(currentTime + 1, TypingEventMode.overwrite,
                0, this.read()));
        return inBox;
    }

    @Override
    public void setOpen(boolean open)
    {
        if (this.in == null)
        {
            if (open)
                try
                {
                    this.in = new BufferedReader(new FileReader(this.file));
                }
                catch (FileNotFoundException e1)
                {
                    e1.printStackTrace();
                }
        }
        else if (!open)
        {
            try
            {
                this.in.close();
                this.in = null;
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean isOpen()
    {
        return this.in != null;
    }

    public String read()
    {
        try
        {
            String str;
            int ln = 1;
            String contents = "";
            while ((str = in.readLine()) != null)
                contents += str + "\n";
            in.close();
            return contents;
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }
    }
}
