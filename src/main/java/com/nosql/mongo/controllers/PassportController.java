package com.nosql.mongo.controllers;

import com.nosql.mongo.dtos.*;
import com.nosql.mongo.entities.PassportEntity;
import com.nosql.mongo.model.Attachment;
import com.nosql.mongo.services.PassportService;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/passport")
public class PassportController {
    private final PassportService passportService;
    private final ModelMapper modelMapper;

    public PassportController(PassportService passportService, ModelMapper modelMapper) {
        this.passportService = passportService;
        this.modelMapper = modelMapper;
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponseDto<PassportEntity>> createPassport(@Valid @RequestBody CreatePassportDTO createPassportDTO){

        PassportEntity savedPP = passportService.createPassport(createPassportDTO);
        ApiResponseDto<PassportEntity> responseEntity = new ApiResponseDto<>(true, "Passport Created Successfully", savedPP);
        return ResponseEntity.ok(responseEntity);
    }

    @PreAuthorize("hasRole('Admin')")
    @GetMapping("/getAll")
    public ResponseEntity<ApiResponseDto<List<PassportEntity>>> getAllPassports(){

        List<PassportEntity> passportEntities = passportService.getAllPassports();
        ApiResponseDto<List<PassportEntity>> responseEntity = new ApiResponseDto<>(true, "Passports fetched Successfully", passportEntities);
        return ResponseEntity.ok(responseEntity);
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<ApiResponseDto<PassportEntity>> getPassportByID(@PathVariable String id){

        PassportEntity passportEntitiy = passportService.getPassportByID(id);
        ApiResponseDto<PassportEntity> responseEntity = new ApiResponseDto<>(true, "Passport fetched Successfully", passportEntitiy);
        return ResponseEntity.ok(responseEntity);
    }

    @GetMapping("/getByUser/{user}")
    public ResponseEntity<ApiResponseDto<List<PassportEntity>>> getPassportsByUser(@PathVariable String user){

        List<PassportEntity> passportEntities = passportService.getPassportsByUser(user);
        ApiResponseDto<List<PassportEntity>> responseEntity = new ApiResponseDto<>(true, "Passports fetched Successfully", passportEntities);
        return ResponseEntity.ok(responseEntity);
    }

    @GetMapping("/getByCertificate/{certificateNumber}")
    public ResponseEntity<ApiResponseDto> getByCertificateID(@PathVariable String certificateNumber){

        PassportEntity passportEntitiy = passportService.getPassportByCertificateNumber(certificateNumber);
        ApiResponseDto<PassportEntity> responseEntity = new ApiResponseDto<PassportEntity>(true, "Passport fetched Successfully", passportEntitiy);
        return ResponseEntity.ok(responseEntity);
    }

    @PutMapping("/updatePassport")
    public  ResponseEntity<ApiResponseDto<PassportEntity>> updatePassport(@Valid @RequestBody UpdatePassportDTO updatePassportDTO){
        PassportEntity updatedPassport = passportService.updatePassport(updatePassportDTO);
        ApiResponseDto<PassportEntity> responseEntity = new ApiResponseDto<>(true,"Passport Updated Successfully", updatedPassport);
        return ResponseEntity.ok(responseEntity);
    }



    @DeleteMapping("/deletePassport/{id}")
    public ResponseEntity<ApiResponseDto<String>> deletePassport(@PathVariable String id){
        passportService.deletePassport(id);
        ApiResponseDto<String> responseEntity = new ApiResponseDto<>(true,"Passport Deleted Successfully", "");
        return ResponseEntity.ok(responseEntity);
    }


    @PostMapping("/addAttachments/{id}")
    public ResponseEntity<ApiResponseDto<AttachmentResponseDTO>> addAttachment(@RequestParam(name = "AttachmentName", required = true) String attachmentName, @RequestParam(name = "AttachmentType", required = true) String attachmentType, @RequestParam(name = "GridingId", required = false) String gridingId,
                                                                               @RequestParam(name = "file", required = true) MultipartFile imageFile, @PathVariable String id) throws IOException {


        AttachmentRequestDTO requestDTO = AttachmentRequestDTO.builder().build();
        requestDTO.setAttachmentName(attachmentName);
        requestDTO.setAttachmentType(attachmentType);
        requestDTO.setGradingID(gridingId);

        Attachment newAttachment = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            String uploadDir = "uploads/passports/";
            String fileName = System.currentTimeMillis() + "_" + imageFile.getOriginalFilename();
            Path filePath = Paths.get(uploadDir, fileName);
            Files.createDirectories(filePath.getParent());
            Files.write(filePath, imageFile.getBytes());

            // Set the image URL in the user entity
            newAttachment = modelMapper.map(requestDTO, Attachment.class);
            newAttachment.setFilePath("/" + uploadDir + fileName);

        }

        AttachmentResponseDTO passportEntity = passportService.addAttachment(id, newAttachment);
        ApiResponseDto<AttachmentResponseDTO> responseEntity = new ApiResponseDto<>(true, "Attachment added successfully", passportEntity);
        return ResponseEntity.ok(responseEntity);
    }

    @DeleteMapping("/deleteAttachment/{passportId}")
    public ResponseEntity<ApiResponseDto<String>> deleteAttachment(@PathVariable String passportId,
                                                           @RequestParam String fileName) {
        // Fetch the Passport entity (assuming you have a service method for this)
        PassportEntity passport = passportService.getPassportByID(passportId);


        try {
            // Call the deleteAttachmentByFileName method
            passportService.deleteAttachment(fileName, passportId);

            return ResponseEntity.ok(new ApiResponseDto(true, "Attachment deleted successfully", ""));
        } catch (RuntimeException ex) {
            throw new RuntimeException("Failed to delete attachment");
        }
    }


}

