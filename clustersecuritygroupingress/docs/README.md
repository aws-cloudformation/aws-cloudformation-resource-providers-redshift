# AWS::Redshift::ClusterSecurityGroupIngress

Resource Type definition for AWS::Redshift::ClusterSecurityGroupIngress

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "Type" : "AWS::Redshift::ClusterSecurityGroupIngress",
    "Properties" : {
        "<a href="#cidrip" title="CIDRIP">CIDRIP</a>" : <i>String</i>,
        "<a href="#clustersecuritygroupname" title="ClusterSecurityGroupName">ClusterSecurityGroupName</a>" : <i>String</i>,
        "<a href="#ec2securitygroupname" title="EC2SecurityGroupName">EC2SecurityGroupName</a>" : <i>String</i>,
        "<a href="#ec2securitygroupownerid" title="EC2SecurityGroupOwnerId">EC2SecurityGroupOwnerId</a>" : <i>String</i>
    }
}
</pre>

### YAML

<pre>
Type: AWS::Redshift::ClusterSecurityGroupIngress
Properties:
    <a href="#cidrip" title="CIDRIP">CIDRIP</a>: <i>String</i>
    <a href="#clustersecuritygroupname" title="ClusterSecurityGroupName">ClusterSecurityGroupName</a>: <i>String</i>
    <a href="#ec2securitygroupname" title="EC2SecurityGroupName">EC2SecurityGroupName</a>: <i>String</i>
    <a href="#ec2securitygroupownerid" title="EC2SecurityGroupOwnerId">EC2SecurityGroupOwnerId</a>: <i>String</i>
</pre>

## Properties

#### CIDRIP

_Required_: No

_Type_: String

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### ClusterSecurityGroupName

_Required_: Yes

_Type_: String

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### EC2SecurityGroupName

_Required_: No

_Type_: String

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### EC2SecurityGroupOwnerId

_Required_: No

_Type_: String

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

## Return Values

### Ref

When you pass the logical ID of this resource to the intrinsic `Ref` function, Ref returns the Id.

### Fn::GetAtt

The `Fn::GetAtt` intrinsic function returns a value for a specified attribute of this type. The following are the available attributes and sample return values.

For more information about using the `Fn::GetAtt` intrinsic function, see [Fn::GetAtt](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-getatt.html).

#### Id

Returns the <code>Id</code> value.
