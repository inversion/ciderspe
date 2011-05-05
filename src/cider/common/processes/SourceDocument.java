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

package cider.common.processes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;

/**
 * 
 * SourceDocument is a live document which is a collection of typing events.
 * 
 * Between executions they can be serialized by the Bot and written to disk to
 * make files persistent. This also allows typing events to be kept.
 * 
 * @author Lawrence, Andrew
 */
public class SourceDocument implements ICodeLocation, Serializable
{

    /**
     * 
     */
    private static final long serialVersionUID = -6700242168976852201L;
    private PriorityQueue<TypingEvent> typingEvents;
    public String name;
    private long latestTime = 0;
    private long creationTime = Long.MAX_VALUE;

    public SourceDocument(String name)
    {
        this.name = name;
        this.typingEvents = new PriorityQueue<TypingEvent>(1000,
                new EventComparer());
    }

    public SourceDocument(String name, String owner,
            PriorityQueue<TypingEvent> typingEvents)
    {
        this.name = name;
        this.typingEvents = new PriorityQueue<TypingEvent>(typingEvents);
    }

    public static void main(String[] args)
    {
        System.out.println(lawrencesTests());
        andrewsTests();
    }

    /**
     * @author Lawrence
     */
    public static String lawrencesTests()
    {
        String testLog = shuffleAndSimplificationTest() + "\n";
        testLog += lengthTest();
        return testLog;
    }

    /**
     * This does some randomised testing of document editing and document
     * simplification
     * 
     * @author Lawrence
     * @return test results
     */
    protected static String shuffleAndSimplificationTest()
    {
        String expected = "the quick 123123123123123123123123123 muddled fox bounced over the lazy dog";

        ArrayList<TypingEvent> tes = sampleEvents(0);

        SourceDocument testDoc = new SourceDocument("testdoc.SourceDocument");
        for (TypingEvent event : tes)
            testDoc.addEvent(event);

        String result = testDoc.toString();
        String testResult = expected.equals(result) ? "pass shuffle test\n"
                : "fail: did not pass shuffled events test since toString returned '"
                        + result
                        + "', where as it should of been '"
                        + expected
                        + "'.\n";

        expected = "the quick 123123123123123123123123123 muddled fox bounced over [this text was inserted after the simplification] the lazy dog";

        testDoc.simplify(3000);
        tes.clear();
        tes.addAll(generateEvents(3000, 3100, 62,
                "[this text was inserted after the simplification] ",
                TypingEventMode.insert, "na"));

        for (TypingEvent event : tes)
            testDoc.addEvent(event);

        result = testDoc.toString();
        testResult += expected.equals(result) ? "pass simplification test"
                : "fail: simplification followed by new events produced '"
                        + result + "', where as it should of been '" + expected
                        + "'.";

        return testResult;
    }

    /**
     * Generates a set of typing events in random order including backspaces,
     * overwrites and inserts. These typing events can be used for automated
     * testing or place-holder text.
     * 
     * @param timeShift
     * 
     * @return TypingEvents which should produce the string the quick
     *         123123123123123123123123123 muddled fox bounced over the lazy dog
     */
    public static ArrayList<TypingEvent> sampleEvents(long offset)
    {
        ArrayList<TypingEvent> tes = new ArrayList<TypingEvent>();
        tes.addAll(generateEvents(offset, offset + 100, 0,
                "the quick brown fox jumped over the lazy dog",
                TypingEventMode.insert, "na"));
        tes.addAll(generateEvents(offset + 200, offset + 500, 10, "muddled",
                TypingEventMode.overwrite, "na"));
        tes.addAll(generateEvents(offset + 600, offset + 700, 16, " f",
                TypingEventMode.insert, "na"));
        tes.addAll(generateEvents(offset + 800, offset + 1000, 27, "jumped",
                TypingEventMode.backspace, "na"));
        tes.addAll(generateEvents(offset + 2000, offset + 2500, 21, "bounced",
                TypingEventMode.insert, "na"));
        tes.addAll(generateEvents(offset + 2600, offset + 3000, 9,
                "123123123123123123123123123 ", TypingEventMode.insert, "na"));

        tes = shuffledEvents(tes, new Date().getTime());
        return tes;
    }

