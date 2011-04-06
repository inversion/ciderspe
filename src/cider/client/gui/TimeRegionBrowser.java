package cider.client.gui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;

import javax.swing.JPanel;

import cider.common.processes.TimeBorder;
import cider.common.processes.TimeBorderList;
import cider.common.processes.TimeRegion;

public class TimeRegionBrowser extends JPanel implements MouseListener,
        MouseMotionListener
{
    TimeBorderList tbl;
    long[] borderTimes;
    double scale = 0.1;
    long eyePosition = 500;
    boolean movingEye = false;
    long latestTime = 0;
    ArrayList<ActionListener> actionListeners = new ArrayList<ActionListener>();
    ActionEvent eyeMoveAction = new ActionEvent(null, 0, "Eye Moved");

    public TimeRegionBrowser(TimeBorderList tbl)
    {
        this.tbl = tbl;
        this.borderTimes = tbl.borderTimes();
        this.setPreferredSize(new Dimension(128, 128));
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
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
        g.clearRect(0, 0, this.getWidth(), this.getHeight());
        int x = 16;
        int y = 0;
        int prevY = 0;
        int width = this.getWidth() - 32;
        TimeBorder timeBorder;
        g.setColor(Color.BLACK);
        g.drawRect(x - 1, 1, width + 1, this.getHeight() - 1);

        for (long t : this.borderTimes)
        {
            timeBorder = this.tbl.getBorder(t);
            y = (int) (((double) t) * this.scale);
            g.setColor(Color.BLACK);
            g.drawLine(x, y, x + width - 1, y);

            if (timeBorder.fullSet)
                g.setColor(new Color(128, 128, 255));
            else
                g.setColor(Color.LIGHT_GRAY);

            g.fillRect(x, prevY + 1, width, y - prevY - 1);
            prevY = y;
        }

        this.latestTime = this.borderTimes[this.borderTimes.length - 1];
        g.setColor(Color.WHITE);
        g.fillRect(x, y + 1, width, this.getHeight() - y);
        this.paintEye(g, x + width);
    }

    public void paintEye(Graphics g, int x)
    {
        g.setColor(Color.BLACK);
        g.drawOval(x + 1, (int) (this.eyePosition * this.scale) - 4, 14, 8);
        g.drawOval(x + 5, (int) (this.eyePosition * this.scale) - 4, 6, 6);
    }

    @Override
    public void mouseDragged(MouseEvent me)
    {
        if (this.movingEye)
        {
            this.eyePosition = (long) (me.getY() / this.scale);
            if (this.eyePosition < 0)
                this.eyePosition = 0;
            if (this.eyePosition > this.latestTime)
                this.eyePosition = this.latestTime;
            this.repaint();
            this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            for (ActionListener al : this.actionListeners)
                al.actionPerformed(this.eyeMoveAction);
        }
    }

    @Override
    public void mouseMoved(MouseEvent arg0)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseClicked(MouseEvent arg0)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseEntered(MouseEvent arg0)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseExited(MouseEvent arg0)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void mousePressed(MouseEvent me)
    {
        if (me.getX() > this.getWidth() - 16)
        {
            this.movingEye = true;
            this.repaint();
        }
    }

    @Override
    public void mouseReleased(MouseEvent arg0)
    {
        this.movingEye = false;
        this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    public void addActionListener(ActionListener al)
    {
        this.actionListeners.add(al);
    }

    public TimeRegion getCurrentRegion()
    {
        return this.tbl.regionLeadingUpTo(this.eyePosition);
    }
}
