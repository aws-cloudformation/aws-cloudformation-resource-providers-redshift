# AWS::Redshift::Cluster

An example resource schema demonstrating some basic constructs and validation rules.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "Type" : "AWS::Redshift::Cluster",
    "Properties" : {
        "<a href="#clusteridentifier" title="ClusterIdentifier">ClusterIdentifier</a>" : <i>String</i>,
        "<a href="#masterusername" title="MasterUsername">MasterUsername</a>" : <i>String</i>,
        "<a href="#masteruserpassword" title="MasterUserPassword">MasterUserPassword</a>" : <i>String</i>,
        "<a href="#nodetype" title="NodeType">NodeType</a>" : <i>String</i>,
        "<a href="#allowversionupgrade" title="AllowVersionUpgrade">AllowVersionUpgrade</a>" : <i>Boolean</i>,
        "<a href="#automatedsnapshotretentionperiod" title="AutomatedSnapshotRetentionPeriod">AutomatedSnapshotRetentionPeriod</a>" : <i>Integer</i>,
        "<a href="#availabilityzone" title="AvailabilityZone">AvailabilityZone</a>" : <i>String</i>,
        "<a href="#clusterparametergroupname" title="ClusterParameterGroupName">ClusterParameterGroupName</a>" : <i>String</i>,
        "<a href="#clustertype" title="ClusterType">ClusterType</a>" : <i>String</i>,
        "<a href="#clusterversion" title="ClusterVersion">ClusterVersion</a>" : <i>String</i>,
        "<a href="#clustersubnetgroupname" title="ClusterSubnetGroupName">ClusterSubnetGroupName</a>" : <i>String</i>,
        "<a href="#dbname" title="DBName">DBName</a>" : <i>String</i>,
        "<a href="#elasticip" title="ElasticIp">ElasticIp</a>" : <i>String</i>,
        "<a href="#encrypted" title="Encrypted">Encrypted</a>" : <i>Boolean</i>,
        "<a href="#hsmclientcertificateidentifier" title="HsmClientCertificateIdentifier">HsmClientCertificateIdentifier</a>" : <i>String</i>,
        "<a href="#hsmconfigurationidentifier" title="HsmConfigurationIdentifier">HsmConfigurationIdentifier</a>" : <i>String</i>,
        "<a href="#kmskeyid" title="KmsKeyId">KmsKeyId</a>" : <i>String</i>,
        "<a href="#numberofnodes" title="NumberOfNodes">NumberOfNodes</a>" : <i>Integer</i>,
        "<a href="#port" title="Port">Port</a>" : <i>Integer</i>,
        "<a href="#preferredmaintenancewindow" title="PreferredMaintenanceWindow">PreferredMaintenanceWindow</a>" : <i>String</i>,
        "<a href="#publiclyaccessible" title="PubliclyAccessible">PubliclyAccessible</a>" : <i>Boolean</i>,
        "<a href="#clustersecuritygroups" title="ClusterSecurityGroups">ClusterSecurityGroups</a>" : <i>[ String, ... ]</i>,
        "<a href="#iamroles" title="IamRoles">IamRoles</a>" : <i>[ String, ... ]</i>,
        "<a href="#tags" title="Tags">Tags</a>" : <i>[ <a href="tag.md">Tag</a>, ... ]</i>,
        "<a href="#vpcsecuritygroupids" title="VpcSecurityGroupIds">VpcSecurityGroupIds</a>" : <i>[ String, ... ]</i>,
        "<a href="#snapshotclusteridentifier" title="SnapshotClusterIdentifier">SnapshotClusterIdentifier</a>" : <i>String</i>,
        "<a href="#snapshotidentifier" title="SnapshotIdentifier">SnapshotIdentifier</a>" : <i>String</i>,
        "<a href="#owneraccount" title="OwnerAccount">OwnerAccount</a>" : <i>String</i>,
        "<a href="#loggingproperties" title="LoggingProperties">LoggingProperties</a>" : <i><a href="loggingproperties.md">LoggingProperties</a></i>,
        "<a href="#endpoint" title="Endpoint">Endpoint</a>" : <i><a href="endpoint.md">Endpoint</a></i>,
        "<a href="#destinationregion" title="DestinationRegion">DestinationRegion</a>" : <i>String</i>,
        "<a href="#snapshotcopyretentionperiod" title="SnapshotCopyRetentionPeriod">SnapshotCopyRetentionPeriod</a>" : <i>Integer</i>,
        "<a href="#snapshotcopygrantname" title="SnapshotCopyGrantName">SnapshotCopyGrantName</a>" : <i>String</i>,
        "<a href="#manualsnapshotretentionperiod" title="ManualSnapshotRetentionPeriod">ManualSnapshotRetentionPeriod</a>" : <i>Integer</i>,
        "<a href="#snapshotcopymanual" title="SnapshotCopyManual">SnapshotCopyManual</a>" : <i>Boolean</i>,
        "<a href="#availabilityzonerelocation" title="AvailabilityZoneRelocation">AvailabilityZoneRelocation</a>" : <i>Boolean</i>,
        "<a href="#availabilityzonerelocationstatus" title="AvailabilityZoneRelocationStatus">AvailabilityZoneRelocationStatus</a>" : <i>String</i>,
        "<a href="#aquaconfigurationstatus" title="AquaConfigurationStatus">AquaConfigurationStatus</a>" : <i>String</i>,
        "<a href="#classic" title="Classic">Classic</a>" : <i>Boolean</i>,
        "<a href="#enhancedvpcrouting" title="EnhancedVpcRouting">EnhancedVpcRouting</a>" : <i>Boolean</i>,
        "<a href="#maintenancetrackname" title="MaintenanceTrackName">MaintenanceTrackName</a>" : <i>String</i>,
        "<a href="#defermaintenance" title="DeferMaintenance">DeferMaintenance</a>" : <i>Boolean</i>,
        "<a href="#defermaintenancestarttime" title="DeferMaintenanceStartTime">DeferMaintenanceStartTime</a>" : <i>String</i>,
        "<a href="#defermaintenanceendtime" title="DeferMaintenanceEndTime">DeferMaintenanceEndTime</a>" : <i>String</i>,
        "<a href="#defermaintenanceduration" title="DeferMaintenanceDuration">DeferMaintenanceDuration</a>" : <i>Integer</i>,
        "<a href="#revisiontarget" title="RevisionTarget">RevisionTarget</a>" : <i>String</i>,
        "<a href="#resourceaction" title="ResourceAction">ResourceAction</a>" : <i>String</i>,
        "<a href="#rotateencryptionkey" title="RotateEncryptionKey">RotateEncryptionKey</a>" : <i>Boolean</i>,
        "<a href="#multiaz" title="MultiAZ">MultiAZ</a>" : <i>Boolean</i>,
        "<a href="#namespaceresourcepolicy" title="NamespaceResourcePolicy">NamespaceResourcePolicy</a>" : <i>Map</i>,
        "<a href="#managemasterpassword" title="ManageMasterPassword">ManageMasterPassword</a>" : <i>Boolean</i>,
        "<a href="#masterpasswordsecretkmskeyid" title="MasterPasswordSecretKmsKeyId">MasterPasswordSecretKmsKeyId</a>" : <i>String</i>,
    }
}
</pre>

