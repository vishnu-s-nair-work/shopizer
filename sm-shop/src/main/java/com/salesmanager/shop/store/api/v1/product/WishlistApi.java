package com.salesmanager.shop.store.api.v1.product;

import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.shop.model.catalog.product.wishlist.ReadableWishlist;
import com.salesmanager.shop.model.catalog.product.wishlist.ReadableWishlistStats;
import com.salesmanager.shop.store.controller.product.facade.WishlistFacade;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;
import springfox.documentation.annotations.ApiIgnore;

@Controller
@RequestMapping("/api/v1")
@Api(tags = {"Wishlist API"})
@SwaggerDefinition(tags = {@Tag(name = "Wishlist resource", description = "Customer wishlist management")})
public class WishlistApi {

    @Inject private WishlistFacade wishlistFacade;

    @GetMapping("/auth/wishlist")
    @ResponseBody
    @ApiImplicitParams({
        @ApiImplicitParam(name = "store", dataType = "string", defaultValue = "DEFAULT"),
        @ApiImplicitParam(name = "lang", dataType = "string", defaultValue = "en")
    })
    public ReadableWishlist getWishlist(
            @ApiIgnore MerchantStore store,
            @ApiIgnore Language language,
            HttpServletRequest request) {
        return wishlistFacade.getWishlist(request.getUserPrincipal().getName(), store, language);
    }

    @PostMapping("/auth/wishlist/{productId}")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiImplicitParams({
        @ApiImplicitParam(name = "store", dataType = "string", defaultValue = "DEFAULT"),
        @ApiImplicitParam(name = "lang", dataType = "string", defaultValue = "en")
    })
    public void addToWishlist(
            @PathVariable Long productId,
            @ApiIgnore MerchantStore store,
            HttpServletRequest request) {
        wishlistFacade.addToWishlist(request.getUserPrincipal().getName(), productId, store);
    }

    @DeleteMapping("/auth/wishlist/{productId}")
    @ResponseStatus(HttpStatus.OK)
    @ApiImplicitParams({
        @ApiImplicitParam(name = "store", dataType = "string", defaultValue = "DEFAULT")
    })
    public void removeFromWishlist(
            @PathVariable Long productId,
            @ApiIgnore MerchantStore store,
            HttpServletRequest request) {
        wishlistFacade.removeFromWishlist(request.getUserPrincipal().getName(), productId, store);
    }

    // Admin stats endpoint
    @GetMapping("/private/wishlist/stats")
    @ResponseBody
    @ApiImplicitParams({
        @ApiImplicitParam(name = "store", dataType = "string", defaultValue = "DEFAULT"),
        @ApiImplicitParam(name = "lang", dataType = "string", defaultValue = "en")
    })
    public List<ReadableWishlistStats> stats(
            @RequestParam(defaultValue = "10") int limit,
            @ApiIgnore MerchantStore store,
            @ApiIgnore Language language) {
        return wishlistFacade.getMostWishlisted(limit, store, language);
    }
}
