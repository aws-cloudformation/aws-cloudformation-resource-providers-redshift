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
        "<a href="#elasticipstatus" title="ElasticIpStatus">ElasticIpStatus</a>" : <i>String</i>,
        "<a href="#elasticresizenumberofnodeoptions" title="ElasticResizeNumberOfNodeOptions">ElasticResizeNumberOfNodeOptions</a>" : <i>String</i>,
        "<a href="#endpointaddress" title="EndpointAddress">EndpointAddress</a>" : <i>String</i>,
        "<a href="#endpointport" title="EndpointPort">EndpointPort</a>" : <i>Integer</i>,
        "<a href="#encrypted" title="Encrypted">Encrypted</a>" : <i>Boolean</i>,
        "<a href="#enhancedvpcrouting" title="EnhancedVpcRouting">EnhancedVpcRouting</a>" : <i>Boolean</i>,
        "<a href="#expectednextsnapshotscheduletime" title="ExpectedNextSnapshotScheduleTime">ExpectedNextSnapshotScheduleTime</a>" : <i>String</i>,
        "<a href="#expectednextsnapshotscheduletimestatus" title="ExpectedNextSnapshotScheduleTimeStatus">ExpectedNextSnapshotScheduleTimeStatus</a>" : <i>String</i>,
        "<a href="#hsmstatus" title="HsmStatus">HsmStatus</a>" : <i>String</i>,
        "<a href="#hsmclientcertificateidentifier" title="HsmClientCertificateIdentifier">HsmClientCertificateIdentifier</a>" : <i>String</i>,
        "<a href="#hsmconfigurationidentifier" title="HsmConfigurationIdentifier">HsmConfigurationIdentifier</a>" : <i>String</i>,
        "<a href="#kmskeyid" title="KmsKeyId">KmsKeyId</a>" : <i>String</i>,
        "<a href="#maintenancetrackname" title="MaintenanceTrackName">MaintenanceTrackName</a>" : <i>String</i>,
        "<a href="#manualsnapshotretentionperiod" title="ManualSnapshotRetentionPeriod">ManualSnapshotRetentionPeriod</a>" : <i>Integer</i>,
        "<a href="#numberofnodes" title="NumberOfNodes">NumberOfNodes</a>" : <i>Integer</i>,
        "<a href="#port" title="Port">Port</a>" : <i>Integer</i>,
        "<a href="#preferredmaintenancewindow" title="PreferredMaintenanceWindow">PreferredMaintenanceWindow</a>" : <i>String</i>,
        "<a href="#publiclyaccessible" title="PubliclyAccessible">PubliclyAccessible</a>" : <i>Boolean</i>,
        "<a href="#allowcancelresize" title="AllowCancelResize">AllowCancelResize</a>" : <i>Boolean</i>,
        "<a href="#resizetype" title="ResizeType">ResizeType</a>" : <i>String</i>,
        "<a href="#currentrestorerateinmegabytespersecond" title="CurrentRestoreRateInMegaBytesPerSecond">CurrentRestoreRateInMegaBytesPerSecond</a>" : <i>Double</i>,
        "<a href="#restoreprogressinmegabytes" title="RestoreProgressInMegaBytes">RestoreProgressInMegaBytes</a>" : <i>Double</i>,
        "<a href="#restoreelapsedtimeinseconds" title="RestoreElapsedTimeInSeconds">RestoreElapsedTimeInSeconds</a>" : <i>Double</i>,
        "<a href="#restoreestimatedtimetocompletioninseconds" title="RestoreEstimatedTimeToCompletionInSeconds">RestoreEstimatedTimeToCompletionInSeconds</a>" : <i>Double</i>,
        "<a href="#restoresnapshotsizeinmegabytes" title="RestoreSnapshotSizeInMegaBytes">RestoreSnapshotSizeInMegaBytes</a>" : <i>Double</i>,
        "<a href="#restorestatus" title="RestoreStatus">RestoreStatus</a>" : <i>String</i>,
        "<a href="#snapshotscheduleidentifier" title="SnapshotScheduleIdentifier">SnapshotScheduleIdentifier</a>" : <i>String</i>,
        "<a href="#snapshotschedulestate" title="SnapshotScheduleState">SnapshotScheduleState</a>" : <i>String</i>,
        "<a href="#clustersecuritygroups" title="ClusterSecurityGroups">ClusterSecurityGroups</a>" : <i>[ String, ... ]</i>,
        "<a href="#iamroles" title="IamRoles">IamRoles</a>" : <i>[ String, ... ]</i>,
        "<a href="#iamrolearn" title="IamRoleArn">IamRoleArn</a>" : <i>String</i>,
        "<a href="#iamroleapplystatus" title="IamRoleApplyStatus">IamRoleApplyStatus</a>" : <i>String</i>,
        "<a href="#modifystatus" title="ModifyStatus">ModifyStatus</a>" : <i>String</i>,
        "<a href="#nextmaintenancewindowstarttime" title="NextMaintenanceWindowStartTime">NextMaintenanceWindowStartTime</a>" : <i>String</i>,
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
        "<a href="#clusterexists" title="ClusterExists">ClusterExists</a>" : <i>Boolean</i>,
        "<a href="#retentionperiod" title="RetentionPeriod">RetentionPeriod</a>" : <i>Integer</i>,
        "<a href="#manual" title="Manual">Manual</a>" : <i>Boolean</i>,
        "<a href="#clusteravailabilitystatus" title="ClusterAvailabilityStatus">ClusterAvailabilityStatus</a>" : <i>String</i>,
        "<a href="#clusternamespacearn" title="ClusterNamespaceArn">ClusterNamespaceArn</a>" : <i>String</i>,
        "<a href="#clusternodes" title="ClusterNodes">ClusterNodes</a>" : <i>[ String, ... ]</i>,
        "<a href="#clusterparametergroups" title="ClusterParameterGroups">ClusterParameterGroups</a>" : <i>[ String, ... ]</i>,
        "<a href="#clusterpublickey" title="ClusterPublicKey">ClusterPublicKey</a>" : <i>String</i>,
        "<a href="#clusterrevisionnumber" title="ClusterRevisionNumber">ClusterRevisionNumber</a>" : <i>String</i>,
        "<a href="#clusternoderole" title="ClusterNodeRole">ClusterNodeRole</a>" : <i>[ String, ... ]</i>,
        "<a href="#clusternodeprivateipaddress" title="ClusterNodePrivateIPAddress">ClusterNodePrivateIPAddress</a>" : <i>[ String, ... ]</i>,
        "<a href="#clusternodepublicipaddress" title="ClusterNodePublicIPAddress">ClusterNodePublicIPAddress</a>" : <i>[ String, ... ]</i>,
        "<a href="#destinationregion" title="DestinationRegion">DestinationRegion</a>" : <i>String</i>,
        "<a href="#snapshotcopygrantname" title="SnapshotCopyGrantName">SnapshotCopyGrantName</a>" : <i>String</i>,
        "<a href="#clusterstatus" title="ClusterStatus">ClusterStatus</a>" : <i>String</i>,
        "<a href="#clustersubnetgroupname" title="ClusterSubnetGroupName">ClusterSubnetGroupName</a>" : <i>String</i>,
        "<a href="#currentrateinmegabytespersecond" title="CurrentRateInMegaBytesPerSecond">CurrentRateInMegaBytesPerSecond</a>" : <i>Double</i>,
        "<a href="#datatransferredinmegabytes" title="DataTransferredInMegaBytes">DataTransferredInMegaBytes</a>" : <i>Double</i>,
        "<a href="#datatransferprogresselapsedtimeinseconds" title="DataTransferProgressElapsedTimeInSeconds">DataTransferProgressElapsedTimeInSeconds</a>" : <i>Double</i>,
        "<a href="#estimatedtimetocompletioninseconds" title="EstimatedTimeToCompletionInSeconds">EstimatedTimeToCompletionInSeconds</a>" : <i>Double</i>,
        "<a href="#datatransferprogressstatus" title="DataTransferProgressStatus">DataTransferProgressStatus</a>" : <i>String</i>,
        "<a href="#totaldatainmegabytes" title="TotalDataInMegaBytes">TotalDataInMegaBytes</a>" : <i>Double</i>,
        "<a href="#bucketname" title="BucketName">BucketName</a>" : <i>String</i>,
        "<a href="#s3prefix" title="S3Prefix">S3Prefix</a>" : <i>String</i>,
        "<a href="#classic" title="Classic">Classic</a>" : <i>Boolean</i>,
        "<a href="#snapshotclusteridentifier" title="SnapshotClusterIdentifier">SnapshotClusterIdentifier</a>" : <i>String</i>,
        "<a href="#newtablename" title="NewTableName">NewTableName</a>" : <i>String</i>,
        "<a href="#sourcedatabasename" title="SourceDatabaseName">SourceDatabaseName</a>" : <i>String</i>,
        "<a href="#sourcetablename" title="SourceTableName">SourceTableName</a>" : <i>String</i>,
        "<a href="#sourceschemaname" title="SourceSchemaName">SourceSchemaName</a>" : <i>String</i>,
        "<a href="#targetdatabasename" title="TargetDatabaseName">TargetDatabaseName</a>" : <i>String</i>,
        "<a href="#targetschemaname" title="TargetSchemaName">TargetSchemaName</a>" : <i>String</i>,
        "<a href="#tablerestorestatusmessage" title="TableRestoreStatusMessage">TableRestoreStatusMessage</a>" : <i>String</i>,
        "<a href="#tablerestorestatusprogressinmegabytes" title="TableRestoreStatusProgressInMegaBytes">TableRestoreStatusProgressInMegaBytes</a>" : <i>Double</i>,
        "<a href="#tablerestorestatusrequesttime" title="TableRestoreStatusRequestTime">TableRestoreStatusRequestTime</a>" : <i>String</i>,
        "<a href="#tablerestorestatusstatus" title="TableRestoreStatusStatus">TableRestoreStatusStatus</a>" : <i>String</i>,
        "<a href="#tablerestorerequestid" title="TableRestoreRequestId">TableRestoreRequestId</a>" : <i>String</i>,
        "<a href="#tablerestorestatustotaldatainmegabytes" title="TableRestoreStatusTotalDataInMegaBytes">TableRestoreStatusTotalDataInMegaBytes</a>" : <i>Double</i>,
        "<a href="#lastfailuremessage" title="LastFailureMessage">LastFailureMessage</a>" : <i>String</i>,
        "<a href="#lastfailuretime" title="LastFailureTime">LastFailureTime</a>" : <i>String</i>,
        "<a href="#lastsuccessfuldeliverytime" title="LastSuccessfulDeliveryTime">LastSuccessfulDeliveryTime</a>" : <i>String</i>,
        "<a href="#loggingenabled" title="LoggingEnabled">LoggingEnabled</a>" : <i>Boolean</i>,
        "<a href="#s3keyprefix" title="S3KeyPrefix">S3KeyPrefix</a>" : <i>String</i>,
        "<a href="#amount" title="Amount">Amount</a>" : <i>Double</i>,
        "<a href="#featuretype" title="FeatureType">FeatureType</a>" : <i>String</i>,
        "<a href="#limittype" title="LimitType">LimitType</a>" : <i>String</i>,
        "<a href="#breachaction" title="BreachAction">BreachAction</a>" : <i>String</i>,
        "<a href="#period" title="Period">Period</a>" : <i>String</i>,
        "<a href="#usagelimitid" title="UsageLimitId">UsageLimitId</a>" : <i>String</i>,
        "<a href="#avgresizerateinmegabytespersecond" title="AvgResizeRateInMegaBytesPerSecond">AvgResizeRateInMegaBytesPerSecond</a>" : <i>Double</i>,
        "<a href="#datatransferprogresspercent" title="DataTransferProgressPercent">DataTransferProgressPercent</a>" : <i>Double</i>,
        "<a href="#cancelresizeelapsedtimeinseconds" title="CancelResizeElapsedTimeInSeconds">CancelResizeElapsedTimeInSeconds</a>" : <i>Double</i>,
        "<a href="#cancelresizeestimatedtimetocompletioninseconds" title="CancelResizeEstimatedTimeToCompletionInSeconds">CancelResizeEstimatedTimeToCompletionInSeconds</a>" : <i>Double</i>,
        "<a href="#importtablescompleted" title="ImportTablesCompleted">ImportTablesCompleted</a>" : <i>[ String, ... ]</i>,
        "<a href="#importtablesinprogress" title="ImportTablesInProgress">ImportTablesInProgress</a>" : <i>[ String, ... ]</i>,
        "<a href="#importtablesnotstarted" title="ImportTablesNotStarted">ImportTablesNotStarted</a>" : <i>[ String, ... ]</i>,
        "<a href="#cancelresizemessage" title="CancelResizeMessage">CancelResizeMessage</a>" : <i>String</i>,
        "<a href="#progressinmegabytes" title="ProgressInMegaBytes">ProgressInMegaBytes</a>" : <i>Double</i>,
        "<a href="#cancelresizestatus" title="CancelResizeStatus">CancelResizeStatus</a>" : <i>String</i>,
        "<a href="#targetclustertype" title="TargetClusterType">TargetClusterType</a>" : <i>String</i>,
        "<a href="#targetencryptiontype" title="TargetEncryptionType">TargetEncryptionType</a>" : <i>String</i>,
        "<a href="#targetnodetype" title="TargetNodeType">TargetNodeType</a>" : <i>String</i>,
        "<a href="#targetnumberofnodes" title="TargetNumberOfNodes">TargetNumberOfNodes</a>" : <i>Integer</i>,
        "<a href="#totalresizedatainmegabytes" title="TotalResizeDataInMegaBytes">TotalResizeDataInMegaBytes</a>" : <i>Double</i>
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
    <a href="#elasticipstatus" title="ElasticIpStatus">ElasticIpStatus</a>: <i>String</i>
    <a href="#elasticresizenumberofnodeoptions" title="ElasticResizeNumberOfNodeOptions">ElasticResizeNumberOfNodeOptions</a>: <i>String</i>
    <a href="#endpointaddress" title="EndpointAddress">EndpointAddress</a>: <i>String</i>
    <a href="#endpointport" title="EndpointPort">EndpointPort</a>: <i>Integer</i>
    <a href="#encrypted" title="Encrypted">Encrypted</a>: <i>Boolean</i>
    <a href="#enhancedvpcrouting" title="EnhancedVpcRouting">EnhancedVpcRouting</a>: <i>Boolean</i>
    <a href="#expectednextsnapshotscheduletime" title="ExpectedNextSnapshotScheduleTime">ExpectedNextSnapshotScheduleTime</a>: <i>String</i>
    <a href="#expectednextsnapshotscheduletimestatus" title="ExpectedNextSnapshotScheduleTimeStatus">ExpectedNextSnapshotScheduleTimeStatus</a>: <i>String</i>
    <a href="#hsmstatus" title="HsmStatus">HsmStatus</a>: <i>String</i>
    <a href="#hsmclientcertificateidentifier" title="HsmClientCertificateIdentifier">HsmClientCertificateIdentifier</a>: <i>String</i>
    <a href="#hsmconfigurationidentifier" title="HsmConfigurationIdentifier">HsmConfigurationIdentifier</a>: <i>String</i>
    <a href="#kmskeyid" title="KmsKeyId">KmsKeyId</a>: <i>String</i>
    <a href="#maintenancetrackname" title="MaintenanceTrackName">MaintenanceTrackName</a>: <i>String</i>
    <a href="#manualsnapshotretentionperiod" title="ManualSnapshotRetentionPeriod">ManualSnapshotRetentionPeriod</a>: <i>Integer</i>
    <a href="#numberofnodes" title="NumberOfNodes">NumberOfNodes</a>: <i>Integer</i>
    <a href="#port" title="Port">Port</a>: <i>Integer</i>
    <a href="#preferredmaintenancewindow" title="PreferredMaintenanceWindow">PreferredMaintenanceWindow</a>: <i>String</i>
    <a href="#publiclyaccessible" title="PubliclyAccessible">PubliclyAccessible</a>: <i>Boolean</i>
    <a href="#allowcancelresize" title="AllowCancelResize">AllowCancelResize</a>: <i>Boolean</i>
    <a href="#resizetype" title="ResizeType">ResizeType</a>: <i>String</i>
    <a href="#currentrestorerateinmegabytespersecond" title="CurrentRestoreRateInMegaBytesPerSecond">CurrentRestoreRateInMegaBytesPerSecond</a>: <i>Double</i>
    <a href="#restoreprogressinmegabytes" title="RestoreProgressInMegaBytes">RestoreProgressInMegaBytes</a>: <i>Double</i>
    <a href="#restoreelapsedtimeinseconds" title="RestoreElapsedTimeInSeconds">RestoreElapsedTimeInSeconds</a>: <i>Double</i>
    <a href="#restoreestimatedtimetocompletioninseconds" title="RestoreEstimatedTimeToCompletionInSeconds">RestoreEstimatedTimeToCompletionInSeconds</a>: <i>Double</i>
    <a href="#restoresnapshotsizeinmegabytes" title="RestoreSnapshotSizeInMegaBytes">RestoreSnapshotSizeInMegaBytes</a>: <i>Double</i>
    <a href="#restorestatus" title="RestoreStatus">RestoreStatus</a>: <i>String</i>
    <a href="#snapshotscheduleidentifier" title="SnapshotScheduleIdentifier">SnapshotScheduleIdentifier</a>: <i>String</i>
    <a href="#snapshotschedulestate" title="SnapshotScheduleState">SnapshotScheduleState</a>: <i>String</i>
    <a href="#clustersecuritygroups" title="ClusterSecurityGroups">ClusterSecurityGroups</a>: <i>
      - String</i>
    <a href="#iamroles" title="IamRoles">IamRoles</a>: <i>
      - String</i>
    <a href="#iamrolearn" title="IamRoleArn">IamRoleArn</a>: <i>String</i>
    <a href="#iamroleapplystatus" title="IamRoleApplyStatus">IamRoleApplyStatus</a>: <i>String</i>
    <a href="#modifystatus" title="ModifyStatus">ModifyStatus</a>: <i>String</i>
    <a href="#nextmaintenancewindowstarttime" title="NextMaintenanceWindowStartTime">NextMaintenanceWindowStartTime</a>: <i>String</i>
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
    <a href="#retentionperiod" title="RetentionPeriod">RetentionPeriod</a>: <i>Integer</i>
    <a href="#manual" title="Manual">Manual</a>: <i>Boolean</i>
    <a href="#clusteravailabilitystatus" title="ClusterAvailabilityStatus">ClusterAvailabilityStatus</a>: <i>String</i>
    <a href="#clusternamespacearn" title="ClusterNamespaceArn">ClusterNamespaceArn</a>: <i>String</i>
    <a href="#clusternodes" title="ClusterNodes">ClusterNodes</a>: <i>
      - String</i>
    <a href="#clusterparametergroups" title="ClusterParameterGroups">ClusterParameterGroups</a>: <i>
      - String</i>
    <a href="#clusterpublickey" title="ClusterPublicKey">ClusterPublicKey</a>: <i>String</i>
    <a href="#clusterrevisionnumber" title="ClusterRevisionNumber">ClusterRevisionNumber</a>: <i>String</i>
    <a href="#clusternoderole" title="ClusterNodeRole">ClusterNodeRole</a>: <i>
      - String</i>
    <a href="#clusternodeprivateipaddress" title="ClusterNodePrivateIPAddress">ClusterNodePrivateIPAddress</a>: <i>
      - String</i>
    <a href="#clusternodepublicipaddress" title="ClusterNodePublicIPAddress">ClusterNodePublicIPAddress</a>: <i>
      - String</i>
    <a href="#destinationregion" title="DestinationRegion">DestinationRegion</a>: <i>String</i>
    <a href="#snapshotcopygrantname" title="SnapshotCopyGrantName">SnapshotCopyGrantName</a>: <i>String</i>
    <a href="#clusterstatus" title="ClusterStatus">ClusterStatus</a>: <i>String</i>
    <a href="#clustersubnetgroupname" title="ClusterSubnetGroupName">ClusterSubnetGroupName</a>: <i>String</i>
    <a href="#currentrateinmegabytespersecond" title="CurrentRateInMegaBytesPerSecond">CurrentRateInMegaBytesPerSecond</a>: <i>Double</i>
    <a href="#datatransferredinmegabytes" title="DataTransferredInMegaBytes">DataTransferredInMegaBytes</a>: <i>Double</i>
    <a href="#datatransferprogresselapsedtimeinseconds" title="DataTransferProgressElapsedTimeInSeconds">DataTransferProgressElapsedTimeInSeconds</a>: <i>Double</i>
    <a href="#estimatedtimetocompletioninseconds" title="EstimatedTimeToCompletionInSeconds">EstimatedTimeToCompletionInSeconds</a>: <i>Double</i>
    <a href="#datatransferprogressstatus" title="DataTransferProgressStatus">DataTransferProgressStatus</a>: <i>String</i>
    <a href="#totaldatainmegabytes" title="TotalDataInMegaBytes">TotalDataInMegaBytes</a>: <i>Double</i>
    <a href="#bucketname" title="BucketName">BucketName</a>: <i>String</i>
    <a href="#s3prefix" title="S3Prefix">S3Prefix</a>: <i>String</i>
    <a href="#classic" title="Classic">Classic</a>: <i>Boolean</i>
    <a href="#snapshotclusteridentifier" title="SnapshotClusterIdentifier">SnapshotClusterIdentifier</a>: <i>String</i>
    <a href="#newtablename" title="NewTableName">NewTableName</a>: <i>String</i>
    <a href="#sourcedatabasename" title="SourceDatabaseName">SourceDatabaseName</a>: <i>String</i>
    <a href="#sourcetablename" title="SourceTableName">SourceTableName</a>: <i>String</i>
    <a href="#sourceschemaname" title="SourceSchemaName">SourceSchemaName</a>: <i>String</i>
    <a href="#targetdatabasename" title="TargetDatabaseName">TargetDatabaseName</a>: <i>String</i>
    <a href="#targetschemaname" title="TargetSchemaName">TargetSchemaName</a>: <i>String</i>
    <a href="#tablerestorestatusmessage" title="TableRestoreStatusMessage">TableRestoreStatusMessage</a>: <i>String</i>
    <a href="#tablerestorestatusprogressinmegabytes" title="TableRestoreStatusProgressInMegaBytes">TableRestoreStatusProgressInMegaBytes</a>: <i>Double</i>
    <a href="#tablerestorestatusrequesttime" title="TableRestoreStatusRequestTime">TableRestoreStatusRequestTime</a>: <i>String</i>
    <a href="#tablerestorestatusstatus" title="TableRestoreStatusStatus">TableRestoreStatusStatus</a>: <i>String</i>
    <a href="#tablerestorerequestid" title="TableRestoreRequestId">TableRestoreRequestId</a>: <i>String</i>
    <a href="#tablerestorestatustotaldatainmegabytes" title="TableRestoreStatusTotalDataInMegaBytes">TableRestoreStatusTotalDataInMegaBytes</a>: <i>Double</i>
    <a href="#lastfailuremessage" title="LastFailureMessage">LastFailureMessage</a>: <i>String</i>
    <a href="#lastfailuretime" title="LastFailureTime">LastFailureTime</a>: <i>String</i>
    <a href="#lastsuccessfuldeliverytime" title="LastSuccessfulDeliveryTime">LastSuccessfulDeliveryTime</a>: <i>String</i>
    <a href="#loggingenabled" title="LoggingEnabled">LoggingEnabled</a>: <i>Boolean</i>
    <a href="#s3keyprefix" title="S3KeyPrefix">S3KeyPrefix</a>: <i>String</i>
    <a href="#amount" title="Amount">Amount</a>: <i>Double</i>
    <a href="#featuretype" title="FeatureType">FeatureType</a>: <i>String</i>
    <a href="#limittype" title="LimitType">LimitType</a>: <i>String</i>
    <a href="#breachaction" title="BreachAction">BreachAction</a>: <i>String</i>
    <a href="#period" title="Period">Period</a>: <i>String</i>
    <a href="#usagelimitid" title="UsageLimitId">UsageLimitId</a>: <i>String</i>
    <a href="#avgresizerateinmegabytespersecond" title="AvgResizeRateInMegaBytesPerSecond">AvgResizeRateInMegaBytesPerSecond</a>: <i>Double</i>
    <a href="#datatransferprogresspercent" title="DataTransferProgressPercent">DataTransferProgressPercent</a>: <i>Double</i>
    <a href="#cancelresizeelapsedtimeinseconds" title="CancelResizeElapsedTimeInSeconds">CancelResizeElapsedTimeInSeconds</a>: <i>Double</i>
    <a href="#cancelresizeestimatedtimetocompletioninseconds" title="CancelResizeEstimatedTimeToCompletionInSeconds">CancelResizeEstimatedTimeToCompletionInSeconds</a>: <i>Double</i>
    <a href="#importtablescompleted" title="ImportTablesCompleted">ImportTablesCompleted</a>: <i>
      - String</i>
    <a href="#importtablesinprogress" title="ImportTablesInProgress">ImportTablesInProgress</a>: <i>
      - String</i>
    <a href="#importtablesnotstarted" title="ImportTablesNotStarted">ImportTablesNotStarted</a>: <i>
      - String</i>
    <a href="#cancelresizemessage" title="CancelResizeMessage">CancelResizeMessage</a>: <i>String</i>
    <a href="#progressinmegabytes" title="ProgressInMegaBytes">ProgressInMegaBytes</a>: <i>Double</i>
    <a href="#cancelresizestatus" title="CancelResizeStatus">CancelResizeStatus</a>: <i>String</i>
    <a href="#targetclustertype" title="TargetClusterType">TargetClusterType</a>: <i>String</i>
    <a href="#targetencryptiontype" title="TargetEncryptionType">TargetEncryptionType</a>: <i>String</i>
    <a href="#targetnodetype" title="TargetNodeType">TargetNodeType</a>: <i>String</i>
    <a href="#targetnumberofnodes" title="TargetNumberOfNodes">TargetNumberOfNodes</a>: <i>Integer</i>
    <a href="#totalresizedatainmegabytes" title="TotalResizeDataInMegaBytes">TotalResizeDataInMegaBytes</a>: <i>Double</i>
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

