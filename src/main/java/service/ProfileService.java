package service;

import dao.ProfileDao;
import entite.Profile;
import Utils.ImgBBUtil;
import java.io.File;
import java.util.Optional;

public class ProfileService {
    private final ProfileDao profileDao = new ProfileDao();

    /**
     * Retrieves the full profile of a user.
     *
     * @param userId The user's ID.
     * @return An Optional containing the Profile if found.
     */
    public Optional<Profile> getProfileByUserId(int userId) {
        return Optional.ofNullable(profileDao.getProfileByUserId(userId));
    }

    /**
     * Creates a new profile or updates an existing one.
     * If an image file is provided, it will be uploaded using ImgBBUtil and its URL saved.
     *
     * @param profile   The Profile object containing the new/updated data.
     * @param imageFile The image file to be uploaded (can be null).
     * @return True if the operation was successful, false otherwise.
     */
    public boolean createOrUpdateProfile(Profile profile, File imageFile) {
        if (imageFile != null) {
            String uploadedImageUrl = ImgBBUtil.uploadImageToImgBB(imageFile);
            if (uploadedImageUrl != null) {
                profile.setPictureUrl(uploadedImageUrl);
            }
        }
        Profile existing = profileDao.getProfileByUserId(profile.getUserId());
        if (existing == null) {
            return profileDao.createProfile(profile);
        } else {
            return profileDao.updateProfile(profile);
        }
    }
}
