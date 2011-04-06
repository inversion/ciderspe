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

import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import cider.common.processes.DocumentID;
import cider.common.processes.SourceDocument;
import cider.common.processes.TimeBorder;
import cider.common.processes.TimeBorderList;
import cider.documentViewerComponents.DocumentHistoryViewer;

@SuppressWarnings("serial")
public class DHVSourceHistoryPane extends JPanel
{
    private DocumentHistoryViewer dhv;
    private JSlider timeSlider;
    private DefaultBoundedRangeModel rangeModel = new DefaultBoundedRangeModel();
    private JScrollPane documentScrollPane;
    private TimeRegionBrowser trb;
    private JScrollPane regionBrowserScrollPane;

    public DHVSourceHistoryPane()
    {
        super(new BorderLayout());
        this.timeSlider = new JSlider(this.rangeModel);
        this.add(this.timeSlider, BorderLayout.NORTH);
        this.timeSlider.addChangeListener(new ChangeListener()
        {
            @Override
            public void stateChanged(ChangeEvent arg0)
            {
                long time = rangeModel.getExtent() + rangeModel.getValue();
                System.out.println(time);
                dhv.updateText(time * time);
            }
        });
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
        this.add(this.regionBrowserScrollPane, BorderLayout.WEST);
    }

    public void setTimeRange(long start, long end)
    {
        // Squash long numbers into an integer
        // TODO: Check this...
        int iStart = (int) Math.sqrt(start);
        int iEnd = (int) Math.sqrt(end);
        int extent = (iEnd - iStart) / 50;
        this.rangeModel.setValue(iEnd - extent);
        this.rangeModel.setExtent(extent);
        this.rangeModel.setMinimum(iStart);
        this.rangeModel.setMaximum(iEnd);
    }

    public static void main(String[] args)
    {
        DocumentID documentID = new DocumentID("Test Document", "testpath");

        SourceDocument doc = new SourceDocument(documentID.name);
        doc.addEvents(SourceDocument.sampleEvents());

        DocumentHistoryViewer dhv = new DocumentHistoryViewer(doc);
        dhv.setDefaultColor(Color.BLACK);
        dhv.updateText();
        dhv.setWaiting(false);

        TimeBorderList tbl = new TimeBorderList();
        tbl.addTimeBorder(new TimeBorder(documentID, 1000));
        tbl.addTimeBorder(new TimeBorder(documentID, 1500));

        TimeRegionBrowser trb = new TimeRegionBrowser(tbl);

        DHVSourceHistoryPane app = new DHVSourceHistoryPane();
        app.setDocumentHistoryViewer(dhv);
        app.setTimeRegionBrowser(trb);
        app.setTimeRange(0, 3000);

        JFrame w = new JFrame();
        w.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        w.setPreferredSize(new Dimension(600, 600));
        w.setLayout(new BorderLayout());
        w.add(app);
        w.pack();
        w.setVisible(true);
    }
}
