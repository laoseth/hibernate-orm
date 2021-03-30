/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.test.query.hql;

import java.util.HashMap;
import java.util.Map;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.Query;

import org.hibernate.testing.TestForIssue;
import org.hibernate.testing.orm.junit.EntityManagerFactoryScope;
import org.hibernate.testing.orm.junit.Jpa;

import org.junit.jupiter.api.Test;

import static javax.persistence.CascadeType.ALL;

/**
 * @author Moritz Becker
 */
@TestForIssue(jiraKey = "HHH-13201")
@Jpa(
		annotatedClasses = {
				FetchNonRootRelativeElementCollectionAndAssociationTest.ProductNaturalId.class,
				FetchNonRootRelativeElementCollectionAndAssociationTest.Product.class,
				FetchNonRootRelativeElementCollectionAndAssociationTest.ProductDetail.class
		}
)
public class FetchNonRootRelativeElementCollectionAndAssociationTest {
	@Test
	public void testJoinedSubclassUpdateWithCorrelation(EntityManagerFactoryScope scope) {
		scope.inTransaction(
				(entityManager) -> {
					// DO NOT CHANGE this query: it used to trigger an error caused
					// by the origin FromElement for the association fetch being resolved to the wrong FromElement due to the
					// presence of an element collection join.
					String u = "select prod from ProductNaturalId nat inner join nat.product prod " +
							"left join fetch prod.productDetail " +
							"left join fetch prod.normalizedPricesByUnit";
					Query query = entityManager.createQuery( u, Product.class );
					query.getResultList();
				}
		);
	}

	@Entity(name = "ProductNaturalId")
	public class ProductNaturalId {
		@Id
		private String naturalId;
		@OneToOne(optional = false)
		private Product product;
	}

	@Entity(name = "Product")
	public class Product {
		@Id
		private Long id;
		@OneToOne(mappedBy = "product", cascade = ALL, fetch = FetchType.LAZY)
		private ProductDetail productDetail;
		@OneToOne(mappedBy = "product", cascade = ALL, fetch = FetchType.LAZY)
		private ProductNaturalId naturalId;
		@ElementCollection(fetch = FetchType.LAZY)
		private Map<String, String> normalizedPricesByUnit = new HashMap<>();
	}

	@Entity(name = "ProductDetail")
	public class ProductDetail {
		@Id
		private Long id;
		@OneToOne(optional = false)
		@JoinColumn(name = "id")
		@MapsId
		private Product product;
	}
}
