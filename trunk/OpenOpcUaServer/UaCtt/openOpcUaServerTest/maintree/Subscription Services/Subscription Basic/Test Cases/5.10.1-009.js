/*  Test 5.10.1 test 9 prepared by Nathan Pocock; nathan.pocock@opcfoundation.org
    Description:
        Create a subscription where requestedLifetimeCounter is less than 3 times the requestedMaxKeepAliveCount.
        ServiceResult = Good.
        All returned parameter values are valid and correct. revisedLifetimeCounter is at least
        3 times that of the revisedMaxKeepAliveCount. If the revised is not 3x then the test
        is fail!

    Revision History
        25-Aug-2009: Initial version.
        20-Nov-2009: REVIEWED.

    MORE INFORMATION:
        Refer to Test Lab Part 8 specifications, section 5.10.1.
*/

function createSubscription5101009()
{
    var subscription = new Subscription( null, null, 10, 15 );
    if( createSubscription( subscription, g_session ) )
    {
        // check that the subscription works anyway.
        if( createMonitoredItems( defaultStaticItem, TimestampsToReturn.Both, subscription, g_session ) )
        {
            wait( subscription.RevisedPublishingInterval );
            AssertEqual( true, publishService.Execute() && publishService.CurrentlyContainsData(), "Expected Publish() to succeed, and to contain a dataChange." );
            deleteMonitoredItems( defaultStaticItem, subscription, g_session );
        }
    }
    deleteSubscription( subscription, g_session );
    publishService.Clear();
}

safelyInvoke( createSubscription5101009 );