### YAML

<pre>
Type: AWS::Redshift::Cluster
Properties:
    <a href="#clusteridentifier" title="ClusterIdentifier">ClusterIdentifier</a>: <i>String</i>
    <a href="#masterusername" title="MasterUsername">MasterUsername</a>: <i>String</i>
    <a href="#masteruserpassword" title="MasterUserPassword">MasterUserPassword</a>: <i>String</i>
    <a href="#nodetype" title="NodeType">NodeType</a>: <i>String</i>
    <a href="#allowversionupgrade" title="AllowVersionUpgrade">AllowVersionUpgrade</a>: <i>Boolean</i>
    <a href="#automatedsnapshotretentionperiod" title="AutomatedSnapshotRetentionPeriod">AutomatedSnapshotRetentionPeriod</a>: <i>Integer</i>
    <a href="#availabilityzone" title="AvailabilityZone">AvailabilityZone</a>: <i>String</i>
    <a href="#clusterparametergroupname" title="ClusterParameterGroupName">ClusterParameterGroupName</a>: <i>String</i>
    <a href="#clustertype" title="ClusterType">ClusterType</a>: <i>String</i>
    <a href="#clusterversion" title="ClusterVersion">ClusterVersion</a>: <i>String</i>
    <a href="#clustersubnetgroupname" title="ClusterSubnetGroupName">ClusterSubnetGroupName</a>: <i>String</i>
    <a href="#dbname" title="DBName">DBName</a>: <i>String</i>
    <a href="#elasticip" title="ElasticIp">ElasticIp</a>: <i>String</i>
    <a href="#encrypted" title="Encrypted">Encrypted</a>: <i>Boolean</i>
    <a href="#hsmclientcertificateidentifier" title="HsmClientCertificateIdentifier">HsmClientCertificateIdentifier</a>: <i>String</i>
    <a href="#hsmconfigurationidentifier" title="HsmConfigurationIdentifier">HsmConfigurationIdentifier</a>: <i>String</i>
    <a href="#kmskeyid" title="KmsKeyId">KmsKeyId</a>: <i>String</i>
    <a href="#numberofnodes" title="NumberOfNodes">NumberOfNodes</a>: <i>Integer</i>
    <a href="#port" title="Port">Port</a>: <i>Integer</i>
    <a href="#preferredmaintenancewindow" title="PreferredMaintenanceWindow">PreferredMaintenanceWindow</a>: <i>String</i>
    <a href="#publiclyaccessible" title="PubliclyAccessible">PubliclyAccessible</a>: <i>Boolean</i>
    <a href="#clustersecuritygroups" title="ClusterSecurityGroups">ClusterSecurityGroups</a>: <i>
      - String</i>
    <a href="#iamroles" title="IamRoles">IamRoles</a>: <i>
      - String</i>
    <a href="#tags" title="Tags">Tags</a>: <i>
      - <a href="tag.md">Tag</a></i>
    <a href="#vpcsecuritygroupids" title="VpcSecurityGroupIds">VpcSecurityGroupIds</a>: <i>
      - String</i>
    <a href="#snapshotclusteridentifier" title="SnapshotClusterIdentifier">SnapshotClusterIdentifier</a>: <i>String</i>
    <a href="#snapshotidentifier" title="SnapshotIdentifier">SnapshotIdentifier</a>: <i>String</i>
    <a href="#owneraccount" title="OwnerAccount">OwnerAccount</a>: <i>String</i>
    <a href="#loggingproperties" title="LoggingProperties">LoggingProperties</a>: <i><a href="loggingproperties.md">LoggingProperties</a></i>
    <a href="#endpoint" title="Endpoint">Endpoint</a>: <i><a href="endpoint.md">Endpoint</a></i>
    <a href="#destinationregion" title="DestinationRegion">DestinationRegion</a>: <i>String</i>
    <a href="#snapshotcopyretentionperiod" title="SnapshotCopyRetentionPeriod">SnapshotCopyRetentionPeriod</a>: <i>Integer</i>
    <a href="#snapshotcopygrantname" title="SnapshotCopyGrantName">SnapshotCopyGrantName</a>: <i>String</i>
    <a href="#manualsnapshotretentionperiod" title="ManualSnapshotRetentionPeriod">ManualSnapshotRetentionPeriod</a>: <i>Integer</i>
    <a href="#snapshotcopymanual" title="SnapshotCopyManual">SnapshotCopyManual</a>: <i>Boolean</i>
    <a href="#availabilityzonerelocation" title="AvailabilityZoneRelocation">AvailabilityZoneRelocation</a>: <i>Boolean</i>
    <a href="#availabilityzonerelocationstatus" title="AvailabilityZoneRelocationStatus">AvailabilityZoneRelocationStatus</a>: <i>String</i>
    <a href="#aquaconfigurationstatus" title="AquaConfigurationStatus">AquaConfigurationStatus</a>: <i>String</i>
    <a href="#classic" title="Classic">Classic</a>: <i>Boolean</i>
    <a href="#enhancedvpcrouting" title="EnhancedVpcRouting">EnhancedVpcRouting</a>: <i>Boolean</i>
    <a href="#maintenancetrackname" title="MaintenanceTrackName">MaintenanceTrackName</a>: <i>String</i>
    <a href="#defermaintenance" title="DeferMaintenance">DeferMaintenance</a>: <i>Boolean</i>
    <a href="#defermaintenancestarttime" title="DeferMaintenanceStartTime">DeferMaintenanceStartTime</a>: <i>String</i>
    <a href="#defermaintenanceendtime" title="DeferMaintenanceEndTime">DeferMaintenanceEndTime</a>: <i>String</i>
    <a href="#defermaintenanceduration" title="DeferMaintenanceDuration">DeferMaintenanceDuration</a>: <i>Integer</i>
    <a href="#revisiontarget" title="RevisionTarget">RevisionTarget</a>: <i>String</i>
    <a href="#resourceaction" title="ResourceAction">ResourceAction</a>: <i>String</i>
    <a href="#rotateencryptionkey" title="RotateEncryptionKey">RotateEncryptionKey</a>: <i>Boolean</i>
    <a href="#multiaz" title="MultiAZ">MultiAZ</a>: <i>Boolean</i>
    <a href="#namespaceresourcepolicy" title="NamespaceResourcePolicy">NamespaceResourcePolicy</a>: <i>Map</i>
    <a href="#managemasterpassword" title="ManageMasterPassword">ManageMasterPassword</a>: <i>Boolean</i>
    <a href="#masterpasswordsecretkmskeyid" title="MasterPasswordSecretKmsKeyId">MasterPasswordSecretKmsKeyId</a>: <i>String</i>
