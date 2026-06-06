package crm.repository;

import crm.dao.ActivityDao;
import crm.model.entity.Activity;
import crm.patterns.GenericDao;

import java.util.List;

public class ActivityRepository extends AbstractRepository<Activity> {

    private static volatile ActivityRepository instance;
    private final ActivityDao activityDao;

    private ActivityRepository() {
        this.activityDao = ActivityDao.getInstance();
    }

    public static ActivityRepository getInstance() {
        if (instance == null) {
            synchronized (ActivityRepository.class) {
                if (instance == null) {
                    instance = new ActivityRepository();
                }
            }
        }
        return instance;
    }

    @Override
    protected GenericDao<Activity, Long> getDao() { return activityDao; }

    @Override
    protected String getEntityName() { return "Activity"; }

    public List<Activity> findByContactId(Long contactId) {
        return activityDao.findByContactId(contactId);
    }

    public List<Activity> findByOpportunityId(Long oppId) {
        return activityDao.findByOpportunityId(oppId);
    }

    public List<Activity> findUpcomingForUser(Long userId, int days) {
        return activityDao.findUpcoming(userId, days);
    }
}
