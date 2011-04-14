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
import java.util.PriorityQueue;

import javax.swing.JPanel;

import cider.common.processes.TimeBorder;
import cider.common.processes.TimeBorderList;
import cider.common.processes.TimeRegion;

public class TimeRegionBrowser extends JPanel implements MouseListener,
        MouseMotionListener
{
    TimeBorderList tbl;
    PriorityQueue<Long> borderTimes;
    double scale = 0.1;
    long eyePosition = 500;
    long startSelection = 0;
    long endSelection = 0;
    boolean movingEye = false;
    int selecting = 0;
    long latestTime = 0;
    ArrayList<ActionListener> actionListeners = new ArrayList<ActionListener>();
    private long lowSelection;
    private long highSelection;
    private static final Color highlightColor = new Color(128, 128, 255);
    private static final Color selectionColor = new Color(highlightColor.getRed(),
            highlightColor.getGreen(), highlightColor.getBlue(), highlightColor.getAlpha() / 3);

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
            this.latestTime = t;
            timeBorder = this.tbl.getBorder(t);
            y = (int) (((double) t) * this.scale);
            g.setColor(Color.BLACK);
            g.drawLine(x, y, x + width - 1, y);

            if (timeBorder.fullSet)
                g.setColor(highlightColor);
            else
                g.setColor(Color.LIGHT_GRAY);

            g.fillRect(x, prevY + 1, width, y - prevY - 1);
            prevY = y;
        }

        g.setColor(Color.WHITE);
        g.fillRect(x, y + 1, width, this.getHeight() - y);
        this.paintEye(g, x + width);
        
        int start = (int) (this.startSelection * this.scale);
        int end = (int) (this.endSelection * this.scale);
        g.setColor(selectionColor);
        g.fillRect(x, start, width, end - start);
    }
    
    public void paintEye(Graphics g, int x)
    {
        g.setColor(Color.BLACK);
        g.drawOval(x + 1, (int) (this.eyePosition * this.scale) - 4, 14, 8);
        g.setColor(Color.LIGHT_GRAY);
        g.fillOval(x + 5, (int) (this.eyePosition * this.scale) - 3, 6, 6);
        g.setColor(Color.BLACK);
        g.drawOval(x + 5, (int) (this.eyePosition * this.scale) - 3, 6, 6);
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
            this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            for (ActionListener al : this.actionListeners)
                al.actionPerformed(new ActionEvent(this.eyePosition, 0,
                        "Eye Moved"));
        }
        
        if(this.selecting != 0)
        {
            long position = (long) (me.getY() / this.scale);
            
            this.endSelection = position;
           
            TimeRegion currentRegion = this.tbl.regionThatCovers(this.startSelection);
            long earliest = currentRegion.getStartTime();
            long latest = currentRegion.getEndTime();
            
            this.lowSelection = this.startSelection;
            this.highSelection = this.endSelection;
            
            if(this.lowSelection > this.highSelection)
            {
                this.highSelection = this.lowSelection;
                this.lowSelection = this.endSelection;
            }
            
            if(this.endSelection < earliest)
                this.endSelection = earliest;
            if(this.endSelection > latest)
                this.endSelection = latest;
            
            this.repaint();
        }
        
        this.repaint();
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
        else
        {
            if(this.selecting == 0)
            {
                this.selecting = -1;
                this.startSelection = (long) (me.getY() / this.scale);
                this.endSelection = this.startSelection;
                this.repaint();
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent arg0)
    {
        this.movingEye = false;
        this.selecting = 0;
        this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    public void addActionListener(ActionListener al)
    {
        this.actionListeners.add(al);
    }

    public TimeRegion getCurrentRegion()
    {
        return this.tbl.regionThatCovers(this.eyePosition);
    }
}
