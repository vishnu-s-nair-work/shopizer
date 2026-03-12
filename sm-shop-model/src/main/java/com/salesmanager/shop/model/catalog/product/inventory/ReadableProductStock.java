package com.salesmanager.shop.model.catalog.product.inventory;

import java.io.Serializable;

public class ReadableProductStock implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long productId;
    private boolean inStock;
    private int quantity;

    public ReadableProductStock() {}

    public ReadableProductStock(Long productId, boolean inStock, int quantity) {
        this.productId = productId;
        this.inStock = inStock;
        this.quantity = quantity;
    }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public boolean isInStock() { return inStock; }
    public void setInStock(boolean inStock) { this.inStock = inStock; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
