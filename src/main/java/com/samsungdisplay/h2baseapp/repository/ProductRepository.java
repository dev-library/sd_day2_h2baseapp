package com.samsungdisplay.h2baseapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.samsungdisplay.h2baseapp.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
