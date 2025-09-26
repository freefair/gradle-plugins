package io.freefair.javadoc;

import org.hibernate.SessionFactory;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import jakarta.persistence.EntityManagerFactory;

/**
 * Some Documentation {@link String}
 *
 * @author Lars Grefer
 */
public class Dummy extends SpringBootServletInitializer {

    /**
     * @return Foo
     */
    public SessionFactory foo() {
        return null;
    }

    /**
     * @return bar.
     */
    public EntityManagerFactory emf() {
        return null;
    }
}
