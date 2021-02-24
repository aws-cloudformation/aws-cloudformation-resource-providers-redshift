# AWS::Redshift::EndpointAccess

An example resource schema demonstrating some basic constructs and validation rules.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "Type" : "AWS::Redshift::EndpointAccess",
    "Properties" : {
        "<a href="#resourceowner" title="ResourceOwner">ResourceOwner</a>" : <i>String</i>,
        "<a href="#endpointname" title="EndpointName">EndpointName</a>" : <i>String</i>,
        "<a href="#subnetgroupname" title="SubnetGroupName">SubnetGroupName</a>" : <i>String</i>,
        "<a href="#vpcid" title="VpcId">VpcId</a>" : <i>String</i>,
        "<a href="#vpcsecuritygroupids" title="VpcSecurityGroupIds">VpcSecurityGroupIds</a>" : <i>[ String, ... ]</i>
    }
}
</pre>

### YAML

<pre>
Type: AWS::Redshift::EndpointAccess
Properties:
    <a href="#resourceowner" title="ResourceOwner">ResourceOwner</a>: <i>String</i>
    <a href="#endpointname" title="EndpointName">EndpointName</a>: <i>String</i>
    <a href="#subnetgroupname" title="SubnetGroupName">SubnetGroupName</a>: <i>String</i>
    <a href="#vpcid" title="VpcId">VpcId</a>: <i>String</i>
    <a href="#vpcsecuritygroupids" title="VpcSecurityGroupIds">VpcSecurityGroupIds</a>: <i>
      - String</i>
</pre>

## Properties

#### ResourceOwner

The cluster owner's AWS account ID.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### EndpointName

The endpoint's name.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### SubnetGroupName

The subnet group that contains the endpoint.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### VpcId

The VPC ID representing the VPC the endpoint belongs to.

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

When you pass the logical ID of this resource to the intrinsic `Ref` function, Ref returns the ClusterIdentifier.

### Fn::GetAtt

The `Fn::GetAtt` intrinsic function returns a value for a specified attribute of this type. The following are the available attributes and sample return values.

For more information about using the `Fn::GetAtt` intrinsic function, see [Fn::GetAtt](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-getatt.html).

#### ClusterIdentifier

A unique identifier for the cluster. You use this identifier to refer to the cluster for any subsequent cluster operations such as deleting or modifying. All alphabetical characters must be lower case, no hypens at the end, no two consecutive hyphens. Cluster name should be unique for all clusters within an AWS account

#### Address

The DNS address associated with the EndpointAccess.

#### EndpointStatus

The status of the endpoint. TODO change to enum?

#### EndpointCreateTime

The date and time when the endpoint was created.

#### VpcSecurityGroups

A list of Virtual Private Cloud (VPC) security groups to be associated with the endpoint.

