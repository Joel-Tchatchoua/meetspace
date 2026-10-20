package com.accenture.meetspace.shared.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;
import jakarta.persistence.Version;
import lombok.Getter;
import org.springframework.data.domain.Persistable;

@Getter
@MappedSuperclass
public abstract class VersionModel extends BaseModel implements Persistable<String> {

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Override
    @Transient
    public boolean isNew() {
        return getId() == null;
    }
}
