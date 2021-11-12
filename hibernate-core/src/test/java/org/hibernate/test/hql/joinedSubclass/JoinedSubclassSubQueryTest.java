/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.test.hql.joinedSubclass;

import java.util.List;

import org.hibernate.testing.TestForIssue;
import org.hibernate.testing.orm.junit.DomainModel;
import org.hibernate.testing.orm.junit.SessionFactory;
import org.hibernate.testing.orm.junit.SessionFactoryScope;
import org.junit.jupiter.api.Test;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * @author Stephen Fikes
 * @author Gail Badner
 */
@DomainModel(
		annotatedClasses = {
				JoinedSubclassSubQueryTest.InvestmentCompany.class,
				JoinedSubclassSubQueryTest.Person.class,
				JoinedSubclassSubQueryTest.Employee.class
		}
)
@SessionFactory
public class JoinedSubclassSubQueryTest {

	@Test
	@TestForIssue(jiraKey = "HHH-11182")
	public void testSubQueryConstraintPropertyInSuperclassTable(SessionFactoryScope scope) {

		scope.inTransaction(
				session -> {
					// employee.firstName is in Person table (not Employee)
					String queryHQL = "from InvestmentCompany investmentCompany "
							+ "where exists "
							+ "(select employee "
							+ "from investmentCompany.employees as employee "
							+ "  where employee.firstName = 'Joe')";
					session.createQuery( queryHQL ).uniqueResult();
				}
		);
	}

	@Test
	@TestForIssue(jiraKey = "HHH-11182")
	public void testSubQueryConstraintPropertyInEntityTable(SessionFactoryScope scope) {
		scope.inTransaction(
				session -> {
					// employee.employeeNumber is in Employee table
					String queryHQL = "from InvestmentCompany investmentCompany "
							+ "where exists "
							+ "(select employee "
							+ "from investmentCompany.employees as employee "
							+ "  where employee.employeeNumber = 666 )";
					session.createQuery( queryHQL ).uniqueResult();
				}
		);
	}

	@Entity(name = "InvestmentCompany")
	@Table
	public static class InvestmentCompany {
		@Id
		@GeneratedValue
		@Column
		private Long id;

		@Basic(optional = false)
		@Column(nullable = false, length = 255)
		private String fullName;

		@Basic(optional = false)
		@Column(nullable = false, length = 16)
		private String shortName;

		@OneToMany(mappedBy = "company")
		List<Employee> employees;
	}

	@Entity(name = "Person")
	@Inheritance(strategy = InheritanceType.JOINED)
	public static abstract class Person {
		@Id
		@GeneratedValue
		@Column
		private Long id;

		@Basic(optional = false)
		@Column(nullable = false)
		private String firstName;
	}

	@Entity(name = "Employee")
	public static class Employee extends Person {
		@Id
		@GeneratedValue
		@Column
		private Long id;

		private int employeeNumber;

		@ManyToOne(optional = false)
		@JoinColumn(nullable = false)
		private InvestmentCompany company;
	}
}
