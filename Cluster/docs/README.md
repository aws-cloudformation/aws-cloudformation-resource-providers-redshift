# AWS::Redshift::Cluster

An example resource schema demonstrating some basic constructs and validation rules.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "Type" : "AWS::Redshift::Cluster",
    "Properties" : {
        "<a href="#masterusername" title="MasterUsername">MasterUsername</a>" : <i>String</i>,
        "<a href="#masteruserpassword" title="MasterUserPassword">MasterUserPassword</a>" : <i>String</i>,
        "<a href="#nodetype" title="NodeType">NodeType</a>" : <i>String</i>,
        "<a href="#additionalinfo" title="AdditionalInfo">AdditionalInfo</a>" : <i>String</i>,
        "<a href="#allowversionupgrade" title="AllowVersionUpgrade">AllowVersionUpgrade</a>" : <i>Boolean</i>,
        "<a href="#automatedsnapshotretentionperiod" title="AutomatedSnapshotRetentionPeriod">AutomatedSnapshotRetentionPeriod</a>" : <i>Integer</i>,
        "<a href="#availabilityzone" title="AvailabilityZone">AvailabilityZone</a>" : <i>String</i>,
        "<a href="#clusterparametergroupname" title="ClusterParameterGroupName">ClusterParameterGroupName</a>" : <i>String</i>,
        "<a href="#clustertype" title="ClusterType">ClusterType</a>" : <i>String</i>,
        "<a href="#clusterversion" title="ClusterVersion">ClusterVersion</a>" : <i>String</i>,
        "<a href="#dbname" title="DBName">DBName</a>" : <i>String</i>,
        "<a href="#elasticip" title="ElasticIp">ElasticIp</a>" : <i>String</i>,
        "<a href="#encrypted" title="Encrypted">Encrypted</a>" : <i>Boolean</i>,
        "<a href="#enhancedvpcrouting" title="EnhancedVpcRouting">EnhancedVpcRouting</a>" : <i>Boolean</i>,
        "<a href="#hsmclientcertificateidentifier" title="HsmClientCertificateIdentifier">HsmClientCertificateIdentifier</a>" : <i>String</i>,
        "<a href="#hsmconfigurationidentifier" title="HsmConfigurationIdentifier">HsmConfigurationIdentifier</a>" : <i>String</i>,
        "<a href="#kmskeyid" title="KmsKeyId">KmsKeyId</a>" : <i>String</i>,
        "<a href="#maintenancetrackname" title="MaintenanceTrackName">MaintenanceTrackName</a>" : <i>String</i>,
        "<a href="#manualsnapshotretentionperiod" title="ManualSnapshotRetentionPeriod">ManualSnapshotRetentionPeriod</a>" : <i>Integer</i>,
        "<a href="#numberofnodes" title="NumberOfNodes">NumberOfNodes</a>" : <i>Integer</i>,
        "<a href="#port" title="Port">Port</a>" : <i>Integer</i>,
        "<a href="#preferredmaintenancewindow" title="PreferredMaintenanceWindow">PreferredMaintenanceWindow</a>" : <i>String</i>,
        "<a href="#publiclyaccessible" title="PubliclyAccessible">PubliclyAccessible</a>" : <i>Boolean</i>,
        "<a href="#snapshotscheduleidentifier" title="SnapshotScheduleIdentifier">SnapshotScheduleIdentifier</a>" : <i>String</i>,
        "<a href="#clustersecuritygroups" title="ClusterSecurityGroups">ClusterSecurityGroups</a>" : <i>[ String, ... ]</i>,
        "<a href="#iamroles" title="IamRoles">IamRoles</a>" : <i>[ String, ... ]</i>,
        "<a href="#tags" title="Tags">Tags</a>" : <i>[ <a href="tag.md">Tag</a>, ... ]</i>,
        "<a href="#vpcsecuritygroupids" title="VpcSecurityGroupIds">VpcSecurityGroupIds</a>" : <i>[ String, ... ]</i>,
        "<a href="#finalclustersnapshotidentifier" title="FinalClusterSnapshotIdentifier">FinalClusterSnapshotIdentifier</a>" : <i>String</i>,
        "<a href="#finalclustersnapshotretentionperiod" title="FinalClusterSnapshotRetentionPeriod">FinalClusterSnapshotRetentionPeriod</a>" : <i>Integer</i>,
        "<a href="#skipfinalclustersnapshot" title="SkipFinalClusterSnapshot">SkipFinalClusterSnapshot</a>" : <i>Boolean</i>,
        "<a href="#marker" title="Marker">Marker</a>" : <i>String</i>,
        "<a href="#maxrecords" title="MaxRecords">MaxRecords</a>" : <i>String</i>,
        "<a href="#newclusteridentifier" title="NewClusterIdentifier">NewClusterIdentifier</a>" : <i>String</i>,
        "<a href="#addiamroles" title="AddIamRoles">AddIamRoles</a>" : <i>[ String, ... ]</i>,
        "<a href="#removeiamroles" title="RemoveIamRoles">RemoveIamRoles</a>" : <i>[ String, ... ]</i>,
        "<a href="#redshiftcommand" title="RedshiftCommand">RedshiftCommand</a>" : <i>String</i>,
        "<a href="#currentdatabaserevision" title="CurrentDatabaseRevision">CurrentDatabaseRevision</a>" : <i>String</i>,
        "<a href="#databaserevisionreleasedate" title="DatabaseRevisionReleaseDate">DatabaseRevisionReleaseDate</a>" : <i>String</i>,
        "<a href="#revisiontarget" title="RevisionTarget">RevisionTarget</a>" : <i>String</i>,
        "<a href="#revisiontargets" title="RevisionTargets">RevisionTargets</a>" : <i>[ String, ... ]</i>,
        "<a href="#defermaintenance" title="DeferMaintenance">DeferMaintenance</a>" : <i>Boolean</i>,
        "<a href="#defermaintenanceduration" title="DeferMaintenanceDuration">DeferMaintenanceDuration</a>" : <i>Integer</i>,
        "<a href="#defermaintenanceendtime" title="DeferMaintenanceEndTime">DeferMaintenanceEndTime</a>" : <i>String</i>,
        "<a href="#defermaintenancestarttime" title="DeferMaintenanceStartTime">DeferMaintenanceStartTime</a>" : <i>String</i>,
        "<a href="#defermaintenanceidentifier" title="DeferMaintenanceIdentifier">DeferMaintenanceIdentifier</a>" : <i>String</i>,
        "<a href="#defermaintenancewindows" title="DeferMaintenanceWindows">DeferMaintenanceWindows</a>" : <i>[ String, ... ]</i>,
        "<a href="#snapshotidentifier" title="SnapshotIdentifier">SnapshotIdentifier</a>" : <i>String</i>,
        "<a href="#force" title="Force">Force</a>" : <i>Boolean</i>,
        "<a href="#accountswithrestoreaccess" title="AccountsWithRestoreAccess">AccountsWithRestoreAccess</a>" : <i>[ String, ... ]</i>,
        "<a href="#actualincrementalbackupsizeinmegabytes" title="ActualIncrementalBackupSizeInMegaBytes">ActualIncrementalBackupSizeInMegaBytes</a>" : <i>Double</i>,
        "<a href="#backupprogressinmegabytes" title="BackupProgressInMegaBytes">BackupProgressInMegaBytes</a>" : <i>Double</i>,
        "<a href="#clustercreatetime" title="ClusterCreateTime">ClusterCreateTime</a>" : <i>String</i>,
        "<a href="#currentbackuprateinmegabytespersecond" title="CurrentBackupRateInMegaBytesPerSecond">CurrentBackupRateInMegaBytesPerSecond</a>" : <i>Double</i>,
        "<a href="#elapsedtimeinseconds" title="ElapsedTimeInSeconds">ElapsedTimeInSeconds</a>" : <i>Double</i>,
        "<a href="#encryptedwithhsm" title="EncryptedWithHSM">EncryptedWithHSM</a>" : <i>Boolean</i>,
        "<a href="#estimatedsecondstocompletion" title="EstimatedSecondsToCompletion">EstimatedSecondsToCompletion</a>" : <i>Double</i>,
        "<a href="#manualsnapshotremainingdays" title="ManualSnapshotRemainingDays">ManualSnapshotRemainingDays</a>" : <i>Integer</i>,
        "<a href="#owneraccount" title="OwnerAccount">OwnerAccount</a>" : <i>String</i>,
        "<a href="#restorablenodetypes" title="RestorableNodeTypes">RestorableNodeTypes</a>" : <i>[ String, ... ]</i>,
        "<a href="#snapshotcreatetime" title="SnapshotCreateTime">SnapshotCreateTime</a>" : <i>String</i>,
        "<a href="#snapshotretentionstarttime" title="SnapshotRetentionStartTime">SnapshotRetentionStartTime</a>" : <i>String</i>,
        "<a href="#snapshottype" title="SnapshotType">SnapshotType</a>" : <i>String</i>,
        "<a href="#sourceregion" title="SourceRegion">SourceRegion</a>" : <i>String</i>,
        "<a href="#status" title="Status">Status</a>" : <i>String</i>,
        "<a href="#totalbackupsizeinmegabytes" title="TotalBackupSizeInMegaBytes">TotalBackupSizeInMegaBytes</a>" : <i>Double</i>,
        "<a href="#vpcid" title="VpcId">VpcId</a>" : <i>String</i>,
        "<a href="#starttime" title="StartTime">StartTime</a>" : <i>String</i>,
        "<a href="#endtime" title="EndTime">EndTime</a>" : <i>String</i>,
        "<a href="#tagkeys" title="TagKeys">TagKeys</a>" : <i>[ String, ... ]</i>,
        "<a href="#tagvalues" title="TagValues">TagValues</a>" : <i>[ String, ... ]</i>,
        "<a href="#clusterexists" title="ClusterExists">ClusterExists</a>" : <i>Boolean</i>
    }
}
</pre>

