/*  RESOURCE TESTING;
    prepared by Nathan Pocock; nathan.pocock@opcfoundation.org

    Description:
        Issues FindServers call (default parameters).

    Revision History
        04-Jan-2010 NP: Initial version.
*/

include( "./library/ResourceTesting/repetitiveCall.js" );
include( "./library/ServiceBased/DiscoveryServiceSet/FindServers/findServers.js" );

const SUPPRESS_MESSAGING = true;

// this is the function that will be called repetitvely
function findServers()
{
    if( !FindServersHelper.Execute( endpoint, undefined, undefined, undefined, SUPPRESS_MESSAGING ) )
    {
        addError( "Could not read the initial values of the Scalar nodes we want to test." );
    }
}

function initialize()
{
    // create our helper object
    FindServersHelper = new FindServers( g_session );
}

function connectOverride()
{
    g_channel = new UaChannel();
    g_session = new UaDiscovery( g_channel );
    return( connectChannel( g_channel, endpoint ) ); //endpoint: configured below
}

function disconnectOverride()
{
    disconnectChannel( g_channel );
}

// Create a FINDSERVERS service call helper
var FindServersHelper;

// Get the test control parameters from the settings
var loopCount = parseInt( readSetting( "/Advanced/ResourceTesting/DiscoveryServicesCallCount" ).toString() );
var endpoint = readSetting( "/Server Test/Discovery URL" ).toString();

// Perform the iterative call loop
g_session = new UaDiscovery( g_channel )
repetitivelyInvoke( initialize, findServers, loopCount, connectOverride, disconnectOverride, undefined, "Find Servers" );

// clean-up
FindServersHelper = null;