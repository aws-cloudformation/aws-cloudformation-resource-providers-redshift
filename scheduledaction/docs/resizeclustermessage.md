# AWS::Redshift::ScheduledAction ResizeClusterMessage

Describes a pause cluster operation. For example, a scheduled action to run the `ResizeCluster` API operation.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#clusteridentifier" title="ClusterIdentifier">ClusterIdentifier</a>" : <i>String</i>,
    "<a href="#clustertype" title="ClusterType">ClusterType</a>" : <i>String</i>,
    "<a href="#nodetype" title="NodeType">NodeType</a>" : <i>String</i>,
    "<a href="#numberofnodes" title="NumberOfNodes">NumberOfNodes</a>" : <i>Integer</i>,
    "<a href="#classic" title="Classic">Classic</a>" : <i>Boolean</i>
}
</pre>

### YAML

<pre>
<a href="#clusteridentifier" title="ClusterIdentifier">ClusterIdentifier</a>: <i>String</i>
<a href="#clustertype" title="ClusterType">ClusterType</a>: <i>String</i>
<a href="#nodetype" title="NodeType">NodeType</a>: <i>String</i>
<a href="#numberofnodes" title="NumberOfNodes">NumberOfNodes</a>: <i>Integer</i>
<a href="#classic" title="Classic">Classic</a>: <i>Boolean</i>
</pre>

## Properties

#### ClusterIdentifier

_Required_: Yes

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ClusterType

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### NodeType

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### NumberOfNodes

_Required_: No

_Type_: Integer

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Classic

_Required_: No

_Type_: Boolean

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)
