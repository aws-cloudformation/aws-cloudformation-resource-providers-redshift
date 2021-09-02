# AWS::Redshift::ScheduledAction ScheduledActionType

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#resizecluster" title="ResizeCluster">ResizeCluster</a>" : <i><a href="resizeclustermessage.md">ResizeClusterMessage</a></i>,
    "<a href="#pausecluster" title="PauseCluster">PauseCluster</a>" : <i><a href="pauseclustermessage.md">PauseClusterMessage</a></i>,
    "<a href="#resumecluster" title="ResumeCluster">ResumeCluster</a>" : <i><a href="resumeclustermessage.md">ResumeClusterMessage</a></i>
}
</pre>

### YAML

<pre>
<a href="#resizecluster" title="ResizeCluster">ResizeCluster</a>: <i><a href="resizeclustermessage.md">ResizeClusterMessage</a></i>
<a href="#pausecluster" title="PauseCluster">PauseCluster</a>: <i><a href="pauseclustermessage.md">PauseClusterMessage</a></i>
<a href="#resumecluster" title="ResumeCluster">ResumeCluster</a>: <i><a href="resumeclustermessage.md">ResumeClusterMessage</a></i>
</pre>

## Properties

#### ResizeCluster

Describes a pause cluster operation. For example, a scheduled action to run the `ResizeCluster` API operation.

_Required_: No

_Type_: <a href="resizeclustermessage.md">ResizeClusterMessage</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### PauseCluster

Describes a resize cluster operation. For example, a scheduled action to run the `PauseCluster` API operation.

_Required_: No

_Type_: <a href="pauseclustermessage.md">PauseClusterMessage</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ResumeCluster

Describes a resume cluster operation. For example, a scheduled action to run the `ResumeCluster` API operation.

_Required_: No

_Type_: <a href="resumeclustermessage.md">ResumeClusterMessage</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)
