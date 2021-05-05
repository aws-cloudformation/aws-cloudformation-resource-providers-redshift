# AWS::Redshift::EndpointAccess

Resource schema for a Redshift-managed VPC endpoint.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "Type" : "AWS::Redshift::EndpointAccess",
    "Properties" : {
        "<a href="#clusteridentifier" title="ClusterIdentifier">ClusterIdentifier</a>" : <i>String</i>,
        "<a href="#resourceowner" title="ResourceOwner">ResourceOwner</a>" : <i>String</i>,
        "<a href="#endpointname" title="EndpointName">EndpointName</a>" : <i>String</i>,
        "<a href="#subnetgroupname" title="SubnetGroupName">SubnetGroupName</a>" : <i>String</i>,
        "<a href="#vpcid" title="VpcId">VpcId</a>" : <i>String</i>,
        "<a href="#vpcsecuritygroupids" title="VpcSecurityGroupIds">VpcSecurityGroupIds</a>" : <i>[ String, ... ]</i>,
    }
}
</pre>

### YAML

<pre>
Type: AWS::Redshift::EndpointAccess
Properties:
    <a href="#clusteridentifier" title="ClusterIdentifier">ClusterIdentifier</a>: <i>String</i>
    <a href="#resourceowner" title="ResourceOwner">ResourceOwner</a>: <i>String</i>
    <a href="#endpointname" title="EndpointName">EndpointName</a>: <i>String</i>
    <a href="#subnetgroupname" title="SubnetGroupName">SubnetGroupName</a>: <i>String</i>
    <a href="#vpcid" title="VpcId">VpcId</a>: <i>String</i>
    <a href="#vpcsecuritygroupids" title="VpcSecurityGroupIds">VpcSecurityGroupIds</a>: <i>
      - String</i>
</pre>

## Properties

#### ClusterIdentifier

A unique identifier for the cluster. You use this identifier to refer to the cluster for any subsequent cluster operations such as deleting or modifying. All alphabetical characters must be lower case, no hypens at the end, no two consecutive hyphens. Cluster name should be unique for all clusters within an AWS account

_Required_: No

_Type_: String

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### ResourceOwner

The AWS account ID of the owner of the cluster.

_Required_: No

_Type_: String

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### EndpointName

The name of the endpoint.

_Required_: Yes

_Type_: String

_Pattern_: <code>[a-z]([a-z0-9]*(-[a-z0-9]+)){0,29}</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### SubnetGroupName

The subnet group name where Amazon Redshift chooses to deploy the endpoint.

_Required_: No

_Type_: String

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### VpcId

The VPC identifier that the endpoint is associated.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### VpcSecurityGroupIds

A list of vpc security group ids to apply to the created endpoint access.

_Required_: No

_Type_: List of String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

## Return Values

### Ref

When you pass the logical ID of this resource to the intrinsic `Ref` function, Ref returns the EndpointName.

### Fn::GetAtt

The `Fn::GetAtt` intrinsic function returns a value for a specified attribute of this type. The following are the available attributes and sample return values.

For more information about using the `Fn::GetAtt` intrinsic function, see [Fn::GetAtt](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-getatt.html).

#### Address

The DNS address of the endpoint.

#### EndpointStatus

The status of the endpoint.

#### EndpointCreateTime

The time (UTC) that the endpoint was created.

#### Port

The port number on which the cluster accepts incoming connections.

#### VpcSecurityGroups

A list of Virtual Private Cloud (VPC) security groups to be associated with the endpoint.

#### VpcEndpoint

The connection endpoint for connecting to an Amazon Redshift cluster through the proxy.

