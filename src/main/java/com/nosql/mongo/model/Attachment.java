package com.nosql.mongo.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class Attachment {

    @NotBlank(message = "Attachment name is a required filed")
    String attachmentName;
    @NotBlank(message = "Attachment type is a required filed")
    String attachmentType;

    String gradingID;

    @NotBlank
    String filePath;


}
