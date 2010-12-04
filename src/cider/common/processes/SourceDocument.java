package cider.common.processes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;
import java.util.TreeMap;

/**
 * Experimental work: Real-time merging (unfinished)
 * 
 * @author Lawrence
 */
public class SourceDocument implements ICodeLocation
{
    private PriorityQueue<TypingEvent> typingEvents;

    private long latestTime;

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
        return testLog;
    }

    protected static String shuffledEventsTest()
    {
        String expected = "the quick muddled fox bounced over the lazy dog";

        ArrayList<TypingEvent> tes = new ArrayList<TypingEvent>();
        tes.addAll(generateEvents(0, 100, 0,
                "the quick brown fox jumped over the lazy dog",
                TypingEventMode.insert));
        tes.addAll(generateEvents(200, 500, 10, "muddled",
                TypingEventMode.overwrite));
        tes.addAll(generateEvents(600, 700, 16, " f", TypingEventMode.insert));
        tes.addAll(generateEvents(800, 1000, 27, "jumped",
                TypingEventMode.backspace));
        tes.addAll(generateEvents(2000, 3000, 21, "bounced",
                TypingEventMode.insert));

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

    protected static ArrayList<TypingEvent> generateEvents(long startTime,
            long endTime, int startingPosition, String text,
            TypingEventMode mode)
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
                tes.add(new TypingEvent(t(startTime, stepSize, i), mode, cp,
                        "\0"));
                cp--;
            }
            else
            {
                tes.add(new TypingEvent(t(startTime, stepSize, i), mode, cp, ""
                        + text.charAt(i)));
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

    protected static Double keyAt(TreeMap<Double, TypingEvent> string,
            int caretPosition)
    {
        Double result = 0.0;
        for (Entry<Double, TypingEvent> entry : string.entrySet())
        {
            if (caretPosition == -1)
                break;
            else
            {
                caretPosition--;
                result = entry.getKey();
            }
        }
        return result;
    }

    protected static Double generateKeyJustAfter(
            TreeMap<Double, TypingEvent> string, int caretPosition)
    {
        Double result = keyAt(string, caretPosition);
        Double higher = string.higherKey(result);
        result += higher == null ? result + 2.0 : higher;
        result /= 2.0;
        return result;
    }

    public TreeMap<Double, TypingEvent> playOutEvents(Long endTime)
    {
        Double key;
        TreeMap<Double, TypingEvent> string = new TreeMap<Double, TypingEvent>();

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
                key = generateKeyJustAfter(string, event.position);
                // System.out.println(event.position);
                string.put(key, event);
            }
                break;
            case overwrite:
            {
                key = keyAt(string, event.position);
                string.put(key, event);
                break;
            }
            case backspace:
            {
                key = keyAt(string, event.position);
                string.remove(key);
                break;
            }
            case deleteAll:
            {
                string.clear();
            }
            }
        }

        return string;
    }

    protected static String treeToString(TreeMap<Double, TypingEvent> survived)
    {
        String str = "";
        for (TypingEvent event : survived.values())
            str += event.text;
        return str;
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

        for (TypingEvent te : this.typingEvents)
            if (te.time > time)
                latestEvents.add(te);

        return latestEvents;
    }

    @Override
    public long lastUpdateTime()
    {
        return this.latestTime;
    }
}
