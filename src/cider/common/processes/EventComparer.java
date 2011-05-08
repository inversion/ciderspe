package cider.common.processes;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Compares event priority based on their time of creation, used by the
 * priority queue
 * 
 * @author Lawrence
 * 
 */
public class EventComparer implements Comparator<TypingEvent>,
        Serializable
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

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

