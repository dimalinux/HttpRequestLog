/**
 * Copyright (C) 2016, Dmitry Holodov. All rights reserved.
 */
package to.xss.httprequestlog.domain;

import javax.persistence.Entity;
import javax.persistence.Table;


@Entity
@Table(name = "param")
public class ParamEntity extends KeyValueBaseEntity {
    public ParamEntity() {
    }

    public ParamEntity(String name, String value) {
        super(name, value);
    }
}