### YAML

<pre>
Type: AWS::Redshift::Cluster
Properties:
    <a href="#masterusername" title="MasterUsername">MasterUsername</a>: <i>String</i>
    <a href="#masteruserpassword" title="MasterUserPassword">MasterUserPassword</a>: <i>String</i>
    <a href="#nodetype" title="NodeType">NodeType</a>: <i>String</i>
    <a href="#additionalinfo" title="AdditionalInfo">AdditionalInfo</a>: <i>String</i>
    <a href="#allowversionupgrade" title="AllowVersionUpgrade">AllowVersionUpgrade</a>: <i>Boolean</i>
    <a href="#automatedsnapshotretentionperiod" title="AutomatedSnapshotRetentionPeriod">AutomatedSnapshotRetentionPeriod</a>: <i>Integer</i>
    <a href="#availabilityzone" title="AvailabilityZone">AvailabilityZone</a>: <i>String</i>
    <a href="#clusterparametergroupname" title="ClusterParameterGroupName">ClusterParameterGroupName</a>: <i>String</i>
    <a href="#clustertype" title="ClusterType">ClusterType</a>: <i>String</i>
    <a href="#clusterversion" title="ClusterVersion">ClusterVersion</a>: <i>String</i>
    <a href="#dbname" title="DBName">DBName</a>: <i>String</i>
    <a href="#elasticip" title="ElasticIp">ElasticIp</a>: <i>String</i>
    <a href="#encrypted" title="Encrypted">Encrypted</a>: <i>Boolean</i>
    <a href="#enhancedvpcrouting" title="EnhancedVpcRouting">EnhancedVpcRouting</a>: <i>Boolean</i>
    <a href="#hsmclientcertificateidentifier" title="HsmClientCertificateIdentifier">HsmClientCertificateIdentifier</a>: <i>String</i>
    <a href="#hsmconfigurationidentifier" title="HsmConfigurationIdentifier">HsmConfigurationIdentifier</a>: <i>String</i>
    <a href="#kmskeyid" title="KmsKeyId">KmsKeyId</a>: <i>String</i>
    <a href="#maintenancetrackname" title="MaintenanceTrackName">MaintenanceTrackName</a>: <i>String</i>
    <a href="#manualsnapshotretentionperiod" title="ManualSnapshotRetentionPeriod">ManualSnapshotRetentionPeriod</a>: <i>Integer</i>
    <a href="#numberofnodes" title="NumberOfNodes">NumberOfNodes</a>: <i>Integer</i>
    <a href="#port" title="Port">Port</a>: <i>Integer</i>
    <a href="#preferredmaintenancewindow" title="PreferredMaintenanceWindow">PreferredMaintenanceWindow</a>: <i>String</i>
    <a href="#publiclyaccessible" title="PubliclyAccessible">PubliclyAccessible</a>: <i>Boolean</i>
    <a href="#snapshotscheduleidentifier" title="SnapshotScheduleIdentifier">SnapshotScheduleIdentifier</a>: <i>String</i>
    <a href="#clustersecuritygroups" title="ClusterSecurityGroups">ClusterSecurityGroups</a>: <i>
      - String</i>
    <a href="#iamroles" title="IamRoles">IamRoles</a>: <i>
      - String</i>
    <a href="#tags" title="Tags">Tags</a>: <i>
      - <a href="tag.md">Tag</a></i>
    <a href="#vpcsecuritygroupids" title="VpcSecurityGroupIds">VpcSecurityGroupIds</a>: <i>
      - String</i>
    <a href="#finalclustersnapshotidentifier" title="FinalClusterSnapshotIdentifier">FinalClusterSnapshotIdentifier</a>: <i>String</i>
    <a href="#finalclustersnapshotretentionperiod" title="FinalClusterSnapshotRetentionPeriod">FinalClusterSnapshotRetentionPeriod</a>: <i>Integer</i>
    <a href="#skipfinalclustersnapshot" title="SkipFinalClusterSnapshot">SkipFinalClusterSnapshot</a>: <i>Boolean</i>
    <a href="#marker" title="Marker">Marker</a>: <i>String</i>
    <a href="#maxrecords" title="MaxRecords">MaxRecords</a>: <i>String</i>
    <a href="#newclusteridentifier" title="NewClusterIdentifier">NewClusterIdentifier</a>: <i>String</i>
    <a href="#addiamroles" title="AddIamRoles">AddIamRoles</a>: <i>
      - String</i>
    <a href="#removeiamroles" title="RemoveIamRoles">RemoveIamRoles</a>: <i>
      - String</i>
    <a href="#redshiftcommand" title="RedshiftCommand">RedshiftCommand</a>: <i>String</i>
    <a href="#currentdatabaserevision" title="CurrentDatabaseRevision">CurrentDatabaseRevision</a>: <i>String</i>
    <a href="#databaserevisionreleasedate" title="DatabaseRevisionReleaseDate">DatabaseRevisionReleaseDate</a>: <i>String</i>
    <a href="#revisiontarget" title="RevisionTarget">RevisionTarget</a>: <i>String</i>
    <a href="#revisiontargets" title="RevisionTargets">RevisionTargets</a>: <i>
      - String</i>
    <a href="#defermaintenance" title="DeferMaintenance">DeferMaintenance</a>: <i>Boolean</i>
    <a href="#defermaintenanceduration" title="DeferMaintenanceDuration">DeferMaintenanceDuration</a>: <i>Integer</i>
    <a href="#defermaintenanceendtime" title="DeferMaintenanceEndTime">DeferMaintenanceEndTime</a>: <i>String</i>
    <a href="#defermaintenancestarttime" title="DeferMaintenanceStartTime">DeferMaintenanceStartTime</a>: <i>String</i>
    <a href="#defermaintenanceidentifier" title="DeferMaintenanceIdentifier">DeferMaintenanceIdentifier</a>: <i>String</i>
    <a href="#defermaintenancewindows" title="DeferMaintenanceWindows">DeferMaintenanceWindows</a>: <i>
      - String</i>
    <a href="#snapshotidentifier" title="SnapshotIdentifier">SnapshotIdentifier</a>: <i>String</i>
    <a href="#force" title="Force">Force</a>: <i>Boolean</i>
    <a href="#accountswithrestoreaccess" title="AccountsWithRestoreAccess">AccountsWithRestoreAccess</a>: <i>
      - String</i>
    <a href="#actualincrementalbackupsizeinmegabytes" title="ActualIncrementalBackupSizeInMegaBytes">ActualIncrementalBackupSizeInMegaBytes</a>: <i>Double</i>
    <a href="#backupprogressinmegabytes" title="BackupProgressInMegaBytes">BackupProgressInMegaBytes</a>: <i>Double</i>
    <a href="#clustercreatetime" title="ClusterCreateTime">ClusterCreateTime</a>: <i>String</i>
    <a href="#currentbackuprateinmegabytespersecond" title="CurrentBackupRateInMegaBytesPerSecond">CurrentBackupRateInMegaBytesPerSecond</a>: <i>Double</i>
    <a href="#elapsedtimeinseconds" title="ElapsedTimeInSeconds">ElapsedTimeInSeconds</a>: <i>Double</i>
    <a href="#encryptedwithhsm" title="EncryptedWithHSM">EncryptedWithHSM</a>: <i>Boolean</i>
    <a href="#estimatedsecondstocompletion" title="EstimatedSecondsToCompletion">EstimatedSecondsToCompletion</a>: <i>Double</i>
    <a href="#manualsnapshotremainingdays" title="ManualSnapshotRemainingDays">ManualSnapshotRemainingDays</a>: <i>Integer</i>
    <a href="#owneraccount" title="OwnerAccount">OwnerAccount</a>: <i>String</i>
    <a href="#restorablenodetypes" title="RestorableNodeTypes">RestorableNodeTypes</a>: <i>
      - String</i>
    <a href="#snapshotcreatetime" title="SnapshotCreateTime">SnapshotCreateTime</a>: <i>String</i>
    <a href="#snapshotretentionstarttime" title="SnapshotRetentionStartTime">SnapshotRetentionStartTime</a>: <i>String</i>
    <a href="#snapshottype" title="SnapshotType">SnapshotType</a>: <i>String</i>
    <a href="#sourceregion" title="SourceRegion">SourceRegion</a>: <i>String</i>
    <a href="#status" title="Status">Status</a>: <i>String</i>
    <a href="#totalbackupsizeinmegabytes" title="TotalBackupSizeInMegaBytes">TotalBackupSizeInMegaBytes</a>: <i>Double</i>
    <a href="#vpcid" title="VpcId">VpcId</a>: <i>String</i>
    <a href="#starttime" title="StartTime">StartTime</a>: <i>String</i>
    <a href="#endtime" title="EndTime">EndTime</a>: <i>String</i>
    <a href="#tagkeys" title="TagKeys">TagKeys</a>: <i>
      - String</i>
    <a href="#tagvalues" title="TagValues">TagValues</a>: <i>
      - String</i>
    <a href="#clusterexists" title="ClusterExists">ClusterExists</a>: <i>Boolean</i>