#### ElasticIpStatus

The status of the elastic IP (EIP) address.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ElasticResizeNumberOfNodeOptions

The number of nodes that you can resize the cluster to with the elastic resize method.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### EndpointAddress

The DNS address of the Cluster.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### EndpointPort

The DNS address of the Port.

_Required_: No

_Type_: Integer

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

#### ExpectedNextSnapshotScheduleTime

The date and time when the next snapshot is expected to be taken for clusters with a valid snapshot schedule and backups enabled.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ExpectedNextSnapshotScheduleTimeStatus

The status of next expected snapshot for clusters having a valid snapshot schedule and backups enabled. Possible values are the following OnTrack, Pending

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### HsmStatus

A value that reports whether the Amazon Redshift cluster has finished applying any hardware security module (HSM) settings changes specified in a modify cluster command. Values active, applying

_Required_: No

_Type_: String

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

#### AllowCancelResize

A boolean value indicating if the resize operation can be cancelled.

_Required_: No

_Type_: Boolean

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ResizeType

Returns the value ClassicResize.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### CurrentRestoreRateInMegaBytesPerSecond

The number of megabytes per second being transferred from the backup storage. Returns the average rate for a completed backup. This field is only updated when you restore to DC2 and DS2 node types.

_Required_: No

_Type_: Double

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### RestoreProgressInMegaBytes

