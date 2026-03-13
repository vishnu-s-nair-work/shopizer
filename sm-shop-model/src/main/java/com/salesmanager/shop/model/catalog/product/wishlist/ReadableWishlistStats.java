package com.salesmanager.shop.model.catalog.product.wishlist;

import java.io.Serializable;

public class ReadableWishlistStats implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long productId;
    private String productName;
    private Long wishlistCount;

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public Long getWishlistCount() { return wishlistCount; }
    public void setWishlistCount(Long wishlistCount) { this.wishlistCount = wishlistCount; }
}