    /**
     * Test for any problems with very long documents
     * 
     * @author Lawrence
     * @return test results
     */
    protected static String lengthTest()
    {
        ArrayList<TypingEvent> tes = new ArrayList<TypingEvent>();
        tes.add(new TypingEvent(0, TypingEventMode.insert, 0, 1, "<", "na",
                null));
        tes.add(new TypingEvent(1, TypingEventMode.insert, 1, 1, ">", "na",
                null));

        String bigString = "";
        final String alphabet = "10";
        final char[] alphaChars = alphabet.toCharArray();
        final int l = alphabet.length();
        for (int i = 0; i < 10000; i++)
            bigString += alphaChars[i % l];

        tes.addAll(generateEvents(2, 10000, 0, bigString,
                TypingEventMode.insert, "na"));

        SourceDocument testDoc = new SourceDocument("testDoc.SourceDocument");
        for (TypingEvent event : tes)
            testDoc.addEvent(event);
        String result = testDoc.toString();
        return (result.startsWith("<") && result.endsWith(">")) ? "pass length test"
                : "fail: did not pass the length test.";
    }

    /**
     * Generates shuffled events for test routines
     * 
     * @author Lawrence
     * @param typingEvents
     * @param seed
     * @return the shuffled typingEvents
     */
    protected static ArrayList<TypingEvent> shuffledEvents(
            ArrayList<TypingEvent> typingEvents, long seed)
    {
        ArrayList<TypingEvent> tes = new ArrayList<TypingEvent>();
        ArrayList<TypingEvent> source = new ArrayList<TypingEvent>();
        source.addAll(typingEvents);
        Random generator = new Random(seed);
        TypingEvent event;

        while (source.size() > 0)
        {
            event = source.get(generator.nextInt(source.size()));
            tes.add(event);
            source.remove(event);
        }

        return tes;
    }
    
    /**
     * @author Andrew
     */
    private static void andrewsTests()
    {
        final String originalMessage = "The quick brown fox jumped over the lazy dog";
        String msg;
        
        System.out.print( "Simple insert into nothing: ");
        SourceDocument doc = new SourceDocument("test");
        TypingEvent te = new TypingEvent(0, TypingEventMode.insert, 0,
                originalMessage.length(), originalMessage, "owner", null);
        doc.addEvents(te.explode());
        String resultingMessage = doc.toString();
        if (resultingMessage.equals(originalMessage))
            System.out.println("pass");
        else
            System.out.println("fail, should of been " + originalMessage
                    + " but got " + resultingMessage);
        
        System.out.print( "Insert into existing string at beginning: ");
        doc = new SourceDocument("test");
        te = new TypingEvent(1, TypingEventMode.insert, 0,
                originalMessage.length(), originalMessage, "owner", null);
        doc.addEvents(te.explode());
        msg = "Thus, ";
        te = new TypingEvent(doc.lastUpdateTime() + 1, TypingEventMode.insert, 0,
                msg.length(), msg, "owner", null);
        doc.addEvents(te.explode());
        resultingMessage = doc.toString();
        if (resultingMessage.equals( "Thus, The quick brown fox jumped over the lazy dog"))
            System.out.println("pass");
        else
            System.out.println("fail, should of been " + "Thus, The quick brown fox jumped over the lazy dog"
                    + " but got " + resultingMessage);

        // Testing for overwrites
        System.out.print( "Overwriting nothing: " );
        doc = new SourceDocument("test");
        te = new TypingEvent(0, TypingEventMode.overwrite, 0,
                originalMessage.length(), originalMessage, "owner", null);
        doc.addEvents(te.explode());
        resultingMessage = doc.toString();
        if (resultingMessage.equals(originalMessage))
            System.out.println("pass");
        else
            System.out.println("fail, should of been " + originalMessage
                    + " but got " + resultingMessage);
        
        System.out.print( "Full overwrite (same length): " );
        doc = new SourceDocument("test");
        te = new TypingEvent(1, TypingEventMode.insert, 0,
                originalMessage.length(), originalMessage, "owner", null);
        doc.addEvents(te.explode());
        msg = "The furry brown fox jumped over the blue dog";
        te = new TypingEvent(doc.lastUpdateTime()+1, TypingEventMode.overwrite, 0, msg.length(), msg, "owner", null);
        doc.addEvents(te.explode());    
        resultingMessage = doc.toString();
        if (resultingMessage.equals( "The furry brown fox jumped over the blue dog"))
            System.out.println("pass");
        else
            System.out.println("fail, should of been " + msg
                    + " but got " + resultingMessage);
        
        System.out.print( "Partial overwrite (same length): " );
        doc = new SourceDocument("test");
        te = new TypingEvent(1, TypingEventMode.insert, 0,
                originalMessage.length(), originalMessage, "owner", null);
        doc.addEvents(te.explode());
        msg = "vaults";
        te = new TypingEvent(doc.lastUpdateTime()+1, TypingEventMode.overwrite, 20, msg.length(), msg, "owner", null);
        doc.addEvents(te.explode());    
        resultingMessage = doc.toString();
        if (resultingMessage.equals( "The quick brown fox vaults over the lazy dog"))
            System.out.println("pass");
        else
            System.out.println("fail, should of been " + "The quick brown fox vaults over the lazy dog"
                    + " but got " + resultingMessage);
        
        System.out.print( "Partial overwrite (short text, trailing deletions): " );
        doc = new SourceDocument("test");
        te = new TypingEvent(1, TypingEventMode.insert, 0,
                originalMessage.length(), originalMessage, "owner", null);
        doc.addEvents(te.explode());
        msg = "vaults";
        te = new TypingEvent(doc.lastUpdateTime()+1, TypingEventMode.overwrite, 20, msg.length()+5, msg, "owner", null);
        doc.addEvents(te.explode());    
        resultingMessage = doc.toString();
        if (resultingMessage.equals( "The quick brown fox vaults the lazy dog"))
            System.out.println("pass");
        else
            System.out.println("fail, should of been " + "The quick brown fox vaults the lazy dog"
                    + " but got " + resultingMessage);
    }

