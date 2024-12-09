package com.nosql.mongo.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nosql.mongo.model.Attachment;
import com.nosql.mongo.model.MetaData;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UpdatePassportDTO {
    String Id;
    @NotBlank(message = "Weight is a required field")
    String weight;



    @NotBlank(message = "Recipient is a required field")
    String recipient;


    @NotBlank(message = "Diamond Type is a required field")
    String diamondType;

    @NotNull(message = "Date of origin is a required field")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime dateOfOrigin;

    String targetCertificate;

    List<MetaData> metaDataList;

    List<Attachment> attachmentList;


    @JsonProperty("isPublished")
    boolean isPublished ;

    String blockChainAddress;
}