</pre>

## Properties

#### ClusterIdentifier

A unique identifier for the cluster. You use this identifier to refer to the cluster for any subsequent cluster operations such as deleting or modifying. All alphabetical characters must be lower case, no hypens at the end, no two consecutive hyphens. Cluster name should be unique for all clusters within an AWS account

_Required_: No

_Type_: String

_Maximum Length_: <code>63</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### MasterUsername

The user name associated with the master user account for the cluster that is being created. The user name can't be PUBLIC and first character must be a letter.

_Required_: Yes

_Type_: String

_Maximum Length_: <code>128</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### MasterUserPassword

The password associated with the master user account for the cluster that is being created. You can't use MasterUserPassword if ManageMasterPassword is true. Password must be between 8 and 64 characters in length, should have at least one uppercase letter.Must contain at least one lowercase letter.Must contain one number.Can be any printable ASCII character.

_Required_: No

_Type_: String

_Maximum Length_: <code>64</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### NodeType

The node type to be provisioned for the cluster.Valid Values: ds2.xlarge | ds2.8xlarge | dc1.large | dc1.8xlarge | dc2.large | dc2.8xlarge | ra3.4xlarge | ra3.16xlarge

_Required_: Yes

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### AllowVersionUpgrade

Major version upgrades can be applied during the maintenance window to the Amazon Redshift engine that is running on the cluster. Default value is True

