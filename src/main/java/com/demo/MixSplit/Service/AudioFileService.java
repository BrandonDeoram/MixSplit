package com.demo.MixSplit.Service;
import com.demo.MixSplit.Entity.AudioFile;
import com.demo.MixSplit.Exception.ResourceNotFoundException;
import com.demo.MixSplit.Repository.AudioFileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

@Service
public class AudioFileService {

    @Autowired
    private  AudioFileRepository audioFileRepository;


    // Adds user audio file to database
    public AudioFile saveUpload(Long userId,String acrId, String s3Key, String fileName) {
        AudioFile upload = new AudioFile(userId,acrId,s3Key,fileName);
        return audioFileRepository.save(upload);
    }

    // Check status of AudioFile
    public String getAudioFileStatus(@RequestParam String fileName){
        return audioFileRepository.findStatusAudioFile(fileName).orElseThrow(() -> new ResourceNotFoundException("Filename: " + fileName + " not found"));
    }

    // Check status of AudioFile using acrId
//    public String getAudioFileStatus(@RequestParam Long acrId){
//        return audioFileRepository.findStatusAudioFileId(acrId).orElseThrow(() -> new ResourceNotFoundException("AcrId: " + acrId + " not found"));
//    }
}
