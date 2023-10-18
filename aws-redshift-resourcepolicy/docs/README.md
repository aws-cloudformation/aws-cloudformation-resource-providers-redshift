# AWS::Redshift::ResourcePolicy

The `AWS::Redshift::ResourcePolicy` resource creates an Amazon Redshift Resource Policy

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "Type" : "AWS::Redshift::ResourcePolicy",
    "Properties" : {
        "<a href="#resourcearn" title="ResourceArn">ResourceArn</a>" : <i>String</i>,
        "<a href="#policy" title="Policy">Policy</a>" : <i>String</i>
    }
}
</pre>

### YAML

<pre>
Type: AWS::Redshift::ResourcePolicy
Properties:
    <a href="#resourcearn" title="ResourceArn">ResourceArn</a>: <i>String</i>
    <a href="#policy" title="Policy">Policy</a>: <i>String</i>
</pre>

## Properties

#### ResourceArn

The Amazon Resource Name (ARN) of the resource that a policy is attached to.

_Required_: Yes

_Type_: String

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### Policy

The content of a resource policy.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

## Return Values

### Ref

When you pass the logical ID of this resource to the intrinsic `Ref` function, Ref returns the ResourceArn.
