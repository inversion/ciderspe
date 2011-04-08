/**
 *  CIDER - Collaborative Integrated Development EnviRonment
    Copyright (C) 2011  Andrew Moss
                        Lawrence Watkiss
                        Jonathan Bannister
                        Alex Sheppard
                        Miles Taylor
                        Ashley Woodman

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
 * @deprecated
 */
public class LocalCodeFile implements ICodeLocation
{
    private File file;
    private BufferedReader in;
    private BufferedWriter out;
    private String oldFileContentString = null;
    private TypingEvent oldFileContent;

    public LocalCodeFile(File file)
    {
        this.file = file;
    }

    public static void main(String[] args)
    {
        System.out.println(testfile());
    }

    /**
     * @return
     */
    public static String testfile()
    {
        Date date = new Date();
        File file = new File("testfile.txt");
        try
        {
            file.createNewFile();
        }
        catch (IOException e)
        {

            e.printStackTrace();
        }
        LocalCodeFile lcf = new LocalCodeFile(file);
        lcf.events();
        TypingEvent timestamp = new TypingEvent(date.getTime(),
                TypingEventMode.insert, lcf.oldFileContentString.length(), 0,
                "Test text: " + date.toString(), "owner", null);
        Queue<TypingEvent> typingEvents = new LinkedList<TypingEvent>();
        typingEvents.addAll(timestamp.explode());
        lcf.push(typingEvents);
        return lcf.read();
    }

    @Override
    public void push(Queue<TypingEvent> typingEvents)
    {
        try
        {
            this.out = new BufferedWriter(new FileWriter(this.file));
            SourceDocument sd = new SourceDocument(this.file.getName());
            sd.addEvent(this.oldFileContent);
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
        long currentTime = 0;
        // this.oldFileContent.add(new TypingEvent(currentTime,
        // TypingEventMode.deleteAll, 0, ""));
        this.oldFileContent = new TypingEvent(currentTime + 1,
                TypingEventMode.overwrite, 0, 0,
                this.oldFileContentString = this.read(), "owner", null);
        Queue<TypingEvent> result = new LinkedList<TypingEvent>();
        result.add(this.oldFileContent);
        return result;
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

    @Override
    public long lastUpdateTime()
    {
        return this.file.lastModified();
    }

    @Override
    public void clearAll()
    {
        if (this.file.exists())
        {
            this.file.delete();
            try
            {
                this.file.createNewFile();
            }
            catch (IOException e)
            {

                e.printStackTrace();
            }
        }
    }
}
