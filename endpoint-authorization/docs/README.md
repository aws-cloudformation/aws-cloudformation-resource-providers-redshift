# AWS::Redshift::EndpointAuthorization

An example resource schema demonstrating some basic constructs and validation rules.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "Type" : "AWS::Redshift::EndpointAuthorization",
    "Properties" : {
        "<a href="#grantor" title="Grantor">Grantor</a>" : <i>String</i>,
        "<a href="#grantee" title="Grantee">Grantee</a>" : <i>String</i>,
        "<a href="#account" title="Account">Account</a>" : <i>String</i>,
        "<a href="#vpcids" title="VpcIds">VpcIds</a>" : <i>[ String, ... ]</i>,
        "<a href="#asgrantee" title="AsGrantee">AsGrantee</a>" : <i>Boolean</i>,
        "<a href="#revoke" title="Revoke">Revoke</a>" : <i>Boolean</i>,
        "<a href="#force" title="Force">Force</a>" : <i>Boolean</i>,
        "<a href="#authorizationtime" title="AuthorizationTime">AuthorizationTime</a>" : <i>String</i>,
        "<a href="#clusterstatus" title="ClusterStatus">ClusterStatus</a>" : <i>String</i>,
        "<a href="#status" title="Status">Status</a>" : <i>String</i>,
        "<a href="#allowedallvpcs" title="AllowedAllVPCs">AllowedAllVPCs</a>" : <i>Boolean</i>,
        "<a href="#allowedvpcs" title="AllowedVPCs">AllowedVPCs</a>" : <i>[ String, ... ]</i>
    }
}
</pre>

### YAML

<pre>
Type: AWS::Redshift::EndpointAuthorization
Properties:
    <a href="#grantor" title="Grantor">Grantor</a>: <i>String</i>
    <a href="#grantee" title="Grantee">Grantee</a>: <i>String</i>
    <a href="#account" title="Account">Account</a>: <i>String</i>
    <a href="#vpcids" title="VpcIds">VpcIds</a>: <i>
      - String</i>
    <a href="#asgrantee" title="AsGrantee">AsGrantee</a>: <i>Boolean</i>
    <a href="#revoke" title="Revoke">Revoke</a>: <i>Boolean</i>
    <a href="#force" title="Force">Force</a>: <i>Boolean</i>
    <a href="#authorizationtime" title="AuthorizationTime">AuthorizationTime</a>: <i>String</i>
    <a href="#clusterstatus" title="ClusterStatus">ClusterStatus</a>: <i>String</i>
    <a href="#status" title="Status">Status</a>: <i>String</i>
    <a href="#allowedallvpcs" title="AllowedAllVPCs">AllowedAllVPCs</a>: <i>Boolean</i>
    <a href="#allowedvpcs" title="AllowedVPCs">AllowedVPCs</a>: <i>
      - String</i>
</pre>

## Properties

#### Grantor

The account ID of the grantor.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Grantee

The account ID of the grantee.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Account

The account that is being authorized or revoked.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### VpcIds

The list of VPC ids that are being authorized or revoked.

_Required_: No

_Type_: List of String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### AsGrantee

Whether to call the API as a grantor or grantee

_Required_: No

_Type_: Boolean

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Revoke

Whether to revoke access by the specified resources or not.

_Required_: No

_Type_: Boolean

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Force

Whether to force revoke access to a cluster.

_Required_: No

_Type_: Boolean

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### AuthorizationTime

The time when the authorization was last given.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ClusterStatus

The cluster status.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Status

The authorization status. TODO create enum

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### AllowedAllVPCs

Whether all VPCs from the grantee are allowed to connect to the cluster

_Required_: No

_Type_: Boolean

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### AllowedVPCs

The VPCs that are allowed to connect to the cluster

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

Returns the <code>Address</code> value.

#### EndpointStatus

Returns the <code>EndpointStatus</code> value.

#### EndpointCreateTime

Returns the <code>EndpointCreateTime</code> value.

#### VpcSecurityGroups

Returns the <code>VpcSecurityGroups</code> value.

