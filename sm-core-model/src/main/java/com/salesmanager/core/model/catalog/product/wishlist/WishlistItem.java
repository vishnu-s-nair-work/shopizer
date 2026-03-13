package com.salesmanager.core.model.catalog.product.wishlist;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.UniqueConstraint;

import com.salesmanager.core.model.catalog.product.Product;
import com.salesmanager.core.model.generic.SalesManagerEntity;

@Entity
@Table(name = "WISHLIST_ITEM",
       uniqueConstraints = @UniqueConstraint(columnNames = {"WISHLIST_ID", "PRODUCT_ID"}))
public class WishlistItem extends SalesManagerEntity<Long, WishlistItem> {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "WISHLIST_ITEM_ID", unique = true, nullable = false)
    @TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME", valueColumnName = "SEQ_COUNT", pkColumnValue = "WISHLIST_ITEM_SEQ_NEXT_VAL")
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "WISHLIST_ID", nullable = false)
    private Wishlist wishlist;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PRODUCT_ID", nullable = false)
    private Product product;

    @Override
    public Long getId() { return id; }

    @Override
    public void setId(Long id) { this.id = id; }

    public Wishlist getWishlist() { return wishlist; }
    public void setWishlist(Wishlist wishlist) { this.wishlist = wishlist; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
}
