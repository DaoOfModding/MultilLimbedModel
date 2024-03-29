package DaoOfModding.mlmanimator.Client.AnimationFramework;

public class AnimationLocker
{
    protected static int locks = 1;

    // Allocate the specified number of animation locks to this project
    // Return int that all animation locks of this project should start from
    public static int allocateLocks(int numberOfLocks)
    {
        int oldLocks = locks;
        locks += numberOfLocks;

        return oldLocks;
    }
}
