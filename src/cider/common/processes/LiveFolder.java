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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Queue;

/**
 * A live folder is basically something that contains SourceDocuments and other
 * live folders. I gave it the name LiveFolder to remind people that this isn't
 * a folder in the operating system's directory structure, and the documents in
 * the folder are being updated in real-time. The LiveFolder is a SourceDocument
 * and LiveFolder factory.
 * 
 * @author Lawrence
 * 
 */
public class LiveFolder
{
    public String name;
    private Hashtable<String, SourceDocument> documents = new Hashtable<String, SourceDocument>();
    private Hashtable<String, LiveFolder> folders = new Hashtable<String, LiveFolder>();
    private String owner;

    public LiveFolder(String name, String owner)
    {
        this.owner = owner;
        this.name = name;
    }

    /**
     * Manufactures a SourceDocument and remembers it, then returns it
     * 
     * @param name
     *            of the document
     * @return the document
     * @author Lawrence
     * @param  
     */
    public SourceDocument makeDocument(String name, long creationTime)
    {
        SourceDocument sourceDocument = new SourceDocument(name);
        sourceDocument.setCreationTime(creationTime);
        this.documents.put(name, sourceDocument);
        return sourceDocument;
    }

    /**
     * Add a SourceDocument to a folder
     * 
     * @param doc
     *            Document to add
     * @author Andrew
     */
    public void addDocument(SourceDocument doc)
    {
        this.documents.put(doc.name, doc);
    }

    /**
     * Manufactures a LiveFolder and remembers it, then returns it
     * 
     * @param name
     * @return the live folder
     * @author Lawrence
     */
    public LiveFolder makeFolder(String name)
    {
        LiveFolder folder = new LiveFolder(name, this.owner);
        this.folders.put(name, folder);
        return folder;
    }

    /**
     * Gets a sub-document (doesn't look in sub-folders)
     * 
     * @param name
     * @return
     * @author Lawrence
     */
    public SourceDocument getDocument(String name)
    {
        return this.documents.get(name);
    }

    /**
     * Gets a folder (doesn't look in sub-folders)
     * 
     * @param name
     * @return
     * @author Lawrence
     */
    public LiveFolder getFolder(String name)
    {
        return this.folders.get(name);
    }

    /**
     * Writes folders and source documents (serialized) to disk.
     * 
     * @param path
     *            The fully qualified path to create the directory tree under.
     */
    public void writeToDisk(File path)
    {
        if (!path.exists())
            path.mkdir();

        for (SourceDocument doc : this.documents.values())
        {
            // Append this file to the pathname
            File file = new File(path, doc.name);

            try
            {
                // Create the file if it doesn't exist
                file.createNewFile();

                // Write the source document to the file
                FileOutputStream fos = new FileOutputStream(file);
                ObjectOutputStream out = new ObjectOutputStream(fos);
                out.writeObject(doc.simplified(Long.MAX_VALUE));
                out.close();
                fos.close();
            }
            catch (IOException e)
            {

                e.printStackTrace();
            }
        }

        for (LiveFolder folder : this.folders.values())
        {
            File dir = new File(path, folder.name);

            // Recursively call the writing method under each directory
            folder.writeToDisk(dir);
        }
    }

    /**
     * 
     * @param indent
     *            - a string that contains tabs which shifts the returned text
     * @return uses recursion to build up an XML representation of the
     *         LiveFolder and its sub-folders.
     * @author Lawrence
     */
    public String xml(String indent)
    {
        String str = "";
        boolean root = indent.equals("");
        if (root)
        {
            str += "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n";
            str += "<Tree>\n";
            indent += "\t";
        }

        for (SourceDocument doc : this.documents.values())
            str += indent + "<Doc>\n" + doc.docFieldXML(indent + "\t") + indent + "</Doc>\n";
        for (LiveFolder folder : this.folders.values())
            str += indent + "<Sub>\n" + indent + "\t" + "<Folder>"
                    + folder.name + "</Folder>\n" + folder.xml(indent + "\t")
                    + indent + "</Sub>\n";

        if (root)
            str += "</Tree>\n";
        return str;
    }

    public static void main(String[] args)
    {
        LiveFolder folder = new LiveFolder("root", "test owner");
        folder.makeDocument("t1", 0);
        folder.makeFolder("testFolder").makeDocument("t2", 0);
        // System.out.println(folder.xml(""));
    }

    /**
     * 
     * @param dest
     *            the path to the source document separated by backslashes
     * @return the source document at that path
     * @author Lawrence
     */
    public SourceDocument path(String dest)
    {
        String[] split = dest.split("\\\\");
        // TODO: Changed so don't need this file extension by Andrew
        // if (split[0].endsWith(".SourceDocument"))
        if (split.length == 1)
            return this.getDocument(split[0]);// .split("\\.")[0]);
        else
        {
            LiveFolder folder = this.getFolder(split[0]);
            String newPath = dest.substring(split[0].length() + 1);
            return folder.path(newPath);
        }
    }

    /**
     * Removes all the folders and documents from this live folder
     * 
     * @author Lawrence
     */
    public void removeAllChildren()
    {
        this.folders.clear();
        this.documents.clear();
    }

    /**
     * returns all events for this folder
     * 
     * @param time
     * @param path
     * @return
     * @author Lawrence
     */
    public Queue<LocalisedTypingEvents> eventsSince(long time, String path)
    {
        Queue<LocalisedTypingEvents> events = new LinkedList<LocalisedTypingEvents>();
        String local = path + this.name;
        LocalisedTypingEvents ltes;
        for (SourceDocument document : this.documents.values())
        {
            ltes = new LocalisedTypingEvents(local + "\\" + document.name);
            ltes.typingEvents.addAll(document.eventsSince(time));
            events.add(ltes);
        }
        for (LiveFolder folder : this.folders.values())
            events.addAll(folder.eventsSince(time, local + "\\"));
        return events;
    }

    /**
     * Goes through a path and finds the corresponding live folder object.
     * 
     * It will be created if it doesn't exist.
     * 
     * @param path
     *            The remainder of the path we are finding.
     * @param current
     *            The current LiveFolder we are in.
     * @return The LiveFolder.
     * 
     * @author Andrew
     */
    public static LiveFolder findFolder(String path, LiveFolder current)
    {
        String part;
        // Trim trailing slash
        if (path.endsWith("\\"))
            path = path.substring(0, path.length() - 1);

        while (path.length() > 0)
        {
            if (path.indexOf("\\") == -1)
            {
                if (current.getFolder(path) == null)
                    current.makeFolder(path);
                return current.getFolder(path);
            }
            else
            {
                part = path.substring(0, path.indexOf("\\"));
                if (current.getFolder(part) == null)
                    current = current.makeFolder(part);
                else
                    current = current.getFolder(part);

                path = path.substring(part.length() + 1);
            }
        }

        return current;
    }
}
