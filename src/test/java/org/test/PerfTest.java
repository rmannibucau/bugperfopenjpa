package org.test;

import org.apache.commons.io.FileUtils;
import org.apache.openjpa.kernel.StateManagerImpl;
import org.apache.sirona.counters.Counter;
import org.apache.sirona.repositories.Repository;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PerfTest {
    @Test
    public void run() {
        StateManagerImpl.class.getName();
        Repository.INSTANCE.counters();
        Lazy.run();

    }

    private static class Lazy {
        public static void run() {
            FileUtils.deleteQuietly(new File("testdb"));

            final EntityManagerFactory factory = Persistence.createEntityManagerFactory("test");
            {
                final EntityManager em = factory.createEntityManager();
                em.getTransaction().begin();
                for (int i = 0; i < 10; i++) {
                    final MainEntity e = new MainEntity();
                    e.setValue("##" + i);
                    e.setRelated(new HashSet<RelationshipEntity>());
                    em.persist(e);
                    for (int j = 0; j < 20; j++) {
                        final RelationshipEntity e2 = new RelationshipEntity();
                        e2.setValue("#-#" + i);
                        e2.setMain(e);
                        e.getRelated().add(e2);
                        em.persist(e2);
                    }
                }
                em.getTransaction().commit();
                System.out.println("Created entities");
            }

            final long start = System.nanoTime();
            for (int i = 0; i < 100; i++) {
                final EntityManager em = factory.createEntityManager();
                em.getTransaction().begin();
                final CriteriaBuilder cb = em.getCriteriaBuilder();
                final CriteriaQuery<MainEntity> root = cb.createQuery(MainEntity.class);
                final Root<MainEntity> from = root.from(MainEntity.class);
                from.fetch("related", JoinType.LEFT);
                final CriteriaQuery<MainEntity> select = root.select(from).where(cb.like(from.<String>get("value"), "#%"));
                final List<MainEntity> resultList = em.createQuery(select).getResultList();
                for (final MainEntity e : resultList) {
                    e.getRelated();
                }
                resultList.size();
                em.getTransaction().commit();
                em.close();
            }

            final long end = System.nanoTime();
            System.out.println("OpenJPA " + factory.getClass().getPackage().getImplementationVersion() + " -> " + TimeUnit.NANOSECONDS.toSeconds(end - start) + "s");

            // now we are "hot" so time to get metrics
            final EntityManager em = factory.createEntityManager();
            em.getTransaction().begin();
            Repository.INSTANCE.clearCounters();
            final CriteriaBuilder cb = em.getCriteriaBuilder();
            final CriteriaQuery<MainEntity> root = cb.createQuery(MainEntity.class);
            final Root<MainEntity> from = root.from(MainEntity.class);
            final CriteriaQuery<MainEntity> select = root.select(from);//.where(cb.like(from.<String>get("value"), "#%"));
            final List<MainEntity> resultList = em.createQuery(select).getResultList();

            // query is done dump metrics to ignore close()
            final List<Counter> counters = new ArrayList<Counter>(Repository.INSTANCE.counters());
            Collections.sort(counters, new Comparator<Counter>() {
                public int compare(Counter o1, Counter o2) {
                    return (int) (o2.getHits() - o1.getHits());
                }

            });
            for (final Counter counter : counters) {
                System.out.println(counter.getKey().getName() + ";" + counter.getHits() + ";" + counter.getSum());
            }

            // finish the em lifecycle
            em.getTransaction().commit();
            em.close();

        }

    }
}
