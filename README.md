# Configuring your S3 path correctly

## TL;DR

In LocalStack, the S3 service stands out for its approach to endpoint configuration, which is distinct from all other 
services. Unlike the standard format used across LocalStack, the S3 service adopts a specialized 
format: `s3.localhost.localstack.cloud`.

This convention mirrors AWS S3's virtual-hosted-style of addressing behavior, facilitating a more accurate 
emulation of S3 interactions in a local development environment.

## Path-Style vs. Virtual-Hosted-Style S3 Requests

The main difference between path-style and virtual hosting-style endpoints when accessing files in an S3 bucket lies in
how the bucket name is included in the URL.

Path-style endpoints format the URL by placing the bucket name as part of the path. The structure looks like
this: `http://s3.amazonaws.com/bucket-name/key-name`.

Virtual hosting-style endpoints, on the other hand, include the bucket name as a subdomain of the domain in the URL. 
The format is: `http://bucket-name.s3.amazonaws.com/key-name`. This method allows S3 to serve requests from different 
buckets using a single IP address while making it easier to use SSL/TLS certificates tied to the bucket name as a domain.
It's the preferred method for most modern applications due to its cleaner URL structure and compatibility with DNS standards.

### S3 Requests in LocalStack

LocalStack, having a high parity level with AWS, also distinguishes between path style and virtual-hosted-style requests
based on the request's Host header. This means that the bucket name is part of the Host header, visible in the URL. 
To ensure LocalStack parses the bucket name correctly, the URL must be prefixed with `s3.`, such as
`s3.localhost.localstack.cloud`.

By default, most SDKs opt for virtual-hosted-style requests, automatically prefixing endpoints with the bucket name. 
If your endpoint doesn't start with s3., LocalStack might not process your request correctly, leading to errors. 
You can address this by adjusting the endpoint to use the `s3.` prefix or by setting your SDK to use path-style requests.

According to the AWS documentation, path-style requests will be discontinued in the near future. However, 
the SDKs still support some method to "force path style," which needs to receive a true argument. 
If your endpoint does not start with `s3.`, LocalStack treats all requests as path style by default. 
For consistent S3 operations, using the `s3.localhost.localstack.cloud` endpoint is recommended.

## Example

### Runs on LocalStack

Let's look at the simplest example of how to properly configure an S3 client in Java to fetch a text file from a 
bucket and read its content.

First, let's create an S3 bucket and add a text file to it.

- Create the bucket.

`aws --endpoint="http://localhost.localstack.cloud:4566" s3api create-bucket --bucket testy-mctestface-bucket`

- Create the file.

`echo "Hello from the test bucket." > s3test.txt`

- Add the file to the bucket.

`aws --endpoint="http://localhost.localstack.cloud:4566" s3 cp s3test.txt s3://testy-mctestface-bucket`

- Programmatically getting the file and reading it.

 #### LocalStack does not enforce IAM policies by default, so this should be enough for now.

```
        String bucketName = "testy-mctestface-bucket";
        String key = "s3test.txt";

        AwsBasicCredentials awsCreds = AwsBasicCredentials.create("test", "test");

        S3Client s3 = S3Client.builder()
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .endpointOverride(URI.create("https://s3.localhost.localstack.cloud:4566"))
                .region(Region.US_EAST_1)
                .build();

        .... 
        
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            ResponseBytes<GetObjectResponse> objectBytes = s3.getObjectAsBytes(getObjectRequest);

            String content = new String(objectBytes.asByteArray());

            System.out.println("File content: \n" + content);
      
}
```

This code creates an S3 client with static credentials and a custom endpoint 
(`https://s3.localhost.localstack.cloud:4566`) to retrieve and print the content of a specific file 
(s3test.txt) from a bucket (testy-mctestface-bucket). In case the endpoint is misconfigured or 
the bucket does not exist, this will result in a The specified bucket does not exist message.

While this code runs locally and required minimal confirguration, other compute services, such as Lambda,
require the same endpoint configuration.

Additionally, you can access your file content using a curl command:

Virtual-hosted-style: `curl http://testy-mctestface-bucket.s3.us-east-1.localhost.localstack.cloud:4566/s3test.txt`

Path-style: `curl http://s3.us-east-1.localhost.localstack.cloud:4566/testy-mctestface-bucket/s3test.txt`

### Runs on AWS

- The previous commands work on AWS by removing the --endpoint flag and making the bucket public.
- Don't forget to configure your AWS CLI to use the right credentials or export the `AWS_ACCESS_KEY_ID` and `AWS_SECRET_ACCESS_KEY` environment variables.
- The S3 client will have a simpler configuration:
  `S3Client s3 = S3Client.builder().region(Region.US_EAST_1).build();`