</pre>

## Properties

#### MasterUsername

The user name associated with the master user account for the cluster that is being created. The user name can't be PUBLIC and first character must be a letter.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### MasterUserPassword

The password associated with the master user account for the cluster that is being created. Password must be between 8 and 64 characters in length, should have at least one uppercase letter.Must contain at least one lowercase letter.Must contain one number.Can be any printable ASCII character.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### NodeType

The node type to be provisioned for the cluster.Valid Values: ds2.xlarge | ds2.8xlarge | dc1.large | dc1.8xlarge | dc2.large | dc2.8xlarge | ra3.4xlarge | ra3.16xlarge

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### AdditionalInfo

Reserved

_Required_: No

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

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ClusterType

The type of the cluster. When cluster type is specified as single-node, the NumberOfNodes parameter is not required and if multi-node, the NumberOfNodes parameter is required

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ClusterVersion

The version of the Amazon Redshift engine software that you want to deploy on the cluster.The version selected runs on all the nodes in the cluster.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### DBName

The name of the first database to be created when the cluster is created. To create additional databases after the cluster is created, connect to the cluster with a SQL client and use SQL commands to create a database.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

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

#### EnhancedVpcRouting

An option that specifies whether to create the cluster with enhanced VPC routing enabled. To create a cluster that uses enhanced VPC routing, the cluster must be in a VPC.

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

