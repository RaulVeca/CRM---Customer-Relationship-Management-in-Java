package crm.web.controller;

import crm.facade.CrmFacade;
import crm.model.entity.Contact;
import crm.model.enums.CourseCategory;
import crm.web.dto.PublicCompanyDto;
import crm.web.dto.PublicCourseDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

/**
 * Public, unauthenticated catalog for the marketing website.
 *
 * <p>Lets prospective individual visitors (and investors) browse the available
 * course types and the business activity of the companies the training firm
 * works with - one of the mandatory web-app requirements.</p>
 */
@RestController
@RequestMapping("/api/public")
public class PublicCatalogController {

    private static final int MAX_PUBLIC_CONTACTS = 500;

    private final CrmFacade facade;

    public PublicCatalogController(CrmFacade facade) {
        this.facade = facade;
    }

    @GetMapping("/courses")
    public List<PublicCourseDto> courses(@RequestParam(required = false) CourseCategory category) {
        var source = category != null
                ? facade.getCoursesByCategory(category)
                : facade.getActiveCourses();
        return source.stream().map(PublicCourseDto::from).toList();
    }

    @GetMapping("/categories")
    public List<CategoryDto> categories() {
        return Arrays.stream(CourseCategory.values())
                .map(c -> new CategoryDto(c.name(), c.getLabel()))
                .toList();
    }

    @GetMapping("/companies")
    public List<PublicCompanyDto> companies() {
        return facade.getAllContacts(0, MAX_PUBLIC_CONTACTS).stream()
                .filter(Contact::isCorporate)
                .filter(c -> c.getCompanyName() != null)
                .map(PublicCompanyDto::from)
                .toList();
    }

    public record CategoryDto(String name, String label) {}
}
