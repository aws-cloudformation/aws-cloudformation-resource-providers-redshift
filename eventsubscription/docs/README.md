# AWS::Redshift::EventSubscription

An example resource schema demonstrating some basic constructs and validation rules.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "Type" : "AWS::Redshift::EventSubscription",
    "Properties" : {
        "<a href="#subscriptionname" title="SubscriptionName">SubscriptionName</a>" : <i>String</i>,
        "<a href="#snstopicarn" title="SnsTopicArn">SnsTopicArn</a>" : <i>String</i>,
        "<a href="#sourcetype" title="SourceType">SourceType</a>" : <i>String</i>,
        "<a href="#sourceids" title="SourceIds">SourceIds</a>" : <i>[ String, ... ]</i>,
        "<a href="#eventcategories" title="EventCategories">EventCategories</a>" : <i>[ String, ... ]</i>,
        "<a href="#severity" title="Severity">Severity</a>" : <i>String</i>,
        "<a href="#enabled" title="Enabled">Enabled</a>" : <i>Boolean</i>,
        "<a href="#tags" title="Tags">Tags</a>" : <i>[ <a href="tag.md">Tag</a>, ... ]</i>,
    }
}
</pre>

### YAML

<pre>
Type: AWS::Redshift::EventSubscription
Properties:
    <a href="#subscriptionname" title="SubscriptionName">SubscriptionName</a>: <i>String</i>
    <a href="#snstopicarn" title="SnsTopicArn">SnsTopicArn</a>: <i>String</i>
    <a href="#sourcetype" title="SourceType">SourceType</a>: <i>String</i>
    <a href="#sourceids" title="SourceIds">SourceIds</a>: <i>
      - String</i>
    <a href="#eventcategories" title="EventCategories">EventCategories</a>: <i>
      - String</i>
    <a href="#severity" title="Severity">Severity</a>: <i>String</i>
    <a href="#enabled" title="Enabled">Enabled</a>: <i>Boolean</i>
    <a href="#tags" title="Tags">Tags</a>: <i>
      - <a href="tag.md">Tag</a></i>
</pre>

## Properties

#### SubscriptionName

The name of the Amazon Redshift event notification subscription

_Required_: Yes

_Type_: String

_Pattern_: <code>^(?=^[a-z][a-z0-9]*(-[a-z0-9]+)*$).{1,255}$</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### SnsTopicArn

The Amazon Resource Name (ARN) of the Amazon SNS topic used to transmit the event notifications.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### SourceType

The type of source that will be generating the events.

_Required_: No

_Type_: String

_Allowed Values_: <code>cluster</code> | <code>cluster-parameter-group</code> | <code>cluster-security-group</code> | <code>cluster-snapshot</code> | <code>scheduled-action</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### SourceIds

A list of one or more identifiers of Amazon Redshift source objects.

_Required_: No

_Type_: List of String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### EventCategories

Specifies the Amazon Redshift event categories to be published by the event notification subscription.

_Required_: No

_Type_: List of String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Severity

Specifies the Amazon Redshift event severity to be published by the event notification subscription.

_Required_: No

_Type_: String

_Allowed Values_: <code>ERROR</code> | <code>INFO</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Enabled

A boolean value; set to true to activate the subscription, and set to false to create the subscription but not activate it.

_Required_: No

_Type_: Boolean

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Tags

An array of key-value pairs to apply to this resource.

_Required_: No

_Type_: List of <a href="tag.md">Tag</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

## Return Values

### Ref

When you pass the logical ID of this resource to the intrinsic `Ref` function, Ref returns the SubscriptionName.

### Fn::GetAtt

The `Fn::GetAtt` intrinsic function returns a value for a specified attribute of this type. The following are the available attributes and sample return values.

For more information about using the `Fn::GetAtt` intrinsic function, see [Fn::GetAtt](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-getatt.html).

#### CustomerAwsId

The AWS account associated with the Amazon Redshift event notification subscription.

#### CustSubscriptionId

The name of the Amazon Redshift event notification subscription.

#### Status

The status of the Amazon Redshift event notification subscription.

#### SubscriptionCreationTime

The date and time the Amazon Redshift event notification subscription was created.

#### SourceIdsList

A list of the sources that publish events to the Amazon Redshift event notification subscription.

#### EventCategoriesList

The list of Amazon Redshift event categories specified in the event notification subscription.
