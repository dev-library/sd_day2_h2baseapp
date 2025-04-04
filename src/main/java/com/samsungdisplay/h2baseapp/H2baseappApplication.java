package com.samsungdisplay.h2baseapp;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.samsungdisplay.h2baseapp.entity.Product;
import com.samsungdisplay.h2baseapp.repository.ProductRepository;

@SpringBootApplication
public class H2baseappApplication {

	public static void main(String[] args) {
		SpringApplication.run(H2baseappApplication.class, args);
	}
	
    @Bean
    public CommandLineRunner initProducts(ProductRepository repository) {
        return args -> {
            if (repository.count() > 0) {
                System.out.println("이미 데이터가 존재하므로 초기화를 건너뜁니다.");
                return;
            }

            System.out.println("H2 DB에 더미 Product 데이터 30만 건 삽입 시작...");

            String[] baseNames = {"Laptop", "Phone", "Tablet", "Shoes", "Jacket", "Cookware", "Headphones", "Monitor", "Chair", "Desk"};
            String[] brands = {"BrandA", "BrandB", "BrandC", "BrandD", "BrandE"};
            String[] categories = {"Electronics", "Fashion", "Kitchen", "Office", "Furniture"};
            Random rand = new Random();

            int batchSize = 1000;
            List<Product> buffer = new ArrayList<>();

            for (int i = 1; i < 300_000; i++) {
                String baseName = baseNames[rand.nextInt(baseNames.length)];
                String brand = brands[rand.nextInt(brands.length)];
                String category = categories[rand.nextInt(categories.length)];

                String name = baseName + " Model " + (100 + rand.nextInt(900));
                Long id = (long)i;
                Integer price = 10000 + rand.nextInt(990000); // 1만원 ~ 100만원
                Double rating = Math.round((1.0 + rand.nextDouble() * 4.0) * 10.0) / 10.0; // 1.0 ~ 5.0

                Product product = Product.builder()
                	    .name(name)
                	    .brand(brand)
                	    .price(price)
                	    .rating(rating)
                	    .build();

                buffer.add(product);

                if (buffer.size() == batchSize) {
                    repository.saveAll(buffer);
                    buffer.clear();
                }
            }

            // 마지막 남은 데이터 삽입
            if (!buffer.isEmpty()) {
                repository.saveAll(buffer);
            }

            System.out.println("더미 데이터 삽입 완료!");
        };
    }

}
