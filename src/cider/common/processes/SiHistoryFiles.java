package cider.common.processes;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class SiHistoryFiles
{
    public static final String localEventFolderPath = System.getenv("APPDATA")
        + "\\cider\\localhistory\\";
    
    public static Set<String> times(Collection<TypingEvent> typingEvents)
    {
        Set<String> results = new LinkedHashSet<String>();
        for(TypingEvent te : typingEvents)
            results.add("" + te.time);
        return results;
    }

    public static Set<String> eventTimesExistsInFile(String documentPath, Set<String> times)
            throws IOException
    {
        FileInputStream fstream = new FileInputStream(localEventFolderPath + documentPath);
        DataInputStream in = new DataInputStream(fstream);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String strLine;
        Set<String> results = new LinkedHashSet<String>();
        boolean foundMatch;
        int i;
        int start;
        while ((strLine = br.readLine()) != null)
        {
            foundMatch = false;
            i = 0;
            for(String time : times)
            {
                if (strLine.startsWith(time))
                {
                    start = time.length();
                    if(strLine.substring(start, strLine.indexOf(' ', start + 1))
                            .equals(TypingEventMode.homogenized.toString()))
                    {
                        foundMatch = true;
                        results.add(time);
                        break;
                    }
                }
                i++;
            }
            
            if(foundMatch)
                times.remove(i);
        }
        in.close();
        return results;
    }

    public static void saveEvents(Collection<TypingEvent> typingEvents,
            String documentPath)
    {
        try
        {
            File f = openFileForWriting(documentPath);
            Set<String> matches = eventTimesExistsInFile(documentPath, times(typingEvents));
            FileWriter fstream = new FileWriter(f, true);
            BufferedWriter out = new BufferedWriter(fstream);
            
            for (TypingEvent typingEvent : typingEvents)
                if(!matches.contains("" + typingEvent.time))
                    out.write(typingEvent.toString() + "\n");
            
            out.close();
        }
        catch (IOException e1)
        {
            e1.printStackTrace();
            JOptionPane.showMessageDialog(new JPanel(),
                    ("Error: " + e1.getMessage()));
            return;
        }
        catch (NullPointerException e)
        {
            e.printStackTrace();
            JOptionPane.showMessageDialog(new JPanel(),
                    "Error: There is no document open!");
            return;
        }
    }

    public static File openFileForWriting(String documentPath)
            throws IOException
    {
        File f = new File(localEventFolderPath + documentPath);
        new File(f.getParent()).mkdirs();
        f.createNewFile();
        return f;
    }

    public static void markDocumentOpening(String path, long time)
    {
        File f;
        try
        {
            f = openFileForWriting(path);
            FileWriter fstream = new FileWriter(f, true);
            BufferedWriter out = new BufferedWriter(fstream);
            out.write("Opened " + time + " " + path + "\n");
            out.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static void markDocumentClosing(String path, long time)
    {
        File f;
        try
        {
            f = openFileForWriting(path);
            FileWriter fstream = new FileWriter(f, true);
            BufferedWriter out = new BufferedWriter(fstream);
            out.write("Closed " + time + " " + path + "\n");
            out.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}