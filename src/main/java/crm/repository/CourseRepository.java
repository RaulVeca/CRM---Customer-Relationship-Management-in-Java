package crm.repository;

import crm.dao.CourseDao;
import crm.model.entity.Course;
import crm.model.enums.CourseCategory;
import crm.patterns.GenericDao;

import java.util.List;
import java.util.Optional;

public class CourseRepository extends AbstractRepository<Course> {

    private static volatile CourseRepository instance;
    private final CourseDao courseDao;

    private CourseRepository() {
        this.courseDao = CourseDao.getInstance();
    }

    public static CourseRepository getInstance() {
        if (instance == null) {
            synchronized (CourseRepository.class) {
                if (instance == null) {
                    instance = new CourseRepository();
                }
            }
        }
        return instance;
    }

    @Override
    protected GenericDao<Course, Long> getDao() { return courseDao; }

    @Override
    protected String getEntityName() { return "Course"; }

    public Optional<Course> findByCode(String code) {
        return courseDao.findByCode(code);
    }

    public List<Course> findActive() {
        return courseDao.findActiveCourses();
    }

    public List<Course> findByCategory(CourseCategory category) {
        return courseDao.findByCategory(category);
    }
}
