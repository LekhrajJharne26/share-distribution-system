package com.example.sharedistribution.controller;

import com.example.sharedistribution.entity.Customer;
import com.example.sharedistribution.repository.CustomerRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerRepository repo;

    public CustomerController(CustomerRepository repo) {
        this.repo = repo;
    }

    @PostMapping
    public ResponseEntity<Customer> create(@RequestBody Customer body) {
        if (body.getName() == null || body.getName().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        Customer saved = repo.save(body);
        return ResponseEntity.ok(saved);
    }

    @GetMapping
    public ResponseEntity<List<Customer>> list() {
        return ResponseEntity.ok(repo.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Customer> get(@PathVariable Long id) {
        return repo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
