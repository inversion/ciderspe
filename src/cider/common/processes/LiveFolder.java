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
        boolean root = indent.equals("");
        if (root)
        {
            str += "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n";
            str += "<Tree>\n";
            indent += "\t";
        }

        for (SourceDocument doc : this.documents.values())
            str += indent + "<Doc>" + doc.name + "</Doc>\n";
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
        LiveFolder folder = new LiveFolder("root");
        folder.makeDocument("t1");
        folder.makeFolder("testFolder").makeDocument("t2");
        System.out.println(folder.xml(""));
    }

    public SourceDocument path(String dest)
    {
        String[] split = dest.split("\\");
        if (split[0].endsWith(".SourceDocument"))
            return this.getDocument(split[0].split(".")[0]);
        else
            return this.getFolder(split[0]).path(
                    dest.substring(split[0].length()));
    }
}
