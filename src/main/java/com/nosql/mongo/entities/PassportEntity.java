package com.nosql.mongo.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nosql.mongo.model.Attachment;
import com.nosql.mongo.model.MetaData;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "passport")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor(access = AccessLevel.NONE)
@Builder
public class PassportEntity {
    @Id
    String id;

    @NotBlank(message = "Certificate ID is a required field")
    String certificateNumber;

    @NotBlank(message = "Weight is a required field")
    String weight;

    @NotBlank(message = "Sender is a required field")
    String sender;

    @NotBlank(message = "Recipient is a required field")
    String recipient;

    @NotBlank(message = "Owner name is a required field")
    String ownerName;

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
