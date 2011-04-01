package cider.client.gui;

import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JPanel;

import cider.common.processes.TimeBorderList;

public class TimeRegionBrowser extends JPanel
{
    TimeBorderList tbl;
    long[] borderTimes;
    double scale = 0.1;

    public TimeRegionBrowser(TimeBorderList tbl)
    {
        this.tbl = tbl;
        this.borderTimes = tbl.borderTimes();
        this.setPreferredSize(new Dimension(128, 128));
    }

    public void updateBorderTimes()
    {
        this.borderTimes = this.tbl.borderTimes();
    }

    public void setScale(double scale)
    {
        this.scale = scale;
    }

    public double getScale()
    {
        return this.scale;
    }

    @Override
    public void paintComponent(Graphics g)
    {
        int y;
        for (long t : this.borderTimes)
        {
            y = (int) (((double) t) * this.scale);
            g.drawLine(8, y, this.getWidth() - 32, y);
        }
    }
}
