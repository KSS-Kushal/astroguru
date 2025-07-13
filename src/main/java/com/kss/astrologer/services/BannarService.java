package com.kss.astrologer.services;

import com.kss.astrologer.exceptions.CustomException;
import com.kss.astrologer.models.Bannar;
import com.kss.astrologer.repository.BannarRepository;
import com.kss.astrologer.services.aws.S3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
public class BannarService {

    @Autowired
    private BannarRepository bannarRepository;

    @Autowired
    private S3Service s3Service;

    public Bannar uploadBannar(MultipartFile file) {
        List<Bannar> bannars = bannarRepository.findAll();
        if(!bannars.isEmpty()) {
            bannars.forEach(b -> {
                s3Service.deleteFileByUrl(b.getImgUrl());
                deleteBannar(b.getId());
            });
        }
        String imgUrl = s3Service.uploadFile(file, "bannar");
        Bannar bannar = new Bannar();
        bannar.setImgUrl(imgUrl);
        return bannarRepository.save(bannar);
    }

    public Bannar deleteBannar(UUID id) {
        Bannar bannar = bannarRepository.findById(id).orElseThrow(()->new CustomException("Bannar not found"));
        bannarRepository.deleteById(id);
        return bannar;
    }

    public List<Bannar> getBannar() {
        return bannarRepository.findAll();
    }
}
