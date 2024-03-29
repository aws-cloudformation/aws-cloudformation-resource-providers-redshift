# AWS::Redshift::ClusterParameterGroup Parameter

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#parametername" title="ParameterName">ParameterName</a>" : <i>String</i>,
    "<a href="#parametervalue" title="ParameterValue">ParameterValue</a>" : <i>String</i>
}
</pre>

### YAML

<pre>
<a href="#parametername" title="ParameterName">ParameterName</a>: <i>String</i>
<a href="#parametervalue" title="ParameterValue">ParameterValue</a>: <i>String</i>
</pre>

## Properties

#### ParameterName

The name of the parameter.

_Required_: Yes

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ParameterValue

The value of the parameter. If `ParameterName` is `wlm_json_configuration`, then the maximum size of `ParameterValue` is 8000 characters.

_Required_: Yes

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)
