package com.example.controller;

import com.example.dto.OrderDTO;
import com.example.model.Category;
import com.example.model.Image;
import com.example.model.Product;
import com.example.service.ProductService;
import com.example.service.CategoryService;
import com.example.service.ImageService;
import com.example.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
    private final ProductService productService;
    private final CategoryService categoryService;
    private final ImageService imageService;
    private final OrderService orderService;

    @Autowired
    public AdminController(ProductService productService, CategoryService categoryService,
                           ImageService imageService, OrderService orderService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.imageService = imageService;
        this.orderService = orderService;
    }

    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        List<Product> products = productService.findAll(null, null).getContent();
        List<Map<String, Object>> productData = products.stream().map(product -> {
            Map<String, Object> data = new HashMap<>();
            data.put("id", product.getId());
            data.put("name", product.getName());
            data.put("price", product.getPrice());
            data.put("stockQuantity", product.getStockQuantity());
            String categoryName = "N/A";
            if (product.getCategory() != null) {
                Category category = categoryService.findById(product.getCategory());
                categoryName = category != null ? category.getName() : "Unknown";
            }
            data.put("categoryName", categoryName);
            logger.info("Product ID: {}, Name: {}, Category: {}",
                    product.getId(), product.getName(), categoryName);
            if (product.getId() == null || product.getId().contains("{")) {
                logger.warn("Invalid product ID detected: {}", product.getId());
            }
            return data;
        }).collect(Collectors.toList());
        model.addAttribute("products", productData);
        return "admin/admin-dashboard";
    }

    @GetMapping("/admin-dashboard")
    public String redirectToDashboard() {
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/products/add")
    public String addProductForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.findAll());
        return "admin/add-product";
    }

    @PostMapping("/products/add")
    public String addProduct(@ModelAttribute Product product,
                             @RequestParam("imageFile") MultipartFile imageFile,
                             RedirectAttributes redirectAttributes) {
        try {
            if (!imageFile.isEmpty()) {
                Image savedImage = imageService.storeImage(imageFile);
                product.setImageId(savedImage.getId());
            } else {
                redirectAttributes.addFlashAttribute("error", "Please upload an image for the product.");
                return "redirect:/admin/products/add";
            }

            if (product.getSizes() != null && !product.getSizes().isEmpty() && product.getSizes().get(0) != null) {
                product.setSizes(Arrays.stream(product.getSizes().get(0).split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toList()));
            } else {
                product.setSizes(Collections.emptyList());
            }

            if (product.getColors() != null && !product.getColors().isEmpty() && product.getColors().get(0) != null) {
                product.setColors(Arrays.stream(product.getColors().get(0).split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toList()));
            } else {
                product.setColors(Collections.emptyList());
            }

            if (product.getSpecifications() != null && !product.getSpecifications().isEmpty() && product.getSpecifications().get(0) != null) {
                product.setSpecifications(Arrays.stream(product.getSpecifications().get(0).split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toList()));
            } else {
                product.setSpecifications(Collections.emptyList());
            }

            productService.save(product);
            redirectAttributes.addFlashAttribute("success", "Product added successfully!");
            return "redirect:/admin/dashboard";
        } catch (IOException e) {
            logger.error("Error uploading image: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Failed to upload image: " + e.getMessage());
            return "redirect:/admin/products/add";
        }
    }

    @DeleteMapping("/products/{id}")
    @ResponseBody
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteProductApi(@PathVariable String id) {
        Map<String, String> response = new HashMap<>();
        try {
            Product product = productService.findById(id);
            if (product == null) {
                logger.warn("Attempted to delete non-existent product with ID: {}", id);
                response.put("status", "error");
                response.put("message", "Product not found.");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }

            if (product.getImageId() != null && !product.getImageId().isEmpty()) {
                imageService.deleteImage(product.getImageId());
                logger.info("Associated image with ID {} deleted for product {}.", product.getImageId(), id);
            }

            productService.deleteById(id);
            logger.info("Product with ID {} deleted successfully via API.", id);
            response.put("status", "success");
            response.put("message", "Product deleted successfully!");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error deleting product with ID {} via API: {}", id, e.getMessage(), e);
            response.put("status", "error");
            response.put("message", "Failed to delete product: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/products/view/{id}")
    public String viewProduct(@PathVariable String id, Model model) {
        Product product = productService.findById(id);
        if (product == null) {
            logger.warn("Product with id {} not found for view", id);
            return "redirect:/admin/dashboard";
        }

        model.addAttribute("product", product);

        if (product.getCategory() != null && !product.getCategory().isEmpty()) {
            Category category = categoryService.findById(product.getCategory());
            if (category != null) {
                model.addAttribute("categoryName", category.getName());
            } else {
                logger.warn("Category with ID {} not found for product {}", product.getCategory(), product.getId());
                model.addAttribute("categoryName", "Unknown Category");
            }
        } else {
            model.addAttribute("categoryName", "N/A");
        }

        if (product.getImageId() != null && !product.getImageId().isEmpty()) {
            Image image = imageService.getImage(product.getImageId());
            if (image != null) {
                model.addAttribute("productImageUrl", "/image/" + image.getId());
            } else {
                logger.warn("Image with ID {} not found for product {}", product.getImageId(), product.getId());
            }
        }

        return "admin/view-product";
    }

    @GetMapping("/products/update/{id}")
    public String updateProductForm(@PathVariable String id, Model model) {
        Product product = productService.findById(id);
        if (product == null) {
            logger.warn("Product with id {} not found", id);
            return "redirect:/admin/dashboard";
        }
        product.setSizes(product.getSizes() != null ? Collections.singletonList(String.join(",", product.getSizes())) : Collections.emptyList());
        product.setColors(product.getColors() != null ? Collections.singletonList(String.join(",", product.getColors())) : Collections.emptyList());
        product.setSpecifications(product.getSpecifications() != null ? Collections.singletonList(String.join(",", product.getSpecifications())) : Collections.emptyList());

        model.addAttribute("product", product);
        model.addAttribute("categories", categoryService.findAll());
        return "admin/update-product";
    }

    @PostMapping("/products/update/{id}")
    public String updateProduct(@PathVariable String id,
                                @ModelAttribute Product product,
                                @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                                RedirectAttributes redirectAttributes) {
        try {
            Product existingProduct = productService.findById(id);
            if (existingProduct == null) {
                logger.warn("Product with id {} not found for update", id);
                redirectAttributes.addFlashAttribute("error", "Product not found.");
                return "redirect:/admin/dashboard";
            }

            existingProduct.setName(product.getName());
            existingProduct.setCategory(product.getCategory());
            existingProduct.setPrice(product.getPrice());
            existingProduct.setStockQuantity(product.getStockQuantity());
            existingProduct.setDescription(product.getDescription());
            existingProduct.setBrand(product.getBrand());
            existingProduct.setRating(product.getRating());
            existingProduct.setReviewCount(product.getReviewCount());

            if (imageFile != null && !imageFile.isEmpty()) {
                Image savedImage = imageService.storeImage(imageFile);
                existingProduct.setImageId(savedImage.getId());
            }

            if (product.getSizes() != null && !product.getSizes().isEmpty() && product.getSizes().get(0) != null) {
                existingProduct.setSizes(Arrays.stream(product.getSizes().get(0).split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toList()));
            } else {
                existingProduct.setSizes(Collections.emptyList());
            }

            if (product.getColors() != null && !product.getColors().isEmpty() && product.getColors().get(0) != null) {
                existingProduct.setColors(Arrays.stream(product.getColors().get(0).split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toList()));
            } else {
                existingProduct.setColors(Collections.emptyList());
            }

            if (product.getSpecifications() != null && !product.getSpecifications().isEmpty() && product.getSpecifications().get(0) != null) {
                existingProduct.setSpecifications(Arrays.stream(product.getSpecifications().get(0).split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toList()));
            } else {
                existingProduct.setSpecifications(Collections.emptyList());
            }

            productService.save(existingProduct);
            redirectAttributes.addFlashAttribute("success", "Product updated successfully!");
            return "redirect:/admin/dashboard";
        } catch (IOException e) {
            logger.error("Error uploading image during update: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Failed to upload image: " + e.getMessage());
            return "redirect:/admin/products/update/" + id;
        }
    }

    @GetMapping("/orders")
    @PreAuthorize("hasRole('ADMIN')")
    public String viewOrders(Model model) {
        model.addAttribute("orders", orderService.getAllOrders());
        Image logoImage = imageService.findByName("apple-icon.png");
        Image faviconImage = imageService.findByName("favicon.ico");
        model.addAttribute("logoImage", logoImage);
        model.addAttribute("faviconImage", faviconImage);
        return "admin/orders";
    }

    @GetMapping("/orders/view/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String viewOrder(@PathVariable String id, Model model) {
        OrderDTO order = orderService.getOrderById(id);
        model.addAttribute("order", order);
        return "admin/order-view";
    }

    @GetMapping("/settings")
    @PreAuthorize("hasRole('ADMIN')")
    public String viewSettings(Model model) {
        String currentTheme = "light"; // Default theme
        model.addAttribute("currentTheme", currentTheme);
        return "admin/settings";
    }
}