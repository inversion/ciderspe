package cider.common.processes;

public class LockingRegion
{
    public String owner;
    public int start;
    public int end;

    public LockingRegion(String owner, int start, int end)
    {
        this.owner = owner;
        this.start = start;
        this.end = end;
    }
}
