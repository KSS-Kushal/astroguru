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
import com.kss.astrologer.models.Wallet;
import com.kss.astrologer.repository.AstrologerRepository;
import com.kss.astrologer.repository.UserRepository;
import com.kss.astrologer.repository.WalletRepository;
import com.kss.astrologer.request.AstrologerRequest;
import com.kss.astrologer.types.Role;

@Service
public class AstrologerService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AstrologerRepository astrologerRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Transactional
    public AstrologerDto createAstrologer(AstrologerRequest astrologerRequest) {
        User existingUser = userRepository.findByMobile(astrologerRequest.getMobile()).orElse(null);
        if(existingUser != null) throw new CustomException("User with this mobile number already exists");
        User user = User.builder()
                .name(astrologerRequest.getName())
                .mobile(astrologerRequest.getMobile())
                .role(Role.ASTROLOGER)
                .imgUri("https://img.freepik.com/free-vector/young-man-orange-hoodie_1308-175788.jpg?ga=GA1.1.1570607994.1749976697&semt=ais_hybrid&w=740")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        user = userRepository.save(user);

        Wallet wallet = new Wallet();
        wallet.setBalance(0.0);
        wallet.setUser(user);
        wallet = walletRepository.save(wallet);
        user.setWallet(wallet);
        user = userRepository.save(user);

        AstrologerDetails astrologerDetails = new AstrologerDetails();
        astrologerDetails.setUser(user);
        astrologerDetails.setAbout(astrologerRequest.getAbout());
        astrologerDetails.setExpertise(astrologerRequest.getExpertise());
        astrologerDetails.setExperienceYears(astrologerRequest.getExperienceYears());
        astrologerDetails.setLanguages(astrologerRequest.getLanguages());
        astrologerDetails.setPricePerMinuteChat(astrologerRequest.getPricePerMinuteChat());
        astrologerDetails.setPricePerMinuteVoice(astrologerRequest.getPricePerMinuteVoice());
        astrologerDetails.setPricePerMinuteVideo(astrologerRequest.getPricePerMinuteVideo());
        astrologerDetails.setBlocked(false);

        astrologerDetails = astrologerRepository.save(astrologerDetails);
        return new AstrologerDto(astrologerDetails);
    }

    @Transactional
    public AstrologerDto updateAstrologer(AstrologerRequest astrologerRequest, UUID astrologerId) {
        AstrologerDetails astrologerDetails = astrologerRepository.findById(astrologerId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "Astrologer not found"));

        User user = astrologerDetails.getUser();

        if (astrologerRequest.getName() != null) user.setName(astrologerRequest.getName());

        if(astrologerRequest.getExpertise() != null) astrologerDetails.setExpertise(astrologerRequest.getExpertise());
        if(astrologerRequest.getAbout() != null) astrologerDetails.setAbout(astrologerRequest.getAbout());
        if(astrologerRequest.getExperienceYears() != null) astrologerDetails.setExperienceYears(astrologerRequest.getExperienceYears());
        if(astrologerRequest.getLanguages() != null) astrologerDetails.setLanguages(astrologerRequest.getLanguages());
        if(astrologerRequest.getPricePerMinuteChat() != null) astrologerDetails.setPricePerMinuteChat(astrologerRequest.getPricePerMinuteChat());
        if(astrologerRequest.getPricePerMinuteVoice() != null) astrologerDetails.setPricePerMinuteVoice(astrologerRequest.getPricePerMinuteVoice());
        if(astrologerRequest.getPricePerMinuteVideo() != null) astrologerDetails.setPricePerMinuteVideo(astrologerRequest.getPricePerMinuteVideo());

        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
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