_Required_: No

_Type_: Boolean

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### AutomatedSnapshotRetentionPeriod

The number of days that automated snapshots are retained. If the value is 0, automated snapshots are disabled. Default value is 1

_Required_: No

_Type_: Integer

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### AvailabilityZone

The EC2 Availability Zone (AZ) in which you want Amazon Redshift to provision the cluster. Default: A random, system-chosen Availability Zone in the region that is specified by the endpoint

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ClusterParameterGroupName

The name of the parameter group to be associated with this cluster.

_Required_: No

_Type_: String

_Maximum Length_: <code>255</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ClusterType

The type of the cluster. When cluster type is specified as single-node, the NumberOfNodes parameter is not required and if multi-node, the NumberOfNodes parameter is required

_Required_: Yes

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ClusterVersion

The version of the Amazon Redshift engine software that you want to deploy on the cluster.The version selected runs on all the nodes in the cluster.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ClusterSubnetGroupName

The name of a cluster subnet group to be associated with this cluster.

_Required_: No

_Type_: String

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### DBName

The name of the first database to be created when the cluster is created. To create additional databases after the cluster is created, connect to the cluster with a SQL client and use SQL commands to create a database.

_Required_: Yes

_Type_: String

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### ElasticIp

The Elastic IP (EIP) address for the cluster.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Encrypted

