package com.salesmanager.shop.model.catalog.product.wishlist;

import java.io.Serializable;

import com.salesmanager.shop.model.catalog.product.ReadableProductPrice;

public class ReadableWishlistItem implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long productId;
    private String productName;
    private String sku;
    private ReadableProductPrice price;
    private String image;

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public ReadableProductPrice getPrice() { return price; }
    public void setPrice(ReadableProductPrice price) { this.price = price; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
}