#### MaintenanceTrackName

An optional parameter for the name of the maintenance track for the cluster. If you don't provide a maintenance track name, the cluster is assigned to the current track.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ManualSnapshotRetentionPeriod

The default number of days to retain a manual snapshot. If the value is -1, the snapshot is retained indefinitely. This setting doesn't change the retention period of existing snapshots.

_Required_: No

_Type_: Integer

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

#### SnapshotScheduleIdentifier

A unique identifier for the snapshot schedule.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ClusterSecurityGroups

A list of security groups to be associated with this cluster.

_Required_: No

_Type_: List of String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### IamRoles

A list of AWS Identity and Access Management (IAM) roles that can be used by the cluster to access other AWS services. You must supply the IAM roles in their Amazon Resource Name (ARN) format. You can supply up to 10 IAM roles in a single request

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

#### FinalClusterSnapshotIdentifier

The identifier of the final snapshot that is to be created immediately before deleting the cluster

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### FinalClusterSnapshotRetentionPeriod

The number of days that a manual snapshot is retained. If the value is -1, the manual snapshot is retained indefinitely.

_Required_: No

_Type_: Integer

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### SkipFinalClusterSnapshot

Determines whether a final snapshot of the cluster is created before Amazon Redshift deletes the cluster. If true, a final cluster snapshot is not created. If false, a final cluster snapshot is created before the cluster is deleted.

