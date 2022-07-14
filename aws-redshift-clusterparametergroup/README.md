# AWS::Redshift::ClusterParameterGroup

The code uses [Lombok](https://projectlombok.org/), and [you may have to install IDE integrations](https://projectlombok.org/setup/overview) to enable auto-complete for Lombok-annotated classes.

## Contract Test
1. Build the package
   ```bash
   mvn clean package
   ```
1. Run the contract test in your local
    1. Initiate a SAM local virtual environment. Type in the following command to your terminal. Note: SAM local command will occupy the terminal window.
    ```bash
    sam local start-lambda
    ```
    1. Perform the contract test
    ```bash
    cfn test --enforce-timeout 60
    ```
    1. If you ever want to perform any specific contract test item, use the following command instead.
    ```bash
    cfn test -- -k <Test-Name>
    ```
1. Modify the `overrides.json` to control the contract test input cases if necessary.
1. For more information, please refer to [Testing resource types locally using SAM](https://docs.aws.amazon.com/cloudformation-cli/latest/userguide/resource-type-test.html)
