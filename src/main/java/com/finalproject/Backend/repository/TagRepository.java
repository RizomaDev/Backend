package com.finalproject.Backend.repository;

import com.finalproject.Backend.model.Tag; 
 
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
}
