package service;

import dao.ProfileDao;
import entite.Profile;
import Utils.ImgBBUtil;
import java.io.File;
import java.util.Optional;

public class ProfileService {
    private final ProfileDao profileDao = new ProfileDao();

    
    public Optional<Profile> getProfileByUserId(int userId) {
        return Optional.ofNullable(profileDao.getProfileByUserId(userId));
    }

    
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
