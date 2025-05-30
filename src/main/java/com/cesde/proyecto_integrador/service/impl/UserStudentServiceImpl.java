package com.cesde.proyecto_integrador.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cesde.proyecto_integrador.dto.ProfileDTO;
import com.cesde.proyecto_integrador.exception.ResourceNotFoundException;
import com.cesde.proyecto_integrador.model.Profile;
import com.cesde.proyecto_integrador.model.User;
import com.cesde.proyecto_integrador.repository.ProfileRepository;
import com.cesde.proyecto_integrador.repository.UserRepository;
import com.cesde.proyecto_integrador.service.UserStudentService;

@Service
public class UserStudentServiceImpl implements UserStudentService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfileRepository profileRepository;

    // Profile
    @Override
    public ProfileDTO getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Profile profile = user.getProfile();
        if (profile == null) {
            profile = new Profile();
            profile.setUser(user);
            user.setProfile(profile);
            profileRepository.save(profile);
        }

        return new ProfileDTO(
                profile.getName() != null ? profile.getName() : "",
                profile.getLastName() != null ? profile.getLastName() : "",
                profile.getPhone() != null ? profile.getPhone() : "",
                profile.getAddress() != null ? profile.getAddress() : "",
                profile.getUrlPhoto() != null ? profile.getUrlPhoto() : "");
    }

    @Override
    public void updateProfile(Long userId, ProfileDTO profile) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        Profile updateProfile = user.getProfile();
        if (updateProfile == null) {
            updateProfile = new Profile();
            updateProfile.setUser(user);
            user.setProfile(updateProfile);
            profileRepository.save(updateProfile);
        }
        
        updateProfile.setName(profile.getName());
        updateProfile.setLastName(profile.getLastName());
        updateProfile.setPhone(profile.getPhone());
        updateProfile.setAddress(profile.getAddress());
        updateProfile.setUrlPhoto(profile.getUrlPhoto());
        
        profileRepository.save(updateProfile);
    }
    }


