package org.example;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {
    private static final SessionFactory sessionFactory = buildSessionFactory();

    private static SessionFactory buildSessionFactory() {
        try {
            System.out.println("Attempt to load hibernate.cfg.xml...");

            Configuration configuration = new Configuration();
            configuration.configure("hibernate.cfg.xml"); // Ищет в classpath
            configuration.addAnnotatedClass(User.class);

            SessionFactory factory = configuration.buildSessionFactory();
            System.out.println("SessionFactory created successfully!");
            return factory;

        } catch (Exception e) {
            System.err.println("Creation issue SessionFactory: " + e.getMessage());
            e.printStackTrace();
            throw new ExceptionInInitializerError(e);
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static void shutdown() {
        if (sessionFactory != null) {
            sessionFactory.close();
            System.out.println("SessionFactory closed successfully!");

        }
    }
}

