package com.samsungdisplay.h2baseapp.controller;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.samsungdisplay.h2baseapp.entity.Product;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class SearchController {

    private final EntityManager em;

    @GetMapping("/")
    public String index() {
        return "main";  // 템플릿 이름
    }

    @GetMapping("/search")
    public String search(
    	    @RequestParam(name = "keyword", required = false) String keyword,
    	    @RequestParam(name = "minPrice", required = false) Integer minPrice,
    	    @RequestParam(name = "maxPrice", required = false) Integer maxPrice,
    	    @RequestParam(name = "minRating", required = false) Double minRating,
    	    @RequestParam(name = "maxRating", required = false) Double maxRating,
    	    @RequestParam(name = "sort", required = false) String sort,
    	    @RequestParam(name = "page", defaultValue = "0") int page,
    	    @RequestParam(name = "size", defaultValue = "10") int size,
    	    Model model
    	) {
    	    Instant start = Instant.now();

    	    String jpql = "SELECT p FROM Product p WHERE 1=1";
    	    String countJpql = "SELECT COUNT(p) FROM Product p WHERE 1=1";

    	    StringBuilder where = new StringBuilder();

    	    if (keyword != null && !keyword.isEmpty()) {
    	        where.append(" AND (LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) ");
    	        where.append(" OR LOWER(p.brand) LIKE LOWER(CONCAT('%', :keyword, '%')) )");
    	    }
    	    if (minPrice != null) {
    	        where.append(" AND p.price >= :minPrice");
    	    }
    	    if (maxPrice != null) {
    	        where.append(" AND p.price <= :maxPrice");
    	    }
    	    if (minRating != null) {
    	        where.append(" AND p.rating >= :minRating");
    	    }
    	    if (maxRating != null) {
    	        where.append(" AND p.rating <= :maxRating");
    	    }

    	    jpql += where;
    	    countJpql += where;

    	    // ✅ 정렬 조건 추가
    	    if (sort != null) {
    	        switch (sort) {
    	            case "priceAsc":
    	                jpql += " ORDER BY p.price ASC";
    	                break;
    	            case "priceDesc":
    	                jpql += " ORDER BY p.price DESC";
    	                break;
    	            case "ratingAsc":
    	                jpql += " ORDER BY p.rating ASC";
    	                break;
    	            case "ratingDesc":
    	                jpql += " ORDER BY p.rating DESC";
    	                break;
    	        }
    	    }

    	    TypedQuery<Product> query = em.createQuery(jpql, Product.class);
    	    TypedQuery<Long> countQuery = em.createQuery(countJpql, Long.class);

    	    if (keyword != null && !keyword.isEmpty()) {
    	        query.setParameter("keyword", keyword);
    	        countQuery.setParameter("keyword", keyword);
    	    }
    	    if (minPrice != null) {
    	        query.setParameter("minPrice", minPrice);
    	        countQuery.setParameter("minPrice", minPrice);
    	    }
    	    if (maxPrice != null) {
    	        query.setParameter("maxPrice", maxPrice);
    	        countQuery.setParameter("maxPrice", maxPrice);
    	    }
    	    if (minRating != null) {
    	        query.setParameter("minRating", minRating);
    	        countQuery.setParameter("minRating", minRating);
    	    }
    	    if (maxRating != null) {
    	        query.setParameter("maxRating", maxRating);
    	        countQuery.setParameter("maxRating", maxRating);
    	    }

    	    int totalElements = countQuery.getSingleResult().intValue();
    	    query.setFirstResult(page * size);
    	    query.setMaxResults(size);

    	    List<Product> resultList = query.getResultList();
    	    Page<Product> resultPage = new PageImpl<>(resultList, PageRequest.of(page, size), totalElements);

    	    Instant end = Instant.now();
    	    long elapsedMillis = Duration.between(start, end).toMillis();

    	    model.addAttribute("products", resultPage);
    	    model.addAttribute("keyword", keyword);
    	    model.addAttribute("minPrice", minPrice);
    	    model.addAttribute("maxPrice", maxPrice);
    	    model.addAttribute("minRating", minRating);
    	    model.addAttribute("maxRating", maxRating);
    	    model.addAttribute("sort", sort); // 👉 현재 정렬 상태 유지
    	    model.addAttribute("searchTime", elapsedMillis);

    	    return "main";
    }
}