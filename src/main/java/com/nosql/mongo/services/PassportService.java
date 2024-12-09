package com.nosql.mongo.services;

import com.nosql.mongo.dtos.AttachmentResponseDTO;
import com.nosql.mongo.dtos.CreatePassportDTO;
import com.nosql.mongo.dtos.UpdatePassportDTO;
import com.nosql.mongo.entities.PassportEntity;
import com.nosql.mongo.model.Attachment;
import com.nosql.mongo.repositories.PassportRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class PassportService {
    private final PassportRepository passportRepository;
    private final ModelMapper modelMapper;

    public PassportService(PassportRepository passportRepository, ModelMapper modelMapper) {
        this.passportRepository = passportRepository;
        this.modelMapper = modelMapper;
    }

    public List<PassportEntity> getAllPassports(){
        return passportRepository.findAll();
    }

    public  PassportEntity getPassportByID(String id){
        return passportRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Passport with this ID not found"));
    }

    public  PassportEntity getPassportByCertificateNumber(String certiID){
        return  passportRepository.findByCertificateNumber(certiID).orElseThrow(() -> new IllegalArgumentException("Passport with this certificate ID not found"));
    }

    public List<PassportEntity> getPassportsByUser(String user){
        List<PassportEntity> senderList = passportRepository.findBySender(user);
        List<PassportEntity> recipientList = passportRepository.findByRecipient(user);
        List<PassportEntity> combinedList = new ArrayList<>();
        combinedList.addAll(senderList);
        combinedList.addAll(recipientList);
        return combinedList;
    }


    public PassportEntity createPassport(CreatePassportDTO createPassportDTO){
        if(passportRepository.existsByCertificateNumber(createPassportDTO.getCertificateNumber())){
            throw new IllegalArgumentException("Passport with this Certificate ID Already exist");
        }

        PassportEntity passportEntity = modelMapper.map(createPassportDTO, PassportEntity.class);
        passportRepository.save(passportEntity);
        return passportEntity;
    }

    public  PassportEntity updatePassport(UpdatePassportDTO updatePassportDTO){

        if(passportRepository.existsById(updatePassportDTO.getId())){

            PassportEntity existingPassport = getPassportByID(updatePassportDTO.getId());
            existingPassport.setAttachmentList(updatePassportDTO.getAttachmentList());
            existingPassport.setDiamondType(updatePassportDTO.getDiamondType());
            existingPassport.setRecipient(updatePassportDTO.getRecipient());
            existingPassport.setPublished(updatePassportDTO.isPublished());
            existingPassport.setWeight(updatePassportDTO.getWeight());
            existingPassport.setDateOfOrigin(updatePassportDTO.getDateOfOrigin());
            existingPassport.setMetaDataList(updatePassportDTO.getMetaDataList());
            existingPassport.setTargetCertificate(updatePassportDTO.getTargetCertificate());
            existingPassport.setBlockChainAddress(updatePassportDTO.getBlockChainAddress());
            return existingPassport;
        }else {
            throw  new IllegalArgumentException("Invalid Passport ID");
        }
    }

    public AttachmentResponseDTO addAttachment(String id, Attachment attachment){

        if(passportRepository.existsById(id)){
            PassportEntity existingPassport = getPassportByID(id);
            List<Attachment> attachmentList = existingPassport.getAttachmentList();
            if (attachmentList == null) attachmentList= new ArrayList<>();
            attachmentList.add(attachment);
            existingPassport.setAttachmentList(attachmentList);
            passportRepository.save(existingPassport);
            AttachmentResponseDTO attachmentResponseDTO = new AttachmentResponseDTO();
            attachmentResponseDTO.setPassportID(existingPassport.getId());
            attachmentResponseDTO.setAttachmentList(existingPassport.getAttachmentList());
            return attachmentResponseDTO;
        }else {
            throw  new IllegalArgumentException("Invalid Passport ID");
        }
    }

    public void deletePassport(String passportID){
        PassportEntity passportEntity = passportRepository.findById(passportID).orElseThrow(() -> new IllegalArgumentException("Passport Not Found!"));
        if(passportEntity.isPublished()){
            throw  new IllegalArgumentException("Passport is published. It can not be delete.");
        }

        List<Attachment> attachments = passportEntity.getAttachmentList();

        if(attachments != null) {
            for (Attachment attachment : attachments) {
                // Remove attachment if found
                if (attachment != null) {
                    Path filePath = Paths.get(System.getProperty("user.dir") + attachment.getFilePath());
                    try {
                        Files.deleteIfExists(filePath);
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to delete file: " + filePath, e);
                    }
                }
            }
        }

        passportRepository.deleteById(passportID);
    }

    public  void deleteAttachment(String fileName,String passportID){
        if(passportRepository.existsById(passportID)) {
            PassportEntity existingPassport = getPassportByID(passportID);
            Attachment attachmentToDelete = null;

            List<Attachment> attachments = existingPassport.getAttachmentList();
            for (Attachment attachment : attachments) {
                if (attachment.getAttachmentName().equals(fileName)) {
                    attachmentToDelete = attachment;
                    break;
                }
            }

            if (attachmentToDelete != null) {
                Path filePath = Paths.get(System.getProperty("user.dir") + attachmentToDelete.getFilePath());
                try {
                    Files.deleteIfExists(filePath);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to delete file: " + filePath, e);
                }

                attachments.remove(attachmentToDelete);
            } else {
                throw new RuntimeException("Attachment with name " + fileName + " not found");
            }

            existingPassport.setAttachmentList(attachments);

            passportRepository.save(existingPassport);

        }else {
            throw  new IllegalArgumentException("Invalid Passport ID");
        }
    }

}
