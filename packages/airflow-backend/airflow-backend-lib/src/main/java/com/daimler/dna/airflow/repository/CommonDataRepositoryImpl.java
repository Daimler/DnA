/* LICENSE START
 * 
 * MIT License
 * 
 * Copyright (c) 2019 Daimler TSS GmbH
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
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
 * LICENSE END 
 */

package com.daimler.dna.airflow.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Objects;

public class CommonDataRepositoryImpl<T, ID> implements CommonDataRepository<T, ID> {

	public enum SORT_TYPE {
		ASC, DESC;
	}

	@PersistenceContext
	protected EntityManager em;

	protected Class<T> entityClass;

	private static final Logger LOG = LoggerFactory.getLogger(CommonDataRepositoryImpl.class);

	@SuppressWarnings("unchecked")
	public CommonDataRepositoryImpl() {
		this.entityClass = ((Class<T>) ((ParameterizedType) getClass().getGenericSuperclass())
				.getActualTypeArguments()[0]);
	}

	@Override
	public void deleteAll() {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaDelete<T> bulkDelete = cb.createCriteriaDelete(entityClass);
		Root<T> root = bulkDelete.from(entityClass);
		int rowsDeleted = em.createQuery(bulkDelete).executeUpdate();
		LOG.info("Number of deleted rows " + rowsDeleted);
	}

	@Override
	public void insertAll(List<T> tNsqlList) {
		if (Objects.nonNull(tNsqlList) && !tNsqlList.isEmpty()) {
			tNsqlList.forEach(t -> em.persist(t));
		}
	}

	@Override
	public T save(T entity) {
		return em.merge(entity);
	}

	@Override
	public List<T> findbyUniqueLiteral(String uniqueliteralName, String value) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<T> cq = cb.createQuery(entityClass);
		Root<T> root = cq.from(entityClass);
		CriteriaQuery<T> byName = cq.select(root);
		Predicate con1 = cb.equal(root.get(uniqueliteralName), value);
		cq.where(con1);
		TypedQuery<T> byNameQuery = em.createQuery(byName);
		List<T> entities = byNameQuery.getResultList();
		if (entities != null && entities.size() > 0)
			return entities;
		else
			return null;
	}

	@Override
	public List<T> findbyUniqueLiteral(String uniqueliteralName, Integer value) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<T> cq = cb.createQuery(entityClass);
		Root<T> root = cq.from(entityClass);
		CriteriaQuery<T> byName = cq.select(root);
		Predicate con1 = cb.equal(root.get(uniqueliteralName), value);
		cq.where(con1);
		TypedQuery<T> byNameQuery = em.createQuery(byName);
		List<T> entities = byNameQuery.getResultList();
		if (entities != null && entities.size() > 0)
			return entities;
		else
			return null;
	}

	@Override
	public List<T> findAll(int limit, int offset) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<T> cq = cb.createQuery(entityClass);
		Root<T> root = cq.from(entityClass);
		CriteriaQuery<T> getAll = cq.select(root);
		TypedQuery<T> getAllQuery = em.createQuery(getAll);
		if (offset >= 0)
			getAllQuery.setFirstResult(offset);
		if (limit > 0)
			getAllQuery.setMaxResults(limit);
		return getAllQuery.getResultList();
	}

	@Override
	public Long getCount(int limit, int offset) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Long> cq = cb.createQuery(Long.class);
		Root<T> root = cq.from(entityClass);
		CriteriaQuery<Long> getAll = cq.select(cb.count(root));
		TypedQuery<Long> getAllQuery = em.createQuery(getAll);
		return getAllQuery.getSingleResult();
	}

	@Override
	public void delete(T entity) {
		em.remove(entity);
	}
}
