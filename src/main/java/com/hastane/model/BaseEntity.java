
package com.hastane.model;

import lombok.Data;
import java.time.LocalDateTime;

// GEREKSİNİM: Abstract Class 1
@Data
public abstract class BaseEntity {
    private int id;
    private LocalDateTime olusturulmaTarihi;

    public BaseEntity() {
        this.olusturulmaTarihi = LocalDateTime.now();
    }
}