package com.nosql.mongo.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class AttachmentRequestDTO {
    @NotBlank(message = "Attachment name is a required filed")
    String attachmentName;
    @NotBlank(message = "Attachment type is a required filed")
    String attachmentType;

    String gradingID;
}
