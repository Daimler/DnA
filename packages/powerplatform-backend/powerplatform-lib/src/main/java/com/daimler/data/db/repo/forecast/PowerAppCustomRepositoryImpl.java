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

package com.daimler.data.db.repo.forecast;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import com.daimler.data.db.entities.PowerAppNsql;
import com.daimler.data.db.json.PowerAppDetails;
import com.daimler.data.db.repo.common.CommonDataRepositoryImpl;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class PowerAppCustomRepositoryImpl extends CommonDataRepositoryImpl<PowerAppNsql, String>
		implements PowerAppCustomRepository {

	@Override
	public long getTotalCount(String userId) {
		String user = userId.toLowerCase();
		String getCountStmt = " select count(*) from powerapp_nsql where  (lower(jsonb_extract_path_text(data,'requestedBy','id')) = '" + user +"') ";
		Query q = em.createNativeQuery(getCountStmt);
		BigInteger results = (BigInteger) q.getSingleResult();
		return results.longValue();
	}
	
	@Override
	public List<PowerAppNsql> getAll(String userId, int offset, int limit){
		String user = userId.toLowerCase();
		String getAllStmt = " select cast(id as text), cast(data as text) from powerapp_nsql where  (lower(jsonb_extract_path_text(data,'requestedBy','id')) = '" + user +"')";
		if (limit > 0)
			getAllStmt = getAllStmt + " limit " + limit;
		if (offset >= 0)
			getAllStmt = getAllStmt + " offset " + offset;
		Query q = em.createNativeQuery(getAllStmt);
		ObjectMapper mapper = new ObjectMapper();
		List<Object[]> results = q.getResultList();
		List<PowerAppNsql> convertedResults = results.stream().map(temp -> {
			PowerAppNsql entity = new PowerAppNsql();
			try {
				String jsonData = temp[1] != null ? temp[1].toString() : "";
				PowerAppDetails tempData = mapper.readValue(jsonData, PowerAppDetails.class);
				entity.setData(tempData);
			} catch (Exception e) {
				log.error("Failed while fetching all projects using native query with exception {} ", e.getMessage());
			}
			String id = temp[0] != null ? temp[0].toString() : "";
			entity.setId(id);
			return entity;
		}).collect(Collectors.toList());
		return convertedResults;
	}

}
