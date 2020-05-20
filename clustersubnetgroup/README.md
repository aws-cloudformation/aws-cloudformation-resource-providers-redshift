# AWS::Redshift::ClusterSubnetGroup
##Background
This is new uluru resource is planned to replace the native resource in CloudFormation. It is developed following [uluru onboarding guide](https://w.amazon.com/bin/view/AWS21/Design/Uluru/Onboarding_Guide) Please refer to [redshift API](https://docs.aws.amazon.com/redshift/latest/APIReference/Welcome.html) when development.

##Code generate
The RPDK will automatically generate the correct resource model from the schema whenever the project is built via Maven. You can also do this manually with the following command: `cfn generate`.

> Please don't modify files under `target/generated-sources/rpdk`, as they will be automatically overwritten.

The code uses [Lombok](https://projectlombok.org/), and [you may have to install IDE integrations](https://projectlombok.org/setup/overview) to enable auto-complete for Lombok-annotated classes.

##Sam Test
Create two subnets using one VPC first, these can be used to test construct the subnet group. Follow [Manual testing guide](https://w.amazon.com/bin/view/AWS21/Design/Uluru/Onboarding_Guide/Uluru_OpenSource_And_Developing_In_Amazon/IntegrationTests/) to create it to test