    /**
     * 
     * startTime + stepSize * i
     * 
     * @author Lawrence
     */
    public static long t(long startTime, long stepSize, int i)
    {
        return startTime + stepSize * i;
    }

    /**
     * works out by how much time the events could be separated
     * 
     * @param startTime
     * @param endTime
     * @param n
     *            number of events
     * @author Lawrence
     * @return
     */
    public static long stepSize(long startTime, long endTime, int n)
    {
        return Math.max((endTime - startTime) / n, 1);
    }

    /**
     * generates events for testing routines
     * 
     * @param startTime
     * @param endTime
     * @param startingPosition
     *            of caret
     * @param text
     * @param mode
     *            typing mode that these events should be generated under
     * @param owner
     * @author Lawrence
     * @return
     */
    public static ArrayList<TypingEvent> generateEvents(long startTime,
            long endTime, int startingPosition, String text,
            TypingEventMode mode, String owner)
    {
        ArrayList<TypingEvent> tes = new ArrayList<TypingEvent>();
        final int n = text.length();
        final long stepSize = stepSize(startTime, endTime, n);
        int cp = startingPosition;
        int i = 0;
        while (i < n)
        {
            if (mode == TypingEventMode.backspace)
            {
                tes.add(new TypingEvent(t(startTime, stepSize, i), mode, cp, 1,
                        "\0", text, null));
                cp--;
            }
            else
            {
                tes.add(new TypingEvent(t(startTime, stepSize, i), mode, cp, 1,
                        "" + text.charAt(i), owner, null));
                cp++;
            }
            i++;
        }
        return tes;
    }

    /**
     * adds an event to the priority queue if it does not already exist. When a
     * locking/unlocking event is passed through here the end result of the
     * document is scanned for events within the region described by the event
     * position and length.
     * 
     * @author Lawrence
     * @param typingEvent
     */
    public void addEvent(TypingEvent typingEvent)
    {
        // TODO: it may be that a more efficient way of doing this can be found
        TypingEvent[] tes = new TypingEvent[this.typingEvents.size()];
        this.typingEvents.toArray(tes);

        if (!typingEvent.existsIn(tes) && !this.lockingEvent(typingEvent))
        {
            if (this.latestTime < typingEvent.time)
                this.latestTime = typingEvent.time;
            if(this.creationTime > typingEvent.time)
                this.creationTime = typingEvent.time;
            this.typingEvents.add(typingEvent);
        }
    }


    /**
     * For locking events only
     * 
     * @author Lawrence
     * @param lockingEvents
     */
    public void addLockingEvents(ArrayList<TypingEvent> lockingEvents)
    {
        for (TypingEvent le : lockingEvents)
            this.lockingEvent(le);
    }

