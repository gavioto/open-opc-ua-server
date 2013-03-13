// Test assistance
include( "./library/ServiceBased/SubscriptionServiceSet/SetPublishingMode/testParallelSubscriptions.js" );
// Objects
include( "./library/Base/Objects/expectedResults.js" );
include( "./library/Base/Objects/subscription.js" );
include( "./library/Base/Objects/monitoredItem.js" );
include( "./library/Base/Objects/integerSet.js" );
include( "./library/Base/assertions.js" );
include( "./library/Base/array.js" );
// utility functions
include( "./library/Base/connect.js" );
include( "./library/Base/check_timestamp.js" );
include( "./library/Base/safeInvoke.js" );
include( "./library/Base/SettingsUtilities/validate_setting.js" );
include( "./library/Base/SettingsUtilities/NodeIds.js" );
// include all library scripts specific to monitored items tests
// CreateSubscription
include( "./library/ServiceBased/SubscriptionServiceSet/CreateSubscription/createSubscription.js" );
include( "./library/ServiceBased/SubscriptionServiceSet/CreateSubscription/multiSessionMultiSubscribeTest.js" );
// ModifySubscription
include( "./library/ServiceBased/SubscriptionServiceSet/ModifySubscription/modifySubscription.js" );
// SetPublishingMode
include( "./library/ServiceBased/SubscriptionServiceSet/SetPublishingMode/setPublishingMode.js" );
// Publish
include( "./library/ServiceBased/SubscriptionServiceSet/Publish/publish.js" );
// Republish
include( "./library/ServiceBased/SubscriptionServiceSet/Republish/check_republish_valid.js" );
include( "./library/ServiceBased/SubscriptionServiceSet/Republish/check_republish_failed.js" );
// DeleteSubscription
include( "./library/ServiceBased/SubscriptionServiceSet/DeleteSubscription/deleteSubscription.js" );
// CreateMonitoredItems
include( "./library/ServiceBased/MonitoredItemServiceSet/CreateMonitoredItems/createMonitoredItems.js" );
// DeleteMonitoredItems
include( "./library/ServiceBased/MonitoredItemServiceSet/DeleteMonitoredItems/deleteMonitoredItems.js" );
// write
include( "./library/ServiceBased/AttributeServiceSet/Write/write.js" );
include( "./library/ServiceBased/AttributeServiceSet/Write/write_attribute.js" );

// setup a default monitoredItem that we can use for the scripts within this CU.
var monitoredItem;
function InitSubscriptionMin5()
{
    var monitoredItemSetting = NodeIdSettings.GetAScalarNodeIdSetting( NodeIdSettings.ScalarStatic(), "dui" ).name;
    monitoredItem = MonitoredItem.fromSetting( monitoredItemSetting, 0 );
    if( monitoredItem === undefined || monitoredItem === null )
    {
        addError( "Conformance unit skipped. Please check setting '" + monitoredItemSetting + "'" );
        stopCurrentUnit();
    }
}
InitSubscriptionMin5();

var g_channel = new UaChannel();
connectChannel( g_channel );

// Create a session
var g_session = new UaSession( g_channel );
g_session.DefaultTimeoutHint = parseInt( readSetting( "/Ua Settings/Session/DefaultTimeoutHint" ) );
if( ! ( createSession( g_session ) && activateSession( g_session ) ) )
{
    addError( "Session creation and activation failed, aborting testing of this conformance unit." );
    stopCurrentUnit();
}

// a WRITE object to help with node writes!
var writeService = new Write( g_session );
var readService  = new Read ( g_session );

// some helpful constants
const DO_NOT_VERIFY_WRITE = false;
const DO_NOT_ACK_SEQUENCE = true;