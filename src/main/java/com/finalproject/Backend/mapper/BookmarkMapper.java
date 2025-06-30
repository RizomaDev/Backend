package com.finalproject.Backend.mapper;

import com.finalproject.Backend.dto.request.BookmarkRequestDTO;
import com.finalproject.Backend.dto.response.BookmarkResponseDTO;
import com.finalproject.Backend.model.Bookmark;
import com.finalproject.Backend.model.Category;
import com.finalproject.Backend.model.Tag;
import com.finalproject.Backend.model.User;
import com.finalproject.Backend.model.Location;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BookmarkMapper {
    public static Bookmark toEntity(BookmarkRequestDTO dto, Tag tag, Category category, User user) {
        Bookmark bookmark = new Bookmark();
        bookmark.setTitle(dto.getTitle());
        bookmark.setDescription(dto.getDescription());
        bookmark.setTag(tag);
        bookmark.setCategory(category);
        bookmark.setUser(user);
        bookmark.setVideoUrl(dto.getVideo());
        bookmark.setInfoAdicional(dto.getUrl());
        bookmark.setLocation(dto.getLocation());
        if (dto.getPublicationDate() != null) {
            bookmark.setPublicationDate(Timestamp.from(dto.getPublicationDate()));
        }
        return bookmark;
    }

    public static BookmarkResponseDTO toDTO(Bookmark bookmark) {
        List<String> imageUrls = bookmark.getImages() != null
            ? bookmark.getImages().stream()
                .map(img -> img.getImagePath())
                .collect(Collectors.toList())
            : new ArrayList<>();
        return new BookmarkResponseDTO(
            bookmark.getId(),
            bookmark.getTitle(),
            bookmark.getDescription(),
            bookmark.getTag() != null ? bookmark.getTag().getName() : null,
            bookmark.getCategory() != null ? bookmark.getCategory().getName() : null,
            imageUrls.isEmpty() ? null : imageUrls.get(0),
            bookmark.getVideoUrl(),
            bookmark.getInfoAdicional(),
            bookmark.getLocation(),
            bookmark.getAddress(),
            bookmark.getPublicationDate() != null ? bookmark.getPublicationDate().toInstant() : null,
            bookmark.getUser().getId(),
            imageUrls
        );
    }
} 