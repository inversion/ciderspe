package cider.common.processes;

public class PositionKey implements Comparable<PositionKey>
{
    /**
     * 'sup dog; so I heard you like integers?
     * 
     * @author Lawrence
     * 
     */
    private class LevelPair
    {
        public Level fst;
        public Level snd;
        public int depth;

        public LevelPair(int depth, Level fst, Level snd)
        {
            this.fst = fst;
            this.snd = snd;
            this.depth = depth;
        }

        public Level sandwich()
        {
            if (this.fst == null)
            {
                if (this.snd == null)
                    return new Level(this.depth, 0);
                else
                {
                    int back = this.snd.value - 1;
                    if (back > 0)
                        back = 0;
                    return new Level(this.depth, back);
                }
            }
            else
            {
                if (this.snd == null)
                    return new Level(this.fst.depth, this.fst.value - 1);
                else
                {
                    int next = this.fst.value + 1;
                    if (next != this.snd.value)
                        return new Level(this.depth, next);
                    else
                    {
                        Level result = new Level(this.depth, this.fst.value);
                        if (this.fst.next == null)
                            result.next = new Level(this.depth + 1, 0);
                        else
                            result.next = new Level(this.depth + 1,
                                    this.fst.next.value + 1);
                        return result;
                    }
                }
            }
        }
    }

    private class Level implements Comparable<Level>
    {
        public int value = 0;
        Level next = null;
        int depth;

        public Level(int depth, int value)
        {
            this.value = value;
            this.depth = depth;
        }

        public Level(Level level)
        {
            this.depth = level.depth;
            this.value = level.value;
            if (level.next != null)
                this.next = new Level(level.next);
            else
                this.next = null;
        }

        LevelPair whereDifferenceBegins(Level other)
        {
            if (this.value != other.value)
                return new LevelPair(this.depth, this, other);
            else
            {
                if (this.next == null || other.next == null)
                    return new LevelPair(this.depth + 1, this.next, other.next);
                else
                    return this.next.whereDifferenceBegins(other.next);
            }
        }

        public void replaceSubLevel(Level level)
        {
            if (this.depth == level.depth)
            {
                this.value = level.value;
                this.next = new Level(level.next);
            }
            else
            {
                if (this.next == null)
                    this.next = new Level(this.depth + 1, 0);
                else
                    this.replaceSubLevel(this.next);
            }
        }

        @Override
        public int compareTo(Level other)
        {
            if (this.value > other.value)
                return 1;
            else if (this.value < other.value)
                return -1;
            else
            {
                if (this.next == null)
                {
                    if (other.next == null)
                        return 0;
                    else if (other.next.value < 0)
                        return 1;
                    else
                        return -1;
                }
                else if (other.next == null)
                {
                    if (this.next.value < 0)
                        return -1;
                    else
                        return 1;
                }
                else
                    return this.next.compareTo(other.next);
            }
        }

        @Override
        public String toString()
        {
            // TODO Auto-generated method stub
            return super.toString();
        }

    }

    private Level root;

    public PositionKey(int topValue)
    {
        this.root = new Level(0, topValue);
    }

    public PositionKey(LevelPair levelPair)
    {
        this.init(levelPair);
    }

    public void init(LevelPair levelPair)
    {
        Level level = new Level(levelPair.fst);
        LevelPair difference = level.whereDifferenceBegins(levelPair.snd);
        level.replaceSubLevel(difference.sandwich());
        this.root = level;
    }

    public PositionKey(PositionKey k1, PositionKey k2)
    {
        LevelPair levelPair = new LevelPair(0, k1.root, k2.root);
        this.init(levelPair);
    }

    int getTopLevelValue()
    {
        return this.root.value;
    }

    @Override
    public int compareTo(PositionKey other)
    {
        return this.root.compareTo(other.root);
    }

    public static void main(String[] args)
    {
        PositionKey k1 = new PositionKey(0);
        PositionKey k2 = new PositionKey(1);
        System.out.println(testInsert(100, k1, k2) ? "passed insertion test"
                : "failed insertion test");
    }

    static boolean testInsert(int numberOfGoes, PositionKey k1, PositionKey k2)
    {
        if (numberOfGoes > 0)
        {
            PositionKey k3 = new PositionKey(k1, k2);
            if (k3.compareTo(k1) == 1 && k3.compareTo(k2) == -1)
                return testInsert(numberOfGoes - 1, k3, k2);
            else
                return false;
        }
        else
            return true;
    }

    @Override
    public String toString()
    {
        return this.root.toString();
    }

}
