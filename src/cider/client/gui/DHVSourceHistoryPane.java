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

package cider.client.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import cider.common.processes.DocumentID;
import cider.common.processes.SourceDocument;
import cider.common.processes.TimeBorder;
import cider.common.processes.TimeBorderList;
import cider.common.processes.TimeRegion;
import cider.documentViewerComponents.DocumentHistoryViewer;

@SuppressWarnings("serial")
public class DHVSourceHistoryPane extends JPanel
{
    private DocumentHistoryViewer dhv;
    private JScrollPane documentScrollPane;
    private TimeRegionBrowser trb;
    private JScrollPane regionBrowserScrollPane;

    public DHVSourceHistoryPane()
    {
        super(new BorderLayout());
    }

    public void setDocumentHistoryViewer(final DocumentHistoryViewer dhv)
    {
        this.dhv = dhv;

        if (this.documentScrollPane != null)
            this.remove(this.documentScrollPane);

        this.documentScrollPane = new JScrollPane(dhv);
        this.add(this.documentScrollPane, BorderLayout.CENTER);
    }

    public void setTimeRegionBrowser(final TimeRegionBrowser trb)
    {
        this.trb = trb;

        if (this.regionBrowserScrollPane != null)
            this.remove(this.regionBrowserScrollPane);

        this.regionBrowserScrollPane = new JScrollPane(trb);
        this.regionBrowserScrollPane
                .setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        this.add(this.regionBrowserScrollPane, BorderLayout.WEST);

        trb.addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                TimeRegion currentRegion = trb.getCurrentRegion();
                dhv.useEventsFrom(currentRegion);
                long t = (Long) e.getSource();
                dhv.updateText(t);
            }

        });
    }

    public static void main(String[] args)
    {
        DocumentID documentID = new DocumentID("Test Document", "testpath");

        DocumentHistoryViewer dhv = new DocumentHistoryViewer(
                new SourceDocument(documentID.name));
        dhv.setDefaultColor(Color.BLACK);
        dhv.updateText();
        dhv.setWaiting(false);

        TimeBorderList tbl = new TimeBorderList();
        SourceDocument doc = new SourceDocument(documentID.name);
        TimeBorder border = new TimeBorder(documentID, 1000,
                doc.orderedEvents());
        tbl.addTimeBorder(border);
        doc.addEvents(SourceDocument.sampleEvents(1000));
        border = new TimeBorder(documentID, 4000, doc.orderedEvents());
        border.fullSet = true;
        tbl.addTimeBorder(border);
        tbl.createRegions();
        TimeRegionBrowser trb = new TimeRegionBrowser(tbl);

        DHVSourceHistoryPane app = new DHVSourceHistoryPane();
        app.setDocumentHistoryViewer(dhv);
        app.setTimeRegionBrowser(trb);

        JFrame w = new JFrame();
        w.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        w.setPreferredSize(new Dimension(600, 600));
        w.setLayout(new BorderLayout());
        w.add(app);
        w.pack();
        w.setVisible(true);
    }
}
