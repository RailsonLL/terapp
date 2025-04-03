package project.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import project.models.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    @Modifying
    @Query(value="update tuser set photo_path = :photoPath where user_id = :userId",nativeQuery = true)
    void saveUserPhoto(@Param("photoPath") String photoPath, @Param("userId") UUID userId);

    @Query("SELECT u.photoPath FROM User u WHERE u.userId = :userId")
    Optional<String> getPhotoPathByUserId(@Param("userId") UUID userId);

}