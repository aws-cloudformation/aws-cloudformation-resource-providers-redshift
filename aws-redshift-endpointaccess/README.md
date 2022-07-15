# AWS::Redshift::EndpointAccess

The code uses [Lombok](https://projectlombok.org/), and [you may have to install IDE integrations](https://projectlombok.org/setup/overview) to enable auto-complete for Lombok-annotated classes.

## Contract Test
1. Create necessary resources for the contract test. All the required resources are defined in the `./contract-test-required-resources.template.yml`. If you have never created this CloudFormation stack to your AWS Console before, use the following command to create the stack.
    ```bash
    aws cloudformation create-stack --region <Region> --template-body file://./contract-test-required-resources.template.yml --stack-name "aws-redshift-endpointaccess-contract-test-required-resources-stack"
    ```
2. Build the package
   ```bash
   mvn clean package
   ```
3. Run the contract test in your local
    1. Initiate a SAM local virtual environment. Type in the following command to your terminal. Note: SAM local command will occupy the terminal window.
    ```bash
    sam local start-lambda
    ```
    2. Perform the contract test
    ```bash
    cfn test --enforce-timeout 60
    ```
    3. If you ever want to perform any specific contract test item, use the following command instead.
    ```bash
    cfn test -- -k <Test-Name>
    ```
4. Modify the `overrides.json` to control the contract test input cases if necessary.
5. For more information, please refer to [Testing resource types locally using SAM](https://docs.aws.amazon.com/cloudformation-cli/latest/userguide/resource-type-test.html)
