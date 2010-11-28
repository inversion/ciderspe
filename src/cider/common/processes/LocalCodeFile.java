package cider.common.processes;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
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
    private File file;
    private BufferedReader in;
    private BufferedWriter out;

    public LocalCodeFile(File file)
    {
        this.file = file;
    }

    public static void main(String[] args)
    {
        System.out.println(testfile());
    }

    public static String testfile()
    {
        Date date = new Date();
        final String testStr = "the time is " + date;
        File file = new File("testfile.txt");
        try
        {
            file.createNewFile();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        LocalCodeFile lcf = new LocalCodeFile(file);
        SourceDocument sd = new SourceDocument();
        sd.push(lcf.events());
        sd.putEvents(new TypingEvent(date.getTime(), TypingEventMode.insert, sd
                .toString().length() + 1, testStr).explode().values());
        lcf.push(sd.eventsSince(date.getTime()));
        return lcf.read();
    }

    @Override
    public void push(Queue<TypingEvent> typingEvents)
    {
        try
        {
            this.out = new BufferedWriter(new FileWriter(this.file));
            SourceDocument sd = new SourceDocument();
            sd.push(this.events());
            sd.push(typingEvents);
            this.out.write(sd.toString());
            this.out.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public Queue<TypingEvent> events()
    {
        Queue<TypingEvent> inBox = new LinkedList<TypingEvent>();
        long currentTime = this.file.lastModified();
        inBox.add(new TypingEvent(currentTime, TypingEventMode.deleteAll, 0, ""));
        inBox.add(new TypingEvent(currentTime + 1, TypingEventMode.overwrite,
                0, this.read()));
        return inBox;
    }

    public String read()
    {
        try
        {
            String str;
            this.in = new BufferedReader(new FileReader(this.file));
            String contents = "";
            while ((str = this.in.readLine()) != null)
                contents += str + "\n";
            this.in.close();
            return contents;
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Queue<TypingEvent> eventsSince(long time)
    {
        if (this.file.lastModified() >= time)
            return this.events();
        else
            return new LinkedList<TypingEvent>();
    }
}
