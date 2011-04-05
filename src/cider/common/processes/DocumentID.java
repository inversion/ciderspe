package cider.common.processes;

/**
 * To save moving around the name and path of a document everywhere I created
 * this class which contains the name and path of the document. This was while I
 * was writing the time region browser, there may be more places I can use this.
 * 
 * @author Lawrence
 * 
 */
public class DocumentID
{
    public String name;
    public String path;

    /**
     * 
     * @param name
     *            of the SourceDocument
     * @param path
     *            of the SourceDocument
     */
    public DocumentID(String name, String path)
    {
        this.name = name;
        this.path = path;
    }
}