    /**
     * When a locking/unlocking event is passed through here the end result of
     * the document is scanned for events within the region described by the
     * event position and length.
     * 
     * @author Lawrence
     * @param typingEvent
     * @return true if it was a locking or unlocking event
     */
    private boolean lockingEvent(TypingEvent typingEvent)
    {
        if (typingEvent.mode == TypingEventMode.lockRegion)
        {
            System.out.println(typingEvent.owner + " locked a region at "
                    + typingEvent.time);
            TypingEventList tel = this.playOutEvents(Long.MAX_VALUE);

            for (int i = 0; i < tel.length(); i++)
                if (this.insideRegion(typingEvent, i, 0))
                    tel.get(i).lockingGroup = typingEvent.owner;
            // te.locked = te.locked || this.insideRegion(typingEvent, te);
            this.typingEvents.add(typingEvent);
            return true;
        }
        else if (typingEvent.mode == TypingEventMode.unlockRegion)
        {
            System.out.println(typingEvent.owner + " unlocked a region at "
                    + typingEvent.time);
            TypingEventList tel = this.playOutEvents(Long.MAX_VALUE);
            TypingEvent te;
            for (int i = 0; i < tel.length(); i++)
            {
                te = tel.get(i);
                if (this.insideRegion(typingEvent, i, 1))
                    if (te.lockingGroup.equals(typingEvent.owner))
                        te.lockingGroup = null;
            }
            // te.locked = !this.insideRegion(typingEvent, te) && te.locked;
            this.typingEvents.add(typingEvent);
            return true;
        }
        else
            return false;
    }

    /**
     * Culls all events that are not making a visual impact on the end result of
     * the document and re-assigns the event times of the remaining events to
     * keep them in order.
     * 
     * @author Lawrence
     * @param endTime
     *            - the point where simplification stops
     */
    public void simplify(long endTime)
    {
        TypingEventList tel = this.playOutEvents(endTime);
        tel.homogenize(endTime);
        this.clearUpTo(endTime);
        this.typingEvents.addAll(tel.events());
    }

    /**
     * Returns a document where all events that are not making a visual impact
     * on the end result of the document have been culled and event times of the
     * remaining events are re assigned to keep them in order.
     * 
     * @author Lawrence
     * @param endTime
     *            - the time where simplification stops
     * @return
     */
    public SourceDocument simplified(long endTime)
    {
        SourceDocument doc = new SourceDocument(this.name, null,
                this.typingEvents);
        TypingEventList tel = this.playOutEvents(endTime);
        tel.homogenize(endTime);
        doc.clearUpTo(endTime);
        doc.typingEvents.addAll(tel.events());
        return doc;
    }

    /**
     * clears any event up to the time specified
     * 
     * @author Lawrence
     * @param endTime
     *            - the time where clearing stops
     */
    public void clearUpTo(long endTime)
    {
        while (this.typingEvents.size() > 0
                && this.typingEvents.peek().time < endTime)
            this.typingEvents.poll();
    }

    /**
     * Uses a typing event to specify a region starting from position of length
     * typingEvent.length.
     * 
     * @author Lawrence
     * @param region
     *            - typing event which represents a region of the document
     *            (usually a locking or unlocking event)
     * @param position
     * @param extendRight
     *            extends the region right by that many spaces
     * @return true if the position was in the region
     */
    private boolean insideRegion(TypingEvent region, int position,
            int extendRight)
    {
        return position >= region.position
                && position <= region.position + region.length - 1
                        + extendRight;
    }

    /**
     * adds a collection of events to the priority queue if they do not already
     * exist. When a locking/unlocking event is passed through here the end
     * result of the document is scanned for events within the region described
     * by the event position and length.
     * 
     * @author Lawrence
     * @param values
     *            - typing events to be added
     */
    public void addEvents(Collection<TypingEvent> values)
    {
        for (TypingEvent typingEvent : values)
            this.addEvent(typingEvent);
    }

