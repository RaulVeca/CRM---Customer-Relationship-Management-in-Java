package crm.service.course;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import crm.exception.BusinessException;
import crm.model.entity.Course;
import crm.model.enums.CourseCategory;
import crm.repository.CourseRepository;

import java.util.List;
import java.util.Optional;

/**
 * SINGLETON PATTERN - CourseService
 */
public class CourseService {

    private static final Logger logger = LoggerFactory.getLogger(CourseService.class);
    private static volatile CourseService instance;

    private final CourseRepository courseRepository;

    private CourseService() {
        this.courseRepository = CourseRepository.getInstance();
    }

    public static CourseService getInstance() {
        if (instance == null) {
            synchronized (CourseService.class) {
                if (instance == null) {
                    instance = new CourseService();
                }
            }
        }
        return instance;
    }

    public Course createCourse(Course course) {
        if (course.getCode() == null || course.getCode().isEmpty()) {
            throw new BusinessException("Codul cursului este obligatoriu");
        }
        if (courseRepository.findByCode(course.getCode()).isPresent()) {
            throw new BusinessException("Cod curs duplicat: " + course.getCode());
        }
        if (course.getName() == null || course.getName().isEmpty()) {
            throw new BusinessException("Numele cursului este obligatoriu");
        }
        if (course.getDurationHours() == null || course.getDurationHours() <= 0) {
            throw new BusinessException("Durata cursului trebuie să fie pozitivă");
        }

        if (course.getActive() == null) course.setActive(true);
        if (course.getMinParticipants() == null) course.setMinParticipants(3);
        if (course.getMaxParticipants() == null) course.setMaxParticipants(15);

        Course saved = courseRepository.save(course);
        logger.info("Curs creat: {} - {}", saved.getCode(), saved.getName());
        return saved;
    }

    public Course updateCourse(Course course) {
        if (course.getId() == null) {
            throw new BusinessException("Course ID este obligatoriu");
        }
        courseRepository.getById(course.getId()); // verifică existența
        return courseRepository.save(course);
    }

    public Optional<Course> findByCode(String code) {
        return courseRepository.findByCode(code);
    }

    public Course getById(Long id) {
        return courseRepository.getById(id);
    }

    public List<Course> getAllActiveCourses() {
        return courseRepository.findActive();
    }

    public List<Course> getByCategory(CourseCategory category) {
        return courseRepository.findByCategory(category);
    }

    public void deactivateCourse(Long id) {
        Course course = courseRepository.getById(id);
        course.setActive(false);
        courseRepository.save(course);
        logger.info("Curs dezactivat: {}", course.getCode());
    }
}