The number of megabytes that have been transferred from snapshot storage. This field is only updated when you restore to DC2 and DS2 node types.

_Required_: No

_Type_: Double

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### RestoreElapsedTimeInSeconds

The amount of time an in-progress restore has been running, or the amount of time it took a completed restore to finish. This field is only updated when you restore to DC2 and DS2 node types.

_Required_: No

_Type_: Double

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### RestoreEstimatedTimeToCompletionInSeconds

The estimate of the time remaining before the restore will complete. Returns 0 for a completed restore. This field is only updated when you restore to DC2 and DS2 node types.

_Required_: No

_Type_: Double

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### RestoreSnapshotSizeInMegaBytes

The size of the set of snapshot data used to restore the cluster. This field is only updated when you restore to DC2 and DS2 node types..

_Required_: No

_Type_: Double

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### RestoreStatus

 The status of the restore action. Returns starting, restoring, completed, or failed.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### SnapshotScheduleIdentifier

A unique identifier for the snapshot schedule.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### SnapshotScheduleState

The current state of the cluster snapshot schedule.

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

#### IamRoleArn

The Amazon Resource Name (ARN) of the IAM role.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### IamRoleApplyStatus

A value that describes the status of the IAM role's association with an Amazon Redshift cluster. Adding, Removing, In-sync

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ModifyStatus

