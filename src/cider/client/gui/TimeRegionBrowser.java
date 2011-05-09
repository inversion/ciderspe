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

import javax.swing.JButton;
import javax.swing.JPanel;

import cider.common.network.client.Client;
import cider.common.processes.TimeBorder;
import cider.common.processes.TimeBorderList;
import cider.common.processes.TimeRegion;

/**
 * Class containing the history viewer tool
 */
public class TimeRegionBrowser extends JPanel implements MouseListener,
        MouseMotionListener
{
    /**
     * 
     */
    private static final long serialVersionUID = -5706626095252241675L;
    private TimeBorderList tbl;
    private PriorityQueue<Long> borderTimes;
    private double scale;
    private long eyePosition = 500;
    private long startSelection = 0;
    private long endSelection = 0;
    private boolean movingEye = false;
    private int selecting = 0;
    private long latestTime = 0;
    private ArrayList<ActionListener> actionListeners = new ArrayList<ActionListener>();
    private long lowSelection;
    private long highSelection;
    private int minHeight;
    public static final double defaultScale = 0.001;
    private static final Color darkColor = new Color(48, 48, 48);
    private static final Color highlightColor = new Color(240, 175, 0);// new
                                                                       // Color(128,
                                                                       // 128,
                                                                       // 255);
    private static final Color selectionColor = new Color(
            highlightColor.getRed(), highlightColor.getGreen(),
            highlightColor.getBlue() / 2, highlightColor.getAlpha() / 3);
    public static final int EYE_MOVED = 0;
    public static final int SELECTION = 1;
    protected static final int EYE_RELEASED = 2;

    public TimeRegionBrowser(TimeBorderList tbl, int width, int height)
    {
        eyePosition = System.currentTimeMillis();
        this.tbl = tbl;
        borderTimes = tbl.borderTimes();
        this.setPreferredSize(new Dimension(width, height));
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        minHeight = height;
    }

    public void addActionListener(ActionListener al)
    {
        actionListeners.add(al);
    }

    public void downloadSelectedRegion(Client client,
            final JButton downloadButton) throws Exception
    {
        TimeBorder selectedBorder1 = new TimeBorder(
                tbl.getDocumentProperties(), this.getSelectionUpperLowerBound());
        TimeBorder selectedBorder2 = new TimeBorder(
                tbl.getDocumentProperties(), this.getSelectionLowerUpperBound());
        TimeRegion outerRegion = this.getRegionContainingSelectedArea();

        // regions during and after selection
        TimeRegion selectedRegion = new TimeRegion(selectedBorder1,
                selectedBorder2);
        TimeRegion afterSelection = new TimeRegion(selectedBorder2,
                outerRegion.end);

        // adjust borders
        tbl.replaceEndBorder(outerRegion, selectedBorder1);
        tbl.addTimeBorder(selectedBorder1);
        tbl.addTimeBorder(selectedBorder2);

        // add new regions
        tbl.addRegion(selectedRegion);
        tbl.addRegion(afterSelection);

        client.setDiversion(selectedRegion);
        client.pullEventsFromBot(tbl.getDocumentProperties().path,
                selectedRegion.getStartTime(), selectedRegion.getEndTime(),
                true);
        System.out.println("Starting histroy download");
        downloadButton.setEnabled(false);

        selectedRegion.addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent ae)
            {
                switch (ae.getID())
                {
                case TimeRegion.FINISHED_UPDATE:
                {
                    System.out.println("Done history download");
                    downloadButton.setEnabled(true);
                    updateBorderTimes();
                    updateUI();
                }
                    break;
                }
            }
        });
    }

    public TimeRegion getCurrentRegion()
    {
        return tbl.regionThatCovers(eyePosition);
    }

    private TimeRegion getRegionContainingSelectedArea()
    {
        return tbl.regionThatCovers(startSelection);
    }

    public double getScale()
    {
        return scale;
    }

    public long getSelectionLength()
    {
        return highSelection - lowSelection;
    }

    public long getSelectionLowerUpperBound()
    {
        return highSelection;
    }

    public long getSelectionUpperLowerBound()
    {
        return lowSelection;
    }

    @Override
    public void mouseClicked(MouseEvent arg0)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseDragged(MouseEvent me)
    {
        if (movingEye)
            this.updateEyePosition(me);
        else if (selecting != 0)
        {
            long position = this.yPixelToTime(me.getY());

            endSelection = position;

            TimeRegion currentRegion = this.getRegionContainingSelectedArea();
            long earliest = currentRegion.getStartTime();
            long latest = currentRegion.getEndTime();

            if (endSelection <= earliest)
                endSelection = earliest + 1;
            if (endSelection > latest)
                endSelection = latest;

            lowSelection = startSelection;
            highSelection = endSelection;

            if (lowSelection > highSelection)
            {
                highSelection = lowSelection;
                lowSelection = endSelection;
            }

            this.repaint();

            for (ActionListener al : actionListeners)
                al.actionPerformed(new ActionEvent(eyePosition, SELECTION,
                        "Selection Changed"));
        }

        this.repaint();
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
    public void mouseMoved(MouseEvent arg0)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void mousePressed(MouseEvent me)
    {
        if (me.getX() > this.getWidth() - 16)
        {
            movingEye = true;
            this.updateEyePosition(me);
            this.repaint();
        }
        else
        {
            if (selecting == 0)
            {
                selecting = -1;
                startSelection = this.yPixelToTime(me.getY());
                endSelection = startSelection;
                this.repaint();
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent arg0)
    {
        if (movingEye)
        {
            for (ActionListener al : actionListeners)
                al.actionPerformed(new ActionEvent(eyePosition, EYE_RELEASED,
                        "Eye Eeleased"));
            movingEye = false;
        }
        selecting = 0;
        this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    @Override
    public void paintComponent(Graphics g)
    {
        int x = 16;
        int y = 0;
        int prevY = 0;
        int width = this.getWidth() - 32;
        TimeBorder timeBorder;
        int endY = this.timeToYPixel(latestTime);
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, this.getWidth(), Math.max(endY, minHeight));
        g.setColor(Color.WHITE);
        g.drawRect(x - 1, 1, width + 1, endY - 1);

        for (long t : borderTimes)
        {
            timeBorder = tbl.getBorder(t);
            y = this.timeToYPixel(t);
            g.setColor(Color.WHITE);
            g.drawLine(x, y, x + width - 1, y);

            if (timeBorder.fullSet)
                g.setColor(highlightColor);
            else
                g.setColor(darkColor);

            g.fillRect(x, prevY + 1, width, y - prevY - 1);
            prevY = y;
        }

        g.setColor(Color.WHITE);
        g.fillRect(x, y + 1, width, endY - y);
        this.paintEye(g, x + width);

        int start = this.timeToYPixel(startSelection);
        int end = this.timeToYPixel(endSelection);
        g.setColor(selectionColor);
        g.fillRect(x, start, width, end - start);
    }

    public void paintEye(Graphics g, int x)
    {
        g.setColor(Color.WHITE);
        g.drawOval(x + 1, this.timeToYPixel(eyePosition) - 4, 14, 8);
        g.fillOval(x + 5, this.timeToYPixel(eyePosition) - 3, 6, 6);
        g.drawOval(x + 5, this.timeToYPixel(eyePosition) - 3, 6, 6);
    }

    public boolean selectionLiesWithinFullRegion()
    {
        return this.getRegionContainingSelectedArea().end.fullSet;
    }

    public void setScale(double scale)
    {
        this.scale = scale;
        latestTime = tbl.getEndTime();
        Dimension size = new Dimension(this.getWidth(),
                this.timeToYPixel(latestTime));
        this.setMinimumSize(size);
        this.setMaximumSize(size);
        this.setSize(size);
        this.setPreferredSize(size);
    }

    public int timeToYPixel(long t)
    {
        if (tbl.hasNoBorders())
            return 0;
        else
            return (int) ((t - tbl.getFirstTime()) * scale);
    }

    public void updateBorderTimes()
    {
        borderTimes = tbl.borderTimes();
    }

    public void updateEyePosition(MouseEvent me)
    {
        eyePosition = this.yPixelToTime(me.getY());
        if (eyePosition < 0)
            eyePosition = 0;
        if (eyePosition > latestTime)
            eyePosition = latestTime;
        this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        for (ActionListener al : actionListeners)
            al.actionPerformed(new ActionEvent(eyePosition, EYE_MOVED,
                    "Eye Moved"));
    }

    public long yPixelToTime(int pix)
    {
        if (tbl.hasNoBorders())
            return 0;
        else
            return (long) ((pix / scale) + tbl.getFirstTime());
    }

    public void setEyePosition(long t)
    {
        this.eyePosition = t;
        for (ActionListener al : actionListeners)
        {
            al.actionPerformed(new ActionEvent(eyePosition, EYE_MOVED,
                    "Eye Moved"));
            al.actionPerformed(new ActionEvent(eyePosition, EYE_RELEASED,
                    "Eye Eeleased"));
        }
    }
}
