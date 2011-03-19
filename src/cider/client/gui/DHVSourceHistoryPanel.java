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

import cider.common.network.client.Client;
import cider.common.processes.SourceDocument;
import cider.specialcomponents.editorTypingArea.DocumentHistoryViewer;

public class DHVSourceHistoryPanel extends JPanel
{
    DocumentHistoryViewer dhv;
    Client client;
    JSlider timeSlider;
    DefaultBoundedRangeModel rangeModel;
    JScrollPane scrollPane;

    public DHVSourceHistoryPanel(final DocumentHistoryViewer dhv,
            Client client, long start, long end)
    {
        super(new BorderLayout());
        this.scrollPane = new JScrollPane(dhv);
        this.dhv = dhv;
        this.client = client;

        // Squash long numbers into an integer
        // TODO: Check this...
        int iStart = (int) Math.sqrt(start);
        int iEnd = (int) Math.sqrt(end);
        int extent = (iEnd - iStart) / 50;

        this.rangeModel = new DefaultBoundedRangeModel(iEnd - extent, extent,
                iStart, iEnd);
        this.timeSlider = new JSlider(this.rangeModel);
        this.add(this.timeSlider, BorderLayout.NORTH);
        this.add(this.scrollPane, BorderLayout.CENTER);

        this.timeSlider.addChangeListener(new ChangeListener()
        {

            @Override
            public void stateChanged(ChangeEvent ce)
            {
                long time = rangeModel.getExtent() + rangeModel.getValue();
                System.out.println(time);
                dhv.updateText(time * time);
            }

        });
    }

    public static void main(String[] args)
    {
        SourceDocument doc = new SourceDocument("Test Bot", "Test Document");
        doc.addEvents(SourceDocument.sampleEvents());
        DocumentHistoryViewer dhv = new DocumentHistoryViewer("Test User", doc);
        dhv.setDefaultColor(Color.BLACK);
        DHVSourceHistoryPanel panel = new DHVSourceHistoryPanel(dhv, null, 0,
                3000);
        dhv.updateText();
        dhv.setWaiting(false);
        JFrame w = new JFrame();
        w.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        w.setPreferredSize(new Dimension(400, 400));
        w.setLayout(new BorderLayout());
        w.add(panel);
        w.pack();
        w.setVisible(true);
    }
}