The status of a modify operation, if any, initiated for the cluster.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### NextMaintenanceWindowStartTime

The date and time in UTC when system maintenance can begin.

_Required_: No

_Type_: String

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

#### RetentionPeriod

The number of days to retain automated snapshots in the destination AWS Region after they are copied from the source AWS Region.By default, this only changes the retention period of copied automated snapshots.

_Required_: No

_Type_: Integer

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Manual

Indicates whether to apply the snapshot retention period to newly copied manual snapshots instead of automated snapshots.

_Required_: No

_Type_: Boolean

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ClusterAvailabilityStatus

The availability status of the cluster for queries. Possible values are Available, Unavailable, Modifying, Maintenance, Failed. 

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ClusterNamespaceArn

The namespace Amazon Resource Name (ARN) of the cluster.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ClusterNodes

The nodes in the cluster.

_Required_: No

_Type_: List of String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ClusterParameterGroups

The list of cluster parameter groups that are associated with this cluster. Each parameter group in the list is returned with its status.

_Required_: No

_Type_: List of String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ClusterPublicKey

The public key for the cluster.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ClusterRevisionNumber

The specific revision number of the database in the cluster.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ClusterNodeRole

Whether the node is a leader node or a compute node.

_Required_: No

_Type_: List of String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ClusterNodePrivateIPAddress

