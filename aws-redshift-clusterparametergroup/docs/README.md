# AWS::Redshift::ClusterParameterGroup

Resource Type definition for AWS::Redshift::ClusterParameterGroup

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "Type" : "AWS::Redshift::ClusterParameterGroup",
    "Properties" : {
        "<a href="#parametergroupname" title="ParameterGroupName">ParameterGroupName</a>" : <i>String</i>,
        "<a href="#description" title="Description">Description</a>" : <i>String</i>,
        "<a href="#parametergroupfamily" title="ParameterGroupFamily">ParameterGroupFamily</a>" : <i>String</i>,
        "<a href="#parameters" title="Parameters">Parameters</a>" : <i>[ <a href="parameter.md">Parameter</a>, ... ]</i>,
        "<a href="#tags" title="Tags">Tags</a>" : <i>[ <a href="tag.md">Tag</a>, ... ]</i>
    }
}
</pre>

### YAML

<pre>
Type: AWS::Redshift::ClusterParameterGroup
Properties:
    <a href="#parametergroupname" title="ParameterGroupName">ParameterGroupName</a>: <i>String</i>
    <a href="#description" title="Description">Description</a>: <i>String</i>
    <a href="#parametergroupfamily" title="ParameterGroupFamily">ParameterGroupFamily</a>: <i>String</i>
    <a href="#parameters" title="Parameters">Parameters</a>: <i>
      - <a href="parameter.md">Parameter</a></i>
    <a href="#tags" title="Tags">Tags</a>: <i>
      - <a href="tag.md">Tag</a></i>
</pre>

## Properties

#### ParameterGroupName

The name of the cluster parameter group.

_Required_: No

_Type_: String

_Maximum Length_: <code>255</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### Description

A description of the parameter group.

_Required_: Yes

_Type_: String

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### ParameterGroupFamily

The Amazon Redshift engine version to which the cluster parameter group applies. The cluster engine version determines the set of parameters.

_Required_: Yes

_Type_: String

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### Parameters

An array of parameters to be modified. A maximum of 20 parameters can be modified in a single request.

_Required_: No

_Type_: List of <a href="parameter.md">Parameter</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Tags

An array of key-value pairs to apply to this resource.

_Required_: No

_Type_: List of <a href="tag.md">Tag</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

## Return Values

### Ref

When you pass the logical ID of this resource to the intrinsic `Ref` function, Ref returns the ParameterGroupName.
