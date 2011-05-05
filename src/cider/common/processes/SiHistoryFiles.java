package cider.common.processes;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.NavigableMap;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.Map.Entry;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import cider.client.gui.CiderApplication;

public class SiHistoryFiles
{
    private static boolean working = true;
    private static boolean showStackTrace = CiderApplication.debugApp;
    public static final String localEventFolderPath = System.getenv("APPDATA")
            + "\\cider\\localhistory\\";
    private static final String opened = "Opened ";
    private static final String closed = "Closed ";

    private static void notWorking(String message)
    {
        System.err.println("Cannot " + message + " because history files are not working - ( showStackTrace = " + showStackTrace + ") continuing...");
    }
    
    private static Set<String> times(Collection<TypingEvent> typingEvents)
    {
        Set<String> results = new LinkedHashSet<String>();
        for (TypingEvent te : typingEvents)
            results.add("" + te.time);
        return results;
    }

    private static Set<String> eventTimesExistsInFile(String documentPath,
            Set<String> times) throws IOException
    {
        FileInputStream fstream = new FileInputStream(localEventFolderPath
                + documentPath);
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
            for (String time : times)
            {
                if (strLine.startsWith(time))
                {
                    start = time.length();
                    if (strLine.substring(start,
                            strLine.indexOf(' ', start + 1)).equals(
                            TypingEventMode.homogenized.toString()))
                    {
                        foundMatch = true;
                        results.add(time);
                        break;
                    }
                }
                i++;
            }

            if (foundMatch)
                times.remove(i);
        }
        in.close();
        return results;
    }

    public static void saveEvents(PriorityQueue<TypingEvent> typingEvents,
            String documentPath)
    {
        try
        {
            if(working)
            {
                File f = openFileForWriting(documentPath);
                Set<String> matches = eventTimesExistsInFile(documentPath,
                        times(typingEvents));
                FileWriter fstream = new FileWriter(f, true);
                BufferedWriter out = new BufferedWriter(fstream);
    
                for (TypingEvent typingEvent : typingEvents)
                    if (!matches.contains(Long.toString(typingEvent.time, TypingEvent.radix)))
                        out.write(typingEvent.pack() + "\n");
    
                out.close();
            }
            else
                notWorking("save history events");
        }
        catch (IOException e)
        {
            working = false;
            if(showStackTrace)
                e.printStackTrace();
            notWorking("save history events");
            JOptionPane.showMessageDialog(new JPanel(),
                    ("Error: " + e.getMessage()));
            return;
        }
        catch (NullPointerException e)
        {
            working = false;
            if(showStackTrace)
                e.printStackTrace();
            notWorking("save history events");
            JOptionPane.showMessageDialog(new JPanel(),
                    "Error: There is no document open!");
            return;
        }
        catch(Exception e)
        {
            if(showStackTrace)
                e.printStackTrace();
            working = false;
            notWorking("save history events");
        }
    }

    private static File openFileForWriting(String documentPath)
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
            if(working)
            {
                f = openFileForWriting(path);
                FileWriter fstream = new FileWriter(f, true);
                BufferedWriter out = new BufferedWriter(fstream);
                out.write("Opened " + Long.toString(time, TypingEvent.radix) + " " + path + "\n");
                out.close();
            }
            else
                notWorking("mark document history opening");
        }
        catch (Exception e)
        {
            if(showStackTrace)
                e.printStackTrace();
            working = false;
            notWorking("mark document history opening");
        }
    }

    public static void markDocumentClosing(String path, long time)
    {
        File f;
        try
        {
            if(working)
            {
                f = openFileForWriting(path);
                FileWriter fstream = new FileWriter(f, true);
                BufferedWriter out = new BufferedWriter(fstream);
                out.write("Closed " + Long.toString(time, TypingEvent.radix) + " " + path + "\n");
                out.close();
            }
            else
                notWorking("mark document history closing");
        }
        catch (Exception e)
        {
            if(showStackTrace)
                e.printStackTrace();
            working = false;
            notWorking("mark document history closing");
        }
    }

    public static ArrayList<Long> getBorderTimes(String path)
    {
        try
        {
            if(working)
            {
                ArrayList<Long> times = new ArrayList<Long>();
                FileInputStream fstream;
                fstream = new FileInputStream(localEventFolderPath + path);
                DataInputStream in = new DataInputStream(fstream);
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                String strLine;
                int a = closed.length();
                int b = opened.length();
                Long t;
                while ((strLine = br.readLine()) != null)
                {
                    t = readBorderTime(strLine, a, b);
                    if(t != null)
                        times.add(t);
                }
    
                in.close();
                return times;
            }
            else
                notWorking("get history border times");
        }
        catch (Exception e)
        {
            if(showStackTrace)
                e.printStackTrace();
            working = false;
            notWorking("get history border times");
        }
        
        return null;
    }

    private static Long readBorderTime(String strLine,
            int a, int b)
    {
        if (strLine.startsWith(closed))
            return Long.parseLong(strLine.substring(a,
                    strLine.indexOf(' ', a)), TypingEvent.radix);
        else if (strLine.startsWith(opened))
            return Long.parseLong(strLine.substring(b,
                    strLine.indexOf(' ', b)), TypingEvent.radix);
        else
            return null;
    }
    

    public static void getEvents(String path, TimeBorderList tbl)
    {
        try
        {
            if(working)
            {
                FileInputStream fstream;
                fstream = new FileInputStream(localEventFolderPath + path);
                DataInputStream in = new DataInputStream(fstream);
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                String strLine;
                TimeBorder border;
                TypingEvent te;
                int a = closed.length();
                int b = opened.length();
                Queue<Entry<Long, TimeBorder>> borderQueue = tbl.borderList();
                border = borderQueue.poll().getValue();
                
                String prevLine = null;
                
                while ((strLine = br.readLine()) != null)
                {
                    if(prevLine != null)
                    {
                        strLine = prevLine + strLine;
                        prevLine = null;
                    }
                    
                    if(strLine.endsWith("~"))
                        prevLine = strLine + "\n";
                    else
                    {
                        if(!strLine.startsWith(opened) && !strLine.startsWith(closed))
                        {
                            te = new TypingEvent(strLine);
                            if(te.text.equals("\n"))
                                System.out.println("newline");
                            else
                                System.out.printf(te.text + "\t" + strLine + "\n");
                            
                            border.typingEvents.add(te);
                        }
                        else
                        {
                            System.out.println("Border " + readBorderTime(strLine, a, b) + "\t" + strLine);
                            if(borderQueue.size() > 0)
                                border = borderQueue.poll().getValue();
                            else
                                return;
                        }
                    }
                }
            }
            else
                notWorking("get history events");
        }
        catch (Exception e)
        {
            if(showStackTrace)
                e.printStackTrace();
            
            working = false;
            notWorking("get history events");
        }
    }

    public static void clearAllHistory()
    {
        if(working)
        {
            try
            {
                File file = new File(SiHistoryFiles.localEventFolderPath + "\\");
                deleteSubFiles(file);
            }
            catch(Exception e)
            {
                if(showStackTrace)
                    e.printStackTrace();
                working = false;
                notWorking("clear all history");
            }
        }
        else
        {
            notWorking("clear all history");
        }
    }
    
    private static void deleteSubFiles(File file)
    {
        for(File f : file.listFiles())
        {
            if(f.isDirectory())
                deleteSubFiles(f);
            else
            {
                System.out.println(f.exists());
                System.out.println(f.delete());
            }
        }
    }
}
