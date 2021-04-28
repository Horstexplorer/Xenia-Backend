/*
 *     Copyright 2020 Horstexplorer @ https://www.netbeacon.de
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.netbeacon.utils.sql.connectionpool;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.netbeacon.utils.shutdownhook.IShutdown;
import de.netbeacon.utils.sql.auth.SQLAuth;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Creates an SQL connection pool
 *
 * @author horstexplorer
 */
public class SQLConnectionPool implements IShutdown{

	private final HikariDataSource hikariDataSource;

	/**
	 * Creates a new instance of this class
	 *
	 * @param settings of the connection pools
	 * @param auth     which should be used
	 */
	public SQLConnectionPool(SQLConnectionPoolSettings settings, SQLAuth auth){
		HikariConfig config = new HikariConfig();
		// auth n login
		config.setDriverClassName("org.postgresql.Driver");
		config.setJdbcUrl(auth.getUrl());
		config.setUsername(auth.getUsername());
		config.setPassword(auth.getPassword());
		// connection settings
		config.setMinimumIdle(settings.getMinCon());
		config.setMaximumPoolSize(settings.getMaxCon());
		config.setConnectionTimeout(settings.getConTimeout());
		config.setIdleTimeout(settings.getIdleTimeout());
		config.setMaxLifetime(settings.getMaxLifetime());
		// build
		this.hikariDataSource = new HikariDataSource(config);
	}

	/**
	 * Returns a connection from the connection pool
	 *
	 * @return Connection
	 *
	 * @throws SQLException on exception
	 */
	public Connection getConnection() throws SQLException{
		return hikariDataSource.getConnection();
	}

	/**
	 * Returns the DSLContext used for JOOP set for an postgres db
	 *
	 * @return DSLContext
	 *
	 * @throws SQLException on exception
	 */
	public DSLContext getContext() throws SQLException{
		return DSL.using(hikariDataSource.getConnection(), SQLDialect.POSTGRES);
	}

	public DSLContext getContext(Connection connection){
		return DSL.using(connection, SQLDialect.POSTGRES);
	}

	/**
	 * Returns the DSLContext used for JOOP with a given dialect
	 *
	 * @param dialect SQLDialect
	 *
	 * @return DSLContext
	 *
	 * @throws SQLException on exception
	 */
	public DSLContext getContext(SQLDialect dialect) throws SQLException{
		return DSL.using(hikariDataSource.getConnection(), dialect);
	}

	/**
	 * Used to close the connection pool
	 */
	public void close(){
		hikariDataSource.close();
	}

	@Override
	public void onShutdown() throws Exception{
		close();
	}

}
