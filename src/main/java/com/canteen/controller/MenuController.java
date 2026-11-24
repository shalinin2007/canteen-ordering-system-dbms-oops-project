 package com.canteen.controller;
import com.canteen.model.Category;
import com.canteen.model.FoodItem;
import com.canteen.service.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;package

/**
 * Public REST Controller for browsing the canteen menu and categories.
 */
@RestController
@RequestMapping("/api/menu")
public class MenuController {

    private final MenuService menuService;

    @Autowired
    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    /**
     * Retrieves all items or filters by category ID if provided.
     */
    @GetMapping
    public ResponseEntity<List<FoodItem>> getMenuItems(@RequestParam(required = false) Integer categoryId) {
        if (categoryId != null && categoryId > 0) {
            return ResponseEntity.ok(menuService.getItemsByCategory(categoryId));
        }
        return ResponseEntity.ok(menuService.getAllItems());
    }

    /**
     * Retrieves a single item by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<FoodItem> getItemById(@PathVariable int id) {
        return ResponseEntity.ok(menuService.getItemById(id));
    }

    /**
     * Retrieves all available categories.
     */
    @GetMapping("/categories")
    public ResponseEntity<List<Category>> getCategories() {
        return ResponseEntity.ok(menuService.getCategories());
    }
}
