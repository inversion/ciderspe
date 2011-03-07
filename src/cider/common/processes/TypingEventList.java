package cider.common.processes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Random;

public class TypingEventList
{
    private ArrayList<TypingEvent> tel = new ArrayList<TypingEvent>();

    public TypingEventList()
    {

    }

    public void insert(TypingEvent te)
    {
        if (te.position >= tel.size())
            this.tel.add(te);
        else
            this.tel.add(te.position + 1, te);
    }

    public void overwrite(TypingEvent te)
    {
        if (te.position >= tel.size())
            this.tel.add(te);
        else
            this.tel.set(te.position, te);
    }

    public void backspace(int i)
    {
        if (i >= this.tel.size())
            i = this.tel.size() - 1;
        if (i < 0)
            return;
        this.tel.remove(i);
    }

    public boolean exists(int i)
    {
        return i > 0 && i < this.tel.size();
    }

    public TypingEvent get(int i)
    {
        return this.tel.get(i);
    }

    public void clear()
    {
        this.tel.clear();
    }

    public static void main(String[] args)
    {
        test();
    }

    public static void test()
    {
        Random rand = new Random();
        TypingEventList tel = new TypingEventList();

        for (int test = 0; test < 100; test++)
        {
            TypingEvent te = new TypingEvent(0, TypingEventMode.deleteAll, 0,
                    1, "x", "test user", null);
            switch (rand.nextInt(3))
            {
            case 0:
                tel.insert(te);
                break;
            case 1:
                tel.overwrite(te);
                break;
            case 2:
                tel.backspace(rand.nextInt(200));
                break;
            }
        }

        System.out.println("reached end");
    }

    @Override
    public String toString()
    {
        String string = "";
        for (TypingEvent te : this.tel)
            string += te.text;
        return string;
    }

    public LinkedList<TypingEventList> split(String string)
    {
        LinkedList<TypingEventList> ll = new LinkedList<TypingEventList>();
        TypingEventList current = new TypingEventList();
        for (TypingEvent te : this.tel)
            if (te.text.equals(string))
            {
                ll.add(current);
                current = new TypingEventList();
            }
            else
                current.tel.add(te);
        ll.add(current);
        return ll;
    }

    static boolean belongsTo(String string, String[] strings)
    {
        for (String str : strings)
            if (str.equals(string))
                return true;
        return false;
    }

    public LinkedList<TypingEventList> splitWords(String[] dividers)
    {
        LinkedList<TypingEventList> ll = new LinkedList<TypingEventList>();
        TypingEventList current;
        current = new TypingEventList();
        for (TypingEvent te : this.tel)
            if (belongsTo(te.text, dividers))
            {
                ll.add(current);
                current = new TypingEventList();
            }
            else
                current.tel.add(te);
        ll.add(current);
        return ll;
    }

    public int locked(int position, String user)
    {
        TypingEvent te = this.tel.get(position);
        return te.lockingGroup == null ? 0 : (te.lockingGroup.equals(user) ? 1
                : 2);
    }

    public int length()
    {
        return this.tel.size();
    }

    public boolean newline()
    {
        return this.tel.size() == 0 || this.tel.size() == 1
                && this.tel.get(0).equals("\n");
    }

    public Collection<? extends TypingEvent> events()
    {
        return this.tel;
    }

    public void homogenize(long end)
    {
        ArrayList<TypingEvent> telh = this.tel;
        int size = telh.size();
        long start = end - size;
        long t = start;
        for (int i = 0; i < size; i++)
            telh.set(i, new TypingEvent(telh.get(i), t++, i,
                    TypingEventMode.insert));
    }

    public int getLastPositionOf(TypingEvent te)
    {
        return this.tel.lastIndexOf(te);
    }

    // TODO lolwut
    public int countCharactersFor(String user)
    {
        int count = 0;
        for (TypingEvent te : this.tel)
        {
            if (te.owner.equals(user) && !te.text.equals("\n"))
            {
                count++;
            }
        }
        return count;
    }

    public Hashtable<String, Integer> countCharactersAll()
    {
        Hashtable<String, Integer> results = new Hashtable<String, Integer>();
        for (TypingEvent te : this.tel)
        {
            if (!te.text.equals("\n"))
            {
                if (results.containsKey(te.owner))
                {
                    results.put(te.owner, results.get(te.owner) + 1);
                }
                else
                {
                    results.put(te.owner, 0);
                }
            }
        }
        return results;
    }
}
