package com.finalproject.Backend.service;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.finalproject.Backend.dto.ImageDTO;
import com.finalproject.Backend.dto.request.BookmarkRequestDTO;
import com.finalproject.Backend.dto.response.BookmarkResponseDTO;
import com.finalproject.Backend.exception.ResourceNotFoundException;
import com.finalproject.Backend.mapper.BookmarkMapper;
import com.finalproject.Backend.model.Bookmark;
import com.finalproject.Backend.model.BookMarkImage;
import com.finalproject.Backend.model.Category;
import com.finalproject.Backend.model.Tag;
import com.finalproject.Backend.model.User;
import com.finalproject.Backend.repository.BookmarkRepository;
import com.finalproject.Backend.repository.BookMarkImageRepository;
import com.finalproject.Backend.repository.CategoryRepository;
import com.finalproject.Backend.repository.TagRepository;
import com.finalproject.Backend.util.ImageValidator;

import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.json.JSONObject;


@Service
public class BookmarkService {
    private static final Logger logger = LoggerFactory.getLogger(BookmarkService.class);
    private final BookmarkRepository bookmarkRepository;
    private final BookmarkImageService bookmarkImageService;
    private final BookMarkImageRepository bookMarkImageRepository;
    private final TagRepository tagRepository;
    private final CategoryRepository categoryRepository;

    @Autowired
    public BookmarkService(BookmarkRepository bookmarkRepository, BookmarkImageService bookmarkImageService, BookMarkImageRepository bookMarkImageRepository, TagRepository tagRepository, CategoryRepository categoryRepository) {
        this.bookmarkRepository = bookmarkRepository;
        this.bookmarkImageService = bookmarkImageService;
        this.bookMarkImageRepository = bookMarkImageRepository;
        this.tagRepository = tagRepository;
        this.categoryRepository = categoryRepository;
    }

    public List<BookmarkResponseDTO> getAll() {
        return bookmarkRepository.findAll()
                .stream()
                .map(BookmarkMapper::toDTO)
                .collect(Collectors.toList());
    }

    public Optional<BookmarkResponseDTO> getById(Long id) {
        return bookmarkRepository.findById(id)
                .map(BookmarkMapper::toDTO);
    }

    public List<BookmarkResponseDTO> searchByTitle(String title) {
        if (title == null || title.trim().length() < 3) {
            return Collections.emptyList();
        }
        return bookmarkRepository.findByTitleContainingIgnoreCase(title)
                .stream()
                .map(BookmarkMapper::toDTO)
                .collect(Collectors.toList());
    }

