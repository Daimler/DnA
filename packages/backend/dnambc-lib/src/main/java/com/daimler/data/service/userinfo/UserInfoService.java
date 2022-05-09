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

package com.daimler.data.service.userinfo;

import java.util.List;

import com.daimler.data.db.entities.UserInfoNsql;
import com.daimler.data.dto.solution.ChangeLogVO;
import com.daimler.data.dto.solution.SolutionVO;
import com.daimler.data.dto.userinfo.UserInfoVO;
import com.daimler.data.service.common.CommonService;

public interface UserInfoService extends CommonService<UserInfoVO, UserInfoNsql, String> {

	/* boolean updateUserToken(String id, String token); */

	boolean updateNewUserToken(String id, boolean isLogin);

	boolean validateUserToken(final String id, String token);

	void addUser(UserInfoNsql userinfo);

	UserInfoVO updateBookMarkedSolutions(final String id, List<String> bookmarks, boolean deleteBookmarks);

	List<SolutionVO> getAllBookMarkedSolutionsForUser(final String userId);

	/**
	 * To check whether user is admin or not
	 * 
	 * @param userId
	 * @return isAdmin
	 */
	public Boolean isAdmin(String userId);

	boolean isLoggedIn(final String id);

	void notifyAllAdminUsers(String eventType, String resourceId, String message, String triggeringUser,
			List<ChangeLogVO> changeLogs);
}