The private IP address of a node within a cluster.

_Required_: No

_Type_: List of String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ClusterNodePublicIPAddress

The public IP address of a node within a cluster.

_Required_: No

_Type_: List of String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### DestinationRegion

The destination region that snapshots are automatically copied to when cross-region snapshot copy is enabled.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### SnapshotCopyGrantName

The name of the snapshot copy grant.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ClusterStatus

The current state of the cluster. 

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ClusterSubnetGroupName

The name of the subnet group that is associated with the cluster. This parameter is valid only when the cluster is in a VPC. 

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### CurrentRateInMegaBytesPerSecond

Describes the data transfer rate in MB's per second.

_Required_: No

_Type_: Double

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### DataTransferredInMegaBytes

Describes the total amount of data that has been transfered in MB's.

_Required_: No

_Type_: Double

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### DataTransferProgressElapsedTimeInSeconds

Describes the number of seconds that have elapsed during the data transfer.

_Required_: No

_Type_: Double

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### EstimatedTimeToCompletionInSeconds

Describes the estimated number of seconds remaining to complete the transfer.

_Required_: No

_Type_: Double

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### DataTransferProgressStatus

Describes the status of the cluster. While the transfer is in progress the status is transferring data.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### TotalDataInMegaBytes

Describes the total amount of data to be transferred in megabytes.

