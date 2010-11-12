package cider.common.processes;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * Experimental work: Real-time merging (unfinished) TODO: concurrent editing
 * where the time field of TypingEvent indicates what time the edit occurred.
 * 
 * @author Lawrence
 */
public class SourceDocument
{
    private TreeMap<Double, TypingEvent> localText;
    private Double caretPosition = 0.0;
    private Double endNumber = 1000.0;

    public SourceDocument()
    {
        this.localText = new TreeMap<Double, TypingEvent>();
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
        String testLog = singleThreadTest() + "\n";
        testLog += lengthTest();
        // TODO: When concurrent merging is ready I'll write a test for that,
        // probably by having two threads representing two different users
        // editing a document at the same time.
        return testLog;
    }

    public static String singleThreadTest()
    {
        final String str1 = "the quick brown fox jumped over the lazy dog.";
        final String str2 = "Jackdaws love my sphinx of black quartz.";
        SourceDocument doc = new SourceDocument();
        doc.setAbsoluptCaretPosition(0);
        int i = 0;
        int j = 0;

        for (; i < 4; i++)
            // the
            doc.type(0, str1.charAt(i));
        for (; j < 9; j++)
            // Jackdaws
            doc.type(0, str2.charAt(j));
        for (; i < 10; i++)
            // quick
            doc.type(0, str1.charAt(i));
        for (; j < 14; j++)
            // love
            doc.type(0, str2.charAt(j));
        for (; i < 16; i++)
            // brown
            doc.type(0, str1.charAt(i));
        for (; j < 16; j++)
            // my
            doc.type(0, str2.charAt(j));

        // replace love
        doc.setAbsoluptCaretPosition(23);

        for (int z = 0; z < 5; z++)
            doc.backspace();

        final String result = doc.toString();
        final String expected = "the Jackdaws quick brown my";
        if (result.equals(expected))
            return "pass";
        else
            return "fail: did not pass singleThreadTest where result should be '"
                    + expected + "', instead it is '" + result + "'.";
    }

    public static String lengthTest()
    {
        SourceDocument doc = new SourceDocument();
        doc.setAbsoluptCaretPosition(0);
        String expected = "";

        int j = 0;
        String alphabet = "abcdefghijklmnopqrstuvwxyz";

        for (int i = 0; i < 10000; i++)
        {
            expected = expected + alphabet.charAt(j);
            doc.type(0, alphabet.charAt(j));
            j++;
            if (j >= alphabet.length())
                j = 0;
        }

        String result = doc.toString();
        if (result.equals(expected))
            return "pass";
        else
            return "fail: did not pass length test where result should be '"
                    + expected + "' instead it is '" + result + "'.";
    }

    public void setCaretPosition(Double caretPosition)
    {
        this.caretPosition = caretPosition;
    }

    public boolean setAbsoluptCaretPosition(int absoluptPosition)
    {
        Iterator<Double> iterator = this.localText.descendingKeySet()
                .descendingIterator();

        for (int i = 0; i < absoluptPosition; i++)
            iterator.next();

        if (iterator.hasNext())
        {
            this.caretPosition = iterator.next();
            return true;
        }
        else
            return false;
    }

    public Double getCaretPosition()
    {
        return this.caretPosition;
    }

    public int absoluptCaretPosition()
    {
        return this.localText.headMap(this.caretPosition).size();
    }

    public void insertCharacter(Double position, TypingEvent typingEvent)
    {
        this.localText.put(position, typingEvent);
    }

    public void type(long time, char chr)
    {
        TypingEvent te = new TypingEvent(chr, time, TypingEventMode.insert);
        te.chr = chr;
        this.type(te);
    }

    public double higherKey()
    {
        Double higherKey = this.localText.higherKey(this.caretPosition);
        if (higherKey == null)
            higherKey = endNumber;
        return higherKey;
    }

    public void type(TypingEvent typingEvent)
    {
        Double upperBound = this.caretPosition + 1.0 - Double.MIN_VALUE;
        Entry<Double, TypingEvent> existingEntry = this.localText
                .floorEntry(upperBound);

        Double nextValue;

        if (existingEntry == null)
            nextValue = this.caretPosition;
        else
        {
            while (existingEntry.getValue().time > typingEvent.time)
                existingEntry = this.localText.floorEntry(existingEntry
                        .getKey());

            Double greatestLowerBound = existingEntry.getKey();
            Double lowestUpperBound = this.localText
                    .higherKey(greatestLowerBound);

            if (lowestUpperBound == null)
                lowestUpperBound = this.caretPosition + 1.0;

            nextValue = (greatestLowerBound + lowestUpperBound) / 2.0;
        }

        this.localText.put(nextValue, typingEvent);

        if (typingEvent.mode == TypingEventMode.insert
                || typingEvent.mode == TypingEventMode.overwrite)
            this.caretPosition++;
        else
            this.caretPosition--;
    }

    public TypingEvent backspace()
    {
        Double lowerKey = this.localText.lowerKey(this.caretPosition);
        return this.localText.remove(lowerKey);
    }

    @Override
    public String toString()
    {
        String result = "";
        for (TypingEvent typingEvent : this.localText.values())
            result += typingEvent;
        return result;
    }
}
