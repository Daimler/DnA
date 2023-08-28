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

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.daimler.data.assembler.UserInfoAssembler;
import com.daimler.data.db.entities.UserInfoNsql;
import com.daimler.data.db.repo.userinfo.UserInfoCustomRepository;
import com.daimler.data.db.repo.userinfo.UserInfoRepository;
import com.daimler.data.dto.userinfo.UserInfoVO;
import com.daimler.data.service.common.BaseCommonService;

import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class BaseUserInfoService extends BaseCommonService<UserInfoVO, UserInfoNsql, String>
		implements UserInfoService {

	private static Logger logger = LoggerFactory.getLogger(BaseUserInfoService.class);
	
	@Autowired
	private UserInfoCustomRepository customRepo;
	@Autowired
	private UserInfoRepository jpaRepo;

	@Autowired
	private UserInfoAssembler userinfoAssembler;

	public BaseUserInfoService() {
		super();
	}

	@Override
	public UserInfoVO getById(String id) {
		Optional<UserInfoNsql> userInfo = customRepo.findById(id);
		return userInfo.isPresent() ? userinfoAssembler.toVo(userInfo.get()) : null;
	}

	@Override
	public void addUser(UserInfoNsql userinfo) {
		jpaRepo.save(userinfo);
		logger.info("Added user {} successfully",userinfo.getId());
	}

}