_Required_: No

_Type_: Boolean

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Marker

An optional parameter that specifies the starting point to return a set of response records. When the results of a DescribeClusters request exceed the value specified in MaxRecords, AWS returns a value in the Marker field of the response

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### MaxRecords

The maximum number of response records to return in each call. If the number of remaining response records exceeds the specified MaxRecords value, a value is returned in a marker field of the response.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### NewClusterIdentifier

The new identifier for the cluster.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### AddIamRoles

Zero or more IAM roles to associate with the cluster. The roles must be in their Amazon Resource Name (ARN) format

_Required_: No

_Type_: List of String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### RemoveIamRoles

Zero or more IAM roles in ARN format to disassociate from the cluster. You can disassociate up to 10 IAM roles from a single cluster in a single request

_Required_: No

_Type_: List of String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### RedshiftCommand

The redshift API command

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### CurrentDatabaseRevision

A string representing the current cluster version.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### DatabaseRevisionReleaseDate

The date on which the database revision was released.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### RevisionTarget

The identifier of the database revision. You can retrieve this value from the response to the DescribeClusterDbRevisions request.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### RevisionTargets

A list of RevisionTarget objects, where each object describes the database revision that a cluster can be updated to.

_Required_: No

_Type_: List of String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### DeferMaintenance

