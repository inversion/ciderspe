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
    private TimeBorderList tbl;
    private PriorityQueue<Long> borderTimes;
    private double scale = 0.001;
    private long eyePosition = 500;
    private long startSelection = 0;
    private long endSelection = 0;
    private boolean movingEye = false;
    private int selecting = 0;
    private long latestTime = 0;
    private ArrayList<ActionListener> actionListeners = new ArrayList<ActionListener>();
    private long lowSelection;
    private long highSelection;
    private static final Color highlightColor = new Color(128, 128, 255);
    private static final Color selectionColor = new Color(highlightColor.getRed(),
            highlightColor.getGreen(), highlightColor.getBlue(
                    ) / 2, highlightColor.getAlpha() / 3);
    public static final int EYE_MOVED = 0;
    public static final int SELECTION = 1;

    public TimeRegionBrowser(TimeBorderList tbl)
    {
        this.eyePosition = System.currentTimeMillis();
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
        int x = 16;
        int y = 0;
        int prevY = 0;
        int width = this.getWidth() - 32;
        TimeBorder timeBorder;
        g.setColor(Color.BLACK);
        this.latestTime = this.tbl.getEndTime();
        int endY = this.timeToYPixel(this.latestTime);
        this.setPreferredSize(new Dimension(this.getWidth(), endY));
        g.clearRect(0, 0, this.getWidth(), endY);
        
        g.drawRect(x - 1, 1, width + 1, endY - 1);

        for (long t : this.borderTimes)
        {
            timeBorder = this.tbl.getBorder(t);
            y = this.timeToYPixel(t);
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
        g.fillRect(x, y + 1, width, endY - y);
        this.paintEye(g, x + width);
        
        int start = this.timeToYPixel(this.startSelection);
        int end = this.timeToYPixel(this.endSelection);
        g.setColor(selectionColor);
        g.fillRect(x, start, width, end - start);
    }
    
    public int timeToYPixel(long t)
    {
        return (int) ((t - this.tbl.getFirstTime()) * this.scale);
    }
    
    public long yPixelToTime(int pix)
    {
        return (long) ((pix / this.scale) + this.tbl.getFirstTime());
    }
    
    public void paintEye(Graphics g, int x)
    {
        g.setColor(Color.BLACK);
        g.drawOval(x + 1, this.timeToYPixel(this.eyePosition) - 4, 14, 8);
        g.setColor(Color.LIGHT_GRAY);
        g.fillOval(x + 5, this.timeToYPixel(this.eyePosition) - 3, 6, 6);
        g.setColor(Color.BLACK);
        g.drawOval(x + 5, this.timeToYPixel(this.eyePosition) - 3, 6, 6);
    }

    @Override
    public void mouseDragged(MouseEvent me)
    {
        if (this.movingEye)
        {
            this.eyePosition = this.yPixelToTime(me.getY());
            if (this.eyePosition < 0)
                this.eyePosition = 0;
            if (this.eyePosition > this.latestTime)
                this.eyePosition = this.latestTime;
            this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            for (ActionListener al : this.actionListeners)
                al.actionPerformed(new ActionEvent(this.eyePosition, EYE_MOVED,
                        "Eye Moved"));
        }
        
        if(this.selecting != 0)
        {
            long position = this.yPixelToTime(me.getY());
            
            this.endSelection = position;
           
            TimeRegion currentRegion = this.getRegionContainingSelectedArea();
            long earliest = currentRegion.getStartTime();
            long latest = currentRegion.getEndTime();
            
            if(this.endSelection <= earliest)
                this.endSelection = earliest + 1;
            if(this.endSelection > latest)
                this.endSelection = latest;
            
            this.lowSelection = this.startSelection;
            this.highSelection = this.endSelection;
            
            if(this.lowSelection > this.highSelection)
            {
                this.highSelection = this.lowSelection;
                this.lowSelection = this.endSelection;
            }
            
            this.repaint();
            
            for (ActionListener al : this.actionListeners)
                al.actionPerformed(new ActionEvent(this.eyePosition, SELECTION,
                        "Selection Changed"));
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
                this.startSelection = this.yPixelToTime(me.getY());
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
    
    public long getSelectionLength()
    {
        return this.highSelection - this.lowSelection;
    }
    
    public long getSelectionUpperLowerBound()
    {
        return this.lowSelection;
    }
    
    public long getSelectionLowerUpperBound()
    {
        return this.highSelection;
    }

    public void downloadSelectedRegion() throws Exception
    {
        TimeBorder selectedBorder1 = new TimeBorder(this.tbl.getDocumentID(), this.getSelectionUpperLowerBound());
        TimeBorder selectedBorder2 = new TimeBorder(this.tbl.getDocumentID(), this.getSelectionLowerUpperBound());
        TimeRegion outerRegion = this.getRegionContainingSelectedArea();
        
        //regions during and after selection
        TimeRegion selectedRegion = new TimeRegion(selectedBorder1, selectedBorder2);
        TimeRegion afterSelection = new TimeRegion(selectedBorder2, outerRegion.end);
        
        //adjust borders
        this.tbl.replaceEndBorder(outerRegion, selectedBorder1);
        this.tbl.addTimeBorder(selectedBorder1);
        this.tbl.addTimeBorder(selectedBorder2);
        
        //add new regions
        this.tbl.addRegion(selectedRegion);
        this.tbl.addRegion(afterSelection);
        
        this.updateBorderTimes();
        this.updateUI();
    }

    private TimeRegion getRegionContainingSelectedArea()
    {
        return this.tbl.regionThatCovers(this.startSelection);
    }

    public boolean selectionLiesWithinFullRegion()
    {
        return this.getRegionContainingSelectedArea().end.fullSet;
    }
}
