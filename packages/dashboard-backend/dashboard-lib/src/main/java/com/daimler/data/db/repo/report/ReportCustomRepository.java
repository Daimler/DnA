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

package com.daimler.data.db.repo.report;

import java.util.List;

import com.daimler.data.db.entities.ReportNsql;
import com.daimler.data.db.repo.common.CommonDataRepository;
import com.daimler.data.dto.report.TeamMemberVO;

public interface ReportCustomRepository extends CommonDataRepository<ReportNsql, String> {

	List<ReportNsql> getAllWithFiltersUsingNativeQuery(Boolean published, List<String> statuses, String userId,
			Boolean isAdmin, List<String> searchTerms, List<String> tags, int offset, int limit, String sortBy,
			String sortOrder, String division, List<String> department, List<String> processOwner,
			List<String> productOwner, List<String> art);

	Long getCountUsingNativeQuery(Boolean published, List<String> statuses, String userId, Boolean isAdmin,
			List<String> searchTerms, List<String> tags, String division, List<String> department,
			List<String> processOwner, List<String> productOwner, List<String> art);

	List<TeamMemberVO> getAllProductOwnerUsingNativeQuery();

	List<TeamMemberVO> getAllProcessOwnerUsingNativeQuery();

}
