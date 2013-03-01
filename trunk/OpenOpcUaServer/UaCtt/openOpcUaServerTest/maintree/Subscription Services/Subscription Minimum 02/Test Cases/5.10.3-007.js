/*  Test 5.10.3 Test 7 prepared by Nathan Pocock; nathan.pocock@opcfoundation.org

    Description:
        Enables mutliple subscriptions whose publishing state varies
        between active and inactive.
        
    Revision History
        24-Aug-2009 DEV: Initial version
        21-Oct-2009 NP : Upgraded script to use new script library functions.
        17-Nov-2009 NP : Revised script to meet new test-case criteria.
                         REVIEWED.
        24-Dec-2009 DP: Finds a configured node setting (instead of always using Int32).
*/

function setPublishingMode5103007()
{
    var subscriptions = [ 
        new Subscription( null, false ), 
        new Subscription() ];

    var publishCount = subscriptions.length * 2;
    var allItems     = [];
    var i;

    for( i=0; i<subscriptions.length; i++ )
    {
        if( createSubscription( subscriptions[i], g_session ) )
        {
            // add some monitored items our subscriptions
            var items = MonitoredItem.Clone( monitoredItem );
            if( !createMonitoredItems( items, TimestampsToReturn.Both, subscriptions[i], g_session ) )
            {
                return;
            }
            allItems[i] = items;
        }
    }

    // now to call Publish on all of these subscriptions to make sure that
    // we are NOT receiving any data change notifications...
    addLog( "Call PUBLISH to make sure that we are receiving data for enabled subscriptions." );
    var publish = new Publish( g_session );
    for( i=0; i<publishCount; i++ )
    {
        publish.Execute( true ); //do not acknowledge any subscriptions
    }

    // now check which subscriptions provided dataChanges
    AssertSubscriptionCallbacks( subscriptions, publish );
    // clear the publish object's properties...
    publish.Clear();

    // set publishing mode, DISABLE ALL subscriptions
    var setPublishing = new SetPublishingMode( g_session );
    if( setPublishing.Execute( subscriptions, true ) )
    {
        // write to the monitoredItem
        var WriteService = new Write( g_session );
        for( i=0; i<allItems.length; i++ )
        {
            GenerateScalarValue( allItems[i].Value.Value, allItems[i].DataType, 7 );
        }
        WriteService.Execute( allItems );

        // we'll call publish a number of times to see if we do NOT get data changes
        // for any of our subscriptions.
        publish = new Publish( g_session );
        addLog( "\nPublish to be called now a maximum of " + publishCount + " times.... NO DATACHANGES EXPECTED!" );
        for( var s=0; s<publishCount; s++ )
        {
            publish.Execute( true );
        }//for s...
    }//setPublishing

    // now check which subscriptions provided dataChanges
    AssertSubscriptionCallbacks( subscriptions, publish );

    // delete all subscriptions added above
    for( i=0; i<subscriptions.length; i++ )
    {
        deleteMonitoredItems( allItems[i], subscriptions[i], g_session );
        deleteSubscription( subscriptions[i], g_session );
    }
    // clear the publish object's properties...
    publish.Clear();
}

safelyInvoke( setPublishingMode5103007 );