If true, the data in the cluster is encrypted at rest.

_Required_: No

_Type_: Boolean

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### HsmClientCertificateIdentifier

Specifies the name of the HSM client certificate the Amazon Redshift cluster uses to retrieve the data encryption keys stored in an HSM

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### HsmConfigurationIdentifier

Specifies the name of the HSM configuration that contains the information the Amazon Redshift cluster can use to retrieve and store keys in an HSM.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### KmsKeyId

The AWS Key Management Service (KMS) key ID of the encryption key that you want to use to encrypt data in the cluster.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### NumberOfNodes

The number of compute nodes in the cluster. This parameter is required when the ClusterType parameter is specified as multi-node.

_Required_: No

_Type_: Integer

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Port

The port number on which the cluster accepts incoming connections. The cluster is accessible only via the JDBC and ODBC connection strings

_Required_: No

_Type_: Integer

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### PreferredMaintenanceWindow

The weekly time range (in UTC) during which automated cluster maintenance can occur.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### PubliclyAccessible

If true, the cluster can be accessed from a public network.

_Required_: No

_Type_: Boolean

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ClusterSecurityGroups

A list of security groups to be associated with this cluster.

_Required_: No

_Type_: List of String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### IamRoles

A list of AWS Identity and Access Management (IAM) roles that can be used by the cluster to access other AWS services. You must supply the IAM roles in their Amazon Resource Name (ARN) format. You can supply up to 50 IAM roles in a single request

_Required_: No

_Type_: List of String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Tags

The list of tags for the cluster parameter group.

_Required_: No

_Type_: List of <a href="tag.md">Tag</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### VpcSecurityGroupIds

A list of Virtual Private Cloud (VPC) security groups to be associated with the cluster.

_Required_: No

_Type_: List of String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### SnapshotClusterIdentifier

The name of the cluster the source snapshot was created from. This parameter is required if your IAM user has a policy containing a snapshot resource element that specifies anything other than * for the cluster name.

_Required_: No

_Type_: String

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### SnapshotIdentifier

The name of the snapshot from which to create the new cluster. This parameter isn't case sensitive.

_Required_: No

_Type_: String

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### OwnerAccount

_Required_: No

_Type_: String

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### LoggingProperties

_Required_: No

_Type_: <a href="loggingproperties.md">LoggingProperties</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Endpoint

_Required_: No

_Type_: <a href="endpoint.md">Endpoint</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### DestinationRegion

