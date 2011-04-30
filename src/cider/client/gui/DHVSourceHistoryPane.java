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
import java.util.PriorityQueue;

import javax.swing.JButton;
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
import cider.common.processes.TimeRegion;
import cider.common.processes.TypingEvent;
import cider.documentViewerComponents.DocumentHistoryViewer;

@SuppressWarnings("serial")
public class DHVSourceHistoryPane extends JPanel
{
    private DocumentHistoryViewer dhv;
    private JScrollPane documentScrollPane;
    private TimeRegionBrowser trb;
    private JScrollPane regionBrowserScrollPane;
    private JPanel westPanel;
    private JButton downloadRegion;
    private JSlider scaleSlider;
    private static final double slideScale = 100000.0;

    public DHVSourceHistoryPane()
    {
        super(new BorderLayout());
        this.westPanel = new JPanel(new BorderLayout());
        this.add(this.westPanel, BorderLayout.WEST);
        this.downloadRegion = new JButton("Download Region");
        this.downloadRegion.setEnabled(false);
        this.westPanel.add(this.downloadRegion, BorderLayout.SOUTH);
        this.scaleSlider = new JSlider(0, 200, (int) (TimeRegionBrowser.defaultScale * slideScale));
        this.scaleSlider.setPreferredSize(new Dimension(128, 16));
        this.westPanel.add(this.scaleSlider, BorderLayout.NORTH);
        
        this.scaleSlider.addChangeListener(new ChangeListener()
        {
            @Override
            public void stateChanged(ChangeEvent ce)
            {
                trb.setScale(scaleSlider.getValue() / slideScale);
                westPanel.updateUI();
            }
        });
        
        this.downloadRegion.addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent ae)
            {
                try
                {
                    trb.downloadSelectedRegion();
                }
                catch (Exception e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
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
            this.westPanel.remove(this.regionBrowserScrollPane);

        this.regionBrowserScrollPane = new JScrollPane(trb);
        this.regionBrowserScrollPane
                .setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        this.westPanel.add(this.regionBrowserScrollPane, BorderLayout.WEST);

        trb.addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                switch (e.getID())
                {
                case TimeRegionBrowser.EYE_MOVED:
                {
                    TimeRegion currentRegion = trb.getCurrentRegion();
                    dhv.useEventsFrom(currentRegion);
                    long t = (Long) e.getSource();
                    dhv.updateText(t);
                    break;
                }
                case TimeRegionBrowser.SELECTION:
                {
                    downloadRegion.setEnabled(trb.getSelectionLength() > 0
                            && !trb.selectionLiesWithinFullRegion());
                    break;
                }
                }
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

        TimeBorderList tbl = new TimeBorderList(documentID);
        SourceDocument doc = new SourceDocument(documentID.name);
        TimeBorder border = new TimeBorder(documentID, 1000,
                new PriorityQueue<TypingEvent>());
        tbl.addTimeBorder(border);
        doc.addEvents(SourceDocument.sampleEvents(1000));
        border = new TimeBorder(documentID, 4000, doc.orderedEvents());
        border.fullSet = true;
        tbl.addTimeBorder(border);
        border = new TimeBorder(documentID, 5000,
                new PriorityQueue<TypingEvent>());
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