_Required_: No

_Type_: Double

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### BucketName

The name of an existing S3 bucket where the log files are to be stored.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### S3Prefix

The prefix applied to the log file names.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Classic

A boolean value indicating whether the resize operation is using the classic resize process. If you don't provide this parameter or set the value to false, the resize type is elastic.

_Required_: No

_Type_: Boolean

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### SnapshotClusterIdentifier

The name of the cluster the source snapshot was created from. This parameter is required if your IAM user has a policy containing a snapshot resource element that specifies anything other than * for the cluster name.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### NewTableName

The name of the table to create as a result of the current request.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### SourceDatabaseName

The name of the source database that contains the table to restore from.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### SourceTableName

The name of the source table to restore from.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### SourceSchemaName

The name of the source schema that contains the table to restore from. If you do not specify a SourceSchemaName value, the default is public.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### TargetDatabaseName

The name of the database to restore the table to.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### TargetSchemaName

The name of the schema to restore the table to.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### TableRestoreStatusMessage

A description of the status of the table restore request. Status values include SUCCEEDED, FAILED, CANCELED, PENDING, IN_PROGRESS.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### TableRestoreStatusProgressInMegaBytes

The amount of data restored to the new table so far, in megabytes (MB).

_Required_: No

_Type_: Double

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### TableRestoreStatusRequestTime

