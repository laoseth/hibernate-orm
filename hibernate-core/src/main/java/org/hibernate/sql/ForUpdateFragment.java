/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.sql;

import java.util.Map;

import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.QueryException;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.RowLockStrategy;
import org.hibernate.internal.util.StringHelper;

/**
 * @author Gavin King
 */
public class ForUpdateFragment {
	private final StringBuilder aliases = new StringBuilder();
	private final Dialect dialect;
	private final LockOptions lockOptions;

	public ForUpdateFragment(Dialect dialect, LockOptions lockOptions, Map<String, String[]> keyColumnNames) throws QueryException {
		this.dialect = dialect;
		LockMode upgradeType = null;
		this.lockOptions =  lockOptions;

		if ( !lockOptions.getAliasSpecificLocks().iterator().hasNext() ) {  // no tables referenced
			final LockMode lockMode = lockOptions.getLockMode();
			if ( LockMode.READ.lessThan(lockMode) ) {
				upgradeType = lockMode;
			}
		}
		else {
			for ( Map.Entry<String, LockMode> me : lockOptions.getAliasSpecificLocks() ) {
				final LockMode lockMode = me.getValue();
				if ( LockMode.READ.lessThan(lockMode) ) {
					final String tableAlias = me.getKey();
					if ( dialect.getWriteRowLockStrategy() == RowLockStrategy.COLUMN ) {
						String[] keyColumns = keyColumnNames.get( tableAlias ); //use the id column alias
						if ( keyColumns == null ) {
							throw new IllegalArgumentException( "alias not found: " + tableAlias );
						}
						keyColumns = StringHelper.qualify( tableAlias, keyColumns );
						for ( String keyColumn : keyColumns ) {
							addTableAlias( keyColumn );
						}
					}
					else {
						addTableAlias( tableAlias );
					}
					if ( upgradeType != null && lockMode != upgradeType ) {
						throw new QueryException( "mixed LockModes" );
					}
					upgradeType = lockMode;
				}
			}
		}
	}

	public ForUpdateFragment addTableAlias(String alias) {
		if ( aliases.length() > 0 ) {
			aliases.append( ", " );
		}
		aliases.append( alias );
		return this;
	}

	public String toFragmentString() {
		if ( aliases.length() == 0) {
			return dialect.getForUpdateString( lockOptions );
		}
		else {
			return dialect.getForUpdateString( aliases.toString(), lockOptions );
		}
	}


}
