package cider.common.processes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;

/**
 * 
 * @author Lawrence
 */
public class SourceDocument implements ICodeLocation
{
    private PriorityQueue<TypingEvent> typingEvents;
    public String name = "untitled";
    private long latestTime;
    private String owner;

    public SourceDocument(String owner, String name)
    {
        this.owner = owner;
        this.name = name;
        this.typingEvents = new PriorityQueue<TypingEvent>(1000,
                new EventComparer());
    }

    public SourceDocument(String owner, String name,
            PriorityQueue<TypingEvent> typingEvents)
    {
        this.owner = owner;
        this.name = name;
        this.typingEvents = new PriorityQueue<TypingEvent>(typingEvents);
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
        String testLog = shuffleAndSimplificationTest() + "\n";
        testLog += lengthTest();
        return testLog;
    }

    protected static String shuffleAndSimplificationTest()
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

        SourceDocument testDoc = new SourceDocument("test owner",
                "testdoc.SourceDocument");
        for (TypingEvent event : tes)
            testDoc.putEvent(event);

        String result = testDoc.toString();
        String testResult = expected.equals(result) ? "pass\n"
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
            testDoc.putEvent(event);

        result = testDoc.toString();
        testResult += expected.equals(result) ? "pass"
                : "fail: simplification followed by new events produced '"
                        + result + "', where as it should of been '" + expected
                        + "'.";

        return testResult;
    }

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

        SourceDocument testDoc = new SourceDocument("test owner",
                "testDoc.SourceDocument");
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
        return Math.max((endTime - startTime) / n, 1);
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

    public void putEvent(TypingEvent typingEvent)
    {
        // TODO: it may be that a more efficient way of doing this can be found
        TypingEvent[] tes = new TypingEvent[this.typingEvents.size()];
        this.typingEvents.toArray(tes);

        if (!typingEvent.existsIn(tes) && !this.lockingEvent(typingEvent))
        {
            if (this.latestTime > typingEvent.time)
                this.latestTime = typingEvent.time;
            this.typingEvents.add(typingEvent);
        }
    }

    public void addLockingEvents(ArrayList<TypingEvent> lockingEvents)
    {
        for (TypingEvent le : lockingEvents)
            this.lockingEvent(le);
    }

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

    public void simplify(long endTime)
    {
        TypingEventList tel = this.playOutEvents(endTime);
        tel.homogenize(endTime);
        this.clearUpTo(endTime);
        this.typingEvents.addAll(tel.events());
    }

    public SourceDocument simplified(long endTime)
    {
        SourceDocument doc = new SourceDocument(this.owner, this.name,
                this.typingEvents);
        TypingEventList tel = this.playOutEvents(endTime);
        tel.homogenize(endTime);
        doc.clearUpTo(endTime);
        doc.typingEvents.addAll(tel.events());
        return doc;
    }

    public void clearUpTo(long endTime)
    {
        while (this.typingEvents.size() > 0
                && this.typingEvents.peek().time < endTime)
            this.typingEvents.poll();
    }

    private boolean insideRegion(TypingEvent region, int position,
            int extendRight)
    {
        return position >= region.position
                && position <= region.position + region.length - 1
                        + extendRight;
    }

    public void putEvents(Collection<TypingEvent> values)
    {
        for (TypingEvent typingEvent : values)
            this.putEvent(typingEvent);
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
            case deleteAll:
            {
                string.clear();
                break;
            }
            }
        }

        return string;
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
            this.putEvents(typingEvent.explode());
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
