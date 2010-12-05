package cider.common.processes;

public class PositionKey
{
    private class LevelPair
    {
        public Level fst;
        public Level snd;

        public LevelPair(Level fst, Level snd)
        {
            this.fst = fst;
            this.snd = snd;
        }
    }

    private class Level
    {
        public int value = 0;
        Level next = null;

        LevelPair whereDifferenceBegins(Level other)
        {
            if (this.value != other.value)
                return new LevelPair(this, other);
            else
            {
                if (this.next == null || other.next == null)
                    return new LevelPair(this.next, other.next);
                else
                    return this.next.whereDifferenceBegins(other.next);
            }
        }
    }

    private Level topLevel = new Level();

    public PositionKey(Level left, Level right)
    {

    }
}
