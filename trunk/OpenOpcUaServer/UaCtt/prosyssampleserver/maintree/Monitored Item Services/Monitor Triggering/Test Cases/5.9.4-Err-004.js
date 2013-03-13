/*    Test 5.9.4 Error Test 4 prepared by Anand Taparia; ataparia@kepware.com
      Description:
          Script specifies some valid linksToAdd and some invalid linksToAdd.

      Revision History:
        Sep-26-2009 AT: Initial version.
        Nov-25-2009 NP: REVIEWED/INCONCLUSIVE. Server doesn't support Triggering!
        Jan-19-2010 DP: Changed NodeId settings to be from Scalar Set 1.
*/

function setTriggering594Err004()
{
    var items = MonitoredItem.fromSettings( NodeIdSettings.ScalarStatic() );
    if( items == null || items.length < 5 )
    {
        addSkipped( "Static Scalar" );
        return;
    }

    if( false ) //if( !MonitorTriggeringSubscription.SubscriptionCreated )
    {
        addError( "Subscription for conformance unit Monitor Triggering was not created." );
    }
    else
    {
        // Add 5 monitored items using default parameters
        var createMonitoredItemsRequest = new UaCreateMonitoredItemsRequest();
        var createMonitoredItemsResponse = new UaCreateMonitoredItemsResponse();
        g_session.buildRequestHeader( createMonitoredItemsRequest.RequestHeader );

        createMonitoredItemsRequest.SubscriptionId = MonitorTriggeringSubscription.SubscriptionId;
        createMonitoredItemsRequest.TimestampsToReturn = TimestampsToReturn.Both;

        var clientHandle = 0;
        var numItemsToMonitor = 5;
        for( var i = 0; i< numItemsToMonitor; i++ )
        {
            createMonitoredItemsRequest.ItemsToCreate[i] = new UaMonitoredItemCreateRequest();
            createMonitoredItemsRequest.ItemsToCreate[i].ItemToMonitor.AttributeId = Attribute.Value;
            createMonitoredItemsRequest.ItemsToCreate[i].RequestedParameters.ClientHandle = clientHandle++;
            createMonitoredItemsRequest.ItemsToCreate[i].RequestedParameters.SamplingInterval = SAMPLING_RATE_FASTEST;
            createMonitoredItemsRequest.ItemsToCreate[i].RequestedParameters.QueueSize = 1;
            createMonitoredItemsRequest.ItemsToCreate[i].RequestedParameters.DiscardOldest = true;

            switch(i)
            {
                // triggering item
                case 0:
                    createMonitoredItemsRequest.ItemsToCreate[i].ItemToMonitor.NodeId = items[0].NodeId;
                    createMonitoredItemsRequest.ItemsToCreate[i].MonitoringMode = MonitoringMode.Reporting;
                    break;
                // item to link
                case 1:
                    createMonitoredItemsRequest.ItemsToCreate[i].ItemToMonitor.NodeId = items[1].NodeId;
                    createMonitoredItemsRequest.ItemsToCreate[i].MonitoringMode = MonitoringMode.Sampling;
                    break;
                // item to link
                case 2:
                    createMonitoredItemsRequest.ItemsToCreate[i].ItemToMonitor.NodeId = items[2].NodeId;
                    createMonitoredItemsRequest.ItemsToCreate[i].MonitoringMode = MonitoringMode.Sampling;
                    break;
                case 3:
                    createMonitoredItemsRequest.ItemsToCreate[i].ItemToMonitor.NodeId = items[3].NodeId;
                    createMonitoredItemsRequest.ItemsToCreate[i].MonitoringMode = MonitoringMode.Sampling;
                    break; 
                case 4:
                    createMonitoredItemsRequest.ItemsToCreate[i].ItemToMonitor.NodeId = items[4].NodeId;
                    createMonitoredItemsRequest.ItemsToCreate[i].MonitoringMode = MonitoringMode.Sampling;
                    break;
                default:
                    break;
            }
        }

        var uaStatus = g_session.createMonitoredItems( createMonitoredItemsRequest, createMonitoredItemsResponse );
        if ( uaStatus.isGood() )
        {
            checkCreateMonitoredItemsValidParameter( createMonitoredItemsRequest, createMonitoredItemsResponse );

            // set triggering for one valid trigger and one valid link
            var setTriggeringRequest = new UaSetTriggeringRequest();
            var setTriggeringResponse = new UaSetTriggeringResponse();
            g_session.buildRequestHeader( setTriggeringRequest.RequestHeader );

            setTriggeringRequest.TriggeringItemId = createMonitoredItemsResponse.Results[0].MonitoredItemId;
            setTriggeringRequest.SubscriptionId = MonitorTriggeringSubscription.SubscriptionId;

            // Let's make the indices 1,2 invalid
            setTriggeringRequest.LinksToAdd[0] = createMonitoredItemsResponse.Results[1].MonitoredItemId;
            setTriggeringRequest.LinksToAdd[1] = createMonitoredItemsResponse.Results[2].MonitoredItemId + 0x1234;
            setTriggeringRequest.LinksToAdd[2] = createMonitoredItemsResponse.Results[3].MonitoredItemId + 0x1234;
            setTriggeringRequest.LinksToAdd[3] = createMonitoredItemsResponse.Results[4].MonitoredItemId;

            uaStatus = g_session.setTriggering( setTriggeringRequest, setTriggeringResponse );

            if( uaStatus.isGood() )
            {
                // Indices 1,2 should fail with BadMonitoredItemIdInvalid
                var ExpectedOperationResultsAdd = new Array( 4 );
                ExpectedOperationResultsAdd [0] = new ExpectedAndAcceptedResults( StatusCode.Good );
                ExpectedOperationResultsAdd [1] = new ExpectedAndAcceptedResults( StatusCode.BadMonitoredItemIdInvalid );
                ExpectedOperationResultsAdd [2] = new ExpectedAndAcceptedResults( StatusCode.BadMonitoredItemIdInvalid );
                ExpectedOperationResultsAdd [3] = new ExpectedAndAcceptedResults( StatusCode.Good );
                
                var ExpectedOperationResultsRemove = new Array(0);            
                checkSetTriggeringError( setTriggeringRequest, setTriggeringResponse, ExpectedOperationResultsAdd, ExpectedOperationResultsRemove );
            }
            else
            {
                addError( "SetTriggering() status " + uaStatus, uaStatus );
            }

            // delete the items we added in this test
            var monitoredItemsIdsToDelete = new UaUInt32s();
            for( var i = 0; i< createMonitoredItemsResponse.Results.length; i++ )
            {
                monitoredItemsIdsToDelete[i] = createMonitoredItemsResponse.Results[i].MonitoredItemId;
            }        
            deleteMonitoredItems( monitoredItemsIdsToDelete, MonitorTriggeringSubscription, g_session );
        }
        else
        {
            addError( "CreateMonitoredItems() status " + uaStatus, uaStatus );
        }
    }
}

safelyInvoke( setTriggering594Err004 );