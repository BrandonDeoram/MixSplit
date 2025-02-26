package com.demo.MixSplit.Repository;

import com.demo.MixSplit.Entity.AudioFile;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AudioFileRepository extends CrudRepository<AudioFile, Long> {
    List<AudioFile> findAll();

    @Query(value="SELECT status FROM audio_file WHERE file_name =:fileName",nativeQuery = true)
    Optional<String> findStatusAudioFile(@Param("fileName") String fileName);


//    @Query(value="SELECT status FROM audio_file WHERE acr_id =:acr_id",nativeQuery = true)
//    Optional<String> findStatusAudioFileId(@Param("acrId") Long acrId);
}

