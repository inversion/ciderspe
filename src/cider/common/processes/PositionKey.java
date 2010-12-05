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
    }

    private Level topLevel = new Level();

    public PositionKey(Level left, Level right)
    {

    }
}