A boolean indicating whether to enable the deferred maintenance window.

_Required_: No

_Type_: Boolean

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### DeferMaintenanceDuration

An integer indicating the duration of the maintenance window in days. If you specify a duration, you can't specify an end time. The duration must be 45 days or less.

_Required_: No

_Type_: Integer

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### DeferMaintenanceEndTime

A timestamp indicating end time for the deferred maintenance window. If you specify an end time, you can't specify a duration.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### DeferMaintenanceStartTime

A timestamp indicating start time for the deferred maintenance window.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### DeferMaintenanceIdentifier

A unique identifier for the deferred maintenance window.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### DeferMaintenanceWindows

A list of Deferred maintenance windows.

_Required_: No

_Type_: List of String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### SnapshotIdentifier

The identifier of the snapshot whose setting you want to modify.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Force

A Boolean option to override an exception if the retention period has already passed.

_Required_: No

_Type_: Boolean

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### AccountsWithRestoreAccess

A list of the AWS customer accounts authorized to restore the snapshot. Returns null if no accounts are authorized. Visible only to the snapshot owner.

_Required_: No

_Type_: List of String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ActualIncrementalBackupSizeInMegaBytes

The size of the incremental backup.

_Required_: No

_Type_: Double

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### BackupProgressInMegaBytes

