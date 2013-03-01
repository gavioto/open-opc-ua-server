/*  Test 6.6 Error Test 8; prepared by Nathan Pocock nathan.pocock@opcfoundation.org

    Description:
        Write a value of FALSE (as a string).
    Expectations:
        ServiceResult=Good. Results[0]=Bad_TypeMismatch.

    Revision History:
        18-Feb-2010 NP: Initial version.

     FOR MORE INFORMATION ABOUT THIS AND OTHER TEST CASES, PLEASE REVIEW:
         Test Lab Specifications Part 8 - UA Server Section 5.8.1.
*/

function write66Err008()
{
    if( twoStateItems == null || twoStateItems.length == 0 )
    {
        addSkipped( TSDT );
        return;
    }
    WriteValue( twoStateItems[0], "FALSE", BuiltInType.String, WriteHelper )
}

safelyInvoke( write66Err008 );