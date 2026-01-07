package com.inventory.invflow.dto.item;

import com.inventory.invflow.entity.Item;
import com.inventory.invflow.enums.WineType;

public record ItemResponse(
    String itemId,
    String sku,
    String name,
    WineType wineType,
    String originCountry,
    Integer vintage,
    Integer volumeMl,
    Integer stock,
    Boolean enabled,
    String supplierId,
    String createdBy,
    String updatedBy
) 
{
    public static ItemResponse fromEntity (Item item){
        return new ItemResponse(
            item.getItemId(),
            item.getSku(),
            item.getName(), 
            item.getWineType(), 
            item.getOriginCountry(), 
            item.getVintage(), 
            item.getVolumeMl(), 
            item.getStock(),
            item.getEnabled(),
            item.getSupplier().getSupplierId(),
            item.getCreatedBy(),
            item.getUpdatedBy()
        );
    }
} 

