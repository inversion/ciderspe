package cider.common.processes;

import java.util.ArrayList;
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
                    1, "x", "test user");
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

    public boolean locked(int position)
    {
        return this.tel.get(position).locked;
    }

    public int length()
    {
        return this.tel.size();
    }
}
