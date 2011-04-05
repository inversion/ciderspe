package cider.documentViewerComponents;

import cider.common.processes.SourceDocument;

/**
 * The DocumentHistoryViewer is a type of SourceDocumentViewer that is read only
 * and you cannot see or move any carets.
 * 
 * @author Lawrence
 * 
 */
public class DocumentHistoryViewer extends SourceDocumentViewer
{

    public DocumentHistoryViewer(SourceDocument doc)
    {
        super(doc);
    }

}
