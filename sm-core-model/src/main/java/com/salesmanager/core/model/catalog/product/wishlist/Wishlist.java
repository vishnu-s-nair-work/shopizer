package com.salesmanager.core.model.catalog.product.wishlist;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import com.salesmanager.core.model.customer.Customer;
import com.salesmanager.core.model.generic.SalesManagerEntity;

@Entity
@Table(name = "WISHLIST")
public class Wishlist extends SalesManagerEntity<Long, Wishlist> {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "WISHLIST_ID", unique = true, nullable = false)
    @TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME", valueColumnName = "SEQ_COUNT", pkColumnValue = "WISHLIST_SEQ_NEXT_VAL")
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CUSTOMER_ID", nullable = false, unique = true)
    private Customer customer;

    @OneToMany(mappedBy = "wishlist", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<WishlistItem> items = new HashSet<>();

    @Override
    public Long getId() { return id; }

    @Override
    public void setId(Long id) { this.id = id; }

    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }

    public Set<WishlistItem> getItems() { return items; }
    public void setItems(Set<WishlistItem> items) { this.items = items; }
}