The time that the table restore request was made, in Universal Coordinated Time (UTC).

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### TableRestoreStatusStatus

The time that the table restore request was made, in Universal Coordinated Time (UTC).

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### TableRestoreRequestId

The unique identifier for the table restore request.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### TableRestoreStatusTotalDataInMegaBytes

The total amount of data to restore to the new table, in megabytes (MB).

_Required_: No

_Type_: Double

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### LastFailureMessage

The message indicating that logs failed to be delivered.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### LastFailureTime

The last time when logs failed to be delivered.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### LastSuccessfulDeliveryTime

The last time that logs were delivered.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### LoggingEnabled

true if logging is on, false if logging is off.

_Required_: No

_Type_: Boolean

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### S3KeyPrefix

The prefix applied to the log file names.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Amount

The limit amount. If time-based, this amount is in minutes. If data-based, this amount is in terabytes (TB). The value must be a positive number.

_Required_: No

_Type_: Double

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### FeatureType

The Amazon Redshift feature that you want to limit. spectrum , concurrency-scaling valid values.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### LimitType

The type of limit. Depending on the feature type, this can be based on a time duration or data size. If FeatureType is spectrum, then LimitType must be data-scanned. If FeatureType is concurrency-scaling, then LimitType must be time. time, data-scanned are two values.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### BreachAction

