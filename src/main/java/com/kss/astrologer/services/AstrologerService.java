package com.kss.astrologer.services;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kss.astrologer.dto.AstrologerDto;
import com.kss.astrologer.exceptions.CustomException;
import com.kss.astrologer.models.AstrologerDetails;
import com.kss.astrologer.models.User;
import com.kss.astrologer.repository.AstrologerRepository;
import com.kss.astrologer.repository.UserRepository;
import com.kss.astrologer.request.AstrologerRequest;
import com.kss.astrologer.types.Role;

@Service
public class AstrologerService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AstrologerRepository astrologerRepository;

    @Transactional
    public AstrologerDto createAstrologer(AstrologerRequest astrologerRequest) {
        User existingUser = userRepository.findByMobile(astrologerRequest.getMobile()).orElse(null);
        if(existingUser != null) throw new CustomException("User with this mobile number already exists");
        User user = User.builder()
                .name(astrologerRequest.getName())
                .mobile(astrologerRequest.getMobile())
                .role(Role.ASTROLOGER)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        user = userRepository.save(user);

        AstrologerDetails astrologerDetails = new AstrologerDetails();
        astrologerDetails.setUser(user);
        astrologerDetails.setExpertise(astrologerRequest.getExpertise());
        astrologerDetails.setExperienceYears(astrologerRequest.getExperienceYears());
        astrologerDetails.setPricePerMinuteChat(astrologerRequest.getPricePerMinuteChat());
        astrologerDetails.setPricePerMinuteVoice(astrologerRequest.getPricePerMinuteVoice());
        astrologerDetails.setPricePerMinuteVideo(astrologerRequest.getPricePerMinuteVideo());
        astrologerDetails.setBlocked(false);

        astrologerDetails = astrologerRepository.save(astrologerDetails);
        return new AstrologerDto(astrologerDetails);
    }


    public AstrologerDto getAstrologerById(UUID id) {
        AstrologerDetails astrologerDetails = astrologerRepository.findById(id)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "Astrologer not found"));
        return new AstrologerDto(astrologerDetails);
    }

    public AstrologerDto deleteAstrologerById(UUID id) {
        AstrologerDetails astrologerDetails = astrologerRepository.findById(id)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "Astrologer not found"));
        astrologerRepository.delete(astrologerDetails);
        return new AstrologerDto(astrologerDetails);
    }

    public Page<AstrologerDto> getAllAstrologers(Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page-1, size);
        Page<AstrologerDetails> astrologerDetailsPage = astrologerRepository.findAll(pageable);
        return astrologerDetailsPage.map(AstrologerDto::new);
    }

    public AstrologerDto getAstrologerByUserId(UUID id) {
        AstrologerDetails astrologerDetails = astrologerRepository.findByUserId(id)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "Astrologer not found for user ID: " + id));
        return new AstrologerDto(astrologerDetails);
    }
}
