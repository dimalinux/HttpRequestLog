/**
 * Copyright (C) 2016, Dmitry Holodov. All rights reserved.
 */
package to.xss.httprequestlog.domain;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "request")
public class RequestEntity implements Serializable {

    @Id
    @GeneratedValue
    private Long id;

    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    @Column(nullable = false)
    private Date serverStamp;

    @Size(max = 45) // 45 = max string length of IPV6 address
    @NotNull
    @Column(length = 45, nullable = false)
    private String remoteIp;

    @Size(max = 255)
    @Column(length = 255)
    private String remoteHost;

    @Min(0)
    private Integer remotePort;

    @NotNull
    @Size(min = 3, max = 7)
    @Column(length = 7, nullable = false)
    String requestMethod; // GET, POST, ...

    @NotNull
    @Size(min = 1, max = 128)
    @Column(length = 128, nullable = false)
    String path;


    @OneToMany(cascade = CascadeType.ALL, mappedBy = "request")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private List<HeaderEntity> requestHeaders = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "request")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private List<ParamEntity> requestParams = new ArrayList<>();


    public RequestEntity(Date serverStamp, String requestMethod, String path) {
        this.serverStamp = serverStamp;
        this.requestMethod = requestMethod;
        this.path = path;
    }

    public RequestEntity() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public Date getServerStamp() {
        return serverStamp;
    }

    public void setServerStamp(Date serverStamp) {
        this.serverStamp = serverStamp;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getRemoteIp() {
        return this.remoteIp;
    }

    public void setRemoteIp(String remoteIp) {
        this.remoteIp = remoteIp;
    }

    public String getRemoteHost() {
        return remoteHost;
    }

    public void setRemoteHost(String remoteHost) {
        this.remoteHost = remoteHost;
    }

    public Integer getRemotePort() {
        return remotePort;
    }

    public void setRemotePort(Integer remotePort) {
        this.remotePort = remotePort;
    }

    public List<HeaderEntity> getRequestHeaders() {
        return requestHeaders;
    }

    public void setRequestHeaders(List<HeaderEntity> requestHeaders) {
        this.requestHeaders = requestHeaders;
    }

    public void addRequestHeader(String name, String value) {
        HeaderEntity header = new HeaderEntity(name, value);
        header.setRequest(this);
        requestHeaders.add(header);
    }

    public List<ParamEntity> getRequestParams() {
        return requestParams;
    }

    public void setRequestParams(List<ParamEntity> requestParams) {
        this.requestParams = requestParams;
    }

    public void addRequestParam(String name, String value) {
        ParamEntity param = new ParamEntity(name, value);
        param.setRequest(this);
        requestParams.add(param);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RequestEntity other = (RequestEntity) obj;
        if (this.id != other.id && (this.id == null || !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return "HttpRequestEntity{id=" + id + "}";
    }

}
