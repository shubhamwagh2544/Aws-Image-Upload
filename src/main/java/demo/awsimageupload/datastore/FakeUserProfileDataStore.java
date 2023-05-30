package demo.awsimageupload.datastore;

import demo.awsimageupload.profile.UserProfile;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
public class FakeUserProfileDataStore {
    private static final List<UserProfile> USER_PROFILES = new ArrayList<>();
    static {
        USER_PROFILES.add(new UserProfile(UUID.fromString("d238319e-83a4-4975-bcb0-6246e92bd420"), "janetjones", null));  //UUID.randomUUID() : generates random each time
        USER_PROFILES.add(new UserProfile(UUID.fromString("28f3bbfc-4c48-4554-8753-ea3498f68c86"), "antoniojunior", null));
    }

    public List<UserProfile> getUserProfiles() {
        return USER_PROFILES;
    }
}
