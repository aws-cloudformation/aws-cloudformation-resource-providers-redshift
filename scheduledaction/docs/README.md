# AWS::Redshift::ScheduledAction

The `AWS::Redshift::ScheduledAction` resource creates an Amazon Redshift Scheduled Action.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "Type" : "AWS::Redshift::ScheduledAction",
    "Properties" : {
        "<a href="#scheduledactionname" title="ScheduledActionName">ScheduledActionName</a>" : <i>String</i>,
        "<a href="#targetaction" title="TargetAction">TargetAction</a>" : <i><a href="scheduledactiontype.md">ScheduledActionType</a></i>,
        "<a href="#schedule" title="Schedule">Schedule</a>" : <i>String</i>,
        "<a href="#iamrole" title="IamRole">IamRole</a>" : <i>String</i>,
        "<a href="#scheduledactiondescription" title="ScheduledActionDescription">ScheduledActionDescription</a>" : <i>String</i>,
        "<a href="#starttime" title="StartTime">StartTime</a>" : <i>String</i>,
        "<a href="#endtime" title="EndTime">EndTime</a>" : <i>String</i>,
        "<a href="#enable" title="Enable">Enable</a>" : <i>Boolean</i>,
    }
}
</pre>

### YAML

<pre>
Type: AWS::Redshift::ScheduledAction
Properties:
    <a href="#scheduledactionname" title="ScheduledActionName">ScheduledActionName</a>: <i>String</i>
    <a href="#targetaction" title="TargetAction">TargetAction</a>: <i><a href="scheduledactiontype.md">ScheduledActionType</a></i>
    <a href="#schedule" title="Schedule">Schedule</a>: <i>String</i>
    <a href="#iamrole" title="IamRole">IamRole</a>: <i>String</i>
    <a href="#scheduledactiondescription" title="ScheduledActionDescription">ScheduledActionDescription</a>: <i>String</i>
    <a href="#starttime" title="StartTime">StartTime</a>: <i>String</i>
    <a href="#endtime" title="EndTime">EndTime</a>: <i>String</i>
    <a href="#enable" title="Enable">Enable</a>: <i>Boolean</i>
</pre>

## Properties

#### ScheduledActionName

The name of the scheduled action. The name must be unique within an account.

_Required_: Yes

_Type_: String

_Pattern_: <code>^(?=^[a-z][a-z0-9]*(-[a-z0-9]+)*$).{1,60}$</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### TargetAction

_Required_: No

_Type_: <a href="scheduledactiontype.md">ScheduledActionType</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Schedule

The schedule in `at( )` or `cron( )` format.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### IamRole

The IAM role to assume to run the target action.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ScheduledActionDescription

The description of the scheduled action.

_Required_: No

_Type_: String

_Pattern_: <code>^(?=^[\x09\x0a\x0d\x20-\xff]*$).{1,255}$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### StartTime

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### EndTime

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Enable

If true, the schedule is enabled. If false, the scheduled action does not trigger.

_Required_: No

_Type_: Boolean

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

## Return Values

### Ref

When you pass the logical ID of this resource to the intrinsic `Ref` function, Ref returns the ScheduledActionName.

### Fn::GetAtt

The `Fn::GetAtt` intrinsic function returns a value for a specified attribute of this type. The following are the available attributes and sample return values.

For more information about using the `Fn::GetAtt` intrinsic function, see [Fn::GetAtt](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-getatt.html).

#### State

The state of the scheduled action.

#### NextInvocations

List of times when the scheduled action will run.