The action that Amazon Redshift takes when the limit is reached. The default is log. log | emit-metric | disable

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Period

The time period that the amount applies to. A weekly period begins on Sunday. The default is monthly.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### UsageLimitId

The identifier of the usage limit.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### AvgResizeRateInMegaBytesPerSecond

The average rate of the resize operation over the last few minutes, measured in megabytes per second. After the resize operation completes, this value shows the average rate of the entire resize operation.

_Required_: No

_Type_: Double

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### DataTransferProgressPercent

The percent of data transferred from source cluster to target cluster.

_Required_: No

_Type_: Double

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### CancelResizeElapsedTimeInSeconds

The amount of seconds that have elapsed since the resize operation began. After the resize operation completes, this value shows the total actual time, in seconds, for the resize operation.

_Required_: No

_Type_: Double

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### CancelResizeEstimatedTimeToCompletionInSeconds

The estimated time remaining, in seconds, until the resize operation is complete. This value is calculated based on the average resize rate and the estimated amount of data remaining to be processed. Once the resize operation is complete, this value will be 0.

_Required_: No

_Type_: Double

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ImportTablesCompleted

The names of tables that have been completely imported.

_Required_: No

_Type_: List of String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ImportTablesInProgress

The names of tables that are being currently imported.

_Required_: No

_Type_: List of String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ImportTablesNotStarted

The names of tables that have not been yet imported.

_Required_: No

_Type_: List of String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### CancelResizeMessage

An optional string to provide additional details about the resize action.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ProgressInMegaBytes

While the resize operation is in progress, this value shows the current amount of data, in megabytes, that has been processed so far. When the resize operation is complete, this value shows the total amount of data, in megabytes, on the cluster, which may be more or less than TotalResizeDataInMegaBytes (the estimated total amount of data before resize).

_Required_: No

_Type_: Double

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### CancelResizeStatus

The status of the resize operation. Valid Values: NONE | IN_PROGRESS | FAILED | SUCCEEDED | CANCELLING

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### TargetClusterType

The cluster type after the resize operation is complete.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### TargetEncryptionType

The type of encryption for the cluster after the resize is complete.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### TargetNodeType

The node type of cluster  after the resize operation is complete.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### TargetNumberOfNodes

The number of nodes in cluster type after the resize operation is complete.

_Required_: No

_Type_: Integer

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### TotalResizeDataInMegaBytes

The estimated total amount of data, in megabytes, on the cluster before the resize operation began.

_Required_: No

_Type_: Double

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

## Return Values

### Ref

When you pass the logical ID of this resource to the intrinsic `Ref` function, Ref returns the ClusterIdentifier.

### Fn::GetAtt

The `Fn::GetAtt` intrinsic function returns a value for a specified attribute of this type. The following are the available attributes and sample return values.

For more information about using the `Fn::GetAtt` intrinsic function, see [Fn::GetAtt](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-getatt.html).

#### ClusterIdentifier

A unique identifier for the cluster. You use this identifier to refer to the cluster for any subsequent cluster operations such as deleting or modifying. All alphabetical characters must be lower case, no hypens at the end, no two consecutive hyphens. Cluster name should be unique for all clusters within an AWS account

