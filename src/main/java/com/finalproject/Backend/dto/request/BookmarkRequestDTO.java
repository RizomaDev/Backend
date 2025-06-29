package com.finalproject.Backend.dto.request;

import lombok.*;
import java.time.Instant;
import com.finalproject.Backend.model.Location;
import java.util.List;
import com.finalproject.Backend.dto.ImageDTO;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookmarkRequestDTO {
    private String title;
    private String description;
    private Long tagId;
    private Long categoryId;
    private String video;
    private String url;
    private Location location;
    private Instant publicationDate;
    private List<ImageDTO> images;
} 