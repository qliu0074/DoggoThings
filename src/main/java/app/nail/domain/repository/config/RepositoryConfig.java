package app.nail.domain.repository.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * JPA Repository配置
 * 启用JPA审计、事务管理等功能
 */
@Configuration
@EnableJpaRepositories(basePackages = "app.nail.domain.repository")
@EnableJpaAuditing
@EnableTransactionManagement
public class RepositoryConfig {
    
    @Autowired
    private EntityManager entityManager;
    
    /**
     * 动态查询构建器
     * 提供灵活的查询条件组合
     */
    public static class DynamicQueryBuilder<T> {
        private final CriteriaBuilder criteriaBuilder;
        private final CriteriaQuery<T> criteriaQuery;
        private final Root<T> root;
        private final List<Predicate> predicates;
        
        public DynamicQueryBuilder(EntityManager em, Class<T> entityClass) {
            this.criteriaBuilder = em.getCriteriaBuilder();
            this.criteriaQuery = criteriaBuilder.createQuery(entityClass);
            this.root = criteriaQuery.from(entityClass);
            this.predicates = new ArrayList<>();
        }
        
        /**
         * 添加等值条件
         */
        public DynamicQueryBuilder<T> eq(String field, Object value) {
            if (value != null) {
                predicates.add(criteriaBuilder.equal(root.get(field), value));
            }
            return this;
        }
        
        /**
         * 添加模糊查询条件
         */
        public DynamicQueryBuilder<T> like(String field, String value) {
            if (value != null && !value.isEmpty()) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get(field)),
                    "%" + value.toLowerCase() + "%"
                ));
            }
            return this;
        }
        
        /**
         * 添加范围查询条件
         */
        public DynamicQueryBuilder<T> between(String field, Comparable min, Comparable max) {
            if (min != null && max != null) {
                predicates.add(criteriaBuilder.between(root.get(field), min, max));
            }
            return this;
        }
        
        /**
         * 添加IN查询条件
         */
        public DynamicQueryBuilder<T> in(String field, List<?> values) {
            if (values != null && !values.isEmpty()) {
                predicates.add(root.get(field).in(values));
            }
            return this;
        }
        
        /**
         * 添加大于条件
         */
        public DynamicQueryBuilder<T> gt(String field, Comparable value) {
            if (value != null) {
                predicates.add(criteriaBuilder.greaterThan(root.get(field), value));
            }
            return this;
        }
        
        /**
         * 添加小于条件
         */
        public DynamicQueryBuilder<T> lt(String field, Comparable value) {
            if (value != null) {
                predicates.add(criteriaBuilder.lessThan(root.get(field), value));
            }
            return this;
        }
        
        /**
         * 添加排序
         */
        public DynamicQueryBuilder<T> orderBy(String field, boolean asc) {
            if (asc) {
                criteriaQuery.orderBy(criteriaBuilder.asc(root.get(field)));
            } else {
                criteriaQuery.orderBy(criteriaBuilder.desc(root.get(field)));
            }
            return this;
        }
        
        /**
         * 构建查询
         */
        public CriteriaQuery<T> build() {
            if (!predicates.isEmpty()) {
                criteriaQuery.where(predicates.toArray(new Predicate[0]));
            }
            return criteriaQuery;
        }
    }
    
    /**
     * 批量操作工具类
     */
    public static class BatchOperationUtil {
        
        /**
         * 批量插入优化
         * 使用JDBC batch size提高性能
         */
        public static <T> void batchInsert(EntityManager em, List<T> entities, int batchSize) {
            int i = 0;
            for (T entity : entities) {
                em.persist(entity);
                i++;
                if (i % batchSize == 0) {
                    em.flush();
                    em.clear();
                }
            }
            em.flush();
            em.clear();
        }
        
        /**
         * 批量更新优化
         */
        public static <T> void batchUpdate(EntityManager em, List<T> entities, int batchSize) {
            int i = 0;
            for (T entity : entities) {
                em.merge(entity);
                i++;
                if (i % batchSize == 0) {
                    em.flush();
                    em.clear();
                }
            }
            em.flush();
            em.clear();
        }
    }
    
    /**
     * 查询性能监控
     */
    public static class QueryPerformanceMonitor {
        
        /**
         * 记录查询执行时间
         */
        public static void logQueryTime(String queryName, long startTime) {
            long executionTime = System.currentTimeMillis() - startTime;
            if (executionTime > 1000) {
                System.err.printf("SLOW QUERY [%s]: %d ms%n", queryName, executionTime);
            }
        }
        
        /**
         * 获取查询统计信息
         */
        public static Map<String, Object> getQueryStatistics(EntityManager em) {
            return Map.of(
                "secondLevelCacheHitCount", em.getEntityManagerFactory().getCache() != null,
                "queryExecutionCount", "N/A" // 需要集成Hibernate Statistics
            );
        }
    }
}