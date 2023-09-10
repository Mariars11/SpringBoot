package com.example.springboot.controllers;

import com.example.springboot.dtos.ProductRecordDto;
import com.example.springboot.models.ProductModel;
import com.example.springboot.repositories.ProductRepository;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
public class ProductController
{
    @Autowired
    ProductRepository productRepository;

    //Post
    @PostMapping("/products")
    public ResponseEntity<ProductModel> saveProduct(@RequestBody @Valid ProductRecordDto productRecordDto){
        ProductModel productModel = new ProductModel();
        BeanUtils.copyProperties(productRecordDto, productModel);

        return ResponseEntity.status(HttpStatus.CREATED).body(productRepository.save(productModel));
    }
    //Get
    @GetMapping("/products")
    public ResponseEntity<List<ProductModel>> getAllProducts(){
        List<ProductModel> productModelList = productRepository.findAll();

        if(!productModelList.isEmpty())
        {
            for (ProductModel product: productModelList)
            {
                UUID id = product.getIdProduct();
                product.add(linkTo(methodOn(ProductController.class).getOneProduct(id)).withSelfRel());
            }
        }
        return ResponseEntity.status(HttpStatus.OK).body(productModelList);
    }
    @GetMapping("/products/{id}")
    public ResponseEntity<Object> getOneProduct(@PathVariable(value = "id") UUID id){
        Optional<ProductModel> productO = productRepository.findById(id);

        if(productO.isEmpty()){
            ResponseEntity.status(HttpStatus.NOT_FOUND).body("Produto não encontrado!");
        }
        productO.get().add(linkTo(methodOn(ProductController.class).getAllProducts()).withRel("Lista de Produtos"));
        return ResponseEntity.status(HttpStatus.OK).body(productO.get());
    }
    //Put
    @PutMapping("/products/{id}")
    public ResponseEntity<Object> updateProduct(@PathVariable(value = "id") UUID id, @RequestBody @Valid ProductRecordDto productRecordDto){
        Optional<ProductModel> productO = productRepository.findById(id);

        if(productO.isEmpty()){
            ResponseEntity.status(HttpStatus.NOT_FOUND).body("Produto não encontrado!");
        }
        ProductModel productModel = new ProductModel();
        if(productO.isPresent())
        {
            productModel = productO.get();
            BeanUtils.copyProperties(productRecordDto, productModel);
        }

        return ResponseEntity.status(HttpStatus.OK).body(productRepository.save(productModel));
    }
    //Delete
    @DeleteMapping("/products/{id}")
    public ResponseEntity<Object> deleteProduct(@PathVariable(value = "id") UUID id){
        Optional<ProductModel> productO = productRepository.findById(id);

        if(productO.isEmpty()){
            ResponseEntity.status(HttpStatus.NOT_FOUND).body("Produto não encontrado!");
        }
        productO.ifPresent(productModel -> productRepository.delete(productModel));

        return ResponseEntity.status(HttpStatus.OK).body("Produto deletado com sucesso!");
    }
}
