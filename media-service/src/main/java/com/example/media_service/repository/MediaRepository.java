package com.example.media_service.repository;

import com.example.media_service.enums.MediaOwnerType;
import com.example.media_service.enums.MediaPurpose;
import com.example.media_service.model.Media;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface MediaRepository extends JpaRepository<Media, String> {
    @Query(value = """
            Select * From media Where owner_id = :ownerId
            and owner_type = :ownerType
            Order by position ASC
            """, nativeQuery = true)
    List<Media> findByOwnerIdAndOwnerType(@Param("ownerId") String ownerId,
                                          @Param("ownerType") String ownerType);
    @Modifying
    @Query(
            value = """
        UPDATE media
        SET position = position - 1
        WHERE owner_id = :ownerId
          AND position > (SELECT position FROM media WHERE url = :url)
    """,
            nativeQuery = true
    )
    void reindexAfterDelete(@Param("ownerId") String ownerId, @Param("url") String url);

    @Modifying
    @Query(value = """
        UPDATE media
        SET position = position - 1
        WHERE owner_id = :ownerId
          AND position > :oldPos
          AND position <= :newPos
        """, nativeQuery = true)
    void shiftDownPositions(@Param("ownerId") String ownerId,
                            @Param("oldPos") int oldPos,
                            @Param("newPos") int newPos);

    @Modifying
    @Query(value = """
        UPDATE media
        SET position = position + 1
        WHERE owner_id = :ownerId
          AND position >= :newPos
          AND position < :oldPos
        """, nativeQuery = true)
    void shiftUpPositions(@Param("ownerId") String ownerId,
                          @Param("oldPos") int oldPos,
                          @Param("newPos") int newPos);

    Media findByUrl(String url);

    boolean existsByOwnerIdAndMediaPurpose(String ownerId, MediaPurpose mediaPurpose);

    @Query(value = """
            Select COALESCE(MAX(position), 0)
            From media
            Where owner_id = :ownerId
            and media_purpose = :purpose
            """,
    nativeQuery = true)
    Optional<Integer> findMaxPositionByOwnerIdAndPurpose(
            @Param("ownerId") String productId,
            @Param("purpose") String mediaPurpose);
}
