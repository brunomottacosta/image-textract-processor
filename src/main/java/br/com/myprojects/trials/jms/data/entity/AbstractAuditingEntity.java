package br.com.myprojects.trials.jms.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.time.Instant;

public abstract class AbstractAuditingEntity implements Serializable {

    @JsonIgnore
    @CreatedDate
    @Field("created_date")
    private Instant createdDate;

    @JsonIgnore
    @LastModifiedDate
    @Field("last_modified_date")
    private Instant lastModifiedDate;

    @Version
    @JsonIgnore
    private Long version;

}
