# AWS::Redshift::Cluster LoggingProperties

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#logdestinationtype" title="LogDestinationType">LogDestinationType</a>" : <i>String</i>,
    "<a href="#logexports" title="LogExports">LogExports</a>" : <i>[ String, ... ]</i>,
    "<a href="#bucketname" title="BucketName">BucketName</a>" : <i>String</i>,
    "<a href="#s3keyprefix" title="S3KeyPrefix">S3KeyPrefix</a>" : <i>String</i>
}
</pre>

### YAML

<pre>
<a href="#logdestinationtype" title="LogDestinationType">LogDestinationType</a>: <i>String</i>
<a href="#logexports" title="LogExports">LogExports</a>: <i>
      - String</i>
<a href="#bucketname" title="BucketName">BucketName</a>: <i>String</i>
<a href="#s3keyprefix" title="S3KeyPrefix">S3KeyPrefix</a>: <i>String</i>
</pre>

## Properties

#### LogDestinationType

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### LogExports

_Required_: No

_Type_: List of String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### BucketName

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### S3KeyPrefix

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

