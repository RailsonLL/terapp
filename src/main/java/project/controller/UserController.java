package project.controller;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import project.dtos.UserDto;
import project.models.UrlResponse;
import project.models.User;
import project.services.UserService;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Log4j2
@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {

    @Value("${aws.s3.bucket.doc}")
    private String bucketName;

    @Autowired
    UserService userService;

    @Autowired
    private AmazonS3 amazonS3;

    @PostMapping
    public ResponseEntity<Object> saveUser(@RequestBody UserDto userDto){
        log.debug("POST saveUser userDto received {} ", userDto.toString());
        var user = new User();
        BeanUtils.copyProperties(userDto, user);
        userService.saveUser(user);
        log.debug("POST saveUser userId saved {} ", user.getUserId());
        log.info("User saved successfully userId {} ", user.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<Object> updateUser(@PathVariable(value="userId") UUID userId,
                                               @RequestBody @Valid UserDto userDto){
        log.debug("PUT updateUser userDto received {} ", userDto.toString());
        Optional<User> userOptional = userService.findById(userId);

        if(!userOptional.isPresent()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User Not Found.");
        }

        var user = userOptional.get();
        BeanUtils.copyProperties(userDto, user);
        userService.saveUser(user);
        log.debug("PUT updateUser userId saved {} ", user.getUserId());
        log.info("User updated successfully userId {} ", user.getUserId());
        return ResponseEntity.status(HttpStatus.OK).body(user);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Object> deleteCourse(@PathVariable(value="userId") UUID userId){
        log.debug("DELETE deleteUser userId received {} ", userId);
        Optional<User> userOptional = userService.findById(userId);
        if(!userOptional.isPresent()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User Not Found.");
        }
        userService.deleteUser(userOptional.get());
        log.debug("DELETE deleteUser userId deleted {} ", userId);
        log.info("User deleted successfully userId {} ", userId);
        return ResponseEntity.status(HttpStatus.OK).body("User deleted successfully.");
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers(){
        return ResponseEntity.status(HttpStatus.OK).body(userService.findAll());
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Object> getOneUser(@PathVariable(value="userId") UUID userId){
        Optional<User> userOptional = userService.findById(userId);
        if(!userOptional.isPresent()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User Not Found.");
        }
        return ResponseEntity.status(HttpStatus.OK).body(userOptional.get());
    }

    @PostMapping(value = "/{userId}/save-photo",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> saveUserPhotoS3(@RequestParam("file") MultipartFile file, @PathVariable String userId) {
        try {
            log.info("saveUserPhotoS3. userID: {}", userId);

            String contentType = file.getContentType();
            if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body("Only images JPG or PNG are supported.");
            }

            String fileExtension = contentType.equals("image/jpeg") ? ".jpg" : ".png";
            String pathPattern = "photos/user-" + userId + "-user";
            String fileName = pathPattern + UUID.randomUUID().toString() + fileExtension;

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(contentType);
            metadata.setContentLength(file.getSize());

            amazonS3.putObject(new PutObjectRequest(
                    bucketName,
                    fileName,
                    file.getInputStream(),
                    metadata
            ));

            String fileUrl = amazonS3.getUrl(bucketName, fileName).toString();

            log.info("Send image with success. URL: {}", fileUrl);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body("Send image with success. URL: " + fileUrl);
        } catch (IOException e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao processar a imagem: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao fazer upload: " + e.getMessage());
        }
    }

    @GetMapping("/{userId}/photo")
    public ResponseEntity<Object> getUserPhoto(@PathVariable(value="userId") UUID userId){
        Optional<String> photoPathOptional = userService.getPhotoByUserId(userId);
        if(photoPathOptional.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User Not Found.");
        }

        try {
            String photoPath = photoPathOptional.get();
            S3Object s3Object = amazonS3.getObject(bucketName, photoPath);

            MediaType mediaType;
            if (photoPath.toLowerCase().endsWith(".jpg") || photoPath.toLowerCase().endsWith(".jpeg")) {
                mediaType = MediaType.IMAGE_JPEG;
            } else if (photoPath.toLowerCase().endsWith(".png")) {
                mediaType = MediaType.IMAGE_PNG;
            } else {
                return ResponseEntity.badRequest().body("Type image not found!");
            }

            byte[] content = s3Object.getObjectContent().readAllBytes();
            s3Object.close();

            log.info("User photo loading with success {} ", userId);
            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .body(content);
        } catch (Exception e) {
            e.printStackTrace();
            log.info("User photo could not loading {} ", userId);
            return ResponseEntity.badRequest().body("Error loading user photo!!!");
        }
    }

    @PostMapping("/presigned-url")
    public ResponseEntity<UrlResponse> createPresignedUrl() {
        UrlResponse urlResponse  = new UrlResponse();
        Instant expirationTime = Instant.now().plus(Duration.ofMinutes(5));
        String processId = "photos/user-46965836-8564-464f-84cf-6ae2de2d66b7-user" + UUID.randomUUID().toString()+".png";
        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                new GeneratePresignedUrlRequest(bucketName, processId)
                        .withMethod(HttpMethod.PUT)
                        .withExpiration(Date.from(expirationTime));

        urlResponse.setExpirationTime(expirationTime.getEpochSecond());
        urlResponse.setUrl(amazonS3.generatePresignedUrl(
                generatePresignedUrlRequest).toString());

        log.info("Invoice Url Created!");
        return new ResponseEntity<UrlResponse>(urlResponse, HttpStatus.OK);
    }

}