The destination AWS Region that you want to copy snapshots to. Constraints: Must be the name of a valid AWS Region. For more information, see Regions and Endpoints in the Amazon Web Services [https://docs.aws.amazon.com/general/latest/gr/rande.html#redshift_region] General Reference

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### SnapshotCopyRetentionPeriod

The number of days to retain automated snapshots in the destination region after they are copied from the source region.

 Default is 7.

 Constraints: Must be at least 1 and no more than 35.

_Required_: No

_Type_: Integer

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### SnapshotCopyGrantName

The name of the snapshot copy grant to use when snapshots of an AWS KMS-encrypted cluster are copied to the destination region.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ManualSnapshotRetentionPeriod

The number of days to retain newly copied snapshots in the destination AWS Region after they are copied from the source AWS Region. If the value is -1, the manual snapshot is retained indefinitely.

The value must be either -1 or an integer between 1 and 3,653.

_Required_: No

_Type_: Integer

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### SnapshotCopyManual

Indicates whether to apply the snapshot retention period to newly copied manual snapshots instead of automated snapshots.

_Required_: No

_Type_: Boolean

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### AvailabilityZoneRelocation

The option to enable relocation for an Amazon Redshift cluster between Availability Zones after the cluster modification is complete.

_Required_: No

_Type_: Boolean

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### AvailabilityZoneRelocationStatus

The availability zone relocation status of the cluster

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### AquaConfigurationStatus

The value represents how the cluster is configured to use AQUA (Advanced Query Accelerator) after the cluster is restored. Possible values include the following.

enabled - Use AQUA if it is available for the current Region and Amazon Redshift node type.
disabled - Don't use AQUA.
auto - Amazon Redshift determines whether to use AQUA.


_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Classic

A boolean value indicating whether the resize operation is using the classic resize process. If you don't provide this parameter or set the value to false , the resize type is elastic.

_Required_: No

_Type_: Boolean

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### EnhancedVpcRouting

An option that specifies whether to create the cluster with enhanced VPC routing enabled. To create a cluster that uses enhanced VPC routing, the cluster must be in a VPC. For more information, see Enhanced VPC Routing in the Amazon Redshift Cluster Management Guide.

If this option is true , enhanced VPC routing is enabled.

Default: false

_Required_: No

_Type_: Boolean

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### MaintenanceTrackName

The name for the maintenance track that you want to assign for the cluster. This name change is asynchronous. The new track name stays in the PendingModifiedValues for the cluster until the next maintenance window. When the maintenance track changes, the cluster is switched to the latest cluster release available for the maintenance track. At this point, the maintenance track name is applied.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### DeferMaintenance

A boolean indicating whether to enable the deferred maintenance window.

_Required_: No

_Type_: Boolean

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### DeferMaintenanceStartTime

A timestamp indicating the start time for the deferred maintenance window.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### DeferMaintenanceEndTime

A timestamp indicating end time for the deferred maintenance window. If you specify an end time, you can't specify a duration.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### DeferMaintenanceDuration

An integer indicating the duration of the maintenance window in days. If you specify a duration, you can't specify an end time. The duration must be 45 days or less.

_Required_: No

_Type_: Integer

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### RevisionTarget

The identifier of the database revision. You can retrieve this value from the response to the DescribeClusterDbRevisions request.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ResourceAction

The Redshift operation to be performed. Resource Action supports pause-cluster, resume-cluster, failover-primary-compute APIs

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### RotateEncryptionKey

A boolean indicating if we want to rotate Encryption Keys.

_Required_: No

_Type_: Boolean

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### MultiAZ

A boolean indicating if the redshift cluster is multi-az or not. If you don't provide this parameter or set the value to false, the redshift cluster will be single-az.

_Required_: No

_Type_: Boolean

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### NamespaceResourcePolicy

The namespace resource policy document that will be attached to a Redshift cluster.

_Required_: No

_Type_: Map

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ManageMasterPassword

A boolean indicating if the redshift cluster's admin user credentials is managed by Redshift or not. You can't use MasterUserPassword if ManageMasterPassword is true. If ManageMasterPassword is false or not set, Amazon Redshift uses MasterUserPassword for the admin user account's password.

_Required_: No

_Type_: Boolean

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### MasterPasswordSecretKmsKeyId

The ID of the Key Management Service (KMS) key used to encrypt and store the cluster's admin user credentials secret.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

## Return Values

### Ref

When you pass the logical ID of this resource to the intrinsic `Ref` function, Ref returns the ClusterIdentifier.

### Fn::GetAtt

The `Fn::GetAtt` intrinsic function returns a value for a specified attribute of this type. The following are the available attributes and sample return values.

For more information about using the `Fn::GetAtt` intrinsic function, see [Fn::GetAtt](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-getatt.html).

#### DeferMaintenanceIdentifier

A unique identifier for the deferred maintenance window.

#### Port

Returns the <code>Port</code> value.

#### Address

Returns the <code>Address</code> value.

#### ClusterNamespaceArn

The Amazon Resource Name (ARN) of the cluster namespace.

#### MasterPasswordSecretArn

The Amazon Resource Name (ARN) for the cluster's admin user credentials secret.
