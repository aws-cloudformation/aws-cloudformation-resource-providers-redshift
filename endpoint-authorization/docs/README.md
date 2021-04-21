# AWS::Redshift::EndpointAuthorization

Describes an endpoint authorization for authorizing Redshift-managed VPC endpoint access to a cluster across AWS accounts.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "Type" : "AWS::Redshift::EndpointAuthorization",
    "Properties" : {
        "<a href="#account" title="Account">Account</a>" : <i>String</i>,
        "<a href="#vpcids" title="VpcIds">VpcIds</a>" : <i>[ String, ... ]</i>,
        "<a href="#asgrantee" title="AsGrantee">AsGrantee</a>" : <i>Boolean</i>,
        "<a href="#force" title="Force">Force</a>" : <i>Boolean</i>,
        "<a href="#clusteridentifier" title="ClusterIdentifier">ClusterIdentifier</a>" : <i>String</i>,
        "<a href="#allowedvpcs" title="AllowedVPCs">AllowedVPCs</a>" : <i>[ String, ... ]</i>,
    }
}
</pre>

### YAML

<pre>
Type: AWS::Redshift::EndpointAuthorization
Properties:
    <a href="#account" title="Account">Account</a>: <i>String</i>
    <a href="#vpcids" title="VpcIds">VpcIds</a>: <i>
      - String</i>
    <a href="#asgrantee" title="AsGrantee">AsGrantee</a>: <i>Boolean</i>
    <a href="#force" title="Force">Force</a>: <i>Boolean</i>
    <a href="#clusteridentifier" title="ClusterIdentifier">ClusterIdentifier</a>: <i>String</i>
    <a href="#allowedvpcs" title="AllowedVPCs">AllowedVPCs</a>: <i>
      - String</i>
</pre>

## Properties

#### Account

The target AWS account ID to grant or revoke access for.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### VpcIds

The virtual private cloud (VPC) identifiers to grant or revoke access to.

_Required_: No

_Type_: List of String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### AsGrantee

Indicates whether to check authorization from a grantor or grantee point of view. If true, Amazon Redshift returns endpoint authorizations that you've been granted. If false (default), checks authorization from a grantor point of view.

_Required_: No

_Type_: Boolean

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Force

 Indicates whether to force the revoke action. If true, the Redshift-managed VPC endpoints associated with the endpoint authorization are also deleted.

_Required_: No

_Type_: Boolean

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ClusterIdentifier

The cluster identifier.

_Required_: Yes

_Type_: String

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### AllowedVPCs

The VPCs allowed access to the cluster.

_Required_: No

_Type_: List of String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

## Return Values

### Fn::GetAtt

The `Fn::GetAtt` intrinsic function returns a value for a specified attribute of this type. The following are the available attributes and sample return values.

For more information about using the `Fn::GetAtt` intrinsic function, see [Fn::GetAtt](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-getatt.html).

#### AllowedAllVPCs

Indicates whether all VPCs in the grantee account are allowed access to the cluster.

#### Status

The status of the authorization action.

#### ClusterStatus

The status of the cluster.

#### AuthorizeTime

Returns the <code>AuthorizeTime</code> value.

#### EndpointCount

The number of Redshift-managed VPC endpoints created for the authorization.

#### Grantor

The AWS account ID of the cluster owner.

#### Grantee

The AWS account ID of the grantee of the cluster.

