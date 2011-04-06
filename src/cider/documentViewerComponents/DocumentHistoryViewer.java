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

package cider.documentViewerComponents;

import cider.common.processes.SourceDocument;
import cider.common.processes.TimeRegion;

/**
 * The DocumentHistoryViewer is a type of SourceDocumentViewer that is read only
 * and you cannot see or move any carets.
 * 
 * @author Lawrence
 * 
 */
public class DocumentHistoryViewer extends SourceDocumentViewer
{
    TimeRegion timeRegion;

    public DocumentHistoryViewer(SourceDocument sourceDocument)
    {
        super(sourceDocument);
    }

    public DocumentHistoryViewer(TimeRegion timeRegion)
    {
        super(new SourceDocument(timeRegion.documentID.name));
        this.doc.addEvents(timeRegion.end.typingEvents);

    }

    public void setRegion(TimeRegion currentRegion)
    {
        this.doc.clearAll();
        this.doc.addEvents(currentRegion.end.typingEvents);
    }
}
