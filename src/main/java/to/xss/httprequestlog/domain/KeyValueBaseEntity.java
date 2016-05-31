/**
 * Copyright (C) 2016, Dmitry Holodov. All rights reserved.
 */
package to.xss.httprequestlog.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.io.Serializable;


@MappedSuperclass
@JsonIgnoreProperties({ "request" })
abstract class KeyValueBaseEntity implements Serializable {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "request_id", nullable = false)
    protected RequestEntity request;

    @Size(max = 128)
    @Column(length = 128)
    private String name;

    @Size(max = 2048)
    @Column(name = "val", length = 2048)
    private String value;

    public KeyValueBaseEntity() {
    }

    public KeyValueBaseEntity(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RequestEntity getRequest() {
        return this.request;
    }

    // setRequest(...) intentionally package local. (Only set by HttpRequestEntity).
    void setRequest(RequestEntity device) {
        this.request = device;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer(getClass().getSimpleName());
        sb.append("{id=").append(id);
        sb.append(", request=").append(request.getId());
        sb.append(", name='").append(name).append('\'');
        sb.append(", value='").append(value).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