    public BookmarkResponseDTO create(BookmarkRequestDTO dto, User user) {
        Tag tag = tagRepository.findById(dto.getTagId()).orElseThrow(() -> new ResourceNotFoundException("Tag", "id", dto.getTagId()));
        Category category = categoryRepository.findById(dto.getCategoryId()).orElseThrow(() -> new ResourceNotFoundException("Category", "id", dto.getCategoryId()));
        Bookmark bookmark = BookmarkMapper.toEntity(dto, tag, category, user);
        // Guardar el bookmark primero, sin address
        bookmark = bookmarkRepository.save(bookmark);
        // Manejo de imágenes base64 (múltiples)
        try {
            if (dto.getImages() != null && !dto.getImages().isEmpty()) {
                for (ImageDTO imageDTO : dto.getImages()) {
                    String filePath = bookmarkImageService.saveBase64File(imageDTO.getBase64(), imageDTO.getName(), imageDTO.getType());
                    BookMarkImage bookmarkImage = new BookMarkImage();
                    bookmarkImage.setImageName(bookmarkImageService.extractFileName(filePath));
                    bookmarkImage.setImagePath(filePath);
                    bookmarkImage.setContentType(imageDTO.getType());
                    bookmarkImage.setImageSize(imageDTO.getSize());
                    bookmarkImage.setBookmark(bookmark);
                    bookMarkImageRepository.save(bookmarkImage);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error saving bookmark images: " + e.getMessage());
        }
        // Lanzar reverse geocoding en segundo plano
        if (bookmark.getLocation() != null) {
            fetchAndUpdateAddressAsync(bookmark.getId(), bookmark.getLocation().getLatitude(), bookmark.getLocation().getLongitude());
        }
        return BookmarkMapper.toDTO(bookmark);
    }

    @Async
    public void fetchAndUpdateAddressAsync(Long bookmarkId, Double lat, Double lon) {
        logger.info("Iniciando fetchAndUpdateAddressAsync para bookmarkId={}, lat={}, lon={}", bookmarkId, lat, lon);
        String address = reverseGeocode(lat, lon);
        logger.info("Dirección obtenida: {}", address);
        Optional<Bookmark> optionalBookmark = bookmarkRepository.findById(bookmarkId);
        if (optionalBookmark.isPresent()) {
            Bookmark bookmarkToUpdate = optionalBookmark.get();
            bookmarkToUpdate.setAddress(address);
            bookmarkRepository.save(bookmarkToUpdate);
            logger.info("Bookmark actualizado con address: {}", address);
        } else {
            logger.error("No se encontró el bookmark con id={}", bookmarkId);
        }
    }

    public BookmarkResponseDTO update(Long id, BookmarkRequestDTO dto, User user) {
        Bookmark bookmark = bookmarkRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Bookmark", "id", id));
        Tag tag = tagRepository.findById(dto.getTagId()).orElseThrow(() -> new ResourceNotFoundException("Tag", "id", dto.getTagId()));
        Category category = categoryRepository.findById(dto.getCategoryId()).orElseThrow(() -> new ResourceNotFoundException("Category", "id", dto.getCategoryId()));
        bookmark.setTitle(dto.getTitle());
        bookmark.setDescription(dto.getDescription());
        bookmark.setTag(tag);
        bookmark.setCategory(category);
        bookmark.setUser(user);
        bookmark.setVideoUrl(dto.getVideo());
        bookmark.setInfoAdicional(dto.getUrl());
        bookmark.setLocation(dto.getLocation());
        if (dto.getPublicationDate() != null) {
            bookmark.setPublicationDate(java.sql.Timestamp.from(dto.getPublicationDate()));
        }
        // Manejo de imágenes base64 (múltiples)
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            List<BookMarkImage> existingImages = bookMarkImageRepository.findByBookmarkId(id);
            for (BookMarkImage img : existingImages) {
                bookmarkImageService.deleteFile(img.getImagePath());
                bookMarkImageRepository.delete(img);
            }
            try {
                for (ImageDTO imageDTO : dto.getImages()) {
                    String filePath = bookmarkImageService.saveBase64File(imageDTO.getBase64(), imageDTO.getName(), imageDTO.getType());
                    BookMarkImage bookmarkImage = new BookMarkImage();
                    bookmarkImage.setImageName(bookmarkImageService.extractFileName(filePath));
                    bookmarkImage.setImagePath(filePath);
                    bookmarkImage.setContentType(imageDTO.getType());
                    bookmarkImage.setImageSize(imageDTO.getSize());
                    bookmarkImage.setBookmark(bookmark);
                    bookMarkImageRepository.save(bookmarkImage);
                }
            } catch (IOException e) {
                throw new RuntimeException("Error updating bookmark images: " + e.getMessage());
            }
        }
        if (bookmark.getLocation() != null) {
            fetchAndUpdateAddressAsync(bookmark.getId(), bookmark.getLocation().getLatitude(), bookmark.getLocation().getLongitude());
        }
        return BookmarkMapper.toDTO(bookmarkRepository.save(bookmark));
    }

    public boolean delete(Long id, User user) {
        Optional<Bookmark> bookmarkOpt = bookmarkRepository.findById(id);
        if (!bookmarkOpt.isPresent()) {
            return false;
        }
        Bookmark bookmark = bookmarkOpt.get();
        if (!bookmark.getUser().getId().equals(user.getId())) {
            return false;
        }
        List<BookMarkImage> images = bookMarkImageRepository.findByBookmarkId(id);
        for (BookMarkImage img : images) {
            bookmarkImageService.deleteFile(img.getImagePath());
        }
        bookmarkRepository.deleteById(id);
        return true;
    }

    public List<BookmarkResponseDTO> getByUserId(Long userId) {
        return bookmarkRepository.findAll().stream()
            .filter(b -> b.getUser() != null && b.getUser().getId().equals(userId))
            .map(BookmarkMapper::toDTO)
            .collect(Collectors.toList());
    }

    private String reverseGeocode(Double lat, Double lon) {
        String url = "https://nominatim.openstreetmap.org/reverse?format=json&lat=" + lat + "&lon=" + lon;
        logger.info("Llamando a OpenStreetMap: {}", url);
        RestTemplate restTemplate = new RestTemplate();
    
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Iwatech-bookmark-app"); // Obligatorio para Nominatim
            HttpEntity<String> entity = new HttpEntity<>(headers);
    
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            logger.info("Respuesta de OpenStreetMap: {}", response.getBody());
            if (response.getStatusCode().is2xxSuccessful()) {
                JSONObject json = new JSONObject(response.getBody());
                return json.getString("display_name");
            }
        } catch (Exception e) {
            logger.error("Error en reverseGeocode: {}", e.getMessage(), e);
        }
    
        return "Dirección no disponible";
    }
    
    
} 