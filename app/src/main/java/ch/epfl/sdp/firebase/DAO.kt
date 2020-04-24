package ch.epfl.sdp.firebase

interface DAO {
    /**
     * Connects to the database using the given credentials
     */
    fun connect(/*Credentials*/)

    /**
     * Returns a list of all groups
     */
    fun getGroups()

    // Group methods
    /**
     * Makes the given user join the given group as a rescuer, if allowed
     */
    fun joinGroupAsRescuer(/*group_id, rescuer_id*/)

    /**
     * Makes the given user join the given group as an operator , if allowed
     */
    fun joinGroupAsOperator(/*group_id, rescuer_id*/)

    /**
     * Creates a group with the given group name and position with the given user as operator
     */
    fun createGroup(/*groupName, groupPosition, user_id */)

    /**
     * Makes the given user leave the given group
     */
    fun leaveGroup(/*groupId, userId*/)

    /**
     * Adds a point to the heatmap of the given group, if allowed
     */
    fun addPointToHeatmap(/*group_id, user_id, pointPosition*/)
    // Add more useful functions to access the database
}