The number of megabytes that have been transferred to the snapshot backup.

_Required_: No

_Type_: Double

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ClusterCreateTime

The time (UTC) when the cluster was originally created.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### CurrentBackupRateInMegaBytesPerSecond

The number of megabytes per second being transferred to the snapshot backup. Returns 0 for a completed backup.

_Required_: No

_Type_: Double

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ElapsedTimeInSeconds

The amount of time an in-progress snapshot backup has been running, or the amount of time it took a completed backup to finish.

_Required_: No

_Type_: Double

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### EncryptedWithHSM

A boolean that indicates whether the snapshot data is encrypted using the HSM keys of the source cluster. true indicates that the data is encrypted using HSM keys.

_Required_: No

_Type_: Boolean

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### EstimatedSecondsToCompletion

The estimate of the time remaining before the snapshot backup will complete. Returns 0 for a completed backup.

_Required_: No

_Type_: Double

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ManualSnapshotRemainingDays

The number of days until a manual snapshot will pass its retention period.

_Required_: No

_Type_: Integer

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### OwnerAccount

For manual snapshots, the AWS customer account used to create or copy the snapshot. For automatic snapshots, the owner of the cluster. The owner can perform all snapshot actions, such as sharing a manual snapshot.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### RestorableNodeTypes

The list of node types that this cluster snapshot is able to restore into.

_Required_: No

_Type_: List of String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### SnapshotCreateTime

The time (in UTC format) when Amazon Redshift began the snapshot. A snapshot contains a copy of the cluster data as of this exact time.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### SnapshotRetentionStartTime

A timestamp representing the start of the retention period for the snapshot.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### SnapshotType

The snapshot type. Snapshots created using CreateClusterSnapshot and CopyClusterSnapshot are of type "manual".

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### SourceRegion

The source region from which the snapshot was copied.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Status

The snapshot status. The value of the status depends on the API operation used.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### TotalBackupSizeInMegaBytes

The size of the complete set of backup data that would be used to restore the cluster.

_Required_: No

_Type_: Double

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### VpcId

The VPC identifier of the cluster if the snapshot is from a cluster in a VPC. Otherwise, this field is not in the output.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### StartTime

A value that requests only snapshots created at or after the specified time. The time value is specified in ISO 8601 format

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### EndTime

A value that requests only snapshots created at or before the specified time. The time value is specified in ISO 8601 format

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### TagKeys

A tag key or keys for which you want to return all matching cluster snapshots that are associated with the specified key or keys.

_Required_: No

_Type_: List of String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### TagValues

A tag value or values for which you want to return all matching cluster snapshots that are associated with the specified tag value or values.

_Required_: No

_Type_: List of String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ClusterExists

A value that indicates whether to return snapshots only for an existing cluster.

_Required_: No

_Type_: Boolean

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

## Return Values

### Ref

When you pass the logical ID of this resource to the intrinsic `Ref` function, Ref returns the ClusterIdentifier.

### Fn::GetAtt

The `Fn::GetAtt` intrinsic function returns a value for a specified attribute of this type. The following are the available attributes and sample return values.

For more information about using the `Fn::GetAtt` intrinsic function, see [Fn::GetAtt](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-getatt.html).

#### ClusterIdentifier

A unique identifier for the cluster. You use this identifier to refer to the cluster for any subsequent cluster operations such as deleting or modifying. All alphabetical characters must be lower case, no hypens at the end, no two consecutive hyphens. Cluster name should be unique for all clusters within an AWS account

