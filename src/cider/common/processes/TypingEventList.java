package cider.common.processes;

import java.util.ArrayList;

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
        this.tel.set(te.position, te);
    }

    public void backspace(int i)
    {
        if (i < 0)
            return;
        else if (i >= this.tel.size())
            this.tel.remove(this.tel.size() - 1);
        else
            this.tel.remove(i);
    }

    public void clear()
    {
        this.tel.clear();
    }

    @Override
    public String toString()
    {
        String string = "";
        for (TypingEvent te : this.tel)
            string += te.text;
        return string;
    }
}
