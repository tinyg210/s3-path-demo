package cloud.localstack;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.net.URI;

public class S3EndpointDemo {
    public static void main(String[] args) {

        String bucketName = "testy-mctestface-bucket";
        String key = "s3test.txt";

        AwsBasicCredentials awsCreds = AwsBasicCredentials.create("test", "test");

        S3Client s3 = S3Client.builder()
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .endpointOverride(URI.create("https://s3.localhost.localstack.cloud:4566"))
                .region(Region.US_EAST_1)
                .build();

//        Using the real AWS:
//        S3Client s3 = S3Client.builder().region(Region.US_EAST_1).build();

        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            ResponseBytes<GetObjectResponse> objectBytes = s3.getObjectAsBytes(getObjectRequest);

            String content = new String(objectBytes.asByteArray());

            System.out.println("File content: \n" + content);
        } catch (S3Exception e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        } catch (SdkClientException | AwsServiceException e) {
            throw new RuntimeException(e);
        }

        s3.close();
    }
}
