package com.finalproject.Backend.repository;

import com.finalproject.Backend.model.BookMarkImage;
import com.finalproject.Backend.model.Bookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BookMarkImageRepository extends JpaRepository<BookMarkImage, Long> {
    List<BookMarkImage> findByBookmarkId(Long bookmarkId);
    List<BookMarkImage> findByBookmark(Bookmark bookmark);
} 