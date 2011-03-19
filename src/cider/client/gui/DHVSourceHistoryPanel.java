package cider.client.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;

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
    }

    public static void main(String[] args)
    {
        SourceDocument doc = new SourceDocument("Test Bot", "Test Document");
        DocumentHistoryViewer dhv = new DocumentHistoryViewer("Test User", doc);
        DHVSourceHistoryPanel panel = new DHVSourceHistoryPanel(dhv, null, 0,
                Long.MAX_VALUE);
        JFrame w = new JFrame();
        w.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        w.setPreferredSize(new Dimension(400, 400));
        w.setLayout(new BorderLayout());
        w.add(panel);
        w.pack();
        w.setVisible(true);
    }
}
