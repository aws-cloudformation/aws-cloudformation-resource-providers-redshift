# AWS::Redshift::EndpointAuthorization

Describes an endpoint authorization for authorizing Redshift-managed VPC endpoint access to a cluster across AWS accounts.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "Type" : "AWS::Redshift::EndpointAuthorization",
    "Properties" : {
        "<a href="#clusteridentifier" title="ClusterIdentifier">ClusterIdentifier</a>" : <i>String</i>,
        "<a href="#account" title="Account">Account</a>" : <i>String</i>,
        "<a href="#vpcids" title="VpcIds">VpcIds</a>" : <i>[ String, ... ]</i>,
        "<a href="#force" title="Force">Force</a>" : <i>Boolean</i>
    }
}
</pre>

### YAML

<pre>
Type: AWS::Redshift::EndpointAuthorization
Properties:
    <a href="#clusteridentifier" title="ClusterIdentifier">ClusterIdentifier</a>: <i>String</i>
    <a href="#account" title="Account">Account</a>: <i>String</i>
    <a href="#vpcids" title="VpcIds">VpcIds</a>: <i>
      - String</i>
    <a href="#force" title="Force">Force</a>: <i>Boolean</i>
</pre>

## Properties

#### ClusterIdentifier

The cluster identifier.

_Required_: Yes

_Type_: String

_Pattern_: <code>^(?=^[a-z][a-z0-9]*(-[a-z0-9]+)*$).{1,63}$</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### Account

_Required_: Yes

_Type_: String

_Pattern_: <code>^\d{12}$</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### VpcIds

The virtual private cloud (VPC) identifiers to grant or revoke access to.

_Required_: No

_Type_: List of String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Force

Indicates whether to force the revoke action for the next Delete or Update stack action. If true, the Redshift-managed VPC endpoints associated with the endpoint authorization are also deleted.

_Required_: No

_Type_: Boolean

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

## Return Values

### Fn::GetAtt

The `Fn::GetAtt` intrinsic function returns a value for a specified attribute of this type. The following are the available attributes and sample return values.

For more information about using the `Fn::GetAtt` intrinsic function, see [Fn::GetAtt](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-getatt.html).

#### Grantor

Returns the <code>Grantor</code> value.

#### Grantee

Returns the <code>Grantee</code> value.

#### AuthorizeTime

The time (UTC) when the authorization was created.

#### ClusterStatus

The status of the cluster.

#### Status

The status of the authorization action.

#### AllowedAllVPCs

Indicates whether all VPCs in the grantee account are allowed access to the cluster.

#### AllowedVPCs

The VPCs allowed access to the cluster.

#### EndpointCount

The number of Redshift-managed VPC endpoints created for the authorization.
