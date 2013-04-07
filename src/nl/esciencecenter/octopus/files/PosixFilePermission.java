package nl.esciencecenter.octopus.files;

public enum PosixFilePermission {

    /**
     * Execute/search permission, group.
     */
    GROUP_EXECUTE,
    /**
     * Read permission, group.
     */
    GROUP_READ,
    /**
     * Write permission, group.
     */
    GROUP_WRITE,
    /**
     * Execute/search permission, others.
     */
    OTHERS_EXECUTE,
    /**
     * Read permission, others.
     */
    OTHERS_READ,
    /**
     * Write permission, others.
     */
    OTHERS_WRITE,
    /**
     * Execute/search permission, owner.
     */
    OWNER_EXECUTE,
    /**
     * Read permission, owner.
     */
    OWNER_READ,
    /**
     * Write permission, owner.
     */
    OWNER_WRITE,

}