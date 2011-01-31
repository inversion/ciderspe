package cider.common.processes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;
import java.util.TreeMap;

/**
 * 
 * @author Lawrence
 */
public class SourceDocument implements ICodeLocation
{
    private PriorityQueue<TypingEvent> typingEvents;
    public String name = "untitled";
    private long latestTime;
    private TreeMap<Integer, LockingRegion> lockingRegions = new TreeMap<Integer, LockingRegion>();
    private String owner = "";

    public SourceDocument(String name)
    {
        this.name = name;
        this.typingEvents = new PriorityQueue<TypingEvent>(1000,
                new EventComparer());
    }

    public SourceDocument()
    {
        this.typingEvents = new PriorityQueue<TypingEvent>(1000,
                new EventComparer());
    }

    public static void main(String[] args)
    {
        System.out.println(test());
    }

    /**
     * No we probably wont write tests all that often, but in this case it was
     * actually the quickest way of trying my code out. It's not a particularly
     * rigorous test.
     * 
     * @author Lawrence
     * @return
     */
    public static String test()
    {
        String testLog = shuffledEventsTest() + "\n";
        testLog += lengthTest();
        return testLog;
    }

    protected static String shuffledEventsTest()
    {
        String expected = "the quick 123123123123123123123123123 muddled fox bounced over the lazy dog";

        ArrayList<TypingEvent> tes = new ArrayList<TypingEvent>();
        tes.addAll(generateEvents(0, 100, 0,
                "the quick brown fox jumped over the lazy dog",
                TypingEventMode.insert, "na"));
        tes.addAll(generateEvents(200, 500, 10, "muddled",
                TypingEventMode.overwrite, "na"));
        tes.addAll(generateEvents(600, 700, 16, " f", TypingEventMode.insert,
                "na"));
        tes.addAll(generateEvents(800, 1000, 27, "jumped",
                TypingEventMode.backspace, "na"));
        tes.addAll(generateEvents(2000, 2500, 21, "bounced",
                TypingEventMode.insert, "na"));
        tes.addAll(generateEvents(2600, 3000, 9,
                "123123123123123123123123123 ", TypingEventMode.insert, "na"));

        tes = shuffledEvents(tes, new Date().getTime());

        SourceDocument testDoc = new SourceDocument();
        for (TypingEvent event : tes)
            testDoc.putEvent(event);
        String result = testDoc.toString();
        return expected.equals(result) ? "pass"
                : "fail: did not pass shuffled events test since toString returned '"
                        + result
                        + "', where as it should of been '"
                        + expected
                        + "'.";
    }

    protected static String lengthTest()
    {
        ArrayList<TypingEvent> tes = new ArrayList<TypingEvent>();
        tes.add(new TypingEvent(0, TypingEventMode.insert, 0, 1, "<", "na"));
        tes.add(new TypingEvent(1, TypingEventMode.insert, 1, 1, ">", "na"));

        String bigString = "";
        final String alphabet = "10";
        final char[] alphaChars = alphabet.toCharArray();
        final int l = alphabet.length();
        for (int i = 0; i < 10000; i++)
            bigString += alphaChars[i % l];

        tes.addAll(generateEvents(0, 10000, 0, bigString,
                TypingEventMode.insert, "na"));

        SourceDocument testDoc = new SourceDocument();
        for (TypingEvent event : tes)
            testDoc.putEvent(event);
        String result = testDoc.toString();
        return (result.startsWith("<") && result.endsWith(">")) ? "pass"
                : "fail: did not pass the length test.";
    }

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

    public static long t(long startTime, long stepSize, int i)
    {
        return startTime + stepSize * i;
    }

    public static long stepSize(long startTime, long endTime, int n)
    {
        return (endTime - startTime) / n;
    }

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
                        "\0", text));
                cp--;
            }
            else
            {
                tes.add(new TypingEvent(t(startTime, stepSize, i), mode, cp, 1,
                        "" + text.charAt(i), owner));
                cp++;
            }
            i++;
        }
        return tes;
    }

    public void putEvent(TypingEvent typingEvent)
    {
        this.typingEvents.add(typingEvent);
        if (this.latestTime > typingEvent.time)
            this.latestTime = typingEvent.time;
    }

    public void putEvents(Collection<TypingEvent> values)
    {
        for (TypingEvent typingEvent : values)
            this.putEvent(typingEvent);
    }

    public boolean lockedOut(int position)
    {
        for (LockingRegion lockingRegion : this.lockingRegions.values())
            if (lockingRegion.coversOver(this.owner, position))
                return true;
        return false;
    }

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

                if (attempts == 0)
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
                if (!this.lockedOut(event.position))
                    string.insert(event);
            }
                break;
            case overwrite:
            {
                if (!this.lockedOut(event.position))
                    string.overwrite(event);
                break;
            }
            case backspace:
            {
                if (!this.lockedOut(event.position))
                    string.backspace(event.position);
                break;
            }
            case deleteAll:
            {
                string.clear();
            }
            case lockRegion:
            {
                if (!(this.lockedOut(event.position) || this
                        .lockedOut(event.position + event.length)))
                    this.lockingRegions.put(event.position, new LockingRegion(
                            this.owner, event.position, event.position
                                    + event.length));
            }
            case unlockRegion:
            {
                this.lockingRegions.remove(event.position);
            }
            }
        }

        return string;
    }

    public LockingRegion getLockingRegion(int position)
    {
        return this.lockingRegions.get(position);
    }

    public String getOwner()
    {
        return this.owner;
    }

    protected static String treeToString(TypingEventList tel)
    {
        return tel.toString();
    }

    @Override
    public String toString()
    {
        return treeToString(this.playOutEvents(Long.MAX_VALUE));
    }

    public String timeTravel(Long endTime)
    {
        return treeToString(this.playOutEvents(endTime));
    }

    class EventComparer implements Comparator<TypingEvent>
    {
        public int compare(TypingEvent e1, TypingEvent e2)
        {
            if (e1.time > e2.time)
                return 1;
            else if (e1.time < e2.time)
                return -1;
            else
                return 0;
        }
    }

    @Override
    public void push(Queue<TypingEvent> typingEvents)
    {
        while (!typingEvents.isEmpty())
        {
            TypingEvent typingEvent = typingEvents.poll();
            Collection<TypingEvent> fragments = typingEvent.explode().values();
            this.putEvents(fragments);
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

        }
        return latestEvents;
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
}
