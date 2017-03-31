/*
 * The MIT License
 *
 * Original work sponsored and donated by National Board of e-Health (NSI), Denmark (http://www.nsi.dk)
 * Further enhancement before move to Codehaus sponsored and donated by Lakeside A/S (http://www.lakeside.dk)
 *
 * Copyright (c) to all contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * $HeadURL: https://svn.codehaus.org/mojo/trunk/sandbox/chronos-maven-plugin/chronos/src/main/java/org/codehaus/mojo/chronos/jmeter/JMeterMojo.java $
 * $Id: JMeterMojo.java 14221 2011-06-24 10:16:28Z soelvpil $
 */
package org.codehaus.mojo.chronos.data;

import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.mojo.chronos.Transactional;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;

import java.io.File;
import java.util.Properties;

/**
 * Utility class for accessing Hibernate.
 * 
 * @author ads@lakeside.dk
 */
public class HibernateUtil {

	private static SessionFactory sessionFactory;
	private static final String DB_LOC = "chronostimerdb";


    public static void run(Transactional transactional)
        throws MojoExecutionException
    {
        Session session = HibernateUtil.getSession();
        try {
            Transaction transaction = session.beginTransaction();
            try {
                transactional.run( session );
                transaction.commit();
            } catch (RuntimeException e) {
                transaction.rollback();
                throw new MojoExecutionException( "Error while accessing database", e );
            }
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
    }

	/**
	 * Retrieve the Hibernate session of the thread.
	 * 
	 * @return The current session.
	 * @throws HibernateException
	 *             Thrown if the operation fails.
	 */
	public static Session getSession() throws HibernateException {
		if (sessionFactory == null) {
			initialize();
		}
		return sessionFactory.getCurrentSession();
	}

	/**
	 * Initializes Hibernate
	 */
	private static void initialize() {
		try {
			org.hibernate.cfg.Configuration ac = new org.hibernate.cfg.Configuration();
			ac.addAnnotatedClass(TestCase.class);

			ac.addAnnotatedClass(TestCaseResult.class);

			Properties properties = new Properties();
			properties.setProperty("hibernate.dialect", "org.hibernate.dialect.DerbyTenSevenDialect");

			properties.setProperty("hibernate.connection.driver_class", "org.apache.derby.jdbc.EmbeddedDriver");
			properties.setProperty("hibernate.connection.url", "jdbc:derby:" + DB_LOC + ";create=true");

			properties.setProperty("hibernate.connection.username", "chronos");
			properties.setProperty("hibernate.connection.password", "chronos");

			properties.setProperty("hibernate.current_session_context_class", "thread");

			properties.setProperty("hibernate.show_sql", "false");

			properties.setProperty("hibernate.search.default.optimizer.operation_limit.max", "1000");
			properties.setProperty("hibernate.search.default.optimizer.transaction_limit.max", "100");

			ac.setProperties(properties);

			if (!new File(".", DB_LOC).exists()) {
				properties.setProperty("hibernate.hbm2ddl.auto", "create");
			}
			ac.setProperties(properties);

			ServiceRegistry serviceRegistry = new ServiceRegistryBuilder().applySettings(properties).buildServiceRegistry();
			sessionFactory = ac.buildSessionFactory(serviceRegistry);
		} catch (Throwable ex) {
			throw new ExceptionInInitializerError(ex);
		}
	}

	private HibernateUtil() {
		// Do nothing
	}
}
