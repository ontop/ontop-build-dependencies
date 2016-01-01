package org.protege.osgi.jdbc;

import java.net.URL;
import java.sql.Driver;
import java.util.Collection;

public interface JdbcRegistry {
	void addJdbcDriver(String className, URL location) throws RegistryException;
	
	void addJdbcDriver(Driver driver);
	
	void removeJdbcDriver(String className);

    Collection<Driver> getJdbcDrivers();
}
