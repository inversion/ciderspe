package cider.common.processes;

@Deprecated
public class LockingRegion implements Comparable<LockingRegion>
{
    public String owner;
    public int start;
    public int end;

    @Deprecated
    public LockingRegion(int start)
    {
        this.start = start;
    }

    @Deprecated
    public LockingRegion(String owner, int start, int end)
    {
        this.owner = owner;
        this.start = start;
        this.end = end;
    }

    @Deprecated
    public boolean coversOver(String self, int position)
    {
        return position >= this.start && position <= this.end;// &&
        // !this.owner.equals(self);
    }

    @Deprecated
    public void move(int amount)
    {
        this.start += amount;
        this.end += amount;
    }

    @Override
    @Deprecated
    public int compareTo(LockingRegion lr)
    {
        if (this.start < lr.start)
            return -1;
        else if (this.start > lr.start)
            return 1;
        else
            return 0;
    }
}
