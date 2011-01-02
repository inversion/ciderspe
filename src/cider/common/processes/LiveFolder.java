package cider.common.processes;

import java.util.Hashtable;

public class LiveFolder
{
    public String name;
    private Hashtable<String, SourceDocument> documents = new Hashtable<String, SourceDocument>();
    private Hashtable<String, LiveFolder> folders = new Hashtable<String, LiveFolder>();

    public LiveFolder(String name)
    {
        this.name = name;
    }

    public SourceDocument makeDocument(String name)
    {
        SourceDocument sourceDocument = new SourceDocument(name);
        this.documents.put(name, sourceDocument);
        return sourceDocument;
    }

    public LiveFolder makeFolder(String name)
    {
        LiveFolder folder = new LiveFolder(name);
        this.folders.put(name, folder);
        return folder;
    }

    public SourceDocument getDocument(String name)
    {
        return this.documents.get(name);
    }

    public LiveFolder getFolder(String name)
    {
        return this.folders.get(name);
    }

    public String xml(String indent)
    {
        String str = "";
        for (SourceDocument doc : this.documents.values())
            str += indent + "<doc>" + doc.name + "<\\doc>\n";
        for (LiveFolder folder : this.folders.values())
            str += indent + "<sub>\n" + indent + "\t" + "<folder>"
                    + folder.name + "<\\folder>\n" + folder.xml(indent + "\t")
                    + indent + "<\\sub>\n";
        return str;
    }

    public static void main(String[] args)
    {
        LiveFolder folder = new LiveFolder("root");
        folder.makeDocument("t1");
        folder.makeFolder("testFolder").makeDocument("t2");
        System.out.println(folder.xml(""));
    }
}
