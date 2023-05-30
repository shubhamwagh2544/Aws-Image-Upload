package demo.awsimageupload.profile;

import com.amazonaws.services.dynamodbv2.xspec.S;
import demo.awsimageupload.bucket.BucketName;
import demo.awsimageupload.datastore.FakeUserProfileDataStore;
import demo.awsimageupload.filestore.FileStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

import static org.apache.http.entity.ContentType.*;

@Service
public class UserProfileService {
    private final FakeUserProfileDataStore fakeUserProfileDataStore;
    private final FileStore fileStore;
    @Autowired
    public UserProfileService(FakeUserProfileDataStore fakeUserProfileDataStore, FileStore fileStore) {
        this.fakeUserProfileDataStore = fakeUserProfileDataStore;
        this.fileStore = fileStore;
    }

    public List<UserProfile> getUserProfiles() {
        return fakeUserProfileDataStore.getUserProfiles();
    }

    public void uploadUserProfileImage(UUID userProfileId, MultipartFile file) {
        //1. check if image is not empty
        isFileEmpty(file);

        //2. if file is an image
        isImage(file);

        //3. if user exists in our database
        UserProfile user = getUserProfileOrThrow(userProfileId);

        //4. grab some metadata from file if any
        Map<String, String> metadata = extractMetadata(file);

        //5. store the image in s3 and update database with s3 image link (userProfileImageLink)
        String path = String.format("%s/%s", BucketName.PROFILE_IMAGE.getBucketName(), user.getUserProfileId());
        String filename = String.format("%s-%s", file.getOriginalFilename(), UUID.randomUUID());

        try {
            fileStore.save(path, filename, file.getSize(),  Optional.of(metadata), file.getInputStream());
            user.setUserProfileImageLink(filename);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

    }

    public byte[] downloadUserProfileImage(UUID userProfileId) {
        UserProfile user = getUserProfileOrThrow(userProfileId);
        String path = String.format("%s/%s", BucketName.PROFILE_IMAGE.getBucketName(), user.getUserProfileId());

        return user.getUserProfileImageLink()
                .map(key -> fileStore.download(path, key))
                .orElse(new byte[0]);
    }

    private static Map<String, String> extractMetadata(MultipartFile file) {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("Content-Type", file.getContentType());
        metadata.put("Content-Length", String.valueOf(file.getSize()));
        return metadata;
    }

    private UserProfile getUserProfileOrThrow(UUID userProfileId) {
        return fakeUserProfileDataStore
                .getUserProfiles()
                .stream()
                .filter(userProfile -> userProfile.getUserProfileId().equals(userProfileId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(String.format("User profile %s was not found", userProfileId)));
    }

    private static void isImage(MultipartFile file) {
        if (!Arrays.asList(
                IMAGE_JPEG.getMimeType(),
                IMAGE_PNG.getMimeType(),
                IMAGE_GIF.getMimeType()).contains(file.getContentType())) {
            throw new IllegalStateException("File must be image [ " + file.getContentType() +" ]");
        }
    }

    private static void isFileEmpty(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalStateException(("Cannot upload empty file [ " + file.getSize() + " ]"));
        }
    }

}
