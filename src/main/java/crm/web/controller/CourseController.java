package crm.web.controller;

import crm.facade.CrmFacade;
import crm.model.entity.Course;
import crm.model.enums.CourseCategory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST endpoints for the course catalog (internal/admin view).
 * The public-facing, read-only catalog lives in {@link PublicCatalogController}.
 */
@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CrmFacade facade;

    public CourseController(CrmFacade facade) {
        this.facade = facade;
    }

    @GetMapping
    public List<Course> list(@RequestParam(required = false) CourseCategory category) {
        return category != null ? facade.getCoursesByCategory(category) : facade.getActiveCourses();
    }

    @GetMapping("/{id}")
    public Course getById(@PathVariable Long id) {
        return facade.getCourse(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Course create(@RequestBody Course course) {
        return facade.createCourse(course);
    }

    @PutMapping("/{id}")
    public Course update(@PathVariable Long id, @RequestBody Course course) {
        course.setId(id);
        return facade.updateCourse(course);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        facade.deactivateCourse(id);
        return ResponseEntity.noContent().build();
    }
}
