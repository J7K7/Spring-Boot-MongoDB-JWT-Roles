package com.nosql.mongo.dtos;

import com.nosql.mongo.model.Attachment;
import lombok.Data;

import java.util.List;

@Data
public class AttachmentResponseDTO {
    String passportID;
    List<Attachment> attachmentList;
}
