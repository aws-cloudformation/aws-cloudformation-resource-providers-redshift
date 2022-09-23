# AWS::Redshift::EndpointAccess VpcEndpoint

The connection endpoint for connecting to an Amazon Redshift cluster through the proxy.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#networkinterfaces" title="NetworkInterfaces">NetworkInterfaces</a>" : <i>[ <a href="networkinterface.md">NetworkInterface</a>, ... ]</i>
}
</pre>

### YAML

<pre>
<a href="#networkinterfaces" title="NetworkInterfaces">NetworkInterfaces</a>: <i>
      - <a href="networkinterface.md">NetworkInterface</a></i>
</pre>

## Properties

#### NetworkInterfaces

One or more network interfaces of the endpoint. Also known as an interface endpoint.

_Required_: No

_Type_: List of <a href="networkinterface.md">NetworkInterface</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)