    /**
     * Simulates the events from the priority queue until the time specified.
     * 
     * @author Lawrence
     * @param endTime
     * @return a TypingEventList which is all the events left over at that point
     *         in time which have a visible impact on the document.
     */
    public TypingEventList playOutEvents(Long endTime)
    {
        // PositionKey key;
        TypingEventList string = new TypingEventList();

        int initialCapacity = this.typingEvents.size();
        if (initialCapacity < 2)
            initialCapacity++;

        PriorityQueue<TypingEvent> q = new PriorityQueue<TypingEvent>(
                initialCapacity, new EventComparer());

        // deadlock quick-fix
        boolean success = false;
        int attempts = 10;
        while (!success && attempts > 0)
        {
            try
            {
                q.addAll(this.typingEvents);
                success = true;
            }
            catch (Exception e)
            {
                attempts--;

                // if (attempts == 0)
                // e.printStackTrace();
                e.printStackTrace();
            }
        }

        TypingEvent event;
        while (!q.isEmpty())
        {
            event = q.poll();
            if (event.time > endTime)
                break;

            switch (event.mode)
            {
            case insert:
            {
                string.insert(event);
            }
                break;
            case homogenized:
            {
                string.insert(event);
            }
            break;
            case overwrite:
            {
                string.overwrite(event);
                break;
            }
            case backspace:
            {
                string.backspace(event.position);
                break;
            }
            case delete:
            {
                string.delete(event.position, event.length);
                break;
            }
            case deleteAll:
            {
                string.clear();
                break;
            }
            }
        }

        return string;
    }

    /**
     * Convenience method which returns toString() of a typing event list.
     * 
     * @author Lawrence
     * @param tel
     *            TypingEventList to be converted to a String.
     * @return
     */
    protected static String treeToString(TypingEventList tel)
    {
        return tel.toString();
    }

    @Override
    /**
     * Returns a string which represents the document at the latest available time,
     * possibly to be displayed to the user or saved to a file.
     * 
     */
    public String toString()
    {
        return treeToString(this.playOutEvents(Long.MAX_VALUE));
    }

    /**
     * Convenience method which returns a String that represents the document at
     * the time specified, possibly to be displayed to the user or saved to a
     * file.
     * 
     * @author Lawrence
     * @param endTime
     * @return
     */
    public String timeTravel(Long endTime)
    {
        return treeToString(this.playOutEvents(endTime));
    }

    @Override
    public void push(Queue<TypingEvent> typingEvents)
    {
        while (!typingEvents.isEmpty())
        {
            TypingEvent typingEvent = typingEvents.poll();
            this.addEvents(typingEvent.explode());
        }
    }

    @Override
    public Queue<TypingEvent> events()
    {
        return this.typingEvents;
    }

    @Override
    public Queue<TypingEvent> eventsSince(long time)
    {
        int initialCapacity = this.typingEvents.size();
        if (initialCapacity < 2)
            initialCapacity++;

        PriorityQueue<TypingEvent> latestEvents = new PriorityQueue<TypingEvent>(
                initialCapacity, new EventComparer());

        try
        {
            for (TypingEvent te : this.typingEvents)
            {
                if (te.time >= time)
                    latestEvents.add(te);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return latestEvents;
    }

    public Queue<TypingEvent> eventsBetween(long start, long end)
    {
        int initialCapacity = this.typingEvents.size();
        if (initialCapacity < 2)
            initialCapacity++;

        PriorityQueue<TypingEvent> eventsOut = new PriorityQueue<TypingEvent>(
                initialCapacity, new EventComparer());

        int stage = 0;
        try
        {
            for (TypingEvent te : this.typingEvents)
            {
                if (te.time >= start && te.time < end)
                {
                    eventsOut.add(te);
                    stage = 1;
                }
                else if (stage == 1)
                    break;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return eventsOut;
    }

    @Override
    public long lastUpdateTime()
    {
        return this.latestTime;
    }

    @Override
    public void clearAll()
    {
        this.typingEvents.clear();
    }

    public PriorityQueue<TypingEvent> orderedEvents()
    {
        return this.typingEvents;
    }

    public String shortName()
    {
        return this.name.split("\\.")[0];
    }

    public Long getCreationTime()
    {
        return this.creationTime;
    }

    public String docFieldXML(String indent)
    {
        return indent + "<Name>" + this.name + "</Name>\n" + indent + "<CreationTime>" + this.creationTime + "</CreationTime>\n";
    }

    public void setCreationTime(long creationTime)
    {
        this.creationTime = creationTime;
    }
}
