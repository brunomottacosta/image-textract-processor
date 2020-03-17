package br.com.myprojects.trials.jms.data.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@Builder
@EqualsAndHashCode(of = {"id"}, callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Document("image-text")
public class ImageText extends AbstractAuditingEntity {

    @Id
    private String id;

    @NonNull
    @Indexed(unique = true)
    @Field("s3_object_key")
    private String s3ObjectKey;

    @NonNull
    @Field("s3_bucket")
    private String s3Bucket;

    @Field("image_byte_length")
    private long imageByteLength;

    @Field("extracted_text_blocks")
    private Object extractedTextBlocks;

}
