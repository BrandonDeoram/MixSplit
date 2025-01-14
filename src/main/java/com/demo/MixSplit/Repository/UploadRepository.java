package com.demo.MixSplit.Repository;

import com.demo.MixSplit.Entity.Upload;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UploadRepository extends JpaRepository<Upload, Long> {
    List<Upload> findAll();
}
