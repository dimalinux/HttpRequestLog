/**
 * Copyright (C) 2016, Dmitry Holodov. All rights reserved.
 */
package to.xss.httprequestlog.domain;

import javax.persistence.Entity;
import javax.persistence.Table;


@Entity
@Table(name = "header")
public class HeaderEntity extends KeyValueBaseEntity {
    public HeaderEntity() {
    }

    public HeaderEntity(String name, String value) {
        super(name, value);
    }
}
