package com.example.controller;

import com.example.dto.UserProfileDTO;
import com.example.dto.UserRegistrationDTO;
import com.example.model.Image;
import com.example.model.User;
import com.example.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/user")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final ProductService productService;
    private final CartService cartService;
    private final OrderService orderService;
    private final ImageService imageService;

    @Autowired
    public UserController(UserService userService, ProductService productService,
                          CartService cartService, OrderService orderService,
                          ImageService imageService) {
        this.userService = userService;
        this.productService = productService;
        this.cartService = cartService;
        this.orderService = orderService;
        this.imageService = imageService;
    }

    @GetMapping("/dashboard")
    public String userDashboard(Model model, Authentication authentication) {
        String username = authentication.getName();
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        model.addAttribute("user", user);
        model.addAttribute("bookmarkedProducts", productService.findBookmarkedProducts(username));
        model.addAttribute("orders", orderService.getOrdersByUserId(username));
        addImageAttributes(model);
        return "user/dashboard";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new UserRegistrationDTO());
        addImageAttributes(model);
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") UserRegistrationDTO userDTO,
                               BindingResult result,
                               @RequestParam(value = "adminCode", required = false) String adminCode,
                               Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            addImageAttributes(model);
            return "register";
        }
        try {
            userService.registerUser(userDTO, adminCode);
            redirectAttributes.addFlashAttribute("success", "Registration successful! Please log in.");
            return "redirect:/login";
        } catch (IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
            addImageAttributes(model);
            return "register";
        }
    }

    @GetMapping("/profile")
    public String userProfile(Model model, Authentication authentication) {
        String username = authentication.getName();
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        UserProfileDTO profileDTO = new UserProfileDTO();
        profileDTO.setFullName(user.getFullName());
        profileDTO.setEmail(user.getEmail());
        model.addAttribute("user", profileDTO);
        model.addAttribute("bookmarkedProducts", productService.findBookmarkedProducts(username));
        model.addAttribute("orders", orderService.getOrdersByUserId(username));
        addImageAttributes(model);
        return "user/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@Valid @ModelAttribute("user") UserProfileDTO profileDTO,
                                BindingResult result, Authentication authentication,
                                Model model, RedirectAttributes redirectAttributes) {
        String username = authentication.getName();
        if (result.hasErrors()) {
            model.addAttribute("bookmarkedProducts", productService.findBookmarkedProducts(username));
            model.addAttribute("orders", orderService.getOrdersByUserId(username));
            addImageAttributes(model);
            return "user/profile";
        }
        try {
            userService.updateProfile(username, profileDTO);
            redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");
            return "redirect:/user/profile";
        } catch (IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("bookmarkedProducts", productService.findBookmarkedProducts(username));
            model.addAttribute("orders", orderService.getOrdersByUserId(username));
            addImageAttributes(model);
            return "user/profile";
        }
    }

    @PostMapping("/bookmark/remove")
    public String removeBookmark(@RequestParam String productId, Authentication authentication) {
        String username = authentication.getName();
        productService.removeBookmark(productId, username);
        return "redirect:/user/profile";
    }

    @GetMapping("/check-username")
    @ResponseBody
    public boolean checkUsername(@RequestParam String username) {
        return userService.findByUsername(username).isEmpty();
    }

    @GetMapping("/check-email")
    @ResponseBody
    public boolean checkEmail(@RequestParam String email) {
        return userService.findByEmail(email).isEmpty();
    }

    private void addImageAttributes(Model model) {
        Image logoImage = imageService.findByName("apple-icon.png");
        Image faviconImage = imageService.findByName("favicon.ico");
        model.addAttribute("logoImage", logoImage != null ? logoImage : createEmptyImage("apple-icon.png"));
        model.addAttribute("faviconImage", faviconImage != null ? faviconImage : createEmptyImage("favicon.ico"));
        logger.debug("Added image attributes for user page");
    }

    private Image createEmptyImage(String name) {
        logger.warn("Image not found for name: {}", name);
        Image image = new Image();
        image.setName(name);
        image.setContentType("image/png");
        image.setData(new byte[0]);
        return image;
    }
}