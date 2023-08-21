package com.example.teste.controllers;

import com.example.teste.dtos.ProductRecordDto;
import com.example.teste.models.ProductModel;
import com.example.teste.repositories.ProductRepository;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
public class ProductController {

    @Autowired
    ProductRepository productRepository;

    @PostMapping("/products")
    public ResponseEntity<ProductModel>saveProduct(@RequestBody @Valid ProductRecordDto productRecordDto) {
        var productModel = new ProductModel();
        BeanUtils.copyProperties(productRecordDto, productModel);
        return ResponseEntity.status(HttpStatus.CREATED).body(productRepository.save(productModel));
    }

    @GetMapping("/products")
    public ResponseEntity<List<ProductModel>> getALLProducts() {
        List<ProductModel> productsList = productRepository.findAll();
        if (!productsList.isEmpty()) {
            for (ProductModel product : productsList) {
               UUID id = product.getIdProduct();
               product.add(linkTo(methodOn(ProductController.class).getOneProduct(id)).withSelfRel());
            }
        }
        return ResponseEntity.status(HttpStatus.OK).body(productsList);
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<Object> getOneProduct(@PathVariable(value = "id") UUID id) {
        Optional<ProductModel> product = productRepository.findById(id);
        if (product.isEmpty()) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Produto não encontrado");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        product.get().add(linkTo(methodOn(ProductController.class).getALLProducts()).withRel("Lista de Produtos"));
        return ResponseEntity.status(HttpStatus.OK).body(product.get());
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<Object> updateProduct(@PathVariable(value = "id") UUID id,
                                                @RequestBody @Valid ProductRecordDto productRecordDto) {
        Optional<ProductModel> product = productRepository.findById(id);
        if (product.isEmpty()) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Produto não encontrado");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        var productModel = product.get();
        BeanUtils.copyProperties(productRecordDto, productModel);
        return ResponseEntity.status(HttpStatus.OK).body(productRepository.save(productModel));
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<Object> deleteProduct(@PathVariable(value = "id") UUID id) {
        Optional<ProductModel> product = productRepository.findById(id);
        if (product.isEmpty()) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Produto não encontrado");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        productRepository.delete(product.get());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
